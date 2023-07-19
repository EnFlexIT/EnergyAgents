package de.enflexit.ea.core.planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import agentgui.simulationService.time.StopWatch;
import energy.helper.AppendDataInterface.AppendStrategy;
import energy.planning.EomPlannerResult;

/**
 * The Class PlanningDispatcherConfiguration.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PlanningDispatcherConfiguration {

	private TreeMap<String, PlannerInformation> plannerInformationTreeMap;
	
	/**
	 * Returns the planner information tree map.
	 * @return the planner information tree map
	 */
	private TreeMap<String, PlannerInformation> getPlannerInformationTreeMap() {
		if (plannerInformationTreeMap==null) {
			plannerInformationTreeMap = new TreeMap<>();
		}
		return plannerInformationTreeMap;
	}

	/**
	 * Returns the planner name list in the order of the planner hierarchy.
	 * @return the planner name list
	 */
	public List<String> getPlannerNameList() {
		ArrayList<String> plannerNameList = new ArrayList<>(this.getPlannerInformationTreeMap().keySet());
		Collections.sort(plannerNameList);
		return plannerNameList;
	}
	
	/**
	 * Returns the planner information for the specified planner.
	 *
	 * @param plannerName the name of the planner (not allowed to be <code>null</code>)
	 * @return the planner information with its current informations
	 */
	public PlannerInformation getPlannerInformation(String plannerName) {
		if (plannerName!=null && plannerName.isBlank()==false) {
			PlannerInformation pi = this.getPlannerInformationTreeMap().get(plannerName);
			if (pi==null) {
				pi = new PlannerInformation();
				this.getPlannerInformationTreeMap().put(plannerName, pi);
			}
			return pi;
		}
		return null;
	}
	
	
	/**
	 * Registers a planner instance for an EnergyAgent.
	 *
	 * @param plannerName the planner name (not allowed to be <code>null</code>)
	 * @param strategyClassNameList the strategy class name list
	 * @return true, if successful registered
	 */
	public boolean registerPlanner(String plannerName, List<String> strategyClassNameList) {
		PlannerInformation pi = this.getPlannerInformation(plannerName);
		if (pi!=null && strategyClassNameList!=null && strategyClassNameList.isEmpty()==false) {
			pi.setStrategyClassNameList(strategyClassNameList);
			return true;
		}
		return false;
	}
	/**
	 * Returns the strategy class name list for the specified planner.
	 *
	 * @param plannerName the planner name
	 * @return the strategy class name list
	 */
	public List<String> getStrategyClassNameList(String plannerName) {
		PlannerInformation pi = this.getPlannerInformation(plannerName);
		if (pi!=null) {
			return pi.getStrategyClassNameList();
		}
		return null;
	}
	
	
	/**
	 * Adds the specified planner result to the available results of the specified planner.
	 *
	 * @param plannerName the planner name
	 * @param plannerResult the planner result to add
	 */
	public void appendPlannerResult(String plannerName, EomPlannerResult plannerResult) {
		PlannerInformation pi = this.getPlannerInformation(plannerName);
		if (pi!=null) {
			if (pi.getEomPlannerResult()==null) {
				pi.setEomPlannerResult(plannerResult);
			} else {
				pi.getEomPlannerResult().append(plannerResult);
			}
		}
	}
	/**
	 * Return the current EomPlannerResult for the specified planner.
	 *
	 * @param plannerName the planner name
	 * @return the planner result
	 */
	public EomPlannerResult getPlannerResult(String plannerName) {
		PlannerInformation pi = this.getPlannerInformation(plannerName);
		if (pi!=null) {
			return pi.getEomPlannerResult();
		}
		return null;
	}
	
	
	
	
	
	/**
	 * The Class PlannerInformation stores all runtime information for a single planner.
	 *
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public class PlannerInformation {
		
		private List<String> strategyClassNameList;
		private EomPlannerResult eomPlannerResult;
		
		private StopWatch stopWatch;
		private long planningDurationMillisLast;
		private List<Double> planningDurationSeconds;
		private int planningDurationLength = 10;
		
		
		/**
		 * Returns the strategy class name list.
		 * @return the strategy class name list
		 */
		public List<String> getStrategyClassNameList() {
			return strategyClassNameList;
		}
		/**
		 * Sets the strategy class name list.
		 * @param strategyClassNameList the new strategy class name list
		 */
		public void setStrategyClassNameList(List<String> strategyClassNameList) {
			this.strategyClassNameList = strategyClassNameList;
		}
		
		/**
		 * Returns the EomPlannerResult.
		 * @return the eom planner result
		 */
		public EomPlannerResult getEomPlannerResult() {
			return eomPlannerResult;
		}
		/**
		 * Sets the EomPlannerResult
		 * @param eomPlannerResult the new eom planner result
		 */
		public void setEomPlannerResult(EomPlannerResult eomPlannerResult) {
			this.eomPlannerResult = eomPlannerResult;
		}
		
		/**
		 * Appends the specified EomPlannerResult to the available one in case that the result has the same identity
		 * and by using an {@link AppendStrategy#OverwriteLocalAvailableData}.
		 * @param eomPlannerResult the EomPlannerResult to append
		 */
		public void appendEomPlannerResult(EomPlannerResult eomPlannerResult) {
			this.appendEomPlannerResult(eomPlannerResult, true, AppendStrategy.OverwriteLocalAvailableData);
		}
		/**
		 * Appends the specified EomPlannerResult to the available one in case that the result has the same identity.
		 *
		 * @param eomPlannerResult the EomPlannerResult to append
		 * @param appendStrategy the strategy to append new data if data with the same global time already exists (keep existing data or overwrite with new data)
		 */
		public void appendEomPlannerResult(EomPlannerResult eomPlannerResult, AppendStrategy appendStrategy) {
			this.appendEomPlannerResult(eomPlannerResult, true, appendStrategy);
		}
		/**
		 * Appends the specified EomPlannerResult to the available one.
		 * 
		 * @param eomPlannerResult the EomPlannerResult to append
		 * @param failOnDifferentSystemIdentity the indicator to verify that both EomPlannerResult have the same system identity. If <code>true</code> and not equal, the method will throw an {@link IllegalArgumentException}.
		 * @param appendStrategy the strategy to append new data if data with the same global time already exists (keep existing data or overwrite with new data)
		 */
		public void appendEomPlannerResult(EomPlannerResult eomPlannerResult, boolean failOnDifferentSystemIdentity, AppendStrategy appendStrategy) {
			if (this.getEomPlannerResult()==null) {
				this.setEomPlannerResult(eomPlannerResult);
			} else {
				this.getEomPlannerResult().append(eomPlannerResult, failOnDifferentSystemIdentity, appendStrategy);
			}
		}
		
		
		// ------------------------------------------------
		// --- From here stop watch handling --------------
		// ------------------------------------------------
		private StopWatch getStopWatch() {
			if (stopWatch==null) {
				stopWatch = new StopWatch(); 
			}
			return stopWatch;
		}
		
		/**
		 * Starts the stopwatch start.
		 */
		public void stopWatchStart() {
			this.getStopWatch().start();
		}
		/**
		 * Stop the stopwatch and reminds the execution duration.
		 */
		public void stopWatchStop() {
			this.getStopWatch().stop();
			
			// --- Remind duration ------------------------
			long durationMilliseconds = this.getStopWatch().getTimeMeasured();
			this.setLastPlanningDurationMillis(durationMilliseconds);

			double durationSeconds = durationMilliseconds / 1000.0;
			this.getPlanningDurationSeconds().add(durationSeconds);
			
			this.getStopWatch().reset();
		}

		/**
		 * Returns the last planning duration in milliseconds.
		 * @return the last planning duration milliseconds
		 */
		public long getLastPlanningDurationMillis() {
			return planningDurationMillisLast;
		}
		/**
		 * Sets the last planning duration in milliseconds.
		 * @param planningDurationMillisLast the new last planning duration millis
		 */
		private void setLastPlanningDurationMillis(long planningDurationMillisLast) {
			this.planningDurationMillisLast = planningDurationMillisLast;
		}

		/**
		 * Returns the list of planning durations in seconds.
		 * @return the list of planning duration in seconds
		 */
		private List<Double> getPlanningDurationSeconds() {
			if (planningDurationSeconds==null) {
				planningDurationSeconds = new ArrayList<>();
			}
			return planningDurationSeconds;
		}
		/**
		 * Returns the average planning duration in seconds.
		 * @return the average planning duration in seconds
		 */
		public double getAveragePlanningDurationSeconds() {
			// --- Reduce list length ---------------------
			while (this.getPlanningDurationSeconds().size() > this.planningDurationLength) {
				this.getPlanningDurationSeconds().remove(0);
			}
			// --- Return average value -------------------
			return this.getPlanningDurationSeconds().stream().collect(Collectors.averagingDouble(Double::doubleValue));
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			
			String description = "";
			if (this.getStrategyClassNameList()==null || this.getStrategyClassNameList().size()==0) {
				description += Planner.DEFAULT_NAME;
			} else {
				description += String.join(", ", this.getStrategyClassNameList());
			}
			description += " - Last Duration: " + this.getLastPlanningDurationMillis() + " ms, Average (" + this.getPlanningDurationSeconds().size() + " x executed): " + this.getAveragePlanningDurationSeconds() + " s";
			return description;
		}
		
	}
	
}
