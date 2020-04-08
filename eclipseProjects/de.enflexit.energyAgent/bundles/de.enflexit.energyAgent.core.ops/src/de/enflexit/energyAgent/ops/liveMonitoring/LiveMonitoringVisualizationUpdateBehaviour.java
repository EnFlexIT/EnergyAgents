package de.enflexit.energyAgent.ops.liveMonitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.energyAgent.core.aggregation.AbstractAggregationHandler;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;
import jade.core.behaviours.TickerBehaviour;

/**
 * This behaviour periodically triggers the grid state estimation and calculation
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringVisualizationUpdateBehaviour extends TickerBehaviour {

	private static final long serialVersionUID = -2048693429412770813L;
	
	private AbstractAggregationHandler aggregationHandler;
	
	private List<String> sensorIDs;
	
	private boolean dataAvailable = false;

	/**
	 * Instantiates a new live monitoring visualization update behaviour.
	 * @param agent the agent
	 * @param period the period
	 */
	public LiveMonitoringVisualizationUpdateBehaviour(LiveMonitoringAgent agent, long period) {
		super(agent, period);
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.TickerBehaviour#onTick()
	 */
	@Override
	protected void onTick() {
		// --- Trigger estimation and calculation of the grid state -----------
		if (this.isDataAvailable()==true) {
			this.getAggregationHandler().runEvaluationUntil(System.currentTimeMillis());
		} else {
			System.out.println("[" + this.getClass().getSimpleName() + "] Not all required sensor data is available, skipping calculation");
		}
	}
	
	private boolean isDataAvailable() {
		if (dataAvailable==false) {
			// --- Check if the required data has arrived -------
			dataAvailable = checkForSensorData();
		}
		return dataAvailable;
	}
	
	private boolean checkForSensorData() {
		for( int i=0; i<this.getSensorIDs().size(); i++) {
			String sensorID = this.getSensorIDs().get(i);
			ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(sensorID);
			TechnicalSystemStateEvaluation tsseLast = this.getLastTechnicalSystemStateEvaluation(sc);
			if (tsseLast==null) {
				// --- Return false as soon as one sensor without TSSEs has been found ------------
				return false;
			}
		}
		
		// --- If this point is reached, every sensor has at least one TSSE -----------------------
		return true;
	}
	
	/**
	 * Returns the last technical system state evaluation.
	 *
	 * @param sc the {@link ScheduleController} that manages the {@link ScheduleList}
	 * @return the last technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation getLastTechnicalSystemStateEvaluation(ScheduleController sc) {
		
		TechnicalSystemStateEvaluation tsseLast = null;
		if (sc!=null && sc.getScheduleList().getSchedules().size()>0) {
			Schedule schedule = sc.getScheduleList().getSchedules().get(0);
			if (schedule.getTechnicalSystemStateEvaluation()!=null) {
				tsseLast = schedule.getTechnicalSystemStateEvaluation();
			}
		}
		return tsseLast;
	}

	/**
	 * Gets the aggregation handler.
	 * @return the aggregation handler
	 */
	private AbstractAggregationHandler getAggregationHandler() {
		if (aggregationHandler==null) {
			aggregationHandler = ((LiveMonitoringAgent)this.myAgent).getAggregationHandler();
		}
		return aggregationHandler;
	}
	
	/**
	 * Creates the lists of the IDs of all sensors.
	 */
	private List<String> getSensorIDs() {
		if (sensorIDs==null) {
			sensorIDs = new ArrayList<>();
			// --- List all available NetworkComponents --- 
			Vector<NetworkComponent> netComps = this.getNetworkModel().getNetworkComponentVectorSorted();
			
			for (int i = 0; i < netComps.size(); i++) {
				NetworkComponent netComp = netComps.get(i);
				if (netComp.getType().equals("Sensor")==true) {
					sensorIDs.add(netComp.getId());
				}
			}
		}
		
		return sensorIDs;
	}
	
	/**
	 * Returns the current overall network model.
	 * @return the network model
	 */
	protected NetworkModel getNetworkModel() {
		return this.getAggregationHandler().getNetworkModel();
	}
	
}
