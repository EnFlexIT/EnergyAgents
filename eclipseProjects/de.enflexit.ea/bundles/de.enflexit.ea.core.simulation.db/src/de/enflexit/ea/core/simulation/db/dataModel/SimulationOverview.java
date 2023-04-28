package de.enflexit.ea.core.simulation.db.dataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SimulationOverview implements Serializable {

	private static final long serialVersionUID = 1151410790396704632L;

	private int idSimulation;	

	private String awbProject;
	private String awbSetup;
	
	private String databaseName;

	private Calendar setupTimeFrom;
	private Calendar setupTimeTo;

	private Calendar timeOfExecution;
	private Calendar timeOfFinalization;

	private List<SimulationProperties> simulationProperties;
	
	
	public int getIdSimulation() {
		return idSimulation;
	}
	public void setIdSimulation(int idSimulation) {
		this.idSimulation = idSimulation;
	}
	
	
	public String getAwbProject() {
		return awbProject;
	}
	public void setAwbProject(String awbProject) {
		this.awbProject = awbProject;
	}
	
	public String getAwbSetup() {
		return awbSetup;
	}
	public void setAwbSetup(String awbSetup) {
		this.awbSetup = awbSetup;
	}
	
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	public Calendar getSetupTimeFrom() {
		return setupTimeFrom;
	}
	public void setSetupTimeFrom(Calendar setupTimeFrom) {
		this.setupTimeFrom = setupTimeFrom;
	}
	public Calendar getSetupTimeTo() {
		return setupTimeTo;
	}
	public void setSetupTimeTo(Calendar setupTimeTo) {
		this.setupTimeTo = setupTimeTo;
	}
	
	
	public Calendar getTimeOfExecution() {
		return timeOfExecution;
	}
	public void setTimeOfExecution(Calendar timeOfExecution) {
		this.timeOfExecution = timeOfExecution;
	}
	
	public Calendar getTimeOfFinalization() {
		return timeOfFinalization;
	}
	public void setTimeOfFinalization(Calendar timeOfFinalization) {
		this.timeOfFinalization = timeOfFinalization;
	}
	
	public List<SimulationProperties> getSimulationProperties() {
		if (simulationProperties==null) {
			simulationProperties = new ArrayList<>();
		}
		return simulationProperties;
	}
	public void setSimulationProperties(List<SimulationProperties> simulationProperties) {
		this.simulationProperties = simulationProperties;
	}
	
}
