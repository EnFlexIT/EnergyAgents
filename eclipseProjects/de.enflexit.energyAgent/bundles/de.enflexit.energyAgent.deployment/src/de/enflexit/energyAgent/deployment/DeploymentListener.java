package de.enflexit.energyAgent.deployment;

/**
 * Classes implementing this interface can register for deployment-related events.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public interface DeploymentListener {
	
	/**
	 * Deployment successful.
	 * @param groupID the group ID
	 */
	public void deploymentSuccessful(String groupID);
	
	/**
	 * Deployment failed.
	 * @param groupID the group ID
	 */
	public void deploymentFailed(String groupID);
}
