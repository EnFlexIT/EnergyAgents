package de.enflexit.ea.core.configuration;

import javax.swing.JComponent;

/**
 * The Interface SetupConfigurationAttributeWithUI.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 * @param <T> the generic type that has to be a data primitive
 */
public interface SetupConfigurationAttributeWithUI<T> extends SetupConfigurationAttribute<T> {

	/**
	 * Has to return a general configuration panel for the current attribute.
	 * @return the configuration panel
	 */
	public JComponent getAttributeConfigurationPanel();
	
	/**
	 * Has to to everything to clean up memory and in order to dispose the attribute configuration panel.
	 */
	public void disposeAttributeConfigurationPanel();
	
}
