package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import de.enflexit.ea.core.globalDataModel.cea.ConversationID;
import de.enflexit.ea.core.globalDataModel.ontology.CEARegistrationResponse;
import de.enflexit.ea.core.globalDataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.globalDataModel.ontology.RemoteAgentInformation;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class ProxyCounterPartNotificationBehaviour.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ProxyCounterPartNotificationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -8544327688799379219L;

	private RemoteAgentInformation remoteAgentInfo;
	
	/**
	 * Instantiates a new send counter part notification behaviour.
	 * @param remoteAgentInfo the remote agent info
	 */
	public ProxyCounterPartNotificationBehaviour(RemoteAgentInformation remoteAgentInfo) {
		this.remoteAgentInfo = remoteAgentInfo;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		this.sendCounterPartNotifications();
	}

	/**
	 * Sends notifications about their counterpart to both partners.
	 * @param entry the directory entry containing the AIDs
	 */
	public void sendCounterPartNotifications(){
		
		// --- Send the ProxyAgent's AID to the remote agent ----------
		CEARegistrationResponse responseForRemoteAgent = new CEARegistrationResponse();
		responseForRemoteAgent.setCounterpartAID(this.remoteAgentInfo.getProxyAgentAID());
		Action remoteAgentAction = new Action(this.myAgent.getAID(), responseForRemoteAgent);
		
		// --- Create and send ACL Message ----------------
		try {
			ACLMessage notificationForRemoteAgent = this.createCounterPartNotificationMessage(remoteAgentAction);
			notificationForRemoteAgent.addReceiver(this.remoteAgentInfo.getRemoteAgentAID());
			this.myAgent.send(notificationForRemoteAgent);
			
		} catch (CodecException | OntologyException e) {
			System.err.println(this.myAgent.getLocalName() + ": Error creating notification message for remote agent " + this.remoteAgentInfo.getRemoteAgentAID().getName());
			e.printStackTrace();
		}
		
		
		// --- Send the remote agent's AID to the ProxyAgent -----------
		CEARegistrationResponse responseForProxyAgent = new CEARegistrationResponse();
		responseForProxyAgent.setCounterpartAID(this.remoteAgentInfo.getRemoteAgentAID());
		Action proxyAgentAction = new Action(this.myAgent.getAID(), responseForProxyAgent);
		
		// --- Create and send ACL message --------
		try {
			ACLMessage notificationForProxyAgent = this.createCounterPartNotificationMessage(proxyAgentAction);
			notificationForProxyAgent.addReceiver(this.remoteAgentInfo.getProxyAgentAID());
			this.myAgent.send(notificationForProxyAgent);
			
		} catch (CodecException | OntologyException e) {
			System.err.println(this.myAgent.getLocalName() + ": Error creating notification message for proxy agent " + this.remoteAgentInfo.getProxyAgentAID().getName());
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the counter part notification message.
	 *
	 * @param content The agent action to send
	 * @return the notification message
	 * @throws CodecException Thrown in case of problems with the language codec
	 * @throws OntologyException the ontology exception Thrown in case of problems with the ontology
	 */
	private ACLMessage createCounterPartNotificationMessage(Action content) throws CodecException, OntologyException{
		
		ACLMessage notificationMessage = new ACLMessage(ACLMessage.INFORM);
		notificationMessage.setSender(this.myAgent.getAID());
		notificationMessage.setConversationId(ConversationID.PROXY_REGISTRATION.toString());
		notificationMessage.setLanguage(new SLCodec().getName());
		notificationMessage.setOntology(HyGridOntology.getInstance().getName());
	
		this.myAgent.getContentManager().fillContent(notificationMessage, content);
		
		return notificationMessage;
	}
	
}
