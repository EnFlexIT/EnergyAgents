package de.enflexit.ea.topologies.pandaPower;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.enflexit.common.csv.CSV_FilePreview;
import de.enflexit.common.csv.CSV_FilePreview.CSV_FilePreviewSelection;
import de.enflexit.common.swing.OwnerDetection;
import de.enflexit.ea.topologies.BundleHelper;
import energy.EomControllerStorageSettings;
import energy.GlobalInfo;
import energy.optionModel.ScheduleList;
import energy.persistence.service.PersistenceConfigurator;
import energy.persistence.service.PersistenceHandler.PersistenceAction;
import energy.persistence.service.PersistenceServiceScheduleList;
import energy.schedule.ScheduleController;
import energy.schedule.loading.ScheduleTimeRange;

/**
 * The SimBenchDataLoadService is used to read ScheduelList data from SimBench csv files. 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PandaPowerScheduelListPersistenceService implements PersistenceServiceScheduleList {

	private PersistenceConfigurator persistenceConfigurator;
	private CSV_FilePreviewSelection csvSelection;
	private ScheduleTimeRange scheduleTimeRange;
	
	
	/**
	 * Creates a new SimBenchDataLoadService.
	 */
	public PandaPowerScheduelListPersistenceService() { }
	
	
	// --------------------------------------------------------------------------------------------
	// --- The visual elements EOM elements for the data handling ---------------------------------
	// --------------------------------------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see energy.persistence.service.PersistenceService#getJMenueItem(energy.persistence.service.PersistenceHandler.PersistenceAction)
	 */
	@Override
	public JMenuItem getJMenueItem(PersistenceAction action) {
		
		JMenuItem jMenuItem = null;
		switch (action) {
		case LOAD:
			jMenuItem = new JMenuItem();
			jMenuItem.setText("Open SimBench csv-Files");
			jMenuItem.setIcon(BundleHelper.getImageIcon("SimBenchData.png"));
			break;

		default:
			break;
		}
		return jMenuItem;
	}
	/* (non-Javadoc)
	 * @see energy.persistence.service.PersistenceService#getIndexPositionOfMenuItem()
	 */
	@Override
	public int getIndexPositionOfMenuItem() {
		return 2;
	}
	/* (non-Javadoc)
	 * @see energy.persistence.service.PersistenceService#setPersistenceConfigurator(energy.persistence.service.PersistenceConfigurator)
	 */
	@Override
	public void setPersistenceConfigurator(PersistenceConfigurator persistenceConfigurator) {
		this.persistenceConfigurator = persistenceConfigurator;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- The actual data loading method --------------------------------------------------------- 
	// --------------------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see energy.persistence.service.PersistenceServiceScheduleList#loadScheduleList(ScheduleController, ScheduleTimeRange, Component)
	 */
	@Override
	public ScheduleList loadScheduleList(ScheduleController sc, ScheduleTimeRange scheduleTimeRange, Component invoker) {
		
		// --------------------------------------------------------------------
		// --- Get a SimBench data file ---------------------------------------
		// --------------------------------------------------------------------
		File simBenchFileSelected = null;
		if (sc!=null && sc.isReloadScheduleList() && sc.getEomControllerStorageSettings().getCurrentFile()!=null && sc.getEomControllerStorageSettings().getCurrentFile().exists()) {
			String loadProfileFilePath = sc.getEomControllerStorageSettings().getCurrentFile().getAbsolutePath() + "/" + PandaPowerFileStore.SIMBENCH_LoadProfile;
			File loadProfileFile = new File(loadProfileFilePath); 
			if (loadProfileFile.exists()==true) {
				simBenchFileSelected = loadProfileFile;
			}
		} 
		// --- Get from user if not defined -----------------------------------
		if (simBenchFileSelected==null) {
			simBenchFileSelected = this.selectFile(sc, PersistenceAction.LOAD, invoker);
		}
		if (simBenchFileSelected==null) return null;
		
		// --------------------------------------------------------------------
		// --- Check for source file and row selection ------------------------
		// --------------------------------------------------------------------
		String sbFileSelection = null;
		int sbRowIndexSelection = -1;
		if (sc!=null && sc.isReloadScheduleList() && sc.getScheduleList().getDescription()!=null) {
			// --- ... get from load description ------------------------------
			try {
				String loadDescription = sc.getScheduleList().getDescription();
				String[] loadDescriptionArray = loadDescription.split("\\|");
				sbFileSelection = loadDescriptionArray[0];
				sbRowIndexSelection  = Integer.parseInt(loadDescriptionArray[1]);
				
			} catch (Exception ex) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error while reading load description.");
				sbFileSelection = null;
				sbRowIndexSelection  = -1;
			} 
		}
		
		// --------------------------------------------------------------------
		// --- Load ScheduleList ----------------------------------------------
		// --------------------------------------------------------------------
		ScheduleList scheduleList = this.loadScheduleList(simBenchFileSelected, sbFileSelection, sbRowIndexSelection, scheduleTimeRange);
		
		// --- Finally configure the ScheduleController -----------------------  
		sc.getEomControllerStorageSettings().setCurrentFile(simBenchFileSelected.getParentFile(), this.getClass());
		sc.setBase64EncodedModel(null);
		
		return scheduleList;
	}
	
	/**
	 * Provides an individual, SimBench specific load method 
	 * @param ppFileSelected any file out of the SimBench source directory (e.g ExternalNet.csv) 
	 * @param ppTable the source file name for the element identification (Node.csv or Load.csv) 
	 * @param ppTableRowIndexSelection the row index for element identification
	 * @param scheduleTimeRange the {@link ScheduleTimeRange} to apply during the load process 
	 * @return the ScheduelList as specified
	 */
	public ScheduleList loadScheduleList(File ppFileSelected, String ppTable, int ppTableRowIndexSelection, ScheduleTimeRange scheduleTimeRange) {
		
		// --------------------------------------------------------------------
		// --- Ensure that the SimBench model will be loaded ------------------
		// --------------------------------------------------------------------
		boolean showUserSelectionDialog = (ppTable==null && ppTableRowIndexSelection==-1);
		PandaPowerFileStore.getInstance().setPandaPowerDirectoryFile(ppFileSelected, showUserSelectionDialog);
		if (showUserSelectionDialog==true) {
			// --- ... let the user select ------------------------------------
			this.setScheduleTimeRange(scheduleTimeRange);
			this.doUserCsvSelection();
			if (this.getCsvSelection()==null) return null;
			
			ppTable     = this.getCsvSelection().getSelectedFile();
			ppTableRowIndexSelection = this.getCsvSelection().getSelectedModelRows()[0];
			scheduleTimeRange   = this.getScheduleTimeRange();
		}
		if (ppTable==null && ppTableRowIndexSelection==-1) return null;
		
		// --------------------------------------------------------------------
		// --- Load the ScheduleList from the SimBenchFileStoreReader ---------
		// --------------------------------------------------------------------
		ScheduleList scheduleList = null;
		PandaPowerFileStoreReader fileStoreReader = new PandaPowerFileStoreReader();
		if (ppTable.equals(PandaPowerFileStore.PANDA_Bus)==true) {
			scheduleList = fileStoreReader.getScheduleListByNode(ppTableRowIndexSelection, scheduleTimeRange);
		} else if (ppTable.equals(PandaPowerFileStore.PANDA_Load)==true) {
			scheduleList = fileStoreReader.getScheduleListByLoad(ppTableRowIndexSelection, scheduleTimeRange);
		}
		// --- Set the load description ---------------------------------------
		if (scheduleList!=null) scheduleList.setDescription(this.getLoadDescription(ppTable, ppTableRowIndexSelection));
		return scheduleList;
	}
	
	/**
	 * Returns the ScheduleList load description for the current ScheduleList
	 * @param sbFileName the SimBench file name that serves as base
	 * @param sbRowSelection the row number that was selected
	 * 
	 * @return the ScheduleList load description
	 */
	private String getLoadDescription(String sbFileName, int sbRowSelection) {
		return sbFileName + "|" + sbRowSelection;
	}
	

	private CSV_FilePreviewSelection getCsvSelection() {
		return csvSelection;
	}
	private  void setCsvSelection(CSV_FilePreviewSelection csvSelection) {
		this.csvSelection = csvSelection;
	}
	private ScheduleTimeRange getScheduleTimeRange() {
		return scheduleTimeRange;
	}
	private void setScheduleTimeRange(ScheduleTimeRange scheduleTimeRange) {
		this.scheduleTimeRange = scheduleTimeRange;
	}
	
	/**
	 * Return the customized {@link CSV_FilePreview} that enables the further proceeding
	 * @return the customized CSV_FilePreview with an added control panel  
	 */
	private void doUserCsvSelection() {
		
		// --- Get the current preview dialog ---------------------------------
		CSV_FilePreview preview = PandaPowerFileStore.getInstance().getCSVFilePreviewDialog();
		if (preview!=null) {
			// --- Set temporary invisible ------------------------------------
			preview.setVisible(false);
			// --- Create SimBenchProceedSelectionPanel -----------------------
			PandaPowerProceedSelectionPanel proceedPanel = new PandaPowerProceedSelectionPanel();
			proceedPanel.setScheduleTimeRange(this.getScheduleTimeRange());
			proceedPanel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					
					// --- Get the SimBenchProceedSelectionPanel --------------
					JButton button = (JButton) ae.getSource();
					PandaPowerProceedSelectionPanel proceedPanel = (PandaPowerProceedSelectionPanel) button.getParent();
					
					// --- Get the instance of the CSV_FilePreview ------------ 
					Dialog parentDialog = OwnerDetection.getOwnerDialogForComponent((JComponent) ae.getSource());
					if (parentDialog instanceof CSV_FilePreview) {
						// --- Get the preview dialog -------------------------
						CSV_FilePreview preview = (CSV_FilePreview) parentDialog;
						if (proceedPanel.isCanceled()==true) {
							preview.setVisible(false);
							return;
						}
						
						// --- Check the selection in the 'node' table --------
						CSV_FilePreviewSelection selection = preview.getSelection();
						// --- Do some error checks ---------------------------
						String title = "Missing selection";
						String message = "";
						String fileSelected = selection.getSelectedFile();
						if (message.isEmpty()==true && fileSelected.equals(PandaPowerFileStore.PANDA_Bus)==false && fileSelected.equals(PandaPowerFileStore.PANDA_Load)==false) {
							message = "Please, select the node (" + PandaPowerFileStore.PANDA_Bus + ") or the load (" + PandaPowerFileStore.PANDA_Load + ") that is to be imported!"; 
						}
						if (message.isEmpty()==true && (selection.getSelectedModelRows()==null || selection.getSelectedModelRows().length==0 )) {
							message = "Please, select the node or the load row that is to be imported!"; 
						}
						if (message.isEmpty()==true && selection.getSelectedModelRows().length>1) {
							message = "Please, only select a single row that is to be imported!"; 
						}
						if (message.isEmpty()==true && fileSelected.equals(PandaPowerFileStore.PANDA_Bus)==true && new PandaPowerFileStoreReader().isCableCabinetNodeSelection(selection.getSelectedModelRows()[0])) {
							message = "The row selected describes a cable cabinet and thus provides no further profile information!"; 
						}
						
						if (message.isEmpty()==false) {
							JOptionPane.showMessageDialog(preview, message, title, JOptionPane.WARNING_MESSAGE, null);
							return;
						}
						
						// --- Close the CSV_FilePreview ----------------------
						PandaPowerScheduelListPersistenceService.this.setCsvSelection(selection);
						PandaPowerScheduelListPersistenceService.this.setScheduleTimeRange(proceedPanel.getScheduleTimeRange());
						preview.setVisible(false);
					}
				}
			});
			// --- Add to CSV_FilePreview -------------------------------------
			preview.getContentPane().add(proceedPanel, BorderLayout.SOUTH);
			preview.validate();
			
			// --- Set focus to 'node' file -----------------------------------
			preview.setTabFocusToFile(PandaPowerFileStore.PANDA_Bus);
			
			// --- Set the dialog to appear modal -----------------------------
			preview.setModal(true);
			preview.setVisible(true);
			// - - Wait for user - - - - - - - - - - - - - - - - - - - - - - -
		}
	}
	
	
	/**
	 * Select file to load or save ScheduleList information.
	 *
	 * @param sc the current ScheduleController
	 * @param action the PersistenceAction
	 * @param parentComponent the parent component
	 * @return the selected file instance
	 */
	public File selectFile(ScheduleController sc, PersistenceAction action, Component parentComponent) {
		
		File fileSelected = null;
		File dirSelected = null;
		boolean isCreatedDirSelected = false; 

		// --- Consider a persistence configurator? -------
		if (this.persistenceConfigurator!=null) {
			// --- Get predefined settings ----------------
			HashMap<String, Object> predefinedSettings = this.persistenceConfigurator.getPersistenceSettingsBeforeUserInteraction(sc, action, this, parentComponent); 
			if (predefinedSettings!=null) {
				Object fileObject = predefinedSettings.get(PersistenceConfigurator.FILE_INSTANCE);
				if (fileObject!=null && fileObject instanceof File) {
					fileSelected = (File) fileObject;
				}
			}
		}
		
		// --- Allow user to select a file ----------------
		if (this.persistenceConfigurator==null || (this.persistenceConfigurator!=null && this.persistenceConfigurator.isAllowUserInteraction(sc, action, this)==true)) {
			// --- Some preparations ----------------------
			String caption = "";
			boolean allowNewFolder = true;
			switch (action) {
			case LOAD:
				caption = "Open File";
				allowNewFolder = false;
				break;
			case SAVE:
				caption = "Save File";
				break;
			case SAVE_AS:
				caption = "Save File As";
				break;
			}
			
			//---------------------------------------------
			// --- Create file choose instance ------------
			//---------------------------------------------
			FileFilter fileFilterCSV = new FileNameExtensionFilter("SimBench csv file", "csv");
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(GlobalInfo.getLastSelectedDirectory());
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(fileFilterCSV);
			fileChooser.setFileFilter(fileFilterCSV);
			if (allowNewFolder==false) this.disableNewFolderButton(fileChooser);	
			
			// --- Use predefined file selection? --------- 
			if (fileSelected!=null) {
				dirSelected = fileSelected.getParentFile();
				if (dirSelected.exists()==false) {
					dirSelected.mkdirs();
					isCreatedDirSelected = true;
				}
				fileChooser.setCurrentDirectory(dirSelected);
				fileChooser.setSelectedFile(fileSelected);	
			}
			
			// --- Show file selection dialog -------------
			int ret = fileChooser.showDialog(parentComponent, caption);
			// - - - - - - - - - - - - - - - - - - - - - -  
			
			if (ret == JFileChooser.APPROVE_OPTION) {
				fileSelected = fileChooser.getSelectedFile();
				String fileExtension = null;
				if (fileChooser.getFileFilter()==fileFilterCSV) {
					fileExtension = ".csv";
				}
				if (fileSelected.getAbsolutePath().toLowerCase().endsWith(fileExtension)==false) {
					fileSelected = new File(fileSelected.getAbsoluteFile() + fileExtension);	
				}
				GlobalInfo.setLastSelectedDirectory(fileChooser.getCurrentDirectory());
				
			} else {
				fileSelected = null;
				if (isCreatedDirSelected==true) {
					dirSelected.delete();
				}
			}
		}
		
		// --- Validate the selected file? ----------------
		if (fileSelected!=null && this.persistenceConfigurator!=null) {
			HashMap<String, Object> finalSettings = new HashMap<>();
			finalSettings.put(PersistenceConfigurator.FILE_INSTANCE, fileSelected);
			if (this.persistenceConfigurator.isValidPersistenceSettings(sc, action, this, parentComponent, finalSettings)==false) {
				fileSelected = null;
			}
		}
		return fileSelected;
	}
	/**
	 * Disable new folder button.
	 * @param checkContainer the container to check
	 */
	private void disableNewFolderButton(Container checkContainer) {
		int len = checkContainer.getComponentCount();
		for (int i = 0; i < len; i++) {
			Component comp = checkContainer.getComponent(i);
			if (comp instanceof JButton) {
				JButton button = (JButton) comp;
				Icon icon = button.getIcon();
				if (icon!=null && icon==UIManager.getIcon("FileChooser.newFolderIcon")) {
					button.setEnabled(false);
					return;
				}
			} else if (comp instanceof Container) {
				disableNewFolderButton((Container) comp);
			}
		}
	}
	
	
	
	
	// --------------------------------------------------------------------------------------------
	// --- The methods below are not required for this service - read only ------------------------ 
	// --------------------------------------------------------------------------------------------	
	@Override
	public boolean saveScheduleList(ScheduleController sc, ScheduleList slSave, Component invoker) {
		return false;
	}
	@Override
	public boolean saveScheduleListAs(ScheduleController sc, ScheduleList slSaveAs, Component invoker) {
		return false;
	}

	@Override
	public Object loadModelInstance(EomControllerStorageSettings eomStorageSettings) {
		// --- Not required / implemented yet ---
		return null;
	}

}
