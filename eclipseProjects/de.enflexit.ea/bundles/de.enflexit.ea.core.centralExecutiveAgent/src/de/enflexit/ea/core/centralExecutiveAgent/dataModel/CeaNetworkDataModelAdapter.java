package de.enflexit.ea.core.centralExecutiveAgent.dataModel;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGuiJDesktopPane;
import org.awb.env.networkModel.controller.ui.BasicGraphGuiProperties;

import de.enflexit.ea.core.globalDataModel.cea.CeaConfigModel;

/**
 * The Class CeaNetworkDataModelAdapter.
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class CeaNetworkDataModelAdapter extends NetworkComponentAdapter4DataModel {

	private CeaConfigModelPanel ceaConfigModelPanel;
	
	/**
	 * Instantiates a new CEA network data model adapter.
	 * @param graphController the graph controller
	 */
	public CeaNetworkDataModelAdapter(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4DataModel#getVisualizationComponent(org.awb.env.networkModel.controller.BasicGraphGuiProperties)
	 */
	@Override
	public JComponent getVisualizationComponent(BasicGraphGuiProperties internalPropertyFrame) {
		return this.getCeaConfigModelPanel();
	}
	/**
	 * Returns the {@link CeaConfigModelPanel}
	 * @return the cea data model panel
	 */
	private CeaConfigModelPanel getCeaConfigModelPanel() {
		if (ceaConfigModelPanel==null) {
			ceaConfigModelPanel = new CeaConfigModelPanel();
		}
		return ceaConfigModelPanel;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4DataModel#setVisualizationComponent(javax.swing.JComponent)
	 */
	@Override
	public void setVisualizationComponent(JComponent visualizationComponent) {
		// --- Nothing to do here -----
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4DataModel#getSizeOfVisualisation(org.awb.env.networkModel.controller.BasicGraphGuiJDesktopPane)
	 */
	@Override
	public Dimension getSizeOfVisualisation(BasicGraphGuiJDesktopPane graphDesktop) {
		return new Dimension(400, 500);
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4DataModel#save()
	 */
	@Override
	public boolean save() {
		return this.getCeaConfigModelPanel().save();
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4DataModel#setDataModel(java.lang.Object)
	 */
	@Override
	public void setDataModel(Object dataModel) {
		if (dataModel==null) {
			this.getCeaConfigModelPanel().setCeaConfigModel(null);
		} else if (dataModel instanceof CeaConfigModel) {
			CeaConfigModel ceaConfigModel = (CeaConfigModel) dataModel;
			CeaConfigModel ceaConfigModelClone = (CeaConfigModel) ceaConfigModel.clone(); 
			this.getCeaConfigModelPanel().setCeaConfigModel(ceaConfigModelClone);
		} else if (dataModel.getClass().isArray()==true) {
			Object[] netCompDataModelArray = (Object[]) dataModel;
			if (netCompDataModelArray.length>0 && netCompDataModelArray[0] instanceof CeaConfigModel) {
				CeaConfigModel ceaConfigModel = (CeaConfigModel) netCompDataModelArray[0];
				CeaConfigModel ceaConfigModelClone = (CeaConfigModel) ceaConfigModel.clone(); 
				this.getCeaConfigModelPanel().setCeaConfigModel(ceaConfigModelClone);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4DataModel#getDataModel()
	 */
	@Override
	public Object getDataModel() {
		return this.getCeaConfigModelPanel().getCeaConfigModel();
	}
	
}
