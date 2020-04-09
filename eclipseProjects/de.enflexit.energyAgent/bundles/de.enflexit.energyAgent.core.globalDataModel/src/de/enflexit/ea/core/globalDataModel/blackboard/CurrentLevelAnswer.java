package de.enflexit.ea.core.globalDataModel.blackboard;

import de.enflexit.ea.core.globalDataModel.ontology.CableState;

/**
 * The Class CurrentLevelAnswer represents an extended {@link AbstractBlackoardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class CurrentLevelAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private String identifier;
	private CableState cableState;

	
	/**
	 * Instantiates a CurrentLevelAnswer for a {@link BlackboardRequest}.
	 *
	 * @param identifier the identifier
	 * @param cableState the cable state
	 */
	public CurrentLevelAnswer(String identifier, CableState cableState) {
		this.identifier = identifier;
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
