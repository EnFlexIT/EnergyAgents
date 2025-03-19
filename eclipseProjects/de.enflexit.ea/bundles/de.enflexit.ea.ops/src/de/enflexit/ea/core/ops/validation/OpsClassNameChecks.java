package de.enflexit.ea.core.ops.validation;

import java.util.Vector;

import de.enflexit.awb.core.project.Project;
import de.enflexit.ea.core.ops.plugin.OpsPlugin;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * This class implements the validation of the OPS class names
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class OpsClassNameChecks extends HyGridValidationAdapter {

	private static final String OPS_PLUGIN_CLASS_NAME_OLD = "hygrid.ops.plugin.OpsPlugin";
	
	@Override
	public HyGridValidationMessage validateProjectAfterFileLoad(Project project) {
	
		HyGridValidationMessage vMessage = null;
		
		Vector<String> pluginClassNames = project.getPluginClassNames();
		for (int i = 0; i < pluginClassNames.size(); i++) {
			String pluginClassName = pluginClassNames.get(i);
			if (pluginClassName.equals(OPS_PLUGIN_CLASS_NAME_OLD) == true) {
				pluginClassNames.set(i, OpsPlugin.class.getName());
				// --- Create inform message ------------------------
				vMessage = this.getClassNameChangedMessage();
				this.printHyGridValidationMessageToConsole(vMessage);
			}
		}
		return vMessage;
	}
	
	/**
	 * Returns a default HyGridValidationMessage that the manages class name was changed.
	 * @return the class name changed message
	 */
	private HyGridValidationMessage getClassNameChangedMessage() {
		String message = "Adjusted class name for OPS-Plugin.";
		String description = "Changed class name from '" + OPS_PLUGIN_CLASS_NAME_OLD + "' to '" + OpsPlugin.class.getName() + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
