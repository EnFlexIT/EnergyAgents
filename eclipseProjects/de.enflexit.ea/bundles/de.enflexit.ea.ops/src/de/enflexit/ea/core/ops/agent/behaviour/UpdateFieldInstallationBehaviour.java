package de.enflexit.ea.core.ops.agent.behaviour;

import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.ops.agent.CeaConnectorAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The Class UpdateFieldInstallationBehaviour is used to invoke the update mechanisms 
 * of the field installations. For this the CEA will be informed to forward this 
 * message to all known agents.  
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class UpdateFieldInstallationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8478282690772054712L;

	private static final long MAXIMUM_WAIT = 10000;
	
	private CeaConnectorAgent ceaConnectorAgent;
	
	/**
	 * Instantiates a new update field installation behaviour.
	 * @param ceaConnectorAgent the instance of the CEA connector agent
	 */
	public UpdateFieldInstallationBehaviour(CeaConnectorAgent ceaConnectorAgent) {
		this.ceaConnectorAgent = ceaConnectorAgent;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		AID aidOfCEA = this.ceaConnectorAgent.getOpsController().getCeaAID();
		if (aidOfCEA!=null) {
			// --- Create and send the update message ---------------
			ACLMessage message = new ACLMessage(ACLMessage.PROPAGATE);
			message.addReceiver(aidOfCEA);
			message.setConversationId(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS.toString());
			this.myAgent.send(message);
			
			// --- Wait for a reply ---------------------------------
			MessageTemplate mt = MessageTemplate.MatchConversationId(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS.toString());
			ACLMessage reply = this.myAgent.blockingReceive(mt, MAXIMUM_WAIT);
			// --- 
			if (reply!=null && reply.getPerformative()==ACLMessage.CONFIRM) {
				this.ceaConnectorAgent.setStatusInfo("CEA invoked on-site update process!");
			} else {
				if (reply==null) {
					this.ceaConnectorAgent.setStatusInfo("Could not get an answer for the invocation of the on-site update process!");
				} else {
					this.ceaConnectorAgent.setStatusInfo("Error while invoking the on-site update process!");
				}
			}
			
		} else {
			this.ceaConnectorAgent.setStatusInfo("Could not find AID of the CEA!");
		}
		
	}

}
