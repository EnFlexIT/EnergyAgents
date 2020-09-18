package de.enflexit.ea.core.blackboard;

import de.enflexit.ea.core.dataModel.blackboard.RequestObjective;

/**
 * Domain-independent blackboard request objectives
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public enum GeneralRequestObjective implements RequestObjective{
	NetworkModel,
	NetworkComponentDataModel,
	GraphNodeDataModel
}
