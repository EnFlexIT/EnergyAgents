package de.enflexit.energyAgent.core.monitoring;

import energy.calculations.AbstractEvaluationCalculation;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.optionModel.Connectivity;
import energy.optionModel.CostFunctionDataSeries;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.InputMeasurement;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;


/**
 * The Class ValueMonitoring takes the real measurements from the system and uses them
 * to update the corresponding FixedVariable.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class ValueMonitoring extends AbstractEvaluationCalculation {

	private AbstractEvaluationStrategyRT rtEvaluationStrategy;
	private AbstractGroupEvaluationStrategyRT rtGroupEvaluationStrategy;

	/**
	 * Gets the real time evaluation strategy.
	 * @return the real time evaluation strategy
	 */
	private AbstractEvaluationStrategyRT getRealTimeEvaluationStrategy() {
		if (rtEvaluationStrategy==null) {
			rtEvaluationStrategy = (AbstractEvaluationStrategyRT) this.getOptionModelController().getEvaluationStrategyRT();
		}
		return rtEvaluationStrategy;
	}
	/**
	 * Gets the real time group evaluation strategy.
	 * @return the real time group evaluation strategy
	 */
	private AbstractGroupEvaluationStrategyRT getRealTimeGroupEvaluationStrategy() {
		if (rtGroupEvaluationStrategy==null) {
			rtGroupEvaluationStrategy = (AbstractGroupEvaluationStrategyRT) this.getOptionModelController().getEvaluationStrategyRT();
		}
		return rtGroupEvaluationStrategy;
	}

	private FixedVariable getMeasurementFromSystem(String variableID) {
		return this.getRealTimeEvaluationStrategy().getMeasurementFromSystem(variableID);
	}
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#updateInputMeasurement(energy.optionModel.TechnicalSystemState, energy.optionModel.FixedVariable)
	 */
	@Override
	public void updateInputMeasurement(TechnicalSystemState tss, FixedVariable variableToUpdate) {
	
		FixedVariable monFV = this.getMeasurementFromSystem(variableToUpdate.getVariableID());
		if (monFV!=null) {
			if (variableToUpdate instanceof FixedBoolean) {
				FixedBoolean monBool = (FixedBoolean) monFV;
				FixedBoolean updBool = (FixedBoolean) variableToUpdate; 
				updBool.setValue(monBool.isValue());
			} else if (variableToUpdate instanceof FixedInteger) {
				FixedInteger monInt = (FixedInteger) monFV;
				FixedInteger updInt = (FixedInteger) variableToUpdate; 
				updInt.setValue(monInt.getValue());
			} else if (variableToUpdate instanceof FixedDouble) {
				FixedDouble monDouble = (FixedDouble) monFV;
				FixedDouble updDouble = (FixedDouble) variableToUpdate; 
				updDouble.setValue(monDouble.getValue());
			}
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
	 * @see energy.calculations.AbstractEvaluationCalculation#getStateCosts(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public double getStateCosts(TechnicalSystemStateEvaluation tsse) {
		return 0;
	}
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getAmountCosts(energy.optionModel.TechnicalSystemStateEvaluation, energy.optionModel.TechnicalInterface, energy.optionModel.Connectivity, double, long, long, double, double)
	 */
	@Override
	public double getAmountCosts(TechnicalInterface ti, Connectivity connectivity, long time1, long time2, double flowValue1, double flowValue2) {
		// TODO Auto-generated method stub
		return 0;
	}

}
