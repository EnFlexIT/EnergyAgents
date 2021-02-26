package de.enflexit.ea.electricity.aggregation.uniPhase;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper.BranchDescription;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper.SlackNodeDescription;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableWithBreaker;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSensorState;
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
import energy.optionModel.GoodFlowMeasured;
import energy.optionModel.GoodInterface;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.calculation.FlowsMeasuredGroup;
import energygroup.calculation.FlowsMeasuredGroupMember;

/**
 * Network calculation strategy for uni-phase (or symmetrical) electricity grids.
 * 
 * @author Jan Mehlich - EVT - University of Wuppertal
 */
public class UniPhaseElectricalNetworkCalculationStrategy extends AbstractElectricalNetworkCalculationStrategy {
	
	private UniPhaseElectricalSlackNodeHandler slackNodeHandler;
	
	/**
	 * Instantiates a new UniPhaseElectricalNetworkCalculationStrategy.
	 * @param optionModelController the option model controller
	 */
	public UniPhaseElectricalNetworkCalculationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy#getSlackNodeHandler()
	 */
	@Override
	public UniPhaseElectricalSlackNodeHandler getSlackNodeHandler() {
		if (slackNodeHandler==null) {
			slackNodeHandler = new UniPhaseElectricalSlackNodeHandler(this);
		}
		return slackNodeHandler;
	}

	/* (non-Javadoc)
	 * @see energygroup.evaluation.AbstractGroupEvaluationStrategy#doNetworkCalculation(javax.swing.tree.DefaultMutableTreeNode, java.util.List, energygroup.calculation.FlowsMeasuredGroup)
	 */
	@Override
	public FlowsMeasuredGroupMember doNetworkCalculation(DefaultMutableTreeNode currentParentNode, List<TechnicalInterface> outerInterfaces, FlowsMeasuredGroup fmGroup) {
		
		String netCalcID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_NETWORK_CALCULATION + this.getSubNetworkConfiguration().getID();
		String flowSumID = AbstractAggregationHandler.AGGREGATION_MEASUREMENT_STRATEGY_FLOW_SUMMARIZATION + this.getSubNetworkConfiguration().getID();
		
		this.debugPrintLine(fmGroup.getGlobalTimeTo(), "Execute network calculation in '" + this.getClass().getSimpleName() + "'");

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
				this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.AllPhases).setSlackNodeVoltageLevel(this.getSlackNodeHandler().getSlackNodeState());
			}
			// --- Reset the calculation parameter --------------------------------------
			this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.AllPhases).resetCalculationBase(currentParentNode, fmGroup);
			
			// --- Notify all calculation threads to (re-)restart the calculation ------- 
			this.getAggregationHandler().setPerformanceMeasurementStarted(netCalcID);
			synchronized (this.getCalculationTrigger()) {
				this.getCalculationTrigger().notifyAll();
			}
		}
		
		// ------------------------------------------------------------------------------
		// --- Summarize interfaces that don't need a network calculation ---------------
		// ------------------------------------------------------------------------------
		FlowsMeasuredGroupMember fmSummarized = new FlowsMeasuredGroupMember();
		this.getAggregationHandler().setPerformanceMeasurementStarted(flowSumID);
		for (int i = 0; i < outerInterfaces.size(); i++) {
			
			TechnicalInterface ti = outerInterfaces.get(i);
			if (ti instanceof EnergyInterface) {
				EnergyInterface ei = (EnergyInterface) ti;
				// --- Calculate the sum of energy flow for this interface --------------
				EnergyFlowMeasured efm = fmGroup.sumUpEnergyFlowMeasuredByEnergyCarrierAndDomainModel(currentParentNode, ei.getInterfaceID(), ei.getEnergyCarrier(), ei.getDomainModel());
				// --- In case that there is no EnergyFlowMeasured ----------------------
				if (efm.getMeasurments().size()==0) {
					efm = this.getEnergyFlowMeasuredZeroOverTime(ei.getInterfaceID(), fmGroup.getGlobalTimeFrom(), fmGroup.getGlobalTimeTo(), this.getDefaultSIPrefix());
				}
				fmSummarized.addEnergyFlowMeasured(efm, ei.getInterfaceID(), ei.getDomain(), ei.getDomainModel(), ei.getEnergyCarrier());
					
			} else if (ti instanceof GoodInterface) {
				GoodFlowMeasured gfm = (GoodFlowMeasured) fmGroup.sumUpFlowMeasuredByDomainAndDomainModel(currentParentNode, ti.getInterfaceID(), ti.getDomain(), ti.getDomainModel());
				if (gfm.getMeasurments().size()==0) {
					gfm = this.getGoodFlowMeasuredZeroOverTime(ti.getInterfaceID(), fmGroup.getGlobalTimeFrom(), fmGroup.getGlobalTimeTo());
				}
				fmSummarized.addGoodFlowMeasured(gfm, ti.getInterfaceID(), ti.getDomain(), ti.getDomainModel());
			}
		}
		this.getAggregationHandler().setPerformanceMeasurementFinalized(flowSumID);
		
		// --- Wait for the end of the power flow calculations --------------------------
		this.waitUntilCalculationFinalized(this.getPowerFlowCalculationThreads().size());
		this.getAggregationHandler().setPerformanceMeasurementFinalized(netCalcID);
		// --- Create the display notifications from the calculation results ------------  
		this.summarizeResults(fmGroup.getGlobalTimeTo());
		// -- Done ----------------------------------------------------------------------
		return fmSummarized;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy#summarizeResults(long)
	 */
	@Override
	protected void summarizeResults(long globalTimeTo) {

		try {
			
			// --- Get the PowerFlowCalculations for each phase -------------------------
			NetworkModelToCsvMapper netModel2CsvMapper = this.getNetworkModelToCsvMapper();
			
			AbstractPowerFlowCalculation pfc = this.getPowerFlowCalculationThread(Thread.currentThread(), Phase.AllPhases).getPowerFlowCalculation();
			
			if (pfc==null) return;
			
			// --- Check if all pfc were successful, then summarize results -------------
			if (pfc.isSucessfullPFC()) {
				// --- Define the GraphNode state: A TriPhaseElectricalNodeState ------------
				this.setNodeStates(pfc, netModel2CsvMapper);
				// --- Edit the 'Cable' data model of the NetworkComponents affected --------
				this.setCableStates(pfc, netModel2CsvMapper);
				// --- Set the transformer state to the Blackboard --------------------------
				this.setTransformerState(globalTimeTo, pfc, netModel2CsvMapper);
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
	 * @param pfc the power flow calculation
	 * @param netModel2CsvMapper the net model 2 csv mapper
	 */
	private void setTransformerState(long globalTimeTo, AbstractPowerFlowCalculation pfc, NetworkModelToCsvMapper netModel2CsvMapper) {

		double transformerPowerReal = 0;
		double transformerPowerImag = 0;

		if (pfc!= null) {
			if(pfc.getPowerOfTransformer().size()>0){
				transformerPowerReal = pfc.getPowerOfTransformer().get(0);
				transformerPowerImag = pfc.getPowerOfTransformer().get(1);
			}
		}
		
		SlackNodeDescription snDesc = null;
		if (netModel2CsvMapper.getSlackNodeVector()!=null && netModel2CsvMapper.getSlackNodeVector().size()>0) {
			 snDesc = netModel2CsvMapper.getSlackNodeVector().get(0);
		}

		// --- Define TechnicalSystemState ----------------------
		TechnicalSystemState tss = new TechnicalSystemState();
		tss.setGlobalTime(globalTimeTo);

		// --- Uni phase -----------------------------------------------
		if (transformerPowerReal != 0 && transformerPowerImag != 0) {
			// --- Active Power  ------------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_P, transformerPowerReal);
			// --- Reactive Power  ----------------------------
			this.addUsageOfInterfaces(tss, TransformerPowerAnswer.TransformerInterface_Q, transformerPowerImag);
		}

		
		if (snDesc.getNetworkComponentID()!=null) {
			this.getTransformerStates().put(snDesc.getNetworkComponentID(), tss);
		}
		
	}


	/**
	 * Sets the sensor technical system states.
	 *
	 * @param globalTimeTo the new sensor technical system states
	 */
	private void setSensorTechnicalSystemStates(long globalTimeTo) {

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
			UniPhaseSensorState sensor = (UniPhaseSensorState) this.getCableStates().get(sensorNetCompID);
			
			// --- Define the 'measurements' --------------------------------------------
			// --- Voltage --------------------------------
			FixedDouble fdVoltage = new FixedDouble();
			fdVoltage.setVariableID("Voltage");
			fdVoltage.setValue(sensor.getMeasuredVoltage().getValue());
			tsseNew.getIOlist().add(fdVoltage);
			
			// --- Current --------------------------------
			FixedDouble fdCurrent = new FixedDouble();
			fdCurrent.setVariableID("Current");
			fdCurrent.setValue(sensor.getCurrent().getValue());
			tsseNew.getIOlist().add(fdCurrent);
			
			// --- Cos Phi --------------------------------
			FixedDouble fdCosPhi = new FixedDouble();
			fdCosPhi.setVariableID("Cos Phi");
			fdCosPhi.setValue(sensor.getCosPhi());
			tsseNew.getIOlist().add(fdCosPhi);
			
			// --- Add to the aggregation as non-real time Schedule -----------
			this.getAggregationHandler().appendToNetworkComponentsScheduleController(sensorNetCompID, tsseNew, false);
		}
	}


	/**
	 * Sets the cable states.
	 *
	 * @param pfc the power flow calculation
	 * @param netModel2CsvMapper the net model 2 csv mapper
	 */
	private void setCableStates(AbstractPowerFlowCalculation pfc, NetworkModelToCsvMapper netModel2CsvMapper) {
		
		Vector<Vector<Double>> iNabs = null;
		Vector<Vector<Double>> iNreal = null;
		Vector<Vector<Double>> iNimag = null;
		Vector<Double> branchCosPhi = null;
		Vector<Double> utili = null;
		Vector<Vector<Double>> p = null;
		Vector<Vector<Double>> q = null;
		Vector<Double> uKReal = null;
		Vector<Double> uKImag = null;
		
		if (pfc != null) {
			iNabs = pfc.getBranchCurrentAbs();
			iNreal = pfc.getBranchCurrentReal();
			iNimag = pfc.getBranchCurrentImag();
			branchCosPhi = pfc.getBranchCosPhi();
			utili = pfc.getBranchUtilization();
			p = pfc.getBranchPowerReal();
			q = pfc.getBranchPowerImag();
			uKReal = pfc.getNodalVoltageReal();
			uKImag = pfc.getNodalVoltageImag();
		}

		// --- Get the reminded BranchDescription's ---------------------------
		Vector<BranchDescription> branchDescriptionVector = netModel2CsvMapper.getBranchDescription(); 
		for (int i=0; i<branchDescriptionVector.size(); i++) {
			
			BranchDescription bd = branchDescriptionVector.get(i);
			NetworkComponent netComp = bd.getNetworkComponent();
			
			int nodeIndexFrom = bd.getNodeNumberFrom()-1;
			int nodeIndexTo = bd.getNodeNumberTo()-1;

			Object[] dataModel = null;
			UniPhaseCableState cableState = null;//TODO Change to UniPhaseElectricalEdgeState as soon as the NetworkComponentStates are switched to the new ontology
			// --- Get the current data model of the branch element -----------
			if (netComp.getDataModel() == null) {
				dataModel = new Object[3];
			} else {
				dataModel = (Object[]) netComp.getDataModel();
				cableState = (UniPhaseCableState) dataModel[1];
			}
			// --- In case that no element was set yet ------------------------
			if (cableState == null) {
				if (netComp.getType().equalsIgnoreCase("Breaker")) {
					cableState = new UniPhaseCableWithBreaker();
				} else if (netComp.getType().equalsIgnoreCase("Sensor")) {
					cableState = new UniPhaseSensorState();
				} else {
					cableState = new UniPhaseCableState();
				}
			}
			
			p.get(nodeIndexFrom).set(nodeIndexTo, p.get(nodeIndexFrom).get(nodeIndexTo) * 3); //Adjustment due to uni-phase powerflow calculation
			q.get(nodeIndexFrom).set(nodeIndexTo, q.get(nodeIndexFrom).get(nodeIndexTo) * 3); //Adjustment due to uni-phase powerflow calculation
			
			cableState.setCurrent(new UnitValue(iNabs.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.setCurrentReal(new UnitValue(iNreal.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.setCurrentImag(new UnitValue(iNimag.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "A"));
			cableState.setCosPhi(branchCosPhi.get(i).floatValue());
			cableState.setUtilization(utili.get(i).floatValue());
			cableState.setP(new UnitValue(p.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "W")); 
			cableState.setQ(new UnitValue(q.get(nodeIndexFrom).get(nodeIndexTo).floatValue(), "var"));
			
			// --- Calculate cable losses -------------------------------------
			double ukRealNode1 = uKReal.get(nodeIndexFrom);
			double ukImagNode1 = uKImag.get(nodeIndexFrom);
			double ukRealNode2 = uKReal.get(nodeIndexTo);
			double ukImagNode2 = uKImag.get(nodeIndexTo);
			CableLosses cableLossesL1 = new CableLosses(cableState.getCurrent().getValue(), cableState.getCosPhi(), ukRealNode1, ukImagNode1, ukRealNode2, ukImagNode2);
			cableState.setLossesP(cableLossesL1.getLossesP());
			cableState.setLossesQ(cableLossesL1.getLossesQ());
			
			
			// --- Set voltage to sensors -------------------------------------
			if (cableState instanceof UniPhaseSensorState) {

				SensorProperties sensorProperties = (SensorProperties) dataModel[0];
				String measureLocationNetComp = sensorProperties.getMeasureLocation();
				if (measureLocationNetComp==null) {
					System.err.println("[" + this.getClass().getSimpleName() + "] No measure location was set for Sensor '" + netComp.getId() + "'!");
					
				} else {
					
					UniPhaseSensorState sensorState = (UniPhaseSensorState) cableState;
					NetworkComponent netCompMeasurement = this.getNetworkModel().getNetworkComponent(measureLocationNetComp);
					Vector<GraphElement> graphNodeMeasurement = this.getNetworkModel().getGraphElementsFromNetworkComponent(netCompMeasurement);
					if (graphNodeMeasurement.size()==1) {
						String graphNodeID = graphNodeMeasurement.get(0).getId();
						UniPhaseElectricalNodeState tpNodeState = (UniPhaseElectricalNodeState) this.getNodeStates().get(graphNodeID);
						if (tpNodeState!=null) {
							sensorState.setMeasuredVoltage(tpNodeState.getVoltageAbs());
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
	 * Sets the node states.
	 *
	 * @param pfc the power flow calculation
	 * @param netModel2CsvMapper the net model 2 csv mapper
	 */
	@SuppressWarnings("unchecked")
	private void setNodeStates(AbstractPowerFlowCalculation pfc, NetworkModelToCsvMapper netModel2CsvMapper) {

		Vector<Double> uKabs = null;
		Vector<Double> uKReal = null;
		Vector<Double> uKImag = null;
		Vector<Double> cosPhi = null;
		Vector<Double> nodalPowerReal= null;
		Vector<Double> nodalPowerImag= null;
		
		if (pfc != null) {
			uKabs = pfc.getNodalVoltageAbs();
			cosPhi = pfc.getNodalCosPhi();
			nodalPowerReal= pfc.getNodalPowerReal();
			nodalPowerImag= pfc.getNodalPowerImag();
			uKReal=pfc.getNodalVoltageReal();
			uKImag=pfc.getNodalVoltageImag();
		}

		// --- Check all calculation results ----------------------------------
		String domain = this.getSubNetworkConfiguration().getDomain();
		for (int i = 0; i < uKabs.size(); i++) {

			int rowNumber = i+1;
			GraphNode graphNode = netModel2CsvMapper.getNodeNumberToGraphNode().get(rowNumber);
			Object[] dataModelArray= null;
			TreeMap<String , Object> dataModeTreeMap = null;
			UniPhaseElectricalNodeState upNodeState = null;

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
						upNodeState = (UniPhaseElectricalNodeState) dataModelArray[1];
					}
				} else {
					// --- Single domain data model ---------------------------
					dataModelArray = (Object[]) graphNode.getDataModel();
					upNodeState = (UniPhaseElectricalNodeState) dataModelArray[1];	
				}
				
			}
			
			// --- In case that no element was set yet ------------------------
			if (upNodeState==null) {
				upNodeState = new UniPhaseElectricalNodeState();
			}
			if (uKabs != null) {
				nodalPowerReal.set(i, nodalPowerReal.get(i) * 3); //Adjustment due to uni-phase powerflow calculation
				nodalPowerImag.set(i, nodalPowerImag.get(i) * 3); //Adjustment due to uni-phase powerflow calculation
				upNodeState = new UniPhaseElectricalNodeState();
				upNodeState.setVoltageAbs(new UnitValue(uKabs.get(i).floatValue(), "V"));
				upNodeState.setCosPhi(cosPhi.get(i).floatValue());
				upNodeState.setP(new UnitValue(nodalPowerReal.get(i).floatValue(), "W"));
				upNodeState.setQ(new UnitValue(nodalPowerImag.get(i).floatValue(), "var"));
				upNodeState.setVoltageReal(new UnitValue(uKReal.get(i).floatValue(), "V"));
				upNodeState.setVoltageImag(new UnitValue(uKImag.get(i).floatValue(), "V"));
				upNodeState.setCurrent(new UnitValue((float)(Math.sqrt(Math.pow(nodalPowerReal.get(i), 2)+Math.pow(nodalPowerImag.get(i), 2))/uKabs.get(i)) / 3, "A")); //Adjusted to show only the current of a single phase.
				upNodeState.setS(new UnitValue((float)(Math.sqrt(Math.pow(nodalPowerReal.get(i), 2)+Math.pow(nodalPowerImag.get(i), 2))), "VA"));

			}

			// --- Finally, set new data model --------------------------------
			//TODO properties required?
			dataModelArray[1] = upNodeState;
			
			// --- Remind this result -----------------------------------------
			this.getNodeStates().put(graphNode.getId(), upNodeState);
		}
	}
}
