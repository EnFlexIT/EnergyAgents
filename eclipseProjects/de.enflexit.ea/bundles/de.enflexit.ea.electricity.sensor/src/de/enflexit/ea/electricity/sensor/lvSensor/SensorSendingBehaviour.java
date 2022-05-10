package de.enflexit.ea.electricity.sensor.lvSensor;

import de.enflexit.common.Observer;
import de.enflexit.ea.core.EnergyAgentIO;
import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.ontology.LongValue;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import energy.FixedVariableList;
import energy.optionModel.FixedDouble;
import jade.core.Agent;

/**
 * The Class SensorSendingBehaviour.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SensorSendingBehaviour extends de.enflexit.ea.electricity.sensor.SensorSendingBehaviour implements Observer {

	private static final long serialVersionUID = 4378325659431942685L;
	
	/**
	 * Instantiates a new sensor sending behaviour.
	 *
	 * @param mySensorAgent the my sensor agent
	 * @param internalDataMode I/O-interface
	 */
	public SensorSendingBehaviour(Agent mySensorAgent, InternalDataModel internalDataModel, EnergyAgentIO ioInterface) {
		super(mySensorAgent, internalDataModel, ioInterface);
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		FixedVariableList measurements = this.internalDataModel.getMeasurementsFromSystem();
		if (measurements!=null && measurements.size()==9) {

			long currentTime = this.energyAgentIO.getTime();
			
			float currentL1 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT_L1)).getValue();
			float currentL2 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT_L2)).getValue();
			float currentL3 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT_L3)).getValue();
			
			float voltageL1 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE_L1)).getValue();
			float voltageL2 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE_L2)).getValue();
			float voltageL3 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE_L3)).getValue();
			
			float cosPhi1 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI_L1)).getValue();
			float cosPhi2 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI_L2)).getValue();
			float cosPhi3 = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI_L3)).getValue();
			
			
			ElectricalMeasurement powerSensorData = new ElectricalMeasurement();
			
			// --- add time and measured values to powerSensorData ---------------------------- 
			LongValue longValue = new LongValue();
			longValue.setLongValue(currentTime);
			powerSensorData.setTimeStamp(longValue);
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCurrent(new UnitValue(currentL1, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL2NodeStateNotNull().setCurrent(new UnitValue(currentL2, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL3NodeStateNotNull().setCurrent(new UnitValue(currentL3, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setVoltageAbs(new UnitValue(voltageL1, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL2NodeStateNotNull().setVoltageAbs(new UnitValue(voltageL2, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL3NodeStateNotNull().setVoltageAbs(new UnitValue(voltageL3, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCosPhi(cosPhi1);
			powerSensorData.getElectricalNodeStateNotNull().getL2NodeStateNotNull().setCosPhi(cosPhi2);
			powerSensorData.getElectricalNodeStateNotNull().getL3NodeStateNotNull().setCosPhi(cosPhi3);
			
			// --- Now, send this information to the derived GSE ---- 
			this.sendMessage2MainServer(powerSensorData);
			
		}
		this.block();
	}
	
}
