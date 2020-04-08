package de.enflexit.energyAgent.electricity.uniPhase;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.energyAgent.electricity.aggregation.AbstractSubAggregationBuilderElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.TechnicalInterface;

/**
 * SubAggregationBuilder for medium voltage grids (uni-phase, 10kV)
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SubAggregationBuilderElectricity10kV extends AbstractSubAggregationBuilderElectricity {

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubAggregationBuilder#getTechnichalInterfaces()
	 */
	@Override
	protected List<TechnicalInterface> getAggregationTechnicalInterfaces() {
		ArrayList<TechnicalInterface> interfaces = new ArrayList<>();
		TechnicalInterface tiP1 = this.getNewEnergyInterfaceElectricity("P", 10000, PowerType.ActivePower, Phase.AllPhases);
		interfaces.add(tiP1);
		TechnicalInterface tiQ1 = this.getNewEnergyInterfaceElectricity("Q", 10000, PowerType.ReactivePower, Phase.AllPhases);
		interfaces.add(tiQ1);
		return interfaces;
	}

}
