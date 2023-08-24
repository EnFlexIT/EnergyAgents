package de.enflexit.ea.electricity.aggregation.triPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationPreprocessor;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.electricity.estimation.AbstractGridStateEstimation;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;

/**
 * The Class ElectricalDistributionGridPreprocessor will be used in case that a state estimation
 * is required. This will be the case, if the known system states in the aggregation are only 
 * described by sensor data.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class TriPhaseElectricalNetworkPreprocessor extends AbstractNetworkCalculationPreprocessor {

	public final static String POWER_FLOW_ESTIMATION_CLASS = "PowerFlowEstimationClass";
	
	private AbstractGridStateEstimation gridStateEstimation; 
	private ArrayList<String> sensorIDs;
	

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractNetworkCalculationPreprocessor#doPreprocessing(long)
	 */
	@Override
	protected boolean doPreprocessing(long evaluationEndTime) {
		
		// --- Only estimate, if the simulation is based on sensor data -------
		if (this.getAggregationHandler().getExecutionDataBase()==ExecutionDataBase.NodePowerFlows) return true;
				
		// --- Start Estimation -----------------------------------------------
		AbstractGridStateEstimation gse = this.getGridStateEstimation();
		if (gse!=null) {
			return gse.doEstimation(this.getLastSensorStates(), evaluationEndTime);
		}
		return false;
	}
	
	/**
	 * Returns the instance of the current GridStateEstimation.
	 * @return the grid state estimation
	 */
	private AbstractGridStateEstimation getGridStateEstimation() {
		if (gridStateEstimation==null) {
			gridStateEstimation = (AbstractGridStateEstimation) this.getSubAggregationConfiguration().getUserClassInstance(POWER_FLOW_ESTIMATION_CLASS);
			if (gridStateEstimation!=null) {
				gridStateEstimation.setNetworkCalculationPreProcessor(this);
				gridStateEstimation.setNetworkModel(this.getNetworkModel());
			}
		}
		return gridStateEstimation;
	}
	
	
	/**
	 * Returns the last sensor states out of the corresponding ScheduleController.
	 * @return the last sensor states
	 */
	protected HashMap<String, TechnicalSystemStateEvaluation> getLastSensorStates() {
		
		HashMap<String, TechnicalSystemStateEvaluation> lastStateHash = new HashMap<>();
		for (String sensorID : this.getSensorIDs()) {
			ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(sensorID);
			TechnicalSystemStateEvaluation tsseLast = this.getLastTechnicalSystemStateEvaluation(sc);
			if (tsseLast!=null) {
				lastStateHash.put(sensorID, tsseLast);
			}
		}
		return lastStateHash;
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
	 * Returns all sensor IDs out of the current NetworkModel.
	 * @return the sensor IDs
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

}