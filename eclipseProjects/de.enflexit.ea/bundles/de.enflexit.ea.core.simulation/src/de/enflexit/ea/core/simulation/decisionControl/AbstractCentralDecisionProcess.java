package de.enflexit.ea.core.simulation.decisionControl;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import de.enflexit.common.classLoadService.BaseClassLoadServiceUtility;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.SnapshotDecisionLocation;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep;
import de.enflexit.ea.core.simulation.manager.AggregationHandler;
import energy.helper.DisplayHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractCentralDecisionProcess.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractCentralDecisionProcess {

	private AggregationHandler aggregationHandler;

	private TreeMap<String, Vector<TechnicalSystemStateEvaluation>> systemsVariability;
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
	 * Executes the central decision process.
	 * @param currTime the current time
	 */
	public final void execute(long currTime) {
		
		this.setEvaluationTime(currTime);
		
		TimeStepDecisions tsd = null;
		try {
			
			// --- Get decisions from sub-class implementation --------------------------
			tsd = this.getTimeStepDecisions(this.getEvaluationTime());
			
			// --- Check if the decision are (in) complete ------------------------------
			String decisionErrMsg = this.getDecisionErrorMessage(tsd);
			if (decisionErrMsg!=null) {
				DisplayHelper.systemOutPrintlnGlobalTime(this.getEvaluationTime(), "[" + this.getClass().getSimpleName() + "] Decision errors for", "\n" + decisionErrMsg, true);
			}
			
			// --- Transfer to simulation process ---------------------------------------
			if (tsd!=null && tsd.getSystemStates().size()>0) {
				// --- Add states to the aggregations handler ScheduleController --------
				List<String> netCompIDs = new ArrayList<String>(tsd.getSystemStates().keySet());
				for (int i = 0; i < netCompIDs.size(); i++) {
					String netCompID = netCompIDs.get(i);
					TechnicalSystemStateEvaluation tsse = tsd.getSystemStates().get(netCompID);
					if (tsse!=null) {
						this.getAggregationHandler().appendToNetworkComponentsScheduleController(netCompID, new DiscreteSimulationStep(tsse, tsd.getDiscreteSystemStateType()));
					}
				}
			}
			
			// --- Send new states to agents ControlBehaviourRT to update local system --
			// TODO
			
			// --- Clear current system variability -------------------------------------
			this.getSystemsVariability().clear();
			
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
