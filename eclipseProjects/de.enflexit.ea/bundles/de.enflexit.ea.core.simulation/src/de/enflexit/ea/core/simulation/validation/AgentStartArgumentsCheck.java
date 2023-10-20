package de.enflexit.ea.core.simulation.validation;

import java.util.Vector;

import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.common.ontology.AgentStartArgument;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * This validation checks if the expected start arguments for all agents are configured.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class AgentStartArgumentsCheck extends HyGridValidationAdapter {
	
	private HyGridValidationMessage validationMessage;
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateSetup(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		
		for (AgentClassElement4SimStart agentElement : setup.getAgentList()) {
			String agentClass = agentElement.getAgentClassReference();
			Vector<AgentStartArgument> expectedArgs =this.getProject().getAgentStartConfiguration().get(agentClass);
			
			if (expectedArgs != null) {
				
				String[] startArgs = agentElement.getStartArguments();
				
				if (startArgs==null) {
					this.addStringToValidationMessage("No start arguments defined for agent " + agentElement.getStartAsName() + " - expecting " + expectedArgs.size() + " arguments!");
				} else if (agentElement.getStartArguments().length != expectedArgs.size()) {
					this.addStringToValidationMessage("Wrong number of start arguments defined for agent " + agentElement.getStartAsName() + " - expecting " + expectedArgs.size() + " arguments, found " + startArgs.length + "!");
				} else {
					for (int i=0; i<agentElement.getStartArguments().length; i++) {
						if (agentElement.getStartArguments()[i] == null) {
							this.addStringToValidationMessage("Start argument " + (i+1) + " defined for agent " + agentElement.getStartAsName() + " is null!");
						} else {
							//TODO Type check
						}
					}
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
			this.validationMessage = new HyGridValidationMessage("Error checking agent start arguments!", MessageType.Error);
			this.validationMessage.setDescription(messageString + "\n");
		} else {
			this.validationMessage.setDescription(this.validationMessage.getDescription() + messageString + "\n");
		}
	}

}
