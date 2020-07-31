package de.enflexit.ea.electricity.blackboard;

import de.enflexit.ea.core.dataModel.blackboard.RequestObjective;

/**
 * Specifies blackboard request objectives for the electricity domain
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public enum ElectricityRequestObjective implements RequestObjective {
	PowerFlowCalculationResults,
	TransformerPower,
	VoltageAndCurrentLevels,
	VoltageLevels,
	CurrentLevels
}
