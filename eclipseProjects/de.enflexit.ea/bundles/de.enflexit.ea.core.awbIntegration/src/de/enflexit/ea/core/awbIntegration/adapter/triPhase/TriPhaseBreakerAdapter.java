package de.enflexit.ea.core.awbIntegration.adapter.triPhase;

import java.util.Vector;

import javax.swing.JComponent;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

import de.enflexit.ea.core.awbIntegration.adapter.CableWithBreakerAdapter;

public class TriPhaseBreakerAdapter extends CableWithBreakerAdapter {
	
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new TriPhaseBreakerDataModelAdapter(this.getGraphEnvironmentController());
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		return super.getJPopupMenuElements();
	}

}
