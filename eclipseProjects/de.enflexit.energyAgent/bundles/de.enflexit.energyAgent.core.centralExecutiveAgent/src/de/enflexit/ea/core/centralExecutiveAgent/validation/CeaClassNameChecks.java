package de.enflexit.ea.core.centralExecutiveAgent.validation;

import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * This class implements the actual validation for the SimulationManager
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class CeaClassNameChecks extends HyGridValidationAdapter {

	private static final String CEA_CLASS_NAME_OLD = "hygrid.agent.centralExecutiveAgent.CentralExecutiveAgent";
	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateSetup(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		
		HyGridValidationMessage vMessage = null;
		
		for (int i = 0; i < setup.getAgentList().size(); i++) {
			AgentClassElement4SimStart agentToStart = setup.getAgentList().get(i);
			if (agentToStart.getAgentClassReference().equals(CEA_CLASS_NAME_OLD)==true) {
				agentToStart.setAgentClassReference(CentralExecutiveAgent.class.getName());
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
		String message = "Adjusted class name for CentralExecutiveAgent";
		String description = "Changed class name from '" + CEA_CLASS_NAME_OLD + "' to '" + CentralExecutiveAgent.class.getName() + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
