package de.enflexit.ea.ui;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.ui.EnergyAgentUiService;

/**
 * The Class EnergyAgentSwingUiService.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EnergyAgentSwingUiService implements EnergyAgentUiService {


	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#openOrFocusUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void openOrFocusUI(AbstractEnergyAgent energyAgent) {
		
		System.out.println("[" + this.getClass() + "] invoked to open or focus the swing UI of an energy agent!");
		
	}

}
