package de.enflexit.ea.core.configuration.eom.systems;

import de.enflexit.ea.core.configuration.eom.BundleHelper;

/**
 * The Class SystemConfigurationManager.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SystemConfigurationManager {

	private SystemConfiguration systemConfiguration;
	
	/**
	 * Instantiates a new system configuration manager.
	 */
	public SystemConfigurationManager() {
		this.initialize();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		// --- Load SystemConfiguration -------------------
		this.setSystemConfiguration(SystemConfiguration.load(BundleHelper.getSystemConfigurationFile()));
	}
	
	/**
	 * Sets the system configuration.
	 * @param systemConfiguration the new system configuration
	 */
	private void setSystemConfiguration(SystemConfiguration systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}
	/**
	 * Returns the current SystemConfiguration.
	 * @return the system configuration
	 */
	public SystemConfiguration getSystemConfiguration() {
		if (systemConfiguration==null) {
			systemConfiguration = new SystemConfiguration();
		}
		return systemConfiguration;
	}
	/**
	 * Saves the current settings.
	 */
	public void saveSettings() {
		this.getSystemConfiguration().save();
	}
	
	
	
}
