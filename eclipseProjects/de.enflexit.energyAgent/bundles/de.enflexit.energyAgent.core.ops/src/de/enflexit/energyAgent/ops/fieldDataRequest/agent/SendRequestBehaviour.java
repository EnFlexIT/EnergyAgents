package de.enflexit.energyAgent.ops.fieldDataRequest.agent;

import de.enflexit.energyAgent.core.globalDataModel.cea.ConversationID;
import de.enflexit.energyAgent.ops.OpsController;
import hygrid.ops.ontology.FieldDataRequest;
import hygrid.ops.ontology.OpsOntology;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * THis behaviour is responsible for sending field data requests tot he CEA.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SendRequestBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -4394816087383731207L;
	
	private boolean debug = false;

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		InternalDataModel idm = ((FieldDataRequestAgent)myAgent).getInternalDataModel();
		
		// --- Initialize the request states --------------
		for (int i=0; i<idm.getDataRequest().getAgentIDs().size(); i++) {
			String agentID = (String) idm.getDataRequest().getAgentIDs().get(i);
			idm.getPendingReplies().add(agentID);
		}
		
		// --- Prepare and send the request --------------- 
		ACLMessage requestMessage = this.prepareRequestMessage(idm.getCeaAID(), idm.getDataRequest());
		myAgent.send(requestMessage);
		
		// --- Show status info to the user ---------------
		String statusInfo = "Field data request was sent to " + idm.getCeaAID().getName() + ", waiting for replies...";
		OpsController.getInstance().setStatusBarMessage(statusInfo);
		
		if (this.debug==true) {
			System.out.println(myAgent.getLocalName() + ": " + statusInfo);
			
		}
	}
	
	/**
	 * Prepare request message.
	 * @return the ACL message
	 */
	private ACLMessage prepareRequestMessage(AID ceaAID, FieldDataRequest dataRequest) {
		ACLMessage requestMessage = null;
		try {
			requestMessage = new ACLMessage(ACLMessage.REQUEST);
			requestMessage.setConversationId(ConversationID.OPS_FIELD_DATA_REQUEST.toString());
			requestMessage.addReceiver(ceaAID);
			requestMessage.setLanguage(new SLCodec().getName());
			requestMessage.setOntology(OpsOntology.getInstance().getName());
			
			Action requestAction = new Action(this.myAgent.getAID(), dataRequest);
			myAgent.getContentManager().fillContent(requestMessage, requestAction);
		} catch (CodecException | OntologyException e) {
			System.err.println(myAgent.getName() + ": Ontology Error: Could not set message content!");
		}
		return requestMessage;
	}

}
