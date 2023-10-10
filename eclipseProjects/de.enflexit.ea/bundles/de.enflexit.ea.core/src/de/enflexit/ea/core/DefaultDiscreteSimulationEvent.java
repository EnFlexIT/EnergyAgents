package de.enflexit.ea.core;

/**
 * Default implementation of {@link AbstractDiscreteSimulationEvent}, that is used if no other implementation is specified
 * in the agents IOSimulated. It provides empty default implementations of all callback method, i.e. does not interfere with 
 * the default behaviour of the energy agent.    
 *  
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DefaultDiscreteSimulationEvent extends AbstractDiscreteSimulationEvent {

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractDiscreteSimulationEvent#beforeMeasurementCommit()
	 */
	@Override
	protected void beforeMeasurementCommit() {
		// --- Empty default implementation ---------------
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractDiscreteSimulationEvent#beforeControlDecision()
	 */
	@Override
	protected void beforeControlDecision() {
		// --- Empty default implementation ---------------
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractDiscreteSimulationEvent#beforeSimulationStateTransmission()
	 */
	@Override
	protected void beforeSimulationStateTransmission() {
		// --- Empty default implementation ---------------
	}
}