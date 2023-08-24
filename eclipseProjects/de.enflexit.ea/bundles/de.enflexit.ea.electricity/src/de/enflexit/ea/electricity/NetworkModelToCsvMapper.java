package de.enflexit.ea.electricity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;

import de.enflexit.ea.core.dataModel.TransformerComponent;
import de.enflexit.ea.core.dataModel.TransformerHelper;
import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.dataModel.ontology.CableWithBreakerProperties;
import de.enflexit.ea.core.dataModel.ontology.CircuitBreaker;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * The Class NetworkModelMapper maps the Agent.GUI internal {@link NetworkModel}
 * to a CSV file corresponding structure. It can be used for the preparation of
 * the power flow calculation and for the export into CSV files.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class NetworkModelToCsvMapper {

	public enum SetupType {
		NodeSetup, BranchSetup, SensorSetup
	}

	public enum SetupColumn {
		NodeSetup_NodeNumber, NodeSetup_isLoadNode, NodeSetup_NominalPower,

		BranchSetup_NodeNumberFrom, BranchSetup_NodeNumberTo, BranchSetup_LengthLine, BranchSetup_LinearResistance,
		BranchSetup_LinearReactance, BranchSetup_LinearCapacitance, BranchSetup_LinearConductance,
		BranchSetup_MaxCurrent, BranchSetup_BranchNumber,

		SensorSetup_NodeNumberFrom, SensorSetup_NodeNumberTo
	}

	private boolean debugTableModels;

	private NetworkModel networkModel;
	private DomainCluster domainCluster;

	private Vector<NetworkComponent> netCompsSorted;

	private Vector<Vector<Double>> nodeSetupVector;
	private Vector<SlackNodeDescription> slackNodeVector;

	private HashMap<Integer, GraphNode> nodeNumberToGraphNode;
	private HashMap<GraphNode, Integer> graphNodeToNodeNumber;
	private HashMap<String, Integer> networkComponentIdToNodeNumber;
	private HashMap<Integer, String> nodeNumberToNetworkComponentId;

	private Vector<Vector<Double>> branchSetupVector;
	private Vector<BranchDescription> branchDescription;
	private Vector<Vector<Double>> sensorSetupVector;

	private SetupTableModel tableModelNode;
	private SetupTableModel tableModelBranch;
	private SetupTableModel tableModelSensor;

	/**
	 * Instantiates a NetworkModelToCsvMapper
	 *
	 * @param networkModel  the network model
	 * @param domainCluster the domain cluster
	 */
	public NetworkModelToCsvMapper(NetworkModel networkModel, DomainCluster domainCluster) {
		this.networkModel = networkModel;
		this.domainCluster = domainCluster;
		this.createCsvStructure();

		if (this.debugTableModels == true) {
			// --- Simply print the SetupTableModel -----------------
			this.getTableModelNodes().printModel();
			this.getTableModelBranches().printModel();
			this.getTableModelSensors().printModel();

			// --- Get a data row as a row HashMap ------------------
			int rowIndex = 0;
			HashMap<SetupColumn, Double> rowHash = this.getTableModelBranches().getTableModelRowAsHashMap(rowIndex);
			System.out.println("Values are from row no. " + (rowIndex + 1));

			// --- Access and print a single value ------------------
			System.out.println("Single value for maximum current: " + rowHash.get(SetupColumn.BranchSetup_MaxCurrent));
			System.out.println();

			// --- Print all values of the row ----------------------
			System.out.println("Print all values of row no. " + (rowIndex + 1));
			List<SetupColumn> headerList = new ArrayList<>(rowHash.keySet());
			for (int i = 0; i < headerList.size(); i++) {
				SetupColumn sCol = headerList.get(i);
				Double sColValue = rowHash.get(sCol);
				System.out.println("Column '" + sCol.toString() + "' \t" + sColValue);
			}
			System.out.println();
		}
	}

	/**
	 * Creates the csv structure.
	 */
	private void createCsvStructure() {
		try {
			// --- Create the node setting ----------------
			this.createNodeSetup();
			// --- Create Branch and Sensor Mapping -------
			this.createBranchSetup();
			// --- Check SlackNode definition -------------
			this.checkSlackNodeDefinition();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Returns the specified setup as an array.
	 * 
	 * @return the setup as array
	 */
	public double[][] getSetupAsArray(SetupType setupType) {

		// --- Select the specified setup vector --------------------
		Vector<Vector<Double>> setupVector = null;
		switch (setupType) {
		case NodeSetup:
			setupVector = this.getNodeSetupVector();
			break;
		case BranchSetup:
			setupVector = this.getBranchSetupVector();
			break;
		case SensorSetup:
			setupVector = this.getSensorSetupVector();
			break;
		}
		if (setupVector == null || setupVector.size() == 0)
			return null;

		// --- Create the double array of the setup -----------------
		int nRows = setupVector.size();
		int nColumns = setupVector.get(0).size();

		double[][] nodeSetup = null;
		if (nRows > 0 && nColumns > 0) {
			// --- Create the array with the correct size -----------
			nodeSetup = new double[nRows][nColumns];
			// --- Transfer from Vector to array --------------------
			for (int row = 0; row < nRows; row++) {
				Vector<Double> rowVector = setupVector.get(row);
				for (int column = 0; column < nColumns; column++) {
					nodeSetup[row][column] = rowVector.get(column);
				}
			}
		}
		return nodeSetup;
	}

	/**
	 * Returns the table model for the nodes of the electrical grid.
	 * 
	 * @return the table model node
	 */
	public SetupTableModel getTableModelNodes() {
		if (tableModelNode == null) {
			Vector<SetupColumn> columns = new Vector<>();
			columns.add(SetupColumn.NodeSetup_NodeNumber);
			columns.add(SetupColumn.NodeSetup_isLoadNode);
			columns.add(SetupColumn.NodeSetup_NominalPower);
			tableModelNode = new SetupTableModel(new Vector<Vector<Double>>(), columns);
		}
		return tableModelNode;
	}

	/**
	 * Return the table model for the branches of the electrical grid.
	 * 
	 * @return the table model branch
	 */
	public SetupTableModel getTableModelBranches() {
		if (tableModelBranch == null) {
			Vector<SetupColumn> columns = new Vector<>();
			columns.add(SetupColumn.BranchSetup_NodeNumberFrom);
			columns.add(SetupColumn.BranchSetup_NodeNumberTo);
			columns.add(SetupColumn.BranchSetup_LengthLine);
			columns.add(SetupColumn.BranchSetup_LinearResistance);
			columns.add(SetupColumn.BranchSetup_LinearReactance);
			columns.add(SetupColumn.BranchSetup_LinearCapacitance);
			columns.add(SetupColumn.BranchSetup_LinearConductance);
			columns.add(SetupColumn.BranchSetup_MaxCurrent);
			columns.add(SetupColumn.BranchSetup_BranchNumber);
			tableModelBranch = new SetupTableModel(new Vector<Vector<Double>>(), columns);
		}
		return tableModelBranch;
	}

	/**
	 * Returns the table model for sensors.
	 * 
	 * @return the table model sensors
	 */
	public SetupTableModel getTableModelSensors() {
		if (tableModelSensor == null) {
			Vector<SetupColumn> columns = new Vector<>();
			columns.add(SetupColumn.SensorSetup_NodeNumberFrom);
			columns.add(SetupColumn.SensorSetup_NodeNumberTo);
			tableModelSensor = new SetupTableModel(new Vector<Vector<Double>>(), columns);
		}
		return tableModelSensor;
	}

	/**
	 * Creates a default table model row, where all values will be 0.
	 *
	 * @param size the size
	 * @return the vector
	 */
	private Vector<Double> createDefaultTableModelRow(int size) {

		Vector<Double> defaultRow = new Vector<>();
		for (int i = 0; i < size; i++) {
			defaultRow.add(0.0);
		}
		return defaultRow;
	}

	/**
	 * Gets the node setup vector.
	 * 
	 * @return the node setup vector
	 */
	public Vector<Vector<Double>> getNodeSetupVector() {
		if (nodeSetupVector == null) {
			nodeSetupVector = new Vector<Vector<Double>>();
		}
		return nodeSetupVector;
	}

	/**
	 * Gets the slack node vector.
	 * 
	 * @return the slack node vector
	 */
	public Vector<SlackNodeDescription> getSlackNodeVector() {
		if (slackNodeVector == null) {
			slackNodeVector = new Vector<SlackNodeDescription>();
		}
		return slackNodeVector;
	}

	/**
	 * Gets the node index to GraphNode.
	 * 
	 * @return the node index to GraphNode
	 */
	public HashMap<Integer, GraphNode> getNodeNumberToGraphNode() {
		if (nodeNumberToGraphNode == null) {
			nodeNumberToGraphNode = new HashMap<Integer, GraphNode>();
		}
		return nodeNumberToGraphNode;
	}

	/**
	 * Gets the graph node to node number.
	 * 
	 * @return the graph node to node number
	 */
	public HashMap<GraphNode, Integer> getGraphNodeToNodeNumber() {
		if (graphNodeToNodeNumber == null) {
			graphNodeToNodeNumber = new HashMap<>();
		}
		return graphNodeToNodeNumber;
	}

	/**
	 * Gets the network component id to node number.
	 * 
	 * @return the network component id to node number
	 */
	public HashMap<String, Integer> getNetworkComponentIdToNodeNumber() {
		if (networkComponentIdToNodeNumber == null) {
			networkComponentIdToNodeNumber = new HashMap<>();
		}
		return networkComponentIdToNodeNumber;
	}

	/**
	 * Gets the node number to network component id.
	 * 
	 * @return the node number to network component id
	 */
	public HashMap<Integer, String> getNodeNumberToNetworkComponentId() {
		if (nodeNumberToNetworkComponentId == null) {
			nodeNumberToNetworkComponentId = new HashMap<>();
		}
		return nodeNumberToNetworkComponentId;
	}

	/**
	 * Returns the sorted Vector of {@link NetworkComponent}s.
	 * 
	 * @return the network component vector sorted
	 */
	private Vector<NetworkComponent> getNetworkComponentVectorSorted() {
		if (netCompsSorted == null) {
			if (domainCluster == null) {
				netCompsSorted = this.networkModel.getNetworkComponentVectorSorted();
			} else {
				netCompsSorted = this.domainCluster.getNetworkComponents();
			}
		}
		return netCompsSorted;
	}

	/**
	 * Creates the node setup table.
	 */
	private void createNodeSetup() {

		int nodeNumber = 1;
		// --- Get the NetworkComponents --------------------------------------
		Vector<NetworkComponent> netCompVector = this.getNetworkComponentVectorSorted();
		for (int i = 0; i < netCompVector.size(); i++) {

			// --- Check single NetworkComponent ------------------------------
			NetworkComponent netComp = netCompVector.get(i);
			HashSet<GraphElement> graphNodeElements = this.networkModel.getGraphElementsOfNetworkComponent(netComp,
					new GraphNode());
			if (graphNodeElements.size() == 1) {
				// --- Distribution nodes only --------------------------------
				double dIsloadNode = 0;
				double dDEAPwrNom = 0;
				GraphNode graphNode = (GraphNode) graphNodeElements.iterator().next();
				if (graphNode.getDataModel() != null) {
					// --- Get the object array of the data model -------------
					Object dataModel = graphNode.getDataModel();
					Object[] dataModelArray = null;
					if (dataModel != null && dataModel.getClass().isArray()) {
						// --- Regular NetworkComponentAdapter4DataModel ------
						dataModelArray = (Object[]) dataModel;

					} else if (dataModel instanceof TreeMap<?, ?>) {
						// --- BundlingNetworkComponentAdapter4DataModel ------
						@SuppressWarnings("unchecked")
						TreeMap<String, Object> dmTreeMap = (TreeMap<String, Object>) dataModel;
						if (this.getDomain() == null) {
							// --- This is just a default fall back! ----------
							dataModel = dmTreeMap.get(this.networkModel.getDomain(netComp));
						} else {
							// --- Work on current domain ---------------------
							dataModel = dmTreeMap.get(this.getDomain());
						}

						if (dataModel != null && dataModel.getClass().isArray()) {
							dataModelArray = (Object[]) dataModel;
						}

					}

					// --- Define 'dDEAPwrNom' and 'dIsloadNode' --------------
					if (dataModelArray != null && dataModelArray.length > 0) {
						// --- Get the node state -----------------------------
						if (dataModelArray[0] instanceof ElectricalNodeProperties) {
							ElectricalNodeProperties nodeProperties = (ElectricalNodeProperties) dataModelArray[0];
							if (nodeProperties.getNominalPower() != null) {
								dDEAPwrNom = nodeProperties.getNominalPower().getValue();
							} else {
								dDEAPwrNom = 0;
							}
							if (nodeProperties.getIsLoadNode() == true) {
								dIsloadNode = 1;
							}
						} else if (dataModelArray[0] == null) {
							System.err.println("==> ToDo: [" + this.getClass().getSimpleName()
									+ "] Data model incomplete: No properties instance set for node "
									+ graphNode.getId() + ", " + netComp.getType() + " " + netComp.getId());
						} else {
							System.err.println("==> ToDo: [" + this.getClass().getSimpleName()
									+ "] Found unknow GraphNode data type '" + dataModelArray[0].getClass().getName()
									+ "'!");
						}

					}
				}

				// --- Define single row for the node setup -------------------
				Vector<Double> row = new Vector<Double>();
				row.add((double) nodeNumber);
				row.add(dIsloadNode);
				row.add(dDEAPwrNom);
				this.getNodeSetupVector().add(row);

				// --- Define single row for the table model ------------------
				Vector<Double> newTableRow = this
						.createDefaultTableModelRow(this.getTableModelNodes().getColumnCount());
				int colNodeNumber = this.getTableModelNodes().findColumn(SetupColumn.NodeSetup_NodeNumber.toString());
				newTableRow.set(colNodeNumber, (double) nodeNumber);
				int colIsLoadNode = this.getTableModelNodes().findColumn(SetupColumn.NodeSetup_isLoadNode.toString());
				newTableRow.set(colIsLoadNode, dIsloadNode);
				int colNominalPower = this.getTableModelNodes()
						.findColumn(SetupColumn.NodeSetup_NominalPower.toString());
				newTableRow.set(colNominalPower, dDEAPwrNom);
				this.getTableModelNodes().addRow(newTableRow);

				// --- Remind relations between no, index and GraphNode -------
				this.getNodeNumberToGraphNode().put(nodeNumber, graphNode);
				this.getGraphNodeToNodeNumber().put(graphNode, nodeNumber);
				// --- Remind relation between NetworkComponent and no --------
				this.getNetworkComponentIdToNodeNumber().put(netComp.getId(), nodeNumber);
				this.getNodeNumberToNetworkComponentId().put(nodeNumber, netComp.getId());

				// --- Remind transformer / slack node ------------------------
				if (TransformerHelper.isTransformer(netComp.getType())) {
					this.getSlackNodeVector().add(new SlackNodeDescription(nodeNumber, netComp.getId()));
				}

				// --- Increase rowBumber -------------------------------------
				nodeNumber++;
			}
		} // end for
	}

	/**
	 * Gets the branch setup vector.
	 * 
	 * @return the branch setup vector
	 */
	public Vector<Vector<Double>> getBranchSetupVector() {
		if (branchSetupVector == null) {
			branchSetupVector = new Vector<Vector<Double>>();
		}
		return branchSetupVector;
	}

	/**
	 * Gets the branch (cable, breaker or mBox) line index to network component
	 * HashMap.
	 * 
	 * @return the branch line number to network component
	 */
	public Vector<BranchDescription> getBranchDescription() {
		if (branchDescription == null) {
			branchDescription = new Vector<BranchDescription>();
		}
		return branchDescription;
	}

	/**
	 * Gets the sensor setup vector.
	 * 
	 * @return the sensor setup vector
	 */
	public Vector<Vector<Double>> getSensorSetupVector() {
		if (sensorSetupVector == null) {
			sensorSetupVector = new Vector<Vector<Double>>();
		}
		return sensorSetupVector;
	}

	/**
	 * Creates the branch setup table.
	 */
	private void createBranchSetup() {

		Graph<GraphNode, GraphEdge> graph = this.networkModel.getGraph();
		int branchNumber = 1;
		for (GraphEdge edge : graph.getEdges()) {

			// --- Check domain of the NetworkComponent -----------------------
			NetworkComponent netComp = this.networkModel.getNetworkComponent(edge);
			if (this.domainCluster != null && this.domainCluster.getNetworkComponents().contains(netComp) == false)
				continue;

			// --- Check domain of the NetworkComponent -----------------------
			String domain = this.networkModel.getDomain(netComp);
			if (ElectricityDomainIdentification.isElectricityDomain(domain) == false)
				continue;
//			if (domain.equals(GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V)==false && domain.equals(GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_10KV)==false) continue; 

			// --- Get start and end point of the Edge ------------------------
			Pair<GraphNode> edgeNodes = graph.getEndpoints(edge);
			GraphNode nodeFrom = edgeNodes.getFirst();
			int nodeNumberFrom = this.getGraphNodeToNodeNumber().get(nodeFrom);

			GraphNode nodeTo = edgeNodes.getSecond();
			Integer nodeNumberTo = this.getGraphNodeToNodeNumber().get(nodeTo);

			// --- Get the NetworkComponent of the edge -----------------------
			if (netComp.getDataModel() instanceof Object[]) {

				// --- Data model is a Object Array of an Ontology adapter ----
				Object[] dataModelArray = (Object[]) netComp.getDataModel();
				if (dataModelArray[0] == null)
					continue;

				// --- Handle cable and sub classes ---------------------------
				if (dataModelArray[0] instanceof CableProperties) {

					CableProperties cable = (CableProperties) dataModelArray[0];
					double dLengthLine = cable.getLength().getValue();
					double dResistanceLinear_R = cable.getLinearResistance() == null ? 0.0
							: cable.getLinearResistance().getValue();
					double dReactanceLinear_X = cable.getLinearReactance() == null ? 0.0
							: cable.getLinearReactance().getValue();
					double dLinearCapacitance_C = cable.getLinearCapacitance() == null ? 0.0
							: cable.getLinearCapacitance().getValue();
					double dLinearConductance_G = cable.getLinearConductance() == null ? 0.0
							: cable.getLinearConductance().getValue();
					double nMaxCurrent = cable.getMaxCurrent() == null ? 0.0 : cable.getMaxCurrent().getValue();

					if (cable instanceof SensorProperties) {
						// ----------------------------------------------------
						// --- Handle Sensor information ----------------------
						// ----------------------------------------------------
						SensorProperties sensor = (SensorProperties) cable;
						if (sensor.getMeasureLocation() != null) {
							// --- Check, where the measurement location is ---
							String locationNetCompNo = sensor.getMeasureLocation();
							NetworkComponent netCompFrom = this.networkModel.getDistributionNode(nodeFrom);
							if (netCompFrom.getId().contains(locationNetCompNo) == false) {
								int nFromNodeNew = nodeNumberTo;
								int nToNodeNew = nodeNumberFrom;
								nodeNumberFrom = nFromNodeNew;
								nodeNumberTo = nToNodeNew;
							}
							// --- Remind this Sensor -------------------------
							Vector<Double> sensorSingle = new Vector<Double>();
							sensorSingle.add((double) nodeNumberFrom);
							sensorSingle.add((double) nodeNumberTo);
							this.getSensorSetupVector().addElement(sensorSingle);

							// --- Remind in table model too ------------------
							Vector<Double> newTableRow = this
									.createDefaultTableModelRow(this.getTableModelSensors().getColumnCount());
							int colNodeNumberFrom = this.getTableModelSensors()
									.findColumn(SetupColumn.SensorSetup_NodeNumberFrom.toString());
							newTableRow.set(colNodeNumberFrom, (double) nodeNumberFrom);
							int colNodeNumberTo = this.getTableModelSensors()
									.findColumn(SetupColumn.SensorSetup_NodeNumberTo.toString());
							newTableRow.set(colNodeNumberTo, (double) nodeNumberTo);
							this.getTableModelSensors().addRow(newTableRow);
						}

					} else if (cable instanceof CableWithBreakerProperties) {
						// ----------------------------------------------------
						// --- Handle breaker configuration -------------------
						// ----------------------------------------------------
						CableWithBreakerProperties cwBreaker = (CableWithBreakerProperties) cable;
						if (this.isOpenBreaker(cwBreaker.getBreakerBegin()) == true)
							continue;
						if (this.isOpenBreaker(cwBreaker.getBreakerEnd()) == true)
							continue;
						if (dLengthLine == 0)
							dLengthLine = 0.001;

					}

					// --- Add row to setup vector ----------------------------
					Vector<Double> row = new Vector<Double>();
					row.add((double) nodeNumberFrom);
					row.add((double) nodeNumberTo);
					row.add(dLengthLine);
					row.add(dResistanceLinear_R);
					row.add(dReactanceLinear_X);
					row.add(dLinearConductance_G);
					row.add(dLinearCapacitance_C);
					row.add(nMaxCurrent);
					row.add((double) branchNumber);
					this.getBranchSetupVector().add(row);

					// --- Define single row for the table model --------------
					Vector<Double> newTableRow = this
							.createDefaultTableModelRow(this.getTableModelBranches().getColumnCount());
					int colNodeNumberFrom = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_NodeNumberFrom.toString());
					newTableRow.set(colNodeNumberFrom, (double) nodeNumberFrom);
					int colNodeNumberTo = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_NodeNumberTo.toString());
					newTableRow.set(colNodeNumberTo, (double) nodeNumberTo);

					int colLineLength = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_LengthLine.toString());
					newTableRow.set(colLineLength, dLengthLine);

					int colLinearResistance = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_LinearResistance.toString());
					newTableRow.set(colLinearResistance, dResistanceLinear_R);
					int colLinearReactance = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_LinearReactance.toString());
					newTableRow.set(colLinearReactance, dReactanceLinear_X);
					int colLinearCapacitance = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_LinearCapacitance.toString());
					newTableRow.set(colLinearCapacitance, dLinearCapacitance_C);
					int colLinearConductance = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_LinearConductance.toString());
					newTableRow.set(colLinearConductance, dLinearConductance_G);

					int colMaxCurrent = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_MaxCurrent.toString());
					newTableRow.set(colMaxCurrent, nMaxCurrent);

					int colBranchNumber = this.getTableModelBranches()
							.findColumn(SetupColumn.BranchSetup_BranchNumber.toString());
					newTableRow.set(colBranchNumber, (double) branchNumber);

					this.getTableModelBranches().addRow(newTableRow);

					// --- Remind indexes for this branch (cable etc.) --------
					this.getBranchDescription()
							.add(new BranchDescription(nodeNumberFrom, nodeNumberTo, branchNumber, netComp));

					branchNumber++;
				}
			} // end if 'instanceof Object[]'

		} // end for
	}

	/**
	 * Checks if the specified CircuitBreaker is open.
	 *
	 * @param cb the CircuitBreaker to check
	 * @return true, if is open breaker
	 */
	private boolean isOpenBreaker(CircuitBreaker cb) {
		if (cb==null) return false;
		if (cb.getBreakerID()==null || cb.getBreakerID().trim().isEmpty()) return false;
		return !cb.getIsClosed();
	}

	/**
	 * Checks, if a slack node definition is available.
	 */
	private void checkSlackNodeDefinition() {
		
		if (this.getSlackNodeVector().size()==0) {
			// ----------------------------------------------------------------
			// --- No transformer was found, take a sensor --------------------
			// ----------------------------------------------------------------
			if (this.getSensorSetupVector().size()>0) {
				// --- Get the first sensor -----------------------------------
				Vector<Double> nodeFromToVector = this.getSensorSetupVector().get(0);
				int nodeNumber = nodeFromToVector.get(0).intValue();
				String networkComponentID = this.getNodeNumberToNetworkComponentId().get(nodeNumber);
				// --- Add the sensor as slack node description ---------------
				SlackNodeDescription snDesc = new SlackNodeDescription(nodeNumber, networkComponentID);
				this.getSlackNodeVector().addElement(snDesc);
			}
			
		} else if (this.getSlackNodeVector().size()>1) {
			// ----------------------------------------------------------------
			// --- Decide which node to use -----------------------------------
			// ----------------------------------------------------------------
			// --- Get vector of NetworkComponents ----------------------------
			Vector<NetworkComponent> slackNodeNetCompVector = new Vector<>();
			this.getSlackNodeVector().forEach((SlackNodeDescription snd) -> slackNodeNetCompVector.add(this.networkModel.getNetworkComponent(snd.getNetworkComponentID())));
			// --- Get corresponding vector of TransformerComponents ----------
			Vector<TransformerComponent> transCompVector = TransformerHelper.getTransformerComponents(this.networkModel, this.getDomain(), slackNodeNetCompVector);
			// --- Sort ascending by opposite rated voltage to get highest ----
			Collections.sort(transCompVector, new Comparator<TransformerComponent>() {
				@Override
				public int compare(TransformerComponent tc1, TransformerComponent tc2) {
					Float oppRatedVoltage1 = tc1.getOppositeRatedVoltage();
					Float oppRatedVoltage2 = tc2.getOppositeRatedVoltage();
					return oppRatedVoltage1.compareTo(oppRatedVoltage2);
				}
			});

			// --- Remove every node, expect the last one ---------------------  
			for (int i = 0; i < (transCompVector.size()-1); i++) {
				TransformerComponent transCompDelete = transCompVector.get(i);
				this.removeSlackNode(transCompDelete.getNetworkComponent().getId());
			}
			System.out.println("[" + this.getClass().getSimpleName() + "] Selected NetworkComponent '" + this.getSlackNodeVector().get(0).getNetworkComponentID() + "' as SlackNode" + (this.getDomain()!=null ? " for Domain '" + this.getDomain() + "'": "") + "!");
		}
	}
	
	/**
	 * Removes the SlackNodeDescription with the specified ID of a NetworkComponent.
	 *
	 * @param netCompID the net comp ID
	 * @return the slack node description that was removed
	 */
	private SlackNodeDescription removeSlackNode(String netCompID) {
		
		int deleteIndex = -1;
		for (int i = 0; i < this.getSlackNodeVector().size(); i++) {
			SlackNodeDescription snd = this.getSlackNodeVector().get(i);
			if (snd.getNetworkComponentID().equals(netCompID)==true) {
				deleteIndex =i;
				break;
			}
		}
		if (deleteIndex!=-1) {
			return this.getSlackNodeVector().remove(deleteIndex);
		}
		return null;
	}
	
	
	/**
	 * Returns the domain of the current NetworkModelToCsvMapper.
	 * @return the domain
	 */
	public String getDomain() {
		if (this.domainCluster==null || this.domainCluster.getDomain()==null || this.domainCluster.getDomain().isEmpty()) {
			return null;
		} 
		return this.domainCluster.getDomain();
	}
	
	/**
	 * The Class BranchDescription describes the start and end node index and the NetworkComponent that is affect with this branch.
	 * 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
	 */
	public class BranchDescription {
		
		private int nodeNumberFrom;
		private int nodeNumberTo;
		private int branchNumber;
		private NetworkComponent networkComponent;
		
		/**
		 * Instantiates a new branch description.
		 *
		 * @param nodeNumberFrom the node number from
		 * @param nodeNumberTo the node number to
		 * @param networkComponent the network component
		 */
		public BranchDescription(int nodeNumberFrom, int nodeNumberTo, int branchNumber, NetworkComponent networkComponent) {
			this.nodeNumberFrom = nodeNumberFrom;
			this.nodeNumberTo = nodeNumberTo;
			this.branchNumber = branchNumber;
			this.networkComponent = networkComponent;
		}
		/**
		 * Gets the node index from.
		 * @return the node index from
		 */
		public int getNodeNumberFrom() {
			return nodeNumberFrom;
		}
		/**
		 * Gets the node index to.
		 * @return the node index to
		 */
		public int getNodeNumberTo() {
			return nodeNumberTo;
		}
		
		/**
		 * Gets the branch number.
		 * @return the branch number
		 */
		public int getBranchNumber() {
			return branchNumber;
		}
		/**
		 * Gets the network component.
		 * @return the network component
		 */
		public NetworkComponent getNetworkComponent() {
			return networkComponent;
		}
	}
	
	/**
	 * The Class SlackNodeDescription hold the needed information to describe a SlackNode.
	 * 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
	 */
	public class SlackNodeDescription {
		
		private int nodeNumber;
		private String networkComponentID;
		
		/**
		 * Instantiates a new slack node description.
		 *
		 * @param nodeNumber the node number
		 * @param networkComponentID the network component id
		 */
		public SlackNodeDescription(int nodeNumber, String networkComponentID) {
			this.nodeNumber = nodeNumber;
			this.networkComponentID = networkComponentID;
		}
		
		/**
		 * Returns the node number.
		 * @return the node number
		 */
		public int getNodeNumber() {
			return nodeNumber;
		}
		/**
		 * Returns the network component id.
		 * @return the network component id
		 */
		public String getNetworkComponentID() {
			return networkComponentID;
		}
	}
	
	/**
	 * The Class SetupTableModel represents and extends a DefaultTableModel, but offers 
	 * additional help methods to access the data.
	 * 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
	 */
	public class SetupTableModel extends javax.swing.table.DefaultTableModel {

		private static final long serialVersionUID = 2754983726558495464L;

		/**
		 * Instantiates a new setup table model.
		 *
		 * @param vector the vector
		 * @param columns the columns
		 */
		public SetupTableModel(Vector<Vector<Double>> vector, Vector<SetupColumn> columns) {
			super(vector, columns);
		}
		
		/**
		 * Return the table model row as hash map.
		 *
		 * @param rowIndex the row
		 * @return the table model row as hash map
		 */
		public HashMap<SetupColumn, Double> getTableModelRowAsHashMap(int rowIndex) {
			@SuppressWarnings("unchecked")
			Vector<Double> dataRow = (Vector<Double>) this.getDataVector().get(rowIndex);
			return this.getTableModelRowAsHashMap(dataRow);
		}

		/**
		 * Return the table model row as hash map.
		 *
		 * @param rowIndex the row
		 * @return the table model row as hash map
		 */
		public HashMap<SetupColumn, Double> getTableModelRowAsHashMap(Vector<Double> dataRow) {
			HashMap<SetupColumn, Double> rowHashMap = new HashMap<>();
			for (int i = 0; i < this.getColumnCount(); i++) {
				String colName = this.getColumnName(i);
				Double colValue = dataRow.get(i);
				rowHashMap.put(SetupColumn.valueOf(colName), colValue);
			}
			return rowHashMap;
		}
		
		/**
		 * Prints the current model to the console.
		 */
		public void printModel() {
			
			// --- Print column header ------------------------------
			String colNames = "";
			for (int i = 0; i < this.getColumnCount(); i++) {
				String colName = this.getColumnName(i);
				if (colNames.isEmpty()==false) {
					colNames += "\t";
				}
				colNames += colName;
			}
			System.out.println(colNames);
			
			// --- Iterate over rows --------------------------------
			for (int row = 0; row < this.getRowCount(); row++) {
				// --- Iterate over columns -------------------------
				String rowString = "";
				for (int col = 0; col < this.getColumnCount(); col++) {
					Object cellValue = this.getValueAt(row, col);
					if (rowString.isEmpty()==false) {
						rowString += "\t";
					}
					rowString += cellValue;
				}
				// --- Print row ------------------------------------
				System.out.println(rowString);
			}
			// --- Print one final empty line -----------------------
			System.out.println();
		}
		
		
	}
	
}
