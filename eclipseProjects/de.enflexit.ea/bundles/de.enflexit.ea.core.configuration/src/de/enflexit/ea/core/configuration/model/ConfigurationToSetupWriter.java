package de.enflexit.ea.core.configuration.model;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;

/**
 * The Class ConfigurationToSetupWriter.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ConfigurationToSetupWriter extends Thread {

	private SetupConfigurationModel configModel;
	
	/**
	 * Instantiates a new configuration to setup writer.
	 * @param configModel the SetupConfigurationModel to use
	 */
	public ConfigurationToSetupWriter(SetupConfigurationModel configModel) {
		this.configModel = configModel;
		this.setName("Setup Configuration Model - Writing");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		this.writeToSetup();
	}
	
	/**
	 * Writes the current configuration to the setup.
	 */
	public void writeToSetup() {
		
		Vector<SetupConfigurationAttributeService> configColumns = this.configModel.getColumnVector();
		// --- Get table model row by row -----------------
		DefaultTableModel configTable = this.configModel.getConfigurationTableModel();
		for (int row = 0; row < configTable.getRowCount(); row++) {
			// --- Get all attributes / columns ----------- 
			ConfigurableComponent configComponent = (ConfigurableComponent) configTable.getValueAt(row, 0);
			for (int column = 1; column < configColumns.size(); column++) {
				SetupConfigurationAttributeService scas = configColumns.get(column);
				Object newValue = configTable.getValueAt(row, column);
				scas.setValue(configComponent, newValue);
			}
			
			// --- Save start arguments -------------------
			configComponent.saveAgentStartArguments();
			
		}
		
		// --- Save project and setup ---------------------
		this.configModel.getProject().save();
	}
	
	
}
