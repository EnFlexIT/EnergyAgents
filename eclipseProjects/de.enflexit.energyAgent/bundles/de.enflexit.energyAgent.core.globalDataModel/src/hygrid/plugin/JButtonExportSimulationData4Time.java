package hygrid.plugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.toolbar.AbstractCustomToolbarComponent;

import de.enflexit.ea.core.globalDataModel.ImageHelper;

/**
 * The Class JButtonExportSimulationData4Time that will be 
 * displayed in the toolbar of the GraphEnvironement.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class JButtonExportSimulationData4Time extends AbstractCustomToolbarComponent implements ActionListener {

	/**
	 * Instantiates a new JButtonExportSimulationData4Time.
	 * @param graphController the graph controller
	 */
	public JButtonExportSimulationData4Time(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.AbstractCustomToolbarComponent#getCustomComponent()
	 */
	@Override
	public JComponent getCustomComponent() {
		JButton jButtonExportLoadProfile = new JButton();
		jButtonExportLoadProfile.setIcon(ImageHelper.getImageIcon("graph_export.png"));
		jButtonExportLoadProfile.setToolTipText("Export simulation data for a single time step");
		jButtonExportLoadProfile.setPreferredSize(new Dimension(26, 26));
		jButtonExportLoadProfile.addActionListener(this);
		return jButtonExportLoadProfile;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		new SimulationDataCsvExporter(this.graphController.getNetworkModel()).doExport();;
	}

}
