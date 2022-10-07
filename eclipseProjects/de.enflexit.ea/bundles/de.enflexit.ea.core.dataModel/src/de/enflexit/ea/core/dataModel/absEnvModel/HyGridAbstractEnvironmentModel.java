package de.enflexit.ea.core.dataModel.absEnvModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import agentgui.core.application.Application;
import agentgui.core.charts.timeseriesChart.TimeSeriesLengthRestriction;
import agentgui.core.gui.projectwindow.simsetup.TimeModelController;
import agentgui.simulationService.environment.AbstractEnvironmentModel;
import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.time.TimeModel;
import agentgui.simulationService.time.TimeModelContinuous;
import agentgui.simulationService.time.TimeModelDiscrete;
import de.enflexit.common.SerialClone;
import de.enflexit.common.ServiceFinder;
import de.enflexit.ea.core.dataModel.deployment.DeploymentSettings;
import de.enflexit.ea.core.dataModel.deployment.SetupExtension;
import de.enflexit.ea.core.dataModel.graphLayout.AbstractGraphElementLayoutSettings;
import de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService;
import energy.GlobalInfo;
import energy.helper.UnitConverter;
import energy.optionModel.ScheduleLengthRestriction;
import energy.schedule.ScheduleTransformerKeyValueConfiguration;

/**
 * The Class AbstractEnvironmentModel represents an additional data model that can be 
 * used within the simulation. It will be placed by the HyGridPlugIn in the 
 * {@link EnvironmentModel}.
 * 
 *  @see EnvironmentModel
 *  @see EnvironmentModel#setAbstractEnvironment(Object)
 *  @see EnvironmentModel#getAbstractEnvironment()
 *  @see HyGridPlugIn#onPrepareForSaving()
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HyGridAbstractEnvironmentModel", propOrder = {
    "simulationIntervalLength",
    "simulationIntervalUnitIndex",
    "isDiscreteSnapshotSimulation",
    "snapshotDecisionLocation",
    "snapshotCentralDecisionClass",
    "networkCalculationIntervalLength",
    "networkCalculationIntervalUnitIndex",
    "stateTransmission",
    "executionDataBase",
    "saveRuntimeInformationToDatabase",
    "energyTransmissionConfiguration",
    "displayUpdateConfiguration",
    "graphElementLayoutSettingsPersisted",
    "scheduleLengthRestriction",
    "deploymentSettings"
})
public class HyGridAbstractEnvironmentModel extends AbstractEnvironmentModel {

	private static final long serialVersionUID = 8372587783223307635L;

	/** The possible types of {@link TimeModel} that can be used within an simulation. */
	public static enum TimeModelType {
		TimeModelDiscrete,
		TimeModelContinuous
	}
	
	public enum StateTransmission {
		AsDefined,
		Reduced
	}
	
	/** The location for control decisions in snapshot simulations that is either within agents or in a specific class */
	public static enum SnapshotDecisionLocation {
		Decentral,
		Central
	}
	
	public enum ExecutionDataBase {
		NodePowerFlows,
		SensorData
	}
	
	@XmlTransient private SimulationStatus simulationStatus;
	@XmlTransient private TimeModelType timeModelType;
	
	@XmlTransient private SetupExtension setupExtension;
	
	private long networkCalculationIntervalLength = 5;
	private int networkCalculationIntervalUnitIndex = 1;
	
	private StateTransmission stateTransmission;
	
	private long simulationIntervalLength = 5;
	private int simulationIntervalUnitIndex = 1;
	
	private boolean isDiscreteSnapshotSimulation;
	private SnapshotDecisionLocation snapshotDecisionLocation;
	private String snapshotCentralDecisionClass;
	
	private ExecutionDataBase executionDataBase;
	private boolean saveRuntimeInformationToDatabase;
	
	private ScheduleTransformerKeyValueConfiguration energyTransmissionConfiguration;
	private DisplayUpdateConfiguration displayUpdateConfiguration;
	
	@XmlTransient
	private TreeMap<String, AbstractGraphElementLayoutSettings> graphElementLayoutSettings;
	
	private ArrayList<GraphElementLayoutSettingsPersistenceTreeMap> graphElementLayoutSettingsPersisted;
	
	private ScheduleLengthRestriction scheduleLengthRestriction;
	
	private DeploymentSettings deploymentSettings;
	
	
	/**
	 * Instantiates a new abstract environment model.
	 */
	public HyGridAbstractEnvironmentModel() { }
	
	// --------------------------------------------------------------
	// --- From here, NON-PERSISTED runtime information -------------
	// --------------------------------------------------------------
	/**
	 * Sets the new simulation status.
	 * @param simulationStatus the new simulation status
	 */
	public void setSimulationStatus(SimulationStatus simulationStatus) {
		this.simulationStatus = simulationStatus;
	}
	/**
	 * Returns the current simulation status.
	 * @return the simulation status
	 */
	public SimulationStatus getSimulationStatus() {
		if (this.simulationStatus==null) {
			this.simulationStatus = new SimulationStatus();
		}
		return this.simulationStatus;
	}
	
	/**
	 * Sets the current time model type according to the specified {@link TimeModel}.
	 * @param timeModelUsed the currently used time model 
	 */
	public void setTimeModelType(TimeModel timeModelUsed) {
		if (timeModelUsed instanceof TimeModelDiscrete) {
			this.timeModelType = TimeModelType.TimeModelDiscrete;
		} else if (timeModelUsed instanceof TimeModelContinuous) {
			this.timeModelType = TimeModelType.TimeModelContinuous;			
		}
	}
	/**
	 * Sets the current time model type.
	 * @param timeModelType the new time model type
	 */
	public void setTimeModelType(TimeModelType timeModelType) {
		this.timeModelType = timeModelType;
	}
	/**
	 * Returns the current {@link TimeModelType} <b>at the runtime of the agent system</b>.<br>
	 * Do not use this method to check the configuration before runtime! Instead, use the projects
	 * {@link TimeModelController} to check the current {@link TimeModel}.
	 *  
	 * @return the time model type
	 */
	public TimeModelType getTimeModelType() {
		return this.timeModelType;
	}
	
	/**
	 * Sets the SetupExtension.
	 * @param hygridSetupExtension the new setup extension
	 */
	public void setSetupExtension(SetupExtension hygridSetupExtension) {
		this.setupExtension = hygridSetupExtension;
	}
	/**
	 * Returns the SetupExtension.
	 * @return the setup extension
	 */
	@XmlTransient
	public SetupExtension getSetupExtension() {
		return setupExtension;
	}
	
	
	// --------------------------------------------------------------
	// --- From here, PERSISTED runtime information -----------------
	// --------------------------------------------------------------
	/**
	 * Gets the network calculation interval length.
	 * @return the network calculation interval length
	 */
	public long getNetworkCalculationIntervalLength() {
		return networkCalculationIntervalLength;
	}
	/**
	 * Sets the network calculation interval length.
	 * @param networkCalculationIntervalLength the new network calculation interval length
	 */
	public void setNetworkCalculationIntervalLength(long networkCalculationIntervalLength) {
		this.networkCalculationIntervalLength = networkCalculationIntervalLength;
	}
	
	/**
	 * Gets the network calculation interval unit index.
	 * @return the network calculation interval unit index
	 */
	public int getNetworkCalculationIntervalUnitIndex() {
		return networkCalculationIntervalUnitIndex;
	}
	/**
	 * Sets the network calculation interval unit index.
	 * @param networkCalculationIntervalUnitIndex the new network calculation interval unit index
	 */
	public void setNetworkCalculationIntervalUnitIndex(int networkCalculationIntervalUnitIndex) {
		this.networkCalculationIntervalUnitIndex = networkCalculationIntervalUnitIndex;
	}
	
	
	/**
	 * Returns the kind of state transmission.
	 * @return the state transmission
	 */
	public StateTransmission getStateTransmission() {
		if (stateTransmission==null) {
			stateTransmission = StateTransmission.Reduced;
		}
		return stateTransmission;
	}
	/**
	 * Sets the kind of state transmission.
	 * @param stateTransmission the new state transmission
	 */
	public void setStateTransmission(StateTransmission stateTransmission) {
		this.stateTransmission = stateTransmission;
	}
	
	
	/**
	 * Gets the simulation interval length.
	 * @return the simulation interval length
	 */
	public long getSimulationIntervalLength() {
		return simulationIntervalLength;
	}
	/**
	 * Sets the simulation interval length.
	 * @param simulationIntervalLength the new simulation interval length
	 */
	public void setSimulationIntervalLength(long simulationIntervalLength) {
		this.simulationIntervalLength = simulationIntervalLength;
	}

	/**
	 * Gets the simulation interval unit index.
	 * @return the simulation interval unit index
	 */
	public int getSimulationIntervalUnitIndex() {
		return simulationIntervalUnitIndex;
	}
	/**
	 * Sets the simulation interval unit index.
	 * @param simulationIntervalUnitIndex the new simulation interval unit index
	 */
	public void setSimulationIntervalUnitIndex(int simulationIntervalUnitIndex) {
		this.simulationIntervalUnitIndex = simulationIntervalUnitIndex;
	}

	
	
	/**
	 * Returns if the current discrete simulation is a snapshot simulation.
	 * @return true, if is snapshot simulation
	 */
	public boolean isDiscreteSnapshotSimulation() {
		return isDiscreteSnapshotSimulation;
	}
	/**
	 * Sets the discrete snapshot simulation.
	 * @param isDiscreteSnapshotSimulation the new discrete snapshot simulation
	 */
	public void setDiscreteSnapshotSimulation(boolean isDiscreteSnapshotSimulation) {
		this.isDiscreteSnapshotSimulation = isDiscreteSnapshotSimulation;
	}
	
	/**
	 * Returns the decision location for snapshot simulations.
	 * @return the snapshot decision location
	 */
	public SnapshotDecisionLocation getSnapshotDecisionLocation() {
		return snapshotDecisionLocation;
	}
	/**
	 * Sets the decision location for snapshot simulations.
	 * @param snapshotDecisionLocation the new snapshot decision location
	 */
	public void setSnapshotDecisionLocation(SnapshotDecisionLocation snapshotDecisionLocation) {
		this.snapshotDecisionLocation = snapshotDecisionLocation;
	}
	
	/**
	 * Returns the snapshot central decision class.
	 * @return the snapshot central decision class
	 */
	public String getSnapshotCentralDecisionClass() {
		return snapshotCentralDecisionClass;
	}
	/**
	 * Sets the snapshot central decision class.
	 * @param snapshotCentralDecisionClass the new snapshot central decision class
	 */
	public void setSnapshotCentralDecisionClass(String snapshotCentralDecisionClass) {
		this.snapshotCentralDecisionClass = snapshotCentralDecisionClass;
	}
	
	/**
	 * Returns the configured data base for the execution of the simulation.
	 * @return the simulation data base
	 */
	public ExecutionDataBase getExecutionDataBase() {
		if (executionDataBase==null) {
			executionDataBase = ExecutionDataBase.NodePowerFlows;
		}
		return executionDataBase;
	}
	/**
	 * Sets the simulation data base.
	 * @param executionDataBase the new execution data base
	 */
	public void setExecutionDataBase(ExecutionDataBase executionDataBase) {
		this.executionDataBase = executionDataBase;
	}
		
	/**
	 * Checks if runtime information are to be save to database.
	 * @return true, if is save runtime information to database
	 */
	public boolean isSaveRuntimeInformationToDatabase() {
		return saveRuntimeInformationToDatabase;
	}
	/**
	 * Sets the save runtime information to database.
	 * @param saveRuntimeInformationToDatabase the new save runtime information to database
	 */
	public void setSaveRuntimeInformationToDatabase(boolean saveRuntimeInformationToDatabase) {
		this.saveRuntimeInformationToDatabase = saveRuntimeInformationToDatabase;
	}
	
	/**
	 * Sets the energy transmission configuration.
	 * @param energyTransmissionConfiguration the new energy transmission configuration
	 */
	public void setEnergyTransmissionConfiguration(ScheduleTransformerKeyValueConfiguration energyTransmissionConfiguration) {
		if (energyTransmissionConfiguration instanceof ScheduleTransformerKeyValueConfiguration) {
			this.energyTransmissionConfiguration = energyTransmissionConfiguration;
		}
	}
	/**
	 * Returns the energy transmission configuration.
	 * @return the energy transmission configuration
	 */
	public ScheduleTransformerKeyValueConfiguration getEnergyTransmissionConfiguration() {
		if (energyTransmissionConfiguration==null) {
			energyTransmissionConfiguration = new ScheduleTransformerKeyValueConfiguration();
		}
		return energyTransmissionConfiguration;
	}

	
	/**
	 * Sets the display update configuration.
	 * @param displayUpdateConfiguration the new display update configuration
	 */
	public void setDisplayUpdateConfiguration(DisplayUpdateConfiguration displayUpdateConfiguration) {
		this.displayUpdateConfiguration = displayUpdateConfiguration;
	}
	/**
	 * Gets the display update configuration.
	 * @return the display update configuration
	 */
	public DisplayUpdateConfiguration getDisplayUpdateConfiguration() {
		if (displayUpdateConfiguration==null) {
			displayUpdateConfiguration = new DisplayUpdateConfiguration();
		}
		return displayUpdateConfiguration;
	}

	/**
	 * Gets the graph element layout settings for domains.
	 * @return the graph element layout settings for domains
	 */
	public TreeMap<String, AbstractGraphElementLayoutSettings> getGraphElementLayoutSettings() {
		if (graphElementLayoutSettings==null) {
			graphElementLayoutSettings = new TreeMap<>();
			
			List<GraphElementLayoutService> layoutServices = ServiceFinder.findServices(GraphElementLayoutService.class);
			for (int i = 0; i < layoutServices.size(); i++) {
				GraphElementLayoutService layoutService = layoutServices.get(i);
				GraphElementLayoutSettingsPersistenceTreeMap settingsTreeMap = this.getGraphElementSettingsForDomain(layoutService.getDomain());
				if (settingsTreeMap!=null) {
					AbstractGraphElementLayoutSettings layoutSettings = layoutService.convertTreeMapToInstance(settingsTreeMap);
					if (layoutSettings!=null) {
						this.getGraphElementLayoutSettings().put(layoutService.getDomain(), layoutSettings);
					}
				}
			}
		}
		return graphElementLayoutSettings;
	}
	/**
	 * Sets the graph element layout settings for domains.
	 * @param graphElementLayoutSettingsForDomains the graph element layout settings for domains
	 */
	public void setGraphElementLayoutSettings(TreeMap<String, AbstractGraphElementLayoutSettings> graphElementLayoutSettingsForDomains) {
		this.graphElementLayoutSettings = graphElementLayoutSettingsForDomains;
	}

	
	/**
	 * Gets the graph element layout settings.
	 * @return the graph element layout settings
	 */
	private ArrayList<GraphElementLayoutSettingsPersistenceTreeMap> getGraphElementLayoutSettingsPersisted() {
		if (graphElementLayoutSettingsPersisted==null) {
			graphElementLayoutSettingsPersisted = new ArrayList<GraphElementLayoutSettingsPersistenceTreeMap>();
		}
		return graphElementLayoutSettingsPersisted;
	}
	/**
	 * Sets the graph element layout settings.
	 * @param graphElementLayoutSettings the new graph element layout settings
	 */
	private void setGraphElementLayoutSettingsPersisted(ArrayList<GraphElementLayoutSettingsPersistenceTreeMap> graphElementLayoutSettings) {
		this.graphElementLayoutSettingsPersisted = graphElementLayoutSettings;
	}
	
	/**
	 * Gets the settings for the specified domain.
	 * @param domain the domain
	 * @return the settings, null if not found
	 */
	public GraphElementLayoutSettingsPersistenceTreeMap getGraphElementSettingsForDomain(String domain) {
		for (GraphElementLayoutSettingsPersistenceTreeMap settings : this.getGraphElementLayoutSettingsPersisted()) {
			if (settings.getDomain().equals(domain)) {
				return settings;
			}
		}
		return null;
	}
	/**
	 * Adds the graph element settings tree map.
	 * @param treeMap the tree map
	 */
	public void addGraphElementSettingsTreeMap(GraphElementLayoutSettingsPersistenceTreeMap treeMap) {
		// --- Make sure there is only one TreeMap per domain ------- 
		if (this.getGraphElementSettingsForDomain(treeMap.getDomain())!=null) {
			this.getGraphElementLayoutSettingsPersisted().remove(this.getGraphElementSettingsForDomain(treeMap.getDomain()));
		}
		this.getGraphElementLayoutSettingsPersisted().add(treeMap);
		Collections.sort(this.getGraphElementLayoutSettingsPersisted());
	}

	/**
	 * Sets the schedule length restriction.
	 * @param newScheduleLengthRestriction the new schedule length restriction
	 */
	public void setScheduleLengthRestriction(ScheduleLengthRestriction newScheduleLengthRestriction ) {
		this.scheduleLengthRestriction = newScheduleLengthRestriction;
		// --- Set HyGrid settings to the EOM settings ---- 
		GlobalInfo.setScheduleLengthRestriction(newScheduleLengthRestriction);
		
		// --- Set HyGrid settings to Agent.Workbenck -----
		TimeSeriesLengthRestriction tsLengthRestriction = new TimeSeriesLengthRestriction();
		long maxDurationMillis = UnitConverter.convertDurationToMilliseconds(newScheduleLengthRestriction.getDuration());
		tsLengthRestriction.setMaxDuration(maxDurationMillis);
		tsLengthRestriction.setMaxNumberOfStates(newScheduleLengthRestriction.getMaxNumberOfSystemStates());
		
		Application.getGlobalInfo().setTimeSeriesLengthRestriction(tsLengthRestriction);
	}
	/**
	 * Returns the schedule length restriction.
	 * @return the schedule length restriction
	 */
	public ScheduleLengthRestriction getScheduleLengthRestriction() {
		if (scheduleLengthRestriction==null) {
			scheduleLengthRestriction = GlobalInfo.getScheduleLengthRestriction();
		}
		return scheduleLengthRestriction;
	}
	
	/**
	 * Sets the deployment settings.
	 * @param deploymentSettings the new deployment settings
	 */
	public void setDeploymentSettings(DeploymentSettings deploymentSettings) {
		this.deploymentSettings = deploymentSettings;
	}
	/**
	 * Returns the deployment settings.
	 * @return the deployment settings
	 */
	public DeploymentSettings getDeploymentSettings() {
		if (deploymentSettings==null) {
			deploymentSettings = new DeploymentSettings();
		}
		return deploymentSettings;
	}
	
	/* (non-Javadoc)
	 * @see agentgui.core.environment.AbstractEnvironmentModel#getCopy()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public HyGridAbstractEnvironmentModel getCopy() {
		
		HyGridAbstractEnvironmentModel copy = new HyGridAbstractEnvironmentModel();
		copy.setSimulationStatus(this.getSimulationStatus().getCopy());
		if (this.getSetupExtension()!=null) {
			copy.setSetupExtension(this.getSetupExtension().getCopy());
		}
		
		// --- Time model type ----------------------------
		copy.setTimeModelType(this.getTimeModelType());
		
		// --- Continuous time model ----------------------
		copy.setNetworkCalculationIntervalLength(this.getNetworkCalculationIntervalLength());
		copy.setNetworkCalculationIntervalUnitIndex(this.getNetworkCalculationIntervalUnitIndex());
		copy.setStateTransmission(this.getStateTransmission());
		copy.setEnergyTransmissionConfiguration(this.getEnergyTransmissionConfiguration().getCopy());

		// --- Discrete time model ------------------------
		copy.setSimulationIntervalLength(this.getSimulationIntervalLength());
		copy.setSimulationIntervalUnitIndex(this.getSimulationIntervalUnitIndex());
		copy.setDiscreteSnapshotSimulation(this.isDiscreteSnapshotSimulation());
		copy.setSnapshotDecisionLocation(this.getSnapshotDecisionLocation());
		copy.setSnapshotCentralDecisionClass(this.getSnapshotCentralDecisionClass());
		
		// --- Data handling ------------------------------
		copy.setExecutionDataBase(this.getExecutionDataBase());
		copy.setScheduleLengthRestriction(SerialClone.clone(this.getScheduleLengthRestriction()));
		copy.setSaveRuntimeInformationToDatabase(this.isSaveRuntimeInformationToDatabase());
		
		// --- Visualization settings ---------------------
		copy.setDisplayUpdateConfiguration(this.getDisplayUpdateConfiguration().getCopy());
		copy.setGraphElementLayoutSettings(this.getGraphElementLayoutSettings()); 
		copy.setGraphElementLayoutSettingsPersisted((ArrayList<GraphElementLayoutSettingsPersistenceTreeMap>) this.getGraphElementLayoutSettingsPersisted().clone());
		
		// --- Deployment settings ------------------------
		copy.setDeploymentSettings(this.getDeploymentSettings().getCopy());
		
		// --- Maybe to be extended ... ------------
		return copy;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObj) {

		if (compObj==null) return false;
		if (compObj==this) return true;
		if (!(compObj instanceof HyGridAbstractEnvironmentModel)) return false;

		// ------------------------------------------------
		// --- Compare object type ------------------------
		HyGridAbstractEnvironmentModel hyGridComp = (HyGridAbstractEnvironmentModel) compObj;

		// ------------------------------------------------
		// --- SimulationStatus ---------------------------
		if (hyGridComp.getSimulationStatus().equals(this.getSimulationStatus())==false) return false;
		
		// ------------------------------------------------
		// --- Compare SetupExtension ---------------------
		SetupExtension seHyGridComp = hyGridComp.getSetupExtension();
		SetupExtension seThis = this.getSetupExtension(); 
		if (seHyGridComp==null && seThis==null) {
			// -- equal !
		} else if ((seHyGridComp!=null && seThis==null) || (seHyGridComp==null && seThis!=null)) {
			return false;
		} else {
			if (seHyGridComp.equals(seThis)==false) return false;
		}

		// ------------------------------------------------
		// --- Compare Time Model Type --------------------
		if (hyGridComp.getTimeModelType()!=this.getTimeModelType()) return false;
		
		// ------------------------------------------------
		// --- Continuous time model ----------------------
		if (hyGridComp.getNetworkCalculationIntervalLength()!=this.getNetworkCalculationIntervalLength()) return false;
		if (hyGridComp.getNetworkCalculationIntervalUnitIndex()!= this.getNetworkCalculationIntervalUnitIndex()) return false;
		if (hyGridComp.getStateTransmission()!=this.getStateTransmission()) return false;
		if (hyGridComp.getEnergyTransmissionConfiguration().equals(this.getEnergyTransmissionConfiguration())==false) return false;

		// ------------------------------------------------
		// --- Discrete time model ------------------------
		if (hyGridComp.getSimulationIntervalLength()!=this.getSimulationIntervalLength()) return false;
		if (hyGridComp.getSimulationIntervalUnitIndex()!=this.getSimulationIntervalUnitIndex()) return false;
		if (hyGridComp.isDiscreteSnapshotSimulation()!=this.isDiscreteSnapshotSimulation()) return false;
		if (hyGridComp.getSnapshotDecisionLocation()!=this.getSnapshotDecisionLocation()) return false;
		String cdcHyGridComp = hyGridComp.getSnapshotCentralDecisionClass();
		String cdcThis = this.getSnapshotCentralDecisionClass();
		if (cdcHyGridComp==null && cdcThis==null) {
			// -- equal !
		} else if ((cdcHyGridComp!=null && cdcThis==null) || (cdcHyGridComp==null && cdcThis!=null)) {
			return false;
		} else {
			if (cdcHyGridComp.equals(cdcThis)==false) return false;
		}
		
		// ------------------------------------------------
		// --- Data handling ------------------------------
		if (hyGridComp.getExecutionDataBase()!=this.getExecutionDataBase()) return false;
		
		ScheduleLengthRestriction slrComp = hyGridComp.getScheduleLengthRestriction();
		ScheduleLengthRestriction slrThis = this.getScheduleLengthRestriction();
		long slrDurationComp = UnitConverter.convertDurationToMilliseconds(slrComp.getDuration());
		long slrDurationThis = UnitConverter.convertDurationToMilliseconds(slrThis.getDuration());
		if (slrDurationComp!=slrDurationThis) return false;
		if (slrComp.getMaxNumberOfSystemStates()!=slrThis.getMaxNumberOfSystemStates()) return false;
		
		if (hyGridComp.isSaveRuntimeInformationToDatabase()!=this.isSaveRuntimeInformationToDatabase()) return false;
		
		// ------------------------------------------------
		// --- Visualization settings ---------------------
		if (hyGridComp.getDisplayUpdateConfiguration().equals(this.getDisplayUpdateConfiguration())==false) return false;
		
		TreeMap<String, AbstractGraphElementLayoutSettings> gelsTreeMapComp = hyGridComp.getGraphElementLayoutSettings();
		TreeMap<String, AbstractGraphElementLayoutSettings> gelsTreeMapThis = this.getGraphElementLayoutSettings();
		if (gelsTreeMapComp.size()!=gelsTreeMapThis.size()) return false;
		
		List<String> gelsDomainNameList = new ArrayList<String>(gelsTreeMapComp.keySet());
		for (int i = 0; i < gelsDomainNameList.size(); i++) {
			String gelsDomainName = gelsDomainNameList.get(i);
			AbstractGraphElementLayoutSettings gelsComp = gelsTreeMapComp.get(gelsDomainName);
			AbstractGraphElementLayoutSettings gelsThis = gelsTreeMapComp.get(gelsDomainName);
			if (gelsComp==null && gelsThis==null) {
				// - equal, but will not happen -
			} else if ((gelsComp!=null && gelsThis==null) || (gelsComp==null && gelsThis!=null)) {
				return false;
			} else {
				if (gelsComp.equals(gelsThis)==false) return false;
			}
		}

		// ------------------------------------------------
		// --- Deployment settings ------------------------
		if (hyGridComp.getDeploymentSettings().equals(this.getDeploymentSettings())==false) return false;

		// --- Maybe to be extended ... ------------
		return true;
	}
	
}
