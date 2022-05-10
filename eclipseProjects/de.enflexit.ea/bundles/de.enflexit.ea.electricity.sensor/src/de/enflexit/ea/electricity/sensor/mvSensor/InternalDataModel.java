package de.enflexit.ea.electricity.sensor.mvSensor;

import de.enflexit.ea.core.AbstractEnergyAgent;
import jade.core.Agent;

// TODO: Auto-generated Javadoc
/**
 * The Class InternalDataModel represents the whole internal data model of the corresponding agent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class InternalDataModel extends de.enflexit.ea.electricity.sensor.InternalDataModel {

	private static final long serialVersionUID = 8589606262871989270L;
	
	/** Specification of the measurement names */
	protected final static String VOLTAGE = "Voltage";
	
	protected final static String CURRENT = "Current";
	
	protected final static String COSPHI = "Cos Phi";
	
	public static final String V= "V";
	
	public static final String C= "C";
	
	public static final String PF= "PF";
	
	/**
	 * Instantiates a new data model.
	 * @param myAgent the my agent
	 */
	public InternalDataModel(AbstractEnergyAgent myAgent) {
		super(myAgent);
	}
	
	/**
	 * Gets the measurement subscription responder.
	 * @param myAgent the agent
	 * @return the measurement subscription responder
	 */
	public MeasurementSubscriptionResponder getMeasurementSubscriptionResponder(Agent myAgent) {
		if (measurementSubscriptionResponder == null) {
			measurementSubscriptionResponder = new MeasurementSubscriptionResponder(myAgent);
		}
		return (MeasurementSubscriptionResponder) measurementSubscriptionResponder;
	}
	
}
