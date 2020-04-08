package de.enflexit.energyAgent.lib.powerFlowEstimation.decentralEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import agentgui.logging.components.SysOutBoard;
import de.enflexit.energyAgent.core.globalDataModel.ontology.ElectricalMeasurement;
import de.enflexit.energyAgent.core.globalDataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.energyAgent.core.globalDataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.energyAgent.core.globalDataModel.ontology.UnitValue;
import de.enflexit.energyAgent.lib.powerFlowCalculation.MeasuredBranchCurrent;
import de.enflexit.energyAgent.lib.powerFlowEstimation.decentralEstimation.AbstractGridStateEstimation;
import de.enflexit.energyAgent.lib.powerFlowEstimation.decentralEstimation.DistrictModel;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;

public class DecentralEstimationManager extends AbstractGridStateEstimation {
	
	protected final static String VOLTAGE_L1 = "Voltage L1";
	protected final static String VOLTAGE_L2 = "Voltage L2";
	protected final static String VOLTAGE_L3 = "Voltage L3";
	protected final static String CURRENT_L1 = "Current L1";
	protected final static String CURRENT_L2 = "Current L2";
	protected final static String CURRENT_L3 = "Current L3";
	protected final static String COSPHI_L1 = "Cos Phi L1";
	protected final static String COSPHI_L2 = "Cos Phi L2";
	protected final static String COSPHI_L3 = "Cos Phi L3";
	
	private DistrictModel districtAgentModel;
	private boolean estimationSuccessful=false;

	
	private DistrictModel getDistrictAgentModel() {
		
		if(districtAgentModel==null) {
			this.districtAgentModel = new DistrictModel();
			this.districtAgentModel.initiation(this.getNetworkModel(),false);
		}
		
		return districtAgentModel;
	}

	
	/**
	 * This method performs the estimation for the selected phase
	 * @param phase
	 * @param lastSensorStates
	 * @param relPowerOfRefPV
	 * @param evaluationEndTime
	 */
	public void doEstimationPerPhase(Phase phase, HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates,double relPowerOfRefPV, long evaluationEndTime ) {
		
		if (lastSensorStates.size()>0) {
			// --- Start estimation if all measurements are exist
			if(lastSensorStates.size()==this.getDistrictAgentModel().getnNumSensors()) {
			
				// --- Get all current measurement of sensors  --------------
				HashMap<String, ElectricalMeasurement> currentMeasurement = this.createHashMapFromTSSEList(lastSensorStates);
					
				// --- Create measurement for selected district Agent
				HashMap<String, ElectricalMeasurement> measurementOfDistrictAgent =currentMeasurement;
				
				// --- Estimation ----------------------------------------------
				GridStateEstimation gridStateEstimation = new GridStateEstimation();
				gridStateEstimation.setEstimationBase(this.getDistrictAgentModel(), phase, measurementOfDistrictAgent,relPowerOfRefPV);
				
				// --- Do the estimation -----------------------------------------
				try {
					estimationSuccessful = gridStateEstimation.estimateGridState();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// --- Get the result of the estimation --------------------------
				HashMap<String, Double> estimatedNodalPowerReal =  gridStateEstimation.getNodalPowerReal();
				HashMap<String, Double> estimatedNodalPowerImag = gridStateEstimation.getNodalPowerImag();
				HashMap<String, Double> estimatedPVNodes = gridStateEstimation.getvPVNodes();
				HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents = gridStateEstimation.getEstimatedBranchCurrents();
				this.setNodalPowerReal(estimatedNodalPowerReal);
				this.setNodalPowerImag(estimatedNodalPowerImag);
				this.setvPVNodes(estimatedPVNodes);
				this.setEstimatedBranchCurrents(estimatedBranchCurrents);
				this.estimationSuccessful=true;
			}
		}
	}
	
	/**
	 * This method creates an hasmap from tsse list
	 * @param lastSensorStates
	 * @return
	 */
	private HashMap<String, ElectricalMeasurement> createHashMapFromTSSEList(HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates){
		// --- Hasmap of electrical measurement  --------------------------
		HashMap<String, ElectricalMeasurement> currentMeasurement= new HashMap<>();
		ArrayList<String> keySet = new ArrayList<>(lastSensorStates.keySet());
		
		// --- Transform tsse into current Measurement ---------------------
		for(int a=0; a<lastSensorStates.size();a++) {
			String sensorId = keySet.get(a);
			
			// --- Filter REFPV tsse
//			if(sensorId.equals(REFPV)==false) {
				TechnicalSystemStateEvaluation tsse = lastSensorStates.get(sensorId);
				ElectricalMeasurement electricalMeasurement = this.transformTSSEintoElectricalMeasurement(tsse);
				currentMeasurement.put(sensorId, electricalMeasurement);
//			}
		}
		return currentMeasurement;
	}
	
	/**
	 * This method transform tsse into electrical measurement
	 * @param tsse
	 * @return
	 */
	private ElectricalMeasurement transformTSSEintoElectricalMeasurement(TechnicalSystemStateEvaluation tsse) {
		
		
		FixedDouble fvVoltageL1 = null;
		FixedDouble fvVoltageL2 = null;
		FixedDouble fvVoltageL3 = null;
		
		FixedDouble fvCurrentL1 = null;
		FixedDouble fvCurrentL2 = null;
		FixedDouble fvCurrentL3 = null;

		FixedDouble fvCosPhiL1 = null;
		FixedDouble fvCosPhiL2 = null;
		FixedDouble fvCosPhiL3 = null;

		for (int i = 0; i < tsse.getIOlist().size(); i++) {
			
			FixedDouble fdIoValue = (FixedDouble) tsse.getIOlist().get(i);
			switch (fdIoValue.getVariableID()) {
			case VOLTAGE_L1:
				fvVoltageL1 = fdIoValue;
				break;
			case VOLTAGE_L2:
				fvVoltageL2 = fdIoValue;
				break;
			case VOLTAGE_L3:
				fvVoltageL3 = fdIoValue;
				break;
			case CURRENT_L1:
				fvCurrentL1 = fdIoValue;
				break;
			case CURRENT_L2:
				fvCurrentL2 = fdIoValue;
				break;
			case CURRENT_L3:
				fvCurrentL3 = fdIoValue;
				break;
			case COSPHI_L1:
				fvCosPhiL1 = fdIoValue;
				break;
			case COSPHI_L2:
				fvCosPhiL2 = fdIoValue;
				break;
			case COSPHI_L3:
				fvCosPhiL3 = fdIoValue;
				break;
			}
		}
		
		UniPhaseElectricalNodeState uniPhaseElectricalNodeStateL1 = new UniPhaseElectricalNodeState();
		UniPhaseElectricalNodeState uniPhaseElectricalNodeStateL2 = new UniPhaseElectricalNodeState();
		UniPhaseElectricalNodeState uniPhaseElectricalNodeStateL3 = new UniPhaseElectricalNodeState();
		
		uniPhaseElectricalNodeStateL1.setVoltageAbs(new UnitValue((float)fvVoltageL1.getValue(), "V"));
		uniPhaseElectricalNodeStateL2.setVoltageAbs(new UnitValue((float)fvVoltageL2.getValue(), "V"));
		uniPhaseElectricalNodeStateL3.setVoltageAbs(new UnitValue((float)fvVoltageL3.getValue(), "V"));
		
		uniPhaseElectricalNodeStateL1.setCurrent(new UnitValue((float)fvCurrentL1.getValue(), "A"));
		uniPhaseElectricalNodeStateL2.setCurrent(new UnitValue((float)fvCurrentL2.getValue(), "A"));
		uniPhaseElectricalNodeStateL3.setCurrent(new UnitValue((float)fvCurrentL3.getValue(), "A"));
		
		uniPhaseElectricalNodeStateL1.setCosPhi((float)fvCosPhiL1.getValue());
		uniPhaseElectricalNodeStateL2.setCosPhi((float)fvCosPhiL2.getValue());
		uniPhaseElectricalNodeStateL3.setCosPhi((float)fvCosPhiL3.getValue());
		
		TriPhaseElectricalNodeState triphaseElectricalNodeState = new TriPhaseElectricalNodeState();
		triphaseElectricalNodeState.setL1(uniPhaseElectricalNodeStateL1);
		triphaseElectricalNodeState.setL2(uniPhaseElectricalNodeStateL2);
		triphaseElectricalNodeState.setL3(uniPhaseElectricalNodeStateL3);
		
		ElectricalMeasurement electricalMeasurement = new ElectricalMeasurement();
		electricalMeasurement.setElectricalNodeState(triphaseElectricalNodeState);
		
		return electricalMeasurement;
	}
	

	public boolean isEstimationSuccessful() {
		return estimationSuccessful;
	}

}
