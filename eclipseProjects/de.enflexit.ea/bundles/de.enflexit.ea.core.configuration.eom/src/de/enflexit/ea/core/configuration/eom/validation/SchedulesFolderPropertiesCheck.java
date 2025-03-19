package de.enflexit.ea.core.configuration.eom.validation;

import de.enflexit.awb.core.project.Project;
import de.enflexit.awb.core.project.setup.SimulationSetup;
import de.enflexit.common.properties.Properties;
import de.enflexit.ea.core.configuration.eom.scheduleList.ScheduleListFile;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;

/**
 * Checks if the properties for CSV schedule folders are present in the project and setup properties.
 * If not found, empty properties are added.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class SchedulesFolderPropertiesCheck extends HyGridValidationAdapter {

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateProject(agentgui.core.project.Project)
	 */
	@Override
	public HyGridValidationMessage validateProject(Project project) {
		// --- Check if the schedules folder property is present in the project properties.
		this.checkForEntry(project.getProperties());
		return super.validateProject(project);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateSetup(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		// --- Check if the schedules folder property is present in the setup properties.
		if (setup!=null) {
			this.checkForEntry(setup.getProperties());
		}
		return super.validateSetup(setup);
	}
	
	/**
	 * Checks if there is a property for the schedules folder in the provided properties,
	 * adds an empty one if not.
	 * @param properties the properties
	 */
	private void checkForEntry(Properties properties) {
		// --- Check if there is a property for the schedules folder ----------
		if (properties.contains(ScheduleListFile.PROPERTIES_KEY_SCHEDULES_FOLDER)==false) {
			// --- If not, add an empty one -----------------------------------
			properties.setStringValue(ScheduleListFile.PROPERTIES_KEY_SCHEDULES_FOLDER, "");
		}
	}
	
}
