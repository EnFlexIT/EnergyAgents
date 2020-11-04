package de.enflexit.ea.core.dashboard.widget;

import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A simple widget for numeric values, consisting of a JTextField for the value and a JLabel for the unit (optional).
 * Can be used for any kind of numeric value, i.e. any subclass of jaba.lang.Number (or the corresponding primitive data types) 
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 *
 */
public class SimpleTextFieldWidget extends JPanel implements DashboardWidget {
	
	private static final long serialVersionUID = 7354301824279022068L;
	
	private JTextField jTextfieldValue;
	private JLabel jLabelUnit;
	
	private String id; 
	private String unit;
	
	private int numberOfDecimals = 1;
	private int defaultTextFieldWidth = 5;
	
	/**
	 * Instantiates a new simple text field widget.
	 * @param widgetID the widget ID
	 * @param unit the unit
	 */
	public SimpleTextFieldWidget(String widgetID, String unit) {
		this.id = widgetID;
		this.unit = unit;
		this.initialize();
	}
	/**
	 * Initialize the GUI components.
	 */
	private void initialize() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
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
	
	/**
	 * Sets the number of decimals.
	 * @param numberOfDecimals the new number of decimals
	 */
	public void setNumberOfDecimals(int numberOfDecimals) {
		this.numberOfDecimals = numberOfDecimals;
	}
	
	/**
	 * Gets the number of decimals.
	 * @return the number of decimals
	 */
	public int getNumberOfDecimals() {
		return numberOfDecimals;
	}
	
	/**
	 * Sets the text field width.
	 * @param textFieldWidth the new text field width
	 */
	public void setTextFieldWidth(int textFieldWidth) {
		this.getJTextfieldValue().setColumns(textFieldWidth);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#processUpdate(de.enflexit.ea.core.aggregation.dashboard.DashboardUpdate)
	 */
	@Override
	public void processUpdate(DashboardWidgetUpdate update) {
		if (update.getID().equals(this.getID())) {
			if (update.getValue() instanceof Number) {
				double roundedValue = this.roundValue(((Number)update.getValue()).doubleValue(), this.numberOfDecimals);
				this.setValue(roundedValue);
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] Wrong data type in update object: " + update.getValue().getClass().getName());
			}
		}
	}
	
	/**
	 * Gets the JTextField to visualize the widget's value.
	 * @return the JTextField
	 */
	protected JTextField getJTextfieldValue() {
		if (jTextfieldValue==null) {
			jTextfieldValue = new JTextField();
			jTextfieldValue.setColumns(defaultTextFieldWidth);
			jTextfieldValue.setText("-");
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
			jLabelUnit.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelUnit;
	}
	
	/**
	 * Sets the value for this widget
	 * @param value the new value
	 */
	public void setValue(double value) {
		this.getJTextfieldValue().setText(""+value);
	}
	
	/**
	 * Round a value according to the numberOfDecimals property.
	 * @param value the value
	 * @return the rounded double
	 */
	private double roundValue(double value, int numberOfDecimals) {
		double scale = Math.pow(10, numberOfDecimals);
		
		return Math.round(value*scale)/scale;
	}
	
	/**
	 * Sets the unit for this widget
	 * @param unit the new unit
	 */
	public void setUnit(String unit) {
		this.getUnitLabel().setText(unit);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#getWidgetComponent()
	 */
	@Override
	public JComponent getWidgetComponent() {
		return this;
	}
}
