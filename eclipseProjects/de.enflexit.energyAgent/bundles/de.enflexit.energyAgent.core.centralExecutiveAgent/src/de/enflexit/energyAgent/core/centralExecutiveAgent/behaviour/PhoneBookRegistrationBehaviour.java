package de.enflexit.energyAgent.core.centralExecutiveAgent.behaviour;

import java.io.IOException;

import de.enflexit.energyAgent.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.energyAgent.core.globalDataModel.PlatformUpdater;
import de.enflexit.energyAgent.core.globalDataModel.phonebook.PhoneBookEntry;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

/**
 * The Class PhoneBookRegistrationBehaviour.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PhoneBookRegistrationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 7331118316645508869L;

	private ACLMessage phoneBookRegistrationRequest;
	
	/**
	 * Instantiates a new phone book registration behaviour.
	 * @param registrationRequest the registration request
	 */
	public PhoneBookRegistrationBehaviour(ACLMessage registrationRequest) {
		this.phoneBookRegistrationRequest = registrationRequest;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		CentralExecutiveAgent cea = (CentralExecutiveAgent) this.myAgent;

		// --- Extract message content ---------------------
		Object contentObject = null;
		try {
			contentObject = this.phoneBookRegistrationRequest.getContentObject();
		} catch (UnreadableException e1) {
			System.err.println(myAgent.getLocalName() + ": Error extracting content object from registration request");
		}
		
		if (contentObject!=null && contentObject instanceof PhoneBookEntry) {
			// --- If a pbe was sent, add it to the phone book ------
			cea.getInternalDataModel().getPhoneBook().addPhoneBookEntry((PhoneBookEntry) contentObject);
		} else {
			// --- If not, just add the AID -------------------------
			cea.getInternalDataModel().getPhoneBook().addAgentAID(this.phoneBookRegistrationRequest.getSender());
		}
		
		// --- Send a confirmation message --------------------------
		ACLMessage reply = this.phoneBookRegistrationRequest.createReply();
		reply.setPerformative(ACLMessage.CONFIRM);
		// --- Add CeaConfigModel to the reply ? --------------------
		if (PlatformUpdater.DEBUG_PLATFORM_UPDATE==true || cea.isExecutedInSimulation()==false) {
			try {
				reply.setContentObject(cea.getInternalDataModel().getCeaConfigModel());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		cea.send(reply);
	}
	
}
