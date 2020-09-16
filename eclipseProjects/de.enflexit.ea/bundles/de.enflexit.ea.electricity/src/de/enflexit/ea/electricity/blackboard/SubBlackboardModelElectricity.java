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
					answer = new PowerFlowCalculationResultAnswer(this.getGraphNodeStates(), this.getNetworkComponentStates());
					break;
				case TransformerPower:
					answer = new TransformerPowerAnswer(requestSpecifier.getIdentifier(), this.getTransformerStates().get(requestSpecifier.getIdentifier()));
					break;
				case VoltageLevels:
					answer = new VoltageLevelAnswer(requestSpecifier.getIdentifier(), this.getGraphNodeStates().get(requestSpecifier.getIdentifier()));
					break;
				case CurrentLevels:
					answer = new CurrentLevelAnswer(requestSpecifier.getIdentifier(), this.getNetworkComponentStates().get(requestSpecifier.getIdentifier()));
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
			if (this.checkComponent(networkComponents.get(i), identifier)==true) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkComponent(NetworkComponent netComp, String identifier) {
		if (netComp.getId().equals(identifier)) {
			return true;
		} else {
			Vector<GraphElement> graphElements = this.getSubAggregationConfiguration().getSubNetworkModel().getGraphElementsFromNetworkComponent(netComp);
			for (int i=0; i<graphElements.size(); i++) {
				if (graphElements.get(i).getId().equals(identifier)) {
					return true;
				}
			}
			return false;
		}
	}

}
