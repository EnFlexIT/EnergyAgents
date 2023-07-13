package de.enflexit.ea.core.ui;

import java.util.List;

import de.enflexit.common.ServiceFinder;
import de.enflexit.ea.core.AbstractEnergyAgent;

/**
 * The Class EnergyAgentUiConnector is used by an energy agent 
 * to transfer internal information to the UI.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EnergyAgentUiConnector {

	private AbstractEnergyAgent energyAgent;
	private List<EnergyAgentUiService> uiServiceList;
	
	
	/**
	 * Instantiates a new energy agent UI connector.
	 * @param energyAgent the energy agent
	 */
	public EnergyAgentUiConnector(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
	}

	/**
	 * Returns the list of currently registered EnergyAgentUiService instances.
	 * @return the list of EnergyAgentUiService
	 */
	private List<EnergyAgentUiService> getEnergyAgentUiServices() {
		if (uiServiceList==null) {
			uiServiceList = ServiceFinder.findServices(EnergyAgentUiService.class); 
		}
		return uiServiceList;
	}
	/**
	 * Checks if an UI service for energy agents is available.
	 *
	 * @param isWriteErrorMessage the indicator to write error messages (or not)
	 * @return true, if is ui service available
	 */
	private boolean isUiServiceAvailable(boolean isWriteErrorMessage) {
		if (this.getEnergyAgentUiServices().size()==0) {
			if (isWriteErrorMessage==true) {
				System.err.println("[" + this.getClass().getSimpleName() + "|" + this.energyAgent.getLocalName() + "] No EnergyAgentUiService could be found!");
			}
			return false;
		}
		return true;
	}
	
	
	/**
	 * Opens or focuses the UI of the energy agent.
	 */
	public void openOrFocusUI() {
		if (this.isUiServiceAvailable(true)==true) {
			this.getEnergyAgentUiServices().forEach(service -> service.openOrFocusUI(this.energyAgent));
		}
	} 
	
	
	
}
