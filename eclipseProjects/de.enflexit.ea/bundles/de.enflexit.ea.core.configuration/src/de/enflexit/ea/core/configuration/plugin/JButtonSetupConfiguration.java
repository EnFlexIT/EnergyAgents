package de.enflexit.ea.core.configuration.plugin;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.toolbar.AbstractCustomToolbarComponent;

import de.enflexit.common.swing.OwnerDetection;
import de.enflexit.ea.core.configuration.BundleHelper;
import de.enflexit.ea.core.configuration.ui.SetupConfigurationDialog;

/**
 * The Class JButtonSetupConfiguration will be displayed in the toolbar of the GraphEnvironement.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JButtonSetupConfiguration extends AbstractCustomToolbarComponent implements ActionListener {

	private SetupConfigurationDialog scd;
	
	/**
	 * Instantiates a new JButtonImportFlowData.
	 * @param graphController the graph controller
	 */
	public JButtonSetupConfiguration(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.AbstractCustomToolbarComponent#getCustomComponent()
	 */
	@Override
	public JComponent getCustomComponent() {
		JButton jButtonSetupConfiguration = new JButton();
		jButtonSetupConfiguration.setIcon(BundleHelper.getImageIcon("SetupConfig.png"));
		jButtonSetupConfiguration.setToolTipText("Energy Agent - Setup configuration ...");
		jButtonSetupConfiguration.setPreferredSize(new Dimension(26, 26));
		jButtonSetupConfiguration.addActionListener(this);
		return jButtonSetupConfiguration;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {

		if (this.scd==null) {
			// --- Create the instance of the SetupConfigurationDialog -------- 
			Frame ownerFrame = OwnerDetection.getOwnerFrameForComponent(this.graphController.getGraphEnvironmentControllerGUI());
			scd = new SetupConfigurationDialog(ownerFrame);
			scd.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent we) {
					JButtonSetupConfiguration.this.scd = null;
				}
			});
			
		} else {
			// --- Set focus to SetupConfigurationDialog ----------------------
			this.scd.requestFocus();
		}
	}
	
}
