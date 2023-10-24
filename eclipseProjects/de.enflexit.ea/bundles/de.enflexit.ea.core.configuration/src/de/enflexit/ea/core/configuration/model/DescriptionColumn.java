package de.enflexit.ea.core.configuration.model;

import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;

/**
 * The Class DescriptionColumn provides a unique way to add a description column to the {@link SetupConfigurationModel}.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class DescriptionColumn extends SetupConfigurationAttributeService{

	private String columnDescription;
	
	/**
	 * Instantiates a new description column.
	 * @param description the description
	 */
	public DescriptionColumn(String description) {
		super(null, null);
		this.columnDescription = description;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttributeService#getServiceName()
	 */
	@Override
	public String getServiceName() {
		return null;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttributeService#getAttributeName()
	 */
	@Override
	public String getAttributeName() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttributeService#toString()
	 */
	@Override
	public String toString() {
		return columnDescription;
	}
	
}
