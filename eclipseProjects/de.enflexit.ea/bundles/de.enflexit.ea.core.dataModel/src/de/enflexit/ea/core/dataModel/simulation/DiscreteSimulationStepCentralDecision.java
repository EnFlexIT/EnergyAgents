package de.enflexit.ea.core.dataModel.simulation;

import java.util.Vector;

import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class DiscreteSimulationStepCentralDecision is used in snapshot simulations with centralized
 * decision processes and will transfer all possible system states to that central process.
 * 
 * @author Christian Derksen - SOFTEC - University of Duisburg-Essen
 */
public class DiscreteSimulationStepCentralDecision extends DiscreteSimulationStep {

	private static final long serialVersionUID = 6904841448098955429L;

	private Vector<TechnicalSystemStateEvaluation> possibleSystemStatesVector;
	private boolean requiresEnvironmentModelInformation;
	
	/**
	 * Instantiates a new discrete simulation step for central decision processes.
	 *
	 * @param tsseVector the vector with the possible system states
	 * @param requiresEnvironmentModelInformation indicator that the system requires environment model information (e.g. after network calculation)
	 */
	public DiscreteSimulationStepCentralDecision(Vector<TechnicalSystemStateEvaluation> tsseVector, boolean requiresEnvironmentModelInformation) {
		super(null, DiscreteSystemStateType.Iteration);
		this.setPossibleSystemStates(tsseVector);
		this.setRequiresEnvironmentModelInformation(requiresEnvironmentModelInformation);
	}

	/**
	 * Return the possible system states.
	 * @return the possible system states
	 */
	public Vector<TechnicalSystemStateEvaluation> getPossibleSystemStates() {
		return possibleSystemStatesVector;
	}
	/**
	 * Sets the possible system states for the discrete simulation step.
	 * @param newTsseVector the new possible system states
	 */
	public void setPossibleSystemStates(Vector<TechnicalSystemStateEvaluation> newTsseVector) {
		this.possibleSystemStatesVector = newTsseVector;
	}
	
	/**
	 * Sets the indicator that the current system requires environment model information 
	 * (e.g. for artificial sensor data that may be received after network calculation via blackboard).
	 * @param requiresEnvironmentModelInformation the new system requires environment information
	 */
	public void setRequiresEnvironmentModelInformation(boolean requiresEnvironmentModelInformation) {
		this.requiresEnvironmentModelInformation = requiresEnvironmentModelInformation;
	}
	/**
	 * Checks if is system requires environment model information.
	 * @return true, if is system requires environment model information
	 */
	public boolean requiresEnvironmentModelInformation() {
		return requiresEnvironmentModelInformation;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep#toString()
	 */
	@Override
	public String toString() {
		
		String displayText = "";
		
		DiscreteSystemStateType stateType = this.getDiscreteSystemStateType();
		if (stateType==null) {
			displayText = "[No DiscreteSystemStateType defined!]";
		} else {
			displayText = "[" + this.getDiscreteSystemStateType().name() + "]";
		}
		displayText += " Requires environment information: " + this.requiresEnvironmentModelInformation() + ", ";
		
		if (this.getPossibleSystemStates()==null) {
			displayText += "No";
		} else {
			displayText += this.getPossibleSystemStates().size();
		}
		displayText += " possible system states\n";
		
		if (this.getPossibleSystemStates()!=null) {
			for (int i = 0; i < this.getPossibleSystemStates().size(); i++) {
				displayText += TechnicalSystemStateHelper.toString(this.getPossibleSystemStates().get(i), false) + "\n"; 
			}
		}
		return displayText;
	}
	
}
