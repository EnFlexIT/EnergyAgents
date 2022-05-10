package de.enflexit.ea.electricity.transformer.strategies;

import java.util.List;
import java.util.Vector;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.helper.NumberHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class SnapshotStrategyConstantTapPos.
 * 
 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
 */
public class SnapshotStrategyControlledVoltageLevel extends AbstractTransformerSnapshotStrategy {
	
	/**
	 * Instantiates a new transformer snapshot strategy.
	 * @param optionModelController the option model controller
	 */
	public SnapshotStrategyControlledVoltageLevel(OptionModelController optionModelController) {
		super(optionModelController);
		this.isPrintIterationSystemState = false;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.transformer.AbstractTransformerSnapshotStrategy#getTranformerEvaluationState(long, java.util.Vector, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public TransformerEvaluationState getTranformerEvaluationState(long evaluationTime, Vector<TechnicalSystemStateEvaluation> possSysStates, TechnicalSystemStateEvaluation tsseIterationResult) {

		boolean debug = false;
		boolean debugAllTransformerSettings = false;
		
		// ----------------------------------------------------------------------------------------
		// --- Debug print the current control node voltage level ---------------------------------  
		if (debug==true) {
			FixedDouble fdCnVoltageReal = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(possSysStates.get(0).getIOlist(), TransformerSystemVariable.cnVoltageReal.name());
			double cnVoltageReal = fdCnVoltageReal==null ? 0.0 : fdCnVoltageReal.getValue(); 
			this.debugPrint(possSysStates.get(0).getGlobalTime(), "Control node real voltage level: " + NumberHelper.round(cnVoltageReal, 2));
		}
		
		
		// ----------------------------------------------------------------------------------------
		// --- Get all possible TransformerSettings -----------------------------------------------
		List<TransformerSetting> transfomerSettingList = this.getTransformerSettingFromPossibleSystemState(possSysStates);
		if (debugAllTransformerSettings==true) {
			this.debugPrintTransformerSettingList(transfomerSettingList);
		}
		
		// ----------------------------------------------------------------------------------------		
		// --- Filter the transformer settings for control node boundaries ------------------------
		TransformerSetting tsSelected = null;
		List<TransformerSetting> tsByControlNodeBoundaries = this.getTransformerSettingByControlNodeBoundaries(transfomerSettingList);
		if (debugAllTransformerSettings==true) {
			System.out.println("=> TransformerSettings that matching the control node boundaries:");
			this.debugPrintTransformerSettingList(tsByControlNodeBoundaries);
		}
		if (tsByControlNodeBoundaries.size()>0) {
			// ------------------------------------------------------------------------------------
			// --- Found one or more settings that allows to use control node boundaries ----------
			// ------------------------------------------------------------------------------------
			// --- Get the best TransformerSetting that matches the control node boundaries -------   
			tsSelected = this.getTransformerSettingWithLowestDistanceToNominalVoltageLevel(tsByControlNodeBoundaries);
		} else {
			// ------------------------------------------------------------------------------------
			// --- Could not find a setting that allows to use control node boundaries ------------
			// ------------------------------------------------------------------------------------
			// --- => Get settings that allow to be within the general boundaries -----------------
			List<TransformerSetting> tsByGeneralBoundaries = this.getTransformerSettingByGeneralVoltageBoundaries(transfomerSettingList);
			if (tsByGeneralBoundaries.size()>0) {
				// --- Get the best TransformerSetting that matches the general boundaries --------
				tsSelected = this.getTransformerSettingWithLowestDistanceToNominalVoltageLevel(tsByGeneralBoundaries);
			} else {
				// --- Find the setting that is as closest as possible to the allowed range ------- 
				tsSelected = this.getTransformerSettingWithLowestDistanceToNominalVoltageLevel(transfomerSettingList);
			}
		}
		if (debug==true) this.debugPrint(tsSelected.getTechnicalSystemStateEvaluation().getGlobalTime(), "Selected transformer state: " + tsSelected);
		
		
		// ----------------------------------------------------------------------------------------
		// --- Return TransformerEvaluationState --------------------------------------------------
		return new TransformerEvaluationState(tsSelected.getTechnicalSystemStateEvaluation(),  DiscreteSystemStateType.Final);
	}
	
}
