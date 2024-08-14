package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

import de.enflexit.common.swing.TableCellColorHelper;
import de.enflexit.ea.core.configuration.eom.BundleHelper;
import de.enflexit.ea.core.configuration.eom.systems.EomSystem;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;

/**
 * The Class EomSystemCellRenderer.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSystemCellRenderer implements TableCellRenderer, UIResource {
	
	
	public enum EomSystemInformationType {
		SystemID,
		StorageInfo
	}
	
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	private EomSystemInformationType eomSystemInformationType;

	private JPanel jPanel;
	private JTextField jTextField;
	private JButton jButton;
	
    /**
     * Instantiates a new table renderer boolean.
     */
    public EomSystemCellRenderer(EomSystemInformationType eomSystemInformationType) {
        super();
        this.eomSystemInformationType = eomSystemInformationType;
    }

    private JTextField getJTextField() {
    	if (jTextField==null) {
    		jTextField = new JTextField();
    		jTextField.setBorder(noFocusBorder);
    	}
    	return jTextField;
    }
    private JButton getJButton() {
    	if (jButton==null) {
    		jButton = new JButton();
    		jButton.setIcon(BundleHelper.getImageIcon("MBopen.png"));
    		jButton.setSize(new Dimension(16, 16));
    		jButton.setMaximumSize(new Dimension(16, 16));
    	}
    	return jButton;
    }
    private JPanel getJPanel() {
    	if (jPanel==null) {
    		jPanel = new JPanel();
    		jPanel.setLayout(new BorderLayout());
    		jPanel.add(this.getJButton(), BorderLayout.WEST);
    		jPanel.add(this.getJTextField(), BorderLayout.CENTER);
    	}
    	return jPanel;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
    	// --- Set current value to text field  -----------
    	JComponent displayComponent = null;
    	EomSystem eomSystem = (EomSystem) value;
    	switch(this.eomSystemInformationType) {
    	case SystemID:
    		this.getJTextField().setText(this.getSystemID(eomSystem));
    		displayComponent = this.getJTextField();
    		break;
    	case StorageInfo:
    		this.getJTextField().setText(eomSystem.getStorageInfo());
    		displayComponent = this.getJPanel();
    		break;
    	}
    	
		TableCellColorHelper.setTableCellRendererColors(this.getJTextField(), row, isSelected, Color.white);
		TableCellColorHelper.setTableCellRendererColors(displayComponent, row, isSelected, Color.white);
        return displayComponent;
    }
    
    /**
     * Returns the system ID.
     *
     * @param eomSystem the eom system
     * @return the system ID
     */
    private String getSystemID(EomSystem eomSystem) {
    	
    	if (eomSystem==null || eomSystem.getDataModel()==null) return null;
    	
    	String systemID = null;
    	Object eomModel = eomSystem.getDataModel();
    	if (eomModel instanceof TechnicalSystem) {
    		systemID = ((TechnicalSystem) eomModel).getSystemID();
    	} else if (eomModel instanceof ScheduleList) {
    		systemID = ((ScheduleList) eomModel).getSystemID();
    	} else if (eomModel instanceof TechnicalSystemGroup) {
    		systemID = ((TechnicalSystemGroup) eomModel).getTechnicalSystem().getSystemID();
    	}
    	return systemID;
    }
}