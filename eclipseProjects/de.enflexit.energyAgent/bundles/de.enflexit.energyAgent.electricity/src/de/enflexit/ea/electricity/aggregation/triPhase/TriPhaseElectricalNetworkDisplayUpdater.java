package de.enflexit.ea.electricity.aggregation.triPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElementLayout;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.visualisation.notifications.DataModelNotification;
import org.awb.env.networkModel.visualisation.notifications.UpdateTimeSeries;

import agentgui.ontology.TimeSeries;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.globalDataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.globalDataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkDisplayUpdater;
import energy.DisplayHelper;
import energy.DomainSettings;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.AbstractUsageOfInterface;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.schedule.ScheduleController;
import hygrid.env.ColorSettingsCollection;

/**
 * The Class ElectricalDistributionGridDisplayUpdater prepares the display updates for the current 
 * state within the electrical distribution grid part of the {@link NetworkModel}. 
 * Once the network component updates were transferred by the current {@link AbstractAggregationHandler},
 * this class prepares the corresponding display updates.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class TriPhaseElectricalNetworkDisplayUpdater extends AbstractElectricalNetworkDisplayUpdater {

	private static final String ACTIVE_POWER_L1 = "Active Power L1";
	private static final String ACTIVE_POWER_L2 = "Active Power L2";
	private static final String ACTIVE_POWER_L3 = "Active Power L3";
	
	private static final String REACTIVE_POWER_L1 = "Reactive Power L1";
	private static final String REACTIVE_POWER_L2 = "Reactive Power L2";
	private static final String REACTIVE_POWER_L3 = "Reactive Power L3";

	private static final String VOLTAGE_L1 = "Voltage L1";
	private static final String VOLTAGE_L2 = "Voltage L2";
	private static final String VOLTAGE_L3 = "Voltage L3";
	
	private static final String CURRENT_L1 = "Current L1";
	private static final String CURRENT_L2 = "Current L2";
	private static final String CURRENT_L3 = "Current L3";
	
	private static final String UTILIZATION_L1 = "Utilization L1";
	private static final String UTILIZATION_L2 = "Utilization L2";
	private static final String UTILIZATION_L3 = "Utilization L3";
	
	private TriPhaseElectricalNetworkGraphElementLayoutService layoutService;
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.electricity.AbstractElectricalNetworkDisplayUpdater#getLayoutService()
	 */
	@Override
	protected TriPhaseElectricalNetworkGraphElementLayoutService getLayoutService() {
		if (layoutService==null) {
			layoutService = new TriPhaseElectricalNetworkGraphElementLayoutService();
		}
		return layoutService;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.electricity.NetworkModelDisplayUpdaterElectricity#applyToNetworkVisualisation()
	 */
	@Override
	protected void applyToNetworkVisualisation() {
		
		// --------------------------------------------------------------------------
		// --- Summarize all changes that occur with the current answers ------------
		// --- that are: The changes in all GraphNodes and the changes in -----------
		// --- the 'Cable' NetworkComponents ----------------------------------------
		// --------------------------------------------------------------------------
		
		if (this.getStateUpdates()==null) return;
		
		// --- Get all affected elements from the changed elements: -----------------
		HashMap<String, TechnicalSystemStateEvaluation> sysStateChanges = this.getStateUpdates();  
	
		// --------------------------------------------------------------------------
		// --- Work on the changes of the GraphNode ---------------------------------
		// --------------------------------------------------------------------------
		ArrayList<String> nodeKeys = new ArrayList<String>(this.getNetworkCalculationStrategy().getGraphNodeStates().keySet());
		for (int i = 0; i < nodeKeys.size(); i++) {

			// --- Get all required informations for this action --------------------
			String graphNodeID = nodeKeys.get(i);
			TimeSeriesSettings tsSettings = this.getTimeSeriesSettingsHash().get(graphNodeID);
			if (tsSettings==null) {
				tsSettings = this.createVisualisationElementsForGraphNode(graphNodeID, sysStateChanges);
			} 
			
			GraphNode graphNode = tsSettings.getGraphNode();
			NetworkComponent netComp = tsSettings.getNetworkComponent();
			TriPhaseElectricalNodeState triNodeState = (TriPhaseElectricalNodeState) this.getNetworkCalculationStrategy().getGraphNodeStates().get(graphNodeID);
			TechnicalSystemStateEvaluation tsse = sysStateChanges.get(netComp.getId());
			
			// --- Set the data model with the new TriPhaseElectricalNodeState ------
			Object[] dataModel = tsSettings.getDataModelArray();
			dataModel[DATA_MODEL_STATE_INDEX] = triNodeState;
			
			DataModelNotification dmn = new DataModelNotification(graphNode);
			dmn.setDataModelPartUpdateIndex(DATA_MODEL_STATE_INDEX);
			this.addDisplayNotification(dmn);

			// --- Update the voltage level -----------------------------------------
			Vector<Float> floatVectorVoltage = this.createFloatVector(tsSettings.getTimeSeriesIndexHash().size());
			int tsIndex = -1;
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(VOLTAGE_L1);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), triNodeState.getL1().getVoltageAbs().getValue());
			floatVectorVoltage.set(tsIndex, triNodeState.getL1().getVoltageAbs().getValue());
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(VOLTAGE_L2);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), triNodeState.getL2().getVoltageAbs().getValue());
			floatVectorVoltage.set(tsIndex, triNodeState.getL2().getVoltageAbs().getValue());
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(VOLTAGE_L3);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), triNodeState.getL3().getVoltageAbs().getValue());
			floatVectorVoltage.set(tsIndex, triNodeState.getL3().getVoltageAbs().getValue());

			// --- Add a layout notification, if configured so ----------------------
			this.addGraphElementLayoutForNodes(tsSettings, triNodeState);

			// --- Define/prepare visualization updates -----------------------
			UpdateTimeSeries udtsVoltage = this.createUpdateTimeSeriesRow(graphNode, DATA_MODEL_TIME_SERIES_INDEX, this.getDisplayTime(), floatVectorVoltage);
			UpdateTimeSeries udtsEnergyFlowStateBeginning = null;
			UpdateTimeSeries udtsEnergyFlowStateEnd = null;
			
			// --- Update minimum and maximum voltages ------------------------
			float nodeMinimumVoltage = Math.min(triNodeState.getL1().getVoltageAbs().getValue(), Math.max(triNodeState.getL2().getVoltageAbs().getValue(), triNodeState.getL3().getVoltageAbs().getValue()));
			float nodeMaximumVoltage = Math.max(triNodeState.getL1().getVoltageAbs().getValue(), Math.max(triNodeState.getL2().getVoltageAbs().getValue(), triNodeState.getL3().getVoltageAbs().getValue()));
			this.updateNetworkMinimumNodeVoltage(nodeMinimumVoltage);
			this.updateNetworkMaximumNodeVoltage(nodeMaximumVoltage);
			
			// ----------------------------------------------------------------
			// --- Update the visualization of the energy flows ---------------
			// ----------------------------------------------------------------
			if (tsse!=null) {
				// --
				Vector<Float> floatVectorEnergyFlows = this.createFloatVector(tsSettings.getTimeSeriesIndexHash().size());
				// --- Walk through the energy flows of the interfaces -------------- 
				for (AbstractUsageOfInterface abstractUOI : tsse.getUsageOfInterfaces()) {
					String interfaceID = abstractUOI.getInterfaceID();
					ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(netComp.getId());
					InterfaceSetting intSet = ScheduleController.getInterfaceSetting(sc.getScheduleList().getInterfaceSettings(), interfaceID);
					
					if (intSet.getDomainModel() instanceof DefaultDomainModelElectricity && ((DefaultDomainModelElectricity)intSet.getDomainModel()).getRatedVoltage()==230) {
						
						UsageOfInterfaceEnergy uoi = (UsageOfInterfaceEnergy) abstractUOI;
						EnergyCarrier ec = DomainSettings.getEnergyCarrier(intSet.getDomain());
						
						TimeSeries ts = null;
						tsIndex = -1;
						
						if (sc!=null && intSet!=null && ec==EnergyCarrier.ELECTRICITY) {
							// --- Get the domain model ---------------------------------
							DefaultDomainModelElectricity domainModel = (DefaultDomainModelElectricity) intSet.getDomainModel();
							if (domainModel.getPowerType()==PowerType.ActivePower) {
								if (domainModel.getPhase()==Phase.L1) {
									interfaceID = ACTIVE_POWER_L1;
								} else if (domainModel.getPhase()==Phase.L2) {
									interfaceID = ACTIVE_POWER_L2;
								} else if(domainModel.getPhase()==Phase.L3) {
									interfaceID = ACTIVE_POWER_L3;
								}
							} else if (domainModel.getPowerType()==PowerType.ReactivePower) {
								if (domainModel.getPhase()==Phase.L1) {
									interfaceID = REACTIVE_POWER_L1;
								} else if (domainModel.getPhase()==Phase.L2) {
									interfaceID = REACTIVE_POWER_L2;
								} else if(domainModel.getPhase()==Phase.L3) {
									interfaceID = REACTIVE_POWER_L3;
								}
							} else {
								// --- Remove, if needed --------------------------------
								continue;
							}
						}
						// --- Get the TimeSeries for this energy flow ------------------
						if (tsSettings.getTimeSeriesHash().get(interfaceID)==null) {
							tsIndex = tsSettings.getTimeSeriesHash().size();
							String unit = DisplayHelper.getDisplayTextForEnergyUnitFactorPrefixSI(uoi.getEnergyFlow().getSIPrefix()) + "W";
							ts = this.addNewTimeSeriesToChart(tsSettings.getGraphNode(), DATA_MODEL_TIME_SERIES_INDEX, tsIndex, interfaceID, unit);
							tsSettings.addTimeSeries(ts);
							floatVectorVoltage.add(null);
							floatVectorEnergyFlows.add(null);
						} else {
							tsIndex = tsSettings.getTimeSeriesIndexHash().get(interfaceID);
						}
						
						// --- Create update information for the current energy flow ----
						if (tsIndex>-1) {
							tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, tsse.getGlobalTime() - tsse.getStateTime(), (float) uoi.getEnergyFlow().getValue());
							while (tsIndex>(floatVectorEnergyFlows.size()-1)) {
								floatVectorEnergyFlows.add(0.0f);
							}
							floatVectorEnergyFlows.set(tsIndex, (float) uoi.getEnergyFlow().getValue());
						}
					}
				
				} // end for Usage of Interfaces

				// --- Send float vector to visualisation -------------------------------
				udtsEnergyFlowStateBeginning = this.createUpdateTimeSeriesRow(graphNode, DATA_MODEL_TIME_SERIES_INDEX, tsse.getGlobalTime()-tsse.getStateTime(), floatVectorEnergyFlows);
				// --- Add a second state to make the time series more consistent -------
				if (tsse.getStateTime()>0) {
					udtsEnergyFlowStateEnd = this.createUpdateTimeSeriesRow(graphNode, DATA_MODEL_TIME_SERIES_INDEX, tsse.getGlobalTime(), floatVectorEnergyFlows);
				}
			} // end tsse!=null
			
			// --- Send the display notifications if set --------------------------------
			if (udtsVoltage!=null) this.addDisplayNotification(udtsVoltage);
			if (udtsEnergyFlowStateBeginning!=null) this.addDisplayNotification(udtsEnergyFlowStateBeginning);
			if (udtsEnergyFlowStateEnd!=null) this.addDisplayNotification(udtsEnergyFlowStateEnd);
			
		} // end for nodeKeys
		
		// --------------------------------------------------------------------------
		// --- Work on the changes of the NetworkComponents -------------------------
		// --------------------------------------------------------------------------
		ArrayList<String> netCompKeys= new ArrayList<String>(this.getNetworkCalculationStrategy().getNetworkComponentStates().keySet());
		for (int i = 0; i < netCompKeys.size(); i++) {
			
			// --- Get all required informations for this action --------------------
			String netCompID = netCompKeys.get(i);
			TimeSeriesSettings tsSettings = this.getTimeSeriesSettingsHash().get(netCompID);
			if (tsSettings==null) {
				tsSettings = this.createVisualisationElementsForNetworkComponent(netCompID);
			} 
			NetworkComponent netComp = tsSettings.getNetworkComponent();
			TriPhaseCableState cableState = (TriPhaseCableState) this.getNetworkCalculationStrategy().getNetworkComponentStates().get(netCompID);
			
			// --- Set the data model with the new TriPhaseElectricalNodeState ------
			Object[] dataModel = (Object[]) netComp.getDataModel();
			dataModel[DATA_MODEL_STATE_INDEX] = cableState;
			DataModelNotification dmn = new DataModelNotification(netComp);
			dmn.setDataModelPartUpdateIndex(DATA_MODEL_STATE_INDEX);
			this.addDisplayNotification(dmn);
			
			// --- Update the current and utilization level -------------------------
			Vector<Float> floatVector = this.createFloatVector(tsSettings.getTimeSeriesIndexHash().size());
			UpdateTimeSeries udts = null;
			int tsIndex = -1;
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(CURRENT_L1);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getCurrent_L1());
			floatVector.set(tsIndex, cableState.getCurrent_L1());
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(CURRENT_L2);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getCurrent_L2());
			floatVector.set(tsIndex, cableState.getCurrent_L2());
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(CURRENT_L3);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getCurrent_L3());
			floatVector.set(tsIndex, cableState.getCurrent_L3());
			
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(UTILIZATION_L1);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getUtil_L1());
			floatVector.set(tsIndex, cableState.getUtil_L1());
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(UTILIZATION_L2);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getUtil_L2());
			floatVector.set(tsIndex, cableState.getUtil_L2());

			tsIndex = tsSettings.getTimeSeriesIndexHash().get(UTILIZATION_L3);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getUtil_L3());
			floatVector.set(tsIndex, cableState.getUtil_L3());
			
			float maxUtilization = Math.max(cableState.getUtil_L1(), Math.max(cableState.getUtil_L2(), cableState.getUtil_L3()));
			this.updateNetworkMaximumCableUtilization(maxUtilization);
			
			// --- Send float vector to visualisation -------------------------------
			udts = this.createUpdateTimeSeriesRow(netComp, DATA_MODEL_TIME_SERIES_INDEX, this.getDisplayTime(), floatVector);
			this.addDisplayNotification(udts);
			
			// --- Add a layout notification, if configured so ----------------------
			this.addGraphElementLayoutForEdges(tsSettings, cableState);
		}
		
		// --- Add the cumulated new GraphElementLayouts ----------------------------
		this.addLayoutNotifications();
		this.setGraphElementLayoutVector(null);
		
		// --- Add the TrafficLight messages ----------------------------------------
		this.setTrafficLightMessages();
		
		// --- Send the cumulative display notifications ----------------------------
		this.sendDisplayAgentNotification(this.getDisplayNotifications());
		this.setDisplayNotifications(null);
		this.resetStateUpdates();
	}

	/**
	 * Adds a new GraphElementLayout to the {@link #graphElementLayoutVector} for nodes.
	 *
	 * @param tsSettings the TimeSeriesSettings
	 * @param triPhaseNodeState the TriPhaseElectricalNodeState
	 */
	private void addGraphElementLayoutForNodes(TimeSeriesSettings tsSettings, TriPhaseElectricalNodeState triPhaseNodeState) {
		
		ColorSettingsCollection colorSettings = this.getLayoutSettings().getColorSettingsForNodes();
		if (colorSettings.isEnabled()==false) return;
		if (colorSettings.getColorSettingsVector().size()==0) return;
		if (tsSettings==null) return;
		if (tsSettings.getGraphNode()==null) return;
		
		GraphNode graphNode = tsSettings.getGraphNode();
		
		GraphElementLayout graphElementLayout = this.getLayoutService().getGraphElementLayout(graphNode, this.getNetworkModel(), this.getLayoutSettings());
		if (graphElementLayout!=null) {
			this.addGraphElementLayout(graphElementLayout);
		}
	}
	/**
	 * Adds a new GraphElementLayout to the {@link #graphElementLayoutVector} for edges
	 *
	 * @param tsSettings the TimeSeriesSettings
	 * @param cableState the corresponding {@link TriPhaseCableState} instance
	 */
	private void addGraphElementLayoutForEdges(TimeSeriesSettings tsSettings, TriPhaseCableState cableState) {
		
		ColorSettingsCollection colorSettings = this.getLayoutSettings().getColorSettingsForEdges();
		if (colorSettings.isEnabled()==false) return;
		if (colorSettings.getColorSettingsVector().size()==0) return;
		if (tsSettings==null) return;
		if (tsSettings.getGraphEdge()==null) return;
		if (cableState==null) return;
		
		GraphEdge edge = tsSettings.getGraphEdge(); 
		GraphElementLayout graphElementLayout = this.getLayoutService().getGraphElementLayout(edge, this.getNetworkModel(), this.getLayoutSettings());
		if (graphElementLayout!=null) {
			this.addGraphElementLayout(graphElementLayout);
		}
	}

	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.electricity.NetworkModelDisplayUpdaterElectricity#createTimeSeriesForNodeComponent()
	 */
	@Override
	protected List<TimeSeries> createTimeSeriesForNodeComponent(String powerUnit) {
		ArrayList<TimeSeries> timeSeriesForNodes = new ArrayList<>();
		
		// --- Active/Reactive Power L1-3--------------
		timeSeriesForNodes.add(this.createTimeSeries(ACTIVE_POWER_L1, powerUnit));
		timeSeriesForNodes.add(this.createTimeSeries(REACTIVE_POWER_L1, powerUnit));
		timeSeriesForNodes.add(this.createTimeSeries(ACTIVE_POWER_L2, powerUnit));
		timeSeriesForNodes.add(this.createTimeSeries(REACTIVE_POWER_L2, powerUnit));
		timeSeriesForNodes.add(this.createTimeSeries(ACTIVE_POWER_L3, powerUnit));
		timeSeriesForNodes.add(this.createTimeSeries(REACTIVE_POWER_L3, powerUnit));
		// --- Voltage ----------------------------------------------
		timeSeriesForNodes.add(this.createTimeSeries(VOLTAGE_L1, "V"));
		timeSeriesForNodes.add(this.createTimeSeries(VOLTAGE_L2, "V"));
		timeSeriesForNodes.add(this.createTimeSeries(VOLTAGE_L3, "V"));
		
		return timeSeriesForNodes;
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.electricity.NetworkModelDisplayUpdaterElectricity#createTimeSeriesForEdgeComponent()
	 */
	@Override
	protected List<TimeSeries> createTimeSeriesForEdgeComponent() {
		
		ArrayList<TimeSeries> timeSeriesForEdges = new ArrayList<>();
		
		// --- Current ------------------------
		timeSeriesForEdges.add(this.createTimeSeries(CURRENT_L1, "A"));
		timeSeriesForEdges.add(this.createTimeSeries(CURRENT_L2, "A"));
		timeSeriesForEdges.add(this.createTimeSeries(CURRENT_L3, "A"));
		
		// --- Utilization_ -------------------
		timeSeriesForEdges.add(this.createTimeSeries(UTILIZATION_L1, "%"));
		timeSeriesForEdges.add(this.createTimeSeries(UTILIZATION_L2, "%"));
		timeSeriesForEdges.add(this.createTimeSeries(UTILIZATION_L3, "%"));
		
		return timeSeriesForEdges;
	}

}
