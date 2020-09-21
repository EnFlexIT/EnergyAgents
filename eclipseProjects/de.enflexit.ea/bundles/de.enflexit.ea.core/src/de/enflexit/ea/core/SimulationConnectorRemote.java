package de.enflexit.ea.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;

import agentgui.core.application.Application;
import agentgui.core.config.GlobalInfo.MtpProtocol;
import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.sensoring.ServiceSensorInterface;
import agentgui.simulationService.transaction.DisplayAgentNotification;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.testbed.AgentNotificationContainer;
import de.enflexit.ea.core.testbed.CEARegistrationBehaviour;
import de.enflexit.ea.core.testbed.DeployedAgentMessageReceiveBehaviour;
import de.enflexit.ea.core.testbed.proxy.ProxyAgent;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.AID;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;

/**
 * The Class SimulationConnectorRemote is responsible for connecting agents on remote JADE platforms to the SimulationManager.
 */
public class SimulationConnectorRemote implements SimulationConnector, ServiceSensorInterface {
	
	protected AbstractEnergyAgent myAgent;
	private AbstractIOSimulated ioSimulated;
	private DeployedAgentMessageReceiveBehaviour msgReceiveBehaviour;

	protected AID proxyAgentAID;
	
	protected EnvironmentModel environmentModel;
	
	private CyclicNotificationHandler notificationHandler;
	private Vector<EnvironmentNotification> notifications = new Vector<EnvironmentNotification>();
	
	
	/**
	 * Instantiates a new simulation connector.
	 * @param agent the agent
	 */
	public SimulationConnectorRemote(AbstractEnergyAgent agent, AbstractIOSimulated ioSimulated) {
		this.myAgent = agent;
		this.ioSimulated = ioSimulated;
		this.myAgent.addBehaviour(this.getRemoteAgentMessageReceiveBehaviour());
	}
	
	/**
	 * Gets the remote agent message receive behaviour.
	 * @return the remote agent message receive behaviour
	 */
	protected DeployedAgentMessageReceiveBehaviour getRemoteAgentMessageReceiveBehaviour() {
		if (msgReceiveBehaviour==null) {
			msgReceiveBehaviour = new DeployedAgentMessageReceiveBehaviour(this.myAgent, this);
		}
		return msgReceiveBehaviour;
	}
	
	
	/**
	 * Gets the proxy agent AID.
	 * @return the proxy agent AID
	 */
	public AID getProxyAgentAID() {
		return proxyAgentAID;
	}
	
	/**
	 * Sets the proxy agent AID.
	 * @param proxyAgentAID the new proxy agent AID
	 */
	public void onRegistrationSuccess(AID proxyAgentAID){
		
		this.proxyAgentAID = proxyAgentAID;
		
		// ------------------------------------------------------------------------
		// --- Set the message templates for both message receive behaviours ------
		
		// --- The remote agent message receive behaviour should listen for messages from the proxy agent ---- 
		MessageTemplate messageTemplateMatchProxy = MessageTemplate.MatchSender(proxyAgentAID);
		this.getRemoteAgentMessageReceiveBehaviour().setMessageTemplate(messageTemplateMatchProxy);
		
		// --- The regular message receive behaviour should ignore messages from the proxy agent --------------
		myAgent.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(messageTemplateMatchProxy);
		
		if(this.environmentModel == null){
			this.environmentModel = this.requestEnvironmentModel();
		}
		
		this.ioSimulated.initialize(this.environmentModel);
	}
	
	/**
	 * Request the environment model from the CEA.
	 *
	 * @return the environment model
	 */
	protected EnvironmentModel requestEnvironmentModel(){
		
		EnvironmentModel environmentModel = null;

		AID ceaAID = myAgent.getInternalDataModel().getCentralAgentAID();
		System.out.println(myAgent.getLocalName() + ": requesting environment model from " + ceaAID.getName());
		
		ACLMessage environmentModelRequest = new ACLMessage(ACLMessage.REQUEST);
		environmentModelRequest.setConversationId(ConversationID.NETWORK_MODEL_REQUEST.toString());
		environmentModelRequest.addReceiver(ceaAID);
		myAgent.send(environmentModelRequest);
		
		ACLMessage response = myAgent.blockingReceive(MessageTemplate.MatchConversationId(ConversationID.NETWORK_MODEL_REQUEST.toString()));
		if (response!=null) {
			try {
				environmentModel = (EnvironmentModel) response.getContentObject();
				if (environmentModel!=null) {
					System.out.println(myAgent.getLocalName() + ": Received environment model from " + response.getSender().getName());
				}
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		return environmentModel;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#pickEnvironmentModelAndStart()
	 */
	@Override
	public void pickEnvironmentModelAndStart() {
		this.environmentModel = Application.getProjectFocused().getEnvironmentController().getEnvironmentModel();
		this.startRegistrationBehaviour();
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#setEnvironmentModel(agentgui.simulationService.environment.EnvironmentModel, boolean)
	 */
	@Override
	public void setEnvironmentModel(EnvironmentModel envModel, boolean aSynchron) {
		this.ioSimulated.setEnvironmentModel(envModel);
		if (aSynchron==true) {
			this.myAgent.addBehaviour(new ServiceStimulus());	
		} else {
			this.onEnvironmentStimulusIntern();	
		}
	}

	/**
	 * Gets the environment model.
	 * @return the environment model
	 */
	public EnvironmentModel getEnvironmentModel() {
		return environmentModel;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#sendManagerNotification(java.lang.Object)
	 */
	@Override
	public boolean sendManagerNotification(Object notification) {
//		System.out.println(myAgent.getLocalName() + ": Sending manager notification to " + proxyAgentAID.getName());
		boolean sent;
		ACLMessage notificationMessage;
		try {
			if (notification instanceof TechnicalSystemStateEvaluation) {
				TechnicalSystemStateEvaluation tsse = (TechnicalSystemStateEvaluation) notification;
				TechnicalSystemStateEvaluation tsseClone = TechnicalSystemStateHelper.copyTechnicalSystemStateEvaluationWithoutParent(tsse);
				notificationMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_MANAGER_NOTIFICATION, tsseClone);
			} else {
				notificationMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_MANAGER_NOTIFICATION, notification);
			}
			myAgent.send(notificationMessage);
			sent = true;
		} catch (IOException e) {
			sent = false;
		}
		return sent;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#sendAgentNotification(jade.core.AID, java.lang.Object)
	 */
	@Override
	public boolean sendAgentNotification(AID receiverAID, Object notification) {
		
		// --- Adjust the receiver's JADE platform - must be the same as the proxy agent's --------
		String receiverPlatformName = this.proxyAgentAID.getHap();
		String receiverGlobalName = receiverAID.getLocalName() + "@" + receiverPlatformName;
		AID targetAID = new AID(receiverGlobalName, AID.ISGUID);
		
		boolean sent;
		ACLMessage notificationMessage;
		AgentNotificationContainer notificationContainer = new AgentNotificationContainer();
		notificationContainer.setReceiver(targetAID);
		notificationContainer.setNotification(notification);
		try {
//			System.out.println(myAgent.getLocalName() + ": Sending agent notification to " + proxyAgentAID.getName());
			notificationMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_AGENT_NOTIFICATION, notificationContainer);
			myAgent.send(notificationMessage);
			sent = true;
		} catch (IOException e) {
			sent = false;
		}
		
		return sent;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#sendDisplayAgentNotification(agentgui.simulationService.transaction.DisplayAgentNotification)
	 */
	@Override
	public void sendDisplayAgentNotification(DisplayAgentNotification displayAgentNotification) {
		
		ACLMessage notificationMessage;
		try {
//			System.out.println(myAgent.getLocalName() + ": Sending display agent notification to " + proxyAgentAID.getName());
			notificationMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_DISPLAY_AGENT_NOTIFICATION, displayAgentNotification);
			myAgent.send(notificationMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#setMyStimulusAnswer(java.lang.Object)
	 */
	@Override
	public void setMyStimulusAnswer(Object myNextState) {
		
		ACLMessage stimulusResponseMessage;
		try {
//			System.out.println(myAgent.getLocalName() + ": Sending stimulus answer to " + proxyAgentAID.getName());
			if (myNextState instanceof TechnicalSystemStateEvaluation) {
				TechnicalSystemStateEvaluation tsse = (TechnicalSystemStateEvaluation) myNextState;
				TechnicalSystemStateEvaluation tsseClone = TechnicalSystemStateHelper.copyTechnicalSystemStateEvaluationWithoutParent(tsse);
				stimulusResponseMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_STIMULUS_RESPONSE, tsseClone);
			} else {
				stimulusResponseMessage = this.createMessage(ProxyAgent.CONVERSATION_ID_STIMULUS_RESPONSE, myNextState);
			}
			myAgent.send(stimulusResponseMessage);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Terminates the IOBehaviour and the agent
	 */
	public void doDelete() {
		this.restartAgent();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.SimulationConnector#onEnd()
	 */
	@Override
	public void onEnd() {
		// --- Nothing to do here -----------
	}
	
	/**
	 * This Behaviour is used to stimulate the agent from the outside 
	 * in a asynchronous way.
	 * 	 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class ServiceStimulus extends OneShotBehaviour {
		private static final long serialVersionUID = 1441989543791055996L;
		@Override
		public void action() {
			onEnvironmentStimulusIntern();
		}
	}
	/**
	 * This method is internally called if a stimulus from the outside reached this agent.
	 */
	private void onEnvironmentStimulusIntern() {
		this.ioSimulated.onEnvironmentStimulus();
	}
	
	/**
	 * Creates an ACL message with the specified conversation ID and content object to be sent to the proxy agent
	 * @param conversationID The conversation ID
	 * @param contentObject The content object
	 * @return The ACL message
	 * @throws IOException Thrown if setting the content object fails
	 */
	private ACLMessage createMessage(String conversationID, Object contentObject) throws IOException{
		
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.addReceiver(this.proxyAgentAID);
		message.setConversationId(conversationID);
		
		if (contentObject!=null){
			if (contentObject instanceof Serializable){
				message.setContentObject((Serializable) contentObject);
			} else {
				throw(new IOException());
			}
		}
		return message;
	}
	
	/**
	 * Initiates the registration of the testbed agent at the CEA
	 */
	protected void startRegistrationBehaviour(){
		
		AID ceaAID = myAgent.getInternalDataModel().getCentralAgentAID();
		
		// --- Tell the agent's general message receive behaviour to ignore messages from the CEA -----
		MessageTemplate ceaTemplate = MessageTemplate.MatchSender(ceaAID);
		myAgent.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(ceaTemplate);
		
		// --- Debug outputs -----------------
		System.out.println("startRegistrationBehaviour for " + myAgent.getClass().getName());

		// --- If the selected MTP is HTTPS and the agent is not a proxy agent, include the agent's encoded certificate ---------
		String certificate = null;
		if (myAgent.getInternalDataModel().getHyGridAbstractEnvironmentModel().getDeploymentSettingsModel().getCentralAgentSpecifier().getMtpType().equals(MtpProtocol.HTTPS.toString())) {
			certificate = this.getEncodedCertificate();
		}
		
		myAgent.addBehaviour(new CEARegistrationBehaviour(myAgent, ceaAID, certificate, this));
	}
	
	/**
	 * Gets the base64 encoded certificate for this agent
	 * @return
	 */
	private String getEncodedCertificate(){
		String encodedCertificate = null;
		try {
			String defaultCertificatePath = Application.getProjectFocused().getProjectFolderFullPath() + "config" + File.separator + myAgent.getLocalName() + ".cer";
			encodedCertificate = this.encodeFileBase64(defaultCertificatePath);
			System.out.println(myAgent.getLocalName() + ": Including certificate");
		} catch (Exception e) {
			System.err.println(myAgent.getLocalName() + ": Error loading certificate!");
		}
		return encodedCertificate;
	}
	
	/**
	 * Encodes the content of a file in base64
	 * @param sourceFilePath Path to the file to be encoded
	 * @return The encoded file content
	 * @throws Exception Error loading or encoding the file
	 */
	private String encodeFileBase64(String sourceFilePath) throws Exception{
		File sourceFile = new File(sourceFilePath);
		return this.encodeFileBase64(sourceFile);
	}
	
	/**
     * Encodes the content of a file in base64
	 * @param sourceFilePath The file to be encoded
	 * @return The encoded file content
	 * @throws Exception Error loading or encoding the file
     */
    private String encodeFileBase64(File sourceFile) throws Exception {
        byte[] base64EncodedData = Base64.encodeBase64(loadFileAsBytesArray(sourceFile));
        String encodedContent = new String(base64EncodedData);
		return encodedContent;
    }
    
    /**
     * This method loads a file from file system and returns the byte array of the content.
     * @param file The file
     * @return bytes The byte array
     * @throws Exception Error reading the file
     */
    private byte[] loadFileAsBytesArray(File file) throws Exception {
        int length = (int) file.length();
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
        byte[] bytes = new byte[length];
        reader.read(bytes, 0, length);
        reader.close();
        return bytes;
    }
	
	/**
	 * Sets the simulation to or from pause state.
	 * @param isPauseSimulation the new pause state
	 */
	public void setPauseSimulation(boolean isPauseSimulation){
		System.out.println(myAgent.getLocalName() + ": Setting paused state to " + isPauseSimulation);
		this.ioSimulated.setPauseSimulation(isPauseSimulation);
	}
	
	
	/**
	 * Restarts the testbed agent, i.e. triggers the creation of a new instance and terminates the old one.
	 */
	protected void restartAgent(){

		// --- Invoke a thread that starts a new agent with the same name and class ---------
		String agentName = myAgent.getLocalName();
		String agentClassName = myAgent.getClass().getName();
		AgentContainer agentContainer = myAgent.getContainerController();
		new AgentStarterThread(agentName, agentClassName, agentContainer).start();
		
		// --- Terminate the old instance ------------
		myAgent.doDelete();
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setMigration(jade.core.Location)
	 */
	@Override
	public void setMigration(Location newLocation) {
		// --- Prepare agent to migrate ---------
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	public void setNotification(EnvironmentNotification notification) {
		
		synchronized (this.notifications) {
			this.notifications.addElement(notification);
		}
		
		if (this.notificationHandler==null || this.notificationHandler.getAgent()==null) {
			this.startNotificationHandler();
		} else {
			this.notificationHandler.restart();
		}
		
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#getAID()
	 */
	@Override
	public AID getAID() {
		return this.myAgent.getAID();
	}
	
	/**
	 * Starts the notification handler.
	 */
	private void startNotificationHandler() {
		if (this.notificationHandler == null) {
			this.notificationHandler = new CyclicNotificationHandler();
		}
		myAgent.addBehaviour(this.notificationHandler);
	}
	
	
	/**
	 * This CyclicBehaviour is used in order to act on the incoming notifications.
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class CyclicNotificationHandler extends CyclicBehaviour {
		
		private static final long serialVersionUID = 4638681927192305608L;
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		@Override
		public void action() {
			
			EnvironmentNotification notification = null;
			boolean removeFirstElement = false;
			boolean moveAsLastElement = false;
			
			// --- Get the first element and work on it ------------------
			if (notifications.size()!=0) {
				notification = notifications.get(0);
				notification = ioSimulated.onEnvironmentNotification(notification);
				if (notification.getProcessingInstruction().isDelete()) {
					removeFirstElement = true;	
					moveAsLastElement = false;
					
				} else if (notification.getProcessingInstruction().isBlock()) {
					removeFirstElement = false;
					moveAsLastElement = false;
					this.block(notification.getProcessingInstruction().getBlockPeriod());
					
				} 
				if (notification.getProcessingInstruction().isMoveLast()) {
					removeFirstElement = false;
					moveAsLastElement = true;
				}
			}
			
			// --- remove this element and control the notifications -----
			synchronized (notifications) {
				if (removeFirstElement==true) {
					notifications.remove(0);
				}
				if (moveAsLastElement==true) {
					if (notifications.size()>1) {
						notifications.remove(0);
						notifications.add(notification);
					} else {
						this.block(notification.getProcessingInstruction().getBlockPeriod());	
					}
				}
				if (notification!=null) {
					notification.resetProcessingInstruction();	
				}
				if (notifications.size()==0) {
					block();
				}
			}
			
		}
	}
}
