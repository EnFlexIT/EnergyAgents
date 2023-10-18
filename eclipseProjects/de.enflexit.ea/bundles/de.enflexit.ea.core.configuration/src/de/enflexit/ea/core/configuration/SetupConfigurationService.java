package de.enflexit.ea.core.configuration;

import java.util.List;

/**
 * The Interface SetupConfigurationService.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface SetupConfigurationService {

	/**
	 * Has to return the configuration attribute list.
	 * @return the configuration attribute list
	 */
	public List<SetupConfigurationAttribute<?>> getConfigurationAttributeList();
	
}
