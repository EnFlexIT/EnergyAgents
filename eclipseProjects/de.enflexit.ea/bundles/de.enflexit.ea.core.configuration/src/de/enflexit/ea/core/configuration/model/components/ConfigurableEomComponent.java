package de.enflexit.ea.core.configuration.model.components;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.settings.ComponentTypeSettings;

import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import energy.OptionModelController;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.schedule.ScheduleController;
import energygroup.GroupController;

/**
 * The Class ConfigurableEomComponent serves as a ConfigurableComponent instance for EOM models
 * like a {@link TechnicalSystem}, a {@link ScheduleList} or a {@link TechnicalSystemGroup}.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ConfigurableEomComponent extends ConfigurableComponent {

	private EomDataModelStorageHandler.EomModelType eomModelType;
	
	private TechnicalSystem ts;
	private ScheduleList sl;
	private TechnicalSystemGroup tsg;

	private boolean isSubSystem;
	private String networkID;
	
	private OptionModelController omc;
	private ScheduleController sc;
	private GroupController gc;
	
	
	/**
	 * Instantiates a new configurable EOM component for a {@link TechnicalSystem}.
	 *
	 * @param graphController the graph controller
	 * @param netComp the net comp
	 * @param componentTypeSettings the component type settings
	 */
	public ConfigurableEomComponent(GraphEnvironmentController graphController, NetworkComponent netComp, ComponentTypeSettings componentTypeSettings, TechnicalSystem technicalSystem) {
		super(graphController, netComp, componentTypeSettings);
		this.ts = technicalSystem;
		this.setEomModelType(EomModelType.TechnicalSystem);
	}

	/**
	 * Instantiates a new configurable EOM component for a {@link ScheduleList}.
	 *
	 * @param graphController the graph controller
	 * @param netComp the net comp
	 * @param componentTypeSettings the component type settings
	 * @param scheduleList the schedule list
	 */
	public ConfigurableEomComponent(GraphEnvironmentController graphController, NetworkComponent netComp, ComponentTypeSettings componentTypeSettings, ScheduleList scheduleList) {
		super(graphController, netComp, componentTypeSettings);
		this.sl = scheduleList;
		this.setEomModelType(EomModelType.ScheduleList);
	}

	/**
	 * Instantiates a new configurable EOM component for a {@link TechnicalSystemGroup}.
	 *
	 * @param graphController the graph controller
	 * @param netComp the net comp
	 * @param componentTypeSettings the component type settings
	 * @param scheduleList the schedule list
	 */
	public ConfigurableEomComponent(GraphEnvironmentController graphController, NetworkComponent netComp, ComponentTypeSettings componentTypeSettings, TechnicalSystemGroup tsg) {
		super(graphController, netComp, componentTypeSettings);
		this.tsg = tsg;
		this.setEomModelType(EomModelType.TechnicalSystemGroup);
	}

	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.model.components.ConfigurableComponent#isEomModel()
	 */
	@Override
	public boolean isEomModel() {
		return true;
	}
	
	
	/**
	 * Sets the EomModelType.
	 * @param eomModelType the new EomModelType
	 */
	private void setEomModelType(EomDataModelStorageHandler.EomModelType eomModelType) {
		this.eomModelType = eomModelType;
	}
	/**
	 * Returns the current EomModelType.
	 * @return the EomModelType
	 */
	public EomDataModelStorageHandler.EomModelType getEomModelType() {
		return eomModelType;
	}
	
	
	/**
	 * Sets that the currently defined system is a sub system of an aggregation.
	 * @param isSubSystem the new checks if is sub system
	 */
	public void setIsSubSystem(boolean isSubSystem) {
		this.isSubSystem = isSubSystem;
	}
	/**
	 * Returns true, if the current EomModel is a subsystem.
	 * @return true, if is sub system
	 */
	public boolean isSubSystem() {
		return isSubSystem;
	}
	
	
	/**
	 * Sets the network ID.
	 * @param networkID the new network ID
	 */
	public void setNetworkID(String networkID) {
		this.networkID = networkID;
	}
	/**
	 * Returns the current network ID.
	 * @return the network ID
	 */
	public String getNetworkID() {
		if (networkID==null) {
			networkID = this.getNetworkComponent().getId();
		}
		return networkID;
	}
	
	
	/**
	 * Returns the TechnicalSystem if the specified EOM model is a TechnicalSystem.
	 * @return the technical system
	 * @see #getEomModelType()
	 */
	public TechnicalSystem getTechnicalSystem() {
		return this.ts;
	}
	/**
	 * Returns an OptionModelController that contains the local {@link TechnicalSystem}.
	 * @return the option model controller
	 */
	public OptionModelController getOptionModelController() {
		if (this.getEomModelType()==EomModelType.TechnicalSystem && omc==null) {
			omc = new OptionModelController();
			omc.setTechnicalSystem(this.getTechnicalSystem());
		}
		return omc;
	}
	
	
	/**
	 * Returns the ScheduleList if the specified EOM model is a ScheduleList.
	 * @return the ScheduleList
	 * @see #getEomModelType()
	 */
	public ScheduleList getScheduleList() {
		return sl;
	}
	/**
	 * Returns s ScheduleController that contains the local {@link ScheduleList}.
	 * @return the schedule controller
	 */
	public ScheduleController getScheduleController() {
		if (this.getEomModelType()==EomModelType.ScheduleList && sc==null) {
			sc = new ScheduleController();
			sc.setScheduleList(this.getScheduleList());
		}
		return sc;
	}
	
	
	/**
	 * Returns the TechnicalSystemGroup if the specified EOM model is a TechnicalSystemGroup.
	 * @return the technical system
	 * @see #getEomModelType()
	 */
	public TechnicalSystemGroup getTechnicalSystemGroup() {
		return tsg;
	}
	/**
	 * Returns a GroupController that contains the local {@link TechnicalSystemGroup}.
	 * @return the group controller
	 */
	public GroupController getGroupController() {
		if (this.getEomModelType()==EomModelType.TechnicalSystemGroup && gc==null) {
			gc = new GroupController();
			gc.setTechnicalSystemGroup(this.getTechnicalSystemGroup());
		}
		return gc;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.configuration.model.components.ConfigurableComponent#toString()
	 */
	@Override
	public String toString() {
		
		String description = super.toString();
		description += " - " + this.getNetworkID() + ":";
		description += "EOM.";

		switch (this.getEomModelType()) {
		case TechnicalSystem:
			description += "TS(" + this.getTechnicalSystem().getSystemID() + ")";
			break;
		case ScheduleList:
			description += "SL(" + this.getScheduleList().getSystemID() + ")";
			break;
		case TechnicalSystemGroup:
			description += "TSG(" + this.getTechnicalSystemGroup().getTechnicalSystem().getSystemID() + ")";
			break;
		}
		
		
		return description;
	}
	
	
}
