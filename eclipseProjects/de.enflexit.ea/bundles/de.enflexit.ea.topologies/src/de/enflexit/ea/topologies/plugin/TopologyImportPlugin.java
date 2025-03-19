package de.enflexit.ea.topologies.plugin;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarSurrounding;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarType;
import org.awb.env.networkModel.controller.ui.toolbar.CustomToolbarComponentDescription;

import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.project.Project;
import de.enflexit.awb.core.project.plugins.PlugIn;

/**
 * The Class MEOPlugin provides specific control elements to Agent.Workbench.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class TopologyImportPlugin extends PlugIn {

	private GraphEnvironmentController graphController;
	
	/**
	 * Instantiates the AWB web server plugin.
	 * @param currProject the current project
	 */
	public TopologyImportPlugin(Project currProject) {
		super(currProject);
	}
	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#getName()
	 */
	@Override
	public String getName() {
		return "Energy Agent - Topology Import PlugIn";
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
			this.getGraphController().addCustomToolbarComponentDescription(new CustomToolbarComponentDescription(ToolBarType.EditControl, ToolBarSurrounding.ConfigurationOnly, JButtonSimBenchData.class, null, true));
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
