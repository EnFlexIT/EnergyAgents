package de.enflexit.energyAgent.electricity.aggregation.triPhase;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.energyAgent.electricity.aggregation.AbstractSubAggregationBuilderElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.TechnicalInterface;

/**
 * SubAggregationBuilder for low voltage electrical distribution grids (tri-phase, 230V)
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SubAggregationBuilderElectricalDistributionGrid extends AbstractSubAggregationBuilderElectricity {

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractSubAggregationBuilder#getTechnichalInterfaces()
	 */
	@Override
	protected List<TechnicalInterface> getAggregationTechnicalInterfaces() {
		
		ArrayList<TechnicalInterface> interfaces = new ArrayList<>();
		
		TechnicalInterface tiP1 = this.getNewEnergyInterfaceElectricity("P - L1", 230, PowerType.ActivePower, Phase.L1);
		interfaces.add(tiP1);
		TechnicalInterface tiQ1 = this.getNewEnergyInterfaceElectricity("Q - L1", 230, PowerType.ReactivePower, Phase.L1);
		interfaces.add(tiQ1);
		TechnicalInterface tiP2 = this.getNewEnergyInterfaceElectricity("P - L2", 230, PowerType.ActivePower, Phase.L2);
		interfaces.add(tiP2);
		TechnicalInterface tiQ2 = this.getNewEnergyInterfaceElectricity("Q - L2", 230, PowerType.ReactivePower, Phase.L2);
		interfaces.add(tiQ2);
		TechnicalInterface tiP3 = this.getNewEnergyInterfaceElectricity("P - L3", 230, PowerType.ActivePower, Phase.L3);
		interfaces.add(tiP3);
		TechnicalInterface tiQ3 = this.getNewEnergyInterfaceElectricity("Q - L3", 230, PowerType.ReactivePower, Phase.L3);
		interfaces.add(tiQ3);
		
		return interfaces;
	}

}
