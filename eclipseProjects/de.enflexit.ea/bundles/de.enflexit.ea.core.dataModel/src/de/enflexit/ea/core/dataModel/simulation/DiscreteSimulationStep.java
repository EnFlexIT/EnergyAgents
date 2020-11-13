package de.enflexit.ea.core.dataModel.simulation;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class DiscreteSimulationStep is used in the context of discrete simulations to
 * transfer a current system states to the simulation manager.
 * 
 * @author Christian Derksen - SOFTEC - University of Duisburg-Essen
 */
public class DiscreteSimulationStep extends AbstractDiscreteSimulationStep<TechnicalSystemStateEvaluation> {

	private static final long serialVersionUID = 7906198315000330449L;

	/**
	 * Instantiates a new discrete simulation step for a system state represented by a {@link TechnicalSystemStateEvaluation}.
	 *
	 * @param systemState the {@link TechnicalSystemStateEvaluation} to use
	 * @param discreteSystemStateType the discrete system state type
	 */
	public DiscreteSimulationStep(TechnicalSystemStateEvaluation systemState, DiscreteSystemStateType discreteSystemStateType) {
		super(systemState, discreteSystemStateType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
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
		displayText += "\n"; 
		
		TechnicalSystemStateEvaluation tsse = this.getSystemState();
		if (tsse==null) {
			displayText += "No TechnicalSystemStateEvaluation defined!";
		} else {
			displayText += TechnicalSystemStateHelper.toString(tsse, true); 
		}
		return displayText;
	}
	
}
