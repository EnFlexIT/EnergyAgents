package de.enflexit.energyAgent.core.aggregation.trafficLight;

import org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessage;
import org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessagePanel;

import de.enflexit.energyAgent.core.aggregation.trafficLight.TrafficLight.TrafficLightColor;

/**
 * The Class TrafficLightStateMessage.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class TrafficLightStateMessage extends GraphUIStateMessage {

	private static final long serialVersionUID = 7544177996464064574L;

	private String trafficLightName;
	private TrafficLightColor trafficLightColor;
	
	
	/**
	 * Instantiates a new traffic light state message.
	 */
	public TrafficLightStateMessage() {	}
	/**
	 * Instantiates a new traffic light state message with the current time stamp.
	 *
	 * @param trafficLightName the traffic light name
	 * @param trafficLightColor the traffic light color
	 */
	public TrafficLightStateMessage(String trafficLightName, TrafficLightColor trafficLightColor) {
		this(System.currentTimeMillis(), trafficLightName, trafficLightColor);
	}
	/**
	 * Instantiates a new traffic light state message.
	 *
	 * @param timeStamp the time stamp
	 * @param trafficLightName the traffic light name
	 * @param trafficLightColor the traffic light color
	 */
	public TrafficLightStateMessage(long timeStamp, String trafficLightName, TrafficLightColor trafficLightColor) {
		this.setTimeStamp(timeStamp);
		this.setTrafficLightName(trafficLightName);
		this.setTrafficLightColor(trafficLightColor);
	}
	
	/**
	 * Gets the traffic light name.
	 * @return the traffic light name
	 */
	public String getTrafficLightName() {
		return trafficLightName;
	}
	/**
	 * Sets the traffic light name.
	 * @param trafficLightName the new traffic light name
	 */
	public void setTrafficLightName(String trafficLightName) {
		this.trafficLightName = trafficLightName;
	}

	/**
	 * Gets the traffic light color.
	 * @return the traffic light color
	 */
	public TrafficLightColor getTrafficLightColor() {
		return trafficLightColor;
	}
	/**
	 * Sets the traffic light color.
	 * @param trafficLightColor the new traffic light color
	 */
	public void setTrafficLightColor(TrafficLightColor trafficLightColor) {
		this.trafficLightColor = trafficLightColor;
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessage#getMessage()
	 */
	@Override
	public String getMessage() {
		return "State of traffic light '" + this.getTrafficLightName() + "' changed to " + getTrafficLightColor().toString() + ".";
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.ui.messaging.GraphUIStateMessage#getVisualizationClass()
	 */
	@Override
	public Class<? extends GraphUIStateMessagePanel> getVisualizationClass() {
		return TrafficLightStatePanel.class;
	}

}
