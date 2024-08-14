package de.enflexit.ea.ui;

import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;

/**
 * The Interface EnergyAgentWindowInterface.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface EnergyAgentWindowInterface {

	/**
	 * Enables to fire PropertyChangeEvent to the energy agents UI window
	 * @param propertyEvent the property event
	 */
	public void firePropertyEvent(PropertyEvent propertyEvent);
	
}
