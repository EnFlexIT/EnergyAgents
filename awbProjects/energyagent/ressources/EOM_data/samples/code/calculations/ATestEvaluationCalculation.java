package energy.samples.calculations;

import energy.GlobalInfo;
import energy.calculations.AbstractEvaluationCalculation;
import energy.optionModel.Connectivity;
import energy.optionModel.CostFunction;
import energy.optionModel.CostsByTime;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedVariable;
import energy.optionModel.InputMeasurement;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;


/**
 * This is just an example and test class for the EOM development.
 */
public class ATestEvaluationCalculation extends AbstractEvaluationCalculation {

	
	private TechnicalSystemState lastTSS;
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getInputMeasurement(energy.optionModel.SystemVariableDefinition)
	 */
	@Override
	public InputMeasurement getInputMeasurement(SystemVariableDefinition sysVarDef) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#updateInputMeasurement(energy.optionModel.TechnicalSystemState, energy.optionModel.FixedVariable)
	 */
	@Override
	public void updateInputMeasurement(TechnicalSystemState tss, FixedVariable variableToUpdate) {

		if (tss!=lastTSS) {
			if (variableToUpdate.getVariableID().equalsIgnoreCase("current")==true) {
				String stateID = tss.getStateID();
				FixedDouble fd = (FixedDouble) variableToUpdate;
				if (stateID.equals("Idle")) {
					fd.setValue(0.1);	
				} else if (stateID.equals("Start Up")) {
					fd.setValue(-4.3478);
				} else if (stateID.equals("Operation")) {
					fd.setValue(-4.3478);
				} else if (stateID.equals("Shutdown")) {
					fd.setValue(0.1);
				}
			}	
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getCostFunction(energy.optionModel.EnergyCarrier, energy.optionModel.Connectivity)
	 */
	@Override
	public CostFunction getCostFunction(EnergyCarrier energyCarrier, Connectivity connectivity) {
		
		long evalStart = this.getEvaluationTimeStart();
		long evalEnd = this.getEvaluationTimeEnd();
		
		if (evalStart==evalEnd) {
			evalEnd = evalStart + (1000*60*60*24);
		}
		
		CostFunction cf = new CostFunction();
		cf.setStepSeries(false);
		
		CostsByTime cbt = new CostsByTime();
		cbt.setPointInTime(evalStart);
		cbt.setCostValue(0.21);
		cf.getCostsByTimeSeries().add(cbt);

		cbt = new CostsByTime();
		cbt.setPointInTime(evalEnd);
		cbt.setCostValue(0.21);
		cf.getCostsByTimeSeries().add(cbt);
		
		return cf;
	}
	
	/* (non-Javadoc)
	 * @see energy.calculations.AbstractEvaluationCalculation#getStateCosts(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public double getStateCosts(TechnicalSystemStateEvaluation tsse) {
		double stateCosts = 0;
		if (tsse.getStateID().equalsIgnoreCase("Idle")) {
			stateCosts = 0.00;
		} else {
			stateCosts = GlobalInfo.getRandomFloat(0.02f, 0.05f);
		}
		return stateCosts;
	}



}
