package de.enflexit.ea.electricity.aggregation.uniPhase;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.aggregation.AbstractSubAggregationBuilderElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.TechnicalInterface;

/**
 * SubAggregationBuilder for medium voltage grids (uni-phase, 10kV)
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseSubAggregationBuilder extends AbstractSubAggregationBuilderElectricity {

	private Double configuredRateVoltage;
	
	/**
	 * Returns the configured rated voltage.
	 * @return the configured rated voltage
	 */
	private double getConfiguredRatedVoltage() {
		if (configuredRateVoltage==null) {
			configuredRateVoltage = ((AbstractElectricalNetworkConfiguration)this.getSubAggregationConfiguration()).getConfiguredRatedVoltageFromNetwork();
		}
		return configuredRateVoltage;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubAggregationBuilder#getTechnichalInterfaces()
	 */
	@Override
	protected List<TechnicalInterface> getAggregationTechnicalInterfaces() {
		
		ArrayList<TechnicalInterface> interfaces = new ArrayList<>();
		
		TechnicalInterface tiP1 = this.getNewEnergyInterfaceElectricity("P", this.getConfiguredRatedVoltage(), PowerType.ActivePower, Phase.AllPhases);
		interfaces.add(tiP1);
		TechnicalInterface tiQ1 = this.getNewEnergyInterfaceElectricity("Q", this.getConfiguredRatedVoltage(), PowerType.ReactivePower, Phase.AllPhases);
		interfaces.add(tiQ1);
		
		return interfaces;
	}
	
}
