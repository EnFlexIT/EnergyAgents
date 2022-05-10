package de.enflexit.ea.electricity.sensor.mvSensor;

import java.util.Vector;

import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.ontology.LongValue;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.electricity.sensor.AbstractSensorAgent;
import energy.FixedVariableList;
import energy.optionModel.FixedDouble;

/**
 * Represents a SensorAgent that measures current and voltage.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MVSensorAgent extends AbstractSensorAgent implements Observer {

	private static final long serialVersionUID = -5336718073413159211L;

	private InternalDataModel internalDataModel;
	private SensorSendingBehaviour sensorSending;

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#setupEnergyAgent()
	 */
	@Override
	protected void setupEnergyAgent() {

		// --- Add SubscriptionParticipant Behaviour ----------------
		this.addBehaviour(getInternalDataModel().getMeasurementSubscriptionResponder(this));
		this.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(MeasurementSubscriptionResponder.getMessageTemplate());

		// --- Add message sending behaviour ------------------------
		this.addBehaviour(getSensorSendingBehaviour());

		// --- Check, if operating mode is Real ---------------------
		if (this.getAgentOperatingMode() == AgentOperatingMode.TestBedReal || this.getAgentOperatingMode() == AgentOperatingMode.RealSystem) {

//			if (ModbusCommunicationInterface.getInstance().isAcquisitionRunning()==false) {
//				// --- Find ModbusGateway Component and set modbusConfiguration
//				Vector<NetworkComponent> netCompVector = this.getInternalDataModel().getNetworkModel().getNetworkComponentVectorSorted();
//				for (int i = 0; i < netCompVector.size(); i++) {
//					NetworkComponent networkComponent = netCompVector.get(i);
//					if (networkComponent.getType().equals("ModbusGateway")) {
//						
//						ModbusConfiguration modbusConfig = (ModbusConfiguration) networkComponent.getDataModel();
//						ModbusCommunicationInterface.getInstance().startAcquisition(modbusConfig);
//	
//					}
//				}
//			}
		}
	}
	
	/**
	 * Checking measurement delta.
	 * @return true, if successful
	 */
	@Override
	protected boolean checkingMeasurementDelta() {

		boolean newMeasurementDeltaDetected = true; // TODO: later = false!
		FixedVariableList newMeasurements = this.internalDataModel.getMeasurementsFromSystem();

		// --- Checking, if newMeasurement is first Measurement
		if (internalDataModel.getMeasurementHistory() != null) {
			// --- Checking, if measurement changes significant
			FixedVariableList measurements = this.internalDataModel.getMeasurementsFromSystem();
			if (measurements != null) {
				float currentNew = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.CURRENT)).getValue();

				float voltageNew = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.VOLTAGE)).getValue();
				
				FixedVariableList measurementHistory = this.internalDataModel.getMeasurementHistory();
				float currentHistory = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.CURRENT)).getValue();

				float voltageHistory = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.VOLTAGE)).getValue();

				// --- Calculating delta in percent
				Vector<Float> deltaVectorVoltage = new Vector<Float>();
				deltaVectorVoltage.add(calculateDeviationInPercent(voltageHistory, voltageNew));
				
				Vector<Float> deltaVectorCurrent = new Vector<Float>();
				deltaVectorCurrent.add(calculateDeviationInPercent(currentHistory, currentNew));

				// --- If max value is exceeded, a new measurement exists
				for (int a = 0; a < deltaVectorVoltage.size(); a++) {
					if (deltaVectorVoltage.get(a) > internalDataModel.dMaxVoltageDeviation) {
						newMeasurementDeltaDetected = true;

					}
					if (deltaVectorCurrent.get(a) > internalDataModel.dMaxCurrentDeviation) {
						newMeasurementDeltaDetected = true;
					}
				}
				// --- If new Measurement is detected, old measurement history will be
				// overwritten
				if (newMeasurementDeltaDetected == true) {
					internalDataModel.setMeasurementHistory(measurementHistory);
				}
			} else {

				// --- No History --> First Measurement
				internalDataModel.setMeasurementHistory(newMeasurements);
				newMeasurementDeltaDetected = true;
			}
		}
		return newMeasurementDeltaDetected;
	}
	
	/**
	 * Creates an {@link ElectricalMeasurement} from the sensor data stored in the internal data model
	 * @return the ElectricalMeasurement
	 */
	@Override
	protected ElectricalMeasurement createElectricalMeasurement() {
		ElectricalMeasurement powerSensorData = null;
		FixedVariableList measurements = this.internalDataModel.getMeasurementsFromSystem();
		if (measurements != null && measurements.size()==9) {
			
			long currentTime = this.getEnergyAgentIO().getTime();
			float current = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT)).getValue();
			
			float voltage = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE)).getValue();
			
			float cosPhi = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI)).getValue();
			
			// --- add time and measured values to powerSensorData
			// ----------------------------
			LongValue longValue = new LongValue();
			longValue.setLongValue(currentTime);
			powerSensorData = new ElectricalMeasurement();
			powerSensorData.setTimeStamp(longValue);
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCurrent(new UnitValue(current, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setVoltageAbs(new UnitValue(voltage, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCosPhi(cosPhi);
		}
		return powerSensorData;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#getIOSimulated()
	 */
	@Override
	public AbstractIOSimulated getIOSimulated() {
		return new IOSimulated(this);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#getIOReal()
	 */
	@Override
	public AbstractIOReal getIOReal() {
		return new IOReal(this);
	}
	
	/**
	 * Returns the sensor sending behaviour.
	 * @return the sensor sending behaviour
	 */
	protected SensorSendingBehaviour getSensorSendingBehaviour() {
		if (sensorSending == null) {
			sensorSending = new SensorSendingBehaviour(this, this.getInternalDataModel(), this.getEnergyAgentIO());
		}
		return sensorSending;
	}
	
	/**
	 * Gets the internal data model of this agent.
	 * @return the internal data model
	 */
	@Override
	public InternalDataModel getInternalDataModel() {
		if (this.internalDataModel == null) {
			this.internalDataModel = new InternalDataModel(this);
			this.internalDataModel.getOptionModelController(); // Necessary to initialize the datamodel's
																// controlledSystemType
			this.internalDataModel.addObserver(this);
		}
		return this.internalDataModel;
	}
	
}
