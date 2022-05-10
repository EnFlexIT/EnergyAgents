package de.enflexit.ea.electricity.sensor.lvSensor;

import java.util.Vector;

import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.AbstractIOSimulated;
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
public class LVSensorAgent extends AbstractSensorAgent implements Observer {

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
			if (measurements!=null) {
				float currentL1New = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.CURRENT_L1)).getValue();
				float currentL2New = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.CURRENT_L2)).getValue();
				float currentL3New = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.CURRENT_L3)).getValue();

				float voltageL1New = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.VOLTAGE_L1)).getValue();
				float voltageL2New = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.VOLTAGE_L2)).getValue();
				float voltageL3New = (float) ((FixedDouble) newMeasurements.getVariable(InternalDataModel.VOLTAGE_L3)).getValue();

				FixedVariableList measurementHistory = this.internalDataModel.getMeasurementHistory();
				float currentL1History = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.CURRENT_L1)).getValue();
				float currentL2History = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.CURRENT_L2)).getValue();
				float currentL3History = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.CURRENT_L3)).getValue();

				float voltageL1History = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.VOLTAGE_L1)).getValue();
				float voltageL2History = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.VOLTAGE_L2)).getValue();
				float voltageL3History = (float) ((FixedDouble) measurementHistory.getVariable(InternalDataModel.VOLTAGE_L3)).getValue();

				// --- Calculating delta in percent
				Vector<Float> deltaVectorVoltage = new Vector<Float>();
				deltaVectorVoltage.add(calculateDeviationInPercent(voltageL1History, voltageL1New));
				deltaVectorVoltage.add(calculateDeviationInPercent(voltageL2History, voltageL2New));
				deltaVectorVoltage.add(calculateDeviationInPercent(voltageL3History, voltageL3New));

				Vector<Float> deltaVectorCurrent = new Vector<Float>();
				deltaVectorCurrent.add(calculateDeviationInPercent(currentL1History, currentL1New));
				deltaVectorCurrent.add(calculateDeviationInPercent(currentL2History, currentL2New));
				deltaVectorCurrent.add(calculateDeviationInPercent(currentL3History, currentL3New));

				// --- If max value is exceeded, a new measurement exists
				for (int a = 0; a < deltaVectorVoltage.size(); a++) {
					if (deltaVectorVoltage.get(a) > internalDataModel.dMaxVoltageDeviation || deltaVectorCurrent.get(a) > internalDataModel.dMaxCurrentDeviation) {
						newMeasurementDeltaDetected = true;
					}
				}
				// --- If new Measurement is detected, old measurement history will be overwritten
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
			float currentL1 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT_L1)).getValue();
			float currentL2 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT_L2)).getValue();
			float currentL3 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT_L3)).getValue();
			
			float voltageL1 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE_L1)).getValue();
			float voltageL2 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE_L2)).getValue();
			float voltageL3 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE_L3)).getValue();
			
			float cosPhiL1 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI_L1)).getValue();
			float cosPhiL2 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI_L2)).getValue();
			float cosPhiL3 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI_L3)).getValue();
			
			// --- add time and measured values to powerSensorData
			// ----------------------------
			LongValue longValue = new LongValue();
			longValue.setLongValue(currentTime);
			powerSensorData = new ElectricalMeasurement();
			powerSensorData.setTimeStamp(longValue);
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCurrent(new UnitValue(currentL1, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL2NodeStateNotNull().setCurrent(new UnitValue(currentL2, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL3NodeStateNotNull().setCurrent(new UnitValue(currentL3, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setVoltageAbs(new UnitValue(voltageL1, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL2NodeStateNotNull().setVoltageAbs(new UnitValue(voltageL2, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL3NodeStateNotNull().setVoltageAbs(new UnitValue(voltageL3, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCosPhi(cosPhiL1);
			powerSensorData.getElectricalNodeStateNotNull().getL2NodeStateNotNull().setCosPhi(cosPhiL2);
			powerSensorData.getElectricalNodeStateNotNull().getL3NodeStateNotNull().setCosPhi(cosPhiL3);
		}
		return powerSensorData;
	}
	
	/**
	 * Gets the internal data model of this agent.
	 * @return the internal data model
	 */
	@Override
	public InternalDataModel getInternalDataModel() {
		if (this.internalDataModel == null) {
			this.internalDataModel = new InternalDataModel(this);
			this.internalDataModel.getOptionModelController(); // Necessary to initialize the datamodel's controlledSystemType
			this.internalDataModel.addObserver(this);
		}
		return this.internalDataModel;
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
	@Override
	protected SensorSendingBehaviour getSensorSendingBehaviour() {
		if (sensorSending == null) {
			sensorSending = new SensorSendingBehaviour(this, this.getInternalDataModel(), this.getEnergyAgentIO());
		}
		return sensorSending;
	}
	
}
