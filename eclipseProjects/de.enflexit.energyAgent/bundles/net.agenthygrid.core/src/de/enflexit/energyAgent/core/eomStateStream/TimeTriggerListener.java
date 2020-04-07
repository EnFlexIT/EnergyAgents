package de.enflexit.energyAgent.core.eomStateStream;

/**
 * The listener interface for receiving timeTrigger events.
 * The class that is interested in processing a timeTrigger
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTimeTriggerListener<code> method. When
 * the timeTrigger event occurs, that object's appropriate
 * method is invoked.
 *
 * @see TimeTrigger
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public interface TimeTriggerListener {

	/**
	 * Will be invoked if the TimeTrigger fires for a time event. Optionally, you can also
	 * return the new next event time. Once you have returned a value <code>!=null</code>, a new 
	 * next event time has to be returned after every method call.
	 * You can switch back to regular operation by returning <code>null</code> as new next event time.  
	 * 
	 * @param triggerTime the trigger time
	 * @return the new next event time or null if you want to use the time that were specified by the constructor of the {@link TimeTrigger}
	 * @see TimeTrigger
	 */
	public Long fireTimeTrigger(long triggerTime);

	/**
	 * Will be invoked, if the TimeTrigger is finalized.
	 */
	public void setTimeTriggerFinalized();
	
}
