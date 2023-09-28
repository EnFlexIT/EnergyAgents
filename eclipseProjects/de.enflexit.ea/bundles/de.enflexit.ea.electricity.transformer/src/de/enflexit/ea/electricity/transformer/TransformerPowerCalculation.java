package de.enflexit.ea.electricity.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerChecker;
import energy.DomainInterfaceFlows;
import energy.OptionModelController;
import energy.calculations.AbstractOptionModelCalculation;
import energy.domain.DefaultDomainModelElectricity;
import energy.helper.UnitConverter;
import energy.optionModel.AbstractInterfaceFlow;
import energy.optionModel.Duration;
import energy.optionModel.EnergyAmount;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyInterface;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.validation.AbstractTechnicalSystemChecker;

/**
 * The Class TransformerPowerCalculation.
 */
public class TransformerPowerCalculation extends AbstractOptionModelCalculation {

	private static final String EVALUATION_CALCULATION_CLASS_NAME = TransformerPowerEvaluationCalculation.class.getName();
	
	private TransformerChecker transformerChecker;
	private TransformerPowerEvaluationCalculation evaluationCalculation = null;
	
	private TechnicalSystemStateEvaluation currentTsse = null;

	private Double highVoltageLevel;
	private Double lowVoltageLevel;
	
	
	/**
	 * Instantiates a new battery power calculation.
	 * @param optionModelController the option model controller
	 */
	public TransformerPowerCalculation(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	// ------------------------------------------------------------------------
	// --- For the validation of the current model ----------------------------
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getCustomTechnicalSystemChecker()
	 */
	@Override
	public AbstractTechnicalSystemChecker getCustomTechnicalSystemChecker() {
		if (transformerChecker==null) {
			transformerChecker = new TransformerChecker();
		}
		return transformerChecker;
	}
	
	// ------------------------------------------------------------------------
	// --- Access to the TransformerPowerEvaluationCalculation ----------------
	// ------------------------------------------------------------------------
	/**
	 * Gets the evaluation calculation.
	 * @return the evaluation calculation
	 */
	public TransformerPowerEvaluationCalculation getEvaluationCalculation() {
		if (evaluationCalculation == null) {
			evaluationCalculation = (TransformerPowerEvaluationCalculation) this.optionModelController.getEvaluationCalculation(EVALUATION_CALCULATION_CLASS_NAME);
		}
		return evaluationCalculation;
	}

	// ------------------------------------------------------------------------
	// --- Methods to determine high and low voltage level --------------------
	// ------------------------------------------------------------------------
	/**
	 * Checks if is error in voltage level definition.
	 * @return true, if is error in voltage level definition
	 */
	private boolean isErrorInVoltageLevelDefinition() {

		boolean isError = false;
		List<Double> voltageLevelList = this.getTransformerVoltageLevelsDefined();
		if (voltageLevelList==null) {
			isError = true;
			System.err.println("[" + this.getClass().getSimpleName() + "] No voltage levels could be found for the current interface configuration.");
		} else if (voltageLevelList.size()<2 || voltageLevelList.size()>2) {
			isError = true;
			System.err.println("[" + this.getClass().getSimpleName() + "] " + voltageLevelList.size() + " voltage levels are specified with the interfaces of the system.");
		} else if (voltageLevelList.size()==2) {
			isError = false;
			this.lowVoltageLevel  = voltageLevelList.get(0);
			this.highVoltageLevel = voltageLevelList.get(1);
		}
		return isError;
	}
	/**
	 * Returns the defined transformer voltage levels in a sorted manner (ascending).
	 * @return the transformer voltage levels
	 */
	private List<Double> getTransformerVoltageLevelsDefined() {
			
		if (this.currentTsse==null || this.currentTsse.getConfigID()==null || this.currentTsse.getConfigID().isEmpty()==true) return null;
		
		// --- Get list of interfaces -------------------------------
		String configID = this.currentTsse.getConfigID();
		TechnicalInterfaceConfiguration tic = this.optionModelController.getTechnicalInterfaceConfiguration(configID);
		Vector<TechnicalInterface> tiVector = this.optionModelController.getTechnicalInterfacesByDomain(tic, EnergyCarrier.ELECTRICITY.value());
		
		// --- Get all defined voltage levels -----------------------
		HashSet<Double> voltageLevelHash = new HashSet<Double>(); 
		if (tiVector!=null) {
			for (int i = 0; i < tiVector.size(); i++) {
				TechnicalInterface ti = tiVector.get(i);
				if (ti.getDomainModel() instanceof DefaultDomainModelElectricity) {
					voltageLevelHash.add(((DefaultDomainModelElectricity) ti.getDomainModel()).getRatedVoltage());
				} else {
					System.err.println("[" + this.getClass().getSimpleName() + "] Error in interface definition '" + ti.getInterfaceID() + "': No or wrong domain model definition!");
				}
			}
		}

		// --- Return voltage levels sorted  ------------------------ 
		List<Double> transformerVoltageLevels = new ArrayList<Double>(voltageLevelHash);
		Collections.sort(transformerVoltageLevels);
		return transformerVoltageLevels;
	}
	
	/**
	 * Returns the low voltage level of the current transformer model.
	 * @return the low voltage level
	 */
	public Double getLowVoltageLevel() {
		if (lowVoltageLevel==null) {
			// --- Call the check that will set the voltage levels ------------
			this.isErrorInVoltageLevelDefinition();
		}
		return lowVoltageLevel;
	}
	/**
	 * Returns the high voltage level of the transformer level.
	 * @return the high voltage level
	 */
	public Double getHighVoltageLevel() {
		if (highVoltageLevel==null) {
			// --- Call the check that will set the voltage levels ------------
			this.isErrorInVoltageLevelDefinition();
		}
		return highVoltageLevel;
	}
	/**
	 * Checks if the current domain configuration specifies a high voltage interface.
	 * @param domainConfig the domain configuration
	 * @return true, if is high voltage
	 */
	private boolean isHighVoltage(DefaultDomainModelElectricity domainConfig) {
		if (domainConfig==null || this.getHighVoltageLevel()==null) return false;
		return domainConfig.getRatedVoltage()==this.getHighVoltageLevel();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see energy.calculations.AbstractOptionModelCalculation#getEnergyOrGoodFlow(energy.optionModel.TechnicalSystemStateEvaluation,
	 * energy.optionModel.TechnicalInterface, boolean)
	 */
	@Override
	public AbstractInterfaceFlow getEnergyOrGoodFlow(TechnicalSystemStateEvaluation tsse, TechnicalInterface ti, boolean isManualConfiguration) {
		
		// --- Execute the calculation ----------------------------------------
		if (tsse!=this.currentTsse) {
			this.getEvaluationCalculation().calculate(tsse);
			this.currentTsse = tsse;
		}

		// --- Get the requested energy flow ----------------------------------
		EnergyFlowInWatt energyFlow = new EnergyFlowInWatt();
		energyFlow.setSIPrefix(this.optionModelController.getCostModelsSIPrefix());
		
		EnergyInterface ei = (EnergyInterface) ti;
		if (ei.getEnergyCarrier()==EnergyCarrier.ELECTRICITY) {
			// --- Is high or low voltage ? -----------------------------------
			DefaultDomainModelElectricity domainConfig = (DefaultDomainModelElectricity) ei.getDomainModel();
			if (this.isHighVoltage(domainConfig)==true) {
				// --- High voltage interface --------------------------------- 
				switch (domainConfig.getPhase()) {
				case AllPhases:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getUvPowerAllPhases(), domainConfig);
					break;
				case L1:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getUvPowerL1(), domainConfig);
					break;
				case L2:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getUvPowerL2(), domainConfig);
					break;
				case L3:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getUvPowerL3(), domainConfig);
					break;
				}
				
			} else {
				// --- Low voltage interfaces ---------------------------------
				switch (domainConfig.getPhase()) {
				case AllPhases:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getLvPowerAllPhases(), domainConfig);
					break;
				case L1:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getLvPowerL1(), domainConfig);
					break;
				case L2:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getLvPowerL2(), domainConfig);
					break;
				case L3:
					this.updateEnergyFlow(energyFlow, this.getEvaluationCalculation().getLvPowerL3(), domainConfig);
					break;
				}
			}
		}
		return energyFlow;
	}
	/**
	 * Updates the specified energy flow with the energyFlow and according to the domain configuration.
	 *
	 * @param energyFlow the energy flow
	 * @param tp the TransformerPower
	 * @param domainConfig the domain configuration
	 */
	private void updateEnergyFlow(EnergyFlowInWatt energyFlow, TransformerPower tp, DefaultDomainModelElectricity domainConfig) {
		
		double powerValue = energyFlow.getValue();
		if (tp!=null) {
			switch (domainConfig.getPowerType()) {
			case ActivePower:
				powerValue = tp.getActivePower();
				break;
			case ApparentPower:
				powerValue = tp.getApparentPower();
				break;
			case ReactivePower:
				powerValue = tp.getReactivePower();
				break;
			}
		}
		
		// --- Convert to right SI Prefix -------------- 
		powerValue = UnitConverter.convertEnergyFlowInWatt(powerValue, energyFlow.getSIPrefix());
		energyFlow.setValue(powerValue);
	}
	
	

	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getEnergyFlowForLosses(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public EnergyFlowInWatt getEnergyFlowForLosses(TechnicalSystemStateEvaluation tsse) {
		
		FixedDouble fvLosses = (FixedDouble) this.optionModelController.getVariableState(tsse.getIOlist(), TransformerSystemVariable.tLossesPAllPhases.name());
		if (fvLosses==null) return null;
		
		EnergyUnitFactorPrefixSI siPrefix = this.optionModelController.getCostModelsSIPrefix();
		
		EnergyFlowInWatt efLosses = new EnergyFlowInWatt();
		efLosses.setSIPrefix(siPrefix);
		efLosses.setValue(UnitConverter.convertEnergyFlowInWatt(fvLosses.getValue(), siPrefix));
		return efLosses;
	}
	
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getNewEnergyStorageLoad(energy.optionModel.EnergyAmount, energy.DomainInterfaceFlows, long)
	 */
	@Override
	public EnergyAmount getNewEnergyStorageLoad(EnergyAmount storageLoadOld, DomainInterfaceFlows domainInterfaceFlows, long duration) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getDuration(energy.calculations.AbstractOptionModelCalculation.DurationType, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public Duration getDuration(DurationType durationType, TechnicalSystemStateEvaluation tsse) {
		return null;
	}
	
}
