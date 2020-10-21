package de.enflexit.ea.core.dataModel.simulation;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;

/**
 * The Interface DiscreteRTStrategyInterface provides the required method extensions for an 
 * {@link AbstractEvaluationStrategyRT} or an {@link AbstractGroupEvaluationStrategyRT} to be
 * used in the context of discrete iterable simulation step.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public interface DiscreteRTStrategyInterface {

	/**
	 * Has to answer the question, if the currently reached and specified system state (a {@link TechnicalSystemStateEvaluation}) 
	 * requires further iterations or if this is the final system state for the current discrete simulation step.
	 * For the handling, a local variable should be introduced to handle the {@link DiscreteSystemStateType}
	 *
	 * @param tsse the current TechnicalSystemStateEvaluation
	 * @return the DiscreteSystemStateType for the current / last evaluation step
	 * @see #setDiscreteSystemStateType(DiscreteSystemStateType)
	 */
	public DiscreteSystemStateType getDiscreteSystemStateType(TechnicalSystemStateEvaluation tsse);

	/**
	 * Here, the discrete system state type should be set with the end of the method-call of {@link AbstractEvaluationStrategyRT#runEvaluationUntil(long)}.
	 * For this, a local variable should be introduced to handle the {@link DiscreteSystemStateType} 
	 *
	 * @param systemState the new discrete system state type
	 * @see #getDiscreteSystemStateType(TechnicalSystemStateEvaluation) 
	 */
	public void setDiscreteSystemStateType(DiscreteSystemStateType systemState);
	
}
