package de.enflexit.ea.core.configuration;

import java.util.List;

/**
 * The Interface SetupConfigurationService.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface SetupConfigurationAttribute <T> {
	
	/**
	 * Has to return a unique attribute name for the configuration attribute.
	 * @return the attribute name
	 */
	public String getAttributeName();
	
	/**
	 * Has to return the type of which the options are.
	 * @return the type
	 */
	public T getType();

	/**
	 * Has to return the configuration options for this configuration.
	 * @return the configuration options
	 */
	public List<T> getConfigurationOptions();
	
	
}
