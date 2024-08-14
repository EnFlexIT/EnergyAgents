package de.enflexit.ea.topologies.pandaPower;

import java.io.File;
import java.util.TreeMap;

import de.enflexit.common.csv.CSV_FilePreview;
import de.enflexit.common.csv.CsvDataController;

/**
 * The SimBenchFileLoader represents a singleton instance that enables to
 * load SimBench data and translate it into ScheduleList's 
 *  
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PandaPowerFileStore {

	// ----------------------------------------------------
	// --- The singleton construction ---------------------
	// ----------------------------------------------------
	private static PandaPowerFileStore thisInstance;
	
	private PandaPowerFileStore() { }
	public static PandaPowerFileStore getInstance() {
		if (thisInstance==null) {
			thisInstance = new PandaPowerFileStore();
		}
		return thisInstance;
	}
	
	// ----------------------------------------------------
	// --- Instance handling ------------------------------
	// ----------------------------------------------------
	public static final String PANDA_Bus		= "bus";
	public static final String PANDA_Trafo	 	= "trafo";
	public static final String PANDA_Load	 	= "load";
	public static final String PANDA_BusGeodata = "bus_geodata";
	
	public static final String PANDA_Line= "line";
	public static final String PANDA_StdLineType= "std_types.line";
	
	public static final String PANDA_Switch     = "switch";
	
	
	public static final String SIMBENCH_ExternalNet	 	= "ExternalNet.csv";
	public static final String SIMBENCH_LineType	 	= "LineType.csv";
	public static final String SIMBENCH_LoadProfile	 	= "LoadProfile.csv";
	public static final String SIMBENCH_Measurement	 	= "Measurement.csv";
	public static final String SIMBENCH_NodePFResult 	= "NodePFResult.csv";
	public static final String SIMBENCH_RES			 	= "RES.csv";
	public static final String SIMBENCH_RESProfile	 	= "RESProfile.csv";
	public static final String SIMBENCH_Storage		 	= "Storage.csv";
	public static final String SIMBENCH_StorageProfile  = "StorageProfile.csv";
	public static final String SIMBENCH_StudyCases		= "StudyCases.csv";
	public static final String SIMBENCH_TransformerType = "TransformerType.csv";
	
	private File pandanPowerFile;
	private PandaPowerTopologyImporter ppTopologyImporter;

	
	// --------------------------------------------------------------------------------------------
	// --- From here: Methods to set and load the csv files ---------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Set the currently selected PandaPower file and thus the directory (parent).
	 * 
	 * @param ppFile the selected SimBench file
	 * @param isDebug indicator to force and reload the specified file
	 */
	public synchronized void setPandaPowerDirectoryFile(File ppFile, boolean isForceReload) {
		
		if (ppFile!=null && ppFile.exists()==true) {
			// --- Check for known file type ------------------------
			if (ppFile.getName().toLowerCase().endsWith(".json")==false) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Unknown PandaPower file '" + ppFile.getAbsolutePath() + "', exit data load process.");
				return;
			}
			
			// --- Check if directory has changed -------------------
			boolean isChangedDir = this.pandanPowerFile==null || this.pandanPowerFile.equals(ppFile)==false;
			if (isForceReload==true || isChangedDir==true || this.getCsvDataController().size()==0) {
				// --- Set to local variables and load files --------
				this.pandanPowerFile = ppFile;
				this.readFileFromJson(ppFile);
			}
			
			// --- Show import preview ? ----------------------------
			this.showImportPreview();
		}
	}
	/**
	 * Read files from the specified JSON file.
	 *
	 * @param graphFile the graph file to read
	 * @return true, if successful
	 */
	private boolean readFileFromJson(File graphFile) {
		
		try {
			this.getCsvDataController().clear();
			PandaPowerJsonReader ppFileReader = new PandaPowerJsonReader(graphFile);
			this.getCsvDataController().putAll(ppFileReader.getCsvDataController());
			return true;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Shows up the PandaPower data.
	 */
	public void showImportPreview() {
		this.getPandaPowerTopologyImporter().showImportPreview(false);
	}
	/**
	 * Returns the CSV file preview dialog.
	 * @return the CSV file preview dialog
	 */
	public CSV_FilePreview getCSVFilePreviewDialog() {
		return this.getPandaPowerTopologyImporter().getCSVFilePreviewDialog();
	}
	
	// --------------------------------------------------------------------------------------------
	// --- Following connect to PandaPowerTopologyImporter (an AbstractNetworkModelCsvImporter) ---
	// --------------------------------------------------------------------------------------------
	/**
	 * Panda power topology importer.
	 * @return the panda power topology importer
	 */
	public PandaPowerTopologyImporter getPandaPowerTopologyImporter() {
		if (ppTopologyImporter==null) {
			ppTopologyImporter = new PandaPowerTopologyImporter();
		}
		return ppTopologyImporter;
	}
	/**
	 * Sets the panda power topology importer.
	 * @param ppTopologyImporter the new panda power topology importer
	 */
	public void setPandaPowerTopologyImporter(PandaPowerTopologyImporter ppTopologyImporter) {
		this.ppTopologyImporter = ppTopologyImporter;
	}
	
	/**
	 * Return the csv data controller.
	 * @return the csv data controller
	 */
	public TreeMap<String, CsvDataController> getCsvDataController() {
		return this.getPandaPowerTopologyImporter().getCsvDataController();
	}
	
	
	// ----------------------------------------------------------------------------------	
	// --- From here, static help methods -----------------------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Checks if the specified voltage level requires a three phase configuration 
	 * (that is basically the case, if the voltage level is <= 400V).
	 *
	 * @param voltageLevel the voltage level to check
	 * @return true, if a three phase configuration is required
	 */
	public static boolean isTriPhaseVoltageLevel(double voltageLevel) {
		return voltageLevel<=400 ? true : false;
	}
	
}
