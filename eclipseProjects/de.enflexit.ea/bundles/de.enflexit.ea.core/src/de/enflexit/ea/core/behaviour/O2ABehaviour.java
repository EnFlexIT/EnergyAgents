package de.enflexit.ea.core.behaviour;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.dataModel.opsOntology.FieldDataReply;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour is handling the for {@link AbstractEnergyAgent}s incoming O2A-Communication 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class O2ABehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 5650249408490429966L;

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		Object appNoteObject = this.myAgent.getO2AObject();
		if (appNoteObject!=null) {
			
			if (appNoteObject instanceof FieldDataReply) {
				
				FieldDataReply reply = (FieldDataReply) appNoteObject;
				ACLMessage replyMessage = this.createFieldDataReplyMessage(reply);
				
				if (replyMessage!=null) {
					myAgent.send(replyMessage);
					if (reply.getMoreComming()==false) {
						((AbstractEnergyAgent)myAgent).getInternalDataModel().setFieldDataRequestMessage(null);
					}
				}
			}
			
		} else {
			block();
		}
	}
	
	
	/**
	 * Creates the field data reply message.
	 * @param reply the reply
	 * @return the ACL message
	 */
	private ACLMessage createFieldDataReplyMessage(FieldDataReply reply) {
		ACLMessage replyMessage = null;
		ACLMessage requestMessage = ((AbstractEnergyAgent)this.myAgent).getInternalDataModel().getFieldDataRequestMessage();
		if (requestMessage!=null) {
			try {
				replyMessage = requestMessage.createReply();
				replyMessage.setPerformative(ACLMessage.INFORM);
				
				Action replyAction = new Action(myAgent.getAID(), reply);
				myAgent.getContentManager().fillContent(replyMessage, replyAction);
			} catch (CodecException | OntologyException e) {
				System.err.println(myAgent.getLocalName() + ": Error creating field data reply message!");
				e.printStackTrace();
			}
		}
		return replyMessage;
	}

}
