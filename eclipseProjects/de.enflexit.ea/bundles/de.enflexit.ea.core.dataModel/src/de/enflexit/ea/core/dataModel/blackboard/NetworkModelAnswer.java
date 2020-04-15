package de.enflexit.ea.core.dataModel.blackboard;

import org.awb.env.networkModel.NetworkModel;

/**
 * The Class NetworkModelAnswer represents an extended {@link AbstractBlackoardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class NetworkModelAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private NetworkModel networkModel;

	
	/**
	 * Instantiates a new network model answer for a {@link BlackboardRequest}.
	 * @param networkModel the network model
	 */
	public NetworkModelAnswer(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
	/**
	 * Gets the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	/**
	 * Sets the network model.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
}
