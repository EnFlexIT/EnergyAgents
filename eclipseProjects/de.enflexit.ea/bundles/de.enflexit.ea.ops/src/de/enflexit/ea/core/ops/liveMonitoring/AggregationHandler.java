package de.enflexit.ea.core.ops.liveMonitoring;

import java.util.ArrayList;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.DefaultSubNetworkConfigurations;
import de.enflexit.ea.core.ops.OpsController;

/**
 * {@link AbstractAggregationHandler} implementation to be used by 
 * the {@link SimulationManager}
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class AggregationHandler extends AbstractAggregationHandler {

	/**
	 * Instantiates a new aggregation handler.
	 *
	 * @param networkModel the network model
	 * @param headlessOperation true for operation without GUI
	 * @param ownerName the owner name
	 */
	public AggregationHandler(NetworkModel networkModel, boolean headlessOperation, String ownerName) {
		super(networkModel, headlessOperation, ownerName);
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractAggregationHandler#getSubnetworkConfiguration()
	 */
	@Override
	protected ArrayList<AbstractSubNetworkConfiguration> getConfigurationOfSubNetworks() {
		// --- Use the default configuration from the super class -------------
		ArrayList<AbstractSubNetworkConfiguration> subNetworkConfigurations = new DefaultSubNetworkConfigurations(this);
		// --- Set the tab from the ops frame as visualization container ------
		for (int i = 0; i < subNetworkConfigurations.size(); i++) {
			subNetworkConfigurations.get(i).setAggregationVisualizationParentContainer(OpsController.getInstance().getJFrameOpsControl().getjTabbedPaneOpsContent());
		}
		return subNetworkConfigurations;
	}

}
