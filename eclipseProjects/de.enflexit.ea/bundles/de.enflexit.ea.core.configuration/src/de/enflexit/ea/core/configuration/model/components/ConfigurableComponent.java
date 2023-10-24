package de.enflexit.ea.core.configuration.model.components;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.DomainSettings;

import agentgui.core.classLoadService.ClassLoadServiceUtility;
import agentgui.core.project.Project;
import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.common.ontology.AgentStartArgument;
import de.enflexit.common.ontology.AgentStartArguments;
import de.enflexit.common.ontology.gui.OntologyInstanceViewer;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.SetupConfigurationService;

/**
 * The abstract class ConfigurableComponent serves as base class for specific aspects that may be configured 
 * by providing and using a {@link SetupConfigurationService}.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ConfigurableComponent {

	private List<SetupConfigurationAttributeService> relevantServiceAttributeList;
	
	private GraphEnvironmentController graphController;
	private NetworkComponent networkComponent;
	private ComponentTypeSettings componentTypeSettings;
	private AgentStartArguments agentStartArguments;
	
	
	/**
	 * Instantiates a new configurable component.
	 *
	 * @param graphController the graph controller
	 * @param netComp the current NetworkComponent
	 * @param componentTypeSettings the corresponding ComponentTypeSettings
	 */
	public ConfigurableComponent(GraphEnvironmentController graphController, NetworkComponent netComp, ComponentTypeSettings componentTypeSettings) {
		this.graphController = graphController;
		this.networkComponent = netComp;
		this.componentTypeSettings = componentTypeSettings;
	}

	
	/**
	 * Returns the list of relevant {@link SetupConfigurationAttributeService}s.
	 * @return the relevant setup configuration service attribute list
	 */
	public List<SetupConfigurationAttributeService> getRelevantSetupConfigurationAttributeServiceList() {
		if (relevantServiceAttributeList==null) {
			relevantServiceAttributeList = new ArrayList<>();
		}
		return relevantServiceAttributeList;
	}
	/**
	 * Adds the specified SetupConfigurationAttributeService to the list of relevant .
	 * @param attributeService the setup conf attribute
	 */
	public void addConfiguredBy(SetupConfigurationAttributeService attributeService) {
		if (this.getRelevantSetupConfigurationAttributeServiceList().contains(attributeService)==false) {
			this.getRelevantSetupConfigurationAttributeServiceList().add(attributeService);
		}
	}
	
	/**
	 * Returns that the current ConfigurableComponent is for an EOM model or not.
	 * @return true, if the current ConfigurableComponent is for an EOM model
	 */
	public boolean isEomModel() {
		return false;
	}
	
	/**
	 * Returns the current project.
	 * @return the project
	 */
	public Project getProject() {
		return this.graphController.getProject();
	}
	/**
	 * Returns the current setup.
	 * @return the setup
	 */
	public SimulationSetup getSetup() {
		return this.getProject().getSimulationSetups().getCurrSimSetup();
	}
	
	/**
	 * Returns the current graph controller.
	 * @return the graph controller
	 */
	public GraphEnvironmentController getGraphController() {
		return graphController;
	}
	/**
	 * Returns the current network component.
	 * @return the network component
	 */
	public NetworkComponent getNetworkComponent() {
		return networkComponent;
	}
	/**
	 * returns the current ComponentTypeSettings.
	 * @return the component type settings
	 */
	public ComponentTypeSettings getComponentTypeSettings() {
		return componentTypeSettings;
	}

	/**
	 * Returns the domain settings for the current component.
	 * @return the domain settings
	 */
	public DomainSettings getDomainSettings() {
		return this.getGraphController().getGeneralGraphSettings4MAS().getDomainSettings().get(this.getDomain());
	}
	/**
	 * Returns the domain.
	 * @return the domain
	 */
	public String getDomain() {
		return this.getComponentTypeSettings().getDomain();
	}
	
	/**
	 * Checks for intended agent start arguments.
	 * @return true, if successful
	 */
	public boolean intendsAgentStartArguments() {
		
		// --- If no agent is specified, simply return --------------
		String agentClass = this.getComponentTypeSettings().getAgentClass(); 
		if (agentClass==null || agentClass.isBlank()==true) return false;
		
		return this.getProject().getAgentStartConfiguration().getAgentStartArguments(agentClass)!=null;
	}
	/**
	 * Returns the {@link AgentStartArguments} if the current component is defined with an agent class .
	 * @return the agent start argument
	 */
	public AgentStartArguments getAgentStartArguments() {
		if (this.intendsAgentStartArguments()==true && agentStartArguments==null) {
			agentStartArguments = this.getProject().getAgentStartConfiguration().getAgentStartArguments(this.getComponentTypeSettings().getAgentClass());
		}
		return agentStartArguments;
	}

	/**
	 * Returns the configured start arguments for the current component.
	 * @return the configured start arguments
	 */
	public AgentClassElement4SimStart getAgentClassElement4SimStart() {
		return this.getSetup().getAgentClassElement4SimStart(this.getNetworkComponent().getId());
	}
	
	/**
	 * Returns the configured agent start arguments.
	 * @return the configured start arguments
	 */
	public Object[] getConfiguredAgentStartArguments() {
		
		if (this.intendsAgentStartArguments()==false) return null;
		
		// --- Get required start arguments ------------------------- 
		Vector<AgentStartArgument> asaVector = this.getAgentStartArguments().getStartArguments();
		// --- Get configured arguments -----------------------------
		AgentClassElement4SimStart ace4SimStart = this.getAgentClassElement4SimStart();

		// --- Create or initiate start arguments -------------------
		Object[] config = this.getStartArguments(ace4SimStart);
		if (config==null) {
			config = new Object[asaVector.size()];
			// --- Create the required instances -------------------- 
			for (int i = 0; i < asaVector.size(); i++) {
				AgentStartArgument startArgument = asaVector.get(i);
				try {
					config[i] = ClassLoadServiceUtility.newInstance(startArgument.getOntologyReference());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoClassDefFoundError ex) {
					ex.printStackTrace();
				} 
			}
			
		}
		return config;
	}
	
	/**
	 * This method will return the Object Array for the start argument of an agent.
	 *
	 * @param ace4SimStart the AgentClassElement4SimStart
	 * @return the start arguments as object array
	 */
	private Object[] getStartArguments(AgentClassElement4SimStart ace4SimStart) {
		
		if (ace4SimStart!=null && ace4SimStart.getStartArguments()!=null) {
			
			String selectedAgentReference = ace4SimStart.getElementClass().getName();
			OntologyInstanceViewer oiv = new OntologyInstanceViewer(this.getProject().getOntologyVisualisationHelper(), this.getProject().getAgentStartConfiguration(), selectedAgentReference);
			oiv.setConfigurationXML(ace4SimStart.getStartArguments());
			return oiv.getConfigurationInstances();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String description = this.getNetworkComponent().getId();
		if (isEomModel()==false) {
			description += " (" + networkComponent.getType() + ")"; 
		} else {
			ConfigurableEomComponent eomComponent = (ConfigurableEomComponent) this;
			if (eomComponent.isSubSystem()==true) {
				// TOOD
			}
		}
		return description;
	}
	
}
