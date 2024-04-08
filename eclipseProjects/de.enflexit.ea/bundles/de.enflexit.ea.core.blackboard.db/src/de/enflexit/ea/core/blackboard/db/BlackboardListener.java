package de.enflexit.ea.core.blackboard.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.blackboard.Blackboard;
import de.enflexit.ea.core.blackboard.Blackboard.BlackboardState;
import de.enflexit.ea.core.blackboard.BlackboardListenerService;
import de.enflexit.ea.core.blackboard.db.dataModel.EdgeResult;
import de.enflexit.ea.core.blackboard.db.dataModel.NetworkState;
import de.enflexit.ea.core.blackboard.db.dataModel.NodeResult;
import de.enflexit.ea.core.blackboard.db.dataModel.TrafoResult;
import de.enflexit.ea.core.dataModel.TransformerHelper;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.ElectricityDomainIdentification;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.blackboard.SubBlackboardModelElectricity;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.HighVoltageUniPhase;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.helper.TechnicalSystemStateHelper;
import energy.helper.UnitConverter;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.UsageOfInterfaceEnergy;

/**
 * The BlackboardListener connects to the {@link Blackboard} of the SimulationManager and thus
 * allows to receive new system states and save them to the database.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BlackboardListener implements BlackboardListenerService {

	private final double sqrtRootThree = Math.sqrt(3.0);
	
	private final double voltageBase = 400;
	private final double voltageBoundaryStepRelative = 0.1;
	private final double voltageBoundaryStep = voltageBase * voltageBoundaryStepRelative;
	private final double voltageBoundaryHigh = voltageBase + voltageBoundaryStep;
	private final double voltageBoundaryLow  = voltageBase - voltageBoundaryStep;
	
	
	private List<SubBlackboardModelElectricity> subBlackboardModelListElectricity;
	
	private Integer idExecution;

	private NetworkModel networkModel;
	
	private List<String> nodeElementList;
	private List<String> edgeElementList;
	private List<String> transformerList;
	
	private DatabaseHandler dbHandler;
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.blackboard.BlackboardListenerService#onSimulationDone()
	 */
	@Override
	public void onSimulationDone() {
		
		// --- Reset local variables ------------
		this.networkModel = null;
		this.idExecution = null;
		
		this.nodeElementList = null;
		this.edgeElementList = null;
		this.transformerList = null;
		
		this.getDatabaseHandler().stopNetworkStateSaveThread();
		this.dbHandler = null;
	}
	
	/**
	 * Returns the current ID scenario result.
	 * @return the ID scenario result
	 */
	private int getIDExecution() {
		if (idExecution==null) {
			idExecution = this.getDatabaseHandler().getNewExecutionID("AWB-Simulator");
		}
		return idExecution; 
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.blackboard.BlackboardListenerService#onNetworkCalculationDone(de.enflexit.ea.core.dataModel.blackboard.Blackboard)
	 */
	@Override
	public void onNetworkCalculationDone(Blackboard blackboard) {

		// ----------------------------------------------------------
		// --- Execute storing procedure? ---------------------------
		// ----------------------------------------------------------
		// --- Check if data has be written to the database ---------
		if (blackboard.getAggregationHandler().getHyGridAbstractEnvironmentModel().isSaveRuntimeInformationToDatabase()==false) return;
		// --- Check it the idExecution was set ----------------
		if (this.getIDExecution()<=0) return;
		// --- Only work on final blackboard states -----------------
		if (blackboard.getBlackboardState()==null || blackboard.getBlackboardState()!=BlackboardState.Final) return;
		
		
		// --- Ensure that all element lists are filled -------------
		this.getNetworkModel();
		
		
		// ----------------------------------------------------------
		// --- Store current network state! -------------------------
		// ----------------------------------------------------------
		// --- Get the state time -----------------------------------
		Calendar stateTime = Calendar.getInstance();
		stateTime.setTimeInMillis(blackboard.getStateTime());
		
		// --- Get the sub blackboard model -------------------------
		AbstractAggregationHandler aggregationHandler = blackboard.getAggregationHandler();

		// --- Write electricity data to DB -------------------------
		this.writeElectricityStatesToDatabase(stateTime, aggregationHandler);
		
		// --- Space for further improvements ;-) -------------------
		
	}
	
	/**
	 * Write electricity states to database.
	 *
	 * @param stateTime the state time
	 * @param aggregationHandler the aggregation handler
	 */
	private void writeElectricityStatesToDatabase(Calendar stateTime, AbstractAggregationHandler aggregationHandler) {
		
		List<SubBlackboardModelElectricity> subBlackboardModelListElectricity = this.getSubBlackboardModelElectricity(aggregationHandler);
		if (subBlackboardModelListElectricity.size()==0) return;
		
		// --- Define state HashMaps --------------------------------
		NodeStateCollector nodeStates = new NodeStateCollector();
		HashMap<String, CableState> cableStates = new HashMap<>();
		HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs = this.getLastTransformerStatesFromBlackboardAggregation(aggregationHandler);

		// --- Get a quick copy of the relevant states --------------
		for (SubBlackboardModelElectricity subBlackboardModel : subBlackboardModelListElectricity) {
			nodeStates.putAll((AbstractElectricalNetworkConfiguration) subBlackboardModel.getSubAggregationConfiguration(), subBlackboardModel.getNodeStates());
			cableStates.putAll(subBlackboardModel.getCableStates());
		}
		
		// --- Prepare and put to database --------------------------
		this.prepareAndPutElectricityStatesToDatabaseInThread(stateTime, nodeStates, cableStates, transformerTSSEs);
	}
	/**
	 * Prepares and puts the specified electricity state data to the database by using a dedicated thread.
	 *
	 * @param stateTime the state time
	 * @param nodeStates the node states
	 * @param cableStates the cable states
	 * @param transformerTSSEs the transformer TSS es
	 */
	private void prepareAndPutElectricityStatesToDatabaseInThread(final Calendar stateTime, final NodeStateCollector nodeStates, final HashMap<String, CableState> cableStates, final HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				BlackboardListener.this.prepareAndPutElectricityStatesToDatabase(stateTime, nodeStates, cableStates, transformerTSSEs);
			}
		}, "DB-Preparation-" + stateTime.getTimeInMillis()).start();
	}
	/**
	 * Prepares and puts the specified electricity state data to the database.
	 *
	 * @param stateTime the state time
	 * @param nodeStates the node states
	 * @param cableStates the cable states
	 * @param transformerTSSEs the transformer TSS es
	 */
	private void prepareAndPutElectricityStatesToDatabase(Calendar stateTime, NodeStateCollector nodeStates, HashMap<String, CableState> cableStates, HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs) {
		
		// --- Create lists to save to database ---------------------
		NetworkState networkState = new NetworkState();
		networkState.setStateTime(stateTime);
		networkState.setNodeResultList(this.getNodeResults(nodeStates, stateTime));
		networkState.setEdgeResultList(this.getEdgeResults(cableStates, stateTime));
		networkState.setTrafoResultList(this.getTrafoResults(nodeStates, transformerTSSEs, stateTime));
		
		// --- Save to database -------------------------------------
		this.getDatabaseHandler().addNetworkStateToSave(networkState);
	}
	
	
	/**
	 * Returns all sub blackboard model for the type electricity.
	 * @param aggregationHandler the aggregation handler
	 * @return the list of sub blackboard model electricity
	 */
	private List<SubBlackboardModelElectricity> getSubBlackboardModelElectricity(AbstractAggregationHandler aggregationHandler) {
		if (subBlackboardModelListElectricity==null) {
			subBlackboardModelListElectricity = new ArrayList<>();

			List<String> domainList = ElectricityDomainIdentification.getDomainList();
			for (String domain : domainList) {
				// --- Get all sub configurations that are of type electricity ----------
				List<AbstractSubNetworkConfiguration> subNetworkConfogurations = aggregationHandler.getSubNetworkConfiguration(domain);
				for (AbstractSubNetworkConfiguration subNetworkConfoguration : subNetworkConfogurations) {
					subBlackboardModelListElectricity.add((SubBlackboardModelElectricity)subNetworkConfoguration.getSubBlackboardModel());
					subNetworkConfoguration.getSubBlackboardModel();
				}
			}
		}
		return subBlackboardModelListElectricity;
		
	}
	/**
	 * Returns the last transformer states from blackboard aggregation.
	 *
	 * @param aggregationHandler the aggregation handler
	 * @return the transformer states from blackboard aggregation
	 */
	private HashMap<String, TechnicalSystemStateEvaluation> getLastTransformerStatesFromBlackboardAggregation(AbstractAggregationHandler aggregationHandler) {
		
		HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs = new HashMap<String, TechnicalSystemStateEvaluation>();
		List<String> transformerIDs = this.getTransformerList();
		for (int i = 0; i < transformerIDs.size(); i++) {
			String transformerID = transformerIDs.get(i);
			TechnicalSystemStateEvaluation transformerTSSE = aggregationHandler.getLastTechnicalSystemStateFromScheduleController(transformerID);
			if (transformerTSSE!=null) {
				transformerTSSEs.put(transformerID, transformerTSSE);
			}
		}
		return transformerTSSEs;
	}
	
	
	/**
	 * Transforms the transformer system state into the DB format {@link TrafoResult}.
	 *
	 * @param nodeStates the node states
	 * @param transformerTSSEs the transformer TSS es
	 * @param calendar the calendar
	 * @return the trafo results
	 */
	private List<TrafoResult> getTrafoResults(NodeStateCollector nodeStates, HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs, Calendar calendar) {
		
		List<TrafoResult> trafoResultList = new ArrayList<>();
		
		List<String> trafoElementList = this.getTransformerList();
		for (int i = 0; i < trafoElementList.size(); i++) {
			
			String trafoID = trafoElementList.get(i);
			String graphNodeID = this.getGraphNodeIDFromNetworkComponentID(trafoID);
			
			TechnicalSystemStateEvaluation tsse = transformerTSSEs.get(trafoID);
			
			// --- Get electrical node states of transformer --------
			HashMap<AbstractElectricalNetworkConfiguration, ElectricalNodeState> elNodeStateHashMap = nodeStates.get(graphNodeID);
			// --- Get list of electrical network configurations ----
			List<AbstractElectricalNetworkConfiguration> elNetworkConfigList = new ArrayList<>(elNodeStateHashMap.keySet());
			// --- Sort by voltage level ----------------------------
			Collections.sort(elNetworkConfigList, new Comparator<AbstractElectricalNetworkConfiguration>() {
				@Override
				public int compare(AbstractElectricalNetworkConfiguration elConfig1, AbstractElectricalNetworkConfiguration elConfig2) {
					Double voltageLeve1 = elConfig1.getConfiguredRatedVoltageFromNetwork();
					Double voltageLeve2 = elConfig2.getConfiguredRatedVoltageFromNetwork();
					return voltageLeve1.compareTo(voltageLeve2);
				}
			});
			// --- Get high and low voltage node state --------------			
			ElectricalNodeState elNodeStateLV = elNetworkConfigList.size()>=1 ? elNodeStateHashMap.get(elNetworkConfigList.get(0)) : null;
			ElectricalNodeState elNodeStateHV = elNetworkConfigList.size()>=2 ? elNodeStateHashMap.get(elNetworkConfigList.get(1)) : null;
			
			
			// ------------------------------------------------------
			// --- Values from system state -------------------------
			// ------------------------------------------------------
			double residualLoadP = 0.0;
			double residualLoadQ = 0.0;
			double trafoUtilization = 0.0;
			double trafoLossesP = 0.0;
			double trafoLossesQ = 0.0;
			Integer tapPosition = null; 

			if (tsse!=null) {
				
				UsageOfInterfaceEnergy uoiHV_P = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), HighVoltageUniPhase.HV_P.getInterfaceID());
				UsageOfInterfaceEnergy uoiHV_Q = (UsageOfInterfaceEnergy) TechnicalSystemStateHelper.getUsageOfInterfaces(tsse.getUsageOfInterfaces(), HighVoltageUniPhase.HV_Q.getInterfaceID());
				EnergyFlowInWatt efiwHV_P = UnitConverter.convertEnergyFlowInWatt(uoiHV_P.getEnergyFlow(), EnergyUnitFactorPrefixSI.NONE_0);
				EnergyFlowInWatt efiwHV_Q = UnitConverter.convertEnergyFlowInWatt(uoiHV_Q.getEnergyFlow(), EnergyUnitFactorPrefixSI.NONE_0);
				
				FixedDouble fdTrafoUtilization = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.tUtil.name());
				FixedDouble fdTrafoLossesP = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.tLossesPAllPhases.name());
				FixedDouble fdTrafoLossesQ = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.tLossesQAllPhases.name());
				FixedInteger fiTapPosition =  (FixedInteger) TechnicalSystemStateHelper.getFixedVariable(tsse.getIOlist(), TransformerSystemVariable.tapPos.name());
				
				residualLoadP = efiwHV_P==null ? 0.0 : efiwHV_P.getValue();
				residualLoadQ = efiwHV_Q==null ? 0.0 : efiwHV_Q.getValue();
				trafoUtilization = fdTrafoUtilization==null ? 0.0 : fdTrafoUtilization.getValue();
				trafoLossesP = fdTrafoLossesP==null ? 0.0 : fdTrafoLossesP.getValue();
				trafoLossesQ = fdTrafoLossesQ==null ? 0.0 : fdTrafoLossesQ.getValue();
				tapPosition = fiTapPosition==null ? null : fiTapPosition.getValue();
			}
			
			
			// ------------------------------------------------------
			// --- Create TrafoResult -------------------------------
			// ------------------------------------------------------
			TrafoResult trafoResult = new TrafoResult();
			trafoResult.setIdExecution(this.getIDExecution());
			trafoResult.setIdTrafo(trafoID);
			trafoResult.setTimestamp(calendar);

			// ------------------------------------------------------
			// --- Work on the high voltage node state --------------
			// ------------------------------------------------------
			if (elNodeStateHV instanceof UniPhaseElectricalNodeState) {
				
				UniPhaseElectricalNodeState hvUpens = (UniPhaseElectricalNodeState) elNodeStateHV;
				
				trafoResult.setHvVoltageAllReal(hvUpens.getVoltageRealNotNull().getValue());
				trafoResult.setHvVoltageAllImag(hvUpens.getVoltageImagNotNull().getValue());
				trafoResult.setHvVoltageAllAbs(hvUpens.getVoltageAbsNotNull().getValue());
				trafoResult.setHvCurrentAll(hvUpens.getCurrentNotNull().getValue());
				
				trafoResult.setHvCosPhiAll(hvUpens.getCosPhi());
				trafoResult.setHvPowerPAll(hvUpens.getPNotNull().getValue());
				trafoResult.setHvPowerQAll(hvUpens.getQNotNull().getValue());

			} else if (elNodeStateHV instanceof TriPhaseElectricalNodeState) {
				
				TriPhaseElectricalNodeState hvTpens = (TriPhaseElectricalNodeState) elNodeStateHV;
				UniPhaseElectricalNodeState hvUpensL1 = hvTpens.getL1();
				UniPhaseElectricalNodeState hvUpensL2 = hvTpens.getL2();
				UniPhaseElectricalNodeState hvUpensL3 = hvTpens.getL3();
				
				trafoResult.setHvVoltageL1Real(hvUpensL1==null ? 0 : hvUpensL1.getVoltageRealNotNull().getValue());
				trafoResult.setHvVoltageL1Imag(hvUpensL1==null ? 0 : hvUpensL1.getVoltageImagNotNull().getValue());
				trafoResult.setHvVoltageL1Abs( hvUpensL1==null ? 0 : hvUpensL1.getVoltageAbsNotNull().getValue());
				
				trafoResult.setHvVoltageL2Real(hvUpensL2==null ? 0 : hvUpensL2.getVoltageRealNotNull().getValue());
				trafoResult.setHvVoltageL2Imag(hvUpensL2==null ? 0 : hvUpensL2.getVoltageImagNotNull().getValue());
				trafoResult.setHvVoltageL2Abs( hvUpensL2==null ? 0 : hvUpensL2.getVoltageAbsNotNull().getValue());
				
				trafoResult.setHvVoltageL3Real(hvUpensL3==null ? 0 : hvUpensL3.getVoltageRealNotNull().getValue());
				trafoResult.setHvVoltageL3Imag(hvUpensL3==null ? 0 : hvUpensL3.getVoltageImagNotNull().getValue());
				trafoResult.setHvVoltageL3Abs( hvUpensL3==null ? 0 : hvUpensL3.getVoltageAbsNotNull().getValue());
				
				trafoResult.setHvCurrentL1(hvUpensL1==null ? 0 : hvUpensL1.getCurrentNotNull().getValue());
				trafoResult.setHvCurrentL2(hvUpensL2==null ? 0 : hvUpensL2.getCurrentNotNull().getValue());
				trafoResult.setHvCurrentL3(hvUpensL3==null ? 0 : hvUpensL3.getCurrentNotNull().getValue());
				
				trafoResult.setHvCosPhiL1(hvUpensL1==null ? 0 : hvUpensL1.getCosPhi());
				trafoResult.setHvCosPhiL2(hvUpensL2==null ? 0 : hvUpensL2.getCosPhi());
				trafoResult.setHvCosPhiL3(hvUpensL3==null ? 0 : hvUpensL3.getCosPhi());
				
				trafoResult.setHvPowerP1(hvUpensL1==null ? 0 : hvUpensL1.getPNotNull().getValue());
				trafoResult.setHvPowerQ1(hvUpensL1==null ? 0 : hvUpensL1.getQNotNull().getValue());
				trafoResult.setHvPowerP2(hvUpensL2==null ? 0 : hvUpensL2.getPNotNull().getValue());
				trafoResult.setHvPowerQ2(hvUpensL2==null ? 0 : hvUpensL2.getQNotNull().getValue());
				trafoResult.setHvPowerP3(hvUpensL3==null ? 0 : hvUpensL3.getPNotNull().getValue());
				trafoResult.setHvPowerQ3(hvUpensL3==null ? 0 : hvUpensL3.getQNotNull().getValue());
				
			}
			
			// ------------------------------------------------------
			// --- Work on the low voltage node state ---------------
			// ------------------------------------------------------
			double voltageRealAllPhases = 0;
			double voltageComplexAllPhases = 0;
			int voltageViolation = 0;
			
			if (elNodeStateLV instanceof UniPhaseElectricalNodeState) {
				
				UniPhaseElectricalNodeState lvUpens = (UniPhaseElectricalNodeState) elNodeStateLV;
				trafoResult.setLvVoltageAllReal(lvUpens.getVoltageRealNotNull().getValue());
				trafoResult.setLvVoltageAllImag(lvUpens.getVoltageImagNotNull().getValue());
				trafoResult.setLvVoltageAllAbs(lvUpens.getVoltageAbsNotNull().getValue());
				trafoResult.setLvCurrentAll(lvUpens.getCurrentNotNull().getValue());
				
				trafoResult.setLvCosPhiAll(lvUpens.getCosPhi());
				trafoResult.setLvPowerPAll(lvUpens.getPNotNull().getValue());
				trafoResult.setLvPowerQAll(lvUpens.getQNotNull().getValue());
				
				
				voltageRealAllPhases = lvUpens.getVoltageRealNotNull().getValue();
				voltageComplexAllPhases = lvUpens.getVoltageRealNotNull().getValue();
				voltageViolation = this.getVoltageViolation(voltageRealAllPhases, voltageComplexAllPhases);
				
			} else if (elNodeStateLV instanceof TriPhaseElectricalNodeState) {
				
				TriPhaseElectricalNodeState lvTpens = (TriPhaseElectricalNodeState) elNodeStateLV;
				
				// --- Values from electrical LV node state -------------
				UniPhaseElectricalNodeState lvUpensL1 = lvTpens.getL1();
				UniPhaseElectricalNodeState lvUpensL2 = lvTpens.getL2();
				UniPhaseElectricalNodeState lvUpensL3 = lvTpens.getL3();
					
				trafoResult.setLvVoltageL1Real(lvUpensL1==null ? 0 : lvUpensL1.getVoltageRealNotNull().getValue());
				trafoResult.setLvVoltageL1Imag(lvUpensL1==null ? 0 : lvUpensL1.getVoltageImagNotNull().getValue());
				trafoResult.setLvVoltageL1Abs( lvUpensL1==null ? 0 : lvUpensL1.getVoltageAbsNotNull().getValue());
				
				trafoResult.setLvVoltageL2Real(lvUpensL2==null ? 0 : lvUpensL2.getVoltageRealNotNull().getValue());
				trafoResult.setLvVoltageL2Imag(lvUpensL2==null ? 0 : lvUpensL2.getVoltageImagNotNull().getValue());
				trafoResult.setLvVoltageL2Abs( lvUpensL2==null ? 0 : lvUpensL2.getVoltageAbsNotNull().getValue());
				
				trafoResult.setLvVoltageL3Real(lvUpensL3==null ? 0 : lvUpensL3.getVoltageRealNotNull().getValue());
				trafoResult.setLvVoltageL3Imag(lvUpensL3==null ? 0 : lvUpensL3.getVoltageImagNotNull().getValue());
				trafoResult.setLvVoltageL3Abs( lvUpensL3==null ? 0 : lvUpensL3.getVoltageAbsNotNull().getValue());
				
				trafoResult.setLvCurrentL1(lvUpensL1==null ? 0 : lvUpensL1.getCurrentNotNull().getValue());
				trafoResult.setLvCurrentL2(lvUpensL2==null ? 0 : lvUpensL2.getCurrentNotNull().getValue());
				trafoResult.setLvCurrentL3(lvUpensL3==null ? 0 : lvUpensL3.getCurrentNotNull().getValue());
				
				trafoResult.setLvCosPhiL1(lvUpensL1==null ? 0 : lvUpensL1.getCosPhi());
				trafoResult.setLvCosPhiL2(lvUpensL2==null ? 0 : lvUpensL2.getCosPhi());
				trafoResult.setLvCosPhiL3(lvUpensL3==null ? 0 : lvUpensL3.getCosPhi());
				
				trafoResult.setLvPowerP1(lvUpensL1==null ? 0 : lvUpensL1.getPNotNull().getValue());
				trafoResult.setLvPowerQ1(lvUpensL1==null ? 0 : lvUpensL1.getQNotNull().getValue());
				trafoResult.setLvPowerP2(lvUpensL2==null ? 0 : lvUpensL2.getPNotNull().getValue());
				trafoResult.setLvPowerQ2(lvUpensL2==null ? 0 : lvUpensL2.getQNotNull().getValue());
				trafoResult.setLvPowerP3(lvUpensL3==null ? 0 : lvUpensL3.getPNotNull().getValue());
				trafoResult.setLvPowerQ3(lvUpensL3==null ? 0 : lvUpensL3.getQNotNull().getValue());

				
				voltageRealAllPhases = this.getVoltageRealAllPhases(lvUpensL1);
				voltageComplexAllPhases = this.getVoltageComplexAllPhases(lvUpensL1);
				voltageViolation = this.getVoltageViolation(voltageRealAllPhases, voltageComplexAllPhases);
			}
			
			trafoResult.setVoltageReal(voltageRealAllPhases);
			trafoResult.setVoltageImag(voltageComplexAllPhases);
			trafoResult.setVoltageViolations(voltageViolation);
			
			
			trafoResult.setResidualLoadP(residualLoadP);
			trafoResult.setResidualLoadQ(residualLoadQ);
			trafoResult.setTrafoUtilization(trafoUtilization);
			trafoResult.setTrafoLossesP(trafoLossesP);
			trafoResult.setTrafoLossesQ(trafoLossesQ);
			
			trafoResult.setTapPos(tapPosition==null ? -9999 : tapPosition);
			
			// --- Add to list ------------------------
			trafoResultList.add(trafoResult);
		}
		return trafoResultList;
	}
	
	/**
	 * Return the GraphNode ID from the specified NetworkCompont ID.
	 *
	 * @param netCompID the network component ID
	 * @return the node state ID
	 */
	private String getGraphNodeIDFromNetworkComponentID(String netCompID) {
		NetworkComponent netComp = this.getNetworkModel().getNetworkComponent(netCompID);
		GraphNode graphNode = this.getNetworkModel().getGraphNodeFromDistributionNode(netComp);
		if (graphNode!=null) {
			return graphNode.getId();
		}
		return null;
	}
	
	/**
	 * Transforms the CableState results into the database format {@link EdgeResult}.
	 *
	 * @param edgeStates the edge states
	 * @param calendar the calendar
	 * @return the edge results
	 */
	private List<EdgeResult> getEdgeResults(HashMap<String, CableState> edgeStates, Calendar calendar) {
		
		List<EdgeResult> edgeResultList = new ArrayList<>();
		
		List<String> edgeElementList = this.getEdgeElementList();
		for (int i = 0; i < edgeElementList.size(); i++) {
			String edgeID = edgeElementList.get(i);
			CableState cableState = edgeStates.get(edgeID);
			
			if (cableState!=null) {
				// --- Create EdgeResult --------------------------------------
				EdgeResult edgeResult = new EdgeResult();
				edgeResult.setIdExecution(this.getIDExecution());
				edgeResult.setIdEdge(edgeID);
				edgeResult.setTimestamp(calendar);

				if (cableState instanceof UniPhaseCableState) {
					// --- => UniPhaseCableState ------------------------------
					UniPhaseCableState upcs = (UniPhaseCableState) cableState;
					edgeResult.setCurrentReal(upcs.getCurrentReal().getValue());
					edgeResult.setCurrentImag(upcs.getCurrentImag().getValue());
					edgeResult.setCurrentAbs(upcs.getCurrent().getValue());
					edgeResult.setCosPhi(upcs.getCosPhi());
					edgeResult.setLossesP(upcs.getLossesP().getValue());
					edgeResult.setLossesQ(upcs.getLossesQ().getValue());
					edgeResult.setPowerP(upcs.getP().getValue());
					edgeResult.setPowerQ(upcs.getQ().getValue());
					edgeResult.setUtilization(upcs.getUtilization());
					
				} else if (cableState instanceof TriPhaseCableState) {
					// --- => TriPhaseCableState ------------------------------
					TriPhaseCableState tpcs = (TriPhaseCableState) cableState;
					UniPhaseCableState upcsL1 = tpcs.getPhase1();
					UniPhaseCableState upcsL2 = tpcs.getPhase2();
					UniPhaseCableState upcsL3 = tpcs.getPhase3();
					
					edgeResult.setCurrentL1Real(upcsL1.getCurrentReal().getValue());
					edgeResult.setCurrentL1Imag(upcsL1.getCurrentImag().getValue());
					edgeResult.setCurrentL1Abs(upcsL1.getCurrent().getValue());
					edgeResult.setCosPhiL1(upcsL1.getCosPhi());
					edgeResult.setLossesL1P(upcsL1.getLossesP().getValue());
					edgeResult.setLossesL1Q(upcsL1.getLossesQ().getValue());
					edgeResult.setPowerP1(upcsL1.getP().getValue());
					edgeResult.setPowerQ1(upcsL1.getQ().getValue());

					edgeResult.setCurrentL2Real(upcsL2.getCurrentReal().getValue());
					edgeResult.setCurrentL2Imag(upcsL2.getCurrentImag().getValue());
					edgeResult.setCurrentL2Abs(upcsL2.getCurrent().getValue());
					edgeResult.setCosPhiL2(upcsL2.getCosPhi());
					edgeResult.setLossesL2P(upcsL2.getLossesP().getValue());
					edgeResult.setLossesL2Q(upcsL2.getLossesQ().getValue());
					edgeResult.setPowerP2(upcsL2.getP().getValue());
					edgeResult.setPowerQ2(upcsL2.getQ().getValue());

					edgeResult.setCurrentL3Real(upcsL3.getCurrentReal().getValue());
					edgeResult.setCurrentL3Imag(upcsL3.getCurrentImag().getValue());
					edgeResult.setCurrentL3Abs(upcsL3.getCurrent().getValue());
					edgeResult.setCosPhiL3(upcsL3.getCosPhi());
					edgeResult.setLossesL3P(upcsL3.getLossesP().getValue());
					edgeResult.setLossesL3Q(upcsL3.getLossesQ().getValue());
					edgeResult.setPowerP3(upcsL3.getP().getValue());
					edgeResult.setPowerQ3(upcsL3.getQ().getValue());
					
					double iReal = upcsL1.getCurrentReal().getValue() + upcsL2.getCurrentReal().getValue() + upcsL3.getCurrentReal().getValue();
					double iImag = upcsL1.getCurrentImag().getValue() + upcsL2.getCurrentImag().getValue() + upcsL3.getCurrentImag().getValue();
					double iAbs  = Math.sqrt(Math.pow(iReal, 2) + Math.pow(iImag, 2));
					
					double dP = upcsL1.getLossesP().getValue() + upcsL2.getLossesP().getValue() + upcsL3.getLossesP().getValue();
					double dQ = upcsL1.getLossesQ().getValue() + upcsL2.getLossesQ().getValue() + upcsL3.getLossesQ().getValue();
					
					double powerP = upcsL1.getP().getValue() + upcsL2.getP().getValue() + upcsL3.getP().getValue();
					double powerQ = upcsL1.getQ().getValue() + upcsL2.getQ().getValue() + upcsL3.getQ().getValue();

					edgeResult.setCurrentReal(iReal);
					edgeResult.setCurrentImag(iImag);
					edgeResult.setCurrentAbs(iAbs);
					edgeResult.setLossesP(dP);
					edgeResult.setLossesQ(dQ);
					edgeResult.setPowerP(powerP);
					edgeResult.setPowerQ(powerQ);
					edgeResult.setUtilization(upcsL1.getUtilization());
					
				}
				// --- Add to list ----------------------------------
				edgeResultList.add(edgeResult);
			}
			
		}
		return edgeResultList;
	}
	
	/**
	 * Transforms the ElectricalNodeState results into the database format {@link NodeResult}.
	 *
	 * @param graphNodeStates the graph node states
	 * @param calendar the calendar
	 * @return the node results
	 */
	private List<NodeResult> getNodeResults(NodeStateCollector graphNodeStates, Calendar calendar) {
		
		List<NodeResult> nodeResultList = new ArrayList<>();
		
		List<String> nodeElementList = this.getNodeElementList();
		for (int i = 0; i < nodeElementList.size(); i++) {

			// --- Get the ID reminded ---------------------------------------- 
			String remindedID = nodeElementList.get(i); // --- may be a NetworkComponent ID ---
			HashMap<AbstractElectricalNetworkConfiguration, ElectricalNodeState> elNodeStateHashMap = graphNodeStates.get(remindedID);
			if (elNodeStateHashMap==null) {
				String graphNodeID = this.getGraphNodeIDFromNetworkComponentID(remindedID);
				if (graphNodeID!=null) {
					elNodeStateHashMap = graphNodeStates.get(graphNodeID);
				}
			}
			
			for (AbstractElectricalNetworkConfiguration elNetworkConfig : elNodeStateHashMap.keySet()) {
				
				// --- Get the ElectricalNodeState for this 
				ElectricalNodeState elNodeState = elNodeStateHashMap.get(elNetworkConfig);
				if (elNodeState!=null) {
					String idNode = remindedID;
					if (elNodeStateHashMap.size()>1) {
						idNode += "-" + elNetworkConfig.getConfiguredRatedVoltageFromNetwork();
					}
					
					// --- Create NodeResult ----------------------------------
					NodeResult nodeResult = new NodeResult();
					nodeResult.setIdExecution(this.getIDExecution());
					nodeResult.setIdNode(idNode);
					nodeResult.setTimestamp(calendar);
					
					// --- Get the electrical node state ----------------------
					if (elNodeState instanceof UniPhaseElectricalNodeState) {
						// --- => UniPhaseElectricalNodeState -----------------
						UniPhaseElectricalNodeState upens = (UniPhaseElectricalNodeState) elNodeState;
						
						nodeResult.setVoltageReal(upens.getVoltageRealNotNull().getValue());
						nodeResult.setVoltageImag(upens.getVoltageImagNotNull().getValue());
						nodeResult.setVoltageAbs(upens.getVoltageAbsNotNull().getValue());
						
						nodeResult.setCurrent(upens.getCurrent().getValue());
						nodeResult.setCosPhi(upens.getCosPhi());
						
						nodeResult.setPowerP(upens.getPCalculated());
						nodeResult.setPowerQ(upens.getQCalculated());
						
						int voltageViolation = this.getVoltageViolation(nodeResult.getVoltageReal(), nodeResult.getVoltageImag());
						nodeResult.setVoltageViolations(voltageViolation);
						
						
					} else if (elNodeState instanceof TriPhaseElectricalNodeState) {
						// --- => TriPhaseElectricalNodeState -----------------
						TriPhaseElectricalNodeState tens = (TriPhaseElectricalNodeState) elNodeState;
						UniPhaseElectricalNodeState upensL1 = tens.getL1();
						UniPhaseElectricalNodeState upensL2 = tens.getL2();
						UniPhaseElectricalNodeState upensL3 = tens.getL3();
						
						double voltageReal = this.getVoltageRealAllPhases(upensL1);
						double voltageComplex = this.getVoltageComplexAllPhases(upensL1);
						
						nodeResult.setVoltageL1Real(upensL1.getVoltageRealNotNull().getValue());
						nodeResult.setVoltageL1Imag(upensL1.getVoltageImagNotNull().getValue());
						nodeResult.setVoltageL1Abs(upensL1.getVoltageAbsNotNull().getValue());
						
						nodeResult.setVoltageL2Real(upensL2.getVoltageRealNotNull().getValue());
						nodeResult.setVoltageL2Imag(upensL2.getVoltageImagNotNull().getValue());
						nodeResult.setVoltageL2Abs(upensL2.getVoltageAbsNotNull().getValue());
						
						nodeResult.setVoltageL3Real(upensL3.getVoltageRealNotNull().getValue());
						nodeResult.setVoltageL3Imag(upensL3.getVoltageImagNotNull().getValue());
						nodeResult.setVoltageL3Abs(upensL3.getVoltageAbsNotNull().getValue());
						
						nodeResult.setVoltageReal(voltageReal);
						nodeResult.setVoltageImag(voltageComplex);
						
						nodeResult.setCurrentL1(upensL1.getCurrent().getValue());
						nodeResult.setCurrentL2(upensL2.getCurrent().getValue());
						nodeResult.setCurrentL3(upensL3.getCurrent().getValue());
						
						nodeResult.setCosPhiL1(upensL1.getCosPhi());
						nodeResult.setCosPhiL2(upensL2.getCosPhi());
						nodeResult.setCosPhiL3(upensL3.getCosPhi());
						
						nodeResult.setPowerP1(upensL1.getPCalculated());
						nodeResult.setPowerP2(upensL2.getPCalculated());
						nodeResult.setPowerP3(upensL3.getPCalculated());
						
						nodeResult.setPowerQ1(upensL1.getQCalculated());
						nodeResult.setPowerQ2(upensL2.getQCalculated());
						nodeResult.setPowerQ3(upensL3.getQCalculated());
						
						nodeResult.setPowerP(upensL1.getPCalculated() + upensL2.getPCalculated() + upensL3.getPCalculated());
						nodeResult.setPowerQ(upensL1.getQCalculated() + upensL2.getQCalculated() + upensL3.getQCalculated());
						
						
						int voltageViolation = this.getVoltageViolation(voltageReal, voltageComplex);
						nodeResult.setVoltageViolations(voltageViolation);
					}
					// --- Add to list ----------------------------------------
					nodeResultList.add(nodeResult);
				}
				
			} // end for sub aggregation 
			
		} // --- end for ---
		return nodeResultList;
	}
	
	
	/**
	 * Returns the voltage real from the specified UniPhaseElectricalNodeState.
	 *
	 * @param upens the UniPhaseElectricalNodeState
	 * @return the voltage real
	 */
	private double getVoltageRealAllPhases(UniPhaseElectricalNodeState upens) {
		return ((double)upens.getVoltageReal().getValue()) * this.sqrtRootThree;
	}
	/**
	 * Returns the complex voltage from the specified UniPhaseElectricalNodeState.
	 *
	 * @param upens the UniPhaseElectricalNodeState
	 * @return the complex voltage 
	 */
	private double getVoltageComplexAllPhases(UniPhaseElectricalNodeState upens) {
		return ((double)upens.getVoltageImag().getValue()) * this.sqrtRootThree;
	}
	
	/**
	 * Returns the indicator for voltage violations.
	 *
	 * @param voltageReal the real voltage real
	 * @param voltageComplex the complex voltage 
	 * @return the voltage violation
	 */
	private int getVoltageViolation(double voltageReal, double voltageComplex) {
		double voltageAbs = Math.sqrt(voltageReal*voltageReal + voltageComplex*voltageComplex);
		if (voltageAbs>=this.voltageBoundaryHigh) {
			return 1;
		} else if (voltageAbs<=this.voltageBoundaryLow) {
			return 1;
		}
		return 0;
	}
	
	
	private List<String> getNodeElementList() {
		if (nodeElementList==null) {
			nodeElementList = new ArrayList<>();
		}
		return nodeElementList;
	}
	private List<String> getEdgeElementList() {
		if (edgeElementList==null) {
			edgeElementList = new ArrayList<>();
		}
		return edgeElementList;
	}
	private List<String> getTransformerList() {
		if (transformerList==null) {
			transformerList = new ArrayList<>();
		}
		return transformerList;
	}
	
	private NetworkModel getNetworkModel() {
		if (networkModel==null) {
			networkModel = Blackboard.getInstance().getNetworkModel();
			this.fillResultIDs();
		}
		return networkModel;
	}
	
	/**
	 * Evaluate the NetworkModel for the IDs of the results.
	 */
	private void fillResultIDs() {
		
		if (this.nodeElementList==null || this.nodeElementList==null || this.transformerList==null) {
		
			// --- Define the NetworkComponets if interest here -----
			HashMap<String, List<String>> compTypeToElementListHashMap = new HashMap<>();
			compTypeToElementListHashMap.put("Prosumer".toLowerCase(), this.getNodeElementList());
			compTypeToElementListHashMap.put("CableCabinet".toLowerCase(), this.getNodeElementList());
			
			compTypeToElementListHashMap.put("Cable".toLowerCase(), this.getEdgeElementList());
			compTypeToElementListHashMap.put("Sensor".toLowerCase(), this.getEdgeElementList());
			compTypeToElementListHashMap.put("Breaker".toLowerCase(), this.getEdgeElementList());
			
			// --- Extract a list of search Phrases -----------------
			List<String> searchPhrases = new ArrayList<>(compTypeToElementListHashMap.keySet());

			
			// --- Check all NetworkComponets -----------------------
			Vector<NetworkComponent> netCompVector = this.getNetworkModel().getNetworkComponentVectorSorted();
			for (int i = 0; i < netCompVector.size(); i++) {

				NetworkComponent netComp = netCompVector.get(i);
				if (TransformerHelper.isTransformer(netComp.getType())==true) {
					// --- For Transformer ----------------
					this.getTransformerList().add(netComp.getId());
					this.getNodeElementList().add(this.getGraphNodeIDFromNetworkComponentID(netComp.getId()));
					
				} else {
					// --- Check for search phrases -------
					String netCompType = netComp.getType().toLowerCase();
					for (String searchPhrase : searchPhrases) {
						if (netCompType.contains(searchPhrase)==true) {
							compTypeToElementListHashMap.get(searchPhrase).add(netComp.getId());
							break;
						}
					}
				}
			} // end for
		}
	}

	/**
	 * Returns the {@link DatabaseHandler} that stores the {@link NetworkState}s in the database.
	 * @return the database handler
	 */
	private DatabaseHandler getDatabaseHandler() {
		if (dbHandler==null) {
			dbHandler = new DatabaseHandler();
			dbHandler.startNetworkStateSaveThread();
		}
		return dbHandler;
	}
	
	/**
	 * The Class NodeStateCollector.
	 *
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	private class NodeStateCollector {
		
		private HashMap<AbstractElectricalNetworkConfiguration, HashMap<String, ElectricalNodeState>> subAggregationToNodeStateHash;
		
		/**
		 * Returns the sub aggregation to node state hash.
		 * @return the sub aggregation to node state hash
		 */
		private HashMap<AbstractElectricalNetworkConfiguration, HashMap<String, ElectricalNodeState>> getSubAggregationToNodeStateHash() {
			if (subAggregationToNodeStateHash==null) {
				subAggregationToNodeStateHash = new HashMap<>();
			}
			return subAggregationToNodeStateHash;
		}
		
		/**
		 * Puts all node states of the specified sub aggregation into the NodeStateCollector.
		 *
		 * @param elNetWorkConfig the el net work config
		 * @param nodeStates the node states
		 */
		public void putAll(AbstractElectricalNetworkConfiguration elNetWorkConfig, HashMap<String, ElectricalNodeState> nodeStates) {
			this.getSubAggregationToNodeStateHash().put(elNetWorkConfig, nodeStates);
		}
		/**
		 * Returns the {@link ElectricalNodeState}s for the specified node ID as HashMap.
		 *
		 * @param nodeID the node ID
		 * @return the list
		 */
		public HashMap<AbstractElectricalNetworkConfiguration, ElectricalNodeState> get(String nodeID) {
			
			HashMap<AbstractElectricalNetworkConfiguration, ElectricalNodeState> graphNodeStateHashMap = new HashMap<>();
			for (AbstractElectricalNetworkConfiguration elNetWorkConfig : this.getSubAggregationToNodeStateHash().keySet()) {
				// --- Get state HashMap for sub aggregation --------
				HashMap<String, ElectricalNodeState> subNodeStateHashMap = this.getSubAggregationToNodeStateHash().get(elNetWorkConfig);
				if (subNodeStateHashMap!=null) {
					ElectricalNodeState nodeState = subNodeStateHashMap.get(nodeID);
					if (nodeState!=null) {
						graphNodeStateHashMap.put(elNetWorkConfig, nodeState);
					}
				}
			}
			return graphNodeStateHashMap;
		}
		
	} // end sub class
	
}
