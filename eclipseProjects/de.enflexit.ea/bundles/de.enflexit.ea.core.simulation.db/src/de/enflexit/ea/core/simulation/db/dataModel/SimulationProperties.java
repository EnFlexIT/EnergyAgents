package de.enflexit.ea.core.simulation.db.dataModel;

import java.io.Serializable;

public class SimulationProperties implements Serializable {

	private static final long serialVersionUID = 1151410790396704632L;

	private int idProperty;	
	private int idSimulation;	
	
	
	private String identifier;
	
	private String stringValue;
	private boolean booleanValue;
	
	private int integerValue;
	private long longValue;
	
	private float floatValue;
	private double doubleValue;
	
	
	public int getIdProperty() {
		return idProperty;
	}
	public void setIdProperty(int idProperty) {
		this.idProperty = idProperty;
	}
	
	public int getIdSimulation() {
		return idSimulation;
	}
	public void setIdSimulation(int idSimulation) {
		this.idSimulation = idSimulation;
	}
	
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	
	public String getStringValue() {
		return stringValue;
	}
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public boolean isBooleanValue() {
		return booleanValue;
	}
	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}
	
	public int getIntegerValue() {
		return integerValue;
	}
	public void setIntegerValue(int integerValue) {
		this.integerValue = integerValue;
	}
	
	public long getLongValue() {
		return longValue;
	}
	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
	
	public float getFloatValue() {
		return floatValue;
	}
	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}
	
	public double getDoubleValue() {
		return doubleValue;
	}
	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}
	
}
	