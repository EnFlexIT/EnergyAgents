package de.enflexit.ea.core.aggregation;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DefaultTaskThreadCoordinator organizes network calculations and display updates
 * for sub network configurations (class {@link AbstractSubNetworkConfiguration})that are not 
 * under control of an other task thread coordinator.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class DefaultTaskThreadCoordinator extends AbstractTaskThreadCoordinator {

	private List<AbstractSubNetworkConfiguration> subNetConfigList;
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#initialize()
	 */
	@Override
	public void initialize() {
		// --- Nothing to do here ---------------
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#getSubNetworkConfigurationUnderControl()
	 */
	@Override
	public List<? extends AbstractSubNetworkConfiguration> getSubNetworkConfigurationsUnderControl() {
		if (subNetConfigList==null) {
			subNetConfigList = new ArrayList<>(this.getAggregationHandler().getSubNetworkConfigurations());
			for (AbstractSubNetworkConfiguration ttcSubNetConfig : this.getAggregationHandler().getTaskThreadCoordinatorsSubNetworkConfigurations() ) {
				subNetConfigList.remove(ttcSubNetConfig);
			}
		}
		return subNetConfigList;
	}

	
}
