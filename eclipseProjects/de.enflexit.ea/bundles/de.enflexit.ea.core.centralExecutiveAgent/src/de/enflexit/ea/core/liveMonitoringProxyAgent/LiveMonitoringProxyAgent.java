package de.enflexit.ea.core.liveMonitoringProxyAgent;

import java.util.List;

import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;

/**
 * This agent is responsible for collecting live data updates from the field agents and 
 * sending them to the subscribed LiveMonitoringAgent for visualization on the OPS side. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringProxyAgent extends Agent {

	private static final long serialVersionUID = -6826962250813798656L;

	private List<AID> responderAIDs;
	
	private LiveMonitoringSubscriptionInitiator subscriptionInitiator;
	private LiveMonitoringSubscriptionResponder subscriptionResponder;
	
	private MessageReceiveBehaviour messageReceiveBehaviour;
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void setup() {
		
		System.out.println("[" + this.getClass().getSimpleName() + "] Starting...");
		
		if (this.getArguments()!=null && this.getArguments().length==1) {
			this.responderAIDs = (List<AID>) this.getArguments()[0];
		}
		
		// --- Check if the responder IDs have been passed ----------
		if (this.responderAIDs==null) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Missing required arguments, terminating ...");
			this.doDelete();
			
		} else {
			
			// --- Register ontology and codec ----------------------
			this.getContentManager().registerLanguage(new SLCodec());
			this.getContentManager().registerOntology(OpsOntology.getInstance());
			
			// --- Start behaviours ---------------------------------
			this.startSubscriptionResponder();
			this.startSubscriptionInitiator();
			
			this.addBehaviour(this.getMessageReceiveBehaviour());
			
		}
		
	}
	

	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected void takeDown() {
		System.out.println("[" + this.getClass().getSimpleName() + "] Terminating...");
		if (this.getSubscriptionInitiator()!=null) {
			this.getSubscriptionInitiator().cancelSubscriptions();
		}
	}
	
	
	/**
	 * Gets the message receive behaviour.
	 * @return the message receive behaviour
	 */
	private MessageReceiveBehaviour getMessageReceiveBehaviour() {
		if (messageReceiveBehaviour==null) {
			messageReceiveBehaviour = new MessageReceiveBehaviour();
		}
		return messageReceiveBehaviour;
	}


	/**
	 * Starts the subscription initiator.
	 */
	private void startSubscriptionInitiator() {
		if (this.subscriptionInitiator==null) {
			this.subscriptionInitiator = new LiveMonitoringSubscriptionInitiator(this, this.getResponderAIDs());
			this.addBehaviour(subscriptionInitiator);
			this.getMessageReceiveBehaviour().addMessageTemplateToIgnoreList(LiveMonitoringSubscriptionInitiator.getMessageTemplateForReplies());
		}
	}
	
	/**
	 * Gets the subscription initiator.
	 * @return the subscription initiator
	 */
	private LiveMonitoringSubscriptionInitiator getSubscriptionInitiator() {
		return subscriptionInitiator;
	}


	/**
	 * Starts the subscription responder.
	 */
	private void startSubscriptionResponder() {
		if (this.subscriptionResponder==null) {
			this.subscriptionResponder = new LiveMonitoringSubscriptionResponder(this);
			this.addBehaviour(subscriptionResponder);
			this.getMessageReceiveBehaviour().addMessageTemplateToIgnoreList(LiveMonitoringSubscriptionResponder.getMessageTemplate());
		}
	}
	
	/**
	 * Gets the subscription responder.
	 * @return the subscription responder
	 */
	protected LiveMonitoringSubscriptionResponder getSubscriptionResponder() {
		return subscriptionResponder;
	}

	/**
	 * Gets the responder AIDs.
	 * @return the responder AIDs
	 */
	private List<AID> getResponderAIDs() {
		return responderAIDs;
	}
	

}
