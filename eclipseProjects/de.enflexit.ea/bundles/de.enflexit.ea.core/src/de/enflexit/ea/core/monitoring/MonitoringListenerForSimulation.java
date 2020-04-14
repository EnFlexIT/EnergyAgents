package de.enflexit.ea.core.monitoring;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.SimulationConnectorRemote;
import de.enflexit.ea.core.testbed.proxy.ProxyAgent;

/**
 * {@link MonitoringListener} implementation that sends status updates to the agent's {@link ProxyAgent}.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class MonitoringListenerForSimulation implements MonitoringListener {

	private AbstractEnergyAgent energyAgent;
	private SimulationConnectorRemote simulationConnector;
	
	private boolean firstTime = true;
	
	/**
	 * Instantiates a new {@link MonitoringListenerForSimulation}.
	 * @param energyAgent the energy agent
	 */
	public MonitoringListenerForSimulation(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
		this.simulationConnector = (SimulationConnectorRemote) energyAgent.getSimulationConnector();
	}
	/**
	 * Returns the simulation connector.
	 * @return the simulation connector
	 */
	private SimulationConnectorRemote getSimulationConnector() {
		return simulationConnector;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.monitoring.MonitoringListener#onMonitoringEvent(de.enflexit.energyAgent.core.monitoring.MonitoringEvent)
	 */
	@Override
	public void onMonitoringEvent(MonitoringEvent monitoringEvent) {
		if (this.energyAgent.getInternalDataModel().getCentralAgentAID()!=null){
			if (this.firstTime == true) {
				this.getSimulationConnector().setMyStimulusAnswer(monitoringEvent.getTSSE());
				this.firstTime = false;
			} else {
				this.getSimulationConnector().sendManagerNotification(monitoringEvent.getTSSE());
			}
		}
	}

}
