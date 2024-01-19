package de.enflexit.ea.core.configuration.eom.scheduleList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import agentgui.core.application.Application;
import de.enflexit.ea.core.configuration.SetupConfigurationAttribute;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;
import de.enflexit.ea.core.configuration.model.components.ConfigurableEomComponent;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import energy.EomControllerStorageSettings;
import energy.optionModel.GroupMember;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemGroup;
import energy.persistence.ScheduleList_StorageHandler;

public class ScheduleListFile implements SetupConfigurationAttribute<String> {

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getColumnHeader()
	 */
	@Override
	public String getColumnHeader() {
		return "EOM Schedule List File";
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

	@Override
	public boolean willBeConfigured(ConfigurableComponent cComponent) {
		if (cComponent instanceof ConfigurableEomComponent) {
			// --- Remove when supporting single ScheduleLists ----------------
			if (((ConfigurableEomComponent)cComponent).isSubSystem()==true) {
				return ((ConfigurableEomComponent)cComponent).getEomModelType() == EomModelType.ScheduleList;
			}
		}
		return false;
	}

	@Override
	public String getValue(ConfigurableComponent cComponent) {
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
		} else {
			// Stand alone schedule list
		}
		return null;
	}
	
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
			
			ConfigurableEomComponent eomComponent = (ConfigurableEomComponent) cComponent;
			
			String fileNameString = (String) newValue;
			if (fileNameString.contains(File.separator)==false) {
				// --- File name only, assume default path ----------
				String relativeFilePath = ScheduleListConfigurationService.SCHEDULES_DEFAULT_SUBDIR + File.separator + fileNameString;
				File defaultAggregationFile = EomDataModelStorageHandler.getFileSuggestion(Application.getProjectFocused(), cComponent.getNetworkComponent());
				Path aggregationFolderPath = defaultAggregationFile.getParentFile().toPath();
				Path scheduleListPath = aggregationFolderPath.resolve(relativeFilePath);
				
				File scheduleListFile = scheduleListPath.toFile();
				if (scheduleListFile.exists()) {
					GroupMember groupMember = this.getScheduleListGroupMember(eomComponent);
					if (groupMember != null) {
						
						// --- Set the parent TSG to partitioned saving, if necessary -------------
						TechnicalSystemGroup parentTSG = this.getParentTechnicalSystemGroup(cComponent);
						if (parentTSG!=null && parentTSG.isPartitionedGroupModel()==false) {
							parentTSG.setPartitionedGroupModel(true);
						}
						
						EomControllerStorageSettings storageSettings = new EomControllerStorageSettings();
						storageSettings.setSaveGroupMemberModelAsLoaded(true);
						storageSettings.setCurrentFile(scheduleListFile, ScheduleList_StorageHandler.class);
						groupMember.getControlledSystem().getStorageSettings().clear();
						groupMember.getControlledSystem().getStorageSettings().addAll(storageSettings.toControlledSystemStorageSettings(aggregationFolderPath.toFile()));
						
						ScheduleList_StorageHandler slsh = new ScheduleList_StorageHandler();
						ScheduleList scheduleList = slsh.loadScheduleListFromCSVFile(scheduleListFile, null);
//						ScheduleList scheduleList = slsh.loadModelInstance(storageSettings);
						groupMember.getControlledSystem().setTechnicalSystemSchedules(scheduleList);
					}
				} else {
					System.err.println("[" + this.getClass().getSimpleName() + "] Schedule list file not found - expecting it at " + scheduleListFile.getPath());
				}
				
			}
		}
	}
	
	private TechnicalSystemGroup getParentTechnicalSystemGroup(ConfigurableComponent cComponent) {
		if (cComponent.getNetworkComponent().getDataModel() instanceof TechnicalSystemGroup) {
			return (TechnicalSystemGroup) cComponent.getNetworkComponent().getDataModel();
		} else {
			return null;
		}
	}

}
