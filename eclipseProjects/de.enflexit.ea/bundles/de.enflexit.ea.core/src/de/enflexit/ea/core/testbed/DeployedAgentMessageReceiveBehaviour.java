package de.enflexit.ea.core.testbed;

import de.enflexit.awb.simulation.environment.EnvironmentModel;
import de.enflexit.awb.simulation.transaction.EnvironmentNotification;
import de.enflexit.ea.core.SimulationConnector;
import de.enflexit.ea.core.SimulationConnectorRemote;
import de.enflexit.ea.core.testbed.proxy.ProxyAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * A special message receive behaviour for handling incoming messages from proxy agents
 * and passing them to the deployed agent's {@link SimulationConnector}.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class DeployedAgentMessageReceiveBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = -3690792581085426262L;
	private SimulationConnectorRemote simulationConnector;
	private MessageTemplate messageTemplate;
	
	/**
	 * Instantiates a new ProxyAgentMessageReceiveBehaviour
	 *
	 * @param agent the testbed agent using this behaviour
	 * @param simulationConnector the simulation connector
	 */
	public DeployedAgentMessageReceiveBehaviour(Agent agent, SimulationConnectorRemote simulationConnector) {
		super(agent);
		this.simulationConnector = simulationConnector;
	}
	
	/**
	 * Returns the {@link MessageTemplate} for this behaviour, which matches all messages that are sent from the CEA or from the agent's proxy agent. 
	 * @return The message template
	 */
	protected MessageTemplate getMessageTemplate() {
		if (messageTemplate==null) {
			AID proxyAgentAID = this.simulationConnector.getProxyAgentAID();
			this.messageTemplate = MessageTemplate.MatchSender(proxyAgentAID);
		}
		return messageTemplate;
	}
	
	/**
	 * Sets the message template.
	 * @param messageTemplate the new message template
	 */
	public void setMessageTemplate(MessageTemplate messageTemplate) {
		this.messageTemplate = messageTemplate;
	}

	@Override
	public void action() {
		
		ACLMessage message = this.myAgent.receive(this.getMessageTemplate());
		if (message!=null) {
			
			// -----------------------------
			// --- Process message ---------
			// -----------------------------
			try {
				if (message.getConversationId()!=null) {
					
					switch (message.getConversationId()) {
						case ProxyAgent.CONVERSATION_ID_ENVIRONMENT_STIMULUS:
							EnvironmentModel envModel = (EnvironmentModel) message.getContentObject();
							this.simulationConnector.setEnvironmentModel(envModel, false);
							break;
							
						case ProxyAgent.CONVERSATION_ID_ENVIRONMENT_NOTIFICATION:
							EnvironmentNotification notification = (EnvironmentNotification) message.getContentObject();
							this.simulationConnector.setNotification(notification);
							break;
							
						case ProxyAgent.CONVERSATION_ID_PAUSE_SIMULATION:
							Boolean isPauseSimulation = (Boolean) message.getContentObject();
							this.simulationConnector.setPauseSimulation(isPauseSimulation);
							break;
							
						case ProxyAgent.CONVERSATION_ID_DO_DELETE:
							this.simulationConnector.doDelete();
							break;
							
						case ProxyAgent.CONVERSATION_ID_FORWARDED_MESSAGE:
							//  --- Put the forwarded message into the agent's message queue --------------
							ACLMessage forwardedMessage = (ACLMessage) message.getContentObject();
							myAgent.postMessage(forwardedMessage);
							break;
							
						default:
							System.err.println(myAgent.getLocalName() + ": Received message with unknown conversation ID from " + simulationConnector.getProxyAgentAID().getName());
					}
					
				}
				
			} catch (UnreadableException e) {
				System.err.println(myAgent.getLocalName() + ": Error extracting message content!");
				e.printStackTrace();
			}
			
		} else {
			// --- wait for the next incoming message ---
			block();
		}
		
	}


}
