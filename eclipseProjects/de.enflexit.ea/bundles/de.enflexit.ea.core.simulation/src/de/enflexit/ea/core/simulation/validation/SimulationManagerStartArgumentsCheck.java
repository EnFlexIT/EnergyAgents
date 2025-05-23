package de.enflexit.ea.core.simulation.validation;

import java.util.Vector;

import agentgui.ontology.Simple_Boolean;
import agentgui.ontology.Simple_String;
import de.enflexit.awb.core.project.setup.AgentClassElement4SimStart;
import de.enflexit.awb.core.project.setup.SimulationSetup;
import de.enflexit.common.ontology.AgentStartArgument;
import de.enflexit.ea.core.simulation.manager.SimulationManager;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * The Class SimulationManagerStartArgumentsCheck.
 *
 * @author Nils Loose - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SimulationManagerStartArgumentsCheck extends HyGridValidationAdapter {
	
	private HyGridValidationMessage validationMessage;

	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateSetup(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		
		Vector<AgentStartArgument> configuredArguments = this.getProject().getAgentStartConfiguration().get(SimulationManager.class.getName());
		
		if (configuredArguments==null || configuredArguments.size()!=5) {
			this.addStringToValidationMessage("Not enough start arguments are configured for the SimulationManager agent! \n\nAt least five arguments are required: \n- isHeadlessOperation (Simple_Boolean), \n- removeEomModelsAfterAggregationBuild (Simple_Boolean), \n- isHideNetworkModelInSimulationStep (Simple_Boolean), \n- showDashboard (Simple_Boolean) and \n- additionalSimAgentClasses (Simple_String with comma separated String-values)!");
		} else {
			if (configuredArguments.get(0).getOntologyReference().equals(Simple_Boolean.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for SimulationManagers first argument: Must be Simple_Boolean for 'isHeadlessOperation'!");
			}
			if (configuredArguments.get(1).getOntologyReference().equals(Simple_Boolean.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for SimulationManagers second argument: Must be Simple_Boolean for 'removeEomModelsAfterAggregationBuild'!");
			}
			if (configuredArguments.get(2).getOntologyReference().equals(Simple_Boolean.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for SimulationManagers third argument: Must be Simple_Boolean for 'isHideNetworkModelInSimulationStep'!");
			}
			if (configuredArguments.get(3).getOntologyReference().equals(Simple_Boolean.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for SimulationManagers fourth argument: Must be Simple_Boolean for 'showDashboard'!");
			}
			if (configuredArguments.get(4).getOntologyReference().equals(Simple_String.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for SimulationManagers fifth argument: Must be a Simple_String for 'additionalSimAgentClasses'!");
			}
			
			AgentClassElement4SimStart simulationManagerClassElement = this.getSiMaClassElement(setup);
			if (simulationManagerClassElement==null) {
				this.addStringToValidationMessage("No SimulationManager is configured in the current setup");
			} else {
				String[] startArgs = simulationManagerClassElement.getStartArguments();
				
				if (startArgs==null) {
					this.addStringToValidationMessage("No start arguments are specified for the simulation manager in the current setup");
				} else if (startArgs.length<3) {
					this.addStringToValidationMessage("Not enough arguments are specified for the simulation manager in the current setup");
				}
			}
		}
		
		return this.validationMessage;
	}
	
	/**
	 * Appends a message string to the validation message, initializes the message before if necessary.
	 * @param messageString the message string
	 */
	private void addStringToValidationMessage(String messageString) {
		if (this.validationMessage==null) {
			this.validationMessage = new HyGridValidationMessage("SimulationManagers start argument configuration is not correct!", MessageType.Error);
			this.validationMessage.setDescription(messageString + "\n");
		} else {
			this.validationMessage.setDescription(this.validationMessage.getDescription() + messageString + "\n");
		}
	}
	
	/**
	 * Returns the SimulationManager (SiMa) class element.
	 *
	 * @param setup the setup
	 * @return the SiMa class element
	 */
	private AgentClassElement4SimStart getSiMaClassElement(SimulationSetup setup) {
		for (AgentClassElement4SimStart agentElement : setup.getAgentList()) {
			if (agentElement.getAgentClassReference().equals(SimulationManager.class.getName())) {
				return agentElement;
			}
		}
		return null;
	}
	
}
