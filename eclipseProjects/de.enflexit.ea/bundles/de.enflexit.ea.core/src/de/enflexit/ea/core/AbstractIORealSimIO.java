package de.enflexit.ea.core;

import energy.FixedVariableList;
import jade.core.behaviours.CyclicBehaviour;

/**
 * Abstract superclass, containing common functionality for IORealSimIO behaviours.
 * IORealSimIO are simulated interfaces for Real Systems.
 * They are designed to test deployed agents without their technical systems
 *
 * @author Erik Wassermann - HSU - IfA - Helmut Schmidt University - University of federal armed forces Hamburg
 * 
 */
public abstract class AbstractIORealSimIO extends CyclicBehaviour implements EnergyAgentIO {

	private static final long serialVersionUID = 6405647583527326051L;

	protected long timeOffset = 0;
	
	private long cycleTime = 150;
	
	private AbstractEnergyAgent myAgent;
	
	/**
	 * Instantiates a new abstract IO real.
	 * @param agent the agent instance
	 */
	public AbstractIORealSimIO(AbstractEnergyAgent energyAgent) {
		super(energyAgent);
		this.myAgent = energyAgent;
	}
	//TODO:
	//for subclasses:	implement getMeasurementsFromSystem()
	//					to get the IO-values from a schedule or file or set them statically
	
	
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

	@Override
	public void setMeasurementsFromSystem(FixedVariableList newmeasurements) {

		this.myAgent.getInternalDataModel().setMeasurementsFromSystem(newmeasurements);
	}

	@Override
	public void setSetPointsToSystem(FixedVariableList newSetPointSettings) {
		// no Set Points will be set, because this is just a simulated interface
	}

	@Override
	public void action() {
		//get the IO-Values from a schedule or file and set them in the internalDataModel of the agent
		this.setMeasurementsFromSystem(this.getMeasurementsFromSystem());
		//block this behaviour for the duration defined in cycleTime
		this.block(cycleTime);
		
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getTime()
	 */
	@Override
	public Long getTime() {
		return System.currentTimeMillis() - this.timeOffset;
	}


	public long getCycleTime() {
		return cycleTime;
	}
	

}