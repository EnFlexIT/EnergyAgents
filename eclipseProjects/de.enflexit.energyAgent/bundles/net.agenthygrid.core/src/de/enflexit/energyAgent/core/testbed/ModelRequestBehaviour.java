package de.enflexit.energyAgent.core.testbed;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

/**
 * Behaviour for requesting the network model or the network component from the CEA. 
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class ModelRequestBehaviour extends SimpleAchieveREInitiator {
	
	private static final long serialVersionUID = -790073472159364854L;

	public enum RequestType{
		NETWORK_MODEL, NETWORK_COMPONENT
	}

	public ModelRequestBehaviour(Agent a, ACLMessage msg, RequestType requestType) {
		super(a, msg);
	}

	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		return super.prepareRequest(msg);
	}
	
	

}
