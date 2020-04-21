package de.enflexit.ea.core.awbIntegration.plugin;

import javax.swing.JOptionPane;

import org.agentgui.gui.swing.project.ProjectWindowTab;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarSurrounding;
import org.awb.env.networkModel.controller.ui.BasicGraphGui.ToolBarType;
import org.awb.env.networkModel.controller.ui.toolbar.CustomToolbarComponentDescription;

import agentgui.core.application.Application;
import agentgui.core.charts.timeseriesChart.TimeSeriesLengthRestriction;
import agentgui.core.gui.projectwindow.simsetup.TimeModelController;
import agentgui.core.plugin.PlugIn;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.core.project.transfer.ProjectExportControllerProvider;
import agentgui.simulationService.time.TimeModelContinuous;
import de.enflexit.ea.core.awbIntegration.plugin.gui.HyGridSettingsTab;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.deployment.AgentDeploymentInformation;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.deployment.SetupExtension;
import energy.GlobalInfo;
import energy.UnitConverter;
import energy.optionModel.ScheduleLengthRestriction;
import energy.schedule.loading.ScheduleTimeRange;
import energy.schedule.loading.ScheduleTimeRangeController;
import jade.core.ProfileImpl;

/**
 * The Class SmartHousePlugIn adds a further tab to the visual representation of the SimulationSetup and an additional abstract data model.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class AWBIntegrationPlugIn extends PlugIn {

	private boolean isShowConstructionSiteButton = false;
	
	private GraphEnvironmentController graphController;
	private SetupExtension setupExtension;

	private ScheduleTimeRangeListener scheduleTimeRangeListener;
	
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
		if (userRuntimeObject==null) {
			this.project.setUserRuntimeObject(new HyGridAbstractEnvironmentModel());
		} else {
			// --- Make sure that the user object is the right type ----
			if (!(userRuntimeObject instanceof HyGridAbstractEnvironmentModel)) {
				this.project.setUserRuntimeObject(new HyGridAbstractEnvironmentModel());
			}
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
			// --- Make sure that just a copy of the current instance is used -
			abstractEnvModel = abstractEnvModel.getCopy();
			// --- Add setupExtension -----------------------------------------
//			abstractEnvModel.setSetupExtension(this.getSetupExtension());

		} catch (Exception ex) {
			System.err.println("Error while trying to get the abstract environment model - using a default model now!");
			abstractEnvModel = new HyGridAbstractEnvironmentModel();
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
		this.getGraphController().addCustomToolbarComponentDescription(new CustomToolbarComponentDescription(ToolBarType.ViewControl, ToolBarSurrounding.RuntimeOnly, JButtonExportSimulationData4Time.class, null, true));
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
		Application.getGlobalInfo().setTimeSeriesLengthRestriction(timeSeriesLengthRestriction);
		
		// --- Set the setup extension ------------------------------
		hygridAbstractEnvironmentModel.setSetupExtension(this.getSetupExtension());
		
		// --- ScheduleTimeRange: reloaded current data ? -----------
		this.getScheduleTimeRangeListener().checkTimeRangeSettingsAccordingToTimeModelBeforeJadeStart();
		
	}

	/*
	 * (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onMasWasTerminated()
	 */
	@Override
	public void onMasWasTerminated() {
		HyGridAbstractEnvironmentModel haemGraphCo = (HyGridAbstractEnvironmentModel) this.getGraphController().getEnvironmentModel().getAbstractEnvironment();
		if (haemGraphCo != null) {
			haemGraphCo.setSetupExtension(null);
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
		if (this.checkForTestBedRealAgents() == false) {
			// --- No preconditions in this case --------------
			validPreconditions = true;
		} else {
			// --- If the current setup contains TestBedReal agents, check if the selected time model is compatible --------
			validPreconditions = this.checkSelectedTimeModelForTestBedRealCompatibility();
		}

		if (validPreconditions == false) {
			System.err.println(this.getName() + ": Incompatible time model - when using the operating mode TestBedReal, please choose a continuous and non-accelerated time model");
			if (Application.isOperatingHeadless() == false) {
				JOptionPane.showMessageDialog(Application.getMainWindow(), "Incompatible time model!\nWhen using the operating mode TestBedReal, please choose a continuous and non-accelerated time model.", "Incompatible Time Model!", JOptionPane.WARNING_MESSAGE);
			}
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
