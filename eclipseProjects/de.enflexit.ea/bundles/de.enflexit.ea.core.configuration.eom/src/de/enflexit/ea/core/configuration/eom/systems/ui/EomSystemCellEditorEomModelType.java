package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;

import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.eom.systems.EomSystem;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;;

/**
 * The Class EomSystemCellRendererEomModelType.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSystemCellEditorEomModelType extends AbstractCellEditor implements TableCellEditor {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	private JTable table;
	private EomSystem eomSystem;
	private DefaultComboBoxModel<EomModelType> comboBoxModel;
	
    /**
     * Instantiates a new table renderer eom model type.
     */
    public EomSystemCellEditorEomModelType() {
        super();
    }

    /**
     * Returns the combo box model for the current editor.
     * @return the combo box model
     */
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
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

    	this.table = table;
    	this.eomSystem = (EomSystem) value;
    	
    	JComboBox<EomModelType> cbEomSystem = new JComboBox<>(this.getComboBoxModel());
    	cbEomSystem.setModel(this.getComboBoxModel());
    	cbEomSystem.setBorder(noFocusBorder);
    	cbEomSystem.setSelectedItem(this.eomSystem.getEomModelType());
    	cbEomSystem.addActionListener(new ActionListener() {
    		
			@Override
			public void actionPerformed(ActionEvent ae) {
				
				JComboBox<?> cbEdit = (JComboBox<?>) ae.getSource();
				EomModelType newEomModelType = (EomModelType) cbEdit.getSelectedItem();
				if (newEomModelType!=EomSystemCellEditorEomModelType.this.eomSystem.getEomModelType()) {
					// --- Set model to null ------------------------
					eomSystem.setDataModel(null);
					eomSystem.clearStorageInfo();
					// --- Save new system type --------------------- 
					eomSystem.setEomModelType(newEomModelType);
					// --- Check if StorageLocation is OK ----------- 
					EomStorageLocation allowedStorageLocation = EomSystemCellEditorEomModelType.this.getAllowedStorageLocation();
					if (allowedStorageLocation!=eomSystem.getStorageLocation()) {
						eomSystem.setStorageLocation(allowedStorageLocation);
					}
					EomSystemCellEditorEomModelType.this.table.repaint();
				}
				fireEditingStopped();
			}
		});
    	
		TableCellColorHelper.setTableCellRendererColors(cbEomSystem, row, isSelected, Color.white);
        return cbEomSystem;
    }
    
    /**
     * Returns the allowed storage location.
     * @return the allowed storage location
     */
    private EomStorageLocation getAllowedStorageLocation() {
    	List<EomStorageLocation> allowedStroageLocations = EomSystemCellEditorEomStorageLocation.getAllowedStorageLocations(EomSystemCellEditorEomModelType.this.eomSystem.getEomModelType());
    	if (allowedStroageLocations.contains(this.eomSystem.getStorageLocation())==false) {
    		return allowedStroageLocations.get(0);
    	}
    	return this.eomSystem.getStorageLocation();
    }
    
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.eomSystem;
	}
	
}