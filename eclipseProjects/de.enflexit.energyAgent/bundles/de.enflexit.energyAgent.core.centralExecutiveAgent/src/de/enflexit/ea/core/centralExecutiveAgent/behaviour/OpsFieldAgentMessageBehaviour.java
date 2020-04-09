package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.globalDataModel.phonebook.PhoneBook;
import de.enflexit.ea.core.globalDataModel.visualizationMessaging.FieldVisualizationMessagingHelper;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

/**
 * The Class OpsFieldAgentMessageBehaviour is used to forward messages from the OPs to an actual agent.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class OpsFieldAgentMessageBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 5312035297035246627L;

	private CentralExecutiveAgent cea;

	private ACLMessage fieldAgentMessage;
	private ACLMessage innerMessage;
	
	/**
	 * Instantiates a new ops field agent message behaviour.
	 *
	 * @param cea the instance of the {@link CentralExecutiveAgent}
	 * @param fieldAgentMessage the field agent message
	 */
	public OpsFieldAgentMessageBehaviour(ACLMessage fieldAgentMessage) {
		this.fieldAgentMessage = fieldAgentMessage;
	}
	
	/**
	 * Gets the central executive agent.
	 * @return the central executive agent
	 */
	private CentralExecutiveAgent getCentralExecutiveAgent() {
		if (cea==null) {
			cea = (CentralExecutiveAgent) this.myAgent;
		}
		return cea;
	}
	/**
	 * Return the CEA's phone book.
	 * @return the phone book
	 */
	private PhoneBook getPhoneBook() {
		return this.getCentralExecutiveAgent().getInternalDataModel().getPhoneBook();
	}
	
	/**
	 * Returns the inner message.
	 * @return the inner message
	 */
	public ACLMessage getInnerMessage() {
		if (innerMessage==null && this.fieldAgentMessage!=null) {

			// --- Try to get the inner message -----------
			try {
				innerMessage = (ACLMessage) this.fieldAgentMessage.getContentObject();
				
			} catch (UnreadableException urEx) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Could not get inner message of received field agent message.");
				urEx.printStackTrace();
			}
			
			// --- Prepare the inner Message --------------
			if (innerMessage!=null) {
				// --- Remove sender and receivers --------
				innerMessage.setSender(null);
				innerMessage.clearAllReceiver();
				innerMessage.clearAllReplyTo();

				// --- Remind the display agent AID -------
				AID displayAgentAID = this.fieldAgentMessage.getSender();
				String displayAgentName = displayAgentAID.getName();
				String [] addressesArray = displayAgentAID.getAddressesArray();
				String displayAgentAddresses = String.join(",", addressesArray);

				innerMessage.addUserDefinedParameter(MessageReceiveBehaviour.FIELD_AGENT_MESSAGE_DISPLAY_AGENT_NAME, displayAgentName);
				innerMessage.addUserDefinedParameter(MessageReceiveBehaviour.FIELD_AGENT_MESSAGE_DISPLAY_AGENT_ADDRESSES, displayAgentAddresses);
				
			}
		}
		return innerMessage;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		// --- Exit here, if something is already wrong ---
		if (this.fieldAgentMessage==null || this.getInnerMessage()==null) return;
			
		// --- Get local receiver names -------------------
		String receiverLocalNames = this.fieldAgentMessage.getUserDefinedParameter(FieldVisualizationMessagingHelper.WRAPPER_PARAMETER_MESSAGE_RECEIVER_LOCAL_NAMES);
		String[] receiverLocalArry = receiverLocalNames.split(",");
		for (int i = 0; i < receiverLocalArry.length; i++) {
			// ---- Get the AID of the receiver -----------
			AID receiver = this.getPhoneBook().getAgentAID(receiverLocalArry[i]);
			if (receiver==null) continue;
			// --- Send inner message to field agent ------ 
			ACLMessage innerAclMsg = this.getInnerMessage();
			innerAclMsg.addReceiver(receiver);
			this.myAgent.send(innerAclMsg);
		}
	}

}
