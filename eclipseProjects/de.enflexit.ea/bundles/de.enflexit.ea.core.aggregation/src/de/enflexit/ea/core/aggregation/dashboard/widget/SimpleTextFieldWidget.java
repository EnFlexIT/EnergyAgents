package de.enflexit.ea.core.aggregation.dashboard.widget;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.enflexit.ea.core.aggregation.dashboard.DashboardController;
import de.enflexit.ea.core.aggregation.dashboard.DashboardWidgetUpdate;

public class SimpleTextFieldWidget extends JPanel implements DashboardWidget {
	
	private static final long serialVersionUID = 7354301824279022068L;
	
	private JLabel jLabelLabel;
	private JTextField jTextfieldValue;
	private JLabel jLabelUnit;
	
	private String id; 
	private String label;
	private String unit;

	
	public SimpleTextFieldWidget(String id, String label, String unit) {
		this.id = id;
		this.label = label;
		this.unit = unit;
		this.initialize();
	}
	/**
	 * Initialize the GUI components.
	 */
	private void initialize() {
		this.setLayout(new FlowLayout());
		this.add(this.getJLabelLabel());
		this.add(this.getJTextfieldValue());
		this.add(this.getUnitLabel());
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#getId()
	 */
	@Override
	public String getID() {
		return this.id;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#setId(java.lang.String)
	 */
	@Override
	public void setID(String id) {
		this.id = id;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#processUpdate(de.enflexit.ea.core.aggregation.dashboard.DashboardUpdate)
	 */
	@Override
	public void processUpdate(DashboardWidgetUpdate update) {
		if (update.getID().equals(this.getID())) {
			this.setValue(update.getValue());
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#register(de.enflexit.ea.core.aggregation.dashboard.DashboardController)
	 */
	@Override
	public void register(DashboardController controller) {
		controller.getWidgets().put(this.getID(), this);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#unregister(de.enflexit.ea.core.aggregation.dashboard.DashboardController)
	 */
	@Override
	public void unregister(DashboardController controller) {
		controller.getWidgets().remove(this.getID());
	}
	
	/**
	 * Gets the JLabel to visualize the widget's label
	 * @return the JLabel
	 */
	private JLabel getJLabelLabel() {
		if (jLabelLabel==null) {
			jLabelLabel = new JLabel(this.label);
		}
		return jLabelLabel;
	}
	/**
	 * Gets the JTextField to visualize the widget's value.
	 * @return the JTextField
	 */
	private JTextField getJTextfieldValue() {
		if (jTextfieldValue==null) {
			jTextfieldValue = new JTextField("6.66");
		}
		return jTextfieldValue;
	}
	/**
	 * Gets the JLabel to visualize the widget's unit
	 * @return the JLabel
	 */
	private JLabel getUnitLabel() {
		if (jLabelUnit==null) {
			jLabelUnit = new JLabel(this.unit);
		}
		return jLabelUnit;
	}
	
	/**
	 * Sets the label for this widget.
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.getJLabelLabel().setText(label);
	}
	/**
	 * Sets the value for this widget
	 * @param value the new value
	 */
	public void setValue(double value) {
		this.getJTextfieldValue().setText(""+value);
	}
	/**
	 * Sets the unit for this widget
	 * @param unit the new unit
	 */
	public void setUnit(String unit) {
		this.getUnitLabel().setText(unit);
	}
}
