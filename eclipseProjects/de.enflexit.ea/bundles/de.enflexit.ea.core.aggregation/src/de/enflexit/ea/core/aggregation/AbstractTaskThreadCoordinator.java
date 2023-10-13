package de.enflexit.ea.core.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.enflexit.ea.core.aggregation.NetworkAggregationTaskThread.NetworkAggregationTask;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractTaskThreadCoordinator.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractTaskThreadCoordinator extends Thread {

	private AbstractAggregationHandler aggregationHandler;
	private List<String> domainList;
	
	private HashMap<AbstractSubNetworkConfiguration, NetworkAggregationTaskThread> networkAggregationTaskThreadHashMap;
	private Object networkAggregationTaskTrigger;
	private List<NetworkAggregationTaskThread> networkAggregationTaskDoneList;
	private Object localThreadTrigger;
	
	
	// --- Coordination task to be done -----------------------------
	protected NetworkAggregationTask currentJob;
	
	// --- Variables for the execution of the network calculations --
	protected long evaluationStepEndTime;
	protected boolean rebuildDecisionGraph;
	
	// --- Variables for the display update -------------------------
	protected HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates;
	protected long displayTime;
	
	protected boolean isDoTerminate;
	
	
	/**
	 * Sets the aggregation handler.
	 * @param aggregationHandler the current parent aggregation handler
	 * 
	 * @see AbstractAggregationHandler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current parent aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Returns the list of domains for which this coordinator is responsible.
	 * @return the domain list
	 */
	public List<String> getDomainList() {
		if (domainList==null) {
			domainList = new ArrayList<>();
		}
		return domainList;
	}
	/**
	 * Register domains.
	 * @param domainsToRegister the domains to register
	 */
	public void registerDomains(List<String> domainsToRegister) {
		
		if (this.getDomainList().size()==0) {
			this.getDomainList().addAll(domainsToRegister);
		} else {
			for (String newDomain : domainsToRegister) {
				if (this.getDomainList().contains(newDomain)==false) {
					this.getDomainList().add(newDomain);
				}
			}
		}
	}


	/**
	 * Will be called to initialize a coordinator ahead of the actual usage during runtime.
	 */
	public abstract void initialize();
	
	/**
	 * Has to returns the sub network configurations that are under control of the current coordinator.
	 * @return the sub network configuration under control
	 */
	public abstract List<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationsUnderControl();
	
	
	// --------------------------------------------------------------
	// --- From here, the thread control ----------------------------
	// --------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		// --- Set the name of the current thread ------------------- 
		this.setName(this.getClass().getSimpleName());
		
		// --- Start thread loop ------------------------------------
		while (true) {
			
			// --- Wait for notification on the below HashMap -------
			if (this.currentJob==null) {
				synchronized(this.getAggregationHandler().getTaskThreadCoordinatorTrigger()) {
					try {
						this.setTaskThreadIsReady();
						this.getAggregationHandler().getTaskThreadCoordinatorTrigger().wait();
						
					} catch (InterruptedException iEx) {
						//iEx.printStackTrace();
					}
				}
			}
			
			// --- Terminate this thread ? --------------------------
			if (this.isDoTerminate==true) break;
			if (this.currentJob==null) continue;
			
			switch (this.currentJob) {
			case ExecuteNetworkCalculation:
				// --- Execute the evaluation -----------------------
				this.doTaskRunEvaluationUntil(this.evaluationStepEndTime, this.rebuildDecisionGraph);
				this.currentJob = null;
				break;

			case UpdateSubBlackBoardModel:
				this.doTaskUpdateSubBlackboardModel();
				this.currentJob = null;
				break;
				
			case CreateDisplayUpdates:
				// --- Execute display updates ----------------------
				this.doTaskUpdateNetworkModelDisplay(this.lastStateUpdates, this.displayTime);
				this.currentJob = null;
				break;
				
			case DoNothing:
				this.currentJob = null;
				break;
			}
			
			// --- Terminate this thread ? --------------------------
			if (this.isDoTerminate==true) break;
		}
	}
	
	/**
	 * Can be invokedSets the task is ready.
	 */
	protected void setTaskThreadIsReady() {
		this.getAggregationHandler().setTaskThreadCoordinatorsReady(this);
	}
	
	/**
	 * Returns the network aggregation task trigger.
	 * @return the calculation trigger
	 */
	public Object getNetworkAggregationTaskTrigger() {
		if (networkAggregationTaskTrigger==null) {
			networkAggregationTaskTrigger = new Object();
		}
		return networkAggregationTaskTrigger;
	}
	
	/**
	 * Starts and waits for all network aggregation task threads.
	 */
	protected void startAndWaitForNetworkAggregationTaskThreads() {
		// --- Start task threads ----------------------------------- 
		this.startNetworkAggregationTaskThreads();
		// --- Again wait for the end of the jobs -------------------
		this.waitForNetworkAggregationTasksDone();
	}
	/**
	 * (Re-)Start network aggregation task threads.
	 */
	protected void startNetworkAggregationTaskThreads() {
		// --- Clear done-list --------------------------------------
		this.getNetworkAggregationTaskDoneList().clear();
		// --- Notify all waiting task threads ----------------------
		synchronized (this.getNetworkAggregationTaskTrigger()) {
			this.getNetworkAggregationTaskTrigger().notifyAll();
		}
	}
	
	/**
	 * Returns the list of NetworkAggregationTaskThread's that have done their job so far.
	 * @return the network aggregation task done list
	 */
	protected List<NetworkAggregationTaskThread> getNetworkAggregationTaskDoneList() {
		if (networkAggregationTaskDoneList==null) {
			networkAggregationTaskDoneList = new ArrayList<>();
		}
		return networkAggregationTaskDoneList;
	}
	/**
	 * Checks if is the NetworkAggregationTasksthreads are done.
	 * @return true, if is done network aggregation tasks
	 */
	protected boolean isDoneNetworkAggregationTasks() {
		return this.getNetworkAggregationTaskDoneList().size()==this.getNetworkAggregationTaskThreadHashMap().size();
	}
	
	/**
	 * Return the local thread trigger.
	 * @return the local thread trigger
	 */
	protected Object getLocalThreadTrigger() {
		if (localThreadTrigger==null) {
			localThreadTrigger = new Object();
		}
		return localThreadTrigger;
	}
	/**
	 * Waits until the network aggregation tasks are done.
	 */
	protected void waitForNetworkAggregationTasksDone() {
		synchronized (this.getLocalThreadTrigger()) {
			if (this.isDoTerminate==false && this.isDoneNetworkAggregationTasks()==false) {
				try {
					this.getLocalThreadTrigger().wait();
				} catch (InterruptedException iEx) {
					//iEx.printStackTrace();
				}
			}
		}
	}
	/**
	 * Sets the specified NetworkAggregationTaskThread done. If complete, the local thread will be reactivated.
	 * @param taskFinalized the NetworkAggregationTaskThread that was finalized
	 */
	protected synchronized void setNetworkAggregationTaskThreadDone(NetworkAggregationTaskThread taskFinalized) {
		this.getNetworkAggregationTaskDoneList().add(taskFinalized);
		if (this.isDoneNetworkAggregationTasks()==true) {
			synchronized (this.getLocalThreadTrigger()) {
				this.getLocalThreadTrigger().notify();
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, creation and reminding of NetworkAggregationTaskThread --
	// ------------------------------------------------------------------------
	/**
	 * Returns the network aggregation task thread hash map.
	 * @return the network aggregation task thread hash map
	 */
	protected HashMap<AbstractSubNetworkConfiguration, NetworkAggregationTaskThread> getNetworkAggregationTaskThreadHashMap() {
		if (networkAggregationTaskThreadHashMap==null) {
			networkAggregationTaskThreadHashMap = new HashMap<>();
			// --- Create and remind all task threads ---------------
			this.getSubNetworkConfigurationsUnderControl().forEach(subNetConfig -> this.getOrCreateNetworkAggregationTaskThread(subNetConfig));
		}
		return networkAggregationTaskThreadHashMap;
	}
	/**
	 * Returns (or creates) a {@link NetworkAggregationTaskThread} for the specified SubNetworkConfiguration.
	 *
	 * @param subNetConfig the extended {@link AbstractSubNetworkConfiguration}
	 * @return the or create network calculation thread
	 */
	public NetworkAggregationTaskThread getOrCreateNetworkAggregationTaskThread(AbstractSubNetworkConfiguration subNetConfig) {
		
		NetworkAggregationTaskThread netAggTaskThread = this.getNetworkAggregationTaskThreadHashMap().get(subNetConfig);
		if (netAggTaskThread==null) {
			netAggTaskThread = new NetworkAggregationTaskThread(this.getAggregationHandler(), this, subNetConfig, Thread.currentThread().getName());
			netAggTaskThread.start();
			this.getNetworkAggregationTaskThreadHashMap().put(subNetConfig, netAggTaskThread);
		}
		return netAggTaskThread;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, executing the network calculation -----------------------
	// ------------------------------------------------------------------------
	/**
	 * Sets to run the aggregators evaluations or network calculation until the specified time.
	 * The method will return when the calculation is done.
	 *
	 * @param timeUntil the time until the evaluation should be executed
	 * @param rebuildDecisionGraph If true, the decision graph will be rebuilt
	 */
	protected final void runEvaluationUntil(long timeUntil, boolean rebuildDecisionGraph) {
		this.evaluationStepEndTime = timeUntil;
		this.rebuildDecisionGraph = rebuildDecisionGraph;
		this.currentJob = NetworkAggregationTask.ExecuteNetworkCalculation;
	}
	/**
	 * Runs the aggregators evaluations or network calculation until the specified time.
	 * The method will return when the calculation is done.
	 *
	 * @param timeUntil the time until the evaluation should be executed
	 * @param rebuildDecisionGraph If true, the decision graph will be rebuilt
	 */
	protected void doTaskRunEvaluationUntil(long timeUntil, boolean rebuildDecisionGraph) {
	
		for (AbstractSubNetworkConfiguration subNetConfig : this.getSubNetworkConfigurationsUnderControl()) {
			// --- Get corresponding NetworkCalculationStrategy -----
			NetworkAggregationTaskThread taskThread = this.getOrCreateNetworkAggregationTaskThread(subNetConfig); 
			AbstractNetworkCalculationStrategy networkCalculationStrategy = subNetConfig.getNetworkCalculationStrategy();
			if (networkCalculationStrategy!=null) {
				taskThread.runEvaluationUntil(timeUntil, rebuildDecisionGraph, true);
			} else {
				taskThread.setDoNothing();
			}
		}
		this.startAndWaitForNetworkAggregationTaskThreads();
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, call to update the SubBlackboardModel -------------------
	// ------------------------------------------------------------------------
	/**
	 * Set the task thread to update the sub blackboard models.
	 */
	protected final void updateSubBlackboardModel() {
		this.currentJob = NetworkAggregationTask.UpdateSubBlackBoardModel;
	}
	/**
	 * Does the actual task to update the SubBlackboardModel. Overwrite this method to customize the calls.
	 * The method will return when the job is done.<br>
	 * Overwrite this method to customize the 
	 *
	 * @param lastStateUpdates the last state updates
	 * @param displayTime the display time
	 */
	protected void doTaskUpdateSubBlackboardModel() {
		
		// --- Assign actual job to task threads --------------------------
		for (AbstractSubNetworkConfiguration subNetConfig : this.getSubNetworkConfigurationsUnderControl()) {
			// --- Get corresponding DisplayUpdater -----------------------
			NetworkAggregationTaskThread taskThread = this.getOrCreateNetworkAggregationTaskThread(subNetConfig); 
			AbstractNetworkCalculationStrategy networkCalculationStrategy = subNetConfig.getNetworkCalculationStrategy();
			if (networkCalculationStrategy!=null) {
				taskThread.updateSubBlackboardModel();
			} else {
				taskThread.setDoNothing();
			}
		}
		this.startAndWaitForNetworkAggregationTaskThreads();
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, the display update job ----------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Set the task thread to update network model display.
	 *
	 * @param lastStateUpdates the last state updates
	 * @param displayTime the display time
	 */
	protected final void updateNetworkModelDisplay(HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates, long displayTime) {
		this.lastStateUpdates = lastStateUpdates;
		this.displayTime = displayTime;
		this.currentJob = NetworkAggregationTask.CreateDisplayUpdates;
	}
	/**
	 * Does the actual task to update network model display. Overwrite this method to customize the calls.
	 * The method will return when the calculation is done.<br>
	 * Overwrite this method to customize the 
	 *
	 * @param lastStateUpdates the last state updates
	 * @param displayTime the display time
	 */
	protected void doTaskUpdateNetworkModelDisplay(HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates, long displayTime) {
		
		// --- Assign actual job to task threads --------------------------
		for (AbstractSubNetworkConfiguration subNetConfig : this.getSubNetworkConfigurationsUnderControl()) {
			// --- Get corresponding DisplayUpdater -----------------------
			NetworkAggregationTaskThread taskThread = this.getOrCreateNetworkAggregationTaskThread(subNetConfig); 
			AbstractNetworkModelDisplayUpdater displayUpdater = subNetConfig.getNetworkDisplayUpdater();
			if (displayUpdater!=null) {
				taskThread.updateNetworkModelDisplay(lastStateUpdates, displayTime);
			} else {
				taskThread.setDoNothing();
			}
		}
		this.startAndWaitForNetworkAggregationTaskThreads();
	}
	
	/**
	 * Sets that the thread has nothing to do after an notify arrives.
	 */
	public void setDoNothing() {
		this.currentJob = NetworkAggregationTask.DoNothing;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, termination of all task threads -------------------------
	// ------------------------------------------------------------------------
	/**
	 * Terminate.
	 *
	 * @return the object
	 */
	public void terminate() {
		this.isDoTerminate = true;
		this.getNetworkAggregationTaskThreadHashMap().values().forEach(naTaskThread -> naTaskThread.terminate());
		synchronized (this.getNetworkAggregationTaskTrigger()) {
			this.getNetworkAggregationTaskTrigger().notifyAll();
		}
	}
	
}
