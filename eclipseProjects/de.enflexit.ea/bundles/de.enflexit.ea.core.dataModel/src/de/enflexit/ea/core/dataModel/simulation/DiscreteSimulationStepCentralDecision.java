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
	
	/**
	 * Instantiates a new discrete simulation step for central decision processes.
	 * @param tsseVector the vector with the possible system states
	 */
	public DiscreteSimulationStepCentralDecision(Vector<TechnicalSystemStateEvaluation> tsseVector) {
		super(null, DiscreteSystemStateType.Iteration);
		this.setPossibleSystemStates(tsseVector);
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
		displayText += " "; 
		
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
