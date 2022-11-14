package de.enflexit.ea.core.blackboard.db;

import java.util.ArrayList;
import java.util.Calendar;
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
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.aggregation.triPhase.SubNetworkConfigurationElectricalDistributionGrids;
import de.enflexit.ea.electricity.blackboard.SubBlackboardModelElectricity;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.HighVoltageUniPhase;
import de.enflexit.ea.electricity.transformer.eomDataModel.TransformerDataModel.TransformerSystemVariable;
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
		SubBlackboardModelElectricity subBlackboardModel = this.getSubBlackboardModelElectricity(aggregationHandler);
		if (subBlackboardModel!=null) {
			// --- Get a quick copy of the relevant states ----------
			HashMap<String, ElectricalNodeState> nodeStates = new HashMap<>(subBlackboardModel.getNodeStates());
			HashMap<String, CableState> cableStates = new HashMap<>(subBlackboardModel.getCableStates());
			HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs = this.getLastTransformerStatesFromBlackboardAggregation(aggregationHandler);
			
			// --- Create lists to save to database -----------------
			NetworkState networkState = new NetworkState();
			networkState.setStateTime(stateTime);
			networkState.setNodeResultList(this.getNodeResults(nodeStates, stateTime));
			networkState.setEdgeResultList(this.getEdgeResults(cableStates, stateTime));
			networkState.setTrafoResultList(this.getTrafoResults(nodeStates, transformerTSSEs, stateTime));
			
			// --- Save to database ---------------------------------
			this.getDatabaseHandler().addNetworkStateToSave(networkState);
			
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] No SubBlackboardModel found for " + SubNetworkConfigurationElectricalDistributionGrids.SUBNET_DESCRIPTION_ELECTRICAL_DISTRIBUTION_GRIDS);
		}
	}
	
	/**
	 * Gets the sub blackboard model electricity.
	 * @param aggregationHandler the aggregation handler
	 * @return the sub blackboard model electricity
	 */
	private SubBlackboardModelElectricity getSubBlackboardModelElectricity(AbstractAggregationHandler aggregationHandler) {

		// TODO what if there are several aggregations of the same kind?
		List<AbstractSubNetworkConfiguration> subNetworkConfogurations = aggregationHandler.getSubNetworkConfiguration(SubNetworkConfigurationElectricalDistributionGrids.SUBNET_DESCRIPTION_ELECTRICAL_DISTRIBUTION_GRIDS);
		if (subNetworkConfogurations.size()>0) {
			return (SubBlackboardModelElectricity) subNetworkConfogurations.get(0).getSubBlackboardModel();
		} else {
			return null;
		}
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
	private List<TrafoResult> getTrafoResults(HashMap<String, ElectricalNodeState> nodeStates, HashMap<String, TechnicalSystemStateEvaluation> transformerTSSEs, Calendar calendar) {
		
		List<TrafoResult> trafoResultList = new ArrayList<>();
		
		List<String> trafoElementList = this.getTransformerList();
		for (int i = 0; i < trafoElementList.size(); i++) {
			
			String trafoID = trafoElementList.get(i);
			String graphNodeID = this.getGraphNodeID(trafoID);
			
			ElectricalNodeState elNodeState = nodeStates.get(graphNodeID);
			TechnicalSystemStateEvaluation tsse = transformerTSSEs.get(trafoID);

			// --- Values from electrical node state ----------------
			UniPhaseElectricalNodeState upensL1 = null;
			UniPhaseElectricalNodeState upensL2 = null;
			UniPhaseElectricalNodeState upensL3 = null;
			
			double voltageReal = 0;
			double voltageComplex = 0;
			int voltageViolation = 0;

			if (elNodeState instanceof TriPhaseElectricalNodeState) {
				
				TriPhaseElectricalNodeState tens = (TriPhaseElectricalNodeState) elNodeState;
				upensL1 = tens.getL1();
				upensL2 = tens.getL2();
				upensL3 = tens.getL3();
				
				voltageReal = this.getVoltageReal(upensL1);
				voltageComplex = this.getVoltageComplex(upensL1);
				voltageViolation = this.getVoltageViolation(voltageReal, voltageComplex);
			}
			
			// --- Values from system state -------------------------
			double residualLoadP = 0.0;
			double residualLoadQ = 0.0;
			double trafoUtilization = 0.0;
			double trafoLossesP = 0.0;
			double trafoLossesQ = 0.0;
			Integer tapPosition = null; 

			if (tsse!=null) {
				// System.out.println(TechnicalSystemStateHelper.toString(tsse, true));
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
			
			// --- Create TrafoResult -------------------------------
			TrafoResult trafoResult = new TrafoResult();
			trafoResult.setIdExecution(this.getIDExecution());
			trafoResult.setIdTrafo(trafoID);
			trafoResult.setTimestamp(calendar);

			
			trafoResult.setLvVoltageL1Real(upensL1==null ? 0 : upensL1.getVoltageRealNotNull().getValue());
			trafoResult.setLvVoltageL1Imag(upensL1==null ? 0 : upensL1.getVoltageImagNotNull().getValue());
			trafoResult.setLvVoltageL1Abs( upensL1==null ? 0 : upensL1.getVoltageAbsNotNull().getValue());
			
			trafoResult.setLvVoltageL2Real(upensL2==null ? 0 : upensL2.getVoltageRealNotNull().getValue());
			trafoResult.setLvVoltageL2Imag(upensL2==null ? 0 : upensL2.getVoltageImagNotNull().getValue());
			trafoResult.setLvVoltageL2Abs( upensL2==null ? 0 : upensL2.getVoltageAbsNotNull().getValue());
			
			trafoResult.setLvVoltageL3Real(upensL3==null ? 0 : upensL3.getVoltageRealNotNull().getValue());
			trafoResult.setLvVoltageL3Imag(upensL3==null ? 0 : upensL3.getVoltageImagNotNull().getValue());
			trafoResult.setLvVoltageL3Abs( upensL3==null ? 0 : upensL3.getVoltageAbsNotNull().getValue());
			
			trafoResult.setLvCurrentL1(upensL1==null ? 0 : upensL1.getCurrentNotNull().getValue());
			trafoResult.setLvCurrentL2(upensL2==null ? 0 : upensL2.getCurrentNotNull().getValue());
			trafoResult.setLvCurrentL3(upensL3==null ? 0 : upensL3.getCurrentNotNull().getValue());
			
			trafoResult.setLvCosPhiL1(upensL1==null ? 0 : upensL1.getCosPhi());
			trafoResult.setLvCosPhiL2(upensL2==null ? 0 : upensL2.getCosPhi());
			trafoResult.setLvCosPhiL3(upensL3==null ? 0 : upensL3.getCosPhi());
			
			trafoResult.setLvPowerP1(upensL1==null ? 0 : upensL1.getPNotNull().getValue());
			trafoResult.setLvPowerQ1(upensL1==null ? 0 : upensL1.getQNotNull().getValue());
			trafoResult.setLvPowerP2(upensL2==null ? 0 : upensL2.getPNotNull().getValue());
			trafoResult.setLvPowerQ2(upensL2==null ? 0 : upensL2.getQNotNull().getValue());
			trafoResult.setLvPowerP3(upensL3==null ? 0 : upensL3.getPNotNull().getValue());
			trafoResult.setLvPowerQ3(upensL3==null ? 0 : upensL3.getQNotNull().getValue());

			
			
			trafoResult.setVoltageReal(voltageReal);
			trafoResult.setVoltageImag(voltageComplex);
			trafoResult.setVoltageViolations(voltageViolation);
			
			trafoResult.setResidualLoadP(residualLoadP);
			trafoResult.setResidualLoadQ(residualLoadQ);
			trafoResult.setTrafoUtilization(trafoUtilization);
			trafoResult.setTrafoLossesP(trafoLossesP);
			trafoResult.setTrafoLossesQ(trafoLossesQ);
			
			trafoResult.setTapPos(tapPosition);
			
			// --- Add to list ------------------------
			trafoResultList.add(trafoResult);
		}
		return trafoResultList;
	}
	
	/**
	 * Return the GraphNode ID from the specified NetworkCompont ID.
	 *
	 * @param transformerID the transformer ID
	 * @return the node state ID
	 */
	private String getGraphNodeID(String transformerID) {
		NetworkComponent netComp = this.getNetworkModel().getNetworkComponent(transformerID);
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
	private List<NodeResult> getNodeResults(HashMap<String, ElectricalNodeState> graphNodeStates, Calendar calendar) {
		
		List<NodeResult> nodeResultList = new ArrayList<>();
		
		List<String> nodeElementList = this.getNodeElementList();
		for (int i = 0; i < nodeElementList.size(); i++) {

			// --- Get the ID reminded ---------------------------------------- 
			String remindedID = nodeElementList.get(i); // --- may be a NetworkComponent ID ---
			ElectricalNodeState elNodeState = graphNodeStates.get(remindedID);
			if (elNodeState==null) {
				String graphNodeID = this.getGraphNodeID(remindedID);
				if (graphNodeID!=null) {
					elNodeState = graphNodeStates.get(graphNodeID);
				}
			}
			
			if (elNodeState!=null) {
				// --- Create NodeResult --------------------------------------
				NodeResult nodeResult = new NodeResult();
				nodeResult.setIdExecution(this.getIDExecution());
				nodeResult.setIdNode(remindedID);
				nodeResult.setTimestamp(calendar);
				
				// --- Get the electrical node state --------------------------
				if (elNodeState instanceof UniPhaseElectricalNodeState) {
					// --- => UniPhaseElectricalNodeState ---------------------
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
					// --- => TriPhaseElectricalNodeState ---------------------
					TriPhaseElectricalNodeState tens = (TriPhaseElectricalNodeState) elNodeState;
					UniPhaseElectricalNodeState upensL1 = tens.getL1();
					UniPhaseElectricalNodeState upensL2 = tens.getL2();
					UniPhaseElectricalNodeState upensL3 = tens.getL3();
					
					double voltageReal = this.getVoltageReal(upensL1);
					double voltageComplex = this.getVoltageComplex(upensL1);
					
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
				// --- Add to list --------------------------------------------
				nodeResultList.add(nodeResult);
			}
			
		} // --- end for ---
		return nodeResultList;
	}
	
	
	/**
	 * Returns the voltage real from the specified UniPhaseElectricalNodeState.
	 *
	 * @param upens the UniPhaseElectricalNodeState
	 * @return the voltage real
	 */
	private double getVoltageReal(UniPhaseElectricalNodeState upens) {
		return ((double)upens.getVoltageReal().getValue()) * this.sqrtRootThree;
	}
	/**
	 * Returns the complex voltage from the specified UniPhaseElectricalNodeState.
	 *
	 * @param upens the UniPhaseElectricalNodeState
	 * @return the complex voltage 
	 */
	private double getVoltageComplex(UniPhaseElectricalNodeState upens) {
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
		
			Vector<NetworkComponent> netCompVector = this.getNetworkModel().getNetworkComponentVectorSorted();
			for (int i = 0; i < netCompVector.size(); i++) {

				NetworkComponent netComp = netCompVector.get(i);
				switch (netComp.getType()) {
				case "Prosumer":
				case "CableCabinet":
					this.getNodeElementList().add(netComp.getId());
					break;
				case "Cable":
				case "Sensor":
				case "Breaker":
					this.getEdgeElementList().add(netComp.getId());
					break;
				case "Transformer":
					this.getTransformerList().add(netComp.getId());
					this.getNodeElementList().add(this.getGraphNodeID(netComp.getId()));
					break;
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
}
