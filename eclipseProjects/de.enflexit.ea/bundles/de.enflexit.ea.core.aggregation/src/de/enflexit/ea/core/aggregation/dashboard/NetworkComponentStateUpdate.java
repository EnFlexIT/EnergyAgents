package de.enflexit.ea.core.aggregation.dashboard;

import java.io.Serializable;
import java.util.HashMap;

import de.enflexit.ea.core.dataModel.ontology.DynamicComponentState;

/**
 * The Class NetworkComponentStateUpdate.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class NetworkComponentStateUpdate implements Serializable {

	private static final long serialVersionUID = -9019095968310911981L;
	
	private HashMap<String, DynamicComponentState> componentStates;
	
	private long timestamp;

	/**
	 * Gets the timestamp.
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the component states.
	 * @return the component states
	 */
	public HashMap<String, DynamicComponentState> getComponentStates() {
		if (componentStates==null) {
			componentStates = new HashMap<String, DynamicComponentState>();
		}
		return componentStates;
	}

	/**
	 * Sets the component states.
	 * @param componentStates the component states
	 */
	public void setComponentStates(HashMap<String, DynamicComponentState> componentStates) {
		this.componentStates = componentStates;
	}

}
