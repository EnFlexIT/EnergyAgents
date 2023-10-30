package de.enflexit.ea.core.configuration.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.model.DescriptionColumn;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;

/**
 * The Class TableHeaderRenderer.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TableHeaderRenderer implements TableCellRenderer {

	private DefaultTableCellRenderer renderer;
	private SetupConfigurationModel confModel;

    /**
     * Instantiates a new table header renderer.
     *
     * @param table the table
     * @param confModel the current {@link SetupConfigurationModel}
     */
    public TableHeaderRenderer(JTable table, SetupConfigurationModel confModel) {
        this.renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        this.confModel = confModel;
        //renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        
    	// --- Check to shorten the description ----------- 
    	String description = (String)value;
    	int hypenPos = description.indexOf("-"); 
    	if (hypenPos>-1) {
    		description = description.substring(hypenPos + 1).trim();
    	}
    	
    	// --- Prepare to set Tool tip text ---------------
    	JLabel label = (JLabel) renderer.getTableCellRendererComponent(table, description, isSelected, hasFocus, row, col);
    	if (this.confModel!=null && this.confModel.getColumnVector().size()>0) {
    		SetupConfigurationAttributeService attributeService = this.confModel.getColumnVector().get(col);
    		if (! (attributeService instanceof DescriptionColumn)) {
    			String serviceName = attributeService.getServiceName();
    			String toolTip = serviceName + ": " +  attributeService.getSetupConfigurationAttribute().getDescription();
    			label.setToolTipText(toolTip);
    		} else {
    			label.setToolTipText(null);
    		}
    	}
    	return label;
    }

}
