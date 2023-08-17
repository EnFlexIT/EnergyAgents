package de.enflexit.ea.core.ui;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.common.properties.Properties;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import energy.EomController;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.decision.AbstractDecider;
import energy.evaluation.decision.AbstractDecisionSwitch;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;
import energygroup.GroupController;

/**
 * The Class RealTimeInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class RealTimeInformation extends Properties {

	private static final long serialVersionUID = -1978895450619267579L;

	private AbstractEnergyAgent energyAgent;
	
	private EomController eomController;
	private TechnicalSystemStateEvaluation technicalSystemStateEvaluation;
	
	public RealTimeInformation(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
		this.fill();
	}
	
	/**
	 * Fill the local properties.
	 */
	private void fill() {
		
		AbstractInternalDataModel<?> intDM = this.energyAgent.getInternalDataModel();
		boolean isActivatedRealTimeControl = this.energyAgent.isExecutedControlBehaviourRT();
		
		// ----------------------------------------------------------
		// --- Real-Time control ------------------------------------
		// ----------------------------------------------------------
		String group = "Real-Time Control";
		// --- Activated real-time control? -------------------------
		this.setBooleanValue(group + ".Activated", isActivatedRealTimeControl);
		if (isActivatedRealTimeControl==true) {
			ControlBehaviourRT rtBehav = this.energyAgent.getControlBehaviourRT();
			// --- Get evaluation class -----------------------------
			AbstractEvaluationStrategy strategy = null;
			switch (intDM.getTypeOfControlledSystem()) {
			case TechnicalSystem:
				strategy = rtBehav.getRealTimeEvaluationStrategy();
				break;
			case TechnicalSystemGroup:
				strategy = rtBehav.getRealTimeGroupEvaluationStrategy();
				break;
			case None:
				strategy = null;
				break;
			}
			this.setStringValue(group + ".StrategyClass", strategy==null ? "No real time strategy defined." : strategy.getClass().getName());
			
			// --- Found a strategy ---------------------------------
			if (strategy!=null) {
				AbstractDecisionSwitch<?> dSwitch = strategy.getDecisionSwitch();
				if (dSwitch!=null) {
					// --- Found decision switch --------------------
					group += ".DecisionSwitch-Active";
					int key = dSwitch.getCurrentPriorityLevel();
					AbstractDecider<?, ?> decider = dSwitch.getCurrentDecider();
					this.setStringValue(group + ".Decider-" + key , decider.getDeciderName());
					this.setStringValue(group + ".Decider-" + key + "-Class" , decider.getClass().getName());
					
				}
			}
		}

		
		// ----------------------------------------------------------
		// --- Get the current tsse ---------------------------------
		// ----------------------------------------------------------
		if (isActivatedRealTimeControl==true) {
			// --- Under real time control --------------------------
			this.eomController = this.energyAgent.getControlBehaviourRT().getEomController();
			this.technicalSystemStateEvaluation = this.energyAgent.getControlBehaviourRT().getTechnicalSystemGroupStateEvaluation();
			if (this.technicalSystemStateEvaluation==null) {
				this.technicalSystemStateEvaluation = this.energyAgent.getControlBehaviourRT().getLastTechnicalSystemStateEvaluation();
			}
			
		} else {
			// --- Not under control of ControlBehaviourRT ----------
			if (energyAgent.getEnergyAgentIO() instanceof AbstractIOSimulated) {
				
				NetworkComponent netComp = energyAgent.getInternalDataModel().getNetworkComponent();
				Object dataModel = netComp.getDataModel(); 
				if (dataModel instanceof ScheduleList) {
					ScheduleController sc = new ScheduleController();
					sc.setScheduleList((ScheduleList) dataModel);
					this.eomController = sc;
				} else if (dataModel instanceof TechnicalSystem) {
					OptionModelController omc = new OptionModelController();
					omc.setTechnicalSystem((TechnicalSystem) dataModel);
					this.eomController = omc;
				} else if (dataModel instanceof TechnicalSystemGroup) {
					GroupController gc = new GroupController();
					gc.setTechnicalSystemGroup((TechnicalSystemGroup)dataModel);
					this.eomController = gc;
				}
				
				AbstractIOSimulated ioSimulated = (AbstractIOSimulated) energyAgent.getEnergyAgentIO();
				this.technicalSystemStateEvaluation = ioSimulated.getSimulationConnector().getLastTechnicalSystemStateEvaluationTransferred();
			}
		}
		
	}
	
	/**
	 * Return the current EomController.
	 * @return the eom controller
	 */
	public EomController getEomController() {
		return this.eomController;
	}
	/**
	 * Returns the current / last technical system state evaluation.
	 * @return the technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
		return technicalSystemStateEvaluation;
	}
	
}
