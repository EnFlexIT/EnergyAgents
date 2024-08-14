package de.enflexit.ea.core;

/**
 * Abstract superclass for discrete simulation events. It provides callback methods to interfere in the
 * default behaviour of an energy agent in a discrete simulation at different points in the simulation step.
 *  
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public abstract class AbstractDiscreteSimulationEvent {
	
	/**
	 * Internal before measurement commit.
	 */
	protected final void internalBeforeMeasurementCommit() {
		try {
			this.beforeMeasurementCommit();
		} catch (Exception ex) {
			System.err.println("[" + Thread.currentThread().getName() + "] Error while executing callback method beforeMeasurementCommit()!");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Internal before control decision.
	 */
	protected final void internalBeforeControlDecision() {
		try {
			this.beforeControlDecision();
		} catch (Exception ex) {
			System.err.println("[" + Thread.currentThread().getName() + "] Error while executing callback method beforeControlDecision()!");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Internal before simulation state transmission.
	 */
	protected final void internalBeforeSimulationStateTransmission() {
		try {
			this.beforeSimulationStateTransmission();
		} catch (Exception ex) {
			System.err.println("[" + Thread.currentThread().getName() + "] Error while executing callback method beforeControlDecision()!");
			ex.printStackTrace();
		}
	}
	
	/**
	 * This callback method is called before the new measurements are committed to the agent.
	 */
	abstract protected void beforeMeasurementCommit();
	
	/**
	 * This callback method is called before the agent makes its control decision.
	 */
	abstract protected void beforeControlDecision();
		
	/**
	 * This callback method is called before the agent sends its new state to the simulation manager.
	 */
	abstract protected void beforeSimulationStateTransmission();
	
}
