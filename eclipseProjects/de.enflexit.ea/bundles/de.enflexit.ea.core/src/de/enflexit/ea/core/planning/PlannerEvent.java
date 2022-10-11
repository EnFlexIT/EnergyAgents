package de.enflexit.ea.core.planning;

/**
 * The Class PlannerEvent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class PlannerEvent {

	public enum PlannerEventType {
		Initialized
	}
	
	private PlannerEventType plannerEventType;
	private Object eventInstance;
	
	
	/**
	 * Instantiates a new planner event.
	 *
	 * @param plannerEventType the planner event type
	 */
	public PlannerEvent(PlannerEventType plannerEventType) {
		this(plannerEventType, null);
	}
	/**
	 * Instantiates a new planner event.
	 *
	 * @param plannerEventType the planner event type
	 * @param eventInstance the event instance
	 */
	public PlannerEvent(PlannerEventType plannerEventType, Object eventInstance) {
		this.setPlannerEventType(plannerEventType);
		this.setEventInstance(eventInstance);
	}

	/**
	 * Returns the planner event type.
	 * @return the planner event type
	 */
	public PlannerEventType getPlannerEventType() {
		return plannerEventType;
	}
	/**
	 * Sets the planner event type.
	 * @param plannerEventType the new planner event type
	 */
	public void setPlannerEventType(PlannerEventType plannerEventType) {
		this.plannerEventType = plannerEventType;
	}
	
	/**
	 * Returns the event instance.
	 * @return the event instance
	 */
	public Object getEventInstance() {
		return eventInstance;
	}
	/**
	 * Sets the event instance.
	 * @param eventInstance the new event instance
	 */
	public void setEventInstance(Object eventInstance) {
		this.eventInstance = eventInstance;
	}
	
}
