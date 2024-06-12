package de.enflexit.ea.ui;

import java.beans.PropertyChangeListener;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;


/**
 * The Interface SwingUiModelInterface.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public interface SwingUiModelInterface {

	/**
	 * Has to return the current energy agent.
	 * @return the energy agent
	 */
	public AbstractEnergyAgent getEnergyAgent();
	
	/**
	 * Adds the property change listener.
	 * @param listener the listener
	 */
	public void addPropertyListener(PropertyChangeListener listener);

	/**
	 * Removes the property change listener.
	 * @param listener the listener
	 */
	public void removePropertyListener(PropertyChangeListener listener);

	/**
	 * Fire property event.
	 * @param event the event
	 */
	public void firePropertyEvent(PropertyEvent event);

	/**
	 * Enables to collect data from the UI.
	 *
	 * @param dataType the data type
	 * @return the data
	 */
	public Object collectUiData(UiDataCollection dataType);
	
}