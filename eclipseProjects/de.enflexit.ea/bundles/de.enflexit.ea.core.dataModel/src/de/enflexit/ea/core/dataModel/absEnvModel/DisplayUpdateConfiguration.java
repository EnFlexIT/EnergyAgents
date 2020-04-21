package de.enflexit.ea.core.dataModel.absEnvModel;

import java.io.Serializable;

/**
 * The Class DisplayUpdateConfiguration represents the 
 * data model for the configuration of the display updates.
 */
public class DisplayUpdateConfiguration implements Serializable {

	private static final long serialVersionUID = 6926658634997409670L;

	/**
	 * The possible UpdateMechanisms for the display of the current environment model.
	 */
	public static enum UpdateMechanism {
		EnableUpdates,
		DisableUpdates
	}

	private UpdateMechanism updateMechanism = UpdateMechanism.EnableUpdates;
	
	
	/**
	 * Instantiates a new display update configuration.
	 */
	public DisplayUpdateConfiguration() {

	}
	
	/**
	 * Sets the current update mechanism.
	 * @param updateMechanism the new update mechanism
	 */
	public void setUpdateMechanism(UpdateMechanism updateMechanism) {
		this.updateMechanism = updateMechanism;
	}
	/**
	 * Returns the current update mechanism.
	 * @return the update mechanism
	 */
	public UpdateMechanism getUpdateMechanism() {
		return updateMechanism;
	}
	
	/**
	 * Returns a copy of the current instance.
	 * @return the copy
	 */
	public DisplayUpdateConfiguration getCopy() {
		DisplayUpdateConfiguration copy = new DisplayUpdateConfiguration();
		copy.setUpdateMechanism(this.getUpdateMechanism());
		return copy;
	}

}
