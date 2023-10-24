package de.enflexit.ea.core.configuration.model;

import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import de.enflexit.ea.core.configuration.SetupConfigurationAttribute;
import de.enflexit.ea.core.configuration.SetupConfigurationService;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.SetupConfigurationServiceHelper;
import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;
import de.enflexit.ea.core.configuration.model.components.ConfigurableEomComponent;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import energy.optionModel.ControlledSystem;
import energy.optionModel.GroupMember;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;

/**
 * The Class ConfigurableComponentVector.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ConfigurableComponentVector extends Vector<ConfigurableComponent> {

	private static final long serialVersionUID = 8218251086643385948L;
	
	private GraphEnvironmentController graphController;
	private List<SetupConfigurationAttributeService> attributeServiceList; 

	/**
	 * Creates a new vector of {@link ConfigurableComponent}s.
	 * @param graphController the graph controller (<code>null</code> is not allowed
	 */
	public ConfigurableComponentVector(GraphEnvironmentController graphController) {
		this.graphController = graphController;
		if (graphController == null) {
			throw new NullPointerException("The GraphEnvironmentController to specify is not allowed to be null!");
		}
		if (this.getSetupConfigurationAttributeServiceList()!=null && this.getSetupConfigurationAttributeServiceList().size()>0) {
			this.collectConfigurableComponents();
		}
	}
	/**
	 * Returns the list of SetupConfigurationAttributeService currently defined by the {@link SetupConfigurationService}s.
	 * @return the setup configuration service attribute list
	 */
	public List<SetupConfigurationAttributeService> getSetupConfigurationAttributeServiceList() {
		if (attributeServiceList==null) {
			attributeServiceList = SetupConfigurationServiceHelper.getSetupConfigurationAttributeList(); 
		}
		return attributeServiceList;
	}
	
	/**
	 * Collects the {@link ConfigurableComponent}s.
	 */
	private void collectConfigurableComponents() {
		
		GeneralGraphSettings4MAS graphSettings = this.graphController.getGeneralGraphSettings4MAS();
		NetworkModel networkModel = this.graphController.getNetworkModel();

		// --- Check each NetworkComponent ------------------------------------
		for (NetworkComponent netComp : networkModel.getNetworkComponentVectorSorted()) {
		
			ComponentTypeSettings cts = graphSettings.getCurrentCTS().get(netComp.getType());

			// --- General check for the component ----------------------------
			ConfigurableComponent confComponent = new ConfigurableComponent(this.graphController, netComp, cts);
			this.addAfterCheckIfComponentWillBeConfigured(confComponent);
			
			// --- Check for an EOM model ------------------------------------- 
			if (this.isEomModel(netComp)==true) {
				
				ConfigurableEomComponent confEomComponent = null;
				switch (this.getEomModelType(netComp)) {
				case TechnicalSystem:
					confEomComponent = new ConfigurableEomComponent(this.graphController, netComp, cts, (TechnicalSystem)netComp.getDataModel());
					break;
				case ScheduleList:
					confEomComponent = new ConfigurableEomComponent(this.graphController, netComp, cts, (ScheduleList)netComp.getDataModel());
					break;
				case TechnicalSystemGroup:
					confEomComponent = new ConfigurableEomComponent(this.graphController, netComp, cts, (TechnicalSystemGroup)netComp.getDataModel());
					break;
				}
				this.addAfterCheckIfComponentWillBeConfigured(confEomComponent);

				// --- If we work on an aggregation, check sub systems -------- 
				if (this.getEomModelType(netComp)==EomModelType.TechnicalSystemGroup) {
					this.checkTechnicalSystemGroupsSubSystems(netComp, cts, (TechnicalSystemGroup)netComp.getDataModel());
				}
			} // end EOM
		} // end for
	}
	/**
	 * Applies check the subsystem of a TechnicalSystemGroup.
	 *
	 * @param netComp the current NetworkComponent
	 * @param cts the current ComponentTypeSettings
	 * @param tsg the current TechnicalSystemGroup
	 */
	private void checkTechnicalSystemGroupsSubSystems(NetworkComponent netComp, ComponentTypeSettings cts, TechnicalSystemGroup tsg) {
		
		// --- Check all GroupMember of the TechnicalSystemGroup ----
		for (GroupMember gm : tsg.getGroupMember()) {
			
			String networkID = gm.getNetworkID();
			ControlledSystem cs =  gm.getControlledSystem();
			
			ConfigurableEomComponent confEomComponent = null;
			if (cs.getTechnicalSystem()!=null) {
				confEomComponent = new ConfigurableEomComponent(this.graphController, netComp, cts, cs.getTechnicalSystem());
			}
			if (cs.getTechnicalSystemSchedules()!=null) {
				confEomComponent = new ConfigurableEomComponent(this.graphController, netComp, cts, cs.getTechnicalSystemSchedules());
			}
			if (cs.getTechnicalSystemGroup()!=null) {
				confEomComponent = new ConfigurableEomComponent(this.graphController, netComp, cts, cs.getTechnicalSystemGroup());
			}
			confEomComponent.setIsSubSystem(true);
			confEomComponent.setNetworkID(networkID);
			this.addAfterCheckIfComponentWillBeConfigured(confEomComponent);

			// --- Recursively work on an aggregation --------------- 
			if (cs.getTechnicalSystemGroup()!=null) {
				this.checkTechnicalSystemGroupsSubSystems(netComp, cts, cs.getTechnicalSystemGroup());
			}
		}
	}
	
	
	/**
	 * Does the actual check if the specified component will be configured by any of the 
	 * locally specified service attributes.
	 * @param confComp the ConfigurableComponent to check
	 */
	private void addAfterCheckIfComponentWillBeConfigured(ConfigurableComponent confComp) {
		if (this.willBeConfigured(confComp)==true) {
			this.add(confComp);
		}
	}
	/**
	 * Checks if the specified ConfigurableComponent will be configured by any of the locally specified service attribute.
	 *
	 * @param cComponent the ConfigurableComponent to check
	 * @return true, if successful
	 */
	private boolean willBeConfigured(ConfigurableComponent cComponent) {
		
		if (cComponent==null) return false;
		
		boolean willBeConfigured = false;
		for (SetupConfigurationAttributeService attributeService : this.getSetupConfigurationAttributeServiceList()) {

			SetupConfigurationAttribute<?> setupConfAttribute = attributeService.getSetupConfigurationAttribute(); 
			// --- Skip descriptions --------------------------------------------------------------
			if (setupConfAttribute==null) continue;
			
			try {
				// --- Ask, if the service attribute wants to configure the current component -----  
				willBeConfigured = setupConfAttribute.willBeConfigured(cComponent);
				if (willBeConfigured==true) {
					cComponent.addConfiguredBy(attributeService);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return willBeConfigured;
	}
	
	
	/**
	 * Checks if the specified NetworkComponent provides an EOM model.
	 *
	 * @param netComp the NetworkComponent to check
	 * @return true, if the current NetworkComponent contains an EOM model
	 */
	private boolean isEomModel(NetworkComponent netComp) {
		return this.getEomModelType(netComp)!=null;
	}
	/**
	 * Returns the EOM model type used with the specified NetworkComponent.
	 *
	 * @param netComp the NetworkComponent to check
	 * @return the EomModelType or <code>null</code>
	 */
	private EomDataModelStorageHandler.EomModelType getEomModelType(NetworkComponent netComp) {
		
		Object dataModel = netComp.getDataModel();
		if (dataModel instanceof TechnicalSystem) {
			return EomModelType.TechnicalSystem;
		} else if (dataModel instanceof ScheduleList) {
			return EomModelType.ScheduleList;
		} else if (dataModel instanceof TechnicalSystemGroup) {
			return EomModelType.TechnicalSystemGroup;
		}
		return null;
	}
	
}
