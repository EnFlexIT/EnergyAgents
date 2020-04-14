package de.enflexit.ea.lib.powerFlowEstimation.centralEstimation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.lib.powerFlowEstimation.decentralEstimation.DistrictModel;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import hygrid.csvFileImport.NetworkModelToCsvMapper;
import hygrid.csvFileImport.NetworkModelToCsvMapper.SetupType;

public class GridStateEstimationInit {
	NetworkModel networkModel = new NetworkModel();
	NetworkModelToCsvMapper netModelToCsvMapper;
	private int nNumNodes; // Number of Nodes
	private int nNumSensors; // Number of Sensors
	private int nNumAutarkicGrids; // Number of Autarkic Grid
	private double[][] dBranchMeasurementParams;
	private double[][] dNodeAssociation;
	private double[][] dBranchParams;
	private ArrayList<List<Integer>> vNodesOfAutarkicGrid; // Vector of clustered Grid
	private Vector<NetworkModel> subNetworks;
	
	/**
	 * This method initializes the estimation base
	 * @param networkModel
	 */
	public void init(NetworkModel networkModel) {
		this.setNetworkModel(networkModel);
		
		// --- Importing ParameterSet from NetworkModel (double Array)---------------------------------------------------------
		dNodeAssociation = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.NodeSetup);
		dBranchParams = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.BranchSetup);
		dBranchMeasurementParams = this.getNetworkModelToCsvMapper().getSetupAsArray(SetupType.SensorSetup);

		// --- Analyzing ParamterSet and Setting additional Parameters
		this.setnNumNodes(dNodeAssociation.length);
		this.setnNumSensors(dBranchMeasurementParams.length);

		// --- Building AutarkicGrids
		this.vNodesOfAutarkicGrid = new ArrayList<List<Integer>>();
		this.buildAutarkicGrids();

		// --- Separate and Distribute NetworkModel into Subnetworks for the districtAgents------------------------------------
		this.distributeNetworkModel();
		
		
	}
	
	/**
	 * Distribute network model. this Methods separates the global NetworkModel and
	 * distribute the data for the subnetworks
	 */
	private void distributeNetworkModel() {

		// --- Get a copy of the original NetworkModel ----
		NetworkModel networkModelCopy = this.getNetworkModel().getCopy();


		Graph<GraphNode, GraphEdge> graph = this.getNetworkModel().getGraph();
		this.subNetworks = new Vector<NetworkModel>();

		for (List<Integer> subList : vNodesOfAutarkicGrid) {

			// --- Example for components to cluster ----------
			HashSet<NetworkComponent> netcompsToCluster = new HashSet<>();
			
			Graph<GraphNode, GraphEdge> subGraph = new SparseGraph<GraphNode, GraphEdge>();

			// --- Build a list of all network components in this subgraph
			Vector<NetworkComponent> netCompVector = new Vector<NetworkComponent>();

			for (int i = 0; i < subList.size(); i++) {
				int nodeNr = (int) subList.get(i);
				String nodeId = "n" + nodeNr;
				NetworkComponent component = this.networkModel.getNetworkComponent(nodeId);
				if(component!=null) {
					netCompVector.addElement(this.networkModel.getNetworkComponent(nodeId));
					netcompsToCluster.add(networkModelCopy.getNetworkComponent(nodeId));
				}
				else {
					System.err.println("Can not find component: n"+nodeId);
				}
			}

			// --- Find adjacent edges and check if they should be added to the
			// subgraph
			for (NetworkComponent netComp : netCompVector) {

				// --- Get the GraphNode for this network component
				Vector<GraphElement> elementsVector = this.networkModel.getGraphElementsFromNetworkComponent(netComp);
				GraphNode componentNode = (GraphNode) elementsVector.get(0);

				subGraph.addVertex(componentNode);
				
				// --- Get the adjacent edges
				Collection<GraphEdge> adjacentEdges = this.getNetworkModel().getGraph().getIncidentEdges(componentNode);

				// --- Check if the edge belongs to the subgraph
				for (GraphEdge edge : adjacentEdges) {

					GraphNode oppositeNode = graph.getOpposite(componentNode, edge);

					List<NetworkComponent> hashSetOfOpppositeNetworkComponents=this.getNetworkModel().getNetworkComponents(oppositeNode);
					List<NetworkComponent> listOfOppositeNetworkComponents = new ArrayList<NetworkComponent>(hashSetOfOpppositeNetworkComponents);
					
					// --- Check all adjacent components
					for (int a=0;a<listOfOppositeNetworkComponents.size();a++) {
						boolean isPartOfSubnetwork = false;
						
						// --- Finding opposite node and not edge
						if(listOfOppositeNetworkComponents.get(a).getType().equals("Sensor")==false&&listOfOppositeNetworkComponents.get(a).getType().equals("Cable")==false) {
							NetworkComponent oppositeNodeNetworkComponent = listOfOppositeNetworkComponents.get(a);
							
							// --- Check if it is part of the subgraph
							if (oppositeNodeNetworkComponent != null) {
								int compNr = Integer.parseInt(oppositeNodeNetworkComponent.getId().substring(1));
								isPartOfSubnetwork = subList.contains(compNr);
								if (isPartOfSubnetwork == true) {
									subGraph.addVertex(oppositeNode);
									if (subGraph.containsEdge(edge) == false) {
										subGraph.addEdge(edge, componentNode, oppositeNode);
									}
								}
							}
						}
					}
				}

			} // end of component check

			// --- Build a new network model based on the subgraph
			NetworkModel subNetworkModel = new NetworkModel();
			subNetworkModel.setGraph(subGraph);
			subNetworkModel.setGeneralGraphSettings4MAS(this.getNetworkModel().getGeneralGraphSettings4MAS());

			// --- Add network components for the nodes
			for (NetworkComponent netComp1 : netcompsToCluster) {
				subNetworkModel.addNetworkComponent(netComp1);
			}

			// --- Add network components for the connecting edges
			for (GraphEdge edge : subGraph.getEdges()) {
				NetworkComponent edgeComponent = this.getNetworkModel().getNetworkComponent(edge);
				subNetworkModel.addNetworkComponent(edgeComponent);
				netcompsToCluster.add(edgeComponent);
			}

			subNetworks.add(subNetworkModel);
		}
	}
	
	public Vector<DistrictModel> buildDistrictAgentModel(){
		Vector<DistrictModel> vDistrictAgentModel = new Vector<>();
		
		String sRefPVName= this.findREFPV();
		
		for(int i=0;i<this.subNetworks.size();i++) {
			DistrictModel districtAgentModel = new DistrictModel();
			districtAgentModel.initiation(this.subNetworks.get(i),true);
			districtAgentModel.setsRefPVName(sRefPVName);
			vDistrictAgentModel.add(districtAgentModel);
		}
		
		return vDistrictAgentModel;
	}
	
	private String  findREFPV () {
		String refPVID = null;
		Vector<NetworkComponent> netComps = this.networkModel.getNetworkComponentVectorSorted();
		
		// --- Searching component ID of Ref PV
		for (NetworkComponent netComp : netComps) {
			// --- If Component is districtAgent
			if (netComp.getType().equals("REF-PV")) {
				refPVID = netComp.getId();
				break;
			}

		}
		if(refPVID==null) {
			System.err.println("No REF PV is found!");
		}
		return refPVID;
	}
	
	/**
	 * Builds the autarkic grids. This method build autarkic grids depending on
	 * connections between nodes
	 */
	private void buildAutarkicGrids() {

		double[][] dDistance2Nodes = new double[nNumNodes][nNumNodes];
		int nFromNode, nToNode;
		// --- Getting length between two sensors
		for (int i = 0; i < dBranchParams.length; i++) {
			nFromNode = (int) dBranchParams[i][0] - 1;
			nToNode = (int) dBranchParams[i][1] - 1;
			dDistance2Nodes[nFromNode][nToNode] = dBranchParams[i][2];
			dDistance2Nodes[nToNode][nFromNode] = dBranchParams[i][2];
		}

		// Setting all Connections between nFromNode and nToNode to zero
		for (int i = 0; i < this.getnNumSensors(); i++) {
			nFromNode = (int) dBranchMeasurementParams[i][0] - 1;
			nToNode = (int) dBranchMeasurementParams[i][1] - 1;
			if (nFromNode >= 0 && nToNode >= 0) {
				dDistance2Nodes[nFromNode][nToNode] = 0;
				dDistance2Nodes[nToNode][nFromNode] = 0;
			}
		}
		// Initializing and Starting of Dijkstra-Algorithm to build Autarkic
		// Grids
		Clustering dijkstraEngine = new Clustering();
		dijkstraEngine.dijkstraManager(dDistance2Nodes);
		// Save Result of Dijkstra-Algorithm
		ArrayList<List<Integer>> vTempNodesOfAutarkicGrid = dijkstraEngine.getAutarkicGridList();
		
		// --- Delete all autarkic grids, which have only one node included
//		ArrayList<List<Integer>> vTempNodeOfAutarkicGridAdjusted = new ArrayList<List<Integer>>();
//		for(int i=0;i<vTempNodesOfAutarkicGrid.size();i++) {
//			if(vTempNodesOfAutarkicGrid.get(i).size()>1) {
//				vTempNodeOfAutarkicGridAdjusted.add(vTempNodesOfAutarkicGrid.get(i));
//			}
//		}
		
		this.setnNumAutarkicGrids(vTempNodesOfAutarkicGrid.size());
		// Add FromNode of BranchMeasurement to AutarkicGrids
		for (int i = 0; i < this.getnNumAutarkicGrids(); i++) {
			List<Integer> newAdded = new ArrayList<Integer>();
			List<Integer> tempArray = vTempNodesOfAutarkicGrid.get(i);
			// If ToNode is part of AutarkicGrid and is unequal 0,--> add
			// FromNode to AutarkicGrid
//			int nSensorPerNode = 0;
			for (int j = 0; j < this.nNumSensors; j++) {
				if (tempArray.contains((int) dBranchMeasurementParams[j][1]) == true
						&& (int) dBranchMeasurementParams[j][0] != 0&& newAdded.contains((int) dBranchMeasurementParams[j][1])==false) {
					tempArray.add((int) dBranchMeasurementParams[j][0]);
					newAdded.add((int) dBranchMeasurementParams[j][0]);
//					nSensorPerNode++;
				}
			}

			// If FromNode is part if AutarkicGrid add ToNode, but only if ToNode is not included

			for (int j = 0; j < this.nNumSensors; j++) {
				if (tempArray.contains((int) dBranchMeasurementParams[j][0]) == true &&tempArray.contains((int) dBranchMeasurementParams[j][1]) == false&& newAdded.contains((int) dBranchMeasurementParams[j][0])==false) {

//					if (nSensorPerNode < 1) {
						tempArray.add((int) dBranchMeasurementParams[j][1]);
						newAdded.add((int) dBranchMeasurementParams[j][1]);
//					}

				}
			}
			//TODO: Check for more than one node
			if(tempArray.size()>2){
				vNodesOfAutarkicGrid.add( tempArray);
			}
		}
//		System.out.println("");
	}
	
	/**
	 * Returns the local {@link NetworkModelToCsvMapper}.
	 * 
	 * @return the NetworkModelToCsvMapper
	 */
	public NetworkModelToCsvMapper getNetworkModelToCsvMapper() {
		if (netModelToCsvMapper == null) {
			// --- DomainCluster can be null, since the NetworkModel is already reduced -----------
			netModelToCsvMapper  = new NetworkModelToCsvMapper(this.getNetworkModel(), null);
		}
		return netModelToCsvMapper;
	}
	
	public NetworkModel getNetworkModel() {
		return networkModel;
	}

	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}

	public NetworkModelToCsvMapper getNetModelToCsvMapper() {
		return netModelToCsvMapper;
	}

	public void setNetModelToCsvMapper(NetworkModelToCsvMapper netModelToCsvMapper) {
		this.netModelToCsvMapper = netModelToCsvMapper;
	}

	public int getnNumNodes() {
		return nNumNodes;
	}

	public void setnNumNodes(int nNumNodes) {
		this.nNumNodes = nNumNodes;
	}

	public int getnNumSensors() {
		return nNumSensors;
	}

	public void setnNumSensors(int nNumSensors) {
		this.nNumSensors = nNumSensors;
	}

	public int getnNumAutarkicGrids() {
		return nNumAutarkicGrids;
	}

	public void setnNumAutarkicGrids(int nNumAutarkicGrids) {
		this.nNumAutarkicGrids = nNumAutarkicGrids;
	}

	public double[][] getdBranchMeasurementParams() {
		return dBranchMeasurementParams;
	}

	public void setdBranchMeasurementParams(double[][] dBranchMeasurementParams) {
		this.dBranchMeasurementParams = dBranchMeasurementParams;
	}

	public double[][] getdNodeAssociation() {
		return dNodeAssociation;
	}

	public void setdNodeAssociation(double[][] dNodeAssociation) {
		this.dNodeAssociation = dNodeAssociation;
	}

	public double[][] getdBranchParams() {
		return dBranchParams;
	}

	public void setdBranchParams(double[][] dBranchParams) {
		this.dBranchParams = dBranchParams;
	}

	public ArrayList<List<Integer>> getvNodesOfAutarkicGrid() {
		return vNodesOfAutarkicGrid;
	}

	public void setvNodesOfAutarkicGrid(ArrayList<List<Integer>> vNodesOfAutarkicGrid) {
		this.vNodesOfAutarkicGrid = vNodesOfAutarkicGrid;
	}

	public Vector<NetworkModel> getSubNetworks() {
		return subNetworks;
	}

	public void setSubNetworks(Vector<NetworkModel> subNetworks) {
		this.subNetworks = subNetworks;
	}

}
