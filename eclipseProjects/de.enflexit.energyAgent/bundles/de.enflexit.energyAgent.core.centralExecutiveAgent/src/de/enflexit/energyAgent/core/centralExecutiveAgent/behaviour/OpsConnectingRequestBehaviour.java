package de.enflexit.energyAgent.core.centralExecutiveAgent.behaviour;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

/**
 * The Class OspConnectingRequestBehaviour.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class OpsConnectingRequestBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 5312035297035246627L;

	private ACLMessage requestMessage;
	
	/**
	 * Instantiates a new osp connecting request behaviour.
	 * @param requestMessage the request message
	 */
	public OpsConnectingRequestBehaviour(ACLMessage requestMessage) {
		this.requestMessage = requestMessage;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		try {
			// --- Get the tested AID ---------------------
			AID testedAID = (AID) this.requestMessage.getContentObject();
			
			// --- Define the reply message ---------------
			ACLMessage reply = this.requestMessage.createReply();
			reply.setPerformative(ACLMessage.CONFIRM);
			reply.setContentObject(testedAID);
			
			this.myAgent.send(reply);
			
		} catch (UnreadableException | IOException unReEx) {
			unReEx.printStackTrace();
		}
	}

}
