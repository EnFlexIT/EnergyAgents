package de.enflexit.ea.core.aggregation;

import org.awb.env.networkModel.NetworkModel;

import energygroup.GroupController;

/**
 * The Class AbstractNetworkCalculationPreProcessor can be used to execute some individual calculations
 * just before the actual network calculation within a network calculation strategy
 * will be executed.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractNetworkCalculationPreprocessor {

	private AbstractAggregationHandler aggregationHandler;
	private AbstractSubNetworkConfiguration subAggregationConfiguration;
	
	
	/**
	 * Sets the aggregation handler.
	 * @param aggregationHandler the current parent aggregation handler
	 * 
	 * @see AbstractAggregationHandler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current parent aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Sets the current sub aggregation configuration.
	 * @param subAggregationConfiguration the new sub aggregation configuration
	 */
	public void setSubAggregationConfiguration(AbstractSubNetworkConfiguration subAggregationConfiguration) {
		this.subAggregationConfiguration = subAggregationConfiguration;
	}
	/**
	 * Returns the current sub aggregation configuration.
	 * @return the sub aggregation configuration
	 */
	public AbstractSubNetworkConfiguration getSubAggregationConfiguration() {
		return subAggregationConfiguration;
	}
	
	/**
	 * Returns the GroupController of the aggregation.
	 * @return the group controller
	 */
	public GroupController getGroupController() {
		return this.getSubAggregationConfiguration().getSubAggregationBuilder().getGroupController();
	}
	/**
	 * Returns the current overall network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return this.getAggregationHandler().getNetworkModel();
	}
	
	/**
	 * Does the preprocessing.
	 * @param evaluationEndTime the evaluation end time
	 */
	protected abstract boolean doPreprocessing(long evaluationEndTime);
	
}
