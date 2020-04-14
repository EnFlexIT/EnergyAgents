package de.enflexit.ea.core;

import energy.FixedVariableList;

/**
 * The Interface AgentIO describes the interface to the actual technical system
 * or energy conversion process respectively. For the actual implementation, a
 * simulated and a real connection to the system can be considered. 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public interface EnergyAgentIO {

	/**
	 * Returns the current time (simulated or real).
	 * @return the time
	 */
	public Long getTime();
	
	/**
	 * Should return the current measurements from the underlying technical system or energy conversion process.
	 * @return the measurements from the system
	 */
	public FixedVariableList getMeasurementsFromSystem();

	/**
	 * Should be invoked if the measurements were received from the underlying technical system or energy conversion process.
	 *
	 * @param newmeasurements the new measurements from system
	 * @return the measurements from the system
	 */
	public void setMeasurementsFromSystem(FixedVariableList newmeasurements);

	
	/**
	 * Sets the sets the points to system.
	 * @param newSetPointSettings the new sets the points to system
	 */
	public void setSetPointsToSystem(FixedVariableList newSetPointSettings);
	
	/**
	 * Gets the sets the points to system.
	 * @return the sets the points to system
	 */
	public FixedVariableList getSetPointsToSystem();
	
}

