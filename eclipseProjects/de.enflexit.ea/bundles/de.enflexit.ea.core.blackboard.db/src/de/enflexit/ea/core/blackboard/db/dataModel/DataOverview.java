package de.enflexit.ea.core.blackboard.db.dataModel;

import java.io.Serializable;
import java.util.Calendar;

public class DataOverview implements Serializable {

	private static final long serialVersionUID = 1151410790396704632L;

	private int idExecution;	

	private String executer;	
	private Calendar executionTime;	
	
	private String projectName;
	private String setupName;	
	
	private Calendar setupTimeFrom;
	private Calendar setupTimeTo;
	
	
	
	public int getIdExecution() {
		return idExecution;
	}
	public void setIdExecution(int idExecution) {
		this.idExecution = idExecution;
	}

	
	public String getExecuter() {
		return executer;
	}
	public void setExecuter(String executer) {
		this.executer = executer;
	}
	
	
	public Calendar getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(Calendar executionTime) {
		this.executionTime = executionTime;
	}
	
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	
	public String getSetupName() {
		return setupName;
	}
	public void setSetupName(String setupName) {
		this.setupName = setupName;
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
	
}
