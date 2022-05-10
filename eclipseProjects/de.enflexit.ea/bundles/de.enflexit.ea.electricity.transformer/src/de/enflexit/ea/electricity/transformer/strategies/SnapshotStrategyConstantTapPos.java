package de.enflexit.ea.electricity.transformer.strategies;

import java.util.Vector;

import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedInteger;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class SnapshotStrategyConstantTapPos.
 * 
 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
 */
public class SnapshotStrategyConstantTapPos extends AbstractTransformerSnapshotStrategy {
	
	private Integer initialTapPosition;
	
	/**
	 * Instantiates a new transformer snapshot strategy.
	 * @param optionModelController the option model controller
	 */
	public SnapshotStrategyConstantTapPos(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.transformer.AbstractTransformerSnapshotStrategy#getTranformerEvaluationState(long, java.util.Vector, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public TransformerEvaluationState getTranformerEvaluationState(long evaluationTime, Vector<TechnicalSystemStateEvaluation> possSysStates, TechnicalSystemStateEvaluation tsseIterationResult) {

		TechnicalSystemStateEvaluation tsseDecision = null;
		if (tsseIterationResult==null) {
			// --- Select the state with the corresponding tap position -------
			for (int i = 0; i < possSysStates.size(); i++) {
				TechnicalSystemStateEvaluation tsseTmp = possSysStates.get(i);
				FixedInteger fIntTapPosCurr = (FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tsseTmp.getIOlist(), TransformerSystemVariable.tapPos.name());
				if (fIntTapPosCurr.getValue()==this.getInitialTapPosition().intValue()) {
					tsseDecision = tsseTmp;
					break;
				}
			}
			
		} else {
			// --- No further iteration, simply return the previous state -----
			tsseDecision = tsseIterationResult;
		}
		return new TransformerEvaluationState(tsseDecision, null);
	}
	
	/**
	 * Returns the initial tap position.
	 * @return the initial tap position
	 */
	public Integer getInitialTapPosition() {
		if (initialTapPosition==null) {
			TechnicalSystemStateEvaluation tsse = this.getInitialTechnicalSystemStateEvaluation();
			FixedInteger fIntTapPosToSet = (FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.tapPos.name());
			initialTapPosition = fIntTapPosToSet.getValue();
		}
		return initialTapPosition;
	}
	
	
}
