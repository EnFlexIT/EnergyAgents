package de.enflexit.ea.core.dataModel.absEnvModel;

import jade.util.leap.Serializable;

/**
 * The Class SimulationStatus can be used to describe the
 * current state of the simulation and is used in the class
 * {@link HyGridAbstractEnvironmentModel}.
 */
public class SimulationStatus implements Serializable {

	private static final long serialVersionUID = -8599386247978952211L;

	/**
	 * The enumeration STATE represents the list of possible simulation states.
	 */
	public static enum STATE {
		A_DistributeEnvironmentModel,
		B_ExecuteSimuation,
		C_StopSimulation
	}
	
	/**
	 * The enumeration STATE_CONFIRMATION can be used as response type 
	 * for the finalization of a simulation state.
	 */
	public static enum STATE_CONFIRMATION {
		Initialized,
		Done,
		Error
	}
	
	private STATE currentState = null;
	
	
	/**
	 * Sets the new state.
	 * @param newState the new state
	 */
	public void setState(STATE newState) {
		this.currentState = newState;
	}
	/**
	 * Returns the current state.
	 * @return the current state
	 */
	public STATE getState() {
		return this.currentState;
	}

	/**
	 * Returns a copy of the current instance.
	 * @return the copy
	 */
	public SimulationStatus getCopy() {
		SimulationStatus copy = new SimulationStatus();
		copy.setState(this.currentState);
		return copy;
	}
	
}
