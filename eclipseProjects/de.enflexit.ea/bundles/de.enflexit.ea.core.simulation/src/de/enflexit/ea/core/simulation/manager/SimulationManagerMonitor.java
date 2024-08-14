package de.enflexit.ea.core.simulation.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import agentgui.simulationService.transaction.EnvironmentNotification;
import de.enflexit.common.DateTimeHelper;
import jade.core.AID;

/**
 * The Class SimulationManagerMonitor.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SimulationManagerMonitor extends Thread {

	public enum MonitorAction {
		DistributeEnvironmentModel("Distribute EnvironmentModel"),
		DiscreteSimulation_ExecuteSimulation("Execute Simulation"),
		DiscreteSimulation_StepSimulation("Step Simulation"),
		DiscreteSimulation_AwaitingAnswerOfIteratingSystem("Awaiting answer of iterating system"),
		DiscreteSimulation_AwaitingFinalAnswerOfIteratingSystem("Awaiting final answer of iterating system"),
		DiscreteSimulation_AwaitingControlBehaviourRTStateUpdate("Awaiting state update of ControlBehaviourRT"),
		StopSimulation("Stop Simulation"),
		;
		
		private final String actionDescription;
		private MonitorAction(String actionDescription) {
			this.actionDescription = actionDescription;
		}
		public String getActionDescription() {
			return actionDescription;
		}
	}
	
	private SimulationManager simulationManager;

	private boolean doTerminate;
	private Object notifier;
	
	private MonitorAction currentMonitorAction;
	private String currentAction;
	
	private Long currentMaxWaitTime;
	private long defaultMaxWaitTime = 10 * 1000;
	
	private int repetitionCounter = 0;
	
	
	/**
	 * Instantiates a new simulation manager monitor.
	 *
	 * @param simulationManager the simulation manager
	 * @param defaultMaxWaitTime the default maximum wait time for an action
	 */
	public SimulationManagerMonitor(SimulationManager simulationManager, Long defaultMaxWaitTime) {
		this.simulationManager = simulationManager;
		if (defaultMaxWaitTime!=null && defaultMaxWaitTime>0) {
			this.defaultMaxWaitTime = defaultMaxWaitTime;
		}
		this.setName(this.getClass().getSimpleName());
	}
	
	// ------------------------------------------------------------------------
	// --- From here, methods for thread control ------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Sets the do terminate.
	 * @param doTerminate the new do terminate
	 */
	public void setDoTerminate(boolean doTerminate) {
		this.doTerminate = doTerminate;
		synchronized (this.getNotifier()) {
			this.getNotifier().notify();
		}
	}
	/**
	 * Checks if is do terminate.
	 * @return true, if is do terminate
	 */
	private boolean isDoTerminate() {
		return doTerminate;
	}
	
	/**
	 * Returns the local notifier object to awake the current thread again.
	 * @return the notifier
	 */
	private Object getNotifier() {
		if (notifier==null) {
			notifier = new Object();
		}
		return notifier;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		while (this.isDoTerminate()==false) {

			try {

				if (this.currentAction!=null) {
					// --- There is an action to monitor ------------
					String action = this.currentAction;
					final long waitTime = this.getCurrentMaxWaitTime();
					final long endTime  = System.currentTimeMillis() + waitTime;
					synchronized (this.getNotifier()) {
						this.getNotifier().wait(waitTime);
					}

					if (this.currentAction!=null && this.currentAction.equals(action)==true && System.currentTimeMillis()>=endTime) {
						// --- Action took too long ----------------- 
						try {
							this.repetitionCounter++;
							this.printOnExpiredAction(waitTime);
							this.reactOnExpiredAction();
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
				} else {
					// --- No action to monitor (yet) ---------------
					synchronized (this.getNotifier()) {
						this.getNotifier().wait();
					}
				}
			
			} catch (InterruptedException inEx) {
				inEx.printStackTrace();
			}
			
		}// end while
	}
	/**
	 * Returns the current max wait time.
	 * @return the current max wait time
	 */
	private Long getCurrentMaxWaitTime() {
		return this.currentMaxWaitTime==null || this.currentMaxWaitTime==0 ? this.defaultMaxWaitTime : this.currentMaxWaitTime;
	}
	
	
	/**
	 * Sets the current monitor action.
	 *
	 * @param monitorAction the monitor action
	 * @param maxWaitTime the max wait time
	 */
	public void setMonitorAction(MonitorAction monitorAction, Long maxWaitTime) {
		this.currentMonitorAction = monitorAction;
		this.setMonitorAction(monitorAction!=null ? monitorAction.getActionDescription() : null, maxWaitTime);
	}
	/**
	 * Sets the monitor action.
	 *
	 * @param actionDescription the action description
	 * @param maxWaitTime the max wait time
	 */
	public void setMonitorAction(String actionDescription, Long maxWaitTime) {

		// --- Set local action ---------------------------
		if (actionDescription==null || actionDescription.isBlank()==true) {
			this.currentAction = null;
			this.repetitionCounter = 0;
		} else {
			if (this.currentAction==null || actionDescription.equals(this.currentAction)==false) {
				this.repetitionCounter = 0;
			}
			this.currentAction = actionDescription;
		}
		// --- Set maximum wait time for the action -------
		if (maxWaitTime!=null && maxWaitTime>0) {
			this.currentMaxWaitTime = maxWaitTime;
		} else {
			this.currentMaxWaitTime = this.defaultMaxWaitTime;
		}
		// --- Awake the monitor thread ------------------- 
		synchronized (this.getNotifier()) {
			this.getNotifier().notify();
		}
	}
	

	// ------------------------------------------------------------------------
	// --- From here, reactions on expiration ---------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Prints the action message.
	 * @param waitTime the wait time
	 */
	private void printOnExpiredAction(long waitTime) {
		this.print("The reaction on '" + this.currentAction + "' takes longer than expected (waiting time was " + (waitTime / 1000)  + " s, " + this.repetitionCounter + " repetition, " + DateTimeHelper.getTimeAsString(System.currentTimeMillis()) + ")!");
	}
	/**
	 * Prints the specified message to the console.
	 * @param message the message
	 */
	private void print(String message) {
		System.err.println("[" + this.getClass().getSimpleName() + "] " + DateTimeHelper.getTimeAsString(this.simulationManager.getTime()) + ": " + message);
	}
	
	
	/**
	 * React on expired action.
	 */
	private void reactOnExpiredAction() {
		
		if (this.currentMonitorAction==null) return;
		
		// --------------------------------------------------------------------
		// --- React on action expiration -------------------------------------
		// --------------------------------------------------------------------
		switch (this.currentMonitorAction) {
		case DistributeEnvironmentModel:
			// --- Check initialized and started agents -----------------------
			this.reactOnExpiredDistributeEnvironmentModel(this.simulationManager.getAgentNotifications(), this.simulationManager.getAgentsKnown(), this.simulationManager.getAgentsInitialized(), this.simulationManager.getAgentsSuccessfulStarted());
			break;
			
		case DiscreteSimulation_ExecuteSimulation:
		case DiscreteSimulation_StepSimulation:
			// --- Try getting the current state of agent answers -------------
			this.reactOnExpiredAgentAnswers(this.simulationManager.getAgentsKnown(), this.simulationManager.getEnvironmentInstanceNextParts(), this.simulationManager.getEnvironmentInstanceNextPartsFromMain());
			break;
			
		case DiscreteSimulation_AwaitingAnswerOfIteratingSystem:
			this.reactOnExpiredAnswerOfIteratingSystem(this.simulationManager.getAggregationHandler().getDiscreteIteratingSystems(), this.simulationManager.getAggregationHandler().getDiscreteIteratingSystemsStateTypeLog());
			break;
			
		case DiscreteSimulation_AwaitingFinalAnswerOfIteratingSystem:
			this.reactOnExpiredFinalAnswerOfIteratingSystem(this.simulationManager.getAggregationHandler().getDiscreteIteratingSystemsStateTypeLog());
			break;
			
		case DiscreteSimulation_AwaitingControlBehaviourRTStateUpdate:
			this.reactOnExpiredControlBehaviourRTStateUpdate(this.simulationManager.getControlBehaviourRTStateUpdateSources(), this.simulationManager.getControlBehaviourRTStateUpdateAnswered());
			break;
			
		case StopSimulation:
			// --- Check if all agents confirmed stop -------------------------
			this.reactOnExpiredStopSimulation(this.simulationManager.getAgentsKnown(), this.simulationManager.getAgentNotifications());
			break;
		}
	}
	

	/**
	 * React on expired distribute environment model.
	 *
	 * @param agentNotifications the agent notifications
	 * @param agentsInitialized the agents initialized
	 * @param agentsSuccessfulStarted the agents successful started
	 */
	private void reactOnExpiredDistributeEnvironmentModel(Vector<EnvironmentNotification> agentNotifications, HashSet<String> agentsKnown, HashSet<String> agentsInitialized, HashSet<String> agentsSuccessfulStarted) {
		
		int noOfAgents   = this.simulationManager.getNumberOfExpectedDeviceAgents();
		int agentsInit   = agentsInitialized.size();
		int agentsStarted = agentsSuccessfulStarted.size();
		
		String msg = "No. of Agents: " + noOfAgents + ", Agents initialized: " + agentsInit + ", Agents started: " + agentsStarted;
		
		// --- Find agents that are not initialized -----------------
		List<String> agentsNotInitialized = new ArrayList<>(agentsKnown);
		agentsNotInitialized.removeAll(agentsInitialized);
		if (agentsNotInitialized.size() > 0) {
			Collections.sort(agentsNotInitialized);
			msg += ", Not initialized: {" + String.join(", ", agentsNotInitialized) + "}";
		}
		
		// --- Find agents that are not initialized -----------------
		List<String> agentsNotStarted = new ArrayList<>(agentsKnown);
		agentsNotStarted.removeAll(agentsSuccessfulStarted);
		if (agentsNotStarted.size()>0) {
			Collections.sort(agentsNotStarted);
			msg += ", Not started: {" + String.join(", ", agentsNotStarted) + "}";
		}
		
		this.print("=> " + msg);
	}
	
	/**
	 * React on expired agent answers.
	 *
	 * @param agentsKnown the agents known
	 * @param agentAnswersAllSingle the agent answers all single
	 * @param agentAnswersMain the agent answers main
	 */
	private void reactOnExpiredAgentAnswers(HashSet<String> agentsKnown, Hashtable<String, Hashtable<AID, Object>> agentAnswersAllSingle, Hashtable<AID, Object> agentAnswersMain) {
	
		// --- Collector for agents that have already answered ------ 
		HashSet<AID> agentsAnswered = new HashSet<>();

		// ----------------------------------------------------------
		// --- Check the answers, available at the MainContainer ----
		agentsAnswered.addAll(agentAnswersMain.keySet());
		int noOfMainContainerAnswers = agentAnswersMain.size();
		String mainMsg = "Centrally collected answers: " + noOfMainContainerAnswers;  
		
		// ----------------------------------------------------------
		// --- Check the answers, available in container ------------
		String containerMsg = "";
		List<String> containerNameList = new ArrayList<>(agentAnswersAllSingle.keySet());
		Collections.sort(containerNameList);
		for (String containerName : containerNameList) {
			
			Hashtable<AID, Object> agentAnswerContainer = agentAnswersAllSingle.get(containerName);
			agentsAnswered.addAll(agentAnswerContainer.keySet());
			int noOfContainerAgentsAnswered  = agentAnswerContainer.size();
			if (containerMsg.isBlank()==false) {
				containerMsg += ", ";
			}
			containerMsg += containerName + "=" + noOfContainerAgentsAnswered; 
		}
		containerMsg = "answers in container: " + containerMsg;

		
		// ----------------------------------------------------------
		// --- Collect agent names that answered already ------------
		List<String> agentsAnsweredNameList = new ArrayList<>();
		agentsAnswered.forEach(agentAID -> agentsAnsweredNameList.add(agentAID.getLocalName()));
		
		// ----------------------------------------------------------
		// --- Determine the missing agents ------------------------- 
		String notAnswered = "";
		List<String> agentAnswersMissing = new ArrayList<>(agentsKnown);
		agentAnswersMissing.removeAll(agentsAnsweredNameList);
		if (agentAnswersMissing.size()>0) {
			Collections.sort(agentAnswersMissing);
			notAnswered += "Missing information from: {" + String.join(", ", agentAnswersMissing) + "}";
		}
		
		// --- Prepare output text ----------------------------------
		int noOfAgents = this.simulationManager.getNumberOfExpectedDeviceAgents();
		String msg = "No. of Agents: " + noOfAgents + ", No. of Agents that reacted: " + agentsAnswered.size() + ".";
		this.print("=> " + msg);
		this.print("=> " + mainMsg + ", " + containerMsg);
		if (notAnswered.isBlank()==false) this.print("=> " + notAnswered);
	}
	
	/**
	 * React on expired answer of iterating system.
	 *
	 * @param iteratingAgents the iterating agents
	 * @param iteratingSystemStateLog the iteration state log
	 */
	private void reactOnExpiredAnswerOfIteratingSystem(HashSet<String> iteratingAgents, HashMap<String, DiscreteSystemStateType> iteratingSystemStateLog) {
		
		List<String> expiredItSys = new ArrayList<>(iteratingAgents);
		for (String agentAnswered : iteratingSystemStateLog.keySet()) {
			expiredItSys.remove(agentAnswered);
		}

		String msg = expiredItSys.size() + " expired iteration answers from: " + String.join(", ", expiredItSys);
		this.print("=> " + msg);
	}
	
	/**
	 * React on expired final answer of iterating system.
	 *
	 * @param iterationStateLog the iteration state log
	 */
	private void reactOnExpiredFinalAnswerOfIteratingSystem(HashMap<String, DiscreteSystemStateType> iterationStateLog) {
		
		List<String> expiredFinalItSys = new ArrayList<>();
		for (String agentAnswered : iterationStateLog.keySet()) {
			DiscreteSystemStateType dssType = iterationStateLog.get(agentAnswered);
			if (dssType==null || dssType==DiscreteSystemStateType.Iteration) {
				expiredFinalItSys.add(agentAnswered);
			}
		}
		
		String msg = expiredFinalItSys.size() + " expired FINAL iteration answers from " + String.join(", ",  expiredFinalItSys);
		this.print("=> " + msg);
	}
	
	/**
	 * React on expired control behaviour RT state updates.
	 *
	 * @param expectedAgents the expected agents that should answer
	 * @param agentsAnswered the agent names that already answered
	 */
	private void reactOnExpiredControlBehaviourRTStateUpdate(HashSet<String> expectedAgents, HashSet<String> agentsAnswered) {
		
		List<String> pendingAnswers = new ArrayList<>(expectedAgents);
		pendingAnswers.removeAll(agentsAnswered);
		if (pendingAnswers.size()>0) {
			Collections.sort(pendingAnswers);
			String updateSnglPlur = pendingAnswers.size()==1 ? "update" : "updates";
			String msg = "Missing " + pendingAnswers.size() + " ControlBehaviourRT state " + updateSnglPlur + " from: {" + String.join(", ", pendingAnswers) + "}";
			this.print("=> " + msg);
		}
	}
	
	/**
	 * React on expired stop simulation.
	 *
	 * @param agentsKnown the agents known
	 * @param agentNotifications the agent notifications
	 */
	private void reactOnExpiredStopSimulation(HashSet<String> agentsKnown, Vector<EnvironmentNotification> agentNotifications) {
		
		List<String> expiredAgents = new ArrayList<>(agentsKnown);
		for (EnvironmentNotification envNote : agentNotifications) {
			expiredAgents.remove(envNote.getSender().getLocalName());
		}
		
		if (expiredAgents.size()>0) {
			Collections.sort(expiredAgents);
			String confirmationSnglPlur = expiredAgents.size()==1 ? "confirmation" : "confirmations";
			String msg = "Missing " + expiredAgents.size() + " " + confirmationSnglPlur + " of simulation finalization from: {" + String.join(", ", expiredAgents) + "}";
			this.print("=> " + msg);
		}
	}
	
}
