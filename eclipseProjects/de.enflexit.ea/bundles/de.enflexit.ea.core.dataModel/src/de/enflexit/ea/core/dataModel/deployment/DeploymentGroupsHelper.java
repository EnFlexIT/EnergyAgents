package de.enflexit.ea.core.dataModel.deployment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.deployment.SetupExtension.Changed;
import jade.core.AID;

/**
 * This class provides a couple of methods for managing deployment groups
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DeploymentGroupsHelper {
	
	private SetupExtension setupExtension;
	
	/**
	 * Instantiates a new deployment groups handler.
	 * @param deploymentGroups the deployment groups
	 */
	public DeploymentGroupsHelper(SetupExtension setupExtension) {
		this.setupExtension = setupExtension;
	}

	/**
	 * Gets the deployment groups.
	 * @return the deployment groups
	 */
	public TreeMap<String, DeploymentGroup> getDeploymentGroups() {
		return this.getSetupExtension().getDeploymentGroups();
	}
	
	/**
	 * Gets the setup extension.
	 * @return the setup extension
	 */
	public SetupExtension getSetupExtension() {
		return setupExtension;
	}

	/**
	 * Sets the setup extension.
	 * @param setupExtension the new setup extension
	 */
	public void setSetupExtension(SetupExtension setupExtension) {
		this.setupExtension = setupExtension;
	}

	/**
	 * Adds a deployment group.
	 * @param deploymentGroup the deployment group
	 */
	public void addDeploymentGroup(DeploymentGroup deploymentGroup) {
		this.getDeploymentGroups().put(deploymentGroup.getGroupID(), deploymentGroup);
		this.getSetupExtension().setSetupExtensionChanged(Changed.DEPLOYED_AGENTS_LIST);
	}
	
	/**
	 * Adds the deployed agent.
	 *
	 * @param deploymentGroupName the deployment group name
	 * @param networkComponent the network component
	 * @param agentClassName the agent class name
	 * @param agentOperatingMode the agent operating mode
	 */
	public void addDeployedAgent(String deploymentGroupName, NetworkComponent networkComponent, String agentClassName, AgentOperatingMode agentOperatingMode) {
		DeploymentGroup deploymentGroup = this.getDeploymentGroups().get(deploymentGroupName);
		if (deploymentGroup==null) {
			deploymentGroup = new DeploymentGroup();
			deploymentGroup.setGroupID(deploymentGroupName);
			this.getDeploymentGroups().put(deploymentGroupName, deploymentGroup);
		}
		deploymentGroup.addAgent(new AgentDeploymentInformation(networkComponent, agentClassName, agentOperatingMode));
		this.getSetupExtension().setSetupExtensionChanged(Changed.DEPLOYED_AGENTS_LIST);
	}
	
	
	/**
	 * Gets the IDs of all deployed agents.
	 * @return the IDs of all deployed agents
	 */
	public List<String> getDeployedAgentIDs() {
		List<String> deployedAgentIDs = new ArrayList<>();
		ArrayList<String> groupIDs = new ArrayList<String>(this.getDeploymentGroups().keySet());
		for(int i=0; i<groupIDs.size(); i++) {
			DeploymentGroup group = this.getDeploymentGroups().get(groupIDs.get(i));
			deployedAgentIDs.addAll(group.getAgentIDs());
		}
		return deployedAgentIDs;
	}
	
	/**
	 * Gets a list of all currently deployed agents.
	 * @return the list of deployed agents
	 */
	public List<AgentDeploymentInformation> getAllDeployedAgents() {
		List<AgentDeploymentInformation> deployedAgentsList = new ArrayList<>();
		ArrayList<String> groupIDs = new ArrayList<String>(this.getDeploymentGroups().keySet());
		for(int i=0; i<groupIDs.size(); i++) {
			DeploymentGroup group = this.getDeploymentGroups().get(groupIDs.get(i));
			deployedAgentsList.addAll(group.getAgentsHashMap().values());
		}
		return deployedAgentsList;
	}
	
	/**
	 * Gets the network component types of all deployed agents.
	 * @return the network component types of all deployed agents
	 */
	public List<String> getNetworkComponentTypes() {
		Set<String> componentTypes = new HashSet<String>();
		ArrayList<String> groupIDs = new ArrayList<String>(this.getDeploymentGroups().keySet());
		for(int i=0; i<groupIDs.size(); i++) {
			DeploymentGroup group = this.getDeploymentGroups().get(groupIDs.get(i));
			componentTypes.addAll(group.getNetworkComponentTypes());
		}
		return new ArrayList<>(componentTypes);
	}
	
	/**
	 * Gets the agent class names of all deployed agents.
	 * @return the agent class names of all deployed agents
	 */
	public List<String> getAgentClassNames() {
		Set<String> classNames = new HashSet<String>();
		ArrayList<String> groupIDs = new ArrayList<String>(this.getDeploymentGroups().keySet());
		for(int i=0; i<groupIDs.size(); i++) {
			DeploymentGroup group = this.getDeploymentGroups().get(groupIDs.get(i));
			classNames.addAll(group.getAgentClassNames());
		}
		return new ArrayList<>(classNames);
	}
	
	/**
	 * Checks if an agent is deployed.
	 * @param aid the agent's aid
	 * @return true, if the agent is deployed
	 */
	public boolean isAgentDeployed(AID aid) {
		return this.isAgentDeployed(aid.getLocalName());
	}
	
	/**
	 * Checks if an agent is deployed.
	 * @param localName the agent's local name
	 * @return true, if is agent deployed
	 */
	public boolean isAgentDeployed(String localName) {
		return this.getDeployedAgentIDs().contains(localName);
	}
	
	/**
	 * Gets the agent deployment information for an agent.
	 * @param localName the local name
	 * @return the agent deployment information, or null if the agent was not deployed
	 */
	public AgentDeploymentInformation getAgentDeploymentInformation(String localName) {

		// --- Check all groups for the agent -------------
		List<String> groupIDs = new ArrayList<>(this.getDeploymentGroups().keySet());
		for (int i=0; i<groupIDs.size(); i++) {
			DeploymentGroup group = this.getDeploymentGroups().get(groupIDs.get(i));
			AgentDeploymentInformation agentInfo = group.getAgentsHashMap().get(localName);
			if (agentInfo!=null) {
				return agentInfo;
			}
		}
		
		// --- Agent not found ----------------------------
		return null;
	}
	
	/**
	 * Looks up the agent operating mode for one agent.
	 * @param localName the agent's local name
	 * @return the agent's operating mode
	 */
	public AgentOperatingMode getAgentOperatingMode(String localName) {
		AgentDeploymentInformation agentInfo = this.getAgentDeploymentInformation(localName);
		if (agentInfo!=null) {
			return agentInfo.getAgentOperatingMode();
		} else {
			return AgentOperatingMode.Simulation;
		}
	}
	
	/**
	 * Gets the deployment group for the agent with the specified AID.
	 * @param aid the aid
	 * @return the deployment group, null if there is no group containing this agent
	 */
	public DeploymentGroup getDeploymentGroupForAgent(AID aid) {
		return this.getDeploymentGroupForAgent(aid.getLocalName());
	}
	
	/**
	 * Gets the deployment group for the agent with the specified local name.
	 * @param localName the local name
	 * @return the deployment group, null if there is no group containing this agent
	 */
	public DeploymentGroup getDeploymentGroupForAgent(String localName) {
		List<DeploymentGroup> deploymentGroups = new ArrayList<>(this.getDeploymentGroups().values());
		for (int i=0; i<deploymentGroups.size(); i++) {
			DeploymentGroup deploymentGroup = deploymentGroups.get(i);
			if (deploymentGroup.getAgentIDs().contains(localName)) {
				return deploymentGroup;
			}
		}
		return null;
	}
	
	/**
	 * Checks if the deployment for the specified agent is currently activated.
	 * @param aid the agent's aid
	 * @return true, if the deployment is currently activated
	 */
	public boolean isDeploymentActivated(AID aid) {
		return this.isDeploymentActivated(aid.getLocalName());
	}
	
	/**
	 * Checks if the deployment for the specified agent is currently activated.
	 * @param localName the agent's local name
	 * @return true, if the deployment is currently activated
	 */
	public boolean isDeploymentActivated(String localName) {
		DeploymentGroup deploymentGroup = this.getDeploymentGroupForAgent(localName);
		if (deploymentGroup!=null) {
			return deploymentGroup.isActive();
		} else {
			return false;
		}
	}
}
