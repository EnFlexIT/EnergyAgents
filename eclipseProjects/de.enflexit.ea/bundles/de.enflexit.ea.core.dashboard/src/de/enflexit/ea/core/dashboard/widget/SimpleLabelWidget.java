package de.enflexit.ea.core.dashboard.widget;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Simple {@link JLabel}-based widget for displaying textual information
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 *
 */
public class SimpleLabelWidget extends JLabel implements DashboardWidget{
	
	private static final long serialVersionUID = -836231361720646146L;
	private String id;

	/**
	 * Instantiates a new simple label widget.
	 * @param widgetID the widget ID
	 */
	public SimpleLabelWidget(String widgetID) {
		super();
		this.id = widgetID;
		this.setFont(new Font("Dialog", Font.PLAIN, 12));
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#getID()
	 */
	@Override
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#processUpdate(de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate)
	 */
	@Override
	public void processUpdate(DashboardWidgetUpdate update) {
		String newText = update.getValue().toString();
		this.setText(newText);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#getWidgetComponent()
	 */
	@Override
	public JComponent getWidgetComponent() {
		return this;
	}

}
