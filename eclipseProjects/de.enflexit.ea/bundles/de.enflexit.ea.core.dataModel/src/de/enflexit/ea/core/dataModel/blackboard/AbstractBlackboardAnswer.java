package de.enflexit.ea.core.dataModel.blackboard;

import java.io.Serializable;

/**
 * The Class AbstratcBlackboardAnswer is used as a base for specific answers 
 * to {@link BlackboardRequest}.
 * 
 * @see Blackboard
 * @see BlackboardRequest
 * @see BlackboardAgent
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractBlackboardAnswer implements Serializable {

	private static final long serialVersionUID = 733219718427055777L;
	
	private int subConfigurationID;
	private String subConfigurationDomain;
	
	/**
	 * Gets the sub configuration ID.
	 * @return the sub configuration ID
	 */
	public int getSubConfigurationID() {
		return subConfigurationID;
	}
	
	/**
	 * Sets the sub configuration ID.
	 * @param subConfigurationID the new sub configuration ID
	 */
	public void setSubConfigurationID(int subConfigurationID) {
		this.subConfigurationID = subConfigurationID;
	}
	
	/**
	 * Gets the sub configuration domain.
	 * @return the sub configuration domain
	 */
	public String getSubConfigurationDomain() {
		return subConfigurationDomain;
	}
	
	/**
	 * Sets the sub configuration domain.
	 * @param domain the new sub configuration domain
	 */
	public void setSubConfigurationDomain(String domain) {
		this.subConfigurationDomain = domain;
	}
	
	

}
