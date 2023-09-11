package de.enflexit.ea.electricity.aggregation.taskThreading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.NetworkAggregationTaskThread;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;

/**
 * The Class ElectricitySubNetworkGraph.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ElectricitySubNetworkGraph {

	private AbstractAggregationHandler aggregationHandler;
	private ElectricityTaskThreadCoordinator taskThreadCoordinator;
	
	private TreeMap<Integer, SubNetworkGraphNode> subNetGraphNodeHash;
	private TreeMap<String, SubNetworkConnection> subNetConnectionHash;
	
	private List<Double> voltageLevelList;
	
	/**
	 * Instantiates a new electricity sub network graph.
	 *
	 * @param aggregationHandler the aggregation handler
	 * @param taskThreadCoordinator the task thread coordinator
	 */
	public ElectricitySubNetworkGraph(AbstractAggregationHandler aggregationHandler, ElectricityTaskThreadCoordinator taskThreadCoordinator) {
		this.aggregationHandler = aggregationHandler;
		this.taskThreadCoordinator = taskThreadCoordinator;
		this.initialize();
	}
	/**
	 * Initializes.
	 */
	private void initialize() {
		
		ArrayList<AbstractElectricalNetworkConfiguration> elSubNetConfigs = this.taskThreadCoordinator.getElectricalSubNetworkConfigurations();
		
		// --- Sort descending by voltage level -----------
		Collections.sort(elSubNetConfigs, new Comparator<AbstractElectricalNetworkConfiguration>() {
			@Override
			public int compare(AbstractElectricalNetworkConfiguration c1, AbstractElectricalNetworkConfiguration c2) {
				Double voltatgeLevel1 = c1.getConfiguredRatedVoltageFromNetwork();
				Double voltatgeLevel2 = c2.getConfiguredRatedVoltageFromNetwork();
				return voltatgeLevel2.compareTo(voltatgeLevel1);
			}
		});
		
		// --- Remind possible voltage level --------------
		this.voltageLevelList = new ArrayList<>();
		for (int i = 0; i < elSubNetConfigs.size(); i++) {
			Double voltageLevel =  elSubNetConfigs.get(i).getConfiguredRatedVoltageFromNetwork();
			if (voltageLevel!=null && this.voltageLevelList.contains(voltageLevel)==false) {
				this.voltageLevelList.add(voltageLevel);
			}
		}
		Collections.sort(this.voltageLevelList, Collections.reverseOrder());
		
		// --- Create all corresponding nodes first -------
		List<SubNetworkGraphNode> subNetworkGraphNodeList = new ArrayList<>();
		for (AbstractElectricalNetworkConfiguration elSubNetConfig : elSubNetConfigs) {
			NetworkAggregationTaskThread subNetTaskThread = this.aggregationHandler.getOrCreateNetworkAggregationTaskThread(elSubNetConfig);
			int graphLevel = this.getElectricalGraphLevelByVoltageLevel(elSubNetConfig.getConfiguredRatedVoltageFromNetwork());
			subNetworkGraphNodeList.add(new SubNetworkGraphNode(elSubNetConfig, subNetTaskThread, graphLevel));
		}

		// --- Fill local graph hash maps -----------------
		for (SubNetworkGraphNode sngn : subNetworkGraphNodeList) {
			// --- Remind the sub network graph node ------
			this.getSubNetGraphNodeHash().put(sngn.getSubNetworkID(), sngn);
			// --- Remind the sub network connections -----
			List<SubNetworkConnection> subNetConnList = sngn.getSubNetworkConnections(subNetworkGraphNodeList);
			for (SubNetworkConnection subNetCon : subNetConnList) {
				this.getSubNetConnectionHash().put(subNetCon.getConnectingNetworkComponentID(), subNetCon);
			}
		}
	}
	
	/**
	 * Returns the list of possible voltage level.
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
		public NetworkAggregationTaskThread getSunNetworkTaskThread() {
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
		 *
		 * @param subNetworkGraphNodeList the sub network tree node list to check and compare the current node with (can be <code>null</code> later on)
		 * @return the sub network connections
		 */
		public List<SubNetworkConnection> getSubNetworkConnections(List<SubNetworkGraphNode> subNetworkGraphNodeList) {
			if (subNetworkConnectionList==null && subNetworkGraphNodeList!=null) {
				subNetworkConnectionList = new ArrayList<>();
				for (SubNetworkGraphNode treeNode : subNetworkGraphNodeList) {
					List<SubNetworkConnection> newSubNetConnFound = this.getSubNetworkConnections(treeNode);
					for (SubNetworkConnection newSubNetConn : newSubNetConnFound) {
						if (subNetworkConnectionList.contains(newSubNetConn)==false) {
							subNetworkConnectionList.add(newSubNetConn);
						}
					}
				}
			}
			return subNetworkConnectionList;
		}
		/**
		 * Return the sub network connections to the .
		 *
		 * @param subNetworkGraphNode the sub network graph node
		 * @return the sub network connections
		 */
		public List<SubNetworkConnection> getSubNetworkConnections(SubNetworkGraphNode subNetworkGraphNode) {
			
			List<SubNetworkConnection> subNetworkConnectionList = new ArrayList<>();
			if (subNetworkGraphNode!=null && subNetworkGraphNode!=this) {
				for (String thisNetCompID : this.getNetworkComponentIDs()) {
					if (subNetworkGraphNode.getNetworkComponentListAsString().contains("," + thisNetCompID + ",")==true) {
						subNetworkConnectionList.add(new SubNetworkConnection(this.getSubNetworkID(), subNetworkGraphNode.getSubNetworkID(), thisNetCompID));
					}
				}
			}
			return subNetworkConnectionList;
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

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "SubNet No. " + this.getSubNetworkID_1() + " to SubNet No. " + this.getSubNetworkID_2() + " by " + this.getConnectingNetworkComponentID();
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
