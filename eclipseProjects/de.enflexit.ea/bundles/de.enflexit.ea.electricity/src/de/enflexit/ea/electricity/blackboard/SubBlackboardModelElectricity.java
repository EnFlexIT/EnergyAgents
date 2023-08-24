package de.enflexit.ea.electricity.blackboard;

import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel;
import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import energy.optionModel.TechnicalSystemState;

/**
 * Sub blackboard model for electricity aggregations
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public class SubBlackboardModelElectricity extends AbstractSubBlackboardModel {

	private static final long serialVersionUID = 3592422280428518923L;
	
	private HashMap<String, ElectricalNodeState> nodeStates;
	private HashMap<String, CableState> cableStates;
	private HashMap<String, TechnicalSystemState> transformerStates;
	
	/**
	 * Gets the graph node states as HashMap.
	 * @return the graph node states
	 */
	public HashMap<String, ElectricalNodeState> getNodeStates() {
		if (nodeStates==null) {
			nodeStates = new HashMap<String, ElectricalNodeState>();
		}
		return nodeStates;
	}
	/**
	 * Gets the network component states as HashMap.
	 * @return the network component states
	 */
	public HashMap<String, CableState> getCableStates() {
		if (cableStates==null) {
			cableStates = new HashMap<String, CableState>();
		}
		return cableStates;
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
	 * @see de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel#getBlackboardRequestAnswer(de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier)
	 */
	@Override
	public AbstractBlackboardAnswer getBlackboardRequestAnswer(SingleRequestSpecifier requestSpecifier) {
		
		AbstractBlackboardAnswer answer = null;

		// --- Check if it is an electricity-related request ------------------
		if (this.isResponsibleForRequest(requestSpecifier)) {
			ElectricityRequestObjective requestObjective = (ElectricityRequestObjective) requestSpecifier.getRequestObjective();
		
			switch (requestObjective) {
			case PowerFlowCalculationResults:
				answer = new PowerFlowCalculationResultAnswer(this.getNodeStates(), this.getCableStates());
				break;
			case TransformerPower:
				answer = new TransformerPowerAnswer(requestSpecifier.getIdentifier(), this.getTransformerStates().get(requestSpecifier.getIdentifier()));
				break;
			case VoltageLevels:
				answer = new VoltageLevelAnswer(requestSpecifier.getIdentifier(), this.getNodeStates().get(requestSpecifier.getIdentifier()));
				break;
			case CurrentLevels:
				answer = new CurrentLevelAnswer(requestSpecifier.getIdentifier(), this.getCableStates().get(requestSpecifier.getIdentifier()));
				break;
			}
		}
		return answer;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel#isResponsibleForRequest(de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier)
	 */
	@Override
	public boolean isResponsibleForRequest(SingleRequestSpecifier requestSpecifier) {
		// --- Check if the requested domain matches this aggregation ---------
		if (requestSpecifier.getRequestObjective() instanceof ElectricityRequestObjective) {
			if (requestSpecifier.getIdentifier()!=null) {
				// --- Check if the requested element is part of this aggregation
				return this.checkIdentifier(requestSpecifier.getIdentifier());
			} else {
				// --- Identifier not set -> general electricity request -> responsible
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if the given identifier belongs to a network component or graph element of this aggregation.
	 * @param identifier the identifier
	 * @return true, if successful
	 */
	private boolean checkIdentifier(String identifier) {
		Vector<NetworkComponent> networkComponents = this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents();
		for (int i=0; i<networkComponents.size(); i++) {
			// --- Check the network component itself ---------------
			NetworkComponent networkComponent = networkComponents.get(i);
			if (networkComponent.getId().equals(identifier)) {
				return true;
			} else {
				// --- Check the component's graph elements ---------
				Vector<GraphElement> graphElements = this.getSubAggregationConfiguration().getSubNetworkModel().getGraphElementsFromNetworkComponent(networkComponent);
				for (int j=0; j<graphElements.size(); j++) {
					if (graphElements.get(j).getId().equals(identifier)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
