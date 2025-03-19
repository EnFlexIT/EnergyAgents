package de.enflexit.ea.deployment.plugin;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarSurrounding;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarType;
import org.awb.env.networkModel.controller.ui.toolbar.CustomToolbarComponentDescription;

import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.project.Project;
import de.enflexit.awb.core.project.plugins.AwbPlugIn;
import de.enflexit.awb.core.project.plugins.PlugIn;
import de.enflexit.awb.core.ui.AwbProjectWindowTab;
import de.enflexit.awb.desktop.project.ProjectWindowTab;
import de.enflexit.ea.deployment.gui.DeploymentSettingsProjectWindowTab;
import de.enflexit.ea.deployment.plugin.gui.JButtonAgentDeployment;
import de.enflexit.ea.deployment.plugin.gui.JButtonDeploymentGroups;

/**
 * This PlugIn class adds agent deployment functionality to Agent.Workbench.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DeploymentPlugIn extends PlugIn {
	
	private static final String PLUGIN_NAME = "Energy Agent - Deployment PlugIn";
	
	private GraphEnvironmentController graphController;

	/**
	 * Instantiates a new deployment plug in.
	 * @param currProject the current project
	 */
	public DeploymentPlugIn(Project currProject) {
		super(currProject);
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#getName()
	 */
	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPlugIn()
	 */
	@Override
	public void onPlugIn() {
		this.addDeploymentButtons();
		this.addDeploymentSettingsConfigurationTab();
		super.onPlugIn();
	}

	/**
	 * Adds a tab for deployment settings configuration to the project window.
	 */
	private void addDeploymentSettingsConfigurationTab() {
		AwbProjectWindowTab parentPWT = this.project.getProjectEditorWindow().getTabForSubPanels(AwbProjectWindowTab.TAB_4_SUB_PANES_Setup);
		ProjectWindowTab pwt = new ProjectWindowTab(this.project, AwbProjectWindowTab.DISPLAY_4_END_USER, "Deployment-Settings", null, null, new DeploymentSettingsProjectWindowTab(this.project), parentPWT.getTitle());
		this.addProjectWindowTab(pwt, 1);
	}
	
	/**
	 * Adds the deployment related buttons to the project window.
	 */
	private void addDeploymentButtons() {
		this.getGraphController().addCustomToolbarComponentDescription(new CustomToolbarComponentDescription(ToolBarType.EditControl, ToolBarSurrounding.ConfigurationOnly, JButtonAgentDeployment.class, null, true));
		this.getGraphController().addCustomToolbarComponentDescription(new CustomToolbarComponentDescription(ToolBarType.ViewControl, ToolBarSurrounding.Both, JButtonDeploymentGroups.class, null, true));
	}
	
	
	/**
	 * Gets the graph controller.
	 * @return the graph controller
	 */
	private GraphEnvironmentController getGraphController() {
		if (graphController==null) {
			graphController = (GraphEnvironmentController) this.project.getEnvironmentController();
		}
		return graphController;
	}

	
	/**
	 * Gets the {@link DeploymentPlugIn} instance for the current project
	 * 
	 * @return The DeploymentPlugIn instance
	 */
	public static DeploymentPlugIn getInstanceForCurrentProject() {
		return getInstanceForProject(Application.getProjectFocused());
	}

	/**
	 * Gets the {@link DeploymentPlugIn} instance for the specified project
	 * 
	 * @param project The project
	 * @return The DeploymentPlugIn instance
	 */
	private static DeploymentPlugIn getInstanceForProject(Project project) {
		DeploymentPlugIn plugIn = null;
		for (int i = 0; i < project.getPlugInsLoaded().size(); i++) {
			AwbPlugIn pi = project.getPlugInsLoaded().get(i);
			if (pi instanceof DeploymentPlugIn) {
				plugIn = (DeploymentPlugIn) pi;
			}
		}
		return plugIn;
	}

}
