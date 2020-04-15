package de.enflexit.ea.deployment.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import agentgui.core.application.Language;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.deployment.DeploymentSettings;

/**
 * A {@link JComboBox}-based {@link TableCellEditor} for selecting an {@link AgentOperatingMode}
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class AgentOperatingModeTableCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener{

	private static final long serialVersionUID = 1362604414245525340L;
	private JComboBox<AgentOperatingMode> editorComponent;

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.getEditorComponent().getSelectedItem();
	}

	/**
	 * Gets the editor component.
	 *
	 * @return the editor component
	 */
	private JComboBox<AgentOperatingMode> getEditorComponent() {
		if (editorComponent==null) {
			editorComponent = new JComboBox<AgentOperatingMode>();
			
			// --- Initialize the combo box content ----------------
			DefaultComboBoxModel<AgentOperatingMode> comboBoxModel = new DefaultComboBoxModel<>();
			comboBoxModel.addElement(AgentOperatingMode.TestBedSimulation);
			comboBoxModel.addElement(AgentOperatingMode.TestBedReal);
			comboBoxModel.addElement(AgentOperatingMode.RealSystem);
			comboBoxModel.addElement(AgentOperatingMode.RealSystemSimulatedIO);
			editorComponent.setModel(comboBoxModel);
			editorComponent.setToolTipText(Language.translate("The testbed agent's operating mode", Language.EN));
			editorComponent.addActionListener(this);
		}
		return editorComponent;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (editorComponent==null) {
			editorComponent = this.getEditorComponent();	
		}
		AgentOperatingMode operatingMode = (AgentOperatingMode) value;
		if(operatingMode == null) {
			operatingMode = DeploymentSettings.DEFAULT_OPERATING_MODE;
		}
		editorComponent.setSelectedItem(operatingMode);
		return editorComponent;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == editorComponent) {
			fireEditingStopped();
		}
	}

}
