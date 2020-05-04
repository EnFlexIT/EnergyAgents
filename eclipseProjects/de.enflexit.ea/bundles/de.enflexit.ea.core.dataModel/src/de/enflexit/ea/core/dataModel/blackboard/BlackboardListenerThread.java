package de.enflexit.ea.core.dataModel.blackboard;

import java.util.List;

import de.enflexit.common.ServiceFinder;

/**
 * The Class BlackboardListenerThread manages the registered {@link BlackboardListenerService}s
 * and will organize their execution.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BlackboardListenerThread extends Thread {

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
			if (this.getBlackboard().isDoTerminate()==true) break;
			
			// --- Notify registered new Blackboard state -----------
			for (int i = 0; i < this.getBlackboardListenerServiceList().size(); i++) {
				try {
					this.getBlackboardListenerServiceList().get(i).onNetworkCalculationDone(this.getBlackboard());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			// --- Terminate thread? --------------------------------
			if (this.getBlackboard().isDoTerminate()==true) break;
		}
		
	}
	
}
