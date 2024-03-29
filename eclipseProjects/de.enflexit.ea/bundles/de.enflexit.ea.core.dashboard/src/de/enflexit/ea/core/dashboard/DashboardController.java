package de.enflexit.ea.core.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.enflexit.ea.core.dashboard.widget.DashboardWidget;
import de.enflexit.ea.core.dashboard.widget.DashboardWidgetUpdate;


/**
 * Abstract superclass for dashboard controller implementations
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public abstract class DashboardController {
	private HashMap<String, DashboardWidget> widgets;
	private Vector<DashboardWidgetUpdate> pendingUpdates;
	
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
		this.getWidgets().put(widget.getID(), widget);
	}
	
	/**
	 * Register multiple widgets.
	 * @param widgets the widgets
	 */
	public void registerWidgets(List<DashboardWidget> widgets) {
		for (DashboardWidget widget : widgets) {
			this.registerWidget(widget);
		}
	}
	
	/**
	 * Gets the pending updates.
	 * @return the pending updates
	 */
	public synchronized Vector<DashboardWidgetUpdate> getPendingUpdates() {
		if (pendingUpdates==null) {
			pendingUpdates = new Vector<DashboardWidgetUpdate>();
		}
		return pendingUpdates;
	}
	
	/**
	 * Applies the pending updates, clears the lists afterwards.
	 */
	public void applyPendingUpdates() {
		SwingUtilities.invokeLater(new Runnable() {
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				for (int i=0; i<DashboardController.this.getPendingUpdates().size(); i++) {
					DashboardWidgetUpdate update = DashboardController.this.getPendingUpdates().get(i);
					DashboardWidget widget = DashboardController.this.getWidgets().get(update.getID());
					if (widget!=null) {
						widget.processUpdate(update);
					} else {
						System.err.println("[" + DashboardController.this.getSimpleClassName() + "] No dashboard widget found for ID " + update.getID());
					}
				}
				DashboardController.this.getPendingUpdates().clear();
			}
		});
	}
	
	/**
	 * Gets the simple class name. Helper method to be used in the runnable above
	 * @return the simple class name
	 */
	private String getSimpleClassName() {
		return this.getClass().getSimpleName();
	}
	/**
	 * Gets the dashboard panel.
	 * @return the dashboard panel
	 */
	public abstract JPanel getDashboardPanel();
	
	/**
	 * Gets the dashboard subscriptions.
	 * @return the dashboard subscriptions
	 */
	public abstract List<DashboardSubscription> getDashboardSubscriptions();
	
	/**
	 * Processes a new dashoard update.
	 * @param dashboardUpdate the dashboard update
	 */
	public abstract void processDashoardUpdate(DashboardUpdate dashboardUpdate);
	
	/**
	 * Gets the domain.
	 * @return the domain
	 */
	public abstract String getDomain();
}
