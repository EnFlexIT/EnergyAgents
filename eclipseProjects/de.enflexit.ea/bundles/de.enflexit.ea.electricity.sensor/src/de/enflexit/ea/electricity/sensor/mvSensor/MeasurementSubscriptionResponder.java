package de.enflexit.ea.electricity.sensor.mvSensor;

import jade.core.Agent;

class MeasurementSubscriptionResponder extends de.enflexit.ea.electricity.sensor.MeasurementSubscriptionResponder {
	
	private static final long serialVersionUID = -2369120728349222744L;

	boolean debug= false;
	
	/**
	 * Instantiates a new measurement subscription responder.
	 *
	 * @param agent the agent
	 */
	MeasurementSubscriptionResponder(Agent agent) {
        super(agent);
    }
	
}
