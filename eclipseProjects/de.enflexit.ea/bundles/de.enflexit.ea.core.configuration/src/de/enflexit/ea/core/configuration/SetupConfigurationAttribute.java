package de.enflexit.ea.core.configuration;

import java.util.List;

import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;

/**
 * The Interface SetupConfigurationAttribute provides the corpus of methods to work
 * on single value within a AWB / Energy Agent project.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 * @param <T> the generic type that has to be a data primitive
 */
public interface SetupConfigurationAttribute<T> {
	
	/**
	 * Has to return a unique attribute name for the configuration attribute.
	 * 
	 * @return the attribute name
	 */
	public String getColumnHeader();
	
	/**
	 * Has to return the description of the current attribute.
	 * 
	 * @return the description
	 */
	public String getDescription();
	
	/**
	 * Has to return the type of which the values and options are.
	 * @return the type
	 */
	public Class<? extends T> getType();

	/**
	 * Has to return the configuration options for this configuration attribute.
	 * @return the configuration options
	 */
	public List<T> getConfigurationOptions();

	/**
	 * Enables to indicate that the specified potential {@link ConfigurableComponent} will be configured by the current {@link SetupConfigurationAttribute}.
	 *
	 * @param cComponent the {@link ConfigurableComponent} to check
	 * @return true, if the current component can be configured by the current attribute
	 */
	public boolean willBeConfigured(ConfigurableComponent cComponent);
	
	/**
	 * Has to return the currently configured value form the designated component or object.
	 *
	 * @param cComponent the {@link ConfigurableComponent} to check
	 * @return the value
	 */
	public T getValue(ConfigurableComponent cComponent);

	/**
	 * Has to set the specified new value to the designated component or object.
	 *
	 * @param cComponent the {@link ConfigurableComponent} to work on
	 * @param newValue the new value to set
	 */
	public void setValue(ConfigurableComponent cComponent, Object newValue);
	
}
