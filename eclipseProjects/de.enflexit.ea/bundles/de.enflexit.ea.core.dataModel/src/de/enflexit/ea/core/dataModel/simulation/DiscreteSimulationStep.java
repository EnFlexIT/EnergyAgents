package de.enflexit.ea.core.dataModel.simulation;

import java.io.Serializable;

import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class DiscreteSimulationStep is used in the context of discrete simulations to
 * transfer a current system state to the simulation manager.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class DiscreteSimulationStep implements Serializable {

	private static final long serialVersionUID = 7906198315000330449L;

	/**
	 * ??? The enumeration RoundType for types of simulation steps in discrete simulations ???.
	 */
	public enum RoundType {
		SingleRound,
		MultipleRound
	}

	/**
	 * The enumeration SystemStateType.
	 */
	public enum SystemStateType {
		Initial,
		Update,
		Final
	}
	
	private TechnicalSystemStateEvaluation technicalSystemStateEvaluation;
	private SystemStateType systemStateType;
	
	
	/**
	 * Instantiates a new discrete simulation step (default consructor).
	 */
	public DiscreteSimulationStep() { }
	/**
	 * Instantiates a new discrete simulation step.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation for the state update
	 * @param systemStateType the system state type
	 */
	public DiscreteSimulationStep(TechnicalSystemStateEvaluation tsse, SystemStateType systemStateType) {
		this.setTechnicalSystemStateEvaluation(tsse);
		this.setSystemStateType(systemStateType);
	}
	
	
	/**
	 * Sets the technical system state evaluation.
	 * @param technicalSystemStateEvaluation the new technical system state evaluation
	 */
	public void setTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation technicalSystemStateEvaluation) {
		this.technicalSystemStateEvaluation = technicalSystemStateEvaluation;
	}
	/**
	 * Returns the technical system state evaluation.
	 * @return the technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
		return technicalSystemStateEvaluation;
	}
	
	/**
	 * Sets the system state type.
	 * @param systemStateType the new system state type
	 */
	public void setSystemStateType(SystemStateType systemStateType) {
		this.systemStateType = systemStateType;
	}
	/**
	 * Returns the system state type.
	 * @return the system state type
	 */
	public SystemStateType getSystemStateType() {
		return systemStateType;
	}
	
}
