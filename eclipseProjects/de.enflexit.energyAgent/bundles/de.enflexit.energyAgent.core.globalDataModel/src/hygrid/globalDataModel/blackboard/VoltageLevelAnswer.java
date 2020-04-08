package hygrid.globalDataModel.blackboard;

import hygrid.globalDataModel.ontology.ElectricalNodeState;

/**
 * The Class VoltageLevelAnswer represents an extended {@link AbstractBlackoardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class VoltageLevelAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private String identifier;
	private ElectricalNodeState electricalNodeState;

	
	/**
	 * Instantiates a VoltageLevelAnswer for a {@link BlackboardRequest}.
	 *
	 * @param identifier the identifier
	 * @param electricalNodeState the electrical node state
	 */
	public VoltageLevelAnswer(String identifier, ElectricalNodeState electricalNodeState) {
		this.identifier = identifier;
		this.electricalNodeState = electricalNodeState;
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
	 * Gets the ElectricalNodeState.
	 * @return the electrical node state
	 */
	public ElectricalNodeState getElectricalNodeState() {
		return electricalNodeState;
	}
	/**
	 * Sets the ElectricalNodeState.
	 * @param electricalNodeState the new electrical node state
	 */
	public void setElectricalNodeState(ElectricalNodeState electricalNodeState) {
		this.electricalNodeState = electricalNodeState;
	}
	
}
