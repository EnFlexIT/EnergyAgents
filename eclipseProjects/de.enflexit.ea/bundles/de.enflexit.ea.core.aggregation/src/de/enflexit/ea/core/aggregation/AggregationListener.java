package de.enflexit.ea.core.aggregation;

import de.enflexit.awb.simulation.transaction.DisplayAgentNotification;

/**
 * This interface must be implemented by classes that want to react 
 * on network calculation results.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public interface AggregationListener {
	
	/**
	 * This method will be invoked, if all network calculations are done. This is controlled
	 * by the execution method of the {@link AbstractAggregationHandler}.
	 * 
	 * @see AbstractAggregationHandler#runEvaluationUntil(long)
	 */
	public void networkCalculationDone();
	
	
	/**
	 * Will be invoked, if the network display updates were prepared by a display updater.
	 * The call of this method is scheduled after the execution of the network calculations.
	 *
	 * @see AbstractNetworkModelDisplayUpdater
	 * @see AbstractSubNetworkConfiguration#getNetworkDisplayUpdaterClass()
	 * @see AbstractAggregationHandler#runEvaluationUntil(long)
	 * 
	 * @param displayNotification the display notification
	 */
	public void sendDisplayAgentNotification(DisplayAgentNotification displayNotification);
}
