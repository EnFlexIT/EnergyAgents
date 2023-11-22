package de.enflexit.ea.core.configuration.eom;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.core.configuration.SetupConfigurationAttribute;
import de.enflexit.ea.core.configuration.SetupConfigurationService;

/**
 * The Class EomSetupConfigurationService provides .
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSetupConfigurationService implements SetupConfigurationService {

	private List<SetupConfigurationAttribute<?>> attributeList;
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationService#getConfigurationAttributeList()
	 */
	@Override
	public List<SetupConfigurationAttribute<?>> getConfigurationAttributeList() {
		if (attributeList==null) {
			attributeList = new ArrayList<>();
			attributeList.add(new EomSetupConfiguration());
		}
		return attributeList;
	}

}
