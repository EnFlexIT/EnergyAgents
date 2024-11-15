package de.enflexit.ea.core.awbIntegration.plugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.agentgui.gui.swing.project.ProjectWindowTab;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarSurrounding;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarType;
import org.awb.env.networkModel.controller.ui.toolbar.CustomToolbarComponentDescription;
import org.hibernate.cfg.Configuration;

import agentgui.core.application.Application;
import agentgui.core.charts.timeseriesChart.StaticTimeSeriesChartConfiguration;
import agentgui.core.charts.timeseriesChart.TimeSeriesLengthRestriction;
import agentgui.core.gui.projectwindow.simsetup.TimeModelController;
import agentgui.core.plugin.PlugIn;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.core.project.transfer.ProjectExportControllerProvider;
import agentgui.simulationService.time.TimeModelContinuous;
import de.enflexit.db.hibernate.HibernateDatabaseService;
import de.enflexit.db.hibernate.HibernateUtilities;
import de.enflexit.db.hibernate.connection.DatabaseConnectionManager;
import de.enflexit.db.hibernate.connection.HibernateDatabaseConnectionService;
import de.enflexit.db.hibernate.relocation.DatabaseRelocator;
import de.enflexit.ea.core.awbIntegration.plugin.gui.HyGridSettingsTab;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.deployment.AgentDeploymentInformation;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.deployment.SetupExtension;
import energy.GlobalInfo;
import energy.helper.UnitConverter;
import energy.optionModel.ScheduleLengthRestriction;
import energy.schedule.loading.ScheduleTimeRange;
import energy.schedule.loading.ScheduleTimeRangeController;
import jade.core.ProfileImpl;

/**
 * The Class AWBIntegrationPlugIn adds a further tab to the visual representation of the SimulationSetup and an additional abstract data model.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class AWBIntegrationPlugIn extends PlugIn {

	private boolean isShowConstructionSiteButton = false;
	
	private GraphEnvironmentController graphController;
	private SetupExtension setupExtension;

	private ScheduleTimeRangeListener scheduleTimeRangeListener;

	private DatabaseRelocator databaseRelocator;
	private SimulationOverviewCollector simulationOverviewCollector;
	
	/**
	 * Instantiates a new Agent.HyGrid plugin .
	 * @param currProject the current project
	 */
	public AWBIntegrationPlugIn(Project currProject) {
		super(currProject); // required !!
	}

	/**
	 * Gets the graph controller.
	 * @return the graph controller
	 */
	public GraphEnvironmentController getGraphController() {
		if (graphController==null) {
			graphController = (GraphEnvironmentController) this.project.getEnvironmentController();
		}
		return graphController;
	}
	
	/**
	 * Returns (and creates) the HyGridScheduleTimeRangeListener for the HyGridPlugin.
	 * @return the schedule time range listener
	 */
	private ScheduleTimeRangeListener getScheduleTimeRangeListener() {
		if (scheduleTimeRangeListener==null) {
			scheduleTimeRangeListener = new ScheduleTimeRangeListener(this.getGraphController());
		}
		return scheduleTimeRangeListener;
	}
	/**
	 * Will destroy the current {@link ScheduleTimeRangeListener}.
	 */
	private void destroyScheduleTimeRangeListener() {
		if (scheduleTimeRangeListener!=null) {
			scheduleTimeRangeListener.removeListener();
			scheduleTimeRangeListener = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#getName()
	 */
	@Override
	public String getName() {
		return "Energy Agent - AWB Integration Plugin";
	}

	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPlugIn()
	 */
	@Override
	public void onPlugIn() {
		
		if (Application.isOperatingHeadless() == false) {
			// --- Add the abstract environment model to the project --------------------
			this.addAbstractEnvironmentModel2Project();
			// --- Add the abstract environment model to GraphController ----------------
			this.addAbstractEnvironmentModel2GraphController();
			// --- Add the configuration tab to the simulation setup --------------------
			this.addHyGridTabs();
			// --- Add an load profile import button ------------------------------------
			this.addHyGridButtons();

			this.project.getTimeModelController().setIndexPositionOfTimeModelTab(1);
		}

		// --- Initially create the HyGridScheduleTimeRangeListener ---------------------
		this.getScheduleTimeRangeListener();
		
		// --- Register the specialized ProjectExportController implementation ----------
		ProjectExportControllerProvider.setProjectExportControllerClass(EnergyAgentProjectExportController.class.getName());
		
		// --- Load the setup extension for the current setup ---------------------------
		SimulationSetup simSetup = this.project.getSimulationSetups().getCurrSimSetup();
		this.loadSetupExtension(simSetup);
		
		// --- Do the super call too (will just print some information ------------------
		super.onPlugIn();
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPlugOut()
	 */
	@Override
	public void onPlugOut() {
		ProjectExportControllerProvider.unsetProjectExportControllerClass();
		this.destroyScheduleTimeRangeListener();
		super.onPlugOut();
	}

	/**
	 * Adds the abstract environment model to the project.
	 */
	private void addAbstractEnvironmentModel2Project() {

		Object userRuntimeObject = this.project.getUserRuntimeObject();
		if (userRuntimeObject==null || !(userRuntimeObject instanceof HyGridAbstractEnvironmentModel)) {
			this.project.setUserRuntimeObject(new HyGridAbstractEnvironmentModel());
		}
	}
	
	/**
	 * Adds the abstract environment model to the GraphController.
	 * @return the {@link HyGridAbstractEnvironmentModel}
	 */
	private HyGridAbstractEnvironmentModel addAbstractEnvironmentModel2GraphController() {
		
		// --- Get the abstract environment model -----------------------------
		HyGridAbstractEnvironmentModel abstractEnvModel = null;
		try {
			abstractEnvModel = (HyGridAbstractEnvironmentModel) this.project.getUserRuntimeObject();
			abstractEnvModel.setProject(this.project);
			// --- Make sure that just a copy of the current instance is used -
			abstractEnvModel = abstractEnvModel.getCopy();

		} catch (Exception ex) {
			System.err.println("Error while trying to get the abstract environment model - using a default model now!");
			abstractEnvModel = new HyGridAbstractEnvironmentModel();
			abstractEnvModel.setProject(this.project);
		}

		// --- Set the abstract model to the environment for the simulation ---
		if (this.getGraphController()!=null) {
			this.getGraphController().setAbstractEnvironmentModel(abstractEnvModel);
		}
		return abstractEnvModel;
	}
	
	
	/**
	 * Adds the smart house tabs that are working on the AbstractEnvironmentModel.
	 * @see #addAbstractEnvironmentModel2Project()
	 */
	private void addHyGridTabs() {

		// --- 1. Add configuration panel to the simulation setup ---
		ProjectWindowTab parentPWT = this.project.getProjectEditorWindow().getTabForSubPanels(ProjectWindowTab.TAB_4_SUB_PANES_Setup);
		ProjectWindowTab pwt = new ProjectWindowTab(this.project, ProjectWindowTab.DISPLAY_4_END_USER, "HyGrid-Settings", null, null, new HyGridSettingsTab(this.project), parentPWT.getTitle());
		this.addProjectWindowTab(pwt, 0);
	}
	
	

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onSimSetupAddNew(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	protected void onSimSetupAddNew(SimulationSetup simSetup) {
		this.loadSetupExtension(simSetup);
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onSimSetupLoad(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	protected void onSimSetupLoad(SimulationSetup simSetup) {
		this.loadSetupExtension(simSetup);
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onSimSetupRemove(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	protected void onSimSetupRemove(SimulationSetup simSetup) {
		this.setSetupExtension(null);
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onSimSetupPrepareSaving(agentgui.core.project.setup.SimulationSetup)
	 */
	@Override
	protected void onSimSetupPrepareSaving(SimulationSetup simSetup) {
		this.getSetupExtension().setScheduleTimeRange(ScheduleTimeRangeController.getScheduleTimeRange());
		simSetup.setUserRuntimeObject(this.getSetupExtension());
	}

	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPrepareForSaving()
	 */
	@Override
	protected void onPrepareForSaving() {
		this.addAbstractEnvironmentModel2GraphController();
	}

	/**
	 * Adds additional buttons to the toolbar's of the graph environment visualization component.
	 */
	private void addHyGridButtons() {
		if (this.isShowConstructionSiteButton==true) {
			this.getGraphController().addCustomToolbarComponentDescription(new CustomToolbarComponentDescription(ToolBarType.EditControl, ToolBarSurrounding.ConfigurationOnly, JButtonConstructionSite.class, null, true));
		}
	}


	// --------------------------------------------------------------
	// --------------------------------------------------------------
	// --------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onMasWillBeExecuted()
	 */
	@Override
	public void onMasWillBeExecuted() {
		
		HyGridAbstractEnvironmentModel hygridAbstractEnvironmentModel = this.addAbstractEnvironmentModel2GraphController();
		// --- Set the schedule length restriction at runtime -------
		ScheduleLengthRestriction scheduleLengthRestriction = hygridAbstractEnvironmentModel.getScheduleLengthRestriction();
		GlobalInfo.setScheduleLengthRestriction(scheduleLengthRestriction);
		
		// --- Set time series length restriction accordingly -------
		TimeSeriesLengthRestriction timeSeriesLengthRestriction = new TimeSeriesLengthRestriction();
		timeSeriesLengthRestriction.setMaxNumberOfStates(scheduleLengthRestriction.getMaxNumberOfSystemStates());
		timeSeriesLengthRestriction.setMaxDuration(UnitConverter.convertDurationToMilliseconds(scheduleLengthRestriction.getDuration()));
		StaticTimeSeriesChartConfiguration.setTimeSeriesLengthRestriction(timeSeriesLengthRestriction);
		
		// --- Set the setup extension ------------------------------
		hygridAbstractEnvironmentModel.setSetupExtension(this.getSetupExtension());
		
		// --- ScheduleTimeRange: reloaded current data ? -----------
		this.getScheduleTimeRangeListener().checkTimeRangeSettingsAccordingToTimeModelBeforeJadeStart();
	
		// --- Adjust database settings? ----------------------------
		this.onMasWillBeExecutedDB();
	}

	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onMasWasTerminated()
	 */
	@Override
	public void onMasWasTerminated() {
		// --- Reset SetupExtension ---------------------------------
		HyGridAbstractEnvironmentModel hyGridDM = this.getHyGridAbstractEnvironmentModel();
		if (hyGridDM != null) {
			hyGridDM.setSetupExtension(null);
		}
		// --- Restore database settings ----------------------------
		this.onMasWasTerminatedDB();
	}
	
	// --------------------------------------------------------------
	// --- From here, DB handling for the execution -----------------
	// --------------------------------------------------------------
	/**
	 * Returns the current HyGridAbstractEnvironmentModel to be used.
	 * @return the hy grid abstract environment model
	 */
	private HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return (HyGridAbstractEnvironmentModel) this.getGraphController().getAbstractEnvironmentModel();
	}
	
	/**
	 * Checks if is configured database switch.
	 * @return true, if is configured database switch
	 */
	private boolean isConfiguredForDedicatedDatabase() {
		
		HyGridAbstractEnvironmentModel hyGridDM = this.getHyGridAbstractEnvironmentModel();
		
		if (hyGridDM.isSaveRuntimeInformationToDatabase()==false) return false;
		if (hyGridDM.isSaveRuntimeInformationToDedicatedDatabase()==false) return false;
		if (hyGridDM.getFactoryIDList().size()==0) return false;
		return true;
	}
	
	/**
	 * Returns the PEAK database re-locator.
	 * @return the database re-locator
	 */
	private DatabaseRelocator getDatabaseRelocator() {
		if (databaseRelocator==null) {
			databaseRelocator = new DatabaseRelocator();
		}
		return databaseRelocator;
	}
	/**
	 * Sets the database re-locator.
	 * @param databaseRelocator the new database re-locator
	 */
	private void setDatabaseRelocator(DatabaseRelocator databaseRelocator) {
		this.databaseRelocator = databaseRelocator;
	}
	
	/**
	 * Returns the simulation overview collector.
	 * @return the simulation overview collector
	 */
	private SimulationOverviewCollector getSimulationOverviewCollector() {
		return simulationOverviewCollector;
	}
	/**
	 * Sets the simulation overview collector.
	 * @param simulationOverviewCollector the new simulation overview collector
	 */
	public void setSimulationOverviewCollector(SimulationOverviewCollector simulationOverviewCollector) {
		this.simulationOverviewCollector = simulationOverviewCollector;
	}
	
	/**
	 * Will adjust the destination database for the MAS execution .
	 */
	private void onMasWillBeExecutedDB() {
		
		boolean verbose = false;
		
		if (this.isConfiguredForDedicatedDatabase()==false) return;
		
		HyGridAbstractEnvironmentModel hyGridDM = this.getHyGridAbstractEnvironmentModel();

		String dbPrefix = hyGridDM.getDedicatedDatabasePrefix();
		if (dbPrefix==null || dbPrefix.isBlank()==true) {
			dbPrefix = Application.getProjectFocused().getProjectName();
		}
		String simSetup = this.getShortText(Application.getProjectFocused().getSimulationSetupCurrent());
		String dateString = new SimpleDateFormat("dd-MM-yy_HH-mm").format(new Date(System.currentTimeMillis()));
		String newDatabase = dbPrefix + "_" + simSetup + "_" + dateString;
		newDatabase = newDatabase.replace(".", "");
		newDatabase = newDatabase.replace(" ", "");
		
		// --- Save information to table ea_simulation_overview ---------------
		this.setSimulationOverviewCollector(new SimulationOverviewCollector(this.project, newDatabase));
		
		// --- Get DatabaseConnectionManager ----------------------------------
		DatabaseConnectionManager dbConnManager = DatabaseConnectionManager.getInstance();
		
		// --- Define the temporary properties to apply -----------------------
		HashMap<String, java.util.Properties> tmpPropertiesHashMap = new HashMap<>();
		List<String> factoryIDListToConsider = hyGridDM.getFactoryIDList();
		List<String> factoryIDList = HibernateUtilities.getSessionFactoryIDList();
		for (String factoryID : factoryIDList) {
			// --- Check if to consider for the DB switch ---------------------
			if (factoryIDListToConsider.contains(factoryID)==false) continue;
			
			// --- Get current configuration and settings ---------------------
			HibernateDatabaseConnectionService hdbcs = dbConnManager.getHibernateDatabaseConnectionService(factoryID);
			Configuration configuration = hdbcs.getConfiguration();
			dbConnManager.loadDatabaseConfigurationProperties(factoryID, configuration);
			String oldDatabase = (String) configuration.getProperties().get(HibernateDatabaseService.HIBERNATE_PROPERTY_Catalog);
			String oldURL      = (String) configuration.getProperties().get(HibernateDatabaseService.HIBERNATE_PROPERTY_URL);
			String newURL      = oldURL.replace(oldDatabase, newDatabase);
			
			// --- Simply change the Catalog / DB-name for saving data --------
			java.util.Properties tmpProps = new java.util.Properties();
			tmpProps.setProperty(HibernateDatabaseService.HIBERNATE_PROPERTY_Catalog, newDatabase);
			tmpProps.setProperty(HibernateDatabaseService.HIBERNATE_PROPERTY_URL, newURL);
			tmpPropertiesHashMap.put(factoryID, tmpProps);
		}
		this.getDatabaseRelocator().setVerbose(verbose);
		this.getDatabaseRelocator().applyTemporaryHibernateProperties(tmpPropertiesHashMap, true, true);
	}
	/**
	 * Return a short text for the specified text.
	 *
	 * @param longText the long text
	 * @return the short text
	 */
	private String getShortText(String longText) {
		
		boolean isDebug = false;
		int maxLowerCaseCharacters = 3;
		
		String shortText = "";
		try {
			
			for (int i = 0; i < longText.length(); i++) {
				
				char ch = longText.charAt(i);
				boolean addCharacter = false;
				
				if (ch=='-' || ch=='_') {
					addCharacter = true;
				} else if (Character.isDigit(ch)==true) {
					addCharacter = true;
				} else if (Character.isUpperCase(ch)==true) {
					addCharacter = true;
				} else if (Character.isLowerCase(ch)==true) {
					// --- Check if the last three characters are lower case ---
					addCharacter = true;
					int lowerCaseCounter = 1;
					for (int j = shortText.length()-1; j>=0; j--) {
						char chCheck = shortText.charAt(j);
						if (Character.isLowerCase(chCheck)==true) {
							lowerCaseCounter++;
						} else {
							break;
						}
						if (lowerCaseCounter >= maxLowerCaseCharacters) {
							addCharacter=false;
							break;
						}
					}
				} 
				
				if (addCharacter==true) {
					shortText+=ch;
				}
			} 
			
		} catch (Exception ex) {
			ex.printStackTrace();
			shortText = longText;
		}
		
		if (isDebug==true) {
			System.out.println(longText + " \t=> \t" + shortText);
		}
		return shortText;
	}
	
	/**
	 * Checks for valid precondition for the MAS execution with respect to the database.
	 * @return true, if successful
	 */
	private boolean hasValidPreconditionForMasExecutionDB() {
		
		if (this.isConfiguredForDedicatedDatabase()==false) return true;
		
		boolean isApplied = this.getDatabaseRelocator().isAppliedTemporaryHibernateProperties(2000);
		if (isApplied==false) {
			System.err.println("[" + this.getClass().getSimpleName() + "] => COULD NOT APPLY TEMPORARY DATABASE CONFIGURATION FOR THE MAS EXECUTION - OBJECTION AGAINST JADE START !!! <="); 
		} else {
			SimulationOverviewCollector soc = this.getSimulationOverviewCollector();
			if (soc!=null) {
				soc.saveToDatabase();
			}
		}
		return isApplied;
	}
	
	/**
	 * Will restore the database settings after the MAS execution .
	 */
	private void onMasWasTerminatedDB() {
		
		if (this.isConfiguredForDedicatedDatabase()==false) return;

		// --- Restore database settings ---------------------------- 
		this.getDatabaseRelocator().restoreTemporaryHibernateProperties();
		this.setDatabaseRelocator(null);
		
		// --- Save finalization time -------------------------------
		SimulationOverviewCollector soc = this.getSimulationOverviewCollector();
		if (soc!=null) {
			soc.getSimulationOverview().setTimeOfFinalization(Calendar.getInstance());
			soc.saveToDatabase();
			this.setSimulationOverviewCollector(null);
		}
		
	}
	// --------------------------------------------------------------
	// --------------------------------------------------------------
	// --------------------------------------------------------------

	/**
	 * Gets the {@link AWBIntegrationPlugIn} instance for the current project
	 * @return The HyGridPlugIn instance
	 */
	public static AWBIntegrationPlugIn getInstanceForCurrentProject() {
		return getInstanceForProject(Application.getProjectFocused());
	}

	/**
	 * Gets the {@link AWBIntegrationPlugIn} instance for the specified project
	 * 
	 * @param project The project
	 * @return The HyGridPlugIn instance
	 */
	private static AWBIntegrationPlugIn getInstanceForProject(Project project) {
		AWBIntegrationPlugIn plugIn = null;
		for (int i = 0; i < project.getPlugInsLoaded().size(); i++) {
			PlugIn pi = project.getPlugInsLoaded().get(i);
			if (pi instanceof AWBIntegrationPlugIn) {
				plugIn = (AWBIntegrationPlugIn) pi;
			}
		}
		return plugIn;
	}
	
	// ----------------------------------------------------
	// -- Methods for managing the setup extension -------- 
	// ----------------------------------------------------
	/**
	 * Gets the setup extension.
	 * @return the setup extension
	 */
	public SetupExtension getSetupExtension() {
		if (setupExtension == null) {
			setupExtension = new SetupExtension();
		}
		return setupExtension;
	}
	/**
	 * Sets the setup extension.
	 * @param setupExtension the new setup extension
	 */
	public void setSetupExtension(SetupExtension setupExtension) {
		this.setupExtension = setupExtension;
		// --- Set the ScheduleTimeRange to the GlobalInfo ----------  
		ScheduleTimeRange str = null;
		if (this.setupExtension!=null) {
			str = this.setupExtension.getScheduleTimeRange();
		}
		ScheduleTimeRangeController.setScheduleTimeRange(str, ScheduleTimeRangeListener.EVENT_SETUP_LOAD);
	}
	/**
	 * Load setup extension.
	 * @param simSetup the sim setup
	 */
	private void loadSetupExtension(SimulationSetup simSetup) {
		if (simSetup.getUserRuntimeObject() == null) {
			this.setSetupExtension(null);
		} else {
			SetupExtension se = (SetupExtension) simSetup.getUserRuntimeObject();
			this.setSetupExtension(se);
		}
	}
	
	// ----------------------------------------------------------------
	// --- Methods related to checking MAS execution preconditions ----
	// ----------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#hasValidPreconditionForMasExecution()
	 */
	@Override
	public boolean hasValidPreconditionForMasExecution() {
		
		boolean validPreconditions;
		if (this.checkForTestBedRealAgents()==false) {
			// --- No preconditions in this case ----------
			validPreconditions = true;
		} else {
			// --- If the current setup contains TestBedReal agents, check if the selected time model is compatible --------
			validPreconditions = this.checkSelectedTimeModelForTestBedRealCompatibility();
		}

		if (validPreconditions == false) {
			String message = "Incompatible time model: When using the operating mode TestBedReal, please choose a continuous and non-accelerated time model.";
			System.err.println("[" + this.getClass().getSimpleName() + "]" + message);
			if (Application.isOperatingHeadless()==false) {
				JOptionPane.showMessageDialog(Application.getMainWindow(), message, "Incompatible Time Model!", JOptionPane.WARNING_MESSAGE);
			}
		}
		
		// --- Check database settings --------------------
		if (validPreconditions==true) {
			validPreconditions = this.hasValidPreconditionForMasExecutionDB();
		}
		return validPreconditions;
	}
	
	/**
	 * Checks if the current setup contains agents in TestBedReal mode
	 * @return true if at least one agent is in TestBedReal mode
	 */
	private boolean checkForTestBedRealAgents() {

		if (this.setupExtension.getDeploymentGroupsHelper().getAllDeployedAgents().size() > 0) {
			for (String testbedAgentID : this.setupExtension.getDeploymentGroupsHelper().getDeployedAgentIDs()) {
				AgentDeploymentInformation agentInfo = this.getSetupExtension().getDeploymentGroupsHelper().getAgentDeploymentInformation(testbedAgentID);
				if (agentInfo.getAgentOperatingMode()==AgentOperatingMode.TestBedReal) {
					// --- An agent in testbed real mode has been found -------
					return true;
				}
			}
		}
		
		// --- No testbed real agents found -----------------------------------
		return false;
	}
	
	/**
	 * Checks if the selected time model is compatible with TestBedReal agents, i.e. continuous and not accelerated
	 * @return true if compatible
	 */
	private boolean checkSelectedTimeModelForTestBedRealCompatibility() {

		boolean compatible = false;
		TimeModelController tmc = Application.getProjectFocused().getTimeModelController();

		if (tmc.getTimeModel() instanceof TimeModelContinuous) {
			TimeModelContinuous timeModel = (TimeModelContinuous) tmc.getTimeModel();
			if (timeModel.getAccelerationFactor() == 1.0) {
				compatible = true;
			}
		}
		return compatible;
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#getJadeProfile(jade.core.ProfileImpl)
	 */
	@Override
	public ProfileImpl getJadeProfile(ProfileImpl jadeContainerProfile) {
		// --- Disable multiple delivery due to a jade bug causing problems when sending to other platforms 
		jadeContainerProfile.setParameter("jade_core_messaging_MessageManager_enablemultipledelivery", "false");
		return jadeContainerProfile;
	}

}
