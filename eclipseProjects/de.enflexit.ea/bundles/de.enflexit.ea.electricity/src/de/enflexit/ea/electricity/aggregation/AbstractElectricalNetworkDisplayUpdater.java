package de.enflexit.ea.electricity.aggregation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.visualisation.notifications.DataModelNotification;

import agentgui.ontology.TimeSeries;
import agentgui.ontology.TimeSeriesChart;
import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.aggregation.AbstractNetworkModelDisplayUpdater;
import de.enflexit.ea.core.aggregation.trafficLight.TrafficLightStateMessage;
import de.enflexit.ea.core.aggregation.trafficLight.TrafficLightStatePanel;
import de.enflexit.ea.core.aggregation.trafficLight.TrafficLight.TrafficLightColor;
import de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.StaticComponentProperties;
import energy.DisplayHelper;
import energy.optionModel.AbstractUsageOfInterface;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;
import hygrid.env.ColorSettingsCollection;
import hygrid.env.DisplayUpdateConfiguration.UpdateMechanism;

/**
 * Common superclass for {@link AbstractNetworkModelDisplayUpdater} implementations for electricity grids,
 * providing methods that can be used by all  
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public abstract class AbstractElectricalNetworkDisplayUpdater extends AbstractNetworkModelDisplayUpdater {
	
	protected static final int DATA_MODEL_PROPERTIES_INDEX = 0;
	protected static final int DATA_MODEL_STATE_INDEX = 1;
	protected static final int DATA_MODEL_TIME_SERIES_INDEX = 2;
	
	private float networkMinimumNodeVoltage = Float.MAX_VALUE;
	private float networkMaximumNodeVoltage = Float.MIN_VALUE;
	private float networkMaximumCableUtilization = Float.MIN_VALUE;
	
	private TrafficLightColor trafficLightColorVoltageOld;
	private TrafficLightColor trafficLightColorCableOld;
	private TrafficLightColor trafficLightColorCableNetworkOld;
	
	private AbstractElectricalNetworkCalculationStrategy netClacStrategy;
	
	protected abstract GraphElementLayoutService getLayoutService();
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractNetworkModelDisplayUpdater#getDomain()
	 */
	@Override
	protected String getSubNetwork() {
		return this.getLayoutService().getDomain();
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractNetworkModelDisplayUpdater#updateNetworkModelDisplay(java.util.HashMap, long)
	 */
	@Override
	protected void updateNetworkModelDisplay(HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates, long displayTime) {
		UpdateMechanism disUpdateMechanism = this.getAggregationHandler().getHyGridAbstractEnvironmentModel().getDisplayUpdateConfiguration().getUpdateMechanism();
		boolean isDoDisplayUpdates = (disUpdateMechanism==null || disUpdateMechanism==UpdateMechanism.EnableUpdates);
		if (isDoDisplayUpdates==true) {
			this.setStateUpdates(lastStateUpdates);
			this.setDisplayTime(displayTime);
			this.applyToNetworkVisualisation();
		}

	}
	
	/**
	 * Applies the last changes to the network visualization.
	 */
	protected abstract void applyToNetworkVisualisation();
	
	/**
	 * Creates the visualization elements for a GraphNode.
	 *
	 * @param graphNodeID the graph node id
	 * @param sysStateChanges the system states changes
	 * @return the time series settings
	 */
	protected TimeSeriesSettings createVisualisationElementsForGraphNode(String graphNodeID, HashMap<String, TechnicalSystemStateEvaluation> sysStateChanges) {

		// --- Get and clone the graph node -------------------------
		GraphNode graphNodeFromModel = (GraphNode) this.getNetworkModel().getGraphElement(graphNodeID);
		GraphNode graphNode = SerialClone.clone(graphNodeFromModel);
		
		ElectricalNodeProperties nodeProperties = null;

		Object[] domainDataModel = this.getDomainSpecificDataModel(graphNode.getDataModel());
		if (domainDataModel!=null) {
			nodeProperties = (ElectricalNodeProperties) domainDataModel[DATA_MODEL_PROPERTIES_INDEX];
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] No data model found for node " + graphNodeID);
		}
		
		this.getLayoutService().getDomain();

		// --- Create the chart and set the data model --------------
		TimeSeriesChart tsc = this.createTimeSeriesChart(graphNodeID, "Power & Voltage", "Power / Voltage");
		Object[] dataModelArray = new Object[3];
		dataModelArray[DATA_MODEL_PROPERTIES_INDEX] = nodeProperties;
		dataModelArray[DATA_MODEL_STATE_INDEX] = this.getNetworkCalculationStrategy().getGraphNodeStates().get(graphNodeID);
		dataModelArray[DATA_MODEL_TIME_SERIES_INDEX] = tsc;
		
		// --- Get domain of current handler and GraphNode ----------
		String currentDomain = this.getSubAggregationConfiguration().getDomain();
		List<String> domainList = this.getNetworkModel().getDomain(graphNodeFromModel);
		if (domainList.size()==1) {
			// --- Single-Domain node -------------------------------
			graphNode.setDataModel(dataModelArray);
		} else {
			// --- Multi-Domain GraphNode ---------------------------
			TreeMap<String, Object> dmTreeMap = new TreeMap<>();
			dmTreeMap.put(currentDomain, dataModelArray);
			graphNode.setDataModel(dmTreeMap);
		}
		
		// --- Create new TimeSeriesSettings ------------------------
		Vector<NetworkComponent> netComps = this.getNetworkModel().getNetworkComponentVectorWithDistributionNodeAsLast(this.getNetworkModel().getNetworkComponents(graphNode));
		NetworkComponent netComp = netComps.get(netComps.size()-1);
		TechnicalSystemStateEvaluation tsse = sysStateChanges.get(netComp.getId());
		TimeSeriesSettings tsSettings = new TimeSeriesSettings(graphNode, currentDomain, netComp);

		String powerUnit = null;
		if (tsse!=null) {
			//TODO find interface for the right domain/subnetwork
			UsageOfInterfaceEnergy usageOfInterfaceEnergy = this.findFirstUsageOfInterfaceEnergy(tsse.getUsageOfInterfaces());
			if (usageOfInterfaceEnergy!=null) {
				EnergyFlowInWatt energyFlow = usageOfInterfaceEnergy.getEnergyFlow();
				if (energyFlow!=null) {
					powerUnit = DisplayHelper.getDisplayTextForEnergyUnitFactorPrefixSI(energyFlow.getSIPrefix()) + "W"; 
				}
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] No energy interface usage defined for " + netComp.getId());
			}
		}
		
		// --- Define the TimeSeries ------------------------------------------
		List<TimeSeries> timeSeries = this.createTimeSeriesForNodeComponent(powerUnit);
		for (int i=0; i<timeSeries.size(); i++) {
			tsSettings.addTimeSeries(timeSeries.get(i));
		}
		
		// --- Send the display notification ------------------------
		this.addDisplayNotification(new DataModelNotification(graphNode));

		// --- Remember the TimeSeriesSettings ----------------------
		this.getTimeSeriesSettingsHash().put(graphNodeID, tsSettings);
		return tsSettings; 
	}
	
	/**
	 * Find the first {@link UsageOfInterfaceEnergy} instance in a list of {@link AbstractUsageOfInterface}s.
	 * @param usageOfInterfaces the usage of interfaces
	 * @return the usage of interface energy
	 */
	private UsageOfInterfaceEnergy findFirstUsageOfInterfaceEnergy(List<AbstractUsageOfInterface> usageOfInterfaces) {
		for (int i=0; i<usageOfInterfaces.size(); i++) {
			AbstractUsageOfInterface abstractUsage = usageOfInterfaces.get(i);
			if (abstractUsage instanceof UsageOfInterfaceEnergy) {
				return (UsageOfInterfaceEnergy) abstractUsage;
			}
		}
		return null;
	}
	
	/**
	 * Creates the visualisation elements for a NetworkComponent.
	 *
	 * @param netCompID the ID of the NetworkComponent
	 * @return the time series settings
	 */
	protected TimeSeriesSettings createVisualisationElementsForNetworkComponent(String netCompID) {

		NetworkComponent netComp = SerialClone.clone(this.getNetworkModel().getNetworkComponent(netCompID));
		
		// --- Create the chart and set the data model --------------
		TimeSeriesChart tsc = this.createTimeSeriesChart(netCompID, "Current & Utilization", "Current / Utilization");
		
		StaticComponentProperties componentProperties = null;
		Object[] domainDataModel = this.getDomainSpecificDataModel(netComp.getDataModel());
		if (domainDataModel!=null) {
			componentProperties = (StaticComponentProperties) domainDataModel[DATA_MODEL_PROPERTIES_INDEX];
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] No data model found for network component " + netCompID);
		}
		
		Object[] dataModelArray = new Object[3];
		dataModelArray[DATA_MODEL_PROPERTIES_INDEX] = componentProperties;
		dataModelArray[DATA_MODEL_STATE_INDEX] = this.getNetworkCalculationStrategy().getNetworkComponentStates().get(netCompID);
		dataModelArray[DATA_MODEL_TIME_SERIES_INDEX] = tsc;
		netComp.setDataModel(dataModelArray);
		
		HashSet<GraphElement> edgesFound = this.getNetworkModel().getGraphElementsOfNetworkComponent(netComp, new GraphEdge(null, null));
		GraphEdge graphEdge = (GraphEdge) edgesFound.iterator().next();
		
		// --- Create new TimeSeriesSettings ----------------------------------
		TimeSeriesSettings tsSettings = new TimeSeriesSettings(graphEdge, netComp);

		// --- Define the TimeSeries ------------------------------------------
		List<TimeSeries> timeSeries = this.createTimeSeriesForEdgeComponent();
		for (int i=0; i<timeSeries.size(); i++) {
			tsSettings.addTimeSeries(timeSeries.get(i));
		}
		
		this.addDisplayNotification(new DataModelNotification(netComp));
		
		// --- Remember the TimeSeriesSettings --------------------------------
		this.getTimeSeriesSettingsHash().put(netCompID, tsSettings);
		return tsSettings;
	}
	
	/**
	 * Creates the time series for the visualization of a node component
	 * @param powerUnit the power unit
	 * @return the list
	 */
	protected abstract List<TimeSeries> createTimeSeriesForNodeComponent(String powerUnit);
	
	/**
	 * Creates the time series for the visualization of an edge component
	 * @return the time series
	 */
	protected abstract List<TimeSeries> createTimeSeriesForEdgeComponent();
	
	/**
	 * Returns the electrical distribution grid network calculation strategy.
	 * @return the network calculation strategy
	 */
	protected AbstractElectricalNetworkCalculationStrategy getNetworkCalculationStrategy() {
		if (netClacStrategy==null) {
			netClacStrategy = (AbstractElectricalNetworkCalculationStrategy) this.getSubAggregationConfiguration().getNetworkCalculationStrategy();
		}
		return netClacStrategy;
	}

	
	// ----------------------------------------------------------------------------------
	// --- From here methods for the TraficLight handling can be found ------------------
	// ----------------------------------------------------------------------------------
	
	/**
	 * Updates the minimum node voltage, replaces it with the given value if it is smaller than the current one.
	 * @param voltage the voltage
	 */
	protected void updateNetworkMinimumNodeVoltage(float voltage) {
		if (voltage<this.networkMinimumNodeVoltage) {
			this.networkMinimumNodeVoltage = voltage;
		}
	}
	
	/**
	 * Updates the maximum node voltage, replaces it with the given value if it is greater than the current one.
	 * @param voltage the voltage
	 */
	protected void updateNetworkMaximumNodeVoltage(float voltage) {
		if (voltage>this.networkMaximumNodeVoltage) {
			this.networkMaximumNodeVoltage = voltage;
		}
	}

	/**
	 * Updates the maximum cable utilization, replaces it with the given value if it is greater than the current one.
	 * @param utilization the utilization
	 */
	protected void updateNetworkMaximumCableUtilization(float utilization) {
		if (utilization>this.networkMaximumCableUtilization) {
			this.networkMaximumCableUtilization = utilization;
		}
	}
	
	/**
	 * Sets the traffic light messages.
	 */
	protected void setTrafficLightMessages() {
		
		// --- Get the traffic light color for the node voltage ---------------
		// --- Get colors for min and max voltage, use the "redder" one ------- 
		TrafficLightColor tlcMinVoltage = this.getTrafficLightColorVoltage(this.networkMinimumNodeVoltage);
		TrafficLightColor tlcMaxVoltage = this.getTrafficLightColorVoltage(this.networkMaximumNodeVoltage);
		TrafficLightColor tlcNodeVoltage = this.getMaxTrafficLightColor(tlcMinVoltage, tlcMaxVoltage);
		
		if (this.trafficLightColorVoltageOld==null || this.trafficLightColorVoltageOld!=tlcNodeVoltage) {
			this.addGraphUiMessage(new TrafficLightStateMessage(this.getDisplayTime(), TrafficLightStatePanel.TRAFFIC_LIGHT_VOLTAGE_LEVEL, tlcNodeVoltage));
			this.trafficLightColorVoltageOld = tlcNodeVoltage;
		}
		
		// --- Get the traffic light color for the cable utilization --
		TrafficLightColor tlColorCableUtilization = this.getTrafficLightColorUtilization(this.networkMaximumCableUtilization);
		if (this.trafficLightColorCableOld==null || this.trafficLightColorCableOld!=tlColorCableUtilization) {
			this.addGraphUiMessage(new TrafficLightStateMessage(this.getDisplayTime(), TrafficLightStatePanel.TRAFFIC_LIGHT_CABLE_STATE, tlColorCableUtilization));
			this.trafficLightColorCableOld = tlColorCableUtilization;
		}
		
		// --- Set the higher of the two for the overall network state --------
		TrafficLightColor tlColorNetwork = this.getMaxTrafficLightColor(tlcNodeVoltage, tlColorCableUtilization);
		if (this.trafficLightColorCableNetworkOld==null || this.trafficLightColorCableNetworkOld!=tlColorNetwork) {
			this.addGraphUiMessage(new TrafficLightStateMessage(this.getDisplayTime(), TrafficLightStatePanel.TRAFFIC_LIGHT_NETWORK_STATE, tlColorNetwork));
			this.trafficLightColorCableNetworkOld = tlColorNetwork;
		}
		
		// --- Reset the max values for the next iteration --------------------
		this.networkMinimumNodeVoltage = Float.MAX_VALUE;
		this.networkMaximumNodeVoltage = Float.MIN_VALUE;
		this.networkMaximumCableUtilization = Float.MIN_VALUE;
		
	}
	
	/**
	 * Derives the traffic light color for voltage from the color index.
	 * @param colorIndex the color index
	 * @return the traffic light color from voltage color index
	 */
	private TrafficLightColor getTrafficLightColorFromVoltageColorIndex(int colorIndex) {
		
		TrafficLightColor tlcVoltage = TrafficLightColor.Green;

		switch (colorIndex) {
		case 0:
		case 4:
			tlcVoltage = TrafficLightColor.Red;
			break;
		case 1:
		case 3:
			tlcVoltage = TrafficLightColor.Yellow;
			break;
		case 2:
			tlcVoltage = TrafficLightColor.Green;
			break;
		}
		return tlcVoltage;
	}
	
	
	/**
	 * Derives the traffic light color for utilization from the color index.
	 * @param colorIndex the color index
	 * @return the traffic light color
	 */
	private TrafficLightColor getTrafficLightColorFromUtilizationColorIndex(int colorIndex) {
		
		TrafficLightColor tlcUtilization = TrafficLightColor.Green;
		switch (colorIndex) {
		case 0:
		case 1:
			tlcUtilization = TrafficLightColor.Green;
			break;
		case 2:
			tlcUtilization = TrafficLightColor.Yellow;
			break;
		case 3:
			tlcUtilization = TrafficLightColor.Red;
			break;
		}
		return tlcUtilization;
	}
	
	/**
	 * Returns the maximum TrafficLightColor, where green is the lowest and red the highest value.
	 *
	 * @param tl1 the first TrafficLightColor
	 * @param tl2 the second TrafficLightColor
	 * @return the traffic light color max
	 */
	protected TrafficLightColor getMaxTrafficLightColor(TrafficLightColor tl1, TrafficLightColor tl2) {
		if (tl1==tl2) return tl1;
		int tlNetworkOrdinal = Math.max(tl1.ordinal(), tl2.ordinal());
		return TrafficLightColor.values()[tlNetworkOrdinal];
	}
	
	
	/**
	 * Gets the traffic light color for the given voltage.
	 * @param voltage the voltage
	 * @return the traffic light color
	 */
	private TrafficLightColor getTrafficLightColorVoltage(float voltage) {
		ColorSettingsCollection colorSettings = this.getLayoutSettings().getColorSettingsForNodes();
		Integer colorIndex = colorSettings.getColorIndexForValue(voltage);
		return this.getTrafficLightColorFromVoltageColorIndex(colorIndex);
	}
	
	/**
	 * Gets the traffic light color for the given utilization.
	 * @param utilization the utilization
	 * @return the traffic light color
	 */
	private TrafficLightColor getTrafficLightColorUtilization(float utilization) {
		ColorSettingsCollection colorSettings = this.getLayoutSettings().getColorSettingsForEdges();
		
		// --- Get the distinct ordered index -----------------------
		Integer colorIndex = colorSettings.getColorIndexForValue(utilization);
		return this.getTrafficLightColorFromUtilizationColorIndex(colorIndex);
	}

}
