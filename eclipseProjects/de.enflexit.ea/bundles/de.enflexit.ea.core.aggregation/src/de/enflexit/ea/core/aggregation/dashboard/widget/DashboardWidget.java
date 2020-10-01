package de.enflexit.ea.core.aggregation.dashboard.widget;

import de.enflexit.ea.core.aggregation.dashboard.DashboardController;
import de.enflexit.ea.core.aggregation.dashboard.DashboardUpdate;

/**
 * Abstract superclass for dashboard widgets
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public interface DashboardWidget {

	/**
	 * Gets the id.
	 * @return the id
	 */
	public String getID();
	/**
	 * Sets the id.
	 * @param id the new id
	 */
	public void setID(String id);
	/**
	 * Process update.
	 * @param update the update
	 */
	public abstract void processUpdate(DashboardUpdate update);
	
	/**
	 * Register this widget at the specified dashboard controller.
	 * @param controller the controller
	 */
	public void register(DashboardController controller);
	
	/**
	 * Unregister this widget from the specified dashboard controller.
	 * @param controller the controller
	 */
	public void unregister(DashboardController controller);
}
