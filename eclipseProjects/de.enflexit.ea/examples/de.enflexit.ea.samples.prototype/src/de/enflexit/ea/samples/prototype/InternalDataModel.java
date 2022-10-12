package de.enflexit.ea.samples.prototype;

import java.io.File;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookEntry;
import de.enflexit.jade.phonebook.PhoneBook;

/**
 * The Class InternalDataModel represents the internal data model of the corresponding agent.
 * 
 * It is advised to only specify 'protected final String' elements and use these 
 * (within IOSimulated or IOReal) when assigning values to the fixedVariableListMeasurements
 * (within AbstractInternalDataModel).  
 * 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class InternalDataModel extends AbstractInternalDataModel<EnergyAgentPhoneBookEntry> {


	private static final long serialVersionUID = 3913554312467337020L;

	/** Specification of the internal data model's elements */
	
	/**
	 * Instantiates a new internal data model.
	 * @param agent the agent
	 */
	public InternalDataModel(AbstractEnergyAgent agent) {
		super(agent);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractInternalDataModel#loadPhoneBookFromFile(java.io.File)
	 */
	@Override
	protected PhoneBook<EnergyAgentPhoneBookEntry> loadPhoneBookFromFile(File phoneBookFile) {
		return PhoneBook.loadPhoneBook(phoneBookFile, EnergyAgentPhoneBookEntry.class);
	}
	
}
