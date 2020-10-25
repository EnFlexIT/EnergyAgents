package de.enflexit.ea.core.aggregation.dashboard.widget;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;

import de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate;

/**
 * Simple {@link JLabel}-based widget implementation for displaying textual information
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 *
 */
public class SimpleLabelWidget extends JLabel implements DashboardWidget{
	
	private static final long serialVersionUID = -836231361720646146L;
	private String id;

	/**
	 * Instantiates a new simple label widget.
	 * @param id the id
	 */
	public SimpleLabelWidget(String id) {
		super();
		this.id = id;
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
