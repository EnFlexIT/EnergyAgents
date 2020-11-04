package de.enflexit.ea.core.testbed.proxy;

import agentgui.core.application.Application;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.deployment.AgentSpecifier;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.testbed.CEARegistrationBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;

/**
 * The ProxyAgent acts as a mediator between the simulation service and testbed agents running on a remote platform.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 *
 */
public class ProxyAgent extends Agent {
	
	// --- Conversation IDs for messages sent to the testbed agent ----------------
	public static final String CONVERSATION_ID_ENVIRONMENT_STIMULUS = "EnvironmentStimulus";
	public static final String CONVERSATION_ID_ENVIRONMENT_NOTIFICATION = "EnvironmentNotification";
	public static final String CONVERSATION_ID_STIMULUS_RESPONSE = "StimulusResponse";
	public static final String CONVERSATION_ID_MANAGER_NOTIFICATION = "ManagerNotification";
	public static final String CONVERSATION_ID_AGENT_NOTIFICATION = "AgentNotification";
	public static final String CONVERSATION_ID_DISPLAY_AGENT_NOTIFICATION = "DisplayAgentNotification";
	public static final String CONVERSATION_ID_PAUSE_SIMULATION = "PauseSimulation";
	public static final String CONVERSATION_ID_DO_DELETE = "DoDelete";
	public static final String CONVERSATION_ID_FORWARDED_MESSAGE = "ForwardedMessage";

	private static final long serialVersionUID = 2071294654492985101L;
	
	private AID ceaAID;
	
	private ProxyAgentSimulationServiceBehaviour simulationServiceBehaviour;
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		super.setup();
		
		// --- Some initializations
		this.getContentManager().registerLanguage(new SLCodec());
		this.getContentManager().registerOntology(HyGridOntology.getInstance());
		
		
		// -------------------------------------
		// --- Add behaviours   ----------------
		
		// --- Behaviour for registering at the CEA
		this.addBehaviour(new CEARegistrationBehaviour(this, getCEAAid()));
		// General message receive behaviour
//		this.addBehaviour(new MessageReceiveBehaviour());
		
		this.simulationServiceBehaviour = new ProxyAgentSimulationServiceBehaviour(this);
		this.addBehaviour(simulationServiceBehaviour);
		
		System.out.println("ProxyAgent " + this.getAID().getLocalName() + " succesfully started!");
	}
	
	/**
	 * Invoked after receiving a registration response.
	 *
	 * @param remoteAgentAID the remote agent AID
	 */
	public void onRegistrationSuccess(AID remoteAgentAID){
		this.simulationServiceBehaviour.setRemoteAgentAID(remoteAgentAID);
		this.addBehaviour(new ProxyAgentMessageReceiveBehaviour(remoteAgentAID));
	}
	
	/**
	 * Gets the CEA aid.
	 *
	 * @return the CEA aid
	 */
	private AID getCEAAid(){
		if(this.ceaAID == null){
			HyGridAbstractEnvironmentModel absEnv = (HyGridAbstractEnvironmentModel) Application.getProjectFocused().getEnvironmentController().getAbstractEnvironmentModel();
			AgentSpecifier ceaSpecifier = absEnv.getDeploymentSettings().getCentralAgentSpecifier();
			this.ceaAID = ceaSpecifier.getAID();
		}
		return this.ceaAID;
	}
	
	
}
