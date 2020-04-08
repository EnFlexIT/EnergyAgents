package de.enflexit.energyAgent.electricity.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.helper.DomainCluster;

import de.enflexit.energyAgent.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.energyAgent.core.globalDataModel.ontology.CableState;
import de.enflexit.energyAgent.core.globalDataModel.ontology.ElectricalNodeState;
import energy.OptionModelController;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedVariable;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.schedule.ScheduleController;
import energygroup.GroupTreeNodeObject;
import energygroup.sequentialNetworks.AbstractSequentialNetworkCalculation;
import hygrid.csvFileImport.NetworkModelToCsvMapper;
import hygrid.csvFileImport.NetworkModelToCsvMapper.SlackNodeDescription;
import hygrid.env.HyGridAbstractEnvironmentModel.ExecutionDataBase;

/**
 * Common super class for eletrical network calculation strategies
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractElectricalNetworkCalculationStrategy extends AbstractNetworkCalculationStrategy {
	
	private NetworkModelToCsvMapper networkModelToCsvMapper;
	
	private HashMap<String, PowerFlowCalculationThread> powerFlowCalculationThreads;
	private Object calculationTrigger;
	private Vector<Phase> powerFlowCalculationsFinalized;

	private HashMap<String, ElectricalNodeState> graphNodeStates;
	private HashMap<String, CableState> networkComponentStates;
	private HashMap<String, TechnicalSystemState> transformerStates;
	
	protected HashMap<Phase, Double> slackNodeVoltageLevel;
	protected boolean isChangedSlackNodeVoltageLevel;

	private List<String> sensorIDs;
	
	
	/**
	 * Instantiates the network calculation strategy for the {@link SimulationManager}.
	 * @param optionModelController the option model controller
	 */
	public AbstractElectricalNetworkCalculationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	
	/**
	 * Returns the electrical node states and represents one result of this network calculation.
	 * @return the electrical node states
	 */
	public HashMap<String, ElectricalNodeState> getGraphNodeStates() {
		if (graphNodeStates == null) {
			graphNodeStates = new HashMap<String, ElectricalNodeState>();
		}
		return graphNodeStates;
	}
	/**
	 * Returns the network component states and represents one result of this network calculation.
	 * @return the network component states
	 */
	public HashMap<String, CableState> getNetworkComponentStates() {
		if (networkComponentStates == null) {
			networkComponentStates = new HashMap<String, CableState>();
		}
		return networkComponentStates;
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
			DomainCluster domainCluster = this.getSubAggregationConfiguration().getDomainCluster();
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
	 * Sets the slack node voltage level for all three phases of the network / transformer.
	 * @param newVoltageLevel the new slack node voltage level
	 */
	public void setSlackNodeVoltageLevel(HashMap<Phase, Double> newSlackNodeVoltageLevel) {
		this.slackNodeVoltageLevel = newSlackNodeVoltageLevel;
		this.isChangedSlackNodeVoltageLevel = true;
	}
	/**
	 * Returns the current slack node voltage level.
	 * @return the slack node voltage level
	 */
	protected HashMap<Phase, Double> getSlackNodeVoltageLevel() {
		if (slackNodeVoltageLevel==null) {
			slackNodeVoltageLevel = new HashMap<>();
			slackNodeVoltageLevel.put(Phase.L1, this.getDefaultSlackNodeVoltage());
			slackNodeVoltageLevel.put(Phase.L2, this.getDefaultSlackNodeVoltage());
			slackNodeVoltageLevel.put(Phase.L3, this.getDefaultSlackNodeVoltage());
		}
		return this.slackNodeVoltageLevel;
	}
	
	protected abstract double getDefaultSlackNodeVoltage();
	
//	/* (non-Javadoc)
//	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#doNetworkCalculation(javax.swing.tree.DefaultMutableTreeNode, java.util.List, energygroup.calculation.FlowsMeasuredGroup)
//	 */
//	@Override
//	public FlowsMeasuredGroupMember doNetworkCalculation(DefaultMutableTreeNode currentParentNode, List<TechnicalInterface> outerInterfaces, FlowsMeasuredGroup efmGroup) {
//		
//		this.debugPrintLine(efmGroup.getGlobalTimeTo(), "Execute '" + this.getClass().getSimpleName() + "'");
//
//		// --- Update slack node voltage level for sensor data based calculations -------
//		this.updateSlackNodeVoltage();
//		
//		// ------------------------------------------------------------------------------
//		// --- (Re) execute the phase dependent electrical network calculation ----------
//		// ------------------------------------------------------------------------------
//		this.getPowerFlowCalculationsFinalized().clear();
//		// --- Reset the slack node voltage level? --------------------------------------
//		if (this.isChangedSlackNodeVoltageLevel==true) {
//			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).setSlackNodeVoltageLevel(this.getSlackNodeVoltageLevel().get(Phase.L1));
//			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L2).setSlackNodeVoltageLevel(this.getSlackNodeVoltageLevel().get(Phase.L2));
//			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L3).setSlackNodeVoltageLevel(this.getSlackNodeVoltageLevel().get(Phase.L3));
//			this.isChangedSlackNodeVoltageLevel = false;
//		}
//		// --- Reset the calculation parameter ------------------------------------------
//		this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).resetCalculationBase(currentParentNode, efmGroup);
//		this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L2).resetCalculationBase(currentParentNode, efmGroup);
//		this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L3).resetCalculationBase(currentParentNode, efmGroup);
//		// --- Notify all calculation threads to (re-)restart the calculation ----------- 
//		synchronized (this.getCalculationTrigger()) {
//			this.getCalculationTrigger().notifyAll();
//		}
//		
//		// ------------------------------------------------------------------------------
//		// --- Summarize interfaces that don't need a network calculation ---------------
//		// ------------------------------------------------------------------------------
//		FlowsMeasuredGroupMember efmSummarized = new FlowsMeasuredGroupMember();
//		for (int i = 0; i < outerInterfaces.size(); i++) {
//			
//			TechnicalInterface ti = outerInterfaces.get(i);
//			EnergyInterface ei = (EnergyInterface) ti;
//			try {
//				// --- Calculate the sum of energy flow for this interface --------------
//				EnergyFlowMeasured efm = efmGroup.sumUpEnergyFlowMeasuredByEnergyCarrierAndDomainModel(currentParentNode, ei.getInterfaceID(), ei.getEnergyCarrier(), ei.getDomainModel());
//				// --- In case that there is no EnergyFlowMeasured ----------------------
//				if (efm.getMeasurments().size()==0) {
//					efm = this.getEnergyFlowMeasuredZeroOverTime(ei.getInterfaceID(), efmGroup.getGlobalTimeFrom(), efmGroup.getGlobalTimeTo(), this.getDefaultSIPrefix());
//				}
//				efmSummarized.addEnergyFlowMeasured(efm, ei.getInterfaceID(), ei.getDomain(), ei.getDomainModel(), ei.getEnergyCarrier());
//				
//			} catch (Exception ex) {
//				System.err.println("[" + this.getClass().getSimpleName() + "] Error summarizing the energy flow for '" + ei.getInterfaceID() + "' Energy Carrier: " + ei.getEnergyCarrier().value() + ", Domain-Model: " + ei.getDomainModel().toString());
//				ex.printStackTrace();
//			}
//		}
//		
//		// --- Wait for the end of the power flow calculations --------------------------
//		this.waitUntilCalculationFinalized(this.getPowerFlowCalculationThreads().size());
//		// --- Create the display notifications from the calculation results ------------  
//		this.summarizeResults(efmGroup.getGlobalTimeTo());
//		// -- Done ----------------------------------------------------------------------
//		return efmSummarized;
//	}
	
	/**
	 * Updates the slack node voltage level in case that the source of the data is based on sensor data.
	 */
	protected void updateSlackNodeVoltage() {

		if (this.getAggregationHandler().getExecutionDataBase()==ExecutionDataBase.SensorData) {

			// ---- Which sensor / sensor data? --------------------- 
			SlackNodeDescription snDesc = this.getNetworkModelToCsvMapper().getSlackNodeVector().get(0);
			NetworkComponent netCompSlackNode = this.getNetworkModel().getNetworkComponent(snDesc.getNetworkComponentID());

			// --- Try to find a sensor Neighbor here -------------- 
			String netCompIDSensor = null;
			Vector<NetworkComponent> snNeighbours = this.getNetworkModel().getNeighbourNetworkComponents(netCompSlackNode);
			for (int i = 0; i < snNeighbours.size(); i++) {
				NetworkComponent snNeighbour = snNeighbours.get(i);
				//TODO adjust type, add mv sensor?
				if (snNeighbour.getType().equalsIgnoreCase("Sensor")==true) {
					netCompIDSensor = snNeighbour.getId();
					break;
				}
			}
			
			// --- Get the last TSSE from the corresponding sensor --
			if (netCompIDSensor!=null) {
				ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(netCompIDSensor);
				
				if (sc!=null && sc.getScheduleList().getSchedules().size()>0) {
					HashMap<Phase,Double> newSlackNodeVoltageLevel = new HashMap<>();
					Schedule schedule = sc.getScheduleList().getSchedules().get(0);
					TechnicalSystemStateEvaluation tsseSensor = schedule.getTechnicalSystemStateEvaluation();
					for (int i = 0; i < tsseSensor.getIOlist().size(); i++) {
						FixedVariable fvIO = tsseSensor.getIOlist().get(i);
						if(fvIO.getVariableID().equals("Voltage L1")) { //TODO UniPhase?
							newSlackNodeVoltageLevel.put(Phase.L1, ((FixedDouble)fvIO).getValue());
						}
						if(fvIO.getVariableID().equals("Voltage L2")) {
							newSlackNodeVoltageLevel.put(Phase.L2, ((FixedDouble)fvIO).getValue());
						}
						if(fvIO.getVariableID().equals("Voltage L3")) {
							newSlackNodeVoltageLevel.put(Phase.L3, ((FixedDouble)fvIO).getValue());
						}
					} 
					this.setSlackNodeVoltageLevel(newSlackNodeVoltageLevel);
				}
			}
		}
	}

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
	
	protected abstract void summarizeResults(long globalTimeTo);
//	/**
//	 * Creates the {@link DisplayAgentNotificationGraph} from the power flow calculation results.
//	 * @param globalTimeTo the global time to which is to be calculated
//	 */
//	private void summarizeResults(long globalTimeTo) {
//
//		try {
//			
//			// --- Get the PowerFlowCalculations for each phase -------------------------
//			NetworkModelToCsvMapper netModel2CsvMapper = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).getNetworkModelToCsvMapper();
//			
//			AbstractPowerFlowCalculation pfcL1 = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).getPowerFlowCalculation();
//			AbstractPowerFlowCalculation pfcL2 = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L2).getPowerFlowCalculation();
//			AbstractPowerFlowCalculation pfcL3 = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L3).getPowerFlowCalculation();
//			
//			if (pfcL1==null && pfcL2==null && pfcL3==null) return;
//			
//			// --- Check if all pfc were successful, then summarize results -------------
//			if (pfcL1.isSucessfullPFC() && pfcL2.isSucessfullPFC() && pfcL3.isSucessfullPFC()) {
//				// --- Define the GraphNode state: A TriPhaseElectricalNodeState ------------
//				this.setNodeStates(pfcL1, pfcL2, pfcL3, netModel2CsvMapper);
//				// --- Edit the 'Cable' data model of the NetworkComponents affected --------
//				this.setCableStates(pfcL1, pfcL2, pfcL3, netModel2CsvMapper);
//				// --- Set the transformer state to the Blackboard --------------------------
//				this.setTransformerState(globalTimeTo, pfcL1, pfcL2, pfcL3, netModel2CsvMapper);
//				// --- Extend the Sensor ScheduleList ---------------------------------------
//				this.setSensorTechnicalSystemStates(globalTimeTo);
//			}	
//			
//		} catch (Exception ex) {
//			System.err.println("[" + this.getClass().getSimpleName() + "] Error summarizing results from PowerFlowCalculation:");
//			ex.printStackTrace();
//		}
//		
//	}

	
	
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
			if (this.getSubAggregationConfiguration().getDomainCluster()==null) {
				netComps = this.getAggregationHandler().getNetworkModel().getNetworkComponentVectorSorted();
			} else {
				netComps = this.getSubAggregationConfiguration().getDomainCluster().getNetworkComponents();
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
	public TechnicalSystemStateDeltaEvaluation meetDecisionForTechnicalSystem(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#meetDecisionForScheduleList(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject, energy.optionModel.ScheduleList)
	 */
	@Override
	public Schedule meetDecisionForScheduleList(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno, ScheduleList scheduleList) {
		return null;
	}
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#getCustomToolBarElements()
	 */
	@Override
	public Vector<JComponent> getCustomToolBarElements() {
		return null;
	}

}
