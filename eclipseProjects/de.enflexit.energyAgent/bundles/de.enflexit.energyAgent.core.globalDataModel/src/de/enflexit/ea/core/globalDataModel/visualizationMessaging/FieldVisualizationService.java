package de.enflexit.ea.core.globalDataModel.visualizationMessaging;

import jade.core.AID;

/**
 * The Interface FieldVisualizationService.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public interface FieldVisualizationService {

	/**
	 * Has to return the central executive agents AID.
	 * @return the central executive agent AID
	 */
	public AID getCeaAID();

	/**
	 * Sets the status bar message text.
	 * @param statusText the new status bar message
	 */
	public void setStatusBarMessage(String statusText);
	
	/**
	 * Has to set the status bar text message to 'Ready'.
	 */
	public void setStatusBarMessageReady();
	
	
}
