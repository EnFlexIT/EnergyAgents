package de.enflexit.ea.electricity.aggregation.taskThreading;

import java.util.ArrayList;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;

/**
 * The Class ElectricityTaskThreadCoordinator.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ElectricityTaskThreadCoordinator extends AbstractTaskThreadCoordinator {

	private ArrayList<AbstractElectricalNetworkConfiguration> elSubNetConfigList;
	private ElectricitySubNetworkGraph subNetworksTree;
	

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#initialize()
	 */
	@Override
	public void initialize() {
		this.getSubNetworkTree();
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#isRequiresNetworkCalculationRestart()
	 */
	@Override
	public boolean requiresNetworkCalculationRestart() {
		// TODO Auto-generated method stub
		
		
		return false;
	}
	
	/**
	 * Returns all electrical sub network configurations.
	 * @return the electrical sub network configurations
	 */
	protected ArrayList<AbstractElectricalNetworkConfiguration> getElectricalSubNetworkConfigurations() {
		if (elSubNetConfigList==null) {
			elSubNetConfigList = new ArrayList<>();
			
			ArrayList<AbstractSubNetworkConfiguration> subNetConfigList = this.getAggregationHandler().getSubNetworkConfigurations();
			for (AbstractSubNetworkConfiguration subNetConfig : subNetConfigList) {
				if (subNetConfig instanceof AbstractElectricalNetworkConfiguration) {
					elSubNetConfigList.add((AbstractElectricalNetworkConfiguration) subNetConfig);
				}
			}
		}
		return elSubNetConfigList;
	}
	
	/**
	 * Return the sub network tree.
	 * @return the sub network tree
	 */
	protected ElectricitySubNetworkGraph getSubNetworkTree() {
		if (subNetworksTree==null) {
			subNetworksTree = new ElectricitySubNetworkGraph(this.getAggregationHandler(), this);
		}
		return subNetworksTree;
	}
	
}
