package de.enflexit.ea.core.configuration.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;

/**
 * The Class SetupConfigurationTablePanel.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationTablePanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 269389487225801638L;

	private SetupConfigurationModel confModel;
	
	private JScrollPane jScrollPane;
	private JTable jTableSetupConfiguration;
	
	/**
	 * Instantiates a new setup configuration table panel.
	 * @param configModel the current SetupConfigurationModel
	 */
	public SetupConfigurationTablePanel(SetupConfigurationModel configModel) {
		this.confModel = configModel;
		this.initialize();
	}
	/**
	 * Initializes this panel.
	 */
	private void initialize() {
		
		this.getSetupConfigurationModel().addPropertyChangeListener(this);
		
		this.setLayout(new BorderLayout(0, 0));
		this.add(this.getJScrollPane());
	}
	
	/**
	 * Returns the current SetupConfigurationModel.
	 * @return the setup configuration model
	 */
	private SetupConfigurationModel getSetupConfigurationModel() {
		if (confModel==null) {
			confModel = new SetupConfigurationModel();
		}
		return confModel;
	}
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(this.getJTableSetupConfiguration());
		}
		return jScrollPane;
	}
	private JTable getJTableSetupConfiguration() {
		if (jTableSetupConfiguration == null) {
			jTableSetupConfiguration = new JTable(this.getSetupConfigurationModel().getConfigurationTableModel());
			jTableSetupConfiguration.setFillsViewportHeight(true);
			jTableSetupConfiguration.getTableHeader().setReorderingAllowed(false);
			jTableSetupConfiguration.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			
			// --- Set some default renderer -------------
			// --- => ... for the header
			jTableSetupConfiguration.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(jTableSetupConfiguration, this.getSetupConfigurationModel()));
			// --- => ... for Boolean values 
			jTableSetupConfiguration.setDefaultRenderer(Boolean.class, new TableRendererBoolean());
			
			this.resetTableColumnWidth();
			
		}
		return jTableSetupConfiguration;
	}
	
	/**
	 * Updates the table column width.
	 */
	private void resetTableColumnWidth() {
		
		// --- Set a preferred column width ----------- 
		TableColumnModel tcm = this.getJTableSetupConfiguration().getColumnModel();
		tcm.getColumn(0).setPreferredWidth(250);
		for (int col = 1; col < tcm.getColumnCount(); col++) {
			tcm.getColumn(col).setPreferredWidth(150);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(SetupConfigurationModel.PROPERTY_MODEL_CREATED)) {
			// --- Reset the table model ------------------
			this.getJTableSetupConfiguration().setModel(this.getSetupConfigurationModel().getConfigurationTableModel());
			this.resetTableColumnWidth();
		}
	}
	
	
}
