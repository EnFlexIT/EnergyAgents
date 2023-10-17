package de.enflexit.ea.electricity.aggregation.taskThreading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.electricity.NetworkModelToCsvMapper;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricitySubNetworkGraph.SubNetworkConnection;
import de.enflexit.ea.electricity.aggregation.taskThreading.ElectricitySubNetworkGraph.SubNetworkGraphNode;
import energy.helper.NumberHelper;

/**
 * The Class ElectricityTaskThreadCoordinator organizes the electrical network calculations.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ElectricityTaskThreadCoordinator extends AbstractTaskThreadCoordinator {

	public static final int ROUND_PRECISION = 5;
	
	private boolean isDebug = false;
	
	private double deltaVoltageRelAvgMax = 0.01; // --- 1% ---
	private int loopCounterMax = 3;
	
	private ArrayList<AbstractElectricalNetworkConfiguration> elSubNetConfigList;
	private ElectricitySubNetworkGraph subNetworksGraph;


	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#initialize()
	 */
	@Override
	public void initialize() {
		this.getSubNetworkGraph();
	}
	/**
	 * Return the sub network graph.
	 * @return the sub network graph
	 */
	protected ElectricitySubNetworkGraph getSubNetworkGraph() {
		if (subNetworksGraph==null) {
			subNetworksGraph = new ElectricitySubNetworkGraph(this);
		}
		return subNetworksGraph;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#getSubNetworkConfigurationUnderControl()
	 */
	@Override
	public List<AbstractElectricalNetworkConfiguration> getSubNetworkConfigurationsUnderControl() {
		if (elSubNetConfigList==null) {
			elSubNetConfigList = new ArrayList<>();
			ArrayList<AbstractSubNetworkConfiguration> subNetConfigList = this.getAggregationHandler().getSubNetworkConfigurations();
			for (AbstractSubNetworkConfiguration subNetConfig : subNetConfigList) {
				if (subNetConfig instanceof AbstractElectricalNetworkConfiguration) {
					elSubNetConfigList.add((AbstractElectricalNetworkConfiguration) subNetConfig);
				}
			}
		}
		return elSubNetConfigList;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractTaskThreadCoordinator#doTaskRunEvaluationUntil(long, boolean)
	 */
	@Override
	protected void doTaskRunEvaluationUntil(long timeUntil, boolean rebuildDecisionGraph) {
		
		// --- Define start values for the iteration --------------------------
		double deltaVoltageRelAvg = 1.0; // -- 100 % --
		int loopCounter = 0; 

		// --- Calculate isolated electrical networks first and once ----------
		this.calculateIsolatedElectricalNetworks(timeUntil, rebuildDecisionGraph);
		
		// --- Loop -----------------------------------------------------------
		while (deltaVoltageRelAvg>=this.deltaVoltageRelAvgMax && loopCounter<this.loopCounterMax) {

			this.debugPrint("Loop round no. " + (loopCounter + 1) + ": Relative delta voltage AVG: " + deltaVoltageRelAvg, false, loopCounter==0);
			// --- Execute network calculations -------------------------------
			deltaVoltageRelAvg = this.calculateConnectedNetworks(timeUntil, rebuildDecisionGraph);
			loopCounter++;
		}
		
		// --- Move so far calculated Transformer TSSE to second schedule -----
		this.finalizeTransformerCalculation(); 
		
		// --- Finally call to update the SubBlackboardModels -----------------
		this.doTaskUpdateSubBlackboardModel();
	}
	
	/**
	 * Does the network calculation for connected electrical networks.
	 *
	 * @param timeUntil the time until
	 * @param rebuildDecisionGraph the rebuild decision graph
	 * @return the average of all relative delta voltage (delta / rated voltage)  
	 */
	private double calculateConnectedNetworks(long timeUntil, boolean rebuildDecisionGraph) {

		// --- Get the voltage level to consider ------------------------------
		List<Double> voltageLevelList = new ArrayList<>(this.getSubNetworkGraph().getVoltageLevelList());
		
		// --- Iterate over the voltage level ---------------------------------
		for (Double voltageLevel : voltageLevelList) {

			// --- Get Nodes for this voltage level from the graph ------------
			List<SubNetworkGraphNode> networksAtVoltageLevel = this.getSubNetworkGraph().getConnectedSubNetworkGraphNodes(voltageLevel);
			if (networksAtVoltageLevel.size()==0) continue;
			
			// --- Set task threads to execute network calculations -----------
			networksAtVoltageLevel.forEach(subNetGraphNode -> this.setTaskThreadToCalculateSubNetworkGraphNode(subNetGraphNode, timeUntil, rebuildDecisionGraph));
			// --- Execute power flow calculation -----------------------------
			this.startAndWaitForNetworkAggregationTaskThreads();
			
			// --- Do transformer jobs and calculations -----------------------
			for (SubNetworkGraphNode subNetGraphNode : networksAtVoltageLevel) {

				// --- Get transformer node state -----------------------------
				for (SubNetworkConnection subNetConn :  subNetGraphNode.getSubNetworkConnections()) {
					
					try {
						
						String netCompID = subNetConn.getConnectingNetworkComponentID();
						ElectricalNodeState elNodeState = this.getElectricalNodeState(subNetGraphNode, netCompID);
						boolean isLowVoltageSide = this.getSubNetworkGraph().isLowVoltageSide(subNetConn, subNetGraphNode);
						if (isLowVoltageSide==true) {
							subNetConn.setLowVoltageElectricalNodeState(elNodeState);
						} else {
							subNetConn.setHighVoltageElectricalNodeState(elNodeState);
						}
						// --- Calculate the transformer state ----------------
						subNetConn.getTransformerCalculation().updateTransformerState(isLowVoltageSide);
						
					} catch (Exception ex) {
						this.debugPrint("Error updating transformer state for voltage level '" + voltageLevel + " V', " + subNetGraphNode + ", " + subNetConn, true, false);
						ex.printStackTrace();
					}

				}
			} // end for list of SubNetworkGraphNode
		} // end for list of voltageLevel
		
		// --- Calculate average relative voltage delta -----------------------
		double avgRelVoltageDelta = this.getDeltaVoltageRelativeAverage();
		this.debugPrint("=> Average, relative voltage delta: " + NumberHelper.round((avgRelVoltageDelta * 100.0), ROUND_PRECISION) + " %" , false);
		return avgRelVoltageDelta;
	}
	
	/**
	 * Returns the delta voltage relative average.
	 * @return the delta voltage relative average
	 */
	private double getDeltaVoltageRelativeAverage() {

		// --- Sort by sub network --------------
		ArrayList<SubNetworkConnection> subNetConList = new ArrayList<>(this.getSubNetworkGraph().getSubNetConnectionHash().values());
		Collections.sort(subNetConList, new Comparator<SubNetworkConnection>() {
			@Override
			public int compare(SubNetworkConnection snCon1, SubNetworkConnection snCon2) {
				Integer netID11 = snCon1.getSubNetworkID_1();
				Integer netID21 = snCon2.getSubNetworkID_1();
				if (netID11.equals(netID21)==false) {
					Integer netID12 = snCon1.getSubNetworkID_2();
					Integer netID22 = snCon2.getSubNetworkID_2();
					return netID12.compareTo(netID22);
				}
				return netID11.compareTo(netID21);
			}
		});
		
		double deltaSum = 0.0; 
		int step = 0;
		for (SubNetworkConnection subNetConn : subNetConList) {
			// --- Some debug output ------------ 
			this.debugPrint(subNetConn .toString() + ": High Voltage Delta: " + subNetConn.getHighVoltageDeltaPercent() + "%, Low Voltage Delta: " + subNetConn.getLowVoltageDeltaPercent() + "%" );
			deltaSum += subNetConn.getLowVoltageDeltaRelative();
			step++;
		}
		
		if (step==0) {
			return 0;
		}
		return NumberHelper.round((deltaSum / step), ROUND_PRECISION);
	}
	
	
	/**
	 * Returns the electrical node state for the specified NetworkComponent-ID.
	 *
	 * @param subNetGraphNode the sub net graph node
	 * @param netCompID the {@link NetworkComponent}s ID
	 * @return the electrical node state ID
	 */
	private ElectricalNodeState getElectricalNodeState(SubNetworkGraphNode subNetGraphNode, String netCompID) {
		
		NetworkModelToCsvMapper csvMapper = subNetGraphNode.getNetworkCalculationStrategy().getNetworkModelToCsvMapper();
		Integer powerFlowNodeNumber = csvMapper.getNetworkComponentIdToNodeNumber().get(netCompID);
		GraphNode nmGraphNode = csvMapper.getNodeNumberToGraphNode().get(powerFlowNodeNumber);
		
		if (nmGraphNode==null) {
			return null;
		}
		return subNetGraphNode.getNetworkCalculationStrategy().getNodeStates().get(nmGraphNode.getId());
	}
	
	/**
	 * Does the network calculation for isolated electrical networks.
	 *
	 * @param timeUntil the time until
	 * @param rebuildDecisionGraph the rebuild decision graph
	 */
	private void calculateIsolatedElectricalNetworks(long timeUntil, boolean rebuildDecisionGraph) {
		
		// --- Get the isolated SubNetworkGraphNodes from the graph -----------
		List<SubNetworkGraphNode> isolatedNetworks = this.getSubNetworkGraph().getIsolatedSubNetworkGraphNodes();
		if (isolatedNetworks.size()==0) return;
			
		// --- Set task threads to execute network calculations ---------------
		isolatedNetworks.forEach(subNetGraphNode -> this.setTaskThreadToCalculateSubNetworkGraphNode(subNetGraphNode, timeUntil, rebuildDecisionGraph));

		// --- Execute power flow calculation ---------------------------------
		this.startAndWaitForNetworkAggregationTaskThreads();
	}
	
	
	/**
	 * Sets the task thread of the specified SubNetworkGraphNode to calculate sub network graph node.
	 *
	 * @param subNetGraphNode the sub net graph node
	 * @param timeUntil the time until
	 * @param rebuildDecisionGraph the rebuild decision graph
	 */
	private void setTaskThreadToCalculateSubNetworkGraphNode(SubNetworkGraphNode subNetGraphNode, long timeUntil, boolean rebuildDecisionGraph) {
		
		// --- Get corresponding task thread and the strategy -------------
		AbstractNetworkCalculationStrategy networkCalculationStrategy = subNetGraphNode.getNetworkCalculationStrategy();
		if (networkCalculationStrategy!=null) {
			subNetGraphNode.getSubNetworkTaskThread().runEvaluationUntil(timeUntil, rebuildDecisionGraph, false); 
		}
	}
	
	/**
	 * Will be invoked to finalize the transformer calculations.
	 */
	private void finalizeTransformerCalculation() {
		this.getSubNetworkGraph().getSubNetConnectionHash().values().forEach(subNetConn -> subNetConn.getTransformerCalculation().finalizeTransformerCalculation());
	}
	
	
	// ------------------------------------------------------------------------
	// --- Some debug print methods -------------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Prints the specified message.
	 * @param msg the message to print
	 */
	public void debugPrint(String msg) {
		this.debugPrint(msg, false);
	}
	/**
	 * Prints the specified message.
	 *
	 * @param msg the message to print
	 * @param isError the is error
	 */
	public void debugPrint(String msg, boolean isError) {
		this.debugPrint(msg, isError, false);
	}
	/**
	 * Prints the specified message.
	 *
	 * @param msg the message to print
	 * @param isError the is error
	 * @param insertNewLineAhead the insert new line ahead
	 */
	public void debugPrint(String msg, boolean isError, boolean insertNewLineAhead) {
		if (this.isDebug==false || msg==null || msg.isBlank()==true) return;
		if (isError==true) {
			if (insertNewLineAhead==true) System.err.println();
			System.err.println("[" + this.getClass().getSimpleName() + "] " + msg);
		} else {
			if (insertNewLineAhead==true) System.out.println();
			System.out.println("[" + this.getClass().getSimpleName() + "] " + msg);
		}
	}
	
}
