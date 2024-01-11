package de.enflexit.ea.core.configuration.eom.scheduleList;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.core.configuration.SetupConfigurationAttribute;
import de.enflexit.ea.core.configuration.SetupConfigurationService;

/**
 * Configuration service for specifying files to load schedule lists from. 
 */
public class ScheduleListConfigurationService implements SetupConfigurationService {
	
	/**
	 * If just a file name is specified, it will be assumed to be located in this sub directory of the setup's EOM files directory.
	 */
	protected static final String SCHEDULES_DEFAULT_SUBDIR = "schedules";
	
	private ArrayList<SetupConfigurationAttribute<?>> attributeList;

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationService#getConfigurationAttributeList()
	 */
	@Override
	public List<SetupConfigurationAttribute<?>> getConfigurationAttributeList() {
		if (attributeList==null) {
			attributeList = new ArrayList<>();
			attributeList.add(new ScheduleListFile());
		}
		return attributeList;
	}

}
