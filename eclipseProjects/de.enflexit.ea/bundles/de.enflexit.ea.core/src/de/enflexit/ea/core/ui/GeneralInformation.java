package de.enflexit.ea.core.ui;

import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.common.properties.Properties;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import de.enflexit.ea.core.planning.PlanningDispatcher;
import de.enflexit.ea.core.planning.PlanningDispatcherConfiguration.PlannerInformation;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.decision.AbstractDecider;
import energy.evaluation.decision.AbstractDecisionSwitch;

/**
 * The Class GeneralInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class GeneralInformation extends Properties {

	private static final long serialVersionUID = -1978895450619267579L;

	private AbstractEnergyAgent energyAgent;
	
	public GeneralInformation(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
		this.fill();
	}
	
	/**
	 * Fill the local properties.
	 */
	private void fill() {
		
		// ------------------------------------------------
		// --- General Agent Information ------------------
		// ------------------------------------------------
		String group = "Agent";
		// --- Agent state --------------------------------
		String aState = this.energyAgent.getAgentState().getName();
		this.setStringValue(group + ".AgentState", aState);
		// --- Agent operating mode -----------------------
		this.setStringValue(group + ".OperatingMode", this.energyAgent.getAgentOperatingMode().name());
		// --- IO behaviour -------------------------------
		this.setStringValue(group + ".IO-Class", this.energyAgent.getEnergyAgentIO().getClass().getName());
		// --- Monitoring activated -----------------------
		this.setBooleanValue(group + ".MonitoringActive", this.energyAgent.isActivatedMonitoring());
		// --- Logging activated --------------------------
		this.setBooleanValue(group + ".LoggingActive", this.energyAgent.isActivatedLogWriter());
		this.setStringValue(group + ".LoggingDestintation", this.energyAgent.getLoggingDestination().name());
		
		
		// ------------------------------------------------
		// --- Internal data model ------------------------
		// ------------------------------------------------
		AbstractInternalDataModel<?> intDM = this.energyAgent.getInternalDataModel();
		group = "Internal Data Model";
		// --- NetworkComponent ---------------------------
		NetworkComponent netComp = intDM.getNetworkComponent();
		if (netComp!=null) {
			this.setStringValue(group + ".NetworkComponent", netComp.getId() + " (" + netComp.getType() + ")");
		}
		// --- Type of controlled system ------------------
		this.setStringValue(group + ".TypeOfControlledSystem", intDM.getTypeOfControlledSystem().name());
		// --- Type of controlled system ------------------
		this.setStringValue(group + ".CentralAgentAID", intDM.getCentralAgentAID()!=null ? intDM.getCentralAgentAID().getName() : "");
		// --- LoggingMode --------------------------------
		this.setStringValue(group + ".LoggingMode", intDM.getLoggingMode().name());
		
		
		// ------------------------------------------------
		// --- Real-Time control --------------------------
		// ------------------------------------------------
		group = "Real-Time Control";
		// --- Activated real-time control? ---------------
		this.setBooleanValue(group + ".Activated", this.energyAgent.isExecutedControlBehaviourRT());
		if (energyAgent.isExecutedControlBehaviourRT()==true) {
			ControlBehaviourRT rtBehav = energyAgent.getControlBehaviourRT();
			// --- Get evaluation class -------------------
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
			
			// --- Found a strategy -----------------------
			if (strategy!=null) {
				AbstractDecisionSwitch<?> dSwitch = strategy.getDecisionSwitch();
				if (dSwitch!=null) {
					// --- Found decision switch ----------
					group += ".DecisionSwitch";
					TreeMap<Integer, ?> dHierarchy = dSwitch.getDecisionHierarchy();
					for (Integer key : dHierarchy.keySet()) {
						AbstractDecider<?, ?> decider = (AbstractDecider<?, ?>) dHierarchy.get(key);
						this.setStringValue(group + ".Decider-" + key , decider.getDeciderName());
						this.setStringValue(group + ".Decider-" + key + "-Class" , decider.getClass().getName());
					}
				}
			}
		}
		
		
		// ------------------------------------------------
		// --- Planning activities ------------------------
		// ------------------------------------------------
		group = "Planning";
		// --- Planning possible --------------------------
		this.setBooleanValue(group + ".Possible", this.energyAgent.isPlanningPossible());
		if (energyAgent.isPlanningPossible()==true) {
			this.setBooleanValue(group + ".Activated", this.energyAgent.isPlanningActivated());
			if (energyAgent.isPlanningActivated()==true) {
				// --- PlanningDispatchers configuration ------  
				PlanningDispatcher pDispatcher = this.energyAgent.getPlanningDispatcher();
				List<String> plannerNameList = pDispatcher.getConfiguration().getPlannerNameList();
				for (int plIdx = 0; plIdx < plannerNameList.size(); plIdx++) {
					
					String plannerName = plannerNameList.get(plIdx);
					String subGroup = group + ".Planner-" + (plIdx+1);
					this.setStringValue(subGroup, plannerName);
					
					// --- Average execution time -------------
					PlannerInformation plannerInformation = pDispatcher.getPlannerInformation(plannerName);
					this.setDoubleValue(subGroup + ".AvgExecTimeInSec", plannerInformation.getAveragePlanningDurationSeconds());
					
					// --- Strategies of the planner ----------
					for (int i = 0; i < plannerInformation.getStrategyClassNameList().size(); i++) {
						String strategy = plannerInformation.getStrategyClassNameList().get(i);
						this.setStringValue(subGroup + ".Strategy-" + (i+1), strategy);
					}
				} 
			}
		}

	}
	
}
