package de.enflexit.ea.core.awbIntegration.adapter.triPhase;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

/**
 * This class describes the data model and the interaction methods 
 * with a tri-phase power cable within the defined {@link NetworkModel}.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseCableAdapter extends NetworkComponentAdapter {

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter#getNewDataModelAdapter()
	 */
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		// TODO Auto-generated method stub
		return new TriPhaseCableDataModelAdapter(this.graphController);
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
