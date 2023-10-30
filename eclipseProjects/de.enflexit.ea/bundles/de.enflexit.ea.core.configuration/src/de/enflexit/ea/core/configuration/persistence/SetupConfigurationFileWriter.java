package de.enflexit.ea.core.configuration.persistence;

import java.io.File;
import java.util.Vector;

import agentgui.core.application.Application;
import de.enflexit.common.csv.CsvFileWriter;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;
import energy.helper.NumberHelper;

/**
 * The Class SetupConfigurationFileWriter.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationFileWriter {

	public static final String DECIMAL_SEPERATOR = NumberHelper.getLocalDecimalSeparator();
	public static final String VALUE_SEPERATOR = DECIMAL_SEPERATOR.equals(",") ? ";" : ",";
	public static final String NEW_LINE = Application.getGlobalInfo().getNewLineSeparator();
	
	private SetupConfigurationModel configModel;
	
	/**
	 * Instantiates a new setup configuration writer.
	 * @param configModel the config model
	 */
	public SetupConfigurationFileWriter(SetupConfigurationModel configModel) {
		this.configModel = configModel;
	}
	
	/**
	 * Writes the model data to the specified file .
	 * @param file the file to write to
	 */
	public void write(File file) {
		
		// --- Get / Prepare required information -------------------
		Vector<SetupConfigurationAttributeService> columnVector = this.configModel.getColumnVector();
		Object[][] tableData = new Object[this.configModel.getConfigurationTableModel().getRowCount()][columnVector.size()];
		String headerLine = "";
		
		// --- Prepare header row and object array ------------------ 
		for (int row = 0; row < this.configModel.getConfigurationTableModel().getRowCount(); row++) {
			for (int column = 0; column < columnVector.size(); column++) {
				if (row==0) {
					SetupConfigurationAttributeService attrService = columnVector.get(column);
					headerLine += headerLine.isBlank()==true ? attrService.toString() : VALUE_SEPERATOR + attrService.toString();
				}
				tableData[row][column] = this.configModel.getConfigurationTableModel().getValueAt(row, column);
			}
		}
		
		// --- Write the data to the CSV file -----------------------
		new CsvFileWriter(VALUE_SEPERATOR, DECIMAL_SEPERATOR, NEW_LINE).exportData(file, tableData, headerLine);

		// --- Write UI message -------------------------------------
		this.configModel.setUIMessage("Exportetd table data to file '" + file.getName() + "'.");
	}
	
}
