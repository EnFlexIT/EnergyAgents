package de.enflexit.ea.core.aggregation;

import java.util.List;

/**
 * This interface defines methods for providing SubNetworkConfigurations via an OSGI service.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public interface SubNetworkConfigurationService {
	
	/**
	 * Has to return the list of domains that 
	 * @return the domain ID
	 */
	public List<String> getDomainIdList();
	
	/**
	 * The name of the class providing the 
	 * @return
	 */
	public Class<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationClass();
}
