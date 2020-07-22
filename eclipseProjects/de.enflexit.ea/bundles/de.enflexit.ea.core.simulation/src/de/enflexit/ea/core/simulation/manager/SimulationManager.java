package de.enflexit.ea.core.simulation.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;
import org.awb.env.networkModel.visualisation.notifications.EnableNetworkModelUpdateNotification;
import org.awb.env.networkModel.visualisation.notifications.EnvironmentModelUpdateNotification;

import agentgui.core.application.Application;
import agentgui.core.classLoadService.ClassLoadServiceUtility;
import agentgui.ontology.Simple_Boolean;
import agentgui.simulationService.SimulationService;
import agentgui.simulationService.SimulationServiceHelper;
import agentgui.simulationService.agents.SimulationManagerAgent;
import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.time.TimeModelContinuous;
import agentgui.simulationService.time.TimeModelDiscrete;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.common.performance.PerformanceMeasurements;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.AggregationListener;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import de.enflexit.ea.core.dataModel.blackboard.Blackboard;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardAgent;
import de.enflexit.ea.core.dataModel.blackboard.DomainBlackboard;
import de.enflexit.ea.core.dataModel.ontology.SlackNodeSetVoltageLevelNotification;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.PowerFlowCalculationThread;
import de.enflexit.ea.electricity.aggregation.triPhase.SubNetworkConfigurationElectricalDistributionGrids;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.evaluation.TechnicalSystemStateHelper;
import energy.optionModel.Schedule;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;
import energy.schedule.ScheduleNotification;
import energy.schedule.ScheduleNotification.Reason;
import energygroup.GroupController;
import jade.core.AID;
import jade.core.ServiceException;
import jade.wrapper.StaleProxyException;

/**
 * This SimulationManager manages the flow of the simulation 
 * and is the administrator of the EnvironmentModel.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimulationManager extends SimulationManagerAgent implements AggregationListener {

	private static final long serialVersionUID = 2816217651269067503L;
	
	private static final String dateFormat = "hh:mm:ss-SSS";
	private SimpleDateFormat sdf;

	private boolean isDoPerformanceMeasurements;
	private static final String SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP  = "1 SimMa: Discrete-Round-Trip-Complete";
	private static final String SIMA_MEASUREMENT_NETWORK_CALCULATIONS = "2 SimMa: - Aggregation-Execution     ";
	
	private HyGridAbstractEnvironmentModel hygridSettings;

	private boolean isHeadlessOperation;
	private boolean isPaused;

	private Integer numberOfExecutedDeviceAgents;
	private Vector<EnvironmentNotification> agentsNotifications;

	private long endTimeNextSimulationStep;
	
	private AbstractAggregationHandler aggregationHandler;
	private Blackboard blackboard;
	
	private Hashtable<AID, Object> environmentNotificationReminder;
	private NetworkCalculationExecuter networkCalculationExecuter;
	private Object networkCalculationTrigger;
	
	
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#setup()
	 */
	@Override
	protected void setup() {

		// --- Set debugging option -------------------------------------------
		this.debug = false;
		this.isDoPerformanceMeasurements = false;
		
		// --- Start the BlackBoardAgent --------------------------------------
		this.startBlackBoardAgent();
		
		// --- work on the start arguments for the simulation manager ---------
		Object[] args = this.getArguments();
		if (args!=null) {
			// --- SimpleBoolean for headless operation -----------------------
			Simple_Boolean sBool = (Simple_Boolean) args[0];
			this.setHeadlessOperation(sBool.getBooleanValue());
		}
		
		// --- super.setup() will get the copy of current EnvironmentModel ----
		super.setup();
		// --- Ensure to reset the time model (applies to the discrete) -------
		this.resetTimeModel();
		// --- Get the current NetworkModel -----------------------------------
		this.getBlackboard().setNetworkModel((NetworkModel) this.getDisplayEnvironment());
		// --- Get settings for Display- and Energy- notifications ------------ 
		this.hygridSettings = (HyGridAbstractEnvironmentModel) this.getAbstractEnvironment();
		this.hygridSettings.setTimeModelType(this.getTimeModel());

		// --- Create 'No-System' - ScheduleList's ----------------------------
		if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelDiscrete) {
			new NoSystemScheduleListCreator(this.getBlackboard().getNetworkModel(), this.getTimeModelDiscrete());	
		} else if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous) {
			new NoSystemScheduleListCreator(this.getBlackboard().getNetworkModel(), this.getTimeModelContinuous());
		}
		
		// --- Prepare the aggregation handler --------------------------------
		this.getAggregationHandler();
		// --- If measurements are activated, configure aggregation handler ---
		this.registerPerformanceMeasurements();
		// --- Add the managers internal cyclic simulation behaviour ----------
		this.addSimulationBehaviour();
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#takeDown()
	 */
	@Override
	protected void takeDown() {
		this.getAggregationHandler().terminate();
		this.stopNetworkCalculationExecuter();
		this.getBlackboard().stopBlackboardListenerServiceThread();
		super.takeDown();
	}
	
	
	/**
	 * Registers the performance measurements of the simulation manager if the
	 * local variable {@link #isDoPerformanceMeasurements} is set to true.
	 */
	private void registerPerformanceMeasurements() {
		if (this.isDoPerformanceMeasurements==true) {
			
			// --- Get PerformanceMeasurements instance ---
			PerformanceMeasurements pm = PerformanceMeasurements.getInstance();
			
			// --- Define a PerformanceGroup --------------
			String[] pGroup = new String[21];
			
			pGroup[0] = SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP;
			pGroup[1] = SIMA_MEASUREMENT_NETWORK_CALCULATIONS;
			
			pGroup[2] = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_EXECUTION;
			pGroup[3] = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_PREPROCESSING;
			pGroup[4] = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_DELTA_STEPS_CALL;
			pGroup[5] = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_NETWORK_CALCULATION;
			pGroup[6] = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_FLOW_SUMMARIZATION;
			pGroup[7] = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_DISPLAY_UPDATE_EXECUTION;
			
			pGroup[8]  = AbstractEvaluationStrategy.EVALUATION_STRATEGY_CALL_OUTEDGES_DETERMINATION;
			pGroup[9]  = AbstractEvaluationStrategy.EVALUATION_STRATEGY_CALL_OUTEDGES_FIND_START_NODE;
			pGroup[10] = AbstractEvaluationStrategy.EVALUATION_STRATEGY_CALL_OUTEDGE_CREATION;
			pGroup[11] = AbstractEvaluationStrategy.EVALUATION_STRATEGY_OUT_EDGE_CHANGE_EVENTS;
			pGroup[12] = AbstractEvaluationStrategy.EVALUATION_STRATEGY_OUT_EDGE_GET_TSSE_VECTOR;
			pGroup[13] = AbstractEvaluationStrategy.EVALUATION_STRATEGY_OUT_EDGE_ADD_TO_COST_GRAPH;
			pGroup[14] = AbstractEvaluationStrategy.EVALUATION_STRATEGY_OUT_EDGE_DELTA_CREATION;

			pGroup[15] = TechnicalSystemStateDeltaEvaluation.TSS_DELTA_COMPLETE_CALCULATIONS;
			pGroup[16] = TechnicalSystemStateDeltaEvaluation.TSS_DELTA_GET_ENERGY_AND_GOOD_FLOWS;
			pGroup[17] = TechnicalSystemStateDeltaEvaluation.TSS_DELTA_GET_GROUP_FLOWS;
			pGroup[18] = TechnicalSystemStateDeltaEvaluation.TSS_DELTA_GET_AMOUNT_AND_COSTS;
			pGroup[19] = TechnicalSystemStateDeltaEvaluation.TSS_DELTA_GET_ENERGY_LOSSES;
			pGroup[20] = TechnicalSystemStateDeltaEvaluation.TSS_DELTA_GET_STATE_COSTS;

			pm.addPerformanceGroup("Simulation Procedure", pGroup, true);

			// --- Define the calculation base ------------
			int avgBase = 480;
			
			// --- Activate in aggregation handler --------
			this.getAggregationHandler().debugIsDoPerformanceMeasurements(true);
			this.getAggregationHandler().debugSetMaxNumberForPerformanceAverage(avgBase);
			
			// --- Register local measurements ------------ 
			pm.addPerformanceMeasurement(SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP, avgBase);
			pm.addPerformanceMeasurement(SIMA_MEASUREMENT_NETWORK_CALCULATIONS, avgBase);

		}
	}
	/**
	 * Returns the singleton instance of the PerformanceMeasurements.
	 * @return the performance measurements
	 */
	public PerformanceMeasurements getPerformanceMeasurements() {
		if (this.isDoPerformanceMeasurements==true) {
			return PerformanceMeasurements.getInstance();
		}
		return null;
	}
	/**
	 * Sets the specified measurement started.
	 * @param taskDescriptor the task descriptor
	 */
	public void setPerformanceMeasurementStarted(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setMeasurementStarted(taskDescriptor);
	}
	/**
	 * Sets the specified measurement finalized (applies regular and for loops).
	 * @param taskDescriptor the new measurement finalized
	 */
	public void setPerformanceMeasurementFinalized(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setMeasurementFinalized(taskDescriptor);
	}
	
	/**
	 * Sets the loop performance measurement started / looped.
	 * @param taskDescriptor the new loop performance measurement started
	 */
	public void setLoopPerformanceMeasurementStarted(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setLoopMeasurementStarted(taskDescriptor);
	}
	/**
	 * Sets the specified loop measurement finalized (applies regular and for loops).
	 * @param taskDescriptor the new measurement finalized
	 */
	public void setLoopPerformanceMeasurementFinalized(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setLoopMeasurementFinalized(taskDescriptor);
	}
	
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#setPauseSimulation(boolean)
	 */
	@Override
	public void setPauseSimulation(boolean doPause) {
		this.isPaused = doPause;
		if (this.isPaused==true) {
			// ------------------------------------------------------
			// --- Pause the simulation -----------------------------
			// ------------------------------------------------------
			this.print("Pause Simulation!", false);
			if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelDiscrete) {
				// --- No further efforts have to be spent ----------
			} else if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous) {
				// --- Pause the continuous simulation --------------
				this.getTimeModelContinuous().setExecuted(false);
			}
			
		} else {
			// ------------------------------------------------------
			// --- Restart the simulation ---------------------------
			// ------------------------------------------------------
			this.print("Re-Execute Simulation!", false);
			if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelDiscrete) {
				// --- Restart discrete simulation ------------------
				this.doSingleSimulationSequennce();
			} else if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous) {
				// --- Restart the continuous simulation ------------
				this.getTimeModelContinuous().setExecuted(true);
			}
		}
	}
	
	/**
	 * Returns the blackboard that contains the current state of the simulation.
	 * @return the blackboard
	 */
	public Blackboard getBlackboard() {
		if (blackboard==null) {
			blackboard = Blackboard.getInstance();
			blackboard.startBlackboardListenerServiceThread();
		}
		return blackboard;
	}
	/**
	 * Starts the {@link BlackboardAgent}.
	 */
	private void startBlackBoardAgent() {

		try {
			Object[] startArgument = new Object[1];
			startArgument[0] = this.getBlackboard();
			this.getContainerController().createNewAgent(GlobalHyGridConstants.BLACKBOARD_AGENT_NAME, BlackboardAgent.class.getName(), startArgument).start();

		} catch (StaleProxyException spe) {
			spe.printStackTrace();
		}
	}
	
	/**
	 * Prints the specified text to the console window.
	 * @param messageText the text that has to be printed to the console
	 * @param isError set true, if is an error
	 */
	public void print(String messageText, boolean isError) {
		if (isError) {
			System.err.println(this.getLocalName() + ": " + messageText);
		} else {
			System.out.println(this.getLocalName() + ": " + messageText);
		}
	}
	/**
	 * Prints the specified message. In case that the phase filter is set, the message will only
	 * be printed, if the local variable phase matches with the phase filter.
	 *
	 * @param message the message
	 * @see PowerFlowCalculationThread#phase
	 */
	public void debugPrintLine(Long timeStamp, String message) {
		if (this.debug==true) {
			if (timeStamp==null) {
				System.out.println(message);
			} else {
				System.out.println(this.getDateFormatter().format(new Date(timeStamp)) + ": " + message);
			}
		}
	}
	/**
	 * Returns a SimpleDateFormat that can be used for debugging.
	 * @return the date formatter
	 */
	private SimpleDateFormat getDateFormatter() {
		if (sdf==null) {
			sdf = new SimpleDateFormat(dateFormat);
		}
		return sdf;
	}
	
	/**
	 * Checks if is headless operation.
	 * @return true, if is headless operation
	 */
	public boolean isHeadlessOperation() {
		if (Application.isOperatingHeadless()==true) {
			return true;
		}
		return isHeadlessOperation;
	}
	/**
	 * Sets the headless operation.
	 * @param isHeadlessOperation the new headless operation
	 */
	public void setHeadlessOperation(boolean isHeadlessOperation) {
		this.isHeadlessOperation = isHeadlessOperation;
	}

	/**
	 * Gets the HyGrid abstract environment model.
	 * @return the HyGrid abstract environment model
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return hygridSettings;
	}
	
	/**
	 * Resets the current time in the time model used to start time.
	 */
	private void resetTimeModel() {
		TimeModelDiscrete timeModelDiscrete = this.getTimeModelDiscrete();
		if (timeModelDiscrete!=null) {
			timeModelDiscrete.setTime(timeModelDiscrete.getTimeStart());
		}
	}
	/**
	 * Returns the current {@link TimeModelDiscrete}, if this is currently used.
	 * @return the time model continuous
	 */
	public TimeModelDiscrete getTimeModelDiscrete() {
		if (this.getEnvironmentModel().getTimeModel() instanceof TimeModelDiscrete ) {
			return (TimeModelDiscrete) this.getEnvironmentModel().getTimeModel();
		}
		return null;
	}
	/**
	 * Returns the current {@link TimeModelContinuous}, if this is currently used.
	 * @return the time model continuous
	 */
	public TimeModelContinuous getTimeModelContinuous() {
		if (this.getEnvironmentModel().getTimeModel() instanceof TimeModelContinuous) {
			return (TimeModelContinuous) this.getEnvironmentModel().getTimeModel();
		}
		return null;
	}
	/**
	 * Returns the current time according to the TimeModel used.
	 * @return the time
	 */
	public Long getTime() {
		switch (this.hygridSettings.getTimeModelType()) {
		case TimeModelDiscrete:
			return this.getTimeModelDiscrete().getTime();			
		case TimeModelContinuous:
			return this.getTimeModelContinuous().getTime();
		}
		return null;
	}
	/**
	 * Returns the time format for the current setup.
	 * @return the time format
	 */
	public String getTimeFormat() {
		String timeFormat = "dd.MM.yy HH:mm:ss";
		switch (this.getHyGridAbstractEnvironmentModel().getTimeModelType()) {
		case TimeModelDiscrete:
			TimeModelDiscrete tmd = this.getTimeModelDiscrete();
			timeFormat = tmd.getTimeFormat();
			break;
		case TimeModelContinuous:
			TimeModelContinuous tmc = this.getTimeModelContinuous();
			timeFormat = tmc.getTimeFormat();
			break;
		}
		return timeFormat;
	}
	
	/**
	 * Gets the number of executed agents from the simulation setup.
	 * @return the number of executed agents from the simulation setup
	 */
	public int getNumberOfExpectedDeviceAgents() {
		if (this.numberOfExecutedDeviceAgents==null) {
			
			// --- Extract number of agents from network model ------------
			this.numberOfExecutedDeviceAgents = 0;
			
			// ----------------------------------------------------------------
			// --- Check if a class extending AbstractEnergyAgent is        ---
			// --- defined for the network component. If so, expect an      ---
			// --- answer from that agent.                                  ---
			// ----------------------------------------------------------------
			GeneralGraphSettings4MAS ggMAS = this.getBlackboard().getNetworkModel().getGeneralGraphSettings4MAS();
			Vector<NetworkComponent> netComps = this.getBlackboard().getNetworkModel().getNetworkComponentVectorSorted();
			HashMap<String, Boolean> energyAgentClasses = new HashMap<String, Boolean>();
			Class<?> energyAgentClass = AbstractEnergyAgent.class;
			for (int i = 0; i < netComps.size(); i++) {
				
				NetworkComponent netComp = netComps.get(i);
			
				// --- Get the ComponentTypeSettings of the NetworkComponent -- 
				ComponentTypeSettings cts = ggMAS.getCurrentCTS().get(netComp.getType());
				
				String className = cts.getAgentClass();
				if (className!=null) {

					// --- Check the HashMap first ----------------------------
					Boolean subclassOfEnergyAgent = energyAgentClasses.get(className);
					if (subclassOfEnergyAgent==null) {
						// --- If no entry found, examine the class -----------
						try {
							// --- Check if class extends AbstractEnergyAgent -
							Class<?> clazz = ClassLoadServiceUtility.forName(className);
							subclassOfEnergyAgent = energyAgentClass.isAssignableFrom(clazz);
							
							// --- Remember the result ------------------------
							energyAgentClasses.put(className, subclassOfEnergyAgent);
							
						} catch (NoClassDefFoundError e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					
					// --- Agent available? -----------------------------------
					if (subclassOfEnergyAgent==true) {
						this.numberOfExecutedDeviceAgents++;
					}
				}
			}
			this.debugPrintLine(this.getTime(), "Number of device Agents = " + this.numberOfExecutedDeviceAgents);
		}
		return this.numberOfExecutedDeviceAgents;
	}
	/**
	 * Distributes the specified {@link EnvironmentModel}.
	 * @param notifyAgents the notify agents
	 */
	private void distributeEnvironmentModel(boolean notifyAgents) {
		try {
			SimulationServiceHelper simHelper = (SimulationServiceHelper) getHelper(SimulationService.NAME);
			simHelper.setEnvironmentModel(this.getEnvironmentModel(), notifyAgents);
		} catch (ServiceException se) {
			this.print("Errror while distributing the EnvironmentModel:", true);
			se.printStackTrace();
		}
	}
	
	/**
	 * Resets the time for the end of the next simulation step to 0.
	 */
	private void resetEndTimeNextSimulationStep() {
		this.endTimeNextSimulationStep = 0;
	}
	/**
	 * Sets the desired time for the end of the next simulation step. The idea is that even in a discrete
	 * simulation, a single simulation step should not exceed this time limit. For this, the configured 
	 * simulation interval will be added to the current time and used as such end time.
	 */
	private void setEndTimeNextSimulationStep() {
		this.endTimeNextSimulationStep = System.currentTimeMillis() + this.hygridSettings.getSimulationIntervalLength();
	}
	/**
	 * Returns the current end time next simulation step.
	 * @return the end time next simulation step
	 */
	public long getEndTimeNextSimulationStep() {
		return endTimeNextSimulationStep;
	}
	/**
	 * Waits until the end of the current simulation step that was defined by {@link #setEndTimeNextSimulationStep()}.
	 * 
	 * @see #setEndTimeNextSimulationStep()
	 * @see #getEndTimeNextSimulationStep()
	 */
	private void waitUntilTheEndOfCurrentSimulationStep() {
		if (this.getEndTimeNextSimulationStep()>0) {
			while (System.currentTimeMillis() < this.getEndTimeNextSimulationStep()) {
				try {
					long sleepTime = this.getEndTimeNextSimulationStep() - System.currentTimeMillis();
					if (sleepTime>0) {
						Thread.sleep(sleepTime);
					} else {
						break;
					}
					
				} catch (InterruptedException ie) {
					//ie.printStackTrace();
				}
			}	
		}
	}
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#doSingleSimulationSequennce()
	 */
	@Override
	public void doSingleSimulationSequennce() {
		
		try {
			// --- Get the current simulation state -----------------------------------------------
			SimulationStatus simState = this.hygridSettings.getSimulationStatus();
			if (simState.getState()==null) {
				// --------------------------------------------------------------------------------
				// --- Initially distribute EnvironmentModel --------------------------------------
				// --------------------------------------------------------------------------------
				simState.setState(STATE.A_DistributeEnvironmentModel);
				this.print("Initially distribute Environment Model!", false);
				this.distributeEnvironmentModel(false);
				
			} else if (simState.getState()==STATE.A_DistributeEnvironmentModel) {
				// --------------------------------------------------------------------------------
				// --- Do the first simulation step -----------------------------------------------
				// --------------------------------------------------------------------------------
				if (this.isPaused==false) {
					simState.setState(STATE.B_ExecuteSimuation);
					this.print("Execute Simulation!", false);
					// --- Case separation Time Model --------------------------------------------
					switch (this.hygridSettings.getTimeModelType()) {
					case TimeModelDiscrete: // ---------------------------------------------------
						this.stepSimulation(this.getNumberOfExpectedDeviceAgents());
						this.setEndTimeNextSimulationStep();
						// --- Disable NetworkModel updates within GUI ----------------------------
						this.sendDisplayAgentNotification(new EnableNetworkModelUpdateNotification(false));
						break;

					case TimeModelContinuous: // --------------------------------------------------
						this.getNetworkCalculationExecuter();
						this.getTimeModelContinuous().setExecuted(true);
						this.distributeEnvironmentModel(true);
						break;
					}
				}
			
			} else if (simState.getState()==STATE.B_ExecuteSimuation) {
				// --------------------------------------------------------------------------------
				// --- Do simulation steps until the end time -------------------------------------
				// --------------------------------------------------------------------------------
				if (this.isPaused==false) {
					
					switch (this.hygridSettings.getTimeModelType()) {
					case TimeModelDiscrete:
						this.waitUntilTheEndOfCurrentSimulationStep();
						TimeModelDiscrete tmd = this.getTimeModelDiscrete();
						if (tmd.getTime()<tmd.getTimeStop()) {
							tmd.step();
						} else {
							simState.setState(STATE.C_StopSimulation);
							this.print("Finalize Simulation!", false);
							this.sendDisplayAgentNotification(new EnableNetworkModelUpdateNotification(true));
						}
						this.stepSimulation(this.getNumberOfExpectedDeviceAgents());
						this.setEndTimeNextSimulationStep();
						break;
						
					case TimeModelContinuous:
						simState.setState(STATE.C_StopSimulation);
						this.print("Finalise Simulation!", false);
						this.getTimeModelContinuous().setExecuted(false);
						this.stepSimulation(this.getNumberOfExpectedDeviceAgents());
						break;
					}
				}
				
			} else if (simState.getState()==STATE.C_StopSimulation) {
				// --------------------------------------------------------------------------------
				// --- Finalise simulation --------------------------------------------------------
				// --------------------------------------------------------------------------------
				this.resetEndTimeNextSimulationStep();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#proceedAgentAnswers(java.util.Hashtable)
	 */
	@Override
	protected void proceedAgentAnswers(Hashtable<AID, Object> agentAnswers) {
		// --------------------------------------------------------------------
		// --- Applies basically for a discrete time model !!! ----------------
		// --------------------------------------------------------------------
		// --- The above simulation step is done and every agent involved -----
		// --- should have send a kind of 'OK'-message or a new system state --
		// --------------------------------------------------------------------
		if (agentAnswers!=null && agentAnswers.size()>0) {
			
			try {
				// --- Get current time ---------------------------------------
				long currTime = this.getTime();
				// --- Set new states to the Schedules of the aggregation -----
				this.getAggregationHandler().setAgentAnswers(agentAnswers);
				this.debugPrintLine(currTime, "proceedAgentAnswers: Received " + agentAnswers.size() + " system states.");
				// --- Distinguish the time model type ------------------------ 
				switch (this.hygridSettings.getTimeModelType()) {
				case TimeModelDiscrete:
					// --- (Re)Execute the network calculation ----------------
					this.setPerformanceMeasurementStarted(SIMA_MEASUREMENT_NETWORK_CALCULATIONS);
					this.getAggregationHandler().runEvaluationUntil(currTime);
					this.setPerformanceMeasurementFinalized(SIMA_MEASUREMENT_NETWORK_CALCULATIONS);
					break;

				case TimeModelContinuous:
					// --- Start network calculation thread -------------------					
					this.getNetworkCalculationExecuter().start();
					break;
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// --- Finally: do the next simulation step by executing ----
		// --- the method 'doSingleSimulationSequennce()' again  ----
		if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous && this.hygridSettings.getSimulationStatus().getState()==STATE.B_ExecuteSimuation) {
			this.resetNetworkCalculationExecuterWaitTime();
		} else {
			this.setPerformanceMeasurementFinalized(SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP);
			this.doNextSimulationStep();
			this.setPerformanceMeasurementStarted(SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP);
		}
	}
	
	/**
	 * Returns the aggregation display behaviour.
	 * @return the display behaviour
	 */
	protected AbstractAggregationHandler getAggregationHandler() {
		if (aggregationHandler==null) {
			aggregationHandler = new AggregationHandler(this.getBlackboard().getNetworkModel(), this.isHeadlessOperation, this.getClass().getSimpleName());
			aggregationHandler.setOwnerInstance(this);
			aggregationHandler.addAggregationListener(this);
		}
		return aggregationHandler;
	}
	/* (non-Javadoc)
	 * @see hygrid.electricalNetwork.NetworkCalculationListener#networkCalculationDone()
	 */
	@Override
	public void networkCalculationDone() {

		// --------------------------------------------------------------------
		// --- Get all electrical distribution grid aggregations --------------
		// --------------------------------------------------------------------
		
//		//TODO implement domain-independent, remove dependency to electricity bundle
//		String subnetworkDescription = SubNetworkConfigurationElectricalDistributionGrids.SUBNET_DESCRIPTION_ELECTRICAL_DISTRIBUTION_GRIDS;
//		List<AbstractSubNetworkConfiguration> subnetConfigList = this.getAggregationHandler().getSubNetworkConfiguration(subnetworkDescription);
//		for (int i = 0; i < subnetConfigList.size(); i++) {
//			AbstractSubNetworkConfiguration subnetConfig = subnetConfigList.get(i);
//			if (subnetConfig!=null) {
//				AbstractElectricalNetworkCalculationStrategy netClacStrategy = (AbstractElectricalNetworkCalculationStrategy) subnetConfig.getNetworkCalculationStrategy();
//				if (netClacStrategy!=null) {
//					// --- Put the calculation results on the blackboard ------
//					this.getBlackboard().getGraphNodeStates().putAll(netClacStrategy.getGraphNodeStates());
//					this.getBlackboard().getNetworkComponentStates().putAll(netClacStrategy.getNetworkComponentStates());
//					this.getBlackboard().getTransformerStates().putAll(netClacStrategy.getTransformerStates());
//				}
//
//			} else {
//				System.err.println("[" + this.getClass().getSimpleName() + "] Could not find subnetwork configuration with the ID '" + subnetworkDescription + "'.");
//			}
//		}

		// --- Publish calculation results to the domain-specific sub-blackboard --------
		List<AbstractSubNetworkConfiguration> subnetConfigList = this.getAggregationHandler().getSubNetworkConfigurations();
		for (int i = 0; i < subnetConfigList.size(); i++) {
			AbstractSubNetworkConfiguration subnetConfig = subnetConfigList.get(i);
			if (subnetConfig!=null) {
				AbstractElectricalNetworkCalculationStrategy netClacStrategy = (AbstractElectricalNetworkCalculationStrategy) subnetConfig.getNetworkCalculationStrategy();
				if (netClacStrategy!=null) {
					DomainBlackboard domainBlackboard = this.getBlackboard().getDomainBlackboard(subnetConfig.getSubNetworkDescription());
					netClacStrategy.publishResultsToDomainBlackboard(domainBlackboard);
				}
			}
		}

		// --- Set state time to the blackboard --------------------------------
		this.getBlackboard().setStateTime(this.getAggregationHandler().getEvaluationEndTime());
		
		// --- Notify blackboard listeners about the new results --------------
		synchronized (this.getBlackboard().getNotificationTrigger()) {
			this.getBlackboard().getNotificationTrigger().notifyAll();
		}
	}
	
	/**
	 * Returns the agents that registered for this simulation.
	 * @return the agents registered
	 */
	private Vector<EnvironmentNotification> getAgentNotifications() {
		if (agentsNotifications==null) {
			agentsNotifications = new Vector<EnvironmentNotification>();
		}
		return agentsNotifications;
	}
	/**
	 * Resets the agent notifications.
	 */
	private void resetAgentNotifications() {
		this.agentsNotifications = null;
	}
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#onManagerNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	protected void onManagerNotification(EnvironmentNotification notification) {
		
		if (notification==null | notification.getNotification()==null) return;
		
		if (notification.getNotification() instanceof NetworkModel) {
			// --- Got an alternative NetworkModel ------------------------------------------------
			NetworkModel networkModelAlternative = (NetworkModel) notification.getNotification();
			String senderName = notification.getSender().getLocalName();
			NetworkModel networkModelLocal = (NetworkModel) this.getDisplayEnvironment();
			networkModelLocal.getAlternativeNetworkModel().put(senderName, networkModelAlternative);
			this.sendDisplayAgentNotification(new EnvironmentModelUpdateNotification(this.getEnvironmentModel()));
			return;
		}
		
		SimulationStatus simState = this.hygridSettings.getSimulationStatus();
		if (simState.getState()==STATE.A_DistributeEnvironmentModel) {
			if (notification.getNotification().equals(STATE_CONFIRMATION.Done) || notification.getNotification() instanceof Schedule) {
				
				this.debugPrintLine(this.getTime(), "Received STATE_CONFIRMATION.Done from agent " + notification.getSender().getLocalName());
				
				// --- Receiving register notification from agents in this simulation -------------
				this.getAgentNotifications().add(notification);
				if (notification.getNotification() instanceof Schedule) {
					// --- Receiving the Schedule for the energy transmission from agents ---------
					Schedule schedule = (Schedule) notification.getNotification();
					String agentName = notification.getSender().getLocalName();
					ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(agentName);
					System.out.println("Received Schedule from " + agentName);
					sc.getScheduleList().getSchedules().add(schedule);
					sc.notifyObservers(new ScheduleNotification(Reason.ScheduleListLoaded, null));
				}
				if (this.getAgentNotifications().size()==this.getNumberOfExpectedDeviceAgents()) {
					this.print("Initialization of simulation agents completed!", false);
					this.resetAgentNotifications();
					this.doNextSimulationStep();
				}
			}
			
		} else if (simState.getState()==STATE.B_ExecuteSimuation) {
			
			// --------------------------------------------------------------------------
			// --- Received a notification for the 'SlackNodeSetVoltageLevel' ? ---------
			// --------------------------------------------------------------------------
			if (notification.getNotification() instanceof SlackNodeSetVoltageLevelNotification) {
				// --- Cast to SlackNodeSetVoltageLevel and set new voltage level -------
				SlackNodeSetVoltageLevelNotification snvl = (SlackNodeSetVoltageLevelNotification) notification.getNotification();
				AID senderAID = notification.getSender();
				
				// --- Find the corresponding calculation strategy ---------------------- 
				String subnetworkDescription = SubNetworkConfigurationElectricalDistributionGrids.SUBNET_DESCRIPTION_ELECTRICAL_DISTRIBUTION_GRIDS;
				List<AbstractSubNetworkConfiguration> subnetConfigList = this.getAggregationHandler().getSubNetworkConfiguration(subnetworkDescription);
				for (int i = 0; i < subnetConfigList.size(); i++) {
					
					AbstractSubNetworkConfiguration subnetConfig = subnetConfigList.get(i);
					
					// --- Check if the aggregator contains the sender system -----------
					GroupController groupController = subnetConfig.getSubAggregationBuilder().getGroupController();
					DefaultMutableTreeNode treeNode = groupController.getGroupTreeModel().getGroupTreeNodeByNetworkID(senderAID.getLocalName());
					if (treeNode==null) continue;
					
					// --- Put slack node voltage level to network calculation strategy - 
					AbstractElectricalNetworkCalculationStrategy netClacStrategy = (AbstractElectricalNetworkCalculationStrategy) subnetConfig.getNetworkCalculationStrategy();
					if (netClacStrategy!=null) {
						HashMap<Phase, Double> slackNodeVoltageLevel = new HashMap<>();
						slackNodeVoltageLevel.put(Phase.L1, (double) snvl.getVoltageAbs().getValue());
						slackNodeVoltageLevel.put(Phase.L2, (double) snvl.getVoltageAbs().getValue());
						slackNodeVoltageLevel.put(Phase.L3, (double) snvl.getVoltageAbs().getValue());
						netClacStrategy.setSlackNodeVoltageLevel(slackNodeVoltageLevel);
						break;
					}
				}
				return;
			}
			
			// --------------------------------------------------------------------------------
			// --- The possible actions for a continuous time model ---------------------------
			// --------------------------------------------------------------------------------
			if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous) {
				
				if (notification.getNotification() instanceof TechnicalSystemStateEvaluation) {
					// --- For a continuous time model: Change of a system state received -----
					this.getAggregationHandler().setAgentAnswer(notification);
					this.debugPrintLine(this.getTime(), "Received new TSSE for " + notification.getSender().getLocalName() + " !");
					// --- Remind changes for the visualization -------------------------------
					this.getEnvironmentNotificationReminder().put(notification.getSender(), notification.getNotification());
					// --- (Re)Wait for further notifications ---------------------------------
					this.resetNetworkCalculationExecuterWaitTime();
					
				} else if (notification.getNotification().equals(STATE_CONFIRMATION.Done)) {
					// --- Finalize the simulation, since no further input can be expected ----
					this.getAgentNotifications().add(notification);
					if (this.getAgentNotifications().size()==this.getNumberOfExpectedDeviceAgents()) {
						this.print("Continuous time simulation completed!", false);
						this.resetAgentNotifications();
						this.doNextSimulationStep();
					}
				}
			}
			
		} else if (simState.getState()==STATE.C_StopSimulation & notification.getNotification().equals(STATE_CONFIRMATION.Done)) {
			// --- Receiving done notification from agents in this simulation ---------------------
			this.getAgentNotifications().add(notification);
			if (this.getAgentNotifications().size()==this.getNumberOfExpectedDeviceAgents()) {
				this.print("Finalisation of simulation completed!", false);
				this.resetAgentNotifications();
				this.doNextSimulationStep();
			}
		}
	}
	
	/**
	 * Gets the environment notification reminder.
	 * @return the environment notification reminder
	 */
	private Hashtable<AID, Object> getEnvironmentNotificationReminder() {
		if (environmentNotificationReminder==null) {
			environmentNotificationReminder = new Hashtable<>();	
		}
		return environmentNotificationReminder;
	}
	
	/**
	 * As a debug method, this allows to find system state in the the environmentNotificationReminder.
	 *
	 * @param componentID2Search4 the component ID tw search 4
	 * @param additionalMessage the additional message
	 */
	public void debugFindSystemState(String componentID2Search4, String additionalMessage) {
		if (this.debug==true) {
			Set<AID> keys = this.environmentNotificationReminder.keySet();
			for (AID aid : keys) {
				if (aid.getLocalName().equals(componentID2Search4)) {
					Object answer = this.environmentNotificationReminder.get(aid);
					if (answer==null) {
						this.debugPrintLine(this.getTime(), "Could not find system state for " + componentID2Search4  + " - " + additionalMessage);
					} else if (answer instanceof TechnicalSystemStateEvaluation) {
						this.debugPrintLine(this.getTime(), "Got System State from " + componentID2Search4 + " - " + additionalMessage); 
						this.debugPrintLine(null, TechnicalSystemStateHelper.toString((TechnicalSystemState) answer, true));
					} else {
						this.debugPrintLine(null, "Answer is not type of TSSE - " + additionalMessage);
					}
					return;
				}
			} // end for
			this.debugPrintLine(this.getTime(), "Could not find system state for " + componentID2Search4 + " - " + additionalMessage);
		}
	}
	
	/**
	 * Returns the {@link NetworkCalculationExecuter} of this simulation manager.
	 * @return the network calculation executer
	 */
	private NetworkCalculationExecuter getNetworkCalculationExecuter() {
		if (networkCalculationExecuter==null) {
			long netCalcInterval =  this.getHyGridAbstractEnvironmentModel().getNetworkCalculationIntervalLength();
			networkCalculationExecuter = new NetworkCalculationExecuter(this.getTimeModelContinuous(), netCalcInterval, this.getLocalName() + "_NetCalc");
		}
		return networkCalculationExecuter;
	}
	/**
	 * Stops the current {@link NetworkCalculationExecuter}.
	 */
	private void stopNetworkCalculationExecuter() {
		if (this.networkCalculationExecuter!=null) {
			this.getNetworkCalculationExecuter().setStopCalculation();
			this.getNetworkCalculationExecuter().interrupt();
		}
		this.networkCalculationExecuter = null;
	}
	/**
	 * Returns the network calculation trigger.
	 * @return the network calculation trigger
	 */
	private Object getNetworkCalculationTrigger() {
		if (networkCalculationTrigger==null) {
			networkCalculationTrigger = new Object();
		}
		return networkCalculationTrigger;
	}
	
	/**
	 * Reset network calculation executer wait time.
	 */
	private void resetNetworkCalculationExecuterWaitTime() {
		synchronized (this.getNetworkCalculationTrigger()) {
			this.getNetworkCalculationExecuter().resetWaitTime();
			this.getNetworkCalculationTrigger().notify();
		}
	}
	
	/**
	 * The Class (Thread) NetworkCalculationExecuter is responsible for the execution 
	 * of the network calculation, if a continuous time model is used.
	 * 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
	 */
	private class NetworkCalculationExecuter extends Thread {

		private long defaultWaitTime = 150;
		
		private TimeModelContinuous timeModel;
		private long calculationInterval;
		private long timeForNextCalculation;
		
		private boolean isExecute = true;
		private boolean isResetWaitTime;
		
		private long calcTimeCounter;
		private long calcTimeSummarize;
		
		/**
		 * Instantiates a new network calculation executer.
		 * @param threadName the thread name
		 */
		public NetworkCalculationExecuter(TimeModelContinuous timeModel, long calculationInterval, String threadName) {
			this.timeModel = timeModel;
			this.calculationInterval = calculationInterval;
			this.setName(threadName);
			// --- Set initial time to begin the calculation ------------------
			this.timeForNextCalculation = this.timeModel.getTimeStart() + this.calculationInterval;
			// --- Configure wait time for further system states --------------
			this.defaultWaitTime = (long) (((double)this.defaultWaitTime) / this.timeModel.getAccelerationFactor());
			
		}
		/**
		 * Sets to stop the calculation executer.
		 */
		public void setStopCalculation() {
			this.isExecute = false;
		}
		
		/**
		 * Reset the indicator to wait in front of the network calculation.
		 */
		public void resetWaitTime() {
			this.isResetWaitTime = true;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			
			// --------------------------------------------------------------------------			
			// --- Execute the network calculation for the initial time -----------------
			// --------------------------------------------------------------------------
			getAggregationHandler().runEvaluationUntil(this.timeModel.getTimeStart());

			// --------------------------------------------------------------------------
			// --- Wait until first calculation time or first state transmission --------
			// --------------------------------------------------------------------------
			try {
				synchronized (getNetworkCalculationTrigger()) {
					getNetworkCalculationTrigger().wait(this.getSleepTimeUntilNextCalculation());
				}
			} catch (InterruptedException ie) {
				//ie.printStackTrace();
			}
			
			// --------------------------------------------------------------------------
			// --- Start with the regular cyclic wait phases ----------------------------
			// --------------------------------------------------------------------------
			while (this.isExecute) {
				
				try {
					synchronized (getNetworkCalculationTrigger()) {
						// --- Shortly wait for further state signals -------------------
						getNetworkCalculationTrigger().wait(this.defaultWaitTime);
						if (this.isResetWaitTime==true) {
							// --- Invert recalculation indicator ----------------------- 
							this.isResetWaitTime = false;
						} else {
							// --- Get the HashTable of new TSSE's from the agents ------
							Hashtable<AID, Object> newTSSEs = null;
							synchronized (getEnvironmentNotificationReminder()) {
								newTSSEs = new Hashtable<>(getEnvironmentNotificationReminder());
								getEnvironmentNotificationReminder().clear();
							}
							// --- Execute network calculation --------------------------
							this.executeNetworkCalculation(newTSSEs);
							// --- Wait until next regular calculation interval --------- 
							getNetworkCalculationTrigger().wait(this.getSleepTimeUntilNextCalculation());
						}
					}
					
				} catch (InterruptedException ie) {
//					ie.printStackTrace();
				}
				
			} // end while
		}
		
		/**
		 * Executes the network calculation and the visualization updates.
		 * @param newTSSEs the new TSSE's from the agents
		 */
		private void executeNetworkCalculation(Hashtable<AID, Object> newTSSEs) {
			
			boolean isDebugCalculateAverageCalculationTime = false;
			
			long simulationTime = SimulationManager.this.getTime();
			long netCalcTime = simulationTime - (simulationTime % 1000); // --- Calculate at exact seconds ---
			long netCalcStartTime = System.nanoTime();

			if (SimulationManager.this.debug==true) {
				SimulationManager.this.debugPrintLine(null, "");
				SimulationManager.this.debugPrintLine(simulationTime, "(Current simulation time) => Execute network calculation for " + newTSSEs.size() + " new system states!");
				SimulationManager.this.debugPrintLine(simulationTime, "=> " + (simulationTime-this.timeForNextCalculation) + " ms between regular calculation execution and current execution.");
				SimulationManager.this.debugPrintLine(this.timeForNextCalculation, "(Current NetCalc target time) => The target time for the regular execution of the network calculation");
				SimulationManager.this.debugPrintLine(netCalcTime, "(Time used for the network calculation");
			}

			// --- (Re)Execute the network calculation ------------------------
			SimulationManager.this.getAggregationHandler().runEvaluationUntil(netCalcTime); 
			
			if (SimulationManager.this.debug==true || isDebugCalculateAverageCalculationTime==true) {
				// --- Calculate average --------------------------------------
				long calcTime = TimeUnit.MILLISECONDS.convert((System.nanoTime() - netCalcStartTime), TimeUnit.NANOSECONDS);
				this.calcTimeCounter++;
				this.calcTimeSummarize +=calcTime;
				int calcTimeAverage = (int) Math.round((double)this.calcTimeSummarize / (double)this.calcTimeCounter);
				System.out.println("=> Finalized evaluation / network calculation in " + calcTime + " ms - Average: " + calcTimeAverage + " ms");
			}
			
		}
	
		/**
		 * Gets the sleep time until next calculation.
		 * @return the sleep time until next calculation
		 */
		private long getSleepTimeUntilNextCalculation() {

			// --- Set the time for the next calculation ------------
			while (this.timeForNextCalculation<=this.timeModel.getTime()) {
				this.timeForNextCalculation += this.calculationInterval;
			}
			
			// --- Exit if next calculation time is out of range ---- 
			if (this.timeForNextCalculation>this.timeModel.getTimeStop()) {
				this.isExecute = false;
				return 1;
			}
			
			// --- Determine the time to sleep for the thread -------
			long timeToSleep = 0;
			if (this.timeModel!=null & this.timeModel.getAccelerationFactor()!=1.0) {
				// --- Consider the acceleration of the time model --
				timeToSleep = ((long) (((double) (this.timeForNextCalculation - this.timeModel.getTime())) / this.timeModel.getAccelerationFactor()));
			} else {
				timeToSleep = this.timeForNextCalculation - this.timeModel.getTime();
			}
			return timeToSleep;
		}
	}

}
