package de.enflexit.ea.core.ops.agent.behaviour;

import de.enflexit.ea.core.ops.agent.CeaConnectorAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class LiveMonitoringTerminationBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 8571424506072157025L;

	@Override
	public void action() {
		System.out.println("[" + this.getClass().getSimpleName() + "] Stopping the LiveMonitoringAgent...");
		
		AgentController liveMonitoringAgentController = ((CeaConnectorAgent)myAgent).getInternalDataModel().getLiveMonitoringAgentController();
		if (liveMonitoringAgentController!=null) {
			try {
				liveMonitoringAgentController.kill();
			} catch (StaleProxyException e) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error stopping the LiveMonitoringAgent!");
			}
		}
	}

}
