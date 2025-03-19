package de.enflexit.ea.deployment.validation;

import java.util.Vector;

import de.enflexit.awb.core.project.Project;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.deployment.plugin.DeploymentPlugIn;

/**
 * This class implements the validation of the deployment class names
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DeploymentClassNameChecks extends HyGridValidationAdapter {

	private static final String DEPLOYMENT_PLUGIN_CLASS_NAME_OLD = "hygrid.deployment.plugin.DeploymentPlugIn";

	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateProjectAfterFileLoad(agentgui.core.project.Project)
	 */
	@Override
	public HyGridValidationMessage validateProjectAfterFileLoad(Project project) {

		HyGridValidationMessage vMessage = null;
		
		Vector<String> pluginClassNames = project.getPluginClassNames();
		for (int i = 0; i < pluginClassNames.size(); i++) {
			String pluginClassName = pluginClassNames.get(i);
			if (pluginClassName.equals(DEPLOYMENT_PLUGIN_CLASS_NAME_OLD) == true) {
				pluginClassNames.set(i, DeploymentPlugIn.class.getName());
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
		String message = "Adjusted class name for Deployment-Plugin.";
		String description = "Changed class name from '" + DEPLOYMENT_PLUGIN_CLASS_NAME_OLD + "' to '" + DeploymentPlugIn.class.getName() + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
