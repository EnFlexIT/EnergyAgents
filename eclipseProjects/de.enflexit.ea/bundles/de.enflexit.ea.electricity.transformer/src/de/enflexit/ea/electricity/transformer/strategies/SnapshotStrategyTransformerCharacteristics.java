package de.enflexit.ea.electricity.transformer.strategies;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.jfree.data.xy.XYSeries;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import energy.OptionModelController;
import energy.helper.NumberHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class SnapshotStrategyTransformerCharacteristics.
 * 
 * @author Christian Derksen - SOFTEC - University Duisburg-Essen
 */
public class SnapshotStrategyTransformerCharacteristics extends AbstractTransformerSnapshotStrategy {
	
	private boolean debugPrintTargetVoltageLevelBand = false;
	
	/**
	 * Instantiates a new transformer snapshot strategy.
	 * @param optionModelController the option model controller
	 */
	public SnapshotStrategyTransformerCharacteristics(OptionModelController optionModelController) {
		super(optionModelController);
		// --- Set to print results during iteration ------  
		this.isPrintIterationSystemState = false;
		// --- Print the target voltage level range? ------
		if (this.debugPrintTargetVoltageLevelBand==true) {
			this.debugPrintTargetVoltageLevelBand();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.transformer.AbstractTransformerSnapshotStrategy#getTranformerEvaluationState(long, java.util.Vector, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public TransformerEvaluationState getTranformerEvaluationState(long evaluationTime, Vector<TechnicalSystemStateEvaluation> possSysStates, TechnicalSystemStateEvaluation tssePrev) {

		boolean debug = false;
		boolean debugAllTransformerSettings = false;
		
		// --- Get all possible TransformerSettings -----------------------------------------------
		List<TransformerSetting> tsList = this.getTransformerSettingFromPossibleSystemState(possSysStates);
		if (debugAllTransformerSettings==true) {
			this.debugPrintTransformerSettingList(tsList);
		}
		
		// --- Filter the transformer settings for allowed voltage range --------------------------
		TransformerSetting tsSelected = null;
		List<TransformerSetting> tsByGeneralVoltageBoundaries = this.getTransformerSettingByGeneralVoltageBoundaries(tsList);
		if (tsByGeneralVoltageBoundaries.size()>0) {
			// ------------------------------------------------------------------------------------
			// --- Found one or more settings that are in the general voltage range ---------------
			// ------------------------------------------------------------------------------------
			// --- Get the best TransformerSetting that matches the control characteristics -------   
			Collections.sort(tsByGeneralVoltageBoundaries, new Comparator<TransformerSetting>() {
				@Override
				public int compare(TransformerSetting ts1, TransformerSetting ts2) {
					Double vdNom1 = Math.abs(ts1.getVoltageDeltaRealToControlCharacteristics());
					Double vdNom2 = Math.abs(ts2.getVoltageDeltaRealToControlCharacteristics());
					return vdNom1.compareTo(vdNom2);
				}
			});
			tsSelected = tsByGeneralVoltageBoundaries.get(0);
			
		} else {
			// ------------------------------------------------------------------------------------
			// --- Could not find a setting that is in the general voltage range ------------------
			// ------------------------------------------------------------------------------------
			// --- Find the setting that is as closest as possible to the allowed range -----------
			tsSelected = this.getTransformerSettingWithLowestDistanceToNominalVoltageLevel(tsList);
		}
		if (debug==true) {
			System.out.println("=> Selected target voltage level: " + NumberHelper.round(tsSelected.getVoltageReal(), 2) + " V");
			System.out.println("=> Selected settings: " + tsSelected);
		}
		
		// ----------------------------------------------------------------------------------------
		// --- Return TransformerEvaluationState --------------------------------------------------
		return new TransformerEvaluationState(tsSelected.getTechnicalSystemStateEvaluation(),  DiscreteSystemStateType.Final);
	}
	
	
	// --------------------------------------------------------------------------------------------
	// --- From here, Some help method for the debugging ------------------------------------------
	// --------------------------------------------------------------------------------------------	
	/**
	 * Prints the target voltage level band for a XY-Chart (e.g. in Excel) that can show the residual load (x) and the relative voltage level in % plus boundaries (y).
	 */
	private void debugPrintTargetVoltageLevelBand() {
		
		// --- Get XY-series of control characteristics ---
		XYSeries xyControl = this.getTransformerDataModel().getControlCharacteristicsXySeries();
		double resLoadMin = xyControl.getMinX();
		double resLoadMax = xyControl.getMaxX();
		double resLoadStep = 1.0;

		// --- Manually set residual load range -----------  
		resLoadMin = -400.0;
		resLoadMax = 200.0;

		// -- Range settings & round precision ------------
		double boundaryLevelPercent = this.getTransformerDataModel().getControlCharacteristicsAllowedDeviation();
		int roundPrecision = 3; 
		
		// --- Print Header -------------------------------
		System.out.println("Res Load [kW]" + "\t" + "Target Voltage Level [V]" + "\t" + "Target Voltage Level [%]" + "\t" + "Upper Bound [%]" + "\t" + "Lower Bound [%]");

		// --- Print value table --------------------------
		double nomVoltageLevelInV = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000.0;
		for (double resLoadPrint = resLoadMin; resLoadPrint<=resLoadMax; resLoadPrint+=resLoadStep) {
			
			double targetVoltageLevel = this.getTransformerDataModel().getTransformerCharacteristicsHandler().getTargetVoltageLevelForLowVoltageLoadInV(resLoadPrint);
			double targetVoltageLevelPercent = this.getTransformerDataModel().getTransformerCharacteristicsHandler().getVoltageToNominalVoltageLevelInPercent(resLoadPrint);
			
			double boundaryStep 		= targetVoltageLevel * boundaryLevelPercent / 100;
			double upperBoundaryPercent = ((targetVoltageLevel + boundaryStep) / nomVoltageLevelInV) * 100.0;
			double lowerBoundaryPercent = ((targetVoltageLevel - boundaryStep) / nomVoltageLevelInV) * 100.0;
			
			// --- Round number values for the string -----
			StringBuilder sb = new StringBuilder();
			sb.append(NumberHelper.round(resLoadPrint, roundPrecision) + "\t");
			sb.append(NumberHelper.round(targetVoltageLevel, roundPrecision) + "\t");
			sb.append(NumberHelper.round(targetVoltageLevelPercent, roundPrecision) + "\t");
			sb.append(NumberHelper.round(upperBoundaryPercent, roundPrecision) + "\t");
			sb.append(NumberHelper.round(lowerBoundaryPercent, roundPrecision) + "\t");
			
			// --- Print to console -----------------------
			System.out.println(sb.toString());
		}
	}
	
}
