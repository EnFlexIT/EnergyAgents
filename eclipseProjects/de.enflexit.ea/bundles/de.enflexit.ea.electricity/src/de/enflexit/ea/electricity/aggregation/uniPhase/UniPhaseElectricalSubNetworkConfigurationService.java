package de.enflexit.ea.electricity.aggregation.uniPhase;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.SubNetworkConfigurationService;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;

/**
 * This SubNetworkConfigurationService implementation provides a SubNetworkConfiguration for uni-phase electrical networks.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseElectricalSubNetworkConfigurationService implements SubNetworkConfigurationService {

	/* (non-Javadoc)
	 * @see hygrid.aggregation.subNetworkConfiguration.SubNetworkConfigurationService#getDomainID()
	 */
	@Override
	public String getDomainID() {
		return GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_10KV;
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.SubNetworkConfigurationService#getSubNetworkConfigurationCass()
	 */
	@Override
	public Class<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationCass() {
		return SubNetworkConfigurationElectricity10kV.class;
	}

}
