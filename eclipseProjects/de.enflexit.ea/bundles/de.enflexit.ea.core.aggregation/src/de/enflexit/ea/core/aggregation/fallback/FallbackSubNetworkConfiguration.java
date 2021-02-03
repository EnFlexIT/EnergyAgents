package de.enflexit.ea.core.aggregation.fallback;

import java.util.HashMap;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.settings.ComponentTypeSettings;

import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationPreprocessor;
import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.ea.core.aggregation.AbstractNetworkModelDisplayUpdater;
import de.enflexit.ea.core.aggregation.AbstractSubAggregationBuilder;
import de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.DefaultSubAggregationBuilder;
import de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation;
import energy.optionModel.AbstractDomainModel;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import jade.core.AID;


/**
 * The FallbackSubNetworkConfiguration specifies the fallback aggregation, 
 * that takes all systems that do not belong to at least one other aggregation, 
 * i.e. that are the only system in their domain cluster, and have an agent class specified
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class FallbackSubNetworkConfiguration extends AbstractSubNetworkConfiguration {
	
	public static final String SUB_NETWORK_DESCRIPTION = "Fallback Aggregation";

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getSubNetworkDescription()
	 */
	@Override
	public String getSubNetworkDescription() {
		return SUB_NETWORK_DESCRIPTION;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#isPartOfSubnetwork(java.lang.String, energy.optionModel.AbstractDomainModel)
	 */
	@Override
	public boolean isPartOfSubnetwork(String domain, AbstractDomainModel domainModel) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#isPartOfSubnetwork(org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public boolean isPartOfSubnetwork(NetworkComponent netComp) {
		
		if (netComp==null) {
			return false;
		}
		
		// --- First check: Is the component already part of another domain aggregation? 
		if (this.getAggregationHandler().getNetworkComponentsScheduleController().get(netComp.getId())!=null) {
			return false;
		}
		
		// --- Second check: Is an energy agent class defined for this component?
		String compType = netComp.getType();
		ComponentTypeSettings cts = this.getAggregationHandler().getNetworkModel().getGeneralGraphSettings4MAS().getCurrentCTS().get(compType);
		if (cts.getAgentClass()==null) {
			// --- Component has no agent class -----------
			return false;
		}
		
		// --- Third check: Does the component contain an EOM-based data model?
		Object netCompDM = netComp.getDataModel();
		if (netCompDM!=null && (netCompDM instanceof TechnicalSystem || netCompDM instanceof TechnicalSystemGroup || netCompDM instanceof ScheduleList)) {
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getSubAggregationBuilderClass()
	 */
	@Override
	public Class<? extends AbstractSubAggregationBuilder> getSubAggregationBuilderClass() {
		return DefaultSubAggregationBuilder.class;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getNetworkCalculationPreprocessorClass()
	 */
	@Override
	public Class<? extends AbstractNetworkCalculationPreprocessor> getNetworkCalculationPreprocessorClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getNetworkCalculationStrategyClass()
	 */
	@Override
	public Class<? extends AbstractNetworkCalculationStrategy> getNetworkCalculationStrategyClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getSubBlackboardModelClass()
	 */
	@Override
	public Class<? extends AbstractSubBlackboardModel> getSubBlackboardModelClass() {
		// TODO Maybe implement later?
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getNetworkDisplayUpdaterClass()
	 */
	@Override
	public Class<? extends AbstractNetworkModelDisplayUpdater> getNetworkDisplayUpdaterClass() {
		// TODO Maybe implement later?
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#getUserClasses()
	 */
	@Override
	public HashMap<String, Class<?>> getUserClasses() {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration#onNetworkStateInformation(jade.core.AID, de.enflexit.ea.core.dataModel.ontology.NetworkStateInformation)
	 */
	@Override
	public boolean onNetworkStateInformation(AID sender, NetworkStateInformation networkStateInformation) {
		// TODO Maybe implement later?
		return false;
	}

}
