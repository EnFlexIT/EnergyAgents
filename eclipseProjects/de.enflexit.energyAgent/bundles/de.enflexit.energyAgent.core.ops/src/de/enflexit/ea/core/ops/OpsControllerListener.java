package de.enflexit.ea.core.ops;

/**
 * The listener interface for receiving opsController events.
 * The class that is interested in processing a opsController
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addOpsControllerListener<code> method. When
 * the opsController event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 *
 * @see OpsControllerEvent
 */
public interface OpsControllerListener {

	/**
	 * On OPS controller event.
	 * @param controllerEvent the controller event
	 */
	public void onOpsControllerEvent(OpsControllerEvent controllerEvent);
	
}
