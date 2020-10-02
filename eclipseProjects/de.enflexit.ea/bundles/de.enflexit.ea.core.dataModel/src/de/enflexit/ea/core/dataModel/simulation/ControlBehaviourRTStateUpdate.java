package de.enflexit.ea.core.dataModel.simulation;

import java.io.Serializable;

import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class ControlBehaviourRTStateUpdate is used to send system state updates 
 * from the real time control strategy of an energy agent to the simulation manager
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ControlBehaviourRTStateUpdate implements Serializable {

	private static final long serialVersionUID = -8774768616152419766L;

	private TechnicalSystemStateEvaluation technicalSystemStateEvaluation;
	
	
	/**
	 * Instantiates a new ControlBehaviourRTStateUpdate (default constructor).
	 */
	public ControlBehaviourRTStateUpdate() { }
	/**
	 * Instantiates a new ControlBehaviourRTStateUpdate.
	 * @param technicalSystemStateEvaluation the technical system state evaluation to be used for the update.
	 */
	public ControlBehaviourRTStateUpdate(TechnicalSystemStateEvaluation technicalSystemStateEvaluation) {
		this.setTechnicalSystemStateEvaluation(technicalSystemStateEvaluation);
	}
	
	
	/**
	 * Sets the {@link TechnicalSystemStateEvaluation} to be used for the update.
	 * @param technicalSystemStateEvaluation the new technical system state evaluation
	 */
	public void setTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation technicalSystemStateEvaluation) {
		this.technicalSystemStateEvaluation = technicalSystemStateEvaluation;
	}
	/**
	 * Returns the {@link TechnicalSystemStateEvaluation} to be used for the update.
	 * @return the technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
		return technicalSystemStateEvaluation;
	}
}
