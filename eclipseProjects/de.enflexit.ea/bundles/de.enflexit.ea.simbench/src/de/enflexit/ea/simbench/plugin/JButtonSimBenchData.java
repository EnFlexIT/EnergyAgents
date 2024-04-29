package de.enflexit.ea.simbench.plugin;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.toolbar.AbstractCustomToolbarComponent;

import de.enflexit.common.swing.OwnerDetection;
import de.enflexit.ea.simbench.BundleHelper;
import de.enflexit.ea.simbench.persistence.SimBenchFileStore;

/**
 * The Class JButtonDatabaseSettings that will be displayed in the toolbar of the GraphEnvironement.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class JButtonSimBenchData extends AbstractCustomToolbarComponent implements ActionListener {

	/**
	 * Instantiates a new JButtonImportFlowData.
	 * @param graphController the graph controller
	 */
	public JButtonSimBenchData(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.AbstractCustomToolbarComponent#getCustomComponent()
	 */
	@Override
	public JComponent getCustomComponent() {
		JButton jButtonExportLoadProfile = new JButton();
		jButtonExportLoadProfile.setIcon(BundleHelper.getImageIcon("SimBenchData.png"));
		jButtonExportLoadProfile.setToolTipText("Show currently loaded Simbench Data");
		jButtonExportLoadProfile.setPreferredSize(new Dimension(26, 26));
		jButtonExportLoadProfile.addActionListener(this);
		return jButtonExportLoadProfile;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {

		Frame ownerFrame = OwnerDetection.getOwnerFrameForComponent(this.graphController.getGraphEnvironmentControllerGUI());
		SimBenchFileStore sfs = SimBenchFileStore.getInstance();
		if (sfs.getCsvDataController().size()>=0) {
			sfs.showSimbenchData();
		} else {
			String title = "No SimBench data could be found!";
			String message = "The current setup seems to not contain any SimBench data!";
			JOptionPane.showMessageDialog(ownerFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
}
