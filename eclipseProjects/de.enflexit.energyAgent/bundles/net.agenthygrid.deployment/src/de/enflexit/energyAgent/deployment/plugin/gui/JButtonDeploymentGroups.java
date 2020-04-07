package de.enflexit.energyAgent.deployment.plugin.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.toolbar.AbstractCustomToolbarComponent;

import de.enflexit.energyAgent.deployment.gui.DeploymentGroupsInternalFrame;
import de.enflexit.energyAgent.deployment.plugin.DeploymentPlugIn;
import hygrid.deployment.dataModel.SetupExtension;
import hygrid.plugin.HyGridPlugIn;

/**
 * Toolbar button for showing the list of deployed agents.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class JButtonDeploymentGroups extends AbstractCustomToolbarComponent implements ActionListener {
	
	private DeploymentGroupsInternalFrame iFrame;
	
	/**
	 * Constructor
	 * @param graphController The graph controller
	 */
	public JButtonDeploymentGroups(GraphEnvironmentController graphController) {
		super(graphController);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		this.getJInternalFrameTestbedAgents().showOnDesktop();
	}

	/**
	 * Gets the JInternalFrame testbed agents.
	 * @return the JInternalFrame testbed agents
	 */
	private DeploymentGroupsInternalFrame getJInternalFrameTestbedAgents() {
		if (iFrame==null) {
			
			// --- Get the setup extension from the HyGridPlugIn instance
			HyGridPlugIn plugin = HyGridPlugIn.getInstanceForCurrentProject();
			if (plugin!=null) {
				SetupExtension setupExtension = plugin.getSetupExtension();
				// --- Create the iFrame and add it to the component's desktop pane ----------
				iFrame = new DeploymentGroupsInternalFrame(setupExtension, this.graphController, this.graphController.getProject()!=null);
			} else {
				System.err.println("Could not find '" + HyGridPlugIn.class.getSimpleName() +"'");
			}
		}
		return iFrame;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.AbstractCustomToolbarComponent#getCustomComponent()
	 */
	@Override
	public JComponent getCustomComponent() {
		JButton jButtonTestbedAgents = new JButton();
		jButtonTestbedAgents.setIcon(new ImageIcon(getClass().getResource(DeploymentPlugIn.ICONS_PATH + "repository.png")));
		jButtonTestbedAgents.setToolTipText("Show deployed agents");
		jButtonTestbedAgents.setPreferredSize(new Dimension(26, 26));
		jButtonTestbedAgents.addActionListener(this);
		return jButtonTestbedAgents;
	}

}
