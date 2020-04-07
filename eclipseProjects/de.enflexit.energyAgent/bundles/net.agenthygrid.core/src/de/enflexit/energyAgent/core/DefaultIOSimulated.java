package de.enflexit.energyAgent.core;

import org.awb.env.networkModel.NetworkModel;

import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.energyAgent.core.AbstractIOSimulated;

/**
 * The Class IOSimulated is used to simulate measurements from an energy conversion 
 * process, if the current project setup is used for simulations. Especially this class
 * is only used, if {@link AbstractEnergyAgent#getIOSimulated()} returns <code>null</code>>.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class DefaultIOSimulated extends AbstractIOSimulated {

	private static final long serialVersionUID = 3659353219575016108L;
	
	/**
	 * Instantiates a new default IO simulated that is used in case 
	 * that {@link AbstractEnergyAgent#getIOSimulated()} returns null.
	 * 
	 * @param energyAgent the current energy agent instance
	 */
	public DefaultIOSimulated(AbstractEnergyAgent energyAgent) {
		super(energyAgent);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#prepareForSimulation(org.awb.env.networkModel.helper.NetworkModel)
	 */
	@Override
	protected void prepareForSimulation(NetworkModel networkModel) { }
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#commitMeasurementsToAgentsManually()
	 */
	@Override
	protected boolean commitMeasurementsToAgentsManually() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentNotification(agentgui.simulationService.transaction.EnvironmentNotification)
	 */
	@Override
	protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification) {
		return super.onEnvironmentNotification(notification);
	}
	
}