package de.enflexit.ea.core.blackboard;

import java.util.List;

import de.enflexit.common.ServiceFinder;

/**
 * The Class BlackboardListenerThread manages the registered {@link BlackboardListenerService}s
 * and will organize their execution.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BlackboardListenerThread extends Thread {

	private enum Job {
		DoneNetworkCalculation,
		DoneSimulation
	}
	
	private Blackboard blackboard;
	private List<BlackboardListenerService> blackboardListenerServiceList; 
	
	
	public BlackboardListenerThread() {
		this.setName(this.getClass().getSimpleName());
	}
	
	/**
	 * Returns the blackboard instance.
	 * @return the blackboard
	 */
	private Blackboard getBlackboard() {
		if (blackboard==null) {
			blackboard = Blackboard.getInstance();
		}
		return blackboard;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		super.run();
		
		while(true) {
			
			// --- Wait for the next restart call -------------------
			try {
				synchronized (this.getBlackboard().getNotificationTrigger()) {
					this.getBlackboard().getNotificationTrigger().wait();	
				}
				
			} catch (IllegalMonitorStateException | InterruptedException imse) {
			}
			
			// --- Terminate thread? --------------------------------
			if (this.getBlackboard().isDoTerminate()==true) {
				this.notifyServices(Job.DoneSimulation);
				break;
			}
			
			// --- Notify about new Blackboard state ----------------
			this.notifyServices(Job.DoneNetworkCalculation);
			
			// --- Terminate thread? --------------------------------
			if (this.getBlackboard().isDoTerminate()==true) {
				this.notifyServices(Job.DoneSimulation);
				break;
			}
		}
		
	}
	
	
	/**
	 * Returns the list of registered {@link BlackboardListenerService}s.
	 * @return the blackboard listener service list
	 */
	private List<BlackboardListenerService> getBlackboardListenerServiceList() {
		if (blackboardListenerServiceList==null) {
			blackboardListenerServiceList = ServiceFinder.findServices(BlackboardListenerService.class);;
		}
		return blackboardListenerServiceList;
	}
	/**
	 * Notifies all services.
	 * @param job the job to be called on the service interfaces
	 */
	private void notifyServices(Job job) {
		
		for (int i = 0; i < this.getBlackboardListenerServiceList().size(); i++) {
			try {
				switch (job) {
				case DoneNetworkCalculation:
					this.getBlackboardListenerServiceList().get(i).onNetworkCalculationDone(this.getBlackboard());
					break;

				case DoneSimulation:
					this.getBlackboardListenerServiceList().get(i).onSimulationDone();
					break;
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
