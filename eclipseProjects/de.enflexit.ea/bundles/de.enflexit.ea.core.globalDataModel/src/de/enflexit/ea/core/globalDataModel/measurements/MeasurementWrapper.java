package de.enflexit.ea.core.globalDataModel.measurements;

/**
 * The Class Measurement hold the instance of the actual type as well as
 * a representation as double array.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MeasurementWrapper<T> {

	private long timeStamp;
	private T measurement;
	
	private Double[] measurementArray;
	
	/**
	 * Instantiates a new measurement.
	 *
	 * @param timeStamp the time stamp
	 * @param measurement the measurement
	 */
	public MeasurementWrapper(long timeStamp, T measurement) {
		this.timeStamp = timeStamp;
		this.measurement = measurement;
	}
	
	/**
	 * Returns the time stamp for the current measurement.
	 * @return the time stamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	/**
	 * Sets the time stamp for the current measurement.
	 * @param timeStamp the new time stamp
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * Gets the measurement.
	 * @return the measurement
	 */
	public T getMeasurement() {
		return measurement;
	}
	/**
	 * Sets the measurement.
	 * @param measurement the new measurement
	 */
	public void setMeasurement(T measurement) {
		this.measurement = measurement;
	}
	
	/**
	 * Gets the measurement array.
	 * @return the measurement array
	 */
	public Double[] getMeasurementArray() {
		return measurementArray;
	}
	/**
	 * Sets the measurement array.
	 * @param measurementArray the new measurement array
	 */
	public void setMeasurementArray(Double[] measurementArray) {
		this.measurementArray = measurementArray;
	}
	
}
