package de.enflexit.ea.electricity.sensor.mvSensor;

import de.enflexit.ea.core.EnergyAgentIO;

/**
 * The Class IOReal can be used for real measurements on physical hardware.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOReal extends de.enflexit.ea.electricity.sensor.IOReal implements EnergyAgentIO {

	private static final long serialVersionUID = 3659353219575016108L;
	
//	private static final long CYCLE_TIME_MS = 5000;
//	private static final long CYCLE_TIME_MS = 60000;
	
	private boolean debug = false;
	
	/**
	 * Instantiates this behaviour.
	 * @param agent  the agent
	 */
	public IOReal(MVSensorAgent agent) {
		super(agent);
	}
	
}
