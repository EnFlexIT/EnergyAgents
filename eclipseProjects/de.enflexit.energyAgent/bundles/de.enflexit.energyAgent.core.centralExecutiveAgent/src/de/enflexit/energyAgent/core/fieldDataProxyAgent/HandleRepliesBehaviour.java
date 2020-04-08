package de.enflexit.energyAgent.core.fieldDataProxyAgent;

import de.enflexit.energyAgent.core.globalDataModel.cea.ConversationID;
import hygrid.ops.ontology.FieldDataReply;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour receives the requested field data from the agents and passes
 * it to the requesting FieldDataRequestAgent.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class HandleRepliesBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 2974690922340906549L;

	private boolean done = false;
	
	private AID requester;
	private MessageTemplate messageTemplate;
	
	private boolean debug = false;
	
	/**
	 * Instantiates a new handle replies behaviour.
	 * @param requester the requester
	 */
	public HandleRepliesBehaviour(AID requester) {
		super();
		this.requester = requester;
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(this.getMessageTemplate());
		if (msg!=null) {
			
			if (msg.getPerformative()==ACLMessage.INFORM) {
				
			
				// --- Remember the sender ------------------------------------
				AID sender = msg.getSender();
				
				if (((FieldDataProxyAgent)myAgent).getWaitingForResponse().contains(sender.getLocalName())) {
					if (this.debug==true) {
						System.out.println(myAgent.getLocalName() + ": Received field data from " + sender.getName());
					}
					
					// --- Forward the message to the requester -----------
					ACLMessage forwardMessage = new ACLMessage(msg.getPerformative());
					forwardMessage.setConversationId(msg.getConversationId());
					forwardMessage.setLanguage(msg.getLanguage());
					forwardMessage.setOntology(msg.getOntology());
					forwardMessage.addReceiver(this.requester);
					
					boolean waitForMore = false;
					try {
						ContentElement contentElement = myAgent.getContentManager().extractContent(msg);
						myAgent.getContentManager().fillContent(forwardMessage, contentElement);
						
						// --- Extract message contents ---------------
						if (contentElement instanceof Action) {
							Concept concept = ((Action)contentElement).getAction();
							if (concept instanceof FieldDataReply) {
								waitForMore = ((FieldDataReply)concept).getMoreComming();
							}
						}
					} catch (CodecException | OntologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					myAgent.send(forwardMessage);
					
					if (this.debug==true) {
						System.out.println(myAgent.getLocalName() + ": Forwarded field data to " + ((FieldDataProxyAgent)myAgent).getRequester().getName());
					}
					
					// --- Remove the sender from the waiting list --------
					if (waitForMore==false) {
						((FieldDataProxyAgent)myAgent).getWaitingForResponse().remove(sender.getLocalName());
					}
					
					// --- If this was the last outstanding response, terminate -----
					if (((FieldDataProxyAgent)myAgent).getWaitingForResponse().isEmpty()==true) {
						this.done = true;
					}
				}
			
			} else if (msg.getPerformative()==ACLMessage.FAILURE) {
				
				// --- Determine the receiver of the failed message -----------
				AID failedReceiver = null;
				try {
					failedReceiver = AMSService.getFailedReceiver(myAgent, msg);
				} catch (FIPAException e1) {
					e1.printStackTrace();
					System.err.println(myAgent.getLocalName() + ": Error extracting the original receiver from t he failure message");
				}
				
				if (failedReceiver!=null && failedReceiver.getName()!=this.requester.getLocalName()) {
					System.out.println(myAgent.getLocalName() + ": Received failure message - request could not be delivered to " + failedReceiver);
					
					// --- Send failure notification --------------------------
					ACLMessage failureMessage = new ACLMessage(ACLMessage.FAILURE);
					failureMessage.setConversationId(ConversationID.OPS_FIELD_DATA_REQUEST.toString());
					failureMessage.setContent(failedReceiver.getLocalName());
					failureMessage.addReceiver(this.requester);
					
					myAgent.send(failureMessage);
				}
			}
			
		} else {
			this.block();
		}
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#done()
	 */
	@Override
	public boolean done() {
		return done;
	}

	@Override
	public int onEnd() {
		// --- Job done, terminate --------------
		System.out.println(myAgent.getLocalName() + ": Job done, terminating...");
		myAgent.doDelete();
		return super.onEnd();
	}

	private MessageTemplate getMessageTemplate() {
		if (messageTemplate==null) {
			messageTemplate = MessageTemplate.MatchConversationId(ConversationID.OPS_FIELD_DATA_REQUEST.toString());
		}
		return messageTemplate;
	}
	
	

}
