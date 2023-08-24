package de.enflexit.ea.electricity.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.helper.DomainCluster;

import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.EdgeComponentState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper;
import de.enflexit.ea.electricity.blackboard.SubBlackboardModelElectricity;
import energy.OptionModelController;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.schedule.ScheduleController;
import energygroup.GroupTreeNodeObject;
import energygroup.sequentialNetworks.AbstractSequentialNetworkCalculation;

/**
 * Common super class for electrical network calculation strategies
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractElectricalNetworkCalculationStrategy extends AbstractNetworkCalculationStrategy {
	
	private NetworkModelToCsvMapper networkModelToCsvMapper;
	
	private HashMap<String, PowerFlowCalculationThread> powerFlowCalculationThreads;
	private Object calculationTrigger;
	private Vector<Phase> powerFlowCalculationsFinalized;

	private HashMap<String, ElectricalNodeState> nodeStates;
	private HashMap<String, CableState> cableStates;
	private HashMap<String, TechnicalSystemState> transformerStates;
	
	private List<String> sensorIDs;
	
	
	/**
	 * Instantiates the network calculation strategy for the {@link SimulationManager}.
	 * @param optionModelController the option model controller
	 */
	public AbstractElectricalNetworkCalculationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy#getNodeStates()
	 */
	public HashMap<String, ElectricalNodeState> getNodeStates() {
		if (nodeStates == null) {
			nodeStates = new HashMap<String, ElectricalNodeState>();
		}
		return nodeStates;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy#getEdgeStates()
	 */
	@Override
	public HashMap<String, ? extends EdgeComponentState> getEdgeStates() {
		return this.getCableStates();
	}

	/**
	 * Returns the network component states and represents one result of this network calculation.
	 * @return the network component states
	 */
	public HashMap<String, CableState> getCableStates() {
		if (cableStates == null) {
			cableStates = new HashMap<String, CableState>();
		}
		return cableStates;
	}
	/**
	 * Returns the transformer states and represents one result of this network calculation.
	 * @return the transformer states
	 */
	public HashMap<String, TechnicalSystemState> getTransformerStates() {
		if (transformerStates == null) {
			transformerStates = new HashMap<String, TechnicalSystemState>();
		}
		return transformerStates;
	}

	// --------------------------------------------------------------------------------------------
	// --- From here, methods for the network calculation are located -----------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the {@link NetworkModelToCsvMapper}.
	 * @return the network model to csv mapper
	 */
	public NetworkModelToCsvMapper getNetworkModelToCsvMapper() {
		if (networkModelToCsvMapper==null) {
			DomainCluster domainCluster = this.getSubNetworkConfiguration().getDomainCluster();
			networkModelToCsvMapper = new NetworkModelToCsvMapper(this.getNetworkModel(), domainCluster);
		}
		return networkModelToCsvMapper;
	}
	/**
	 * Returns all {@link PowerFlowCalculationThread}'s 
	 * @return the power flow calculation threads
	 */
	public HashMap<String, PowerFlowCalculationThread> getPowerFlowCalculationThreads() {
		if (powerFlowCalculationThreads==null) {
			powerFlowCalculationThreads = new HashMap<String, PowerFlowCalculationThread>();
		}
		return powerFlowCalculationThreads;
	}
	/**
	 * Returns the {@link PowerFlowCalculationThread} that is responsible for the specified {@link Phase}.
	 * @param phaseToCalculate the phase
	 * @return the power flow calculation thread
	 */
	public synchronized PowerFlowCalculationThread getPowerFlowCalculationThread(Thread threadUsed, Phase phaseToCalculate) {
		String key = threadUsed.getName()  + "_PFC_" + phaseToCalculate;
		PowerFlowCalculationThread pfcThread = this.getPowerFlowCalculationThreads().get(key);
		if (pfcThread==null) {
			pfcThread = new PowerFlowCalculationThread(this, phaseToCalculate, this.optionModelController);
			pfcThread.setName(key);
			pfcThread.start();
			getPowerFlowCalculationThreads().put(key, pfcThread);
		}
		return pfcThread;
	}
	/**
	 * Gets the calculation trigger that can be notified in order to restart the calculations.
	 * @return the calculation trigger
	 */
	public Object getCalculationTrigger() {
		if (calculationTrigger==null) {
			calculationTrigger = new Object();
		}
		return calculationTrigger;
	}
	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractNetworkCalculationStrategy#terminateRelatedStrategyInstances()
	 */
	@Override
	public void terminateRelatedStrategyInstances() {
		// --- Stop the local PowerFlowCalculationThread ------------
		ArrayList<PowerFlowCalculationThread> pfcThreads = new ArrayList<>(this.getPowerFlowCalculationThreads().values());
		for (int i = 0; i < pfcThreads.size(); i++) {
			pfcThreads.get(i).terminate();
		}
		synchronized (this.getCalculationTrigger()) {
			this.getCalculationTrigger().notifyAll();
		}
		Thread.currentThread().interrupt();
	}
	
	/**
	 * Has to return the current slack node handler.
	 * @return the slack node handler
	 */
	public abstract AbstractSlackNodeHandler getSlackNodeHandler();

	/**
	 * Wait until the calculation is finalized.
	 * @param noOfResultsExpected the no of results expected
	 */
	protected void waitUntilCalculationFinalized(int noOfResultsExpected) {
		while (this.getPowerFlowCalculationsFinalized().size()<noOfResultsExpected) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException ie) {
				//ie.printStackTrace();
			}
		}
	}
	/**
	 * Gets all power flow calculations that have been finalized.
	 * @return the power flow calculations finalized
	 */
	protected Vector<Phase> getPowerFlowCalculationsFinalized() {
		//TODO check if necessary for uni phase, move to distribution grid strategy if not
		if (powerFlowCalculationsFinalized==null) {
			powerFlowCalculationsFinalized = new Vector<>();
		}
		return powerFlowCalculationsFinalized;
	}
	/**
	 * Sets that a phase calculation was finalized.
	 * @param phase the new calculation finalized
	 */
	public void setCalculationFinalized(Phase phase) {
		this.getPowerFlowCalculationsFinalized().addElement(phase);
	}
	
	/**
	 * has to summarize results from the actual power flow calculation.
	 * @param globalTimeTo the global time to
	 */
	protected abstract void summarizeResults(long globalTimeTo);
	
	/**
	 * Returns the last sensor states out of the corresponding ScheduleController.
	 * @return the last sensor states
	 */
	protected HashMap<String, TechnicalSystemStateEvaluation> getLastSensorStates() {
		
		HashMap<String, TechnicalSystemStateEvaluation> lastStateHash = new HashMap<>();
		
		List<String> sensorIDs = this.getSensorIDs();
		for (int i = 0; i < sensorIDs.size(); i++) {
			
			String sensorID = sensorIDs.get(i);
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
	protected List<String> getSensorIDs() {
		if (sensorIDs==null) {
			sensorIDs = new ArrayList<>();
			// --- List all available NetworkComponents --- 
			Vector<NetworkComponent> netComps = null;
			if (this.getSubNetworkConfiguration().getDomainCluster()==null) {
				netComps = this.getAggregationHandler().getNetworkModel().getNetworkComponentVectorSorted();
			} else {
				netComps = this.getSubNetworkConfiguration().getDomainCluster().getNetworkComponents();
			}
			
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
	 * Adds a new instance of <i>>UsageOfInterface</i> to the specified {@link TechnicalSystemState}.
	 *
	 * @param tss the {@link TechnicalSystemState} to edit
	 * @param interfaceID the interface id
	 * @param energyFlow the energy flow
	 */
	protected void addUsageOfInterfaces(TechnicalSystemState tss, String interfaceID, double energyFlowInWatt) {
		
		// --- Create a new EnergyFlowInWatt ---- 
		EnergyFlowInWatt ef = new EnergyFlowInWatt();
		ef.setSIPrefix(EnergyUnitFactorPrefixSI.NONE_0);
		ef.setValue(energyFlowInWatt);
		
		// --- Define the usage of interfaces ---
		UsageOfInterfaceEnergy uoi = new UsageOfInterfaceEnergy();
		uoi.setInterfaceID(interfaceID);
		uoi.setEnergyFlow(ef);
		tss.getUsageOfInterfaces().add(uoi);
	}
	
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#getSequentialNetworkCalculation()
	 */
	@Override
	public AbstractSequentialNetworkCalculation<?> getSequentialNetworkCalculation() {
		return null;
	}
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#meetDecisionForTechnicalSystem(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject, java.util.Vector)
	 */
	@Override
	public TechnicalSystemStateDeltaEvaluation decideForDeltaStepToBeUsed(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#meetDecisionForScheduleList(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject, energy.optionModel.ScheduleList)
	 */
	@Override
	public Schedule decideForScheduleToBeUsed(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, ScheduleList scheduleList) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#getCustomToolBarElements()
	 */
	@Override
	public Vector<JComponent> getCustomToolBarElements() {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy#updateAggregationBlackboardModel(de.enflexit.ea.core.aggregation.AbstractSubBlackboardModel)
	 */
	@Override
	public void updateSubBlackboardModel() {
		SubBlackboardModelElectricity subBlackboardModel = (SubBlackboardModelElectricity) this.getSubBlackboardModel();
		subBlackboardModel.getNodeStates().putAll(this.getNodeStates());
		subBlackboardModel.getCableStates().putAll(this.getCableStates());
		subBlackboardModel.getTransformerStates().putAll(this.getTransformerStates());
	}

	// ------------------------------------------------------------------------
	// --- From here some general calculation classes -------------------------
	// ------------------------------------------------------------------------	
	/**
	 * Calculates the node state current.
	 *
	 * @param nodalPowerReal the nodal power real
	 * @param nodalPowerImag the nodal power imag
	 * @param uKabs the u kabs
	 * @return the node state current
	 */
	protected double getNodeStateCurrent(double nodalPowerReal, double nodalPowerImag, double uKabs) {
		return getNodeStateS(nodalPowerReal, nodalPowerImag) / uKabs;  
	}
	/**
	 * Calculates the node state apparent power.
	 *
	 * @param nodalPowerReal the nodal power real
	 * @param nodalPowerImag the nodal power imag
	 * @return the node state S
	 */
	protected double getNodeStateS(double nodalPowerReal, double nodalPowerImag) {
		return Math.signum(nodalPowerReal) * (Math.sqrt(Math.pow(nodalPowerReal, 2) + Math.pow(nodalPowerImag, 2)));
	}

}
