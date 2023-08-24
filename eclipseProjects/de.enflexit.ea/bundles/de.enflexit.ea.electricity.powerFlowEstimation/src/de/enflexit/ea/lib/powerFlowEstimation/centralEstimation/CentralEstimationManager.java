package de.enflexit.ea.lib.powerFlowEstimation.centralEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.lib.powerFlowCalculation.MeasuredBranchCurrent;
import de.enflexit.ea.lib.powerFlowEstimation.AbstractEstimation;
import de.enflexit.ea.lib.powerFlowEstimation.DistrictModel;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;

public class CentralEstimationManager extends AbstractEstimation {
	
	protected final static String REFPV = "REF PV";
	protected final static String VOLTAGE_L1 = "Voltage L1";
	protected final static String VOLTAGE_L2 = "Voltage L2";
	protected final static String VOLTAGE_L3 = "Voltage L3";
	protected final static String CURRENT_L1 = "Current L1";
	protected final static String CURRENT_L2 = "Current L2";
	protected final static String CURRENT_L3 = "Current L3";
	protected final static String COSPHI_L1 = "Cos Phi L1";
	protected final static String COSPHI_L2 = "Cos Phi L2";
	protected final static String COSPHI_L3 = "Cos Phi L3";
	
	private GridStateEstimationInit estimationInit;
	private Vector<DistrictModel> vDistrictAgentModel;
	private boolean estimationSuccessful=false;

	
	//TODO: Rüber in Abstract
	/**
	 * This method returns the init of estimation
	 * @param networkModel
	 * @return
	 */
	private GridStateEstimationInit getGridStateEstimationInit() {
		if (estimationInit==null) {
			estimationInit = new GridStateEstimationInit();
			estimationInit.init(this.getNetworkModel());
		}
		return estimationInit;
	}
	/**
	 * Gets the district agent model vector.
	 * @return the district agent model vector
	 */
	private Vector<DistrictModel> getDistrictAgentModelVector() {
		if (vDistrictAgentModel==null) {
			vDistrictAgentModel = this.getGridStateEstimationInit().buildDistrictAgentModel();
		}
		return vDistrictAgentModel;
	}
	
	/**
	 * This method performs the estimation for the selected phase
	 * @param phase
	 * @param lastSensorStates
	 * @param relPowerOfRefPV
	 * @param evaluationEndTime
	 */
	public void doEstimationPerPhase(Phase phase, HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates,double relPowerOfRefPV, long evaluationEndTime ) {
		
		// --- Get all current measurement of sensors  --------------
		HashMap<String, ElectricalMeasurement> currentMeasurement = this.createHashMapFromTSSEList(lastSensorStates);
		
		// --- Create empty hashmaps for estimation -----------------
		HashMap<String, Double> estimatedNodalPowerRealGlobal = new HashMap<>();
		HashMap<String, Double> estimatedNodalPowerImagGlobal = new HashMap<>();
		HashMap<String, Double> estimatedPVNodesGlobal = new HashMap<>();
		HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents = new HashMap<>();
		
		if (lastSensorStates.size()>0) {
			for(int i=0; i<this.getDistrictAgentModelVector().size();i++) {
				
				// --- Create measurement for selected district Agent
				HashMap<String, ElectricalMeasurement> measurementOfDistrictAgent =this.currentMeasurementSelection(currentMeasurement, getDistrictAgentModelVector().get(i));
				
				// --- Estimation ----------------------------------------------
				GridStateEstimation gridStateEstimation = new GridStateEstimation();
				gridStateEstimation.setEstimationBase(this.getDistrictAgentModelVector().get(i), phase, measurementOfDistrictAgent,relPowerOfRefPV);
				
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
				estimatedBranchCurrents = gridStateEstimation.getEstimatedBranchCurrents();
				
				// --- Insert local Hashmap in global Hashmap --------------------------
				ArrayList<String> keySetNodalPower = new ArrayList<>(estimatedNodalPowerReal.keySet());
				for(int a=0; a<keySetNodalPower.size();a++) {
					estimatedNodalPowerRealGlobal.put(keySetNodalPower.get(a), estimatedNodalPowerReal.get(keySetNodalPower.get(a)));
					estimatedNodalPowerImagGlobal.put(keySetNodalPower.get(a), estimatedNodalPowerImag.get(keySetNodalPower.get(a)));
				}
				
				ArrayList<String> keySetPVNode = new ArrayList<>(estimatedPVNodes.keySet());
				for(int a=0; a< keySetPVNode.size();a++) {
					estimatedPVNodesGlobal.put(keySetPVNode.get(a), estimatedPVNodes.get(keySetPVNode.get(a)));
				}
				
			}
			this.setNodalPowerReal(estimatedNodalPowerRealGlobal);
			this.setNodalPowerImag(estimatedNodalPowerImagGlobal);
			this.setvPVNodes(estimatedPVNodesGlobal);
			this.setEstimatedBranchCurrents(estimatedBranchCurrents);
			this.estimationSuccessful=true;
		}		
	}
	
	
	
	/**
	 * This methods returns the current measurement for the selected district agent
	 * @param currentMeasurement
	 * @param districtAgentModel
	 * @return
	 */
	private HashMap<String, ElectricalMeasurement> currentMeasurementSelection(HashMap<String, ElectricalMeasurement> currentMeasurement, DistrictModel districtAgentModel) {
		HashMap<String, ElectricalMeasurement> selectedMeasurement = new HashMap<>();
		Vector<NetworkComponent> sensors = this.findSensors(districtAgentModel.getvNetworkComponents());
		
		for(int i=0; i<sensors.size();i++) {
			String sensorKey = sensors.get(i).getId();
			ElectricalMeasurement electricalMeasurement = currentMeasurement.get(sensorKey);
			selectedMeasurement.put(sensorKey, electricalMeasurement);
		}
		
		return selectedMeasurement;
	}
	
	private Vector<NetworkComponent> findSensors(Vector<NetworkComponent> networkComponents){
		Vector<NetworkComponent> sensors = new Vector<>();
		
		for(int i=0;i<networkComponents.size();i++) {
			if(networkComponents.get(i).getType().equals("Sensor")) {
				sensors.add(networkComponents.get(i));
			}
		}
		return sensors; 
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
			if(sensorId.equals(REFPV)==false) {
				TechnicalSystemStateEvaluation tsse = lastSensorStates.get(sensorId);
				ElectricalMeasurement electricalMeasurement = this.transformTSSEintoElectricalMeasurement(tsse);
				currentMeasurement.put(sensorId, electricalMeasurement);
			}
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
