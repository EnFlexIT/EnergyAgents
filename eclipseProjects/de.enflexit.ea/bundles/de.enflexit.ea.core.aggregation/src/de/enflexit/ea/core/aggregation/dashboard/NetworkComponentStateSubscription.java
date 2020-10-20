package de.enflexit.ea.core.aggregation.dashboard;

import java.util.ArrayList;

/**
 * Dashboard subscription specifier for updates about the state of one or more network components.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class NetworkComponentStateSubscription extends AbstractDashboardSubscription {

	private static final long serialVersionUID = 5313240223611813840L;
	private ArrayList<String> subscriptionSpecifiers;
	
	private SubscriptionMode subscriptionMode;
	
	/**
	 * Possible subscription modes. Subscriptions can be made for network components with the specified IDs or of the specified types. 
	 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
	 */
	public enum SubscriptionMode {
		BY_TYPE, BY_ID
	}
	
	/**
	 * Gets the subscription mode.
	 * @return the subscription mode
	 */
	public SubscriptionMode getSubscriptionMode() {
		return subscriptionMode;
	}

	/**
	 * Sets the subscription mode.
	 * @param subscriptionMode the new subscription mode
	 */
	public void setSubscriptionMode(SubscriptionMode subscriptionMode) {
		this.subscriptionMode = subscriptionMode;
	}

	
	/**
	 * Gets the subscription specifiers, which are either component IDs or component types, depending on the subscription mode. 
	 * @return the subscription specifiers
	 */
	public ArrayList<String> getSubscriptionSpecifiers() {
		if (subscriptionSpecifiers==null) {
			subscriptionSpecifiers = new ArrayList<String>();
		}
		return subscriptionSpecifiers;
	}

	/**
	 * Sets the subscription specifiers, which are either component IDs or component types, depending on the subscription mode.
	 * @param subscriptionSpecifiers the new subscription specifiers
	 */
	public void setSubscriptionSpecifiers(ArrayList<String> subscriptionSpecifiers) {
		this.subscriptionSpecifiers = subscriptionSpecifiers;
	}

}
