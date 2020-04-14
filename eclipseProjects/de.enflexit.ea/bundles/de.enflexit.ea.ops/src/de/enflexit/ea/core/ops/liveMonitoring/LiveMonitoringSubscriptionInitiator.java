package de.enflexit.ea.core.ops.liveMonitoring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.codec.binary.Base64;

import de.enflexit.ea.core.globalDataModel.GlobalHyGridConstants;
import energy.optionModel.TechnicalSystemStateEvaluation;
import hygrid.ops.ontology.LiveMonitoringUpdate;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

/**
 * With this behaviour, the LiveMonitoringAgent can subscribe for updates from the field agents.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringSubscriptionInitiator extends SubscriptionInitiator {

	private static final long serialVersionUID = -8380798454235202583L;
	
	private AID proxyAID;
	
	private boolean debug = false;

	/**
	 * Instantiates a new live monitoring subscription initiator.
	 * @param liveMonitoringAgent the live monitoring agent
	 * @param proxyAID the proxy AID
	 */
	public LiveMonitoringSubscriptionInitiator(LiveMonitoringAgent liveMonitoringAgent, AID proxyAID) {
		super(liveMonitoringAgent, getSubscriptionMessage(proxyAID));
		this.proxyAID = proxyAID;
	}
	
	/**
	 * Gets the subscription message.
	 * @param responderAID the responder AID
	 * @return the subscription message
	 */
	private static ACLMessage getSubscriptionMessage(AID responderAID) {
		ACLMessage subscriptionMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
		subscriptionMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		subscriptionMessage.setConversationId(GlobalHyGridConstants.CONVERSATION_ID_LIVE_MONITORING_OPS);
		subscriptionMessage.addReceiver(responderAID);
		return subscriptionMessage;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage updateMessage) {
		LiveMonitoringUpdate liveMonitoringUpdate = this.extractMessageContent(updateMessage);
		if (liveMonitoringUpdate!=null) {
			String componentID = liveMonitoringUpdate.getAgentID();
			TechnicalSystemStateEvaluation newTsse = this.decodeTsseFromBase64(liveMonitoringUpdate.getNewTsseBase64());
			if (this.debug==true) {
				System.out.println("Received new TSSE: Agent " + liveMonitoringUpdate.getAgentID() + ", timestamp " + newTsse.getGlobalTime());
			}
			
			((LiveMonitoringAgent)myAgent).getAggregationHandler().appendToNetworkComponentsScheduleController(componentID, newTsse);
		} else {
			System.out.println("[" + this.getClass().getSimpleName() + "] " + myAgent.getLocalName() + " could not read update message from " + updateMessage.getSender());
		}
		
	}
	
	private TechnicalSystemStateEvaluation decodeTsseFromBase64(String tsseBase64) {
		TechnicalSystemStateEvaluation tsse = null;
		
		try {
			byte[] byteArray = Base64.decodeBase64(tsseBase64.getBytes());
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArray));
			tsse = (TechnicalSystemStateEvaluation) ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return tsse;
	}
	
	
	/**
	 * Extract the live monitoring update from the update message.
	 * @param updateMessage the update message
	 * @return the live monitoring update
	 */
	private LiveMonitoringUpdate extractMessageContent(ACLMessage updateMessage) {
		LiveMonitoringUpdate liveMonitoringUpdate = null;
		try {
			ContentElement contentElement = myAgent.getContentManager().extractContent(updateMessage);
			if (contentElement instanceof Action) {
				Concept concept = ((Action)contentElement).getAction();
				if (concept instanceof LiveMonitoringUpdate) {
					liveMonitoringUpdate = (LiveMonitoringUpdate) concept;
				}
				
			}
		} catch (CodecException | OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return liveMonitoringUpdate;
	}
	
	protected void cancel() {
		this.cancel(this.proxyAID, true);
	}


}
