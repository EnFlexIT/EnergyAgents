package de.enflexit.ea.electricity.blackboard;

import de.enflexit.ea.core.dataModel.blackboard.RequestObjective;

/**
 * Domain-specific blackboard request objectives for electricity
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public enum ElectricityRequestObjective implements RequestObjective {
	PowerFlowCalculationResults,
	TransformerPower,
	VoltageLevels,
	CurrentLevels
}
