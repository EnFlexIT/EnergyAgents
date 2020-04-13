package de.enflexit.ea.prosumer;

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
	 * Instantiates a new simulated IO behaviour for the {@link ProsumerAgent}.
	 *
	 * @param agent the current {@link ProsumerAgent}
	 * @param internalDataModel the internal data model
	 */
	public IOSimulated(ProsumerAgent agent) {
		super(agent);
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#prepareForSimulation(org.awb.env.networkModel.helper.NetworkModel)
	 */
	@Override
	protected void prepareForSimulation(NetworkModel networkModel) {
		
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractIOSimulated#expectFurtherMeasurementsFromBlackboard()
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
		System.out.println(this.getEnergyAgent().getLocalName() + ": Received Notification!");
		return super.onEnvironmentNotification(notification);
	}

}