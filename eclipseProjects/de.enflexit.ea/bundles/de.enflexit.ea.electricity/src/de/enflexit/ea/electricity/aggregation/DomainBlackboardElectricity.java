package de.enflexit.ea.electricity.aggregation;

import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardAgent;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;
import de.enflexit.ea.core.dataModel.blackboard.CurrentLevelAnswer;
import de.enflexit.ea.core.dataModel.blackboard.DomainBlackboard;
import de.enflexit.ea.core.dataModel.blackboard.GraphNodeAnswer;
import de.enflexit.ea.core.dataModel.blackboard.MultipleBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.NetworkModelAnswer;
import de.enflexit.ea.core.dataModel.blackboard.PowerFlowCalculationResultAnswer;
import de.enflexit.ea.core.dataModel.blackboard.RequestSpecifier;
import de.enflexit.ea.core.dataModel.blackboard.TransformerPowerAnswer;
import de.enflexit.ea.core.dataModel.blackboard.VoltageAndCurrentLevelAnswer;
import de.enflexit.ea.core.dataModel.blackboard.VoltageLevelAnswer;
import de.enflexit.ea.core.dataModel.blackboard.RequestSpecifier.RequestObjective;
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
	
	private NetworkModel networkModel;
	
	/**
	 * Instantiates a new domain blackboard electricity.
	 * @param networkModel the network model
	 */
	public DomainBlackboardElectricity(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
	/**
	 * Gets the network model.
	 * @return the network model
	 */
	private NetworkModel getNetworkModel() {
		return networkModel;
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
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.blackboard.DomainBlackboard#resetBlackboardDataModel()
	 */
	@Override
	protected void resetBlackboardDataModel() {
		this.graphNodeStates = null;
		this.networkComponentStates = null;
		this.transformerStates = null;	
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.blackboard.DomainBlackboard#processBlackboardRequest(de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest)
	 */
	@Override
	public AbstractBlackboardAnswer processBlackboardRequest(BlackboardRequest bbRequest) {
		AbstractBlackboardAnswer answer = null;
		// TODO Auto-generated method stub
		
		if (bbRequest.getRequestSpecifierVector().size()==1) {
			// --- Single value request -------------------------------
			answer = this.getBlackboardAnswer(bbRequest.getRequestSpecifierVector().get(0));					
			
		} else {
			
			if (bbRequest.getRequestSpecifierVector().size()==2 && bbRequest.getRequestSpecifierVector().get(0).getRequestObjective()==RequestObjective.VoltageAndCurrentLevels) {
				// --- Special case for sensor agents -----------------
				RequestSpecifier spec1 = bbRequest.getRequestSpecifierVector().get(0);
				RequestSpecifier spec2 = bbRequest.getRequestSpecifierVector().get(1);
				ElectricalNodeState nodeState = this.getGraphNodeStates().get(spec1.getIdentifier());
				if (nodeState==null) {
					nodeState = this.getGraphNodeStates().get(spec2.getIdentifier());
				}
				CableState cableState = this.getNetworkComponentStates().get(spec2.getIdentifier());
				if (cableState==null) {
					cableState = this.getNetworkComponentStates().get(spec1.getIdentifier());
				}
				if (nodeState!=null & cableState!=null) {
					answer = new VoltageAndCurrentLevelAnswer(spec1.getIdentifier(), nodeState, cableState);
				}
				
			} else {
				// --- Multiple Request -------------------------------
				Vector<AbstractBlackboardAnswer> answers = new Vector<>();
				for (RequestSpecifier spec : bbRequest.getRequestSpecifierVector()) {
					AbstractBlackboardAnswer tmpAnswer = this.getBlackboardAnswer(spec);
					if (tmpAnswer!=null) {
						answers.add(tmpAnswer);	
					}
				}
				// --- Send multiple answers --------------------------
				answer = new MultipleBlackboardAnswer(answers); 
			}
		}
		
		return answer;
	}
	
	/**
	 * Returns the blackboard answer for the specified request.
	 * @param singleRequest the single request 
	 * @return the blackboard answer
	 */
	private AbstractBlackboardAnswer getBlackboardAnswer(RequestSpecifier singleRequest) {
	
		AbstractBlackboardAnswer answer = null;
		NetworkModel networkModel = this.getNetworkModel();
		
		switch (singleRequest.getRequestObjective()) {
		case NetworkModel:
			answer = new NetworkModelAnswer(networkModel);
			break;
		case GraphNodeDataModel:
			GraphNode node = (GraphNode) networkModel.getGraphElement(singleRequest.getIdentifier());
			answer = new GraphNodeAnswer(singleRequest.getIdentifier(), node.getDataModel());
			break;
		case NetworkComponentDataModel:
			NetworkComponent netComp = networkModel.getNetworkComponent(singleRequest.getIdentifier());
			answer = new GraphNodeAnswer(singleRequest.getIdentifier(), netComp.getDataModel());
			break;
		case PowerFlowCalculationResults:
			answer = new PowerFlowCalculationResultAnswer(this.getGraphNodeStates(), this.getNetworkComponentStates());
			break;
		case TransformerPower:
			answer = new TransformerPowerAnswer(singleRequest.getIdentifier(), this.getTransformerStates().get(singleRequest.getIdentifier()));
			break;
		case VoltageLevels:
			answer = new VoltageLevelAnswer(singleRequest.getIdentifier(), this.getGraphNodeStates().get(singleRequest.getIdentifier()));
			break;
		case CurrentLevels:
			answer = new CurrentLevelAnswer(singleRequest.getIdentifier(), this.getNetworkComponentStates().get(singleRequest.getIdentifier()));
			break;
		case VoltageAndCurrentLevels:
			break;
		}
		return answer;
	}
	
}
