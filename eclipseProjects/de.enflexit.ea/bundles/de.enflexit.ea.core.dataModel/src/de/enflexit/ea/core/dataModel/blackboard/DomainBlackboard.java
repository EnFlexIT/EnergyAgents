package de.enflexit.ea.core.dataModel.blackboard;

/**
 * Superclass for domain-specific blackboards.
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg - Essen
 */
public abstract class DomainBlackboard {
	
	/**
	 * Resets the blackboard's data model.
	 */
	protected abstract void resetBlackboardDataModel();
	
	
	/**
	 * Process a blackboard request that is related to the blackboard's domain.
	 * @param blackboardRequest the blackboard request
	 * @return the blackoard answer
	 */
	public abstract AbstractBlackboardAnswer processBlackboardRequest(BlackboardRequest blackboardRequest);
}
