package de.enflexit.ea.core.configuration.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

import de.enflexit.common.swing.TableCellColorHelper;

/**
 * The Class TableRendererNoEdit.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TableRendererNoEdit extends JLabel implements TableCellRenderer, UIResource {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    /**
     * Instantiates a new table renderer for cells, where noc value can be configured.
     */
    public TableRendererNoEdit() {
        super();
        this.setFont(new Font("Dialog", Font.PLAIN, 12));
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
    	int modelColumn = table.convertColumnIndexToModel(column);
    	Class<?> columnClass = table.getModel().getColumnClass(modelColumn);
    	if (columnClass==Boolean.class) {
    		this.setHorizontalAlignment(JLabel.CENTER);
    	} else {
    		this.setHorizontalAlignment(JLabel.RIGHT);
    	}
    	
    	if (isSelected) {
			this.setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			this.setForeground(table.getForeground());
			this.setBackground(table.getBackground());
		}
		this.setText("   -   ");

		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			setBorder(noFocusBorder);
		}
		
		TableCellColorHelper.setTableCellRendererColors(this, row, isSelected, Color.white);
		
        return this;
    }
}