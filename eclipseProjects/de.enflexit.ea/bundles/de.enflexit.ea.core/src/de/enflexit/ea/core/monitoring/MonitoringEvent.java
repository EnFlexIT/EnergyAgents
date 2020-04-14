package de.enflexit.ea.core.monitoring;

import java.util.EventObject;

import energy.FixedVariableList;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * Class for events triggered by monitoring behaviours.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 *
 */
public class MonitoringEvent extends EventObject {

	private static final long serialVersionUID = -6091024714439467599L;
	
	private TechnicalSystemStateEvaluation tsse;
	private FixedVariableList measurements;
	private FixedVariableList setpoints;
	private long eventTime;

	/**
	 * Instantiates a new monitoring event.
	 * @param source The event source
	 */
	public MonitoringEvent(Object source) {
		super(source);
	}

	/**
	 * Gets the {@link TechnicalSystemStateEvaluation} for this event.
	 * @return The the {@link TechnicalSystemStateEvaluation} for this event
	 */
	public TechnicalSystemStateEvaluation getTSSE() {
		return tsse;
	}

	/**
	 * Sets the{@link TechnicalSystemStateEvaluation} for this event.
	 * @param tsse The {@link TechnicalSystemStateEvaluation} for this event.
	 */
	public void setTsse(TechnicalSystemStateEvaluation tsse) {
		this.tsse = tsse;
	}

	/**
	 * Gets the measurements.
	 * @return the measurements
	 */
	public FixedVariableList getMeasurements() {
		return measurements;
	}

	/**
	 * Sets the measurements.
	 * @param measurements the new measurements
	 */
	public void setMeasurements(FixedVariableList measurements) {
		this.measurements = measurements;
	}

	/**
	 * Gets the setpoints.
	 * @return the setpoints
	 */
	public FixedVariableList getSetpoints() {
		return setpoints;
	}

	/**
	 * Sets the setpoints.
	 * @param setpoints the new setpoints
	 */
	public void setSetpoints(FixedVariableList setpoints) {
		this.setpoints = setpoints;
	}

	/**
	 * @return the eventTime
	 */
	public long getEventTime() {
		return eventTime;
	}

	/**
	 * @param eventTime the eventTime to set
	 */
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	
	

}
