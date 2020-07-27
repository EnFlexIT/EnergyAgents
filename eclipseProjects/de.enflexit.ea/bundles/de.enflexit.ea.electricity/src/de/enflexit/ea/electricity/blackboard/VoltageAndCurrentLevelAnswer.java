package de.enflexit.ea.electricity.blackboard;

import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;

/**
 * The Class VoltageAndCurrentLevelAnswer represents an extended {@link AbstractBlackboardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class VoltageAndCurrentLevelAnswer extends AbstractBlackboardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private String identifier;
	private ElectricalNodeState electricalNodeState;
	private CableState cableState;
	
	/**
	 * Instantiates a VoltageAndCurrentLevelAnswer for a {@link BlackboardRequest}.
	 *
	 * @param identifier the identifier
	 * @param electricalNodeState the electrical node state
	 * @param cableState the cable state
	 */
	public VoltageAndCurrentLevelAnswer(String identifier, ElectricalNodeState electricalNodeState, CableState cableState) {
		this.identifier = identifier;
		this.electricalNodeState = electricalNodeState;
		this.cableState = cableState;
	}
	
	/**
	 * Gets the identifier of the NetworkComponent.
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * Sets the identifier of the NetworkComponent.
	 * @param identifier the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Gets the TriPhaseElectricalNodeState.
	 * @return the tri phase electrical node state
	 */
	public ElectricalNodeState getElectricalNodeState() {
		return electricalNodeState;
	}
	/**
	 * Sets the TriPhaseElectricalNodeState.
	 * @param triPhaseElectricalNodeState the new tri phase electrical node state
	 */
	public void setElectricalNodeState(ElectricalNodeState electricalNodeState) {
		this.electricalNodeState = electricalNodeState;
	}
	
	/**
	 * Gets the cable state.
	 * @return the cable state
	 */
	public CableState getCableState() {
		return cableState;
	}
	/**
	 * Sets the cable state.
	 * @param cableState the new cable state
	 */
	public void setCableState(CableState cableState) {
		this.cableState = cableState;
	}
}
