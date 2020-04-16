package de.enflexit.ea.core.awbIntegration.adapter.uniPhase;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

/**
 * This class describes the data model for a uni-phase sensor within the defined {@link NetworkModel}.
 * Context menu interaction is delegated to the superclass, as it is identical for uni- and tri-phase sensors.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseSensorAdapter extends NetworkComponentAdapter {

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter#getNewDataModelAdapter()
	 */
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new UniPhaseSensorDataModelAdapter(this.getGraphEnvironmentController());
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		// TODO Auto-generated method stub
		return null;
	}


}
