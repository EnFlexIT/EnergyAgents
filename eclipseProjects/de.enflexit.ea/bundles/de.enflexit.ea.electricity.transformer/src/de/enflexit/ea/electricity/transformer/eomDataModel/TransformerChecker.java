package de.enflexit.ea.electricity.transformer.eomDataModel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.electricity.transformer.TransformerPowerEvaluationCalculation;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TransformerSystemVariable;
import energy.optionModel.AbstractInputMeasurement;
import energy.optionModel.Duration;
import energy.optionModel.FixedInteger;
import energy.optionModel.InputMeasurement;
import energy.optionModel.InputMeasurementCalculatedByState;
import energy.optionModel.RangeInteger;
import energy.optionModel.State;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionDouble;
import energy.optionModel.SystemVariableDefinitionInteger;
import energy.optionModel.SystemVariableDefinitionOntology;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateTime;
import energy.optionModel.TimeUnit;
import energy.optionModel.VariableState;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import energy.validation.AbstractTechnicalSystemChecker;

/**
 * The Class TransformChecker.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class TransformerChecker extends AbstractTechnicalSystemChecker {

	private TransformerDataModel transformerDataModel;
	private List<TransformerSystemVariable> transformerMeasurementList;
	
	/**
	 * Gets the transformer data model.
	 * @return the transformer data model
	 */
	private TransformerDataModel getTransformerDataModel() {
		return transformerDataModel;
	}
	/**
	 * Sets the transformer data model.
	 * @param transformerDataModel the new transformer data model
	 */
	private void setTransformerDataModel(TransformerDataModel transformerDataModel) {
		this.transformerDataModel = transformerDataModel;
	}
	
	/**
	 * Return the list of system variables.
	 * @return the system variables
	 */
	private List<SystemVariableDefinition> getSystemVariables() {
		return this.getOptionModelController().getTechnicalSystem().getSystemVariables();
	}
	/**
	 * Returns the list of abstract input measurements.
	 * @return the system variables
	 */
	private List<AbstractInputMeasurement> getInputMeasurements() {
		return this.getOptionModelController().getInputMeasurements();
	}

	
	/* (non-Javadoc)
	 * @see energy.validation.AbstractTechnicalSystemChecker#afterStaticModelUpdate(energy.optionModel.SystemVariableDefinitionStaticModel, energy.optionModel.gui.sysVariables.AbstractStaticModel)
	 */
	@Override
	public void afterStaticModelUpdate(SystemVariableDefinitionStaticModel sysVarStatic, AbstractStaticModel staticModel) {
	
		if (! (staticModel instanceof TransformerStaticDataModel)) return;

		// --- Get the Transformer data model first ---------------------------
		TransformerStaticDataModel tsdm = (TransformerStaticDataModel) staticModel;
		TransformerDataModel tdm = (TransformerDataModel) tsdm.getStaticDataModel();
		this.setTransformerDataModel(tdm);
		
		// --- Checks according to the settings in the static data model ------
		this.setIOTapPosition();
		this.setIOMeasurements();
		
	}

	/**
	 * Returns the list of required measurements for the transformer that are to be placed in the IO-List of the current model.
	 * @return the transformer measurement list
	 */
	private List<TransformerSystemVariable> getTransformerMeasurementList() {
		if (transformerMeasurementList==null) {
			transformerMeasurementList = new ArrayList<TransformerDataModel.TransformerSystemVariable>();
			
			transformerMeasurementList.add(TransformerSystemVariable.hvVoltageRealAllPhases);
			transformerMeasurementList.add(TransformerSystemVariable.hvVoltageImagAllPhases);
			
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentRealAllPhases);
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentImagAllPhases);
			
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentRealL1);
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentRealL2);
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentRealL3);
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentImagL1);
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentImagL2);
			transformerMeasurementList.add(TransformerSystemVariable.lvTotaCurrentImagL3);
			
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageRealAllPhases);
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageImagAllPhases);
			
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageRealL1);
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageRealL2);
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageRealL3);
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageImagL1);
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageImagL2);
			transformerMeasurementList.add(TransformerSystemVariable.lvVoltageImagL3);
			
			transformerMeasurementList.add(TransformerSystemVariable.voltageViolation);
			transformerMeasurementList.add(TransformerSystemVariable.tUtil);
			
			transformerMeasurementList.add(TransformerSystemVariable.tLossesPAllPhases);
			transformerMeasurementList.add(TransformerSystemVariable.tLossesQAllPhases);
			
			transformerMeasurementList.add(TransformerSystemVariable.tLossesPL1);
			transformerMeasurementList.add(TransformerSystemVariable.tLossesQL1);
			transformerMeasurementList.add(TransformerSystemVariable.tLossesPL2);
			transformerMeasurementList.add(TransformerSystemVariable.tLossesQL2);
			transformerMeasurementList.add(TransformerSystemVariable.tLossesPL3);
			transformerMeasurementList.add(TransformerSystemVariable.tLossesQL3);
			
			transformerMeasurementList.add(TransformerSystemVariable.cnVoltageReal);
			transformerMeasurementList.add(TransformerSystemVariable.cnVoltageImag);
			
		}
		return transformerMeasurementList;
	}
	
	/**
	 * Sets the IO measurements.
	 */
	private void setIOMeasurements() {
		
		for (int i = 0; i < this.getTransformerMeasurementList().size(); i++) {
			
			TransformerSystemVariable tsv = this.getTransformerMeasurementList().get(i);
			SystemVariableDefinition svd = this.getOptionModelController().getSystemVariableDefinition(tsv.name());
			AbstractInputMeasurement aim = this.getOptionModelController().getInputMeasurement(tsv.name());
			
			// --- Decide if this is a required variable ------------
			boolean requiredVariable = true;
			boolean requiresCalculatedInputMeasurement = false;
			boolean keepCurrentInputMeasurement = false;
			
			switch (tsv) {
			// ----------------------------------
			// --- High Voltage variables -------  
			case hvVoltageRealAllPhases:
			case hvVoltageImagAllPhases:
				if (aim instanceof InputMeasurement) {
					keepCurrentInputMeasurement = true;
				}
				break;
			// ----------------------------------
			// --- Current and cosinus phi ------  
			case lvTotaCurrentRealAllPhases:
			case lvTotaCurrentImagAllPhases:
				if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
					requiredVariable = false;
				}
				if (requiredVariable==true) requiresCalculatedInputMeasurement = true;
				break;

			case lvTotaCurrentRealL1:
			case lvTotaCurrentRealL2:
			case lvTotaCurrentRealL3:
			case lvTotaCurrentImagL1:
			case lvTotaCurrentImagL2:
			case lvTotaCurrentImagL3:
				if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==false) {
					requiredVariable = false;
				}
				if (requiredVariable==true) requiresCalculatedInputMeasurement = true;
				break;
				
			// ----------------------------------
			// --- lv voltage real and imag -----
			case lvVoltageRealAllPhases:
			case lvVoltageImagAllPhases:
				if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
					requiredVariable = false;
				}
				if (requiredVariable==true) requiresCalculatedInputMeasurement = true;
				break;
				
			case lvVoltageRealL1:
			case lvVoltageRealL2:
			case lvVoltageRealL3:
			case lvVoltageImagL1:
			case lvVoltageImagL2:
			case lvVoltageImagL3:
				if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==false) {
					requiredVariable = false;
				}
				if (requiredVariable==true) requiresCalculatedInputMeasurement = true;
				break;
			
			case tLossesPL1:
			case tLossesQL1:
			case tLossesPL2:
			case tLossesQL2:
			case tLossesPL3:
			case tLossesQL3:
				if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==false) {
					requiredVariable = false;
				}
				if (requiredVariable==true) requiresCalculatedInputMeasurement = true;
				break;
			
			case cnVoltageReal:
			case cnVoltageImag:
				if (this.getTransformerDataModel().isControlBasedOnNodeVoltage()==false) {
					requiredVariable = false;
				}
				if (requiredVariable==true) requiresCalculatedInputMeasurement = true;
				break;
				
			default:
				break;
			}
			
			// ------------------------------------------------------
			// --- Create or remove SysVarDef -----------------------
			if (requiredVariable==true) {
				if (svd==null) {
					// --- Create and add variable ------------------
					svd = this.createSystemVariableDefinition(tsv);
					this.getSystemVariables().add(svd);
				}
			} else {
				if (svd!=null) {
					// --- Remove variable --------------------------
					this.getSystemVariables().remove(svd);
				}
			}
			
			// ------------------------------------------------------
			// --- Create or remove input measurement ---------------
			if (requiresCalculatedInputMeasurement==true) {
				// --- Check if of right type -----------------------
				if (aim!=null && !(aim instanceof InputMeasurementCalculatedByState)) {
					this.getInputMeasurements().remove(aim);
					aim = null;
				}
				// --- Create input measurement by state ------------
				if (aim==null) {
					InputMeasurementCalculatedByState imByState = new InputMeasurementCalculatedByState();
					imByState.setVariableID(tsv.name());
					imByState.setCalculationClass(TransformerPowerEvaluationCalculation.class.getName());
					aim = imByState;
					this.getInputMeasurements().add(aim);
				}
				
			} else {
				if (aim!=null && keepCurrentInputMeasurement==false) {
					this.getInputMeasurements().remove(aim);
				}
			}
			
		}
	}
	
	/**
	 * Sets the tap position variable to the IO-List. 
	 */
	private void setIOTapPosition() {
		
		SystemVariableDefinition svd = this.getOptionModelController().getSystemVariableDefinition(TransformerSystemVariable.tapPos.name());
		if (this.getTransformerDataModel().isTapable()==true) {
			// ----------------------------------------------------------------
			// --- Ensure that a set point variable is available --------------
			SystemVariableDefinitionInteger svdTapPos = null;
			if (svd==null) {
				svdTapPos = (SystemVariableDefinitionInteger) this.createSystemVariableDefinition(TransformerSystemVariable.tapPos);
				svdTapPos.setSetPoint(true);
				svdTapPos.setStateIdentifier(true);
				this.getSystemVariables().add(svdTapPos);
				
			} else {
				svdTapPos = (SystemVariableDefinitionInteger) svd;
			}
			
			// --- Configure according to tap settings ------------------------
			svdTapPos.setEvaluationStepSize(1);			
			svdTapPos.setValueMin(this.getTransformerDataModel().getTapMinimum());
			svdTapPos.setValueMax(this.getTransformerDataModel().getTapMaximum());
			
			Duration gradDuration = new Duration();
			gradDuration.setValue(30);
			gradDuration.setUnit(TimeUnit.SECOND_S);
					
			svdTapPos.setGradientTime(gradDuration);
			svdTapPos.setGradientValue(1);
			
			// ----------------------------------------------------------------
			// --- Check IO-Ranges of States ----------------------------------
			List<TechnicalInterfaceConfiguration> tics = this.getOptionModelController().getTechnicalSystem().getInterfaceConfigurations();
			for (int i = 0; i < tics.size(); i++) {
				TechnicalInterfaceConfiguration tic = tics.get(i);
				List<State> states = tic.getSystemStates();
				for (int j = 0; j < states.size(); j++) {
					State state = states.get(j);
					List<VariableState> setPointRanges = state.getStateEvaluationRanges();
					// --- Check if variable is already their -----------------
					VariableState varState = this.getOptionModelController().getVariableState(setPointRanges, TransformerSystemVariable.tapPos.name());
					if (varState==null) {
						// --- Add set point variable -------------------------
						RangeInteger tapSetPointRange = new RangeInteger();
						tapSetPointRange.setVariableID(TransformerSystemVariable.tapPos.name());
						tapSetPointRange.setValueMin(svdTapPos.getValueMin());
						tapSetPointRange.setValueMax(svdTapPos.getValueMax());
						setPointRanges.add(tapSetPointRange);
						
					} else {
						// --- Edit set point variable ------------------------
						RangeInteger tapSetPointRange = (RangeInteger) varState;
						tapSetPointRange.setValueMin(svdTapPos.getValueMin());
						tapSetPointRange.setValueMax(svdTapPos.getValueMax());
					}
				}
			}
			
			// ----------------------------------------------------------------
			// --- Check IO variable in evaluation settings -------------------
			List<TechnicalSystemStateTime> tsstList = this.getOptionModelController().getEvaluationStateList();
			for (int i = 0; i < tsstList.size(); i++) {
				TechnicalSystemStateTime tsst = tsstList.get(i);
				if (tsst instanceof TechnicalSystemState) {
					TechnicalSystemState tss = (TechnicalSystemState) tsst;
					VariableState var = this.getOptionModelController().getVariableState(tss.getIOlist(), TransformerSystemVariable.tapPos.name());
					if (var==null) {
						// --- Add system variable ----------------------------
						FixedInteger tapVar = new FixedInteger();
						tapVar.setVariableID(TransformerSystemVariable.tapPos.name());
						tapVar.setValue(0);
						tss.getIOlist().add(tapVar);
					} else {
						// --- Edit system variable ---------------------------
						FixedInteger tapVar = (FixedInteger) var;
						tapVar.setValue(0);
					}
				}
			}
			
		} else {
			// --- Remove set point system variable, if available -------------
			this.getOptionModelController().removeSystemVariableDefinition(this.getSystemVariables(), TransformerSystemVariable.tapPos.name());

			// --- Remove set point ranges in states --------------------------
			List<TechnicalInterfaceConfiguration> tics = this.getOptionModelController().getTechnicalSystem().getInterfaceConfigurations();
			for (int i = 0; i < tics.size(); i++) {
				TechnicalInterfaceConfiguration tic = tics.get(i);
				List<State> states = tic.getSystemStates();
				for (int j = 0; j < states.size(); j++) {
					State state = states.get(j);
					List<VariableState> setPointRanges = state.getStateEvaluationRanges();
					// --- Check if variable is already their -----------------
					this.getOptionModelController().removeVariableState(setPointRanges, TransformerSystemVariable.tapPos.name());
				}
			}
			
			// --- Remove from IO-list in evaluation settings -----------------
			List<TechnicalSystemStateTime> tsstList = this.getOptionModelController().getEvaluationStateList();
			for (int i = 0; i < tsstList.size(); i++) {
				TechnicalSystemStateTime tsst = tsstList.get(i);
				if (tsst instanceof TechnicalSystemState) {
					TechnicalSystemState tss = (TechnicalSystemState) tsst;
					this.getOptionModelController().removeVariableState(tss.getIOlist(), TransformerSystemVariable.tapPos.name());
				}
			}
			
		}
	}
	
	
	/**
	 * Creates the system variable definition according to the specified TransformerSystemVariable.
	 *
	 * @param tsv the TransformerSystemVariable
	 * @return the system variable definition
	 */
	@SuppressWarnings("unchecked")
	private SystemVariableDefinition createSystemVariableDefinition(TransformerSystemVariable tsv) {
		
		SystemVariableDefinition svd = null;
		
		Class<SystemVariableDefinition> svdClass;
		try {
			// --- Create instance --------------
			svdClass = (Class<SystemVariableDefinition>) Class.forName(tsv.getSystemVarialbleClassName());
			svd = svdClass.getDeclaredConstructor().newInstance();
			svd.setVariableID(tsv.name());
			svd.setVariableDescription(tsv.getDescription());
			
			// --- Set Unit? --------------------
			if (svd instanceof SystemVariableDefinitionInteger) {
				SystemVariableDefinitionInteger svdInteger = (SystemVariableDefinitionInteger) svd;
				svdInteger.setUnit(tsv.getUnit());
			} else if (svd instanceof SystemVariableDefinitionDouble) {
				SystemVariableDefinitionDouble svdDouble = (SystemVariableDefinitionDouble) svd;
				svdDouble.setUnit(tsv.getUnit());
			}
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
		return svd;
	}
	
	
	/* (non-Javadoc)
	 * @see energy.validation.AbstractTechnicalSystemChecker#afterOntologyConceptUpdate(energy.optionModel.SystemVariableDefinitionOntology, java.lang.Object)
	 */
	@Override
	public void afterOntologyConceptUpdate(SystemVariableDefinitionOntology sysVarOnto, Object concept) {
		// --- Nothing to do here ---
	}
	/* (non-Javadoc)
	 * @see energy.validation.AbstractTechnicalSystemChecker#checkTechnicalSystem(energy.optionModel.TechnicalSystem)
	 */
	@Override
	public void checkTechnicalSystem(TechnicalSystem ts) {
		// --- Nothing to do here so far ---
	}

}
