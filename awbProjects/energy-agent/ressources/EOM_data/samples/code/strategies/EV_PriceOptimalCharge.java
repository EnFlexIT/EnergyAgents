package energy.samples.strategies;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;

import energy.OptionModelController;
import energy.UnitConverter;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation.TechnicalInterfaceCondition;
import energy.optionModel.Connectivity;
import energy.optionModel.CostFunction;
import energy.optionModel.CostsByTime;
import energy.optionModel.EnergyAmount;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.EnergyFlow;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.State;
import energy.optionModel.StorageInterface;
import energy.optionModel.TechnicalSystemState.StorageLoads;
import energy.optionModel.TechnicalSystemStateEvaluation;


/**
 * The Class FullEvaluationStrategy will fully investigate the possibilities 
 * of the current evaluation time span.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class EV_PriceOptimalCharge extends AbstractEvaluationStrategy {

	private final String loadStateName = "Charge";
	private final String idleStateName = "Idle";	
	@SuppressWarnings("unused")
	private final String unloadStateName = "Discharge";
	
	/**
	 * Instantiates a new full evaluation strategy.
	 * @param optionModelController the option model controller
	 */
	public EV_PriceOptimalCharge(OptionModelController optionModelController) {
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
		
		// --- Get context information --------------------------------------------------
		String interfaceID = "Battery";
		StorageInterface si = (StorageInterface) this.optionModelController.getTechnicalInterface(this.getTechnicalInterfaceConfiguration(), interfaceID);
		StorageLoads storageLoads = this.optionModelController.getStorageLoads(tsse, interfaceID);
		
		State loadState = this.optionModelController.getState(this.getTechnicalInterfaceConfiguration(), this.loadStateName);
		EnergyFlow ef = (EnergyFlow) this.optionModelController.getEnergyFlow(loadState, interfaceID);
		
		// --- Get capacity, load and demand information ----------------------
		EnergyAmount stroageCapacity = si.getCapacity();
		EnergyAmount stroageLoad = storageLoads.getStorageLoad();
		EnergyAmount demand = this.optionModelController.subtractEnergyAmount(stroageCapacity, stroageLoad); 
		
		// --- Get the energy flow for load state -----------------------------
		EnergyFlowInWatt ef4LoadDefined = (EnergyFlowInWatt) ef.getEnergyFlow();
		// --- Convert to SIPrefix like demand --------------------------------
		EnergyFlowInWatt ef4LoadDemand = UnitConverter.convertEnergyFlowInWatt(ef4LoadDefined, demand.getSIPrefix());
		
		// --- Create amount that will be charged ------------------------------
		EnergyAmount chargeAmount = new EnergyAmount();
		chargeAmount.setSIPrefix(demand.getSIPrefix());
		chargeAmount.setTimeUnit(demand.getTimeUnit());
		
		// --- Select the intervals to charge the EV --------------------------
		CostFunction cf = this.getCostFunction(EnergyCarrier.ELECTRICITY, Connectivity.INPUT);
		Vector<CostFunctionInterval> cfIntervals = this.getCostFunctionInTimeIntervals(cf);
		for (CostFunctionInterval cfi : cfIntervals) {

			// --- Check if is within the evaluation period -------------------
			if (cfi.getTimeFrom()>this.getStartTime() && cfi.getTimeFrom()<this.getEndTime()) {
				
				double durationInDemandUnit = UnitConverter.convertDuration(cfi.getDuration(), demand.getTimeUnit());
				double amountToAdd = durationInDemandUnit * ef4LoadDemand.getValue();
				if (chargeAmount.getValue()+amountToAdd > demand.getValue()) {
					// --- Reduce the amount to charge here ------------------- 
					amountToAdd = demand.getValue() - chargeAmount.getValue();
				}
				
				EnergyAmount eaToAdd = new EnergyAmount();
				eaToAdd.setSIPrefix(demand.getSIPrefix());
				eaToAdd.setTimeUnit(demand.getTimeUnit());
				eaToAdd.setValue(amountToAdd);
				cfi.setEnergyAmountToUse(eaToAdd);
				
				chargeAmount.setValue(chargeAmount.getValue() + amountToAdd);
				if (chargeAmount.getValue()>=demand.getValue()) {
					break;
				}
			}
			
		}
		this.sortCostFunctionIntervalByTime(cfIntervals);
		
		// ------------------------------------------------------------------------------
		// --- Search by walking through time -------------------------------------------
		// ------------------------------------------------------------------------------
		while (tsse.getGlobalTime() < this.getEndTime() ) {
			
			// --- Get the possible subsequent steps and states -------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size()==0) {
				System.err.println("No further delta steps possible => interrupt search!");
				break;
			}
			
			// --- Decide for the next step, if possible -------------------------------- 
			TechnicalSystemStateDeltaEvaluation tssDeltaDecision = null;
			if (deltaSteps.size()==1) {
				// --- No decision can be made. Just take the current result ------------
				tssDeltaDecision = deltaSteps.get(0);
				
			} else {
				// --- Decide regarding the costs ---------------------------------------
				for (TechnicalSystemStateDeltaEvaluation tssDelta : deltaSteps) {
					TechnicalSystemStateEvaluation tsseNextStep = tssDelta.getTechnicalSystemStateEvaluation();
					long timeFrom = tsse.getGlobalTime();
					long timeTo = tsseNextStep.getGlobalTime(); 
					CostFunctionInterval cfiFound = this.getCostFunctionInterval(timeFrom, timeTo, cfIntervals);
					if (cfiFound.getEnergyAmountToUse()==null) {
						// --- NO usage of energy was planned for this time interval ----
						if (tsseNextStep.getStateID().equals("Idle")==true) {
							tssDeltaDecision = tssDelta;
							break;
						}
						
					} else {
						// --- Energy usage was planned for this time interval ----------
						if (cfiFound.getEnergyAmountToUse().getValue()<=0 && tsseNextStep.getStateID().equals(this.idleStateName)==true) {
							tssDeltaDecision = tssDelta;
							break;
							
						} else if (cfiFound.getEnergyAmountToUse().getValue()>0 && tsseNextStep.getStateID().equals(this.loadStateName)==true) {
							tssDeltaDecision = tssDelta;
							TechnicalInterfaceCondition tiCond = tssDelta.getTechnicalInterfaceDeltas().get(interfaceID);
							EnergyAmount newEnergyAmountToUse = this.optionModelController.subtractEnergyAmount(cfiFound.getEnergyAmountToUse(), tiCond.getDeltaEnergyAmountAndCosts().getEnergyAmount());
							cfiFound.setEnergyAmountToUse(newEnergyAmountToUse);
							break;
							
						}
					}
				}

			}
			
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

	
	private CostFunctionInterval getCostFunctionInterval(long timeFrom, long timeTo, Vector<CostFunctionInterval> cfIntervals) {
		
		CostFunctionInterval cfiFound = null;
		for (CostFunctionInterval cfi : cfIntervals) {
			if (cfi.getTimeFrom()<=timeFrom && cfi.getTimeTo()>=timeTo) {
				cfiFound = cfi;
				break;
			}
		}
		return cfiFound;
	}
	
	/**
	 * Returns the specified CostFunction in time intervals and ascending sorted by the price.
	 *
	 * @param cf the CostFunction
	 * @return the cost function in time intervals
	 */
	private Vector<CostFunctionInterval> getCostFunctionInTimeIntervals(CostFunction cf) {
		
		Vector<CostFunctionInterval> cfIntervals = new Vector<EV_PriceOptimalCharge.CostFunctionInterval>();
		
		List<CostsByTime> cfList = cf.getCostsByTimeSeries();
		for (int i = 0; i < cf.getCostsByTimeSeries().size()-1; i++) {
			
			CostsByTime cbt1 = cfList.get(i);
			CostsByTime cbt2 = cfList.get(i+1);
			
			long startTime = cbt1.getPointInTime();
			double costValueStart = cbt1.getCostValue();
			long endTime = cbt2.getPointInTime();
			double costValueEnd = cbt2.getCostValue();
			
			if (cf.isStepSeries()==true) {
				CostFunctionInterval cfi = new CostFunctionInterval(startTime, endTime, costValueStart);
				cfIntervals.add(cfi);
				
			} else {
				// --- Possibly an error here -------------  
				CostFunctionInterval cfi = new CostFunctionInterval(startTime, endTime, (costValueStart+costValueEnd)/2);
				cfIntervals.add(cfi);
			}
		} // end for
		
		// --- Sort by Price ------------------------------
		this.sortCostFunctionIntervalByPrice(cfIntervals);
		return cfIntervals;
	}
	
	private void sortCostFunctionIntervalByPrice(Vector<CostFunctionInterval> cfIntervals) {
		Collections.sort(cfIntervals, new Comparator<CostFunctionInterval>() {
			@Override
			public int compare(CostFunctionInterval cfi1, CostFunctionInterval cfi2) {
				Double price1 = cfi1.getPrice();
				Double price2 = cfi2.getPrice();
				return price1.compareTo(price2);
			}
		});
	}
	private void sortCostFunctionIntervalByTime(Vector<CostFunctionInterval> cfIntervals) {
		Collections.sort(cfIntervals, new Comparator<CostFunctionInterval>() {
			@Override
			public int compare(CostFunctionInterval cfi1, CostFunctionInterval cfi2) {
				Long time1 = cfi1.getTimeFrom();
				Long time2 = cfi2.getTimeFrom();
				return time1.compareTo(time2);
			}
		});
	}
	
	private class CostFunctionInterval {
		
		private long timeFrom;
		private long timeTo;
		private Long duration;
		private double price;
		
		private EnergyAmount energyAmountToUse;
		
		public CostFunctionInterval(long timeFrom, long timeTo, double price) {
			this.setTimeFrom(timeFrom);
			this.setTimeTo(timeTo);
			this.setPrice(price);
		}
		public long getTimeFrom() {
			return timeFrom;
		}
		public void setTimeFrom(long timeFrom) {
			this.timeFrom = timeFrom;
			this.duration=null;
		}

		public long getTimeTo() {
			return timeTo;
		}
		public void setTimeTo(long timeTo) {
			this.timeTo = timeTo;
			this.duration=null;
		}
		public long getDuration() {
			if (this.duration==null) {
				duration = this.getTimeTo() - this.getTimeFrom();
			}
			return duration;
		}
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		
		public EnergyAmount getEnergyAmountToUse() {
			return energyAmountToUse;
		}
		public void setEnergyAmountToUse(EnergyAmount energyAmountToUse) {
			this.energyAmountToUse = energyAmountToUse;
		}
		
	}
	
	
}
