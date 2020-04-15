package de.enflexit.ea.core.dataModel.deployment;

import de.enflexit.ea.core.dataModel.deployment.SetupExtension.Changed;

/**
 * The listener interface for receiving setupExtension events.
 * The class that is interested in processing a setupExtension
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSetupExtensionListener<code> method. When
 * the setupExtension event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SetupExtensionEvent
 */
public interface SetupExtensionListener {

	/**
	 * Sets that the setup extension changed its settings.
	 * @param reason the reason for the change
	 */
	public void setSetupExtensionChanged(Changed reason);
	
}
