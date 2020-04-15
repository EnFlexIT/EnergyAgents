package de.enflexit.ea.core.liveMonitoringProxyAgent;

import java.util.List;

import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

/**
 * Using this class, the LiveDataProxyAgent can subscribe for new TSSEs from the field agents
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringSubscriptionInitiator extends SubscriptionInitiator {

	private static final long serialVersionUID = 7750502030398924988L;
	
	private List<AID> responderAIDs;

	/**
	 * Instantiates a new live monitoring subscription initiator.
	 * @param agent the agent
	 * @param message the message
	 */
	public LiveMonitoringSubscriptionInitiator(Agent agent, List<AID> responderAIDs) {
		super(agent, getSubscriptionMessage(responderAIDs));
		this.responderAIDs = responderAIDs;
		System.out.println(myAgent.getLocalName() + ": Starting " + this.getClass().getSimpleName() + ", subscribing to " + responderAIDs.size() + " agents");
	}
	
	
	
	/**
	 * Gets the subscription message.
	 * @param responderAIDs the responder AIDs
	 * @return the subscription message
	 */
	public static ACLMessage getSubscriptionMessage(List<AID> responderAIDs) {
		ACLMessage subscriptionMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
		subscriptionMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		subscriptionMessage.setConversationId(GlobalHyGridConstants.CONVERSATION_ID_LIVE_MONITORING_FIELD);
		for (int i=0; i<responderAIDs.size(); i++) {
			subscriptionMessage.addReceiver(responderAIDs.get(i));
		}
		
		return subscriptionMessage;
	}
	
	/**
	 * Gets the message template for replies to this subscription, which should be added to the MessageReceiveBehaviour's ignore list to prevent conflicts.
	 * @return the message template for replies
	 */
	public static MessageTemplate getMessageTemplateForReplies() {
		MessageTemplate matchProtocol = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		MessageTemplate matchConversationID = MessageTemplate.MatchConversationId(GlobalHyGridConstants.CONVERSATION_ID_LIVE_MONITORING_FIELD);
		MessageTemplate matchPerformative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		
		MessageTemplate matchAll = MessageTemplate.and(matchProtocol, MessageTemplate.and(matchConversationID, matchPerformative));
		return matchAll;
	}
	
	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		String agentID = inform.getSender().getLocalName();
		
		// --- Extract the new TSSE from the message ------
		TechnicalSystemStateEvaluation tsse = null;
		try {
			tsse = (TechnicalSystemStateEvaluation) inform.getContentObject();
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// --- Forward it to the LiveMonitoringAgents -----
		if (tsse!=null) {
//			System.out.println("[" + this.getClass().getSimpleName() + "] " + myAgent.getLocalName() + " received a new TSSE from " + agentID);
			((LiveMonitoringProxyAgent)myAgent).getSubscriptionResponder().sendNewTSSE(agentID, tsse);
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] " + myAgent.getLocalName() + " Could not extract a TSSE from the update message");
		}
	}

	/**
	 * Cancel the subscriptions.
	 */
	protected void cancelSubscriptions() {
		System.out.println("[" + this.getClass().getSimpleName() + "] Canceling all subscriptions");
		// --- Cancel all subscriptions -------------------
		for (int i=0; i<this.responderAIDs.size(); i++) {
			this.cancel(this.responderAIDs.get(i), true);
		}
	}
	

}
