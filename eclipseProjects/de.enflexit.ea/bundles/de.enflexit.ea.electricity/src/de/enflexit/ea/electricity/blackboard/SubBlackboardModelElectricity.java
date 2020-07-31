package de.enflexit.ea.electricity.blackboard;

import java.util.HashMap;

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
	public AbstractBlackboardAnswer getBlackboardRequestAnswer(SingleRequestSpecifier request) {
		
		AbstractBlackboardAnswer answer = null;

		// --- Check if it is an electricity-related request ------------------
		if (request.getRequestObjective() instanceof ElectricityRequestObjective) {
			ElectricityRequestObjective requestObjective = (ElectricityRequestObjective) request.getRequestObjective();
		
			switch (requestObjective) {
				case PowerFlowCalculationResults:
					answer = new PowerFlowCalculationResultAnswer(this.getGraphNodeStates(), this.getNetworkComponentStates());
					break;
				case TransformerPower:
					answer = new TransformerPowerAnswer(request.getIdentifier(), this.getTransformerStates().get(request.getIdentifier()));
					break;
				case VoltageLevels:
					answer = new VoltageLevelAnswer(request.getIdentifier(), this.getGraphNodeStates().get(request.getIdentifier()));
					break;
				case CurrentLevels:
					answer = new CurrentLevelAnswer(request.getIdentifier(), this.getNetworkComponentStates().get(request.getIdentifier()));
					break;
				case VoltageAndCurrentLevels:
					//TODO handle request
					break;
			}
		}
		
		return answer;
	}

}
