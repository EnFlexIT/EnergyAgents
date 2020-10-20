package de.enflexit.ea.core.aggregation.dashboard;

import java.io.Serializable;

/**
 * Abstract base class for dashboard subscriptions
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 *
 */
public abstract class AbstractDashboardSubscription implements Serializable {
	private static final long serialVersionUID = 8900335102913706667L;
	
	String domain;

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
	
}
