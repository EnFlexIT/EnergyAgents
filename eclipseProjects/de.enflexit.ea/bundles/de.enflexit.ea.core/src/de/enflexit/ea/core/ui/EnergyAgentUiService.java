package de.enflexit.ea.core.ui;

import de.enflexit.ea.core.AbstractEnergyAgent;

/**
 * The Interface EnergyAgentUiService describes the method structure to 
 * connect an Energy Agent with a visualization.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface EnergyAgentUiService {

	/**
	 * Has to open (or (focus) the UI for the specified energy agent.
	 * @param energyAgent the energy agent
	 */
	public void openOrFocusUI(AbstractEnergyAgent energyAgent);
	
	/**
	 * Has to update the UI for the specified energy agent.
	 * @param energyAgent the energy agent
	 */
	public void updateUI(AbstractEnergyAgent energyAgent);
	
	/**
	 * Has to close the UI of the specified energy agent.
	 * @param energyAgent the energy agent
	 */
	public void closeUI(AbstractEnergyAgent energyAgent);
	
}
