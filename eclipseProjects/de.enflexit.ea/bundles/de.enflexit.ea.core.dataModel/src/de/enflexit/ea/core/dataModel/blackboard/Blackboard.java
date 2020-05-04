package de.enflexit.ea.core.dataModel.blackboard;

import java.util.HashMap;

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
	
	private NetworkModel networkModel;
	
	private HashMap<String, ElectricalNodeState> graphNodeStates;
	private HashMap<String, CableState> networkComponentStates;
	private HashMap<String, TechnicalSystemState> transformerStates;	
	
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
	// --- From here, blackboard content and management -----------------------
	// ------------------------------------------------------------------------
	/**
	 * Resets the blackboards data model.
	 */
	private void resetBlackboardDataModel() {
		this.setNetworkModel(null);
		this.graphNodeStates = null;
		this.networkComponentStates = null;
		this.transformerStates = null;	
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
	 * Gets the graph node states as HashMap.
	 * @return the graph node states
	 */
	public HashMap<String, ElectricalNodeState> getGraphNodeStates() {
		if (graphNodeStates==null) {
			graphNodeStates = new HashMap<String, ElectricalNodeState>();
		}
		return graphNodeStates;
	}
	/**
	 * Gets the network component states as HashMap.
	 * @return the network component states
	 */
	public HashMap<String, CableState> getNetworkComponentStates() {
		if (networkComponentStates==null) {
			networkComponentStates = new HashMap<String, CableState>();
		}
		return networkComponentStates;
	}
	/**
	 * Returns the transformer states of the current network.
	 * @return the transformer states
	 */
	public HashMap<String, TechnicalSystemState> getTransformerStates() {
		if (transformerStates==null) {
			transformerStates = new HashMap<String, TechnicalSystemState>();
		}
		return transformerStates;
	}
	
}
