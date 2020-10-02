package de.enflexit.ea.core.eomStateStream;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import de.enflexit.ea.core.AbstractIOSimulated;
import energy.FixedVariableList;
import energy.FixedVariableListForAggregation;
import energy.GlobalInfo;
import energy.OptionModelController;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.evaluation.AbstractEvaluationStrategyRT.InitialStateAdaption;
import energy.helper.DisplayHelper;
import energy.optionModel.AbstractFlow;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.GroupTreeNodeObject;
import energygroup.calculation.FlowsMeasuredGroup;
import energygroup.calculation.FlowsMeasuredGroupMember;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import energygroup.sequentialNetworks.AbstractSequentialNetworkCalculation;

/**
 * The Class IOSetPointStrategyTechnicalSystemRT will be used within an IOSimulated (an extension of 
 * {@link AbstractIOSimulated}) in order to control a TechnicalSystemGroup according to the specified 
 * set points in real time.  
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class IOSetPointStrategyTechnicalSystemGroupRT extends AbstractGroupEvaluationStrategyRT {

	private AbstractIOSimulated ioSimulated;
	private InitialStateAdaption initialStateAdaption;
	
	private String networkComponentID;
	
	private FixedVariableListForAggregation setPoint4Aggregation;
	private HashMap<String, Integer> lastFixedVariableListIndex;
	
	/**
	 * Instantiates a new IO set point strategy technical system group rt.
	 *
	 * @param optionModelController the option model controller
	 * @param ioSimulated the current simulated IO behaviour
	 * @param initialStateAdaption the initial state adaption
	 */
	public IOSetPointStrategyTechnicalSystemGroupRT(OptionModelController optionModelController, AbstractIOSimulated ioSimulated, InitialStateAdaption initialStateAdaption, String networkComponentID) {
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
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategyRT#getInitialStateAdaption()
	 */
	@Override
	public InitialStateAdaption getInitialStateAdaption() {
		return this.initialStateAdaption;
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {
		
		String systemDescription = this.networkComponentID + " - '" + this.optionModelController.getTechnicalSystem().getSystemID() + "'";
		
		// --- Initialise search --------------------------------------------------------
		TechnicalSystemStateEvaluation tsse = this.getTechnicalSystemStateEvaluation();
		// --- Search by walking through time -------------------------------------------
		while (tsse.getGlobalTime() < this.evaluationStepEndTime ) {
			
			// --- Get all possible subsequent steps and states -------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
			if (deltaSteps.size()==0) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No further delta steps possible => interrupt search of " + systemDescription  + "!");
				break;
			}
			
			// --- Meet decision for the current set points ----------------------------- 
			FixedVariableList fvListSetPoint = this.ioSimulated.getSetPointsToSystem();
			if (! (fvListSetPoint instanceof FixedVariableListForAggregation)) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": The set point list of the aggregation " + systemDescription + " is NOT of type FixedVariableListForAggregation.");
			} else {
				// --- Remind decision set for sub systems ------------------------------
				this.setPoint4Aggregation = (FixedVariableListForAggregation) fvListSetPoint;
				this.resetLastFixedVariableListIndex();
			}

			// --- Work on the superordinate system -------------------------------------
			if (fvListSetPoint==null) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No system set points are available for " + systemDescription + ". Try to stay in the current system state.");
				fvListSetPoint = this.createSetPointListFromSystemState(tsse);
			} else if (! (fvListSetPoint instanceof FixedVariableListForAggregation)) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": The set point list of the aggregation " + systemDescription + " is NOT of type FixedVariableListForAggregation.");
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
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": Error while using selected delta => interrupt search of " + systemDescription + "!");
				break;
			} else {
				// --- Set next state as new current state ------------------------------
				tsse = tsseNext;
			}
			this.setTechnicalSystemStateEvaluation(tsse);
			this.setIntermediateStateToResult(tsse);

		} // end while
	}

	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#meetDecisionForTechnicalSystem(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject, java.util.Vector)
	 */
	@Override
	public TechnicalSystemStateDeltaEvaluation meetDecisionForTechnicalSystem(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps) {

		// --- Meet decision for the current set points ---------------------------------
		String subSystemDescription = gtno.getSystemDescription(false);
		String subSystemID = gtno.getGroupMember().getNetworkID();
		FixedVariableList fvListSetPoint = this.getNextSetPointListForSubSystem(subSystemID);

		// --- Work on the actual sub system --------------------------------------------
		if (fvListSetPoint==null) {
			DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No system set points are available for '" + subSystemDescription + "'. Try to stay in the current system state.");
			fvListSetPoint = this.createSetPointListFromSystemState(tsse);
		} else {
			if (this.isCompleteSetPointList(fvListSetPoint)==false) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": Found incomplete list of set points for '" + subSystemDescription + "'.");
			}
		}
		
		// --- Check the subsequent states after delta ----------------------------------
		TechnicalSystemStateDeltaEvaluation tssDeltaDecision = null;
		for (TechnicalSystemStateDeltaEvaluation tsseDelta : deltaSteps) {
			TechnicalSystemStateEvaluation tsseNext = tsseDelta.getTechnicalSystemStateEvaluation();
			if (this.isEqualSetPointVariation(fvListSetPoint, tsseNext.getIOlist())) {
				tssDeltaDecision = tsseDelta;
				break;
			}
		}
		
		// --- Next system state found ? ------------------------------------------------
		if (tssDeltaDecision==null) {
			if (deltaSteps.size()==0) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No subsequent state found for '" + subSystemDescription + "'.");
			} else if (deltaSteps.size()==1) {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No set point matching found '" + subSystemDescription + "'. - Use residual system state instead!"); 
				tssDeltaDecision = deltaSteps.get(0);
			} else {
				DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No set point matching found '" + subSystemDescription + "'. - Use random decision!");
				int decisionIndex = GlobalInfo.getRandomInteger(0, deltaSteps.size()-1);
				tssDeltaDecision = deltaSteps.get(decisionIndex);	
			}
		}
		return tssDeltaDecision;
	}
	
	/**
	 * Returns the next set point list of the specified sub system.
	 *
	 * @param subSystemID the sub system ID
	 * @return the next set point list
	 */
	private FixedVariableList getNextSetPointListForSubSystem(String subSystemID) {
		
		FixedVariableList setPointsFound = null;
		if (this.setPoint4Aggregation!=null) {
			Vector<FixedVariableList> fvlVector = this.setPoint4Aggregation.getFixedVariableListForSubSystem(subSystemID);
			if (fvlVector!=null) {
				// --- Check the number of set-point lists ------------------------
				if (fvlVector.size()==0) {
					// --- Nothing to do here -------------------------------------
					
				} else if (fvlVector.size()==1) {
					// --- Just get this single set point list --------------------
					setPointsFound = fvlVector.get(0);
					
				} else {
					// --- Get the last index used for this system ----------------
					Integer newLastIndex = this.getLastFixedVariableListIndex().get(subSystemID);
					if (newLastIndex==null) {
						newLastIndex=0;
					} else {
						newLastIndex++;
					}
					// --- Check for valid index ----------------------------------
					if (newLastIndex > (fvlVector.size()-1)) {
						DisplayHelper.systemOutPrintlnGlobalTime(tsse.getGlobalTime(), "=> ", this.getClass().getSimpleName() + ": No set point list was found for '" + subSystemID + "'!");
					} else {
						// --- Remind this as last used index ---------------------
						this.getLastFixedVariableListIndex().put(subSystemID, newLastIndex);
						// --- Set actual FixedVariableList as return value -------
						setPointsFound = fvlVector.get(newLastIndex);
					}
					
				}
			}
		}
		return setPointsFound;
	}
	/**
	 * Returns the HashMap of indexes for used {@link FixedVariableList}s in the current set point settings.
	 * @return the last fixed variable list index
	 */
	private HashMap<String, Integer> getLastFixedVariableListIndex() {
		if (lastFixedVariableListIndex==null) {
			lastFixedVariableListIndex = new HashMap<>();
		}
		return lastFixedVariableListIndex;
	}
	/**
	 * Resets the HashMap of indexes for used {@link FixedVariableList}s in the current set point settings.
	 */
	private void resetLastFixedVariableListIndex() {
		lastFixedVariableListIndex = null;
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
	
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#doNetworkCalculation(javax.swing.tree.DefaultMutableTreeNode, java.util.List, energygroup.calculation.FlowsMeasuredGroup)
	 */
	@Override
	public FlowsMeasuredGroupMember doNetworkCalculation(DefaultMutableTreeNode currentParentNode, List<TechnicalInterface> outerInterfaces, FlowsMeasuredGroup efmGroup) {
		FlowsMeasuredGroupMember efmGroupMember = new FlowsMeasuredGroupMember();
		for (TechnicalInterface ti : outerInterfaces) {
			// --- Calculate the energy flow for this interface ---------------
			AbstractFlow afm = efmGroup.sumUpFlowMeasuredByDomainAndDomainModel(currentParentNode, ti.getInterfaceID(), ti.getDomain(), ti.getDomainModel());
			efmGroupMember.addAbstractFlowMeasured(afm, ti);
		}
		return efmGroupMember;
	}
	
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#meetDecisionForScheduleList(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject, energy.optionModel.ScheduleList)
	 */
	@Override
	public Schedule meetDecisionForScheduleList(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, ScheduleList scheduleList) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#getSequentialNetworkCalculation()
	 */
	@Override
	public AbstractSequentialNetworkCalculation<?> getSequentialNetworkCalculation() {
		return null;
	}
	
}
