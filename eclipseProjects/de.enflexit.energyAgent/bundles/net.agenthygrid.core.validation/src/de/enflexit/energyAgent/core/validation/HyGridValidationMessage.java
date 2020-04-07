package de.enflexit.energyAgent.core.validation;

/**
 * The Class HyGridValidationMessage.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class HyGridValidationMessage {

	public enum MessageType {
		Information,
		Warning,
		Error
	}
	
	private String message;
	private MessageType messageType = MessageType.Information;
	private String description;
	private String foundByClass;

	
	/**
	 * Instantiates a new HyGridValidationMessage.
	 *
	 * @param message the message
	 * @param messageType the message type
	 */
	public HyGridValidationMessage(String message, MessageType messageType) {
		this(message, messageType, null);
	}
	/**
	 * Instantiates a new HyGridValidationMessage.
	 *
	 * @param message the message
	 * @param messageType the message type
	 * @param description the description
	 */
	public HyGridValidationMessage(String message, MessageType messageType, String description) {
		this.setMessage(message);
		this.setMessageType(messageType);
		this.setDescription(description);
	}

	
	/**
	 * Returns the message.
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Sets the message.
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the message type.
	 * @return the message type
	 */
	public MessageType getMessageType() {
		return messageType;
	}
	/**
	 * Sets the message type.
	 * @param messageType the new message type
	 */
	public void setMessageType(MessageType messageType) {
		if (messageType!=null) {
			this.messageType = messageType;
		}
	}
	
	/**
	 * Gets the optional description of the message.
	 * @return the  description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the optional description of the message.
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the found by class.
	 * @return the found by class
	 */
	public String getFoundByClass() {
		return foundByClass;
	}
	/**
	 * Sets the found by class.
	 * @param foundByClass the new found by class
	 */
	public void setFoundByClass(String foundByClass) {
		this.foundByClass = foundByClass;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getMessageType().name() + ": " + this.getMessage();
	}
	
}
