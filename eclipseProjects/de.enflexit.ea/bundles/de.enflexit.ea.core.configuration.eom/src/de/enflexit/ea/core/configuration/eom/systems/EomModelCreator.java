package de.enflexit.ea.core.configuration.eom.systems;

import java.io.File;
import java.util.TreeMap;

import org.awb.env.networkModel.NetworkComponent;

import agentgui.core.project.Project;
import de.enflexit.ea.core.configuration.eom.BundleHelper;
import de.enflexit.ea.core.configuration.eom.EomSetupConfiguration;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;
import energy.optionModel.ControlledSystem;
import energy.optionModel.GroupMember;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;

/**
 * The Class EomModelCreator is used to produce new EOM models based on the specified parameters.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomModelCreator {

	private SystemConfiguration systemConfiguration;
	private String systemBlueprintID;
	private NetworkComponent networkComponent;
	
	private Object eomModel;
	private TreeMap<String, String> storageSettings;
	
	/**
	 * Instantiates a new EOM model creator.
	 *
	 * @param systemConfiguration the system configuration
	 * @param systemBlueprintID the system blueprint ID
	 * @param networkComponent the network component
	 */
	public EomModelCreator(SystemConfiguration systemConfiguration, String systemBlueprintID, NetworkComponent networkComponent) {
		this.systemConfiguration = systemConfiguration;
		this.systemBlueprintID = systemBlueprintID;
		this.networkComponent = networkComponent;
		this.createEomModel();
		this.createStorageSettings();
	}
	/**
	 * Creates the EOM model.
	 */
	private void createEomModel() {

		if (this.systemBlueprintID.equals(SystemConfiguration.NOT_CONFIGURED)==true) return;
		
		SystemBlueprint systemBlueprint = this.systemConfiguration.getSystemBlueprint(this.systemBlueprintID);
		if (this.systemConfiguration.requiresAggregation(systemBlueprint)==false) {
			// --- Just create a copy of the single EOM model ------- 
			EomSystem eomSystem = this.systemConfiguration.getEomSystem(systemBlueprint.getEomSystemIdList().get(0));
			this.eomModel = eomSystem.getDataModelCopy();
			
			// --- Adjust naming of component -------------------
			if (eomSystem.getEomModelType()==EomModelType.ScheduleList) {
				ScheduleList subSL = (ScheduleList) eomModel;
				subSL.setNetworkID(this.networkComponent.getId());
			}
			
		} else {
			// --- Find the aggregator first ------------------------
			EomSystem eomSystemTSG = this.systemConfiguration.getEomSystemTechnicalSystemGroup(systemBlueprint);
			TechnicalSystemGroup tsg = (TechnicalSystemGroup) eomSystemTSG.getDataModelCopy();
			if (tsg==null) return;
			
			// --- Adjust naming of component -----------------------
			tsg.getTechnicalSystem().setSystemID("Aggregation " + this.networkComponent.getId());
			tsg.getTechnicalSystem().setSystemDescription(systemBlueprintID);
			
			// --- Get the sub systems to integrate -----------------
			int subModelCounter = tsg.getGroupMember().size();
			for (String eomSystemID : systemBlueprint.getEomSystemIdList()) {
				
				EomSystem eomSystem = this.systemConfiguration.getEomSystem(eomSystemID);
				if (eomSystem.getEomModelType()==EomModelType.TechnicalSystemGroup) continue;
				
				Object eomModelCopy = eomSystem.getDataModelCopy();
				if (eomModelCopy==null) continue;
				
				// --- Adjust naming of component -------------------
				subModelCounter++;
				String subModelID = this.networkComponent.getId() + "_" + subModelCounter;
				
				ControlledSystem cs = new ControlledSystem();
				switch (eomSystem.getEomModelType()) {
				case TechnicalSystem:
					TechnicalSystem subTS = (TechnicalSystem) eomModelCopy;
					cs.setTechnicalSystem(subTS);
					break;
				case ScheduleList:
					ScheduleList subSL = (ScheduleList) eomModelCopy;
					subSL.setNetworkID(subModelID);
					cs.setTechnicalSystemSchedules(subSL);
					break;
				case TechnicalSystemGroup:
					TechnicalSystemGroup subTSG = (TechnicalSystemGroup) eomModelCopy;
					cs.setTechnicalSystemGroup(subTSG);
					break;
				}
				// --- Create new GroupMember and add to new TSG ----
				GroupMember gm = new GroupMember(); 
				gm.setControlledSystem(cs);
				gm.setNetworkID(subModelID);
				tsg.getGroupMember().add(gm);
				
			}
			this.eomModel = tsg;
		}
		
	}

	/**
	 * Creates the storage settings according to the locally created eom model.
	 */
	private void createStorageSettings() {
		
		if (this.eomModel==null) return;
		
		Project project = BundleHelper.getProject(); 
		File eomModelFile = EomDataModelStorageHandler.getFileSuggestion(project, this.networkComponent);
		String relativePath = EomDataModelStorageHandler.getRelativeProjectPath(project, eomModelFile);
		
		if (this.eomModel instanceof TechnicalSystem) {
			this.getStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.TechnicalSystem.toString());
		} else if (this.eomModel instanceof ScheduleList) {
			this.getStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.ScheduleList.toString());
		} else if (this.eomModel instanceof TechnicalSystemGroup) {
			this.getStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.TechnicalSystemGroup.toString());
		}
		this.getStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.File.toString());
		this.getStorageSettings().put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, relativePath);
	}
	
	/**
	 * Returns the locally created storage settings.
	 * @return the storage settings
	 */
	public TreeMap<String, String> getStorageSettings() {
		if (storageSettings==null) {
			storageSettings = new TreeMap<>();
		}
		return storageSettings;
	}
	
	
	/**
	 * Returns the EOM model created.
	 * @return the EOM model
	 */
	public Object getEomModel() {
		if (eomModel==null) {
			this.createEomModel();
		}
		return eomModel;
	}
	
}
