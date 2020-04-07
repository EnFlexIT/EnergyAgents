package de.enflexit.energyAgent.core.monitoring;

import java.util.HashMap;

import energy.FixedVariableList;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;

/**
 * This class can be used to limit the logging of new states on changes in the  
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class IOListFilterForLogging {
	
	private static final long DEFAULT_MAX_TIME_MS = 10 * 60 * 1000;	// Ten minutes
	
	private long lastSavedTimestamp = 0;
	private FixedVariableList lastSavedValues = null;
	
	private long maxTimeBetweenStates;
	private double generalThreshold;
	
	private HashMap<String, Double> specificThresholds;
	
	private boolean debug = false;
	
	/**
	 * Instantiates a new IO value filter for logging with a general threshold of 0.
	 * With this filter, states with unchanged IO values will be ignored. 
	 */
	public IOListFilterForLogging() {
		this(0, DEFAULT_MAX_TIME_MS);
	}
	
	/**
	 * Instantiates a new IO value filter for logging with the specified general threshold.
	 * Changes below this threshold will be ignored. 
	 * @param generalThreshold the general threshold
	 */
	public IOListFilterForLogging(double generalThreshold) {
		this(generalThreshold, DEFAULT_MAX_TIME_MS);
	}
	
	/**
	 * Instantiates a new IO value filter for logging.
	 * @param maxTimeBetweenStates the max time between states
	 */
	public IOListFilterForLogging(long maxTimeBetweenStates) {
		this(0, maxTimeBetweenStates);
	}

	/**
	 * Instantiates a new IO value filter for logging.
	 * @param generalThreshold the threshold below which a change will be ignored
	 * @param maxTimeBetweenStates the max time between states
	 */
	public IOListFilterForLogging(double generalThreshold, long maxTimeBetweenStates) {
		this.generalThreshold = generalThreshold;
		this.maxTimeBetweenStates = maxTimeBetweenStates;
	}
	/**
	 * Gets the max time between states.
	 * @return the max time between states
	 */
	public long getMaxTimeBetweenStates() {
		return maxTimeBetweenStates;
	}
	/**
	 * Sets the max time between states.
	 * @param maxTimeBetweenStates the new max time between states
	 */
	public void setMaxTimeBetweenStates(long maxTimeBetweenStates) {
		this.maxTimeBetweenStates = maxTimeBetweenStates;
	}
	
	/**
	 * Gets the general threshold.
	 * @return the general threshold
	 */
	public double getGeneralThreshold() {
		return generalThreshold;
	}
	/**
	 * Sets the general threshold.
	 * @param generalThreshold the new general threshold
	 */
	public void setGeneralThreshold(double generalThreshold) {
		this.generalThreshold = generalThreshold;
	}

	/**
	 * Gets the specific thresholds.
	 * @return the specific thresholds
	 */
	private HashMap<String, Double> getSpecificThresholds() {
		if (specificThresholds==null) {
			specificThresholds = new HashMap<>();
		}
		return specificThresholds;
	}
	/**
	 * Adds a specific threshold for a variable.
	 * @param variableID the variable ID
	 * @param threshold the threshold
	 */
	public void addSpecificThresholdForVariable(String variableID, double threshold) {
		this.getSpecificThresholds().put(variableID, threshold);
	}
	/**
	 * Removes a specific threshold for a variable.
	 * @param variableID the variable ID
	 */
	public void removeSpecificThresholdForVariable(String variableID) {
		this.getSpecificThresholds().remove(variableID);
	}
	/**
	 * Gets the last saved values.
	 * @return the last saved values
	 */
	private FixedVariableList getLastSavedValues() {
		if (lastSavedValues==null) {
			lastSavedValues = new FixedVariableList();
		}
		return lastSavedValues;
	}

	/**
	 * Checks if a variable has changed significantly.
	 * @param variable the variable
	 * @return true, if there is a significant change
	 */
	private boolean checkForSignificantChange(FixedVariable variable) {
		FixedVariable compareTo = this.getLastSavedValues().getVariable(variable.getVariableID());
		if (compareTo==null) {
			// --- No value for this variable yet -------------------
			return true;
		} else {
			if (variable instanceof FixedBoolean) {
				// --- Compare booleans -----------------------------
				boolean varValue = ((FixedBoolean)variable).isValue();
				boolean compareValue = ((FixedBoolean)compareTo).isValue();
				return (varValue!=compareValue);
			} else if (variable instanceof FixedDouble) {
				// --- Compare doubles ------------------------------ 
				double varValue = ((FixedDouble)variable).getValue();
				double compValue = ((FixedDouble)compareTo).getValue();
				double difference = Math.abs(varValue-compValue);
				double threshold = this.getThreshold(variable.getVariableID());
				return (difference>threshold);
			} else if (variable instanceof FixedInteger) {
				// --- Compare integers -----------------------------
				int varValue = ((FixedInteger)variable).getValue();
				int compValue = ((FixedInteger)compareTo).getValue();
				int difference = Math.abs(varValue-compValue);
				double threshold = this.getThreshold(variable.getVariableID());
				return (difference>threshold);
			}
		}
		return false;
	}
	
	/**
	 * Gets the threshold for a specific variable.
	 * @param variableID the variable ID
	 * @return the threshold
	 */
	private double getThreshold(String variableID) {
		Double specificThreshold = this.getSpecificThresholds().get(variableID);
		if (specificThreshold!=null) {
			return specificThreshold.doubleValue();
		} else {
			return this.generalThreshold;
		}
	}

	/**
	 * Checks if a state should be saved.
	 * @param ioValues The state's io values
	 * @param timeStamp The state's time stamp
	 * @return true, if successful
	 */
	public boolean hasChangedSignificantly(FixedVariableList ioValues, long timeStamp) {
		if (timeStamp-this.lastSavedTimestamp>this.maxTimeBetweenStates) {
			// --- Maximum time between states exceeded, no further checks necessary --------------
			this.lastSavedTimestamp = timeStamp;
			return true;
		} else {
			// --- Check for changes in the IO values ---------------------------------------------
			for (int i=0; i<ioValues.size(); i++) {
				if (this.checkForSignificantChange(ioValues.get(i))==true) {
					if (this.debug==true) {
						System.out.println(ioValues.get(i).getVariableID() + " has changed");
					}
					// --- A significant change has been found, no need to look further -----------
					this.lastSavedValues = ioValues;
					return true;
				}
			}
		}
		return false;
	}
}
