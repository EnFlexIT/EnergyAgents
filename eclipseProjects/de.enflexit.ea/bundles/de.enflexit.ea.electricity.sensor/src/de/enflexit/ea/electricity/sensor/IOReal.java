package de.enflexit.ea.electricity.sensor;

import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.EnergyAgentIO;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import energy.FixedVariableList;

/**
 * The Class IOReal can be used for real measurements on physical hardware.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class IOReal extends AbstractIOReal implements EnergyAgentIO {

	private static final long serialVersionUID = 3659353219575016108L;
	
	private String sensorID;
	
	protected Float[] newRegisterValues;

	/**
	 * Instantiates this behaviour.
	 * @param agent  the agent
	 */
	public IOReal(AbstractSensorAgent agent) {
		super(agent);
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onStart()
	 */
	@Override
	public void onStart() {
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onEnd()
	 */
	@Override
	public int onEnd() {
		return super.onEnd();
	}

	/**
	 * Gets the sensor ID.
	 * @return the sensor ID
	 */
	protected String getSensorID() {
		if (sensorID==null) {
			// --- Get the sensor ID from the network component -----
			Object[] dataModel = (Object[]) this.getInternalDataModel().getNetworkComponent().getDataModel();
			SensorProperties sensor = (SensorProperties) dataModel[0];
			sensorID = sensor.getSensorID().trim();
		}
		return sensorID;
	}
	

	/**
	 * Cyclic behaviour to read modbus register 
	 */
	public void action() {
		this.block();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see smartHouse.agent.internalDataModel.AgentIO#getInputMeasurements()
	 */
	@Override
	public FixedVariableList getMeasurementsFromSystem() {
		return this.getInternalDataModel().getMeasurementsFromSystem();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#setMeasurementsFromSystem(de.enflexit.energyAgent.core.
	 * FixedVariableList)
	 */
	@Override
	public void setMeasurementsFromSystem(FixedVariableList newMeasurements) {
		this.getInternalDataModel().setMeasurementsFromSystem(newMeasurements);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * smartHouse.agent.internalDataModel.AgentIO#setOutputMeasurements(smartHouse.
	 * agent.internalDataModel.Measurements)
	 */
	@Override
	public void setSetPointsToSystem(FixedVariableList newOutputMeasurements) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getSetPointsToSystem()
	 */
	@Override
	public FixedVariableList getSetPointsToSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see hygrid.modbusTCP.ModbusCommunicationListener#receivedNewRegisterValues(java.lang.Float[])
	 */
	public void receivedNewRegisterValues(Float[] receivedRegisters) {
		this.newRegisterValues = receivedRegisters;
		this.restart();
	}

}
