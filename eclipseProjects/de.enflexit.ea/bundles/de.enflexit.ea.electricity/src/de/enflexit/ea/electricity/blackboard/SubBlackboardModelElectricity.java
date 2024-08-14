package de.enflexit.ea.electricity.blackboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

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
	
	private HashMap<String, Boolean> isPartOfSubAggregationHashMap;
	
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
		if (this.isResponsibleForRequest(requestSpecifier)==true) {
			
			ElectricityRequestObjective requestObjective = (ElectricityRequestObjective) requestSpecifier.getRequestObjective();
			switch (requestObjective) {
			case PowerFlowCalculationResults:
				answer = new PowerFlowCalculationResultAnswer(this.getNodeStates(), this.getCableStates());
				break;
			case TransformerPower:
				answer = new TransformerPowerAnswer(requestSpecifier.getIdentifier(), this.getTransformerStates().get(requestSpecifier.getIdentifier()));
				break;
			case VoltageLevels:
				String identifier = requestSpecifier.getIdentifier();
				// --- Case identifier is GraphNodeID -------------------------
				ElectricalNodeState elNodeState = this.getNodeStates().get(identifier);
				if (elNodeState==null) {
					// --- Check if identifier is NetworkComponent ID ---------
					NetworkModel grossNetworkModel = this.getAggregationHandler().getNetworkModel();
					NetworkComponent netComp = grossNetworkModel.getNetworkComponent(identifier);
					if (netComp!=null && grossNetworkModel.isDistributionNode(netComp)==true) {
						String graphNodeID = netComp.getGraphElementIDs().iterator().next();
						elNodeState = this.getNodeStates().get(graphNodeID);
					}
				}
				if (elNodeState!=null) {
					answer = new VoltageLevelAnswer(requestSpecifier.getIdentifier(), elNodeState);
				}
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
				return this.isPartOfSubAggregation(requestSpecifier.getIdentifier());
			} else {
				// --- Identifier not set -> general electricity request -> responsible
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Local Reminder for checks if an identifier will be handled by the local blackboard.
	 * @return the reminder hash map
	 */
	private HashMap<String, Boolean> isPartOfSubAggregationHashMap() {
		if (isPartOfSubAggregationHashMap==null) {
			isPartOfSubAggregationHashMap = new HashMap<>();
		}
		return isPartOfSubAggregationHashMap;
	}
	/**
	 * Check if the given identifier belongs to a network component or graph element of the current sub aggregation.
	 * @param identifier the {@link NetworkComponent}- or {@link GraphElement}- identifier
	 * @return true, if successful
	 */
	private boolean isPartOfSubAggregation(String identifier) {
		
		// --------------------------------------------------------------------
		// --- Is that a NetworkComponent- or a GraphNode-ID? -----------------
		// --------------------------------------------------------------------
		
		if (identifier==null || identifier.isBlank()==true) return false;
		
		// --------------------------------------------------------------------
		// --- Check reminder first -------------------------------------------
		Boolean isPartOf = this.isPartOfSubAggregationHashMap().get(identifier);
		if (isPartOf!=null) {
			return isPartOf;
		} else {
			isPartOf = false; // -- set method default -- 
		}
		
		// --------------------------------------------------------------------		
		// --- Define list of relevant NetworkComponents ----------------------
		List<NetworkComponent> netCompListClusterCheck = new ArrayList<>();
		
		// --- Try getting NeworkComponent list, related to the identifier ----
		NetworkModel grossNetworkModel = this.getAggregationHandler().getNetworkModel();
		NetworkComponent netCompFound = grossNetworkModel.getNetworkComponent(identifier);
		if (netCompFound!=null) {
			netCompListClusterCheck.add(netCompFound);
			
		} else  {
			// --- Alternatively, check graphElements ------------------------- 
			GraphElement graphElement = grossNetworkModel.getGraphElement(identifier);
			if (graphElement!=null) {
				// --- GraphNode or GraphEdge ---------------------------------
				if (graphElement instanceof GraphEdge) {
					netCompFound = grossNetworkModel.getNetworkComponent((GraphEdge)graphElement);
					netCompListClusterCheck.add(netCompFound);
					
				} else if (graphElement instanceof GraphNode) {
					List<NetworkComponent> netCompFoundList = grossNetworkModel.getNetworkComponents((GraphNode)graphElement);
					if (netCompFoundList!=null && netCompFoundList.size()>0) {
						netCompListClusterCheck.addAll(netCompFoundList);
					}
				}
			}
		}
		
		
		// --- Found NetworkComponents related to the identifier -------------- 
		if (netCompListClusterCheck.size()>0) {
			// --- Get NetowkrComponents of sub aggregation -------------------
			Vector<NetworkComponent> subAggNetComps = this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents();
			// --- Check the cluster NetworkCompoents for the IDs found -------
			ClusterCheck: for (NetworkComponent netCompCheck : netCompListClusterCheck) {
				for (NetworkComponent netComp : subAggNetComps) {
					if (netComp.getId().equals(netCompCheck.getId())==true) {
						isPartOf = true;
						break ClusterCheck;
					}
				}
			}
		}
		
		// --------------------------------------------------------------------
		// --- Remind this answer ---------------------------------------------
		this.isPartOfSubAggregationHashMap().put(identifier, isPartOf);
		
		return isPartOf;
	}

}
