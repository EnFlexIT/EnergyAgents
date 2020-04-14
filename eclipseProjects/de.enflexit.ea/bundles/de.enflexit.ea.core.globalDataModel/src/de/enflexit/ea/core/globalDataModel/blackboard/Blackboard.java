package de.enflexit.ea.core.globalDataModel.blackboard;

import java.util.HashMap;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.globalDataModel.ontology.CableState;
import de.enflexit.ea.core.globalDataModel.ontology.ElectricalNodeState;
import energy.optionModel.TechnicalSystemState;

/**
 * The Class Blackboard contains the currents data representations of the SimulationManager.
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
	
	private NetworkModel networkModel;
	
	private HashMap<String, ElectricalNodeState> graphNodeStates;
	private HashMap<String, CableState> networkComponentStates;
	private HashMap<String, TechnicalSystemState> transformerStates;	
	
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
