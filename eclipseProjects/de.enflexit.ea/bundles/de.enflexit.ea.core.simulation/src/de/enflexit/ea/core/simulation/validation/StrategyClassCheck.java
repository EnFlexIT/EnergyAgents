package de.enflexit.ea.core.simulation.validation;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
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
		
		if (evalStrategyClassName!=null && evalStrategyClassName.isEmpty()==false) {
			// --- Check with respect to the current simulation type --------------------
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
				// --- Do the type check ------------------------------------------------
				boolean isSnapShotSimulation = getHyGridAbstractEnvironmentModel().isDiscreteSnapshotSimulation();
				if (isAggregation==false) {
					// --- Check for TechnicalSystem instances --------------------------
					if (isSnapShotSimulation==true) {
						if (!(strategy instanceof AbstractSnapshotStrategy)) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a snapshot strategy!";
						}
					} else {
						if (!(strategy instanceof AbstractEvaluationStrategyRT) || strategy instanceof AbstractSnapshotStrategy) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
						}
					}
					
				} else {
					// --- Check for TechnicalSystemGroup instances ---------------------
					if (isSnapShotSimulation==true) {
						if (!(strategy instanceof AbstractGroupSnapshotStrategy)) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a snapshot strategy!";
						}
					} else {
						if (!(strategy instanceof AbstractGroupEvaluationStrategyRT) || strategy instanceof AbstractGroupSnapshotStrategy) {
							msgDescription = "The evaluation strategy for the EOM model of " + netCompDescription + " is not a real time strategy!";
						}
					}					
					
				}
			}
			
		} else {
			// --- No evaluation class could be found -----------------------------------
			msgDescription = "No default evaluation strategy could be found for the EOM model of " + netCompDescription + ".";
		}
		
		
		// --- Prepare return value -----------------------------------------------------
		HyGridValidationMessage hvm = null;
		if (msgDescription!=null) {
			hvm = new HyGridValidationMessage(msg, msgType);
			hvm.setDescription(msgDescription);
		}
		return hvm;
	}
	
}
