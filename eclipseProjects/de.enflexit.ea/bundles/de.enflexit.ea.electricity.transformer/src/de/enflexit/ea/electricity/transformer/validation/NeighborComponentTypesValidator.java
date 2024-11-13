package de.enflexit.ea.electricity.transformer.validation;

import agentgui.core.project.Project;
import de.enflexit.common.properties.Properties;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.electricity.transformer.TransformerTotalCurrentCalculation;

/**
 * This validator checks if the possible types for components adjacent to the transformer
 * are configured in the project properties. If not, a set of default types is added.     
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class NeighborComponentTypesValidator extends HyGridValidationAdapter {
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateProject(agentgui.core.project.Project)
	 */
	@Override
	public HyGridValidationMessage validateProject(Project project) {
		if (project!=null) {
			// --- Get the entry from the project properties ------------------
			Properties projectProperties = project.getProperties();
			String propertiesEntry = projectProperties.getStringValue(TransformerTotalCurrentCalculation.NEIGHBOUR_COMPONENT_TYPES_PROPERTY_KEY);
			if (propertiesEntry==null || propertiesEntry.isBlank()==true) {
				// --- If  the entry is missing or empty, add the default types ---------
				String defaultEntry = getDefaultEntry();
				projectProperties.setStringValue(TransformerTotalCurrentCalculation.NEIGHBOUR_COMPONENT_TYPES_PROPERTY_KEY, getDefaultEntry());
				
				// --- Return an information message that the defaults were added -------
				String messageContent = "In the project properties, there was no entry for the possible types component adjacent to the transformer. The default entry was  added, which contains " + defaultEntry + ".";
				HyGridValidationMessage message = new HyGridValidationMessage("Added default entry for transformer neighbour components", MessageType.Information, messageContent);
				return message;
			}
		}
		return super.validateProject(project);
	}
	
	/**
	 * Gets the default entry.
	 * @return the default entry
	 */
	private static String getDefaultEntry() {
		return String.join(",", TransformerTotalCurrentCalculation.DEFAULT_NEIGHBOUR_COMPOENNT_TYPES);
	}
}
