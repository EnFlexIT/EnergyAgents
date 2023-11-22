package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;

/**
 * The Class EomSystemCellRendererEomStorageLocation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSystemCellEditorEomStorageLocation extends AbstractCellEditor implements TableCellEditor {
	
	private static final long serialVersionUID = -8965685551115502359L;
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	private JTable table;
	private EomSystem eomSystem;
	
    /**
     * Instantiates a new table renderer EomStorageLocation.
     */
    public EomSystemCellEditorEomStorageLocation() {
        super();
    }

    /**
     * Returns the allowed storage locations for the specified {@link EomModelType}.
     *
     * @param eomModelType the eom model type
     * @return the allowed storage locations
     */
    public static List<EomStorageLocation> getAllowedStorageLocations(EomModelType eomModelType) {
    	
    	List<EomStorageLocation> allowedStorageLocations = new ArrayList<>();
    	switch (eomModelType) {
		case TechnicalSystem:
			//allowedStorageLocations.add(EomStorageLocation.NetworkElementBase64);
			allowedStorageLocations.add(EomStorageLocation.File);
			allowedStorageLocations.add(EomStorageLocation.BundleLocation);
			//allowedStorageLocations.add(EomStorageLocation.Customized);
			break;
		case ScheduleList:
			//allowedStorageLocations.addElement(EomStorageLocation.NetworkElementBase64);
			allowedStorageLocations.add(EomStorageLocation.File);
			allowedStorageLocations.add(EomStorageLocation.Database);
			//allowedStorageLocations.add(EomStorageLocation.Customized);
			break;
		case TechnicalSystemGroup:
			//allowedStorageLocations.add(EomStorageLocation.NetworkElementBase64);
			allowedStorageLocations.add(EomStorageLocation.File);
			//allowedStorageLocations.add(EomStorageLocation.Customized);
			break;
		}
    	return allowedStorageLocations;
    }
    
    /**
     * Gets the combo box model.
     *
     * @param eomSystem the eom system
     * @return the combo box model
     */
    public DefaultComboBoxModel<String> getComboBoxModel() {
    	
    	DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
		for (EomStorageLocation eomStorageLocation : EomSystemCellEditorEomStorageLocation.getAllowedStorageLocations(this.eomSystem.getEomModelType())) {
			comboBoxModel.addElement(eomStorageLocation.getDescription());
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

    	JComboBox<String> cbEomSystem = new JComboBox<>(this.getComboBoxModel());
    	cbEomSystem.setBorder(noFocusBorder);
    	cbEomSystem.setSelectedItem(this.eomSystem.getStorageLocation().getDescription());
    	cbEomSystem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JComboBox<?> cbEdit = (JComboBox<?>) ae.getSource();
				EomStorageLocation newStorageLocation = EomStorageLocation.valueOfDescription((String) cbEdit.getSelectedItem());
				if (newStorageLocation!=null && newStorageLocation!=eomSystem.getStorageLocation()) {
					// --- Set model to null ------------------------
					eomSystem.setDataModel(null);
					eomSystem.clearStorageInfo();
					// --- Set new storage location ----------------- 
					eomSystem.setStorageLocation(newStorageLocation);
					EomSystemCellEditorEomStorageLocation.this.table.repaint();
				}
				fireEditingStopped();
			}
		});
    	
		TableCellColorHelper.setTableCellRendererColors(cbEomSystem, row, isSelected, Color.white);
        return cbEomSystem;
    }
    
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return this.eomSystem;
	}
    
}