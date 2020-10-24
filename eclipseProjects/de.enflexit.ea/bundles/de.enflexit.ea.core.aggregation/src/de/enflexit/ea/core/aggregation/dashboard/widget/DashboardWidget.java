package de.enflexit.ea.core.aggregation.dashboard.widget;

import javax.swing.JComponent;

import de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate;

/**
 * Common interface for dashboard widgets
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public interface DashboardWidget {

	/**
	 * Gets the id.
	 * @return the id
	 */
	public String getID();
	/**
	 * Process update.
	 * @param update the update
	 */
	public void processUpdate(DashboardWidgetUpdate update);
	
	public JComponent getWidgetComponent();
	
}
