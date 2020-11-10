package de.enflexit.ea.core.dashboard;

import java.io.Serializable;
import java.util.HashMap;

import de.enflexit.ea.core.dashboard.DashboardSubscription.SubscriptionFor;
import de.enflexit.ea.core.dataModel.ontology.DynamicComponentState;

/**
 * The Class NetworkComponentStateUpdate.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DashboardUpdate implements Serializable {

	private static final long serialVersionUID = -9019095968310911981L;
	private long timestamp;
	private SubscriptionFor subscriptionFor;
	private HashMap<String, Object> updateObjects;

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
	 * Gets the subscription for.
	 * @return the subscription for
	 */
	public SubscriptionFor getSubscriptionFor() {
		return subscriptionFor;
	}

	/**
	 * Sets the subscription for.
	 * @param subscriptionFor the new subscription for
	 */
	public void setSubscriptionFor(SubscriptionFor subscriptionFor) {
		this.subscriptionFor = subscriptionFor;
	}

	/**
	 * Gets the update objects, which can be either TSSEs or DynamicComponentStates, depending on the subscriptionFr setting 
	 * @return the update objects
	 */
	public HashMap<String, Object> getUpdateObjects() {
		if (updateObjects==null) {
			updateObjects = new HashMap<String, Object>();
		}
		return updateObjects;
	}

	/**
	 * Sets the update objects, which can be either TSSEs or DynamicComponentStates, depending on the subscriptionFr setting
	 * @param updateObjects the update objects
	 */
	public void setUpdateObjects(HashMap<String, Object> updateObjects) {
		this.updateObjects = updateObjects;
	}

}
