package de.enflexit.ea.core.blackboard;

import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.BlackboardRequest;

/**
 * The Class NetworkComponentAnswer represents an extended {@link AbstractBlackboardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class NetworkComponentAnswer extends AbstractBlackboardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private String identifier;
	private Object networkComponentDataModel;

	
	/**
	 * Instantiates a new network component answer for a {@link BlackboardRequest}.
	 *
	 * @param identifier the identifier
	 * @param networkComponentDataModel the network component data model
	 */
	public NetworkComponentAnswer(String identifier, Object networkComponentDataModel) {
		this.identifier = identifier;
		this.networkComponentDataModel = networkComponentDataModel;
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
	 * Gets the network component data model.
	 * @return the network component data model
	 */
	public Object getNetworkComponentDataModel() {
		return networkComponentDataModel;
	}
	/**
	 * Sets the network component data model.
	 * @param networkComponentDataModel the new network component data model
	 */
	public void setNetworkComponentDataModel(Object networkComponentDataModel) {
		this.networkComponentDataModel = networkComponentDataModel;
	}
	
}
