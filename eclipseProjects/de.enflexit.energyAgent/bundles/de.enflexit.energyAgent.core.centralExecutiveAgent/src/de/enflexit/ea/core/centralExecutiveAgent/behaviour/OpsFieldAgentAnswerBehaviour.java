package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class OpsFieldAgentAnswerBehaviour is used to forward a messages from a field agent 
 * to the a display agent, specified in the user defined parameter of a message.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class OpsFieldAgentAnswerBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 5312035297035246627L;

	private ACLMessage fieldAgentAnswer;
	
	/**
	 * Instantiates a new ops field agent answer behaviour.
	 *
	 * @param cea the instance of the {@link CentralExecutiveAgent}
	 * @param fieldAgentAnswer the field agent message
	 */
	public OpsFieldAgentAnswerBehaviour(ACLMessage fieldAgentAnswer) {
		this.fieldAgentAnswer = fieldAgentAnswer;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		// --- Exit here, if something is already wrong ---
		if (this.fieldAgentAnswer==null || MessageReceiveBehaviour.isDisplayAgentMessage(this.fieldAgentAnswer)==false) return;
		
		// --- Define display agent AID -------------------
		String displayAgentName = this.fieldAgentAnswer.getUserDefinedParameter(MessageReceiveBehaviour.FIELD_AGENT_MESSAGE_DISPLAY_AGENT_NAME);
		String displayAgentAddresses = this.fieldAgentAnswer.getUserDefinedParameter(MessageReceiveBehaviour.FIELD_AGENT_MESSAGE_DISPLAY_AGENT_ADDRESSES); 
		String[] displayAgentAddressesArry = displayAgentAddresses.split(",");
		
		// ---- Get the display agent AID -----------------
		AID displayAgentAID = new AID(displayAgentName, true);
		for (int i = 0; i < displayAgentAddressesArry.length; i++) {
			displayAgentAID.addAddresses(displayAgentAddressesArry[i]);
		}
				
		// --- Prepare the message ------------------------
		this.fieldAgentAnswer.setSender(null);
		this.fieldAgentAnswer.clearAllReceiver();
		this.fieldAgentAnswer.clearAllReplyTo();
		
		// --- Finally, send the message ------------------
		this.fieldAgentAnswer.addReceiver(displayAgentAID);
		this.myAgent.send(this.fieldAgentAnswer);
	}

}
