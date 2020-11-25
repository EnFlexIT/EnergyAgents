package de.enflexit.ea.core.simulation.decisionControl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.SnapshotDecisionLocation;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStepCentralDecision;
import de.enflexit.ea.core.simulation.manager.AggregationHandler;
import de.enflexit.ea.core.simulation.manager.SimulationManager;
import energy.helper.DisplayHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.AID;

/**
 * The Class AbstractCentralDecisionProcess.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractCentralDecisionProcess {

	private AggregationHandler aggregationHandler;

	private TreeMap<String, Vector<TechnicalSystemStateEvaluation>> systemsVariability;
	private HashSet<String> environmentDependentSystems;
	
	private long evaluationTime;
	private long evaluationTimePrevious;
	private boolean newEvaluationTime;
	
	
	/**
	 * Constructor for central decision process.
	 */
	public AbstractCentralDecisionProcess() { }
	
	
	/**
	 * Sets the aggregation handler.
	 * @param aggregationHandler the new aggregation handler
	 */
	public void setAggregationHandler(AggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current aggregation handler.
	 * @return the aggregation handler
	 */
	public AggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Return the TreeMap with the involved systems variability.
	 * @return the systems variability
	 */
	public TreeMap<String, Vector<TechnicalSystemStateEvaluation>> getSystemsVariability() {
		if (systemsVariability==null) {
			systemsVariability = new TreeMap<String, Vector<TechnicalSystemStateEvaluation>>();
		}
		return systemsVariability;
	}
	
	/**
	 * Returns the IDs of the environment dependent systems.
	 * @return the environment dependent systems
	 */
	protected HashSet<String> getEnvironmentDependentSystems() {
		if (environmentDependentSystems==null) {
			environmentDependentSystems = new HashSet<String>();
		}
		return environmentDependentSystems;
	}
	/**
	 * Registers the possible simulation steps that are provided by the specified simulation step
	 * for central decisions.
	 *
	 * @param dsStepCD the DiscreteSimulationStepCentralDecision
	 */
	public void registerDiscreteSimulationStepCentralDecision(String networkComponentID, DiscreteSimulationStepCentralDecision dsStepCD) {
		
		if (dsStepCD==null) return;
		
		// --- Add to state variability -------------------------------------------------------------------------------
		if (dsStepCD.getPossibleSystemStates()!=null) {
			this.getSystemsVariability().put(networkComponentID, dsStepCD.getPossibleSystemStates());
		}
		// --- Remind systems that dependent on the EnvironmentModel (and Blackboard) for local measurements ----------
		if (dsStepCD.requiresEnvironmentModelInformation()==true) {
			this.getEnvironmentDependentSystems().add(networkComponentID);
		}
	}
	/**
	 * Resets the systems variability TreeMap after the decision process.
	 * @param discreteSystemStateType the discrete system state type
	 */
	private void resetSystemsVariability(DiscreteSystemStateType discreteSystemStateType) {
		
		switch (discreteSystemStateType) {
		case Final:
			this.getSystemsVariability().clear();
			break;

		case Iteration:
			for (String netCompID : this.getEnvironmentDependentSystems()) {
				this.getSystemsVariability().remove(netCompID);
			}
			break;
		}
	}
	
	/**
	 * Returns the current evaluation time.
	 * @return the evaluation time
	 */
	public long getEvaluationTime() {
		return evaluationTime;
	}
	/**
	 * Sets the current evaluation time.
	 * @param evaluationTime the new evaluation time
	 */
	public void setEvaluationTime(long evaluationTime) {
		if (evaluationTime!=this.evaluationTimePrevious) {
			this.newEvaluationTime = true;
		} else {
			this.newEvaluationTime = false;
		}
		this.evaluationTime = evaluationTime;
		this.evaluationTimePrevious = evaluationTime;
	}
	/**
	 * Returns if the current evaluation time was newly set for the simulation step.
	 * @return true, if is new evaluation time
	 */
	public boolean isNewEvaluationTime() {
		return newEvaluationTime;
	}
	
	
	/**
	 * Executes the central decision process and sends the system states 
	 * that are to be used by the local systems to the systems.
	 *
	 * @param siMa the current Simulation Manager
	 */
	public final void execute(SimulationManager siMa) {
		
		// --- Set the current simulation time ------------------------------------------
		this.setEvaluationTime(siMa.getTime());
		
		try {
			
			// --- Get decisions from sub-class implementation --------------------------
			TimeStepDecisions tsd = this.getTimeStepDecisions(this.getEvaluationTime());
			
			// --- Check if the decision are complete -----------------------------------
			String decisionErrMsg = this.getDecisionErrorMessage(tsd);
			if (decisionErrMsg!=null) {
				DisplayHelper.systemOutPrintlnGlobalTime(this.getEvaluationTime(), "[" + this.getClass().getSimpleName() + "] Decision errors for", "\n" + decisionErrMsg, true);
			}
			
			// --- Transfer to simulation process ---------------------------------------
			if (tsd!=null && tsd.getSystemStates().size()>0) {
				// --- Get each single decision -----------------------------------------
				List<String> netCompIDs = new ArrayList<String>(tsd.getSystemStates().keySet());
				for (int i = 0; i < netCompIDs.size(); i++) {
					String netCompID = netCompIDs.get(i);
					TechnicalSystemStateEvaluation tsse = tsd.getSystemStates().get(netCompID);
					if (tsse!=null) {
						// --- Define the DiscreteSimulationStep for the system --------- 
						DiscreteSimulationStep dsStep = new DiscreteSimulationStep(tsse, tsd.getDiscreteSystemStateType());
						// --- Add states into aggregations handler ScheduleController --
						this.getAggregationHandler().appendToNetworkComponentsScheduleController(netCompID, dsStep);
						// --- Send TSSE's to the local agents ControlBehaviourRT -------
						siMa.sendAgentNotification(new AID(netCompID, AID.ISLOCALNAME), dsStep);
					}
				}
			}
			
			// --- Clear TreeMap with the systems variability ---------------------------
			this.resetSystemsVariability(tsd.getDiscreteSystemStateType());
			
		} catch (Exception ex) {
			DisplayHelper.systemOutPrintlnGlobalTime(this.getEvaluationTime(), "[" + this.getClass().getSimpleName() + "] Error while executing central decision process for", ":", true);
			ex.printStackTrace();
		}
	}

	/**
	 * Has to return the control decisions for each variable system in the current time step.
	 *
	 * @return the time step decisions
	 * @see #getSystemsVariability()
	 * @see #getEvaluationTime()
	 */
	public abstract TimeStepDecisions getTimeStepDecisions(long evaluationTime);
	
	/**
	 * Returns an error message as string in case of inhomogeneous decisions, otherwise <code>null</code>.
	 * Thus, this method can be used to individually check for errors in the decision process.
	 *
	 * @param tsd the TimeStepDecisions to check
	 * @return the missing message
	 */
	protected String getDecisionErrorMessage(TimeStepDecisions tsd) {
		
		String errorMsg = null;
		
		// --- Compare number of decisions and systems variability ------------
		if (tsd.getSystemStates().size()!=this.getSystemsVariability().size()) {
			errorMsg = this.addError(errorMsg, "The number of control decisions is unequal to the number of systems that provide variability (" + tsd.getSystemStates().size() + " decisions for " + this.getSystemsVariability().size() + " systems)!");
		}
		
		// --- Check for wrong or missing decisions ---------------------------
		List<String> netCompIDsVariable = new ArrayList<String>(this.getSystemsVariability().keySet());
		for (int i = 0; i < netCompIDsVariable.size(); i++) {
			String netCompID = netCompIDsVariable.get(i);
			TechnicalSystemStateEvaluation tsse = tsd.getSystemStates().get(netCompID);
			if (tsse==null) {
				errorMsg = this.addError(errorMsg, "No decision could be found for NetowrkComponent '" + netCompID + "'!");
			} else if (tsse.getGlobalTime()!=this.getEvaluationTime()) {
				errorMsg = this.addError(errorMsg, "The time stamp for the system state of NetowrkComponent '" + netCompID + "' is not equal to the current evaluation time!");
			}
		}
		return errorMsg;
	}
	/**
	 * Adds the specified error extension to the current err message.
	 *
	 * @param errMessage the err message
	 * @param errMessageExtension the err message extension
	 * @return the string
	 */
	private String addError(String errMessage, String errMessageExtension) {
		
		String separator = "\n";
		if (errMessage==null || errMessage.isEmpty()==true) {
			errMessage = errMessageExtension;
		} else {
			errMessage += separator + errMessageExtension; 
		}
		return errMessage;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, static help methods central decisions are located -----------------
	// ----------------------------------------------------------------------------------
	/**
	 * Checks if the specified HyGridAbstractEnvironmentModel specifies a central controlled snapshot simulation.
	 *
	 * @param hyGridAbsEnvModel the {@link HyGridAbstractEnvironmentModel} to check
	 * @return true, if is central controlled snapshot simulation
	 */
	public static boolean isCentralControlledSnapshotSimulation(HyGridAbstractEnvironmentModel hyGridAbsEnvModel) {
		
		boolean centralControlledSnapshotSimulation = false;
		if (hyGridAbsEnvModel!=null) {
			boolean isSnapshot = hyGridAbsEnvModel.isDiscreteSnapshotSimulation();
			boolean isSnapshotCentral = hyGridAbsEnvModel.getSnapshotDecisionLocation()==SnapshotDecisionLocation.Central;
			boolean isSnapshotCentralClassAvailable = hyGridAbsEnvModel.getSnapshotCentralDecisionClass()!=null;
			centralControlledSnapshotSimulation = isSnapshot && isSnapshotCentral && isSnapshotCentralClassAvailable;
		} else {
			centralControlledSnapshotSimulation = false;
		}
		return centralControlledSnapshotSimulation;
	}
	
	/**
	 * Creates the specified central decision process.
	 *
	 * @param decisionProcessClassName the decision process class name
	 * @return the abstract central decision process
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static AbstractCentralDecisionProcess createCentralDecisionProcess(String decisionProcessClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		AbstractCentralDecisionProcess cdp = null;
		if (decisionProcessClassName!=null && decisionProcessClassName.isEmpty()==false) {
			// --- Initiate this class --------------------------				
			cdp = (AbstractCentralDecisionProcess) BaseClassLoadServiceUtility.newInstance(decisionProcessClassName);
		}
		return cdp;
	}

	
}
