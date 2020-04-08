package de.enflexit.energyAgent.lib.powerFlowEstimation.decentralEstimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.energyAgent.lib.powerFlowCalculation.MeasuredBranchCurrent;
import de.enflexit.energyAgent.lib.powerFlowCalculation.PVNodeParameters;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class AbstractGridStateEstimation.
 */
public abstract class AbstractGridStateEstimation {

	private Phase actualPhase= null;
	
	// --- Result of grid State estimation -----------------------------
	private HashMap<String,Double> nodalPowerReal= new HashMap<>();
	private HashMap<String,Double> nodalPowerImag= new HashMap<>();
	private HashMap<String,Double> vPVNodes = new HashMap<>();
	private HashMap<String,MeasuredBranchCurrent> estimatedBranchCurrents= new HashMap<>();
	
	private NetworkModel networkModel;
	private DistrictModel districtModel;
	private boolean central =true;

	
	/**
	 * Sets the network model.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	/**
	 * Gets the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	
	/**
	 * Gets the actual phase.
	 * @return the actual phase
	 */
	public Phase getActualPhase() {
		return actualPhase;
	}
	/**
	 * Sets the actual phase.
	 * @param actualPhase the new actual phase
	 */
	public void setActualPhase(Phase actualPhase) {
		this.actualPhase = actualPhase;
	}

	/**
	 * Gets the nodal power real.
	 *
	 * @return the nodal power real
	 */
	public HashMap<String,Double> getNodalPowerReal() {
		return nodalPowerReal;
	}
	
	/**
	 * Sets the nodal power real.
	 *
	 * @param nodalPowerReal the nodal power real
	 */
	public void setNodalPowerReal(HashMap<String,Double> nodalPowerReal) {
		this.nodalPowerReal = nodalPowerReal;
	}

	/**
	 * Gets the nodal power imag.
	 *
	 * @return the nodal power imag
	 */
	public HashMap<String,Double> getNodalPowerImag() {
		return nodalPowerImag;
	}
	
	/**
	 * Sets the nodal power imag.
	 *
	 * @param nodalPowerImag the nodal power imag
	 */
	public void setNodalPowerImag(HashMap<String,Double> nodalPowerImag) {
		this.nodalPowerImag = nodalPowerImag;
	}

	/**
	 * Gets the v PV nodes.
	 *
	 * @return the v PV nodes
	 */
	public HashMap<String,Double> getvPVNodes() {
		return vPVNodes;
	}
	
	/**
	 * Setv PV nodes.
	 *
	 * @param vPVNodes the v PV nodes
	 */
	public void setvPVNodes(HashMap<String,Double> vPVNodes) {
		this.vPVNodes = vPVNodes;
	}

	
	/**
	 * Pv nodes from hash map.
	 *
	 * @param pvNodes the pv nodes
	 * @return the vector
	 */
	public Vector<PVNodeParameters> pvNodesFromHashMap(HashMap<String, Double> pvNodes) {
		Vector<PVNodeParameters> pvNodeVector= new Vector<>();
		ArrayList<String> keySet = new ArrayList<>(pvNodes.keySet());
		
		for(int a=0; a<keySet.size();a++) {
			String networkComponent = keySet.get(a);
			 int nNode = this.getDistrictModel().getNetworkComponentIdToNodeNumber().get(networkComponent);
			 PVNodeParameters pvNode = new PVNodeParameters();
			 pvNode.setnPVNode(nNode);
			 pvNode.setdVoltageOfPVNode(pvNodes.get(networkComponent));
			 pvNode.setNetworkComponent(networkComponent);
			 
			 pvNodeVector.add(pvNode);
		}
		
		return pvNodeVector;
	}

	/**
	 * Returns the district model.
	 * @return the district model
	 */
	private  DistrictModel getDistrictModel() {
		if (districtModel==null) {
			districtModel = new DistrictModel();
			districtModel.initiation(this.getNetworkModel(),central);
		}
		return districtModel;
	}
	
	
	/**
	 * Do estimation per phase.
	 *
	 * @param phase the phase
	 * @param lastSensorStates the last sensor states
	 * @param relPowerOfRefPV the rel power of ref PV
	 * @param evaluationEndTime the evaluation end time
	 */
	public abstract void doEstimationPerPhase(Phase phase, HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates, double relPowerOfRefPV, long evaluationEndTime);

	/**
	 * Checks if is estimation successful.
	 * @return true, if is estimation successful
	 */
	public abstract boolean isEstimationSuccessful();
	
	public boolean isCentral() {
		return central;
	}
	public void setCentral(boolean central) {
		this.central = central;
	}
	public HashMap<String, MeasuredBranchCurrent> getEstimatedBranchCurrents() {
		return estimatedBranchCurrents;
	}
	public void setEstimatedBranchCurrents(HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents) {
		this.estimatedBranchCurrents = estimatedBranchCurrents;
	}


}
