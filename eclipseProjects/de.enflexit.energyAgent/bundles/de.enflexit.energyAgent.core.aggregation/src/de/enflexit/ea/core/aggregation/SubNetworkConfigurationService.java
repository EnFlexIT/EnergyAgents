package de.enflexit.ea.core.aggregation;

/**
 * This interface defines methods for providing SubNetworkConfigurations via an OSGI service.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public interface SubNetworkConfigurationService {
	
	/**
	 * Gets ID of the domain 
	 * @return the domain ID
	 */
	public String getDomainID();
	/**
	 * The name of the class providing the 
	 * @return
	 */
	public Class<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationCass();
}
