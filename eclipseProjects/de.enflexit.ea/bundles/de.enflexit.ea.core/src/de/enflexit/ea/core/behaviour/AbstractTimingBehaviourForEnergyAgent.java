package de.enflexit.ea.core.behaviour;

import java.time.Duration;
import java.time.Instant;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.jade.behaviour.AbstractTimingBehaviour;

/**
 * Abstract superclass for timed behaviours executed by energy agents, providing an implementation 
 * of getCurrentTime that uses the getTime() method from the EnergyAgent's IO behaviour. 
 * 
 * IMPORTANT: TimingBehaviours must not be added like regular behaviours, since they will block the agent
 * thread then! Use the behaviour's start method again, and call its' stop method from the executing agent's 
 * takeDown method for proper termination!
 *
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public abstract class AbstractTimingBehaviourForEnergyAgent extends AbstractTimingBehaviour {
	
	private static final long serialVersionUID = 1913926656157056148L;

	/**
	 * Instantiates a new timing behaviour, starting at the specified instant and repeated with the specified interval length.
	 *
	 * @param energyAgent the energy agent executing the behaviour
	 * @param startAt the instant to start at with interval ticks
	 * @param interval the interval duration
	 * @param executionTiming specifies if the should start at or be finished until the interval times.
	 */
	public AbstractTimingBehaviourForEnergyAgent(AbstractEnergyAgent energyAgent, Instant startAt, Duration interval, ExecutionTiming executionTiming) {
		super(energyAgent, startAt, interval, executionTiming);
	}

	
	/**
	 * Instantiates a new abstract timing behaviour for energy agents, based on the specified interval and offset durations. 
	 * For example, an interval of 10 and an offset 2 minutes means the action is scheduled every ten minutes at xx:02, xx:12 etc.
	 * Attention: Currently only works with real time! Use the constructor above for simulation time support.
	 *
	 * @param energyAgent the energy agent executing the behaviour
	 * @param interval the tick interval
	 * @param offset the duration offset
	 * @param executionTiming specifies if the should start at or be finished until the interval times.tionTiming the execution timing
	 */
	public AbstractTimingBehaviourForEnergyAgent(AbstractEnergyAgent energyAgent, Duration interval, Duration offset, ExecutionTiming executionTiming) {
		super(energyAgent, interval, offset, executionTiming);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.jade.behaviour.AbstractTimingBehaviour#getCurrentTime()
	 */
	@Override
	protected Instant getCurrentTime() {
		long agentTime = ((AbstractEnergyAgent)this.myAgent).getEnergyAgentIO().getTime();
		return Instant.ofEpochMilli(agentTime);
	}
	

}
