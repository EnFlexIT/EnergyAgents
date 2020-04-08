package de.enflexit.energyAgent.core.testbed;

import de.enflexit.energyAgent.core.AbstractEnergyAgent;
import de.enflexit.energyAgent.core.SimulationConnectorRemote;
import de.enflexit.energyAgent.core.globalDataModel.cea.ConversationID;
import de.enflexit.energyAgent.core.globalDataModel.ontology.CEARegistrationResponse;
import de.enflexit.energyAgent.core.globalDataModel.ontology.HyGridOntology;
import de.enflexit.energyAgent.core.globalDataModel.ontology.ProxyAgentRegistrationRequest;
import de.enflexit.energyAgent.core.globalDataModel.ontology.RemoteAgentRegistrationRequest;
import de.enflexit.energyAgent.core.globalDataModel.ontology.TestbedAgentManagement;
import de.enflexit.energyAgent.core.testbed.proxy.ProxyAgent;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by remote and proxy agents to send a registration request to the {@link CentralExecutiveAgent}
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class CEARegistrationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -8086847237256938342L;
	
	private static final long MESSAGE_RECEIVE_TIMEOUT = 5000;
	
	private AID ceaAID;
	private String encodedCertificate;
	
	private MessageTemplate messageTemplate;
	
	private SimulationConnectorRemote simulationConnector;
	
	/**
	 * Constructor for ProxyAgents communicating via HTTP
	 * @param a The agent executing this behaviour
	 * @param ceaAID The AID of the CEA
	 */
	public CEARegistrationBehaviour(Agent a, AID ceaAID) {
		super(a);
		this.ceaAID = ceaAID;
	}
	
	/**
	 * Constructor for ProxyAgents communicating via HTTPS
	 * @param a The agent executing this behaviour
	 * @param ceaAID The AID of the CEA
	 * @param encodedCertificate The agent's certificate
	 */
	public CEARegistrationBehaviour(Agent a, AID ceaAID, String encodedCertificate) {
		super(a);
		this.ceaAID = ceaAID;
		this.encodedCertificate = encodedCertificate;
	}
	
	/**
	 * Constructor for remote agents communicating via HTTP
	 * @param a the agent
	 * @param ceaAID the CEA's AID
	 * @param encodedCertificate the encoded certificate
	 * @param simulationConnector the simulation connector
	 */
	public CEARegistrationBehaviour(Agent a, AID ceaAID, SimulationConnectorRemote simulationConnector) {
		super(a);
		this.ceaAID = ceaAID;
		this.simulationConnector = simulationConnector;
	}
	
	/**
	 * Constructor for remote agents communicating via HTTPS
	 * @param a the agent
	 * @param ceaAID the CEA's AID
	 * @param encodedCertificate the encoded certificate
	 * @param simulationConnector the simulation connector
	 */
	public CEARegistrationBehaviour(Agent a, AID ceaAID, String encodedCertificate, SimulationConnectorRemote simulationConnector) {
		super(a);
		this.ceaAID = ceaAID;
		this.encodedCertificate = encodedCertificate;
		this.simulationConnector = simulationConnector;
	}
	

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		// --- Prepare the ontology object instance for this action ---------
		TestbedAgentManagement registration;
		if (myAgent instanceof ProxyAgent){
			registration = new ProxyAgentRegistrationRequest();
		} else {
			registration= new RemoteAgentRegistrationRequest();
			if (this.encodedCertificate!=null) {
				((RemoteAgentRegistrationRequest)registration).setCertificateBase64(this.encodedCertificate);
			}
		}
		
		// --- Create the corresponding agent action
		Action registrationAction = new Action(myAgent.getAID(), registration);
		
		// --- Create an ACL message and set the required fields -------------
		ACLMessage registrationMessage = new ACLMessage(ACLMessage.REQUEST);
		registrationMessage.setSender(myAgent.getAID());
		registrationMessage.addReceiver(this.ceaAID);
		registrationMessage.setLanguage(new SLCodec().getName());
		registrationMessage.setOntology(HyGridOntology.getInstance().getName());
		registrationMessage.setConversationId(ConversationID.PROXY_REGISTRATION.toString());
		
		// --- Set the content of the message ----------------------
		try {
			myAgent.getContentManager().fillContent(registrationMessage, registrationAction);
		} catch (CodecException | OntologyException e) {
			System.err.println("Error creating registration message for agent " + myAgent.getName());
			e.printStackTrace();
		}
		
		if (registrationMessage.getContent()!=null) {
			// --- Send the message -------------
			myAgent.send(registrationMessage);
			System.out.println(myAgent.getClass().getSimpleName() + " " + myAgent.getLocalName() + ": CEA registration message was sent to " + this.ceaAID.getName());
			
			// --- Wait for the CEA's response and process it -----------
			ACLMessage response = myAgent.blockingReceive(this.getMessageTemplate(), this.getMessageReceiveTimeout());
			this.handleResponse(response);
			
		} else {
			// --- Setting the content failed ----------------
			// TODO figure out how to handle this
		}
		
	}
	
	/**
	 * Gets the message receive timeout, which is zero (= no timeout) for {@link ProxyAgent}s and defined by the above constant for {@link AbstractEnergyAgent}s.
	 * @return the message receive timeout
	 */
	private long getMessageReceiveTimeout() {
		if (myAgent instanceof AbstractEnergyAgent) {
			return MESSAGE_RECEIVE_TIMEOUT;
		} else {
			return 0;
		}
	}

	/**
	 * Gets a message template for receiving registration-related messages.
	 *
	 * @return the message template
	 */
	private MessageTemplate getMessageTemplate(){
		if (this.messageTemplate==null) {
			this.messageTemplate = MessageTemplate.MatchConversationId(ConversationID.PROXY_REGISTRATION.toString());
		}
		return this.messageTemplate;
	}
	
	
	/**
	 * Handles the CEA's response.
	 * @param response the response
	 */
	private void handleResponse(ACLMessage response){
		
		if (response==null || response.getPerformative()==ACLMessage.FAILURE) {
			// --------------------------------------------
			// --- CEA could not be reached, retry  -------
			System.err.println(myAgent.getLocalName() + ": Sending registration request to the CEA failed, trying again later");
			myAgent.addBehaviour(new RetryRegistrationBehaviour(myAgent, ceaAID, simulationConnector));
		
		} else if (response.getPerformative()==ACLMessage.INFORM) {
			// ----------------------------------------------------
			// --- Registration successful, process response ------
			System.out.println(myAgent.getLocalName() + ": Received registration response from " + response.getSender().getName());
			
			// --- Extract message content -----------
			CEARegistrationResponse registrationResponse = null;
			try {
				ContentElement messageContent = myAgent.getContentManager().extractContent(response);
				registrationResponse = (CEARegistrationResponse) ((Action)messageContent).getAction();
			} catch (CodecException | OntologyException | ClassCastException e) {
				System.err.println(myAgent.getLocalName() + ": Error extracting message content from regostration response");
				e.printStackTrace();
			}
			
			// --- Process message content -----------
			if (registrationResponse!=null) {
				if (this.simulationConnector!=null) {
					this.simulationConnector.onRegistrationSuccess(registrationResponse.getCounterpartAID());
				} else if(myAgent instanceof ProxyAgent) {
					((ProxyAgent)myAgent).onRegistrationSuccess(registrationResponse.getCounterpartAID());
				}
			}
			
		} else if (response.getPerformative()==ACLMessage.REFUSE) {
			// --------------------------------
			// --- Registration refused -------
			// In the current version the CEA never refuses a registration. 
			// If a later version does, handling the refuse can be implemented here.
			
		}
			
	}
	
	
	/**
	 * This helper behaviour adds a {@link CEARegistrationBehaviour} after a specified waiting time.
	 * It can be used to retry if the first registration attempt failed. 
	 *
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
	 */
	public static class RetryRegistrationBehaviour extends WakerBehaviour{

		private static final long serialVersionUID = -7498476968575405106L;
		public static final long DEFAULT_WAITING_TIME = 5000;

		private AID ceaAID;
		
		private SimulationConnectorRemote simulationConnector;
		
		/**
		 * Instantiates a new {@link RetryRegistrationBehaviour} using the default waiting time
		 * @param agent The agent that is trying to register
		 * @param ceaAID The CEA to register at
		 */
		public RetryRegistrationBehaviour(Agent agent, AID ceaAID) {
			this(agent, ceaAID, null, DEFAULT_WAITING_TIME);
		}
		
		/**
		 * Instantiates a new {@link RetryRegistrationBehaviour} using a specified waiting time
		 * @param agent The agent that is trying to register
		 * @param ceaAID The CEA to register at
		 * @param waitingTime The waiting time in milliseconds
		 */
		public RetryRegistrationBehaviour(Agent agent, AID ceaAID, long waitingTime) {
			this(agent, ceaAID, null, waitingTime);
		}

		/**
		 * Instantiates a new {@link RetryRegistrationBehaviour} using the default waiting time
		 * @param agent The agent that is trying to register
		 * @param ceaAID The CEA to register at
		 */
		public RetryRegistrationBehaviour(Agent agent, AID ceaAID, SimulationConnectorRemote simulationConnector) {
			this(agent, ceaAID, simulationConnector, DEFAULT_WAITING_TIME);
		}
		
		/**
		 * Instantiates a new {@link RetryRegistrationBehaviour} using a specified waiting time
		 * @param agent The agent that is trying to register
		 * @param ceaAID The CEA to register at
		 * @param waitingTime The waiting time in milliseconds
		 */
		public RetryRegistrationBehaviour(Agent agent, AID ceaAID, SimulationConnectorRemote simulationConnector, long waitingTime) {
			super(agent, waitingTime);
			this.ceaAID = ceaAID;
			this.simulationConnector = simulationConnector;
		}
		
		@Override
		protected void onWake() {
			// --- Add a new CEARegistrationBehaviour after the specified waiting time is passed --------
			myAgent.addBehaviour(new CEARegistrationBehaviour(myAgent, ceaAID, simulationConnector));
		}
		
		
	}

}
