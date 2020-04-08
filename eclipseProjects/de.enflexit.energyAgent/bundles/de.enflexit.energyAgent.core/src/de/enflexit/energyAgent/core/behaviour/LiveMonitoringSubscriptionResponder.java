package de.enflexit.energyAgent.core.behaviour;

import java.io.IOException;
import java.util.Vector;

import de.enflexit.energyAgent.core.globalDataModel.GlobalHyGridConstants;
import de.enflexit.energyAgent.core.monitoring.MonitoringEvent;
import de.enflexit.energyAgent.core.monitoring.MonitoringListener;
import energy.evaluation.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

/**
 * Subscription responder that will send new TSSEs of an EnergyAgent to the LiveDataProxyAgent
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringSubscriptionResponder extends SubscriptionResponder implements MonitoringListener{

	private static final long serialVersionUID = -7104513060179768222L;
	
	private boolean debug = false;
	
	/**
	 * Instantiates a new live monitoring subscription responder.
	 * @param agent the agent
	 */
	public LiveMonitoringSubscriptionResponder(Agent agent) {
		super(agent, getMessageTemplate());
	}
	
	/**
	 * Instantiates a new live monitoring subscription responder.
	 * @param agent the agent
	 * @param messageTemplate the message template
	 */
	public LiveMonitoringSubscriptionResponder(Agent agent, MessageTemplate messageTemplate) {
		super(agent, messageTemplate);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.monitoring.MonitoringListener#onMonitoringEvent(de.enflexit.energyAgent.core.monitoring.MonitoringEvent)
	 */
	@Override
	public void onMonitoringEvent(MonitoringEvent monitoringEvent) {
		@SuppressWarnings("unchecked")
		Vector<Subscription> subscriptions = this.getSubscriptions();
		
		// --- Check if there are subscriptions ---------------------
		if (subscriptions.size()>0) {
			TechnicalSystemStateEvaluation newTSSE = monitoringEvent.getTSSE();
			
			if (newTSSE!=null) {
				
				// --- Get a copy without history -------------------
				TechnicalSystemStateEvaluation tsseForUpdateMessage = TechnicalSystemStateHelper.getTsseCloneWithoutParent(newTSSE);
				
				if (this.debug==true) {
					System.out.println("Sending new TSSE: Agent " + myAgent.getLocalName() + ", timestamp " + tsseForUpdateMessage.getGlobalTime());
				}
				
				// --- Create an update message ---------------------
				ACLMessage updateMessage = null;
				try {
					updateMessage = new ACLMessage(ACLMessage.INFORM);
					updateMessage.setContentObject(tsseForUpdateMessage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// --- Send the update to all subscribers -----------
				if (updateMessage!=null) {
					
					for (int i=0; i<subscriptions.size(); i++) {
						subscriptions.get(i).notify(updateMessage);
					}
				}
			}
		}
	}
	
	
	/**
	 * Gets the required message template for the initialization of this responder behaviour.
	 * @return the message template
	 */
	public static MessageTemplate getMessageTemplate() {
		MessageTemplate matchConversationID = MessageTemplate.MatchConversationId(GlobalHyGridConstants.CONVERSATION_ID_LIVE_MONITORING_FIELD);
		MessageTemplate matchProtocol = MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE);
		MessageTemplate matchPerformative = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
		
		// --- Match protocol, conversation ID and performative -----
		MessageTemplate matchAll = MessageTemplate.and(matchProtocol, MessageTemplate.and(matchConversationID, matchPerformative));
		return matchAll;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleSubscription(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
		//TODO This method was only overridden for adding debug output, remove when no longer needed
		if (this.debug==true) {
			System.out.println("[" + this.getClass().getSimpleName() + "] " + myAgent.getLocalName() + " received subscription from " + subscription.getSender().getName());
		}
		return super.handleSubscription(subscription);
	}
	
//	/* (non-Javadoc)
//     * @see jade.proto.SubscriptionResponder#createSubscription(jade.lang.acl.ACLMessage)
//     */
//    @Override
//	public Subscription createSubscription(ACLMessage subsMsg) {
//    	// --- Workaround to enable subscriptions from multiple agents with the same conversation ID
//    	// --- Append the local name, as creating subscriptions requires a unique conversation ID
//    	String originalConversationID = subsMsg.getConversationId();
//    	subsMsg.setConversationId(originalConversationID + "_" + subsMsg.getSender().getLocalName());
//    	Subscription subscription = super.createSubscription(subsMsg);
//    	// --- Return to the original ID to prevent problems with responses
//    	subsMsg.setConversationId(originalConversationID);
//		return subscription;
//	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleCancel(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
		// --- Cancel the subscription ------------------------------
		if (this.debug==true) {
			System.out.println(myAgent.getLocalName() + ": " + cancel.getSender().getName() + " canceled the subscription");
		}
//		String originalConversationID = cancel.getConversationId();
//		cancel.setConversationId(originalConversationID + "_" + cancel.getSender().getLocalName());
		return super.handleCancel(cancel);
		
	}
	
	
}
