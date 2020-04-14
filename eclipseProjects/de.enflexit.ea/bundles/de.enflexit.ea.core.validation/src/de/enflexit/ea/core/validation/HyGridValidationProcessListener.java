package de.enflexit.ea.core.validation;

/**
 * The listener interface for the {@link HyGridValidationProcess} that allows receiving information
 * about the execution, the finalization and messages that were generated from process.
 *
 * @see HyGridValidationMessage
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
