package hygrid.env.adapter.uniPhase;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

import hygrid.env.adapter.CableWithBreakerAdapter;

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
