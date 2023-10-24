package de.enflexit.ea.core.configuration;

import java.util.List;

import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;

/**
 * The Interface SetupConfigurationService.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface SetupConfigurationAttribute<T> {
	
	/**
	 * Has to return a unique attribute name for the configuration attribute.
	 * @return the attribute name
	 */
	public String getColumnHeader();
	
	/**
	 * Has to return the description of the current attribute.
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * Has to return the type of which the options are.
	 * @return the type
	 */
	public Class<? extends T> getType();

	/**
	 * Enables to rise that the specified potential {@link ConfigurableComponent} will be configured by the current {@link SetupConfigurationAttribute}.
	 *
	 * @param cComponent the {@link ConfigurableComponent} to check
	 * @return true, if the current component can be configured by the current attribute
	 */
	public boolean willBeConfigured(ConfigurableComponent cComponent);
	
	/**
	 * Has to return the configuration options for this configuration.
	 * @return the configuration options
	 */
	public List<T> getConfigurationOptions();

	/**
	 * Has to return the currently configured value.
	 *
	 * @param cComponent the {@link ConfigurableComponent} to check
	 * @return the value
	 */
	public T getValue(ConfigurableComponent cComponent);
	
}
