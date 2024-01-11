package de.enflexit.ea.core.configuration.eom.scheduleList;

import java.io.File;
import java.util.List;

import de.enflexit.ea.core.configuration.SetupConfigurationAttribute;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;
import de.enflexit.ea.core.configuration.model.components.ConfigurableEomComponent;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import energy.EomControllerStorageSettings;
import energy.optionModel.GroupMember;
import energy.optionModel.TechnicalSystemGroup;

public class ScheduleListFile implements SetupConfigurationAttribute<String> {

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getColumnHeader()
	 */
	@Override
	public String getColumnHeader() {
		return "Schedule List File";
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
			return ((ConfigurableEomComponent)cComponent).getEomModelType() == EomModelType.ScheduleList;
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
				
				String fullPath = storageSettings.getCurrentFile().getPath();
				String fileName = fullPath.substring(fullPath.lastIndexOf(File.separator)+1);
				
				return fileName;
				
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

	@Override
	public void setValue(ConfigurableComponent cComponent, Object newValue) {
		// TODO Auto-generated method stub
		
	}

}
