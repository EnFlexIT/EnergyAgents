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
    "networkCalculationIntervalLength",
    "networkCalculationIntervalUnitIndex",
    "executionDataBase",
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
	
	public enum ExecutionDataBase {
		NodePowerFlows,
		SensorData
	}
	
	@XmlTransient private SimulationStatus simulationStatus;
	@XmlTransient private TimeModelType timeModelType;
	
	@XmlTransient private SetupExtension setupExtension;
	
	private long simulationIntervalLength = 5;
	private int simulationIntervalUnitIndex = 1;
	
	private long networkCalculationIntervalLength = 5;
	private int networkCalculationIntervalUnitIndex = 1;
	
	private ExecutionDataBase executionDataBase;
	
	private ScheduleTransformerKeyValueConfiguration energyTransmissionConfiguration;
	private DisplayUpdateConfiguration displayUpdateConfiguration;
	
	@XmlTransient
	private TreeMap<String, AbstractGraphElementLayoutSettings> graphElementLayoutSettings;
	
	private ArrayList<GraphElementLayoutSettingsPersistenceTreeMap> graphElementLayoutSettingsPersisted;
	
	private ScheduleLengthRestriction  scheduleLengthRestriction;
	
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
	 * Gets the current time model type.
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
			for (GraphElementLayoutService layoutService : layoutServices) {
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
	public GraphElementLayoutSettingsPersistenceTreeMap getGraphElementSettingsForDomain (String domain) {
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
	 * Gets the deployment settings.
	 * @return the deployment settings
	 */
	public DeploymentSettings getDeploymentSettingsModel() {
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
		copy.setTimeModelType(this.getTimeModelType());
		if (this.getSetupExtension()!=null) {
			copy.setSetupExtension(this.getSetupExtension().getCopy());
		}
		
		copy.setSimulationIntervalLength(this.getSimulationIntervalLength());
		copy.setSimulationIntervalUnitIndex(this.getSimulationIntervalUnitIndex());
		copy.setNetworkCalculationIntervalLength(this.getNetworkCalculationIntervalLength());
		copy.setNetworkCalculationIntervalUnitIndex(this.getNetworkCalculationIntervalUnitIndex());
		copy.setExecutionDataBase(this.getExecutionDataBase());
		copy.setEnergyTransmissionConfiguration(this.getEnergyTransmissionConfiguration().getCopy());
		copy.setDisplayUpdateConfiguration(this.getDisplayUpdateConfiguration().getCopy());
		copy.setGraphElementLayoutSettings(this.getGraphElementLayoutSettings()); 
		copy.setGraphElementLayoutSettingsPersisted((ArrayList<GraphElementLayoutSettingsPersistenceTreeMap>) this.getGraphElementLayoutSettingsPersisted().clone());
		copy.setDeploymentSettings(this.getDeploymentSettingsModel().getCopy());
		copy.setScheduleLengthRestriction(SerialClone.clone(this.getScheduleLengthRestriction()));
		// --- Maybe to be extended ... ------------
		return copy;
	}

}
