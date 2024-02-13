package de.enflexit.ea.core.configuration.eom.scheduleList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.ComponentTypeSettings;

import agentgui.core.application.Application;
import agentgui.core.environment.EnvironmentController;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import de.enflexit.common.PathHandling;
import de.enflexit.common.ServiceFinder;
import de.enflexit.common.properties.Properties;
import de.enflexit.ea.core.configuration.SetupConfigurationAttribute;
import de.enflexit.ea.core.configuration.eom.EomSetupConfiguration;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;
import de.enflexit.ea.core.configuration.model.components.ConfigurableEomComponent;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;
import de.enflexit.eom.awb.adapter.csvScheduleStructure.ScheduleListCsvStructureFileService;
import energy.EomControllerStorageSettings;
import energy.optionModel.GroupMember;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemGroup;
import energy.persistence.ScheduleList_StorageHandler;

/**
 * {@link SetupConfigurationAttribute} for the configuration of schedule list files for
 * sub-systems of an {@link TechnicalSystemGroup}, or stand-alone {@link ScheduleList}s.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class ScheduleListFile implements SetupConfigurationAttribute<String> {
	
	private static final String PROPERTIES_KEY_SCHEDULES_FOLDER = "schedulesFolder";

	/**
	 * If just a file name is specified, it will be assumed to be located in this sub directory of the setup's EOM files directory.
	 */
	protected static final String SCHEDULES_DEFAULT_SUBDIR = "schedules";
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getColumnHeader()
	 */
	@Override
	public String getColumnHeader() {
		return "EOM b) Schedule List File";
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getDescription()
	 */
	@Override
	public String getDescription() {
		return "The file to load the schedule list from";
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getType()
	 */
	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getConfigurationOptions()
	 */
	@Override
	public List<String> getConfigurationOptions() {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#willBeConfigured(de.enflexit.ea.core.configuration.model.components.ConfigurableComponent)
	 */
	@Override
	public boolean willBeConfigured(ConfigurableComponent cComponent) {
		
		// --- Stand-alone schedule list ----------------------------
		if (cComponent.getNetworkComponent().getDataModel() instanceof ScheduleList) {
			if (cComponent instanceof ConfigurableEomComponent == false) {
				return true;
			}
		}
		
		// --- Aggregation sub-system with schedule list ------------
		if (cComponent instanceof ConfigurableEomComponent) {
			ConfigurableEomComponent eomComponent = (ConfigurableEomComponent) cComponent;
			if (eomComponent.isSubSystem()==true && eomComponent.getEomModelType() == EomModelType.ScheduleList) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getValue(de.enflexit.ea.core.configuration.model.components.ConfigurableComponent)
	 */
	@Override
	public String getValue(ConfigurableComponent cComponent) {
		
		if (cComponent instanceof ConfigurableEomComponent) {
			// --- Handle sub-systems of aggregations ---------------
			ConfigurableEomComponent eomComponent = (ConfigurableEomComponent) cComponent;
			if (eomComponent.isSubSystem()==true) {
				// Schedule list as a part of an aggregation
				GroupMember scheduleGroupMember = this.getScheduleListGroupMember(eomComponent);
				if (scheduleGroupMember!=null) {
					EomControllerStorageSettings storageSettings = new EomControllerStorageSettings();
					storageSettings.fromControlledSystemStorageSettings(null, scheduleGroupMember.getControlledSystem().getStorageSettings());
					
					if (storageSettings.getCurrentFile()!=null) {
						String fullPath = storageSettings.getCurrentFile().getPath();
						String fileName = fullPath.substring(fullPath.lastIndexOf(File.separator)+1);
						return fileName;
					}
					
				} else {
					System.err.println("[" + this.getClass().getSimpleName() + "] No group member found for " + eomComponent.getNetworkID());
				}
			}
		} else {
			// --- Handle stand-alone schedule lists ----------------
			TreeMap<String, String> storageSettings = cComponent.getNetworkComponent().getDataModelStorageSettings();
			String relativeFilePath =  PathHandling.getPathName4LocalOS(storageSettings.get(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION));
			if (relativeFilePath!=null && relativeFilePath.isEmpty()==false) {
				String fileName = relativeFilePath.substring(relativeFilePath.lastIndexOf(File.separator)+1);
				return fileName;
			}
		}
		return null;
	}
	
	/**
	 * Gets the (first) group member of an aggregation that contains a ScheduleList.
	 * @param eomComponent the eom component
	 * @return the schedule list group member
	 */
	private GroupMember getScheduleListGroupMember(ConfigurableEomComponent eomComponent) {
		TechnicalSystemGroup tsg = (TechnicalSystemGroup) eomComponent.getNetworkComponent().getDataModel();
		for (GroupMember groupMember : tsg.getGroupMember()) {
			if (groupMember.getNetworkID().equals(eomComponent.getNetworkID())) {
				return groupMember;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#setValue(de.enflexit.ea.core.configuration.model.components.ConfigurableComponent, java.lang.Object)
	 */
	@Override
	public void setValue(ConfigurableComponent cComponent, Object newValue) {
		
		if (newValue!=null && newValue instanceof String) {
			
			String fileNameString = (String) newValue;
			if (fileNameString.contains(File.separator)==false) {
				// --- File name only, assume default path ----------
				
				Path scheduleListPath = this.getSchedulesFolderPath().resolve(fileNameString);
				
				File scheduleListFile = scheduleListPath.toFile();
				
				if (scheduleListFile.exists()) {
					if (cComponent instanceof ConfigurableEomComponent) {
						// --- Handle for aggregation sub systems -------------
						ConfigurableEomComponent eomComponent = (ConfigurableEomComponent) cComponent;
						
						GroupMember groupMember = this.getScheduleListGroupMember(eomComponent);
						
						// --- Set the parent TSG to partitioned saving, if necessary -------------
						TechnicalSystemGroup parentTSG = this.getParentTechnicalSystemGroup(cComponent);
						if (parentTSG!=null && parentTSG.isPartitionedGroupModel()==false) {
							parentTSG.setPartitionedGroupModel(true);
						}
						
						File defaultAggregationFile = EomDataModelStorageHandler.getFileSuggestion(Application.getProjectFocused(), cComponent.getNetworkComponent());
						
						EomControllerStorageSettings storageSettings = new EomControllerStorageSettings();
						storageSettings.setSaveGroupMemberModelAsLoaded(true);
						storageSettings.setCurrentFile(scheduleListFile, ScheduleList_StorageHandler.class);
						
						File csvStructureFile = this.getCsvStructureFile(cComponent.getNetworkComponent(), scheduleListFile);
						if (csvStructureFile!=null && csvStructureFile.exists()) {
							storageSettings.setCsvStructureFile(csvStructureFile);
						}
						
						groupMember.getControlledSystem().getStorageSettings().clear();
						groupMember.getControlledSystem().getStorageSettings().addAll(storageSettings.toControlledSystemStorageSettings(defaultAggregationFile.getParentFile()));
						
						ScheduleList_StorageHandler slsh = new ScheduleList_StorageHandler();
						ScheduleList scheduleList = slsh.loadModelInstance(storageSettings);
						groupMember.getControlledSystem().setTechnicalSystemSchedules(scheduleList);
						
					} else {
						// --- Handle stand-alone schedule list ---------------
						Path projectFolderPath = new File(Application.getProjectFocused().getProjectFolderFullPath()).toPath();
						Path scheduleListRelativePath = projectFolderPath.relativize(scheduleListPath);
						
						// --- Get or create the storage settings TreeMap ----- 
						TreeMap<String, String> storageSettings = cComponent.getNetworkComponent().getDataModelStorageSettings();
						if (storageSettings==null) {
							storageSettings = new TreeMap<>();
						}
						
						// --- Remember the previously configured blueprint ID
						String blueprintID = storageSettings.get(EomSetupConfiguration.STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID); 
						
						storageSettings.clear();
						
						storageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.File.toString());
						storageSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, scheduleListRelativePath.toString());
						storageSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.ScheduleList.toString());
						
						
						// --- If a blueprint ID was set, set it again --------
						if (blueprintID!=null) {
							storageSettings.put(EomSetupConfiguration.STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID, blueprintID);
						}
						
						
						// --- Find and set the correct CSV structure file --------------
						File csvStructureFile = this.getCsvStructureFile(cComponent.getNetworkComponent(), scheduleListFile);
						if (csvStructureFile!=null && csvStructureFile.exists()) {
							storageSettings.put(EomDataModelStorageHandler.EOM_SETTING_CSV_SRTUCTURE_FILE, csvStructureFile.getName());
						}
						
						ScheduleList_StorageHandler slsh = new ScheduleList_StorageHandler();
						slsh.setCsvStructureFile(csvStructureFile);
						ScheduleList scheduleList = slsh.loadScheduleListFromCSVFile(scheduleListFile, null);
						cComponent.getNetworkComponent().setDataModel(scheduleList);
						
					}
					
				} else {
					System.err.println("[" + this.getClass().getSimpleName() + "] Schedule list file not found - expecting it at " + scheduleListFile.getPath());
				}
				
			}
		}
	}
	
	/**
	 * Gets the parent technical system group of a .
	 * @param cComponent the c component
	 * @return the parent technical system group
	 */
	private TechnicalSystemGroup getParentTechnicalSystemGroup(ConfigurableComponent cComponent) {
		if (cComponent.getNetworkComponent().getDataModel() instanceof TechnicalSystemGroup) {
			return (TechnicalSystemGroup) cComponent.getNetworkComponent().getDataModel();
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the correct csv structure file for the domain of a network component.
	 * @param networkComponent the network component
	 * @return the csv structure file
	 */
	private File getCsvStructureFile(NetworkComponent networkComponent, File csvScheduleFile) {
		
		GraphEnvironmentController graphEnvironmentController = this.getGraphEnvironmentController();
		
		if (graphEnvironmentController!=null) {
			// --- Get the domain for the network element ---------------------
			ComponentTypeSettings cts = this.getGraphEnvironmentController().getComponentTypeSettings().get(networkComponent.getType());
			String domain = cts.getDomain();
			
			// --- Get the list of available structure file providers --------- 
			List<ScheduleListCsvStructureFileService> structureFileServices = ServiceFinder.findServices(ScheduleListCsvStructureFileService.class, true);
			if (structureFileServices!=null) {
				
				
				// --- Use the first one that provides a matching file --------
				for (ScheduleListCsvStructureFileService structureFileService : structureFileServices) {
					String structureFileName = structureFileService.getCsvStructureFilePathForDomain(domain);
					if (structureFileName.contains(File.separator)==false) {
						Path scheduleFolderPath = csvScheduleFile.getParentFile().toPath();
						File structureFile = scheduleFolderPath.resolve(structureFileName).toFile();
						
						if (structureFile!=null && structureFile.exists()) {
							return structureFile;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets the graph environment controller.
	 * @return the graph environment controller
	 */
	private GraphEnvironmentController getGraphEnvironmentController() {
		EnvironmentController environmentController = Application.getProjectFocused().getEnvironmentController();
		if (environmentController instanceof GraphEnvironmentController) {
			return (GraphEnvironmentController) environmentController;
		} else {
			return null;
		}
	}
	
	private Path getSchedulesFolderPath() {
		Project currentProject = Application.getProjectFocused();
		SimulationSetup currentSetup = currentProject.getSimulationSetups().getCurrSimSetup();
		
		// --- Get the schedules folder from the setup properties, if configured --------
		Properties setupProperties = currentSetup.getProperties();
		String schedulesFolder = setupProperties.getStringValue(PROPERTIES_KEY_SCHEDULES_FOLDER);
		
		// --- If not found, check the project properties -------------------------------
		if (schedulesFolder==null) {
			Properties projectProperties = currentProject.getProperties();
			schedulesFolder = projectProperties.getStringValue(PROPERTIES_KEY_SCHEDULES_FOLDER);
		}
		
		Path schedulesFolderPath = null;
		if (schedulesFolder != null) {
			// --- If a folder was configured, create the corresponding full path -------
			Path projectFolderPath = new File(currentProject.getProjectFolderFullPath()).toPath();
			schedulesFolderPath = projectFolderPath.resolve(schedulesFolder);
		} else {
			// --- If still not found, use the default path -----------------------------
			String envPath = currentProject.getEnvSetupPath();
			String setupName = currentProject.getSimulationSetupCurrent();
			schedulesFolder = envPath + File.separator + setupName + File.separator + SCHEDULES_DEFAULT_SUBDIR;
			schedulesFolderPath = new File(schedulesFolder).toPath();
		}
		
		return schedulesFolderPath;
	}

}
