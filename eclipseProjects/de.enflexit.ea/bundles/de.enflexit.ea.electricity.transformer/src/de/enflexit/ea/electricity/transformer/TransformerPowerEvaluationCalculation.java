package de.enflexit.ea.electricity.transformer;

import de.enflexit.ea.core.EnergyAgentConnector;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TapSide;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TransformerSystemVariable;
import energy.FixedVariableList;
import energy.OptionModelController;
import energy.calculations.AbstractEvaluationCalculation;
import energy.helper.NumberHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.Connectivity;
import energy.optionModel.CostFunctionDataSeries;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.InputMeasurement;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * This class performs the actual calculations for the evaluations of the reference pv EOM model
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TransformerPowerEvaluationCalculation extends AbstractEvaluationCalculation {
	
	private boolean isPrintCalculationResults = false;
	
	private EnergyAgentConnector<TransformerAgent, InternalDataModel> eaConnector;
	private TransformerDataModel transformerDataModel;
	
	private double transformerLowVoltageLevelReal;
	private double resistance_R;
	private double reactance_X;

	private TransformerPower lvPowerAllPhases;
	private TransformerPower lvPowerL1;
	private TransformerPower lvPowerL2;
	private TransformerPower lvPowerL3;
	
	private TransformerPower uvPowerAllPhases;
	private TransformerPower uvPowerL1;
	private TransformerPower uvPowerL2;
	private TransformerPower uvPowerL3;
	
	
	// ------------------------------------------------------------------------
	// --- To connect to the current EnergyAgent ------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the energy agent connector for this class.
	 * @return the energy agent connector
	 */
	private EnergyAgentConnector<TransformerAgent, InternalDataModel> getEnergyAgentConnector() {
		if (eaConnector==null) {
			eaConnector = new EnergyAgentConnector<TransformerAgent, InternalDataModel>(this.getOptionModelController());
		}
		return eaConnector;
	}
	/**
	 * Returns the transformer agents internal data model.
	 * @return the internal data model
	 */
	private InternalDataModel getInternalDataModel() {
		return this.getEnergyAgentConnector().getInternalDataModel();
	}
	
	// ------------------------------------------------------------------------
	// --- Get the current transformer data model -----------------------------
	// ------------------------------------------------------------------------
	/**
	 * Returns the transformer data model.
	 * @return the transformer data model
	 */
	private TransformerDataModel getTransformerDataModel() {
		if (this.transformerDataModel == null) {
			// --- Get the agents TransformerDataModel ------------------------
			if (this.getEnergyAgentConnector().getEnergyAgent()!=null) {
				this.transformerDataModel = this.getInternalDataModel().getTransformerDataModel();
			} else {
				OptionModelController omc = this.getOptionModelController();
				if (omc!=null) {
					SystemVariableDefinitionStaticModel sysVarDefStaticModel = (SystemVariableDefinitionStaticModel) this.getOptionModelController().getSystemVariableDefinition(this.getOptionModelController().getTechnicalSystem().getSystemVariables(), "StaticParameters");
					this.transformerDataModel = (TransformerDataModel) this.getOptionModelController().getStaticModelInstance(sysVarDefStaticModel);
				}
			}
		}
		return this.transformerDataModel;
	}
	
	/**
	 * Checks if the transformer is used and loaded. Therefore, the real current 
	 * of the low voltage side will be checked for a current greater zero. 
	 *
	 * @param tss the current system state
	 * @return true, if is transformer loaded
	 */
	private boolean isTransformerUsed(TechnicalSystemState tss) {
		
		double checkCurrent = 0.0;
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==false) {
			// --- Uni-phase power flow -----------------------------
			FixedDouble fdLvTotaCurrentRealAllPhases = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealAllPhases.name());
			checkCurrent = fdLvTotaCurrentRealAllPhases==null ? 0.0 : fdLvTotaCurrentRealAllPhases.getValue();
			
		} else {
			// --- Tri-phase power flow -----------------------------
			FixedDouble lvTotaCurrentRealL1 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealL1.name());
			double lvCurrRealL1 = lvTotaCurrentRealL1==null ? 0.0 : lvTotaCurrentRealL1.getValue();
			checkCurrent += lvCurrRealL1;
			if (checkCurrent>0) return true;	// may accelerate execution
			
			FixedDouble lvTotaCurrentRealL2 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealL2.name());
			double lvCurrRealL2 = lvTotaCurrentRealL2==null ? 0.0 : lvTotaCurrentRealL2.getValue();
			checkCurrent += lvCurrRealL2;
			if (checkCurrent>0) return true;	// may accelerate execution
			
			FixedDouble lvTotaCurrentRealL3 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealL3.name());
			double lvCurrRealL3 = lvTotaCurrentRealL3==null ? 0.0 : lvTotaCurrentRealL3.getValue();
			checkCurrent += lvCurrRealL3;
			if (checkCurrent>0) return true;	// may accelerate execution
			
			checkCurrent = lvCurrRealL1 + lvCurrRealL2 + lvCurrRealL3; 
		}
		return checkCurrent>0;
	}
	
	/**
	 * Calculate slack voltage.
	 * @param tss the TechnicalSystemState
	 */
	private void calculateSlackVoltage(TechnicalSystemState tss) {
		
		double U_rTUS = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000;
		double U_rTOS = this.getTransformerDataModel().getUpperVoltage_vmHV() * 1000;

		TapSide slackNodeSide = this.getTransformerDataModel().getSlackNodeSide();
		double configuredVoltagelevel = this.getTransformerDataModel().getSlackNodeVoltageLevel();
		double transLVRealAllPhases = configuredVoltagelevel;		// --- Just as a default value ---
		
		// --- Calculate based on slack node side -----------------------------
		switch (slackNodeSide) {
		case LowVoltageSide:
			// --- Correct configured slack node voltage ----------------------
			if (configuredVoltagelevel==0.0) {
				configuredVoltagelevel = U_rTUS;
			}
			transLVRealAllPhases = configuredVoltagelevel;
			break;

		case HighVoltageSide:
			// --- Correct configured slack node voltage ----------------------
			if (configuredVoltagelevel==0.0) {
				configuredVoltagelevel = U_rTOS;
			}
			
			// --- Check for measurement of HV voltage level ------------------
			FixedDouble hvVoltageLevelAllPhases = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tss.getIOlist(), TransformerSystemVariable.hvVoltageRealAllPhases.name());
			if (hvVoltageLevelAllPhases!=null) {
				double hvMeasurementVoltageReal = hvVoltageLevelAllPhases.getValue();
				if (hvMeasurementVoltageReal==0) {
					// --- Check if the transformer is used -------------------
					if (this.isTransformerUsed(tss)==true) {
						// --- Use the above voltage level as fallback --------
						// --- => Nothing to do here --------------------------
					} else {
						// --- Really 0.0: use this measurement ---------------
						configuredVoltagelevel = hvVoltageLevelAllPhases.getValue();						
					}
				} else {
					configuredVoltagelevel = hvVoltageLevelAllPhases.getValue();
				}
			}
			
			// --- Get Tap information ----------------------------------------
			TapSide tapSide = this.getTransformerDataModel().getTapSide();
			double tapNeutral = this.getTransformerDataModel().getTapNeutral();
			double voltageDeltaPerTap = this.getTransformerDataModel().getVoltageDeltaPerTap_dVm();
			FixedInteger tapPos = (FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tss.getIOlist(), TransformerSystemVariable.tapPos.name());

			// --- Calculate slack voltage ------------------------------------
			switch (tapSide) {
			case LowVoltageSide:
				transLVRealAllPhases = configuredVoltagelevel / U_rTOS * U_rTUS * (1 + voltageDeltaPerTap * (tapPos.getValue() - tapNeutral)/100);
				break;

			case HighVoltageSide:
				transLVRealAllPhases = configuredVoltagelevel / U_rTOS * U_rTUS / (1 + voltageDeltaPerTap * (tapPos.getValue() - tapNeutral)/100);
				break;
			}
			break;
		}
		
		// --- TODO Martin: Check if this is right ----------------------------
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			this.setTransformerLowVoltageLevelReal(transLVRealAllPhases / Math.sqrt(3));
		} else {
			this.setTransformerLowVoltageLevelReal(transLVRealAllPhases);
		}
	}
	/**
	 * Calculate transformer impedance.
	 * @param tss the TechnicalSystemState
	 */
	private void calculateTransformerImpedance(TechnicalSystemState tss) {
		
		//Get Values from DataModel
		double U_rTUS = this.getTransformerDataModel().getLowerVoltage_vmLV();
		double u_kr = this.getTransformerDataModel().getShortCircuitImpedance_vmImp();
		double S_rT = this.getTransformerDataModel().getRatedPower_sR();
		double P_krT = this.getTransformerDataModel().getCopperLosses_pCu();
		
		//Calculation
		double u_Rr = P_krT / (S_rT*1000) * 100;
		double Z_1TUS = u_kr / 100 * U_rTUS*U_rTUS / S_rT;
		double R_1TUS = u_Rr / 100 * U_rTUS*U_rTUS / S_rT;
		double X_1TUS = Math.sqrt(Z_1TUS*Z_1TUS - R_1TUS*R_1TUS);
		
		//Save calculated data
		this.resistance_R = R_1TUS;
		this.reactance_X = X_1TUS;
	}
	
	/**
	 * Perform the calculations for the given {@link TechnicalSystemState}
	 * @param tss the TechnicalSystemState to calculate for
	 */
	public void calculate(TechnicalSystemState tss) {

		boolean voltageViolations = false;
		double residualLoadPAllPhases = 0;
		double residualLoadQAllPhases = 0;
		double trafoUtilization = 0;
		double trafoLossesPAllPhases = 0;
		double trafoLossesQAllPhases = 0;
		double uvLoadPAllPhases = 0;
		double uvLoadQAllPhases = 0;
		
		// --- Do required calculations ---------------------------------------
		this.calculateSlackVoltage(tss); 			// Slack Voltage
		this.calculateTransformerImpedance(tss); 	// Transformer Impedance

		// --- TODO: Es fehlen noch die Leerlauf-Verluste! ------
		// --- Magnetisierungsstrom iMagO
		
		// --- Case separation for number of phases ---------------------------
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==false) {
			// --- UniPhase calculation ---------------------------------------
			FixedDouble fdLvTotaCurrentRealAllPhases = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealAllPhases.name());
			FixedDouble fdLvTotaCurrentImagAllPhases = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentImagAllPhases.name());
			
			// --- If there are no current information, simply exit -----------
			boolean isMissingValues = fdLvTotaCurrentRealAllPhases==null || fdLvTotaCurrentImagAllPhases==null;
			if (isMissingValues==true) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Missing input values for further calculations: UniPhase - electrical currents!");
				return;
			}
			
			// Current, cosPhi and sinPhi
			double lvTotaCurrentRealAllPhases = fdLvTotaCurrentRealAllPhases.getValue(); 
			double lvTotaCurrentImagAllPhases = fdLvTotaCurrentImagAllPhases.getValue(); 
			
			// Calculate voltage drop on transformer
			double dURealAllPhases = this.resistance_R * lvTotaCurrentRealAllPhases - this.reactance_X * lvTotaCurrentImagAllPhases;
			double dUImagAllPhases = this.reactance_X * lvTotaCurrentRealAllPhases + this.resistance_R * lvTotaCurrentImagAllPhases;
			
			
			// --- Specify for Slack Voltage----------------------------------- 
			double voltageRealAllPhases = this.getTransformerLowVoltageLevelReal() - dURealAllPhases;
			double voltageImagAllPhases = -dUImagAllPhases;
			switch (this.getTransformerDataModel().getSlackNodeSide()) {
			case LowVoltageSide:
				// --- Calculate Voltage for Slack node on LV site ------------
				voltageRealAllPhases = this.getTransformerLowVoltageLevelReal();
				voltageImagAllPhases = 0;
				break;
				
			case HighVoltageSide:
				// --- Calculate Voltage for Slack node on HV site ------------
				voltageRealAllPhases = this.getTransformerLowVoltageLevelReal() - dURealAllPhases;
				voltageImagAllPhases = - dUImagAllPhases;
				break;
			}
			
			// Calculate voltage violations
			double voltageBand = 0.1;
			double voltageAbsAllPhases = Math.sqrt(voltageRealAllPhases * voltageRealAllPhases + voltageImagAllPhases * voltageImagAllPhases);
			if (voltageAbsAllPhases <= (1-voltageBand)*this.getTransformerLowVoltageLevelReal() || voltageAbsAllPhases >= (1+voltageBand)*this.getTransformerLowVoltageLevelReal()) {
				voltageViolations = true;
			}
			
			// Calculate residual load
			residualLoadPAllPhases = this.getTransformerLowVoltageLevelReal() * lvTotaCurrentRealAllPhases;
			residualLoadQAllPhases = -this.getTransformerLowVoltageLevelReal() * lvTotaCurrentImagAllPhases;
			
			// Calculate transformator utilization
			double residualLoadAbs = this.getTransformerLowVoltageLevelReal() * Math.sqrt(Math.pow(lvTotaCurrentRealAllPhases,2) + Math.pow(lvTotaCurrentImagAllPhases, 2));
			trafoUtilization = residualLoadAbs / this.transformerDataModel.getRatedPower_sR();
			
			// Calculate transformator losses
			double ironLosses = this.getTransformerDataModel().getIronLosses_pFe() * 1000.0;
			trafoLossesPAllPhases = ironLosses + (dURealAllPhases * lvTotaCurrentRealAllPhases + dUImagAllPhases * lvTotaCurrentImagAllPhases);
			trafoLossesQAllPhases = -dURealAllPhases * lvTotaCurrentImagAllPhases + dUImagAllPhases * lvTotaCurrentRealAllPhases;
			
			// Calculate load in the upper voltage level
			uvLoadPAllPhases = residualLoadPAllPhases + trafoLossesPAllPhases;
			uvLoadQAllPhases = residualLoadQAllPhases + trafoLossesQAllPhases;
			
			// --- Fill the 'artificial' measurements -----------------------------
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageRealAllPhases, voltageRealAllPhases);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageImagAllPhases, voltageImagAllPhases);
			
		} else {
			// --- TriPhase calculation ---------------------------------------
			FixedDouble lvTotaCurrentRealL1 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealL1.name());
			FixedDouble lvTotaCurrentRealL2 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealL2.name());
			FixedDouble lvTotaCurrentRealL3 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentRealL3.name());
			
			FixedDouble lvTotaCurrentImagL1 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentImagL1.name());
			FixedDouble lvTotaCurrentImagL2 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentImagL2.name());
			FixedDouble lvTotaCurrentImagL3 = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.lvTotaCurrentImagL3.name());
			
			// --- If there are no current information, simply exit -----------
			boolean isMissingValues = lvTotaCurrentRealL1==null || lvTotaCurrentRealL2==null || lvTotaCurrentRealL3==null || lvTotaCurrentImagL1==null || lvTotaCurrentImagL2==null || lvTotaCurrentImagL3==null;
			if (isMissingValues==true) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Missing input values for further calculations: ThriPhase - electrical currents!");
				return;
			}
			
			// Calculate voltage drop on transformer
			double dURealL1 = this.resistance_R * lvTotaCurrentRealL1.getValue() - this.reactance_X * lvTotaCurrentImagL1.getValue();
			double dUImagL1 = this.reactance_X * lvTotaCurrentRealL1.getValue() + this.resistance_R * lvTotaCurrentImagL1.getValue();
			double dURealL2 = this.resistance_R * lvTotaCurrentRealL2.getValue() - this.reactance_X * lvTotaCurrentImagL2.getValue();
			double dUImagL2 = this.reactance_X * lvTotaCurrentRealL2.getValue() + this.resistance_R * lvTotaCurrentImagL2.getValue();
			double dURealL3 = this.resistance_R * lvTotaCurrentRealL3.getValue() - this.reactance_X * lvTotaCurrentImagL3.getValue();
			double dUImagL3 = this.reactance_X * lvTotaCurrentRealL3.getValue() + this.resistance_R * lvTotaCurrentImagL3.getValue();
			
			// --- Specify for Slack Voltage----------------------------------- 
			double voltageRealL1 = 0;
			double voltageImagL1 = 0;
			double voltageRealL2 = 0;
			double voltageImagL2 = 0;
			double voltageRealL3 = 0;
			double voltageImagL3 = 0;
			switch (this.getTransformerDataModel().getSlackNodeSide()) {
			case LowVoltageSide:
				// --- Calculate Voltage for Slack node on LV site ------------
				voltageRealL1 = this.getTransformerLowVoltageLevelReal();
				voltageImagL1 = 0;
				voltageRealL2 = this.getTransformerLowVoltageLevelReal();
				voltageImagL2 = 0;
				voltageRealL3 = this.getTransformerLowVoltageLevelReal();
				voltageImagL3 = 0;
				break;
				
			case HighVoltageSide:
				// --- Calculate Voltage for Slack node on HV site ------------
				voltageRealL1 = this.getTransformerLowVoltageLevelReal() - dURealL1;
				voltageImagL1 = - dUImagL1;
				voltageRealL2 = this.getTransformerLowVoltageLevelReal() - dURealL2;
				voltageImagL2 = - dUImagL2;
				voltageRealL3 = this.getTransformerLowVoltageLevelReal() - dURealL3;
				voltageImagL3 = - dUImagL3;
				break;
			}
			
			// Calculate voltage violations
			double voltageBand = 0.1;
			double voltageAbsL1 = Math.sqrt(voltageRealL1 * voltageRealL1 + voltageImagL1 * voltageImagL1);
			double voltageAbsL2 = Math.sqrt(voltageRealL2 * voltageRealL2 + voltageImagL2 * voltageImagL2);
			double voltageAbsL3 = Math.sqrt(voltageRealL3 * voltageRealL3 + voltageImagL3 * voltageImagL3);
			if (voltageAbsL1 <= (1-voltageBand)*this.getTransformerLowVoltageLevelReal() || voltageAbsL1 >= (1+voltageBand)*this.getTransformerLowVoltageLevelReal()) {
				voltageViolations = true;
			} else if (voltageAbsL2 <= (1-voltageBand)*this.getTransformerLowVoltageLevelReal() || voltageAbsL2 >= (1+voltageBand)*this.getTransformerLowVoltageLevelReal()) {
				voltageViolations = true;
			} else if (voltageAbsL3 <= (1-voltageBand)*this.getTransformerLowVoltageLevelReal() || voltageAbsL3 >= (1+voltageBand)*this.getTransformerLowVoltageLevelReal()) {
				voltageViolations = true;
			}
			
			// Calculate residual load
			double residualLoadPL1 = this.getTransformerLowVoltageLevelReal() * lvTotaCurrentRealL1.getValue();
			double residualLoadPL2 = this.getTransformerLowVoltageLevelReal() * lvTotaCurrentRealL2.getValue();
			double residualLoadPL3 = this.getTransformerLowVoltageLevelReal() * lvTotaCurrentRealL3.getValue();
			residualLoadPAllPhases = residualLoadPL1 + residualLoadPL2 + residualLoadPL3;
			double residualLoadQL1 = -this.getTransformerLowVoltageLevelReal() * lvTotaCurrentImagL1.getValue();
			double residualLoadQL2 = -this.getTransformerLowVoltageLevelReal() * lvTotaCurrentImagL2.getValue();
			double residualLoadQL3 = -this.getTransformerLowVoltageLevelReal() * lvTotaCurrentImagL3.getValue();
			residualLoadQAllPhases = residualLoadQL1 + residualLoadQL2 + residualLoadQL3;
			
			// Calculate transformator utilization
			double residualLoadAbs = Math.sqrt(Math.pow(residualLoadPAllPhases, 2) + Math.pow(residualLoadQAllPhases, 2));
			trafoUtilization = residualLoadAbs / this.transformerDataModel.getRatedPower_sR() / 1000000 * 100;
			
			// Calculate transformator losses
			double ironLossesSinglePhase = this.getTransformerDataModel().getIronLosses_pFe() * 1000.0 / 3.0;
			double trafoLossesPL1 = ironLossesSinglePhase + (dURealL1 * lvTotaCurrentRealL1.getValue() + dUImagL1 * lvTotaCurrentImagL1.getValue());
			double trafoLossesPL2 = ironLossesSinglePhase + (dURealL2 * lvTotaCurrentRealL2.getValue() + dUImagL2 * lvTotaCurrentImagL2.getValue());
			double trafoLossesPL3 = ironLossesSinglePhase + (dURealL3 * lvTotaCurrentRealL3.getValue() + dUImagL3 * lvTotaCurrentImagL3.getValue());
			trafoLossesPAllPhases = trafoLossesPL1 + trafoLossesPL2 + trafoLossesPL3;
			
			double trafoLossesQL1 = -dURealL1 * lvTotaCurrentImagL1.getValue() + dUImagL1 * lvTotaCurrentRealL1.getValue();
			double trafoLossesQL2 = -dURealL2 * lvTotaCurrentImagL2.getValue() + dUImagL2 * lvTotaCurrentRealL2.getValue();
			double trafoLossesQL3 = -dURealL3 * lvTotaCurrentImagL3.getValue() + dUImagL3 * lvTotaCurrentRealL3.getValue();
			trafoLossesQAllPhases = trafoLossesQL1 + trafoLossesQL2 + trafoLossesQL3;
			
			// Calculate load in the upper voltage level
			double uvLoadPL1 = residualLoadPL1 + trafoLossesPL1;
			double uvLoadPL2 = residualLoadPL2 + trafoLossesPL2;
			double uvLoadPL3 = residualLoadPL3 + trafoLossesPL3;
			uvLoadPAllPhases = uvLoadPL1 + uvLoadPL2 + uvLoadPL3;
			double uvLoadQL1 = residualLoadQL1 + trafoLossesQL1;
			double uvLoadQL2 = residualLoadQL2 + trafoLossesQL2;
			double uvLoadQL3 = residualLoadQL3 + trafoLossesQL3;
			uvLoadQAllPhases = uvLoadQL1 + uvLoadQL2 + uvLoadQL3;
			
			// --- Fill the 'artificial' measurements -----------------------------
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageRealL1, voltageRealL1);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageRealL2, voltageRealL2);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageRealL3, voltageRealL3);
			
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageImagL1, voltageImagL1);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageImagL2, voltageImagL2);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.lvVoltageImagL3, voltageImagL3);
			
			
			this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesPL1, trafoLossesPL1);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesPL2, trafoLossesPL2);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesPL3, trafoLossesPL3);
			
			this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesQL1, trafoLossesQL1);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesQL2, trafoLossesQL2);
			this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesQL3, trafoLossesQL3);


			if (this.getLvPowerL1()!=null) {
				this.getLvPowerL1().setActivePower(residualLoadPL1);
				this.getLvPowerL1().setReactivePower(residualLoadQL1);
				this.getLvPowerL2().setActivePower(residualLoadPL2);
				this.getLvPowerL2().setReactivePower(residualLoadQL2);
				this.getLvPowerL3().setActivePower(residualLoadPL3);
				this.getLvPowerL3().setReactivePower(residualLoadQL3);
			}
			
			if (this.getUvPowerL1()!=null) {
				this.getUvPowerL1().setActivePower(uvLoadPL1);
				this.getUvPowerL1().setReactivePower(uvLoadQL1);
				this.getUvPowerL2().setActivePower(uvLoadPL2);
				this.getUvPowerL2().setReactivePower(uvLoadQL2);
				this.getUvPowerL3().setActivePower(uvLoadPL3);
				this.getUvPowerL3().setReactivePower(uvLoadQL3);
			}
		}
		
		// --- Fill the 'artificial' measurements with general values -----------------------------
		this.setIoListBooleanValue(tss, TransformerSystemVariable.voltageViolation, voltageViolations);
		
		this.setIoListDoubleValue(tss, TransformerSystemVariable.tUtil, trafoUtilization);
		
		this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesPAllPhases, trafoLossesPAllPhases);
		this.setIoListDoubleValue(tss, TransformerSystemVariable.tLossesQAllPhases, trafoLossesQAllPhases);

		// --- Active and reactive power (Transformer is slack, power not important for slack) ----
		this.getLvPowerAllPhases().setActivePower(residualLoadPAllPhases);
		this.getLvPowerAllPhases().setReactivePower(residualLoadQAllPhases);
		
		this.getUvPowerAllPhases().setActivePower(uvLoadPAllPhases);
		this.getUvPowerAllPhases().setReactivePower(uvLoadQAllPhases);

		// --- Print calculation results ----------------------------------------------------------
		this.debugPrintCalculationResults(tss);
	}
	
	// ------------------------------------------------------------------------
	// --- From here, methods to print calculation results --------------------
	// ------------------------------------------------------------------------	
	/**
	 * Debug print calculation results.
	 */
	private void debugPrintCalculationResults(TechnicalSystemState tss) {

		if (this.isPrintCalculationResults==false) return;
		
		// --- Get variables / values to pint ---------------------------------
		TransformerPower tpHvAllPhase = this.getUvPowerAllPhases();
		TransformerPower tpLvAllPhase = this.getLvPowerAllPhases();

		FixedInteger fiTapPos = (FixedInteger) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.tapPos.name());
		int tapPos = fiTapPos.getValue();
		
		FixedDouble fdTrafoLosse = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.tLossesPAllPhases.name());
		double trafoLossesAllPhases = fdTrafoLosse.getValue();
		
		double checkSum = tpLvAllPhase.getActivePower() + trafoLossesAllPhases;  
		double checkSumDiff = tpHvAllPhase.getActivePower() - checkSum;
		
		// --- Prepare console output -----------------------------------------
		int roundPrecision = 2;
		
		StringBuilder sb = new StringBuilder();
		sb.append("tapPos: " + tapPos + "\t");
		sb.append("P-LV: " + NumberHelper.round(tpLvAllPhase.getActivePower() / 1000.0, roundPrecision) + " kW\t");
		sb.append("P-Losses: " + NumberHelper.round(trafoLossesAllPhases / 1000.0, roundPrecision) + " kW\t");
		sb.append("P-HV: " + NumberHelper.round(tpHvAllPhase.getActivePower() / 1000.0, roundPrecision) + " kW\t");
		sb.append("HV-Diff: " + NumberHelper.round(checkSumDiff / 1000.0, roundPrecision) + " kW\t");
		System.out.println(sb.toString());
	}
	
	/**
	 * Sets the io list double value.
	 *
	 * @param tss the TechnicalSystemState
	 * @param tsv the TransformerSystemVariable
	 * @param newValue the new value
	 */
	private void setIoListDoubleValue(TechnicalSystemState tss, TransformerSystemVariable tsv, double newValue) {
		FixedDouble fd = (FixedDouble) this.getOptionModelController().getVariableState(tss.getIOlist(), tsv.name());
		if (fd!=null) {
			fd.setValue(newValue);
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] Could not found transformer variable '" + tsv.name() + "' in state IO-list.");
		}
	}
	/**
	 * Sets the io list boolean value.
	 *
	 * @param tss the TechnicalSystemState
	 * @param tsv the TransformerSystemVariable
	 * @param newValue the new value
	 */
	private void setIoListBooleanValue(TechnicalSystemState tss, TransformerSystemVariable tsv, boolean newValue) {
		FixedBoolean fd = (FixedBoolean) this.getOptionModelController().getVariableState(tss.getIOlist(), tsv.name());
		if (fd!=null) {
			fd.setValue(newValue);
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] Could not found transformer variable '" + tsv.name() + "' in state IO-list.");
		}
	}
	
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#updateInputMeasurement(energy.optionModel.TechnicalSystemState, energy.optionModel.FixedVariable)
	 */
	@Override
	public void updateInputMeasurement(TechnicalSystemState tss, FixedVariable variableToUpdate) {
		
		FixedVariableList measurements = this.getEnergyAgentConnector().getMeasurementsFromSystem();
		if (measurements==null) return;
		
		if (variableToUpdate instanceof FixedDouble) {
			FixedDouble fvDoubleSource = (FixedDouble) measurements.getVariable(variableToUpdate.getVariableID());
			FixedDouble	fvDoubleDestin = (FixedDouble) variableToUpdate;
			fvDoubleDestin.setValue(fvDoubleSource.getValue());
		} else if (variableToUpdate instanceof FixedBoolean) {
			FixedBoolean fvBoolSource = (FixedBoolean) measurements.getVariable(variableToUpdate.getVariableID());
			FixedBoolean fvBoolDestin = (FixedBoolean) variableToUpdate;
			fvBoolDestin.setValue(fvBoolSource.isValue());
		}
	}


	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getInputMeasurement(energy.optionModel.SystemVariableDefinition)
	 */
	@Override
	public InputMeasurement getInputMeasurement(SystemVariableDefinition sysVarDef) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getCostFunctionDataSeries(java.lang.String, energy.optionModel.Connectivity)
	 */
	@Override
	public CostFunctionDataSeries getCostFunctionDataSeries(String domain, Connectivity connectivity) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getAmountCosts(energy.optionModel.TechnicalInterface, energy.optionModel.Connectivity, long, long, double, double)
	 */
	@Override
	public double getAmountCosts(TechnicalInterface ti, Connectivity connectivity, long time1, long time2, double flowValue1, double flowValue2) {
		return 0;
	}
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getStateCosts(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public double getStateCosts(TechnicalSystemStateEvaluation tsse) {
		return 0;
	}

	
	// ------------------------------------------------------------------------
	// --- Access method for the low voltage level of the transformer ---------
	// ------------------------------------------------------------------------	
	/**
	 * Returns the low voltage level real for the transformer.
	 * @return the transformer low voltage level real
	 */
	public double getTransformerLowVoltageLevelReal() {
		return transformerLowVoltageLevelReal;
	}
	/**
	 * Sets the low voltage level real for the transformer.
	 * @param transformerLowVoltageLevelReal the new transformer low voltage level real
	 */
	public void setTransformerLowVoltageLevelReal(double transformerLowVoltageLevelReal) {
		this.transformerLowVoltageLevelReal = transformerLowVoltageLevelReal;
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, methods to access power values --------------------------
	// ------------------------------------------------------------------------	
	public TransformerPower getLvPowerAllPhases() {
		if (lvPowerAllPhases==null) {
			lvPowerAllPhases = new TransformerPower();
		}
		return lvPowerAllPhases;
	}
	public void setLvPowerAllPhases(TransformerPower lvPowerAllPhases) {
		this.lvPowerAllPhases = lvPowerAllPhases;
	}

	public TransformerPower getLvPowerL1() {
		if (lvPowerL1==null && this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			lvPowerL1 = new TransformerPower();
		}
		return lvPowerL1;
	}
	public void setLvPowerL1(TransformerPower lvPowerL1) {
		this.lvPowerL1 = lvPowerL1;
	}

	public TransformerPower getLvPowerL2() {
		if (lvPowerL2==null && this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			lvPowerL2 = new TransformerPower();
		}
		return lvPowerL2;
	}
	public void setLvPowerL2(TransformerPower lvPowerL2) {
		this.lvPowerL2 = lvPowerL2;
	}

	public TransformerPower getLvPowerL3() {
		if (lvPowerL3==null && this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			lvPowerL3 = new TransformerPower();
		}
		return lvPowerL3;
	}
	public void setLvPowerL3(TransformerPower lvPowerL3) {
		this.lvPowerL3 = lvPowerL3;
	}

	
	public TransformerPower getUvPowerAllPhases() {
		if (uvPowerAllPhases==null) {
			uvPowerAllPhases = new TransformerPower();
		}
		return uvPowerAllPhases;
	}
	public void setUvPowerAllPhases(TransformerPower uvPowerAllPhases) {
		this.uvPowerAllPhases = uvPowerAllPhases;
	}

	public TransformerPower getUvPowerL1() {
		if (uvPowerL1==null && this.getTransformerDataModel().isUpperVoltage_ThriPhase()==true) {
			uvPowerL1 = new TransformerPower();
		}
		return uvPowerL1;
	}
	public void setUvPowerL1(TransformerPower uvPowerL1) {
		this.uvPowerL1 = uvPowerL1;
	}

	public TransformerPower getUvPowerL2() {
		if (uvPowerL2==null && this.getTransformerDataModel().isUpperVoltage_ThriPhase()==true) {
			uvPowerL2 = new TransformerPower();
		}
		return uvPowerL2;
	}
	public void setUvPowerL2(TransformerPower uvPowerL2) {
		this.uvPowerL2 = uvPowerL2;
	}

	public TransformerPower getUvPowerL3() {
		if (uvPowerL3==null && this.getTransformerDataModel().isUpperVoltage_ThriPhase()==true) {
			uvPowerL3 = new TransformerPower();
		}
		return uvPowerL3;
	}
	public void setUvPowerL3(TransformerPower uvPowerL3) {
		this.uvPowerL3 = uvPowerL3;
	}
	
}
