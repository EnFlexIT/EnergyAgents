package de.enflexit.energyAgent.core.validation;

/**
 * The listener interface for receiving hyGridValidationProcess events.
 * The class that is interested in processing a hyGridValidationProcess
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addHyGridValidationProcessListener<code> method. When
 * the hyGridValidationProcess event occurs, that object's appropriate
 * method is invoked.
 *
 * @see HyGridValidationProcessEvent
 */
public interface HyGridValidationProcessListener {

	/**
	 * Will be invoked right before the validation process is executed.
	 */
	public void processExecuted();
	
	/**
	 * Will be invoked if a HyGridValidationMessage message was received by the validation process.
	 * @param message the HyGridValidationMessage
	 */
	public void messageReceived(HyGridValidationMessage message);
	
	/**
	 * Will be invoked right after the validation process was finalized.
	 */
	public void processFinalized();
	
}
