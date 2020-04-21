package de.enflexit.ea.core.aggregation;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElementLayout;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.ui.messaging.GraphUIMessage;
import org.awb.env.networkModel.helper.DomainCluster;
import org.awb.env.networkModel.visualisation.notifications.DisplayAgentNotificationGraph;
import org.awb.env.networkModel.visualisation.notifications.DisplayAgentNotificationGraphMultiple;
import org.awb.env.networkModel.visualisation.notifications.GraphLayoutNotification;
import org.awb.env.networkModel.visualisation.notifications.UIMessage;
import org.awb.env.networkModel.visualisation.notifications.UpdateTimeSeries;

import agentgui.core.application.Application;
import agentgui.core.charts.gui.ChartTab;
import agentgui.core.charts.timeseriesChart.TimeSeriesChartRealTimeWrapper;
import agentgui.ontology.TimeSeries;
import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import agentgui.simulationService.transaction.DisplayAgentNotification;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractNetworkModelDisplayUpdater can be extended to update the graphical representation of 
 * a NetworkModel in the 'Runtime Visualization'. For this, {@link DisplayAgentNotification} instances have 
 * to be prepared first. Thereafter, the collected display updates should be send to the registered {@link AggregationListener}
 * by using the corresponding method {@link #sendDisplayAgentNotification(DisplayAgentNotification)}.
 * 
 * The extend class should prepare display update for the current state of the {@link NetworkModel}. 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractNetworkModelDisplayUpdater {

	private AbstractAggregationHandler aggregationHandler;
	private AbstractSubNetworkConfiguration subAggregationConfiguration;
	
	private HashMap<String, TimeSeriesSettings> timeSeriesSettings;

	private DisplayAgentNotificationGraphMultiple displayNotifications;
	private Vector<GraphElementLayout> graphElementLayoutVector;
	
	private long displayTime;
	private HashMap<String, TechnicalSystemStateEvaluation> stateUpdates;
	
	
	/**
	 * Gets the display time.
	 * @return the display time
	 */
	protected long getDisplayTime() {
		return this.displayTime; 
	}
	
	/**
	 * Sets the new display time to be used for the visualization.
	 * @param newDisplayTime the new display time
	 */
	protected void setDisplayTime(Long newDisplayTime) {
		this.displayTime = newDisplayTime;
	}
	
	/**
	 * Gets the state updates.
	 * @return the state updates
	 */
	protected HashMap<String, TechnicalSystemStateEvaluation> getStateUpdates() {
		return stateUpdates;
	}

	/**
	 * Sets the state updates.
	 * @param stateUpdates the state updates
	 */
	protected void setStateUpdates(HashMap<String, TechnicalSystemStateEvaluation> stateUpdates) {
		
		// --- Ignore empty HashMaps -------------------------------- 
		if (stateUpdates!=null && stateUpdates.size()>0) {
		
			// --- Create individual local instance ----------------- 
			this.stateUpdates = new HashMap<>(stateUpdates);
			
			// --- Remove all entries that do not belong to the current domain cluster
			DomainCluster dc = this.getSubAggregationConfiguration().getDomainCluster();
			if (dc!=null) {
				// --- Create Vector of NetworkComponent IDs to delete
				Vector<String> deleteCandidates = new Vector<>(this.stateUpdates.keySet());
				// --- Remove all IDs that belong to the the Domain Cluster
				for (int i = 0; i < dc.getNetworkComponents().size(); i++) {
					deleteCandidates.remove(dc.getNetworkComponents().get(i).getId());
				}
				// --- Remove the entries for the remaining IDs (= not in the domain cluster) 
				for (int i = 0; i < deleteCandidates.size(); i++) {
					this.stateUpdates.remove(deleteCandidates.get(i));
				}
			}
		}
	}
	
	/**
	 * Reset state updates.
	 */
	protected void resetStateUpdates() {
		this.setStateUpdates(null);
	}
	
	
	/**
	 * Sets the aggregation handler and registers it as listener for the actual network calculation.
	 * @param aggregationHandler the new aggregation handler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Sets the current sub aggregation configuration.
	 * @param subAggregationConfiguration the new sub aggregation configuration
	 */
	public void setSubAggregationConfiguration(AbstractSubNetworkConfiguration subAggregationConfiguration) {
		this.subAggregationConfiguration = subAggregationConfiguration;
	}
	/**
	 * Returns the current sub aggregation configuration.
	 * @return the sub aggregation configuration
	 */
	public AbstractSubNetworkConfiguration getSubAggregationConfiguration() {
		return subAggregationConfiguration;
	}

	/**
	 * Return the current network model from the aggregation handler.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return this.getAggregationHandler().getNetworkModel();
	}
	/**
	 * Gets the time format to use with this simulation.
	 * @return the time format
	 */
	protected String getTimeFormat() {
		return this.getAggregationHandler().getTimeFormat();
	}
	
	// ----------------------------------------------------------------------------------
	// --- The following two method define the input and output of this class -----------
	// ----------------------------------------------------------------------------------
	/**
	 * Has to set the last network component updates to the display.
	 *
	 * @param lastStateUpdates the last state updates
	 * @param displayTime the display time to use
	 */
	protected abstract void updateNetworkModelDisplay(final HashMap<String, TechnicalSystemStateEvaluation> lastStateUpdates, final long displayTime);
	
	/**
	 * Has to be invoked, if the prepared display agent notifications should be transferred to the visualization.
	 * @param displayNotification the new prepared display agent notifications
	 */
	protected void sendDisplayAgentNotification(DisplayAgentNotification displayNotification) {
		this.getAggregationHandler().notifyListenerAboutDisplayNotifications(displayNotification);
	}

	// ----------------------------------------------------------------------------------
	// --- From here, help methods to build display updates can be found ----------------
	// ------------------------------------------------------------------------
	/**
	 * Sets the current Vector of DisplayAgentNotification that is in fact a {@link DisplayAgentNotificationGraphMultiple} instance.
	 * @param displayNotifications the new display notifications
	 */
	protected void setDisplayNotifications(DisplayAgentNotificationGraphMultiple displayNotifications) {
		this.displayNotifications = displayNotifications;
	}
	/**
	 * Returns the current Vector of DisplayAgentNotification that is in fact a {@link DisplayAgentNotificationGraphMultiple} instance.
	 * @return the display notifications
	 */
	protected DisplayAgentNotificationGraphMultiple getDisplayNotifications() {
		if (displayNotifications==null) {
			displayNotifications = new DisplayAgentNotificationGraphMultiple();
		}
		return displayNotifications;
	}
	/**
	 * Adds a new display notification to the current set of notifications.
	 * @param displayNotification the display notification
	 */
	protected void addDisplayNotification(DisplayAgentNotificationGraph displayNotification) {
		this.getDisplayNotifications().addDisplayNotification(displayNotification);
	}
	
	
	/**
	 * Returns the graph element layout vector that is to be used to define and collect layout changes 
	 * for NetworkComponets or GraphElements that can finally be packed into a {@link DisplayAgentNotification}.
	 * @return the graph element layout vector
	 */
	protected Vector<GraphElementLayout> getGraphElementLayoutVector() {
		if (graphElementLayoutVector==null) {
			graphElementLayoutVector = new Vector<GraphElementLayout>();
		}
		return graphElementLayoutVector;
	}
	/**
	 * Sets the graph element layout vector.
	 * @param graphElementLayoutVector the new graph element layout vector
	 */
	protected void setGraphElementLayoutVector(Vector<GraphElementLayout> graphElementLayoutVector) {
		this.graphElementLayoutVector = graphElementLayoutVector;
	}
	/**
	 * Adds the specified graph element layout to the .
	 * @param newGraphElementLayout the new graph element layout
	 */
	protected void addGraphElementLayout(GraphElementLayout newGraphElementLayout) {
		this.getGraphElementLayoutVector().add(newGraphElementLayout);
	}
	/**
	 * Adds the collected layout notifications that are stored in the {@link #graphElementLayoutVector} to the {@link #displayNotifications}.
	 */
	protected void addLayoutNotifications() {
		if (this.getGraphElementLayoutVector().size()>0) {
			GraphLayoutNotification layoutNotification = new GraphLayoutNotification();
			layoutNotification.addGraphElementLayouts(this.getGraphElementLayoutVector());
			this.addDisplayNotification(layoutNotification);	
		}
	}
	
	/**
	 * Gets the layout settings.
	 * @return the layout settings
	 */
	protected HyGridGraphElementLayoutSettings getLayoutSettings() {
		HyGridAbstractEnvironmentModel absEnv = this.getAggregationHandler().getHyGridAbstractEnvironmentModel();
		return (HyGridGraphElementLayoutSettings) absEnv.getGraphElementLayoutSettings().get(this.getSubNetwork());
	}
	
	
	/**
	 * Adds the specified {@link GraphUIMessage} to the stack of UI messages.
	 * @param graphUIMessage the graph UI message
	 * 
	 * @see #getUiMessages()
	 */
	protected void addGraphUiMessage(GraphUIMessage graphUIMessage) {
		if (graphUIMessage!=null) {
			this.addDisplayNotification(new UIMessage(graphUIMessage));
		}
	}
	
	/**
	 * Creates a new Vector, containing float values of 0.0f.
	 *
	 * @param numberOfElements the number of elements
	 * @return the vector
	 */
	protected Vector<Float> createFloatVector(int numberOfElements) {
		Vector<Float> floatVector = new Vector<Float>();
		for (int i=0; i < numberOfElements; i++) {
			floatVector.add(null);
		}
		return floatVector;
	}
	/**
	 * Creates a new UpdateTimeSeries notification that can be send o the display.
	 *
	 * @param graphNodeOrNetworkComponent the graph node or network component
	 * @param dataModelIndex the data model index
	 * @param time the time
	 * @param floatVector the float vector
	 * @return the update time series
	 */
	protected UpdateTimeSeries createUpdateTimeSeriesRow(Object graphNodeOrNetworkComponent, int dataModelIndex, long time, Vector<Float> floatVector ) {
		
		UpdateTimeSeries uts = null;
		if (graphNodeOrNetworkComponent instanceof GraphNode) {
			uts = new UpdateTimeSeries((GraphNode)graphNodeOrNetworkComponent, this.getSubAggregationConfiguration().getDomain(), dataModelIndex);
		} else if (graphNodeOrNetworkComponent instanceof NetworkComponent) {
			uts = new UpdateTimeSeries((NetworkComponent)graphNodeOrNetworkComponent, dataModelIndex);
		}
		uts.editTimeSeriesChartAddOrExchangeDataRow(time, floatVector);
		return uts;
	}
	/**
	 * Adds a new TimeSeries to the chart that should be available at the data model index position
	 * and adds this as a notification to the local display notifications.
	 *
	 * @param graphNodeOrNetworkComponent the {@link GraphNode} or {@link NetworkComponent} instance
	 * @param dataModelIndex the data model index
	 * @param dataSeriesIndex the data series index
	 * @param label the new label
	 * @param unit the new unit
	 * @return the newly created {@link TimeSeries}
	 * @see #addDisplayNotification(DisplayAgentNotificationGraph)
	 */
	protected TimeSeries addNewTimeSeriesToChart(Object graphNodeOrNetworkComponent, int dataModelIndex, int dataSeriesIndex, String label, String unit) {
		// --- Create TimeSeries ------------------------------------
		TimeSeries timeSeries = this.createTimeSeries(label, unit);
		// --- Prepare an update notification -----------------------
		UpdateTimeSeries uts = null;
		if (graphNodeOrNetworkComponent instanceof GraphNode) {
			uts = new UpdateTimeSeries((GraphNode) graphNodeOrNetworkComponent, this.getSubAggregationConfiguration().getDomain(), dataModelIndex);
		} else if (graphNodeOrNetworkComponent instanceof NetworkComponent) {
			uts = new UpdateTimeSeries((NetworkComponent)graphNodeOrNetworkComponent, dataModelIndex);
		}
		uts.addOrExchangeTimeSeries(timeSeries, dataSeriesIndex);
		this.addDisplayNotification(uts);
		return timeSeries;
	}
	/**
	 * Creates a time series with the specified label.
	 *
	 * @param label the label
	 * @return the time series
	 */
	protected TimeSeries createTimeSeries(String label, String unit) {
		TimeSeries timeSeries = new TimeSeries();
		timeSeries.setLabel(label);
		timeSeries.setUnit(unit);
		return timeSeries;
	}
	/**
	 * Creates a {@link TimeSeriesChart} for the specified component that is to be used 
	 * in the data model of the {@link GraphNode} of the {@link NetworkComponent}.
	 *
	 * @param componentName the component name
	 * @param chartTitle the chart title
	 * @param yAxisLabel the y axis label
	 * @return the new time series chart
	 */
	protected TimeSeriesChart createTimeSeriesChart(String componentName, String chartTitle, String yAxisLabel) {
		TimeSeriesChart tsc = new TimeSeriesChart();
		TimeSeriesChartSettings tscSettings = new TimeSeriesChartSettings();
		tscSettings.setChartTitle(chartTitle + " for " + componentName);
		tscSettings.setRendererType(ChartTab.getRenderType(ChartTab.RENDERER_Step_Renderer));
		tscSettings.setXAxisLabel("Time");
		tscSettings.setYAxisLabel(yAxisLabel);
		tscSettings.setTimeFormat(this.getTimeFormat());
		tsc.setTimeSeriesVisualisationSettings(tscSettings);
		tsc.setRealTime(true);
		return tsc;
	}
	
	/**
	 * Gets the object array from data model.
	 * @param dataModel the data model
	 * @return the object array from data model
	 */
	protected Object[] getDomainSpecificDataModel(Object dataModel) {
		if (dataModel instanceof Object[]) {
			// --- Only one domain -> return data model directly --------------
			return (Object[]) dataModel;
		} else {
			// --- Multiple domains -> return the right data model ------------
			if (dataModel instanceof TreeMap<?, ?>) {
				@SuppressWarnings("unchecked")
				TreeMap<String, Object> dmTreeMap = (TreeMap<String, Object>) dataModel; 
				Object domainDataModel = dmTreeMap.get(this.getSubNetwork());
				return (Object[]) domainDataModel;
			} else {
				// --- Unexpected data model type -----------------------------
				return null;
			}
		}
	}
	
	/**
	 * Gets the domain this display updater is responsible for-
	 * @return the domain
	 */
	protected abstract String getSubNetwork();
		
	
	/**
	 * Returns the components {@link TimeSeriesSettings} (for {@link GraphNode}s or {@link NetworkComponent}s).
	 * @return the {@link TimeSeriesSettings}, if available 
	 */
	public HashMap<String, TimeSeriesSettings> getTimeSeriesSettingsHash() {
		if (timeSeriesSettings==null) {
			timeSeriesSettings = new HashMap<String, TimeSeriesSettings>();
		}
		return timeSeriesSettings;
	}
	/**
	 * The Class TimeSeriesSettings reminds all important Time series settings
	 * for a GraphNode or a NetworkComponent.
	 *
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
	 */
	public class TimeSeriesSettings {
		
		private GraphNode graphNode;
		private String graphNodeDomain;

		private GraphEdge graphEdge;
		private NetworkComponent networkComponent;
		
		private TimeSeriesChartRealTimeWrapper timeSeriesChartRealTimeWrapper;
		
		private HashMap<String, TimeSeries> timeSeriesHash;
		private HashMap<String, Integer> timeSeriesIndexHash;
		
		/**
		 * Instantiates a new time series settings.
		 *
		 * @param graphNode the graph node
		 * @param graphNodeDomain the actual domain of the GraphNode to work on
		 * @param networkComponent the network component
		 */
		public TimeSeriesSettings(GraphNode graphNode, String graphNodeDomain, NetworkComponent networkComponent) {
			this.graphNode = graphNode;
			this.graphNodeDomain = graphNodeDomain;
			this.networkComponent = networkComponent;
		}
		/**
		 * Instantiates a new time series settings.
		 * 
		 * @param graphEdge the graph edge
		 * @param networkComponent the network component
		 */
		public TimeSeriesSettings(GraphEdge graphEdge, NetworkComponent networkComponent) {
			this.graphEdge= graphEdge;
			this.networkComponent = networkComponent;
		}
		
		/**
		 * Gets the graph node.
		 * @return the graph node
		 */
		public GraphNode getGraphNode() {
			return graphNode;
		}
		/**
		 * Returns the domain of the GraphNode.
		 * @return the graph node domain
		 */
		public String getGraphNodeDomain() {
			return graphNodeDomain;
		}
		/**
		 * Gets the graph edge.
		 * @return the graph edge
		 */
		public GraphEdge getGraphEdge() {
			return graphEdge;
		}
		/**
		 * Gets the network component.
		 * @return the network component
		 */
		public NetworkComponent getNetworkComponent() {
			return networkComponent;
		}
		
		/**
		 * Returns the data model array for the specified domain.
		 *
		 * @param domain the domain
		 * @return the data model array
		 */
		public Object[] getDataModelArray() {
			
			Object[] dmArray = null;
			if (this.getGraphNode()!=null && this.getGraphNode().getDataModel()!=null) {
				if (this.getGraphNode().getDataModel() instanceof TreeMap<?, ?>) {
					TreeMap<?, ?> dmTreeMap = (TreeMap<?, ?>) this.getGraphNode().getDataModel();
					dmArray = (Object[]) dmTreeMap.get(this.getGraphNodeDomain());
				} else {
					dmArray = (Object[]) this.getGraphNode().getDataModel();
				}
				
			} else {
				dmArray = (Object[]) this.getNetworkComponent().getDataModel();
			}
			return dmArray;
		}
		
		/**
		 * Gets the time series chart.
		 * @return the time series chart
		 */
		public TimeSeriesChart getTimeSeriesChart() {
			
			// --- Get the right data model -------------------------
			Object[] dmArray = this.getDataModelArray();
			if (dmArray!=null) {
				for (int i=0; i<dmArray.length; i++) {
					if (dmArray[i] instanceof TimeSeriesChart) {
						return (TimeSeriesChart) dmArray[i];
					}
				}
			}
			
			// --- Chart not found - should never happen ------------
			return null;
		}
		
		/**
		 * Gets the time series chart real time wrapper.
		 * @return the time series chart real time wrapper
		 */
		public TimeSeriesChartRealTimeWrapper getTimeSeriesChartRealTimeWrapper() {
			if (timeSeriesChartRealTimeWrapper==null) {
				timeSeriesChartRealTimeWrapper = new TimeSeriesChartRealTimeWrapper(getTimeSeriesChart(), Application.getGlobalInfo().getTimeSeriesLengthRestriction());
			}
			return timeSeriesChartRealTimeWrapper;
		}
		/**
		 * Gets the time series hash.
		 * @return the time series hash
		 */
		public HashMap<String, TimeSeries> getTimeSeriesHash() {
			if (timeSeriesHash==null) {
				timeSeriesHash = new HashMap<String, TimeSeries>();
			}
			return timeSeriesHash;
		}
		/**
		 * Returns the time series index hash.
		 * @return the time series index hash
		 */
		public HashMap<String, Integer> getTimeSeriesIndexHash() {
			if (timeSeriesIndexHash==null) {
				timeSeriesIndexHash = new HashMap<String, Integer>();
			}
			return timeSeriesIndexHash;
		}
		/**
		 * Reminds a TimeSeries for the later access.
		 * @param timeSeries the time series
		 * @param indexPosition the index position
		 */
		public void addTimeSeries(TimeSeries timeSeries) {
			int newIndex = this.getTimeSeriesChart().getTimeSeriesChartData().size();
			this.getTimeSeriesChart().getTimeSeriesChartData().add(timeSeries);
			this.getTimeSeriesHash().put(timeSeries.getLabel(), timeSeries);
			this.getTimeSeriesIndexHash().put(timeSeries.getLabel(), newIndex);
		}
	}
	
}
