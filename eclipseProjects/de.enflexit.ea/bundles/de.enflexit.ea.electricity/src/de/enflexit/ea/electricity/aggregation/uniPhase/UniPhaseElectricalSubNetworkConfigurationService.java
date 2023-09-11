package de.enflexit.ea.electricity.aggregation.uniPhase;

import java.util.List;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator;
import de.enflexit.ea.core.aggregation.SubNetworkConfigurationService;
import de.enflexit.ea.electricity.ElectricityDomainIdentification;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricityTaskThreadCoordinator;

/**
 * This SubNetworkConfigurationService implementation provides a SubNetworkConfiguration for uni-phase electrical networks.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseElectricalSubNetworkConfigurationService implements SubNetworkConfigurationService {

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.SubNetworkConfigurationService#getDomainIdList()
	 */
	@Override
	public List<String> getDomainIdList() {
		return ElectricityDomainIdentification.getDomainList(this);
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.SubNetworkConfigurationService#getSubNetworkConfigurationCass()
	 */
	@Override
	public Class<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationClass() {
		return UniPhaseSubNetworkConfiguration.class;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.SubNetworkConfigurationService#getTaskThreadCoordinator()
	 */
	@Override
	public Class<? extends AbstractTaskThreadCoordinator> getTaskThreadCoordinator() {
		return ElectricityTaskThreadCoordinator.class;
	}

}
