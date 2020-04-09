package de.enflexit.ea.core.behaviour;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.databaseRequest.DatabaseRequestInterface;
import hygrid.ops.ontology.FieldDataRequest;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour handles incoming requests for field data. It gets the requested
 * data from the local database and sends it to the requesting agent.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class HandleFieldDataRequestBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -5726495090150875111L;
	private ACLMessage requestMessage;

	public HandleFieldDataRequestBehaviour(ACLMessage requestMessage) {
		super();
		this.requestMessage = requestMessage;
	}


	@Override
	public void action() {
		AbstractEnergyAgent energyAgent = (AbstractEnergyAgent) myAgent;
		if (energyAgent.getInternalDataModel().getFieldDataRequestMessage()==null) {
			energyAgent.getInternalDataModel().setFieldDataRequestMessage(requestMessage);
			
			DatabaseRequestInterface dbInterface = DatabaseRequestInterface.getInstance();
			FieldDataRequest request = this.extractFieldDataRequestFromMessage(requestMessage);
			request.getAgentIDs().clear();
			request.getAgentIDs().add(myAgent.getLocalName());
			dbInterface.requestFieldData(energyAgent, request);
			
		} else {
			//TODO handle multiple parallel requests
			System.err.println(myAgent.getLocalName() + ": HandleFieldDataRequestBehaviour currently running, multiple parallel requests are not supported");
		}
	}
	
	
	/**
	 * Extract field data request from message.
	 * @param requestMessage the request message
	 * @return the field data request
	 */
	private FieldDataRequest extractFieldDataRequestFromMessage(ACLMessage requestMessage) {
		FieldDataRequest fieldDataRequest = null;
		try {
			ContentElement contentElement = myAgent.getContentManager().extractContent(requestMessage);
			
			// --- Extract message contents ---------------
			if (contentElement instanceof Action) {
				Concept concept = ((Action)contentElement).getAction();
				if (concept instanceof FieldDataRequest) {
					fieldDataRequest = (FieldDataRequest) concept;
				}
			}
		} catch (CodecException | OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fieldDataRequest;
	}

}
