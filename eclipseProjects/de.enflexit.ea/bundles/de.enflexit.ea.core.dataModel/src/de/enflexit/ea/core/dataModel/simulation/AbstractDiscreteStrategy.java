package de.enflexit.ea.core.dataModel.simulation;

import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategyRT;

/**
 * The Class AbstractDiscreteStrategy serves as base class for strategies to be used in discrete simulations.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public abstract class AbstractDiscreteStrategy extends AbstractEvaluationStrategyRT implements DiscreteRTStrategyInterface {

	/**
	 * Instantiates a new abstract discrete strategy.
	 * @param optionModelController the option model controller
	 */
	public AbstractDiscreteStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {

		long pointInTime = this.evaluationStepEndTime;
		
		// --- Question: Do we have to consider previous states? --------------
		
		
		
		
	}
}
