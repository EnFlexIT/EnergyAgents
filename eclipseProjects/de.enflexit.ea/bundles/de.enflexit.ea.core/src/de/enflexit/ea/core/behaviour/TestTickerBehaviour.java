package de.enflexit.ea.core.behaviour;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.dataModel.visualizationMessaging.EnergyAgentVisualizationMessagging;
import jade.core.behaviours.TickerBehaviour;

/**
 * The Class TestTickerBehaviour.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TestTickerBehaviour extends TickerBehaviour {

	private static final long serialVersionUID = -6823141746051343215L;

	private AbstractEnergyAgent energyAgent;
	
	/**
	 * Instantiates a new test ticker behaviour.
	 *
	 * @param energyAgent the energy agent
	 * @param period the period
	 */
	public TestTickerBehaviour(AbstractEnergyAgent energyAgent, long period) {
		super(energyAgent, period);
		this.energyAgent = energyAgent;
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.TickerBehaviour#onTick()
	 */
	@Override
	protected void onTick() {
		
		// ----------------------------------------------------------------------------------------
		// --- Here some space to do what is required for testing / development purposes ----------
		// ----------------------------------------------------------------------------------------
		
		// --- Send a message to myself -------------------
		EnergyAgentVisualizationMessagging.sendShowUIMessageToEnergyAgent(this.energyAgent, this.energyAgent.getAID());
		
	}
	
}
