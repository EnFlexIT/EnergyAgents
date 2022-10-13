package de.enflexit.ea.prosumer;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookEntry;

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

	@Override
	protected Class<EnergyAgentPhoneBookEntry> getPhoneBookEntryClass() {
		return EnergyAgentPhoneBookEntry.class;
	}

	// --- To be extended ---
	
}
