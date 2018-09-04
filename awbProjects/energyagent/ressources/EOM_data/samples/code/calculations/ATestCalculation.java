package energy.samples.calculations;

import energy.GlobalInfo;
import energy.OptionModelController;
import energy.calculations.AbstractOptionModelCalculation;
import energy.optionModel.AbstractEnergyFlow;
import energy.optionModel.AbstractEnergyFlowInWatt;
import energy.optionModel.Duration;
import energy.optionModel.EnergyFlow;
import energy.optionModel.EnergyFlowGradient;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyFlowInWattCalculated;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.State;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * This is just an example and test class for the EOM development.
 */
public class ATestCalculation extends AbstractOptionModelCalculation {

	/**
	 * Instantiates a new a test calculation.
	 * @param optionModelController the option model controller
	 */
	public ATestCalculation(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getDuration(energy.calculations.AbstractOptionModelCalculation.DurationType, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public Duration getDuration(DurationType durationType, TechnicalSystemStateEvaluation tsse) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getEnergyFlow(energy.calculations.AbstractOptionModelCalculation.EnergyFlowType, energy.optionModel.TechnicalSystemStateEvaluation, energy.optionModel.TechnicalInterface, boolean)
	 */
	@Override
	public EnergyFlowInWatt getEnergyFlow(EnergyFlowType energyFlowType, TechnicalSystemStateEvaluation tsse, TechnicalInterface ti, boolean isManualConfiguration) {
	
		double rndNumber = GlobalInfo.getRandomFloat(0.0f, 100.0f);
		rndNumber = GlobalInfo.round(rndNumber, 5);
		EnergyFlowInWatt efWatt = null;
		
		TechnicalInterfaceConfiguration tic = this.optionModelController.getTechnicalInterfaceConfiguration(tsse.getConfigID());
		State state = this.optionModelController.getState(tic, tsse.getStateID());
		AbstractEnergyFlow abstractEnergyFlow = this.optionModelController.getEnergyFlow(state, ti.getInterfaceID());

		if(abstractEnergyFlow instanceof EnergyFlow) {
			AbstractEnergyFlowInWatt abstractEnergyFlowInWatt = ((EnergyFlow) abstractEnergyFlow).getEnergyFlow();
			if(abstractEnergyFlowInWatt instanceof EnergyFlowInWattCalculated) {
				efWatt = new EnergyFlowInWatt();
				efWatt.setSIPrefix(EnergyUnitFactorPrefixSI.KILO_K_3);
				efWatt.setValue(rndNumber);
			}
		}
		return efWatt;
	}

	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getEnergyFlowGradient(energy.calculations.AbstractOptionModelCalculation.EnergyFlowGradientType, energy.optionModel.TechnicalSystemStateEvaluation, energy.optionModel.TechnicalInterface)
	 */
	@Override
	public EnergyFlowGradient getEnergyFlowGradient(EnergyFlowGradientType energyFlowGradientType, TechnicalSystemStateEvaluation tsse, TechnicalInterface ti) {
		return null;
	}

	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getEnergyFlowForLosses(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public EnergyFlowInWatt getEnergyFlowForLosses(TechnicalSystemStateEvaluation tsse) {
		return null;
	}

	
}
