package de.enflexit.ea.electricity.aggregation;

import java.util.HashMap;

import de.enflexit.ea.core.dataModel.blackboard.DomainBlackboard;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import energy.optionModel.TechnicalSystemState;

/**
 * Domain blackboard for electricity networks
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public class DomainBlackboardElectricity extends DomainBlackboard {
	private HashMap<String, ElectricalNodeState> graphNodeStates;
	private HashMap<String, CableState> networkComponentStates;
	private HashMap<String, TechnicalSystemState> transformerStates;
	
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
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.blackboard.DomainBlackboard#resetBlackboardDataModel()
	 */
	@Override
	protected void resetBlackboardDataModel() {
		this.graphNodeStates = null;
		this.networkComponentStates = null;
		this.transformerStates = null;	
	}
}
