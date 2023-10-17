package de.enflexit.ea.electricity.aggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.ontology.UniPhaseSlackNodeState;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper.SetupType;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper.SlackNodeDescription;
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
	
	private boolean shutdown = false;
	
	private Phase phase;
	private OptionModelController optionModelController;
	private AbstractElectricalNetworkCalculationStrategy calculationStrategy; 
	
	private HashMap<Integer, ActiveReactivePowerPair> powerPairHashMap;
	private String transformerNetworkComponentID;
	
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
				// --- Get the SlackNodeState --------------
				UniPhaseSlackNodeState upsns = this.calculationStrategy.getSlackNodeHandler().getSlackNodeState(this.phase);
				powerFlowParameter = new PowerFlowParameter(matNodeSetup, matGridData, nSlackNode, upsns.getVoltageReal().getValue(), upsns.getVoltageImag().getValue());
				// --- Set the transformer/slack node to the parameter ------------
				if (slackNodeVector!=null) {
					if (slackNodeVector.size() > 1) {
						System.err.println("[" + this.getClass().getSimpleName() + "]=> More than one slack node was found for the current network - just use the first node for the PowerFlowCalculation!");
						powerFlowParameter.setnSlackNode(slackNodeVector.get(0).getNodeNumber());
					}
				}
			}
		}
		return powerFlowParameter;
	}
	
	/**
	 * Sets the slack node voltage level.
	 * @param slackNodeState the new slack node voltage level
	 */
	public void setSlackNodeVoltageLevel(UniPhaseSlackNodeState slackNodeState) {
		PowerFlowParameter powerFlowPara = this.getPowerFlowParameter();
		if (powerFlowPara!=null) {
			// --- For debugging, set true ------
			boolean debug = false;
			if (debug==true) System.out.println("[" + this.getClass().getSimpleName() + "] " + phase.name() + " New slack node voltage real: " + slackNodeState.getVoltageReal().getValue() + ", imaginary: " + slackNodeState.getVoltageImag().getValue() + "");
			powerFlowPara.setdSlackVoltageImag(slackNodeState.getVoltageImag().getValue());
			powerFlowPara.setdSlackVoltageReal(slackNodeState.getVoltageReal().getValue());
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
				powerFlowCalculation = (AbstractPowerFlowCalculation) this.calculationStrategy.getSubNetworkConfiguration().getUserClassInstance(POWER_FLOW_CALCULATION_CLASS);
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
					// --- Do the actual calculation now ------------ 
					HashMap<Integer, ActiveReactivePowerPair> powerPairs = this.getPowerPairsForPhase(this.currentParentNode, this.efmGroup);
					if (this.getPowerFlowCalculation()!=null) {
						this.getPowerFlowCalculation().setNodalPower(powerPairs);
						this.getPowerFlowParameter().setNodalPowerReal(this.getPowerFlowCalculation().getNodalPowerReal());
						this.getPowerFlowParameter().setNodalPowerImag(this.getPowerFlowCalculation().getNodalPowerImag());
						if (this.pvNodes!=null) {
							this.getPowerFlowParameter().setvPVNodes(this.pvNodes);
						}
						
						if (this.estimatedBranchCurrents!=null) {
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
	 * @param fmGroup the EnergyFlowsMeasuredGroup
	 * @return the node to power hash
	 */
	private HashMap<Integer, ActiveReactivePowerPair> getPowerPairsForPhase(DefaultMutableTreeNode currentParentNode, FlowsMeasuredGroup fmGroup) {
		
		if (powerPairHashMap==null) {
			powerPairHashMap = new HashMap<Integer, ActiveReactivePowerPair>();	
		}
		
		// --- Reminder for the transformer power -------------------------------------------------
		ActiveReactivePowerPair transformerPowerPair = null;

		// --- Iterate over all known nodes that require information ------------------------------
		HashMap<Integer, String> nodeNumberToNetCompIdHashMap = this.getNetworkModelToCsvMapper().getNodeNumberToNetworkComponentId();
		List<Integer> nodeNumberList = new ArrayList<>(nodeNumberToNetCompIdHashMap.keySet());
		for (Integer nodeNumber : nodeNumberList) {
			
			// --- Get required information for the further proceeding ----------------------------
			String networkID = nodeNumberToNetCompIdHashMap.get(nodeNumber);
			boolean isTransfomer = this.isTransformer(networkID);

			double activePower   = 0;
			double reactivePower = 0;
			
			// --- Try to get the node of the tree to find the energy flow ------------------------
			DefaultMutableTreeNode treeNode = this.calculationStrategy.getGroupController().getGroupTreeModel().getGroupTreeNodeByNetworkID(networkID);
			if (treeNode!=null) {
				
				FlowsMeasuredGroupMember efmGrouMember = fmGroup.getFlowsMeasuredGroupMember(treeNode);
				if (efmGrouMember!=null) {
					// ----------------------------------------------------------------------------
					// --- Found measurements for node --------------------------------------------
					// --- Cumulate the Energy Flow in Watt ---------------------------------------
					// ----------------------------------------------------------------------------
					ArrayList<AbstractFlowMeasuredAtInterface> afmArray = efmGrouMember.getFlowMeasuredAtInterfaceByDomain(EnergyCarrier.ELECTRICITY.value());
					for (AbstractFlowMeasuredAtInterface afmInterface : afmArray) {
						
						FlowMeasuredAtInterfaceEnergy efmInterface = (FlowMeasuredAtInterfaceEnergy) afmInterface;
						DefaultDomainModelElectricity domainModel = (DefaultDomainModelElectricity) efmInterface.getDomainModel();
						
						// --- Get SubNetworkConfiguration for rated voltage ----------------------
						AbstractElectricalNetworkConfiguration aenc = (AbstractElectricalNetworkConfiguration) this.calculationStrategy.getSubNetworkConfiguration();
						
						// --- Use Energy Flows from the correct domain ---------------------------
						if (aenc.getConfiguredRatedVoltageFromNetwork() == domainModel.getRatedVoltage()) {
							if (domainModel.getPhase()==this.phase) {
								if (domainModel.getPowerType()==PowerType.ActivePower) {
									activePower = this.getAverageEnergyFlowInWatt(efmInterface.getEnergyFlowMeasured());
									if (domainModel.getPhase()==Phase.AllPhases) {
										// --- Adjustment due to uni-phase power flow calculation -
										activePower = activePower / 3; 
									}
									
								} else if (domainModel.getPowerType()==PowerType.ReactivePower) {
									reactivePower = this.getAverageEnergyFlowInWatt(efmInterface.getEnergyFlowMeasured());
									if (domainModel.getPhase()==Phase.AllPhases) {
										// --- Adjustment due to uni-phase power flow calculation -
										reactivePower = reactivePower / 3; 
									}
								}
							}
						}
					}
				}
			}
			
			// --- Add to node power Hash ---------------------------------------------------------
			ActiveReactivePowerPair nodePowerPair = new ActiveReactivePowerPair(activePower, reactivePower);
			if (isTransfomer==true) {
				// --- Remind for the subsequent sum-up ------------------------------------------- 
				transformerPowerPair = nodePowerPair;
			}
			powerPairHashMap.put(nodeNumber, nodePowerPair);
		}
		
		// --- Update the transformer power pair ------------------------------
		this.updateTransformerPowerPair(powerPairHashMap, transformerPowerPair);
		
		return powerPairHashMap;
	}
	
	/**
	 * Updates the transformer power pair by sum-up all other power pairs.
	 *
	 * @param powerPairHashMap the power pair hash map
	 * @param transformerPowerPair the transformer power pair
	 */
	private void updateTransformerPowerPair(HashMap<Integer, ActiveReactivePowerPair> powerPairHashMap, ActiveReactivePowerPair transformerPowerPair) {
		
		if (transformerPowerPair==null) return;
		if (powerPairHashMap==null || powerPairHashMap.size()==0) return;
		
		double activePower   = 0.0;
		double reactivePower = 0.0;

		for (ActiveReactivePowerPair nodePowerPair : powerPairHashMap.values()) {
			// --- Skip the transformer -----------------------------
			if (nodePowerPair==transformerPowerPair) continue;
			activePower   += nodePowerPair.getActivePowerInWatt();
			reactivePower += nodePowerPair.getReactivePowerInWatt(); 
		}
		
		// --- Set the recalculated values to the power pair --------
		transformerPowerPair.setActivePowerInWatt(activePower);
		transformerPowerPair.setReactivePowerInWatt(reactivePower);
	}
	
	/**
	 * Checks if the specified ID of a {@link NetworkComponent} belongs to the networks transformer.
	 *
	 * @param networkComponentID the network component ID
	 * @return true, if is transformer
	 */
	private boolean isTransformer(String networkComponentID) {
		if (networkComponentID==null) return false;
		if (transformerNetworkComponentID==null) {
			// --- Fast exit ? --------------------------------
			if (this.getNetworkModelToCsvMapper().getSlackNodeVector().size()==0) return false;
			// --- Check the SlackNodeDescription -------------
			SlackNodeDescription snDesc = this.getNetworkModelToCsvMapper().getSlackNodeVector().get(0);
			if (snDesc==null || snDesc.getNetworkComponentID()==null) return false;
			transformerNetworkComponentID = snDesc.getNetworkComponentID();
		}
		return networkComponentID.equals(transformerNetworkComponentID); 
	}
	
	/**
	 * Returns the average energy flow in watt as a double value.
	 *
	 * @param measuredEnergyFlow the measured energy flow
	 * @return the average energy flow in watt
	 */
	private double getAverageEnergyFlowInWatt(EnergyFlowMeasured measuredEnergyFlow) {
		EnergyFlowInWatt efiw = this.optionModelController.getEnergyFlowInWattAverage(measuredEnergyFlow);
		return UnitConverter.convertEnergyFlowToWatt(efiw);
	}

	/**
	 * Returns the estimated branch currents.
	 * @return the estimated branch currents
	 */
	public HashMap<String, MeasuredBranchCurrent> getEstimatedBranchCurrents() {
		return estimatedBranchCurrents;
	}
	/**
	 * Sets the estimated branch currents.
	 * @param estimatedBranchCurrents the estimated branch currents
	 */
	public void setEstimatedBranchCurrents(HashMap<String, MeasuredBranchCurrent> estimatedBranchCurrents) {
		this.estimatedBranchCurrents = estimatedBranchCurrents;
	}
	
}
