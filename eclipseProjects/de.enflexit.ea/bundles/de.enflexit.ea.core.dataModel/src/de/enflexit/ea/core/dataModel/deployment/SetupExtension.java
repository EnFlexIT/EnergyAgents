package de.enflexit.ea.core.dataModel.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.awb.env.networkModel.NetworkComponent;

import agentgui.core.application.Application;
import agentgui.core.common.AbstractUserObject;
import de.enflexit.common.SerialClone;
import energy.schedule.loading.ScheduleTimeRange;

/**
 * Container class for additional information extending the regular simulation setup.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
@XmlRootElement
public class SetupExtension extends AbstractUserObject {
	
	private static final long serialVersionUID = -1811252701202491266L;

	/** Lists the possible reasons why a SimulationSetup can be changed and unsaved  */
	public enum Changed {
		DEPLOYED_AGENTS_LIST
	}

	private TreeMap<String, DeploymentGroup> deploymentGroups;
	
	private transient DeploymentGroupsHelper deploymentGroupsHandler;
	private transient List<SetupExtensionListener> listener;
	
	private ScheduleTimeRange scheduleTimeRange;
	
	/**
	 * Gets the deployment groups.
	 * @return the deployment groups
	 */
	public TreeMap<String, DeploymentGroup> getDeploymentGroups() {
		if (deploymentGroups==null) {
			deploymentGroups = new TreeMap<>();
		}
		return deploymentGroups;
	}
	
	/**
	 * Gets the deployment groups handler.
	 * @return the deployment groups handler
	 */
	public DeploymentGroupsHelper getDeploymentGroupsHelper() {
		if (deploymentGroupsHandler==null) {
			deploymentGroupsHandler = new DeploymentGroupsHelper(this);
		}
		return deploymentGroupsHandler;
	}


	/**
	 * Sets the deployment groups.
	 * @param deploymentGroups the deployment groups
	 */
	public void setDeploymentGroups(TreeMap<String, DeploymentGroup> deploymentGroups) {
		this.deploymentGroups = deploymentGroups;
	}
	
	/**
	 * Adds a deployment group.
	 * @param deploymentGroup the deployment group
	 */
	public void addDeploymentGroup(DeploymentGroup deploymentGroup) {
		this.getDeploymentGroups().put(deploymentGroup.getGroupID(), deploymentGroup);
		this.setSetupExtensionChanged(Changed.DEPLOYED_AGENTS_LIST);
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
		this.setSetupExtensionChanged(Changed.DEPLOYED_AGENTS_LIST);
	}

	
	/**
	 * Returns the ScheduleTimeRange for the current setup.
	 * @return the ScheduleTimeRange
	 */
	public ScheduleTimeRange getScheduleTimeRange() {
		return scheduleTimeRange;
	}
	/**
	 * Sets the ScheduleTimeRange for the current setup.
	 * @param scheduleTimeRange the new schedule time range
	 */
	public void setScheduleTimeRange(ScheduleTimeRange scheduleTimeRange) {
		this.scheduleTimeRange = scheduleTimeRange;
	}
	
	
	/**
	 * Returns the copy of the current instance.
	 * @return the copy
	 */
	public SetupExtension getCopy() {
		return SerialClone.clone(this);
	}
	
	
	
	
	
	// --------------------------------------------------------------
	// --- From here, methods for listener can be found ------------- 
	// --------------------------------------------------------------
	private List<SetupExtensionListener> getListener() {
		if (listener==null) {
			listener = new ArrayList<>();
		}
		return listener;
	}
	/**
	 * Adds the specified setup extension listener.
	 * @param newListener the new listener
	 */
	public void addSetupExtensionListener(SetupExtensionListener newListener) {
		if (newListener!=null && this.getListener().contains(newListener)==false) {
			this.getListener().add(newListener);
		}
	}
	/**
	 * Removes the specified setup extension listener, if registered.
	 * @param newListener the new listener
	 */
	public boolean removeSetupExtensionListener(SetupExtensionListener listener) {
		if (listener!=null && this.getListener().contains(listener)==true) {
			return this.getListener().remove(listener);
		}
		return false;
	}
	/**
	 * Notifies registered listener about changes in the current instance
	 */
	public void setSetupExtensionChanged(Changed reason){
		if (this.getListener().size()>0) {
			for (int i = 0; i < this.getListener().size(); i++) {
				this.getListener().get(i).setSetupExtensionChanged(reason);
			}
			Application.getProjectFocused().setUnsaved(true);
		}
	}
	
}
