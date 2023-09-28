package de.enflexit.ea.electricity.transformer.strategies;

import java.util.List;
import java.util.Vector;

import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.electricity.transformer.InternalDataModel;
import de.enflexit.ea.electricity.transformer.TransformerAgent;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class TransformerControlStrategyRT.
 */
public class TransformerControlStrategyRT extends AbstractEvaluationStrategyRT {
	
	private boolean debug = false;

	private TransformerAgent transformerAgent;
	private InternalDataModel internalDataModel;
	private TransformerDataModel transformerDataModel;
	
	private double nominalSlackVoltage= 230.94;
	
	//activationTime to wait after the need for an action has been detected.
	private long activationTime = 3000;
	private long activationEndTime = 0;
	private boolean activationTimeRunning = false;
	
	//delayTime to wait after a step is changed
	private long delayTime = 3000;		//Delay Time in Milliseconds (delayTime should be at least 2s to avoid oscillation)
	private long delayEndTime = 0;
	
	/**
	 * Instantiates a new droop control strategy RT.
	 * @param optionModelController the option model controller
	 */
	public TransformerControlStrategyRT(OptionModelController optionModelController) {
		super(optionModelController);
	}

	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategyRT#getInitialStateAdaption()
	 */
	@Override
	public InitialStateAdaption getInitialStateAdaption() {
		return InitialStateAdaption.TemporalMoveToStateDurationsEnd;
	}
	
	/**
	 * Return the current transformer agent.
	 * @return the transformer agent
	 */
	private TransformerAgent getTransformerAgent() {
		if (transformerAgent==null) {
			transformerAgent = (TransformerAgent) this.optionModelController.getControllingAgent();
		}
		return transformerAgent;
	}
	/**
	 * Returns the internal data model of the BatteryAgent.
	 * @return the internal data model
	 */
	private InternalDataModel getInternalDataModel() {
		if (internalDataModel==null && this.getTransformerAgent()!=null) {
			internalDataModel = this.getTransformerAgent().getInternalDataModel();
		}
		return internalDataModel;
	}
	/**
	 * Return the transformer data model.
	 * @return the transformer data model
	 */
	public TransformerDataModel getTransformerDataModel() {
		if (transformerDataModel==null) {
			SystemVariableDefinitionStaticModel sysVarDefStaticModel = (SystemVariableDefinitionStaticModel) this.optionModelController.getSystemVariableDefinition(this.optionModelController.getTechnicalSystem().getSystemVariables(), "StaticParameters");
			transformerDataModel = (TransformerDataModel) this.optionModelController.getStaticModelInstance(sysVarDefStaticModel);
		}
		return transformerDataModel;
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {

		double slackVoltage = this.nominalSlackVoltage;
		
		// --- Check if CNP for transformer step is ready
		if (this.getInternalDataModel()!=null && this.getInternalDataModel().isNewSetPointFromCNP()==true) {
			this.getInternalDataModel().setNewSetPointFromCNP(false);
			
		} else {
				
			// --- Search by walking through time ---------------------------------
			TechnicalSystemStateEvaluation tsse = this.getTechnicalSystemStateEvaluation();
//			String ioListValues = TechnicalSystemStateHelper.toString(tsse.getIOlist(), ", ");
//			DisplayHelper.systemOutPrintlnGlobalTime(this.getEvaluationEndTime(), "[" + this.getClass().getSimpleName() + "]", "Evaluate with " + ioListValues);

			while (tsse.getGlobalTime() < this.getEvaluationEndTime()) {
				
				// --- Get all possible subsequent steps and states ---------------
				Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse);
				if (deltaSteps.size() == 0) {
					System.err.println("No further delta steps possible => interrupt search!");
					break;
				}
				
				// --- Decide for the next system state ---------------------------
				TechnicalSystemStateDeltaEvaluation tssDeltaDecision = null;
				
				//--- get actual transformer Step
				FixedInteger oldTransformerStepFI = (FixedInteger) this.getFixedVariableByID(tsse.getIOlist(), TransformerSystemVariable.tapPos.name());
				int oldTransformerStep = oldTransformerStepFI.getValue();
				
				// --- Should the old State remain?
				boolean useOldState = false;

				if (tsse.getGlobalTime()>=this.delayEndTime) {

					// --- AGENT DECISIONs: Let's see how to decide ---------------
					// --- Get necessary voltage adjustment for example from district agent
					ElectricalMeasurement currentMeasurement = this.getTransformerAgent().getInternalDataModel().getSensorMeasurement();
					if (currentMeasurement!=null) {
						
						// --- Calculate average voltage ---------------------
						TriPhaseElectricalNodeState nodeState = currentMeasurement.getElectricalNodeState();
						if (nodeState != null) {
							float u1 = nodeState.getL1().getVoltageAbs().getValue();
							float u2 = nodeState.getL2().getVoltageAbs().getValue();
							float u3 = nodeState.getL3().getVoltageAbs().getValue();
							
							float uAverage = (u1+u2+u3)/3;
							float uDevAbsolut = (float) (uAverage-230.94);
							
							// --- Get actual voltage adjustment by actual transformer step
							float voltageAdjustment = (float) this.slackVoltageAdjustmentByTransformerStep(oldTransformerStep);
							
							// --- Add  Voltage adjustment to actual deviation
							float uDev=uDevAbsolut-voltageAdjustment;
							
							// --- 1. Find setpoint on basis of voltage deviation
							int necessaryTransformerStep=this.evaluateActualOperationState(uDev);
							
							// --- Check if Adjustment of transformer step is recommended
							if (oldTransformerStep != necessaryTransformerStep) {
								// --- ActivationTime has expired -> Adjust transformer step
								if (this.activationTimeRunning && tsse.getGlobalTime() >= this.activationEndTime) {
									
									// --- 2. Realize selected transformer Step
									tssDeltaDecision= this.realizeOperationState(necessaryTransformerStep, deltaSteps);
									// --- 3. Calculate slack voltage
									slackVoltage = calculateSlackVoltage(necessaryTransformerStep);
									// --- Set new tssDeltaDecision
									if (this.debug == true) {
										System.out.println(this.getTransformerAgent().getLocalName() + ":  voltage deviation with step " + uDev + ":  voltage deviation without transformer" + uDevAbsolut + ", choosing transformer step: "+ necessaryTransformerStep+ ", slackVoltage: "+ slackVoltage);
									}
									//Calculate time for next control process
									if (necessaryTransformerStep != oldTransformerStep) {
										this.delayEndTime = tsse.getGlobalTime() + this.delayTime;
									}
									
								} else if(!this.activationTimeRunning) {
									this.activationTimeRunning = true;
									this.activationEndTime = tsse.getGlobalTime() + this.activationTime;
									useOldState = true;
									
								} else {
									useOldState = true;
								}
								
							} else {
								// --- Turn activationTimeRunning off
								this.activationTimeRunning = false;
								useOldState = true;
							}
							
						} else {
							// --- No data for a qualified decision, choosing idle -------------------
							int necessaryTransformerStep=0;
							slackVoltage= this.nominalSlackVoltage;
							tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
							if (this.debug == true) {
								System.err.println(this.getTransformerAgent().getLocalName() + ": No sensor data available - cannot make a qualified decission!");
							}
						}
						
					} else {
						if (this.debug==true) {
							System.err.println(this.getTransformerAgent().getLocalName() + ": Invalid sensor data, electricalNodeState==null!");
						}
						useOldState = true;
					} // --- end of current !=null
					
				} else { 
					//delayEndTime not reached
					useOldState = true;
				}
				
				if (useOldState==true) {
					// --- 2. Realize selected transformer Step
					tssDeltaDecision= this.realizeOperationState(oldTransformerStep, deltaSteps);
					// --- 3. Calculate slack voltage
					slackVoltage = calculateSlackVoltage(oldTransformerStep);
					// --- Set new tssDeltaDecision
					if (this.debug == true) {
						System.out.println(this.getTransformerAgent().getLocalName() + ":  No control of the transformator state necessary.");
					}
				}
				
				// --- 
				if (tssDeltaDecision==null) {
					System.err.println("[" + this.getClass().getSimpleName() + "]: No valid subsequent state found!");
					break;
				}
				
				// --- Set new current TechnicalSystemStateEvaluation -------------
				TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaDecision);
				if (tsseNext == null) {
					System.err.println("Error while using selected delta => interrupt search!");
					break;
				} else {
					// --- Set next state as new current state --------------------
					tsse = tsseNext;
				}
				this.setTechnicalSystemStateEvaluation(tsse);
				this.setIntermediateStateToResult(tsse);
				 
			}// end while
		}// end if
		
	}
	
	/**
	 * This method calculates the actual slackVoltage adjustment on basis of the actual transformer step
	 * @param transformerStep
	 * @return
	 */
	private double slackVoltageAdjustmentByTransformerStep(int transformerStep) {
		double slackVoltageAdjustment =0;
		
		switch(transformerStep) {
		case 0:
			slackVoltageAdjustment=0;
			break;
		case 1: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(0.025);
			break;
		case 2: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(0.05);
			break;
		case 3: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(0.075);
			break;
		case 4: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(0.1);
			break;
		case -1: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(-0.025);
			break;
		case -2: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(-0.05);
			break;
		case -3: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(-0.075);
			break;
		case -4: 
			slackVoltageAdjustment= this.nominalSlackVoltage*(-0.1);
			break;
		default:
			slackVoltageAdjustment=0;
			break;
		}
		
		return slackVoltageAdjustment;
	}
	
	private TechnicalSystemStateDeltaEvaluation realizeOperationState (int necessaryTransformerStep, Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps) {
		TechnicalSystemStateDeltaEvaluation tssDeltaDecision = null;
		double slackVoltage=0;
		
		switch (necessaryTransformerStep) {
		
		case 0: // Default State
			if (this.debug==true) {
				System.out.println("Default State");
			}
			slackVoltage= this.nominalSlackVoltage;
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case 1: // +2.5%
			if (this.debug==true) {
				System.out.println("Slack Voltage +2.5%");
			}
			slackVoltage= this.nominalSlackVoltage*(1+2.5/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case 2: // +5%
			if (this.debug==true) {
				System.out.println("Slack Voltage +5%");
			}
			slackVoltage= this.nominalSlackVoltage*(1+5/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case 3: // +7.5%
			if (this.debug==true) {
				System.out.println("Slack Voltage +7.5%");
			}
			slackVoltage= this.nominalSlackVoltage*(1+7.5/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case 4: // 10%
			if (this.debug==true) {
				System.out.println("Slack Voltage +10%");
			}
			slackVoltage= this.nominalSlackVoltage*(1+10/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case -1: // -2.5%
			if (this.debug==true) {
				System.out.println("Slack Voltage -2.5%");
			}
			slackVoltage= this.nominalSlackVoltage*(1-2.5/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;	
		case -2: // -5%
			if (this.debug==true) {
				System.out.println("Slack Voltage -5%");
			}
			slackVoltage= this.nominalSlackVoltage*(1-5/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case -3: // -7.5%
			if (this.debug==true) {
				System.out.println("Slack Voltage -7.5%");
			}
			slackVoltage= this.nominalSlackVoltage*(1-7.5/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		case -4: // -10%
			if (this.debug==true) {
				System.out.println("Slack Voltage -10%");
			}
			slackVoltage= this.nominalSlackVoltage*(1-10/100);
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
			break;
		default:
			System.err.println("[" + this.getClass().getSimpleName() + "] No Operation Point found!");
			necessaryTransformerStep=0;
			slackVoltage= this.nominalSlackVoltage;
			tssDeltaDecision = this.getDeltaStepForSystemState(deltaSteps, necessaryTransformerStep, slackVoltage);
	} // --- End of switch Case
		return tssDeltaDecision;
	}
	
	private double calculateSlackVoltage(int transfromerStep) {
double slackVoltage=0;
		
		switch (transfromerStep) {
		
		case 0: // Default State
			slackVoltage= this.nominalSlackVoltage;
			break;
		case 1: // +2.5%
			slackVoltage= this.nominalSlackVoltage*(1.025);
			break;
		case 2: // +5%
			slackVoltage= this.nominalSlackVoltage*(1.05);
			break;
		case 3: // +7.5%
			slackVoltage= this.nominalSlackVoltage*(1.075);
			break;
		case 4: // 10%
			slackVoltage= this.nominalSlackVoltage*(1.1);
			break;
		case -1: // -2.5%
			slackVoltage= this.nominalSlackVoltage*(0.975);
			break;	
		case -2: // -5%
			slackVoltage= this.nominalSlackVoltage*(0.95);
			break;
		case -3: // -7.5%
			slackVoltage= this.nominalSlackVoltage*(0.925);
			break;
		case -4: // -10%
			slackVoltage= this.nominalSlackVoltage*(0.9);
			break;
		default:
			slackVoltage= this.nominalSlackVoltage;
	} // --- End of switch Case
		return slackVoltage;
	}
	/**
	 * This method evaluate the necessary OperationState of basis of the voltage deviation
	 * @param uDev
	 * @return
	 */
	private int evaluateActualOperationState(double uDev) {
		int selectedOperationState=0;
		
		// --- Check if transformer should reduce or increase slack voltage
		if (uDev> 0){
			// ---Reduce slack voltage
			if (uDev> 10*230.94/100){
				// -10% step 
				selectedOperationState=-4;
			}
			else if (uDev>7.5*230.94/100) {
				// -7.5% step 
				selectedOperationState=-3;
			}
			else if (uDev>5*230.94/100) {
				// -5% step 
				selectedOperationState=-2;
				
			}
			else if (uDev>2.5*230.94/100) {
				// -2.5% step 
				selectedOperationState=-1;
			}
			
		}
		else {
			// --- Increase slack Voltage
			if (Math.abs(uDev)> 10*230.94/100){
				// +10% step 
				selectedOperationState=4;
			}
			else if (Math.abs(uDev)>7.5*230.94/100) {
				// +7.5% step 
				selectedOperationState=3;
			}
			else if (Math.abs(uDev)>5*230.94/100) {
				// +5% step 
				selectedOperationState=2;
				
			}
			else if (Math.abs(uDev)>2.5*230.94/100) {
				// +2.5% step 
				selectedOperationState=1;
			}
		}
			
		
	 return selectedOperationState;
	}
	
	/**
	 * Gets the delta step that leads to the desired system state.
	 * @param deltaSteps the list of possible delta steps
	 * @param systemState the id of the desired system state
	 * @return the delta step leading to the state, or null if not found
	 */
	private TechnicalSystemStateDeltaEvaluation getDeltaStepForSystemState(Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps, int actualtTransformerStep, double slackVoltage) {
		
		for (int i = 0; i < deltaSteps.size(); i++) {
			
			TechnicalSystemStateDeltaEvaluation deltaStep = deltaSteps.get(i);
			TechnicalSystemStateEvaluation tsse = deltaStep.getTechnicalSystemStateEvaluation();
			
			// --- Get fixed doubles from io list of tsse --------------
			FixedInteger transformerStepFI = (FixedInteger) this.getFixedVariableByID(tsse.getIOlist(), TransformerSystemVariable.tapPos.name());
			
			if (transformerStepFI!=null) {
				// --- Find right delta step corresponding to setpoints
				if (actualtTransformerStep==transformerStepFI.getValue()) {
					return deltaStep;
				}
			}
		}
		return null;
	}
	

	/**
	 * get fixed Variable by variable id from io list
	 * @param ioList
	 * @param variableID
	 * @return
	 */
	private FixedVariable getFixedVariableByID (List<FixedVariable> ioList, String variableID) {
		
		for (int i=0; i<ioList.size();i++) {
			if(variableID.equals(ioList.get(i).getVariableID())) {
				return ioList.get(i);
			}
		}
		return null;
	}


}

