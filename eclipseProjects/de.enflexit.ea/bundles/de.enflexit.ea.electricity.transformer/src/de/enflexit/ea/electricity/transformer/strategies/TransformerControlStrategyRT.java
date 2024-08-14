package de.enflexit.ea.electricity.transformer.strategies;

import java.util.Vector;

import de.enflexit.common.DateTimeHelper;
import de.enflexit.ea.electricity.transformer.InternalDataModel;
import de.enflexit.ea.electricity.transformer.TransformerAgent;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.helper.TechnicalSystemStateDeltaHelper;
import energy.helper.TechnicalSystemStateDeltaHelper.DeltaSelectionBy;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedInteger;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class TransformerControlStrategyRT.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerControlStrategyRT extends AbstractEvaluationStrategyRT {
	
	private TransformerAgent transformerAgent;
	private InternalDataModel internalDataModel;
	private TransformerDataModel transformerDataModel;

	/**
	 * Instantiates a new droop control strategy RT.
	 * @param optionModelController the option model controller
	 */
	public TransformerControlStrategyRT(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/**
	 * Return the current transformer agent.
	 * @return the transformer agent
	 */
	protected TransformerAgent getTransformerAgent() {
		if (transformerAgent==null) {
			transformerAgent = (TransformerAgent) this.optionModelController.getControllingAgent();
		}
		return transformerAgent;
	}
	/**
	 * Returns the internal data model of the BatteryAgent.
	 * @return the internal data model
	 */
	protected InternalDataModel getInternalDataModel() {
		if (internalDataModel==null && this.getTransformerAgent()!=null) {
			internalDataModel = this.getTransformerAgent().getInternalDataModel();
		}
		return internalDataModel;
	}
	/**
	 * Return the transformer data model.
	 * @return the transformer data model
	 */
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel==null) {
			SystemVariableDefinitionStaticModel sysVarDefStaticModel = (SystemVariableDefinitionStaticModel) this.optionModelController.getSystemVariableDefinition(this.optionModelController.getTechnicalSystem().getSystemVariables(), "StaticParameters");
			transformerDataModel = (TransformerDataModel) this.optionModelController.getStaticModelInstance(sysVarDefStaticModel);
		}
		return transformerDataModel;
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {

		boolean debug = false;
		debug = debug && this.getTransformerAgent().getLocalName().equals("n35");
		
		// --- Search by walking through time ---------------------------------
		TechnicalSystemStateEvaluation tsse = this.getTechnicalSystemStateEvaluation();
		if (debug==true) {
			String evalEndTime = DateTimeHelper.getTimeAsString(this.getEvaluationEndTime(), "HH:mm:ss");
			String globalTime  = DateTimeHelper.getTimeAsString(tsse.getGlobalTime(), "HH:mm:ss");
			System.out.println("\n[" + this.getClass().getSimpleName() + "] Evaluation End-Time: " + evalEndTime + ", TSSE-globalTime: " + globalTime);
		}
		
		while (tsse.getGlobalTime() < this.getEvaluationEndTime()) {
			
			// --- Get all possible subsequent steps and states ---------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size() == 0) {
				System.err.println("No further delta steps possible => interrupt search!");
				break;
			}
			
			// --- Decide for the next system state ---------------------------
			int oldTransformerStep = ((FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(),TransformerSystemVariable.tapPos.name())).getValue();
			int newTransformerStep = oldTransformerStep;
			
			TechnicalSystemStateDeltaEvaluation tssDeltaDecision = null;
			Vector<TechnicalSystemStateDeltaEvaluation> filteredSteps = TechnicalSystemStateDeltaHelper.filterTechnicalSystemStateDeltaEvaluation(deltaSteps, DeltaSelectionBy.IO_List, TransformerSystemVariable.tapPos.name(), newTransformerStep);
			if (filteredSteps.size()>0) {
				tssDeltaDecision = filteredSteps.get(0);
			}
			
			// --- 
			if (tssDeltaDecision==null) {
				System.err.println("[" + this.getClass().getSimpleName() + "]: No valid subsequent state found!");
				break;
			}
			
			// --- Set new current TechnicalSystemStateEvaluation -------------
			TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaDecision);
			if (tsseNext == null) {
				System.err.println("Error while using selected delta => interrupt search!");
				break;
			} else {
				// --- Set next state as new current state --------------------
				tsse = tsseNext;
				if (debug==true) {
					String globalTime  = DateTimeHelper.getTimeAsString(tsse.getGlobalTime(), "HH:mm:ss");
					System.out.println("[" + this.getClass().getSimpleName() + "] Next TSSE-globalTime: " + globalTime);
				}
			}
			this.setTechnicalSystemStateEvaluation(tsse);
			this.setIntermediateStateToResult(tsse);
			 
		}// end while
		if (debug) {
			System.out.println();
		}
	}

	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategyRT#getInitialStateAdaption()
	 */
	@Override
	public InitialStateAdaption getInitialStateAdaption() {
		return null;
	}
}

