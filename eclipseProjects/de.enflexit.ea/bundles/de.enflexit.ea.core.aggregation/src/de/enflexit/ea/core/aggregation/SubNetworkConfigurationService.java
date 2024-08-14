package de.enflexit.ea.core.aggregation;

import java.util.List;

/**
 * This interface defines methods for providing SubNetworkConfigurations via an OSGI service.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public interface SubNetworkConfigurationService {
	
	/**
	 * Has to return the list of domains that are covered by the current service.  
	 * @return the domain ID
	 */
	public List<String> getDomainIdList();
	
	/**
	 * Has to return the class that provides a sub network configuration.
	 * @return the sub network configuration class
	 */
	public Class<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationClass();
	
	/**
	 * Has to return the task thread coordinator to be used for the current service and its domains.<br>
	 * => For example: a task thread coordinator will be used to coordinate the network calculations within 
	 * an electrical grid, by considering the interdependencies between low- and hibgh-voltage levels. 
	 * 
	 * @return the task thread coordinator or <code>null</code> if such coordinator is not necessary
	 */
	public Class <? extends AbstractTaskThreadCoordinator> getTaskThreadCoordinator();

			
}
