package de.enflexit.ea.core.eomStateStream;

import java.util.List;
import java.util.Vector;

import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import energy.FixedVariableList;
import energy.GlobalInfo;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.helper.DisplayHelper;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class IOSetPointStrategyTechnicalSystemRT will be used within an IOSimulated (an extension of 
 * {@link AbstractIOSimulated}) in order to control a single TechnicalSystem according to the specified 
 * set points in real time.  
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class IOSetPointStrategyTechnicalSystemRT extends AbstractEvaluationStrategyRT {

	private AbstractIOSimulated ioSimulated;
	private InitialStateAdaption initialStateAdaption;
	
	private String networkComponentID;
	
	
	/**
	 * Instantiates a new full evaluation strategy.
	 *
	 * @param optionModelController the option model controller
	 * @param ioSimulated the current IO simulated
	 * @param initialStateAdaption the initial state adaption to use 
	 */
	public IOSetPointStrategyTechnicalSystemRT(OptionModelController optionModelController, AbstractIOSimulated ioSimulated, InitialStateAdaption initialStateAdaption, String networkComponentID) {
		super(optionModelController);
		this.ioSimulated = ioSimulated;
		this.initialStateAdaption = initialStateAdaption;
		this.networkComponentID = networkComponentID;
	}

	/**
	 * Returns the EomModelStateInputStream.
	 * @return the EomModelStateInputStream
	 */
	private EomModelStateInputStream getEomModelStateInputStream() {
		if (ioSimulated!=null && ioSimulated.getStateInputStream() instanceof EomModelStateInputStream) {
			return (EomModelStateInputStream) ioSimulated.getStateInputStream();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategyRT#getInitialStateAdaption()
	 */
	@Override
	public InitialStateAdaption getInitialStateAdaption() {
		return this.initialStateAdaption;
	}
	
	/**
	 * Returns the sets the points to system during evaluation.
	 * @return the sets the points to system during evaluation
	 */
	private FixedVariableList getSetPointsToSystemDuringEvaluation(long timeStep) {
		
		// --- Default set point list: last set points to IO interface --------
		FixedVariableList fvListSetPoint = this.ioSimulated.getSetPointsToSystem();
		
		// --- Try accessing ControlBehaviourRT -------------------------------
		if (this.ioSimulated.getEnergyAgent().isExecutedControlBehaviourRT()==true) {
			ControlBehaviourRT cbRT = this.ioSimulated.getEnergyAgent().getControlBehaviourRT();
			TechnicalSystemStateEvaluation lastTSSE = cbRT.getRealTimeEvaluationStrategy().getTechnicalSystemStateEvaluation();
			while (lastTSSE.getGlobalTime()>timeStep) {
				if (lastTSSE.getParent()==null) break;
				lastTSSE = lastTSSE.getParent();
			}
			
			if (lastTSSE.getGlobalTime()==timeStep) {
				// --- Extract set points -------------------------------------
				fvListSetPoint = this.createSetPointListFromSystemState(lastTSSE);
			}
		}
		return fvListSetPoint;
	}
	

	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {
		
		String systemDescription = this.networkComponentID + " - '" + this.optionModelController.getTechnicalSystem().getSystemID() + "'";
		
		// --- Initialize search --------------------------------------------------------
		TechnicalSystemStateEvaluation tsse = this.getTechnicalSystemStateEvaluation();
		// --- Search by walking through time -------------------------------------------
		while (tsse.getGlobalTime() < this.evaluationEndTime ) {
			
			// --- Get all possible subsequent steps and states -------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size()==0) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No further delta steps possible => interrupt search for subsequent states of " + systemDescription  + "!");
				break;
			}
			
			// --- Get or create the current set point list ----------------------------- 
			FixedVariableList fvListSetPoint = this.getSetPointsToSystemDuringEvaluation(tsse.getGlobalTime());			
			if (fvListSetPoint==null) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No system set points are available for " + systemDescription + ". Try to stay in the current system state.");
				fvListSetPoint = this.createSetPointListFromSystemState(tsse);
			} else {
				if (this.isCompleteSetPointList(fvListSetPoint)==false) {
					DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": Found incomplete list of set points for " + systemDescription + ".");
				}
			}
			
			// --- Check the subsequent states after delta ------------------------------
			TechnicalSystemStateDeltaEvaluation tssDeltaDecision = null;
			for (TechnicalSystemStateDeltaEvaluation tsseDelta : deltaSteps) {
				TechnicalSystemStateEvaluation tsseNext = tsseDelta.getTechnicalSystemStateEvaluation();
				if (this.isEqualSetPointVariation(fvListSetPoint, tsseNext.getIOlist())) {
					tssDeltaDecision = tsseDelta;
					break;
				}
			}
			
			// --- Next system state found ? --------------------------------------------
			if (tssDeltaDecision==null) {
				if (deltaSteps.size()==0) {
					DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No subsequent state found for " + systemDescription + ".");
					return;
				} else if (deltaSteps.size()==1) {
					DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No set point matching found " + systemDescription + ". - Use residual system state instead!"); 
					tssDeltaDecision = deltaSteps.get(0);
				} else {
					DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No set point matching found " + systemDescription + ". - Use random decision!");
					int decisionIndex = GlobalInfo.getRandomInteger(0, deltaSteps.size()-1);
					tssDeltaDecision = deltaSteps.get(decisionIndex);	
				}
			}
			
			// --- Set new current TechnicalSystemStateEvaluation -----------------------
			TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaDecision);
			if (tsseNext==null) {
				break;
			} else {
				// --- Set next state as new current state ------------------------------
				tsse = tsseNext;
			}
			this.setTechnicalSystemStateEvaluation(tsse);
			this.setIntermediateStateToResult(tsse);

		} // end while
	}
	
	/**
	 * Checks if is equal set point variation.
	 *
	 * @param fvListSetPoint the FixedVariableList list with the actual set points to follow
	 * @param stateVarList the state variable list
	 * @return true, if is equal set point variation
	 */
	private boolean isEqualSetPointVariation(FixedVariableList fvListSetPoint, List<FixedVariable> stateVarList) {
		
		boolean equalSetPoints = true;
		for (FixedVariable fvSetPoint : fvListSetPoint) {
			// --- Get corresponding variable from the state or IO-list -------
			FixedVariable fvFromList = this.getFixedVariable(stateVarList, fvSetPoint.getVariableID());
			if (fvFromList==null) {
				return false;
			} else {
				// --- Compare the actual value of the set point --------------
				if (fvSetPoint instanceof FixedBoolean) {
					FixedBoolean fBoolSetPoint = (FixedBoolean) fvSetPoint;
					FixedBoolean fBoolFromList = (FixedBoolean) fvFromList;
					if (fBoolSetPoint.isValue()!=fBoolFromList.isValue())  return false;
				} else if (fvSetPoint instanceof FixedInteger) {
					FixedInteger fIntSetPoint = (FixedInteger) fvSetPoint;
					FixedInteger fIntFromList = (FixedInteger) fvFromList;
					if (fIntSetPoint.getValue()!=fIntFromList.getValue()) return false;
				} else if (fvSetPoint instanceof FixedDouble) {
					FixedDouble fDoubleSetPoint = (FixedDouble) fvSetPoint;
					FixedDouble fDoubleFromList = (FixedDouble) fvFromList;
					if (fDoubleSetPoint.getValue()!=fDoubleFromList.getValue()) return false;
				}
			}
		}
		return equalSetPoints;
	}

	/**
	 * Gets the specified fixed variable out of the specified list of fixed variable.
	 *
	 * @param fvList the List of FixedVariable 
	 * @param variableID the variable id
	 * @return the fixed variable
	 */
	private FixedVariable getFixedVariable(List<FixedVariable> fvList, String variableID) {
		for (FixedVariable fv : fvList) {
			if (fv.getVariableID().equals(variableID)) return fv;
		}
		return null;
	}
	
	/**
	 * Creates the a new set point list from a specified system state.
	 *
	 * @param tsse the {@link TechnicalSystemStateEvaluation}
	 * @return the fixed variable list
	 */
	private FixedVariableList createSetPointListFromSystemState(TechnicalSystemStateEvaluation tsse) {
		
		Vector<String> setPointIDs = this.getEomModelStateInputStream().getVariableIDsForSystemSetPoints();
		// ---- Create new FixedVariableList -------------- 
		FixedVariableList fvListSetPoint = new FixedVariableList();
		// --- Check if the variables are set points ------ 
		for (FixedVariable fv : tsse.getIOlist()) {
			if (setPointIDs.contains(fv.getVariableID())==true) {
				fvListSetPoint.add(fv);
			}
		}
		return fvListSetPoint;
	}
	
	/**
	 * Checks if the specified set point list is complete.
	 *
	 * @param fixedVariableList the fixed variable list
	 * @return true, if is complete set point list
	 */
	private boolean isCompleteSetPointList(FixedVariableList fixedVariableList) {
		
		Vector<String> setPointIDs = this.getEomModelStateInputStream().getVariableIDsForSystemSetPoints();
		for (int i = 0; i < setPointIDs.size(); i++) {
			String variableIDSetPoint = setPointIDs.get(i);
			if (fixedVariableList.getVariable(variableIDSetPoint)==null) {
				return false;
			}
		}
		return true;
	}

}
