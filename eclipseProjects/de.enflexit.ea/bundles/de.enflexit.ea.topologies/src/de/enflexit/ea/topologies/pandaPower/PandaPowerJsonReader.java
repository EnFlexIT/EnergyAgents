package de.enflexit.ea.topologies.pandaPower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import de.enflexit.common.csv.CsvDataController;

/**
 * The Class PandaPowerJsonReader.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PandaPowerJsonReader {

	public static final String KEY_GENERAL_PROPERTIES = "a) General Properties"; 
	
	private boolean isDebug = false;
	
	private File jsonFile;
	private JsonObject jsonObject;
	private String currPropertyName;
	
	private TreeMap<String, CsvDataController> csvDataController;
	
	/**
	 * Instantiates a new panda power file reader.
	 * @param jsonFile the json file to read
	 */
	public PandaPowerJsonReader(File jsonFile) {
		this.jsonFile = jsonFile;
		this.readFile();
		this.processJsonObject();
	}
	
	/**
	 * Reads the file specified with the constructor.
	 */
	private void readFile() {
		
		BufferedReader br = null; 
		try {
			br = new BufferedReader(new FileReader(this.jsonFile));
			JsonElement jsonElement = JsonParser.parseReader(br);
			this.jsonObject = jsonElement.getAsJsonObject();
			
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException ex) {
			ex.printStackTrace();
			
		} finally {
			if (br!=null) {
				try {
					br.close();
				} catch (IOException ioEx) {
					ioEx.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Returns the locally produced CsvDataController.
	 * @return the csv data controller
	 */
	public TreeMap<String, CsvDataController> getCsvDataController() {
		if (csvDataController==null) {
			csvDataController = new TreeMap<>();
		}
		return csvDataController;
	}
	
	/**
	 * Return the table model for the general properties.
	 * @return the general properties
	 */
	private DefaultTableModel getGeneralProperties() {
		
		CsvDataController csvContGenProps =  this.getCsvDataController().get(KEY_GENERAL_PROPERTIES);
		if (csvContGenProps==null) {
			
			Vector<String> colNames = new Vector<>();
			colNames.add("key");
			colNames.add("value");
			DefaultTableModel tmGenProps = new DefaultTableModel(null, colNames);
			csvContGenProps = new CsvDataController(null, null, true, tmGenProps);
			this.getCsvDataController().put(KEY_GENERAL_PROPERTIES, csvContGenProps);
		}
		return csvContGenProps.getDataModel();
	}
	/**
	 * Adds a general property.
	 * @param key the key
	 * @param value the value
	 */
	private void addGeneralProperty(String key, String value) {
		Vector<Object> row = new Vector<>();
		row.add(key);
		row.add(value);
		this.getGeneralProperties().addRow(row);
	}
	
	/**
	 * Processes the imported JSON object.
	 */
	private void processJsonObject() {
		if (this.jsonObject==null) return;
		this.processJsonObject(this.jsonObject);
	}
	/**
	 * Processes the specified JsonObject.
	 * @param jsonObject the json object
	 */
	private void processJsonObject(JsonObject jsonObject) {
		
		JsonElement jeModule = jsonObject.get("_module");
		JsonElement jeClazz = jsonObject.get("_class");
		JsonElement jeObject = jsonObject.get("_object");
		JsonElement jeOrient = jsonObject.get("orient");
		JsonElement jeObjectDType = jsonObject.get("dtype");
		JsonElement jeIsMultiindex  = jsonObject.get("is_multiindex");
		JsonElement jeIsMulticolumn = jsonObject.get("is_multicolumn");
		
		String module = jeModule!=null ? jeModule.getAsString() : null;
		String clazz = jeClazz!=null ? jeClazz.getAsString() : null;
		JsonObject object = this.parsePandaPowerObject(jeObject, clazz);
		String orient = jeOrient!=null ? jeOrient.getAsString() : null;
		JsonObject objectDType = jeObjectDType!=null ? jeObjectDType.getAsJsonObject() : null;
		Boolean isMultiindex  = jeIsMultiindex!=null ? jeIsMultiindex.getAsBoolean() : null;
		Boolean isMulticolumn = jeIsMulticolumn!=null ? jeIsMulticolumn.getAsBoolean() : null;
		
		if (clazz==null) {
			// --- No PandaPower '_class' was found -----------------
			if (jsonObject.entrySet().size()>0) {
				// --- In case of JSON list entries -----------------
				for (String key : jsonObject.keySet()) {
					// --- Set current property to work on ----------
					String prevPropertyName = this.currPropertyName;
					this.currPropertyName = key;
					JsonElement currPropertyValue = jsonObject.get(this.currPropertyName);
					
					if (currPropertyValue.isJsonPrimitive()) {
						// --- For primitives -----------------------
						String valueString = currPropertyValue.getAsString();
						this.addGeneralProperty(this.currPropertyName, valueString);
						
					} else if (currPropertyValue.isJsonObject()) {
						// --- For objects --------------------------
						JsonObject value = currPropertyValue.getAsJsonObject();
						if (prevPropertyName!=null && prevPropertyName.equals("std_types")==true) {
							this.addTableModelForStandardType(this.currPropertyName, value);
							
						} else {
							this.processJsonObject(value);
						}
						
					}
					// --- Revert to previous property --------------
					this.currPropertyName = prevPropertyName;
				} // end for
				
			} else {
				// --- Empty JSON object ----------------------------
				String propDescription = this.currPropertyName==null ? "" : " for property '" + this.currPropertyName + "'";
				if (this.isDebug==true) System.out.println("Found empty JSON Object" + propDescription + ": " + jsonObject);
				
			}
			
		} else {
			// --- PandaPower specific data types -------------------
			if (clazz.equals("pandapowerNet")==true) {
				this.processJsonObject(object);
			} else if (clazz.equals("DataFrame")==true) {
				this.processDataFrame(module, clazz, object, orient, objectDType, isMultiindex, isMulticolumn);
			}
			
		}
	}
	/**
	 * Parses the specified PandaPower 'object' JsonElement.
	 *
	 * @param jeObject the JsonElement to evaluate
	 * @param clazz the PandaPower class name
	 * @return the json object
	 */
	private JsonObject parsePandaPowerObject(JsonElement jeObject, String clazz) {
		
		if (jeObject==null) return null;

		// --- DataFrame specific parsing ---------------------------
		if (clazz.equals("DataFrame")==true) {
			try {
				JsonPrimitive jsonString = jeObject.getAsJsonPrimitive();
				return new Gson().fromJson(jsonString.getAsString(), JsonObject.class);

			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			}
		}
		return jeObject.getAsJsonObject();
	}

	/**
	 * Process data frame.
	 *
	 * @param module the module
	 * @param clazz the PandaPower _clazz parameter
	 * @param jsObject the JSON object
	 * @param orient the orient
	 * @param objectDType the object D type
	 * @param isMultiindex the is multiindex
	 * @param isMulticolumn the is multicolumn
	 */
	private void processDataFrame(String module, String clazz, JsonObject jsObject, String orient, JsonObject objectDType, Boolean isMultiindex, Boolean isMulticolumn) {
		
		if (jsObject.entrySet().size()>0) {
			
			JsonElement valueColumns = null;
			JsonArray valueIndex = null;
			JsonElement valueData = null;
			
			// --- Collect required JSON information ----------------
			for (String key : jsObject.keySet()) {
				// --- Set current property to work on --------------
				JsonElement value = jsObject.get(key);
				switch (key) {
				case "columns":
					valueColumns = value;
					break;
				case "index":
					valueIndex = value.getAsJsonArray();
					break;
				case "data":
					valueData = value;
					break;
				}
			} // end for

			// --- Create table model -------------------------------
			List<Integer> indexList = new ArrayList<>();
			if (valueIndex!=null) {
				for (int i = 0; i < valueIndex.size(); i++) {
					indexList.add(valueIndex.get(i).getAsInt());
				}			
			}
			boolean isCreateIndexColumn = indexList.size()>0; 
			
			DefaultTableModel tableModel = this.getTableModelForDataFrame(valueColumns, objectDType, isCreateIndexColumn);
			this.fillTableModelForDataFrame(tableModel, indexList, valueData, objectDType);
			
			
			// --- Remind the table model ---------------------------
			if (tableModel!=null && tableModel.getRowCount()>0) {
				if (this.isDebug==true) System.out.println("Adding data for table '" + this.currPropertyName + "'");
				this.getCsvDataController().put(this.currPropertyName, new CsvDataController(null, null, true, tableModel));
			}
		}
	}
	
	/**
	 * Returns the table model for a data frame.
	 *
	 * @param elementList the element list
	 * @param objectDType the object D type
	 * @return the table model for data frame
	 */
	private DefaultTableModel getTableModelForDataFrame(JsonElement elementList, JsonObject objectDType, boolean createIndexColumn) {
		
		// --- Create columns --------------------------------------- 
		Vector<String> colNames = new Vector<>();
		if (createIndexColumn==true) {
			colNames.add("index");
		}
		
		JsonArray colArray = (JsonArray) elementList;
		for (int i = 0; i < colArray.size(); i++) {
			colNames.add(colArray.get(i).getAsString());
		}

		// --- Check the column classes -----------------------------
		final List<Class<?>> columnClasses = this.getColumnClassesForDataFrame(objectDType, createIndexColumn);
		
		// --- Create the table model -------------------------------
		DefaultTableModel tableModel = new DefaultTableModel(null, colNames) {
			private static final long serialVersionUID = -8099611902571500361L;
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex>=columnClasses.size()) {
					return super.getColumnClass(columnIndex);
				}
				return columnClasses.get(columnIndex);
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		return tableModel;
	}
	/**
	 * Gets the column classes for data frame.
	 *
	 * @param objectDType the object D type
	 * @return the column classes for data frame
	 */
	private List<Class<?>> getColumnClassesForDataFrame(JsonObject objectDType, boolean createIndexColumn) {
		
		List<Class<?>> colClasses = new ArrayList<>();
		if (objectDType==null) return colClasses;
		
		if (createIndexColumn==true) {
			colClasses.add(Integer.class);
		}
		
		for (String key : objectDType.keySet()) {
			String value = objectDType.get(key).getAsString();
			switch (value) {
			case "object":
				colClasses.add(String.class);
				break;
			case "bool":
				colClasses.add(Boolean.class);
				break;
			case "uint32":
			case "int32":
				colClasses.add(Integer.class);
				break;
			case "int64":
				colClasses.add(Long.class);
				break;
			case "float64":
				colClasses.add(Double.class);
				break;
			default:
				System.err.println("[" + this.getClass().getSimpleName() + "] Unknown data type '" + value + "' for column '" + key + "' in table '" + this.currPropertyName + "'! => Adding as string");
				colClasses.add(String.class);
				break;
			}
		}
		return colClasses;
	}
	
	/**
	 * Fill table model for data frame.
	 *
	 * @param tableModel the table model
	 * @param indexList the index list
	 * @param jsElement the js element
	 * @param objectDType the object D type
	 */
	private void fillTableModelForDataFrame(DefaultTableModel tableModel, List<Integer> indexList, JsonElement jsElement, JsonObject objectDType) {
		
		JsonArray colArray = (JsonArray) jsElement;
		for (int i = 0; i < colArray.size(); i++) {
			
			Integer indexValue = null;
			if (indexList.size()>0 && i<indexList.size()) {
				indexValue = indexList.get(i);
			}
			
			JsonArray rowData = (JsonArray) colArray.get(i);
			Vector<Object> tbRow = this.getTableRowForDataFrame(indexValue, rowData, objectDType);
			tableModel.addRow(tbRow);
		}
	}
	/**
	 * Fill table row for data frame.
	 *
	 * @param indexValue the index
	 * @param rowData the row data
	 * @param objectDType the object D type
	 * @return the table row for data frame
	 */
	private Vector<Object> getTableRowForDataFrame(Integer indexValue, JsonArray rowData, JsonObject objectDType) {
		
		int indexShift = 0;
		Vector<Object> tbRow = new Vector<>();
		if (indexValue!=null) {
			indexShift = 1;
			tbRow.add(indexValue);
		}
		
		List<Class<?>> columnClasses = this.getColumnClassesForDataFrame(objectDType, indexValue!=null);
		
		for (int i = 0; i < rowData.size(); i++) {
			
			JsonElement jsCellValue = rowData.get(i);
			Object cellValue = null;

			if (jsCellValue.isJsonNull()==false) {
				Class<?> colClass = columnClasses.get(i + indexShift);
				if (colClass == String.class) {
					cellValue = jsCellValue.getAsString();
				} else if (colClass == Boolean.class) {
					cellValue = jsCellValue.getAsBoolean();
				} else if (colClass == Integer.class) {
					cellValue = jsCellValue.getAsInt();
				} else if (colClass == Long.class) {
					cellValue = jsCellValue.getAsLong();
				} else if (colClass == Double.class) {
					cellValue = jsCellValue.getAsDouble();
				}
			}
			
			tbRow.add(cellValue);
		}
		return tbRow;
	}
	
	/**
	 * Gets the table model for standard type.
	 *
	 * @param stdType the std type
	 * @param jsObject the js object
	 * @return the table model for standard type
	 */
	private void addTableModelForStandardType(String stdType, JsonObject jsObject) {
		
		DefaultTableModel tm = null;
		
		// --- Iterate over elements ----------------------
		for (String key : jsObject.keySet()) {
			JsonElement jsValueElement = jsObject.get(key);
			JsonObject jsValueObject = jsValueElement.isJsonObject()==true ? jsValueElement.getAsJsonObject() : null;
			
			// --------------------------------------------
			// --- Create the table model? ----------------
			// --------------------------------------------
			if (tm==null) {
				// --- Create column header --------------- 
				Vector<String> header = new Vector<>();
				header.add("ID");
				for (String headerKey : jsValueObject.keySet()) {
					header.add(headerKey);
				}
				
				// --- Create table model -----------------
				tm = new DefaultTableModel(null, header) {
					private static final long serialVersionUID = 1223334406957020165L;
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
			}
			// --------------------------------------------
			
			// --------------------------------------------
			// --- Add data -------------------------------
			// --------------------------------------------
			Vector<Object> rowData = new Vector<>();
			rowData.add(key);
			for (String headerKey : jsValueObject.keySet()) {
				JsonElement cellValue = jsValueObject.get(headerKey);
				
				if (cellValue.isJsonNull()==true) {
					rowData.add(null);
					
				} else if (cellValue.isJsonPrimitive()==true) {
					JsonPrimitive prim = cellValue.getAsJsonPrimitive();
					if (prim.isBoolean()==true) {
						rowData.add(prim.getAsBoolean());
					} else if (prim.isNumber()==true) {
						rowData.add(prim.getAsNumber());
					} else if (prim.isString()) {
						rowData.add(prim.getAsString());
					}
					
				} else if (cellValue.isJsonArray()==true) {
					rowData.add(new Gson().toJson(cellValue));
					
				} else if (cellValue.isJsonObject()==true) {
					rowData.add(null);
					
				}
				
			}
			tm.addRow(rowData);
			// --------------------------------------------
		}
		
		// --- Add to local storage -----------------------
		this.getCsvDataController().put("std_types." + stdType, new CsvDataController(null, null, true, tm));
		
	}
	
	
}
