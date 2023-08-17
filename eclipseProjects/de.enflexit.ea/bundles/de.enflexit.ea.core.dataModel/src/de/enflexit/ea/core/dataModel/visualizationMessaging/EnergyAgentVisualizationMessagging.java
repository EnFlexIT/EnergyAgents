package de.enflexit.ea.core.dataModel.visualizationMessaging;

import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.ontology.ShowUiAction;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * The Class EnergyAgentVisualizationMessagging contains static methods to 
 * send visualization messages to an energy agent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EnergyAgentVisualizationMessagging {

	public enum EnergyAgentConversationID {
		ShowUI
	}
	
	/**
	 * Sends a message to show the UI of the specified energy agent.
	 * @param receiver the receiver
	 */
	public static void sendShowUIMessageToEnergyAgent(Agent sender,  AID receiver) {
		
		try {
			
			// --- Register codec and ontology to sender agent ------
			sender.getContentManager().registerLanguage(new SLCodec());
			sender.getContentManager().registerOntology(HyGridOntology.getInstance());
			
			// --- Prepare message ----------------------------------
			ACLMessage acl = new ACLMessage(ACLMessage.INFORM);
			acl.setConversationId(EnergyAgentConversationID.ShowUI.name());
			acl.setPerformative(ACLMessage.REQUEST);
			acl.addReceiver(receiver);
			acl.setLanguage(new SLCodec().getName());
			acl.setOntology(HyGridOntology.getInstance().getName());

			// --- Prepare content and send message -----------------
			sender.getContentManager().fillContent(acl, new Action(sender.getAID(), new ShowUiAction()));
			sender.send(acl);
			
		} catch (CodecException | OntologyException ex) {
			ex.printStackTrace();
		}
	}
	
}
