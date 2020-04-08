package de.enflexit.energyAgent.core.fieldDataProxyAgent;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.energyAgent.core.globalDataModel.cea.ConversationID;
import hygrid.ops.ontology.FieldDataRequest;
import hygrid.ops.ontology.OpsOntology;
import hygrid.ops.ontology.ScheduleRangeDefinition;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour sends a field data requests to all specified agents.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SendRequestsBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 8837361138518881421L;

	private ScheduleRangeDefinition rangeDefinition;
	private List<AID> agentAIDs;
	
	/**
	 * Instantiates a new send requests behaviour.
	 * @param rangeDefinition the range definition
	 * @param agentAIDs the agent AI ds
	 */
	public SendRequestsBehaviour(ScheduleRangeDefinition rangeDefinition, List<AID> agentAIDs) {
		super();
		this.rangeDefinition = rangeDefinition;
		this.agentAIDs = agentAIDs;
	}

	/**
	 * Gets the agent AIDs.
	 * @return the agent AIDs
	 */
	private List<AID> getAgentAIDs() {
		if (agentAIDs==null) {
			agentAIDs = new ArrayList<>();
		}
		return agentAIDs;
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		ACLMessage message = this.prepareMessage();
		if (message!=null) {
			for(int i=0; i<this.getAgentAIDs().size(); i++) {
			AID agentAid = this.getAgentAIDs().get(i);
				((FieldDataProxyAgent)myAgent).getWaitingForResponse().add(agentAid.getLocalName());
				message.addReceiver(agentAid);
			}
			myAgent.send(message);
		} else {
			System.err.println(myAgent.getLocalName() + ": Error creating field data request message!");
		}
	}
	
	/**
	 * Prepare message.
	 * @param agent the agent
	 * @return the ACL message
	 */
	private ACLMessage prepareMessage() {
		ACLMessage message = null;
		
		try {
			message = new ACLMessage(ACLMessage.REQUEST);
			message.setConversationId(ConversationID.OPS_FIELD_DATA_REQUEST.toString());
			message.setLanguage(new SLCodec().getName());
			message.setOntology(OpsOntology.getInstance().getName());
			FieldDataRequest request = new FieldDataRequest();
			request.setScheduleRangeDefinition(this.rangeDefinition);
			Action contentAction = new Action(myAgent.getAID(), request);
			myAgent.getContentManager().fillContent(message, contentAction);
		} catch (CodecException | OntologyException e) {
			System.err.println(myAgent.getName() + ": Ontology Error: Could not set message content!");
		}
		
		return message;
	}

}
