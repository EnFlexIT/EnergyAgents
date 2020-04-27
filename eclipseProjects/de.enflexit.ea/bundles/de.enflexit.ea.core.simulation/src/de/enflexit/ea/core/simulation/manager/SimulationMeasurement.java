package de.enflexit.ea.core.simulation.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class SimulationMeasurement serves as as a container for a specific topic / task (e.g. a 'round trip time').
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimulationMeasurement {

	// --- Variables for the measurement ----------------
	
	private String taskDescriptor; 
	
	private long measurementStartTime = 0;
	private List<Long> measurementIntervalList;
	private long measurementIntervalAverage = 0;
	
	private int maxNumberForAverages = 16;
	private int startCounter;
	
	
	/**
	 * Instantiates a new simulation measurement.
	 * @param taskDescriptor the task descriptor
	 */
	public SimulationMeasurement(String taskDescriptor) {
		this(taskDescriptor, null);
	}
	/**
	 * Instantiates a new simulation measurement.
	 *
	 * @param taskDescriptor the task descriptor
	 * @param maxNumberForAverages the max number for averages
	 */
	public SimulationMeasurement(String taskDescriptor, Integer maxNumberForAverages) {
		this.taskDescriptor = taskDescriptor;
		if (maxNumberForAverages!=null && maxNumberForAverages>0) {
			this.maxNumberForAverages = maxNumberForAverages;
		}
	}
	
	/**
	 * Sets the measurement started.
	 */
	public void setMeasurementStarted() {
		this.measurementStartTime = System.currentTimeMillis();
		this.startCounter++;
	}
	/**
	 * Sets the measurement finalized and updates the average value.
	 */
	public void setMeasurementFinalized() {
		long duration = System.currentTimeMillis() - this.measurementStartTime;
		this.getMeasurementIntervalList().add(duration);
		this.updateMeasurementIntervalAverage();
		// --- Print current average? -----------
		if (this.startCounter==this.maxNumberForAverages) {
			this.startCounter = 0;
			System.out.println("[" + this.getClass().getSimpleName() + "][" + this.taskDescriptor + "] \tAverage time: " + this.getMeasurementIntervalAverage() + " ms");
		}
	}

	/**
	 * Return the list of provision interval list.
	 * @return the provision interval list
	 */
	private List<Long> getMeasurementIntervalList() {
		if (measurementIntervalList==null) {
			measurementIntervalList = new ArrayList<>();
		}
		return measurementIntervalList;
	}
	/**
	 * Updates the provision interval average.
	 */
	private void updateMeasurementIntervalAverage() {

		// --- Reduce number of durations -------
		while (this.getMeasurementIntervalList().size()>this.maxNumberForAverages) {
			this.getMeasurementIntervalList().remove(0);
		}
		// --- For the first measurement --------
		if (this.getMeasurementIntervalList().size()==1) {
			this.measurementIntervalAverage = this.getMeasurementIntervalList().get(0); 
			return;
		}
		// --- Summarize ------------------------
		long sum = 0;
		for (int i = 0; i < this.getMeasurementIntervalList().size(); i++) {
			sum += this.getMeasurementIntervalList().get(i);
		}
		// --- Calculate new average ------------
		this.measurementIntervalAverage = sum / this.getMeasurementIntervalList().size();
	}
	/**
	 * Returns the average measurement interval.
	 * @return the interval average of the measurement in ms
	 */
	public long getMeasurementIntervalAverage() {
		return measurementIntervalAverage;
	}
	
}
