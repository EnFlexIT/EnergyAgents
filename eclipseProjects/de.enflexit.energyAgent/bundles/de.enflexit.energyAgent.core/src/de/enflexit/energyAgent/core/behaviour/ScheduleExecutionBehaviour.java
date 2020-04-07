package de.enflexit.energyAgent.core.behaviour;

import de.enflexit.energyAgent.core.EnergyAgentIO;
import energy.optionModel.Schedule;
import hygrid.env.HyGridAbstractEnvironmentModel.TimeModelType;
import jade.core.behaviours.CyclicBehaviour;

/**
 * The Class ScheduleExecutionBehaviour executes the specified {@link Schedule}
 * according to the current use case. Here, discrete of continuous time model are 
 * supported, as well as the use within simulations, test-bed environments or the
 * usage on dedicated hardware. 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ScheduleExecutionBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = -6431640573975521806L;

	private EnergyAgentIO agentIO;
	private TimeModelType timeModelType;
	private Schedule scheduleToExecute;
	
	/**
	 * Instantiates a new schedule execution behaviour.
	 * @param agentIO the current agent IO behaviour used
	 * @param timeModelType the current time model type used
	 */
	public ScheduleExecutionBehaviour(EnergyAgentIO agentIO, TimeModelType timeModelType) {
		this(agentIO, timeModelType, null);
	}
	/**
	 * Instantiates a new schedule execution behaviour.
	 * @param agentIO the current agent IO behaviour used
	 * @param timeModelType the current time model type used
	 * @param scheduleToExecute the schedule to execute
	 */
	public ScheduleExecutionBehaviour(EnergyAgentIO agentIO, TimeModelType timeModelType, Schedule scheduleToExecute) {
		this.agentIO = agentIO;
		this.timeModelType = timeModelType;
		this.setScheduleToExecute(scheduleToExecute);
	}
	
	/**
	 * Returns the current Schedule that is to be executed.
	 * @return the schedule to execute
	 */
	public Schedule getScheduleToExecute() {
		return scheduleToExecute;
	}
	/**
	 * Sets the schedule that is to be executed.
	 * @param scheduleToExecute the new schedule to execute
	 */
	public void setScheduleToExecute(Schedule scheduleToExecute) {
		this.scheduleToExecute = scheduleToExecute;
		this.restart();
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		if (this.timeModelType==null || this.timeModelType==TimeModelType.TimeModelDiscrete) {
			// --- Nothing to do in case of discrete time model. The needed --- 
			// --- system states will simply be requested by the IO behaviour -
			this.block();
			
		} else if (this.timeModelType==TimeModelType.TimeModelContinuous) {
			// --- In case of a continuous time model, check when the ---------
			// --- needed system state has to be set --------------------------

			long timeStamp = this.agentIO.getTime();
			// TODO
			
			
			
		}
	}

}
