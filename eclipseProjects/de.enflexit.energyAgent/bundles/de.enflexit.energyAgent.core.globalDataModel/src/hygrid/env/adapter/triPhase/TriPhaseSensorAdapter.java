package hygrid.env.adapter.triPhase;

import java.util.Vector;

import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

/**
 * This class describes the data model and the interaction methods 
 * with a tri-phase sensor within the defined {@link NetworkModel}.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseSensorAdapter extends NetworkComponentAdapter {

	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new TriPhaseSensorDataModelAdapter(this.getGraphEnvironmentController());
	}

	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		// TODO Auto-generated method stub
		return null;
	}


}
