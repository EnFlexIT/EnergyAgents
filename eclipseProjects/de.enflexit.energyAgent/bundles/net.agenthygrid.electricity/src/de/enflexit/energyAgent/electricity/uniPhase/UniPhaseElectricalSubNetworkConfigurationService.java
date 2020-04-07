package de.enflexit.energyAgent.electricity.uniPhase;

import de.enflexit.energyAgent.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.energyAgent.core.aggregation.SubNetworkConfigurationService;
import hygrid.globalDataModel.GlobalHyGridConstants;

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
