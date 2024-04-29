package de.enflexit.ea.simbench.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter;

/**
 * The SimBenchFileLoader represents a singleton instance that enables to
 * load SimBench data and translate it into ScheduleList's 
 *  
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimBenchFileStore extends AbstractNetworkModelCsvImporter {

	// ----------------------------------------------------
	// --- The singleton construction ---------------------
	// ----------------------------------------------------
	private static SimBenchFileStore thisInstance;
	
	private SimBenchFileStore() { }
	public static SimBenchFileStore getInstance() {
		if (thisInstance==null) {
			thisInstance = new SimBenchFileStore();
		}
		return thisInstance;
	}
	
	// ----------------------------------------------------
	// --- Instance handling ------------------------------
	// ----------------------------------------------------
	public static final String SIMBENCH_Coordinates	 	= "Coordinates.csv";
	public static final String SIMBENCH_ExternalNet	 	= "ExternalNet.csv";
	public static final String SIMBENCH_Line		 	= "Line.csv";
	public static final String SIMBENCH_LineType	 	= "LineType.csv";
	public static final String SIMBENCH_Load		 	= "Load.csv";
	public static final String SIMBENCH_LoadProfile	 	= "LoadProfile.csv";
	public static final String SIMBENCH_Measurement	 	= "Measurement.csv";
	public static final String SIMBENCH_Node		 	= "Node.csv";
	public static final String SIMBENCH_NodePFResult 	= "NodePFResult.csv";
	public static final String SIMBENCH_RES			 	= "RES.csv";
	public static final String SIMBENCH_RESProfile	 	= "RESProfile.csv";
	public static final String SIMBENCH_Storage		 	= "Storage.csv";
	public static final String SIMBENCH_StorageProfile  = "StorageProfile.csv";
	public static final String SIMBENCH_StudyCases		= "StudyCases.csv";
	public static final String SIMBENCH_Transformer	 	= "Transformer.csv";
	public static final String SIMBENCH_TransformerType = "TransformerType.csv";
	
	private List<FileFilter> fileFilterList;
	private File simBenchDirecotry;  

	
	// --------------------------------------------------------------------------------------------
	// --- From here: Methods to set and load the csv files ---------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Set the currently selected SimBench file and thus the directory (parent).
	 * 
	 * @param simBenchDirecotryFile the selected SimBench file
	 * @param isDebug indicator if the current invocation is for debugging and if the import files should be shown
	 */
	public synchronized void setSimBenchDirectoryFile(File simBenchDirecotryFile, boolean isDebug) {
		if (simBenchDirecotryFile!=null && simBenchDirecotryFile.exists()==true) {

			// --- Get the parent directory to find all files -------  
			File simBenchDirecotry = simBenchDirecotryFile.getParentFile();
			boolean validDir = simBenchDirecotry!=null && simBenchDirecotry.exists() && simBenchDirecotry.isDirectory(); 
			if (validDir==false) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Invalid SimBench directory '" + simBenchDirecotry.getAbsolutePath() + "', exit data load process.");
				return;
			}
			
			// --- Check if directory has changed -------------------
			boolean isChangedDir = this.simBenchDirecotry==null || this.simBenchDirecotry.equals(simBenchDirecotry)==false;
			if (isChangedDir==true) {
				// --- Set to local variables and load files --------
				this.simBenchDirecotry = simBenchDirecotry;
				this.readCsvFiles(simBenchDirecotryFile, true);
			}
			
			// --- Show import preview ? ----------------------------
			this.debug = isDebug;
			this.showImportPreview();
		}
	}

	/**
	 * Show the SimBench data.
	 */
	public void showSimbenchData() {
		boolean isDebug = this.isDebug();
		this.setDebug(true);
		super.showImportPreview();
		this.setDebug(isDebug);
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here: Some configuration for the super class AbstractNetworkModelCsvImporter ------
	// --------------------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter#getListOfRequiredFileNames()
	 */
	@Override
	protected Vector<String> getListOfRequiredFileNames() {
		
		Vector<String> fileNameVector = new Vector<>(); 
		fileNameVector.add(SIMBENCH_Coordinates);
		fileNameVector.add(SIMBENCH_ExternalNet);
		fileNameVector.add(SIMBENCH_Line);
		fileNameVector.add(SIMBENCH_LineType);
		fileNameVector.add(SIMBENCH_Load);
		fileNameVector.add(SIMBENCH_LoadProfile);
		fileNameVector.add(SIMBENCH_Measurement);
		fileNameVector.add(SIMBENCH_Node);
		fileNameVector.add(SIMBENCH_NodePFResult);
		fileNameVector.add(SIMBENCH_RES);
		fileNameVector.add(SIMBENCH_RESProfile);
		fileNameVector.add(SIMBENCH_Storage);
		fileNameVector.add(SIMBENCH_StorageProfile);
		fileNameVector.add(SIMBENCH_StudyCases);
		fileNameVector.add(SIMBENCH_Transformer);
		fileNameVector.add(SIMBENCH_TransformerType);
		return fileNameVector;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#getFileFilters()
	 */
	@Override
	public List<FileFilter> getFileFilters() {
		if (fileFilterList==null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(this.createFileFilter(".csv", "SimBench Eletrical Network (csv files)"));
		}
		return fileFilterList;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#importNetworkModelFromFile(java.io.File)
	 */
	@Override
	public NetworkModel importNetworkModelFromFile(File graphFile) {
		return null; // --- Nothing to do here ---
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
