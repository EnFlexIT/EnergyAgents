package de.enflexit.ea.deployment.plugin.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.toolbar.AbstractCustomToolbarComponent;

import de.enflexit.ea.deployment.AgentDeployment;
import de.enflexit.ea.deployment.ImageHelper;
import de.enflexit.ea.deployment.gui.AgentDeploymentDialog;

/**
 * The Class JButtonDeployAgent represents the JButton that is used to open the 
 * AgentDeploymentDialog {@link AgentDeploymentDialog}.
 * 
 * @author Mohamed Amine JEDIDI, mohamedamine_jedidi@outlook.com
 * @version 1.0
 * @since 06-06-2016
 */
public class JButtonAgentDeployment extends AbstractCustomToolbarComponent implements ActionListener {

	/**
	 * Instantiates a new JButtonImportStaticLoadProfiles.
	 * @param graphController the graph controller
	 */
	public JButtonAgentDeployment(GraphEnvironmentController graphController) {
		super(graphController);
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.AbstractCustomToolbarComponent#getCustomComponent()
	 */
	@Override
	public JComponent getCustomComponent() {
		JButton jButtonImportLoadProfile = new JButton();
		jButtonImportLoadProfile.setIcon(ImageHelper.getImageIcon("box.png"));
		jButtonImportLoadProfile.setToolTipText("Deploy selected Agent");
		jButtonImportLoadProfile.setPreferredSize(new Dimension(26, 26));
		jButtonImportLoadProfile.addActionListener(this);
		return jButtonImportLoadProfile;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		List<NetworkComponent> networkComponents = new ArrayList<>();
		
		// --- Get the selected GraphObject ---------------
		HashSet<Object> hashSet = (HashSet<Object>) this.graphController.getGraphEnvironmentControllerGUI().getBasicGraphGuiRootJSplitPane().getBasicGraphGui().getSelectedGraphObject();
		
		if (hashSet!=null) {
			NetworkComponent[] componentsArray = hashSet.toArray(new NetworkComponent[hashSet.size()]);
			networkComponents.addAll(Arrays.asList(componentsArray));
		}

		// --- Start the actual deployment ----------------
		AgentDeployment deploymentHandler = new AgentDeployment();
		deploymentHandler.deploySelectedAgents(networkComponents);
	}
	
}
