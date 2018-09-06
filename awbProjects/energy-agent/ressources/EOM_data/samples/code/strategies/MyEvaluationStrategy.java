package energy.samples.strategies;

import java.util.Vector;

import javax.swing.JComponent;

import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.optionModel.TechnicalSystemStateEvaluation;

public class MyEvaluationStrategy extends AbstractEvaluationStrategy {

	public MyEvaluationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}

	@Override
	public Vector<JComponent> getCustomToolBarElements() {
		return null;
	}

	@Override
	public void runEvaluation() {
		// --- Get initial systems state here ---
		TechnicalSystemStateEvaluation previousState = this.getInitialTechnicalSystemStateEvaluation();
		
		// ------------------------------------------------
		// --- Possibly here a loop -----------------------
		// ------------------------------------------------
		TechnicalSystemStateEvaluation targetState = new TechnicalSystemStateEvaluation();
		// --- do what is needed here -----------
		targetState.setParent(previousState); // - ! ! ! --
		// --------------------------------------
		// --- Do what is needed here -----------
		// --------------------------------------
		// ------------------------------------------------
		
		// --- Add result schedule --------------
		this.addStateToResults(targetState);
		// --- Done ! ---------------------------
	}

}
