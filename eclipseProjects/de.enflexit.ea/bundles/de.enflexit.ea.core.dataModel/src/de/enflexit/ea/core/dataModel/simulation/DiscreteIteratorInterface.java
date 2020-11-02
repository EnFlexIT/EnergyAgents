package de.enflexit.ea.core.dataModel.simulation;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.AbstractSnapshotStrategy;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import energygroup.evaluation.AbstractGroupSnapshotStrategy;

/**
 * The Interface DiscreteIteratorInterface provides the required method extensions for extended classes that are based on 
 * an {@link AbstractEvaluationStrategyRT}, an {@link AbstractGroupEvaluationStrategyRT}, {@link AbstractSnapshotStrategy}
 * or an {@link AbstractGroupSnapshotStrategy} to be used in the context of discrete iterable simulations.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public interface DiscreteIteratorInterface {

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
