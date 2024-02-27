package de.enflexit.ea.core.configuration.eom.validation;

import java.util.ArrayList;

import de.enflexit.ea.core.configuration.eom.EomSetupConfigurationService;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationService;

/**
 * This {@link HyGridValidationService} implementation specifies validation checks
 * related to the {@link EomSetupConfigurationService}. 
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class EomSetupConfigurationValidationService implements HyGridValidationService {
	
	private ArrayList<HyGridValidationAdapter> validationChecks;

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationService#getHyGridValidationChecks(boolean)
	 */
	@Override
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation) {
		if (validationChecks==null) {
			validationChecks = new ArrayList<>();
			validationChecks.add(new SchedulesFolderPropertiesCheck());
		}
		return validationChecks;
	}

}
