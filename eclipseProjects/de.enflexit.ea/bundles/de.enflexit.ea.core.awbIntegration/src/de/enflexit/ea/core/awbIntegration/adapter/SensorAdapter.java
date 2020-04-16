package de.enflexit.ea.core.awbIntegration.adapter;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;

/**
 * The Class SensorAdapter describes the data model and the interaction methods 
 * with a measuring point within the defined {@link NetworkModel}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SensorAdapter extends NetworkComponentAdapter {
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getNewDataModelAdapter()
	 */
	@Override
	public SensorDataModelAdapter getNewDataModelAdapter() {
		return new SensorDataModelAdapter(this.graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		return null;
	}
}
