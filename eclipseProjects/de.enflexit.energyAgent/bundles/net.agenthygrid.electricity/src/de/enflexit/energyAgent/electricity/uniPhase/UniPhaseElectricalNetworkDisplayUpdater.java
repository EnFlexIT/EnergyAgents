package de.enflexit.energyAgent.electricity.uniPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElementLayout;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.visualisation.notifications.DataModelNotification;
import org.awb.env.networkModel.visualisation.notifications.UpdateTimeSeries;

import agentgui.ontology.TimeSeries;
import de.enflexit.energyAgent.core.aggregation.AbstractNetworkModelDisplayUpdater;
import de.enflexit.energyAgent.electricity.aggregation.AbstractElectricalNetworkDisplayUpdater;
import energy.DisplayHelper;
import energy.DomainSettings;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.AbstractUsageOfInterface;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.schedule.ScheduleController;
import hygrid.env.ColorSettingsCollection;
import hygrid.globalDataModel.ontology.UniPhaseCableState;
import hygrid.globalDataModel.ontology.UniPhaseElectricalNodeState;

/**
 * {@link AbstractNetworkModelDisplayUpdater} implementation for uni-phase 10kV electrical grids.
 *  
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class UniPhaseElectricalNetworkDisplayUpdater extends AbstractElectricalNetworkDisplayUpdater {

	private static final String ACTIVE_POWER = "Active Power";
	private static final String REACTIVE_POWER = "Reactive Power";
	private static final String VOLTAGE = "Voltage";
	private static final String CURRENT = "Current";
	private static final String UTILIZATION = "Utilization";
	
	private UniPhaseElectricalNetworkGraphElementLayoutService layoutService;
	
	protected UniPhaseElectricalNetworkGraphElementLayoutService getLayoutService() {
		if (layoutService==null) {
			layoutService = new UniPhaseElectricalNetworkGraphElementLayoutService();
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
			UniPhaseElectricalNodeState uniNodeState = (UniPhaseElectricalNodeState) this.getNetworkCalculationStrategy().getGraphNodeStates().get(graphNodeID);
			TechnicalSystemStateEvaluation tsse = sysStateChanges.get(netComp.getId());
			
			// --- Set the data model with the new TriPhaseElectricalNodeState ------
			Object[] dataModel = tsSettings.getDataModelArray();
			dataModel[DATA_MODEL_STATE_INDEX] = uniNodeState;
			
			DataModelNotification dmn = new DataModelNotification(graphNode);
			dmn.setDataModelPartUpdateIndex(DATA_MODEL_STATE_INDEX);
			this.addDisplayNotification(dmn);

			// --- Update the voltage level -----------------------------------------
			Vector<Float> floatVectorVoltage = this.createFloatVector(tsSettings.getTimeSeriesIndexHash().size());
			int tsIndex = -1;
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(VOLTAGE);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), uniNodeState.getVoltageAbs().getValue());
			floatVectorVoltage.set(tsIndex, uniNodeState.getVoltageAbs().getValue());
			
			// --- Add a layout notification, if configured so ----------------------
			this.addGraphElementLayoutForNodes(tsSettings, uniNodeState);

			// --- Define/prepare visualization updates -----------------------------
			UpdateTimeSeries udtsVoltage = this.createUpdateTimeSeriesRow(graphNode, DATA_MODEL_TIME_SERIES_INDEX, this.getDisplayTime(), floatVectorVoltage);
			UpdateTimeSeries udtsEnergyFlowStateBeginning = null;
			UpdateTimeSeries udtsEnergyFlowStateEnd = null;
			
			
			// ----------------------------------------------------------------------
			// --- Update the visualization of the energy flows ---------------------
			// ----------------------------------------------------------------------
			if (tsse!=null) {
				// --
				Vector<Float> floatVectorEnergyFlows = this.createFloatVector(tsSettings.getTimeSeriesIndexHash().size());
				// --- Walk through the energy flows of the interfaces -------------- 
				for (AbstractUsageOfInterface abstractUOI : tsse.getUsageOfInterfaces()) {
					
					String interfaceID = abstractUOI.getInterfaceID();
					ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(netComp.getId());
					InterfaceSetting intSet = ScheduleController.getInterfaceSetting(sc.getScheduleList().getInterfaceSettings(), interfaceID);
					
//					if (intSet.getDomain().equals(this.getDomain())) {
					if (intSet.getDomainModel() instanceof DefaultDomainModelElectricity && ((DefaultDomainModelElectricity)intSet.getDomainModel()).getRatedVoltage()==10000) {
						
						
						UsageOfInterfaceEnergy uoi = (UsageOfInterfaceEnergy) abstractUOI;
						EnergyCarrier ec = DomainSettings.getEnergyCarrier(intSet.getDomain());
						
						TimeSeries ts = null;
						tsIndex = -1;
						
						if (sc!=null && intSet!=null && ec==EnergyCarrier.ELECTRICITY) {
							// --- Get the domain model ---------------------------------
							DefaultDomainModelElectricity domainModel = (DefaultDomainModelElectricity) intSet.getDomainModel();
							if (domainModel.getPowerType()==PowerType.ActivePower) {
								interfaceID = ACTIVE_POWER;
							} else if (domainModel.getPowerType()==PowerType.ReactivePower) {
								interfaceID = REACTIVE_POWER;
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
							EnergyFlowInWatt energyFlow = uoi.getEnergyFlow();
							if (energyFlow!=null) {
								tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, tsse.getGlobalTime() - tsse.getStateTime(), (float) uoi.getEnergyFlow().getValue());
								while (tsIndex>(floatVectorEnergyFlows.size()-1)) {
									floatVectorEnergyFlows.add(0.0f);
								}
								floatVectorEnergyFlows.set(tsIndex, (float) uoi.getEnergyFlow().getValue());
							}
						}
					}
					
				
				} // end for Usage of Interfaces

				// --- Send float vector to visualisation -------------------------------
				udtsEnergyFlowStateBeginning = this.createUpdateTimeSeriesRow(graphNode, DATA_MODEL_TIME_SERIES_INDEX, tsse.getGlobalTime()-tsse.getStateTime(), floatVectorEnergyFlows);
				// --- Add a second point to make the time series more consistent -------
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
			UniPhaseCableState cableState = (UniPhaseCableState) this.getNetworkCalculationStrategy().getNetworkComponentStates().get(netCompID);
			
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
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(CURRENT);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getCurrent().getValue());
			floatVector.set(tsIndex, cableState.getCurrent().getValue());
			
			tsIndex = tsSettings.getTimeSeriesIndexHash().get(UTILIZATION);
			tsSettings.getTimeSeriesChartRealTimeWrapper().addValuePair(tsIndex, this.getDisplayTime(), cableState.getUtilization());
			floatVector.set(tsIndex, cableState.getUtilization());
			
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
	private void addGraphElementLayoutForNodes(TimeSeriesSettings tsSettings, UniPhaseElectricalNodeState triPhaseNodeState) {
		
		ColorSettingsCollection colorSettings = this.getLayoutSettings().getColorSettingsForNodes();
		if (colorSettings.isEnabled()==false) return;
		if (colorSettings.getColorSettingsVector().size()==0) return;
		if (tsSettings==null) return;
		if (tsSettings.getGraphNode()==null) return;
		if (triPhaseNodeState==null) return;
		
		// --- Get the GraphElementLayout for this node ---------------
		GraphNode node = tsSettings.getGraphNode(); 
		GraphElementLayout layout = this.getLayoutService().getGraphElementLayout(node, this.getNetworkModel(), this.getLayoutSettings());
		
		if (layout!=null) {
			this.addGraphElementLayout(layout);
		}
	}
	/**
	 * Adds a new GraphElementLayout to the {@link #graphElementLayoutVector} for edges
	 *
	 * @param tsSettings the TimeSeriesSettings
	 * @param cableState the corresponding {@link TriPhaseCableState} instance
	 */
	private void addGraphElementLayoutForEdges(TimeSeriesSettings tsSettings, UniPhaseCableState cableState) {
		
		ColorSettingsCollection colorSettings = this.getLayoutSettings().getColorSettingsForEdges();
		if (colorSettings.isEnabled()==false) return;
		if (colorSettings.getColorSettingsVector().size()==0) return;
		if (tsSettings==null) return;
		if (tsSettings.getGraphEdge()==null) return;
		if (cableState==null) return;
		
		GraphEdge edge = tsSettings.getGraphEdge();
		GraphElementLayout layout = this.getLayoutService().getGraphElementLayout(edge, this.getNetworkModel(), this.getLayoutSettings());
		if (layout!=null) {
			this.addGraphElementLayout(layout);
		}
	}

	
	/* (non-Javadoc)
	 * @see hygrid.aggregation.electricity.NetworkModelDisplayUpdaterElectricity#createTimeSeriesForNodeComponent(java.lang.String)
	 */
	@Override
	protected List<TimeSeries> createTimeSeriesForNodeComponent(String powerUnit) {
		ArrayList<TimeSeries> timeSeriesForNodes = new ArrayList<>();
		// --- Active/Reactive Power L1-3--------------
		timeSeriesForNodes.add(this.createTimeSeries(ACTIVE_POWER, powerUnit));
		timeSeriesForNodes.add(this.createTimeSeries(REACTIVE_POWER, powerUnit));
		// --- Voltage ----------------------------------------------
		timeSeriesForNodes.add(this.createTimeSeries(VOLTAGE, "V"));
		return timeSeriesForNodes;
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.electricity.NetworkModelDisplayUpdaterElectricity#createTimeSeriesForEdgeComponent()
	 */
	@Override
	protected List<TimeSeries> createTimeSeriesForEdgeComponent() {
		ArrayList<TimeSeries> timeSeriesForEdges = new ArrayList<>();
		// --- Current ------------------------			
		timeSeriesForEdges.add(this.createTimeSeries(CURRENT, "A"));
		// --- Utilization_ -------------------
		timeSeriesForEdges.add(this.createTimeSeries(UTILIZATION, "%"));
		return timeSeriesForEdges;
	}
	
}
