package de.enflexit.ea.core.dataModel.deployment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

/**
 * This class stores information about a group of {@link AbstractEnergyAgent}s that 
 * are deployed together on the same target system.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentGroup", propOrder = {
	"groupID",
	"active",
    "projectVersion",
    "deploymentSettings",
    "agentsHashMap"
})
public class DeploymentGroup implements Serializable{
	
	private static final long serialVersionUID = 5012223151823840073L;
	
	private String groupID;
	private boolean active;
	private String projectVersion;
	private DeploymentSettings deploymentSettings;
	
	@XmlElementWrapper(name="agentsHashMap")
	private TreeMap<String, AgentDeploymentInformation> agentsHashMap;
	
	/**
	 * Gets the group ID.
	 * @return the group ID
	 */
	public String getGroupID() {
		return groupID;
	}
	/**
	 * Sets the group ID.
	 * @param groupID the new group ID
	 */
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	
	/**
	 * Checks if is active.
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}
	/**
	 * Sets the active.
	 * @param active the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Gets the project version.
	 * @return the project version
	 */
	public String getProjectVersion() {
		return projectVersion;
	}
	/**
	 * Sets the project version.
	 * @param projectVersion the new project version
	 */
	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}
	
	/**
	 * Gets the deployment settings.
	 * @return the deployment settings
	 */
	public DeploymentSettings getDeploymentSettings() {
		return deploymentSettings;
	}
	/**
	 * Sets the deployment settings.
	 * @param deploymentSettings the new deployment settings
	 */
	public void setDeploymentSettings(DeploymentSettings deploymentSettings) {
		this.deploymentSettings = deploymentSettings;
	}

	/**
	 * Gets the agents list.
	 * @return the agents list
	 */
	public TreeMap<String, AgentDeploymentInformation> getAgentsHashMap() {
		if (agentsHashMap==null) {
			agentsHashMap = new TreeMap<>();
		}
		return agentsHashMap;
	}

	/**
	 * Adds an agent to the group.
	 * @param agentInfo the agent info
	 */
	public void addAgent(AgentDeploymentInformation agentInfo) {
		this.getAgentsHashMap().put(agentInfo.getAgentID(), agentInfo);
	}
	
	/**
	 * Adds a list of agents to this group.
	 * @param agentsList the agents list
	 */
	public void addAllAgents(List<AgentDeploymentInformation> agentsList) {
		for (int i=0; i<agentsList.size(); i++) {
			AgentDeploymentInformation agentInfo = agentsList.get(i);
			this.getAgentsHashMap().put(agentInfo.getAgentID(), agentInfo);
		}
	}
	
	/**
	 * Gets the IDs of all agents in this group.
	 * @return the IDs
	 */
	public List<String> getAgentIDs() {
		return new ArrayList<>(this.getAgentsHashMap().keySet());
	}
	
	/**
	 * Gets the network component types of all agents in this group
	 * @return the network component types
	 */
	public List<String> getNetworkComponentTypes() {
		Set<String> componentTypes = new HashSet<>();
		List<AgentDeploymentInformation> agentsList = new ArrayList<>(this.getAgentsHashMap().values());
		for (int i=0; i<agentsList.size(); i++) {
			componentTypes.add(agentsList.get(i).getComponentType());
		}
		return new ArrayList<>(componentTypes);
	}
	
	/**
	 * Gets the class names of all agents in this group
	 * @return the agent class names
	 */
	public List<String> getAgentClassNames() {
		Set<String> agentClassNames = new HashSet<>();
		List<AgentDeploymentInformation> agentsList = new ArrayList<>(this.getAgentsHashMap().values());
		for (int i=0; i<agentsList.size(); i++) {
			agentClassNames.add(agentsList.get(i).getAgentClassName());
		}
		return new ArrayList<>(agentClassNames);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObj) {

		if (compObj==null) return false;
		if (compObj==this) return true;
		if (!(compObj instanceof DeploymentGroup)) return false;

		// ------------------------------------------------
		// --- Compare object type ------------------------
		DeploymentGroup dgComp = (DeploymentGroup) compObj;
		
		// --- Group-ID -----------------------------------
		String groupIDComp = dgComp.getGroupID();
		String groupIDThis = this.getGroupID();
		if (groupIDComp==null && groupIDThis==null) {
			// - equal -
		} else if ((groupIDComp!=null && groupIDThis==null) || (groupIDComp==null && groupIDThis!=null)) {
			return false;
		} else {
			if (groupIDComp.equals(groupIDThis)==false) return false;
		}
		
		// --- Active? ------------------------------------
		if (dgComp.isActive()==this.isActive()==false) return false;
		
		// --- Project version ----------------------------
		String pVersionComp = dgComp.getProjectVersion();
		String pVersionThis = this.getProjectVersion();
		if (pVersionComp==null && pVersionThis==null) {
			// - equal -
		} else if ((pVersionComp!=null && pVersionThis==null) || (pVersionComp==null && pVersionThis!=null)) {
			return false;
		} else {
			if (pVersionComp.equals(pVersionThis)==false) return false;
		}

		// --- DeploymentSettings -------------------------
		DeploymentSettings dsComp = dgComp.getDeploymentSettings();
		DeploymentSettings dsThis = this.getDeploymentSettings();
		if (dsComp==null && dsThis==null) {
			// - equal -
		} else if ((dsComp!=null && dsThis==null) || (dsComp==null && dsThis!=null)) {
			return false;
		} else {
			if (dsComp.equals(dsThis)==false) return false;
		}
		
		// --- TreeMap for AgentDeploymentInformation -----
		TreeMap<String, AgentDeploymentInformation> adInfComp = dgComp.getAgentsHashMap();
		TreeMap<String, AgentDeploymentInformation> adInfThis = this.getAgentsHashMap();
		if (adInfComp.size()!=adInfThis.size()) return false;
		
		List<String> agentIDList = new ArrayList<String>(adInfComp.keySet());
		for (int i = 0; i < agentIDList.size(); i++) {
			String agentID = agentIDList.get(i);
			AgentDeploymentInformation adiComp = adInfComp.get(agentID);
			AgentDeploymentInformation adiThis = adInfThis.get(agentID);
			if (adiComp==null && adiThis==null) {
				// --- equal, will not happen
			} else if ((adiComp!=null && adiThis==null) || (adiComp==null && adiThis!=null)) {
				return false;
			} else {
				if (adiComp.equals(adiThis)==false) return false;
			}
		}

		// --- No differences found -------------
		return true;
	}
	
	
}