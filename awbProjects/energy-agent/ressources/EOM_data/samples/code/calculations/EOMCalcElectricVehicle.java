package energy.samples.calculations;

import java.util.Vector;

import energy.OptionModelController;
import energy.UnitConverter;
import energy.calculations.AbstractOptionModelCalculation;
import energy.optionModel.Duration;
import energy.optionModel.EnergyAmount;
import energy.optionModel.EnergyFlowGradient;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.StorageInterface;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystemState.StorageLoads;
import energy.optionModel.TechnicalSystemState.UsageOfInterfaces;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.TimeUnit;


/**
 * This is just an example and test class for the EOM development.
 */
public class EOMCalcElectricVehicle extends AbstractOptionModelCalculation {

	/**
	 * Instantiates this class.
	 * @param optionModelController the {@link OptionModelController}
	 */
	public EOMCalcElectricVehicle(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getStateTransitionDurationMax(energy.SelectionModel)
	 */
	@Override
	public Duration getDuration(DurationType durationType, TechnicalSystemStateEvaluation tsse) {

		Duration durationCalculated = new Duration();
		switch (durationType) {
		case StateDuration:
			// --- Nothing to do here ---------------------
			break;

		case StateTransitionDurationMin:
			// --- Nothing to do here ---------------------			
			break;

		case StateTransitionDurationMax:
			// --- Calculate the remaining time to stay -------------
			TechnicalInterfaceConfiguration tic = this.optionModelController.getTechnicalInterfaceConfiguration(tsse.getConfigID());
			for (StorageLoads storageLoad : tsse.getStorageLoads()) {
				
				StorageInterface si = (StorageInterface) this.optionModelController.getTechnicalInterface(tic, storageLoad.getInterfaceID());
				Vector<TechnicalInterface> tiVector = this.optionModelController.getTechnicalInterfacesByEnergyCarrier(tic, si.getEnergyCarrier());

				// --- Get capacity and load of the storage ---------
				EnergyAmount eaCapacity = si.getCapacity();
				EnergyAmount eaLoad = new EnergyAmount();
				eaLoad.setSIPrefix(storageLoad.getStorageLoad().getSIPrefix());
				eaLoad.setTimeUnit(storageLoad.getStorageLoad().getTimeUnit());
				eaLoad.setValue( (-1) * (storageLoad.getStorageLoad().getValue()));
				EnergyAmount eaRemaining = this.optionModelController.sumUpEnergyAmounts(eaCapacity, eaLoad);
				
				// --- Sum up the deltas for the energy carrier -----
				EnergyFlowInWatt efSum = null;
				for (TechnicalInterface ti : tiVector) {
					UsageOfInterfaces uoi = this.optionModelController.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), ti.getInterfaceID());
					if (uoi!=null) {
						efSum = this.optionModelController.sumUpEnergyFlowInWatt(efSum, uoi.getEnergyFlow());	
					}
				}
				// --- Calculate the remaining time -----------------
				double efFlowSumDouble = UnitConverter.convertEnergyFlowToWatt(efSum);
				double efFlowSumDoubleSI = UnitConverter.convertEnergyFlowInWatt(efFlowSumDouble, eaRemaining.getSIPrefix());
				
				double remainingTime = eaRemaining.getValue() / efFlowSumDoubleSI;
				double remainingTimeMillis = UnitConverter.convertDurationToMilliseconds(remainingTime, eaRemaining.getTimeUnit());
				durationCalculated.setUnit(TimeUnit.MILLISECOND_MS);
				durationCalculated.setValue((long)remainingTimeMillis);
			}
			
			break;
		}
		
		return durationCalculated;
	}

	/* (non-Javadoc)
	 * @see energy.calculations.AbstractOptionModelCalculation#getEnergyFlow(energy.calculations.AbstractOptionModelCalculation.EnergyFlowType, energy.optionModel.TechnicalSystemStateEvaluation, energy.optionModel.TechnicalInterface, boolean)
	 */
	@Override
	public EnergyFlowInWatt getEnergyFlow(EnergyFlowType energyFlowType, TechnicalSystemStateEvaluation tsse, TechnicalInterface ti, boolean isManualConfiguration) {
		return null;
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
