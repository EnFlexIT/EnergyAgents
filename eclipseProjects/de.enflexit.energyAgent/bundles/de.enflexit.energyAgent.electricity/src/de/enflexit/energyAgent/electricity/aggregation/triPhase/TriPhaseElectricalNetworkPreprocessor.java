package de.enflexit.energyAgent.electricity.aggregation.triPhase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.common.SerialClone;
import de.enflexit.energyAgent.core.aggregation.AbstractNetworkCalculationPreprocessor;
import de.enflexit.energyAgent.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.energyAgent.lib.powerFlowCalculation.MeasuredBranchCurrent;
import de.enflexit.energyAgent.lib.powerFlowEstimation.decentralEstimation.AbstractGridStateEstimation;
import energy.OptionModelController;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.optionModel.AbstractUsageOfInterface;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.FixedDouble;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.TechnicalSystemStateTime;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.schedule.ScheduleController;
import hygrid.env.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import hygrid.globalDataModel.ontology.ElectricalNodeProperties;

/**
 * The Class ElectricalDistributionGridPreprocessor will be used in case that a state estimation
 * is required. This will be the case, if the known system states in the aggregation are only 
 * described by sensor data.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class TriPhaseElectricalNetworkPreprocessor extends AbstractNetworkCalculationPreprocessor {

	public final static String POWER_FLOW_ESTIMATION_CLASS = "PowerFlowEstimationClass";
	
	private static final String TSSE_CONFIGURATION_ID = "Default";
	private static final String TSSE_STATE_ID = "EstimatedState";
	private static final String REFPV_TYPE = "REF-PV";
	
	private AbstractGridStateEstimation gridStateEstimation; 
	
	private ArrayList<String> sensorIDs;
	private String REFPVID;
	private NetworkComponent refPVcomponent;
	
	
	public NetworkComponent getRefPVcomponent() {
		
		if (refPVcomponent==null){
			// --- Find REF-PV network component
			Vector<NetworkComponent> nmList = this.getNetworkModel().getNetworkComponentVectorSorted();
			for (int i=0;i<nmList.size();i++) {
				if( nmList.get(i).getType().equals(REFPV_TYPE)) {
					refPVcomponent = nmList.get(i);
				}	
			}
		}
		return refPVcomponent;
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractNetworkCalculationPreprocessor#doPreprocessing(long)
	 */
	@Override
	protected boolean doPreprocessing(long evaluationEndTime) {
		// --- Only estimate, if the simulation is based on sensor data -------
		if (this.getAggregationHandler().getExecutionDataBase()==ExecutionDataBase.NodePowerFlows) return true;

		// --- Get the sensor states ------------------------------------------
		HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates = this.getLastSensorStates();
				
		// --- Start Estimation -----------------------------------------------
		return this.startEstimation(lastSensorStates, evaluationEndTime);
		 
	}
	
	/** 
	 * This method performs the central grid state estimation 
	 * @param lastSensorStates
	 * @param evaluationEndTime
	 */
	public boolean startEstimation(HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates, long evaluationEndTime) {
		
		boolean sucessFull = false;
		// --- Find Ref PV ID
		String refPVId = this.getRefPVID();
		
		// --- Initial set of rel Power -----------------------------------------------------
		double powerOfRefPV = 0;
		double relPowerOfRefPV =0;
		//--- Get rel Power of refPV --------------------------------------------------------
		if (refPVId!=null) {
			// Getting TSSE From REFPV from last sensor States --------------------------------
			TechnicalSystemStateEvaluation tsseREFPV = lastSensorStates.get(refPVId);
			if(tsseREFPV!=null) {
				double voltageAbs = (((FixedDouble)tsseREFPV.getIOlist().get(0)).getValue()+((FixedDouble)tsseREFPV.getIOlist().get(1)).getValue()+((FixedDouble)tsseREFPV.getIOlist().get(2)).getValue())/3;
				double currentAbs = (((FixedDouble)tsseREFPV.getIOlist().get(3)).getValue()+((FixedDouble)tsseREFPV.getIOlist().get(4)).getValue()+((FixedDouble)tsseREFPV.getIOlist().get(5)).getValue())/3;
				powerOfRefPV = voltageAbs*currentAbs;
			}
			
			
				// --- Get nominal power of REF-PV
				double nominalPower=0;
				NetworkComponent refPVComponent = this.getRefPVcomponent();
				// --- Get Graph Node from network Component
				Vector<GraphElement> refPvGraphElements = this.getNetworkModel().getGraphElementsFromNetworkComponent(refPVComponent);
				if (refPvGraphElements!=null && refPvGraphElements.size()>0) {
					if (refPvGraphElements.get(0) instanceof GraphNode) {
						// --- Check if GraphNode is really this instance
						GraphNode refPvGraphNode = (GraphNode) refPvGraphElements.get(0);
						Object[] refPvNodeDataModel = (Object[]) refPvGraphNode.getDataModel();
						// --- nodeState is at the zero index of the object Array! --> See EletcricalTriNodeDataModelAdapter
						ElectricalNodeProperties refPvNodeState = (ElectricalNodeProperties) refPvNodeDataModel[0];
						nominalPower= refPvNodeState.getNominalPower().getValue();
					}
				}
				
				// --- Calculate relative power 
				relPowerOfRefPV = powerOfRefPV/(nominalPower*1000);
				
				
		}
		
		// --- If rel Power was found in TSSE, do estimation per phase
		sucessFull = this.estimationManagerPerPhase(this.getGridStateEstimation(), Phase.L1, lastSensorStates, relPowerOfRefPV, evaluationEndTime);
		sucessFull &= this.estimationManagerPerPhase(this.getGridStateEstimation(), Phase.L2, lastSensorStates, relPowerOfRefPV, evaluationEndTime);
		sucessFull &= this.estimationManagerPerPhase(this.getGridStateEstimation(), Phase.L3, lastSensorStates, relPowerOfRefPV, evaluationEndTime);
		
		return sucessFull;
	}
	
	private String getRefPVID() {
		if(this.REFPVID==null) {
			
			// Get REF-PV ID
			Vector<NetworkComponent> nmList = this.getNetworkModel().getNetworkComponentVectorSorted();
			NetworkComponent refPV =null;
			
			// --- Find REF-PV network component
			if( nmList.get(0).getType().equals(REFPV_TYPE)) {
				refPV = nmList.get(0);
			}
			
			
			if (refPV!=null) {
				// --- Find neighboured components
				Vector<NetworkComponent> neighbouredComponents = this.getNetworkModel().getNeighbourNetworkComponents(refPV);
				
				for (int i=0; i< neighbouredComponents.size();i++) {
					if (neighbouredComponents.get(i).getType().equals("Sensor"));{
						this.REFPVID=neighbouredComponents.get(i).getId();
					}
					
				}
			}
			
		}
		return this.REFPVID;
	}
	
	/**
	 * Gets the grid state estimation.
	 * @return the grid state estimation
	 */
	private AbstractGridStateEstimation getGridStateEstimation() {
		if (gridStateEstimation==null) {
			gridStateEstimation = (AbstractGridStateEstimation) this.getSubAggregationConfiguration().getUserClassInstance(POWER_FLOW_ESTIMATION_CLASS);
			if (gridStateEstimation!=null) {
				gridStateEstimation.setNetworkModel(getNetworkModel());
			}
		}
		return gridStateEstimation;
	}
	
	/**
	 * This method mananges the estimation per Phase
	 * @param gridStateEstimation
	 * @param phase
	 * @param lastSensorStates
	 * @param relPowerOfRefPV
	 * @param evaluationEndTime
	 */
	private boolean estimationManagerPerPhase(AbstractGridStateEstimation gridStateEstimation, Phase phase, HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates,double relPowerOfRefPV, long evaluationEndTime) {
		
		boolean isEstimationSuccessful=false;
		// --- Do estimation --------------------------------------------------------------------------------------
		gridStateEstimation.doEstimationPerPhase(phase, lastSensorStates, relPowerOfRefPV, evaluationEndTime);
		
		// --- Get results of estimation --------------------------------------------------------------------------
		HashMap<String, Double> estimatedNodalPowerRealGlobal = gridStateEstimation.getNodalPowerReal();
		HashMap<String, Double> estimatedNodalPowerImagGlobal = gridStateEstimation.getNodalPowerImag();
		HashMap<String, Double> estimatedPVNodesGlobal = gridStateEstimation.getvPVNodes();
		HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents = gridStateEstimation.getEstimatedBranchCurrents();
		isEstimationSuccessful = gridStateEstimation.isEstimationSuccessful();
		
		// --- Checking
		if (estimatedNodalPowerRealGlobal.size()>0 && estimatedNodalPowerImagGlobal.size()>0) {
			
			this.setEstimatedNodalPowerPerPhase(gridStateEstimation, phase, estimatedNodalPowerRealGlobal, estimatedNodalPowerImagGlobal, estimatedPVNodesGlobal, evaluationEndTime);
		
			// --- get Key Set from estimated Nodal Power --------------------
			ArrayList<String> netCompIdList = new ArrayList<>(estimatedNodalPowerRealGlobal.keySet());
			
			for (int i = 0; i < netCompIdList.size(); i++) {
				
				// --- Mapping i to NetworkComponent ----------------
				String netCompID = netCompIdList.get(i); 
				double powerFlowReal = estimatedNodalPowerRealGlobal.get(netCompID);
				double powerFlowImag = estimatedNodalPowerImagGlobal.get(netCompID);
				
				ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(netCompID);
				ScheduleList sl = sc.getScheduleList();
				TechnicalSystemStateEvaluation tsseNew = this.getCurrentOrNewTechnicalSystemState(evaluationEndTime, netCompID, sl);
				// --- If estimation is successful create TSSE for powerflow calculation---------------------------------
				if (isEstimationSuccessful==true) {
					// --- Update TSSE for the estimated power flows --------------
					this.updateSystemStateEnergyFlows(sl, tsseNew, powerFlowReal, powerFlowImag, phase);
				}
			}
		
			// ----------------------------------------------------------------
			// --- Set PV nodes info to the network calculation ---------------
			// ----------------------------------------------------------------
//			Class<?> powerFlowClass = this.getSubAggregationConfiguration().getUserClasses().get(PowerFlowCalculationThread.POWER_FLOW_CALCULATION_CLASS);
//			if (powerFlowClass.getName().equals(PowerFlowCalculationPV.class.getName())==true) {
			AbstractElectricalNetworkCalculationStrategy netClacStrat = (AbstractElectricalNetworkCalculationStrategy) this.getSubAggregationConfiguration().getNetworkCalculationStrategy();
			netClacStrat.getPowerFlowCalculationThread(Thread.currentThread(), phase).setMeasuredPvVoltage(gridStateEstimation.pvNodesFromHashMap(estimatedPVNodesGlobal));
			netClacStrat.getPowerFlowCalculationThread(Thread.currentThread(), phase).setEstimatedBranchCurrents(estimatedBranchCurrents);;
//			}
			// ----------------------------------------------------------------
		}
		return isEstimationSuccessful;
	}

	private void setEstimatedNodalPowerPerPhase(AbstractGridStateEstimation gridStateEstimation, Phase phase,HashMap<String, Double> estimatedNodalPowerReal, HashMap<String, Double> estimatedNodalPowerImag, HashMap<String, Double> estimatedPVNodes, long evaluationEndTime) {
		
		// --- get Key Set from estimated Nodal Power --------------------
		ArrayList<String> netCompIdList = new ArrayList<>(estimatedNodalPowerReal.keySet());
		for (int i = 0; i < netCompIdList.size(); i++) {
			
			// --- Mapping i to NetworkComponent ----------------
			String netCompID = netCompIdList.get(i); 
			double powerFlowReal = estimatedNodalPowerReal.get(netCompID);
			double powerFlowImag = estimatedNodalPowerImag.get(netCompID);
			
			ScheduleController sc = this.getAggregationHandler().getNetworkComponentsScheduleController().get(netCompID);
			ScheduleList sl = sc.getScheduleList();
			TechnicalSystemStateEvaluation tsseNew = this.getCurrentOrNewTechnicalSystemState(evaluationEndTime, netCompID, sl);
			// --- Update TSSE for the estimated power flows --------------
			this.updateSystemStateEnergyFlows(sl, tsseNew, powerFlowReal, powerFlowImag, phase);
		}
	}
	
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
	 * Returns the current or new technical system state. If a new state was 
	 * created, it will automatically be added to the Schedule
	 *
	 * @param timeStamp the time stamp
	 * @param netCompID the net comp ID
	 * @param sl the sl
	 * @return the default technical system state
	 */
	private TechnicalSystemStateEvaluation getCurrentOrNewTechnicalSystemState(long timeStamp, String netCompID, ScheduleList sl) {
		
		boolean appendToSchedule = false;
		TechnicalSystemStateEvaluation tsse = null;
		if (sl.getSchedules().size()==0 || sl.getSchedules().get(0).getTechnicalSystemStateEvaluation()==null) {
			// --- Create complete new TSSE -----------------------------------
			tsse = new TechnicalSystemStateEvaluation();
			// --- Create a TSSE for the current measurements -----------------
			tsse.setGlobalTime(timeStamp);
			tsse.setStateTime(timeStamp-this.getEvaluationStartTime());
			tsse.setConfigID(TSSE_CONFIGURATION_ID);
			tsse.setStateID(TSSE_STATE_ID);
			this.updateSystemStateEnergyFlows(sl, tsse, 0, 0, Phase.L1);
			this.updateSystemStateEnergyFlows(sl, tsse, 0, 0, Phase.L2);
			this.updateSystemStateEnergyFlows(sl, tsse, 0, 0, Phase.L3);
			appendToSchedule = true;
			
		} else {
			// --- Get the last system state ----------------------------------
			Schedule schedule = sl.getSchedules().get(0);
			TechnicalSystemStateEvaluation tsseWork = schedule.getTechnicalSystemStateEvaluation();
			if (tsseWork.getGlobalTime()==timeStamp) {
				// ------------------------------------------------------------
				// --- If already there, use the tsse found -------------------
				// ------------------------------------------------------------
				tsse = tsseWork;
				
			} else {
				// ------------------------------------------------------------
				// --- If not already found, adjust copy of last state --------
				// ------------------------------------------------------------
				// --- Remind parent ------------------------------------------
				TechnicalSystemStateEvaluation tsseParent = tsseWork .getParent();
				tsseWork.setParent(null);
				// --- Copy last system state ---------------------------------
				tsse = SerialClone.clone(tsseWork); 
				if (tsseParent!=null) {
					tsseWork.setParent(tsseParent);
				}
				// --- Configure return value ---------------------------------
				tsse.setGlobalTime(timeStamp);
				tsse.setStateTime(timeStamp - tsseWork.getGlobalTime());
				appendToSchedule = true;
				
			}
		}
		
		// --- Append a new state to the ScheudleList ? ----------------------- 
		if (tsse!=null && appendToSchedule==true) {
			this.getAggregationHandler().appendToNetworkComponentsScheduleController(netCompID, tsse);
		}
		return tsse;
	}
	
	
	/**
	 * Gets the evaluation start time from the option model controller.
	 * @return the evaluation start time
	 */
	private long getEvaluationStartTime() {
		OptionModelController omc = this.getGroupController().getGroupOptionModelController();
		TechnicalSystemStateTime initialState = omc.getEvaluationSettings().getEvaluationStateList().get(0);
		return initialState.getGlobalTime();
	}
	
	/**
	 * Update system state energy flows.
	 *
	 * @param sl the sl
	 * @param tsse the tsse
	 * @param estimatedNodalPowerReal the estimated nodal power real
	 * @param estimatedNodalPowerImag the estimated nodal power imag
	 * @param actualPhase the actual phase
	 */
	private void updateSystemStateEnergyFlows(ScheduleList sl, TechnicalSystemStateEvaluation tsse, double estimatedNodalPowerReal, double estimatedNodalPowerImag, Phase actualPhase) {
		
		// --- Add the real power flow --------------------
		EnergyFlowInWatt efwReal = new EnergyFlowInWatt();
		efwReal.setSIPrefix(EnergyUnitFactorPrefixSI.NONE_0);
		efwReal.setValue(estimatedNodalPowerReal);
		
		UsageOfInterfaceEnergy uoiReal = new UsageOfInterfaceEnergy();
		uoiReal.setInterfaceID(this.getInterfaceIDFromPhaseAndPowerType(sl, actualPhase, PowerType.ActivePower));
		uoiReal.setEnergyFlow(efwReal);
		this.addOrUpdateUsageOfInterface(tsse, uoiReal);
		
		// --- Add the imag power flow --------------------
		EnergyFlowInWatt efwImag = new EnergyFlowInWatt();
		efwImag .setSIPrefix(EnergyUnitFactorPrefixSI.NONE_0);
		efwImag .setValue(estimatedNodalPowerImag);
		
		UsageOfInterfaceEnergy uoiImag = new UsageOfInterfaceEnergy();
		uoiImag.setInterfaceID(this.getInterfaceIDFromPhaseAndPowerType(sl, actualPhase, PowerType.ReactivePower));
		uoiImag.setEnergyFlow(efwImag);
		this.addOrUpdateUsageOfInterface(tsse, uoiImag);
	}
	
	/**
	 * Gets the interface ID from phase and power type.
	 *
	 * @param sl the ScheduleList
	 * @param phaseToSearchFor the phase to search for
	 * @param powerTypeToSearchFor the power type to search for
	 * @return the interface ID from phase and power type
	 */
	private String getInterfaceIDFromPhaseAndPowerType(ScheduleList sl, Phase phaseToSearchFor, PowerType powerTypeToSearchFor) {
		
		String interfaceID = null;
		List<InterfaceSetting> intSettings = sl.getInterfaceSettings();
		for (int i = 0; i < intSettings.size(); i++) {
			InterfaceSetting intSetting = intSettings.get(i);
			if (intSetting.getDomainModel() instanceof DefaultDomainModelElectricity) {
				DefaultDomainModelElectricity dmElec = (DefaultDomainModelElectricity) intSetting.getDomainModel();
				if (dmElec.getPhase()==phaseToSearchFor && dmElec.getPowerType()==powerTypeToSearchFor) {
					interfaceID = intSetting.getInterfaceID();
				}
			}
		}
		if (interfaceID==null) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Could not find interface ID for '" + sl.getNetworkID() + "' '" + sl.getSystemID() + "' for power type '" + powerTypeToSearchFor.name() + " and phase " + phaseToSearchFor.name());
		}
		return interfaceID;
	}
	
	/**
	 * Adds the or update the usage of interface.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation
	 * @param auoi the AbstractUsageOfInterface to consider
	 */
	private void addOrUpdateUsageOfInterface(TechnicalSystemStateEvaluation tsse, AbstractUsageOfInterface auoi) {
		
		int editIndex = -1;
		for (int i = 0; i < tsse.getUsageOfInterfaces().size(); i++) {
			AbstractUsageOfInterface auoiFound = tsse.getUsageOfInterfaces().get(i);
			if (auoiFound.getInterfaceID().equals(auoi.getInterfaceID())==true) {
				editIndex = i;
				break;
			}
		}
		
		if (editIndex==-1) {
			tsse.getUsageOfInterfaces().add(auoi);
		} else {
			tsse.getUsageOfInterfaces().set(editIndex, auoi);
		}
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
	private List<String> getSensorIDs() {
		if (sensorIDs==null) {
			this.createNetworkComponentLists();
		}
		return sensorIDs;
	}
	/**
	 * Creates the lists for sensor and non-sensor NetworkComponent .
	 */
	private void createNetworkComponentLists() {
		sensorIDs = new ArrayList<>();
		// --- List all available NetworkComponents --- 
		Vector<NetworkComponent> netComps = this.getNetworkModel().getNetworkComponentVectorSorted();
		for (int i = 0; i < netComps.size(); i++) {
			NetworkComponent netComp = netComps.get(i);
			if (netComp.getType().equals("Sensor")==true) {
				sensorIDs.add(netComp.getId());
			}
		}
	}

}