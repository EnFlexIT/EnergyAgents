package de.enflexit.ea.core.aggregation;

import java.util.HashMap;

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
		this.start();
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
			synchronized(this.aggregationHandler.getNetworkAggregationTaskTrigger()) {
				try {
					this.aggregationHandler.getNetworkAggregationTaskDoneList().add(this);
					this.aggregationHandler.getNetworkAggregationTaskTrigger().wait();
					
				} catch (InterruptedException iEx) {
					//iEx.printStackTrace();
				}
			}
			
			// --- Terminate this thread ? --------------------------
			if (this.isDoTerminate()==true) break;
			
			switch (this.currentJob) {
			case ExecuteNetworkCalculation:
				// --- Execute the evaluation -----------------------
				AbstractNetworkCalculationStrategy netClacStrategy = this.subNetConfig.getNetworkCalculationStrategy();
				if (netClacStrategy!=null) {
					netClacStrategy.runEvaluationUntil(this.evaluationStepEndTime, this.rebuildDecisionGraph);
				}
				break;

			case CreateDisplayUpdates:
				AbstractNetworkModelDisplayUpdater displayUpdater = this.subNetConfig.getNetworkDisplayUpdater();
				if (displayUpdater!=null) {
					displayUpdater.updateNetworkModelDisplay(this.lastStateUpdates, this.displayTime);
				}
				break;
				
			default:
				// --- Do Nothing -----
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
