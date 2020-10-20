package de.enflexit.ea.core.aggregation.dashboard;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

/**
 * The Class DashboardSubscriptionInitiator.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DashboardSubscriptionInitiator extends SubscriptionInitiator {

	private static final long serialVersionUID = -3362886788353990119L;
	
	/**
	 * Instantiates a new DashboardSubscriptionInitiator.
	 *
	 * @param agent the agent
	 * @param aclMessage the initial ACL message
	 */
	public DashboardSubscriptionInitiator(Agent agent, ACLMessage aclMessage) {
		super(agent, aclMessage);
		AID receiver = (AID) aclMessage.getAllReceiver().next();
		System.out.println("[" + myAgent.getLocalName() + "] Subscribing to " + receiver.getName());
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage notificationMessage) {
		try {
			Object contentObject = notificationMessage.getContentObject();
			System.out.println("[" + this.getClass().getSimpleName() + "] Received state update, processing...");
			//TODO Update dasboard widgets
		} catch (UnreadableException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error extracting content object!");
			e.printStackTrace();
		}
	}
	
	
	
}
