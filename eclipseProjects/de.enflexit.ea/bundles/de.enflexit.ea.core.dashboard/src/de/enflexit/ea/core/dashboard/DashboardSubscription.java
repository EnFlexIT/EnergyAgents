package de.enflexit.ea.core.dashboard;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Abstract base class for dashboard subscriptions
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 *
 */
public class DashboardSubscription implements Serializable {
	private static final long serialVersionUID = 8900335102913706667L;
	
	/**
	 * Possible data objects to subscribe for.
	 * DOMAIN_DATAMODEL_STATE: The state part of the domain-specific ontology data model of the specified components, usually dataModelArray[1]
	 * CURRENT_TSSE: The current TechnicalSystemStateEvaluation of the specified components
	 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
	 */
	public enum SubscriptionFor {
		DOMAIN_DATAMODEL_STATE,
		CURRENT_TSSE
	}
	
	/**
	 * Possible options to specify which components are of interest.
	 * COMPONENT_TYPE: Get updates for all network components of the specified types
	 * COMPONENT_ID: Get updates only for the network components with the specified IDs 
	 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
	 */
	public enum SubscriptionBy {
		COMPONENT_TYPE, COMPONENT_ID
	}
	
	private String domain;
	private ArrayList<String> subscriptionSpecifiers;
	private SubscriptionFor subscriptionFor;
	private SubscriptionBy subscriptionBy;

	/**
	 * Gets the domain.
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Sets the domain.
	 * @param domain the new domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * Gets the subscription specifiers, which are either component IDs or component types, depending on the subscriptionBy setting. 
	 * @return the subscription specifiers
	 */
	public ArrayList<String> getSubscriptionSpecifiers() {
		if (subscriptionSpecifiers==null) {
			subscriptionSpecifiers = new ArrayList<String>();
		}
		return subscriptionSpecifiers;
	}

	/**
	 * Sets the subscription specifiers, which are either component IDs or component types, depending on the subscriptionBy setting.
	 * @param subscriptionSpecifiers the new subscription specifiers
	 */
	public void setSubscriptionSpecifiers(ArrayList<String> subscriptionSpecifiers) {
		this.subscriptionSpecifiers = subscriptionSpecifiers;
	}

	/**
	 * Gets the subscription for.
	 * @return the subscription for
	 */
	public SubscriptionFor getSubscriptionFor() {
		return subscriptionFor;
	}

	/**
	 * Specifies which data objects should be sent. See {@link SubscriptionFor}
	 * @param subscriptionFor the new subscription for
	 */
	public void setSubscriptionFor(SubscriptionFor subscriptionFor) {
		this.subscriptionFor = subscriptionFor;
	}

	/**
	 * Gets the subscription by.
	 * @return the subscription by
	 */
	public SubscriptionBy getSubscriptionBy() {
		return subscriptionBy;
	}

	/**
	 * Specifies how to identify the network components of interest. See {@link SubscriptionBy}
	 * @param subscriptionBy the new subscription by
	 */
	public void setSubscriptionBy(SubscriptionBy subscriptionBy) {
		this.subscriptionBy = subscriptionBy;
	}
	
	
	
}
