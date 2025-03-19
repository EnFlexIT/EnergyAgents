package de.enflexit.ea.electricity.transformer.strategies;

import java.util.Vector;

import de.enflexit.awb.simulation.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import energy.OptionModelController;
import energy.helper.NumberHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class TransformerSnapshotStrategy.
 * 
 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
 */
public class SnapshotStrategyRandom extends AbstractTransformerSnapshotStrategy {
	
	private int myIterationCounter;
	
	/**
	 * Instantiates a new transformer snapshot strategy.
	 * @param optionModelController the option model controller
	 */
	public SnapshotStrategyRandom(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.transformer.AbstractTransformerSnapshotStrategy#getTranformerEvaluationState(long, java.util.Vector, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public TransformerEvaluationState getTranformerEvaluationState(long evaluationTime, Vector<TechnicalSystemStateEvaluation> possSysStates, TechnicalSystemStateEvaluation tsseIterationResult) {

		// ----------------------------------------------------------
		// --- Check for new evaluation time ------------------------
		// ----------------------------------------------------------
		if (this.isNewEvaluationEndTime()==true) {
			this.myIterationCounter = 0;
		}
		
		DiscreteSystemStateType discreteSystemStateType = null;
		TechnicalSystemStateEvaluation tsseDecision = tsseIterationResult;
		if (tsseIterationResult==null) {
			// -- Get TSSE from selection method --------------------
			tsseDecision = this.selectSystemState(possSysStates);
			this.myIterationCounter++;
			
		} else {
			// --- See own DiscreteSystemStateType ------------------
			if (this.myIterationCounter>=2) {
				// --- No further iteration -------------------------
				discreteSystemStateType = DiscreteSystemStateType.Final;
			} else {
				// --- Do another iteration -------------------------
				discreteSystemStateType = DiscreteSystemStateType.Iteration;
				tsseDecision = this.selectSystemState(possSysStates);
				this.myIterationCounter++;
			}
		}
		return new TransformerEvaluationState(tsseDecision, discreteSystemStateType);
	}
	
	/**
	 * Select system state.
	 *
	 * @param possSysStates the poss sys states
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation selectSystemState(Vector<TechnicalSystemStateEvaluation> possSysStates) {
		
		// ----------------------------------------------------------
		// --- Here, the individual code can be placed --------------
		// ----------------------------------------------------------
		int iRandom = NumberHelper.getRandomInteger(0, possSysStates.size()-1);
		TechnicalSystemStateEvaluation tsseDecision = possSysStates.get(iRandom);
//		String ioListValues = TechnicalSystemStateHelper.toString(tsseDecision.getIOlist(), ", ");
//		DisplayHelper.systemOutPrintlnGlobalTime(this.getEvaluationEndTime(), "[" + this.getClass().getSimpleName() + "]", "Answer with " + ioListValues);

		return tsseDecision;
	}
	
	
}
