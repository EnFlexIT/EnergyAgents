package de.enflexit.ea.core.testbed.proxy;

import java.io.IOException;
import agentgui.simulationService.SimulationService;
import agentgui.simulationService.SimulationServiceHelper;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.globalDataModel.blackboard.BlackboardRequest;
import de.enflexit.ea.core.testbed.AgentNotificationContainer;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * A special message receive behaviour used by ProxyAgents to receive and process messages sent by their remote agent.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class ProxyAgentMessageReceiveBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = -191900206443647395L;
	
	private AID remoteAgentAID;
	private MessageTemplate messageTemplate;
	
	private SimulationServiceHelper simServiceHelper;
	
	private boolean debug = false;
	
	/**
	 * Constructor
	 * @param remoteAgentAID The AID of the remote agent
	 */
	public ProxyAgentMessageReceiveBehaviour(AID remoteAgentAID) {
		super();
		this.remoteAgentAID = remoteAgentAID;
	}


	@Override
	public void action() {
		
		ACLMessage message = myAgent.receive();
		if (message != null) {
			
			try {
				if (message.getConversationId()==null) {
					// --- No conversation ID -> not a proxy-specific message, forward ------- 
					this.forwardMessageToRemoteAgent(message);
					
				} else {
					
					switch(message.getConversationId()){
					case ProxyAgent.CONVERSATION_ID_AGENT_NOTIFICATION:
						
						this.debugPrint(myAgent.getLocalName() + ": Received agent notification from " + message.getSender().getName());
						AgentNotificationContainer notificationContainer = (AgentNotificationContainer) message.getContentObject();
						Object requestObject = notificationContainer.getNotification();
						if (requestObject instanceof BlackboardRequest) {
							((BlackboardRequest)requestObject).setRequester(myAgent.getAID());
						}
						EnvironmentNotification agentNotification = new EnvironmentNotification(myAgent.getAID(), false, notificationContainer.getNotification());
						this.getSimHelper().notifySensorAgent(notificationContainer.getReceiver(), agentNotification);
						break;
						
					case ProxyAgent.CONVERSATION_ID_DISPLAY_AGENT_NOTIFICATION:
						
						this.debugPrint(myAgent.getLocalName() + ": Received display agent notification from " + message.getSender().getName());
						EnvironmentNotification displayAgentNotification = new EnvironmentNotification(myAgent.getAID(), false, message.getContentObject());
						this.getSimHelper().displayAgentNotification(displayAgentNotification);
						break;
						
					case ProxyAgent.CONVERSATION_ID_MANAGER_NOTIFICATION:
	
						this.debugPrint(myAgent.getLocalName() + ": Received manager notification from " + message.getSender().getName());
						EnvironmentNotification managerNotification = new EnvironmentNotification(myAgent.getAID(), false, message.getContentObject());
						this.getSimHelper().notifyManagerAgent(managerNotification);
						break;
						
					case ProxyAgent.CONVERSATION_ID_STIMULUS_RESPONSE:
						
						this.debugPrint(myAgent.getLocalName() + ": Received stimulus answer from " + message.getSender().getName());
						Object myNextState = message.getContentObject();
						this.getSimHelper().setEnvironmentInstanceNextPart(myAgent.getAID(), myNextState);
						break;
						
					default:
						
						// --- No recognized conversation ID - probably a message for the remote agent --------
						this.debugPrint(myAgent.getLocalName() + ": Message from " + message.getSender().getName() + " will be forwarded to the remote agent");
						this.forwardMessageToRemoteAgent(message);
						
					}
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			
		} else {
			block();
		}
	}


	/**
	 * Gets a message template for receiving messages from this agent's remote agent.
	 *
	 * @return the message template
	 */
	public MessageTemplate getMessageTemplate() {
		if(this.messageTemplate == null){
			this.messageTemplate = MessageTemplate.MatchSender(this.remoteAgentAID);
		}
		return messageTemplate;
	}
	
	/**
	 * Forward message to remote agent.
	 * @param message the message
	 */
	private void forwardMessageToRemoteAgent(ACLMessage message){
		try {
			ACLMessage forwardMessage = new ACLMessage(ACLMessage.INFORM);
			forwardMessage.setConversationId(ProxyAgent.CONVERSATION_ID_FORWARDED_MESSAGE);
			forwardMessage.addReceiver(remoteAgentAID);
			forwardMessage.setContentObject(message);
			myAgent.send(forwardMessage);
		} catch (IOException e) {
			System.err.println(myAgent.getLocalName() + ": Error forwarding message to " + remoteAgentAID.getName());
			e.printStackTrace();
		}
	}


	/**
	 * Gets the sim helper.
	 * @return the sim helper
	 * @throws ServiceException the service exception
	 */
	private SimulationServiceHelper getSimHelper() throws ServiceException {
		if (simServiceHelper == null) {
			simServiceHelper = (SimulationServiceHelper) this.myAgent.getHelper(SimulationService.NAME);
		}
		return simServiceHelper;
	}
	
	/**
	 * Prints a string to the console in debug mode only
	 * @param debugString the debug string
	 */
	private void debugPrint(String debugString) {
		if (this.debug==true) {
			System.out.println(debugString);
		}
	}
	

}
