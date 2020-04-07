package de.enflexit.energyAgent.core.centralExecutiveAgent.behaviour;

import java.util.ArrayList;
import java.util.List;

import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.energyAgent.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.energyAgent.core.liveMonitoringProxyAgent.LiveMonitoringProxyAgent;
import hygrid.deployment.dataModel.AgentDeploymentInformation;
import hygrid.deployment.dataModel.AgentOperatingMode;
import hygrid.deployment.dataModel.SetupExtension;
import hygrid.globalDataModel.cea.ConversationID;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * This class is handling live monitoring proxy requests. If not already running, a new 
 * LiveMonitoringProxyAgent will be started. The AID of the LiveMonitoringProxyAgent
 * will be sent to the requesting LiveMonitoringAgent.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringProxyRequestResponder extends SimpleAchieveREResponder {

	private static final long serialVersionUID = 4160573215157932961L;
	
	private static final String PROXY_AGENT_LOCAL_NAME = LiveMonitoringProxyAgent.class.getSimpleName();

	/**
	 * Instantiates a new live monitoring proxy request responder.
	 * @param agent the agent
	 */
	public LiveMonitoringProxyRequestResponder(Agent agent) {
		super(agent, getMessageTemplate());
	}

	/* (non-Javadoc)
	 * @see jade.proto.SimpleAchieveREResponder#prepareResultNotification(jade.lang.acl.ACLMessage, jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		
		ACLMessage resultNotification = request.createReply();
		String liveMonitoringProxyAgentName = this.getLiveMonitoringProxyAgentName();
		if (liveMonitoringProxyAgentName!=null) {
			resultNotification.setPerformative(ACLMessage.INFORM);
			resultNotification.setContent(liveMonitoringProxyAgentName);
		} else {
			resultNotification.setPerformative(ACLMessage.FAILURE);
		}
		return resultNotification;
	}
	
	/**
	 * Gets the message template.
	 * @return the message template
	 */
	public static MessageTemplate getMessageTemplate() {
		MessageTemplate matchProtocol = MessageTemplate.MatchProtocol(FIPA_REQUEST);
		MessageTemplate matchConversationID = MessageTemplate.MatchConversationId(ConversationID.OPS_LIVE_MONITORING_START.toString());
		return MessageTemplate.and(matchProtocol, matchConversationID);
	}
	
	/**
	 * Gets the LiveMonitoringProxyAgent's name. If not already running, it will be started.
	 * @return the live monitoring proxy agent name
	 */
	private String getLiveMonitoringProxyAgentName() {
		String agentName = null;
		
		AgentController agentController = null;
		try {
			// --- Check if the agent already exists ----------------
			agentController = myAgent.getContainerController().getAgent(PROXY_AGENT_LOCAL_NAME);
		} catch (ControllerException e) {
			// --- Agent not found, start it ------------------------
			agentController = this.startLiveMonitoringProxyAgent();
		}
		if (agentController!=null) {
			try {
				agentName = agentController.getName();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return agentName;
	}
	
	/**
	 * Start live monitoring proxy agent.
	 * @return the agent controller
	 */
	private AgentController startLiveMonitoringProxyAgent() {
		
		AgentController agentController = null;
		
		try {
			Object[] arguments = new Object[1];
			arguments[0] = this.getMonitoringTargetAIDs();
			
			LiveMonitoringProxyAgent proxyAgent = new LiveMonitoringProxyAgent();
			proxyAgent.setArguments(arguments);
			agentController = myAgent.getContainerController().acceptNewAgent(PROXY_AGENT_LOCAL_NAME, proxyAgent);
			agentController.start();
			((CentralExecutiveAgent)myAgent).getInternalDataModel().setLiveMonitoringProxyAgentAID(proxyAgent.getAID());
			
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return agentController;
		
	}
	
	/**
	 * Gets the AIDs of the monitoring targets (= all agents running in real system mode).
	 * @return the monitoring target AIDs
	 */
	private List<AID> getMonitoringTargetAIDs(){
		List<AID> monitoringTargetAIDs = new ArrayList<>();
		
		// --- Get the list of deployed agents ----------------------
		SimulationSetup simSetup = this.getCentralExecutiveAgent().getInternalDataModel().getProject().getSimulationSetups().getCurrSimSetup();
		SetupExtension setEx = (SetupExtension) simSetup.getUserRuntimeObject();
		List<AgentDeploymentInformation> deployedAgents = setEx.getDeploymentGroupsHelper().getAllDeployedAgents();
		for (int i=0; i<deployedAgents.size(); i++) {
			AgentDeploymentInformation agentDeploymentInformation = deployedAgents.get(i);
			
			// --- Add all real system agents to the list -----------
			if (agentDeploymentInformation.getAgentOperatingMode()==AgentOperatingMode.RealSystem && agentDeploymentInformation.getComponentType().equals("CEA")==false) {
				AID agentAID = this.getCentralExecutiveAgent().getInternalDataModel().getAidFromPhoneBook(agentDeploymentInformation.getAgentID());
				monitoringTargetAIDs.add(agentAID);
			}
		}
		
		return monitoringTargetAIDs;
	}
	
	private CentralExecutiveAgent getCentralExecutiveAgent() {
		return (CentralExecutiveAgent) myAgent;
	}

}
