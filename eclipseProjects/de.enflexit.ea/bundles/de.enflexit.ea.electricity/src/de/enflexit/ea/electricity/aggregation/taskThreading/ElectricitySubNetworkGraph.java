package de.enflexit.ea.electricity.aggregation.taskThreading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.NetworkAggregationTaskThread;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.ElectricalNodeStateConverter;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.transformer.TransformerCalculation;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import energy.helper.NumberHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;

/**
 * The Class ElectricitySubNetworkGraph.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ElectricitySubNetworkGraph {

	private ElectricityTaskThreadCoordinator taskThreadCoordinator;
	
	private TreeMap<Integer, SubNetworkGraphNode> subNetGraphNodeHash;
	private TreeMap<String, SubNetworkConnection> subNetConnectionHash;
	
	private List<Double> voltageLevelList;
	
	/**
	 * Instantiates a new electricity sub network graph.
	 * @param taskThreadCoordinator the task thread coordinator
	 */
	public ElectricitySubNetworkGraph(ElectricityTaskThreadCoordinator taskThreadCoordinator) {
		this.taskThreadCoordinator = taskThreadCoordinator;
		this.initialize();
	}
	/**
	 * Initializes.
	 */
	private void initialize() {
		
		List<AbstractElectricalNetworkConfiguration> elSubNetConfigs = this.taskThreadCoordinator.getSubNetworkConfigurationsUnderControl();
		
		// --- Sort descending by voltage level -------------------------------
		Collections.sort(elSubNetConfigs, new Comparator<AbstractElectricalNetworkConfiguration>() {
			@Override
			public int compare(AbstractElectricalNetworkConfiguration c1, AbstractElectricalNetworkConfiguration c2) {
				Double voltatgeLevel1 = c1.getConfiguredRatedVoltageFromNetwork();
				Double voltatgeLevel2 = c2.getConfiguredRatedVoltageFromNetwork();
				return voltatgeLevel2.compareTo(voltatgeLevel1);
			}
		});
		
		// --- Remind possible voltage level ----------------------------------
		this.voltageLevelList = new ArrayList<>();
		for (int i = 0; i < elSubNetConfigs.size(); i++) {
			Double voltageLevel =  elSubNetConfigs.get(i).getConfiguredRatedVoltageFromNetwork();
			if (voltageLevel!=null && this.voltageLevelList.contains(voltageLevel)==false) {
				this.voltageLevelList.add(voltageLevel);
			}
		}
		Collections.sort(this.voltageLevelList);
		
		// --- Create all corresponding nodes first ---------------------------
		List<SubNetworkGraphNode> subNetworkGraphNodeList = new ArrayList<>();
		for (AbstractElectricalNetworkConfiguration elSubNetConfig : elSubNetConfigs) {
			NetworkAggregationTaskThread subNetTaskThread = this.taskThreadCoordinator.getOrCreateNetworkAggregationTaskThread(elSubNetConfig);
			int graphLevel = this.getElectricalGraphLevelByVoltageLevel(elSubNetConfig.getConfiguredRatedVoltageFromNetwork());
			subNetworkGraphNodeList.add(new SubNetworkGraphNode(elSubNetConfig, subNetTaskThread, graphLevel));
		}

		// --- Fill local graph and connection hash maps ----------------------
		for (SubNetworkGraphNode sngn : subNetworkGraphNodeList) {
			
			// --- Remind that sub network graph node -------------------------
			this.getSubNetGraphNodeHash().put(sngn.getSubNetworkID(), sngn);
			
			// --- Check if same NetComp's can be found in other nodes -------- 
			for (SubNetworkGraphNode sngnSearch : subNetworkGraphNodeList) {

				// --- Skip same instance --------------------------------- 
				if (sngnSearch==sngn) continue;

				// --- Search NetworkComponentList ----------------------------
				for (String sngnSearchNetCompID : sngnSearch.getNetworkComponentIDs()) {
					if (sngn.getNetworkComponentListAsString().contains("," + sngnSearchNetCompID + ",")==true) {
						// --- Found ID of connection NetworkComponent --------
						SubNetworkConnection subNetConn = this.getSubNetConnectionHash().get(sngnSearchNetCompID);
						if (subNetConn==null) {
							// --- Remind in local HashMap --------------------
							subNetConn = new SubNetworkConnection(sngn.getSubNetworkID(), sngnSearch.getSubNetworkID(), sngnSearchNetCompID);
							this.getSubNetConnectionHash().put(subNetConn.getConnectingNetworkComponentID(), subNetConn);
							// --- Assign to both graph nodes -----------------
							sngn.getSubNetworkConnections().add(subNetConn);
							sngnSearch.getSubNetworkConnections().add(subNetConn);
						}
					}
				} // end for sngnSearchNetCompID
			} // end for 'sngnSearch'
		} // end for 'sngn'
	}
	
	/**
	 * Returns the list of possible voltage level in a ascending order.
	 * @return the voltage level list
	 */
	public List<Double> getVoltageLevelList() {
		return voltageLevelList;
	}
	/**
	 * Returns the electrical graph level based on the specified voltage level.
	 *
	 * @param voltageLevel the voltage level
	 * @return the electrical graph level or -1
	 */
	public int getElectricalGraphLevelByVoltageLevel(double voltageLevel) {
		return this.getVoltageLevelList().indexOf(voltageLevel);
	}

	/**
	 * Returns the sub net graph node hash (key is the sub network ID of the aggregation handler).
	 * @return the sub net graph node hash
	 */
	public TreeMap<Integer, SubNetworkGraphNode> getSubNetGraphNodeHash() {
		if (subNetGraphNodeHash==null) {
			subNetGraphNodeHash = new TreeMap<>(); 
		}
		return subNetGraphNodeHash;
	}
	/**
	 * Returns the sub net connection hash (key is the connection NetworkComponent ID.
	 * @return the sub net connection hash
	 */
	public TreeMap<String, SubNetworkConnection> getSubNetConnectionHash() {
		if (subNetConnectionHash==null) {
			subNetConnectionHash = new TreeMap<>();
		}
		return subNetConnectionHash;
	}
	
	/**
	 * Returns the connected sub network graph nodes as list.
	 * @return the list of connected sub network graph nodes
	 */
	public List<SubNetworkGraphNode> getConnectedSubNetworkGraphNodes() {
		
		List<SubNetworkGraphNode> connectedNodeList = new ArrayList<>();
		for (SubNetworkGraphNode subNetGraphNode : this.getSubNetGraphNodeHash().values()) {
			if (subNetGraphNode.getSubNetworkConnections().size()>0) {
				connectedNodeList.add(subNetGraphNode);
			}
		}
		return connectedNodeList;
	}
	/**
	 * Returns the connected sub network graph nodes as list.
	 *
	 * @param voltageLevel the voltage level to filter for
	 * @return the list of connected sub network graph nodes
	 */
	public List<SubNetworkGraphNode> getConnectedSubNetworkGraphNodes(double voltageLevel) {
		
		List<SubNetworkGraphNode> connectedNodeList = new ArrayList<>();
		for (SubNetworkGraphNode subNetGraphNode : this.getConnectedSubNetworkGraphNodes()) {
			if (subNetGraphNode.getConfiguredRatedVoltage()==voltageLevel) {
				connectedNodeList.add(subNetGraphNode);
			}
		}
		return connectedNodeList;
	}
	
	/**
	 * Returns the isolated sub network graph nodes as list.
	 * @return the list of isolated sub network graph nodes
	 */
	public List<SubNetworkGraphNode> getIsolatedSubNetworkGraphNodes() {
		
		List<SubNetworkGraphNode> isolatedNodeList = new ArrayList<>();
		for (SubNetworkGraphNode subNetGraphNode : this.getSubNetGraphNodeHash().values()) {
			if (subNetGraphNode.getSubNetworkConnections().size()==0) {
				isolatedNodeList.add(subNetGraphNode);
			}
		}
		return isolatedNodeList;
	}
	
	
	/**
	 * Checks if the specified {@link SubNetworkGraphNode} is on the low voltage side of the specified {@link SubNetworkConnection}.
	 *
	 * @param subNetConn the SubNetworkConnection
	 * @param subNetGraphNode the SubNetworkGraphNode
	 * @return true, if is low voltage side
	 */
	public boolean isLowVoltageSide(SubNetworkConnection subNetConn, SubNetworkGraphNode subNetGraphNode) {
		
		List<SubNetworkGraphNode> subNetworkGraphNodeList = new ArrayList<>();
		subNetworkGraphNodeList.add(this.getSubNetGraphNodeHash().get(subNetConn.getSubNetworkID_1()));
		subNetworkGraphNodeList.add(this.getSubNetGraphNodeHash().get(subNetConn.getSubNetworkID_2()));

		Collections.sort(subNetworkGraphNodeList, new Comparator<SubNetworkGraphNode>() {
			@Override
			public int compare(SubNetworkGraphNode subNetGraphNode1, SubNetworkGraphNode subNetGraphNode2) {
				Double ratedVoltage1 = subNetGraphNode1.getConfiguredRatedVoltage();
				Double ratedVoltage2 = subNetGraphNode2.getConfiguredRatedVoltage();
				return ratedVoltage1.compareTo(ratedVoltage2);
			}
		});
		
		return (subNetworkGraphNodeList.get(0)==subNetGraphNode); 
	}
	/**
	 * Checks if the specified {@link SubNetworkGraphNode} is on the high voltage side of the specified {@link SubNetworkConnection}.
	 *
	 * @param subNetConn the SubNetworkConnection
	 * @param subNetGraphNode the SubNetworkGraphNode
	 * @return true, if is low voltage side
	 */
	public boolean isHighVoltageSide(SubNetworkConnection subNetConn, SubNetworkGraphNode subNetGraphNode) {
		return ! this.isLowVoltageSide(subNetConn, subNetGraphNode);
	}
	
	
	
	// ------------------------------------------------------------------------
	// --- Sub class SubNetworkGraphNode --------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * The Class SubNetworkGraphNode.
	 *
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public class SubNetworkGraphNode {
		
		private int electricalGraphLevel;
		private AbstractElectricalNetworkConfiguration subNetConfig;
		private NetworkAggregationTaskThread subNetTaksThread;
		
		private AbstractElectricalNetworkCalculationStrategy netClacStrategy;
		private String[] networComponentIds;
		private String networComponentList;
		private List<SubNetworkConnection> subNetworkConnectionList;
		
		/**
		 * Instantiates a new sub network graph node.
		 *
		 * @param subNetConfig the sub net configuration
		 * @param subNetTaksThread the sub net taks thread
		 * @param electricalGraphLevel the organizational / electrical graph level (a high number indicates a low voltage level)
		 */
		public SubNetworkGraphNode(AbstractElectricalNetworkConfiguration subNetConfig, NetworkAggregationTaskThread subNetTaksThread, int electricalGraphLevel) {
			this.subNetConfig = subNetConfig;
			this.subNetTaksThread = subNetTaksThread;
			this.electricalGraphLevel = electricalGraphLevel;
		}

		/**
		 * Returns the electrical sub network configuration.
		 * @return the sub network configuration
		 */
		public AbstractElectricalNetworkConfiguration getSubNetworkConfiguration () {
			return this.subNetConfig;
		}
		/**
		 * Returns the sun network task thread.
		 * @return the sun network task thread
		 */
		public NetworkAggregationTaskThread getSubNetworkTaskThread() {
			return this.subNetTaksThread;
		}
		/**
		 * Returns the electrical graph level.
		 * @return the electrical graph level
		 */
		public int getElectricalGraphLevel() {
			return electricalGraphLevel;
		}
		
		/**
		 * Returns the sub network ID.
		 * @return the sub network ID
		 */
		public int getSubNetworkID() {
			return this.subNetConfig.getID();
		}
		/**
		 * Returns the configured rated voltage level of the electrical sub network.
		 * @return the configured rated voltage
		 */
		public double getConfiguredRatedVoltage() {
			return this.getSubNetworkConfiguration().getConfiguredRatedVoltageFromNetwork();
		}
		/**
		 * Returns the network calculation strategy.
		 * @return the network calculation strategy
		 */
		public AbstractElectricalNetworkCalculationStrategy getNetworkCalculationStrategy() {
			if (netClacStrategy==null) {
				netClacStrategy = (AbstractElectricalNetworkCalculationStrategy) this.subNetConfig.getNetworkCalculationStrategy();
			}
			return netClacStrategy;
		}
		/**
		 * Returns the vector of network components that belong to the current sub network.
		 * @return the network components
		 */
		public Vector<NetworkComponent> getNetworkComponents() {
			return this.subNetConfig.getDomainCluster().getNetworkComponents();
		}
		/**
		 * Returns the IDs of all NetworkComponent as array.
		 * @return the network component IDs
		 */
		public String[] getNetworkComponentIDs() {
			if (networComponentIds==null) {
				Vector<NetworkComponent> netCompVector = this.getNetworkComponents();
				networComponentIds = new String[netCompVector.size()];
				for (int i = 0; i < netCompVector.size(); i++) {
					networComponentIds[i] = netCompVector.get(i).getId();
				}
			}
			return networComponentIds;
		}
		/**
		 * Returns the network component list as a comma separated string.
		 * @return the network component list as string
		 */
		public String getNetworkComponentListAsString() {
			if (networComponentList==null) {
				networComponentList = "," + String.join(",", this.getNetworkComponentIDs()) + ",";
			}
			return networComponentList;
		}

		/**
		 * Return all sub network connections to the current {@link SubNetworkGraphNode}.
		 * @return the sub network connections
		 */
		public List<SubNetworkConnection> getSubNetworkConnections() {
			if (subNetworkConnectionList==null) {
				subNetworkConnectionList = new ArrayList<>();
			}
			return this.subNetworkConnectionList;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "SubNet No. " + this.getSubNetworkID() + " (" + this.getConfiguredRatedVoltage() + " V - graphLevel " + this.getElectricalGraphLevel() + "), " + this.getNetworkComponents().size() + " components";
		}
	}
	
	// ------------------------------------------------------------------------
	// --- Sub class SubNetworkConnection -------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * The Class SubNetworkConnection.
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public class SubNetworkConnection {
		
		private int subNetworkID_1;
		private int subNetworkID_2;
		private String connectingNetworkComponentID;
		
		Double highVoltageLevel;
		Double lowVoltageLevel;
		
		private ElectricalNodeState lvElNodeState;
		private ElectricalNodeState hvElNodeState;
		
		private ScheduleController scheduleController;
		private TransformerCalculation transformerCalculation;
		
		
		/**
		 * Instantiates a new sub network connection.
		 *
		 * @param subNetworkID_1 the sub network I D 1
		 * @param subNetworkID_2 the sub network I D 2
		 * @param connectingNetworkComponentID the connecting network component ID
		 */
		public SubNetworkConnection(int subNetworkID_1, int subNetworkID_2, String connectingNetworkComponentID) {
			if (subNetworkID_1 < subNetworkID_2) {
				this.subNetworkID_1 = subNetworkID_1;
				this.subNetworkID_2 = subNetworkID_2;
			} else {
				this.subNetworkID_1 = subNetworkID_2;
				this.subNetworkID_2 = subNetworkID_1;
			}
			this.connectingNetworkComponentID = connectingNetworkComponentID;
			this.getResultScheduleController();
			this.getTransformerCalculation();
		}

		/**
		 * Gets the sub network ID no. 1.
		 * @return the sub network I D 1
		 */
		public int getSubNetworkID_1() {
			return subNetworkID_1;
		}
		/**
		 * Return the sub network ID no. 2.
		 * @return the sub network I D 2
		 */
		public int getSubNetworkID_2() {
			return subNetworkID_2;
		}
		/**
		 * Returns the connecting NetworkComponent ID.
		 * @return the connecting NetworkComponent ID
		 */
		public String getConnectingNetworkComponentID() {
			return connectingNetworkComponentID;
		}

		/**
		 * Sets the low voltage electrical node state.
		 * @param elNodeState the new low voltage electrical node state
		 */
		public void setLowVoltageElectricalNodeState(ElectricalNodeState elNodeState) {
			this.lvElNodeState = elNodeState;
		}
		/**
		 * Returns the low voltage electrical node state.
		 * @return the low voltage electrical node state
		 */
		public ElectricalNodeState getLowVoltageElectricalNodeState() {
			return this.lvElNodeState;
		}
		/**
		 * Returns the low voltage node state always as UniPhaseElectricalNodeState.
		 * @return the low voltage electrical node state
		 */
		public UniPhaseElectricalNodeState getLowVoltageUniPhaseElectricalNodeState() {
			return ElectricalNodeStateConverter.convertToUniPhaseElectricalNodeState(this.getLowVoltageElectricalNodeState());
		}
		
		/**
		 * Sets the high voltage electrical node state.
		 * @param elNodeState the new high voltage electrical node state
		 */
		public void setHighVoltageElectricalNodeState(ElectricalNodeState elNodeState) {
			this.hvElNodeState = elNodeState;
		}
		/**
		 * Returns the high voltage electrical node state.
		 * @return the high voltage electrical node state
		 */
		public ElectricalNodeState getHighVoltageElectricalNodeState() {
			return this.hvElNodeState;
		}
		/**
		 * Returns the high voltage node state always as UniPhaseElectricalNodeState.
		 * @return the high voltage node state
		 */
		public UniPhaseElectricalNodeState getHighVoltageUniPhaseElectricalNodeState() {
			return ElectricalNodeStateConverter.convertToUniPhaseElectricalNodeState(this.getHighVoltageElectricalNodeState());
		}
		
		// --------------------------------------------------------------------
		// --- From here methods to determine voltage deltas ------------------
		// --------------------------------------------------------------------
		/**
		 * Assign the high and low voltage level to the local variables.
		 */
		private void assignVoltageLevel() {
			
			SubNetworkGraphNode subGraphNode1 = ElectricitySubNetworkGraph.this.getSubNetGraphNodeHash().get(this.getSubNetworkID_1());
			SubNetworkGraphNode subGraphNode2 = ElectricitySubNetworkGraph.this.getSubNetGraphNodeHash().get(this.getSubNetworkID_2());
		
			double vl1 = subGraphNode1.getConfiguredRatedVoltage();
			double vl2 = subGraphNode2.getConfiguredRatedVoltage();
			
			if (vl1>vl2) {
				this.highVoltageLevel = vl1;
				this.lowVoltageLevel = vl2;
			} else {
				this.highVoltageLevel = vl2;
				this.lowVoltageLevel = vl1;
			}
		}
		
		/**
		 * Returns the rated high voltage level.
		 * @return the high voltage level
		 */
		public double getHighVoltageLevel() {
			if (this.highVoltageLevel==null) this.assignVoltageLevel();
			return this.highVoltageLevel;
		}
		/**
		 * Returns the absolute delta between transformer calculation and network calculation on the HIGH VOLTAGE SIDE 
		 * of the transformer (voltageRealNode - voltageRealTransformer).
		 * @return the high voltage delta
		 */
		public double getHighVoltageDelta() {
			
			UniPhaseElectricalNodeState upElNodeState = this.getHighVoltageUniPhaseElectricalNodeState();
			if (upElNodeState==null) return Double.MAX_VALUE;
			
			double highVoltageRealNode = this.getHighVoltageUniPhaseElectricalNodeState().getVoltageRealNotNull().getValue();

			TechnicalSystemStateEvaluation tsseTransformer = this.getTransformerCalculation().getTechnicalSystemStateEvaluation();
			FixedDouble fdHighVoltageRealTransformer  = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsseTransformer.getIOlist(), TransformerSystemVariable.hvVoltageRealAllPhases.name());
			double highVoltageRealTransformer = fdHighVoltageRealTransformer.getValue();
			
			return NumberHelper.round(highVoltageRealNode - highVoltageRealTransformer, ElectricityTaskThreadCoordinator.ROUND_PRECISION);
		}
		/**
		 * Returns the relative high voltage delta.
		 * @return the high voltage delta relative
		 */
		public double getHighVoltageDeltaRelative() {
			return NumberHelper.round(this.getHighVoltageDelta() / this.getHighVoltageLevel(), ElectricityTaskThreadCoordinator.ROUND_PRECISION);
		}
		/**
		 * Returns the relative high voltage delta in percent.
		 * @return the high voltage delta in percent
		 */
		public double getHighVoltageDeltaPercent() {
			return NumberHelper.round(this.getHighVoltageDeltaRelative() * 100.0, ElectricityTaskThreadCoordinator.ROUND_PRECISION);
		}

		
		/**
		 * Returns the rated low voltage level.
		 * @return the high voltage level
		 */
		public double getLowVoltageLevel() {
			if (this.lowVoltageLevel==null) this.assignVoltageLevel();
			return this.lowVoltageLevel;
		}
		/**
		 * Returns the absolute delta between transformer calculation and network calculation on the LOW VOLTAGE SIDE 
		 * of the transformer (voltageRealNode - voltageRealTransformer).
		 * @return the high voltage delta
		 */
		public double getLowVoltageDelta() {
			
			UniPhaseElectricalNodeState upElNodeState = this.getLowVoltageUniPhaseElectricalNodeState();
			if (upElNodeState==null) return Double.MAX_VALUE;
			
			double lowVoltageRealNode = this.getLowVoltageUniPhaseElectricalNodeState().getVoltageRealNotNull().getValue();

			double lowVoltageRealTransformer = 0;
			TechnicalSystemStateEvaluation tsseTransformer = this.getTransformerCalculation().getTechnicalSystemStateEvaluation();
			if (this.getTransformerCalculation().getTransformerDataModel().isLowerVoltage_ThriPhase()==true) {
				// --- Calculate the single phase voltage ----------- 
				FixedDouble fdLowVoltageRealTransformerL1 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsseTransformer.getIOlist(), TransformerSystemVariable.lvVoltageRealL1.name());
				double lowVoltageRealTransformerL1 = fdLowVoltageRealTransformerL1.getValue(); 
				FixedDouble fdLowVoltageRealTransformerL2 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsseTransformer.getIOlist(), TransformerSystemVariable.lvVoltageRealL2.name());
				double lowVoltageRealTransformerL2 = fdLowVoltageRealTransformerL2.getValue();
				FixedDouble fdLowVoltageRealTransformerL3 = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsseTransformer.getIOlist(), TransformerSystemVariable.lvVoltageRealL3.name());
				double lowVoltageRealTransformerL3 = fdLowVoltageRealTransformerL3.getValue();
				
				lowVoltageRealTransformer = ((lowVoltageRealTransformerL1 + lowVoltageRealTransformerL2 + lowVoltageRealTransformerL3) / 3.0) * Math.sqrt(3); 
				
			} else {
				FixedDouble fdLowVoltageRealTransformer = (FixedDouble) TechnicalSystemStateHelper.getFixedVariable(tsseTransformer.getIOlist(), TransformerSystemVariable.lvVoltageRealAllPhases.name());
				lowVoltageRealTransformer = fdLowVoltageRealTransformer.getValue(); 
			}
			return NumberHelper.round(lowVoltageRealNode - lowVoltageRealTransformer, 5);
		}
		/**
		 * Returns the relative low voltage delta.
		 * @return the high voltage delta relative
		 */
		public double getLowVoltageDeltaRelative() {
			return NumberHelper.round(this.getLowVoltageDelta() / this.getLowVoltageLevel(), ElectricityTaskThreadCoordinator.ROUND_PRECISION);
		}
		/**
		 * Returns the relative low voltage delta in percent.
		 * @return the low voltage delta in percent
		 */
		public double getLowVoltageDeltaPercent() {
			return NumberHelper.round(this.getLowVoltageDeltaRelative() * 100.0, ElectricityTaskThreadCoordinator.ROUND_PRECISION);
		}
		
		
		/**
		 * Returns the transformers result ScheduleController of the aggregation handler.
		 * @return the result schedule controller
		 */
		public ScheduleController getResultScheduleController() {
			if (scheduleController==null) {
				scheduleController = ElectricitySubNetworkGraph.this.taskThreadCoordinator.getAggregationHandler().getNetworkComponentsScheduleController().get(this.getConnectingNetworkComponentID());
			}
			return scheduleController;
		}
		/**
		 * Returns the transformer calculation for the execution of transformer calculations.
		 * @return the transformer calculation
		 */
		public TransformerCalculation getTransformerCalculation() {
			if (transformerCalculation==null) {
				transformerCalculation = new TransformerCalculation(ElectricitySubNetworkGraph.this.taskThreadCoordinator, this);
			}
			return transformerCalculation;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "SubNet " + this.getSubNetworkID_1() + " <=> SubNet " + this.getSubNetworkID_2() + " by " + this.getConnectingNetworkComponentID();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object compObject) {
			
			if (compObject==null) return false;
			if (!(compObject instanceof SubNetworkConnection)) return false;
			if (compObject==this) return true;
			
			// --- Cast ----------------------
			SubNetworkConnection compConnection = (SubNetworkConnection) compObject;
			if (this.getConnectingNetworkComponentID().equals(compConnection.getConnectingNetworkComponentID())==false) return false;
			if (this.getSubNetworkID_1() != compConnection.getSubNetworkID_1()) return false;
			if (this.getSubNetworkID_2() != compConnection.getSubNetworkID_2()) return false;
			return true;
		}

	}
	
}
