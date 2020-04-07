package de.enflexit.energyAgent.core;

import energy.FixedVariableList;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractStateInputStream serves as base class for data streams that are used in simulations.
 *
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public abstract class AbstractStateInputStream {

	private AbstractIOSimulated ioSimulated;

	
	/**
	 * Instantiates a new abstract input data stream.
	 * @param ioSimulated the current {@link AbstractIOSimulated}
	 */
	public AbstractStateInputStream(AbstractIOSimulated ioSimulated) {
		this.ioSimulated = ioSimulated;
	}
	/**
	 * Returns the current IO simulated.
	 * @return the actual implementation of the {@link AbstractIOSimulated}
	 */
	public AbstractIOSimulated getIoSimulated() {
		return ioSimulated;
	}
	
	
	/**
	 * Prepare for simulation will be invoked with the initialization of an simulated IO 
	 * interface for energy agents and will give you the chance to do something ahead simulations
	 * (e.g. connect to a data source). 
	 */
	public abstract void prepareForSimulation();
	
	/**
	 * Will be invoked, if the simulation is to be paused.
	 * @param isPauseSimulation the indicator to pause or resume the simulation
	 */
	public abstract void setPauseSimulation(boolean isPauseSimulation);
	
	/**
	 * Will be invoked when the simulation is to be stopped.
	 */
	public abstract void stopSimulation();
	
	
	/**
	 * Has to return the system state (a {@link TechnicalSystemStateEvaluation}) for the specified time.
	 * Thus, this method represents the main job to be done from a data input stream. It may handle 
	 * the data to be loaded to the memory as flexible and slim as possible. E.g. to reduce memory 
	 * footprint for long time simulations.     
	 *
	 * @param globalTime the global time
	 * @return the system state
	 */
	public abstract TechnicalSystemStateEvaluation getSystemState(long globalTime);
	
	/**
	 * Has to return the IO settings for the specified system state.
	 *
	 * @param globalTime the global time
	 * @param tsse the current system state
	 * @return the IO settings
	 */
	public abstract FixedVariableList getIOSettings(long globalTime, TechnicalSystemStateEvaluation tsse);
	
	
}
