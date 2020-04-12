package de.enflexit.ea.core.centralExecutiveAgent.validation;

import java.util.ArrayList;

import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationService;

/**
 * CeaValidationService implementation for checking if the CEA is configured correctly.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class CeaValidationService implements HyGridValidationService {

	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationService#getHyGridValidationChecks(boolean)
	 */
	@Override
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation) {
		
		ArrayList<HyGridValidationAdapter> validationChecks = new ArrayList<>();
		validationChecks.add(new CeaClassNameChecks());
		return validationChecks;
	}


}
