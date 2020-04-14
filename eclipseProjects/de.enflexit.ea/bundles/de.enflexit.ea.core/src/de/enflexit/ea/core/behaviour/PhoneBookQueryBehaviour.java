package de.enflexit.ea.core.behaviour;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.globalDataModel.cea.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This behaviour can be used to request an agent's AID from the CEA. 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class PhoneBookQueryBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8764091891166343355L;
	
	private static final long RETRY_TIME = 5000;
	private static final long MAXIMUM_WAIT = 5000;
	
	private AID ceaAID;
	private String localName;
	private boolean retryOnFailure;

	/**
	 * Instantiates a new phone book query behaviour.
	 * @param ceaAID the AID of the {@link CentralExecutiveAgent}
	 * @param localName the local name of the agent to look up
	 */
	public PhoneBookQueryBehaviour(AID ceaAID, String localName) {
		this(ceaAID, localName, false);
	}

	/**
	 * Instantiates a new phone book query behaviour.
	 * @param ceaAID the AID of the {@link CentralExecutiveAgent}
	 * @param localName the local name of the agent to look up
	 * @param retryOnFailure if true, the query will be repeated on failure
	 */
	public PhoneBookQueryBehaviour(AID ceaAID, String localName, boolean retryOnFailure) {
		this.ceaAID = ceaAID;
		this.localName = localName;
		this.retryOnFailure = retryOnFailure;
	}



	@Override
	public void action() {
		
		// --- In case that no CEA was specified ------------------------------
		if (this.ceaAID==null) return;
		
		// --- Prepare and send the query ---------------------------
		ACLMessage request = new ACLMessage(ACLMessage.QUERY_REF);
		request.setConversationId(ConversationID.PHONEBOOK_QUERY_SIMPLE.toString());
		request.setContent(this.localName);
		request.addReceiver(this.ceaAID);
		myAgent.send(request);
		
		if (this.retryOnFailure==true) {
			// --- Wait for the reply ---------------------
			MessageTemplate mt = MessageTemplate.MatchConversationId(ConversationID.PHONEBOOK_QUERY_SIMPLE.toString());
			ACLMessage reply = myAgent.blockingReceive(mt, MAXIMUM_WAIT);
			
			if (reply!=null && reply.getPerformative()==ACLMessage.INFORM_REF) {
				
				// --- Request successful - extract the AID and add it to the local phone book ----
				AID requestedAID = null;
				try {
					requestedAID = (AID) reply.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				if (requestedAID!=null) {
					((AbstractEnergyAgent)myAgent).getInternalDataModel().addAidToPhoneBook(requestedAID);
				}
				
			} else {
				// --- Request failed - try again after the specified waiting time ----------------
				System.out.println(myAgent.getLocalName() + ": Phonebook query failed, trying again after " + RETRY_TIME/1000 + " seconds");
				myAgent.addBehaviour(new RetryQueryBehaviour(myAgent, RETRY_TIME));
			}
		}
	}
	
	/**
	 * This {@link WakerBehaviour} will reschedule the {@link PhoneBookQueryBehaviour} after a specified time.
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class RetryQueryBehaviour extends WakerBehaviour{
		
		private static final long serialVersionUID = -4180203573762714628L;

		/**
		 * Instantiates a new retry query behaviour.
		 *
		 * @param agent the agent
		 * @param timeout the timeout
		 */
		public RetryQueryBehaviour(Agent agent, long timeout) {
			super(agent, timeout);
		}

		@Override
		protected void onWake() {
			myAgent.addBehaviour(new PhoneBookQueryBehaviour(ceaAID, localName, true));
		}
		
	}

}
