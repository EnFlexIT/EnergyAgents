package de.enflexit.ea.electricity.sensor.mvSensor;

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
			
			float current = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.CURRENT)).getValue();
			
			float voltage = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.VOLTAGE)).getValue();
			
			float cosPhi = (float) ((FixedDouble) measurements.getVariable(InternalDataModel.COSPHI)).getValue();
			
			
			ElectricalMeasurement powerSensorData = new ElectricalMeasurement();
			
			// --- add time and measured values to powerSensorData ---------------------------- 
			LongValue longValue = new LongValue();
			longValue.setLongValue(currentTime);
			powerSensorData.setTimeStamp(longValue);
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCurrent(new UnitValue(current, "A"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setVoltageAbs(new UnitValue(voltage, "V"));
			powerSensorData.getElectricalNodeStateNotNull().getL1NodeStateNotNull().setCosPhi(cosPhi);
			
			// --- Now, send this information to the derived GSE ---- 
			this.sendMessage2MainServer(powerSensorData);
			
		}
		this.block();
	}
	
}
