package de.enflexit.ea.electricity.topologyImport;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.helper.GraphNodePairs;
import org.awb.env.networkModel.helper.NetworkComponentFactory;
import org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter;
import org.awb.env.networkModel.persistence.NetworkModelImportService;

import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import de.enflexit.common.csv.CsvDataController;
import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.CableWithBreakerProperties;
import de.enflexit.ea.core.dataModel.ontology.CircuitBreaker;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.SensorProperties;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalTransformerState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSensorState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;

/**
 * The Class CsvFileImporterLowVoltageGrid provides an import adapter for the 
 * SAG / BUW low voltage grid descriptions.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class CSV_FileImporterLowVoltageGrid extends AbstractNetworkModelCsvImporter implements NetworkModelImportService{

	private final String FILE_NodeAssociation = "NodeAssociation.csv";
	private final String FILE_BranchParams = "BranchParams.csv";
	private final String FILE_BranchMeasurementParams = "BranchMeasurementParams.csv";
	private final String FILE_BreakerParams = "BreakerParams.csv";
	private final String FILE_ControlParams = "ControlParams.csv";
	
	private List<FileFilter> fileFilters;
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.NetworkModelImportService#requiresToStoreNetworkElements()
	 */
	@Override
	public boolean requiresToStoreNetworkElements() {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.NetworkModelImportService#getDataModelNetworkElementToSave()
	 */
	@Override
	public Vector<DataModelNetworkElement> getDataModelNetworkElementToSave() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.NetworkModelImportService#getMaxNumberOfThreadsForSaveAction()
	 */
	@Override
	public Integer getMaxNumberOfThreadsForSaveAction() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#importNetworkModelFromFile(java.io.File)
	 */
	@Override
	public NetworkModel importNetworkModelFromFile(File graphFile) {

		// --- Read the specified file ----------------------------------------
		if (this.readCsvFiles(graphFile) == false)
			this.showError();
		// --- Show table preview if this.isDebug() returns true --------------
		this.showImportPreview();

		// --- Determine number of connections for each node ------------------
		HashMap<Integer, Integer> nodeConnections = this.getNodeConnections();
		// --- Create the nodes of the NetworkModel ---------------------------
		HashMap<Integer, NetworkComponent> nodeID2NetComp = this.setNodes(nodeConnections);
		// --- Create the edges of the NetworkModel ---------------------------
		this.createEdges(nodeID2NetComp);

		// --- Finally, return the NetworkModel -------------------------------
		return this.getNetworkModel();
	}

	/**
	 * Creates the edges of the network.
	 */
	private void createEdges(HashMap<Integer, NetworkComponent> nodeID2NetComp) {

		CsvDataController dc = this.getCsvDataController().get(this.FILE_BranchParams);
		for (int row = 0; row < dc.getDataModel().getRowCount(); row++) {

			// --- Determine the node ID's ------------------------------------
			Integer nodeIDFrom = Integer.parseInt((String) dc.getDataModel().getValueAt(row, 0));
			Integer nodeIDTo = Integer.parseInt((String) dc.getDataModel().getValueAt(row, 1));

			// --- Determine the general edge parameter -----------------------
			Float length = Float.parseFloat((String) dc.getDataModel().getValueAt(row, 2));
			Float linResistance = Float.parseFloat((String) dc.getDataModel().getValueAt(row, 3));
			Float linReactance = Float.parseFloat((String) dc.getDataModel().getValueAt(row, 4));
			Float maxCurrent = Float.parseFloat((String) dc.getDataModel().getValueAt(row, 5));

			// --- Create the corresponding NetworkComponent ------------------
			NetworkModel newCompNM = null;
			CableProperties cableProperties = null;
			CableState cableState = null;
			if (this.isBreaker(nodeIDFrom, nodeIDTo) == true) {
				// --- Create Breaker component -------------------------------
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Breaker");
				// --- Create Breaker data model --------------------
				Vector<?> breakerRow = this.getBreakerRowVector(nodeIDFrom, nodeIDTo);
				String breakerID = (String) breakerRow.get(0);
				Integer intClosed = Integer.parseInt((String) breakerRow.get(3));

				CircuitBreaker breaker = new CircuitBreaker();
				breaker.setBreakerID(breakerID);
				if (intClosed == null || intClosed == 0) {
					breaker.setIsClosed(false);
				} else {
					breaker.setIsClosed(true);
				}
				CableWithBreakerProperties cableWithBreaker = new CableWithBreakerProperties();
				cableWithBreaker.setBreakerBegin(breaker);
				cableProperties = cableWithBreaker;
				cableState = new TriPhaseCableState();

			} else if (this.isMeasurement(nodeIDFrom, nodeIDTo) == true) {
				// --- Create Measurement/mBox component ----------------------
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Sensor");
				// --- Create Sensor data model ---------------------
				Vector<?> sensorRow = this.getMeasurementRowVector(nodeIDFrom, nodeIDTo);
				String sensorID = (String) sensorRow.get(0);
				
				cableProperties = new SensorProperties();
				((SensorProperties)cableProperties).setSensorID(sensorID);

				cableState = new TriPhaseSensorState();

			} else {
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Cable");
				cableProperties = new CableProperties();
				cableState = new TriPhaseCableState();

			}
			// --- Get start and end GraphNode of the component ---------------
			NetworkComponent newComp = newCompNM.getNetworkComponents().values().iterator().next();
			Object[] graphNodes = newCompNM.getGraphElementsOfNetworkComponent(newComp, new GraphNode()).toArray();
			GraphNode newNodeFrom = (GraphNode) graphNodes[0];
			GraphNode newNodeTo = (GraphNode) graphNodes[1];

			// --- Get the corresponding NetworkComponents for the Nodes ------
			NetworkComponent netCompFrom = nodeID2NetComp.get(nodeIDFrom);
			NetworkComponent netCompTo = nodeID2NetComp.get(nodeIDTo);

			// --- Set position of the new GraphNodes -------------------------
			GraphNode graphNodeFrom = (GraphNode) this.getNetworkModel().getGraphElementsFromNetworkComponent(netCompFrom).get(0);
			GraphNode graphNodeTo = (GraphNode) this.getNetworkModel().getGraphElementsFromNetworkComponent(netCompTo).get(0);
			newNodeFrom.setPosition(graphNodeFrom.getPosition());
			newNodeTo.setPosition(graphNodeTo.getPosition());

			GraphNodePairs pairFrom = new GraphNodePairs(graphNodeFrom, newNodeFrom);
			GraphNodePairs pairTo = new GraphNodePairs(graphNodeTo, newNodeTo);

			// --- Set the parameters for the component ------------------------
			cableProperties.setLength(new UnitValue(length, "m"));
			cableProperties.setLinearResistance(new UnitValue(linResistance, "Ω/km"));
			cableProperties.setLinearReactance(new UnitValue(linReactance, "Ω/km"));
			cableProperties.setMaxCurrent(new UnitValue(maxCurrent, "A"));

			// --- Add an empty time series chart object to match the requirements of the adapter ------
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());

			// --- Set the data model --------------
			Object[] dataModel = new Object[3];
			dataModel[0] = cableProperties;
			dataModel[1] = cableState;
			dataModel[2] = tsc;
			newComp.setDataModel(dataModel);

			// --- Merge into the new NetworkModel ----------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, true);
			this.getNetworkModel().mergeNodes(pairFrom);
			this.getNetworkModel().mergeNodes(pairTo);

		}
	}

	/**
	 * Checks if the specified edge is a breaker.
	 *
	 * @param nodeIDFrom the node id from
	 * @param nodeIDTo the node id to
	 * @return true, if is breaker
	 */
	private boolean isBreaker(Integer checkNodeIDFrom, Integer checkNodeIDTo) {
		return (this.getBreakerRowVector(checkNodeIDFrom, checkNodeIDTo) != null);
	}
	/**
	 * Gets the breaker for the specified from to node.
	 *
	 * @param checkNodeIDFrom the check node id from
	 * @param checkNodeIDTo the check node id to
	 * @return the breaker row vector
	 */
	private Vector<?> getBreakerRowVector(Integer checkNodeIDFrom, Integer checkNodeIDTo) {

		CsvDataController dc = this.getCsvDataController().get(this.FILE_BreakerParams);
		for (int row = 0; row < dc.getDataModel().getRowCount(); row++) {
			// --- Determine the node ID's ------------------------------------
			Vector<?> rowVector = (Vector<?>) dc.getDataModel().getDataVector().get(row);
			Integer nodeIDFrom = Integer.parseInt((String) rowVector.get(1));
			Integer nodeIDTo = Integer.parseInt((String) rowVector.get(2));
			if (nodeIDFrom.equals(checkNodeIDFrom) == true) {
				if (nodeIDTo.equals(checkNodeIDTo) == true) {
					return rowVector;
				}
			} else if (nodeIDFrom.equals(checkNodeIDTo) == true) {
				if (nodeIDTo.equals(checkNodeIDFrom) == true) {
					return rowVector;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the specified edge is a measurement.
	 *
	 * @param checkNodeIDFrom the node id from
	 * @param checkNodeIDTo the node id to
	 * @return true, if is measurement
	 */
	private boolean isMeasurement(Integer checkNodeIDFrom, Integer checkNodeIDTo) {
		return (this.getMeasurementRowVector(checkNodeIDFrom, checkNodeIDTo) != null);
	}

	/**
	 * Gets the measurement point for the specified from to node.
	 *
	 * @param checkNodeIDFrom the check node id from
	 * @param checkNodeIDTo the check node id to
	 * @return the measurement point row vector
	 */
	private Vector<?> getMeasurementRowVector(Integer checkNodeIDFrom, Integer checkNodeIDTo) {

		CsvDataController dc = this.getCsvDataController().get(this.FILE_BranchMeasurementParams);
		for (int row = 0; row < dc.getDataModel().getRowCount(); row++) {
			// --- Determine the node ID's ------------------------------------
			Vector<?> rowVector = (Vector<?>) dc.getDataModel().getDataVector().get(row);
			Integer nodeIDFrom = Integer.parseInt((String) rowVector.get(1));
			Integer nodeIDTo = Integer.parseInt((String) rowVector.get(2));
			if (nodeIDFrom.equals(checkNodeIDFrom) == true) {
				if (nodeIDTo.equals(checkNodeIDTo) == true) {
					return rowVector;
				}
			} else if (nodeIDFrom.equals(checkNodeIDTo) == true) {
				if (nodeIDTo.equals(checkNodeIDFrom) == true) {
					return rowVector;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the number connections for each Node.
	 * 
	 * @return the node connections
	 */
	private HashMap<Integer, Integer> getNodeConnections() {

		HashMap<Integer, Integer> nodeConnections = new HashMap<>();

		CsvDataController dc = this.getCsvDataController().get(this.FILE_BranchParams);
		for (int row = 0; row < dc.getDataModel().getRowCount(); row++) {

			for (int column = 0; column < 2; column++) {
				Integer nodeID = Integer.parseInt((String) dc.getDataModel().getValueAt(row, column));
				Integer noOfConnection = nodeConnections.get(nodeID);
				if (noOfConnection == null) {
					nodeConnections.put(nodeID, 1);
				} else {
					nodeConnections.put(nodeID, noOfConnection + 1);
				}
			}
		}

		// --- Prepare return value -------------------------------------------
		if (nodeConnections.size() == 0)
			nodeConnections = null;
		return nodeConnections;
	}

	/**
	 * Sets the nodes of the NetworkModel.
	 * 
	 * @param nodeConnections the node connections
	 * @return the nodeID to NetworkComponent hash map
	 */
	private HashMap<Integer, NetworkComponent> setNodes(HashMap<Integer, Integer> nodeConnections) {

		HashMap<Integer, NetworkComponent> nodeID2NetComp = new HashMap<Integer, NetworkComponent>();

		double newX = 0;
		double newY = 0;
		double step = 25;

		// --- Import nodes of the graph --------------------------------------
		// --- The different types of nodes are imported in this section
		CsvDataController dc = this.getCsvDataController().get(this.FILE_NodeAssociation);
		CsvDataController dc1 = this.getCsvDataController().get(this.FILE_BranchMeasurementParams);
		for (int row = 0; row < dc.getDataModel().getRowCount(); row++) {

			Integer nodeID = Integer.parseInt((String) dc.getDataModel().getValueAt(row, 0));

			// --- Additional Information, which decides weather a node is
			// --- LoadNode, a DEA or only a sleeve or CableCabinet
			Integer nIsLoadNode = Integer.parseInt((String) dc.getDataModel().getValueAt(row, 1));
			Double dDEAPwrNom = Double.parseDouble((String) dc.getDataModel().getValueAt(row, 2));

			// --- Create the new NetworkComponent for the new NetworkModel ---
			NetworkModel newCompNM = null;
			ElectricalNodeProperties nodeProperties = null;

			Integer noOfConnections = nodeConnections.get(nodeID);

			// --- Checking if node is sleeve or CableCabinet
			if (nIsLoadNode == 0 && dDEAPwrNom == 0) {
				if (noOfConnections > 2) {
					newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "CableCabinet");
					nodeProperties = new ElectricalNodeProperties();
					nodeProperties.setNominalPower(new UnitValue(0, "W"));
					nodeProperties.setIsLoadNode(false);
				} else {
					// --- If node has only two connections and is not a
					// --- loadNode and has no DEA Injection --> Sleeve
					newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Sleeve");
					nodeProperties = new ElectricalNodeProperties();
					nodeProperties.setNominalPower(new UnitValue(0, "W"));
					nodeProperties.setIsLoadNode(false);
				}
				
			} else {
				// --- if node is no Sleeve or CableCabinet --> Transformer,Prosumer, Consumer, Ref-PV-Plant or DEA are possible
				// --- If node is loadNode and has no DEA Injection --> Consumer
				if (nIsLoadNode == 1 && dDEAPwrNom == 0) {
					newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Consumer");
					nodeProperties = new ElectricalNodeProperties();
					nodeProperties.setNominalPower(new UnitValue(0, "W"));
					nodeProperties.setIsLoadNode(true);
					
				} else {
					// DEA, REF-PV, Transformer or Prosumer are possible
					int nSearchingNode = nodeID;
					int nSensorFromNode = 0;
					int nSensorToNode = 0;
					String cSensorID;

					// --- SensorID decides, which type the node belongs to Checking if DEA or
					for (int a = 0; a < dc1.getDataModel().getRowCount(); a++) {
						
						cSensorID = (String) dc1.getDataModel().getValueAt(a, 0);
						nSensorFromNode = Integer.parseInt((String) dc1.getDataModel().getValueAt(a, 1));
						nSensorToNode = Integer.parseInt((String) dc1.getDataModel().getValueAt(a, 2));

						// Checking, if node is Transformer or REF-PV-Plant or DEA
						if (nSensorFromNode == 0 && nSensorToNode == nSearchingNode) {
							// --- SensorID starts with 12 --> Ref-PV
							if (cSensorID.charAt(0) == '1' && cSensorID.charAt(1) == '2') {
								newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "REF-PV");
								a = dc1.getDataModel().getRowCount();
								nodeProperties = new ElectricalNodeProperties();
								nodeProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
								nodeProperties.setIsLoadNode(false);
							
							} else if (cSensorID.charAt(0) == '1' && cSensorID.charAt(1) == '4') {
								// --- SensorID starts with 14 --> Transformer
								newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Transformer");
								a = dc1.getDataModel().getRowCount();
								
								UnitValue ratedVoltage = new UnitValue();
								ratedVoltage.setValue(400.0f);
								ratedVoltage.setUnit("V");

								TransformerNodeProperties transformerProperties = new TransformerNodeProperties();
								transformerProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
								transformerProperties.setIsLoadNode(false);
								transformerProperties.setRatedVoltage(ratedVoltage);
								nodeProperties = transformerProperties;
								
							} else {
								newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "DEA");
								nodeProperties = new ElectricalNodeProperties();
								nodeProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
								nodeProperties.setIsLoadNode(false);
								
							}
							
						} else {
							if (nIsLoadNode == 1 && dDEAPwrNom > 0) {
								newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Prosumer");
								nodeProperties = new ElectricalNodeProperties();
								nodeProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
								nodeProperties.setIsLoadNode(true);
								
							} else if (nIsLoadNode == 0 && dDEAPwrNom > 0) {
								if (cSensorID.charAt(0) == '1' && cSensorID.charAt(1) == '4') {
									newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Transformer");
									a = dc1.getDataModel().getRowCount();
									
									UnitValue ratedVoltage = new UnitValue();
									ratedVoltage.setValue(400.0f);
									ratedVoltage.setUnit("V");

									TransformerNodeProperties transformerProperties = new TransformerNodeProperties();
									transformerProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
									transformerProperties.setIsLoadNode(false);
									transformerProperties.setRatedVoltage(ratedVoltage);
									nodeProperties = transformerProperties;
									
								} else {
									newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "DEA");
									nodeProperties = new ElectricalNodeProperties();
									nodeProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
									nodeProperties.setIsLoadNode(false);
								}
							} 
						}

					}// end for
				}
			}

			if (newCompNM == null) {
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Prosumer");
				nodeProperties = new ElectricalNodeProperties();
				nodeProperties.setNominalPower(new UnitValue(dDEAPwrNom.floatValue(), "W"));
				nodeProperties.setIsLoadNode(true);
			}

			NetworkComponent newComp = newCompNM.getNetworkComponents().values().iterator().next();
			String nodeName = newComp.getGraphElementIDs().iterator().next();

			// --- Remind the new component -----------------------------------
			nodeID2NetComp.put(nodeID, newComp);

			// --- Define the GraphNode position ------------------------------
			GraphNode graphNode = (GraphNode) newCompNM.getGraphElement(nodeName);
			newX += step;
			newY += step;
			Point2D pos = new Point2D.Double(newX, newY);
			graphNode.setPosition(pos);

			// --- Add an empty time series chart object to match the requirements of the adapter ------
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
			
			TriPhaseElectricalNodeState nodeState;
			if (newComp.getType().equals("Transformer")) {
				nodeState = new TriPhaseElectricalTransformerState();
			} else {
				nodeState = new TriPhaseElectricalNodeState();
			}

			// --- Set the data model --------------
			Object[] dataModel = new Object[3];
			dataModel[0] = nodeProperties;
			dataModel[1] = nodeState;
			dataModel[2] = tsc;
			graphNode.setDataModel(dataModel);

			// --- Merge into the new NetworkModel ----------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, true);
		}

		// --- Prepare return value -------------------------------------------
		if (nodeID2NetComp.size() == 0) nodeID2NetComp = null;
		return nodeID2NetComp;
	}

	/* (non-Javadoc)
	 * @see hygrid.electricalNetwork.csvImport.CsvFileImporter#initializeValidFileNames()
	 */
	@Override
	protected Vector<String> getListOfRequiredFileNames() {
		Vector<String> validFileNames = new Vector<>();
		validFileNames.add(this.FILE_NodeAssociation);
		validFileNames.add(this.FILE_BranchParams);
		validFileNames.add(this.FILE_BranchMeasurementParams);
		validFileNames.add(this.FILE_BreakerParams);
		validFileNames.add(this.FILE_ControlParams);
		return validFileNames;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.csv.NetworkTopologyImporterService#setEnvironmentController(org.awb.env.networkModel.controller.GraphEnvironmentController)
	 */
	@Override
	public void setGraphController(GraphEnvironmentController graphController) {
		this.graphController = graphController;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.csv.NetworkTopologyImporterService#getFileFilters()
	 */
	@Override
	public List<FileFilter> getFileFilters() {
		if (fileFilters==null) {
			fileFilters = new ArrayList<FileFilter>();
			fileFilters.add(this.createFileFilter(".zip", "SAG Electrical Network (zip File)"));
			fileFilters.add(this.createFileFilter(".csv", "SAG Electrical Network (csv File)"));
		}
		return fileFilters;
	}

}
