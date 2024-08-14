package de.enflexit.ea.electricity.transformer.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import de.enflexit.common.DateTimeHelper;
import de.enflexit.ea.core.dataModel.ontology.SlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorInterface;
import de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerPower;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.LowVoltageThriPhase;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.LowVoltageUniPhase;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.evaluation.AbstractSnapshotStrategy;
import energy.helper.DisplayHelper;
import energy.helper.NumberHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.helper.UnitConverter;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;

/**
 * The Class AbstractTransformerSnapshotStrategy serves as base class for transformer .
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractTransformerSnapshotStrategy extends AbstractSnapshotStrategy implements DiscreteIteratorInterface {

	private static final int MAX_NUMBER_OF_ITERATIONS = 5; 
	private static final double DESTINATION_DELTA_VOLTS_REAL = 0.001;		// --- In Volt [V] ---   
	private static final double DESTINATION_DELTA_VOLTS_IMAG = 0.001;		// --- In Volt [V] ---
	
	private TransformerDataModel transformerDataModel;

	private Double lowVoltageLevel;
	private double relativeVoltageBand = 0.1;
	
	private Double upperVoltageBoundaryInVoltGeneral;
	private Double lowerVoltageBoundaryInVoltGeneral;

	private Double upperVoltageBoundaryInVoltForControlNode;
	private Double lowerVoltageBoundaryInVoltForControlNode;

	
	private List<String> setPointVariableIDs;
	
	private TechnicalSystemStateEvaluation tssePrev;
	private DiscreteSystemStateType lastSubClassDiscreteSystemStateType;
	private int iterationCounter;
	
	protected boolean isPrintIterationSystemState;
	protected boolean isPrintIterationErrorsOnly;
	
	private DiscreteSystemStateType discreteSystemStateType;
	
	
	/**
	 * Instantiates a new abstract transformer snapshot strategy.
	 * @param optionModelController the option model controller
	 */
	public AbstractTransformerSnapshotStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	// ----------------------------------------------------------------------------------
	// --- Methods to access the current TransformerDataModel ---------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Returns the transformer data model.
	 * @return the transformer data model
	 */
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel == null) {
			if (this.optionModelController!=null) {
				SystemVariableDefinitionStaticModel sysVarDefStaticModel = (SystemVariableDefinitionStaticModel) this.optionModelController.getSystemVariableDefinition(this.optionModelController.getTechnicalSystem().getSystemVariables(), "StaticParameters");
				this.transformerDataModel = (TransformerDataModel) this.optionModelController.getStaticModelInstance(sysVarDefStaticModel);
			}
		}
		return this.transformerDataModel;
	}
	
	
	/**
	 * Gets the low voltage level.
	 * @return the low voltage level
	 */
	public Double getTransformerLowVoltageLevel() {
		if (lowVoltageLevel==null) {
			lowVoltageLevel = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000.0;
		}
		return lowVoltageLevel;
	}

	
	/**
	 * Returns the general upper voltage boundary in volt.
	 * @return the general upper voltage boundary in volt
	 */
	public Double getUpperVoltageBoundaryInVoltGeneral() {
		if (upperVoltageBoundaryInVoltGeneral==null) {
			upperVoltageBoundaryInVoltGeneral = this.getTransformerLowVoltageLevel() + (this.getTransformerLowVoltageLevel() * this.relativeVoltageBand);
		}
		return upperVoltageBoundaryInVoltGeneral;
	}
	/**
	 * Returns the general lower voltage boundary in volt.
	 * @return the general lower voltage boundary in volt
	 */
	public Double getLowerVoltageBoundaryInVoltGeneral() {
		if (lowerVoltageBoundaryInVoltGeneral==null) {
			lowerVoltageBoundaryInVoltGeneral = this.getTransformerLowVoltageLevel() - (this.getTransformerLowVoltageLevel() * this.relativeVoltageBand);
		}
		return lowerVoltageBoundaryInVoltGeneral;
	}
	/**
	 * Returns the voltage delta to the general voltage boundaries.
	 *
	 * @param voltageReal the voltage real
	 * @return the voltage delta to control node boundaries or 0 if the voltage level is in the allowed range
	 */
	public double getVoltageDeltaToGeneralBoundaries(double voltageReal) {
		
		if (voltageReal<=this.getUpperVoltageBoundaryInVoltGeneral() & voltageReal>=this.getLowerVoltageBoundaryInVoltGeneral()) {
			return 0;
		} else {
			if (voltageReal>this.getUpperVoltageBoundaryInVoltGeneral()) {
				return voltageReal - this.getUpperVoltageBoundaryInVoltGeneral();
			} else {
				return voltageReal - this.getLowerVoltageBoundaryInVoltGeneral();
			}
		}
	}
	

	/**
	 * Returns the upper voltage boundary in volt for the control node.
	 * @return the upper voltage boundary in volt for the control node
	 */
	private Double getUpperVoltageBoundaryInVoltForControlNode() {
		if (upperVoltageBoundaryInVoltForControlNode==null) {
			double relUpperBoundary = this.getTransformerDataModel().getControlNodeUpperVoltageLevel() / 100.0;
			upperVoltageBoundaryInVoltForControlNode = this.getTransformerLowVoltageLevel() * relUpperBoundary;
		}
		return upperVoltageBoundaryInVoltForControlNode;
	}
	/**
	 * Returns the lower voltage boundary in volt for the control node.
	 * @return the lower voltage boundary in volt for the control node
	 */
	private Double getLowerVoltageBoundaryInVoltForControlNode() {
		if (lowerVoltageBoundaryInVoltForControlNode==null) {
			double relLowerBoundary = this.getTransformerDataModel().getControlNodeLowerVoltageLevel() / 100.0;
			lowerVoltageBoundaryInVoltForControlNode = this.getTransformerLowVoltageLevel() * relLowerBoundary;
		}
		return lowerVoltageBoundaryInVoltForControlNode;
	}
	/**
	 * Returns the voltage delta to the control node voltage boundaries.
	 *
	 * @param voltageAbs the absolute voltage level
	 * @return the voltage delta to control node boundaries or 0 if the voltage level is in the allowed range
	 */
	private double getVoltageDeltaToControlNodeBoundaries(double voltageAbs) {
		
		if (voltageAbs<=this.getUpperVoltageBoundaryInVoltForControlNode() & voltageAbs>=this.getLowerVoltageBoundaryInVoltForControlNode()) {
			return 0;
		} else {
			if (voltageAbs>this.getUpperVoltageBoundaryInVoltForControlNode()) {
				return voltageAbs - this.getUpperVoltageBoundaryInVoltForControlNode();
			} else {
				return voltageAbs - this.getLowerVoltageBoundaryInVoltForControlNode();
			}
		}
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- Methods for the set point handling -------------------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Returns the list of variable IDs that are set points in the current transformer model.
	 * @return the list of set points variable IDs
	 */
	private List<String> getSetPointVariableIDs() {
		if (setPointVariableIDs==null) {
			setPointVariableIDs = new ArrayList<String>();
			// --- List all system variables defined ----------------
			List<SystemVariableDefinition> sysVarDefList =  this.optionModelController.getTechnicalSystem().getSystemVariables();
			for (int i = 0; i < sysVarDefList.size(); i++) {
				SystemVariableDefinition sysVarDef = sysVarDefList.get(i);
				if (sysVarDef.isSetPoint()==true && sysVarDef.isSetPointForUser()==false) {
					setPointVariableIDs.add(sysVarDef.getVariableID());
				}
			}
		}
		return setPointVariableIDs;
	}
	/**
	 * Checks if the specified {@link FixedVariable} is in the local list of set points.
	 *
	 * @param fv the FixedVariable
	 * @return true, if the specified variabel represents a set point
	 */
	private boolean isSetPointVariable(FixedVariable fv) {
		return this.getSetPointVariableIDs().contains(fv.getVariableID());
	}
	/**
	 * Return the list of {@link FixedVariable}s that are set points.
	 *
	 * @param ioList the list of FixedVariable's (e.g. from the IO-list of a tsse) 
	 * @return the sets the point settings
	 */
	private List<FixedVariable> getSetPointSettings(List<FixedVariable> ioList) {
		
		if (ioList==null || ioList.size()==0) return null;
		
		List<FixedVariable> setPointList = new ArrayList<FixedVariable>();
		
		for (int i = 0; i < ioList.size(); i++) {
			FixedVariable fv = ioList.get(i);
			if (this.isSetPointVariable(fv)==true) {
				setPointList.add(fv);
			}
		}
		return setPointList;
	}
	
	/**
	 * Return the TechnicalSystemStateEvaluation with the specified target set point values (or null).
	 *
	 * @param tsseVector the tsse vector
	 * @param targetSetPoints the target set points
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluationWithTargetSetPoints(Vector<TechnicalSystemStateEvaluation> tsseVector, List<FixedVariable> targetSetPoints) {
		for (int i = 0; i < tsseVector.size(); i++) {
			TechnicalSystemStateEvaluation tsseCheck = tsseVector.get(i);
			if (this.isTechnicalSystemStateEvaluationWithTargetSetPoints(tsseCheck, targetSetPoints)==true) {
				return tsseCheck;
			}
		}
		return null;
	}
	/**
	 * Checks if the specified system state matches the specified set points.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation
	 * @param targetSetPoints the target set points
	 * @return true, if is technical system state evaluation with target set points
	 */
	private boolean isTechnicalSystemStateEvaluationWithTargetSetPoints(TechnicalSystemStateEvaluation tsse, List<FixedVariable> targetSetPoints) {
		
		// --- Check list of set points -------------------
		for (int i = 0; i < targetSetPoints.size(); i++) {
			
			FixedVariable fvSetPoint = targetSetPoints.get(i);
			FixedVariable fvFromTsse = TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), fvSetPoint.getVariableID());
			if (fvFromTsse==null) return false;
			
			if (fvSetPoint instanceof FixedBoolean) {
				FixedBoolean fbSetPoint = (FixedBoolean) fvSetPoint;
				FixedBoolean fbFromTsse = (FixedBoolean) fvFromTsse;
				if (fbSetPoint.isValue()!=fbFromTsse.isValue()) return false;
				
			} else if (fvSetPoint instanceof FixedInteger) {
				FixedInteger fiSetPoint = (FixedInteger) fvSetPoint;
				FixedInteger fiFromTsse = (FixedInteger) fvFromTsse;
				if (fiSetPoint.getValue()!=fiFromTsse.getValue()) return false;
				
			} else if (fvSetPoint instanceof FixedDouble) {
				FixedDouble fdSetPoint = (FixedDouble) fvSetPoint;
				FixedDouble fdFromTsse = (FixedDouble) fvFromTsse;
				if (fdSetPoint.getValue()!=fdFromTsse.getValue()) return false;
				
			}
		}
		return true;
	}
	
	
	/**
	 * Return the slack node state from the specified TechnicalSystemStateEvaluation.
	 * @param tsse the TechnicalSystemStateEvaluation
	 * @return the slack node state from technical system state evaluation
	 */
	private SlackNodeState getSlackNodeStateFromTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsse) {
		
		SlackNodeState slackNodeState = null;
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			// --- Three phase slack node state ---------------------
			slackNodeState = AbstractSlackNodeHandler.getTriPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(tsse);
		} else {
			// --- Uni phase slack node state -----------------------
			slackNodeState = AbstractSlackNodeHandler.getUniPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(tsse);
		}
		return slackNodeState;
	}

	/**
	 * Checks if a another iteration is required with the specified SlackNodeState's.
	 *
	 * @param snsPrev the previous SlackNodeState
	 * @param snsNew the new/current SlackNodeState
	 * @return true, if a another iteration step is required
	 */
	private boolean isDoFurtherIterationStep(SlackNodeState snsPrev, SlackNodeState snsNew) {
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
			return isDoFurtherIterationStep((TriPhaseSlackNodeState)snsPrev, (TriPhaseSlackNodeState)snsNew);
		} else {
			return isDoFurtherIterationStep((UniPhaseSlackNodeState)snsPrev, (UniPhaseSlackNodeState)snsNew);
		}
	}
	/**
	 * Checks if a another iteration is required with the specified TriPhaseSlackNodeState's.
	 * @param snsPrev the previous TriPhaseSlackNodeState
	 * @param snsNew the new/current TriPhaseSlackNodeState
	 * @return true, if a another iteration step is required
	 */
	private boolean isDoFurtherIterationStep(TriPhaseSlackNodeState tpSnsPrev, TriPhaseSlackNodeState tpSnsNew) {
		if (this.isDoFurtherIterationStep(tpSnsPrev.getSlackNodeStateL1(), tpSnsNew.getSlackNodeStateL1())==true) return true;
		if (this.isDoFurtherIterationStep(tpSnsPrev.getSlackNodeStateL2(), tpSnsNew.getSlackNodeStateL2())==true) return true;
		if (this.isDoFurtherIterationStep(tpSnsPrev.getSlackNodeStateL3(), tpSnsNew.getSlackNodeStateL3())==true) return true;
		return false;
	}
	/**
	 * Checks if a another iteration is required with the specified UniPhaseSlackNodeState's.
	 * @param snsPrev the previous UniPhaseSlackNodeState
	 * @param snsNew the new/current UniPhaseSlackNodeState
	 * @return true, if a another iteration step is required
	 */
	private boolean isDoFurtherIterationStep(UniPhaseSlackNodeState upSnsPrev, UniPhaseSlackNodeState upSnsNew) {
		float diffReal = Math.abs(upSnsPrev.getVoltageReal().getValue() - upSnsNew.getVoltageReal().getValue());
		float diffImag = Math.abs(upSnsPrev.getVoltageImag().getValue() - upSnsNew.getVoltageImag().getValue());
		return this.isDoFurtherIterationStep(diffReal, diffImag);
	}
	/**
	 * Checks if a another iteration is required with the specified differential voltages between previous and current (new) iteration step.
	 *
	 * @param diffReal the absolute difference of the real voltage between previous and new/current state
	 * @param diffImag the absolute difference of the imaginary voltage between previous and new/current state
	 * @return true, if a another iteration step is required
	 */
	private boolean isDoFurtherIterationStep(float diffReal, float diffImag) {
		if (this.iterationCounter>=MAX_NUMBER_OF_ITERATIONS) {
			System.err.println("[" + this.getClass().getSimpleName() + "] The maximum number of Iteration between transformer and poer flow calculation is reached. Finalize this iteration.");
			return false;
		}
		if (diffReal<=DESTINATION_DELTA_VOLTS_REAL && diffImag<=DESTINATION_DELTA_VOLTS_IMAG) return false;
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractSnapshotStrategy#getTechnicalSystemStateEvaluation(long, java.util.Vector)
	 */
	@Override
	public final TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation(long evaluationTime, Vector<TechnicalSystemStateEvaluation> possSysStates) {
		
		TransformerEvaluationState transformerAnswer = null;
		TechnicalSystemStateEvaluation tsseAnswer = null;
		
		// --- Check if we are in an iteration for the current time step ----------------
		if (this.tssePrev!=null && this.tssePrev.getGlobalTime()==evaluationTime) {
			// --- Get the previously calculated voltage levels -------------------------
			List<FixedVariable> setPointSettings = this.getSetPointSettings(this.tssePrev.getIOlist());
			SlackNodeState snsPrev = this.getSlackNodeStateFromTechnicalSystemStateEvaluation(this.tssePrev);
			
			// --- Get new calculated voltage levels while using same set points --------
			TechnicalSystemStateEvaluation tsseNew = this.getTechnicalSystemStateEvaluationWithTargetSetPoints(possSysStates, setPointSettings);
			SlackNodeState snsNew = this.getSlackNodeStateFromTechnicalSystemStateEvaluation(tsseNew);
			
			// --- Decide to further iterate --------------------------------------------
			boolean doIterationBecauseOfDiff = this.isDoFurtherIterationStep(snsPrev, snsNew);
			if (doIterationBecauseOfDiff==true) {
				// --- Return new TSSE with new slack node voltage ----------------------
				tsseAnswer = tsseNew;
				this.setDiscreteSystemStateType(DiscreteSystemStateType.Iteration);
				this.iterationCounter++;
				
			} else {
				// --- Do another iteration or done? ------------------------------------
				if (this.lastSubClassDiscreteSystemStateType!=null && this.lastSubClassDiscreteSystemStateType==DiscreteSystemStateType.Final) {
					// --- Nothing else to do - return previous state again ------------- 
					tsseAnswer = tsseNew;
					this.setDiscreteSystemStateType(this.lastSubClassDiscreteSystemStateType);
					
				} else {
					// --- Ask sub class again ------------------------------------------
					transformerAnswer = this.getTranformerEvaluationState(evaluationTime, possSysStates, tsseNew);
					// --- Remind DiscreteSystemStateType of subclass -------------------
					this.lastSubClassDiscreteSystemStateType = transformerAnswer.getDiscreteSystemStateType();
					if (transformerAnswer.getTechnicalSystemStateEvaluation()!=null) {
						tsseAnswer = transformerAnswer.getTechnicalSystemStateEvaluation();
					} else {
						tsseAnswer = tsseNew;	
					}
					if (transformerAnswer.getDiscreteSystemStateType()!=null) {
						this.setDiscreteSystemStateType(transformerAnswer.getDiscreteSystemStateType());
					} else {
						this.setDiscreteSystemStateType(DiscreteSystemStateType.Final);
					}
					
				}
				this.iterationCounter = 0;
			}

		} else {
			// --- Get a new decision state from sub class ------------------------------
			transformerAnswer = this.getTranformerEvaluationState(evaluationTime, possSysStates, null);
			// --- Remind DiscreteSystemStateType of subclass ---------------------------
			this.lastSubClassDiscreteSystemStateType = transformerAnswer.getDiscreteSystemStateType();
			tsseAnswer = transformerAnswer.getTechnicalSystemStateEvaluation();
			this.setDiscreteSystemStateType(DiscreteSystemStateType.Iteration);
			this.iterationCounter = 0;
		}
		
		// --- Remind returned system state ---------------------------------------------
		this.tssePrev = tsseAnswer;
		
		// --- Print system state? ------------------------------------------------------
		if (this.isPrintIterationSystemState==true) {
			this.debugPrintSystemState(tsseAnswer);
		}
		return tsseAnswer;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorInterface#getDiscreteSystemStateType()
	 */
	@Override
	public final DiscreteSystemStateType getDiscreteSystemStateType() {
		if (discreteSystemStateType==null) {
			discreteSystemStateType = DiscreteSystemStateType.Iteration;
		}
		return discreteSystemStateType;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorInterface#setDiscreteSystemStateType(agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType)
	 */
	@Override
	public final void setDiscreteSystemStateType(DiscreteSystemStateType newSystemStateType) {
		this.discreteSystemStateType = newSystemStateType;
	}
	
	/**
	 * <b>Replacement and extension for method {@link #getTechnicalSystemStateEvaluation(long, Vector)} in snapshot strategy and thus same question and handling.</b><br><br>
	 * 
	 * Has to return the {@link TransformerEvaluationState} for the transformer at the specified evaluation time out of the specified list 
	 * of possible system states. The list contains system states with updated IO values as well as heron based energy and good flow calculations.<br><br>
	 * 
	 * The specified system state 'tssePrev' will either be <code>null</code> with a new time step. Within an iteration process between power flow calculation and transformer
	 * calculation, this state will represent the final iteration result, reaching the desired difference between transformer and slack node voltage level. 
	 * If specified, as answer, a new state with different set point setting can be returned or simply this instance.<br>
	 * 
	 * In the case that 'tssePrev' is null it will be assumed that the {@link DiscreteSystemStateType} is always 'Iteration'!
	 *
	 * @param evaluationTime the current evaluation time
	 * @param possSysStates the possible system states that was derived from the EOM system model
	 * @param tsseIterationResult the TechnicalSystemStateEvaluation at the end of current iteration process between transformer and power flow calculation. Will be <code>null</code> in a new time step.
	 * @return has to return the TranformerEvaluationState containing the selected system state and the {@link DiscreteSystemStateType}
	 */
	public abstract TransformerEvaluationState getTranformerEvaluationState(long evaluationTime, Vector<TechnicalSystemStateEvaluation> possSysStates, TechnicalSystemStateEvaluation tsseIterationResult);
	
	// ----------------------------------------------------------------------------------
	// --- Result type definition for transformer states --------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * The Class TransformerEvaluationState serves as answer type for sub strategies.
	 */
	public class TransformerEvaluationState {
		
		private DiscreteSystemStateType discreteSystemStateType;
		private TechnicalSystemStateEvaluation tsse;
		
		/**
		 * Instantiates a new transformer evaluation state.
		 *
		 * @param tsseSelected the system state to use as answer for the current step from a sub class.
		 * @param discreteSystemStateType the discrete system state type to be used  
		 */
		public TransformerEvaluationState(TechnicalSystemStateEvaluation tsseSelected, DiscreteSystemStateType discreteSystemStateType) {
			this.setTechnicalSystemStateEvaluation(tsseSelected);
			this.setDiscreteSystemStateType(discreteSystemStateType);
		}

		public DiscreteSystemStateType getDiscreteSystemStateType() {
			return discreteSystemStateType;
		}
		public void setDiscreteSystemStateType(DiscreteSystemStateType discreteSystemStateType) {
			this.discreteSystemStateType = discreteSystemStateType;
		}

		public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
			return tsse;
		}
		public void setTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsse) {
			this.tsse = tsse;
		}
	}

	
	// ----------------------------------------------------------------------------------
	// --- Translation methods for easier decisions and debugging -----------------------
	// ----------------------------------------------------------------------------------
	/**
	 * The Class TransformerSetting.
	 */
	public class TransformerSetting {
		
		private TechnicalSystemStateEvaluation systemState;
		
		private Integer tapPos;
		private double voltageReal;
		private double voltageImag;
		private Double voltageAbs;
		
		private Double voltageDeltaRealToNominalLevel;
		private Double voltageDeltaRealToGeneralBoundaries;
		private Double voltageDeltaRealToControlNodeBoundaries;
		private Double voltageDeltaRealToControlCharacteristics;
		
		/**
		 * Instantiates a new setting tried.
		 *
		 * @param tsse the TechnicalSystemStateEvaluation
		 * @param tapPos the tap position
		 * @param voltageReal the voltage real
		 * @param voltageImag the voltage imaginary
		 * @param generalVoltageDeltaReal the general voltage delta real
		 * @param controlNodeVoltageDelta the control node voltage delta
		 */
		public TransformerSetting(TechnicalSystemStateEvaluation tsse, Integer tapPos, double voltageReal, double voltageImag, double voltageAbs, double nominalVoltageDeltaReal, double generalVoltageDeltaReal, double controlNodeVoltageDelta, double voltageDeltaRealToControlCharacteristic) {
			this.systemState = tsse;
			this.tapPos = tapPos;
			this.voltageReal = voltageReal;
			this.voltageImag = voltageImag;
			this.voltageAbs  = voltageAbs;
			this.voltageDeltaRealToNominalLevel = nominalVoltageDeltaReal;
			this.voltageDeltaRealToGeneralBoundaries = generalVoltageDeltaReal;
			this.voltageDeltaRealToControlNodeBoundaries = controlNodeVoltageDelta;
			this.voltageDeltaRealToControlCharacteristics = voltageDeltaRealToControlCharacteristic;
		}
		
		public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation () {
			return systemState;
		}
		
		public Integer getTapPos() {
			return tapPos;
		}
		
		public double getVoltageReal() {
			return voltageReal;
		}
		public double getVoltageImag() {
			return voltageImag;
		}
		public double getVoltageAbs() {
			return voltageAbs;
		}
		
		public Double getVoltageDeltaRealToNominalLevel() {
			return voltageDeltaRealToNominalLevel;
		}
		public Double getVoltageDeltaRealToGeneralBoundaries() {
			return voltageDeltaRealToGeneralBoundaries;
		}
		public Double getVoltageDeltaRealToControlNodeBoundaries() {
			return voltageDeltaRealToControlNodeBoundaries;
		}
		public Double getVoltageDeltaRealToControlCharacteristics() {
			return voltageDeltaRealToControlCharacteristics;
		}
		
		
		public Object getValueOfSystemVariable(String systemVariableID) {
			
			Object value = null;
			FixedVariable fv = TechnicalSystemStateHelper.getFixedVariable(this.getTechnicalSystemStateEvaluation().getIOlist(), systemVariableID);
			if (fv instanceof FixedBoolean) {
				value = ((FixedBoolean) fv).isValue();
			} else if (fv instanceof FixedInteger) {
				value = ((FixedInteger) fv).getValue();
			} else if (fv instanceof FixedDouble) {
				value = ((FixedDouble) fv).getValue();
			}
			return value;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object compObject) {
			
			if (compObject==null) return false;
			if (! (compObject instanceof TransformerSetting)) return false;
			
			TransformerSetting stComp = (TransformerSetting) compObject;
			if (stComp.getTapPos()!=this.getTapPos()) return false;
			return true;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			
			double hvVR = 0;
			Object hvVoltageReal = this.getValueOfSystemVariable(TransformerSystemVariable.hvVoltageRealAllPhases.name());
			if (hvVoltageReal instanceof Double) {
				hvVR = (double) hvVoltageReal;
			}
			
			String display = "[hvReal = " + NumberHelper.round(hvVR, 2) + " V] ";
			display += "TapPos: " + (this.getTapPos()>=0 ? " " + this.getTapPos() : this.getTapPos()) + ", ";
			display += "voltage real: " + NumberHelper.round(this.getVoltageReal(), 2) + " V, ";
			display += "voltage imag: " + NumberHelper.round(this.getVoltageImag(), 2) + " V, ";
			display += "voltage abs: " + NumberHelper.round(this.getVoltageAbs(), 2) + " V, ";
			display += "deltaV nominal: " + NumberHelper.round(this.getVoltageDeltaRealToNominalLevel(), 2) + " V, ";
			display += "deltaV general: " + NumberHelper.round(this.getVoltageDeltaRealToGeneralBoundaries(), 2) + " V, ";
			display += "deltaV control node: " + NumberHelper.round(this.getVoltageDeltaRealToControlNodeBoundaries(), 2) + "V, ";
			display += "deltaV control chrar.: " + NumberHelper.round(this.getVoltageDeltaRealToControlCharacteristics(), 2) + "";
			return display;
		}
	}

	/**
	 * Prints the specified transformer setting list if the local {@link #debug} variable is true.
	 * @param transformerSettingList the transformer setting list
	 */
	protected void debugPrintTransformerSettingList(List<TransformerSetting> transformerSettingList) {
		for (TransformerSetting ts : transformerSettingList) {
			System.out.println(ts);
		}
	}
	/**
	 * Prints the specified message to the console.
	 *
	 * @param printTime the print time. Can be <code>null</code>
	 * @param message the message to print
	 */
	protected void debugPrint(Long printTime, String message) {
		if (printTime==null) {
			System.out.println(message);
		} else {
			System.out.println("[" + DateTimeHelper.getTimeAsString(printTime) + "] " + message);
		}
	}
	/**
	 * Prints the specified technical system state for special purposes (feel free to adjust this method).
	 * @param tsse the {@link TechnicalSystemStateEvaluation}
	 */
	protected void debugPrintSystemState(TechnicalSystemStateEvaluation tsse) {

		boolean isError = false;
		
		DiscreteSystemStateType dsst = this.getDiscreteSystemStateType();
		String timeStamp = DateTimeHelper.getTimeAsString(tsse.getGlobalTime());

		TransformerSetting ts = this.getTransformerSettingFromSystemState(tsse);
		double nomVoltageLevel = this.getTransformerDataModel().getLowerVoltage_vmLV() * 1000.0;
		double relVoltageLevel = ts.getVoltageReal() / nomVoltageLevel * 100.0; 
		double roundPrecision = 3.0;
		
		// ----------------------------------------------------------
		// --- Get the residual load --------------------------------
		UsageOfInterfaceEnergy uoiEnergy = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), TransformerDataModel.HighVoltageUniPhase.HV_P.getInterfaceID());
		EnergyFlowInWatt efiwResLoad = UnitConverter.convertEnergyFlowInWatt(uoiEnergy.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3);		
		
		// --- Prepare system output --------------------------------
		StringBuilder sb = new StringBuilder();
		sb.append("[" + dsst.toString().subSequence(0, 5) + "] \t");
		sb.append(timeStamp + "\t");
		sb.append(ts.getTapPos() + "\t");

		// --- Print residual load and voltage level ----------------
		sb.append(DisplayHelper.getDisplayTextForAbstractEnergyFlowInWatt(efiwResLoad, false, 2) + "\t");
		sb.append(NumberHelper.round(ts.getVoltageReal(), roundPrecision) + " [V-real]\t");
		sb.append(NumberHelper.round(ts.getVoltageImag(), roundPrecision) + " [V-imag]\t");
		sb.append(NumberHelper.round(ts.getVoltageAbs(), roundPrecision) + " [V-abs]\t");
		sb.append(NumberHelper.round(relVoltageLevel, roundPrecision)  + " [% to Vn]\t");
		
		
		// ----------------------------------------------------------
		// --- Check state with respect to control characteristics --
		// ----------------------------------------------------------
		if (this.getTransformerDataModel().isControlBasedOnCharacteristics()==true) {
			
			double targetVoltageLevel = this.getTransformerDataModel().getTransformerCharacteristicsHandler().getTargetVoltageLevelForLowVoltageLoadInV(efiwResLoad.getValue());
			double targetVoltageLevelPercent = this.getTransformerDataModel().getTransformerCharacteristicsHandler().getVoltageToNominalVoltageLevelInPercent(efiwResLoad.getValue());
			
			double boundaryLevelPercent = this.getTransformerDataModel().getControlCharacteristicsAllowedDeviation();
			double boundaryStep 		= targetVoltageLevel * boundaryLevelPercent / 100;
			double upperBoundaryPercent = ((targetVoltageLevel + boundaryStep) / nomVoltageLevel) * 100.0;
			double lowerBoundaryPercent = ((targetVoltageLevel - boundaryStep) / nomVoltageLevel) * 100.0;
			
			// --- Prepare printing ---------------------------------
			relVoltageLevel = NumberHelper.round(relVoltageLevel, roundPrecision);
			upperBoundaryPercent = NumberHelper.round(upperBoundaryPercent, roundPrecision);
			lowerBoundaryPercent = NumberHelper.round(lowerBoundaryPercent, roundPrecision);
			boolean inRange = relVoltageLevel > lowerBoundaryPercent & relVoltageLevel < upperBoundaryPercent;
			
			// --- Reset String builder -----------------------------
			sb.setLength(0);
			sb.append("[" + dsst.toString().subSequence(0, 5) + "] \t");
			sb.append(timeStamp + "\t");
			sb.append(ts.getTapPos() + "\t");

			// --- Print residual load and voltage level ----------------
			sb.append(DisplayHelper.getDisplayTextForAbstractEnergyFlowInWatt(efiwResLoad, false, 2) + "\t");
			sb.append(NumberHelper.round(ts.getVoltageReal(), roundPrecision) + " [V]\t");
			sb.append(NumberHelper.round(targetVoltageLevel, roundPrecision)  + " [V-target]\t");

			sb.append(NumberHelper.round(relVoltageLevel, roundPrecision)  + " [%]\t");
			sb.append(NumberHelper.round(targetVoltageLevelPercent, roundPrecision) + " [%-target]\t");

			sb.append(lowerBoundaryPercent + " - " +  relVoltageLevel + " - " + upperBoundaryPercent +  " [%-Range lower/rel/upper]\t");
			if (inRange==true) {
				sb.append("in Range: " + inRange + " \t");
			} else {
				if (relVoltageLevel > upperBoundaryPercent) {
					sb.append("in Range: " + inRange + " HIGHER\t");
				} else if (relVoltageLevel < lowerBoundaryPercent) {
					sb.append("in Range: " + inRange + " LOWER\t");
					isError = true;
				}
			}
		}
		// ----------------------------------------------------------
		
		// --- Final iteration step ? -------------------------------
		if (dsst==DiscreteSystemStateType.Final) sb.append("\n");
		
		// ----------------------------------------------------------
		// --- Print out --------------------------------------------
		// ----------------------------------------------------------
		if (isError==true) {
			System.err.println(sb.toString());
		} else {
			if (this.isPrintIterationErrorsOnly==false) {
				System.out.println(sb.toString());
			}
		}
	}
	
	/**
	 * Returns the transformer settings from the collection of possible state.
	 * @param possSysStates the possible system states
	 * @return the transformer setting of possible state
	 */
	public List<TransformerSetting> getTransformerSettingFromPossibleSystemState(Vector<TechnicalSystemStateEvaluation> possSysStates) {
		
		List<TransformerSetting> transformerSettingList = new ArrayList<>();
		for (int i = 0; i < possSysStates.size(); i++) {
			TechnicalSystemStateEvaluation tsseTmp = possSysStates.get(i);
			TransformerSetting ts = this.getTransformerSettingFromSystemState(tsseTmp);
			transformerSettingList.add(ts);
		}
		// --- Sort list according to tap position --------
		Collections.sort(transformerSettingList, new Comparator<TransformerSetting>() {
			@Override
			public int compare(TransformerSetting ts1, TransformerSetting ts2) {
				return ts1.getTapPos().compareTo(ts2.getTapPos());
			}
		});
		return transformerSettingList;
	}
	/**
	 * Returns the transformer setting from the specified system state.
	 *
	 * @param tsse the {@link TechnicalSystemStateEvaluation}
	 * @return the voltage level from the system state
	 */
	public TransformerSetting getTransformerSettingFromSystemState(TechnicalSystemStateEvaluation tsse) {
		
		double currVoltageReal = 0.0;
		double currVoltageImag = 0.0;
		double currVoltageAbs  = 0.0;
		
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()) {
			// ----------------------------------------------------------------
			// --- Three phase power flow ------------------------------------- 
			// ----------------------------------------------------------------
			// --- Real part ----------------
			FixedDouble fdVoltageRealL1 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageRealL1.name());
			FixedDouble fdVoltageRealL2 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageRealL2.name());
			FixedDouble fdVoltageRealL3 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageRealL3.name());
			
			double currVoltageRealL1 = fdVoltageRealL1==null ? 0.0 : fdVoltageRealL1.getValue();
			double currVoltageRealL2 = fdVoltageRealL2==null ? 0.0 : fdVoltageRealL2.getValue();
			double currVoltageRealL3 = fdVoltageRealL3==null ? 0.0 : fdVoltageRealL3.getValue();
			
			double currVoltageRealLevelForOnePhase = 0.0;
			if (currVoltageRealL1==currVoltageRealL2 && currVoltageRealL1==currVoltageRealL3) {
				currVoltageRealLevelForOnePhase = currVoltageRealL1;
			} else {
				currVoltageRealLevelForOnePhase = (currVoltageRealL1 + currVoltageRealL2 + currVoltageRealL3) / 3.0;
			}
			
			// --- Imaginary part -----------
			FixedDouble fdVoltageImagL1 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageImagL1.name());
			FixedDouble fdVoltageImagL2 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageImagL2.name());
			FixedDouble fdVoltageImagL3 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageImagL3.name());
			
			double currVoltageImagL1 = fdVoltageImagL1==null ? 0.0 : fdVoltageImagL1.getValue();
			double currVoltageImagL2 = fdVoltageImagL2==null ? 0.0 : fdVoltageImagL2.getValue();
			double currVoltageImagL3 = fdVoltageImagL3==null ? 0.0 : fdVoltageImagL3.getValue();
			
			double currVoltageImagLevelForOnePhase;
			if (currVoltageImagL1==currVoltageImagL2 && currVoltageImagL1==currVoltageImagL3) {
				currVoltageImagLevelForOnePhase = currVoltageImagL1;
			} else {
				currVoltageImagLevelForOnePhase = (currVoltageImagL1 + currVoltageImagL2 + currVoltageImagL3) / 3.0;
			}
			
			// --- Calculate voltage levels for single phase --------------
			currVoltageReal = currVoltageRealLevelForOnePhase * Math.sqrt(3.0);
			currVoltageImag = currVoltageImagLevelForOnePhase * Math.sqrt(3.0);
			
		} else {
			// ----------------------------------------------------------------
			// --- Single phase power flow ------------------------------------
			// ----------------------------------------------------------------
			FixedDouble fdVoltageReal = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageRealAllPhases.name());
			FixedDouble fdVoltageImag = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.lvVoltageImagAllPhases.name());
			
			currVoltageReal = fdVoltageReal==null ? 0.0 : fdVoltageReal.getValue();
			currVoltageImag = fdVoltageImag==null ? 0.0 : fdVoltageImag.getValue();
			
		}
		
		// --- Calculate absolute voltage level -------------------------------
		currVoltageAbs = Math.pow(Math.pow(currVoltageReal, 2.0) + Math.pow(currVoltageImag, 2.0), 0.5);
		
		// --- Calculate voltage deltas ---------------------------------------
		double dvNominal = currVoltageAbs - this.getTransformerLowVoltageLevel();
		double dVGeneralBoundaries = this.getVoltageDeltaToGeneralBoundaries(currVoltageAbs);
		double dvControlNode = this.getVoltageDeltaToControlNodeBoundaries(currVoltageAbs);
		double dvControlChar = 0.0;
		
		// --- Consider transformer characteristics? --------------------------
		if (this.getTransformerDataModel().isControlBasedOnCharacteristics()==true) {
			// --- Get LV power flow ------------------------------------------
			TransformerPower lvTpInkW = this.getLowVoltagePowerFlowInKW(tsse);
			double targetVoltageLevel = this.getTransformerDataModel().getTransformerCharacteristicsHandler().getTargetVoltageLevelForLowVoltageLoadInV(lvTpInkW.getActivePower());
			dvControlChar = targetVoltageLevel - currVoltageAbs;
		}
		
		// --- Return the voltage level at once -------------------------------
		return new TransformerSetting(tsse, this.getTapPositionFromSystemState(tsse), currVoltageReal, currVoltageImag, currVoltageAbs, dvNominal, dVGeneralBoundaries, dvControlNode, dvControlChar);
	}
	
	/**
	 * Return the low voltage power flow in KW from the specified system state.
	 *
	 * @param tsse the system state to look in
	 * @return the low voltage power flow in KW
	 */
	protected TransformerPower getLowVoltagePowerFlowInKW(TechnicalSystemStateEvaluation tsse) {
	
		TransformerPower lvTransformerPower = null;
		if (this.getTransformerDataModel().isLowerVoltage_ThriPhase()) {
			// ----------------------------------------------------------------
			// --- Three phase power flow ------------------------------------- 
			// ----------------------------------------------------------------
			UsageOfInterfaceEnergy uoiLV_P_L1 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageThriPhase.LV_P1.getInterfaceID());
			UsageOfInterfaceEnergy uoiLV_P_L2 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageThriPhase.LV_P2.getInterfaceID());
			UsageOfInterfaceEnergy uoiLV_P_L3 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageThriPhase.LV_P3.getInterfaceID());
			double efiwLV_P_L1_Value = UnitConverter.convertEnergyFlowInWatt(uoiLV_P_L1.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			double efiwLV_P_L2_Value = UnitConverter.convertEnergyFlowInWatt(uoiLV_P_L2.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			double efiwLV_P_L3_Value = UnitConverter.convertEnergyFlowInWatt(uoiLV_P_L3.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			
			UsageOfInterfaceEnergy uoiLV_Q_L1 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageThriPhase.LV_Q1.getInterfaceID());
			UsageOfInterfaceEnergy uoiLV_Q_L2 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageThriPhase.LV_Q2.getInterfaceID());
			UsageOfInterfaceEnergy uoiLV_Q_L3 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageThriPhase.LV_Q3.getInterfaceID());
			double efiwLV_Q_L1_Value = UnitConverter.convertEnergyFlowInWatt(uoiLV_Q_L1.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			double efiwLV_Q_L2_Value = UnitConverter.convertEnergyFlowInWatt(uoiLV_Q_L2.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			double efiwLV_Q_L3_Value = UnitConverter.convertEnergyFlowInWatt(uoiLV_Q_L3.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			
			// --- Calculate the low voltage power flow -----------------------
			double lvLoadP_in_KW  = efiwLV_P_L1_Value + efiwLV_P_L2_Value + efiwLV_P_L3_Value;;
			double lvLoadQ_in_KW = efiwLV_Q_L1_Value + efiwLV_Q_L2_Value + efiwLV_Q_L3_Value;;
			lvTransformerPower = new TransformerPower(lvLoadP_in_KW, lvLoadQ_in_KW);
			
		} else {
			// ----------------------------------------------------------------
			// --- Single phase power flow ------------------------------------
			// ----------------------------------------------------------------
			UsageOfInterfaceEnergy uoiLV_P_L1 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageUniPhase.LV_P.getInterfaceID());
			double lvLoadP_in_KW = UnitConverter.convertEnergyFlowInWatt(uoiLV_P_L1.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();

			UsageOfInterfaceEnergy uoiLV_Q_L1 = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), LowVoltageUniPhase.LV_Q.getInterfaceID());
			double lvLoadQ_in_KW = UnitConverter.convertEnergyFlowInWatt(uoiLV_Q_L1.getEnergyFlow(), EnergyUnitFactorPrefixSI.KILO_K_3).getValue();
			
			lvTransformerPower = new TransformerPower(lvLoadP_in_KW, lvLoadQ_in_KW);
			
		}
		return lvTransformerPower;
	}
	
	/**
	 * Returns the tap position from the specified system state.
	 *
	 * @param tsse the {@link TechnicalSystemStateEvaluation}
	 * @return the tap position from the system state
	 */
	private Integer getTapPositionFromSystemState(TechnicalSystemStateEvaluation tsse) {
		FixedInteger fIntTapPos = (FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.tapPos.name());
		return fIntTapPos==null ? null : fIntTapPos.getValue();
	}

	
	// ----------------------------------------------------------------------------------
	// --- Filter methods for lists of TransformerSettings ------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Returns the TransformerSetting with lowest distance to the nominal voltage level.
	 * @param tsList the TransformerSetting list to check
	 * @return the transformer setting with lowest distance to nominal voltage level
	 */
	public TransformerSetting getTransformerSettingWithLowestDistanceToNominalVoltageLevel(List<TransformerSetting> tsList) {
		
		TransformerSetting ts = null;
		if (tsList.size()>0) {
			if (tsList.size()>1) {
				// --- Several elements were found that matches the control node boundaries -------
				Collections.sort(tsList, new Comparator<TransformerSetting>() {
					@Override
					public int compare(TransformerSetting ts1, TransformerSetting ts2) {
						Double vdNom1 = Math.abs(ts1.getVoltageDeltaRealToNominalLevel());
						Double vdNom2 = Math.abs(ts2.getVoltageDeltaRealToNominalLevel());
						return vdNom1.compareTo(vdNom2);
					}
				});
			}
			// --- Select the first element -------------------------------------------------------
			ts = tsList.get(0);
		}
		return ts;
	}
	
	/**
	 * Returns the transformer setting with lowest distance to the specified voltage level.
	 *
	 * @param tsList the TransformerSetting list to check
	 * @param targetVoltageLevel the target voltage level
	 * @return the transformer setting with lowest distance to specified voltage level
	 */
	public TransformerSetting getTransformerSettingWithLowestDistanceToSpecifiedVoltageLevel(List<TransformerSetting> tsList, final double targetVoltageLevel) {
		
		TransformerSetting ts = null;
		if (tsList.size()>0) {
			if (tsList.size()>1) {
				// --- Several elements were found that matches the control node boundaries -------
				Collections.sort(tsList, new Comparator<TransformerSetting>() {
					@Override
					public int compare(TransformerSetting ts1, TransformerSetting ts2) {
						Double vdNom1 = Math.abs(ts1.getVoltageReal() - targetVoltageLevel);
						Double vdNom2 = Math.abs(ts2.getVoltageReal() - targetVoltageLevel);
						return vdNom1.compareTo(vdNom2);
					}
				});
			}
			// --- Select the first element -------------------------------------------------------
			ts = tsList.get(0);
		}
		return ts;
	}
	
	/**
	 * Returns the list of TransformerSetting that are in the range of the general voltage boundaries.
	 *
	 * @param possSysStates the possible system* states
	 * @return the transformer state by sufficient voltage level
	 */
	public List<TransformerSetting> getTransformerSettingByGeneralVoltageBoundaries(List<TransformerSetting> possSysStates) {
		List<TransformerSetting> sufficientStates = new ArrayList<>();
		for (int i = 0; i < possSysStates.size(); i++) {
			TransformerSetting ts = possSysStates.get(i);
			if (ts.getVoltageDeltaRealToGeneralBoundaries()==0) {
				sufficientStates.add(ts);
			}
		}
		return sufficientStates;
	}
	/**
 	 * Returns the list of TransformerSetting that are in the range of the control node boundaries.
 	 * 
	 * @param possSysStates the possible system* states
	 * @return the transformer state by sufficient voltage level
	 */
	public List<TransformerSetting> getTransformerSettingByControlNodeBoundaries(List<TransformerSetting> possSysStates) {
		List<TransformerSetting> sufficientStates = new ArrayList<>();
		for (int i = 0; i < possSysStates.size(); i++) {
			TransformerSetting ts = possSysStates.get(i);
			if (ts.getVoltageDeltaRealToControlNodeBoundaries()==0) {
				sufficientStates.add(ts);
			}
		}
		return sufficientStates;
	}
	
}
