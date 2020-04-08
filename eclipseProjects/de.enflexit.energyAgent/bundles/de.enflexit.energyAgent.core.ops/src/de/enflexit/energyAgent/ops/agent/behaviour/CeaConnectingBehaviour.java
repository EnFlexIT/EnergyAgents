package de.enflexit.energyAgent.ops.agent.behaviour;

import java.io.IOException;
import java.util.Vector;

import de.enflexit.energyAgent.core.globalDataModel.cea.ConversationID;
import de.enflexit.energyAgent.ops.agent.CeaConnectorAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class CeaConnectingBehaviour is used to establish the first connection
 * between CeaConnectorAgent and the CEA.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class CeaConnectingBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8478282690772054712L;

	private CeaConnectorAgent ceaConnectorAgent;
	
	/**
	 * Instantiates a new cea connecting behaviour.
	 * @param ceaConnectorAgent the cea connector agent
	 */
	public CeaConnectingBehaviour(CeaConnectorAgent ceaConnectorAgent) {
		this.ceaConnectorAgent = ceaConnectorAgent;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		// --- Reset counting within the MessageReceiveBehaviour ---- 
		this.ceaConnectorAgent.getMessageReceiveBehaviour().resetCountingForCeaAIDs();
		
		// --- Get the vector of possible CEA AID's -----------------
		this.ceaConnectorAgent.setStatusInfo("Receiving possible CEA AID's ...");
		Vector<AID> ceaAidVecort = this.ceaConnectorAgent.getInternalDataModel().getCentralAgentAIDVector();
		for (int i = 0; i < ceaAidVecort.size(); i++) {
			
			try {
				AID testAidOfCEA = ceaAidVecort.get(i);
				this.ceaConnectorAgent.setStatusInfo("Trying to connect to CEA with AID " +  testAidOfCEA.getName() + " ...");
				
				// --- Create and send the connecting message -----------
				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.addReceiver(testAidOfCEA);
				message.setConversationId(ConversationID.OPS_CONNECTING_REQUEST.toString());
				message.setContentObject(testAidOfCEA);
				this.myAgent.send(message);
				
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
		this.ceaConnectorAgent.setStatusInfo("Waiting for the CEA connection ...");
	}

	
}
