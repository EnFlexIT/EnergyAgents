package de.enflexit.ea.core.dataModel.blackboard;

import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import energy.optionModel.TechnicalSystemState;

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

	private Object notificationTrigger;
	private boolean doTerminate;
	
	private long stateTime;
	private NetworkModel networkModel;
	
	private HashMap<String, DomainBlackboard> domainBlackboards;
	
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

	
	/**
	 * Returns the notification trigger.
	 * @return the notification trigger
	 */
	public Object getNotificationTrigger() {
		if (notificationTrigger==null) {
			notificationTrigger = new Object();
		}
		return notificationTrigger;
	}
	/**
	 * Sets this thread to terminate.
	 */
	public void setDoTerminate() {
		this.doTerminate = true;
		synchronized (this.getNotificationTrigger()) {
			this.getNotificationTrigger().notifyAll();
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
	// --- From here, domain blackboard handling ------------------------------
	// ------------------------------------------------------------------------
	
	private HashMap<String, DomainBlackboard> getDomainBlackboards() {
		if (domainBlackboards==null) {
			domainBlackboards = new HashMap<>();
		}
		return domainBlackboards;
	}
	
	/**
	 * Gets the domain blackboard for the specified domain
	 * @param domain the domain
	 * @return the domain blackboard
	 */
	public DomainBlackboard getDomainBlackboard(String domain) {
		DomainBlackboard domainBlackboard = this.getDomainBlackboards().get(domain);
		if (domainBlackboard==null) {
			//TODO implement domain blackboard initialization
		}
		return domainBlackboard;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, blackboard content and management -----------------------
	// ------------------------------------------------------------------------
	/**
	 * Resets the blackboards data model.
	 */
	private void resetBlackboardDataModel() {
		this.setNetworkModel(null);
		Vector<DomainBlackboard> subBlackboards = new Vector<>(this.getDomainBlackboards().values());
		for (int i=0; i<subBlackboards.size(); i++) {
			subBlackboards.get(i).resetBlackboardDataModel();
		}
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
	
}
