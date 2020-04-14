/*
 * 
 */
package de.enflexit.ea.core.globalDataModel.measurements;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import de.enflexit.ea.core.globalDataModel.ontology.Measurement;

/**
 * The Class AbstractMeasurementSeries can be extended in order to handle measurements for a single node.
 * 
 * @author Sebastian Toersleff - Institute for Automation Technology - Helmut Schmidt University
 */
public abstract class AbstractMeasurementSeries<T> {

	private enum AggregationAction {
		Average,
		Minimum,
		Maximum
	}
	
	private Vector<MeasurementWrapper<T>> measurementSeries;
	private Long seriesDuration;

	
	/**
	 * Instantiates a new measurement series.
	 *
	 * @param seriesDurationInMinutes the series duration in minutes
	 * @param measurementIntervalInSeconds the measurement interval in seconds
	 */
	public AbstractMeasurementSeries(int seriesDurationInMinutes) {
		this((Long) (long) seriesDurationInMinutes*60*1000);
	}
	
	
	/**
	 * Instantiates a new measurement series.
	 *
	 * @param measurementIntervalInSeconds the measurement interval in milliseconds
	 */
	public AbstractMeasurementSeries(long seriesDuration) {
		this.seriesDuration = seriesDuration; 
	}
	
	
	/**
	 * Returns the measurement series.
	 * @return the measurement series
	 */
	public Vector<MeasurementWrapper<T>> getMeasurementSeries() {
		if (measurementSeries==null) {
			measurementSeries = new Vector<>();
		}
		return measurementSeries;
	}
	
	
	/**
	 * Has to convert the specified measurement of type to a double array.
	 * @param measurement the measurement
	 * @return the double[]
	 */
	public abstract Double[] convertMeasurementToArray(T measurement);
	
	
	/**
	 * Has to convert the specified array into a measurement.
	 * @param arrayToConvert the array to convert
	 * @return the measurement of type T
	 */
	public abstract T convertArrayToMeasurement(Double[] arrayToConvert);

	
	/**
	 * Adds the measurement to the measurement series.
	 * @param timeStamp the timeStamp
	 * @param newMeasurement the new measurement
	 */
	public void addMeasurement(long timeStamp, T newMeasurement) {
		MeasurementWrapper<T> mw = new MeasurementWrapper<T>(timeStamp, newMeasurement);
		mw.setMeasurementArray(this.convertMeasurementToArray(newMeasurement)); 
		this.getMeasurementSeries().add(mw);
		this.removeExpiredMeasurements();
		Vector<MeasurementWrapper<T>> ms = this.getMeasurementSeries(); 
		if (ms.size() > 1 && timeStamp < ms.get(ms.size()-2).getTimeStamp()) {
			this.sortMeasurementSeries();
		}
	}
	
	
	/**
	 * Adds the measurement to the measurement series.
	 * @param newMeasurement the new measurement
	 */
	public void addMeasurement(T newMeasurement) {
		Long timeStamp = ((Measurement) newMeasurement).getTimeStamp().getLongValue(); 
		this.addMeasurement(timeStamp, newMeasurement);
	}
	
	
	/**
     * Sorts the current measurement series according to the time stamp.
     */
     protected void sortMeasurementSeries() {
    	 Collections.sort(this.getMeasurementSeries(), new Comparator<MeasurementWrapper<T>>() {
    		 public int compare(MeasurementWrapper<T> m1, MeasurementWrapper<T> m2) {
    			 Long t1 = m1.getTimeStamp();
    			 Long t2 = m2.getTimeStamp();
    			 return t1.compareTo(t2);
    			 }
    		 }
    	 );
    }
     

	/**
	 * Gets the measurement for the specified time. 
	 * 
	 * @param time 
	 * @return the measurement for the specified time if available, otherwise null
	 */
	public T getMeasurementAtTime(long time) {
		for (int row = 0; row <= this.getMeasurementSeries().size()-1; row++) {
			if (this.getMeasurementSeries().get(row).getTimeStamp() == time) {
				return this.getMeasurementSeries().get(row).getMeasurement(); 
			}
		}
		return null; 
	}

	
	/**
	 * Returns the time stamp of the latest measurement.
	 * 
	 * @return the time stamp last measurement
	 */
	protected long getLatestMeasurementTimeStamp() {
		long timeStampLatestMeasurement = 0;
		if (this.getMeasurementSeries().size()>0) {
			timeStampLatestMeasurement = this.getMeasurementSeries().get(this.getMeasurementSeries().size()-1).getTimeStamp();
		}
		return timeStampLatestMeasurement;
	}
	
	
	/**
	 * Removes the expired measurements.
	 */
	protected void removeExpiredMeasurements() {
		if (this.getMeasurementSeries().size()==0) return;
		long expirationTimeStamp = this.getLatestMeasurementTimeStamp()-this.seriesDuration;
		while (this.getMeasurementSeries().get(0).getTimeStamp()<=expirationTimeStamp) {
			this.getMeasurementSeries().remove(0);
		}
	}
	
	/**
	 * Gets the average measurement.
	 * @return the average measurement
	 */
	public T getAverageMeasurement() {
		return this.doMeasurementAggregation(AggregationAction.Average);
	}
	/**
	 * Returns the minimum measurement.
	 * @return the minimum measurement
	 */
	public T getMinimumMeasurement() {
		return this.doMeasurementAggregation(AggregationAction.Minimum);
	}
	/**
	 * Gets the average measurement.
	 * @return the average measurement
	 */
	public T getMaximumMeasurement() {
		return this.doMeasurementAggregation(AggregationAction.Maximum);
	}
	
	/**
	 * Does the measurement aggregation.
	 *
	 * @param action the action
	 * @return the t
	 */
	private T doMeasurementAggregation(AggregationAction action) {
		
		if (this.getMeasurementSeries().size()==0) {
			// --- In case that no data is available ------ 
			return null;
		} else if (this.getMeasurementSeries().size()==1) {
			// --- In case of just one row ----------------
			return this.getMeasurementSeries().get(0).getMeasurement();
		}
		
		// --- Predefine a result double array ------------
		Double[] resultArray = new Double[this.getMeasurementSeries().get(0).getMeasurementArray().length];
		
		int colCount = this.getMeasurementSeries().get(0).getMeasurementArray().length;
		for (int col = 0; col < colCount; col++) {
			
			// --- Visit all rows for this column ---------
			Vector<Double> columnStack = new Vector<>();
			for (int row=0; row<this.getMeasurementSeries().size(); row++) {
				Double value = this.getMeasurementSeries().get(row).getMeasurementArray()[col];  
				if (value!=null) columnStack.add(value);
			}

			// --- Do the specific action here ------------
			Double result = null;
			switch (action) {
			case Average:
				result = this.getAverage(columnStack);
				break;
			case Minimum:
				result = this.getMinimum(columnStack);
				break;
			case Maximum:
				result = this.getMaximum(columnStack);
				break;
			}
			
			// --- Add to the resultArray -----------------  
			resultArray[col] = result;
		}
		return this.convertArrayToMeasurement(resultArray);
	}
	
	/**
	 * Returns the average of the specified double vector.
	 *
	 * @param columnStack the column stack
	 * @return the average
	 */
	protected Double getAverage(Vector<Double> columnStack) {

		Double result = null;
		if (columnStack.size()>0) {
			double sum = 0;
			double counter = 0;
			for (int i = 0; i < columnStack.size(); i++) {
				if (columnStack.get(i)!=null) {
					sum += columnStack.get(i);
					counter++;
				}
			}
			if (counter>0) {
				result = sum / counter;
			}
		}
		return result;
	}
	
	/**
	 * Returns the minimum of the specified double vector.
	 *
	 * @param columnStack the column stack
	 * @return the minimum
	 */
	protected Double getMinimum(Vector<Double> columnStack) {

		Double result = null;
		if (columnStack.size()>0) {
			double minimum = 0;
			for (int i = 0; i < columnStack.size(); i++) {
				if (columnStack.get(i)!=null) {
					minimum = Math.min(minimum, columnStack.get(i));
				}
			}
			result = minimum;
		}
		return result;
	}
	
	/**
	 * Returns the maximum of the specified double vector.
	 *
	 * @param columnStack the column stack
	 * @return the maximum
	 */
	protected Double getMaximum(Vector<Double> columnStack) {
		Double result = null;
		if (columnStack.size()>0) {
			double maximum = 0;
			for (int i = 0; i < columnStack.size(); i++) {
				if (columnStack.get(i)!=null) {
					maximum = Math.min(maximum, columnStack.get(i));
				}
			}
			result = maximum;
		}
		return result;
	}
	
	/**
	 * Checks if there is a measurement at the specified time.
	 *
	 * @param time the time
	 * @return true, if successful
	 */
	public boolean hasMeasurementAtTime(long time) {
		for (int row = 0; row <= this.getMeasurementSeries().size()-1; row++) {
			MeasurementWrapper<T> mw = this.getMeasurementSeries().get(row); 
			if (mw.getTimeStamp() == time) {
				return true; 
			}
		}
		return false; 
	}


	/**
	 * Generates a substitute measurement for the specified time if there is not already a measurement for the specified time
	 * @param time
	 */
	public void generateSubstituteMeasurement(long time) {
		if (this.hasMeasurementAtTime(time) == false) {
			MeasurementWrapper<T> substituteMeasurement = null; 
			for (int row = 0; row <= this.getMeasurementSeries().size()-1; row++) {
				substituteMeasurement = this.getMeasurementSeries().get(row); 
				if (this.getMeasurementSeries().get(row).getTimeStamp() >= time) {
					break; 
				}
			}
			substituteMeasurement.setTimeStamp(time);	
		}	
	}
	
	
}
