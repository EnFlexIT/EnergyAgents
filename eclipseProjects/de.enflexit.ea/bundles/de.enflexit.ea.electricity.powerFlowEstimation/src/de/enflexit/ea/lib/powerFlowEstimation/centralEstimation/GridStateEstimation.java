package de.enflexit.ea.lib.powerFlowEstimation.centralEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.ontology.ElectricalMeasurement;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.lib.powerFlowCalculation.MeasuredBranchCurrent;
import de.enflexit.ea.lib.powerFlowCalculation.PVNodeParameters;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowCalculationPV;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowParameter;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.Complex;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.PowerPerNode;
import de.enflexit.ea.lib.powerFlowEstimation.decentralEstimation.AbstractGridStateEstimation;
import de.enflexit.ea.lib.powerFlowEstimation.decentralEstimation.DistrictModel;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;


public class GridStateEstimation extends AbstractGridStateEstimation {

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
	
//	private GridStateEstimationInit estimationInit;
	private DistrictModel districtAgentModel;
	private boolean estimationSuccessful=false;
	
	// --- Input of grid state estimation ------------------------------
	private HashMap<String, ElectricalMeasurement> currentMeasurement;
	
	private double deltaVoltage = 0;// Delta Voltage of Grid
	private Complex powerSaldo = new Complex(0,0);// Power Saldo of Grid
	private Complex powerLosses = new Complex(0,0);// Power Losses of Grid
	private double dTotalDERPowerPerAutarkicGrid = 0;
	
	private Vector<PowerPerNode> vLoadPerNode = new Vector<PowerPerNode>();// Load
	private Vector<PowerPerNode> vGenerationPerNode = new Vector<PowerPerNode>();// Generation DER
	private double relPowerOfREFPV;
	
	
	/**
	 * This method sets all necessary input data for estimation
	 * @param getDistrictAgentModel()
	 * @param phase
	 * @param currentMeasurement
	 */
	public void setEstimationBase (DistrictModel districtAgentModel, Phase actualPhase, HashMap<String, ElectricalMeasurement> currentMeasurement, double relPowerOfREFPV) {
		this.setDistrictAgentModel(districtAgentModel);
		this.setCurrentMeasurement(currentMeasurement);
		this.setActualPhase(actualPhase);
		this.setRelPowerOfREFPV(relPowerOfREFPV);
		
		//--- Create empty nodal Power Hashmap
		HashMap<String, Double> emptyPowerHashMap = new HashMap<>();
		
		ArrayList<Integer> keySet = new ArrayList<>(this.getDistrictAgentModel().getNodeNumberToNetworkComponentId().keySet());
		
		for(int a=0; a<keySet.size();a++) {
			int key = keySet.get(a);
			String networkComponentID = this.getDistrictAgentModel().getNodeNumberToNetworkComponentId().get(key);
			emptyPowerHashMap.put(networkComponentID, 0.0);	
		}
		
		this.setNodalPowerReal(emptyPowerHashMap);
		this.setNodalPowerImag(emptyPowerHashMap);
	}
	
	/**
	 * This method estimates the grid state
	 */
	public boolean estimateGridState() {
		
		boolean isEstimationSuccessful= true;
		// --- Calculate Power Injection of each DEA depending on REF PV Plant
		this.calculateDERPowerOfAutarkicGrid();
	
		// --- Build Load Power Saldos, subtracting DEA Power
		this.buildingPowerSaldos();
	
		// --- Checking, if new Method for Power Replacement is possible or old Linear Method is only possible
		if (this.getDistrictAgentModel().isNewMethodPossible()== true) {
	
			// --- Building Delta Voltage of two Sensors
			this.calculateMeasuredDeltaVoltage();
	
			// --- Calculate Power Losses between two sensors
			this.calculatePowerLosses();
	
			// --- This method checks, which loadAssignment (linear, bestCase, worstCase, weighted) is most probably
			this.vLoadPerNode= this.calculateLoadAssignment();
		} else {
	
			// --- New Method is not possible,--> linear load replacement
			this.vLoadPerNode= this.vLinearPowerReplachement();
	
		}
		
		// --- Integrate measured nodes ----------------------------------------------
		this.vLoadPerNode= this.integrateMeasuredNode(this.vLoadPerNode);
		
		
		
		// --- Set PV-Nodes ----------------------------------------------------------
		this.setvPVNodes(this.setPVNodes());
		
		// --- Create Hashmap from Vector
		HashMap<String, Double> pHashMap = this.createHashMapFromLoadPerNode(this.vLoadPerNode,"P");
		HashMap<String, Double> qHashMap = this.createHashMapFromLoadPerNode(this.vLoadPerNode,"Q");
		
		// --- Integrate missing nodes, which have no power
		HashMap<String, Double> pHashMapComplete =this.integrateUnmeasuredNodes(pHashMap);
		HashMap<String, Double> qHashMapComplete =this.integrateUnmeasuredNodes(qHashMap);
		
		
		this.setNodalPowerReal(pHashMapComplete);
		this.setNodalPowerImag(qHashMapComplete);
		
		HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrentsTest = this.estimateBranchCurrents();
		this.setEstimatedBranchCurrents(estimatedBranchCurrentsTest);
		
		return isEstimationSuccessful;
	}
	
	/**
	 * Set measured branch Currents
	 * @return
	 */
	private HashMap<String,MeasuredBranchCurrent> estimateBranchCurrents() {
		HashMap<String,MeasuredBranchCurrent> estimatedBranchCurrents = new HashMap<>();
		String nSensorName = null;
		double current=0;
		ArrayList<String> keySet = new ArrayList<>(currentMeasurement.keySet());
		for(int i=0;i<keySet.size();i++) {
			nSensorName = keySet.get(i);
			current =0;
			
			// --- Get nFromNode and nToNode from Sensor
			NetworkComponent netComp = this.getDistrictAgentModel().getNetworkModel().getNetworkComponent(nSensorName);
			Vector<NetworkComponent> neighbouredNetworkComponnents = this.getDistrictAgentModel().getNetworkModel().getNeighbourNetworkComponents(netComp);
			Vector<NetworkComponent> neighbouredNodes = new Vector<>();
			for(int a=0;a<neighbouredNetworkComponnents.size();a++) {
				if (neighbouredNetworkComponnents.get(a).getType().equals("Sensor")==false&&neighbouredNetworkComponnents.get(a).getType().equals("Cable")==false) {
					neighbouredNodes.add(neighbouredNetworkComponnents.get(a));
				}
			}
			
			
			
			if(this.getActualPhase().equals(Phase.L1)==true)
				current = currentMeasurement.get(nSensorName).getElectricalNodeState().getL1().getCurrent().getValue();
			if(this.getActualPhase().equals(Phase.L2)==true)
				current = currentMeasurement.get(nSensorName).getElectricalNodeState().getL2().getCurrent().getValue();
			if(this.getActualPhase().equals(Phase.L3)==true)
				current = currentMeasurement.get(nSensorName).getElectricalNodeState().getL3().getCurrent().getValue();
			MeasuredBranchCurrent measuredBranchCurrent = new MeasuredBranchCurrent();
			measuredBranchCurrent.setSensorName(nSensorName);
			measuredBranchCurrent.setCurrent(current);
			measuredBranchCurrent.setnFromNodeComponentName(neighbouredNodes.get(0).getId());
			measuredBranchCurrent.setnToNodeComponentName(neighbouredNodes.get(1).getId());
			estimatedBranchCurrents.put(nSensorName, measuredBranchCurrent);
		}
		
		// District agent n33
				if(keySet.size()==8) {
					double currentN17=0;
					double currentN18=0;
					double currentN19=0;
					double currentN20=0;
					double currentN21=0;
					double currentN31=0;
					if(this.getActualPhase().equals(Phase.L1)==true) {
						currentN17= currentMeasurement.get("n17").getElectricalNodeState().getL1().getCurrent().getValue();
						currentN18= currentMeasurement.get("n18").getElectricalNodeState().getL1().getCurrent().getValue();
						currentN19= currentMeasurement.get("n19").getElectricalNodeState().getL1().getCurrent().getValue();
						currentN20= currentMeasurement.get("n20").getElectricalNodeState().getL1().getCurrent().getValue();
						currentN21= currentMeasurement.get("n21").getElectricalNodeState().getL1().getCurrent().getValue();
						currentN31= currentMeasurement.get("n31").getElectricalNodeState().getL1().getCurrent().getValue();
						
					}
					if(this.getActualPhase().equals(Phase.L2)==true) {
						currentN17= currentMeasurement.get("n17").getElectricalNodeState().getL2().getCurrent().getValue();
						currentN18= currentMeasurement.get("n18").getElectricalNodeState().getL2().getCurrent().getValue();
						currentN19= currentMeasurement.get("n19").getElectricalNodeState().getL2().getCurrent().getValue();
						currentN20= currentMeasurement.get("n20").getElectricalNodeState().getL2().getCurrent().getValue();
						currentN21= currentMeasurement.get("n21").getElectricalNodeState().getL2().getCurrent().getValue();
						currentN31= currentMeasurement.get("n31").getElectricalNodeState().getL2().getCurrent().getValue();
					}
					if(this.getActualPhase().equals(Phase.L3)==true) {
						currentN17= currentMeasurement.get("n17").getElectricalNodeState().getL3().getCurrent().getValue();
						currentN18= currentMeasurement.get("n18").getElectricalNodeState().getL3().getCurrent().getValue();
						currentN19= currentMeasurement.get("n19").getElectricalNodeState().getL3().getCurrent().getValue();
						currentN20= currentMeasurement.get("n20").getElectricalNodeState().getL3().getCurrent().getValue();
						currentN21= currentMeasurement.get("n21").getElectricalNodeState().getL3().getCurrent().getValue();
						currentN31= currentMeasurement.get("n31").getElectricalNodeState().getL3().getCurrent().getValue();
					}	
						
					double currentN24= currentN17+currentN18+currentN19+currentN20+currentN21+currentN31;
					String cableName = "n24";
					// --- Get nFromNode and nToNode from Sensor
					NetworkComponent netComp = this.getDistrictAgentModel().getNetworkModel().getNetworkComponent(cableName);
					Vector<NetworkComponent> neighbouredNetworkComponnents = this.getDistrictAgentModel().getNetworkModel().getNeighbourNetworkComponents(netComp);
					Vector<NetworkComponent> neighbouredNodes = new Vector<>();
					for(int a=0;a<neighbouredNetworkComponnents.size();a++) {
						if (neighbouredNetworkComponnents.get(a).getType().equals("Sensor")==false&&neighbouredNetworkComponnents.get(a).getType().equals("Cable")==false) {
							neighbouredNodes.add(neighbouredNetworkComponnents.get(a));
						}
					}
					
					MeasuredBranchCurrent measuredBranchCurrent = new MeasuredBranchCurrent();
					measuredBranchCurrent.setSensorName(cableName);
					measuredBranchCurrent.setCurrent(currentN24);
					measuredBranchCurrent.setnFromNodeComponentName(neighbouredNodes.get(0).getId());
					measuredBranchCurrent.setnToNodeComponentName(neighbouredNodes.get(1).getId());
					estimatedBranchCurrents.put(cableName, measuredBranchCurrent);
				}
				
		return estimatedBranchCurrents;
	}
	

	
	
	/**
	 * This method determines the PV Nodes
	 * 
	 * @return
	 */
	private HashMap<String, Double> setPVNodes() {
		HashMap<String, Double> pvNodes = new HashMap<>();
		
		// --- Check, if number of sensors is greater than 1
		if (this.getDistrictAgentModel().getnNumSensors() > 1) {
			
			
			Vector<Integer> collectedPVNodes = new Vector<Integer>();
			for (int nIndexSensor = 0; nIndexSensor < this.getDistrictAgentModel().getvSensorNames().size(); nIndexSensor++) {
	
				String nSensorName = this.getDistrictAgentModel().getvSensorNames().get(nIndexSensor);
				
				int nNode = this.getDistrictAgentModel().getvBranchMeasurementParams().get(nIndexSensor).getnFromNode();
				
				if (collectedPVNodes.contains(nNode) == false) {
					collectedPVNodes.add(nNode);
					if (nNode == 0) {
						nNode = this.getDistrictAgentModel().getvBranchMeasurementParams().get(nIndexSensor).getnToNode();
					}
					
					double voltage =0;
					
					if(this.getActualPhase().equals(Phase.L1)==true)
						voltage = currentMeasurement.get(nSensorName).getElectricalNodeState().getL1().getVoltageAbs().getValue();
					if(this.getActualPhase().equals(Phase.L2)==true)
						voltage = currentMeasurement.get(nSensorName).getElectricalNodeState().getL2().getVoltageAbs().getValue();
					if(this.getActualPhase().equals(Phase.L3)==true)
						voltage = currentMeasurement.get(nSensorName).getElectricalNodeState().getL3().getVoltageAbs().getValue();
					
					// --- Get network Component ID from node Number
					String networkID = this.getDistrictAgentModel().getNodeNumberToNetworkComponentId().get(nNode);
					
					pvNodes.put(networkID, voltage);
	
				}
			}
	
		} else {
			
			int nNode = this.getDistrictAgentModel().getvBranchMeasurementParams().get(0).getnFromNode();
	
			String nSensorName = this.getDistrictAgentModel().getvSensorNames().get(0);
			
			double voltage =0;
			
			if(this.getActualPhase().equals(Phase.L1)==true)
				voltage = currentMeasurement.get(nSensorName).getElectricalNodeState().getL1().getVoltageAbs().getValue();
			if(this.getActualPhase().equals(Phase.L2)==true)
				voltage = currentMeasurement.get(nSensorName).getElectricalNodeState().getL2().getVoltageAbs().getValue();
			if(this.getActualPhase().equals(Phase.L3)==true)
				voltage = currentMeasurement.get(nSensorName).getElectricalNodeState().getL3().getVoltageAbs().getValue();
			
			// --- Get network Component ID from node Number
			String networkID = this.getDistrictAgentModel().getNodeNumberToNetworkComponentId().get(nNode);
			pvNodes.put(networkID, voltage);
	
		}
		return pvNodes;
	}
	
	/**
	 * This method prepares the Power Flow Calculation
	 * 
	 * @param powerFlowParameter
	 * @param vLoadPerNode
	 * @param vGenerationPerNode
	 * @param nNumNodes
	 * @param deltaVoltageCalculation
	 * @return
	 */
	private PowerFlowParameter preparingPFC(PowerFlowParameter powerFlowParameter, Vector<PowerPerNode> vLoadPerNode,Vector<PowerPerNode> vGenerationPerNode, int nNumNodes, boolean deltaVoltageCalculation) {
		Vector<Double> pK = new Vector<Double>();
		Vector<Double> qK = new Vector<Double>();
		Vector<Double> loadReal = new Vector<Double>();
		Vector<Double> loadImag = new Vector<Double>();
		Vector<Double> generationReal = new Vector<Double>();
		Vector<Double> generationImag = new Vector<Double>();
		int index = 0;
		
		for (int i = 0; i < nNumNodes; i++) {
			pK.add(0.0);
			qK.add(0.0);
			generationReal.add(0.0);
			generationImag.add(0.0);
			loadReal.add(0.0);
			loadImag.add(0.0);
	
		}
	
		// --- Setting Load
		for (int i = 0; i < vLoadPerNode.size(); i++) {
			index = vLoadPerNode.get(i).getnNode() - 1;
			loadReal.set(index, vLoadPerNode.get(i).getComplexPowerPerPhase().getReal());
			loadImag.set(index, vLoadPerNode.get(i).getComplexPowerPerPhase().getImag());
		}
	
		// --- Setting Generation
		for (int i = 0; i < vGenerationPerNode.size(); i++) {
			index = vGenerationPerNode.get(i).getnNode() - 1;
			generationReal.set(index, pK.get(vGenerationPerNode.get(i).getnNode() - 1)+ vGenerationPerNode.get(i).getComplexPowerPerPhase().getReal());
			generationImag.set(index, qK.get(vGenerationPerNode.get(i).getnNode() - 1)+ vGenerationPerNode.get(i).getComplexPowerPerPhase().getImag());
		}
	
		// --- Finally, combine all load, generation
		for (int i = 0; i < nNumNodes; i++) {
			pK.set(i, -1 * loadReal.get(i) + generationReal.get(i));
			qK.set(i, -1 * loadImag.get(i) + generationImag.get(i));
		}
	
		if (deltaVoltageCalculation == false) {
			// --- Add measured power of all sensors----------------------------------------------------------------------------
			double p;
			double q;
			int nNodeIndex = 0;
			double voltage=0;
			double current=0;
			double cosPhi;
			for (int a = 0; a < this.getDistrictAgentModel().getvSensorNames().size(); a++) {
	
				if(this.getActualPhase().equals(Phase.L1)==true) {
					p = 1 * currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL1().getP().getValue();
					q = 1 * currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL1().getQ().getValue();
				}
				if(this.getActualPhase().equals(Phase.L2)==true) {
					p = 1 * currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL2().getP().getValue();
					q = 1 * currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL2().getQ().getValue();
				}
				if(this.getActualPhase().equals(Phase.L3)==true) {
					p = 1 * currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL3().getP().getValue();
					q = 1 * currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL3().getQ().getValue();
				}
			
	
				nNodeIndex = this.getDistrictAgentModel().getvBranchMeasurementParams().get(a).getnFromNode() - 1;
				if (nNodeIndex < 0) {
					nNodeIndex = this.getDistrictAgentModel().getvBranchMeasurementParams().get(a).getnToNode() - 1;
					if(this.getActualPhase().equals(Phase.L1)==true) {
						voltage = currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL1().getVoltageAbs().getValue();
						current = currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL1().getCurrent().getValue();
					}
					if(this.getActualPhase().equals(Phase.L2)==true) {
						voltage = currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL2().getVoltageAbs().getValue();
						current = currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL2().getCurrent().getValue();
					}
					if(this.getActualPhase().equals(Phase.L3)==true) {
						voltage = currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL3().getVoltageAbs().getValue();
						current = currentMeasurement.get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL3().getCurrent().getValue();
					}
					
					cosPhi = 1; // Assuming a cosPhi=1
					// this.getDistrictAgentModel().getcurrentMeasurement().get(this.getDistrictAgentModel().getvSensorNames().get(a)).getElectricalNodeState().getL1().getCosPhi();
					p = voltage * current * cosPhi;
					q = 0;
					pK.set(nNodeIndex, p);
					qK.set(nNodeIndex, q);
				}
			}
		}
		
		// --- Create Hasmap from Vector
		
		this.setNodalPowerReal(this.createHashMapFromVector(pK));
		this.setNodalPowerImag(this.createHashMapFromVector(qK));
		
		powerFlowParameter.setNodalPowerReal(pK);
		powerFlowParameter.setNodalPowerImag(qK);
	
		return powerFlowParameter;
	}
	
	private HashMap<String,Double> createHashMapFromVector(Vector<Double> vector) {
		HashMap<String,Double> hashMap = new HashMap<String,Double>();
		
		for(int i=0;i<vector.size();i++) {
			String networkComponent = this.getDistrictAgentModel().getNodeNumberToNetworkComponentId().get(i);
			hashMap.put(networkComponent, vector.get(i));
			
		}
		
		return hashMap;
	}
	
	private HashMap<String,Double> createHashMapFromLoadPerNode(Vector<PowerPerNode> loadPerNode, String kindOfPower) {
		HashMap<String,Double> hashMap = new HashMap<String,Double>();
		
		for(int i=0;i<loadPerNode.size();i++) {
			String networkComponent = this.getDistrictAgentModel().getNodeNumberToNetworkComponentId().get(loadPerNode.get(i).getnNode());
			if(kindOfPower.equals("P")) {
				hashMap.put(networkComponent, loadPerNode.get(i).getComplexPowerPerPhase().getReal());}
			else {
				hashMap.put(networkComponent, loadPerNode.get(i).getComplexPowerPerPhase().getImag());	
			}
			
		}
		
		return hashMap;
	}
	
	
	/**
	 * This method calculates the Voltage Delta between two sensors
	 */
	private void calculateMeasuredDeltaVoltage() {
	
		String nFirstSensorName = null;
		String nSecondSensorName = null;
		double voltageSensor1 = 0;
		double voltageSensor2 = 0;
		
		// --- Finding first Sensor
		nFirstSensorName = this.getDistrictAgentModel().getFirstSensor();
	
		if (nFirstSensorName != null) {
			// --- Getting voltage form sensor 1
			
			if(this.getActualPhase().equals(Phase.L1)==true) 
				voltageSensor2 =currentMeasurement.get(nFirstSensorName).getElectricalNodeState().getL1().getVoltageAbs().getValue();
			if(this.getActualPhase().equals(Phase.L2)==true)
				voltageSensor2 =currentMeasurement.get(nFirstSensorName).getElectricalNodeState().getL2().getVoltageAbs().getValue();
			if(this.getActualPhase().equals(Phase.L3)==true)
				voltageSensor2 =currentMeasurement.get(nFirstSensorName).getElectricalNodeState().getL3().getVoltageAbs().getValue();
		}
	
		// --- Finding second Sensor
		nSecondSensorName = this.getDistrictAgentModel().getSecondSensor();
	
		if (nSecondSensorName != null) {
			// --- Getting voltage form sensor 2
			
			if(this.getActualPhase().equals(Phase.L1)==true) 
				voltageSensor2 =currentMeasurement.get(nSecondSensorName).getElectricalNodeState().getL1().getVoltageAbs().getValue();
			if(this.getActualPhase().equals(Phase.L2)==true)
				voltageSensor2 =currentMeasurement.get(nSecondSensorName).getElectricalNodeState().getL2().getVoltageAbs().getValue();
			if(this.getActualPhase().equals(Phase.L3)==true)
				voltageSensor2 =currentMeasurement.get(nSecondSensorName).getElectricalNodeState().getL3().getVoltageAbs().getValue();
			
		} else {
			System.err.println("Problem: Delta Voltage Calculation");
		}
		// --- Calculating delta voltage of the two sensors
			this.deltaVoltage =voltageSensor1 - voltageSensor2; 
			
		}
	
	/**
	 * This method calculates the assumed generation of the installed PV-Plants in
	 * the grid
	 */
	private void calculateDERPowerOfAutarkicGrid() {
		// --- Getting normed Power of Reference PV Plant
		double dActualDERPower = 0;
		double dSumDERPower = 0;
	
		Vector<PowerPerNode> vGenerationPerNode = new Vector<PowerPerNode>();
		// --- Calculate temporary power of DER
		for (int i = 0; i < this.getDistrictAgentModel().getvNodeAssociation().size(); i++) {
			PowerPerNode generationPerNode = new PowerPerNode();
			// Trafo is not a DER
			if (this.getDistrictAgentModel().getvNodeAssociation().get(i).getnNode() != this.getDistrictAgentModel().getnSlackNode()) {
				// KW-->1000W
				double dRatedPower = this.getDistrictAgentModel().getvNodeAssociation().get(i).getdPowerAbs();
				dActualDERPower = dRatedPower * this.getRelPowerOfREFPV();
				if (dActualDERPower != 0) {
					generationPerNode.setnNode(i + 1);
					generationPerNode.setComplexPowerPerPhase(new Complex(dActualDERPower, 0));
					vGenerationPerNode.add(generationPerNode);
				}
			}
	
			// this.getDistrictAgentModel().getvNodeAssociation().get(i).setdActivePower(dActualDERPower);
			dSumDERPower = dSumDERPower + dActualDERPower;
		}
	
		// --- Checking, if genarated Power is unequal zero
		if (dSumDERPower != 0) {
			this.vGenerationPerNode= vGenerationPerNode;
		} else {
			PowerPerNode temp = new PowerPerNode(1, new Complex(0, 0));
			vGenerationPerNode.add(temp);
			this.vGenerationPerNode= vGenerationPerNode;
		}
		this.dTotalDERPowerPerAutarkicGrid =dSumDERPower;
	}
	
	/**
	 * This method calculates different ways of a load assignment
	 * 
	 * @return
	 */
	private Vector<PowerPerNode> calculateLoadAssignment() {
	
		// --- This vector includes the measured DeltaVoltages
		double dVoltageDeltaMeasured = this.deltaVoltage;
	
		// --- This vector includes the calculated DeltaVoltages
		Vector<Double> vVoltageDeltaCalculated = new Vector<Double>();
	
		// --- This vector all load assignments for the four methods
		Vector<PowerPerNode> vecLinearLoadAssigment = new Vector<PowerPerNode>();
		Vector<PowerPerNode> vecBestLoadAssigment = new Vector<PowerPerNode>();
		Vector<PowerPerNode> vecWorstLoadAssigment = new Vector<PowerPerNode>();
		
		// Vector<PowerPerNode> vecWeightedLoadAssigment = new Vector<PowerPerNode>();
	
		// --- First: Assuming a linear Power Assignment
		vecLinearLoadAssigment = this.vLinearPowerReplachement();
		vVoltageDeltaCalculated.add(this.calculateDeltaVoltage(vecLinearLoadAssigment));
	
		// --- Second: Assuming a BestCase Power Assignment
		vecBestLoadAssigment = this.vBestCasePowerReplacement();
		vVoltageDeltaCalculated.add(this.calculateDeltaVoltage(vecBestLoadAssigment));
	
		// --- Third: Assuming a WorstCase Power Assignment
		vecWorstLoadAssigment = this.vWorstCasePowerReplacement();
		vVoltageDeltaCalculated.add(this.calculateDeltaVoltage(vecWorstLoadAssigment));
	
		// --- Fourth: Assuming a Weighted Power Assignment
		//TODO
		// vecLoadPerNodePhase1.add(this.vecBestCasePowerReplachement().get(0));
		// vecLoadPerNodePhase2.add(this.vecBestCasePowerReplachement().get(1));
		// vecLoadPerNodePhase3.add(this.vecBestCasePowerReplachement().get(2));
	
		// --- Calculate delta Voltage of assumed load Replacement
	
		// --- Checking, which Assignment has the biggest congruence to measured delta
		// Voltage
		double minVoltageDifference = 100;
		int selectedAssignment = 0;
		Vector<Double> vDeltaMeasuredAndCalculated = new Vector<Double>();
		for (int a = 0; a < vVoltageDeltaCalculated.size(); a++) {
			minVoltageDifference = 100;
			selectedAssignment = 0;
			vDeltaMeasuredAndCalculated.add(vVoltageDeltaCalculated.get(a) - dVoltageDeltaMeasured);
		}
	
		// --- Checking, which Assignment has the smallest deviation
		for (int a = 0; a < vVoltageDeltaCalculated.size(); a++) {
			if (vDeltaMeasuredAndCalculated.get(a) < minVoltageDifference) {
				minVoltageDifference= vDeltaMeasuredAndCalculated.get(a);
				selectedAssignment= a;
			}
	
		}
		// TODO: Only for now: Now we assume a linear load assignment
		selectedAssignment= 0;
	
		// --- Selected Node Assignment: 0=Linear, 1=BestCase, 2=WorstCase
	
		// Vector of selected Load Assginment per Phase
		Vector<PowerPerNode> vSelectedLoadAssignment = new Vector<PowerPerNode>();
	
			switch (selectedAssignment) {
	
			case 0:
				vSelectedLoadAssignment=vecLinearLoadAssigment;
				break;
			case 1:
				vSelectedLoadAssignment=vecBestLoadAssigment;
				break;
			case 2:
				vSelectedLoadAssignment=vecWorstLoadAssigment;
				break;
			case 3:
				// vSelectedLoadAssignment.add(vecWeightedLoadAssigment.get(nPhase));
				break;
			}
			// TODO: Change between linear method and all methods
			// vSelectedLoadAssignment.add(vecLoadPerNodePhase.get(0).get(nPhase));
			
		return vSelectedLoadAssignment;
	
	}
	
	/**
	 * This method calculates the Voltage Delta between two sensors with an assumed
	 * load assignment
	 * 
	 * @param vLoadPerNodePhase
	 * @return
	 */
	private double calculateDeltaVoltage(Vector<PowerPerNode> vLoadPerNodePhase) {
	
		PowerFlowParameter actualPowerFlowParameter = this.getDistrictAgentModel().getPowerFlowParameter();
		
		//TODO: Hier TSSEs
		// --- First sensor is slack Node
		double dSlackVoltage =230.94;
		if(this.getActualPhase().equals(Phase.L1)==true) 
			dSlackVoltage = currentMeasurement.get(this.getDistrictAgentModel().getFirstSensor()).getElectricalNodeState().getL1().getVoltageAbs().getValue();
		if(this.getActualPhase().equals(Phase.L2)==true) 
			dSlackVoltage = currentMeasurement.get(this.getDistrictAgentModel().getFirstSensor()).getElectricalNodeState().getL2().getVoltageAbs().getValue();
		if(this.getActualPhase().equals(Phase.L3)==true) 
			dSlackVoltage = currentMeasurement.get(this.getDistrictAgentModel().getFirstSensor()).getElectricalNodeState().getL3().getVoltageAbs().getValue();	
		
		int nSlackNode = this.getDistrictAgentModel().getvBranchMeasurementParams().get(0).getnFromNode();
		
		Vector<PVNodeParameters> vPvNode = new Vector<PVNodeParameters>();
		PVNodeParameters pvNode = new PVNodeParameters();
		pvNode.setdVoltageOfPVNode(dSlackVoltage);
		pvNode.setnPVNode(nSlackNode);
		vPvNode.add(pvNode);
	
		// --- Setting PV Nodes
		actualPowerFlowParameter.setvPVNodes(vPvNode);
		actualPowerFlowParameter.setdSlackVoltageReal(dSlackVoltage);
		actualPowerFlowParameter.setNodalPowerReal(new Vector<Double>());
		actualPowerFlowParameter.setNodalPowerImag(new Vector<Double>());
	
	
		// --- Prepare PFC
		actualPowerFlowParameter = this.preparingPFC(actualPowerFlowParameter, this.vLoadPerNode,this.vGenerationPerNode, this.getDistrictAgentModel().getnNumNodes(), true);
		
		
		PowerFlowCalculationPV pfc = new PowerFlowCalculationPV();
		pfc.setPowerFlowParameter(actualPowerFlowParameter);
		
		// --- Calculate PowerFlow
		pfc.calculate();
	
		// --- Getting result of powerflow calculation
		Vector<Double> vNodalVoltage = pfc.getNodalVoltageAbs();
	
		// --- Calculating DeltaVoltage
		int nIndexOfSlackNode = this.getDistrictAgentModel().getvBranchMeasurementParams().get(this.getDistrictAgentModel().getnIndexFirstSensor()).getnFromNode() - 1;
		int nIndexOfSensor2 = this.getDistrictAgentModel().getvBranchMeasurementParams().get(this.getDistrictAgentModel().getnIndexSecondSensor()).getnFromNode() - 1;
	
		double calculatedDeltaVoltage = vNodalVoltage.get(nIndexOfSlackNode) - vNodalVoltage.get(nIndexOfSensor2);
	
		return calculatedDeltaVoltage;
	}
	
	/**
	 * This method executes a linear power Assignment
	 * 
	 * @return
	 */
	private Vector<PowerPerNode> vLinearPowerReplachement() {
		Vector<PowerPerNode> vecLoadPerNode = new Vector<PowerPerNode>();
	
		int nActualNode;
	
		// --- Checking if there are loadNodes
		if (this.getDistrictAgentModel().getnNumLoadNodes() != 0) {
			Complex complexPowerPerNode = new Complex(0, 0);
	
			// --- Calculating Power Per Node P= PSaldo/nNumLoadNodes
			complexPowerPerNode.setReal(powerSaldo.getReal() / this.getDistrictAgentModel().getnNumLoadNodes());
	
			// --- Calculating Power Per Node Q= QSaldo/nNumLoadNodes
			complexPowerPerNode.setImag(powerSaldo.getImag() / this.getDistrictAgentModel().getnNumLoadNodes());
	
			// --- Distribute PowerPerNode on Nodes of Grid
			for (int nNode = 0; nNode < this.getDistrictAgentModel().getvNodeAssociation().size(); nNode++) {
				if (this.getDistrictAgentModel().getvNodeAssociation().get(nNode).isLoadNode() == true) {
					PowerPerNode loadPerNodePhase = new PowerPerNode();
	
					nActualNode = this.getDistrictAgentModel().getvNodeAssociation().get(nNode).getnNode();
	
					loadPerNodePhase.setnNode(nActualNode);
					loadPerNodePhase.setComplexPowerPerPhase(complexPowerPerNode);
	
					// --- Add load Per Node for each phase
					vecLoadPerNode.add(loadPerNodePhase);
				}
	
			}
		} else {
			// --- if no LoadNode exists --> Setting Node one as LoadNode with load=0
			for (int nNode = 0; nNode < this.getDistrictAgentModel().getvNodeAssociation().size(); nNode++) {
				if (this.getDistrictAgentModel().getvNodeAssociation().get(nNode).isLoadNode() == true) {
					nActualNode = this.getDistrictAgentModel().getvNodeAssociation().get(nNode).getnNode();
					Complex complexPower =  new Complex(0, 0);
					PowerPerNode loadPerNodePhase = new PowerPerNode();
					loadPerNodePhase.setnNode(nActualNode);
					loadPerNodePhase.setComplexPowerPerPhase(complexPower);
					vecLoadPerNode.add(loadPerNodePhase);
				}
			}
			
		}
	
		return vecLoadPerNode;
	}
	
	/**
	 * This method executes a best-Case Power Replacement
	 * 
	 * @return
	 */
	private Vector<PowerPerNode> vBestCasePowerReplacement() {
		Vector<PowerPerNode> vecLoadPerNode = new Vector<PowerPerNode>();
	
		// --- Checking if there are loadNodes
		if (this.getDistrictAgentModel().getnBestCaseNode() != 0) {
			Complex complexPowerPerNode = new Complex(0, 0);
	
			// --- Calculating Power Per Node P= PSaldo/nNumLoadNodes
			complexPowerPerNode.setReal(powerSaldo.getReal());
	
			// --- Calculating Power Per Node Q= QSaldo/nNumLoadNodes
			complexPowerPerNode.setImag(powerSaldo.getImag());
	
			// --- Distribute PowerPerNode on Best Case Node of Grid
			PowerPerNode loadPerNodePhase = new PowerPerNode();
	
			loadPerNodePhase.setnNode(this.getDistrictAgentModel().getnBestCaseNode());
			loadPerNodePhase.setComplexPowerPerPhase(complexPowerPerNode);
		
	
			// --- Add load Per Node for each phase
			vecLoadPerNode.add(loadPerNodePhase);
		}
	
		return vecLoadPerNode;
	}
	
	/**
	 * This method executes a worst case power replacement
	 * 
	 * @return
	 */
	private Vector<PowerPerNode> vWorstCasePowerReplacement() {
		Vector<PowerPerNode> vecLoadPerNode = new Vector<PowerPerNode>();
	
		// --- Checking if there are loadNodes
		if (this.getDistrictAgentModel().getnWorstCaseNode() != 0) {
			Complex complexPowerPerNode = new Complex(0, 0);
	
			// --- Calculating Power Per Node P= PSaldo/nNumLoadNodes
			complexPowerPerNode.setReal(powerSaldo.getReal());
	
			// --- Calculating Power Per Node Q= QSaldo/nNumLoadNodes
			complexPowerPerNode.setImag(powerSaldo.getImag());
	
			// --- Distribute PowerPerNode on Worst Case Node of Grid
			PowerPerNode loadPerNodePhase = new PowerPerNode();
	
			loadPerNodePhase.setnNode(this.getDistrictAgentModel().getnWorstCaseNode());
			loadPerNodePhase.setComplexPowerPerPhase(complexPowerPerNode);
	
			// --- Add load Per Node for each phase
			vecLoadPerNode.add(loadPerNodePhase);
		}
		
	
		return vecLoadPerNode;
	}
	
	private HashMap<String, Double> integrateUnmeasuredNodes(HashMap<String, Double> hasmapInput){
		
		for(int i=0;i<this.getDistrictAgentModel().getvNetworkComponents().size();i++) {
			if(hasmapInput.get(this.getDistrictAgentModel().getvNetworkComponents().get(i).getId())==null&&this.getDistrictAgentModel().getvNetworkComponents().get(i).getType().equals("Sensor")==false&&this.getDistrictAgentModel().getvNetworkComponents().get(i).getType().equals("Cable")==false){
			   hasmapInput.put(this.getDistrictAgentModel().getvNetworkComponents().get(i).getId(), 0.0);
			}
		}
		
		return hasmapInput;
	}
	
	private Vector<PowerPerNode> integrateMeasuredNode(Vector<PowerPerNode> vecLoadPerNode){
		// --- Integrate measurement into load
		float dU=0,dI=0,dcosPhi=0, p=0, q=0;
		
		 
		 // ---Checking, if sensor measurement is outgoing
		 for (int nSensor = 0; nSensor < this.getDistrictAgentModel().getvSensorNames().size(); nSensor++) {
				String sensorName = this.getDistrictAgentModel().getvSensorNames().get(nSensor);
				NetworkComponent sensorComp = this.getDistrictAgentModel().getNetworkModel().getNetworkComponent(sensorName);
				Vector<NetworkComponent> neighbouredComponent = this.getDistrictAgentModel().getNetworkModel().getNeighbourNetworkComponents(sensorComp);
				Vector<NetworkComponent> neighbouredNodes = new Vector<>();
				
				// ---Filtering, only nodes
				for(int d=0;d<neighbouredComponent.size();d++) {
					if(neighbouredComponent.get(d).getType().equals("Sensor")==false&&neighbouredComponent.get(d).getType().equals("Cable")==false)
					neighbouredNodes.add(neighbouredComponent.get(d));
				}
				int node1 = this.getDistrictAgentModel().getNetworkComponentIdToNodeNumber().get(neighbouredNodes.get(0).getId());
				int node2 = this.getDistrictAgentModel().getNetworkComponentIdToNodeNumber().get(neighbouredNodes.get(1).getId());
				
				// --- Cast netComp to sensor
				Object[] sensordataModel = (Object[]) sensorComp.getDataModel();
				SensorProperties sensor = (SensorProperties) sensordataModel[0];
				int nFromNode=0, nToNode=0;
				
				// --- Finding right direction
				if(neighbouredNodes.get(0).getId().equals(sensor.getMeasureLocation())) {
					nFromNode= node1;
					nToNode=node2;
				}
				else {
					nFromNode= node2;
					nToNode=node1;
				}
	
					if(this.getActualPhase().equals(Phase.L1)==true) {
						dU = currentMeasurement.get(sensorName).getElectricalNodeState().getL1().getVoltageAbs().getValue();
						dI = currentMeasurement.get(sensorName).getElectricalNodeState().getL1().getCurrent().getValue();
						dcosPhi = currentMeasurement.get(sensorName).getElectricalNodeState().getL1().getCosPhi();
						
					}
					if(this.getActualPhase().equals(Phase.L2)==true) {
						dU = currentMeasurement.get(sensorName).getElectricalNodeState().getL2().getVoltageAbs().getValue();
						dI = currentMeasurement.get(sensorName).getElectricalNodeState().getL2().getCurrent().getValue();
						dcosPhi = currentMeasurement.get(sensorName).getElectricalNodeState().getL2().getCosPhi();
					}
					if(this.getActualPhase().equals(Phase.L3)==true) {
						dU = currentMeasurement.get(sensorName).getElectricalNodeState().getL3().getVoltageAbs().getValue();
						dI = currentMeasurement.get(sensorName).getElectricalNodeState().getL3().getCurrent().getValue();
						dcosPhi = currentMeasurement.get(sensorName).getElectricalNodeState().getL3().getCosPhi();
					}
	
					// --- Influence
					double dInfluence = this.getDistrictAgentModel().getvBranchMeasurementParams().get(nSensor).getdInfluenceOnAutarkicGrid();
					double dS = dU * dI * dInfluence*-1;
					if(dcosPhi>1) {
						dcosPhi=1;
					}
					
					p= (float) (dS*dcosPhi);
					q= (float) (dS*Math.sqrt((1-dcosPhi*dcosPhi)));
					
					Complex complexPower = new Complex(p, q);
					
					PowerPerNode measuredNode = new PowerPerNode();
					measuredNode.setComplexPowerPerPhase(complexPower);
					if(dInfluence==1) {
						measuredNode.setnNode(nFromNode);
					}
					else {
						measuredNode.setnNode(nToNode);
					}
					//TODO --- Checking, if node is a not cable cabinet
					for(int a=0;a<this.getDistrictAgentModel().getvNodeAssociation().size();a++) {
						
						if(this.getDistrictAgentModel().getvNodeAssociation().get(a).getnNode()==measuredNode.getnNode()) {
							
							// --- Only insert if node is load Node or PV Plant
							if(this.getDistrictAgentModel().getvNodeAssociation().get(a).isLoadNode()==true||this.getDistrictAgentModel().getvNodeAssociation().get(a).getdPowerAbs()!=0) {
								// --- Checking if node exits already
								boolean exist= false;
								for(int b=0;b<vecLoadPerNode.size();b++) {
									if(vecLoadPerNode.get(b).getnNode()==measuredNode.getnNode()) {
										vecLoadPerNode.get(b).setComplexPowerPerPhase(measuredNode.getComplexPowerPerPhase());
										exist=true;
									}
								}
								if(exist==false) {
									vecLoadPerNode.add(measuredNode);}
							}
						}
						
					}
					
					
		 
	}
		 
	return vecLoadPerNode;
	}
	
	
	/**
	 * This method builds the power Saldos of an autarkic grid
	 */
	private boolean buildingPowerSaldos() {
		boolean isEstimationSuccessful= true;
		double dU= 0;
		double dI= 0;
		double dcosPhi= 1;
		double dInfluence;
		double dSSaldo = 0;
		double dPSaldo = 0;
		double dQSaldo = 0;
		double dPSaldoWithoutDER = 0;
		double dSSaldoWithoutDER = 0;
		int nNode = 0;
	
		for (int nSensor = 0; nSensor < this.getDistrictAgentModel().getvSensorNames().size(); nSensor++) {
			String sensorName = this.getDistrictAgentModel().getvSensorNames().get(nSensor);
			
			nNode = this.getDistrictAgentModel().getvBranchMeasurementParams().get(nSensor).getnFromNode();
	
			// --- Ref-PV and measured Actuators
			if (nNode != 0) {
				
				if(this.getActualPhase().equals(Phase.L1)==true) {
					dU = currentMeasurement.get(sensorName).getElectricalNodeState().getL1().getVoltageAbs().getValue();
					dI = currentMeasurement.get(sensorName).getElectricalNodeState().getL1().getCurrent().getValue();
					dcosPhi = currentMeasurement.get(sensorName).getElectricalNodeState().getL1().getCosPhi();
				}
				if(this.getActualPhase().equals(Phase.L2)==true) {
					dU = currentMeasurement.get(sensorName).getElectricalNodeState().getL2().getVoltageAbs().getValue();
					dI = currentMeasurement.get(sensorName).getElectricalNodeState().getL2().getCurrent().getValue();
					dcosPhi = currentMeasurement.get(sensorName).getElectricalNodeState().getL2().getCosPhi();
				}
				if(this.getActualPhase().equals(Phase.L3)==true) {
					dU = currentMeasurement.get(sensorName).getElectricalNodeState().getL3().getVoltageAbs().getValue();
					dI = currentMeasurement.get(sensorName).getElectricalNodeState().getL3().getCurrent().getValue();
					dcosPhi = currentMeasurement.get(sensorName).getElectricalNodeState().getL3().getCosPhi();
				}
	
				// --- Influence
				dInfluence = this.getDistrictAgentModel().getvBranchMeasurementParams().get(nSensor).getdInfluenceOnAutarkicGrid();
				double dS = dU * dI * dInfluence;
				
				// S = U*I*influence (+1 or-1)
				dSSaldo = dSSaldo + dS;
	
				// P= S*cosPhi
				dPSaldo = dPSaldo + dU * dI * dInfluence * dcosPhi;
			}
		}
	
		// --- Subtracting DER Power from Power Saldo
		double dDERPower = this.dTotalDERPowerPerAutarkicGrid;
	
		// --- Checking, if DER can be substract or has to be added
		if (dPSaldo < 0) {
			// --- Saldo is smaller than DER Power --> DER Power and Saldo are total load
			dPSaldoWithoutDER = 1 * (dDERPower + dPSaldo);
			dSSaldoWithoutDER = 1 * (dDERPower + dSSaldo);
		} else {
			// --- Saldo is bigger than DER Power --> Load = Saldo- DER Power
			dPSaldoWithoutDER = 1 * (dPSaldo + dDERPower);
			dSSaldoWithoutDER = 1 * (dSSaldo + dDERPower);
		}
	
		// Q= (S^2-P^2)^0.5
		// --- Checking if Apparent Power is bigger than Real Power
		if (Math.abs(dSSaldoWithoutDER) > Math.abs(dPSaldoWithoutDER)) {
			dQSaldo = Math.sqrt(dSSaldoWithoutDER * dSSaldoWithoutDER - dPSaldoWithoutDER * dPSaldoWithoutDER);
		} else {
			dQSaldo = 0;
		}
	
		// --- Building Complex Power Saldo of Load
		Complex complexPowerSaldo = new Complex(0, 0);
		
		// --- Checking if calculated load is valid
		if (dPSaldoWithoutDER > 0) {
			complexPowerSaldo = new Complex(dPSaldoWithoutDER, dQSaldo);
		} else {
	//		System.err.println("Estimation Error:  : Saldo of "+this.getActualPhase()+" is negativ!");
			isEstimationSuccessful=false;
		}
	
		// --- Setting Complex Power Saldo
		this.powerSaldo =complexPowerSaldo;
		
		return isEstimationSuccessful;
	}
	
	/**
	 * This method calculates the Power Losses between two sensors
	 */
	private void calculatePowerLosses() {
		Complex complexImpedance = this.getDistrictAgentModel().getComplexImpedance();
		Complex complexPowerLosses = new Complex(0, 0);
	
		double dU;
	
		// Resistance of grid
		double dR = complexImpedance.getReal();
		double dX = complexImpedance.getImag();
	
		// Simple verification to avoid division by zero
		if (complexImpedance.getReal() != 0 && complexImpedance.getImag() != 0) {
	
			// Delta Voltage for each phase
			dU =this.deltaVoltage;
	
			// u^2/(R+jX)=u^2*R/(R^2+X^2)-j*u^2*X/(R^2+X^2)
			complexPowerLosses.setReal(dU * dU * dR / (dR * dR + dX * dX));
			
			complexPowerLosses.setImag(-1 * dU * dU * dX / (dR * dR + dX * dX));
	
		}
		this.setPowerLosses(complexPowerLosses);
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
	
	public Complex getPowerLosses() {
		return powerLosses;
	}
	
	public void setPowerLosses(Complex powerLosses) {
		this.powerLosses = powerLosses;
	}
	
	public HashMap<String, ElectricalMeasurement> getCurrentMeasurement() {
		return currentMeasurement;
	}
	
	public void setCurrentMeasurement(HashMap<String, ElectricalMeasurement> currentMeasurement) {
		this.currentMeasurement = currentMeasurement;
	}
	
	
	public double getRelPowerOfREFPV() {
		return relPowerOfREFPV;
	}
	
	public void setRelPowerOfREFPV(double relPowerOfREFPV) {
		this.relPowerOfREFPV = relPowerOfREFPV;
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
	/* (non-Javadoc)
	 * @see net.agenthygrid.estimation.decentralEstimation.AbstractGridStateEstimation#isEstimationSuccessful()
	 */
	
	public boolean isEstimationSuccessful() {
		return estimationSuccessful;
	}

	private DistrictModel getDistrictAgentModel() {
		return districtAgentModel;
	}

	private void setDistrictAgentModel(DistrictModel districtAgentModel) {
		this.districtAgentModel = districtAgentModel;
	}

	@Override
	public void doEstimationPerPhase(Phase phase, HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates,
			double relPowerOfRefPV, long evaluationEndTime) {
		// TODO Auto-generated method stub
		
	}
	
}

