package de.enflexit.ea.electricity.aggregation.triPhase;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.visualisation.notifications.DisplayAgentNotificationGraph;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper.BranchDescription;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper.SlackNodeDescription;
import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSensorState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.CableLosses;
import de.enflexit.ea.electricity.blackboard.TransformerPowerAnswer;
import de.enflexit.ea.lib.powerFlowCalculation.AbstractPowerFlowCalculation;
import energy.OptionModelController;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.EnergyFlowMeasured;
import energy.optionModel.EnergyInterface;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.calculation.FlowsMeasuredGroup;
import energygroup.calculation.FlowsMeasuredGroupMember;

/**
 * Network calculation strategy implementation for low voltage distribution grids.
 * 
 * @author mehlich
 */
public class TriPhaseElectricalNetworkCalculationStrategy extends AbstractElectricalNetworkCalculationStrategy {
	
	private TriPhaseElectricalSlackNodeHandler slackNodeHandler;
	
	/**
	 * Instantiates a new TriPhaseElectricalNetworkCalculationStrategy.
	 * @param optionModelController the option model controller
	 */
	public TriPhaseElectricalNetworkCalculationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy#getSlackNodeHandler()
	 */
	@Override
	public TriPhaseElectricalSlackNodeHandler getSlackNodeHandler() {
		if (slackNodeHandler==null) {
			slackNodeHandler = new TriPhaseElectricalSlackNodeHandler(this);
		}
		return slackNodeHandler;
	}
	
	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#doNetworkCalculation(javax.swing.tree.DefaultMutableTreeNode, java.util.List, energygroup.calculation.FlowsMeasuredGroup)
	 */
	@Override
	public FlowsMeasuredGroupMember doNetworkCalculation(DefaultMutableTreeNode currentParentNode, List<TechnicalInterface> outerInterfaces, FlowsMeasuredGroup efmGroup) {
		
		String netCalcID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_NETWORK_CALCULATION + this.getSubNetworkConfiguration().getID();
		String flowSumID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_FLOW_SUMMARIZATION + this.getSubNetworkConfiguration().getID();

		this.debugPrintLine(efmGroup.getGlobalTimeTo(), "Execute network calculation in '" + this.getClass().getSimpleName() + "'");
		
		boolean isSkipNetworkCalculation = this.getAggregationHandler().debugIsSkipActualNetworkCalculation();
		if (isSkipNetworkCalculation==false) {
			
			// --- Update slack node voltage level for sensor data based calculations ---
			this.getSlackNodeHandler().updateSlackNodeState();
			
			// --------------------------------------------------------------------------
			// --- (Re) execute the phase dependent electrical network calculation ------
			// --------------------------------------------------------------------------
			this.getPowerFlowCalculationsFinalized().clear();
			// --- Reset the slack node voltage level? ----------------------------------
			if (this.getSlackNodeHandler().isChangedSlackNodeState()==true) {
				this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).setSlackNodeVoltageLevel(this.getSlackNodeHandler().getSlackNodeState().getSlackNodeStateL1());
				this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L2).setSlackNodeVoltageLevel(this.getSlackNodeHandler().getSlackNodeState().getSlackNodeStateL2());
				this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L3).setSlackNodeVoltageLevel(this.getSlackNodeHandler().getSlackNodeState().getSlackNodeStateL3());
			}
			// --- Reset the calculation parameter --------------------------------------
			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).resetCalculationBase(currentParentNode, efmGroup);
			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L2).resetCalculationBase(currentParentNode, efmGroup);
			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L3).resetCalculationBase(currentParentNode, efmGroup);
			// --- Notify all calculation threads to (re-)restart the calculation ------- 
			this.getAggregationHandler().setPerformanceMeasurementStarted(netCalcID);
			synchronized (this.getCalculationTrigger()) {
				this.getCalculationTrigger().notifyAll();
			}
		}
		
		// ------------------------------------------------------------------------------
		// --- Summarize interfaces that don't need a network calculation ---------------
		// ------------------------------------------------------------------------------
		FlowsMeasuredGroupMember efmSummarized = new FlowsMeasuredGroupMember();
		this.getAggregationHandler().setPerformanceMeasurementStarted(flowSumID);
		for (int i = 0; i < outerInterfaces.size(); i++) {
			
			TechnicalInterface ti = outerInterfaces.get(i);
			EnergyInterface ei = (EnergyInterface) ti;
			try {
				// --- Calculate the sum of energy flow for this interface --------------
				EnergyFlowMeasured efm = efmGroup.sumUpEnergyFlowMeasuredByEnergyCarrierAndDomainModel(currentParentNode, ei.getInterfaceID(), ei.getEnergyCarrier(), ei.getDomainModel());
				// --- In case that there is no EnergyFlowMeasured ----------------------
				if (efm.getMeasurments().size()==0) {
					efm = this.getEnergyFlowMeasuredZeroOverTime(ei.getInterfaceID(), efmGroup.getGlobalTimeFrom(), efmGroup.getGlobalTimeTo(), this.getDefaultSIPrefix());
				}
				efmSummarized.addEnergyFlowMeasured(efm, ei.getInterfaceID(), ei.getDomain(), ei.getDomainModel(), ei.getEnergyCarrier());
				
			} catch (Exception ex) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error summarizing the energy flow for '" + ei.getInterfaceID() + "' Energy Carrier: " + ei.getEnergyCarrier().value() + ", Domain-Model: " + ei.getDomainModel().toString());
				ex.printStackTrace();
			}
		}
		this.getAggregationHandler().setPerformanceMeasurementFinalized(flowSumID);
		
		// --- Wait for the end of the power flow calculations --------------------------
		this.waitUntilCalculationFinalized(this.getPowerFlowCalculationThreads().size());
		this.getAggregationHandler().setPerformanceMeasurementFinalized(netCalcID);
		
		// --- Create the display notifications from the calculation results ------------  
		this.summarizeResults(efmGroup.getGlobalTimeTo());
		// -- Done ----------------------------------------------------------------------
		return efmSummarized;
	}
	
	
	/**
	 * Creates the {@link DisplayAgentNotificationGraph} from the power flow calculation results.
	 * @param globalTimeTo the global time to which is to be calculated
	 */
	@Override
	protected void summarizeResults(long globalTimeTo) {

		try {
			
			// --- Get the PowerFlowCalculations for each phase -------------------------
			NetworkModelToCsvMapper netModel2CsvMapper = this.getNetworkModelToCsvMapper();
			
			AbstractPowerFlowCalculation pfcL1 = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L1).getPowerFlowCalculation();
			AbstractPowerFlowCalculation pfcL2 = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L2).getPowerFlowCalculation();
			AbstractPowerFlowCalculation pfcL3 = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.L3).getPowerFlowCalculation();
			
			if (pfcL1==null && pfcL2==null && pfcL3==null) return;
			
			// --- Check if all pfc were successful, then summarize results -------------
			if (pfcL1.isSucessfullPFC() && pfcL2.isSucessfullPFC() && pfcL3.isSucessfullPFC()) {
				// --- Define the GraphNode state: A TriPhaseElectricalNodeState ------------
				this.setNodeStates(pfcL1, pfcL2, pfcL3, netModel2CsvMapper);
				// --- Edit the 'Cable' data model of the NetworkComponents affected --------
				this.setCableStates(pfcL1, pfcL2, pfcL3, netModel2CsvMapper);
				// --- Set the transformer state to the Blackboard --------------------------
				this.setTransformerState(globalTimeTo, pfcL1, pfcL2, pfcL3, netModel2CsvMapper);
				// --- Extend the Sensor ScheduleList ---------------------------------------
				this.setSensorTechnicalSystemStates(globalTimeTo);
			}	
			
		} catch (Exception ex) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error summarizing results from PowerFlowCalculation:");
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Sets the transformer state.
	 *
	 * @param globalTimeTo the global time to
	 * @param pfcL1 the PowerFlowCalculation for L1
	 * @param pfcL2 the PowerFlowCalculation for L2
	 * @param pfcL3 the PowerFlowCalculation for L3
	 * @param netModel2CsvMapper the net model 2 csv mapper
	 */
	protected void setTransformerState(long globalTimeTo, AbstractPowerFlowCalculation pfcL1, AbstractPowerFlowCalculation pfcL2, AbstractPowerFlowCalculation pfcL3, NetworkModelToCsvMapper netModel2CsvMapper) {
		
		double transformerPowerRealL1 = 0;
		double transformerPowerRealL2 = 0;
		double transformerPowerRealL3 = 0;
		double transformerPowerImagL1 = 0;
		double transformerPowerImagL2 = 0;
		double transformerPowerImagL3 = 0;

		if (pfcL1 != null) {
			if(pfcL1.getPowerOfTransformer().size()>0){
				transformerPowerRealL1 = pfcL1.getPowerOfTransformer().get(0);
				transformerPowerImagL1 = pfcL1.getPowerOfTransformer().get(1);
			}
		}
		if (pfcL2 != null) {
			if(pfcL2.getPowerOfTransformer().size()>0){
				transformerPowerRealL2 = pfcL2.getPowerOfTransformer().get(0);
				transformerPowerImagL2 = pfcL2.getPowerOfTransformer().get(1);
			}
		}
		if (pfcL3 != null) {
			if(pfcL3.getPowerOfTransformer().size()>0){
				transformerPowerRealL3 = pfcL3.getPowerOfTransformer().get(0);
				transformerPowerImagL3 = pfcL3.getPowerOfTransformer().get(1);
			}
		}
		
		SlackNodeDescription snDesc = null;
		if (netModel2CsvMapper.getSlackNodeVector()!=null) {
			 snDesc = netModel2CsvMapper.getSlackNodeVector().get(0);
		}

		// --- Define TechnicalSystemState ----------------------
		TechnicalSystemState tss = new TechnicalSystemState();
		tss.setGlobalTime(globalTimeTo);

		// --- L1 -----------------------------------------------
		if (transformerPowerRealL1 != 0 && transformerPowerImagL1 != 0) {
			// --- Active Power L1 ------------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_L1_P, transformerPowerRealL1);
			// --- Reactive Power L1 ----------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_L1_Q, transformerPowerImagL1);
		}

		// --- L2 -----------------------------------------------
		if (transformerPowerRealL2 != 0 && transformerPowerImagL2 != 0) {
			// --- Active Power L2 ------------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_L2_P, transformerPowerRealL2);
			// --- Reactive Power L2 ----------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_L2_Q, transformerPowerImagL2);
		}

		// --- L3 -----------------------------------------------
		if (transformerPowerRealL3 != 0 && transformerPowerImagL3 != 0) {
			// --- Active Power L3 ------------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_L3_P, transformerPowerRealL3);
			// --- Reactive Power L3 ----------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_L3_Q, transformerPowerImagL3);
		}
		if (snDesc.getNetworkComponentID()!=null) {
			this.getTransformerStates().put(snDesc.getNetworkComponentID(), tss);
		}
	}
	
	/**
	 * Sets the sensors technical system states (TSSE's).
	 * @param globalTimeTo the timestamp to use for the sensors technical states
	 */
	protected void setSensorTechnicalSystemStates(long globalTimeTo) {
		
		// --- Only set this states, if the simulation is based on node power flows -----
		if (this.getAggregationHandler().getExecutionDataBase()==ExecutionDataBase.SensorData) return;
		
		// --- Get the last sensor states -----------------------------------------------
		HashMap<String, TechnicalSystemStateEvaluation> lastSensorStates = this.getLastSensorStates(); 
		for (int i = 0; i < this.getSensorIDs().size(); i++) {
			
			String sensorNetCompID = this.getSensorIDs().get(i);
			// --- Calculate the state time ---------------------------------------------
			TechnicalSystemStateEvaluation tsseOld = lastSensorStates.get(sensorNetCompID); 
			long statetime = 0;
			if (tsseOld!=null) {
				statetime = globalTimeTo - tsseOld.getGlobalTime();
			}
			
			// --- Define the new TSSE --------------------------------------------------
			TechnicalSystemStateEvaluation tsseNew = new TechnicalSystemStateEvaluation();
			tsseNew.setGlobalTime(globalTimeTo);
			tsseNew.setDescription("Calculated Sensor State");
			tsseNew.setConfigID("Conf");
			tsseNew.setStateID("Sensing");
			tsseNew.setStateTime(statetime);
	
			// --- Get the sensor state -------------------------------------------------
			TriPhaseSensorState sensor = (TriPhaseSensorState) this.getCableStates().get(sensorNetCompID);
			
			// --- Define the 'measurements' --------------------------------------------
			// --- Voltage --------------------------------
			FixedDouble fdVoltageL1 = new FixedDouble();
			fdVoltageL1.setVariableID("Voltage L1");
			fdVoltageL1.setValue(sensor.getVoltage_L1());
			tsseNew.getIOlist().add(fdVoltageL1);
			
			FixedDouble fdVoltageL2 = new FixedDouble();
			fdVoltageL2.setVariableID("Voltage L2");
			fdVoltageL2.setValue(sensor.getVoltage_L2());
			tsseNew.getIOlist().add(fdVoltageL2);
			
			FixedDouble fdVoltageL3 = new FixedDouble();
			fdVoltageL3.setVariableID("Voltage L3");
			fdVoltageL3.setValue(sensor.getVoltage_L3());
			tsseNew.getIOlist().add(fdVoltageL3);
			
			// --- Current --------------------------------
			FixedDouble fdCurrentL1 = new FixedDouble();
			fdCurrentL1.setVariableID("Current L1");
			fdCurrentL1.setValue(sensor.getCurrent_L1());
			tsseNew.getIOlist().add(fdCurrentL1);
			
			FixedDouble fdCurrentL2 = new FixedDouble();
			fdCurrentL2.setVariableID("Current L2");
			fdCurrentL2.setValue(sensor.getCurrent_L2());
			tsseNew.getIOlist().add(fdCurrentL2);
			
			FixedDouble fdCurrentL3 = new FixedDouble();
			fdCurrentL3.setVariableID("Current L3");
			fdCurrentL3.setValue(sensor.getCurrent_L3());
			tsseNew.getIOlist().add(fdCurrentL3);
			
			// --- Cos Phi --------------------------------
			FixedDouble fdCosPhiL1 = new FixedDouble();
			fdCosPhiL1.setVariableID("Cos Phi L1");
			fdCosPhiL1.setValue(sensor.getCosPhi_L1());
			tsseNew.getIOlist().add(fdCosPhiL1);
			
			FixedDouble fdCosPhiL2 = new FixedDouble();
			fdCosPhiL2.setVariableID("Cos Phi L2");
			fdCosPhiL2.setValue(sensor.getCosPhi_L2());
			tsseNew.getIOlist().add(fdCosPhiL2);
			
			FixedDouble fdCosPhiL3 = new FixedDouble();
			fdCosPhiL3.setVariableID("Cos Phi L3");
			fdCosPhiL3.setValue(sensor.getCosPhi_L3());
			tsseNew.getIOlist().add(fdCosPhiL3);
			
			// --- Add to the aggregation as non-real time Schedule -----------
			this.getAggregationHandler().appendToNetworkComponentsScheduleController(sensorNetCompID, tsseNew, false);
		}
	}
	
	/**
	 * Sets the node states.
	 *
	 * @param pfcL1 the PowerFlowCalculation for L1
	 * @param pfcL2 the PowerFlowCalculation for L2
	 * @param pfcL3 the PowerFlowCalculation for L3
	 * @param netModel2CsvMapper the net model 2 csv mapper
	 */
	@SuppressWarnings("unchecked")
	protected void setNodeStates(AbstractPowerFlowCalculation pfcL1, AbstractPowerFlowCalculation pfcL2, AbstractPowerFlowCalculation pfcL3, NetworkModelToCsvMapper netModel2CsvMapper) {
		
		Vector<Double> uKabs_L1 = null;
		Vector<Double> uKabs_L2 = null;
		Vector<Double> uKabs_L3 = null;
		
		Vector<Double> uKReal_L1 = null;
		Vector<Double> uKReal_L2 = null;
		Vector<Double> uKReal_L3 = null;
		
		Vector<Double> uKImag_L1 = null;
		Vector<Double> uKImag_L2 = null;
		Vector<Double> uKImag_L3 = null;

		Vector<Double> cosPhi_L1 = null;
		Vector<Double> cosPhi_L2 = null;
		Vector<Double> cosPhi_L3 = null;
		
		Vector<Double> nodalPowerReal_L1= null;
		Vector<Double> nodalPowerReal_L2= null;
		Vector<Double> nodalPowerReal_L3= null;
		
		Vector<Double> nodalPowerImag_L1= null;
		Vector<Double> nodalPowerImag_L2= null;
		Vector<Double> nodalPowerImag_L3= null;
		
		if (pfcL1 != null) {
			uKabs_L1  = pfcL1.getNodalVoltageAbs();
			cosPhi_L1 = pfcL1.getNodalCosPhi();
			nodalPowerReal_L1 = pfcL1.getNodalPowerReal();
			nodalPowerImag_L1 = pfcL1.getNodalPowerImag();
			uKReal_L1 = pfcL1.getNodalVoltageReal();
			uKImag_L1 = pfcL1.getNodalVoltageImag();
		}
		if (pfcL2 != null) {
			uKabs_L2  = pfcL2.getNodalVoltageAbs();
			cosPhi_L2 = pfcL2.getNodalCosPhi();
			nodalPowerReal_L2 = pfcL2.getNodalPowerReal();
			nodalPowerImag_L2 = pfcL2.getNodalPowerImag();
			uKReal_L2 = pfcL2.getNodalVoltageReal();
			uKImag_L2 = pfcL2.getNodalVoltageImag();
		}
		if (pfcL3 != null) {
			uKabs_L3  = pfcL3.getNodalVoltageAbs();
			cosPhi_L3 = pfcL3.getNodalCosPhi();
			nodalPowerReal_L3 = pfcL3.getNodalPowerReal();
			nodalPowerImag_L3 = pfcL3.getNodalPowerImag();
			uKReal_L3 = pfcL3.getNodalVoltageReal();
			uKImag_L3 = pfcL3.getNodalVoltageImag();
		}

		// --- Check all calculation results ----------------------------------
		String domain = this.getSubNetworkConfiguration().getDomain();
		for (int i = 0; i < this.getArrayLength(uKabs_L1, uKabs_L2, uKabs_L3); i++) {

			int rowNumber = i+1;
			GraphNode graphNode = netModel2CsvMapper.getNodeNumberToGraphNode().get(rowNumber);
			Object[] dataModelArray= null;
			TreeMap<String , Object> dataModeTreeMap = null;
			TriPhaseElectricalNodeState tpNodeState = null;

			// --- Get the current data model of the GraphNode element --------
			if (graphNode.getDataModel()==null) {
				// --- Create the GraphNode data model ------------------------
				dataModelArray = new Object[3];
				List<String> domains = this.getNetworkModel().getDomain(graphNode);
				if (domains.size()>1) {
					dataModeTreeMap = new TreeMap<>();
					dataModeTreeMap.put(domain, dataModelArray);
					graphNode.setDataModel(dataModeTreeMap);
				} else {
					graphNode.setDataModel(dataModelArray);
				}
				
			} else {
				// --- Work on the current GraphNode data model ---------------
				if (graphNode.getDataModel() instanceof TreeMap<?, ?>) {
					// --- Combined domain data model -------------------------
					dataModeTreeMap = (TreeMap<String, Object>) graphNode.getDataModel();
					Object partDataModel = dataModeTreeMap.get(domain);
					if (partDataModel==null) {
						dataModelArray = new Object[3];
						dataModeTreeMap.put(domain, dataModelArray);
					} else {
						dataModelArray = (Object[]) partDataModel;
						tpNodeState = (TriPhaseElectricalNodeState) dataModelArray[1];
					}
				} else {
					// --- Single domain data model ---------------------------
					dataModelArray = (Object[]) graphNode.getDataModel();
					tpNodeState = (TriPhaseElectricalNodeState) dataModelArray[1];	
				}
				
			}
			
			// --- In case that no element was set yet ------------------------
			if (tpNodeState==null) {
				tpNodeState = new TriPhaseElectricalNodeState();
			}
			if (uKabs_L1 != null) {
				UniPhaseElectricalNodeState upNodeStateL1 = new UniPhaseElectricalNodeState();
				upNodeStateL1.setVoltageAbs(new UnitValue(uKabs_L1.get(i).floatValue(), "V"));
				upNodeStateL1.setCosPhi(cosPhi_L1.get(i).floatValue());
				upNodeStateL1.setP(new UnitValue(nodalPowerReal_L1.get(i).floatValue(), "W"));
				upNodeStateL1.setQ(new UnitValue(nodalPowerImag_L1.get(i).floatValue(), "var"));
				upNodeStateL1.setVoltageReal(new UnitValue(uKReal_L1.get(i).floatValue(), "V"));
				upNodeStateL1.setVoltageImag(new UnitValue(uKImag_L1.get(i).floatValue(), "V"));
				upNodeStateL1.setCurrent(new UnitValue((float)(this.getNodeStateCurrent(nodalPowerReal_L1.get(i), nodalPowerImag_L1.get(i), uKabs_L1.get(i))), "A"));
				upNodeStateL1.setS(new UnitValue((float)(this.getNodeStateS(nodalPowerReal_L1.get(i), nodalPowerImag_L1.get(i))), "VA"));
				tpNodeState.setL1(upNodeStateL1);
			}
			if (uKabs_L2 != null) {
				UniPhaseElectricalNodeState upNodeStateL2 = new UniPhaseElectricalNodeState();
				upNodeStateL2.setVoltageAbs(new UnitValue(uKabs_L2.get(i).floatValue(), "V"));
				upNodeStateL2.setCosPhi(cosPhi_L2.get(i).floatValue());
				upNodeStateL2.setP(new UnitValue(nodalPowerReal_L2.get(i).floatValue(), "W"));
				upNodeStateL2.setQ(new UnitValue(nodalPowerImag_L2.get(i).floatValue(), "var"));
				upNodeStateL2.setVoltageReal(new UnitValue(uKReal_L2.get(i).floatValue(), "V"));
				upNodeStateL2.setVoltageImag(new UnitValue(uKImag_L2.get(i).floatValue(), "V"));
				upNodeStateL2.setCurrent(new UnitValue((float)(this.getNodeStateCurrent(nodalPowerReal_L2.get(i), nodalPowerImag_L2.get(i), uKabs_L2.get(i))), "A"));
				upNodeStateL2.setS(new UnitValue((float)(this.getNodeStateS(nodalPowerReal_L2.get(i), nodalPowerImag_L2.get(i))), "VA"));
				tpNodeState.setL2(upNodeStateL2);
			}
			if (uKabs_L3 != null) {
				UniPhaseElectricalNodeState upNodeStateL3 = new UniPhaseElectricalNodeState();
				upNodeStateL3.setVoltageAbs(new UnitValue(uKabs_L3.get(i).floatValue(), "V"));
				upNodeStateL3.setCosPhi(cosPhi_L3.get(i).floatValue());
				upNodeStateL3.setP(new UnitValue(nodalPowerReal_L3.get(i).floatValue(), "W"));
				upNodeStateL3.setQ(new UnitValue(nodalPowerImag_L3.get(i).floatValue(), "var"));
				upNodeStateL3.setVoltageReal(new UnitValue(uKReal_L3.get(i).floatValue(), "V"));
				upNodeStateL3.setVoltageImag(new UnitValue(uKImag_L3.get(i).floatValue(), "V"));
				upNodeStateL3.setCurrent(new UnitValue((float)(this.getNodeStateCurrent(nodalPowerReal_L3.get(i), nodalPowerImag_L3.get(i), uKabs_L3.get(i))), "A"));
				upNodeStateL3.setS(new UnitValue((float)(this.getNodeStateS(nodalPowerReal_L3.get(i), nodalPowerImag_L3.get(i))), "VA"));
				tpNodeState.setL3(upNodeStateL3);
			}

			// --- Finally, set new data model --------------------------------
			dataModelArray[1] = tpNodeState;
			//graphNode.setDataModel(dataModelArray);
			// --- Remind this result -----------------------------------------
			this.getNodeStates().put(graphNode.getId(), tpNodeState);
		}
	}
	
	/**
	 * Sets the cable states.
	 *
	 * @param globalTimeTo the global time to
	 * @param pfcL1 the PowerFlowCalculation for L1
	 * @param pfcL2 the PowerFlowCalculation for L2
	 * @param pfcL3 the PowerFlowCalculation for L3
	 * @param netModel2CsvMapper the net model 2 csv mapper
	 */
	protected void setCableStates(AbstractPowerFlowCalculation pfcL1, AbstractPowerFlowCalculation pfcL2, AbstractPowerFlowCalculation pfcL3, NetworkModelToCsvMapper netModel2CsvMapper) {
	
		Vector<Vector<Double>> iNabs_L1 = null;
		Vector<Vector<Double>> iNabs_L2 = null;
		Vector<Vector<Double>> iNabs_L3 = null;
		
		Vector<Vector<Double>> iNreal_L1 = null;
		Vector<Vector<Double>> iNreal_L2 = null;
		Vector<Vector<Double>> iNreal_L3 = null;
		
		Vector<Vector<Double>> iNimag_L1 = null;
		Vector<Vector<Double>> iNimag_L2 = null;
		Vector<Vector<Double>> iNimag_L3 = null;

		Vector<Double> branchCosPhi_L1 = null;
		Vector<Double> branchCosPhi_L2 = null;
		Vector<Double> branchCosPhi_L3 = null;
		
		Vector<Double> utili_L1 = null;
		Vector<Double> utili_L2 = null;
		Vector<Double> utili_L3 = null;
		
		Vector<Vector<Double>> p_L1 = null;
		Vector<Vector<Double>> p_L2 = null;
		Vector<Vector<Double>> p_L3 = null;
		
		Vector<Vector<Double>> q_L1 = null;
		Vector<Vector<Double>> q_L2 = null;
		Vector<Vector<Double>> q_L3 = null;
		
		Vector<Double> uKReal_L1 = null;
		Vector<Double> uKReal_L2 = null;
		Vector<Double> uKReal_L3 = null;
		
		Vector<Double> uKImag_L1 = null;
		Vector<Double> uKImag_L2 = null;
		Vector<Double> uKImag_L3 = null;

		if (pfcL1 != null) {
			iNabs_L1 = pfcL1.getBranchCurrentAbs();
			iNreal_L1 = pfcL1.getBranchCurrentReal();
			iNimag_L1 = pfcL1.getBranchCurrentImag();
			branchCosPhi_L1 = pfcL1.getBranchCosPhi();
			utili_L1 = pfcL1.getBranchUtilization();
			p_L1 = pfcL1.getBranchPowerReal();
			q_L1 = pfcL1.getBranchPowerImag();
			uKReal_L1 = pfcL1.getNodalVoltageReal();
			uKImag_L1 = pfcL1.getNodalVoltageImag();
		}
		
		if (pfcL2 != null) {
			iNabs_L2 = pfcL2.getBranchCurrentAbs();
			iNreal_L2 = pfcL2.getBranchCurrentReal();
			iNimag_L2 = pfcL2.getBranchCurrentImag();
			branchCosPhi_L2 = pfcL2.getBranchCosPhi();
			utili_L2 = pfcL2.getBranchUtilization();
			p_L2 = pfcL2.getBranchPowerReal();
			q_L2 = pfcL2.getBranchPowerImag();
			uKReal_L2 = pfcL2.getNodalVoltageReal();
			uKImag_L2 = pfcL2.getNodalVoltageImag();
		}
		
		if (pfcL3 != null) {
			iNabs_L3 = pfcL3.getBranchCurrentAbs();
			iNreal_L3 = pfcL3.getBranchCurrentReal();
			iNimag_L3 = pfcL3.getBranchCurrentImag();
			branchCosPhi_L3 = pfcL3.getBranchCosPhi();
			utili_L3 = pfcL3.getBranchUtilization();
			p_L3 = pfcL3.getBranchPowerReal();
			q_L3 = pfcL3.getBranchPowerImag();
			uKReal_L3 = pfcL2.getNodalVoltageReal();
			uKImag_L3 = pfcL2.getNodalVoltageImag();
		}

		// --- Get the reminded BranchDescription's ---------------------------
		Vector<BranchDescription> branchDescriptionVector = netModel2CsvMapper.getBranchDescription(); 
		for (int i=0; i<branchDescriptionVector.size(); i++) {
			
			BranchDescription bd = branchDescriptionVector.get(i);
			NetworkComponent netComp = bd.getNetworkComponent();
			
			int nodeIndexFrom = bd.getNodeNumberFrom()-1;
			int nodeIndexTo = bd.getNodeNumberTo()-1;

			Object[] dataModel = null;
			TriPhaseCableState cableState = null;
			CableProperties cableProperties = null;
			// --- Get the current data model of the branch element -----------
			if (netComp.getDataModel() == null) {
				dataModel = new Object[3];
			} else {
				dataModel = (Object[]) netComp.getDataModel();
				cableProperties = (CableProperties) dataModel[0];
				cableState = (TriPhaseCableState) dataModel[1];
			}
			// --- In case that no element was set yet ------------------------
			if (cableState == null) {
				if (netComp.getType().equalsIgnoreCase("Sensor")) {
					cableState = new TriPhaseSensorState();
				} else {
					cableState = new TriPhaseCableState();
				}
			}
			
			cableState.setCurrent_L1(iNabs_L1.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			cableState.setCurrent_L2(iNabs_L2.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			cableState.setCurrent_L3(iNabs_L3.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			
			cableState.getPhase1().setCurrentReal(new UnitValue(iNreal_L1.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.getPhase2().setCurrentReal(new UnitValue(iNreal_L2.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.getPhase3().setCurrentReal(new UnitValue(iNreal_L3.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			
			cableState.getPhase1().setCurrentImag(new UnitValue(iNimag_L1.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.getPhase2().setCurrentImag(new UnitValue(iNimag_L2.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.getPhase3().setCurrentImag(new UnitValue(iNimag_L3.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			
			cableState.setCosPhi_L1(branchCosPhi_L1.get(i).floatValue());
			cableState.setCosPhi_L2(branchCosPhi_L2.get(i).floatValue());
			cableState.setCosPhi_L3(branchCosPhi_L3.get(i).floatValue());
			
			cableState.setUtil_L1(utili_L1.get(i).floatValue());
			cableState.setUtil_L2(utili_L2.get(i).floatValue());
			cableState.setUtil_L3(utili_L3.get(i).floatValue());
			
			cableState.setP_L1(p_L1.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			cableState.setP_L2(p_L2.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			cableState.setP_L3(p_L3.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			
			cableState.setQ_L1(q_L1.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			cableState.setQ_L2(q_L2.get(nodeIndexFrom).get(nodeIndexTo).floatValue());
			cableState.setQ_L3(q_L3.get(nodeIndexFrom).get(nodeIndexTo).floatValue());

			// --- Calculate cable losses -------------------------------------
			double ukRealNode1_L1 = uKReal_L1.get(nodeIndexFrom);
			double ukImagNode1_L1 = uKImag_L1.get(nodeIndexFrom);
			double ukRealNode2_L1 = uKReal_L1.get(nodeIndexTo);
			double ukImagNode2_L1 = uKImag_L1.get(nodeIndexTo);
			
			
			CableLosses cableLossesL1 = new CableLosses(cableState.getPhase1().getCurrentReal().getValue(), cableState.getPhase1().getCurrentImag().getValue(), ukRealNode1_L1, ukImagNode1_L1, ukRealNode2_L1, ukImagNode2_L1, cableProperties);
			cableState.getPhase1().setLossesP(cableLossesL1.getLossesP());
			cableState.getPhase1().setLossesQ(cableLossesL1.getLossesQ());
			
			double ukRealNode1_L2 = uKReal_L2.get(nodeIndexFrom);
			double ukImagNode1_L2 = uKImag_L2.get(nodeIndexFrom);
			double ukRealNode2_L2 = uKReal_L2.get(nodeIndexTo);
			double ukImagNode2_L2 = uKImag_L2.get(nodeIndexTo);
			CableLosses cableLossesL2 = new CableLosses(cableState.getPhase2().getCurrentReal().getValue(), cableState.getPhase2().getCurrentImag().getValue(), ukRealNode1_L2, ukImagNode1_L2, ukRealNode2_L2, ukImagNode2_L2, cableProperties);
			cableState.getPhase2().setLossesP(cableLossesL2.getLossesP());
			cableState.getPhase2().setLossesQ(cableLossesL2.getLossesQ());
			
			double ukRealNode1_L3 = uKReal_L3.get(nodeIndexFrom);
			double ukImagNode1_L3 = uKImag_L3.get(nodeIndexFrom);
			double ukRealNode2_L3 = uKReal_L3.get(nodeIndexTo);
			double ukImagNode2_L3 = uKImag_L3.get(nodeIndexTo);
			CableLosses cableLossesL3 = new CableLosses(cableState.getPhase3().getCurrentReal().getValue(), cableState.getPhase3().getCurrentImag().getValue(), ukRealNode1_L3, ukImagNode1_L3, ukRealNode2_L3, ukImagNode2_L3, cableProperties);
			cableState.getPhase3().setLossesP(cableLossesL3.getLossesP());
			cableState.getPhase3().setLossesQ(cableLossesL3.getLossesQ());
			
			
			// --- Set voltage to sensors -------------------------------------
			if (cableState instanceof TriPhaseSensorState) {
				
				SensorProperties sensorProperties = (SensorProperties) dataModel[0];

				TriPhaseSensorState sensorState = (TriPhaseSensorState) cableState;
				String measureLocationNetComp = sensorProperties.getMeasureLocation();
				if (measureLocationNetComp==null) {
					System.err.println("[" + this.getClass().getSimpleName() + "] No measure location was set for Sensor '" + netComp.getId() + "'!");
					
				} else {
					
					NetworkComponent netCompMeasurement = this.getNetworkModel().getNetworkComponent(measureLocationNetComp);
					Vector<GraphElement> graphNodeMeasurement = this.getNetworkModel().getGraphElementsFromNetworkComponent(netCompMeasurement);
					if (graphNodeMeasurement.size()==1) {
						String graphNodeID = graphNodeMeasurement.get(0).getId();
						TriPhaseElectricalNodeState tpNodeState = (TriPhaseElectricalNodeState) this.getNodeStates().get(graphNodeID);
						if (tpNodeState!=null) {
							sensorState.setVoltage_L1(tpNodeState.getL1NodeStateNotNull().getVoltageAbs().getValue());
							sensorState.setVoltage_L2(tpNodeState.getL2NodeStateNotNull().getVoltageAbs().getValue());
							sensorState.setVoltage_L3(tpNodeState.getL3NodeStateNotNull().getVoltageAbs().getValue());
						}
					}
				}
				
			}
			
			// --- Finally, set new data model --------------------------------
			dataModel[1] = cableState;
			netComp.setDataModel(dataModel);
			// --- Remind this result -----------------------------------------
			this.getCableStates().put(netComp.getId(), cableState);
		}
	}
	
	/**
	 * Gets the longest array length of specified arrays.
	 *
	 * @param arrayL1 the array for L1
	 * @param arrayL2 the array for L2
	 * @param arrayL3 the array for L3
	 * @return the longest array length of the three specified arrays
	 */
	private int getArrayLength(Vector<Double> arrayL1, Vector<Double> arrayL2, Vector<Double> arrayL3) {
		int length = 0;
		if (arrayL1 != null) {
			int lengthL1 = arrayL1.size();
			if (lengthL1 > length)
				length = lengthL1;
		}
		if (arrayL2 != null) {
			int lengthL2 = arrayL2.size();
			if (lengthL2 > length)
				length = lengthL2;
		}
		if (arrayL3 != null) {
			int lengthL3 = arrayL3.size();
			if (lengthL3 > length)
				length = lengthL3;
		}
		return length;
	}

}
