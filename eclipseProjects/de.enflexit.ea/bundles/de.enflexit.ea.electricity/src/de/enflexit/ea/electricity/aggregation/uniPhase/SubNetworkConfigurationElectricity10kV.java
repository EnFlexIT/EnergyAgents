package de.enflexit.ea.electricity.aggregation.uniPhase;

import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkComponent;
import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationPreprocessor;
import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.ea.core.aggregation.AbstractNetworkModelDisplayUpdater;
import de.enflexit.ea.core.aggregation.AbstractSubAggregationBuilder;
import de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation;
import de.enflexit.ea.core.dataModel.ontology.SlackNodeSetVoltageLevelNotification;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.PowerFlowCalculationThread;
import de.enflexit.ea.electricity.aggregation.triPhase.SubNetworkConfigurationElectricalDistributionGrids;
import de.enflexit.ea.electricity.aggregation.triPhase.TriPhaseElectricalNetworkPreprocessor;
import de.enflexit.ea.electricity.blackboard.SubBlackboardModelElectricity;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowCalculation;
import de.enflexit.ea.lib.powerFlowEstimation.centralEstimation.CentralEstimationManager;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.AbstractDomainModel;
import energy.optionModel.EnergyCarrier;
import energygroup.GroupController;
import jade.core.AID;

/**
 * The Class SubNetworkConfigurationElectricity10kV.
 */
public class SubNetworkConfigurationElectricity10kV extends AbstractSubNetworkConfiguration {

	public static final String SUBNET_DESCRIPTION_ELECTRICITY_10KV = "Electricity 10kV";
	
	private static final float VOLTAGE_BAND_MIN = 1000;
	private static final float VOLTAGE_BAND_MAX = 50000;
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getSubnetworkID()
	 */
	@Override
	public String getSubNetworkDescription() {
		return SUBNET_DESCRIPTION_ELECTRICITY_10KV;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#isPartOfSubnetwork(org.awb.env.networkModel.helper.NetworkComponent)
	 */
	@Override
	public boolean isPartOfSubnetwork(NetworkComponent netComp) {
//			if (netComp.getType().equals("Sensor")) {
//				return true;
//			}
		return super.isPartOfSubnetwork(netComp);
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#isPartOfSubnetwork(java.lang.String, energy.optionModel.AbstractDomainModel)
	 */
	@Override
	public boolean isPartOfSubnetwork(String domain, AbstractDomainModel domainModel) {
		if (domain.equals(EnergyCarrier.ELECTRICITY.value())==true) {
			if (domainModel instanceof DefaultDomainModelElectricity) {
				DefaultDomainModelElectricity dmElec = (DefaultDomainModelElectricity) domainModel;
				if (dmElec.getRatedVoltage()==10000.0) return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getSubAggregationBuilderClass()
	 */
	@Override
	public Class<? extends AbstractSubAggregationBuilder> getSubAggregationBuilderClass() {
		return SubAggregationBuilderElectricity10kV.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getNetworkCalculationPreProcessorClass()
	 */
	@Override
	public Class<? extends AbstractNetworkCalculationPreprocessor> getNetworkCalculationPreprocessorClass() {
		//TODO change to 10kV specific implementation if necessary
		return TriPhaseElectricalNetworkPreprocessor.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getNetworkCalculationStrategyClass()
	 */
	@Override
	public Class<? extends AbstractNetworkCalculationStrategy> getNetworkCalculationStrategyClass() {
		return UniPhaseElectricalNetworkCalculationStrategy.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getNetworkDisplayUpdaterClass()
	 */
	@Override
	public Class<? extends AbstractNetworkModelDisplayUpdater> getNetworkDisplayUpdaterClass() {
		return UniPhaseElectricalNetworkDisplayUpdater.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getUserClasses()
	 */
	@Override
	public HashMap<String, Class<?>> getUserClasses() {
		HashMap<String, Class<?>> myUserClasses = new HashMap<>();
		myUserClasses.put(PowerFlowCalculationThread.POWER_FLOW_CALCULATION_CLASS, PowerFlowCalculation.class);
//		myUserClasses.put(PowerFlowCalculationThread.POWER_FLOW_CALCULATION_CLASS, PowerFlowCalculationPV.class);
		myUserClasses.put(TriPhaseElectricalNetworkPreprocessor.POWER_FLOW_ESTIMATION_CLASS, CentralEstimationManager.class);
		return myUserClasses;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getSubBlackboardModelClass()
	 */
	@Override
	public Class<? extends AbstractSubBlackboardModel> getSubBlackboardModelClass() {
		return SubBlackboardModelElectricity.class;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#onNetworkStateInformation(jade.core.AID, de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation)
	 */
	@Override
	public boolean onNetworkStateInformation(AID sender, NetworkStateInformation networkStateInformation) {
		if (networkStateInformation instanceof SlackNodeSetVoltageLevelNotification && this.getDomainCluster().isPartOfDomainCluster(sender.getLocalName())==true) {
			// --- Cast to SlackNodeSetVoltageLevel and set new voltage level -------
			SlackNodeSetVoltageLevelNotification snvl = (SlackNodeSetVoltageLevelNotification) networkStateInformation;
			
			double slackNodeVoltage = snvl.getVoltageAbs().getValue();
			
			if (slackNodeVoltage>=VOLTAGE_BAND_MIN && slackNodeVoltage<VOLTAGE_BAND_MAX) {
				// --- Find the corresponding calculation strategy ---------------------- 
				String subnetworkDescription = SubNetworkConfigurationElectricalDistributionGrids.SUBNET_DESCRIPTION_ELECTRICAL_DISTRIBUTION_GRIDS;
				List<AbstractSubNetworkConfiguration> subnetConfigList = this.getAggregationHandler().getSubNetworkConfiguration(subnetworkDescription);
				for (int i = 0; i < subnetConfigList.size(); i++) {
					
					// --- Check if the aggregator contains the sender system -----------
					GroupController groupController = this.getSubAggregationBuilder().getGroupController();
					DefaultMutableTreeNode treeNode = groupController.getGroupTreeModel().getGroupTreeNodeByNetworkID(sender.getLocalName());
					if (treeNode==null) continue;
					
					// --- Put slack node voltage level to network calculation strategy - 
					AbstractElectricalNetworkCalculationStrategy netClacStrategy = (AbstractElectricalNetworkCalculationStrategy) this.getNetworkCalculationStrategy();
					if (netClacStrategy!=null) {
						HashMap<Phase, Double> slackNodeVoltageLevel = new HashMap<>();
						slackNodeVoltageLevel.put(Phase.AllPhases, (double) snvl.getVoltageAbs().getValue());
						netClacStrategy.setSlackNodeVoltageLevel(slackNodeVoltageLevel);
						break;
					}
				}
				return true;
			}
		}
		return false;
	}
	
}
