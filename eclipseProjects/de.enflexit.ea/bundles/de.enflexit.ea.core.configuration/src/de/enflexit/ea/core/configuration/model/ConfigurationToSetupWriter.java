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

	private static final String THREAD_NAME = "Setup Configuration Model - Writing";
	
	private SetupConfigurationModel configModel;
	private Vector<DataModelWriterThread> dataModelWriterThreads;
	
	/**
	 * Instantiates a new configuration to setup writer.
	 * @param configModel the SetupConfigurationModel to use
	 */
	public ConfigurationToSetupWriter(SetupConfigurationModel configModel) {
		this.configModel = configModel;
		this.setName(THREAD_NAME);
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
		
		this.configModel.setUIMessage("Writing table data to setup ...");
		
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
			
			// --- Save EOM model changes -----------------
			if (configComponent.isEomModel()==true || configComponent.containsEomModel()) {
				// --- Create new extra save thread -------
				DataModelWriterThread eomModelThread = new DataModelWriterThread(configComponent);
				this.getEomModelWriterThreads().add(eomModelThread);
				eomModelThread.start();
			}
		}
		
		
		if (this.getEomModelWriterThreads().size()>0) {
			// --- Wait for the end of the saving tasks ---
			while (this.getEomModelWriterThreads().size()>0) {
				try {
					this.configModel.setUIMessage("Waiting for EOM model saving ...");
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		// --- Save project and setup ---------------------
		this.configModel.getProject().save();
		// --- Set UI message -----------------------------
		this.configModel.setUIMessage("Wrote table data and saved setup!");
		// --- Recreate table model ----------------------- 
		this.configModel.reCreateConfigurationTableModel();
	}
	
	/**
	 * Returns the vector of DataModelWriterThread.
	 * @return the eom model writer threads
	 */
	private Vector<DataModelWriterThread> getEomModelWriterThreads() {
		if (dataModelWriterThreads==null) {
			dataModelWriterThreads = new Vector<>();
		}
		return dataModelWriterThreads;
	}
	
	
	/**
	 * The Class DataModelWriterThread.
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	private class DataModelWriterThread extends Thread {
		
		private ConfigurableComponent cComponent;
		
		/**
		 * Instantiates a DataModelWriterThread.
		 * @param cComponent the ConfigurableComponent to save
		 */
		public DataModelWriterThread(ConfigurableComponent cComponent) {
			this.cComponent = cComponent;
			this.setName(THREAD_NAME + " (" + cComponent.toString() + ")");
		}
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				this.cComponent.saveDataModel();
			} catch (StackOverflowError se) {
				System.err.println("[" + this.getClass().getSimpleName() + "] StackOverflow at " + this.getName());
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ConfigurationToSetupWriter.this.getEomModelWriterThreads().remove(this);
			}
		}
	}
	
}
