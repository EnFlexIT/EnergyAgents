package de.enflexit.ea.core.aggregation;

import java.util.HashMap;

import de.enflexit.common.performance.PerformanceMeasurements;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class NetworkAggregationTaskThread is used to execute the calculation in a network calculation strategy 
 * for a single single sub system (which corresponds to one SubNetworkConfiguration) or for the creation of
 * visualization updates.
 */
public class NetworkAggregationTaskThread extends Thread {

	public enum NetworkAggregationTask {
		DoNothing,
		ExecuteNetworkCalculation,
		CreateDisplayUpdates
	}
	
	private AbstractAggregationHandler aggregationHandler;
	private AbstractSubNetworkConfiguration subNetConfig;
	
	private NetworkAggregationTask currentJob;
	
	// --- Variables for the execution of the network calculations -- 
	private long evaluationStepEndTime;
	private boolean rebuildDecisionGraph;
	
	// --- Variables for the display update -------------------------
	private HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates;
	private long displayTime;
	
	private boolean doTerminate;
	
	
	/**
	 * Instantiates a new network calculation thread.
	 *
	 * @param aggregationHandler the aggregation handler
	 * @param subNetConfig the actual AbstractSubNetworkConfiguration
	 * @param executerThreadName the executer thread name
	 */
	public NetworkAggregationTaskThread(AbstractAggregationHandler aggregationHandler, AbstractSubNetworkConfiguration subNetConfig, String executerThreadName) {
		this.aggregationHandler = aggregationHandler;
		this.subNetConfig = subNetConfig;
		this.setName(executerThreadName + "-SubAggregationTaskThread-" + this.subNetConfig.getID());
		this.registerPerformanceMeasurements();
	}
	/**
	 * Register the performance measurements if configured so in the aggregation handler .
	 */
	private void registerPerformanceMeasurements() {
		PerformanceMeasurements pm = this.aggregationHandler.getPerformanceMeasurements();
		if (pm!=null) {
			String stratExMeasureID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_EXECUTION + this.subNetConfig.getID();
			pm.addPerformanceMeasurement(stratExMeasureID, this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
			String preprocessorID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_PREPROCESSING + this.subNetConfig.getID();
			pm.addPerformanceMeasurement(preprocessorID, this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
			String deltaStepsID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_DELTA_STEPS_CALL + this.subNetConfig.getID();
			pm.addPerformanceMeasurement(deltaStepsID, this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
			String netCalcID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_NETWORK_CALCULATION + this.subNetConfig.getID();
			pm.addPerformanceMeasurement(netCalcID, this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
			String flowSumID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_FLOW_SUMMARIZATION + this.subNetConfig.getID();
			pm.addPerformanceMeasurement(flowSumID, this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
			String disUpMeasureID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_DISPLAY_UPDATE_EXECUTION + this.subNetConfig.getID();
			pm.addPerformanceMeasurement(disUpMeasureID, this.aggregationHandler.debugGetMaxNumberForPerformanceAverage());
		}
	}
	
	/**
	 * Sets that the thread has nothing to do after an notify arives.
	 */
	public void setDoNothing() {
		this.currentJob = NetworkAggregationTask.DoNothing;
	}

	/**
	 * Executes the evaluation and network calculation until the specified time.
	 *
	 * @param timeUntil the time to evaluate until
	 * @param rebuildDecisionGraph the rebuild decision graph
	 */
	public void runEvaluationUntil(long timeUntil, boolean rebuildDecisionGraph) {
		this.evaluationStepEndTime = timeUntil;
		this.rebuildDecisionGraph = rebuildDecisionGraph;
		this.currentJob = NetworkAggregationTask.ExecuteNetworkCalculation;
	}
	
	/**
	 * Forwards the last system states updates to the display updater.
	 *
	 * @param lastStateUpdates the last state updates
	 * @param displayTime the display time
	 */
	public void updateNetworkModelDisplay(HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates, long displayTime) {
		this.lastStateUpdates = lastStateUpdates;
		this.displayTime = displayTime;
		this.currentJob = NetworkAggregationTask.CreateDisplayUpdates;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		
		while (true) {
			
			// --- Wait for notification on the below HashMap -------
			if (this.currentJob==null) {
				synchronized(this.aggregationHandler.getNetworkAggregationTaskTrigger()) {
					try {
						this.aggregationHandler.setNetworkAggregationTaskThreadDone(this);
						this.aggregationHandler.getNetworkAggregationTaskTrigger().wait();
						
					} catch (InterruptedException iEx) {
						//iEx.printStackTrace();
					}
				}
			}
			
			// --- Terminate this thread ? --------------------------
			if (this.isDoTerminate()==true) break;
			
			if (this.currentJob==null) continue;
			
			switch (this.currentJob) {
			case ExecuteNetworkCalculation:
				// --- Execute the evaluation -----------------------
				AbstractNetworkCalculationStrategy netCalcStrategy = this.subNetConfig.getNetworkCalculationStrategy();
				if (netCalcStrategy!=null) {
					String stratExMeasureID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_EXECUTION + this.subNetConfig.getID();
					this.aggregationHandler.setPerformanceMeasurementStarted(stratExMeasureID);
					netCalcStrategy.runEvaluationUntil(this.evaluationStepEndTime, this.rebuildDecisionGraph);
					this.aggregationHandler.setPerformanceMeasurementFinalized(stratExMeasureID);
					// --- Update the sub blackboard model ---------- 
					netCalcStrategy.updateSubBlackboardModel();
				}
				this.currentJob = null;
				break;

			case CreateDisplayUpdates:
				AbstractNetworkModelDisplayUpdater displayUpdater = this.subNetConfig.getNetworkDisplayUpdater();
				if (displayUpdater!=null) {
					String disUpMeasureID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_DISPLAY_UPDATE_EXECUTION + this.subNetConfig.getID();
					this.aggregationHandler.setPerformanceMeasurementStarted(disUpMeasureID);
					displayUpdater.updateNetworkModelDisplay(this.lastStateUpdates, this.displayTime);
					this.aggregationHandler.setPerformanceMeasurementFinalized(disUpMeasureID);
				}
				this.currentJob = null;
				break;
				
			case DoNothing:
				this.currentJob = null;
				break;
			}
			
			// --- Terminate this thread ? --------------------------
			if (this.isDoTerminate()==true) break;
			
		}
	}
	
	
	public void setDoTerminate(boolean doTerminate) {
		this.doTerminate = doTerminate;
	}
	public boolean isDoTerminate() {
		return doTerminate;
	}
	
}
