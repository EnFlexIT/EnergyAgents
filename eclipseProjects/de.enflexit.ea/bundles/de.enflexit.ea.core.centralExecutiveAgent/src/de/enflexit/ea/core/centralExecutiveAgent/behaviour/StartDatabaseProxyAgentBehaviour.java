package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import java.util.ArrayList;
import java.util.List;

import agentgui.core.application.Application;
import agentgui.core.jade.Platform;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.centralExecutiveAgent.InternalDataModel;
import de.enflexit.ea.core.dataModel.opsOntology.FieldDataRequest;
import de.enflexit.ea.core.fieldDataProxyAgent.FieldDataProxyAgent;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour starts a {@link FieldDataProxyAgent}, that will take care of a field data request.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class StartDatabaseProxyAgentBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 70283758810489352L;
	
	private ACLMessage requestMessage;
	
	private FieldDataRequest dataRequest;
	private List<AID> requestedAgentsAIDs;

	public StartDatabaseProxyAgentBehaviour(ACLMessage requestMessage) {
		this.requestMessage = requestMessage;
	}

	private List<AID> getRequestedAgentsAIDs() {
		if (requestedAgentsAIDs==null) {
			requestedAgentsAIDs = new ArrayList<AID>();
		}
		return requestedAgentsAIDs;
	}

	@Override
	public void action() {
		if (this.requestMessage!=null) {
			
			if (this.extractMessageContent()==true) {
				this.startAgent();
			} else {
				System.err.println(myAgent.getLocalName() + ": Could not start DatabaseProxyAgent, request details missing!");
			}
			
		} else {
			System.err.println(myAgent.getLocalName() + ": Could not start DatabaseProxyAgent, request details not specified!");
		}
	}
	
	
	/**
	 * Extracts all necessary information from the request message.
	 * @return true, if successful
	 */
	private boolean extractMessageContent() {
		
		try {
			
			// --- Extract message contents ---------------
			ContentElement contentAction = myAgent.getContentManager().extractContent(this.requestMessage);
			if (contentAction instanceof Action) {
				Concept content = ((Action)contentAction).getAction();
				if (content instanceof FieldDataRequest) {
					this.dataRequest = (FieldDataRequest) content;
				}
			} else {
				System.err.println(myAgent.getLocalName() + ": Could not start DatabaseProxyAgent, invalid message content!");
				return false;
			}
			
			// --- Get the agent's AIDs -------------------
			InternalDataModel idm = ((CentralExecutiveAgent)myAgent).getInternalDataModel();
			for (int i=0; i<dataRequest.getAgentIDs().size(); i++) {
				String localName = (String) dataRequest.getAgentIDs().get(i);
				AID aid = idm.getAidFromPhoneBook(localName);
				if (aid!=null) {
					this.getRequestedAgentsAIDs().add(aid);
				} else {
					//TODO No AID found, how to handle this?
				}
			}
			
		} catch (CodecException | OntologyException e) {
			System.err.println(myAgent.getLocalName() + ": Could not start DatabaseProxyAgent, error extracting message content!");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Starts the agent.
	 */
	private void startAgent() {
		Platform jadePlatform = Application.getJadePlatform();
		
		// --- Find an available name for the agent -------------
		String agentName = FieldDataProxyAgent.DEFAULT_AGENT_NAME;
		int suffix = 1;
		while (jadePlatform.isAgentRunning(agentName)==true) {
			agentName = FieldDataProxyAgent.DEFAULT_AGENT_NAME + suffix;
			suffix++;
		}
		
		Object[] startArgs = new Object[4];
		startArgs[0] = this.requestMessage.getSender();
		startArgs[1] = this.dataRequest.getScheduleRangeDefinition();
		startArgs[2] = this.getRequestedAgentsAIDs();
		
		jadePlatform.startAgent(agentName, FieldDataProxyAgent.class.getName(), startArgs);
	}

}
