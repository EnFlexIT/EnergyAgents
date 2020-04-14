package de.enflexit.ea.prosumer.validation;

import java.util.ArrayList;
import java.util.List;

import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.prosumer.ProsumerAgent;

/**
 * This class implements the actual validation for the Prosumer agent.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class ProsumerClassNameChecks extends HyGridValidationAdapter {

	private static final String PROSUMER_AGENT_CLASS_NAME_OLD = "hygrid.agent.prosumer.ProsumerAgent";
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateSetupAfterFileLoad(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetupAfterFileLoad(SimulationSetup setup) {
		
		HyGridValidationMessage vMessage = null;
		
		for (int i = 0; i < setup.getAgentList().size(); i++) {
			AgentClassElement4SimStart agentToStart = setup.getAgentList().get(i);
			if (agentToStart.getAgentClassReference().equals(PROSUMER_AGENT_CLASS_NAME_OLD)==true) {
				agentToStart.setAgentClassReference(ProsumerAgent.class.getName());
				// --- Create inform message ------------------------
				if (vMessage==null) {
					vMessage = this.getClassNameChangedMessage();
					vMessage.setMessage(vMessage.getMessage() + " in the setups start list for agents!");
					this.printHyGridValidationMessageToConsole(vMessage);
				}
			}
		} 
		return vMessage;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateGeneralGraphSettingsAfterFileLoad(org.awb.env.networkModel.settings.GeneralGraphSettings4MAS)
	 */
	@Override
	public HyGridValidationMessage validateGeneralGraphSettingsAfterFileLoad(GeneralGraphSettings4MAS graphSettings) {

		HyGridValidationMessage vMessage = null;
		
		List<ComponentTypeSettings> ctsList = new ArrayList<>(graphSettings.getCurrentCTS().values());
		for (int i = 0; i < ctsList.size(); i++) {
			// --- Check each ComponentTypeSettings instance -------- 
			ComponentTypeSettings cts = ctsList.get(i);
			if (cts.getAgentClass()!=null && cts.getAgentClass().isEmpty()==false && cts.getAgentClass().equals(PROSUMER_AGENT_CLASS_NAME_OLD)==true) {
				cts.setAgentClass(ProsumerAgent.class.getName());
				// --- Create inform message ------------------------
				if (vMessage==null) {
					vMessage = this.getClassNameChangedMessage();
					vMessage.setMessage(vMessage.getMessage() + " in the graph settings of the network model!");
					this.printHyGridValidationMessageToConsole(vMessage);
				}
			}
		}
		return vMessage;
	}
	
	/**
	 * Returns a default HyGridValidationMessage that the manages class name was changed.
	 * @return the class name changed message
	 */
	private HyGridValidationMessage getClassNameChangedMessage() {
		String message = "Adjusted class name for Prosumer";
		String description = "Changed class name from '" + PROSUMER_AGENT_CLASS_NAME_OLD + "' to '" + ProsumerAgent.class.getName() + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
