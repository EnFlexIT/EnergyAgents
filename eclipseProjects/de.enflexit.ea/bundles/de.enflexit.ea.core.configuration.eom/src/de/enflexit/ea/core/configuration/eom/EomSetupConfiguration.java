package de.enflexit.ea.core.configuration.eom;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;

import de.enflexit.ea.core.awbIntegration.adapter.EnergyAgentAdapter;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeWithUI;
import de.enflexit.ea.core.configuration.eom.systems.EomModelCreator;
import de.enflexit.ea.core.configuration.eom.systems.SystemConfigurationManager;
import de.enflexit.ea.core.configuration.eom.systems.ui.SystemConfigurationPanel;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;
import de.enflexit.eom.awb.adapter.EomAdapter;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;

/**
 * The Class EomSetupConfiguration.
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EomSetupConfiguration implements SetupConfigurationAttributeWithUI<String> {

	private static final String STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID = "Blueprint-ID";
	
	private List<String> eomAdapterClassList;

	private SystemConfigurationManager systemConfigurationManager;
	private SystemConfigurationPanel systemConfigurationPanel;
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getAttributeName()
	 */
	@Override
	public String getColumnHeader() {
		return "EOM Setup Type";
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getDescription()
	 */
	@Override
	public String getDescription() {
		return "The EOM SystemBlueprint for the current system";
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getType()
	 */
	@Override
	public Class<String> getType() {
		return String.class;
	}
	
	/**
	 * Returns the SystemConfigurationManager.
	 * @return the system configuration manager
	 */
	public SystemConfigurationManager getSystemConfigurationManager() {
		if (systemConfigurationManager==null) {
			systemConfigurationManager = new SystemConfigurationManager();
		}
		return systemConfigurationManager;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttributeWithUI#getAttributeConfigurationPanel()
	 */
	@Override
	public JComponent getAttributeConfigurationPanel() {
		if (systemConfigurationPanel==null) {
			systemConfigurationPanel = new SystemConfigurationPanel(this.getSystemConfigurationManager());
		}
		return systemConfigurationPanel;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttributeWithUI#disposeAttributeConfigurationPanel()
	 */
	@Override
	public void disposeAttributeConfigurationPanel() {
		this.systemConfigurationPanel = null;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getConfigurationOptions()
	 */
	@Override
	public List<String> getConfigurationOptions() {
		return this.getSystemConfigurationManager().getSystemConfiguration().getConfigurationOptions();
	}

	/**
	 * Returns the list of {@link NetworkComponentAdapter} that are to be used for an EOM model configuration.
	 * @return the EOM adapter class list
	 */
	private List<String> getEomAdapterClassList() {
		if (eomAdapterClassList==null) {
			eomAdapterClassList = new ArrayList<>();
			eomAdapterClassList.add(EnergyAgentAdapter.class.getName());
			eomAdapterClassList.add(EomAdapter.class.getName());
		}
		return eomAdapterClassList;
	}
	/**
	 * Checks if the specified class is a relevant EOM adapter.
	 *
	 * @param className the class name to check
	 * @return true, if is eom adapter
	 */
	private boolean isEomAdapter(String className) {
		return this.getEomAdapterClassList().contains(className);
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#willBeConfigured(de.enflexit.ea.core.configuration.model.components.ConfigurableComponent)
	 */
	@Override
	public boolean willBeConfigured(ConfigurableComponent cComponent) {
		if (cComponent.isEomModel()==true) return false;
		return this.isEomAdapter(cComponent.getComponentTypeSettings().getAdapterClass());
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#getValue(de.enflexit.ea.core.configuration.model.components.ConfigurableComponent)
	 */
	@Override
	public String getValue(ConfigurableComponent cComponent) {
		
		// --- Check the assigned value in the NetworkComponents storage settings -------
		NetworkComponent netComp = cComponent.getNetworkComponent();
		String systemBlueprintID = netComp.getDataModelStorageSettings()!=null ? netComp.getDataModelStorageSettings().get(STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID) : null;
		if (systemBlueprintID==null || this.getConfigurationOptions().contains(systemBlueprintID)==false) {
			systemBlueprintID = this.getConfigurationOptions().get(0);
		}
		return systemBlueprintID;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.SetupConfigurationAttribute#setValue(de.enflexit.ea.core.configuration.model.components.ConfigurableComponent, java.lang.Object)
	 */
	@Override
	public void setValue(ConfigurableComponent cComponent, Object newValue) {
		
		if (newValue==null) return;
		String systemBlueprintID = (String) newValue;
		if (systemBlueprintID==null || this.getConfigurationOptions().contains(systemBlueprintID)==false) {
			// --- Nothing to do in this case ---------------------------------
			return;
		}
		
		// --- Check if NetworkComponent already contains that blueprint ------ 
		NetworkComponent netComp = cComponent.getNetworkComponent();
		if (netComp.getDataModel()!=null && 
			netComp.getDataModelStorageSettings()!=null && 
			netComp.getDataModelStorageSettings().get(STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID)!=null &&
			netComp.getDataModelStorageSettings().get(STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID).equals(systemBlueprintID)==true) {
			// --- Blueprint was already assigned to the NetworkComponent -----
			return;
		}
		
		// --- Create a new EOM model system based on the blueprint -----------
		EomModelCreator eomModelCreator = new EomModelCreator(this.getSystemConfigurationManager().getSystemConfiguration(), systemBlueprintID, netComp);
		netComp.setDataModel(eomModelCreator.getEomModel());
		netComp.setDataModelStorageSettings(eomModelCreator.getStorageSettings());
		
		// --- Save the EOM file ----------------------------------------------
		new EomDataModelStorageHandler(null).saveDataModel(netComp);
		
		// --- Finally remind the SystemBlueprint ID -------------------------- 
		netComp.getDataModelStorageSettings().put(STORAGE_SETTINGS_KEY_SYSTEM_BLUEPRINT_ID, systemBlueprintID);
		
	}
}
