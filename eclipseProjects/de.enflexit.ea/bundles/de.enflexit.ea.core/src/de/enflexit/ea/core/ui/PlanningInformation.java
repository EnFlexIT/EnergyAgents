package de.enflexit.ea.core.ui;

import java.util.List;
import java.util.TreeMap;

import de.enflexit.common.properties.Properties;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.planning.AbstractPlanningDispatcherManager;
import de.enflexit.ea.core.planning.PlanningDispatcher;
import de.enflexit.ea.core.planning.PlanningDispatcherConfiguration.PlannerInformation;
import energy.planning.EomPlannerResult;

/**
 * The Class PlannerInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PlanningInformation extends Properties {

	private static final long serialVersionUID = -1978895450619267579L;

	private AbstractEnergyAgent energyAgent;
	
	private EomPlannerResult realTimePlannerResult;
	private TreeMap<String, EomPlannerResult> plannerResultTreeMap;
	
	/**
	 * Instantiates a new planner information.
	 * @param energyAgent the energy agent
	 */
	public PlanningInformation(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
		this.fill();
	}
	
	/**
	 * Fill the local properties.
	 */
	private void fill() {
		
		String group = "Planning";
		// --- Planning possible --------------------------
		this.setBooleanValue(group + ".Possible", this.energyAgent.isPlanningPossible());
		if (energyAgent.isPlanningPossible()==false) return;
		
		this.setBooleanValue(group + ".Activated", this.energyAgent.isPlanningActivated()); 
		if (energyAgent.isPlanningActivated()==false) return; 

		PlanningDispatcher pDispatcher = this.energyAgent.getPlanningDispatcher();
		AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> pDispatcherManager = this.energyAgent.getPlanningDispatcherManager();
		this.realTimePlannerResult = pDispatcherManager.getPlannerResultForRealTimeExecution();

		// --- Get the registered planner -----------------
		List<String> plannerNameList = pDispatcher.getConfiguration().getPlannerNameList();
		if (plannerNameList.size()==0) return;
		
		// --- Collect Planner information ----------------
		this.plannerResultTreeMap = new TreeMap<>();
		for (int plIdx = 0; plIdx < plannerNameList.size(); plIdx++) {
			
			String plannerName = plannerNameList.get(plIdx);
			String subGroup = group + ".Planner-" + (plIdx+1);
			this.setStringValue(subGroup, plannerName);
			
			// --- Average execution time -----------------
			PlannerInformation plannerInformation = pDispatcher.getPlannerInformation(plannerName);
			this.setDoubleValue(subGroup + ".AvgExecTimeInSec", plannerInformation.getAveragePlanningDurationSeconds());
			
			// --- Strategies of the planner --------------
			for (int i = 0; i < plannerInformation.getStrategyClassNameList().size(); i++) {
				String strategy = plannerInformation.getStrategyClassNameList().get(i);
				this.setStringValue(subGroup + ".Strategy-" + (i+1), strategy);
			}
			this.plannerResultTreeMap.put(plannerName, pDispatcher.getEomPlannerResult(plannerName));
		} 
	}
	
	/**
	 * Returns the real time planner result.
	 * @return the real time planner result
	 */
	public EomPlannerResult getRealTimePlannerResult() {
		return realTimePlannerResult;
	}
	/**
	 * Returns the planner result tree map.
	 * @return the planner result tree map
	 */
	public TreeMap<String, EomPlannerResult> getPlannerResultTreeMap() {
		return plannerResultTreeMap;
	}
	
}
