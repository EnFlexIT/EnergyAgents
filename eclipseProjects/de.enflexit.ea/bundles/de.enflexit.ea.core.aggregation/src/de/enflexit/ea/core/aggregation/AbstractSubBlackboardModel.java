package de.enflexit.ea.core.aggregation;

import java.io.Serializable;
import de.enflexit.ea.core.dataModel.blackboard.AbstractBlackboardAnswer;
import de.enflexit.ea.core.dataModel.blackboard.SingleRequestSpecifier;

/**
 * Abstract superclass for aggregation-specific blackboard models.
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public abstract class AbstractSubBlackboardModel implements Serializable {

	private static final long serialVersionUID = 8939125806080187613L;
	
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
	 * Checks if this SubBlackboardModel is responsible for the request.
	 * @param requestSpecifier the request specifier
	 * @return true, if is responsible for request
	 */
	public abstract boolean isResponsibleForRequest(SingleRequestSpecifier requestSpecifier);
	
	/**
	 * Returns the blackboard request answer for the specified request, or null if not responsible.
	 * @param request the BlackboardRequest 
	 * @return the blackboard request answer
	 */
	public abstract AbstractBlackboardAnswer getBlackboardRequestAnswer(SingleRequestSpecifier request);
	
}
