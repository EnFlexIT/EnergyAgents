package de.enflexit.ea.core.simulation.validation;

import org.awb.env.networkModel.NetworkComponent;

import agentgui.simulationService.time.TimeModelContinuous;
import agentgui.simulationService.time.TimeModelDiscrete;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.SnapshotDecisionLocation;
import de.enflexit.ea.core.simulation.decisionControl.AbstractCentralDecisionProcess;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.AbstractSnapshotStrategy;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energygroup.GroupController;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import energygroup.evaluation.AbstractGroupSnapshotStrategy;

/**
 * The Class StrategyClassCheck check each {@link TechnicalSystem} or a {@link TechnicalSystemGroup} for the right
 * default evaluation strategy class that should match with the simulation type in the {@link HyGridAbstractEnvironmentModel}.
 * This follows the case separation, e.g. for discrete simulations (e.g. snapshot simulations).
 * 
 * @author Christian Derksen - SOFTEC - University of Duisburg-Essen
 */
public class StrategyClassCheck extends HyGridValidationAdapter {

	private OptionModelController omc;
	private GroupController gc;
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateHyGridAbstractEnvironmentModel(de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel)
	 */
	@Override
	public HyGridValidationMessage validateHyGridAbstractEnvironmentModel(HyGridAbstractEnvironmentModel absEnvModel) {

		// -------------------------------------------------------------------------------
		// --- Check the central decision class here -------------------------------------
		// -------------------------------------------------------------------------------
		MessageType msgType = MessageType.Error;
		String msg = "Class definition error for central decision process!";
		String msgDescription = null;
		
		boolean isDiscreteSimulation = (this.getTimeModel() instanceof TimeModelDiscrete);
		boolean isSnapShotSimulation = absEnvModel.isDiscreteSnapshotSimulation();
		boolean isCentralDecsion = absEnvModel.getSnapshotDecisionLocation()==SnapshotDecisionLocation.Central;
		if (isDiscreteSimulation==true && isSnapShotSimulation && isCentralDecsion==true) {
			// --- Check the specified class for central decisions ----------------------
			String decisionProcessClass = absEnvModel.getSnapshotCentralDecisionClass();
			if (decisionProcessClass==null) {
				msgDescription = "No class was defined for the central decision process.";
			} else {
				// --- Try to create an instance of the decision process ----------------
				AbstractCentralDecisionProcess decisionProcess = null;
				try {
					decisionProcess = AbstractCentralDecisionProcess.createCentralDecisionProcess(decisionProcessClass);
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
				if (decisionProcess==null) {
					msgDescription = "The currently spcified class '" + decisionProcessClass + "' for the central decision process could not be initiated.";
				}
			}
		}
		
		HyGridValidationMessage hvm = null;
		if (msgDescription!=null) {
			hvm = new HyGridValidationMessage(msg, msgType);
			hvm.setDescription(msgDescription);
		}
		return hvm;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateEomTechnicalSystem(org.awb.env.networkModel.NetworkComponent, energy.optionModel.TechnicalSystem)
	 */
	@Override
	public HyGridValidationMessage validateEomTechnicalSystem(NetworkComponent netComp, TechnicalSystem ts) {
		String evalStrategyClass = ts.getEvaluationSettings().getEvaluationClass();
		this.getOptionModelController().setTechnicalSystem(ts);
		return this.getValidationMessage(netComp, evalStrategyClass, false);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateEomTechnicalSystemGroup(org.awb.env.networkModel.NetworkComponent, energy.optionModel.TechnicalSystemGroup)
	 */
	@Override
	public HyGridValidationMessage validateEomTechnicalSystemGroup(NetworkComponent netComp, TechnicalSystemGroup tsg) {
		String evalStrategyClass = tsg.getTechnicalSystem().getEvaluationSettings().getEvaluationClass();
		this.getGroupController().setTechnicalSystemGroup(tsg);
		return this.getValidationMessage(netComp, evalStrategyClass, true);
	}

	/**
	 * Returns the private OptionModelController that provides common help functions.
	 * @return the option model controller
	 */
	private OptionModelController getOptionModelController() {
		if (omc==null) {
			omc = new OptionModelController();
		}
		return omc;
	}
	/**
	 * Returns the private GroupController.
	 * @return the group controller
	 */
	private GroupController getGroupController() {
		if (gc==null) {
			gc = new GroupController();
		}
		return gc;
	}
	
	/**
	 * Gets the validation message for the specified evaluation class.
	 *
	 * @param netComp the current NetworkComponent to check for
	 * @param evalStrategyClassName the evaluation strategy class name
	 * @param isAggregation the indicator that the system to check is an aggregation (a {@link TechnicalSystemGroup})
	 * @return the validation message
	 */
	private HyGridValidationMessage getValidationMessage(NetworkComponent netComp, String evalStrategyClassName, boolean isAggregation) {
		
		String netCompDescription = "network component '" + netComp.getId() + "' (" + netComp.getType() + ")";

		String msg = "EOM-strategy definition error for " + netCompDescription;
		String msgDescription = null;
		MessageType msgType = MessageType.Error; // --- default ---
		
		if (evalStrategyClassName==null || evalStrategyClassName.isEmpty()==true) {
			// --- No evaluation class could be found -----------------------------------
			msgDescription = "No default evaluation strategy could be found for the EOM model of " + netCompDescription + ".";
			
		} else {
			// --- Get instance of strategy ---------------------------------------------
			AbstractEvaluationStrategy strategy = null;
			if (isAggregation==false) {
				strategy = this.getOptionModelController().createEvaluationStrategy(evalStrategyClassName);
			} else {
				strategy = this.getGroupController().getGroupOptionModelController().createEvaluationStrategy(evalStrategyClassName);
			}
			
			if (strategy==null) {
				// --- Error while initiating strategy class ----------------------------
				msgDescription = "Error while initiating strategy class '" + evalStrategyClassName + "' for " + netCompDescription + ". See console for further information.";
				
			} else {
				// ----------------------------------------------------------------------
				// --- Check with respect to the current simulation type ----------------
				// ----------------------------------------------------------------------
				
				// ------------------------------------
				// --- => Continuous Simulations <= --- 
				boolean isContinuousSimulation = (this.getProject().getTimeModelController().getTimeModel() instanceof TimeModelContinuous);
				if (isContinuousSimulation==true) {
					return this.getValidationMessageForContinuousSimulation(netCompDescription, msg, strategy, isAggregation);
				}
				
				// ------------------------------------
				// --- => Discrete Simulations <= -----
				boolean isSnapShotSimulation = this.getHyGridAbstractEnvironmentModel().isDiscreteSnapshotSimulation();
				if (isSnapShotSimulation==false) {
					return this.getValidationMessageForDiscreteSimulation(netCompDescription, msg, strategy, isAggregation);
				}
				
				// ------------------------------------
				// --- => Snapshot Simulations <= -----
				return this.getValidationMessageForSnapshotSimulation(netCompDescription, msgDescription, strategy, isAggregation);
				
			}
		}
		return this.getHyGridValidationMessage(msg, msgDescription, msgType);
	}

	/**
	 * Gets the validation message for continuous simulation.
	 *
	 * @param netCompDescription the network component description
	 * @param msg the short message
	 * @param strategy the strategy
	 * @param isAggregation the is aggregation
	 * @return the validation message for continuous simulation
	 */
	private HyGridValidationMessage getValidationMessageForContinuousSimulation(String netCompDescription, String msg, AbstractEvaluationStrategy strategy, boolean isAggregation) {
		
		String msgDescription = null;
		MessageType msgType = MessageType.Error;
		
		if (isAggregation==false) {
			// ------------------------------------------------------------------
			// --- Check for TechnicalSystem instances --------------------------
			// ------------------------------------------------------------------
			if (strategy instanceof AbstractSnapshotStrategy) {
				msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is a snapshot strategy and can not be used for real time purposes!";
			} else {
				if (!(strategy instanceof AbstractEvaluationStrategyRT)) {
					msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
					msgType = MessageType.Information;
				}
			}
			
		} else {
			// ------------------------------------------------------------------
			// --- Check for TechnicalSystemGroup instances ---------------------
			// ------------------------------------------------------------------
			if (strategy instanceof AbstractGroupSnapshotStrategy) {
				msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is a snapshot strategy and can not be used for real time purposes!";
			} else {
				if (!(strategy instanceof AbstractGroupEvaluationStrategyRT)) {
					msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
					msgType = MessageType.Information;
				}
			}
		}
		return this.getHyGridValidationMessage(msg, msgDescription, msgType);
	}
	
	/**
	 * Gets the validation message for discrete simulation.
	 *
	 * @param netCompDescription the the network component description
	 * @param msg the short message
	 * @param strategy the strategy
	 * @param isAggregation the is aggregation
	 * @return the validation message for discrete simulation
	 */
	private HyGridValidationMessage getValidationMessageForDiscreteSimulation(String netCompDescription, String msg, AbstractEvaluationStrategy strategy, boolean isAggregation) {
		
		String msgDescription = null;
		MessageType msgType = MessageType.Error;
		
		if (isAggregation==false) {
			// ------------------------------------------------------------------
			// --- Check for TechnicalSystem instances --------------------------
			// ------------------------------------------------------------------
			if (strategy instanceof AbstractSnapshotStrategy) {
				msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is a snapshot strategy and can not be used for discrete simulations!";
			} else if (strategy instanceof AbstractEvaluationStrategyRT) {
				msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is a real time strategy and can not be used for discrete simulations!";
			}
		} else {
			// ------------------------------------------------------------------
			// --- Check for TechnicalSystemGroup instances ---------------------
			// ------------------------------------------------------------------
			if (strategy instanceof AbstractGroupSnapshotStrategy) {
				msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is a snapshot strategy and can not be used for discrete simulations!";
			} else  if (strategy instanceof AbstractGroupEvaluationStrategyRT) {
				msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is a real time strategy and can not be used for discrete simulations!";
			}
		}
		return this.getHyGridValidationMessage(msg, msgDescription, msgType);
	}

	
	/**
	 * Gets the validation message for discrete simulation.
	 *
	 * @param netCompDescription the the network component description
	 * @param msg the short message
	 * @param strategy the strategy
	 * @param isAggregation the is aggregation
	 * @return the validation message for discrete simulation
	 */
	private HyGridValidationMessage getValidationMessageForSnapshotSimulation(String netCompDescription, String msg, AbstractEvaluationStrategy strategy, boolean isAggregation) {
		
		String msgDescription = null;
		MessageType msgType = MessageType.Error;
		
		boolean isSnapShotSimulation = this.getHyGridAbstractEnvironmentModel().isDiscreteSnapshotSimulation();
		SnapshotDecisionLocation sdl = this.getHyGridAbstractEnvironmentModel().getSnapshotDecisionLocation();
		if (isAggregation==false) {
			// ------------------------------------------------------------------
			// --- Check for TechnicalSystem instances --------------------------
			// ------------------------------------------------------------------
			if (sdl!=null) {
				switch (sdl) {
				case Decentral:
					// --- Check for the right strategy type ------------------------
					if (isSnapShotSimulation==true) {
						if (!(strategy instanceof AbstractSnapshotStrategy)) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a snapshot strategy!";
						}
					} else {
						if (!(strategy instanceof AbstractEvaluationStrategyRT) || strategy instanceof AbstractSnapshotStrategy) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
						}
					}
					break;
					
				case Central:
					// --- Ensure that the class is of type RT strategy -------------
					if (!(strategy instanceof AbstractEvaluationStrategyRT)) {
						msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
					}
					break;
				}
			}
			
			
		} else {
			// ------------------------------------------------------------------
			// --- Check for TechnicalSystemGroup instances ---------------------
			// ------------------------------------------------------------------
			if (sdl!=null) {
				switch (sdl) {
				case Decentral:
					// --- Check for the right strategy type ------------------------
					if (isSnapShotSimulation==true) {
						if (!(strategy instanceof AbstractGroupSnapshotStrategy)) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a snapshot strategy!";
						}
					} else {
						if (!(strategy instanceof AbstractGroupEvaluationStrategyRT) || strategy instanceof AbstractGroupSnapshotStrategy) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
						}
					}		
					break;
					
				case Central:
					// --- Ensure that the class is of type RT strategy -------------
					if (!(strategy instanceof AbstractGroupEvaluationStrategyRT)) {
						msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
					}
					break;
				}
			}
		}
		return this.getHyGridValidationMessage(msg, msgDescription, msgType);
	}
	
	
	/**
	 * Gets the instance of a HyGridValidationMessage based on specified parameter.
	 *
	 * @param msg the short message 
	 * @param msgDescription the message description (if <code>null</code>, this method returns <code>null</code>)
	 * @param msgType the {@link MessageType}
	 * @return the HyGridValidationMessage or <code>null</code>
	 */
	private HyGridValidationMessage getHyGridValidationMessage(String msg, String msgDescription , MessageType msgType) {
		if (msgDescription==null || msgDescription.isBlank()) return null;
		return new HyGridValidationMessage(msg, msgType, msgDescription);
	}
	
}
