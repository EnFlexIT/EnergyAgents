package de.enflexit.ea.prosumer.validation;

import java.util.ArrayList;

import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationService;

/**
 * HyGridValidationService implementation for checking if a Prosumer agent is configured correctly.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class ProsumerValidationService implements HyGridValidationService {

	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationService#getHyGridValidationChecks(boolean)
	 */
	@Override
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation) {
		
		ArrayList<HyGridValidationAdapter> validationChecks = new ArrayList<>();
		validationChecks.add(new ProsumerClassNameChecks());
		return validationChecks;
	}


}
