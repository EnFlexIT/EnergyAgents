package de.enflexit.ea.core.configuration.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTable;

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
			
			// --- Set some default renderer -------------
			// --- => for the header
			jTableSetupConfiguration.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(jTableSetupConfiguration, this.getSetupConfigurationModel()));
			// --- => for Boolean values 
			jTableSetupConfiguration.setDefaultRenderer(Boolean.class, new TableRendererBoolean());
			
		}
		return jTableSetupConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(SetupConfigurationModel.PROPERTY_MODEL_CREATED)) {
			// --- Reset the table model ------------------
			this.getJTableSetupConfiguration().setModel(this.getSetupConfigurationModel().getConfigurationTableModel());
		}
	}
	
	
}
