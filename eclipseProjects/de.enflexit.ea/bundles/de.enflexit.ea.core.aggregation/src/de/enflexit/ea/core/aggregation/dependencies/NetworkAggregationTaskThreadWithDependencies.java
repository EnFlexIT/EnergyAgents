package de.enflexit.ea.core.aggregation.dependencies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.NetworkAggregationTaskThread;


/**
 * {@link NetworkAggregationTaskThread} subclass with additional dependency handling.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class NetworkAggregationTaskThreadWithDependencies extends NetworkAggregationTaskThread implements PropertyChangeListener {
	
	private static final String CALCULATION_DONE = "CalculationDone";
	
	private ArrayList<NetworkAggregationTaskThreadWithDependencies> myDependencies;
	private ArrayList<PropertyChangeListener> dependingOnMe;
	
	private ReentrantLock waitingLock;
	private Condition waitingCondition;
	
	private HashMap<NetworkAggregationTaskThreadWithDependencies, Boolean> resultsAvailable;
	
	/**
	 * Instantiates a new network aggregation task thread with dependencies.
	 * @param aggregationHandler the aggregation handler
	 * @param subNetConfig the sub network configuration
	 * @param executerThreadName the executer thread name
	 */
	public NetworkAggregationTaskThreadWithDependencies(AbstractAggregationHandler aggregationHandler, AbstractSubNetworkConfiguration subNetConfig, String executerThreadName) {
		super(aggregationHandler, subNetConfig, executerThreadName);
	}
	
	/**
	 * Gets the list of all aggregation task threads this thread depends on.
	 * @return the my dependencies
	 */
	private ArrayList<NetworkAggregationTaskThreadWithDependencies> getMyDependencies() {
		if (myDependencies==null) {
			myDependencies = new ArrayList<>();
		}
		return myDependencies;
	}
	
	/**
	 * This HashMap stores for each dependency if the results are available.
	 * @return the results available
	 */
	private HashMap<NetworkAggregationTaskThreadWithDependencies, Boolean> getResultsAvailable() {
		if (resultsAvailable==null) {
			resultsAvailable = new HashMap<>();
		}
		return resultsAvailable;
	}
	
	/**
	 * Adds a new dependency for this thread.
	 * @param dependency the dependency
	 */
	public void addDependency(NetworkAggregationTaskThreadWithDependencies dependency) {
		this.getMyDependencies().add(dependency);
		dependency.addListener(this);
		this.resultsAvailable.put(dependency, false);
	}
	
	/**
	 * Checks if all dependencies are available, waits if not.
	 */
	private void waitIfNecessary() {
		while (this.allDependenciesAvailable()==false) {
			this.getWaitingLock().lock();
			try {
				this.getWaitingCondition().await();
			} catch (InterruptedException e) {
				System.err.println("[" + this.getClass().getSimpleName() + "] " + this.getName() + " was interrupted when waiting for dependencies!");
			} finally {
				this.getWaitingLock().unlock();
			}
		}
	}
	
	/**
	 * Checks if all dependencies are available.
	 * @return true, if successful
	 */
	private boolean allDependenciesAvailable() {
		boolean oneNotAvailable = this.getResultsAvailable().values().contains(Boolean.FALSE);
		return (oneNotAvailable==false);
	}
	

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.NetworkAggregationTaskThread#runEvaluationUntil(long, boolean)
	 */
	@Override
	public void runEvaluationUntil(long timeUntil, boolean rebuildDecisionGraph) {
		this.waitIfNecessary();
		super.runEvaluationUntil(timeUntil, rebuildDecisionGraph);
		this.notifyDependingThreads();
		this.resetResultsList();
	}
	

	/**
	 * Resets the results list, i.e. sets all entries to false.
	 */
	private void resetResultsList() {
		for (NetworkAggregationTaskThreadWithDependencies dependency : this.myDependencies) {
			this.getResultsAvailable().put(dependency, false);
		}
	}
	
	/**
	 * Adds a new listener to this task thread.
	 * @param listener the listener
	 */
	public void addListener(PropertyChangeListener listener) {
		this.getDependingOnMe().add(listener);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		if (pce.getPropertyName().equals(CALCULATION_DONE)) {
			NetworkAggregationTaskThreadWithDependencies source = (NetworkAggregationTaskThreadWithDependencies) pce.getSource();
			if (this.getMyDependencies().contains(source)) {
				this.getResultsAvailable().put(source, Boolean.TRUE);
				this.getWaitingCondition().signal();
			}
		}
	}
	
	/**
	 * Gets the list of task threads depending on this thread-
	 * @return the depending on me
	 */
	private ArrayList<PropertyChangeListener> getDependingOnMe() {
		if (dependingOnMe==null) {
			dependingOnMe = new ArrayList<>();
		}
		return dependingOnMe;
	}
	
	/**
	 * Notifies the depending threads.
	 */
	private void notifyDependingThreads() {
		this.getWaitingLock().lock();
		PropertyChangeEvent calculationDone = new PropertyChangeEvent(this, CALCULATION_DONE, null, null);
		for (PropertyChangeListener listener : this.getDependingOnMe()) {
			listener.propertyChange(calculationDone);
		}
		this.getWaitingLock().unlock();
	}
	
	/**
	 * Gets the waiting lock.
	 * @return the waiting lock
	 */
	private ReentrantLock getWaitingLock() {
		if (this.waitingLock==null) {
			waitingLock = new ReentrantLock();
		}
		return waitingLock;
	}
	
	/**
	 * Gets the waiting condition.
	 * @return the waiting condition
	 */
	private Condition getWaitingCondition() {
		if (waitingCondition==null) {
			waitingCondition = this.getWaitingLock().newCondition();
		}
		return waitingCondition;
	}

}
