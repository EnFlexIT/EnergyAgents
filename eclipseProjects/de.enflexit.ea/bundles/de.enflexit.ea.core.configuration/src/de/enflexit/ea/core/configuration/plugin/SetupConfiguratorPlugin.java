package de.enflexit.ea.core.configuration.plugin;

import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.project.Project;
import de.enflexit.awb.core.project.plugins.PlugIn;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarSurrounding;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarType;
import org.awb.env.networkModel.controller.ui.toolbar.CustomToolbarComponentDescription;


/**
 * The Class SetupConfiguratorPlugin.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfiguratorPlugin extends PlugIn {

	private GraphEnvironmentController graphController;
	
	/**
	 * Instantiates a new setup configurator plugin.
	 * @param currProject the current project
	 */
	public SetupConfiguratorPlugin(Project currProject) {
		super(currProject);
	}
	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#getName()
	 */
	@Override
	public String getName() {
		return "Energy Agent - Large Setup Configurator PlugIn";
	}
	/**
	 * Gets the graph controller.
	 * @return the graph controller
	 */
	public GraphEnvironmentController getGraphController() {
		if (graphController==null) {
			graphController = (GraphEnvironmentController) this.project.getEnvironmentController();
		}
		return graphController;
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPlugIn()
	 */
	@Override
	public void onPlugIn() {
		if (Application.isOperatingHeadless()==false) {
			this.getGraphController().addCustomToolbarComponentDescription(new CustomToolbarComponentDescription(ToolBarType.EditControl, ToolBarSurrounding.ConfigurationOnly, JButtonSetupConfiguration.class, null, true));
		}
		super.onPlugIn();
	}
	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPlugOut()
	 */
	@Override
	public void onPlugOut() {
		super.onPlugOut();
	}
	
}
