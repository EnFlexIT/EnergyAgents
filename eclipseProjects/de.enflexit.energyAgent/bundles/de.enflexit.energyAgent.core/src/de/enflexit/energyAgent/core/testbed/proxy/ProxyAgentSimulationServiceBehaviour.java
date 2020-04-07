package de.enflexit.energyAgent.core.testbed.proxy;

import java.io.IOException;
import java.io.Serializable;

import agentgui.simulationService.behaviour.SimulationServiceBehaviour;
import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.transaction.EnvironmentNotification;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.lang.acl.ACLMessage;

public class ProxyAgentSimulationServiceBehaviour extends SimulationServiceBehaviour {
	
	private static final long serialVersionUID = -4127659334265406223L;
	
	private AID remoteAgentAID;
	
	

	/**
	 * Instantiates a new proxy agent simulation service behaviour.
	 *
	 * @param proxyAgent the proxy agent
	 */
	public ProxyAgentSimulationServiceBehaviour(Agent proxyAgent) {
		super(proxyAgent);
	}

	/**
	 * Instantiates a new proxy agent simulation service behaviour.
	 *
	 * @param agent the agent
	 * @param passive the passive
	 */
	public ProxyAgentSimulationServiceBehaviour(Agent agent, boolean passive) {
		super(agent, passive);
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setPauseSimulation(boolean)
	 */
	@Override
	public void setPauseSimulation(boolean isPauseSimulation) {
		try {
			System.out.println(myAgent.getLocalName() + ": Simulation paused: " + isPauseSimulation);
			ACLMessage message = this.createMessage(ProxyAgent.CONVERSATION_ID_PAUSE_SIMULATION, new Boolean(isPauseSimulation));
			myAgent.send(message);
		} catch (IOException e) {
			System.err.println(myAgent.getLocalName() + ": Error sending pause notification to " + remoteAgentAID.getName());
			e.printStackTrace();
		}
		
	}
	
	

	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#doDelete()
	 */
	@Override
	public void doDelete() {
		try {
			// --- Send notification to the remote agent ----------------
			System.out.println(myAgent.getLocalName() + ": Sending shutdown notification to " + remoteAgentAID.getName());
			ACLMessage message = this.createMessage(ProxyAgent.CONVERSATION_ID_DO_DELETE, null);
			myAgent.send(message);
		} catch (IOException e) {
			System.err.println(myAgent.getLocalName() + ": Error sending stop notification to " + remoteAgentAID.getName());
			e.printStackTrace();
		}
		
		// --- Delete the proxy agent -------------------
		super.doDelete();
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#setMigration(jade.core.Location)
	 */
	@Override
	public void setMigration(Location newLocation) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentStimulus()
	 */
	@Override
	public void onEnvironmentStimulus() {

		// -------------------------------------------------------------------
		// --- Send the environment model to the remote agent ----------------
		// -------------------------------------------------------------------
		
		if (myEnvironmentModel!=null && remoteAgentAID!=null){
			
			try {
				
				// --- Prepare environment model for sending -----------
				EnvironmentModel environmentModelToSend = new EnvironmentModel();
				environmentModelToSend.setTimeModel(this.myEnvironmentModel.getTimeModel());
				environmentModelToSend.setAbstractEnvironment(this.myEnvironmentModel.getAbstractEnvironment());
				
				// --- Create an ACL message containing the environment model and send it ------------
				ACLMessage environmentStimulusMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_ENVIRONMENT_STIMULUS, environmentModelToSend);
				myAgent.send(environmentStimulusMessage);
				
			} catch (IOException e) {
				System.err.println(myAgent.getLocalName() + ": Error sending environment stimulus to " + remoteAgentAID.getName());
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification) {
		
		try {
			ACLMessage environmentNotificationMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_ENVIRONMENT_NOTIFICATION, notification);
			myAgent.send(environmentNotificationMessage);
		} catch (IOException e) {
			System.err.println(myAgent.getLocalName() + ": Error sending environment notification to " + remoteAgentAID.getName());
			e.printStackTrace();
		}
		
		return super.onEnvironmentNotification(notification);
	}

	/**
	 * Sets the remote agent AID.
	 * @param remoteAgentAID the new remote agent AID
	 */
	public void setRemoteAgentAID(AID remoteAgentAID){
		this.remoteAgentAID = remoteAgentAID;
	}
	
	
	/**
	 * Creates an ACL message t be sent to the remote agent.
	 *
	 * @param conversationID the conversation ID
	 * @param content the content
	 * @return the ACL message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private ACLMessage createMessage(String conversationID, Serializable content) throws IOException{
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.addReceiver(this.remoteAgentAID);
		message.setConversationId(conversationID);
		if(content != null){
			message.setContentObject(content);
		}
		return message;
	}

}
