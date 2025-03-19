package de.enflexit.ea.core.dataModel.deployment;

import java.io.Serializable;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.awb.core.config.DeviceAgentDescription;
import de.enflexit.common.StringHelper;

/**
 * This class is used to handle the information that is required for deploying agents
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class AgentDeploymentInformation implements Serializable, Comparable<AgentDeploymentInformation>{
	
	private static final long serialVersionUID = 8880091343037227671L;

	private String agentID;
	private String componentType;
	private String agentClassName;
	private AgentOperatingMode agentOperatingMode;
	
	
	/**
	 * Default constructor (just available for JAXB).
	 */
	@Deprecated
	public AgentDeploymentInformation() { }
	/**
	 * Instantiates a new deployed agent information.
	 * @param networkComponent the network component
	 * @param agentOperatingMode the agent operating mode
	 */
	public AgentDeploymentInformation(NetworkComponent networkComponent, String agentClassName, AgentOperatingMode agentOperatingMode) {
		this.agentID = networkComponent.getId();
		this.componentType = networkComponent.getType();
		this.agentClassName = agentClassName;
		this.agentOperatingMode = agentOperatingMode;
	}
	
	/**
	 * Gets the agent ID.
	 * @return the agent ID
	 */
	public String getAgentID() {
		return agentID;
	}
	/**
	 * Sets the agent ID.
	 * @param agentID the new agent ID
	 */
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	/**
	 * Gets the component type.
	 * @return the component type
	 */
	public String getComponentType() {
		return componentType;
	}
	/**
	 * Sets the component type.
	 * @param componentType the new component type
	 */
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	/**
	 * Gets the agent class name.
	 * @return the agent class name
	 */
	public String getAgentClassName() {
		return agentClassName;
	}
	/**
	 * Sets the agent class name.
	 * @param agentClassName the new agent class name
	 */
	public void setAgentClassName(String agentClassName) {
		this.agentClassName = agentClassName;
	}

	/**
	 * Gets the agent operating mode.
	 * @return the agent operating mode
	 */
	public AgentOperatingMode getAgentOperatingMode() {
		return agentOperatingMode;
	}
	/**
	 * Sets the agent operating mode.
	 * @param agentOperatingMode the new agent operating mode
	 */
	public void setAgentOperatingMode(AgentOperatingMode agentOperatingMode) {
		this.agentOperatingMode = agentOperatingMode;
	}
	
	/**
	 * Creates a {@link DeviceAgentDescription} for the agent.
	 * @return the device agent description
	 */
	public DeviceAgentDescription getDeviceAgentDescription() {
		return new DeviceAgentDescription(this.getAgentID(), this.getAgentClassName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return agentID + " - " + agentOperatingMode;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObj) {
	
		if (compObj==null) return false;
		if (compObj==this) return true;
		if (!(compObj instanceof AgentDeploymentInformation)) return false;

		// ------------------------------------------------
		// --- Compare object type ------------------------
		AgentDeploymentInformation adiComp = (AgentDeploymentInformation) compObj;
		
		if (StringHelper.isEqualString(adiComp.getAgentID(), this.getAgentID())==false) return false;
		if (StringHelper.isEqualString(adiComp.getAgentClassName(), this.getAgentClassName())==false) return false;
		if (StringHelper.isEqualString(adiComp.getComponentType(), this.getComponentType())==false) return false;
		
		AgentOperatingMode aomComp = adiComp.getAgentOperatingMode();
		AgentOperatingMode aomThis = this.getAgentOperatingMode();
		if (aomComp==null && aomThis==null) {
			// - equal -
		} else if ((aomComp!=null && aomThis==null) || (aomComp==null && aomThis!=null)) {
			return false;
		} else {
			if (aomComp!=aomThis) return false;
		}
		
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AgentDeploymentInformation other) {
		// --- Compare two AgentDeploymentInformations based on the agent IDs
		
		// --- Extract the numeric part from the ID -----------------
		Integer n1 = null;
		Integer n2 = null;
		try {
			// --- Remove all non-digit characters, parse the result ---------- 
			n1 = Integer.parseInt(this.getAgentID().replaceAll("\\D+",""));
			n2 = Integer.parseInt(other.getAgentID().replaceAll("\\D+",""));
		} catch (NumberFormatException e) {
		}
		
		if (n1!=null && n2!=null) {
			// --- If successful, compare the numbers -----------------
			return n1.compareTo(n2);
		} else {
			// --- Otherwise compare the original strings -------------
			return this.getAgentID().compareTo(other.getAgentID());
		}
	}
	
}
