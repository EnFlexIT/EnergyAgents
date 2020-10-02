package de.enflexit.ea.electricity.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper.SetupType;
import de.enflexit.ea.core.dataModel.csv.NetworkModelToCsvMapper.SlackNodeDescription;
import de.enflexit.ea.lib.powerFlowCalculation.AbstractPowerFlowCalculation;
import de.enflexit.ea.lib.powerFlowCalculation.ActiveReactivePowerPair;
import de.enflexit.ea.lib.powerFlowCalculation.MeasuredBranchCurrent;
import de.enflexit.ea.lib.powerFlowCalculation.PVNodeParameters;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowCalculation;
import de.enflexit.ea.lib.powerFlowCalculation.PowerFlowParameter;
import energy.OptionModelController;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.helper.UnitConverter;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyFlowMeasured;
import energygroup.GroupTreeNodeObject;
import energygroup.calculation.AbstractFlowMeasuredAtInterface;
import energygroup.calculation.FlowMeasuredAtInterfaceEnergy;
import energygroup.calculation.FlowsMeasuredGroup;
import energygroup.calculation.FlowsMeasuredGroupMember;

/**
 * The Class PowerFlowCalculationThread is used within the aggregation handler
 * in order to concurrently execute the power flow calculation for each {@link Phase}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PowerFlowCalculationThread extends Thread {

	public static final String POWER_FLOW_CALCULATION_CLASS = "PowerFlowCalculationClass";
	
	private boolean debug = false;
	private boolean shutdown = false;
	
	private Phase phase;
	private OptionModelController optionModelController;
	private AbstractElectricalNetworkCalculationStrategy calculationStrategy; 
	
	private HashMap<Integer, ActiveReactivePowerPair> powerPairHash;
	
	private PowerFlowParameter powerFlowParameter;
	private AbstractPowerFlowCalculation powerFlowCalculation;
	
	private boolean successfulPowerFlowCalculation;
	
	private DefaultMutableTreeNode currentParentNode;
	private FlowsMeasuredGroup efmGroup;

	private Vector<PVNodeParameters> pvNodes; 
	private HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents;
	
	
	/**
	 * Instantiates a new power flow calculation thread.
	 *
	 * @param calculationStrategy the calculation strategy
	 * @param phase the phase to calculate
	 * @param optionModelController the option model controller of the aggregation
	 */
	public PowerFlowCalculationThread(AbstractElectricalNetworkCalculationStrategy calculationStrategy, Phase phase, OptionModelController optionModelController) {
		super();
		this.calculationStrategy = calculationStrategy;
		this.phase = phase;
		this.optionModelController = optionModelController;
	}
	
	/**
	 * Returns the local {@link NetworkModelToCsvMapper}.
	 * @return the NetworkModelToCsvMapper
	 */
	public NetworkModelToCsvMapper getNetworkModelToCsvMapper() {
		return this.calculationStrategy.getNetworkModelToCsvMapper();
	}
	
	/**
	 * Returns the {@link PowerFlowParameter} for the {@link PowerFlowCalculation}.
	 * @return the power flow parameter
	 */
	public PowerFlowParameter getPowerFlowParameter() {
		if (powerFlowParameter==null) {
			
			double[][] matNodeSetup = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.NodeSetup);
			double[][] matGridData  = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.BranchSetup);
			Vector<SlackNodeDescription> slackNodeVector = this.getNetworkModelToCsvMapper().getSlackNodeVector();
			
			int nSlackNode = 0;
			if (slackNodeVector!=null && slackNodeVector.size()>0) {
				nSlackNode = slackNodeVector.get(0).getNodeNumber();
			}
			
			if (matNodeSetup!=null & matGridData!=null) {
				powerFlowParameter = new PowerFlowParameter(matNodeSetup, matGridData, nSlackNode, this.calculationStrategy.getDefaultSlackNodeVoltage());
				// --- Set the transformer/slack node to the parameter ------------
				if (slackNodeVector!=null) {
					if (slackNodeVector.size() > 1) {
						System.err.println("=> More than one slack node was found for the current network - just use the first node for the PowerFlowCalculation!");
						powerFlowParameter.setnSlackNode(slackNodeVector.get(0).getNodeNumber());
					}
				}
			}
		}
		return powerFlowParameter;
	}
	/**
	 * Sets the slack node voltage level.
	 * @param newVoltageLevel the new slack node voltage level
	 */
	public void setSlackNodeVoltageLevel(double newVoltageLevel) {
		PowerFlowParameter powerFlowPara = this.getPowerFlowParameter();
		if (powerFlowPara!=null) {
			powerFlowPara.setdSlackVoltage(newVoltageLevel);
		}
	}
	/**
	 * Sets the measured pv voltage.
	 * @param pVVoltageVector the new measured pv voltage
	 */
	public void setMeasuredPvVoltage(Vector<PVNodeParameters> pVVoltageVector) {
		this.pvNodes = pVVoltageVector;
	}
	
	/**
	 * Gets the {@link PowerFlowCalculation} for the current thread.
	 * @return the power flow calculation
	 */
	public AbstractPowerFlowCalculation getPowerFlowCalculation() {
		if (powerFlowCalculation==null) {
			PowerFlowParameter powerFlowPara = this.getPowerFlowParameter();
			if (powerFlowPara!=null) {
				powerFlowCalculation = (AbstractPowerFlowCalculation) this.calculationStrategy.getSubAggregationConfiguration().getUserClassInstance(POWER_FLOW_CALCULATION_CLASS);
				powerFlowCalculation.setPowerFlowParameter(powerFlowPara);
			}
		}
		return powerFlowCalculation;
	}
	
	/**
	 * Terminates this {@link PowerFlowCalculationThread}.
	 */
	public void terminate() {
		this.shutdown = true;	
	}
	
	/**
	 * Resets the calculation base.
	 *
	 * @param currentParentNode the current parent node
	 * @param efmGroup the overall EnergyFlowsMeasuredGroup for all sub nodes
	 */
	public void resetCalculationBase(DefaultMutableTreeNode currentParentNode, FlowsMeasuredGroup efmGroup) {
		this.currentParentNode = currentParentNode;
		this.efmGroup = efmGroup;
	}

	/**
	 * Checks if the last round had a successful {@link PowerFlowCalculation}.
	 * @return true, if successful power flow calculation
	 */
	public boolean isSuccessfulPowerFlowCalculation() {
		return successfulPowerFlowCalculation;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		while(true) {
			
			try {
				// --- Try-Catch block for the calculation ----------
				if (this.currentParentNode!=null && this.efmGroup!=null) {
					//TODO: remove this debugging block
					if (this.calculationStrategy.getAggregationHandler().getClass().getSimpleName().equals("DecisionAggregationHandler") && this.debug ) {
						System.out.println("executing calculation for DecionAgrHandler "+this);
					}
					// --- Do the actual calculation now ------------ 
					HashMap<Integer, ActiveReactivePowerPair> powerPairs = this.getPowerPairsForPhase(this.currentParentNode, this.efmGroup);
					if (this.getPowerFlowCalculation()!=null) {
						this.getPowerFlowCalculation().setNodalPower(powerPairs);
						this.getPowerFlowParameter().setNodalPowerReal(this.getPowerFlowCalculation().getNodalPowerReal());
						this.getPowerFlowParameter().setNodalPowerImag(this.getPowerFlowCalculation().getNodalPowerImag());
						if (this.pvNodes!=null) {
							this.getPowerFlowParameter().setvPVNodes(this.pvNodes);
						}
						
						if(this.estimatedBranchCurrents!=null) {
							HashMap<String, MeasuredBranchCurrent> temp = this.integrateFromNodeToNodeInBranchCurrents(this.estimatedBranchCurrents);
							this.setEstimatedBranchCurrents(temp);
							this.getPowerFlowParameter().setEstimatedBranchCurrents(temp);
						}
						
						this.getPowerFlowCalculation().setPowerFlowParameter(this.getPowerFlowParameter());
						
						// --- Checking if preconditions for calculation are ok
						if (this.getPowerFlowCalculation().checkPreconditionsForCalculation()==true) {
							this.successfulPowerFlowCalculation = this.getPowerFlowCalculation().calculate();
						}
					}
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				this.calculationStrategy.setCalculationFinalized(this.phase);
			}
				
			try {
				// --- Check if this Thread has to be finalized -----
				if (this.shutdown==true) break;
				// --- Wait for the next restart call --------------- 
				synchronized (this.calculationStrategy.getCalculationTrigger()) {
					this.calculationStrategy.getCalculationTrigger().wait();	
				}
				// --- Check if this Thread has to be finalized -----
				if (this.shutdown==true) break;
				
			} catch (IllegalMonitorStateException imse) {
				// imse.printStackTrace();
			} catch (InterruptedException ie) {
				// ie.printStackTrace();
			}
			
		} // end while
	}
	
	private HashMap<String, MeasuredBranchCurrent> integrateFromNodeToNodeInBranchCurrents(HashMap<String, MeasuredBranchCurrent> tempEstimatedBranchCurrents) {
		ArrayList<String> keySet = new ArrayList<>(tempEstimatedBranchCurrents.keySet());
		
		for(int i=0;i<keySet.size();i++) {
			String cableName = keySet.get(i);
			String nFromNodeName = tempEstimatedBranchCurrents.get(cableName).getnFromNodeComponentName();
			String nToNodeName = tempEstimatedBranchCurrents.get(cableName).getnToNodeComponentName();
			
			int nFromNode = this.getNetworkModelToCsvMapper().getNetworkComponentIdToNodeNumber().get(nFromNodeName);
			int nToNode = this.getNetworkModelToCsvMapper().getNetworkComponentIdToNodeNumber().get(nToNodeName);
			tempEstimatedBranchCurrents.get(cableName).setnFromNode(nFromNode);
			tempEstimatedBranchCurrents.get(cableName).setnToNode(nToNode);
		}
			
		
		return tempEstimatedBranchCurrents;
	}
	
	/**
	 * Returns the current node to power pair hash map that is determined in the child's of the current parent node.
	 *
	 * @param currentParentNode the current parent node
	 * @param pahse the current Phase to use
	 * @param fmGroup the EnergyFlowsMeasuredGroup
	 * @return the node to power hash
	 */
	private HashMap<Integer, ActiveReactivePowerPair> getPowerPairsForPhase(DefaultMutableTreeNode currentParentNode, FlowsMeasuredGroup fmGroup) {
		
		if (powerPairHash==null) {
			powerPairHash = new HashMap<Integer, ActiveReactivePowerPair>();	
		}
		
		int numberOfChildren = currentParentNode.getChildCount();
		for (int i=0; i<numberOfChildren; i++) {
			// --- Get the tree nodes network ID ------------------------------
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) currentParentNode.getChildAt(i);
			GroupTreeNodeObject gtno = (GroupTreeNodeObject) treeNode.getUserObject();
			String networkID = gtno.getGroupMember().getNetworkID();
			Integer nodeNumber = this.getNetworkModelToCsvMapper().getNetworkComponentIdToNodeNumber().get(networkID);
			if (nodeNumber==null) continue;
			
			double activePower= 0;
			double reactivePower = 0;
			
			FlowsMeasuredGroupMember efmGrouMember = fmGroup.getFlowsMeasuredGroupMember(treeNode);
			if (efmGrouMember!=null) {
				// ------------------------------------------------------------
				// --- Found measurements for node ----------------------------
				// --- Cumulate the Energy Flow in Watt -----------------------
				// ------------------------------------------------------------				
				ArrayList<AbstractFlowMeasuredAtInterface> afmArray = efmGrouMember.getFlowMeasuredAtInterfaceByDomain(EnergyCarrier.ELECTRICITY.value());
				for (AbstractFlowMeasuredAtInterface afmInterface : afmArray) {
					
					FlowMeasuredAtInterfaceEnergy efmInterface = (FlowMeasuredAtInterfaceEnergy) afmInterface;
					
					DefaultDomainModelElectricity domainModel = (DefaultDomainModelElectricity) efmInterface.getDomainModel();
					if (domainModel.getPhase()==this.phase) {
						if (domainModel.getPowerType()==PowerType.ActivePower) {
							activePower = this.getAverageEnergyFlowInWatt(efmInterface.getEnergyFlowMeasured());
							if (domainModel.getPhase()==Phase.AllPhases) {
								activePower = activePower / 3; //Adjustment due to uni-phase powerflow calculation
							}
							
						} else if (domainModel.getPowerType()==PowerType.ReactivePower) {
							reactivePower = this.getAverageEnergyFlowInWatt(efmInterface.getEnergyFlowMeasured());
							if (domainModel.getPhase()==Phase.AllPhases) {
								reactivePower = reactivePower / 3; //Adjustment due to uni-phase powerflow calculation
							}
						}
					}
				}
				
			}
			// --- Add to node power Hash -------------------------------------
			powerPairHash.put(nodeNumber, new ActiveReactivePowerPair(activePower, reactivePower));
		}
		return powerPairHash;
	}
	
	/**
	 * Gets the average energy flow in watt as a double value.
	 *
	 * @param measuredEnergyFlow the measured energy flow
	 * @return the average energy flow in watt
	 */
	private double getAverageEnergyFlowInWatt(EnergyFlowMeasured measuredEnergyFlow) {
		EnergyFlowInWatt efiw = this.optionModelController.getEnergyFlowInWattAverage(measuredEnergyFlow);
		return UnitConverter.convertEnergyFlowToWatt(efiw);
	}

	public HashMap<String, MeasuredBranchCurrent> getEstimatedBranchCurrents() {
		return estimatedBranchCurrents;
	}

	public void setEstimatedBranchCurrents(HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents) {
		this.estimatedBranchCurrents = estimatedBranchCurrents;
	}
	
}
