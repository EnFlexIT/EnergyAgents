package de.enflexit.ea.core.aggregation;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AbstractTaskThreadCoordinator.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public abstract class AbstractTaskThreadCoordinator {

	private AbstractAggregationHandler aggregationHandler;
	private List<String> domainList;
	
	
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
	 * Returns the list of domains for which this coordinator is responsible.
	 * @return the domain list
	 */
	public List<String> getDomainList() {
		if (domainList==null) {
			domainList = new ArrayList<>();
		}
		return domainList;
	}
	/**
	 * Register domains.
	 * @param domainsToRegister the domains to register
	 */
	public void registerDomains(List<String> domainsToRegister) {
		
		if (this.getDomainList().size()==0) {
			this.getDomainList().addAll(domainsToRegister);
		} else {
			for (String newDomain : domainsToRegister) {
				if (this.getDomainList().contains(newDomain)==false) {
					this.getDomainList().add(newDomain);
				}
			}
		}
	}
	
	/**
	 * Will be called to initialize a coordinator ahead of the actual usage during runtime.
	 */
	public abstract void initialize();
	
	/**
	 * Has to check and return, if network calculations need to be restarted.
	 */
	public abstract boolean requiresNetworkCalculationRestart();
	
	
}
