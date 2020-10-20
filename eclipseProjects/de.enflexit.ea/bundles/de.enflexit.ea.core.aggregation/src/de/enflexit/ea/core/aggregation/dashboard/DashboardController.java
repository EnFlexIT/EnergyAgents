package de.enflexit.ea.core.aggregation.dashboard;

import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget;

/**
 * The Class DashboardController.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public abstract class DashboardController {
	private HashMap<String, DashboardWidget> widgets;
	
	/**
	 * Gets the widgets HashMap, creates it if necessary.
	 * @return the widgets
	 */
	public HashMap<String, DashboardWidget> getWidgets() {
		if (widgets==null) {
			widgets = new HashMap<String, DashboardWidget>();
		}
		return widgets;
	}
	
	/**
	 * Registers a widget.
	 * @param widget the widget
	 */
	public void registerWidget(DashboardWidget widget) {
		widget.register(this);
	}
	
	/**
	 * Gets the dashboard panel.
	 * @return the dashboard panel
	 */
	public abstract JPanel getDashboardPanel();
	
	/**
	 * Gets the dashboard requests.
	 * @return the dashboard requests
	 */
	public abstract List<AbstractDashboardSubscription> getDashboardRequests();
	
	public abstract String getDomain();
}
