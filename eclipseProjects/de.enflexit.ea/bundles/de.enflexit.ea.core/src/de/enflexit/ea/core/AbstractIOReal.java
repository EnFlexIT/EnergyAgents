package de.enflexit.ea.core;

import jade.core.behaviours.CyclicBehaviour;

/**
 * Abstract superclass, containing common functionality for IOReal behaviours.
 *
 * @author Hanno-Felix Wagner - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractIOReal extends CyclicBehaviour implements EnergyAgentIO {

	private static final long serialVersionUID = -872510444824256293L;
	
	protected long timeOffset = 0;
	private AbstractEnergyAgent energyAgent;
	
	/**
	 * Instantiates a new abstract IO real.
	 * @param agent the agent instance
	 */
	public AbstractIOReal(AbstractEnergyAgent energyAgent) {
		super(energyAgent);
		this.energyAgent = energyAgent;
	}
	
	/**
	 * Sets the time offset for this IO behaviour.
	 * Time-offsets can be used to adjust the time delivered by the behaviours getTime()-Method, i.e. 
	 * to synchronize with simulation time models in testbed mode, or adapt to message delivery delays.
	 * @param timeOffset the new time offset
	 */
	public void setTimeOffset(long timeOffset) {
		System.out.println(myAgent.getLocalName() + ": Time offset set to " + timeOffset);
		this.timeOffset = timeOffset;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getTime()
	 */
	@Override
	public Long getTime() {
		return System.currentTimeMillis() - this.timeOffset;
	}

	/**
	 * Returns the current instance of the energy agent, if needed.
	 * @return the energy agent
	 */
	public AbstractEnergyAgent getEnergyAgent() {
		return this.energyAgent;
	}
	/**
	 * Gets the internal data model.
	 * @return the internal data model
	 */
	public AbstractInternalDataModel getInternalDataModel() {
		return this.getEnergyAgent().getInternalDataModel();
	}
	
}