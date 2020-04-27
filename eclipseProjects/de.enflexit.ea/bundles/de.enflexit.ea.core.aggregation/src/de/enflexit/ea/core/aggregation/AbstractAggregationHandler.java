package de.enflexit.ea.core.aggregation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkModel;

import agentgui.core.application.Application;
import agentgui.core.environment.EnvironmentController;
import agentgui.core.project.Project;
import agentgui.simulationService.SimulationService;
import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.time.TimeModelDateBased;
import agentgui.simulationService.transaction.DisplayAgentNotification;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.common.SystemEnvironmentHelper;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import energy.OptionModelController;
import energy.calculations.AbstractOptionModelCalculation;
import energy.evaluation.TechnicalSystemStateHelper;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.TechnicalSystemStateTime;
import energy.schedule.ScheduleController;
import energy.schedule.ScheduleNotification;
import energygroup.GroupController;
import energygroup.GroupTreeNodeObject;
import energygroup.calculation.GroupCalculation;
import energygroup.evaluation.MemberEvaluationStrategy;
import energygroup.evaluation.MemberEvaluationStrategyScheduList;
import jade.core.AID;
import jade.core.Agent;

/**
 * The Class ConstructionSiteAggregationHandler estimates, calculates, assesses and finally displays 
 * the current state of the simulation. Once the 'Agent answers' were transferred to 
 * the SimulationManager, this class updates the detail schedules and executes a 
 * network estimation and calculation.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractAggregationHandler {

	private String ownerName;
	private Object ownerInstance;
	
	private NetworkModel networkModel;
	private String timeFormat;
	private boolean headlessOperation;
	private ExecutionDataBase executionDataBase;
	
	private int scheduleControllerFailuresMax = 5;
	private HashMap<String, Integer> scheduleControllerFailureHashMap;
	
	private ArrayList<AbstractSubNetworkConfiguration> subNetworkConfigurations;
	
	private Vector<AbstractSubAggregationBuilder> executedBuilds;
	
	private HashMap<String, ScheduleController> networkComponentsScheduleController;
	private HashMap<String, TechnicalSystemStateEvaluation> lastNetworkComponentUpdates;

	private HashMap<AbstractSubNetworkConfiguration, NetworkAggregationTaskThread> networkAggregationTaskThreadHashMap;
	private Object networkAggregationTaskTrigger;
	private List<NetworkAggregationTaskThread> networkAggregationTaskDoneList;
	
	private Vector<AggregationListener> aggregationListenerListeners;
	
	// --- From here, some control variables for debugging purposes can be found -------- 
	private boolean debugIsSkipActualNetworkCalculation;
	
	
	/**
	 * Instantiates a new aggregation handler.
	 *
	 * @param networkModel the network model to use for the aggregation
	 * @param isheadlessOperation the indicator if is headless operation
	 * @param ownerName the aggregators owner name
	 */
	public AbstractAggregationHandler(NetworkModel networkModel, boolean isheadlessOperation, String ownerName) {
		this.networkModel = networkModel;
		this.headlessOperation = isheadlessOperation;
		this.ownerName = ownerName;
		this.initialize();
	}


	/**
	 * For debugging purposes: Can be used to disable the actual network calculation during the execution of the aggregation handler.
	 * @param isSkipNetworkCalculation the boolean value to skip the actual network calculation
	 */
	public void debugIsSkipActualNetworkCalculation(boolean isSkipNetworkCalculation) {
		this.debugIsSkipActualNetworkCalculation = isSkipNetworkCalculation;
	}
	/**
	 * Checks if the actual network calculation should be skipped.
	 * @return true, if the actual network calculation should be skipped
	 */
	public boolean debugIsSkipActualNetworkCalculation() {
		return debugIsSkipActualNetworkCalculation;
	}
	
	
	/**
	 * May return an individual configuration that describes how an overall {@link NetworkModel} is to
	 * split into sub networks and which handling and calculation classes are used here.<br>
	 * If this method returns <code>null</code>, the {@link DefaultSubNetworkConfigurations} will used. 
	 * 
	 * @see AbstractSubNetworkConfiguration
	 *
	 * @return the subnetwork configuration
	 */
	protected abstract ArrayList<AbstractSubNetworkConfiguration> getConfigurationOfSubNetworks();
	
	/**
	 * Internally used to define  the sub network configurations.
	 * @return the sub network configurations
	 */
	public final ArrayList<AbstractSubNetworkConfiguration> getSubNetworkConfigurations() {
		if (subNetworkConfigurations==null) {
			subNetworkConfigurations = this.getConfigurationOfSubNetworks();
			if (subNetworkConfigurations==null) {
				// --- Get a default configuration ------------------
				subNetworkConfigurations = new DefaultSubNetworkConfigurations(this);
			}
			// --- Set aggregation handler to configurations --------
			for (int i = 0; i < subNetworkConfigurations.size(); i++) {
				subNetworkConfigurations.get(i).setAggregationHandler(this);
			}
		}
		return subNetworkConfigurations;
	}
	/**
	 * Returns the list of sub network configurations for the specified subnetwork description (e.g. 'HeatNetwork' and so on).
	 *
	 * @param subnetworkDescription the subnetwork description
	 * @return the list of sub network configurations that were found with the description
	 */
	public List<AbstractSubNetworkConfiguration> getSubNetworkConfiguration(String subnetworkDescription) {
		List<AbstractSubNetworkConfiguration> configurationsFound = new ArrayList<>();
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			AbstractSubNetworkConfiguration checkConfig = this.getSubNetworkConfigurations().get(i); 
			if (checkConfig.getSubNetworkDescription().equals(subnetworkDescription)) {
				configurationsFound.add(checkConfig);
			}
		}
		return configurationsFound;
	}
	
	/**
	 * Initializes the aggregation handler.
	 */
	private void initialize() {
		// --- Create an aggregation for each configuration ---------
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			AbstractSubNetworkConfiguration subConfig = this.getSubNetworkConfigurations().get(i);
			AbstractSubAggregationBuilder subEomBuilder = subConfig.getSubAggregationBuilder();
			this.getExecutedBuilds().add(subEomBuilder);
			subEomBuilder.createEomAggregationInThread();
		}
		
		// --- Wait for the end of the build processes --------------
		while (this.getExecutedBuilds().size()>0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException iEx) {
				//iEx.printStackTrace();
			}
		}
		// --- Reset executed builds Vector -------------------------
		this.executedBuilds = null;
	}
	/**
	 * Returns the list running build processes.
	 * @return the running builder
	 */
	protected synchronized Vector<AbstractSubAggregationBuilder> getExecutedBuilds() {
		if (executedBuilds==null) {
			executedBuilds = new Vector<>();
		}
		return executedBuilds;
	}
	
	
	/**
	 * Terminates the current aggregation handler. 
	 */
	public void terminate() {
		
		// --- Terminate NetworkCalculationThreads ----------------------------
		try {
			List<NetworkAggregationTaskThread> netCalcThreadList = new ArrayList<>(this.getNetworkAggregationTaskThreadHashMap().values());
			for (int i = 0; i < netCalcThreadList.size(); i++) {
				netCalcThreadList.get(i).setDoTerminate(true);
			}
			synchronized (this.getNetworkAggregationTaskTrigger()) {
				this.getNetworkAggregationTaskTrigger().notifyAll();
			}
			
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		
		// --- Terminate sub aggregation handler for each configuration -------
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			AbstractSubNetworkConfiguration subConfig = this.getSubNetworkConfigurations().get(i);
			AbstractSubAggregationBuilder subEomBuilder = subConfig.getSubAggregationBuilder();
			subEomBuilder.terminateEomAggregation();
		}
		
	}
	
	/**
	 * Returns the HashMap of the NetworkComponents {@link ScheduleController}.
	 * @return the agents schedule controller
	 */
	public HashMap<String, ScheduleController> getNetworkComponentsScheduleController() {
		if (networkComponentsScheduleController==null) {
			networkComponentsScheduleController = new HashMap<String, ScheduleController>();
		}
		return networkComponentsScheduleController;
	}
	
	// ----------------------------------------------------------------------------------
	// --- Access methods for the overall environment model can be found from here ------ 
	// ----------------------------------------------------------------------------------
	/**
	 * Returns the current project.
	 * @return the project
	 */
	public Project getProject() {
		return Application.getProjectFocused();
	}
	/**
	 * Returns the environment model.
	 * @return the environment model
	 */
	private EnvironmentModel getEnvironmentModel() {
		if (this.getProject()!=null) {
			EnvironmentController envController = this.getProject().getEnvironmentController();
			if (envController!=null) {
				return envController.getEnvironmentModel();
			}
		}
		return null;
	}
	/**
	 * Returns the currently configured date based time model.
	 * @return the time model
	 */
	public TimeModelDateBased getTimeModel() {
		EnvironmentModel environmentModel = this.getEnvironmentModel();
		if (environmentModel!=null && environmentModel.getTimeModel()!=null && environmentModel.getTimeModel() instanceof TimeModelDateBased) {
			return (TimeModelDateBased) environmentModel.getTimeModel();
		}
		return null;
	}
	/**
	 * Return the time format that can be used by a {@link SimpleDateFormat} for example.
	 * @return the time format
	 */
	public String getTimeFormat() {
		if (timeFormat==null) {
			TimeModelDateBased tmDateBased = this.getTimeModel();
			if (tmDateBased==null || tmDateBased.getTimeFormat()==null || tmDateBased.getTimeFormat().isEmpty()==true) {
				// --- Use the internal default -------
				timeFormat = "dd.MM.yy HH:mm:ss";
			} else {
				timeFormat = tmDateBased.getTimeFormat();
			}
		}
		return timeFormat;
	}
	/**
	 * Sets the time format.
	 * @param timeFormat the time format to be used for visualizations. If no date based time model is defined and 
	 * the time format is <code>null</code>, the default of 'dd.MM.yy HH:mm:ss' will be used.
	 */
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
	
	/**
	 * Returns the current overall NetworkModel.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return this.networkModel;
	}
	/**
	 * Returns the HyGridAbstractEnvironmentModel from the current EnvironmentModel.
	 * @return the HyGridAbstractEnvironmentModel
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		HyGridAbstractEnvironmentModel hyGridModel = null;
		EnvironmentModel envModel = this.getEnvironmentModel();
		if (envModel!=null && envModel.getAbstractEnvironment()!=null && envModel.getAbstractEnvironment() instanceof HyGridAbstractEnvironmentModel) {
			hyGridModel = (HyGridAbstractEnvironmentModel) envModel.getAbstractEnvironment();
		}
		return hyGridModel;
	}
	
	/**
	 * Returns the owner name of the aggregation.
	 * @return the owner name
	 */
	public String getOwnerName() {
		if (ownerName==null || ownerName.isEmpty()) {
			ownerName = this.getClass().getSimpleName();
		}
		return ownerName;
	}
	/**
	 * Returns the owning instance of this aggregation handler (if this was explicitly set).
	 * @return the owner instance
	 * @see #setOwnerInstance(Object)
	 */
	public Object getOwnerInstance() {
		return ownerInstance;
	}
	/**
	 * Sets the owner instance.
	 * @param ownerInstance the new owner instance
	 */
	public void setOwnerInstance(Object ownerInstance) {
		this.ownerInstance = ownerInstance;
	}
	/**
	 * Returns the current owner agent if the owning instance was set and is an agent.
	 * @return the owner agent or null, if the owner instance is not an agent
	 * @see #setOwnerInstance(Object)
	 */
	public Agent getOwnerAgent() {
		if (this.getOwnerInstance()!=null && this.getOwnerInstance() instanceof Agent) {
			return (Agent) this.getOwnerInstance();
		}
		return null;
	}
	
	/**
	 * Checks if is headless operation.
	 * @return true, if is headless operation
	 */
	public boolean isHeadlessOperation() {
		if (SystemEnvironmentHelper.isHeadlessOperation()==true) {
			return true;
		}
		return headlessOperation;
	}
	
	/**
	 * Sets the execution data base to be used for the aggregator.
	 * @param executionDataBase the new execution data base
	 */
	public void setExecutionDataBase(ExecutionDataBase executionDataBase) {
		this.executionDataBase = executionDataBase;
	}
	/**
	 * Return the execution data base for the aggregation that is either based 
	 * on complete node information or on sensor data.
	 *
	 * @return the execution data base
	 */
	public ExecutionDataBase getExecutionDataBase() {
		if (executionDataBase==null) {
			executionDataBase = this.getHyGridAbstractEnvironmentModel().getExecutionDataBase();
		}
		return executionDataBase;
	}
	
	// ----------------------------------------------------------------------------------
	// --- From here on, access to the individual Schedules will be organized -----------  
	// ----------------------------------------------------------------------------------	
	/**
	 * Sets a single agent answer to the corresponding aggregation element that may come from
	 * the {@link SimulationService}.
	 * @param notification the new agent answers
	 */
	public void setAgentAnswer(EnvironmentNotification notification) {
		AID agentAID = notification.getSender();
		Object updateObject = notification.getNotification(); 
		if (agentAID!=null && updateObject!=null) {
			this.appendToNetworkComponentsScheduleController(agentAID.getLocalName(), updateObject);
		}
	}
	/**
	 * Sets the agent answers that may come from the {@link SimulationService} during a discrete simulation.
	 * @param agentAnswers the agent answers
	 */
	public void setAgentAnswers(Hashtable<AID, Object> agentAnswers) {
		
		if (agentAnswers!=null && agentAnswers.size()>0) {
			ArrayList<AID> aidKeys = new ArrayList<AID>(agentAnswers.keySet());
			for (int i = 0; i < aidKeys.size(); i++) {
				AID agentAID = aidKeys.get(i);
				Object updateObject = agentAnswers.get(agentAID);
				if (agentAID!=null && updateObject!=null) {
					this.appendToNetworkComponentsScheduleController(agentAID.getLocalName(), updateObject);
				}
			}
		}
	}
	
	/**
	 * Appends the specified object (a {@link TechnicalSystemStateEvaluation}?) to network components schedule controller.
	 *
	 * @param networkComponentID the network component ID
	 * @param updateObject the update object that normally should be of Type {@link TechnicalSystemStateEvaluation}
	 */
	public void appendToNetworkComponentsScheduleController(String networkComponentID, Object updateObject) {
	
		if (networkComponentID==null || updateObject==null) return;
		
		try {

			if (updateObject instanceof String) {
				// --- Most probably a 'null' feedback ------------------------
				String newStateString = (String) updateObject;
				if (newStateString.equals("null")==false) {
					System.out.println("Answer from " + networkComponentID + ": " + newStateString);
				}
				
			} else if (updateObject instanceof TechnicalSystemStateEvaluation) {
				// --- Got a new system state from a part of the network ------
 				this.appendToNetworkComponentsScheduleController(networkComponentID, (TechnicalSystemStateEvaluation)updateObject);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Appends the specified {@link TechnicalSystemStateEvaluation} to network components schedule controller.
	 *
	 * @param networkComponentID the network component ID
	 * @param tsseNew the new {@link TechnicalSystemStateEvaluation} to append
	 */
	public void appendToNetworkComponentsScheduleController(String networkComponentID, TechnicalSystemStateEvaluation tsseNew) {
		this.appendToNetworkComponentsScheduleController(networkComponentID, tsseNew, true);
	}
	/**
	 * Appends the specified {@link TechnicalSystemStateEvaluation} to network components schedule controller.
	 *
	 * @param networkComponentID the network component ID
	 * @param tsseNew the new {@link TechnicalSystemStateEvaluation} to append
	 * @param isRealTimeSchedule the is real time schedule
	 */
	public void appendToNetworkComponentsScheduleController(String networkComponentID, TechnicalSystemStateEvaluation tsseNew, boolean isRealTimeSchedule) {
	
		if (networkComponentID==null || tsseNew==null) return;
		
		// --- Try to find the corresponding ScheduelController -----
		ScheduleController sc = this.getNetworkComponentsScheduleController().get(networkComponentID);
		if (sc==null) {
			
			// --- Get number of failures ---------------------------
			int failures = this.getScheduleControllerFailureCounter(networkComponentID);
			// --- Increase counting --------------------------------
			this.getScheduleControllerFailureHashMap().put(networkComponentID, failures + 1);
			// --- Print info to console ----------------------------
			if (failures < this.scheduleControllerFailuresMax) {
				System.out.println("[" + this.getOwnerName() + "] Got state update from NetworkComponent '" + networkComponentID + "', but no ScheduleController could be found.");
			} else if (failures==this.scheduleControllerFailuresMax) {
				System.out.println("[" + this.getOwnerName() + "] NetworkComponent '" + networkComponentID + "': Informed " + failures + " times about missing ScheduleController! - Stop now repeating that!");
			}
			return;
		}
		this.appendToNetworkComponentsScheduleController(sc, tsseNew, isRealTimeSchedule);
	}
	
	/**
	 * Return the ScheduleController failure counter for the specified component.
	 *
	 * @param networkComponentID the network component ID
	 * @return the schedule controller failure counter
	 */
	private int getScheduleControllerFailureCounter(String networkComponentID) {
		
		Integer counter = this.getScheduleControllerFailureHashMap().get(networkComponentID);
		if (counter==null) {
			this.getScheduleControllerFailureHashMap().put(networkComponentID, 0);
			counter = 0;
		}
		return counter;
	}
	/**
	 * Gets the schedule controller failure hash map.
	 * @return the schedule controller failure hash map
	 */
	private HashMap<String, Integer> getScheduleControllerFailureHashMap() {
		if (scheduleControllerFailureHashMap==null) {
			scheduleControllerFailureHashMap = new HashMap<>();
		}
		return scheduleControllerFailureHashMap;
	}
	
	/**
	 * Appends the specified {@link TechnicalSystemStateEvaluation} to network components schedule controller.
	 *
	 * @param sc the {@link ScheduleController} that controls the {@link ScheduleList}
	 * @param tsseNew the new {@link TechnicalSystemStateEvaluation} to append
	 */
	public void appendToNetworkComponentsScheduleController(ScheduleController sc, TechnicalSystemStateEvaluation tsseNew) {
		this.appendToNetworkComponentsScheduleController(sc, tsseNew, true);
	}
	/**
	 * Appends the specified {@link TechnicalSystemStateEvaluation} to network components schedule controller.
	 *
	 * @param sc the {@link ScheduleController} that controls the {@link ScheduleList}
	 * @param tsseNew the new {@link TechnicalSystemStateEvaluation} to append
	 * @param isRealTimeSchedule the is real time schedule
	 */
	private void appendToNetworkComponentsScheduleController(ScheduleController sc, TechnicalSystemStateEvaluation tsseNew, boolean isRealTimeSchedule) {
		
		if (sc==null || tsseNew==null) return;
		
		ScheduleList sl = sc.getScheduleList();
		Schedule schedule = null;

		boolean updateMemberEvaluationStrategy = false;
		if (sl.getSchedules().size()==0) {
			// --- Create a Schedule first --------------------------
			schedule = this.createSchedule(isRealTimeSchedule);
			sl.getSchedules().add(schedule);
			// --- Remind to update the MemberEvaluationStrategy ----
			updateMemberEvaluationStrategy = true;
			this.notifyScheduleListObserver(sc, ScheduleNotification.Reason.ScheduleListLoaded, null);
			
		} else {
			// --- Get the schedule ---------------------------------
			schedule = sl.getSchedules().get(0);
		}
		
		// --- Check the required real time attribute --------------- 
		if (schedule.isRealTimeSchedule()!=isRealTimeSchedule) {
			schedule.setRealTimeSchedule(isRealTimeSchedule);
		}
		
		// --- Set the parent of the new state ----------------------
		TechnicalSystemStateEvaluation tssePrev = schedule.getTechnicalSystemStateEvaluation();
		if (tssePrev!=null) {
			if(tssePrev.getGlobalTime()<tsseNew.getGlobalTime()) {
				// --- Later time stamp -> append -------------------
				tsseNew.setParent(tssePrev);
			} else if (tssePrev.getGlobalTime()==tsseNew.getGlobalTime()) {
				// --- Same time stamp -> replace -------------------
				tsseNew.setParent(tssePrev.getParent());
			}
		}
		// --- Set new state as new final state of the Schedule -----
		schedule.setTechnicalSystemStateEvaluation(tsseNew);
		
		// --- Update the MemberEvaluationStrategy ? ----------------
		if (updateMemberEvaluationStrategy==true) {
			this.restartMemberEvaluationStrategyScheduleList(sc);
		}
		this.appendToMemberEvaluationStrategyScheduleList(sc, tsseNew);
		
		// --- Remind the update for a later visualization ----------
		String networkComponentID = sl.getNetworkID();
		if (networkComponentID!=null && tsseNew!=null) {
			this.getLastNetworkComponentUpdates().put(networkComponentID, tsseNew);
		}
		
		// --- Do controller and visualization update ---------------
		this.notifyScheduleListObserver(sc, ScheduleNotification.Reason.ScheduleUpdated, schedule);
	}
	
	// ------------------------------------------------------------------------	
	// --- From here, exchange methods for an aggregation can be found --------
	// ------------------------------------------------------------------------
	/**
	 * Returns the last technical system states from schedule controller.
	 * @return the last technical system states from schedule controller
	 */
	public HashMap<String, TechnicalSystemStateEvaluation> getLastTechnicalSystemStatesFromScheduleController() {
		
		HashMap<String, TechnicalSystemStateEvaluation> latestTSSEs = new HashMap<>(); 
		
		List<String> netCompIDs = new ArrayList<>(this.getNetworkComponentsScheduleController().keySet()); 
		for (int i = 0; i < netCompIDs.size(); i++) {
			String netCompID = netCompIDs.get(i);
			ScheduleController sc = this.getNetworkComponentsScheduleController().get(netCompID);
			if (sc!=null && sc.getScheduleList().getSchedules().size()>0) {
				Schedule schedule = sc.getScheduleList().getSchedules().get(0);
				if (schedule!=null) {
					TechnicalSystemStateEvaluation tsse = schedule.getTechnicalSystemStateEvaluation();
					if (tsse!=null) {
						TechnicalSystemStateEvaluation tsseCopy = TechnicalSystemStateHelper.getTsseCloneWithoutParent(tsse);
						latestTSSEs.put(netCompID, tsseCopy);
					}
				}
			}
		}
		return latestTSSEs;
	}
	/**
	 * Sets the specified HashMap of states to network components schedule controller as the only available state (will not be appended but exchanged).
	 * @param tsseHash the HashMap of {@link TechnicalSystemStateEvaluation}
	 */
	public void setTechnicalSystemStatesToScheduleController(HashMap<String, TechnicalSystemStateEvaluation> tsseHash) {
		if (tsseHash==null || tsseHash.size()==0) return;
		List<String> netCompIDs = new ArrayList<>(tsseHash.keySet());
		for (int i = 0; i < netCompIDs.size(); i++) {
			String netCompID = netCompIDs.get(i);
			TechnicalSystemStateEvaluation tsse = tsseHash.get(netCompID);
			ScheduleController sc = this.getNetworkComponentsScheduleController().get(netCompID);
			if (sc.getScheduleList().getSchedules().size()==0) {
				this.appendToNetworkComponentsScheduleController(netCompID, tsse);
			} else {
				Schedule schedule = sc.getScheduleList().getSchedules().get(0);
				schedule.setTechnicalSystemStateEvaluation(tsse);
			}
		}
	}
	/**
	 * Append the specified HashMap of states to network components schedule controller.
	 * @param tsseHash the HashMap of {@link TechnicalSystemStateEvaluation} 
	 */
	public void appendToNetworkComponentsScheduleController(HashMap<String, TechnicalSystemStateEvaluation> tsseHash) {
		if (tsseHash==null || tsseHash.size()==0) return;
		List<String> netCompIDs = new ArrayList<>(tsseHash.keySet());
		for (int i = 0; i < netCompIDs.size(); i++) {
			String netCompID = netCompIDs.get(i);
			TechnicalSystemStateEvaluation tsse = tsseHash.get(netCompID); 
			this.appendToNetworkComponentsScheduleController(netCompID, tsse);
		}
	}

	/**
	 * Returns the old states that were exchanged with the specified system states at schedule controller.
	 * @return the last technical system states from schedule controller
	 */
	public HashMap<String, TechnicalSystemStateEvaluation> exchangeLastTechnicalSystemStatesInScheduleController(HashMap<String, TechnicalSystemStateEvaluation> tsseHash) {
		
		if (tsseHash==null || tsseHash.size()==0) return null;
		
		HashMap<String, TechnicalSystemStateEvaluation> tsseHashOld = new HashMap<>();
		List<String> netCompIDs = new ArrayList<>(tsseHash.keySet());
		for (int i = 0; i < netCompIDs.size(); i++) {
			String netCompID = netCompIDs.get(i);
			TechnicalSystemStateEvaluation tsseNew = tsseHash.get(netCompID);
			TechnicalSystemStateEvaluation tsseOld = this.exchangeLastTechnicalSystemStatesInScheduleController(netCompID, tsseNew);
			tsseHashOld.put(netCompID, tsseOld);
		}
		return tsseHashOld;
	}
	/**
	 * Exchange the last {@link TechnicalSystemStateEvaluation} in the corresponding {@link ScheduleController} and return the last / old system state.
	 *
	 * @param netCompID the ID of the network component 
	 * @param tsseNew the new {@link TechnicalSystemStateEvaluation}
	 * @return the technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation exchangeLastTechnicalSystemStatesInScheduleController(String netCompID, TechnicalSystemStateEvaluation tsseNew) {
		
		if (netCompID==null || tsseNew==null) return null;
		
		TechnicalSystemStateEvaluation tsseOld = null;
		ScheduleController sc = this.getNetworkComponentsScheduleController().get(netCompID);
		if (sc!=null) {
			if (sc!=null && sc.getScheduleList().getSchedules().size()>0) {
				Schedule schedule = sc.getScheduleList().getSchedules().get(0);
				tsseOld = schedule.getTechnicalSystemStateEvaluation();
				if (tsseNew!=tsseOld) {
					if (tsseOld!=null && tsseOld.getParent()!=null) {
						tsseNew.setParent(tsseOld.getParent());
						tsseOld.setParent(null);
					}
					schedule.setTechnicalSystemStateEvaluation(tsseNew);
					this.resetMemberEvaluationStrategies(netCompID);
				}
			}
		}
		return tsseOld;
	}
	
	
	/**
	 * Resets the member evaluation strategies for this network component
	 * @param netCompID the network component ID
	 */
	private void resetMemberEvaluationStrategies(String netCompID) {

		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			// --- Get the aggregation builder for this subnetwork ----------------------
			AbstractSubNetworkConfiguration config = this.getSubNetworkConfigurations().get(i); 
			GroupController groupController = config.getSubAggregationBuilder().getGroupController();
			
			// --- Get the GroupTreeNodeObject for this network component----------------
			DefaultMutableTreeNode treeNode = groupController.getGroupTreeModel().getGroupTreeNodeByNetworkID(netCompID);
			if (treeNode!=null) {
				// --- Get and reset the MemberEvaluationStrategy -----------------------
				GroupTreeNodeObject gnto = (GroupTreeNodeObject) treeNode.getUserObject();
				MemberEvaluationStrategy mes = gnto.getGroupMemberEvaluationStrategy(config.getNetworkCalculationStrategy());
				if (mes!=null && mes instanceof MemberEvaluationStrategyScheduList) {
					((MemberEvaluationStrategyScheduList)mes).resetSchedule();
				}
			}
		}
	}
	
	/**
	 * Reverts all aggregators result {@link ScheduleList}'s by one state.
	 * @return the removed {@link TechnicalSystemStateEvaluation} for each subnetwork configuration, identified by the subnetwork-ID
	 */
	public HashMap<String, TechnicalSystemStateEvaluation> revertAggregatorScheduleListsByOneState() {

		HashMap<String, TechnicalSystemStateEvaluation> revertedStates = new HashMap<>();
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			// --- Check all sub network configurations ---
			AbstractSubNetworkConfiguration config = this.getSubNetworkConfigurations().get(i);
			String subNetworkDescriptionID = config.getSubNetworkDescriptionID();
			TechnicalSystemStateEvaluation tsseRemoved = this.revertAggregatorScheduleListByOneState(config);
			if (tsseRemoved!=null) {
				revertedStates.put(subNetworkDescriptionID, tsseRemoved);	
			}
		}
		return revertedStates;
	}
	
	/**
	 * Reverts the aggregator result {@link ScheduleList} by one state for the specified subnetwork.
	 *
	 * @param config the AbstractSubNetworkConfiguration
	 * @return the technical system state evaluation that were removed
	 */
	public TechnicalSystemStateEvaluation revertAggregatorScheduleListByOneState(AbstractSubNetworkConfiguration config) {
		
		// --- Define the result --------------------------
		TechnicalSystemStateEvaluation revertedState = null;

		// --- Get GroupController from builder -----------
		AbstractSubAggregationBuilder builder = config.getSubAggregationBuilder();
		OptionModelController groupOMC = builder.getGroupController().getGroupOptionModelController();
		
		// --- Get the result ScheduleList ----------------
		ScheduleController scResult = groupOMC.getEvaluationProcess().getScheduleController();
		ScheduleList sl = scResult.getScheduleList();
		if (sl!=null && sl.getSchedules().size()>0) {
			Schedule schedule = sl.getSchedules().get(0);
			TechnicalSystemStateEvaluation tsseLast = schedule.getTechnicalSystemStateEvaluation();
			if (tsseLast!=null) {
				schedule.setTechnicalSystemStateEvaluation(tsseLast.getParent());
				revertedState = tsseLast;	
			}
		}
		return revertedState;
	}
	
	
	/**
	 * Resets the aggregator schedule lists.
	 */
	public void resetAggregatorScheduleLists() {
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			this.resetAggregatorScheduleList(this.getSubNetworkConfigurations().get(i));
		}
	}
	/**
	 * Resets the aggregator schedule list for the specified sub network.
	 * @param subNetworkID the sub network ID
	 */
	public void resetAggregatorScheduleList(AbstractSubNetworkConfiguration config) {

		if (config==null) return;
		
		// --- Get GroupController from builder -----------
		AbstractSubAggregationBuilder builder = config.getSubAggregationBuilder();
		OptionModelController groupOMC = builder.getGroupController().getGroupOptionModelController();
		// --- Get the result ScheduleList ----------------
		ScheduleController scResult = groupOMC.getEvaluationProcess().getScheduleController();
		ScheduleList sl = scResult.getScheduleList();
		if (sl!=null && sl.getSchedules().size()>0) {
			sl.getSchedules().clear();
		}

		// --- Reset the calculation strategy -------------
		config.getNetworkCalculationStrategy().setTechnicalSystemStateEvaluation(null);
		
		// --- Reset the group calculation ----------------
		AbstractOptionModelCalculation calculation = groupOMC.getOptionModelCalculation();
		if (calculation instanceof GroupCalculation) {
			((GroupCalculation)calculation).resetTsseCalculatedLast();
		}
		
	}
	
	/**
	 * Creates a new schedule.
	 * @return the schedule
	 */
	protected Schedule createSchedule(boolean isRealTimeSchedule) {
		Schedule schedule = new Schedule();
		schedule = new Schedule();
		schedule.setRealTimeSchedule(isRealTimeSchedule);
		schedule.setPriority(0);
		schedule.setSourceThread(Thread.currentThread().getName());
		schedule.setStrategyClass(this.getClass().getName());
		return schedule;
	}
	/**
	 * Restarts the corresponding member evaluation strategy for ScheduleList's.
	 * @param sc the ScheduleController to consider
	 */
	protected void restartMemberEvaluationStrategyScheduleList(ScheduleController sc) {
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			AbstractSubNetworkConfiguration config = getSubNetworkConfigurations().get(i);
			AbstractNetworkCalculationStrategy networkCalculationStrategy = config.getNetworkCalculationStrategy();
			if (networkCalculationStrategy!=null) {
				networkCalculationStrategy.restartMemberEvaluationStrategyScheduleList(sc);
			}
		}
	}
	/**
	 * Appends the specified {@link TechnicalSystemStateEvaluation} to the member evaluation strategy for a ScheduleList.
	 *
	 * @param sc the ScheduleController to consider
	 * @param tsseNew the new {@link TechnicalSystemStateEvaluation}
	 */
	protected void appendToMemberEvaluationStrategyScheduleList(ScheduleController sc, TechnicalSystemStateEvaluation tsseNew) {
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			AbstractSubNetworkConfiguration config = getSubNetworkConfigurations().get(i);
			AbstractNetworkCalculationStrategy networkCalculationStrategy = config.getNetworkCalculationStrategy();
			if (networkCalculationStrategy!=null) {
				networkCalculationStrategy.appendToMemberEvaluationStrategyScheduleList(sc, tsseNew);
			}
		}
	}
	
	/**
	 * Notifies the observer of the specified ScheduleController by using a {@link ScheduleNotification} - e.g. for updates of the visualization .
	 *
	 * @param sc the {@link ScheduleController} to use for the notification
	 * @param reason the reason for the notification
	 * @param notificationObject the notification object
	 */
	protected void notifyScheduleListObserver(final ScheduleController sc, final ScheduleNotification.Reason reason, final Object notificationObject) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sc.setChangedAndNotifyObservers(new ScheduleNotification(reason, notificationObject));
			}
		});
	}
	
	// ----------------------------------------------------------------------------------
	// --- From here, the execution of network calculations are organized ---------------  
	// ----------------------------------------------------------------------------------
	
	/**
	 * Sets the evaluation start time for all sub aggregations.
	 * @param newGlobalTime the new evaluation start time
	 */
	public void setEvaluationStartTime(long newGlobalTime) {
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			AbstractSubNetworkConfiguration config = this.getSubNetworkConfigurations().get(i);
			OptionModelController omcGroup = config.getSubAggregationBuilder().getGroupController().getGroupOptionModelController();
			// --- Set new global time to the state list ------------
			if (omcGroup.getEvaluationStateList().size()>0) {
				if (omcGroup.getEvaluationStateList().get(0) instanceof TechnicalSystemStateTime) {
					TechnicalSystemStateTime tsst = omcGroup.getEvaluationStateList().get(0); 
					tsst.setGlobalTime(newGlobalTime);
				} else if (omcGroup.getEvaluationStateList().get(0) instanceof TechnicalSystemState) {
					TechnicalSystemState tssInitial = (TechnicalSystemState) omcGroup.getEvaluationStateList().get(0);
					tssInitial.setGlobalTime(newGlobalTime);
				}
			}
			// --- Set to the initial TSSE --------------------------
			omcGroup.getEvaluationProcess().resetEvaluationProcess(true);
			TechnicalSystemStateEvaluation tsseInitial = omcGroup.getEvaluationProcess().getInitialTechnicalSystemStateEvaluation();
			if (tsseInitial!=null) {
				tsseInitial.setGlobalTime(newGlobalTime);
			}
		}
	}	
	
	/**
	 * Runs the aggregators evaluations or network calculation until the specified time.
	 * The method will return when the calculation is done.
	 * @param timeUntil the time until the evaluation should be executed
	 */
	public void runEvaluationUntil(final long timeUntil) {
		this.runEvaluationUntil(timeUntil, false);
	}
	
	/**
	 * Runs the aggregators evaluations or network calculation until the specified time.
	 * The method will return when the calculation is done.
	 * @param timeUntil the time until the evaluation should be executed
	 * @param rebuildDecisionGraph If true, the decision graph will be rebuilt
	 */
	public void runEvaluationUntil(long timeUntil, boolean rebuildDecisionGraph) {

		// --- Assign actual job to task thread ---------------------
		for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
			// --- Get corresponding NetworkCalculationStrategy -----
			AbstractSubNetworkConfiguration subNetConfig = getSubNetworkConfigurations().get(i);
			NetworkAggregationTaskThread taskThread = this.getOrCreateNetworkAggregationTaskThread(subNetConfig); 
			AbstractNetworkCalculationStrategy networkCalculationStrategy = subNetConfig.getNetworkCalculationStrategy();
			if (networkCalculationStrategy!=null) {
				taskThread.runEvaluationUntil(timeUntil, rebuildDecisionGraph);
			} else {
				taskThread.setDoNothing();
			}
		}

		// --- Check if done list is filled (first time!) -----------
		this.waitForNetworkAggregationTasksDone();
		// --- Start and wait for task threads ---------------------- 
		this.startAndWaitForNetworkAggregationTaskThreads();;
		
		
		// --- Notify listeners that calculation is done ------------
		this.notifyListenerAboutNetworkCalculationDone();

		// --- Forward last updates to display updater --------------
		this.forwardLastUpdatesToDisplayUpdater(timeUntil);
		
	}
	
	/**
	 * Returns the network aggregation task thread hash map.
	 * @return the network aggregation task thread hash map
	 */
	private HashMap<AbstractSubNetworkConfiguration, NetworkAggregationTaskThread> getNetworkAggregationTaskThreadHashMap() {
		if (networkAggregationTaskThreadHashMap==null) {
			networkAggregationTaskThreadHashMap = new HashMap<>();
		}
		return networkAggregationTaskThreadHashMap;
	}
	/**
	 * Returns (or creates) a {@link NetworkAggregationTaskThread} for the specified SubNetworkConfiguration.
	 *
	 * @param subNetConfig the extended {@link AbstractSubNetworkConfiguration}
	 * @return the or create network calculation thread
	 */
	private NetworkAggregationTaskThread getOrCreateNetworkAggregationTaskThread(AbstractSubNetworkConfiguration subNetConfig) {
		
		NetworkAggregationTaskThread netAggTaskThread = this.getNetworkAggregationTaskThreadHashMap().get(subNetConfig);
		if (netAggTaskThread==null) {
			netAggTaskThread = new NetworkAggregationTaskThread(this, subNetConfig, Thread.currentThread().getName());
			this.getNetworkAggregationTaskThreadHashMap().put(subNetConfig, netAggTaskThread);
		}
		return netAggTaskThread;
	}
	
	/**
	 * Returns the network aggregation task trigger.
	 * @return the calculation trigger
	 */
	protected Object getNetworkAggregationTaskTrigger() {
		if (networkAggregationTaskTrigger==null) {
			networkAggregationTaskTrigger = new Object();
		}
		return networkAggregationTaskTrigger;
	}
	/**
	 * Returns the list of NetworkAggregationTaskThread's that have done their job so far.
	 * @return the network aggregation task done list
	 */
	protected synchronized List<NetworkAggregationTaskThread> getNetworkAggregationTaskDoneList() {
		if (networkAggregationTaskDoneList==null) {
			networkAggregationTaskDoneList = new ArrayList<>();
		}
		return networkAggregationTaskDoneList;
	}
	/**
	 * Starts and waits for network aggregation task threads.
	 */
	private void startAndWaitForNetworkAggregationTaskThreads() {
		// --- Start task threads ----------------------------------- 
		this.startNetworkAggregationTaskThreads();
		// --- Again wait for the end of the jobs -------------------
		this.waitForNetworkAggregationTasksDone();

	}
	/**
	 * (Re-)Start network aggregation task threads.
	 */
	private void startNetworkAggregationTaskThreads() {
		// --- Clear done-list --------------------------------------
		this.getNetworkAggregationTaskDoneList().clear();
		// --- Notify all waiting task threads ----------------------
		synchronized (this.getNetworkAggregationTaskTrigger()) {
			this.getNetworkAggregationTaskTrigger().notifyAll();
		}
	}
	/**
	 * Waits until the network aggregation tasks are done.
	 */
	private void waitForNetworkAggregationTasksDone() {
		while (this.getNetworkAggregationTaskDoneList().size()<this.getNetworkAggregationTaskThreadHashMap().size()) {
			try {
				Thread.sleep(3);
			} catch (InterruptedException iEx) {
				//iEx.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Returns the HashMap with the last network component updates.
	 * @return the last network component updates
	 */
	private HashMap<String, TechnicalSystemStateEvaluation> getLastNetworkComponentUpdates() {
		if (lastNetworkComponentUpdates==null) {
			lastNetworkComponentUpdates = new HashMap<>();
		}
		return lastNetworkComponentUpdates;
	}
	/**
	 * Forwards the last system states updates to the display updater.
	 */
	private void forwardLastUpdatesToDisplayUpdater(long displayTime) {
		
		// --- Get last changes for this method and clear local instance ------
		HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates = null;
		synchronized (this.getLastNetworkComponentUpdates()) {
			lastStateUpdates = new HashMap<>(this.getLastNetworkComponentUpdates());
			this.getLastNetworkComponentUpdates().clear();
		}
		
		// --- Call the display update mechanism? -----------------------------
		if (this.getAggregationListeners().size()>0 && lastStateUpdates!=null && lastStateUpdates.size()>0) {
			// --- Assign actual job to task threads --------------------------
			for (int i = 0; i < this.getSubNetworkConfigurations().size(); i++) {
				// --- Get corresponding DisplayUpdater -----------------------
				AbstractSubNetworkConfiguration subNetConfig = getSubNetworkConfigurations().get(i);
				NetworkAggregationTaskThread taskThread = this.getOrCreateNetworkAggregationTaskThread(subNetConfig); 
				AbstractNetworkModelDisplayUpdater displayUpdater = subNetConfig.getNetworkDisplayUpdater();
				if (displayUpdater!=null) {
					taskThread.updateNetworkModelDisplay(lastStateUpdates, displayTime);
				} else {
					taskThread.setDoNothing();
				}
			}
			// --- Start and wait for task threads ----------------------------
			this.startAndWaitForNetworkAggregationTaskThreads();;
		}
		
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, the network calculation listener are organized --------------------  
	// ----------------------------------------------------------------------------------	
	/**
	 * Returns the list of registered aggregation listeners.
	 * @return the network calculation listeners
	 */
	private Vector<AggregationListener> getAggregationListeners() {
		if (aggregationListenerListeners == null) {
			aggregationListenerListeners = new Vector<AggregationListener>();
		}
		return aggregationListenerListeners;
	}
	/**
	 * Adds an aggregation listener.
	 * @param listener the listener
	 */
	public void addAggregationListener(AggregationListener listener) {
		this.getAggregationListeners().addElement(listener);
	}
	/**
	 * Removes an aggregation listener.
	 * @param listener the listener
	 */
	public void removeAggregationListener(AggregationListener listener) {
		this.getAggregationListeners().remove(listener);
	}
	/**
	 * Notifies all listeners about new calculation results.
	 */
	private void notifyListenerAboutNetworkCalculationDone() {
		for (int i=0; i<this.getAggregationListeners().size(); i++) {
			AggregationListener listener = this.getAggregationListeners().get(i);
			listener.networkCalculationDone();
		}
	}
	/**
	 * Notify listener that network display updates were prepared from the aggregator.
	 * @param displayNotification the display notification
	 */
	protected void notifyListenerAboutDisplayNotifications(DisplayAgentNotification displayNotification) {
		for (int i=0; i<this.getAggregationListeners().size(); i++) {
			AggregationListener listener = this.getAggregationListeners().get(i);
			listener.sendDisplayAgentNotification(displayNotification);
		}
	}
	
}
