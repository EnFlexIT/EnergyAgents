package de.enflexit.ea.core.testbed;

import java.io.Serializable;

import jade.core.AID;

/**
 * Container class for sending agent notifications as content of an ACL message
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 *
 */
public class AgentNotificationContainer implements Serializable {

	private static final long serialVersionUID = 4223516059571618077L;
	
	private AID notificationReceiver;
	private Object notification;
	
	
	/**
	 * Gets the notification receiver.
	 * @return the notification receiver
	 */
	public AID getReceiver() {
		return notificationReceiver;
	}
	
	/**
	 * Sets the notification receiver.
	 * @param receiver the new notification receiver
	 */
	public void setReceiver(AID receiver) {
		this.notificationReceiver = receiver;
	}
	
	/**
	 * Gets the notification content.
	 * @return the notification content
	 */
	public Object getNotification() {
		return notification;
	}
	
	/**
	 * Sets the notification content.
	 * @param notification the new notification content
	 */
	public void setNotification(Object notification) {
		this.notification = notification;
	}
	
	

}
