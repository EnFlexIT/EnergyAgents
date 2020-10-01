package de.enflexit.ea.core.aggregation.dashboard.widget;

import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.enflexit.ea.core.aggregation.dashboard.DashboardController;
import de.enflexit.ea.core.aggregation.dashboard.DashboardUpdate;

public class MultipleTextFieldWidget extends JPanel implements DashboardWidget {

	private static final long serialVersionUID = 8257914709405488023L;
	
	private String id;
	private String label;

	private ArrayList<String> subWidgetIDs;
	private ArrayList<String> subWidgetUnits;

	private JLabel widgetLabel;
	private HashMap<String, JTextField> subWidgetTextfields;
	private HashMap<String, JLabel> subWidgetUnitLabels;
	

	/**
	 * Instantiates a new multiple text field widget, containing one text field for every entry in the
	 * subWidgetIDs list. Please make sure to specify a unit for every sub-widget, i.e. both lists have 
	 * the same length. The unit list may contain null entries if a sub-widget needs no unit.  
	 * @param id the ID
	 * @param label the label
	 * @param subWidgetIDs the sub widget IDs
	 * @param subWidgetUnits the sub widget units 
	 */
	public MultipleTextFieldWidget(String id, String label, List<String> subWidgetIDs, List<String> subWidgetUnits) {
		this.id = id;
		this.label = label;
		this.subWidgetIDs = new ArrayList<String>(subWidgetIDs);
		this.subWidgetUnits = new ArrayList<String>(subWidgetUnits);
		if (subWidgetIDs.size()!=subWidgetUnits.size()) {
			throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid arguments: Same number of IDs and units required for sub-widgets!");
		}
		this.initialize();
	}
	
	/**
	 * Initialize the GUI components.
	 */
	private void initialize() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(this.getWidgetLabel());
		
		for (int i=0; i<subWidgetIDs.size(); i++) {
			
			String subWidgetID = this.id + "." + this.subWidgetIDs.get(i);
			String subWidgetUnit = this.subWidgetUnits.get(i);
			
			JTextField subWidgetTextField = this.createNewSubWidgetValueTextField();
			this.add(subWidgetTextField);
			this.getSubWidgetTextfields().put(subWidgetID, subWidgetTextField);
			
			JLabel subWidgetUnitLabel = this.createNewSubWidgetUnitLabel(subWidgetUnit);
			this.add(subWidgetUnitLabel);
			this.getSubWidgetUnitLabels().put(subWidgetID, subWidgetUnitLabel);
			
			
		}
	}
	
	/**
	 * Gets the widget label.
	 * @return the widget label
	 */
	private JLabel getWidgetLabel() {
		if (widgetLabel==null) {
			widgetLabel = new JLabel(this.label);
			widgetLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return widgetLabel;
	}

	/**
	 * Gets the sub widget textfields.
	 * @return the sub widget textfields
	 */
	private HashMap<String, JTextField> getSubWidgetTextfields() {
		if (subWidgetTextfields==null) {
			subWidgetTextfields = new HashMap<String, JTextField>();
		}
		return subWidgetTextfields;
	}

	/**
	 * Gets the sub widget unit labels.
	 * @return the sub widget unit labels
	 */
	private HashMap<String, JLabel> getSubWidgetUnitLabels() {
		if (subWidgetUnitLabels==null) {
			subWidgetUnitLabels = new HashMap<String, JLabel>();
		}
		return subWidgetUnitLabels;
	}

	/**
	 * Creates a new sub widget value text field.
	 * @return the j text field
	 */
	private JTextField createNewSubWidgetValueTextField() {
		JTextField subWidgetValueTextField = new JTextField();
		//TODO further customizations, or remove this method
		return subWidgetValueTextField;
	}
	
	/**
	 * Creates a new sub widget unit label.
	 * @param unit the unit
	 * @return the j label
	 */
	private JLabel createNewSubWidgetUnitLabel(String unit) {
		JLabel subWidgetUnitLabel = new JLabel(unit);
		subWidgetUnitLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		return subWidgetUnitLabel;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#getID()
	 */
	@Override
	public String getID() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#setID(java.lang.String)
	 */
	@Override
	public void setID(String id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#processUpdate(de.enflexit.ea.core.aggregation.dashboard.DashboardUpdate)
	 */
	@Override
	public void processUpdate(DashboardUpdate update) {
		JTextField subWidget = this.getSubWidgetTextfields().get(update.getID());
		if (subWidget!=null) {
			subWidget.setText("" + update.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#register(de.enflexit.ea.core.aggregation.dashboard.DashboardController)
	 */
	@Override
	public void register(DashboardController controller) {
		List<String> subWidgetIDs = new ArrayList<String>(this.getSubWidgetTextfields().keySet());
		for (int i=0; i<subWidgetIDs.size(); i++) {
			controller.getWidgets().put(subWidgetIDs.get(i), this);
		}
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.dashboard.widget.DashboardWidget#unregister(de.enflexit.ea.core.aggregation.dashboard.DashboardController)
	 */
	@Override
	public void unregister(DashboardController controller) {
		List<String> subWidgetIDs = new ArrayList<String>(this.getSubWidgetTextfields().keySet());
		for (int i=0; i<subWidgetIDs.size(); i++) {
			controller.getWidgets().remove(subWidgetIDs.get(i));
		}
	}

}
