package de.enflexit.ea.core.configuration.persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import de.enflexit.common.csv.CsvFileReader;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;
import energy.helper.NumberHelper;

/**
 * The Class SetupConfigurationFileWriter.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationFileReader {

	private SetupConfigurationModel configModel;
	private Vector<Vector<Object>> rowDataVector;

	private HashMap<String, Integer> rowIndexHashMap;
	
	private HashMap<Integer, String>  dataHeaderHashMap;
	private HashMap<String, SetupConfigurationAttributeService> attributeServiceHashMap;
	private HashMap<SetupConfigurationAttributeService, Integer> attributeColumnIndexHashMap;
	
	
	/**
	 * Instantiates a new setup configuration writer.
	 * @param configModel the SetupConfigurationModel to work on
	 */
	public SetupConfigurationFileReader(SetupConfigurationModel configModel) {
		this.configModel = configModel;
	}
	
	/**
	 * Reads the model data from the specified CSV file.
	 * @param file the file to read
	 */
	public void read(File file) {
		
		// --- Read data from file ------------------------------------------------------
		this.rowDataVector = new CsvFileReader(SetupConfigurationFileWriter.VALUE_SEPERATOR).importData(file);
		
		// --- Proceed each row --------------------------------------------------------- 
		for (int row = 1; row < this.rowDataVector.size(); row++) {
			
			// --- Find the corresponding model row -------------------------------------
			Vector<Object> rowVector = this.rowDataVector.get(row);
			Integer modelRow = this.getRowIndexHashMap().get(rowVector.get(0));
			if (modelRow==null) {
				System.err.println("[" + this.getClass().getSimpleName() + "] No table model row could be found for a component named '" + rowVector.get(0) + "'; skipping row!");
				continue;
			}
			
			// --- Work on each row column ----------------------------------------------
			for (int rowVectorColumn = 1; rowVectorColumn < rowVector.size(); rowVectorColumn++) {
				
				// --- Find the correct model column in the destination table ----------- 
				String header = this.getDataHeaderHashMap().get(rowVectorColumn);
				SetupConfigurationAttributeService serviceAttribute = this.getAttributeServiceHashMap().get(header);
				Integer modelColumn = this.getAttributeColumnIndexHashMap().get(serviceAttribute);  
				if (modelColumn==null) {
					System.err.println("[" + this.getClass().getSimpleName() + "] No table model column could be found with a header named '" + header + "'; skipping column!");
					continue;
				}
				
				try {
					// --- Convert the value to the column type ------------------------- 
					String objectValueString = (String) rowVector.get(rowVectorColumn);
					Object objectValue = null;
					
					Class<?> type = serviceAttribute.getSetupConfigurationAttribute().getType(); 
					if (type==String.class) {
						objectValue = objectValueString;
					} else if (type==Boolean.class) {
						objectValue = Boolean.parseBoolean(objectValueString);
					} else if (type==Integer.class) {
						objectValue = NumberHelper.parseInteger(objectValueString);
					} else if (type==Float.class) {
						objectValue = NumberHelper.parseFloat(objectValueString);
					} else if (type==Double.class) {
						objectValue = NumberHelper.parseDouble(objectValueString);
					} else {
						System.err.println("[" + this.getClass().getSimpleName() + "] Unknown type '" + type.getClass().getName() + "' for column '" + header + "'; try to use string value.");
						objectValue = objectValueString;
					}
					
					// --- Set to table model -------------------------------------------
					this.configModel.getConfigurationTableModel().setValueAt(objectValue, modelRow, modelColumn);
					
				} catch (Exception ex) {
					System.err.println("[" + this.getClass().getSimpleName() + "] Error while proceeding column '" + header + "' of line " + (row+1) + ".");
					ex.printStackTrace();
				}
								
			} // end column for
		} // end row for
		
	}
	
	
	/**
	 * Returns the row index hash map.
	 * @return the row index hash map
	 */
	public HashMap<String, Integer> getRowIndexHashMap() {
		if (rowIndexHashMap==null) {
			rowIndexHashMap = new HashMap<>();
			DefaultTableModel configTableModel = this.configModel.getConfigurationTableModel();
			for (int row = 0; row < configTableModel.getRowCount(); row++) {
				ConfigurableComponent confComponent = (ConfigurableComponent) configTableModel.getValueAt(row, 0);
				rowIndexHashMap.put(confComponent.toString(), row);
			} 
		}
		return rowIndexHashMap;
	}
	
	
	/**
	 * Returns the data header hash map.
	 * @return the data header hash map
	 */
	private HashMap<Integer, String> getDataHeaderHashMap() {
		if (dataHeaderHashMap==null) {
			dataHeaderHashMap = new HashMap<>();
			if (this.rowDataVector!=null && this.rowDataVector.size()>1) {
				Vector<Object> headerVector = this.rowDataVector.get(0);
				for (int i = 0; i < headerVector.size(); i++) {
					dataHeaderHashMap.put(i, headerVector.get(i).toString());
				}
			}
		}
		return dataHeaderHashMap;
	}
	/**
	 * Returns the attribute service hash map.
	 * @return the attribute service hash map
	 */
	private HashMap<String, SetupConfigurationAttributeService> getAttributeServiceHashMap() {
		if (attributeServiceHashMap==null) {
			attributeServiceHashMap = new HashMap<>();
			for (SetupConfigurationAttributeService attributeService : this.configModel.getColumnVector()) {
				attributeServiceHashMap.put(attributeService.toString(), attributeService);
			}
		}
		return attributeServiceHashMap;
	}
	/**
	 * Returns the attribute to column index hash map.
	 * @return the attribute column hash map
	 */
	private HashMap<SetupConfigurationAttributeService, Integer> getAttributeColumnIndexHashMap() {
		if (attributeColumnIndexHashMap==null) {
			attributeColumnIndexHashMap = new HashMap<>();
			for (int i = 0; i < this.configModel.getColumnVector().size(); i++) {
				attributeColumnIndexHashMap.put(this.configModel.getColumnVector().get(i), i);
			}
		}
		return attributeColumnIndexHashMap;
	}
	
}
