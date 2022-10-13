package de.enflexit.ea.core.dataModel.phoneBook;

import de.enflexit.jade.phonebook.search.PhoneBookSearchFilter;
import jade.core.AID;

/**
 * {@link PhoneBookSearchFilter} implementation for {@link EnergyAgentPhoneBookEntry}s.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class EnergyAgentPhoneBookSearchFilter implements PhoneBookSearchFilter<EnergyAgentPhoneBookEntry> {

	private static final long serialVersionUID = 595932542434110722L;
	
	private AID agentAID;
	private String localName;
	private String componentType;
	
	/**
	 * This factory method creates a search filter that matches the provided AID. 
	 * @param aid the aid
	 * @return the search filter
	 */
	public static EnergyAgentPhoneBookSearchFilter matchAID(AID aid) {
		EnergyAgentPhoneBookSearchFilter filter = new EnergyAgentPhoneBookSearchFilter();
		filter.setAgentAID(aid);
		return filter;
	}
	
	/**
	 * This factory method creates a search filter that matches the provided local name.
	 * @param localName the local name
	 * @return the search filter
	 */
	public static EnergyAgentPhoneBookSearchFilter matchLocalName(String localName) {
		EnergyAgentPhoneBookSearchFilter filter = new EnergyAgentPhoneBookSearchFilter();
		filter.setLocalName(localName);
		return filter;
	}
	
	/**
	 * This factory method creates a search filter that matches the provided component type.
	 * @param componentType the component type
	 * @return the search filter
	 */
	public static EnergyAgentPhoneBookSearchFilter matchComponentType(String componentType) {
		EnergyAgentPhoneBookSearchFilter filter = new EnergyAgentPhoneBookSearchFilter();
		filter.setComponentType(componentType);
		return filter;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.jade.phonebook.search.PhoneBookSearchFilter#matches(de.enflexit.jade.phonebook.AbstractPhoneBookEntry)
	 */
	@Override
	public boolean matches(EnergyAgentPhoneBookEntry entry) {
		
		// --- If the AID filter was set, check for match -----------
		if (this.getAgentAID()!=null) {
			if (entry.getAgentAID()==null || entry.getAgentAID().equals(this.getAgentAID())==false) {
				// --- Check failed -> no match ---------------------
				return false;
			}
		}
		
		// --- If the local name filter was set, check for a match --
		if (this.getLocalName()!=null) {
			if (entry.getAgentAID()==null || entry.getAgentAID().getLocalName().equals(this.getLocalName())==false) {
				// --- Check failed -> no match ---------------------
				return false;
			}
			
		}
		// --- If the component type filter was set, check for a match
		if (this.getComponentType()!=null) {
			EnergyAgentPhoneBookEntry eaEntry = (EnergyAgentPhoneBookEntry) entry;
			if (eaEntry.getComponentType()==null || eaEntry.getComponentType().equals(this.getComponentType())==false) {
				// --- Check failed -> no match ---------------------
				return false;
			}
		}

		// --- All checks passed -> match ---------------------------
		return true;
	}
	
	/**
	 * Gets the agent AID.
	 * @return the agent AID
	 */
	public AID getAgentAID() {
		return agentAID;
	}
	/**
	 * Sets the agent AID.
	 * @param agentAID the new agent AID
	 */
	public void setAgentAID(AID agentAID) {
		this.agentAID = agentAID;
	}
	
	/**
	 * Gets the local name.
	 * @return the local name
	 */
	public String getLocalName() {
		return localName;
	}
	/**
	 * Sets the local name.
	 * @param localName the new local name
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
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
