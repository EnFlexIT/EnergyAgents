package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.eom.systems.EomSystem;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;;

/**
 * The Class EomSystemCellRendererEomModelType.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSystemCellRendererEomModelType extends JComboBox<EomModelType> implements TableCellRenderer, UIResource {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	private DefaultComboBoxModel<EomModelType> comboBoxModel;
	
    /**
     * Instantiates a new table renderer eom model type.
     */
    public EomSystemCellRendererEomModelType() {
        super();
        this.setModel(this.getComboBoxModel());
        this.setBorder(noFocusBorder);
    }

    private DefaultComboBoxModel<EomModelType> getComboBoxModel() {
    	if (comboBoxModel==null) {
    		comboBoxModel = new DefaultComboBoxModel<>();
    		for (EomModelType eomModelType : EomModelType.values()) {
    			comboBoxModel.addElement(eomModelType);
    		}
    	}
    	return comboBoxModel;
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
    	
    	// --- Set current value to ComboBox --------------
    	EomSystem eomSystem = (EomSystem) value;
    	this.setSelectedItem(eomSystem.getEomModelType());
    	
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			setBorder(noFocusBorder);
		}
		
		TableCellColorHelper.setTableCellRendererColors(this, row, isSelected, Color.white);
		
        return this;
    }
}