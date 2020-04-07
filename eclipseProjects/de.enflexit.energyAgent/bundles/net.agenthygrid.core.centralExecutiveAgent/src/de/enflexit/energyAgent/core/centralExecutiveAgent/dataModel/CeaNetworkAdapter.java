package de.enflexit.energyAgent.core.centralExecutiveAgent.dataModel;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

/**
 * The Class CeaNetworkModelAdapter.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class CeaNetworkAdapter extends NetworkComponentAdapter {

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getNewDataModelAdapter()
	 */
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new CeaNetworkDataModelAdapter(this.graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		return null;
	}

}
