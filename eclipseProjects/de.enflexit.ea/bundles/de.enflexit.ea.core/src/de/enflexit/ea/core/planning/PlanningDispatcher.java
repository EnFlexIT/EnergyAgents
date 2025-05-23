package de.enflexit.ea.core.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.enflexit.common.DateTimeHelper;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.planning.PlanningDispatcherConfiguration.PlannerInformation;
import energy.planning.EomPlanner.ControlledSystemType;
import energy.planning.EomPlannerResult;

/**
 * The Class PlanningDispatcher serves as central entity for an energy agent to
 * plan and analyze the current situation of the system under control in the
 * context of an application use case (e.g. Demand Response vs. local markets vs. 
 * Virtual Power Plant and so on).
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PlanningDispatcher {

	private AbstractEnergyAgent energyAgent;

	private AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> dispatcherManager;
	private PlanningDispatcherConfiguration configuration;

	private HashMap<String, Planner> plannerHashMap;
	private boolean isDoTermination;
	
	
	/**
	 * Instantiates a new planning dispatcher.
	 *
	 * @param energyAgent the energy agent
	 * @param dispatcherManager the dispatcher manager
	 */
	public PlanningDispatcher(AbstractEnergyAgent energyAgent, AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> dispatcherManager) {
		this.energyAgent = energyAgent;
		this.dispatcherManager = dispatcherManager;
		this.dispatcherManager.registerPlanner(this);
	}
	/**
	 * Returns the configuration.
	 * @return the configuration
	 */
	public PlanningDispatcherConfiguration getConfiguration() {
		if (configuration==null) {
			configuration = new PlanningDispatcherConfiguration();
		}
		return configuration;
	}
	
	/**
	 * Registers a planner instance for an EnergyAgent.
	 *
	 * @param plannerName the planner name (not allowed to be <code>null</code>)
	 * @param strategyClassName the strategy class name to use for the planner (not allowed to be <code>null</code>)
	 */
	public void registerPlanner(String plannerName, String strategyClassName) {
		this.registerPlanner(plannerName, new ArrayList<>(Arrays.asList(strategyClassName)));
	}
	/**
	 * Registers a planner instance for an EnergyAgent.
	 *
	 * @param plannerName the planner name (not allowed to be <code>null</code>)
	 * @param strategyClassNameList the strategy class name list (not allowed to be <code>null</code>)
	 */
	public void registerPlanner(String plannerName, List<String> strategyClassNameList) {
		if (this.getConfiguration().registerPlanner(plannerName, strategyClassNameList)==false) {
			this.print("To register a planner, a planner name and a list of evaluation strategies for the planning needs to be provided!", true);
		}
	}
	
	/**
	 * Returns the PlannerInformation for the specified planner.
	 *
	 * @param plannerName the planner name
	 * @return the planner information
	 */
	public PlannerInformation getPlannerInformation(String plannerName) {
		return this.getConfiguration().getPlannerInformation(plannerName);
	}
	/**
	 * Returns the current EomPlannerResult for the specified Planner.
	 *
	 * @param plannerName the planner name
	 * @return the EomPlannerResult
	 */
	public EomPlannerResult getEomPlannerResult(String plannerName) {
		PlannerInformation pi = this.getPlannerInformation(plannerName);
		if (pi!=null) {
			return pi.getEomPlannerResult();
		}
		return null;
	}
	
	
	/**
	 * Truncates all {@link EomPlannerResult}s to the specified start time and thus should save memory.
	 * @param startTime the time at which the EomPlannnerResult should begin after calling this method
	 */
	public void applyScheduleLengthRestriction(long startTime) {
		List<String> plannerNameList = this.getPlannerNameList();
		for (String plannerName : plannerNameList) {
			this.applyScheduleLengthRestriction(startTime, plannerName);
		}
	}
	/**
	 * Truncates the {@link EomPlannerResult} of the specified planner to the specified start time and thus should save memory.
	 * @param startTime Time the time at which the EomPlannnerResult should begin after calling this method
	 * @param plannerName the planner name
	 */
	public void applyScheduleLengthRestriction(long startTime, String plannerName) {
		EomPlannerResult eomPlannerResult = this.getEomPlannerResult(plannerName);
		if (eomPlannerResult!=null) {
			eomPlannerResult.applyScheduleLengthRestriction(startTime);
		}
	}
	/**
	 * Truncates the sub result (see networkID) of the {@link EomPlannerResult} of the specified planner to the specified start time and thus should save memory.
	 *
	 * @param startTime the time at which the EomPlannnerResult should begin after calling this method
	 * @param plannerName the planner name
	 * @param networkID the network ID of a sub result (or sub ScheduleList)
	 */
	public void applyScheduleLengthRestriction(long startTime, String plannerName, String networkID) {
		EomPlannerResult eomPlannerResult = this.getEomPlannerResult(plannerName);
		if (eomPlannerResult!=null) {
			eomPlannerResult.applyScheduleLengthRestriction(startTime, networkID);
		}
	}
	
	
	/**
	 * Starts the default planning, which means all strategies configured as planning strategies will be executed.
	 *
	 * @param planFrom the time to plan from
	 * @param planTo the time to plan to
	 * @return true, if the planning was successfully started
	 */
	public boolean startPlanning(long planFrom, long planTo) {
		return this.startPlanningInThread(null, planFrom, planTo);
	}
	
	/**
	 * Starts the planning of the specified planner.
	 *
	 * @param plannerName the name of the planner (if <code>null</code>, the default planning will
	 * be used; means that all strategies configured as planning strategies will be executed. 
	 * @param planFrom the time to plan from
	 * @param planTo the time to plan to
	 * @return true, if the planning was successfully started
	 */
	public boolean startPlanning(String plannerName, long planFrom, long planTo) {
		
		boolean isDebug = false;
		String agentToDebug = "LV6.201 Bus 25";
		String plannerToDebug = "marketPlanner";
		if (isDebug==true && this.energyAgent.getLocalName().equals(agentToDebug)==true && plannerName.equals(plannerToDebug)==true) {
			String timeFrom = DateTimeHelper.getTimeAsString(planFrom);
			String timeTo 	= DateTimeHelper.getTimeAsString(planTo);
			System.out.println("[" + this.energyAgent.getLocalName() + "] Starting '" + plannerName + "' from " + timeFrom + " to " + timeTo);
		}
		
		if (this.getOrCreatePlanner(plannerName).getControlledSystemType()==ControlledSystemType.None) {
			// --- No flexibility available -> planning doesn't make sense ----
			return false;
		}
		return this.startPlanningInThread(plannerName, planFrom, planTo);
	}
	
	/**
	 * Internally: Starts the planning of the specified planner in a dedicated thread.
	 *
	 * @param plannerName the name of the planner (if <code>null</code>, the default planning will
	 * be used; means that all strategies configured as planning strategies will be executed. 
	 * @param planFrom the time to plan from
	 * @param planTo the time to plan to
	 * @return true, if the planning was successfully started
	 */
	private boolean startPlanningInThread(final String plannerName, final long planFrom, final long planTo) {
		
		if (this.isDoTermination==true) {
			this.print("The Planning Dispatcher is going to terminate and thus cannot start new planning processes!", true);
			return false;
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				PlanningDispatcher.this.startPlanningInternally(plannerName, planFrom, planTo);
			}
		}, this.energyAgent.getLocalName() + "-PlanningExecution").start();
		
		return true;
	}
	/**
	 * Internally: Starts the planning of the specified planner.
	 *
	 * @param plannerName the name of the planner (if <code>null</code>, the default planning will  
	 * be used; means that all strategies configured as planning strategies will be executed. 
	 * @param planFrom the time to plan from
	 * @param planTo the time to plan to
	 */
	private void startPlanningInternally(String plannerName, long planFrom, long planTo) {
		
		if (this.isDoTermination==true) {
			this.print("The Planning Dispatcher is going to terminate and thus cannot start new planning processes!", true);
			return;
		}
		
		if (plannerName==null || plannerName.isBlank()==true) {
			// --- Start the default planner ----------------------------------
			Planner planner = this.getOrCreatePlanner(Planner.DEFAULT_NAME);
			planner.startPlanning(planFrom, planTo);
			
		} else {
			// --- Starts the planner with its assigned strategy classes ------ 
			List<String> strategyClassNameList = this.getConfiguration().getStrategyClassNameList(plannerName);
			if (strategyClassNameList!=null) {
				Planner planner = this.getOrCreatePlanner(plannerName);
				planner.startPlanning(planFrom, planTo, strategyClassNameList);
			} else {
				this.print("No strategy classes were defined for the planning process '" + plannerName + "'!", true);
			}
		}
	}
	
	
	/**
	 * Returns the local reminder planner hash map.
	 * @return the planner hash map
	 */
	private HashMap<String, Planner> getPlannerHashMap() {
		if (plannerHashMap==null) {
			plannerHashMap = new HashMap<>();
		}
		return plannerHashMap;
	}
	
	/**
	 * Returns the list of planner names.
	 * @return the planner name list
	 */
	public List<String> getPlannerNameList() {
		return new ArrayList<>(this.getPlannerHashMap().keySet());
	}
	
	/**
	 * Gets or creates the planner instance for the specified name.
	 *
	 * @param plannerName the planner name
	 * @return the or create planner
	 */
	private Planner getOrCreatePlanner(String plannerName) {
		Planner planner = this.getPlannerHashMap().get(plannerName);
		if (planner==null) {
			planner = new Planner(this.energyAgent, plannerName, this.dispatcherManager);
			this.getPlannerHashMap().put(plannerName, planner);
		}
		return planner;
	}
	
	/**
	 * Stops running planning processes.
	 */
	public void terminate() {
		
		// --- Mark as 'in termination' -------------------
		this.isDoTermination = true;
		
		// --- Kill planning processes --------------------
		for (Planner planner : this.getPlannerHashMap().values()) {
			try {
				planner.stopPlanning();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// --- Throw away local instances ----------------- 
		this.energyAgent = null;
		this.configuration = null;
		this.dispatcherManager=null;
		this.plannerHashMap = null;
	}
	
	/**
	 * Prints the specified message as error or info to the console.
	 *
	 * @param message the message
	 * @param isError the is error
	 */
	public void print(String message, boolean isError) {

		if (message==null || message.isBlank()==true) return;
		
		String msgPrefix = "[" + this.getClass().getSimpleName() + "." + this.energyAgent.getLocalName() + "] ";
		String msgFinal  = msgPrefix + message; 
		// --- Print to console -----------------------
		if (isError==true) {
			System.err.println(msgFinal);
		} else {
			System.out.println(msgFinal);
		}
	}
	
}
