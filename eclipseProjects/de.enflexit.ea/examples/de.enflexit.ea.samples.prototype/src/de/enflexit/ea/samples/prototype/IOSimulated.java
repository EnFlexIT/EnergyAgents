package de.enflexit.ea.samples.prototype;

import org.awb.env.networkModel.NetworkModel;

import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.ea.core.AbstractIOSimulated;

/**
 * The Class IOSimulated is used to simulate measurements from an energy conversion 
 * process, if the current project setup is used for simulations.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOSimulated extends AbstractIOSimulated {

	private static final long serialVersionUID = 3659353219575016108L;

	/**
	 * Instantiates a new IO simulated.
	 *
	 * @param agent the agent
	 * @param internalDataModel the internal data model
	 */
	public IOSimulated(PrototypeAgent agent) {
		super(agent);
	}
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification) {
		return super.onEnvironmentNotification(notification);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#commitMeasurementsToAgentsManually()
	 */
	@Override
	protected boolean commitMeasurementsToAgentsManually() {
		return false;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#prepareForSimulation(org.awb.env.networkModel.helper.NetworkModel)
	 */
	@Override
	protected void prepareForSimulation(NetworkModel networkModel) {
		
	}
	
}