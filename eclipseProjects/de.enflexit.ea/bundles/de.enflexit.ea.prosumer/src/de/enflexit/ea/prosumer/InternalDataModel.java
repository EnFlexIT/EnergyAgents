package de.enflexit.ea.prosumer;

import java.io.File;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookEntry;
import de.enflexit.jade.phonebook.PhoneBook;

/**
 * The Class InternalDataModel represents the whole internal data model of the corresponding agent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class InternalDataModel extends AbstractInternalDataModel<EnergyAgentPhoneBookEntry> {

	private static final long serialVersionUID = 8589606262871989270L;

	/**
	 * Instantiates a new internal data model.
	 * @param myAgent the my agent
	 */
	public InternalDataModel(AbstractEnergyAgent myAgent) {
		super(myAgent);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractInternalDataModel#loadPhoneBookFromFile(java.io.File)
	 */
	@Override
	protected PhoneBook<EnergyAgentPhoneBookEntry> loadPhoneBookFromFile(File phoneBookFile) {
		return PhoneBook.loadPhoneBook(phoneBookFile, EnergyAgentPhoneBookEntry.class);
	}

	// --- To be extended ---
	
}
