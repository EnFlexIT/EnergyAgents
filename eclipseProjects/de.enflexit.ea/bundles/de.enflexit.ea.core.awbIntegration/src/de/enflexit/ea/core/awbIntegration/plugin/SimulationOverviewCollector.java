package de.enflexit.ea.core.awbIntegration.plugin;

import java.util.Calendar;

import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.simulationService.time.TimeModel;
import agentgui.simulationService.time.TimeModelDateBased;
import de.enflexit.common.properties.Properties;
import de.enflexit.common.properties.Properties.PropertyType;
import de.enflexit.common.properties.PropertyValue;
import de.enflexit.ea.core.simulation.db.DatabaseHandler;
import de.enflexit.ea.core.simulation.db.dataModel.SimulationOverview;
import de.enflexit.ea.core.simulation.db.dataModel.SimulationProperties;

/**
 * The Class SimulationOverviewCollector.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SimulationOverviewCollector {

	private SimulationOverview simulationOverview;
	
	/**
	 * Instantiates a new simulation overview collector.
	 *
	 * @param project the project
	 * @param databaseName the database name
	 * @throws NullPointerException in case that no project or no database name is specified
	 */
	public SimulationOverviewCollector(Project project, String databaseName) {
		if (project==null) {
			throw new NullPointerException("No project was specified to save execution information!");
		}
		if (databaseName==null) {
			throw new NullPointerException("No database name was specified to save execution information!");
		}
		this.createSimulationOverview(project, databaseName);
	}

	/**
	 * Creates a simulation overview, based on the specified {@link Project}.
	 *
	 * @param project the project
	 * @param databaseName the database name
	 */
	private void createSimulationOverview(Project project, String databaseName) {
		
		SimulationSetup setup = project.getSimulationSetups().getCurrSimSetup();
		
		// --- Create SimulationOverview instance --------- 
		SimulationOverview so = new SimulationOverview();
		so.setAwbProject(project.getProjectName());
		so.setAwbSetup(project.getSimulationSetupCurrent());
		so.setDatabaseName(databaseName);
		
		// --- Simulation time from to to -----------------
		TimeModel tm = project.getTimeModelController().getTimeModel();
		if (tm!=null && tm instanceof TimeModelDateBased) {
			TimeModelDateBased tmdb = (TimeModelDateBased) tm;

			Calendar simTimeFrom = Calendar.getInstance();
			simTimeFrom.setTimeInMillis(tmdb.getTimeStart());
			so.setSetupTimeFrom(simTimeFrom);
			
			Calendar simTimeTo = Calendar.getInstance();
			simTimeTo.setTimeInMillis(tmdb.getTimeStop());
			so.setSetupTimeTo(simTimeTo);
		}
		
		so.setTimeOfExecution(Calendar.getInstance());
		
		// --- Add properties to SimulationOverview -------
		this.addProperties(so, project.getProperties());
		this.addProperties(so, setup.getProperties());
		
		// --- Remind the SimulationOverview instance -----
		this.setSimulationOverview(so);
	}
	
	/**
	 * Adds the specified properties to the SimulationOverview instance.
	 *
	 * @param so the SimulationOverview instance to work on
	 * @param properties the properties to add
	 */
	private void addProperties(SimulationOverview so, Properties properties) {
		
		for (String identifier : properties.getIdentifierList()) {
			PropertyValue pValue = properties.getPropertyValue(identifier);
			this.addProperty(so, identifier, pValue);
		}
	}
	/**
	 * Adds a SimulationProperties instance to the specified SimulationOverview instance.
	 *
	 * @param so the SimulationOverview instance to work on
	 * @param identifier the identifier
	 * @param pValue the value
	 */
	private void addProperty(SimulationOverview so, String identifier, PropertyValue pValue) {
		
		if (pValue==null) return;
		
		SimulationProperties simProp = new SimulationProperties();
		simProp.setIdentifier(identifier);
		
		if (pValue.getValue()!=null) {
			PropertyType pType = pValue.getPropertyType();
			switch (pType) {
			case String:
				simProp.setStringValue(pValue.getStringValue());
				break;
			case Boolean:
				simProp.setBooleanValue(pValue.getBooleanValue());
				break;
			case Integer:
				simProp.setIntegerValue(pValue.getIntegerValue());
				break;
			case Long:
				simProp.setLongValue(pValue.getLongValue());
				break;
			case Float:
				simProp.setFloatValue(pValue.getFloatValue());
				break;
			case Double:
				simProp.setDoubleValue(pValue.getDoubleValue());
				break;
			}
		}
		so.getSimulationProperties().add(simProp);
	}
	
	
	/**
	 * Returns the current SimulationOverview instance.
	 * @return the simulation overview
	 */
	public SimulationOverview getSimulationOverview() {
		return simulationOverview;
	}
	private void setSimulationOverview(SimulationOverview simulationOverview) {
		this.simulationOverview = simulationOverview;
	}
	
	/**
	 * Save the local SimulationOverview instance to the database.
	 * @return true, if successful
	 */
	public boolean saveToDatabase() {
		
		DatabaseHandler dbHandler = new DatabaseHandler();
		boolean successful = dbHandler.saveOrUpdateSimulationOverview(this.getSimulationOverview());
		dbHandler.dispose();
		return successful;
	}
	
}
