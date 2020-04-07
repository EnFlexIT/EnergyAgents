package de.enflexit.energyAgent.ops;

/**
 * The Class OpsControllerEvent describes the Events that may be occur
 * with the {@link OpsController}.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class OpsControllerEvent {

	public enum OpsControllerEvents {
		OPS_CONNECTED,
		OPS_DISCONNECTED
	}
	
	private OpsControllerEvents event;
	
	/**
	 * Instantiates a new OPS controller event.
	 * @param event the event
	 */
	public OpsControllerEvent(OpsControllerEvents event) {
		this.event = event;
	}
	/**
	 * Returns the actual controller event.
	 * @return the event
	 */
	public OpsControllerEvents getControllerEvent() {
		return event;
	}
}
