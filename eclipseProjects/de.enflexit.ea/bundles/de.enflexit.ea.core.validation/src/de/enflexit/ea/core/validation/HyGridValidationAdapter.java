package de.enflexit.ea.core.validation;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import hygrid.env.HyGridAbstractEnvironmentModel;

/**
 * The Class HyGridValidationAdapter implements the base methods that may be overwritten in extended classes.
 * In contrast to an interface, it also contains methods that enable to access instances 
 * of the current project and setup (e.g. the current {@link Project} or the current {@link SimulationSetup}). 
 */
public class HyGridValidationAdapter {

	private Project project;
	private SimulationSetup setup;
	
	private GraphEnvironmentController graphController;
	private NetworkModel networkModel;
	private HyGridAbstractEnvironmentModel hyGridAbsEnvModel;
	

	
	/**
	 * Validate the project after the project files were loaded.
	 *
	 * @param project the project
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateProjectAfterFileLoad(Project project) {
		return null;
	}
	
	/**
	 * Validate SimulationSetup after the setup files were loaded.
	 *
	 * @param setup the setup
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateSetupAfterFileLoad(SimulationSetup setup) {
		return null;
	}
	
	/**
	 * Validate graph settings after file load.
	 *
	 * @param graphSettings the graph settings
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateGeneralGraphSettingsAfterFileLoad(GeneralGraphSettings4MAS graphSettings) {
		return null;
	}
	
	
	
	
	/**
	 * Validate the specified project here.
	 *
	 * @param project the project
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateProject(Project project) {
		return null;
	}

	/**
	 * Validate the specified SimulationSetup here.
	 *
	 * @param setup the setup
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		return null;
	}
	
	/**
	 * Validate the specified NetworkModel here.
	 *
	 * @param networkModel the network model
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateNetworkModel(NetworkModel networkModel) {
		return null;
	}

	/**
	 * Validate the specified NetworkComponent here.
	 *
	 * @param netComp the net comp
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateNetworkComponent(NetworkComponent netComp) {
		return null;
	}

	/**
	 * Validate the specified HyGridAbstractEnvironmentModel here.
	 *
	 * @param absEnvModel the abs env model
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateHyGridAbstractEnvironmentModel(HyGridAbstractEnvironmentModel absEnvModel) {
		return null;
	}

	
	
	
	/**
	 * Validate an EOM {@link TechnicalSystem}.
	 *
	 * @param netComp the corresponding NetworkComponent
	 * @param ts the TechnicalSystem to check
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateEomTechnicalSystem(NetworkComponent netComp, TechnicalSystem ts) {
		return null;
	}

	/**
	 * Validate an EOM {@link TechnicalSystemGroup}.
	 *
	 * @param netComp the corresponding NetworkComponent
	 * @param tsg the TechnicalSystemGroup to check
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateEomTechnicalSystemGroup(NetworkComponent netComp, TechnicalSystemGroup tsg) {
		return null;
	}

	/**
	 * Validate an EOM {@link ScheduleList}.
	 *
	 * @param netComp the corresponding NetworkComponent
	 * @param sl the ScheduleList to check
	 * @return a {@link HyGridValidationMessage} if something is newsworthy or <code>null</code>
	 */
	public HyGridValidationMessage validateEomScheduleList(NetworkComponent netComp, ScheduleList sl) {
		return null;
	}
	
	
	
	
	/**
	 * Gets the project.
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}
	/**
	 * Sets the project.
	 * @param project the new project
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Gets the setup.
	 * @return the setup
	 */
	public SimulationSetup getSetup() {
		return setup;
	}
	/**
	 * Sets the setup.
	 * @param setup the new setup
	 */
	public void setSetup(SimulationSetup setup) {
		this.setup = setup;
	}

	/**
	 * Gets the graph controller.
	 * @return the graph controller
	 */
	public GraphEnvironmentController getGraphController() {
		return graphController;
	}
	/**
	 * Sets the graph controller.
	 * @param graphController the new graph controller
	 */
	public void setGraphController(GraphEnvironmentController graphController) {
		this.graphController = graphController;
	}

	/**
	 * Gets the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	/**
	 * Sets the network model.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}

	/**
	 * Gets the hy grid abstract environment model.
	 * @return the hy grid abstract environment model
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return hyGridAbsEnvModel;
	}
	/**
	 * Sets the hy grid abstract environment model.
	 * @param hyGridAbsEnvModel the new hy grid abstract environment model
	 */
	public void setHyGridAbstractEnvironmentModel(HyGridAbstractEnvironmentModel hyGridAbsEnvModel) {
		this.hyGridAbsEnvModel = hyGridAbsEnvModel;
	}

	
	/**
	 * Prints the message of the specified HyGridValidationMessage to the console.
	 * @param vMessage the HyGridValidationMessage
	 */
	protected void printHyGridValidationMessageToConsole(HyGridValidationMessage vMessage) {
		if (vMessage!=null && vMessage.getMessage()!=null && vMessage.getMessage().isEmpty()==false) {
			if (vMessage.getMessageType()==MessageType.Information) {
				System.out.println("[HyGridValidation][" + this.getClass().getSimpleName() + "] " + vMessage.getMessage());
			} else {
				System.err.println("[HyGridValidation][" + this.getClass().getSimpleName() + "] " + vMessage.getMessage());
			}
		}
	}
	
}
