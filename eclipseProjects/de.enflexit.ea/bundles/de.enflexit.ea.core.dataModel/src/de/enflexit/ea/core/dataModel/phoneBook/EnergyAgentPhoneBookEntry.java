package de.enflexit.ea.core.dataModel.phoneBook;

import de.enflexit.jade.phonebook.AbstractPhoneBookEntry;

/**
 * This basic implementation of {@link AbstractPhoneBookEntry} can be used if an AID is all you need.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class EnergyAgentPhoneBookEntry extends AbstractPhoneBookEntry {

	private static final long serialVersionUID = -7075326381062693539L;
	
	private String componentType;

	/* (non-Javadoc)
	 * @see de.enflexit.jade.phonebook.AbstractPhoneBookEntry#getUniqueIdentifier()
	 */
	@Override
	public String getUniqueIdentifier() {
		return this.getAgentAID().getName();
	}

	/* (non-Javadoc)
	 * @see de.enflexit.jade.phonebook.AbstractPhoneBookEntry#isValid()
	 */
	@Override
	public boolean isValid() {
		return true;
	}

	/**
	 * Gets the component type.
	 * @return the component type
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * Sets the component type.
	 * @param componentType the new component type
	 */
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	
	

}
