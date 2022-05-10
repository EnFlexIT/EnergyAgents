package de.enflexit.ea.electricity.sensor.validation;

import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * The Class checks for configuration errors of sensors.
 */
public class ValidateSensor extends HyGridValidationAdapter {

	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateNetworkComponent(org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public HyGridValidationMessage validateNetworkComponent(NetworkComponent netComp) {

		if (netComp.getType().equals("Sensor")==true) {
			
			String netCompID = netComp.getId();
			// --- Check if a data model is specified ---------------
			Object dm = netComp.getDataModel();
			if (dm==null || dm.getClass().isArray()==false) {
				return new HyGridValidationMessage("No data model was specified for Sensor '" + netCompID + "'!", MessageType.Error);
			
			} else {
				// --- Check for sensor data model ------------------
				Object[] dmArray = (Object[]) dm; 
				if (dmArray.length==0 || dmArray[0]==null || !(dmArray[0] instanceof SensorProperties)) {
					return new HyGridValidationMessage("No " + SensorProperties.class.getSimpleName() + " data model was specified for Sensor '" + netCompID + "'!", MessageType.Error);
					
				} else {
					// --- Check the sensor data model --------------
					SensorProperties dmSensor = (SensorProperties) dmArray[0];
					if (dmSensor.getSensorID()==null || dmSensor.getSensorID().isEmpty()==true) {
						return new HyGridValidationMessage("No Sensor-ID was specified in Sensor '" + netCompID + "' !", MessageType.Information);
					} else if (dmSensor.getMeasureLocation()==null || dmSensor.getMeasureLocation().isEmpty()==true) {
						return new HyGridValidationMessage("No measurement location was specified in Sensor '" + netCompID + "' !", MessageType.Error);
					}
					
					// --- Check the measurement location -----------
					String measureLocation = dmSensor.getMeasureLocation();
					HyGridValidationMessage locationCheck = this.checkMeasurementLocation(netComp, measureLocation);
					if (locationCheck!=null) return locationCheck;
					
					// --- Space for further check and improvements -
					// :-)
				}
			}
		}
		return null;
	}
	
	/**
	 * Check measurement location.
	 *
	 * @param netComp the NetworkComponent
	 * @param measureLocation the measure location
	 */
	private HyGridValidationMessage checkMeasurementLocation(NetworkComponent netComp, String measureLocation) {
		
		Vector<NetworkComponent> neighbourList = this.getNetworkModel().getNeighbourNetworkComponents(netComp);
		for (int i = 0; i < neighbourList.size(); i++) {
			NetworkComponent neighbour = neighbourList.get(i);
			if (neighbour.getId().equals(measureLocation)==true) {
				return null;
			}
		}
		return new HyGridValidationMessage("The measurement location (" + measureLocation + ") for Sensor '" + netComp.getId() + "' is not correct!", MessageType.Error);
	}
	
}
