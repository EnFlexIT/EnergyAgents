package de.enflexit.ea.electricity.estimation;

import java.util.HashMap;

import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationPreprocessor;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.lib.powerFlowCalculation.MeasuredBranchCurrent;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.GroupController;

/**
 * The Class AbstractGridStateEstimation.
 */
public abstract class AbstractGridStateEstimation {

	private Phase actualPhase = null;
	
	// --- Result of grid State estimation -----------------------------
	private HashMap<String, Double> nodalPowerReal = new HashMap<>();
	private HashMap<String, Double> nodalPowerImag = new HashMap<>();
	private HashMap<String, Double> vPVNodes = new HashMap<>();
	private HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents = new HashMap<>();
	
	private AbstractNetworkCalculationPreprocessor networkCalculationPreProcessor;
	private NetworkModel networkModel;
	
	private boolean isCentralEstimation = true;

	
	
	/**
	 * Sets the current NetworkCalculationPreprocessor. 
	 * @param networkCalculationPreProcessor the new network calculation pre processor
	 */
	public void setNetworkCalculationPreProcessor(AbstractNetworkCalculationPreprocessor networkCalculationPreProcessor) {
		this.networkCalculationPreProcessor = networkCalculationPreProcessor;
	}
	/**
	 * Return the current NetworkCalculationPreprocessor .
	 * @return the network calculation pre processor
	 */
	public AbstractNetworkCalculationPreprocessor getNetworkCalculationPreProcessor() {
		return networkCalculationPreProcessor;
	}
	/**
	 * Returns the current sub aggregation configuration.
	 * @return the sub aggregation configuration
	 */
	public AbstractSubNetworkConfiguration getSubAggregationConfiguration() {
		return this.getNetworkCalculationPreProcessor()==null ? null :this.getNetworkCalculationPreProcessor().getSubAggregationConfiguration();
	}
	/**
	 * Return the currently used aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return this.getNetworkCalculationPreProcessor()==null ? null :this.getNetworkCalculationPreProcessor().getAggregationHandler();
	}
	/**
	 * Returns the GroupController of the aggregation.
	 * @return the group controller
	 */
	public GroupController getGroupController() {
		return this.getNetworkCalculationPreProcessor()==null ? null :this.getNetworkCalculationPreProcessor().getGroupController();
	}
	
	
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
	 * @return the nodal power real
	 */
	public HashMap<String,Double> getNodalPowerReal() {
		return nodalPowerReal;
	}
	/**
	 * Sets the nodal power real.
	 * @param nodalPowerReal the nodal power real
	 */
	public void setNodalPowerReal(HashMap<String,Double> nodalPowerReal) {
		this.nodalPowerReal = nodalPowerReal;
	}

	/**
	 * Gets the nodal power imag.
	 * @return the nodal power imag
	 */
	public HashMap<String,Double> getNodalPowerImag() {
		return nodalPowerImag;
	}
	/**
	 * Sets the nodal power imag.
	 * @param nodalPowerImag the nodal power imag
	 */
	public void setNodalPowerImag(HashMap<String,Double> nodalPowerImag) {
		this.nodalPowerImag = nodalPowerImag;
	}

	
	/**
	 * Gets the v PV nodes.
	 * @return the v PV nodes
	 */
	public HashMap<String,Double> getvPVNodes() {
		return vPVNodes;
	}
	/**
	 * Setv PV nodes.
	 * @param vPVNodes the v PV nodes
	 */
	public void setvPVNodes(HashMap<String,Double> vPVNodes) {
		this.vPVNodes = vPVNodes;
	}

	
	public boolean isCentralEstimation() {
		return isCentralEstimation;
	}
	public void setCentralEstimation(boolean central) {
		this.isCentralEstimation = central;
	}
	public HashMap<String, MeasuredBranchCurrent> getEstimatedBranchCurrents() {
		return estimatedBranchCurrents;
	}
	public void setEstimatedBranchCurrents(HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents) {
		this.estimatedBranchCurrents = estimatedBranchCurrents;
	}
	
	/**
	 * Has to do the actual estimation.
	 *
	 * @param lastSensorStates the last sensor states
	 * @param evaluationEndTime the evaluation end time
	 * @return true, if the estimation was successful
	 */
	public abstract boolean doEstimation(HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates, long evaluationEndTime);
	

}
