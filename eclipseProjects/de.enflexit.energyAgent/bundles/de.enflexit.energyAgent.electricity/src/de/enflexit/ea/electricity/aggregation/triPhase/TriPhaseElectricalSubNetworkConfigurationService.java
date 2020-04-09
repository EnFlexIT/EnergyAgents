package de.enflexit.ea.electricity.aggregation.triPhase;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.SubNetworkConfigurationService;
import de.enflexit.ea.core.globalDataModel.GlobalHyGridConstants;

/**
 * This SubNetworkConfigurationService implementation provides a SubNetworkConfiguration for tri-phase electrical networks.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseElectricalSubNetworkConfigurationService implements SubNetworkConfigurationService {

	/* (non-Javadoc)
	 * @see hygrid.aggregation.subNetworkConfiguration.SubNetworkConfigurationService#getDomainID()
	 */
	@Override
	public String getDomainID() {
		return GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V;
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.SubNetworkConfigurationService#getSubNetworkConfigurationCass()
	 */
	@Override
	public Class<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationCass() {
		return SubNetworkConfigurationElectricalDistributionGrids.class;
	}

}
