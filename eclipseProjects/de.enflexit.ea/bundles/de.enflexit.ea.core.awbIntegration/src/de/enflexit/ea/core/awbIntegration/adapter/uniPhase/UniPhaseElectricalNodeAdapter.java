package de.enflexit.ea.core.awbIntegration.adapter.uniPhase;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

/**
 * This class describes the data model and the interaction methods 
 * with a uni-phase electrical node within the defined {@link NetworkModel}.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseElectricalNodeAdapter extends NetworkComponentAdapter {

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter#getNewDataModelAdapter()
	 */
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new UniPhaseElectricalNodeDataModelAdapter(this.graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		return null;
	}

}
