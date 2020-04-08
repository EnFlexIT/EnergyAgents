package de.enflexit.energyAgent.core.centralExecutiveAgent.behaviour;

import java.io.IOException;

import de.enflexit.energyAgent.core.centralExecutiveAgent.CentralExecutiveAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class PhoneBookQueryBehaviour.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PhoneBookQueryBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -1762419685007328554L;

	private ACLMessage queryMessage;
	
	/**
	 * Instantiates a new phone book query behaviour.
	 */
	public PhoneBookQueryBehaviour(ACLMessage queryMessage) {
		this.queryMessage = queryMessage;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		ACLMessage reply = this.queryMessage.createReply();

		String agentsLocalName = this.queryMessage.getContent();
		AID aid = ((CentralExecutiveAgent)myAgent).getInternalDataModel().getPhoneBook().getAgentAID(agentsLocalName);
		if (aid!=null) {
			try {
				reply.setPerformative(ACLMessage.INFORM_REF);
				reply.setContentObject(aid);
				
			} catch (IOException e) {
				System.err.println(myAgent.getName() + ": Error setting the AID for agent " + aid.getName() + " as message content");
				reply.setPerformative(ACLMessage.FAILURE);
				e.printStackTrace();
			}
			
		} else {
			reply.setPerformative(ACLMessage.FAILURE);
		}
		myAgent.send(reply);
	}
	
}
