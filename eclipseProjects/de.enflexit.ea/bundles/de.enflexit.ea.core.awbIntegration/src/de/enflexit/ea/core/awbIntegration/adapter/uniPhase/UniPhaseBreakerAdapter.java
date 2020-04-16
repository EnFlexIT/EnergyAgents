package de.enflexit.ea.core.awbIntegration.adapter.uniPhase;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

import de.enflexit.ea.core.awbIntegration.adapter.CableWithBreakerAdapter;

public class UniPhaseBreakerAdapter extends CableWithBreakerAdapter{
	
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new UniPhaseBreakerDataModelAdapter(this.graphController);
	}

	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		return super.getJPopupMenuElements();
	}

}
