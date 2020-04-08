package de.enflexit.energyAgent.lib.powerFlowCalculation;

import java.util.HashMap;
import java.util.Vector;

import de.enflexit.energyAgent.lib.powerFlowCalculation.parameter.BranchParams;
import de.enflexit.energyAgent.lib.powerFlowCalculation.parameter.NodeAssociationParams;

/**
 ***************************************************************************
 ** @author  Marcel Ludwig - EVT - University of Wuppertal (BUW)
 * 02.03.2018
 *Description: PowerFlow Parameter for PowerFlowCalculation
 ***************************************************************************
 * 
 */
public class PowerFlowParameter {
	
	private Vector<Double> nodalVoltageReal = new Vector<Double>();
	private Vector<Double> nodalVoltageImag = new Vector<Double>();
	private Vector<Double> nodalPowerReal = new Vector<Double>();
	private Vector<Double> nodalPowerImag = new Vector<Double>();
	private Vector<Vector<Double>> ykkReal = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> ykkImag = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> maxCurrent = new Vector<Vector<Double>>();
	private double[][] dMatNodeSetup;
	private double[][] dMatGridData;
	
	private Vector<NodeAssociationParams> matNodeSetup;
	private Vector<BranchParams> matGridData;
	
	private double[][] dX;
	private int nNumNodes;
	private int nNumBranches;
	private double dSlackVoltage;
	private int nSlackNode;
	
	private Vector<PVNodeParameters> vPVNodes;
	private HashMap<String,MeasuredBranchCurrent> estimatedBranchCurrents = new HashMap<>();
	
	
	/**
	 * Instantiates a new power flow parameter.
	 *
	 * @param matNodeSetup the mat node setup
	 * @param matGridData the mat grid data
	 * @param nSlackNode the n slack node
	 */
	public PowerFlowParameter(double[][] matNodeSetup, double[][] matGridData, int nSlackNode, double initialSlackNodeVoltage) {
		this.nSlackNode = nSlackNode;
		this.dMatNodeSetup=matNodeSetup;
		this.dMatGridData=matGridData;
		this.nNumNodes= matNodeSetup.length;
		this.setdSlackVoltage(initialSlackNodeVoltage);
		this.convertDoubleArrays2Vector(matNodeSetup, matGridData);
		this.initializeVectors();
	}

	/**
	 * Instantiates a new power flow parameter.
	 * @param matNodeSetup the mat node setup
	 * @param matGridData the mat grid data
	 * @param nSlackNode the n slack node
	 */
	public PowerFlowParameter(Vector<NodeAssociationParams> matNodeSetup, Vector<BranchParams> matGridData, int nSlackNode, double initialSlackNodeVoltage) {
		this.nSlackNode=nSlackNode;
		this.matNodeSetup=matNodeSetup;
		this.matGridData=matGridData;
		this.nNumNodes=matNodeSetup.size();
		this.setdSlackVoltage(initialSlackNodeVoltage);
		this.initializeVectors();
	}
	
	/**
	 * Initialize vectors.
	 */
	private void initializeVectors() {

		this.nNumNodes = this.matNodeSetup.size();
		this.nNumBranches = this.matGridData.size();
		// --- Create Nodal Admittance Matrix
		this.buildNodalAdmittanceMatrix();
		this.setNodalVoltageVector(this.dSlackVoltage);
		this.calculatex();
		this.setMaxCurrentOnBasisofDataType();

	}
	
	/**
	 * Convert double arrays 2 vector.
	 * @param matNodeSetup the mat node setup
	 * @param matGridData the mat grid data
	 */
	private void convertDoubleArrays2Vector(double[][]matNodeSetup, double[][]matGridData) {
		Vector<NodeAssociationParams> vNodeAssociation = new Vector<NodeAssociationParams>();
		int nNumLoadNodes = 0;
		// --- Building vNodeAssociation
		for (int i = 0; i < matNodeSetup.length; i++) {

			NodeAssociationParams temp = new NodeAssociationParams();
			temp.setnNode((int) matNodeSetup[i][0]);
			if (matNodeSetup[i][1] == 0) {
				temp.setLoadNode(false);

			} else {
				temp.setLoadNode(true);
				nNumLoadNodes = nNumLoadNodes + 1;
			}
			temp.setPower(matNodeSetup[i][2] );
			vNodeAssociation.add(temp);
		}
		this.matNodeSetup=vNodeAssociation;

		// --- Building vBranchParams
		// If only one Node exists, no Branches are available
		if (matNodeSetup.length> 1) {
			
			Vector<BranchParams> vBranchParams = new Vector<BranchParams>();
			// --- Seperate MV and LV grid data
			
			// --- Grid Data translation ----
			for (int i = 0; i < matGridData.length; i++) {
				BranchParams temp = new BranchParams();
				temp.setnFromNode((int) matGridData[i][0]);
				temp.setnToNode((int) matGridData[i][1]);
				temp.setdLength(matGridData[i][2]);
				temp.setdR(matGridData[i][3]);
				temp.setdX(matGridData[i][4]);
				temp.setdG(matGridData[i][5]);
				temp.setdC(matGridData[i][6]);
				temp.setdMaxCurrent(matGridData[i][7]);
				vBranchParams.add(temp);
			}
					
			this.matGridData= vBranchParams;
		}

	}

	/**
	 * Sets the nodal voltage vector.
	 * @param uSlack the new nodal voltage vector
	 */
	private void setNodalVoltageVector(double uSlack) {
		nodalVoltageReal = new Vector<Double>();
		nodalVoltageImag = new Vector<Double>();
		for (int a = 0; a < nNumNodes; a++) {
			nodalVoltageReal.add(uSlack);
			nodalVoltageImag.add(0.0);
		}
		this.calculatex();
	}

//	/**
//	 * Sets the max current.
//	 */
//	private void setMaxCurrent() {
//		double[][] dMaxCurrent = new double[nNumNodes][nNumNodes];
//		for (int indexOfBranch = 0; indexOfBranch < nNumBranches; indexOfBranch++) {
//			dMaxCurrent[(int) dMatGridData[indexOfBranch][0] - 1][(int) dMatGridData[indexOfBranch][1] - 1] = dMatGridData[indexOfBranch][5];
//			dMaxCurrent[(int) dMatGridData[indexOfBranch][1] - 1][(int) dMatGridData[indexOfBranch][0] - 1] = dMatGridData[indexOfBranch][5];
//		}
//		for(int a=0;a<nNumNodes;a++){
//			Vector<Double> tempCurrent= new Vector<Double>();
//			for(int b=0;b<nNumNodes;b++){
//				tempCurrent.add(dMaxCurrent[a][b]);
//			}
//			maxCurrent.add(tempCurrent);
//		}
//	}

	/**
	 * Sets the max current 1.
	 */
	private void setMaxCurrentOnBasisofDataType() {
		double[][] dMaxCurrent = new double[nNumNodes][nNumNodes];
		for (int indexOfBranch = 0; indexOfBranch < nNumBranches; indexOfBranch++) {
			int nFromNode= matGridData.get(indexOfBranch).getnFromNode();
			int nToNode= matGridData.get(indexOfBranch).getnToNode();
			double maxCurrent= matGridData.get(indexOfBranch).getdMaxCurrent();
			dMaxCurrent[nFromNode - 1][nToNode - 1] = maxCurrent;
			dMaxCurrent[nToNode - 1][nFromNode - 1] = maxCurrent;
		}
		for(int a=0;a<nNumNodes;a++){
			Vector<Double> tempCurrent= new Vector<Double>();
			for(int b=0;b<nNumNodes;b++){
				tempCurrent.add(dMaxCurrent[a][b]);
			}
			maxCurrent.add(tempCurrent);
		}
	}
	
	/**
	 * Calculate x.
	 */
	private void calculatex() {

		double[][] X = new double[2 * nNumNodes][1];
		int a;
		for (a = 0; a < nNumNodes; a++) {
			if (nodalVoltageReal.get(a) == 0) {
				X[a][0] = Math.PI;
			} else if (nodalVoltageImag.get(a) == 0) {
				X[a][0] = 0;
			} else {
				X[a][0] = Math.tan(nodalVoltageImag.get(a) / nodalVoltageReal.get(a));
			}
		}
		for (a = nNumNodes; a < 2 * nNumNodes; a++) {
			X[a][0] = Math.sqrt(nodalVoltageReal.get(a - nNumNodes) * nodalVoltageReal.get(a - nNumNodes) + nodalVoltageImag.get(a - nNumNodes) * nodalVoltageImag.get(a - nNumNodes));
		}
		this.dX=X;
	}
	
	/**
	 * Builds the nodal admittance matrix.
	 */
	private void buildNodalAdmittanceMatrix() {
		
		double[][] matYKKreal = new double[nNumNodes][nNumNodes];
		double[][] matYKKimag = new double[nNumNodes][nNumNodes];
		
		for (int i = 0; i < nNumBranches; i++) {
			double dR = matGridData.get(i).getdR();
			double dX = matGridData.get(i).getdX();
			double dG = 0.5*matGridData.get(i).getdG();
			double dB = 50 * Math.PI * matGridData.get(i).getdC()*1E-9;
			double dL = matGridData.get(i).getdLength()/ 1000;
			
			int nFromNode = (int)matGridData.get(i).getnFromNode();
			int nToNode = (int)matGridData.get(i).getnToNode();
			
			matYKKreal[nFromNode - 1][nToNode - 1] = -dR* (1 / (dL  * (dR * dR + dX * dX)));
			matYKKreal[nToNode - 1][nFromNode - 1] = -dR* (1 / (dL  * (dR * dR + dX * dX)));

			matYKKimag[nFromNode - 1][nToNode - 1] = dX * (1 / (dL  * (dR * dR + dX * dX)));
			matYKKimag[nToNode - 1][nFromNode - 1] = dX * (1 / (dL  * (dR * dR + dX * dX)));
			
			//Nulladmitanzen
			matYKKreal[nFromNode - 1][nFromNode - 1] = matYKKreal[nFromNode - 1][nFromNode - 1] - dL*dG;
			matYKKreal[nToNode - 1][nToNode - 1] = matYKKreal[nToNode - 1][nToNode - 1] - dL*dG;
			matYKKimag[nFromNode - 1][nFromNode - 1] = matYKKimag[nFromNode - 1][nFromNode - 1] - dL*dB;
			matYKKimag[nToNode - 1][nToNode - 1] = matYKKimag[nToNode - 1][nToNode - 1] - dL*dB;
		}
		
		for (int i = 0; i < nNumNodes; i++) {
			for (int j = 0; j < nNumNodes; j++) {
				if (i != j) {
					matYKKreal[i][i] = matYKKreal[i][i] + matYKKreal[i][j];
					matYKKimag[i][i] = matYKKimag[i][i] + matYKKimag[i][j];
				}
			}
			matYKKreal[i][i] = -1 * matYKKreal[i][i];
			matYKKimag[i][i] = -1 * matYKKimag[i][i];
		}

		for (int a = 0; a < nNumNodes; a++) {
			Vector<Double> tempReal = new Vector<Double>();
			Vector<Double> tempImag = new Vector<Double>();

			for (int b = 0; b < nNumNodes; b++) {
				tempReal.add(matYKKreal[a][b]);
				tempImag.add(matYKKimag[a][b]);
			}

			this.ykkReal.add(tempReal);
			this.ykkImag.add(tempImag);
		}
	}
	
	/**
	 * Insert PV node voltages.
	 */
	private void insertPVNodeVoltages() {
		for (int i = 0; i < this.vPVNodes.size(); i++) {
			this.nodalVoltageReal.set(this.vPVNodes.get(i).getnPVNode() - 1, this.vPVNodes.get(i).getdVoltageOfPVNode());
		}
		this.calculatex();
	}

	/**
	 * Sets the PV nodes.
	 * @param vPVNodes the new v PV nodes
	 * @param vPVNodes
	 */
	public void setvPVNodes(Vector<PVNodeParameters> vPVNodes) {

		// Sort PV-Nodes-Array
		PVNodeParameters temp = new PVNodeParameters(0, 0,null);

		for (int a = 0; a < vPVNodes.size(); a++) {
			for (int b = a + 1; b < vPVNodes.size(); b++) {
				if (vPVNodes.get(a).getnPVNode() > vPVNodes.get(b).getnPVNode()) {

					temp.setnPVNode(vPVNodes.get(a).getnPVNode());
					temp.setdVoltageOfPVNode(vPVNodes.get(a).getdVoltageOfPVNode());
					temp.setNetworkComponent(vPVNodes.get(a).getNetworkComponentName());

					vPVNodes.get(a).setnPVNode(vPVNodes.get(b).getnPVNode());
					vPVNodes.get(a).setdVoltageOfPVNode(vPVNodes.get(b).getdVoltageOfPVNode());
					vPVNodes.get(a).setNetworkComponent(vPVNodes.get(b).getNetworkComponentName());
					
					int node = temp.getnPVNode();
					double voltage = temp.getdVoltageOfPVNode();
					String networkComponent = temp.getNetworkComponentName();
					vPVNodes.get(b).setnPVNode(node);
					vPVNodes.get(b).setdVoltageOfPVNode(voltage);
					vPVNodes.get(b).setNetworkComponent(networkComponent);

				}
			}
		}
		this.vPVNodes = vPVNodes;
		// Set Node Voltage for calculation
		this.insertPVNodeVoltages();

	}

	/**
	 * Gets the d slack voltage.
	 *
	 * @return the d slack voltage
	 */
	public double getdSlackVoltage() {
		return dSlackVoltage;
	}

	/**
	 * Sets the d slack voltage.
	 *
	 * @param dSlackVoltage the new d slack voltage
	 */
	public void setdSlackVoltage(double dSlackVoltage) {
		this.dSlackVoltage = dSlackVoltage;
		this.setNodalVoltageVector(dSlackVoltage);
		this.calculatex();
	}

	/**
	 * Gets the nodal voltage real.
	 *
	 * @return the nodal voltage real
	 */
	public Vector<Double> getNodalVoltageReal() {
		return nodalVoltageReal;
	}

	/**
	 * Gets the nodal voltage imag.
	 *
	 * @return the nodal voltage imag
	 */
	public Vector<Double> getNodalVoltageImag() {
		return nodalVoltageImag;
	}

	
	/**
	 * Gets the nodal power real.
	 * @return the nodal power real
	 */
	public Vector<Double> getNodalPowerReal() {
		return nodalPowerReal;
	}
	/**
	 * Sets the nodal power real.
	 * @param nodalPowerReal the new nodal power real
	 */
	public void setNodalPowerReal(Vector<Double> nodalPowerReal) {
		this.nodalPowerReal = nodalPowerReal;
	}
		
	/**
	 * Gets the nodal power imag.
	 *
	 * @return the nodal power imag
	 */
	public Vector<Double> getNodalPowerImag() {
		return nodalPowerImag;
	}

	/**
	 * Gets the ykk real.
	 *
	 * @return the ykk real
	 */
	public Vector<Vector<Double>> getYkkReal() {
		return ykkReal;
	}

	/**
	 * Gets the ykk imag.
	 *
	 * @return the ykk imag
	 */
	public Vector<Vector<Double>> getYkkImag() {
		return ykkImag;
	}

	/**
	 * Gets the n num nodes.
	 *
	 * @return the n num nodes
	 */
	public int getnNumNodes() {
		return nNumNodes;
	}


	/**
	 * Gets the n num branches.
	 *
	 * @return the n num branches
	 */
	public int getnNumBranches() {
		return nNumBranches;
	}


	public int getnSlackNode() {
		return nSlackNode;
	}

	/**
	 * Gets the max current.
	 *
	 * @return the max current
	 */
	public Vector<Vector<Double>> getMaxCurrent() {
		return maxCurrent;
	}

	/**
	 * Gets the d X.
	 *
	 * @return the d X
	 */
	public double[][] getdX() {
		return dX;
	}

	/**
	 * Gets the v PV nodes.
	 *
	 * @return the v PV nodes
	 */
	public Vector<PVNodeParameters> getvPVNodes() {
		return vPVNodes;
	}

	/**
	 * Gets the d mat node setup.
	 *
	 * @return the d mat node setup
	 */
	public double[][] getdMatNodeSetup() {
		return dMatNodeSetup;
	}

	/**
	 * Gets the d mat grid data.
	 *
	 * @return the d mat grid data
	 */
	public double[][] getdMatGridData() {
		return dMatGridData;
	}


	

	/**
	 * Sets the nodal power imag.
	 *
	 * @param nodalPowerImag the new nodal power imag
	 */
	public void setNodalPowerImag(Vector<Double> nodalPowerImag) {
		this.nodalPowerImag = nodalPowerImag;
	}

	/**
	 * Sets the d X.
	 *
	 * @param dX the new d X
	 */

//	public void setdX(double[][] dX) {
//		this.dX = dX;
//	}

	public void setnSlackNode(int nSlackNode) {
		this.nSlackNode = nSlackNode;
	}

	public void setNodalVoltageReal(Vector<Double> nodalVoltageReal) {
		this.nodalVoltageReal = nodalVoltageReal;
	}

	public void setNodalVoltageImag(Vector<Double> nodalVoltageImag) {
		this.nodalVoltageImag = nodalVoltageImag;
	}

	public HashMap<String,MeasuredBranchCurrent> getEstimatedBranchCurrents() {
		return estimatedBranchCurrents;
	}

	public void setEstimatedBranchCurrents(HashMap<String,MeasuredBranchCurrent> estimatedBranchCurrents) {
		this.estimatedBranchCurrents = estimatedBranchCurrents;
	}




}
