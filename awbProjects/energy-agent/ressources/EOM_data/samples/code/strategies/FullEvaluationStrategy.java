package energy.samples.strategies;

import java.util.Vector;

import javax.swing.JComponent;

import energy.GlobalInfo;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.TechnicalSystemStateEvaluation;


/**
 * The Class FullEvaluationStrategy will fully investigate the possibilities 
 * of the current evaluation time span.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class FullEvaluationStrategy extends AbstractEvaluationStrategy {

	/**
	 * Instantiates a new full evaluation strategy.
	 * @param optionModelController the option model controller
	 */
	public FullEvaluationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#getCustomToolBarElements()
	 */
	@Override
	public Vector<JComponent> getCustomToolBarElements() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	
	public void runEvaluation() {
		
		// --- Initialise search --------------------------------------------------------
		TechnicalSystemStateEvaluation tsse = this.getInitialTechnicalSystemStateEvaluation();
		
		// --- Search by walking through time -------------------------------------------
		while (tsse.getGlobalTime() < this.getEndTime() ) {
			
			// --- Get the possible subsequent steps and states -------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size()==0) {
				System.err.println("No further delta steps possible => interrupt search!");
				break;
			}
			
			// --- Make a random decision for the next system state here ----------------
			int decisionIndex = GlobalInfo.getRandomInteger(0, deltaSteps.size()-1);
			TechnicalSystemStateDeltaEvaluation tssDeltaDecision = deltaSteps.get(decisionIndex);
			
			// --- Set new current TechnicalSystemStateEvaluation -----------------------
			TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaDecision);
			if (tsseNext==null) {
				System.err.println("Error while using selected delta => interrupt search!");
				break;
			} else {
				// --- Set next state as new current state ------------------------------
				tsse = tsseNext;
			}
			// --- Stop evaluation ? ----------------------------------------------------
			if (isStopEvaluation()==true) break;
		} // end while
		
		// --- Add the schedule found to the list of results ----------------------------
		this.addStateToResults(tsse);
		// --- Done ! -------------------------------------------------------------------
	}

	
}
