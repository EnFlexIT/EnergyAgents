package de.enflexit.ea.core.simulation.validation;

import java.util.Vector;

import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.ontology.Simple_Boolean;
import agentgui.ontology.Simple_String;
import de.enflexit.common.ontology.AgentStartArgument;
import de.enflexit.ea.core.simulation.manager.SimulationManager;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

public class SimulationManagerStartArgumentsCheck extends HyGridValidationAdapter {
	
	private HyGridValidationMessage validationMessage;

	@Override
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		
		Vector<AgentStartArgument> configuredArguments =this.getProject().getAgentStartConfiguration().get(SimulationManager.class.getName());
		
		if (configuredArguments==null || configuredArguments.size()>3) {
			this.addStringToValidationMessage("Not enough start arguments configured for the Simulation Manager class! At least three arguments are required: headlessOperation (boolean), showDashboard(boolean) and simulationSgentClasses(String)!");
		} else {
			if (configuredArguments.get(0).getOntologyReference().equals(Simple_Boolean.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for Simulation Manager first argument: Must be boolean!");
			}
			if (configuredArguments.get(1).getOntologyReference().equals(Simple_Boolean.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for Simulation Manager second argument: Must be boolean!");
			}
			if (configuredArguments.get(2).getOntologyReference().equals(Simple_String.class.getName())==false) {
				this.addStringToValidationMessage("Wrong data type for Simulation Manager first argument: Must be a String!");
			}
			
			AgentClassElement4SimStart simulationManagerClassElement = this.getSiMaClassElement(setup);
			if (simulationManagerClassElement==null) {
				this.addStringToValidationMessage("No simulation manager configured in the current setup");
			} else {
				String[] startArgs = simulationManagerClassElement.getStartArguments();
				
				if (startArgs==null) {
					this.addStringToValidationMessage("No start arguments specified for the simulaiton manager in the current setup");
				} else if (startArgs.length<3) {
					this.addStringToValidationMessage("Not enough arguments specified for the simulaiton manager in the current setup");
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
			this.validationMessage = new HyGridValidationMessage("Error checking simulation manager start arguments!", MessageType.Error);
			this.validationMessage.setDescription(messageString + "\n");
		} else {
			this.validationMessage.setDescription(this.validationMessage.getDescription() + messageString + "\n");
		}
	}
	
	private AgentClassElement4SimStart getSiMaClassElement(SimulationSetup setup) {
		for (AgentClassElement4SimStart agentElement : setup.getAgentList()) {
			if (agentElement.getAgentClassReference().equals(SimulationManager.class.getName())) {
				return agentElement;
			}
		}
		
		return null;
	}
	
}
