package hygrid.plugin;

import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.core.project.Project;
import agentgui.simulationService.time.TimeModel;
import agentgui.simulationService.time.TimeModelDateBased;
import de.enflexit.eom.awb.adapter.EomDataModelAdapter;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import energy.schedule.loading.ScheduleTimeRange;
import energy.schedule.loading.ScheduleTimeRange.RangeType;
import energy.schedule.loading.ScheduleTimeRangeAdapter;
import energy.schedule.loading.ScheduleTimeRangeController;
import energy.schedule.loading.ScheduleTimeRangeEvent;

/**
 * The HyGridScheduleTimeRangeListener serves as listener to the globally managed instance
 * of the {@link ScheduleTimeRange} and will react in the context of Energy Agents to a change
 * of that instance. Basically it will reload the data in the  
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class HyGridScheduleTimeRangeListener extends ScheduleTimeRangeAdapter {

	public static final String EVENT_SETUP_LOAD = "SetupLoaded";
	public static final String EVENT_CONFIGURED_IN_AWB_MAIN_WINDOW = ScheduleTimeRangeEvent.AWB_SETUP_CONFIGURED_MANUALLY_IN_AWB_MAIN_WINDOW;
	public static final String EVENT_CONFIGURED_IN_EXTERNAL_WINDOW = ScheduleTimeRangeEvent.AWB_SETUP_CONFIGURED_MANUALLY_IN_EXTERNAL_WINDOW;
	
	private boolean debug = false;
	
	private GraphEnvironmentController graphController;
	
	public HyGridScheduleTimeRangeListener(GraphEnvironmentController graphController) {
		this.graphController = graphController;
		this.addListener();
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here the check process before JADE start ------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * This method check the current time range settings according to time model.
	 */
	public void checkTimeRangeSettingsAccordingToTimeModelBeforeJadeStart() {
		
		// --- Get global ScheduleTimeRange --------------- 
		ScheduleTimeRange strGlobal = ScheduleTimeRangeController.getScheduleTimeRange();
		if (strGlobal==null) return;
		
		// --- Get Project instance -----------------------
		Project project = this.graphController.getProject();
		if (project==null) return; // (will not happen, but see 'save' call below)
		
		// --- Get date based time model ------------------
		TimeModel timeModel = this.graphController.getTimeModel();
		if (! (timeModel instanceof TimeModelDateBased)) return;
				
		// --- Get start and end time of the execution ---- 	
		TimeModelDateBased tmDataBased = (TimeModelDateBased) timeModel;
		long startTime = tmDataBased.getTimeStart();
		
		if (strGlobal.getTimeFrom()!=startTime) {
			// --- ScheduleTimeRange needs to adjusted! --- 
			long shift = strGlobal.getTimeFrom() - startTime; 
			ScheduleTimeRange strGlobalNew = strGlobal.getCopy();
			strGlobalNew.setTimeFrom(strGlobal.getTimeFrom() - shift);
			if (strGlobal.getRangeType()==RangeType.TimeRange) {
				strGlobalNew.setTimeTo(strGlobal.getTimeTo() - shift);
			}
			// --- Set as new global ScheduleTimeRange ----
			ScheduleTimeRangeController.setScheduleTimeRange(strGlobalNew , EVENT_CONFIGURED_IN_AWB_MAIN_WINDOW);
			
			// --- Save the project again -----------------
			project.save();
		}
		
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here, the actual listener that reacts on changes of the global ScheduleTimeRange --
	// --------------------------------------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see energy.schedule.loading.ScheduleTimeRangeListener#onUpdatedScheduleTimeRange(energy.schedule.loading.ScheduleTimeRangeEvent)
	 */
	@Override
	public void onUpdatedScheduleTimeRange(ScheduleTimeRangeEvent strEvent) {
		
		boolean isAwMainWindowEvent = strEvent.getEventDescription().equals(EVENT_CONFIGURED_IN_AWB_MAIN_WINDOW);
		boolean isExternalWindowEvent = strEvent.getEventDescription().equals(EVENT_CONFIGURED_IN_EXTERNAL_WINDOW);
		if (isAwMainWindowEvent==true || isExternalWindowEvent==true) {
			// --- Some debug output --------------------------------
			if (this.debug==true) {
				if (strEvent.getNewScheduleTimeRange()==null) {
					System.out.println("[" + this.getClass().getSimpleName() + "] Reload ScheduelList with all states");
				} else {
					System.out.println("[" + this.getClass().getSimpleName() + "] Reload ScheduelList according to: " + strEvent.getNewScheduleTimeRange().getDisplayText(false));
				}
			}
			// --- Get components to do the reload on ---------------
			Vector<DataModelNetworkElement> reloadVector = this.getNetworkElementsToReload();
			this.graphController.loadDataModelNetworkElements(isAwMainWindowEvent, reloadVector);
			// --- Set project unsaved ------------------------------ 
			this.graphController.setProjectUnsaved();
		}
	}
	
	/**
	 * Returns the network elements to be reload through the change of the ScheduleTimeRange.
	 * @return the network elements to reload
	 */
	private Vector<DataModelNetworkElement> getNetworkElementsToReload() {
		
		Vector<DataModelNetworkElement> netElementsFound = new Vector<DataModelNetworkElement>();
		
		// --- Define the result vector -----------------------------
		NetworkModel networkModel = this.graphController.getNetworkModel();
		Object[] netComps = networkModel.getNetworkComponents().values().toArray();
		for (int i = 0; i < netComps.length; i++) {
			NetworkComponent netComp = (NetworkComponent) netComps[i];
			NetworkComponentAdapter netCompAdapter = networkModel.getNetworkComponentAdapter(this.graphController, netComp);
			if (netCompAdapter==null) continue;
			
			NetworkComponentAdapter4DataModel dmNetCompAdapter = netCompAdapter.getStoredDataModelAdapter();
			if (dmNetCompAdapter!=null && dmNetCompAdapter.containsDataModelType(EomDataModelAdapter.DATA_MODEL_TYPE_EOM_MODEL)==true) {
				// --- Check for non-ScheduleList's -----------------
				if (this.hasCertainlyNoScheduleList(netComp)==false) {
					netElementsFound.add(netComp);
				}
			}
		}
		
		
		// --- Work on the GraphNodes -------------------------------
		Object[] graphNodes = networkModel.getGraph().getVertices().toArray();
		for (int i = 0; i < graphNodes.length; i++) {
			GraphNode graphNode = (GraphNode) graphNodes[i];
			NetworkComponentAdapter netCompAdapter = networkModel.getNetworkComponentAdapter(this.graphController, graphNode);
			if (netCompAdapter==null) continue;
			
			NetworkComponentAdapter4DataModel dmNetCompAdapter = netCompAdapter.getStoredDataModelAdapter();
			if (dmNetCompAdapter!=null && dmNetCompAdapter.containsDataModelType(EomDataModelAdapter.DATA_MODEL_TYPE_EOM_MODEL)==true) {
				// --- Check for non-ScheduleList's -----------------
				if (this.hasCertainlyNoScheduleList(graphNode)==false) {
					netElementsFound.add(graphNode);
				}
			}
		}
		
		// --- Some debug printing? ---------------------------------
		if (this.debug==true) {
			System.out.println("[" + this.getClass().getSimpleName() + "] " + (netComps.length + graphNodes.length) + " network elements in the NetworkModel, " + netElementsFound.size() + " to reload.");
		}
		return netElementsFound;
	}
	

	/**
	 * Checks if the specified network element has certainly no schedule list.
	 * @return true, if is certainly no schedule list
	 */
	private boolean hasCertainlyNoScheduleList(DataModelNetworkElement networkElement) {
		
		EomModelType eomModelType = this.getEomModelType(networkElement.getDataModelStorageSettings());
		if (eomModelType==null) {
			// --- We don't know here - maybe an old style handling? ----------
			return false;
		}

		boolean isNoScheduleList = false;

		switch (eomModelType) {
		case TechnicalSystem:
			isNoScheduleList = true;
			break;
		case ScheduleList:
			isNoScheduleList = false;
			break;
		case TechnicalSystemGroup:
			// --- An aggregation may contain ScheduleList's also ------------- 
			isNoScheduleList = false;
			break;
		}
		return isNoScheduleList;
	}
	/**
	 * Receives the EomModelType from the specified storage settings.
	 *
	 * @param storageSettings the storage settings
	 * @return the EomModelType found in the settings
	 */
	private EomModelType getEomModelType(TreeMap<String, String> storageSettings) {
		
		if (storageSettings==null) return null;
		
		EomModelType eomModelType = null;
		String eomModelTypeString = storageSettings.get(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE);
		if (eomModelTypeString!=null) {
			eomModelType = EomModelType.valueOf(eomModelTypeString);
		}
		return eomModelType;
	}
	
}
