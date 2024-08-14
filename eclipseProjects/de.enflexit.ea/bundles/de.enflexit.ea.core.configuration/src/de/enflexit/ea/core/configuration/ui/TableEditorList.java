package de.enflexit.ea.core.configuration.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;

import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;

/**
 * The Class TableEditorList.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TableEditorList extends AbstractCellEditor implements TableCellEditor {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private SetupConfigurationModel setupConfigModel;
	private JComboBox<Object> jComboBox;
	
    /**
     * Instantiates a new table editor list.
     * @param setupConfigModel the current SetupConfigurationModel
     */
    public TableEditorList(SetupConfigurationModel setupConfigModel) {
    	this.setupConfigModel = setupConfigModel;
    }

    /**
     * Returns the JComboBox for the configuration options.
     * @return the j combo box configuration
     */
    private JComboBox<Object> getJComboBoxConfiguration() {
    	if (jComboBox==null) {
    		jComboBox = new JComboBox<>();
    		jComboBox.setBorder(new EmptyBorder(1, 1, 1, 1));
    		jComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
    	}
    	return jComboBox;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    	
    	// --- Get the corresponding SetupConfigurationAttributeService -------
    	SetupConfigurationAttributeService attributeService = this.setupConfigModel.getColumnVector().get(column);

    	// --- Set model and value --------------------------------------------
    	this.getJComboBoxConfiguration().setModel(attributeService.getComboBoxModel());
    	this.getJComboBoxConfiguration().setSelectedIndex(attributeService.getIndexOfSelectedConfigurationOption(value));
    	
		TableCellColorHelper.setTableCellRendererColors(jComboBox, row, isSelected, Color.white);
        return this.getJComboBoxConfiguration();
    }
    
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.getJComboBoxConfiguration().getSelectedItem();
	}
	
}