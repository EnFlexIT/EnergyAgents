package de.enflexit.ea.lib.powerFlowCalculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * The Class AbstractPowerFlowCalculation.
 */
public abstract class AbstractPowerFlowCalculation {
	
	private PowerFlowParameter powerFlowParameter;
	private HashMap<Integer, ActiveReactivePowerPair> nodePowerPairs;
	
	private Vector<Integer> vNodesToDelete= new Vector<Integer>();
	private Vector<Double> nodalVoltageReal= new Vector<Double>();
	private Vector<Double> nodalVoltageImag= new Vector<Double>();
	private Vector<Double> nodalVoltageAbs= new Vector<Double>();
	private Vector<Double> nodalCurrentReal= new Vector<Double>();
	private Vector<Double> nodalCurrentImag= new Vector<Double>();
	private Vector<Double> nodalCurrentAbs= new Vector<Double>();
	private Vector<Double> nodalPowerReal= new Vector<Double>();
	private Vector<Double> nodalPowerImag= new Vector<Double>();
	private Vector<Double> nodalCosPhi= new Vector<Double>();
	
	private Vector<Integer> branchNumbersVector;
	private Vector<Integer> branchFromNodes;
	private Vector<Integer> branchToNodes;
	private Vector<Vector<Double>> branchPowerReal = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> branchPowerImag = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> branchPowerAbs = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> branchCurrentReal = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> branchCurrentImag = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> branchCurrentAbs = new Vector<Vector<Double>>();
	private Vector<Double> branchUtilization = new Vector<Double>();
	private Vector<Double> branchCosPhi = new Vector<Double>();
	
	private Vector<Double> powerOfTransformer = new Vector<>();// Power Upper Voltage -> Lower Voltage [P Q]
	private int nIterationCounter=0; // Number of Cycles
	private int nIterationCounterLimit=5;// Limit for PFC
	private double dIterationLimit=0; 
	private boolean bIterationProcess=false; // State of PFC false=ready
	private boolean sucessfullPFC=false; 
	
	public HashMap<String, MeasuredBranchCurrent> getEstimatedBranchCurrents() {
		return this.getPowerFlowParameter().getEstimatedBranchCurrents();
	}
	public void setEstimatedBranchCurrents(HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents) {
		this.getPowerFlowParameter().setEstimatedBranchCurrents(estimatedBranchCurrents);
	}
	public PowerFlowParameter getPowerFlowParameter() {
		return powerFlowParameter;
	}
	public void setPowerFlowParameter(PowerFlowParameter powerFlowParameter) {
		this.powerFlowParameter = powerFlowParameter;
	}
	
	/**
	 * Calculates the grid state
	 */
	public abstract boolean calculate();
	
	/**
	 * Set Nodal Power by powerFlowParameter
	 */
	public void setNodalPower(){
		Vector<Double> nodalPowerReal= new Vector<Double>();
		Vector<Double> nodalPowerImag= new Vector<Double>();
			
			// --- Set new power values to power flow parameters --------
			double[][] matNodeSetup = this.powerFlowParameter.getdMatNodeSetup();
			for (int i = 0; i < matNodeSetup.length; i++) {

				Double nodeNo = matNodeSetup[i][0];
				ActiveReactivePowerPair powerPair = nodePowerPairs.get(nodeNo.intValue());
				double activePower = 0.0;
				double reactivePower = 0.0;
				if (powerPair != null) {
					activePower = powerPair.getActivePowerInWatt();
					reactivePower = powerPair.getReactivePowerInWatt();
				}
				matNodeSetup[i][1] = activePower;
				matNodeSetup[i][2] = reactivePower;
				nodalPowerReal.add(activePower);
				nodalPowerImag.add(reactivePower);
			}

			this.setNodalPower(nodalPowerReal, nodalPowerImag);

	}
	/**
	 * Set Nodal Power by Vector<Double>
	 * @param nodalPowerReal
	 * @param nodalPowerImag
	 */
	public void setNodalPower(Vector<Double> nodalPowerReal,Vector<Double> nodalPowerImag ) {
		this.setNodalPowerReal(nodalPowerReal);
		this.setNodalPowerImag(nodalPowerImag);
	}
	
	/**
	 * Set Nodal Power by Hasmap
	 * @param nodePowerPairs
	 */
	public void setNodalPower(HashMap<Integer, ActiveReactivePowerPair> nodePowerPairs) {
		List<Integer> keySet = new ArrayList<Integer>(nodePowerPairs.keySet());
		nodalPowerReal = new Vector<>();
		nodalPowerImag = new Vector<>();
		for (int i = 0; i < keySet.size(); i++) {
			
			int nodeNo = keySet.get(i);
			ActiveReactivePowerPair powerPair = nodePowerPairs.get(nodeNo);
			double activePower = 0.0;
			double reactivePower = 0.0;
			if (powerPair != null) {
				activePower = powerPair.getActivePowerInWatt();
				reactivePower = powerPair.getReactivePowerInWatt();
			}
			nodalPowerReal.add(activePower);
			nodalPowerImag.add(reactivePower);
		}
	}
	
	/**
	 * Prints the specified double array.
	 * 
	 * @param array2Print
	 *            the array2 print
	 */
	protected void printArray(double[][] array2Print) {
		for (int a = 0; a < array2Print.length; a++) {
			double lineElements[] = array2Print[a];
			for (int b = 0; b < lineElements.length; b++) {
				System.out.print(lineElements[b] + "\t");
			}
			System.out.print("\n");
		}
	}
	
	
	public Vector<Double> getNodalCosPhi() {
		return nodalCosPhi;
	}
	public void setNodalCosPhi(Vector<Double> nodalCosPhi) {
		this.nodalCosPhi = nodalCosPhi;
	}
	public HashMap<Integer, ActiveReactivePowerPair> getNodePowerPairs() {
		return nodePowerPairs;
	}
	public void setNodePowerPairs(HashMap<Integer, ActiveReactivePowerPair> nodePowerPairs) {
		this.nodePowerPairs = nodePowerPairs;
	}
	public Vector<Double> getNodalVoltageReal() {
		return this.nodalVoltageReal;
	}
	public void setNodalVoltageReal(Vector<Double> nodalVoltageReal) {
		this.nodalVoltageReal = nodalVoltageReal;
	}
	public Vector<Double> getNodalVoltageImag() {
		return nodalVoltageImag;
	}
	public void setNodalVoltageImag(Vector<Double> nodalVoltageImag) {
		this.nodalVoltageImag = nodalVoltageImag;
	}
	public Vector<Double> getNodalVoltageAbs() {
		return nodalVoltageAbs;
	}
	public void setNodalVoltageAbs(Vector<Double> nodalVoltageAbs) {
		this.nodalVoltageAbs = nodalVoltageAbs;
	}
	public Vector<Double> getNodalCurrentReal() {
		return nodalCurrentReal;
	}
	public void setNodalCurrentReal(Vector<Double> nodalCurrentReal) {
		this.nodalCurrentReal = nodalCurrentReal;
	}
	public Vector<Double> getNodalCurrentImag() {
		return nodalCurrentImag;
	}
	public void setNodalCurrentImag(Vector<Double> nodalCurrentImag) {
		this.nodalCurrentImag = nodalCurrentImag;
	}
	public Vector<Double> getNodalCurrentAbs() {
		return nodalCurrentAbs;
	}
	public void setNodalCurrentAbs(Vector<Double> nodalCurrentAbs) {
		this.nodalCurrentAbs = nodalCurrentAbs;
	}
	public Vector<Double> getNodalPowerReal() {
		return nodalPowerReal;
	}
	public void setNodalPowerReal(Vector<Double> nodalPowerReal) {
		this.nodalPowerReal = nodalPowerReal;
	}
	public Vector<Double> getNodalPowerImag() {
		return nodalPowerImag;
	}
	public void setNodalPowerImag(Vector<Double> nodalPowerImag) {
		this.nodalPowerImag = nodalPowerImag;
	}
	public Vector<Integer> getBranchNumbersVector() {
		if (branchNumbersVector == null) {
			double[][] matgriddata = this.getPowerFlowParameter().getdMatGridData();
			int branchNumberIndex = matgriddata[0].length-1;
			
			//New branchNumberVector
			branchNumbersVector = new Vector<Integer>();
			for (int a = 0; a < this.getnNumBranches(); a++) {
				branchNumbersVector.add((int) matgriddata[a][branchNumberIndex]);
			}
		}
		return branchNumbersVector;
	}
	public Vector<Integer> getBranchFromNodes() {
		if (branchFromNodes == null) {
			double[][] matgriddata = this.getPowerFlowParameter().getdMatGridData();
			
			//New branchFromNodes Vector
			branchFromNodes = new Vector<Integer>();
			for (int a = 0; a < this.getnNumBranches(); a++) {
				branchFromNodes.add((int) matgriddata[a][0]);
			}
		}
		return branchFromNodes;
	}
	public Vector<Integer> getBranchToNodes() {
		if (branchToNodes == null) {
			double[][] matgriddata = this.getPowerFlowParameter().getdMatGridData();
			
			//New branchToNodesHashMap
			branchToNodes = new Vector<Integer>();
			for (int a = 0; a < this.getnNumBranches(); a++) {
				branchToNodes.add((int) matgriddata[a][1]);
			}
		}
		return branchToNodes;
	}
	public Vector<Vector<Double>> getBranchPowerReal() {
		return branchPowerReal;
	}
	public void setBranchPowerReal(Vector<Vector<Double>> branchPowerReal) {
		this.branchPowerReal = branchPowerReal;
	}
	public Vector<Vector<Double>> getBranchPowerImag() {
		return branchPowerImag;
	}
	public void setBranchPowerImag(Vector<Vector<Double>> branchPowerImag) {
		this.branchPowerImag = branchPowerImag;
	}
	public Vector<Vector<Double>> getBranchPowerAbs() {
		return branchPowerAbs;
	}
	public void setBranchPowerAbs(Vector<Vector<Double>> branchPowerAbs) {
		this.branchPowerAbs = branchPowerAbs;
	}
	public Vector<Vector<Double>> getBranchCurrentReal() {
		return branchCurrentReal;
	}
	public void setBranchCurrentReal(Vector<Vector<Double>> branchCurrentReal) {
		this.branchCurrentReal = branchCurrentReal;
	}
	public Vector<Vector<Double>> getBranchCurrentImag() {
		return branchCurrentImag;
	}
	public void setBranchCurrentImag(Vector<Vector<Double>> branchCurrentImag) {
		this.branchCurrentImag = branchCurrentImag;
	}
	public Vector<Vector<Double>> getBranchCurrentAbs() {
		return branchCurrentAbs;
	}
	public void setBranchCurrentAbs(Vector<Vector<Double>> branchCurrentAbs) {
		this.branchCurrentAbs = branchCurrentAbs;
	}
	public Vector<Double> getBranchUtilization() {
		return branchUtilization;
	}
	public void setBranchUtilization(Vector<Double> branchUtilization) {
		this.branchUtilization = branchUtilization;
	}
	public Vector<Double> getBranchCosPhi() {
		return branchCosPhi;
	}
	public void setBranchCosPhi(Vector<Double> branchCosPhi) {
		this.branchCosPhi = branchCosPhi;
	}
	public Vector<PVNodeParameters> getvPVNodes() {
		return this.getPowerFlowParameter().getvPVNodes();
	}
	public void setvPVNodes(Vector<PVNodeParameters> vPVNodes) {
		this.getPowerFlowParameter().setvPVNodes(vPVNodes);
	}
	public Vector<Integer> getvNodesToDelete() {
		return vNodesToDelete;
	}
	public void setvNodesToDelete(Vector<Integer> vNodesToDelete) {
		this.vNodesToDelete = vNodesToDelete;
	}

	public Vector<Double> getPowerOfTransformer() {
		return powerOfTransformer;
	}

	public void setPowerOfTransformer(Vector<Double> powerOfTransformer) {
		this.powerOfTransformer = powerOfTransformer;
	}

	public int getnIterationCounter() {
		return nIterationCounter;
	}

	public void setnIterationCounter(int nIterationCounter) {
		this.nIterationCounter = nIterationCounter;
	}

	public boolean isbIterationProcess() {
		return bIterationProcess;
	}

	public void setbIterationProcess(boolean bIterationProcess) {
		this.bIterationProcess = bIterationProcess;
	}

	public double getdIterationLimit() {
		return dIterationLimit;
	}

	public void setdIterationLimit(double dIterationLimit) {
		this.dIterationLimit = dIterationLimit;
	}

	public int getnIterationCounterLimit() {
		return nIterationCounterLimit;
	}

	public void setnIterationCounterLimit(int nIterationCounterLimit) {
		this.nIterationCounterLimit = nIterationCounterLimit;
	}
	
	protected double getdSlackNodeVoltage() {
		return this.getPowerFlowParameter().getdSlackVoltage();
	}
	
	protected int getnNumNodes() {
		return this.getPowerFlowParameter().getnNumNodes();
	}
	
	protected int getnNumBranches() {
		return this.getPowerFlowParameter().getnNumBranches();
	}
	
	protected int getnSlackNode() {
		return this.getPowerFlowParameter().getnSlackNode();
	}
	
	protected Vector<Vector<Double>> getYkkReal(){
		return this.getPowerFlowParameter().getYkkReal();
	}
	
	protected Vector<Vector<Double>> getYkkImag(){
		return this.getPowerFlowParameter().getYkkImag();
	}
	
	public boolean isSucessfullPFC() {
		return sucessfullPFC;
	}
	public void setSucessfullPFC(boolean sucessfullPFC) {
		this.sucessfullPFC = sucessfullPFC;
	}
	
	/**
	 * Checks if all preconditions for the calculation are fulfilled
	 * @return true if the calculation can be executed
	 */
	public abstract boolean checkPreconditionsForCalculation();
	

}
