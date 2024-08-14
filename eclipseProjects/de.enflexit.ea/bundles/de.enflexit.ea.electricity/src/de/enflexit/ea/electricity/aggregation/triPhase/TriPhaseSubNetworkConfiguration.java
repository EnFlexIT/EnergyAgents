package de.enflexit.ea.electricity.aggregation.triPhase;

import java.util.HashMap;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationPreprocessor;
import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.ea.core.aggregation.AbstractNetworkModelDisplayUpdater;
import de.enflexit.ea.core.aggregation.AbstractSubAggregationBuilder;
import de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel;
import de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.aggregation.PowerFlowCalculationThread;
import de.enflexit.ea.electricity.blackboard.SubBlackboardModelElectricity;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowCalculation;
import energy.domain.DefaultDomainModelElectricity;
import energy.optionModel.AbstractDomainModel;
import energy.optionModel.EnergyCarrier;
import jade.core.AID;

/**
 * The Class TriPhaseSubNetworkConfiguration.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen 
 */
public class TriPhaseSubNetworkConfiguration extends AbstractElectricalNetworkConfiguration {

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#isPartOfSubnetwork(org.awb.env.networkModel.helper.NetworkComponent)
	 */
	@Override
	public boolean isPartOfSubnetwork(NetworkComponent netComp) {
		if (netComp.getType().equals("Sensor")) {
			return true;
		}
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
				if (dmElec.getRatedVoltage()==this.getConfiguredRatedVoltageFromNetwork()) return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getSubAggregationBuilderClass()
	 */
	@Override
	public Class<? extends AbstractSubAggregationBuilder> getSubAggregationBuilderClass() {
		return TriPhaseSubAggregationBuilder.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getNetworkCalculationPreProcessorClass()
	 */
	@Override
	public Class<? extends AbstractNetworkCalculationPreprocessor> getNetworkCalculationPreprocessorClass() {
		return TriPhaseElectricalNetworkPreprocessor.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getNetworkCalculationStrategyClass()
	 */
	@Override
	public Class<? extends AbstractNetworkCalculationStrategy> getNetworkCalculationStrategyClass() {
		return TriPhaseElectricalNetworkCalculationStrategy.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getNetworkDisplayUpdaterClass()
	 */
	@Override
	public Class<? extends AbstractNetworkModelDisplayUpdater> getNetworkDisplayUpdaterClass() {
		return TriPhaseElectricalNetworkDisplayUpdater.class;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubNetworkConfiguration#getUserClasses()
	 */
	@Override
	public HashMap<String, Class<?>> getUserClasses() {
		HashMap<String, Class<?>> myUserClasses = new HashMap<>();  
		myUserClasses.put(PowerFlowCalculationThread.POWER_FLOW_CALCULATION_CLASS, PowerFlowCalculation.class);
		myUserClasses.put(TriPhaseElectricalNetworkPreprocessor.POWER_FLOW_ESTIMATION_CLASS, this.classForName("de.enflexit.ea.lib.powerFlowEstimation.centralEstimation.CentralEstimationManager"));
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
		// --- Previously, the handling of the 'SlackNodeSetVoltageLevelNotification' could be found here ---
		// --- => Removed since the improvements on the transformer model and handling ----------------------
		return false;
	}
	
}
