package de.enflexit.ea.core.aggregation.internal;

import java.util.ArrayList;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;

/**
 * {@link AbstractAggregationHandler} implementation to be used by 
 * the {@link SimulationManager}
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ConstructionSiteAggregationHandler extends AbstractAggregationHandler {

	/**
	 * Instantiates a new aggregation handler.
	 *
	 * @param networkModel the network model
	 * @param headlessOperation true for operation without GUI
	 * @param ownerName the owner name
	 */
	public ConstructionSiteAggregationHandler(NetworkModel networkModel, boolean headlessOperation, String ownerName) {
		super(networkModel, headlessOperation, ownerName);
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractAggregationHandler#getSubnetworkConfiguration()
	 */
	@Override
	protected ArrayList<AbstractSubNetworkConfiguration> getConfigurationOfSubNetworks() {
		return null;
	}

}
