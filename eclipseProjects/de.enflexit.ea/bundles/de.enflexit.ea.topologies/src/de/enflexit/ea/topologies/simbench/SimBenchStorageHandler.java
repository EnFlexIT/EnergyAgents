package de.enflexit.ea.topologies.simbench;

import java.awt.Window;
import java.io.File;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.awb.core.Application;
import de.enflexit.common.PathHandling;
import de.enflexit.eom.awb.adapter.AbstractEomStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelAdapter;
import energy.EomController;
import energy.optionModel.ScheduleList;
import energy.persistence.service.PersistenceService;
import energy.schedule.ScheduleController;

/**
 * The SimBench implementation for an EOM storage handler. This is used within Agent.Workbench
 * to load ScheduelList from the SimBench files to the {@link NetworkComponent}s.  
 *  
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class SimBenchStorageHandler extends AbstractEomStorageHandler {

	public static final String SIM_BENCH_SETTING_PATH_NAME = "SimBenchPath";
	public static final String SIM_BENCH_SETTING_FILE_NAME = "SimBenchSourceFileName";
	public static final String SIM_BENCH_SETTING_ROW_INDEX = "SimBenchRowIndex";

	
	/**
	 * Creates a new SomBenchStorageHandler.
	 * @param eomDataModelAdapter
	 */
	public SimBenchStorageHandler(EomDataModelAdapter eomDataModelAdapter) {
		super(eomDataModelAdapter);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.adapter.AbstractEomStorageHandler#getPersistenceServiceClass(EomModelType)
	 */
	@Override
	public Class<? extends PersistenceService> getPersistenceServiceClass(EomModelType modelType) {
		
		Class<? extends PersistenceService> psClass = null;
		switch (modelType) {
		case TechnicalSystem:
			break;

		case ScheduleList:
			psClass = SimBenchScheduelListPersistenceService.class;
			break;
			
		case TechnicalSystemGroup:
			break;
		}
		return psClass;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.adapter.AbstractEomStorageHandler#loadDataModelCustomized(org.awb.env.networkModel.DataModelNetworkElement)
	 */
	@Override
	public void configureEomController(TreeMap<String, String> storageSettings, EomController eomController) {
		
		if (eomController instanceof ScheduleController) {
			
			ScheduleController sc = (ScheduleController) eomController;
			
			// --- Source directory -----------------
			String sbPathName = storageSettings.get(SIM_BENCH_SETTING_PATH_NAME);
			if (sbPathName==null || sbPathName.isEmpty()==true) {
				this.printToConsole("No source directory was specified for the data of network element.", true);
				return;
			}
			File sbPath = new File(sbPathName);
			if (sbPath.exists()==false) {
				// --- Try project location ---------
				String projectPathName = this.getProject().getProjectFolderFullPath();
				String sbPathNameProject = projectPathName + sbPathName;
				sbPath = new File(sbPathNameProject);
				if (sbPath.exists()==false) {
					this.printToConsole("The specified source directory '" + sbPathName + "' for the data of network element is invalid.", true);
					return;
				} else {
					sbPathName = sbPathNameProject;
				}
			}
			sc.getEomControllerStorageSettings().setCurrentFile(sbPath, SimBenchScheduelListPersistenceService.class);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.adapter.AbstractEomStorageHandler#hasSettingError(java.util.TreeMap, energy.EomController)
	 */
	@Override
	public boolean hasSettingError(TreeMap<String, String> storageSettings, EomController eomController) {
	
		if (eomController instanceof ScheduleController) {

			// --- Get settings from ScheduelController -------------
			ScheduleController sc = (ScheduleController) eomController;

			// --- Check if in project path -------------------------
			String sbPathName = sc.getEomControllerStorageSettings().getCurrentFile().getAbsolutePath();
			String projectPathName = this.getProject().getProjectFolderFullPath();
			if (sbPathName.startsWith(projectPathName)==true) {
				sbPathName = sbPathName.substring(projectPathName.length(), sbPathName.length());
			}
			
			// --- Get source table and row index -------------------- 
			String sbFileSelection = null;
			int sbRowSelection     = -1;
			
			String sbLoadDescription = sc.getScheduleList().getDescription();
			if (sbLoadDescription!=null) {
				try {
					String[] sbLoadDescriptionArray = sbLoadDescription.split("\\|");
					sbFileSelection = sbLoadDescriptionArray[0];
					sbRowSelection  = Integer.parseInt(sbLoadDescriptionArray[1]);
				} catch (Exception ex) {
					System.err.println("[" + this.getClass().getSimpleName() + "] Error while reading load description.");
					sbFileSelection = null;
					sbRowSelection  = -1;
				}
			}

			// --- Do error checks ----------------------------------
			String title = "Error in SimBench settings!";
			String partMessage = null;
			String message = null;
			if (sbPathName==null) {
				partMessage = "No source path could be found for the current ScheduleList.";
				message = message==null ? partMessage : message + "\n" + partMessage; 
			}
			if (sbFileSelection==null) {
				partMessage = "No SimBench source file could be found for the current ScheduleList.";
				message = message==null ? partMessage : message + "\n" + partMessage;
			}
			if (sbRowSelection<0) {
				partMessage = "No row selection could be found for the current ScheduleList.";
				message = message==null ? partMessage : message + "\n" + partMessage;
			}
			
			// --- Show error message -------------------------------
			if (message!=null) {
				message = "Errors found: \n" + message;
				JOptionPane.showMessageDialog((Window)Application.getMainWindow(), message, title, JOptionPane.ERROR_MESSAGE, null);
				return true;
			}

			// --- Set to storage settings --------------------------
			storageSettings.put(SIM_BENCH_SETTING_PATH_NAME, sbPathName);
			storageSettings.put(SIM_BENCH_SETTING_FILE_NAME, sbFileSelection);
			storageSettings.put(SIM_BENCH_SETTING_ROW_INDEX, sbRowSelection + "");
			
		}
		return false;
	}

	
	// ----------------------------------------------------------------------------------
	// --- From here, the headless save and load methods -------------------------------- 
	// ----------------------------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.adapter.AbstractEomStorageHandler#loadDataModelCustomized(org.awb.env.networkModel.DataModelNetworkElement)
	 */
	@Override
	public Object loadDataModelCustomized(DataModelNetworkElement networkElement) {
		
		// --- Early exit ? -----------------------------------------
		if (networkElement==null || networkElement.getDataModelStorageSettings()==null || networkElement.getDataModelStorageSettings().size()==0) return null;
		
		// --- Get storage information ------------------------------
		TreeMap<String, String> storageSettings = networkElement.getDataModelStorageSettings();

		String modelTypeString = storageSettings.get(EOM_SETTING_EOM_MODEL_TYPE);
		String sbPathName = PathHandling.getPathName4LocalOS(storageSettings.get(SIM_BENCH_SETTING_PATH_NAME));
		String sbFileSelection = storageSettings.get(SIM_BENCH_SETTING_FILE_NAME);
		String sbRowIndexString = storageSettings.get(SIM_BENCH_SETTING_ROW_INDEX);
		
		// --------------------------------------
		// --- Model type -----------------------
		EomModelType eomModelType = null;
		if (modelTypeString==null || modelTypeString.isEmpty()==true) {
			this.printToConsole("No EOM model type was specified for network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "'.", true);
			return null;
		} else {
			eomModelType = EomModelType.valueOf(modelTypeString);
		}
		if (eomModelType!=EomModelType.ScheduleList) {
			this.printToConsole("Network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "' is not of type ScheduleList.", true);
			return null;
		}
		
		// --------------------------------------
		// --- Source directory -----------------
		if (sbPathName==null || sbPathName.isEmpty()==true) {
			this.printToConsole("No source directory was specified for the data of network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "'.", true);
			return null;
		}
		File sbPath = new File(sbPathName);
		if (sbPath.exists()==false) {
			// --- Try project location ---------
			String projectPathName = this.getProject().getProjectFolderFullPath();
			String sbPathNameProject = projectPathName + sbPathName;
			sbPath = new File(sbPathNameProject);
			if (sbPath.exists()==false) {
				this.printToConsole("The specified source directory '" + sbPathName + "' for the data of network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "' is invalid.", true);
				return null;
			} else {
				sbPathName = sbPathNameProject;
			}
		}
		
		// --------------------------------------
		// --- Source file ----------------------
		if (sbFileSelection==null || sbFileSelection.isEmpty()==true) {
			this.printToConsole("No source file for the component identification was found in the settings of network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "'.", true);	
			return null;
		}
		File sourceFile = new File(sbPathName + File.separator + sbFileSelection);
		if (sourceFile.exists()==false) {
			this.printToConsole("The specified source file '" + sbFileSelection + "' for the component identification of network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "' could not be found.", true);
			return null;
		}

		// --------------------------------------
		// --- Row index ------------------------
		int sbRowIndex = -1;
		if (sbRowIndexString==null || sbRowIndexString.isEmpty()==true) {
			this.printToConsole("No row index was specified for the component identification of network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "'.", true);
			return null;
		}
		try {
			sbRowIndex = Integer.parseInt(sbRowIndexString);
		} catch (Exception ex) { }

		if (sbRowIndex==-1) {
			this.printToConsole("No valid row index was specified for the component identification of network element " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "'.", true);
			return null;
		}
		return this.loadScheduleList(sourceFile, sbFileSelection, sbRowIndex);
	}
	/**
	 * Loads and returns the ScheduleList from the the specified SimBench data. 
	 * @param simBenchFileSelected any file out of the SimBench source directory (e.g ExternalNet.csv)
	 * @param sbFileSelection the source file name for the element identification (Node.csv or Load.csv)
	 * @param sbRowIndexSelection the row index for element identification
	 * @return the ScheduelList as specified or <code>null</code>
	 */
	private ScheduleList loadScheduleList(File simBenchFileSelected, String sbFileSelection, int sbRowIndexSelection) {
		ScheduleList sl = null;
		try {
			SimBenchScheduelListPersistenceService sbPS = new SimBenchScheduelListPersistenceService();
			sl = sbPS.loadScheduleList(simBenchFileSelected, sbFileSelection, sbRowIndexSelection, this.getScheduleTimeRange());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sl;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.adapter.AbstractEomStorageHandler#saveDataModelCustomized(org.awb.env.networkModel.DataModelNetworkElement)
	 */
	@Override
	public TreeMap<String, String> saveDataModelCustomized(DataModelNetworkElement networkElement) {
		networkElement.setDataModelBase64(null);
		// --- Nothing further to do here ---
		return networkElement.getDataModelStorageSettings();
	}
	
	
	/**
	 * Prints the specified error to console
	 * @param message the message
	 * @param isError the indicator if the message is an error
	 */
	private void printToConsole(String message, boolean isError) {
		if (message!=null && message.isEmpty()==false) {
			String prefix = "[" + this.getClass().getSimpleName() + "] ";
			if (isError==true) {
				System.err.println(prefix + message);
			} else {
				System.out.println(prefix + message);
			}
		}
	}
	
}
