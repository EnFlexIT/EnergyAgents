package de.enflexit.ea.core.monitoring;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

import energy.FixedVariableList;
import energy.OptionModelController;
import energy.calculations.AbstractOptionModelCalculation;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.helper.FixedVariableHelper;
import energy.helper.TechnicalSystemStateDeltaHelper;
import energy.helper.TechnicalSystemStateDeltaHelper.DeltaSelectionBy;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class MonitoringStrategyRT.
 *
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class MonitoringStrategyRT extends AbstractEvaluationStrategyRT {

	private static final String VAR_SUFFIX_MEASURED = "_measured";
	
	private AbstractOptionModelCalculation optionModelCalculation;
	private long remainingEvaluationStepPeriod;

	/**
	 * Instantiates a new realtime MonitoringStrategy.
	 * 
	 * @param optionModelController the option model controller
	 */
	public MonitoringStrategyRT(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/*
	 * @see energy.evaluation.AbstractEvaluationStrategyRT#getInitialStateAdaption()
	 */
	@Override
	public InitialStateAdaption getInitialStateAdaption() {
		return InitialStateAdaption.TemporalMoveToStateDurationsEnd;
	}

	/**
	 * Check, whether running the evaluation is necessary, i.e. whether the given tss is newer than the set evaluation step end time. 
	 * Also, store the remaining period to be spent in the current evaluation step.
	 *
	 * @param tss the TechnicalSystemState, whose global time is to be checked for it's up-to-dateness
	 * @return true, if evaluation is necessary
	 */
	boolean evaluationNecessary(TechnicalSystemState tss) {
		this.remainingEvaluationStepPeriod = this.evaluationEndTime - tss.getGlobalTime();
		return remainingEvaluationStepPeriod > 0;
	}
	/*
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {

		// --- Get initial or last system state ---------------------
		TechnicalSystemStateEvaluation tsse = this.getTechnicalSystemStateEvaluation();

		// --- Do evaluation until 'evaluationStepEndTime' ---------- 
		while (this.evaluationNecessary(tsse)) {

			// --- Get all possible subsequent steps and states -----
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse, null, remainingEvaluationStepPeriod, true);
			if (deltaSteps.isEmpty()) {
				System.err.println("Monitoring: No further delta steps possible => interrupt search!");
				break;
			}

			// --- Decide for the subsequent state ------------------
			TechnicalSystemStateDeltaEvaluation tssDeltaDecision = this.decideForDeltaStep(deltaSteps, getSetPointsToSystem());
			if (tssDeltaDecision == null) {
				System.err.println("Monitoring: No valid subsequent state found => interrupt search!");
				break;
			}

			// --- Add real measurement copy -------------------------
			this.addRealMeasurementCopy(tssDeltaDecision, getMeasurementsFromSystem());

			// --- Apply tssDeltaDecision on tsse to retrieve next state as new current state
			tsse = getNextTechnicalSystemStateEvaluation(tsse, tssDeltaDecision);
			if (tsse == null) {
				System.err.println("Monitoring: Error while using selected delta => interrupt search!");
				break;
			}

			this.setIntermediateStateToResult(tsse);
			this.setTechnicalSystemStateEvaluation(tsse);
		}

	}

	/**
	 * Decide for one of the possible delta steps, by filtering for set-point values.
	 *
	 * @param deltaSteps the delta steps that are possible to decide between
	 * @param setPoints the set points to be filtered for
	 * @return the technical system state delta evaluation decided for
	 */
	private TechnicalSystemStateDeltaEvaluation decideForDeltaStep(Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps, FixedVariableList setPoints) {
		
		// --- TODO Check accuracy of delta steps ---------
		if (deltaSteps.size()==1) return deltaSteps.get(0);
		
		if (setPoints != null) {
			for (FixedVariable targetSetpoint : setPoints) {
				Object setpointValue = null;
				if (targetSetpoint instanceof FixedBoolean) {
					// System.out.println("Filter "+deltaSteps.size()+" deltaSteps for "+targetSetpoint.getVariableID());
					setpointValue = ((FixedBoolean) targetSetpoint).isValue();
				} else if (targetSetpoint instanceof FixedInteger) {
					setpointValue = ((FixedInteger) targetSetpoint).getValue();
				} else {
					System.err.println("Type (" + targetSetpoint.getClass().getSimpleName() + ") of setpoint (" + targetSetpoint.getVariableID() + ") unsupported for filtering");
				}
				deltaSteps = TechnicalSystemStateDeltaHelper.filterTechnicalSystemStateDeltaEvaluation(deltaSteps, DeltaSelectionBy.IO_List, targetSetpoint.getVariableID(), setpointValue);
			}
		}

		if (deltaSteps == null || deltaSteps.isEmpty()) {
			System.err.println("[" + this.getClass().getSimpleName() + "] No delta steps left after filtering for setpoints => interrupt search!");
			return null;
		} else if (deltaSteps.size() > 1) {// it should be only one left
			// --- More than one delta steps, choose the one with the highest global time ---------
			System.err.println("[" + this.getClass().getSimpleName() + "] Too many (" + deltaSteps.size() + ") delta steps left after filtering for setpoints => interrupt search!");
			return null;
		} else {
			return deltaSteps.get(0);
		}
	}

	/**
	 * Adds a copy of the measurements to the variables of the tss.
	 *
	 * @param tssDeltaDecision the tss delta decision, whose variables should be copied
	 * @param measurements the measurements to be copied
	 */
	private void addRealMeasurementCopy(TechnicalSystemStateDeltaEvaluation tssDeltaDecision, List<FixedVariable> measurements) {
		for (FixedVariable measurement : measurements) {
			FixedVariable measurementCopy = FixedVariableHelper.copyFixedVariable(measurement);
			measurementCopy.setVariableID(measurementCopy.getVariableID() + VAR_SUFFIX_MEASURED);
			tssDeltaDecision.getTechnicalSystemStateEvaluation().getIOlist().add(measurementCopy);
		}
	}

	/*
	 * @see energy.evaluation.AbstractEvaluationStrategy#getOptionModelCalculation()
	 * 
	 * Overrides the parent method to have the optionModelCalculation available here
	 */
	@Override
	public AbstractOptionModelCalculation getOptionModelCalculation() {
		if (optionModelCalculation == null) {
			optionModelCalculation = this.optionModelController.createOptionModelCalculation();
		}
		return optionModelCalculation;
	}

	/**
	 * Sets the calculation instance used by the EOM evaluation.
	 *
	 * @param optionModelCalculation the new option model calculation
	 */
	public void setOptionModelCalculation(AbstractOptionModelCalculation optionModelCalculation) {
		this.optionModelCalculation = optionModelCalculation;
	}

	/**
	 * Sets the calculation class and instantiates an object to be used by the EOM evaluation.
	 *
	 * @param optionModelCalculationClass the new option model calculation class
	 */
	public void setOptionModelCalculationClass(Class<? extends AbstractOptionModelCalculation> optionModelCalculationClass) {
		try {
			setOptionModelCalculation(optionModelCalculationClass.getDeclaredConstructor(new Class[] { OptionModelController.class }).newInstance(new Object[] { optionModelController }));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
}
