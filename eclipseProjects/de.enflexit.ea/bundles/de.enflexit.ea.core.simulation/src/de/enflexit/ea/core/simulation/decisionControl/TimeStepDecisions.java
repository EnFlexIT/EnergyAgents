package de.enflexit.ea.core.simulation.decisionControl;

import java.io.Serializable;
import java.util.TreeMap;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class TimeStepDecision describes the selected system states for a simulation time step 
 * in a discrete simulation. Further, it provides an information about the {@link DiscreteSystemStateType}
 * and thus allows to iterate in a single simulation time step if required.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TimeStepDecisions implements Serializable {

	private static final long serialVersionUID = 8068083040980961066L;
	
	private DiscreteSystemStateType discreteSystemStateType;
	private TreeMap<String, TechnicalSystemStateEvaluation> systemStates;
	
	
	/**
	 * Instantiates a new time step decision (default constructor).
	 */
	public TimeStepDecisions() { }
	/**
	 * Instantiates a new time step decision.
	 * @param discreteSystemStateType the discrete system state type
	 */
	public TimeStepDecisions(DiscreteSystemStateType discreteSystemStateType) {
		this.setDiscreteSystemStateType(discreteSystemStateType);
	}
	

	/**
	 * Sets the system state type.
	 * @param discreteSystemStateType the new system state type
	 */
	public void setDiscreteSystemStateType(DiscreteSystemStateType discreteSystemStateType) {
		this.discreteSystemStateType = discreteSystemStateType;
	}
	/**
	 * Returns the system state type.
	 * @return the system state type
	 */
	public DiscreteSystemStateType getDiscreteSystemStateType() {
		return discreteSystemStateType;
	}
	
	/**
	 * Returns the system states for the current time step. If not set or created yet, the result TreeMap will be created.
	 * @return the system states
	 */
	public TreeMap<String, TechnicalSystemStateEvaluation> getSystemStates() {
		if (systemStates==null) {
			systemStates = new TreeMap<String, TechnicalSystemStateEvaluation>();
		}
		return systemStates;
	}
	/**
	 * Sets the system states.
	 * @param systemStates the new system states
	 */
	public void setSystemStates(TreeMap<String, TechnicalSystemStateEvaluation> systemStates) {
		this.systemStates = systemStates;
	}
	
}
