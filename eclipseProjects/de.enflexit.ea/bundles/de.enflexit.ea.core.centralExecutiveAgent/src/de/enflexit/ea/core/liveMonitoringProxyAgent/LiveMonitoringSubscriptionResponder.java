package de.enflexit.ea.core.liveMonitoringProxyAgent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;

import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.opsOntology.LiveMonitoringUpdate;
import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This class handles subscriptions from LiveMonitoringAgents and provides them with updates.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringSubscriptionResponder extends HygridSubscriptionResponder {

	private static final long serialVersionUID = -5885507863333475116L;
	
	private boolean debug = false;
	
	/**
	 * Instantiates a new live monitoring subscription responder.
	 * @param liveMonitoringProxyAgent the live monitoring proxy agent
	 */
	public LiveMonitoringSubscriptionResponder(LiveMonitoringProxyAgent liveMonitoringProxyAgent) {
		super(liveMonitoringProxyAgent, getMessageTemplate());
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleSubscription(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
		if (this.debug==true) {
			System.out.println("[" + this.getClass().getSimpleName() + "] Received subscription from " + subscription.getSender().getName());
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
////    	subsMsg.setConversationId(originalConversationID);
//		return subscription;
//	}

	/**
	 * Gets the message template for this behaviour.
	 * @return the message template
	 */
	public static MessageTemplate getMessageTemplate() {
		MessageTemplate matchConversationID = MessageTemplate.MatchConversationId(GlobalHyGridConstants.CONVERSATION_ID_LIVE_MONITORING_OPS);
		MessageTemplate matchProtocol = MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE);
		MessageTemplate matchPerformative = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
		
		// --- Match protocol, conversation ID and performative -----
		MessageTemplate matchAll = MessageTemplate.and(matchProtocol, MessageTemplate.and(matchConversationID, matchPerformative));
		
		return matchAll;
	}
	
	/**
	 * Send new TSSE.
	 *
	 * @param agentID the agent ID
	 * @param tsse the tsse
	 */
	public void sendNewTSSE(String agentID, TechnicalSystemStateEvaluation tsse) {
		
		if (this.debug==true) {
			System.out.println("Forwarding TSSE: Agent " + agentID + ", timestamp " + tsse.getGlobalTime());
		}

		// --- Check if there are any subscriptions -------
		@SuppressWarnings("unchecked")
		Vector<Subscription> subscriptions = this.getSubscriptions();
		if (subscriptions.size()>0) {
			
			// --- If there are any, send the update ------
			ACLMessage updateMessage = null;
			try {
				
				// --- Prepare the message ----------------
				updateMessage = new ACLMessage(ACLMessage.INFORM);
				updateMessage.setLanguage(new SLCodec().getName());
				updateMessage.setOntology(OpsOntology.getInstance().getName());
				
				// --- Prepare the content ----------------
				String tsseBase64 = this.encodeTsseAsBase64(tsse);
				LiveMonitoringUpdate liveMonitoringUpdate = new LiveMonitoringUpdate();
				liveMonitoringUpdate.setAgentID(agentID);
				liveMonitoringUpdate.setNewTsseBase64(tsseBase64);
				
				// --- Set the content --------------------
				Action updateAction = new Action(myAgent.getAID(), liveMonitoringUpdate);
				myAgent.getContentManager().fillContent(updateMessage, updateAction);
				
			} catch (CodecException | OntologyException e) {
				System.err.println(myAgent.getLocalName() + ": Could not send live monitoring update for agent " + agentID + ", error setting message content!");
			}
			
			// --- Send the update to all subscribers -----
			if (updateMessage!=null) {
				for (int i=0; i<subscriptions.size(); i++) {
					subscriptions.get(i).notify(updateMessage);
				}
//				System.out.println("[" + this.getClass().getSimpleName() + "] " + myAgent.getLocalName() + " forwarded new TSSE from " + agentID + " to " + subscriptions.size() + " subscribers");
			}
		} else {
			if (this.debug==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "] " + myAgent.getLocalName() + " received a new TSSE, but has no subscribers");
			}
		}	// end subscriptions.size()>0
	}

	
	/**
	 * Encodes a {@link TechnicalSystemStateEvaluation} as Base64-String.
	 * @param tsse the tsse
	 * @return the encoded tsse
	 */
	private String encodeTsseAsBase64(TechnicalSystemStateEvaluation tsse) {
		String tsseBase64 = null;
		
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream( baos );
			oos.writeObject(tsse);
			tsseBase64 = new String(Base64.encodeBase64(baos.toByteArray()));
			
		} catch (IOException e) {
			System.err.println(myAgent.getLocalName() + ": Error encoding TSSE as Base64 String!");
			e.printStackTrace();
		} finally {
			try {
				if (oos!=null) {
						oos.close();
				}
				if (baos!=null) {
					baos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return tsseBase64;
	}



	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleCancel(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
		
		// --- Cancel the subscription ------------------------------
//		String originalConversationID = cancel.getConversationId();
//		cancel.setConversationId(originalConversationID + "_" + cancel.getSender().getLocalName());
		super.handleCancel(cancel);
		
		// --- Terminate if there are no subscriptions left --------- 
		if (this.getSubscriptions().isEmpty()) {
			if (this.debug==true) {
				System.out.println(myAgent.getLocalName() + ": The last subscription was canceled, terminating...");
			}
			myAgent.doDelete();
		}
		
		// --- No response needed -----------------------------------
		return null;
	}

	
}
