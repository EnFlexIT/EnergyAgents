package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;

import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.eom.systems.EomSystem;
import de.enflexit.ea.core.configuration.eom.systems.SystemConfiguration;;

/**
 * The Class TableEditorList.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSystemCellEditorID extends AbstractCellEditor implements TableCellEditor {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private SystemConfigurationPanel systemConfigurationPanel;
	private SystemConfiguration systemConfiguration;
	
	private JTextField tfEomSystem;
	
	private EomSystem eomSystem;
	private String oldID;
	private String newID;
	
	
    /**
     * Instantiates a new table renderer eom model type.
     *
     * @param systemConfigurationPanel the system configuration panel
     * @param systemConfiguration the system configuration
     */
    public EomSystemCellEditorID(SystemConfigurationPanel systemConfigurationPanel, SystemConfiguration systemConfiguration) {
        this.systemConfigurationPanel = systemConfigurationPanel;
    	this.systemConfiguration = systemConfiguration;
        
        this.addCellEditorListener(new CellEditorListener() {
			/* (non-Javadoc)
			 * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
			 */
			@Override
			public void editingStopped(ChangeEvent e) {
				if (EomSystemCellEditorID.this.newID!=null && EomSystemCellEditorID.this.newID.equals(EomSystemCellEditorID.this.oldID)==false && systemConfiguration.getEomSystem(EomSystemCellEditorID.this.newID)==null) {
					//TableEditorList.this.eomSystem.setId(TableEditorList.this.newID);
					EomSystemCellEditorID.this.systemConfiguration.renameEomSystem(EomSystemCellEditorID.this.oldID, EomSystemCellEditorID.this.newID);
					EomSystemCellEditorID.this.systemConfiguration.save();
					EomSystemCellEditorID.this.systemConfigurationPanel.reFillTableSystemBlueprints();
				}
			}
			/* (non-Javadoc)
			 * @see javax.swing.event.CellEditorListener#editingCanceled(javax.swing.event.ChangeEvent)
			 */
			@Override
			public void editingCanceled(ChangeEvent e) {
				EomSystemCellEditorID.this.eomSystem.setId(EomSystemCellEditorID.this.oldID);
			}
		});
    }

    /**
     * Returns the JTextField to edit the ID.
     * @return the j text field
     */
    private JTextField getJTextFieldID() {
    	if (tfEomSystem==null) {
    		tfEomSystem = new JTextField();
    		tfEomSystem.setBorder(new EmptyBorder(1, 1, 1, 1));
    		
    		tfEomSystem.getDocument().addDocumentListener(new DocumentListener() {
    			@Override
    			public void removeUpdate(DocumentEvent de) {
    				this.remindNewValue(de);
    			}
    			@Override
    			public void insertUpdate(DocumentEvent de) {
    				this.remindNewValue(de);
    			}
    			@Override
    			public void changedUpdate(DocumentEvent de) {
    				this.remindNewValue(de);
    			}
    			private void remindNewValue(DocumentEvent de) {
    				String newID = null;
    				try {
    					newID = de.getDocument().getText(0, de.getDocument().getLength());
    				} catch (BadLocationException blEx) {
    					blEx.printStackTrace();
    				}
    				if (newID!=null && newID.equals(oldID)==false && systemConfiguration.getEomSystem(newID)==null) {
    					EomSystemCellEditorID.this.newID = newID;
    					EomSystemCellEditorID.this.getJTextFieldID().setForeground(Color.BLACK);
    				} else {
    					EomSystemCellEditorID.this.newID = null;
    					EomSystemCellEditorID.this.getJTextFieldID().setForeground(Color.RED);
    				}
    			}
    		});
        
    	}
    	return tfEomSystem;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

    	this.eomSystem = (EomSystem) value;
    	this.oldID = this.eomSystem.getId();
    	this.getJTextFieldID().setText(this.oldID);
    		
		TableCellColorHelper.setTableCellRendererColors(tfEomSystem, row, isSelected, Color.white);
        return this.getJTextFieldID();
    }
    
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.eomSystem;
	}
	
}