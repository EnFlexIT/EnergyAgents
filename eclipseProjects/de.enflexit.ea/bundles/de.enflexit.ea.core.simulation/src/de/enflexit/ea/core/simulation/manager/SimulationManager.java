package de.enflexit.ea.core.simulation.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
import agentgui.simulationService.time.TimeModelDateBased;
import agentgui.simulationService.time.TimeModelDiscrete;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.common.performance.PerformanceMeasurements;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.AggregationListener;
import de.enflexit.ea.core.blackboard.Blackboard;
import de.enflexit.ea.core.blackboard.Blackboard.BlackboardState;
import de.enflexit.ea.core.blackboard.BlackboardAgent;
import de.enflexit.ea.core.dashboard.DashboardSubscriptionResponder;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation;
import de.enflexit.ea.core.dataModel.simulation.ControlBehaviourRTStateUpdate;
import de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorRegistration;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep;
import de.enflexit.ea.core.dataModel.simulation.RTControlRegistration;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.helper.DisplayHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.Schedule;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;
import energy.schedule.ScheduleNotification;
import energy.schedule.ScheduleNotification.Reason;
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

	private boolean isDebugDiscreteSimulationSchedule = false;
	
	private boolean isDoPerformanceMeasurements;
	private static final String SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP  = "1 SimMa: Discrete-Round-Trip-Complete";
	private static final String SIMA_MEASUREMENT_NETWORK_CALCULATIONS = "2 SimMa: - Aggregation-Execution     ";
	
	private HyGridAbstractEnvironmentModel hygridSettings;

	private boolean isHeadlessOperation;
	private boolean showDashboard;
	private boolean isPaused;

	private Integer numberOfExecutedDeviceAgents;
	private Integer averageOfAgentAnswersExpected;
	private HashSet<String> agentsInitialized;
	private HashSet<String> agentsSuccessfulStarted;

	private Vector<EnvironmentNotification> agentsNotifications;
	
	private long endTimeNextSimulationStep;
	
	private AggregationHandler aggregationHandler;
	private Blackboard blackboard;
	
	private Hashtable<AID, Object> environmentNotificationReminder;
	private NetworkCalculationExecuter networkCalculationExecuter;
	private Object networkCalculationTrigger;
	
	private HashSet<String> controlBehaviourRTStateUpdateSources;
	private HashSet<String> controlBehaviourRTStateUpdateAnswered;
	
	private long statSimulationStartTime;
	private long statSimulationEndTime;
	private int statSimulationStepsDiscrete;
	
	private DashboardSubscriptionResponder dashboardSubscriptionResponder;
	
	
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
			
			// --- SimpleBoolean for dashboard configuration ------------------
			if (args.length>1 && args[1] instanceof Simple_Boolean) {
				this.setShowDashboard(((Simple_Boolean)args[1]).getBooleanValue());
			}
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
		this.getBlackboard().setAggregationHandler(this.getAggregationHandler());
		// --- If measurements are activated, configure aggregation handler ---
		this.registerPerformanceMeasurements();
		// --- Add the managers internal cyclic simulation behaviour ----------
		this.addSimulationBehaviour();
		
		// --- Start the dashboard responder if configured --------------------
		if (this.showDashboard==true) {
			this.addBehaviour(this.getDashboardSubscriptionResponder());
		}
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
		if (doPause==true) {
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
				this.doSingleSimulationSequence();
			} else if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous) {
				// --- Restart the continuous simulation ------------
				this.getTimeModelContinuous().setExecuted(true);
			}
		}
	}
	
	/**
	 * Prints the specified text to the console window.
	 * @param messageText the text that has to be printed to the console
	 * @param isError set true, if is an error
	 */
	public void print(String messageText, boolean isError) {
		if (isError) {
			System.err.println("[" + this.getClass().getSimpleName() + "] " + messageText);
		} else {
			System.out.println("[" + this.getClass().getSimpleName() + "] " + messageText);
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
	 * Checks if is show dashboard.
	 * @return true, if is show dashboard
	 */
	public boolean isShowDashboard() {
		return showDashboard;
	}
	/**
	 * Sets the show dashboard.
	 * @param showDashboard the new show dashboard
	 */
	public void setShowDashboard(boolean showDashboard) {
		this.showDashboard = showDashboard;
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
	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#getTimeModel()
	 */
	@Override
	public TimeModelDateBased getTimeModel() {
		return (TimeModelDateBased) super.getTimeModel();
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
	
	// --------------------------------------------------------------------------------------------
	// --- For discrete simulations to realize constant time steps of the simulator ---------------
	// --------------------------------------------------------------------------------------------
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
	
	// --------------------------------------------------------------------------------------------
	// --- Blackboard handling --------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
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
	
	// --------------------------------------------------------------------------------------------
	// --- Aggregation handling & simulation schedule ---------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the aggregation handler of the simulation manager.
	 * @return the aggregation handler
	 */
	protected AggregationHandler getAggregationHandler() {
		if (aggregationHandler==null) {
			aggregationHandler = new AggregationHandler(this.getEnvironmentModel(), this.isHeadlessOperation, this.getClass().getSimpleName());
			aggregationHandler.setOwnerInstance(this);
			aggregationHandler.addAggregationListener(this);
		}
		return aggregationHandler;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AggregationListener#networkCalculationDone()
	 */
	@Override
	public void networkCalculationDone() {

		// --------------------------------------------------------------------
		// --- Blackboard-Jobs ------------------------------------------------
		// --------------------------------------------------------------------
		// --- Set state time to the blackboard -------------------------------
		this.getBlackboard().setStateTime(this.getAggregationHandler().getEvaluationEndTime());
		
		// --- Set the blackboard state ---------------------------------------
		BlackboardState bBoardSate = null;
		boolean isDiscreteSimulation = this.getHyGridAbstractEnvironmentModel().getTimeModelType()==TimeModelType.TimeModelDiscrete;
		boolean isPendingSysteminSimulationStep = this.getAggregationHandler().isPendingIteratingSystemInSimulationStep();
		if (isDiscreteSimulation==true && isPendingSysteminSimulationStep==true) {
			bBoardSate = BlackboardState.NotFinal;
		} else {
			bBoardSate = BlackboardState.Final;
		}
		this.getBlackboard().setBlackboardState(bBoardSate);
		
		// --- Notify blackboard listeners about the new results --------------
		this.getBlackboard().wakeUpWorkingThreads();
		
		// --- Wait for the Blackboard jobs to be done ------------------------
		this.getBlackboard().waitForBlackboardWorkingThread(null);
	}
	
	/**
	 * Returns the dashboard subscription responder.
	 * @return the dashboard subscription responder
	 */
	private DashboardSubscriptionResponder getDashboardSubscriptionResponder() {
		if (dashboardSubscriptionResponder==null) {
			dashboardSubscriptionResponder = new DashboardSubscriptionResponder(this, this.getAggregationHandler());
		}
		return dashboardSubscriptionResponder;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- Handling of ControlBehaviourRTStateUpdate's --------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the currently known sources of ControlBehaviourRTStateUpdate's.
	 * @return the control behaviour RT state updates sources
	 */
	public HashSet<String> getControlBehaviourRTStateUpdateSources() {
		if (controlBehaviourRTStateUpdateSources==null) {
			controlBehaviourRTStateUpdateSources = new HashSet<String>();
		}
		return controlBehaviourRTStateUpdateSources;
	}
	/**
	 * Returns the system ID's of the systems that have provided an ControlBehaviourRTStateUpdate per discrete simulation step.
	 * @return the control behaviour RT state updates answered
	 */
	public HashSet<String> getControlBehaviourRTStateUpdateAnswered() {
		if (controlBehaviourRTStateUpdateAnswered==null) {
			controlBehaviourRTStateUpdateAnswered = new HashSet<String>();
		}
		return controlBehaviourRTStateUpdateAnswered;
	}
	/**
	 * Checks if there is a pending ControlBehaviourRTStateUpdate.
	 * @return true, if is pending control behaviour RT state update
	 */
	private boolean isPendingControlBehaviourRTStateUpdate() {
		return this.getControlBehaviourRTStateUpdateAnswered().size()!=this.getControlBehaviourRTStateUpdateSources().size();
	}	
	
	
	// --------------------------------------------------------------------------------------------
	// --- Simulation sequence handling (discrete and continuous) ---------------------------------
	// --------------------------------------------------------------------------------------------
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
	/* (non-Javadoc)
	 * @see agentgui.simulationService.agents.SimulationManagerAgent#doSingleSimulationSequence()
	 */
	@Override
	public void doSingleSimulationSequence() {
		
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
					this.statSimulationStartTime = System.currentTimeMillis();
					
					// --- Case separation Time Model --------------------------------------------
					switch (this.hygridSettings.getTimeModelType()) {
					case TimeModelDiscrete: // ---------------------------------------------------
						this.getControlBehaviourRTStateUpdateAnswered().clear();
						this.stepSimulation(this.getNumberOfExpectedDeviceAgents());
						this.statSimulationStepsDiscrete++;
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
						// --- Check if discrete simulation step is done --------------------------
						this.discreteSimulationCheckEndOfSimulationStep(false);
						break;
						
					case TimeModelContinuous:
						simState.setState(STATE.C_StopSimulation);
						this.statSimulationEndTime = System.currentTimeMillis();
						this.print("Finalize Simulation!", false);
						this.getTimeModelContinuous().setExecuted(false);
						this.stepSimulation(this.getNumberOfExpectedDeviceAgents());
						break;
					}
				}
				
			} else if (simState.getState()==STATE.C_StopSimulation) {
				// --------------------------------------------------------------------------------
				// --- Finalize simulation --------------------------------------------------------
				// --------------------------------------------------------------------------------
				this.resetEndTimeNextSimulationStep();
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Check if the end of a discrete simulation step is reached. If so, the next 
	 * discrete simulation step will be initialized.
	 *
	 * @param isUpdatedDiscreteSimulationStep the indicator if a new or updated discrete simulation step was delivered
	 */
	private void discreteSimulationCheckEndOfSimulationStep(boolean isUpdatedDiscreteSimulationStep) {
		
		// --- To avoid reaction in continuous time simulations -------------------------
		if (this.hygridSettings.getTimeModelType()!=TimeModelType.TimeModelDiscrete) return;

		
		boolean isDoNextSimulationStep = true;
		
		AggregationHandler agh = this.getAggregationHandler();
		if (agh.isCentralSnapshotSimulation()==true) {
			// --------------------------------------------------------------------------
			// --- Central decision case ------------------------------------------------
			// --------------------------------------------------------------------------
			// --- Do we expect further DiscreteSimulationStepCentralDecision? ----------
			if (agh.isPendingSystemInCentralSnapshotSimulation()==true) return;
			// --- Execute evaluation in central decision process -----------------------
			if (agh.getCentralDecisionProcess()!=null) {
				agh.getCentralDecisionProcess().execute(this);
			}
		}
		
		// --------------------------------------------------------------------------
		// --- Decentral decision case ----------------------------------------------
		// --------------------------------------------------------------------------
		boolean isPendingSystemInPartSequence = agh.isPendingIteratingSystemInPartSequence();
		boolean isPendingSysteminSimulationStep = agh.isPendingIteratingSystemInSimulationStep();
		boolean isPendingControlBehaviourRTStateUpdate = this.isPendingControlBehaviourRTStateUpdate();
		
		isDoNextSimulationStep = isPendingSystemInPartSequence==false && isPendingSysteminSimulationStep==false && isPendingControlBehaviourRTStateUpdate==false; 
		
		// --------------------------------------------------------------------------
		// --- In case of any discrete iterating system -----------------------------
		// --------------------------------------------------------------------------
		if (isUpdatedDiscreteSimulationStep==true && (agh.isIteratingSystem()==true || agh.isCentralSnapshotSimulation()==true)) {
			
			// --- Do we expect further discrete simulation part steps --------------
			if (isPendingSystemInPartSequence==true) return;
			// --- Reset part step reminder -----------------------------------------
			agh.clearDiscreteIteratingSystemsStateTypeLogFromIterations();
			
			// --- Disable Blackboard notifications? --------------------------------
			if (isPendingSysteminSimulationStep==false) {
				// --- No further Blackboard notifications are required for this (time) step
				this.getBlackboard().setAgentNotificationsEnabled(false);
			}
			// --- Execute Network calculation --------------------------------------
			agh.runEvaluationUntil(this.getTime(), true, this.isDebugDiscreteSimulationSchedule);
			// --- Reset Blackboard notifications! ----------------------------------
			this.getBlackboard().setAgentNotificationsEnabled(true);
		}
		
	

		// ------------------------------------------------------------------------------
		// --- Do next simulation step? -------------------------------------------------
		// ------------------------------------------------------------------------------
		if (isDoNextSimulationStep==true) {
			// --- Preconditions met: Start next simulation step ------------------------
			this.discreteSimulationStartNextSimulationStep();
		}
	}
	/**
	 * Start the next simulation step in discrete simulations.
	 */
	private void discreteSimulationStartNextSimulationStep() {

		try {
			// --- Wait until the end of configured time between simulations steps ------
			this.waitUntilTheEndOfCurrentSimulationStep();

			// --- If simulation is paused, exit here ----------------------------------- 
			if (this.isPaused==true) return;
			
			// --- Prepare next simulation step -----------------------------------------
			TimeModelDiscrete tmd = this.getTimeModelDiscrete();
			if (tmd.getTime()<tmd.getTimeStop()) {
				tmd.step();
				if (this.isDebugDiscreteSimulationSchedule==true) {
					System.out.println("");
					DisplayHelper.systemOutPrintlnGlobalTime(tmd.getTime(), "=> [" + this.getClass().getSimpleName() + "]", "Execute next simulation step ...");
				}
			} else {
				this.hygridSettings.getSimulationStatus().setState(STATE.C_StopSimulation);
				this.statSimulationEndTime = System.currentTimeMillis();
				this.print("Finalize Simulation!", false);
				this.sendDisplayAgentNotification(new EnableNetworkModelUpdateNotification(true));
			}
			
			// --- Clear simulation step logs -------------------------------------------
			this.getAggregationHandler().clearDiscreteIteratingSystemsStateTypeLog();
			this.getControlBehaviourRTStateUpdateAnswered().clear();

			// --- Start next simulation step -------------------------------------------
			this.stepSimulation(this.getNumberOfExpectedDeviceAgents());
			this.statSimulationStepsDiscrete++;
			this.setEndTimeNextSimulationStep();
		
			
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
				STATE simState = this.hygridSettings.getSimulationStatus().getState();
				if (simState==STATE.B_ExecuteSimuation) {
					// --- Debug text -----------------------------------------
					String displayTextNumbers = "(Expected: " + this.getNumberOfExpectedDeviceAgents() + ", Initialized: " + this.getAgentsInitialized().size() + ", Started: " + this.getAgentsSuccessfulStarted().size() + ")!";
					this.debugPrintLine(currTime, "proceedAgentAnswers: Received " + agentAnswers.size() + " system states " + displayTextNumbers + ".");
					// --- Error during simulation? ---------------------------
					if (agentAnswers.size()!=this.getAverageOfAgentAnswersExpected()) {
						this.print("Received " + agentAnswers.size() + " instead of " + this.getAverageOfAgentAnswersExpected() + " expected answers from agents in simulation.", true);
					}
					this.getAggregationHandler().setAgentAnswers(agentAnswers);
				}
				
				// --- Distinguish the time model type ------------------------ 
				switch (this.hygridSettings.getTimeModelType()) {
				case TimeModelDiscrete:
					// --- (Re)Execute the network calculation ----------------
					if (simState==STATE.B_ExecuteSimuation) {
						if (this.isDebugDiscreteSimulationSchedule==true) {
							DisplayHelper.systemOutPrintlnGlobalTime(currTime, "=> [" + this.getClass().getSimpleName() + "]", "Proceed Agent Answers ...");
						}
						this.setPerformanceMeasurementStarted(SIMA_MEASUREMENT_NETWORK_CALCULATIONS);
						this.getAggregationHandler().runEvaluationUntil(currTime, false, this.isDebugDiscreteSimulationSchedule);
						this.setPerformanceMeasurementFinalized(SIMA_MEASUREMENT_NETWORK_CALCULATIONS);
					}
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
		switch (this.hygridSettings.getTimeModelType()) {
		case TimeModelContinuous:
			if (this.hygridSettings.getSimulationStatus().getState()==STATE.B_ExecuteSimuation) {
				this.resetNetworkCalculationExecuterWaitTime();
			}
			break;

		case TimeModelDiscrete:
			this.setPerformanceMeasurementFinalized(SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP);
			this.doNextSimulationStep();
			this.setPerformanceMeasurementStarted(SIMA_MEASUREMENT_DISCRETE_ROUND_TRIP);
			break;
			
		}
		
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
			this.debugPrintLine(this.getTime(), "Number of Energy Agents = " + this.numberOfExecutedDeviceAgents);
		}
		return this.numberOfExecutedDeviceAgents;
	}
	/**
	 * Returns the average of agent answers expected.
	 * @return the average of agent answers expected
	 */
	private int getAverageOfAgentAnswersExpected() {
		if (averageOfAgentAnswersExpected==null) {
			averageOfAgentAnswersExpected = (int) ((this.getNumberOfExpectedDeviceAgents() + this.getAgentsInitialized().size() + this.getAgentsSuccessfulStarted().size()) / 3.0);
		}
		return averageOfAgentAnswersExpected;
	}

	// --------------------------------------------------------------------------------------------
	// --- Handling / counting of involved agents during start ------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the agents that were initialized for the simulation.
	 * @return the agents initialized
	 */
	private HashSet<String> getAgentsInitialized() {
		if (agentsInitialized==null) {
			agentsInitialized = new HashSet<String>();
		}
		return agentsInitialized;
	}
	/**
	 * Return the agents that were successfully started for the simulation.
	 * @return the agents successful started
	 */
	private HashSet<String> getAgentsSuccessfulStarted() {
		if (agentsSuccessfulStarted==null) {
			agentsSuccessfulStarted = new HashSet<String>();
		}
		return agentsSuccessfulStarted;
	}
	
	
	
	// --------------------------------------------------------------------------------------------
	// --- Handling of EnvironmentNotification's --------------------------------------------------
	// --------------------------------------------------------------------------------------------
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
	protected void onManagerNotification(EnvironmentNotification envNote) {
		
		if (envNote==null || envNote.getNotification()==null) return;
		
		if (envNote.getNotification() instanceof NetworkModel) {
			// --- Got an alternative NetworkModel ------------------------------------------------
			NetworkModel networkModelAlternative = (NetworkModel) envNote.getNotification();
			String senderName = envNote.getSender().getLocalName();
			NetworkModel networkModelLocal = (NetworkModel) this.getDisplayEnvironment();
			networkModelLocal.getAlternativeNetworkModel().put(senderName, networkModelAlternative);
			this.sendDisplayAgentNotification(new EnvironmentModelUpdateNotification(this.getEnvironmentModel()));
			return;

		} else if (envNote.getNotification() instanceof RTControlRegistration) {
			this.getAggregationHandler().registerRealTimeControlledSystem(envNote.getSender().getLocalName());
			return;
			
		} else if (envNote.getNotification() instanceof DiscreteIteratorRegistration) {
			this.getAggregationHandler().registerDiscreteIteratingSystem(envNote.getSender().getLocalName());
			return;
		}
		
		// ----------------------------------------------------------------------------------------
		// --- Notification handling depending on simulation status--------------------------------
		// ----------------------------------------------------------------------------------------
		SimulationStatus simState = this.hygridSettings.getSimulationStatus();
		if (simState.getState()==STATE.A_DistributeEnvironmentModel) {
			
			// --- Get the notification content / object ------------------------------------------
			Object noteContent = envNote.getNotification();
			String agentName = envNote.getSender().getLocalName();
			
			// --- Receiving register notification from agents in this simulation -----------------
			this.getAgentNotifications().add(envNote);
			if (noteContent instanceof Schedule) {
				// --- Receiving the Schedule for the energy transmission from agents -------------
				Schedule schedule = (Schedule) envNote.getNotification();
				ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(agentName);
				this.print("Received Schedule from " + agentName, false);
				sc.getScheduleList().getSchedules().add(schedule);
				sc.notifyObservers(new ScheduleNotification(Reason.ScheduleListLoaded, null));
				
			} else if (envNote.getNotification() instanceof ControlBehaviourRTStateUpdate) {
				// --- Got an initial ControlBehaviourRTStateUpdate from a system -----------------
				this.getControlBehaviourRTStateUpdateSources().add(envNote.getSender().getLocalName());
					
			} else if (noteContent instanceof STATE_CONFIRMATION) {
				// --- Check state confirmation type ----------------------------------------------
				this.debugPrintLine(this.getTime(), "Received STATE_CONFIRMATION from agent " + agentName);
				STATE_CONFIRMATION stateConformation = (STATE_CONFIRMATION) noteContent;
				switch (stateConformation) {
				case Initialized:
					this.getAgentsInitialized().add(agentName);
					break;

				case Done:
					this.getAgentsSuccessfulStarted().add(agentName);
					// ----------------------------------------------------------------------------
					// --- In distribute environments some agents may be slower in response !!----- 
					// --- => At least have 95 % of expected agents started ----------------------- 
					// ----------------------------------------------------------------------------
					int minStarted = (int)Math.round(((double)this.getNumberOfExpectedDeviceAgents()) * 0.95);
					if (this.getAgentsSuccessfulStarted().size()>=minStarted &&  this.getAgentsSuccessfulStarted().size()==this.getAgentsInitialized().size()) {
						this.print("Initialization of agents completed (Expected: " + this.getNumberOfExpectedDeviceAgents() + ", Initialized: " + this.getAgentsInitialized().size() + ", Started: " + this.getAgentsSuccessfulStarted().size() + ")!", false);
						this.resetAgentNotifications();
						this.doNextSimulationStep();	
					}
					break;
					
				case Error:
					// --- Nothing to do here yet -----
					break;
				}
			}
			
		} else if (simState.getState()==STATE.B_ExecuteSimuation) {
			
			// ------------------------------------------------------------------------------------
			// --- Received a notification of the type NetworkStateInformation ? ------------------
			// ------------------------------------------------------------------------------------
			if (envNote.getNotification() instanceof NetworkStateInformation) {

				NetworkStateInformation nsInf = (NetworkStateInformation) envNote.getNotification();
				AID senderAID = envNote.getSender();
				
				// --- Find the corresponding sub aggregation -------------------------------------
				List<AbstractSubNetworkConfiguration> subnetConfigList = this.getAggregationHandler().getSubNetworkConfigurations();
				for (int i = 0; i < subnetConfigList.size(); i++) {
					if (subnetConfigList.get(i).onNetworkStateInformation(senderAID, nsInf)==true) {
						//TODO Check if multiple aggregations can depend on the same
						break;
					}
				}
				return;
				
			} else if (envNote.getNotification() instanceof DiscreteSimulationStep) {
				// --- Got a new DiscreteSimulationStep from a system ------------------------------
				this.getAggregationHandler().setAgentAnswer(envNote);
				this.discreteSimulationCheckEndOfSimulationStep(true);
				return;
				
			} else if (envNote.getNotification() instanceof ControlBehaviourRTStateUpdate) {
				// --- Got a ControlBehaviourRTStateUpdate from a system --------------------------
				this.getControlBehaviourRTStateUpdateAnswered().add(envNote.getSender().getLocalName());
				this.getAggregationHandler().setAgentAnswer(envNote);
				this.discreteSimulationCheckEndOfSimulationStep(false);
				return;
			}
			
			// --------------------------------------------------------------------------------
			// --- The possible actions for a continuous time model ---------------------------
			// --------------------------------------------------------------------------------
			if (this.hygridSettings.getTimeModelType()==TimeModelType.TimeModelContinuous) {
				
				if (envNote.getNotification() instanceof TechnicalSystemStateEvaluation) {
					// --- For a continuous time model: Change of a system state received -----
					this.getAggregationHandler().setAgentAnswer(envNote);
					this.debugPrintLine(this.getTime(), "Received new TSSE for " + envNote.getSender().getLocalName() + " !");
					// --- Remind changes for the visualization -------------------------------
					this.getEnvironmentNotificationReminder().put(envNote.getSender(), envNote.getNotification());
					// --- (Re)Wait for further notifications ---------------------------------
					this.resetNetworkCalculationExecuterWaitTime();
					
				} else if (envNote.getNotification().equals(STATE_CONFIRMATION.Done)) {
					// --- Finalize the simulation, since no further input can be expected ----
					this.getAgentNotifications().add(envNote);
					if (this.getAgentNotifications().size()==this.getNumberOfExpectedDeviceAgents()) {
						this.print("Continuous time simulation completed!", false);
						this.resetAgentNotifications();
						this.doNextSimulationStep();
					}
				}
			}
			
		} else if (simState.getState()==STATE.C_StopSimulation & envNote.getNotification().equals(STATE_CONFIRMATION.Done)) {
			// --- Receiving done notification from agents in this simulation ---------------------
			this.getAgentNotifications().add(envNote);
			if (this.getAgentNotifications().size()==this.getNumberOfExpectedDeviceAgents()) {
				this.print("Finalisation of simulation completed!", false);
				this.printRuntimeStatistics();
				this.resetAgentNotifications();
				this.doNextSimulationStep();
			}
		}
	}
	
	/**
	 * Prints the simulation statistics.
	 */
	private void printRuntimeStatistics() {
		
		long timeMillis = this.statSimulationEndTime - this.statSimulationStartTime; 
		
		double timeSeconds = (double)timeMillis / 1000.0;
		timeSeconds = Math.round(timeSeconds*1000.0) / 1000.0;

		double timeMinutes = ((((double)timeMillis / 1000) / 60.0));
		timeMinutes = Math.round(timeMinutes * 100.0) / 100.0;
		String timeExplanation = timeMillis + " ms, " + timeSeconds + " s, " + timeMinutes + " Min";

		// --- Prepare Text -----------
		String msg = "Total Runtime: " + timeExplanation;
		if (this.statSimulationStepsDiscrete>0) {
			int timePerStep = (int) ((double)timeMillis / (double) this.statSimulationStepsDiscrete);
			msg += " (" + this.statSimulationStepsDiscrete + " discrete simulation steps with " + timePerStep + " ms/step)";
		}
		this.print(msg, false);
	}
	
	// ----------------------------------------------------------------------------------
	// --- The following is used in the context of continuous time simulations ----------
	// ----------------------------------------------------------------------------------
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
