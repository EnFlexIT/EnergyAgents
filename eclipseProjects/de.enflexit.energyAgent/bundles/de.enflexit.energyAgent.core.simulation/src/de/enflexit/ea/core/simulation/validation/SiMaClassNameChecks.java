package de.enflexit.ea.core.simulation.validation;

import java.util.Vector;

import agentgui.core.project.Project;
import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.common.ontology.AgentStartArguments;
import de.enflexit.common.ontology.AgentStartConfiguration;
import de.enflexit.ea.core.simulation.manager.SimulationManager;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * This class implements the actual validation for the SimulationManager
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SiMaClassNameChecks extends HyGridValidationAdapter {

	private static final String SIMULATION_MANAGER_CLASS_NAME_OLD = "hygrid.agent.manager.SimulationManager";
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateProjectAfterFileLoad(agentgui.core.project.Project)
	 */
	@Override
	public HyGridValidationMessage validateProjectAfterFileLoad(Project project) {
	
		HyGridValidationMessage vMessage = null;
		
		// --- Check the start arguments have old entries -----------
		AgentStartConfiguration agentStartConfiguration = project.getAgentStartConfiguration();
		Vector<AgentStartArguments> startArgumentsVector = agentStartConfiguration.getAgentStartArguments();
		for (int i = 0; i < startArgumentsVector.size(); i++) {
			AgentStartArguments startArguments = startArgumentsVector.get(i);
			String agentClassName = startArguments.getAgentReference();
			if (agentClassName.equals(SIMULATION_MANAGER_CLASS_NAME_OLD)==true) {
				startArguments.setAgentReference(SimulationManager.class.getName());
				// --- Create inform message ------------------------
				vMessage = this.getClassNameChangedMessage();
				vMessage.setMessage(vMessage.getMessage() + " in the project start argument list!");
				this.printHyGridValidationMessageToConsole(vMessage);
			}
		}
		return vMessage;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateSetupAfterFileLoad(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetupAfterFileLoad(SimulationSetup setup) {
		
		HyGridValidationMessage vMessage = null;
		
		for (int i = 0; i < setup.getAgentList().size(); i++) {
			AgentClassElement4SimStart agentToStart = setup.getAgentList().get(i);
			if (agentToStart.getAgentClassReference().equals(SIMULATION_MANAGER_CLASS_NAME_OLD)==true) {
				agentToStart.setAgentClassReference(SimulationManager.class.getName());
				// --- Create inform message ------------------------
				vMessage = this.getClassNameChangedMessage();
				vMessage.setMessage(vMessage.getMessage() + " in the setups start list for agents!");
				this.printHyGridValidationMessageToConsole(vMessage);
			}
		} 
		return vMessage;
	}

	/**
	 * Returns a default HyGridValidationMessage that the manages class name was changed.
	 * @return the class name changed message
	 */
	private HyGridValidationMessage getClassNameChangedMessage() {
		String message = "Adjusted class name for SimulationManger";
		String description = "Changed class name from 'hygrid.agent.manager.SimulationManager' to '" + SimulationManager.class.getName() + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
