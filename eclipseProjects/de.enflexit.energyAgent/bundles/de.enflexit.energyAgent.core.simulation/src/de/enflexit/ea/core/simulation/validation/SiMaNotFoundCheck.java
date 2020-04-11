package de.enflexit.ea.core.simulation.validation;

import javax.swing.DefaultListModel;

import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.ea.core.simulation.manager.SimulationManager;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * This class implements the actual validation for the SimulationManager
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SiMaNotFoundCheck extends HyGridValidationAdapter {

	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateSetup(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	public HyGridValidationMessage validateSetup(SimulationSetup setup) {
		
		DefaultListModel<AgentClassElement4SimStart> agentList = setup.getAgentDefaultListModel(SimulationSetup.AGENT_LIST_ManualConfiguration);
		if (agentList==null) return null;
		
		for (int i=0; i<agentList.getSize(); i++) {
			AgentClassElement4SimStart ace = agentList.get(i);
			if (ace.getAgentClassReference().equals(SimulationManager.class.getName())) {
				// --- SimulationManager found -> no error ----------
				return null;
			}
		}
		// --- SimulationManager not found -------------------------- 
		return new HyGridValidationMessage("No simulation manager found!", MessageType.Error, "No simulation manager found - please add a SimulationManager to the agent start list!");
	}

}
