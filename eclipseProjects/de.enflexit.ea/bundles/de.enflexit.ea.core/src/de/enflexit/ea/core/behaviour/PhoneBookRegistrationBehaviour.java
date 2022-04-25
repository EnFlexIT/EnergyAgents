package de.enflexit.ea.core.behaviour;

import java.io.IOException;
import java.io.Serializable;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.dataModel.PlatformUpdater;
import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.phonebook.PhoneBookEntry;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This behaviour sends a message to the CentralExecutiveAgent to be registered in the central phone book
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class PhoneBookRegistrationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 2703064014472915717L;
	
	private static final long RETRY_TIME_SIMULATION = 5000;
	private static final long MAXIMUM_WAIT = 5000;
	
	private AID agentAID;
	private PhoneBookEntry myPhoneBookEntry;
	private String typeString;
	
	private boolean retryOnFailure;
	
	/**
	 * Instantiates a new phone book registration behaviour.
	 * @param centralAgentAID the {@link CentralExecutiveAgent}'s {@link AID}
	 * @param retryOnFailure if true, the agent will wait for the result and schedule another try on failure
	 */
	public PhoneBookRegistrationBehaviour(AID centralAgentAID, boolean retryOnFailure) {
		this.agentAID = centralAgentAID;
		this.myPhoneBookEntry = ((AbstractEnergyAgent)myAgent).getInternalDataModel().getMyPhoneBookEntry();
		this.retryOnFailure = retryOnFailure;
	}
	
	public PhoneBookRegistrationBehaviour(AID agentAID, PhoneBookEntry myPhoneBookEntry, String typeString, boolean retryOnFailure) {
		this.agentAID = agentAID;
		this.typeString = typeString;
		this.myPhoneBookEntry = this.createMyPhoneBookEntry();
		this.retryOnFailure = retryOnFailure;
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		// --- In case that no CEA was specified ------------------------------
		if (this.agentAID==null) return;
		
		try {
			// --- Create and send the registration message -----------------------
			ACLMessage message = new ACLMessage(ACLMessage.INFORM_REF);
			message.addReceiver(this.agentAID);
			message.setConversationId(ConversationID.PHONEBOOK_REGISTRATION.toString());
			message.setContentObject(this.myPhoneBookEntry);
			this.myAgent.send(message);
			
		} catch (IOException e1) {
			System.err.println(myAgent.getLocalName() + ": Error creating phone book registration message");
		}
		
		// --- Retry on failure, if configured --------------------------------
		if (this.retryOnFailure==true) {
			
			// --- Wait for the reply -----------------------------------------
			MessageTemplate mt = MessageTemplate.MatchConversationId(ConversationID.PHONEBOOK_REGISTRATION.toString());
			ACLMessage reply = this.myAgent.blockingReceive(mt, MAXIMUM_WAIT);
			
			// --- If there was no reply or no confirmation, try again --------
			if (reply==null || reply.getPerformative()!=ACLMessage.CONFIRM) {
				//System.out.println(myAgent.getLocalName() + ": Phonebook registration failed, trying again after " + RETRY_TIME_SIMULATION/1000 + " seconds");
				this.myAgent.addBehaviour(new RetryRegistrationBehaviour(this.myAgent, this.agentAID, RETRY_TIME_SIMULATION));
				
			} else {
				// --- Set the CeaConfigModel to the internal data model ? ----
				if (this.myAgent instanceof AbstractEnergyAgent) {
					
					AbstractEnergyAgent energyAgent = (AbstractEnergyAgent) this.myAgent;
					if (PlatformUpdater.DEBUG_PLATFORM_UPDATE==true || energyAgent.getAgentOperatingMode()!=AgentOperatingMode.Simulation) {
						// --- Only in deployed cases -------------------------
						try {
							Serializable ccmObject = reply.getContentObject();
							if (ccmObject!=null && ccmObject instanceof CeaConfigModel) {
								CeaConfigModel ceaConfigModel = (CeaConfigModel) ccmObject;
								energyAgent.getInternalDataModel().setCeaConfigModel(ceaConfigModel);
							}
							
						} catch (UnreadableException e) {
							e.printStackTrace();
						}	
					}
				}
				
			}
		}
	}
	
	/**
	 * Gets the agent's own phone book entry.
	 * @return the agent's own phone book entry
	 */
	public PhoneBookEntry createMyPhoneBookEntry() {
		PhoneBookEntry ownPhoneBookEntry = new PhoneBookEntry();
		ownPhoneBookEntry.setAID(myAgent.getAID());
		ownPhoneBookEntry.setComponentType(this.typeString);
		ownPhoneBookEntry.setControllable(false);
		return ownPhoneBookEntry;
	}

	/**
	 * This class reschedules the registration behaviour after a specified time.
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class RetryRegistrationBehaviour extends WakerBehaviour {
		
		private static final long serialVersionUID = 1648437300205920042L;

		private AID ceaAID;

		/**
		 * Instantiates a new retry registration behaviour.
		 * @param agent the agent
		 * @param ceaAID the cea AID
		 * @param timeout the timeout
		 */
		public RetryRegistrationBehaviour(Agent agent, AID ceaAID, long timeout) {
			super(agent, timeout);
			this.ceaAID = ceaAID;
		}

		/* (non-Javadoc)
		 * @see jade.core.behaviours.WakerBehaviour#onWake()
		 */
		@Override
		protected void onWake() {
			this.myAgent.addBehaviour(new PhoneBookRegistrationBehaviour(ceaAID, true));
		}
		
	}

}
