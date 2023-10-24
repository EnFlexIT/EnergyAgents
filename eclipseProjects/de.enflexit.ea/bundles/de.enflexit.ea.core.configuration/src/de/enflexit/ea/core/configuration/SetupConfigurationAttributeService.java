package de.enflexit.ea.core.configuration;

/**
 * The Class SetupConfigurationAttributeService serves as container for the combination
 * of a {@link SetupConfigurationService} and the List of {@link SetupConfigurationAttributeService}s.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationAttributeService implements Comparable<SetupConfigurationAttributeService> {

	private SetupConfigurationService service;
	private SetupConfigurationAttribute<?> attribute;
	
	/**
	 * Instantiates a new configuration service attribute.
	 *
	 * @param service the setup configuration service
	 * @param attribute the setup configuration attribute
	 */
	public SetupConfigurationAttributeService(SetupConfigurationService service, SetupConfigurationAttribute<?> attribute) {
		this.service = service;
		this.attribute = attribute;
	}
	/**
	 * Returns the setup configuration service.
	 * @return the setup configuration service
	 */
	public SetupConfigurationService getSetupConfigurationService() {
		return service;
	}
	/**
	 * Returns the setup configuration attribute.
	 * @return the setup configuration attribute
	 */
	public SetupConfigurationAttribute<?> getSetupConfigurationAttribute() {
		return attribute;
	}
	/**
	 * Returns the simple service name.
	 * @return the service name
	 */
	public String getServiceName() {
		return this.getSetupConfigurationService().getClass().getSimpleName();
	}
	/**
	 * Returns the attribute name.
	 * @return the attribute name
	 */
	public String getAttributeName() {
		return this.getSetupConfigurationAttribute().getColumnHeader();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getServiceName() + " - " + this.getAttributeName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SetupConfigurationAttributeService scsa) {
		return this.toString().toLowerCase().compareTo(scsa.toString().toLowerCase());
	}
	
}
