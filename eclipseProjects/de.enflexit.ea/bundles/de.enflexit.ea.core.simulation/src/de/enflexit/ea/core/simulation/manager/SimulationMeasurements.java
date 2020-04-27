package de.enflexit.ea.core.simulation.manager;

import java.util.HashMap;

/**
 * The Class SimulationMeasurements is used by the simulation manager to measure the simulation cycles.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimulationMeasurements {

	private HashMap<String, SimulationMeasurement> measurementHashMap;
	

	/**
	 * Returns the measurements HashMap.
	 * @return the measurement HashMap
	 */
	public HashMap<String, SimulationMeasurement> getMeasurementHashMap() {
		if (measurementHashMap==null) {
			measurementHashMap = new HashMap<>();
		}
		return measurementHashMap;
	}
	
	/**
	 * Adds a SimulationMeasurement to the know local measurements.
	 *
	 * @param taskDescriptor the task descriptor
	 * @return the simulation measurement added
	 */
	public SimulationMeasurement addSimulationMeasurement(String taskDescriptor) {
		return this.addSimulationMeasurement(taskDescriptor, null);
	}
	/**
	 * Adds a SimulationMeasurement to the know local measurements.
	 *
	 * @param taskDescriptor the task descriptor
	 * @param maxNumberForAverage the max number for average
	 * @return the simulation measurement added
	 */
	public SimulationMeasurement addSimulationMeasurement(String taskDescriptor, Integer maxNumberForAverage) {
		SimulationMeasurement simMeasure = null;
		if (this.getMeasurementHashMap().get(taskDescriptor)==null) {
			simMeasure = new SimulationMeasurement(taskDescriptor, maxNumberForAverage);
			this.getMeasurementHashMap().put(taskDescriptor, simMeasure);
		}
		return simMeasure;
	}

	/**
	 * Sets the specified measurement started.
	 * @param taskDescriptor the new measurement started
	 */
	public void setMeasurementStarted(String taskDescriptor) {
		
		SimulationMeasurement simMeasure = this.getMeasurementHashMap().get(taskDescriptor);
		if (simMeasure==null) {
			simMeasure = addSimulationMeasurement(taskDescriptor);
		}
		simMeasure.setMeasurementStarted();
	}
	/**
	 * Sets the specified measurement finalized.
	 * @param taskDescriptor the new measurement finalized
	 */
	public void setMeasurementFinalized(String taskDescriptor) {
		SimulationMeasurement simMeasure = this.getMeasurementHashMap().get(taskDescriptor);
		if (simMeasure!=null) {
			simMeasure.setMeasurementFinalized();
		}
	}
	/**
	 * Returns the measurement interval average for the specified task.
	 *
	 * @param taskDescriptor the task descriptor
	 * @return the measurement interval average
	 */
	public Long getMeasurementIntervalAverage(String taskDescriptor) {
		SimulationMeasurement simMeasure = this.getMeasurementHashMap().get(taskDescriptor);
		if (simMeasure!=null) {
			return simMeasure.getMeasurementIntervalAverage();
		}
		return null;
	}
	
	
}
