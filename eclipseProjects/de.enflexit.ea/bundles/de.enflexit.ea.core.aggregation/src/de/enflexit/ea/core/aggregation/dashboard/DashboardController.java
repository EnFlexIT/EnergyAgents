package de.enflexit.ea.core.aggregation.dashboard;

import java.util.HashMap;

import de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget;

public class DashboardController {
	private HashMap<String, DashboardWidget> widgets;
	
	public HashMap<String, DashboardWidget> getWidgets() {
		if (widgets==null) {
			widgets = new HashMap<String, DashboardWidget>();
		}
		return widgets;
	}
	
	public void registerWidget(DashboardWidget widget) {
		
	}
}
