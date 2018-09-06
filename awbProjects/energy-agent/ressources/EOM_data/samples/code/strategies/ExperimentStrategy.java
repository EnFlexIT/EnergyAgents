package energy.samples.strategies;

import java.util.Vector;

import javax.swing.JComponent;

import energy.GlobalInfo;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.TechnicalSystemStateEvaluation;


/**
 * The Class ExperimentStrategy will fully investigate the possibilities 
 * of the current evaluation time span.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class ExperimentStrategy extends AbstractEvaluationStrategy {

	/**
	 * Instantiates a new full evaluation strategy.
	 * @param optionModelController the option model controller
	 */
	public ExperimentStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#getCustomToolBarElements()
	 */
	@Override
	public Vector<JComponent> getCustomToolBarElements() {
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
			
			// --------------------------------------------------------------------------
			// --- Get the possible subsequent steps and states -------------------------
			// --------------------------------------------------------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size()==0) {
				System.err.println("No further 'deltaStepsPossible' => interrupt search!");
				break;
			}
			
			// --------------------------------------------------------------------------
			// --- Prepare the next time step -------------------------------------------
			// --------------------------------------------------------------------------			
			// --- Make a decision ------------------------------------------------------
			int decisionIndex = 0;
//			// --- Find specific state --------------------------------------------------
//			for (int i = 0; i < deltaSteps.size(); i++) {
//				if (deltaSteps.get(i).getTechnicalSystemStateEvaluation().getStateID().equals("Load") ) {
//					decisionIndex = i;
//					break;
//				}
//			}
			decisionIndex = GlobalInfo.getRandomInteger(0, deltaSteps.size()-1);
			TechnicalSystemStateDeltaEvaluation tssDeltaEvaluation = deltaSteps.get(decisionIndex);
			
			// --- Replace TechnicalSystemStateEvaluation by the next one ---------------
			TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaEvaluation);
			while (tsseNext==null) {
				// --- Error with the previous decision - make a new decision -----------
				decisionIndex++;
				if (decisionIndex > (deltaSteps.size()-1)) break;
				
				tssDeltaEvaluation = deltaSteps.get(decisionIndex);
				tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaEvaluation);
				if (isStopEvaluation()==true) break;
			}
			
			// --------------------------------------------------------------------------
			// --- Set new current TechnicalSystemStateEvaluation -----------------------
			// --------------------------------------------------------------------------
			if (tsseNext==null) {
				System.err.println("No possible delta evaluation could be found!");
				break;
			} else {
				tsse = tsseNext;
			}
			
			// --- Stop evaluation ? ----------------------------------------------------
			if (isStopEvaluation()==true) break;
		}
		
		// --- Add search run to the overall results ....................................
		this.addStateToResults(tsse);
	
//		System.out.println("GraphNodes for States: " + this.getGraphForStateVariability().getVertexCount());
//		System.out.println("GraphEdges for States: " + this.getGraphForStateVariability().getEdgeCount());

//		System.out.println("Delta Costs Nodes: " + this.getGraphForDeltaCosts().getVertexCount());
//		System.out.println("Delta Costs Edges: " + this.getGraphForDeltaCosts().getEdgeCount());
		
	}

	
}
