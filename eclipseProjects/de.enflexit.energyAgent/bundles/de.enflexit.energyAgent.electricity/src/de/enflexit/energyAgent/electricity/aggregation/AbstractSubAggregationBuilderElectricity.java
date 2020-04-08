package de.enflexit.energyAgent.electricity.aggregation;

import de.enflexit.energyAgent.core.aggregation.AbstractSubAggregationBuilder;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.CurrentType;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.AbstractDomainModel;
import energy.optionModel.Connectivity;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.EnergyInterface;
import energy.optionModel.TechnicalInterface;

/**
 * This superclass for electricity SubAggregationBuilders provides some methods that are useful for 
 * different electricity domain aggregations (like tri-phase distribution grid and uni-phase 10kV).
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public abstract class AbstractSubAggregationBuilderElectricity extends AbstractSubAggregationBuilder {

	/**
	 * Returns a new energy interface for electricity.
	 *
	 * @param interfaceID the interface id
	 * @param powerType the power type
	 * @param phase the phase
	 * @return the new technical interface
	 */
	protected TechnicalInterface getNewEnergyInterfaceElectricity(String interfaceID, double voltageLevel, PowerType powerType, Phase phase) {
		
		AbstractDomainModel domainModel = this.getNewDomainModelElectricity(voltageLevel, powerType, phase);
		
		EnergyInterface ei = new EnergyInterface();
		ei.setInterfaceID(interfaceID);
		ei.setDescription(interfaceID + " " + domainModel.toString());
		ei.setDomain(EnergyCarrier.ELECTRICITY.value());
		ei.setDomainModel(domainModel);
		ei.setEnergyCarrier(EnergyCarrier.ELECTRICITY);
		ei.setConnectivity(Connectivity.UNDIRECTED);
		ei.setCostRelevant(true);
		return ei;
	}
	/**
	 * Returns a new domain model for electricity.
	 *
	 * @param powerType the power type
	 * @param phase the phase
	 * @return the new domain model electricity
	 */
	protected AbstractDomainModel getNewDomainModelElectricity(double voltageLevel, PowerType powerType, Phase phase) {
		DefaultDomainModelElectricity dmElec = new DefaultDomainModelElectricity();
		dmElec.setCurrentType(CurrentType.AC);
		dmElec.setRatedVoltage(voltageLevel);
		dmElec.setFrequency(50);
		dmElec.setPowerType(powerType);
		dmElec.setPhase(phase);
		return dmElec;
	}
}
