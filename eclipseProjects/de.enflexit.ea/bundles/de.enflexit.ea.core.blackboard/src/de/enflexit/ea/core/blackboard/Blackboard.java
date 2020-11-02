package de.enflexit.ea.core.blackboard;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;

/**
 * The Class Blackboard contains the currents data representations of the SimulationManager.<p>
 * 
 * To get information about the current state, please send a {@link BlackboardRequest} to the current  
 * instance of the {@link BlackboardAgent}. Don't try to use or access the blackboard directly, since 
 * in a distributed simulation you will run into synchronization problems.
 * 
 *  @see BlackboardAgent
 *  @see BlackboardRequest
 *  
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class Blackboard {

	public enum BlackboardWorkingThread {
		BlackboardAgent,
		BlackboardListenerThread
	}
	
	public enum BlackboardState {
		Final,
		NotFinal
	}
	
	private long stateTime;
	private NetworkModel networkModel;
	private AbstractAggregationHandler aggregationHandler;
	
	private boolean agentNotificationsEnabled = true;
	private BlackboardState blackboardState;
	
	private Object wakeUpTrigger;
	private FinalizationTrigger finalizationTriggerBlackboardAgent;
	private FinalizationTrigger finalizationTriggerBlackboardListenerThread;
	private boolean doTerminate;
	
	// --- The listener thread for OSGI services ---------- 
	private BlackboardListenerThread listenerServiceThread;
	
	// --- The singleton construct of the Blackboard ------
	private static Blackboard thisInstance;
	private Blackboard() { }
	/**
	 * Return the single instance of the Blackboard.
	 * @return single instance of Blackboard
	 */
	public static Blackboard getInstance() {
		if (thisInstance==null) {
			thisInstance = new Blackboard();
		}
		return thisInstance;
	}
	
	// ------------------------------------------------------------------------
	// --- Methods for the BlackboardListenerThread ---------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the OSGI service listener thread.
	 * @return the listener service thread
	 */
	private BlackboardListenerThread getListenerServiceThread() {
		if (listenerServiceThread==null) {
			listenerServiceThread = new BlackboardListenerThread();
		}
		return listenerServiceThread;
	}
	/**
	 * Reset the local instance of the BlackboardListenerThread.
	 */
	private void resetListenerServiceThread() {
		this.listenerServiceThread = null;
	}
	/**
	 * Starts the blackboard listener service thread that provides the registered OSGI services
	 * with updates of the Blackboard.
	 */
	public void startBlackboardListenerServiceThread() {
		this.doTerminate = false;
		this.getListenerServiceThread().start();
	}
	/**
	 * Stops the blackboard listener service thread that provides the registered OSGI services
	 * with updates of the Blackboard.
	 */
	public void stopBlackboardListenerServiceThread() {
		this.setDoTerminate();
		this.resetListenerServiceThread();
	}

	
	// ------------------------------------------------------------------------
	// --- Methods for synchronized thread control ----------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the wake-up trigger that can be used to wait for a wake-up notification or to restart waiting threads and agents.
	 * @return the notification trigger
	 */
	protected Object getWakeUpTrigger() {
		if (wakeUpTrigger==null) {
			wakeUpTrigger = new Object();
		}
		return wakeUpTrigger;
	}
	/**
	 * Returns the finalization trigger that can be used to wait for the finalization of the {@link BlackboardAgent}.
	 * @return the finalize trigger
	 */
	private FinalizationTrigger getFinalizationTriggerBlackboardAgent() {
		if (finalizationTriggerBlackboardAgent==null) {
			finalizationTriggerBlackboardAgent = new FinalizationTrigger();
		}
		return finalizationTriggerBlackboardAgent;
	}
	/**
	 * Returns the finalization trigger that can be used to wait for the finalization of the {@link BlackboardListenerThread}.
	 * @return the finalize trigger
	 */
	private FinalizationTrigger getFinalizationTriggerBlackboardListenerThread() {
		if (finalizationTriggerBlackboardListenerThread==null) {
			finalizationTriggerBlackboardListenerThread = new FinalizationTrigger();
		}
		return finalizationTriggerBlackboardListenerThread;
	}
	
	/**
	 * Wakes up the working threads of the blackboard (agent and listern thread) to do their jobs.
	 */
	public void wakeUpWorkingThreads() {
		
		this.getFinalizationTriggerBlackboardAgent().setFinalized(false);
		this.getFinalizationTriggerBlackboardListenerThread().setFinalized(false);
		synchronized (this.getWakeUpTrigger()) {
			this.getWakeUpTrigger().notifyAll();
		}
	}
	/**
	 * Waits for the specified blackboard working thread. Should be invoked by external threads only!
	 * @param workingThreadToWaitFor the working thread to wait for. Set <code>null</code> to wait for both threads.
	 */
	public void waitForBlackboardWorkingThread(BlackboardWorkingThread workingThreadToWaitFor) {

		boolean isWaitForBlackboardAgent = (workingThreadToWaitFor==null || workingThreadToWaitFor==BlackboardWorkingThread.BlackboardAgent); 
		boolean isWaitForBlackboardListenerThread = (workingThreadToWaitFor==null || workingThreadToWaitFor==BlackboardWorkingThread.BlackboardListenerThread);
		
		if (isWaitForBlackboardAgent==true) {
			FinalizationTrigger ftAgent = this.getFinalizationTriggerBlackboardAgent();
			synchronized (ftAgent) {
				if (ftAgent.isFinalized()==false) {
					try {
						ftAgent.wait();
					} catch (InterruptedException iEx) {
					}
				}
			}
		}
		
		if (isWaitForBlackboardListenerThread==true) {
			FinalizationTrigger ftThread4Listener = this.getFinalizationTriggerBlackboardListenerThread();
			synchronized (ftThread4Listener) {
				if (ftThread4Listener.isFinalized()==false) {
					try {
						ftThread4Listener.wait();
					} catch (InterruptedException iExe) {
					}
				}
			}	
		}
	}
	/**
	 * Sets the specified blackboard working thread finalized.
	 * @param workingThreadFinalized the new blackboard working thread finalized
	 */
	protected void setBlackboardWorkingThreadFinalized(BlackboardWorkingThread workingThreadFinalized) {
		
		FinalizationTrigger ft = null;
		switch (workingThreadFinalized) {
		case BlackboardAgent:
			ft = this.getFinalizationTriggerBlackboardAgent();
			break;
		case BlackboardListenerThread:
			ft = this.getFinalizationTriggerBlackboardListenerThread();
			break;
		}
		
		synchronized (ft) {
			ft.setFinalized(true);
			ft.notifyAll();
		}
	}

	
	/**
	 * Sets this thread to terminate.
	 */
	public void setDoTerminate() {
		this.doTerminate = true;
		synchronized (this.getWakeUpTrigger()) {
			this.getWakeUpTrigger().notifyAll();
		}
		this.resetBlackboardDataModel();
	}
	/**
	 * Returns if the blackboard is to be terminated.
	 * @return true, if is do terminate
	 */
	public boolean isDoTerminate() {
		return doTerminate;
	}
	
	// ------------------------------------------------------------------------
	// --- From here, blackboard content and management -----------------------
	// ------------------------------------------------------------------------
	/**
	 * Resets the blackboards data model.
	 */
	private void resetBlackboardDataModel() {
		this.setNetworkModel(null);
		this.setAggregationHandler(null);
	}
	
	/**
	 * Sets the state time of the blackboard and its variables.
	 * @param stateTime the new state time
	 */
	public void setStateTime(long stateTime) {
		this.stateTime = stateTime;
	}
	/**
	 * Returns the state time of the blackboard states.
	 * @return the state time
	 */
	public long getStateTime() {
		return stateTime;
	}
	
	/**
	 * Gets the {@link NetworkModel}.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	/**
	 * Sets the current {@link NetworkModel}.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
	/**
	 * Sets the aggregation handler.
	 * @param aggregationHandler the new aggregation handler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Gets the aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, blackboard state (especially for discrete simulations) --
	// ------------------------------------------------------------------------
	/**
	 * Sets the agent notifications enabled or disabled.
	 * @param isEnabled the new agent notifications enabled
	 */
	public void setAgentNotificationsEnabled(boolean isEnabled) {
		this.agentNotificationsEnabled = isEnabled;
	}
	/**
	 * Returns if agent notifications are enabled.
	 * @return true, if is agent notifications enabled
	 */
	public boolean isAgentNotificationsEnabled() {
		return agentNotificationsEnabled;
	}
	
	/**
	 * Sets the blackboard state.
	 * @param blackboardState the new blackboard state
	 */
	public void setBlackboardState(BlackboardState blackboardState) {
		this.blackboardState = blackboardState;
	}
	/**
	 * Returns the blackboard state. This especially is used in discrete simulations and for cases where 
	 * the simulation is still in a discrete iterating state. In case that blackboard listener like to use 
	 * (e.g. save) the blackboard data, this state helps to decide when exactly the data can be used.  
	 *    
	 * @return the blackboard state
	 */
	public BlackboardState getBlackboardState() {
		if (blackboardState==null) {
			blackboardState = BlackboardState.Final;
		}
		return blackboardState;
	}
	
	/**
	 * Serves as trigger object for waiting threads 
	 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
	 */
	private class FinalizationTrigger {
		
		boolean finalized = true;
		
		public boolean isFinalized() {
			return finalized;
		}
		public void setFinalized(boolean finalized) {
			this.finalized = finalized;
		}
	}
}
