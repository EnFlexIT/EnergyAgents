package de.enflexit.energyAgent.ops.agent.behaviour;

import de.enflexit.energyAgent.ops.agent.CeaConnectorAgent;
import de.enflexit.energyAgent.ops.liveMonitoring.LiveMonitoringAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * The Class LiveMonitoringInitiatorBehaviour will try to start the live monitoring agent.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class LiveMonitoringInitiatorBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -6639155691647156826L;

	private CeaConnectorAgent ceaConAgent;

	/**
	 * Instantiates a new live monitoring initiator behaviour.
	 * @param ceaConAgent the CeaConnectorAgent
	 */
	public LiveMonitoringInitiatorBehaviour(CeaConnectorAgent ceaConAgent) {
		this.ceaConAgent = ceaConAgent;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		try {
		
			// --- Prepare the agent ----------------------
			Object[] startArgs = new Object[1];
			startArgs[0] = this.ceaConAgent.getOpsController();
			Agent liveMonAgent = new LiveMonitoringAgent();
			liveMonAgent.setArguments(startArgs);
			
			// --- Start the agent ------------------------
			AgentController agentController = this.ceaConAgent.getContainerController().acceptNewAgent(LiveMonitoringAgent.DEFAULT_LOCAL_NAME, liveMonAgent);
			agentController.start();
			
			// --- Keep the AgentController ---------------
			this.ceaConAgent.getInternalDataModel().setLiveMonitoringAgentController(agentController);
			
		} catch (StaleProxyException spEx) {
			spEx.printStackTrace();
		}
	}

}
