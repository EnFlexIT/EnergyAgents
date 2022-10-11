package de.enflexit.ea.core.planning;

/**
 * The listener interface for receiving planner events.
 * The class that is interested in processing a planner
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPlannerListener<code> method. When
 * the planner event occurs, that object's appropriate
 * method is invoked.
 *
 * @see PlannerEvent
 */
public interface PlannerListener {

	/**
	 * The method that informs about a {@link PlannerEvent}.
	 * @param plannerEvent the planner event
	 */
	public void onPlannerEvent(PlannerEvent plannerEvent);
	
}
