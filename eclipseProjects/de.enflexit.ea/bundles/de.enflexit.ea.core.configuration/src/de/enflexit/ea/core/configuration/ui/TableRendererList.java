package de.enflexit.ea.core.configuration.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;

/**
 * The Class TableRendererList.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TableRendererList extends JComboBox<Object> implements TableCellRenderer, UIResource {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	private SetupConfigurationModel setupConfigModel;
	
    /**
     * Instantiates a new table renderer boolean.
     */
    public TableRendererList(SetupConfigurationModel setupConfigModel) {
        super();
        this.setupConfigModel = setupConfigModel;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
    	if (isSelected) {
			setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		
    	// --- Get the corresponding SetupConfigurationAttributeService ------- 
    	SetupConfigurationAttributeService attributeService = this.setupConfigModel.getColumnVector().get(column);
    	
    	// --- Set model and value --------------------------------------------
    	this.setModel(attributeService.getComboBoxModel());
    	this.setSelectedIndex(attributeService.getIndexOfSelectedConfigurationOption(value));

		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			setBorder(noFocusBorder);
		}
		
		TableCellColorHelper.setTableCellRendererColors(this, row, isSelected, Color.white);
        return this;
    }
    
}