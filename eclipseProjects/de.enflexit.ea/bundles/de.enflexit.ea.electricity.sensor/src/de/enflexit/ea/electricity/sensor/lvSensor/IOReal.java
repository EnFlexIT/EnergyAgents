package de.enflexit.ea.electricity.sensor.lvSensor;

import de.enflexit.ea.core.EnergyAgentIO;

/**
 * The Class IOReal can be used for real measurements on physical hardware.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOReal extends de.enflexit.ea.electricity.sensor.IOReal implements EnergyAgentIO {

	private static final long serialVersionUID = 3659353219575016108L;
	
	/**
	 * Instantiates this behaviour.
	 * @param agent  the agent
	 */
	public IOReal(LVSensorAgent agent) {
		super(agent);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.sensor.IOReal#action()
	 */
	@Override
	public void action() {
		this.block();
	}
	
	/* (non-Javadoc)
	 * @see hygrid.modbusTCP.ModbusCommunicationListener#receivedNewRegisterValues(java.lang.Float[])
	 */
	@Override
	public void receivedNewRegisterValues(Float[] receivedRegisters) {
		this.newRegisterValues = receivedRegisters;
		this.restart();
	}

}
