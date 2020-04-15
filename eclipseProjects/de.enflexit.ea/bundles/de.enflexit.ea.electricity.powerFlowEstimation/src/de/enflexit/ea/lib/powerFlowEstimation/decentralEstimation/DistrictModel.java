package de.enflexit.ea.lib.powerFlowEstimation.decentralEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.lib.powerFlowCalculation.PVNodeParameters;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowParameter;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.BranchMeasurement;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.BranchParams;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.Complex;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.NodeAssignment;
import de.enflexit.ea.lib.powerFlowCalculation.parameter.NodeAssociationParams;
import hygrid.csvFileImport.NetworkModelToCsvMapper;
import hygrid.csvFileImport.NetworkModelToCsvMapper.SetupType;
import jampack.JampackException;

public class DistrictModel {
	
	private NetworkModel networkModel;
	private Vector<NetworkComponent> vNetworkComponents = new Vector<NetworkComponent>();
	private Vector<NetworkComponent> vSensor = new Vector<NetworkComponent>();
	
	// --- Standard Parameters
	private int nNumNodes;
	private int nNumBranches;
	private int nSlackNode;
	private int nGlobalSlackNode;
	private String sensorIdOfSlackNode;
	private int nNumBreakers;
	private int nNumSensors;

	// --- Parameters for Load Assignment of Power Values
	private int nBestCaseNode;
	private int nWorstCaseNode;
	private boolean isNewMethodPossible;
	private int nNumLoadNodes;

	// --- Name of First and Second Sensor
	private String firstSensor;
	private String secondSensor;
	private int nIndexFirstSensor;
	private int nIndexSecondSensor;
	private Vector<String> vSensorNames = new Vector<String>();
	private String sRefPVName= new String();
	


	private NetworkModelToCsvMapper netModelToCsvMapper;
	private HashMap<String, Integer> networkComponentIdToNodeNumber= new HashMap<>();
	private HashMap<Integer, String> nodeNumberToNetworkComponentId= new HashMap<>();

	// --- Chosen Node Assignment
	private Vector<NodeAssignment> vNodeAssignment;

	// --- Internal Parameter Set for local autarkic Grid
	private Vector<BranchParams> vBranchParams = new Vector<BranchParams>();
	private Vector<NodeAssociationParams> vNodeAssociation = new Vector<NodeAssociationParams>();
	private Vector<BranchMeasurement> vBranchMeasurementParams = new Vector<BranchMeasurement>();
	private Vector<PVNodeParameters> vPVNodeParameters = new Vector<PVNodeParameters>();
	private Vector<NetworkComponent> vActuatorParams= new Vector<>();
	private Vector<String> vActuatorNames= new Vector<>();

	// --- Internal Parameter Set for external autarkic Grid
	private Vector<BranchParams> vBranchParamsExternal = new Vector<BranchParams>();
	private Vector<NodeAssociationParams> vNodeAssociationExternal = new Vector<NodeAssociationParams>();
	private Vector<BranchMeasurement> vBranchMeasurementParamsExternal = new Vector<BranchMeasurement>();

	// --- Local Sensitivity Matrix
	private double[][] dSensitivityReal;
	private double[][] dSensitivityImag;

	// --- Necessary Parameters for PowerFlowCalculation
	private PowerFlowParameter powerFlowParameter;

	// --- complex Impedance between two sensors, necessary for power loss calculation
	private Complex complexImpedance = new Complex(0, 0);
	private NetworkComponent networkComponent;
	boolean isCentral=true;
	
	
	public void initiation(NetworkModel networkModel, boolean isCentral) {
		
		if ( networkModel != null) {
			
			this.isCentral= isCentral; 
			
			this.setNetworkModel(networkModel);
			
			// --- Find district agent in networkmodel (to delete from networkModel)
			Vector<NetworkComponent> districtAgentNetworkComponents = this.findComponentByType("DistrictAgent");
			
			if(districtAgentNetworkComponents.size()>0&&districtAgentNetworkComponents!=null) {
				// --- Delete district agent components from network Model
				this.deleteNetworkComponents(districtAgentNetworkComponents);
				
				// --- Set NetworkComponent
				this.setNetworkComponent(districtAgentNetworkComponents.get(0));
				
			}
			
			// ---SetNetworkModel----------------------------------------------------------------------------------------
			this.setvNetworkComponents(this.getNetworkModel().getNetworkComponentVectorSorted());

			// --- Getting GIS-Data from NetworkModel---------------------------------------------------------------------
			double[][] dNodeSetupExternal = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.NodeSetup);
			double[][] dBranchParametersExternal = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.BranchSetup);
			double[][] dSensorParametersExternal = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.SensorSetup);
			
			// --- Get List of networkComponent to Node Number
			networkComponentIdToNodeNumber = this.getNetworkModelToCsvMapper().getNetworkComponentIdToNodeNumber();
			
			// --- Switch Hashmap ------------------------------------------------------------------------------------------
			setNodeNumberToNetworkComponentId(this.switchHashMap(networkComponentIdToNodeNumber));
			
			// --- Transform networkComponent ID to Node Number -------------------------------------------------------------
			nodeNumberToNetworkComponentId =this.switchHashMap(networkComponentIdToNodeNumber);
			
			if (dNodeSetupExternal != null && dBranchParametersExternal != null && dSensorParametersExternal != null) {
				Vector<double[][]> vNodeSetupExternal = convertingDoubleToVector(dNodeSetupExternal);
				Vector<double[][]> vBranchParametersExternal = convertingDoubleToVector(dBranchParametersExternal);
				Vector<double[][]> vSensorParametersExternal = convertingDoubleToVector(dSensorParametersExternal);

				// --- Finding Sensors from NetworkModel----------------------------------------------------------------------
				Vector<String> vSensorNames = findingSensors();
				
				// --- Initialization of districtAgents---------------------------------------------------------------------------------------------
				try {
					this.initDistrictAgent(vNodeSetupExternal, vBranchParametersExternal, vSensorParametersExternal,vSensorNames);
				} catch (JampackException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	private HashMap<Integer, String> switchHashMap(HashMap<String, Integer> networkComponentIdToNodeNumber){
		HashMap<Integer,String> nodeNumberToNetworkComponentId = new HashMap<>();
		ArrayList<String> keySet = new ArrayList<>(networkComponentIdToNodeNumber.keySet());
		
		for(int a=0; a<networkComponentIdToNodeNumber.size();a++) {
			String networkComponentId = keySet.get(a);
			int nodeNumber = networkComponentIdToNodeNumber.get(networkComponentId);
			nodeNumberToNetworkComponentId.put(nodeNumber,networkComponentId);
		}
		return nodeNumberToNetworkComponentId;
	}
	
	private void deleteNetworkComponents (Vector<NetworkComponent> networkComponentsToDelete) {
		NetworkModel networkModelNew = this.getNetworkModel();
		// --- Delete District Agent Component from networkmodel
		for(int i=0; i<networkComponentsToDelete.size();i++) {
			networkModelNew.removeNetworkComponent(networkComponentsToDelete.get(i));
		}
		
		this.setNetworkModel(networkModelNew);
	}
	
	/**
	 * Init of district Agent
	 * @param vNodeSetupExternal
	 * @param vBranchParametersExternal
	 * @param vSensorParametersExternal
	 * @param vSensorNames
	 * @throws JampackException
	 */
	private void initDistrictAgent(Vector<double[][]> vNodeSetupExternal, Vector<double[][]> vBranchParametersExternal,Vector<double[][]> vSensorParametersExternal, Vector<String> vSensorNames) throws JampackException{
		
		// --- Set Name of Sensors
		this.setvSensorNames(vSensorNames);
		int nSlackNode =1;
		
		
		
		if(this.isCentral==true) {
			// --- Central estimation has transformer as slackNode
			for(int i=0;i<this.getvNetworkComponents().size();i++) {
				if(this.getvNetworkComponents().get(i).getType().equals("Transformer")) {
					nSlackNode = this.getNetworkModelToCsvMapper().getNetworkComponentIdToNodeNumber().get(this.getvNetworkComponents().get(i).getId());
				}
				
			}
		}
		else {
			// --- Decentral estimation: First sensor is slack Node ------------------------------------------------------------------------------------
			String key= vSensorNames.get(0);
			NetworkComponent sensorComp = this.getNetworkModel().getNetworkComponent(key);
			Object[] sensordataModel = (Object[]) sensorComp.getDataModel();
			SensorProperties sensor = (SensorProperties) sensordataModel[0];
			String measureLocation = sensor.getMeasureLocation();
			String fromNodeID = this.getNetworkModel().getNetworkComponent(measureLocation).getId();
			nSlackNode = this.getNetworkModelToCsvMapper().getNetworkComponentIdToNodeNumber().get(fromNodeID);
		}
		
		
		
		// --- Getting global SlackNode
		this.setnSlackNode(nSlackNode);
	
//		this.setnGlobalSlackNode(nSlackNode);
	
		// Building Internal NetworkModel with external Data------------------------------------------------------------------
		this.buildExternalNetworkModel(vNodeSetupExternal, vBranchParametersExternal, vSensorParametersExternal);
	
		// --- Building local ParameterSet with internal Node Assignment------------------------------------------------------
		this.setnNumNodes(vNodeSetupExternal.size());
		this.buildInternalNodeAssociation();
	
		// --- Building BranchMeasurement Params------------------------------------------------------------------------------
		this.buildInternalBranchMeasurementParams();
		this.setnNumSensors(vSensorParametersExternal.size());
	
		// --- In Case of one Node, do not calculate BranchParams and PowerFlowParameter--------------------------------------
		if (vNodeSetupExternal.size() > 1) {
	
			// --- Building Internal for Branch Params------------------------------------------------------------------------
			this.buildInternalBranchParams();
			this.setnNumBranches(vBranchParametersExternal.size());
			
			// --- Building actuatorList
			this.buildActuatorList();
	
			// --- Determine Sensor Influence
			this.buildSensorInfluence();
	
			// --- Setting powerflow parameters-------------------------------------------------------------------------------
			PowerFlowParameter powerFlowParameter = new PowerFlowParameter(this.getvNodeAssociation(),this.getvBranchParams(), this.getnSlackNode(), 400.0/Math.sqrt(3));
			
			this.setPowerFlowParameter(powerFlowParameter);
			// this.getvPowerFlowParameter().get(0).setvPVNodes(this.getvPVNodeParameters());
						try {
				// --- Building
				// Sensitivity-Matrix----------------------------------------------------------------------------
				BuildSensitivity Sens = new BuildSensitivity();
				Sens.calculateSensitivity(this.getPowerFlowParameter().getYkkReal(),this.getPowerFlowParameter().getYkkImag(),this.nSlackNode, this.getnNumNodes());
	
				// --- Setting local sensitivity matrix-----------------------------------------------------------------------
				this.setdSensitivityReal(Sens.getSensreal());
				this.setdSensitivityImag(Sens.getSensimag());
	
				// --- Find, which Node becomes SlackNode and which PV-Node
				this.findSlackNodeAndPVNodes();
			} catch (NullPointerException e) {
				System.err.println("District Agent: " + " :Error calculation of Sensitivity Matrix");
			}
	
		}
	
		// --- Checking if new loadAssignment Method is possible--------------------------------------------------------------
		this.checkingIfNewMethodPossible();
	
		// --- Determine bestCase and worstCase Node--------------------------------------------------------------------------
		this.determineBestAndWorstCaseNode();
	
		// --- Determine impedance between two sensors, if new method is possible, necessary for power loss calculation-------
		if (this.isNewMethodPossible() == true) {
			// --- Find first and second sensor
			this.findFirstAndSecondSensor();
			this.calculateImpendanceBetweenTwoSensors();
		}
	}
	
	private Vector<NetworkComponent> findComponentByType(String type) {
		Vector<NetworkComponent> networkComponents = this.getNetworkModel().getNetworkComponentVectorSorted();
		Vector<NetworkComponent> networkComponentsToDelete= new Vector<>();
		for (int a = 0; a < networkComponents.size(); a++) {
			if (networkComponents.get(a).getType().equals(type)) {
				networkComponentsToDelete.add(networkComponents.get(a));
			}
		}
		return networkComponentsToDelete;	
	}
	
	
	/**
	 * This method finds the names of the sensors in the grid
	 * 
	 * @return
	 */
	private Vector<String> findingSensors() {
		Vector<String> vSensorNames = new Vector<String>();
		Vector<NetworkComponent> networkComponents = this.getNetworkModel().getNetworkComponentVectorSorted();

		for (int a = 0; a < networkComponents.size(); a++) {
			if (networkComponents.get(a).getType().equals("Sensor")) {
				vSensorNames.add(networkComponents.get(a).getId());
			}
		}
		return vSensorNames;
	}
	
	private void buildActuatorList() {
		Vector<NetworkComponent> vActuatorList = new  Vector<NetworkComponent>();
		Vector<String> vActuatorListNames = new Vector<>();
		for(int i=0;i<this.getvNetworkComponents().size();i++) {
			if(this.getvNetworkComponents().get(i).getType().equals("ChargingStation")==true||this.getvNetworkComponents().get(i).getType().equals("REF-PV")==true||this.getvNetworkComponents().get(i).getType().equals("Electrolyser")==true||this.getvNetworkComponents().get(i).getType().equals("Windturbine")==true) {
				vActuatorList.add(this.getvNetworkComponents().get(i));
				vActuatorListNames.add(this.getvNetworkComponents().get(i).getId());
			}
		}
		this.setvActuatorParams(vActuatorList);
		this.setvActuatorNames(vActuatorListNames);
	}
	
	/**
	 * This method finds the first and the second sensor
	 */
	private void findFirstAndSecondSensor() {

		double dMinimalSensitivity = 100;
		double dMaximalSensitivity = 0;
		double dActualSensitivity;
		int nSearchedNode = 0;
		String firstSens = null;
		String secondSens = null;

		for (int nSensorIndex = 0; nSensorIndex < this.getvBranchMeasurementParams()
				.size(); nSensorIndex++) {

			// --- Getting
			nSearchedNode = this.getvBranchMeasurementParams().get(nSensorIndex).getnFromNode();
			if (nSearchedNode != 0) {

				// nSearchedNode =this.getNodeAssignmentGlobal2Local().get(nActualNodeIndex);

				// --- Getting Sensitivity from SlackNode to searched Node
				dActualSensitivity = this.getdSensitivityReal()[nSearchedNode - 1][this
						.getnGlobalSlackNode() - 1];

				// --- Finding Sensor, which is the nearest to global slack
				if (dActualSensitivity > dMaximalSensitivity) {
					dMaximalSensitivity = dActualSensitivity;
					secondSens = this.getvSensorNames().get(nSensorIndex);
					this.setnIndexSecondSensor(nSensorIndex);
				}

				// --- Finding Sensor, which has the biggest distance to global slack
				if (dActualSensitivity < dMinimalSensitivity) {
					dMinimalSensitivity = dActualSensitivity;
					firstSens = this.getvSensorNames().get(nSensorIndex);
					this.setnIndexFirstSensor(nSensorIndex);
				}

			}
		}

		// --- Setting first and second Sensor
		if (firstSens != null && secondSens != null) {
			this.setFirstSensor(firstSens);
			this.setSecondSensor(secondSens);
		} else {
			System.err.println("No first or second sensor found!");
		}

	}
	
	/**
	 * This method converts a Double Array to an Vector Array
	 * 
	 * @param Array
	 * @return
	 */
	private Vector<double[][]> convertingDoubleToVector(double[][] Array) {
		Vector<double[][]> vector = new Vector<double[][]>();
		for (int a = 0; a < Array.length; a++) {
			double[][] dTemp = new double[1][Array[0].length];
			for (int b = 0; b < Array[0].length; b++) {
				dTemp[0][b] = Array[a][b];
			}
			vector.add(dTemp);
		}
		return vector;

	}
	
	/**
	 * --- This method builds the internal Network Model
	 * 
	 * @param dNodeSetup
	 * @param dBranchParameters
	 * @param dSensorParameters
	 */
	private void buildExternalNetworkModel(Vector<double[][]> dNodeSetup, Vector<double[][]> dBranchParameters,
			Vector<double[][]> dSensorParameters) {

		Vector<NodeAssociationParams> vNodeAssociationExternal = new Vector<NodeAssociationParams>();
		int nNumLoadNodes = 0;
		// --- Building vNodeAssociation
		for (int i = 0; i < dNodeSetup.size(); i++) {

			NodeAssociationParams temp = new NodeAssociationParams();
			temp.setnNode((int) dNodeSetup.get(i)[0][0]);
			if (dNodeSetup.get(i)[0][1] == 0) {
				temp.setLoadNode(false);

			} else {
				temp.setLoadNode(true);
				nNumLoadNodes = nNumLoadNodes + 1;
			}
			temp.setPower(dNodeSetup.get(i)[0][2] * 1000);
			vNodeAssociationExternal.add(temp);
		}
		this.setnNumLoadNodes(nNumLoadNodes);
		this.setvNodeAssociationExternal(vNodeAssociationExternal);

		// --- Building vBranchParams
		// If only one Node exists, no Branches are available
		if (dNodeSetup.size() > 1) {
			Vector<BranchParams> vBranchParamsExternal = new Vector<BranchParams>();
			for (int i = 0; i < dBranchParameters.size(); i++) {
				BranchParams temp = new BranchParams();
				temp.setnFromNode((int) dBranchParameters.get(i)[0][0]);
				temp.setnToNode((int) dBranchParameters.get(i)[0][1]);
				temp.setdLength(dBranchParameters.get(i)[0][2]);
				temp.setdR(dBranchParameters.get(i)[0][3]);
				temp.setdX(dBranchParameters.get(i)[0][4]);
				temp.setdMaxCurrent(dBranchParameters.get(i)[0][5]);
				vBranchParamsExternal.add(temp);
			}
			this.setvBranchParamsExternal(vBranchParamsExternal);
		}

		if (dSensorParameters.size() > 0) {
			Vector<BranchMeasurement> vBranchMeasurementParamsExternal = new Vector<BranchMeasurement>();
			for (int i = 0; i < dSensorParameters.size(); i++) {
				BranchMeasurement temp = new BranchMeasurement();
				temp.setnFromNode((int) dSensorParameters.get(i)[0][0]);
				temp.setnToNode((int) dSensorParameters.get(i)[0][1]);
				vBranchMeasurementParamsExternal.add(temp);
			}
			this.setvBranchMeasurementParamsExternal(vBranchMeasurementParamsExternal);
		}
	}

	/**
	 * This method builds the internal Branch Measurement Params------------------------------------
	 */
	private void buildInternalBranchMeasurementParams() {
		int localFromNode = 0, localToNode = 0;
		Vector<BranchMeasurement> vBranchMeasurementParams = new Vector<BranchMeasurement>();
		for (int i = 0; i < this.getvBranchMeasurementParamsExternal().size(); i++) {
			BranchMeasurement temp = new BranchMeasurement();

			// Searching for Node
			localFromNode = this.getvBranchMeasurementParamsExternal().get(i).getnFromNode();
			localToNode = this.getvBranchMeasurementParamsExternal().get(i).getnToNode();

			if (localFromNode != 0) {
//				localFromNode = this.getNodeAssignmentGlobal2Local().get(globalFromNode);
//				localToNode = this.getNodeAssignmentGlobal2Local().get(globalToNode);
				temp.setnFromNode(localFromNode);
				temp.setnToNode(localToNode);
				temp.setdInfluenceOnAutarkicGrid(
						this.getvBranchMeasurementParamsExternal().get(i).getdInfluenceOnAutarkicGrid());
			} else {
				localFromNode = this.getvBranchMeasurementParamsExternal().get(i).getnFromNode();
				localToNode = this.getvBranchMeasurementParamsExternal().get(i).getnToNode();
				localFromNode = 0;
//				localToNode = this.getNodeAssignmentGlobal2Local().get(globalToNode);
				temp.setnFromNode(localFromNode);
				temp.setnToNode(localToNode);
				temp.setdInfluenceOnAutarkicGrid(
						this.getvBranchMeasurementParamsExternal().get(i).getdInfluenceOnAutarkicGrid());
			}
			vBranchMeasurementParams.add(temp);
		}
		this.setvBranchMeasurementParams(vBranchMeasurementParams);

	}

	/**
	 * This methods builds an array, where the influence of each sensors to the autarkic grid is defined.
	 */
	private void buildSensorInfluence() {

		for (int nSensor = 0; nSensor < this.getvBranchMeasurementParams().size(); nSensor++) {
			int nFromNode = this.getvBranchMeasurementParams().get(nSensor).getnToNode();
			int numberOfAdjacences = 0;

			// --- Determine, how many adjacencies ToNode From Sensor has
			for (int i = 0; i < this.getvBranchParams().size(); i++) {
				if (nFromNode == this.getvBranchParams().get(i).getnFromNode()
						|| nFromNode == this.getvBranchParams().get(i).getnToNode()) {
					numberOfAdjacences++;

				}
			}
			if (numberOfAdjacences > 1) {
				// --- if sensor has more than 1 adjacencies, sensor influence is positive,
				// because toNode sensor is in autarkic grid -->
				// (adjacency)---(ToNode)---(adjacency)
				this.getvBranchMeasurementParams().get(nSensor).setdInfluenceOnAutarkicGrid(1);
				this.getvBranchMeasurementParamsExternal().get(nSensor).setdInfluenceOnAutarkicGrid(1);
			} else {
				// --- if sensor has only 1 adjacencies, sensor influence is negative, because
				// toNode sensor is border of autarkic grid-->
				// (ToNode)---(adjacency)
				if (this.getvBranchMeasurementParams().get(nSensor).getnFromNode() != 0) {
					this.getvBranchMeasurementParams().get(nSensor).setdInfluenceOnAutarkicGrid(-1);
					this.getvBranchMeasurementParamsExternal().get(nSensor)
							.setdInfluenceOnAutarkicGrid(-1);
				} else {
					// --- DER Injection must be positive
					this.getvBranchMeasurementParams().get(nSensor).setdInfluenceOnAutarkicGrid(1);
					this.getvBranchMeasurementParamsExternal().get(nSensor).setdInfluenceOnAutarkicGrid(1);
				}
			}
		}

	}

	/**
	 * --- This method builds the internal Node Association-----------------------------------------------------------------------
	 */
	private void buildInternalNodeAssociation() {
		Vector<NodeAssociationParams> vNodeAssociation = new Vector<NodeAssociationParams>();
		for (int i = 0; i < this.getvNodeAssociationExternal().size(); i++) {
			NodeAssociationParams temp = new NodeAssociationParams();

			// Searching for Node
			int searchingNode = this.getvNodeAssociationExternal().get(i).getnNode();
//			int searchedNode = this.getNodeAssignmentGlobal2Local().get(searchingNode);
			temp.setLoadNode(this.getvNodeAssociationExternal().get(i).isLoadNode());
			temp.setnNode(searchingNode);
			temp.setPower(this.getvNodeAssociationExternal().get(i).getdPowerAbs());
			vNodeAssociation.add(temp);
		}
		this.setvNodeAssociation(vNodeAssociation);
	}

	/**
	 * --- This method builds the internal Branch Params
	 */
	private void buildInternalBranchParams() {
		Vector<BranchParams> vBranchParams = new Vector<BranchParams>();
		int searchingFromNode = 0, searchingToNode = 0;
		if (this.getvBranchParamsExternal().size() != 0) {

			for (int i = 0; i < this.getvBranchParamsExternal().size(); i++) {
				BranchParams temp = new BranchParams();
				// Searching for Node
				// For From Node
				searchingFromNode = this.getvBranchParamsExternal().get(i).getnFromNode();
//				int searchingFromNodeLocal = this.getNodeAssignmentGlobal2Local().get(searchingFromNode);
				temp.setnFromNode(searchingFromNode);

				searchingToNode = this.getvBranchParamsExternal().get(i).getnToNode();
//				int searchingToNodeLocal = this.getNodeAssignmentGlobal2Local().get(searchingToNode);
				temp.setnToNode(searchingToNode);

				temp.setdLength(this.getvBranchParamsExternal().get(i).getdLength());
				temp.setdR(this.getvBranchParamsExternal().get(i).getdR());
				temp.setdX(this.getvBranchParamsExternal().get(i).getdX());
				temp.setdMaxCurrent(this.getvBranchParamsExternal().get(i).getdMaxCurrent());
				vBranchParams.add(temp);
			}

		}
		this.setvBranchParams(vBranchParams);
	}

	/**
	 * --- Calculates complex impedance between two sensors, necessary for power
	 * loss calculation
	 */
	private void calculateImpendanceBetweenTwoSensors() {

		// --- Getting Index of Sensors
		int nIndexOfSensor1 = this.getvBranchMeasurementParams().get(0).getnFromNode() - 1;
		int nIndexOfSensor2 = this.getvBranchMeasurementParams().get(1).getnFromNode() - 1;

		// --- Calculate Impedance with Sensitivity Matrix
		// Sens(P1,P1)+Sens(P2,P2)-Sens(P1,P2)-Sens(P2,P1)
		double dImpedanceReal = 0;
		double dImpedanceImag = 0;
		if (nIndexOfSensor1 >= 0 && nIndexOfSensor2 >= 0) {
			dImpedanceReal = this.getdSensitivityReal()[nIndexOfSensor1][nIndexOfSensor1]+ this.getdSensitivityReal()[nIndexOfSensor2][nIndexOfSensor2]- this.getdSensitivityReal()[nIndexOfSensor1][nIndexOfSensor2]	- this.getdSensitivityReal()[nIndexOfSensor2][nIndexOfSensor1];
			dImpedanceImag = this.getdSensitivityImag()[nIndexOfSensor1][nIndexOfSensor1]+ this.getdSensitivityImag()[nIndexOfSensor2][nIndexOfSensor2]- this.getdSensitivityImag()[nIndexOfSensor1][nIndexOfSensor2]- this.getdSensitivityImag()[nIndexOfSensor2][nIndexOfSensor1];

		}
		Complex complexImpedance = new Complex(dImpedanceReal, dImpedanceImag);
		this.setComplexImpedance(complexImpedance);

	}

	/**
	 * --- This method finds the slack node and nodes with pv injection
	 */
	private void findSlackNodeAndPVNodes() {

		// --- Searching SlackNode and PV Nodes
		// --- If there is only one Measurement, FromNode is slack and PVNode
		if (this.getvBranchMeasurementParams().size() > 0) {
			Vector<PVNodeParameters> vPVNodeParameters = new Vector<PVNodeParameters>();
			if (this.getvBranchMeasurementParams().size() == 1) {
				this.setnSlackNode(this.getvBranchMeasurementParams().get(0).getnFromNode());
				PVNodeParameters temp = new PVNodeParameters();

				temp.setnPVNode(this.getvBranchMeasurementParams().get(0).getnFromNode());
				vPVNodeParameters.add(temp);
				
				this.setvPVNodeParameters(vPVNodeParameters);

			}
			// --- If there are more Measurements, slackNode has to be found
			else if (this.getvBranchMeasurementParams().size() > 1) {

				// First, setting all PVNodes
				Vector<Integer> addedPVNodes = new Vector<>();
				PVNodeParameters temp = new PVNodeParameters();
				for (int a = 0; a < this.getvBranchMeasurementParams().size(); a++) {
					
					if (this.getvBranchMeasurementParams().get(a).getnFromNode() != 0) {
						
						temp.setnPVNode(this.getvBranchMeasurementParams().get(a).getnFromNode());
						
					} else {
						// TODO: Fraglich ob gemessene PV Anlagen auch als PVNode agieren
						temp.setnPVNode(this.getvBranchMeasurementParams().get(a).getnToNode());

					}
					if(addedPVNodes.contains(temp.getnPVNode())==false) {
					addedPVNodes.add(temp.getnPVNode());
					}
					
				}
				vPVNodeParameters.add(temp);
				this.setvPVNodeParameters(vPVNodeParameters);

				// --- Finding Slack Node--> SlackNode is the nearest node to
				// global slack node
				double minSensitvity = 100;
				double dActualSensitivity;
				int nSlackNode = 0;
				int searchingNodeIndex;

				for (int a = 0; a < vPVNodeParameters.size(); a++) {
					searchingNodeIndex = this.getvPVNodeParameters().get(a).getnPVNode()-1;
					dActualSensitivity = this.getdSensitivityReal()[searchingNodeIndex][searchingNodeIndex];
					if (dActualSensitivity < minSensitvity) {
						nSlackNode = searchingNodeIndex+1;
						minSensitvity = dActualSensitivity;
					}

				}
				if (nSlackNode != 0) {
					this.setnSlackNode(nSlackNode);
				} else {
					System.out.println("No SlackNode found ");
					System.out.println("Check BranchMeasurement!");
				}
			}

		} else {
			System.out.println("No SlackNode found for Agent: ");
			System.out.println("Check BranchMeasurement!");
		}

	}

	/**
	 * --- This method determines best and worst case node
	 */
	private void determineBestAndWorstCaseNode() {
		double dMinimalSensitivity = 100;
		double dMaximalSensitivity = 0;
		double dActualSensitivity;
		int nBestCaseNode = 0;
		int nWorstCaseNode = 0;
		int nSearchedNodeIndex = 0;
		int nSlackNodeIndex = 0;
		for (int a = 0; a < this.getvNodeAssociation().size(); a++) {

			// --- If node is load node
			if (this.getvNodeAssociation().get(a).isLoadNode()) {
				// nActualNodeIndex = this.getvNodeAssociation().get(a).getnNode()
				// - 1;
				nSlackNodeIndex = this.getnSlackNode() - 1;
				nSearchedNodeIndex = this.getvNodeAssociation().get(a).getnNode() - 1;

				// --- Getting Sensitivity from SlackNode to searched Node
				dActualSensitivity = this.getdSensitivityReal()[nSlackNodeIndex][nSearchedNodeIndex];

				// --- Finding BestCase Node, here the Sensitivity to itself is the biggest
				if (dActualSensitivity > dMaximalSensitivity) {
					nWorstCaseNode = nSearchedNodeIndex + 1;
					dMaximalSensitivity = dActualSensitivity;
				}

				// --- Finding WorstCase Node, here the Sensitivity to itself is the smallest
				if (dActualSensitivity < dMinimalSensitivity) {
					nBestCaseNode = nSearchedNodeIndex + 1;
					dMinimalSensitivity = dActualSensitivity;
				}

			}

		}

		// --- Setting worst and best case node per autarkic grid
		this.setnBestCaseNode(nBestCaseNode);
		this.setnWorstCaseNode(nWorstCaseNode);

	}

	/**
	 * This method checks if new method is possible
	 */
	private void checkingIfNewMethodPossible() {
		int nNumSensors = 0;

		// --- Checking, how much sensors are borders of autarkic grid
		for (int a = 0; a < this.getvBranchMeasurementParams().size(); a++) {
			if (this.getvBranchMeasurementParams().get(a).getnFromNode() != 0) {
				nNumSensors++;
			}
		}

		// --- if number of Sensors is equal 2, then new method is possible
		if (nNumSensors == 2) {
			this.setNewMethodPossible(true);
		} else {
			this.setNewMethodPossible(false);
		}
	}

	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
	/**
	 * Returns the local {@link NetworkModelToCsvMapper}.
	 * 
	 * @return the NetworkModelToCsvMapper
	 */
	public NetworkModelToCsvMapper getNetworkModelToCsvMapper() {
		if (netModelToCsvMapper == null) {
			// TODO if this.isCentral==true, DomainCluster have to be created and used !!!
			if (this.isCentral==true) {
				System.err.println("[" + this.getClass().getSimpleName() + "] TODO: DistrictModel is for central use, but no DomainCluster are created!");
				netModelToCsvMapper = new NetworkModelToCsvMapper(this.getNetworkModel(), null);
			} else {
				netModelToCsvMapper = new NetworkModelToCsvMapper(this.getNetworkModel(), null);
			}
		}
		return netModelToCsvMapper;
	}
	public void setNetModelToCsvMapper(NetworkModelToCsvMapper netModelToCsvMapper) {
		this.netModelToCsvMapper = netModelToCsvMapper;
	}

	public Vector<NetworkComponent> getvNetworkComponents() {
		return vNetworkComponents;
	}
	public void setvNetworkComponents(Vector<NetworkComponent> vNetworkComponents) {
		this.vNetworkComponents = vNetworkComponents;
	}

	public Vector<NetworkComponent> getvSensor() {
		return vSensor;
	}
	public void setvSensor(Vector<NetworkComponent> vSensor) {
		this.vSensor = vSensor;
	}

	public int getnNumNodes() {
		return nNumNodes;
	}
	public void setnNumNodes(int nNumNodes) {
		this.nNumNodes = nNumNodes;
	}

	public int getnNumBranches() {
		return nNumBranches;
	}
	public void setnNumBranches(int nNumBranches) {
		this.nNumBranches = nNumBranches;
	}

	public int getnSlackNode() {
		return nSlackNode;
	}
	public void setnSlackNode(int nSlackNode) {
		this.nSlackNode = nSlackNode;
	}

	public int getnGlobalSlackNode() {
		return nGlobalSlackNode;
	}
	public void setnGlobalSlackNode(int nGlobalSlackNode) {
		this.nGlobalSlackNode = nGlobalSlackNode;
	}

	public int getnNumBreakers() {
		return nNumBreakers;
	}
	public void setnNumBreakers(int nNumBreakers) {
		this.nNumBreakers = nNumBreakers;
	}

	public int getnNumSensors() {
		return nNumSensors;
	}
	public void setnNumSensors(int nNumSensors) {
		this.nNumSensors = nNumSensors;
	}

	public int getnBestCaseNode() {
		return nBestCaseNode;
	}
	public void setnBestCaseNode(int nBestCaseNode) {
		this.nBestCaseNode = nBestCaseNode;
	}

	public int getnWorstCaseNode() {
		return nWorstCaseNode;
	}
	public void setnWorstCaseNode(int nWorstCaseNode) {
		this.nWorstCaseNode = nWorstCaseNode;
	}

	public boolean isNewMethodPossible() {
		return isNewMethodPossible;
	}
	public void setNewMethodPossible(boolean isNewMethodPossible) {
		this.isNewMethodPossible = isNewMethodPossible;
	}

	public int getnNumLoadNodes() {
		return nNumLoadNodes;
	}
	public void setnNumLoadNodes(int nNumLoadNodes) {
		this.nNumLoadNodes = nNumLoadNodes;
	}

	public String getFirstSensor() {
		return firstSensor;
	}
	public void setFirstSensor(String firstSensor) {
		this.firstSensor = firstSensor;
	}

	public String getSecondSensor() {
		return secondSensor;
	}
	public void setSecondSensor(String secondSensor) {
		this.secondSensor = secondSensor;
	}

	public int getnIndexFirstSensor() {
		return nIndexFirstSensor;
	}
	public void setnIndexFirstSensor(int nIndexFirstSensor) {
		this.nIndexFirstSensor = nIndexFirstSensor;
	}

	public int getnIndexSecondSensor() {
		return nIndexSecondSensor;
	}
	public void setnIndexSecondSensor(int nIndexSecondSensor) {
		this.nIndexSecondSensor = nIndexSecondSensor;
	}

	public Vector<String> getvSensorNames() {
		return vSensorNames;
	}
	public void setvSensorNames(Vector<String> vSensorNames) {
		this.vSensorNames = vSensorNames;
	}

	public String getsRefPVName() {
		return sRefPVName;
	}
	public void setsRefPVName(String sRefPVName) {
		this.sRefPVName = sRefPVName;
	}

	public Vector<NodeAssignment> getvNodeAssignment() {
		return vNodeAssignment;
	}
	public void setvNodeAssignment(Vector<NodeAssignment> vNodeAssignment) {
		this.vNodeAssignment = vNodeAssignment;
	}

	public Vector<BranchParams> getvBranchParams() {
		return vBranchParams;
	}
	public void setvBranchParams(Vector<BranchParams> vBranchParams) {
		this.vBranchParams = vBranchParams;
	}

	public Vector<NodeAssociationParams> getvNodeAssociation() {
		return vNodeAssociation;
	}
	public void setvNodeAssociation(Vector<NodeAssociationParams> vNodeAssociation) {
		this.vNodeAssociation = vNodeAssociation;
	}

	public Vector<BranchMeasurement> getvBranchMeasurementParams() {
		return vBranchMeasurementParams;
	}
	public void setvBranchMeasurementParams(Vector<BranchMeasurement> vBranchMeasurementParams) {
		this.vBranchMeasurementParams = vBranchMeasurementParams;
	}

	public Vector<PVNodeParameters> getvPVNodeParameters() {
		return vPVNodeParameters;
	}
	public void setvPVNodeParameters(Vector<PVNodeParameters> vPVNodeParameters) {
		this.vPVNodeParameters = vPVNodeParameters;
	}

	public Vector<NetworkComponent> getvActuatorParams() {
		return vActuatorParams;
	}
	public void setvActuatorParams(Vector<NetworkComponent> vActuatorParams) {
		this.vActuatorParams = vActuatorParams;
	}

	public Vector<String> getvActuatorNames() {
		return vActuatorNames;
	}
	public void setvActuatorNames(Vector<String> vActuatorNames) {
		this.vActuatorNames = vActuatorNames;
	}

	public Vector<BranchParams> getvBranchParamsExternal() {
		return vBranchParamsExternal;
	}
	public void setvBranchParamsExternal(Vector<BranchParams> vBranchParamsExternal) {
		this.vBranchParamsExternal = vBranchParamsExternal;
	}

	public Vector<NodeAssociationParams> getvNodeAssociationExternal() {
		return vNodeAssociationExternal;
	}
	public void setvNodeAssociationExternal(Vector<NodeAssociationParams> vNodeAssociationExternal) {
		this.vNodeAssociationExternal = vNodeAssociationExternal;
	}

	public Vector<BranchMeasurement> getvBranchMeasurementParamsExternal() {
		return vBranchMeasurementParamsExternal;
	}
	public void setvBranchMeasurementParamsExternal(Vector<BranchMeasurement> vBranchMeasurementParamsExternal) {
		this.vBranchMeasurementParamsExternal = vBranchMeasurementParamsExternal;
	}

	public double[][] getdSensitivityReal() {
		return dSensitivityReal;
	}
	public void setdSensitivityReal(double[][] dSensitivityReal) {
		this.dSensitivityReal = dSensitivityReal;
	}

	public double[][] getdSensitivityImag() {
		return dSensitivityImag;
	}
	public void setdSensitivityImag(double[][] dSensitivityImag) {
		this.dSensitivityImag = dSensitivityImag;
	}

	public PowerFlowParameter getPowerFlowParameter() {
		return powerFlowParameter;
	}
	public void setPowerFlowParameter(PowerFlowParameter powerFlowParameter) {
		this.powerFlowParameter = powerFlowParameter;
	}

	public Complex getComplexImpedance() {
		return complexImpedance;
	}


	public void setComplexImpedance(Complex complexImpedance) {
		this.complexImpedance = complexImpedance;
	}
	public NetworkComponent getNetworkComponent() {
		return networkComponent;
	}
	public void setNetworkComponent(NetworkComponent networkComponent) {
		this.networkComponent = networkComponent;
	}

	public HashMap<Integer, String> getNodeNumberToNetworkComponentId() {
		return nodeNumberToNetworkComponentId;
	}

	public void setNodeNumberToNetworkComponentId(HashMap<Integer, String> nodeNumberToNetworkComponentId) {
		this.nodeNumberToNetworkComponentId = nodeNumberToNetworkComponentId;
	}
	
	public HashMap<String, Integer> getNetworkComponentIdToNodeNumber() {
		return networkComponentIdToNodeNumber;
	}

	public String getSensorIdOfSlackNode() {
		
		if(sensorIdOfSlackNode==null) {
			
			//--- Get SlackNode 
			String nSlackNodeId = this.getNetworkModelToCsvMapper().getSlackNodeVector().get(0).getNetworkComponentID();
			NetworkComponent netComp = this.getNetworkModel().getNetworkComponent(nSlackNodeId);
			
			Vector<NetworkComponent> neighbouredComponents = this.getNetworkModel().getNeighbourNetworkComponents(netComp);
			
			// ---Filtering, only nodes
			for(int d=0;d<neighbouredComponents.size();d++) {
				if(neighbouredComponents.get(d).getType().equals("Sensor")==true) {
					sensorIdOfSlackNode=neighbouredComponents.get(d).getId();
					break;
				}
			}
		}
		
		return sensorIdOfSlackNode;
	}

	public void setSensorIdOfSlackNode(String sensorIdOfSlackNode) {
		this.sensorIdOfSlackNode = sensorIdOfSlackNode;
	}

	public void setNetworkComponentIdToNodeNumber(HashMap<String, Integer> networkComponentIdToNodeNumber) {
		this.networkComponentIdToNodeNumber = networkComponentIdToNodeNumber;
	}
	
	// private void internalNodeAssignmentTemporary(double[][] nodeAssignment) {
	//
	// HashMap<Integer, Integer> nodeAssignmentLocal2Global = new HashMap<>();
	// HashMap<Integer, Integer> nodeAssignmentGlobal2Local = new HashMap<>();
	// for (int i = 0; i < nodeAssignment.length; i++) {
	// nodeAssignmentLocal2Global.put((int) nodeAssignment[i][0], (int)
	// nodeAssignment[i][1]);
	// nodeAssignmentGlobal2Local.put((int) nodeAssignment[i][1], (int)
	// nodeAssignment[i][0]);
	// NodeAssignment temp = new NodeAssignment();
	// // internal Node Assignment
	// temp.setnLocalNodeNumber(i + 1);
	// temp.setnGlobalNodeNumber(internalDataModel.getvNodeAssociationExternal().get(i).getnNode());
	// // this.vNodeAssignment.add(temp);
	// }
	// internalDataModel.setNodeAssignmentLocal2Global(nodeAssignmentLocal2Global);
	// internalDataModel.setNodeAssignmentGlobal2Local(nodeAssignmentGlobal2Local);
	// // System.out.println();
	// }

}
