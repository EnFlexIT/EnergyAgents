package de.enflexit.ea.electricity.aggregation.uniPhase;

import de.enflexit.ea.core.dataModel.blackboard.RequestObjective;

/**
 * Domain-specific blackboard request objectives for electricity
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public enum UniPhaseElectricityRequestObjective implements RequestObjective {
	PowerFlowCalculationResults,
	TransformerPower,
	VoltageLevels,
	CurrentLevels
}
