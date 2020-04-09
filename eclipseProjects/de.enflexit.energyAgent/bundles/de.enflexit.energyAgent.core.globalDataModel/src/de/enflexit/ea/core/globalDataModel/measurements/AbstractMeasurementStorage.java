package de.enflexit.ea.core.globalDataModel.measurements;

import java.util.HashMap;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;

import de.enflexit.ea.core.globalDataModel.ontology.Measurement;

/**
 * The Class AbstractMeasurementStorage can be extended in order to handle a storage of measurement series.
 * 
 * @author Sebastian Toersleff - Institute for Automation Technology - Helmut Schmidt University
 */
public abstract class AbstractMeasurementStorage<T> extends Observable {
	
	private HashMap<String, AbstractMeasurementSeries<T>> measurementStorage;

	private Long storageDuration;
	private Long measurementInterval; 
	private boolean normalizeMeasurementTimeStamp; 
	private boolean isInitializing = true; 
	
	/**
	 * Instantiates a new abstract measurement storage.
	 *
	 * @param storageDurationInMinutes the storage duration in minutes
	 * @param measurementIntervalInSeconds the measurement interval in seconds
	 */
	public AbstractMeasurementStorage(int storageDurationInMinutes, int measurementIntervalInSeconds, boolean normalizeMeasurementTimeStamp) {
		this.storageDuration = (Long) (long) storageDurationInMinutes*60*1000; 
		this.measurementInterval = (Long) (long) measurementIntervalInSeconds*1000; 
		this.normalizeMeasurementTimeStamp = normalizeMeasurementTimeStamp;
	}

	/**
	 * Gets the measurement storage.
	 * @return the measurement storage
	 */
	public HashMap<String, AbstractMeasurementSeries<T>> getMeasurementStorage() {
		if (measurementStorage==null) {
			measurementStorage = new HashMap<>();
		}
		return measurementStorage;
	}

	
	/**
	 * Has to convert the specified measurement of type to a double array.
	 * @param measurement the measurement
	 * @return the double[]
	 */
	public abstract Double[] convertMeasurementToRowArray(T measurement);
	
	
	/**
	 * Has to convert the specified array into a measurement.
	 * @param arrayToConvert the array to convert
	 * @return the measurement of type T
	 */
	public abstract T convertRowArrayToMeasurement(Double[] arrayToConvert);
	
	
	/**
	 * Adds the measurement.
	 *
	 * @param id the id
	 * @param newMeasurement the new measurement
	 */
	public void addMeasurement(String id, T newMeasurement) {
		AbstractMeasurementSeries<T> series = this.getMeasurementStorage().get(id);
		if (series==null) {
			series = new MeasurementSeries(this.storageDuration);
			this.getMeasurementStorage().put(id, series);
		} else {
			this.isInitializing = false; 
		}
		Long timeStamp = ((Measurement) newMeasurement).getTimeStamp().getLongValue(); 
		if (normalizeMeasurementTimeStamp) {
			timeStamp = this.normalizeMeasurementTime(timeStamp);
		}
		series.addMeasurement(timeStamp, newMeasurement);
	}
	
	
	/**
	 * Normalize measurement time.
	 *
	 * @param timeStamp the time stamp
	 * @return the long
	 */
	protected long normalizeMeasurementTime(long timeStamp) {
		return ((timeStamp + this.measurementInterval/2)/this.measurementInterval)*this.measurementInterval;
	}
	
	
	/**
	 * Compares the number of measurements available for the specified time to the size of the container. 
	 * Should only be used if normalizeMeasurementTimeStamp is set to true.
	 * 
	 * @param time
	 * @return
	 */
	protected boolean isMeasurementsCompleteAtTime(long time) {
		int numberOfMeasurements = 0; 
		Set<String> keys = this.getMeasurementStorage().keySet();
 		for (String key : keys) {
			if (this.getMeasurementStorage().get(key).hasMeasurementAtTime(time)) {
				numberOfMeasurements++;
			}
		} 
		return numberOfMeasurements == this.getMeasurementStorage().size();
	}
	
	/**
	 * Generates substitute measurements based on the most recent measurement for the specified time 
	 * in all measurement storages that do not have a measurement at the specified time. 
	 * 
	 * @param time
	 */
	protected void substituteMissingMeasurements(long time) {
		Set<String> keys = this.getMeasurementStorage().keySet();
		for (String key : keys) {
			this.getMeasurementStorage().get(key).generateSubstituteMeasurement(time); 
		} 
	}
	
	
	protected int getStorageDurationInMinutes() {
		return (int) (this.storageDuration/1000/60);
	}
	
	
	
	protected boolean isInitializing() {
		return this.isInitializing;
	}


	protected int getMeasurementIntervalInMinutes() {
		return (int) (this.measurementInterval/1000/60);
	}
	
	/**
	 * The Class MeasurementSeries.
	 */
	protected class MeasurementSeries extends AbstractMeasurementSeries<T> {

		public MeasurementSeries(long storageTimeHorizon) {
			super(storageTimeHorizon);
		}

		/* (non-Javadoc)
		 * @see hygrid.measurements.AbstractMeasurementStore#convertMeasurementToArray(java.lang.Object)
		 */
		@Override
		public Double[] convertMeasurementToArray(T measurement) {
			return convertMeasurementToRowArray(measurement);
		}
		
		/* (non-Javadoc)
		 * @see hygrid.measurements.AbstractMeasurementStore#convertArrayToMeasurement(java.lang.Double[])
		 */
		@Override
		public T convertArrayToMeasurement(Double[] arrayToConvert) {
			return convertRowArrayToMeasurement(arrayToConvert);
		}
	}
	
	/**
	 * Returns the list of nodes that are contained in the storage.
	 * @return the list of nodes
	 */
	public Vector<String> getVectorOfNodes() {
		return new Vector<String>(this.getMeasurementStorage().keySet());
	}
	
	/**
	 * Returns a measurement for a specific node at a specific point in time
	 */
	public T getMeasurementForNodeAtTime(String node, Long time) {
		return this.getMeasurementStorage().get(node).getMeasurementAtTime(time); 
	}

}
