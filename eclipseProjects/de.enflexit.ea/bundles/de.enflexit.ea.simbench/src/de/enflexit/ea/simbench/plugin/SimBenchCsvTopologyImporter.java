package de.enflexit.ea.simbench.plugin;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.NetworkComponentFactory;
import org.awb.env.networkModel.maps.MapSettings;
import org.awb.env.networkModel.maps.MapSettings.MapScale;
import org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter;
import org.awb.env.networkModel.persistence.NetworkModelImportService;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import agentgui.core.application.Application;
import agentgui.core.gui.projectwindow.simsetup.TimeModelController;
import agentgui.core.project.Project;
import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import agentgui.simulationService.environment.AbstractEnvironmentModel;
import agentgui.simulationService.time.TimeModel;
import agentgui.simulationService.time.TimeModelDateBased;
import de.enflexit.common.csv.CsvDataController;
import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.simbench.persistence.SimBenchFileStore;
import de.enflexit.ea.simbench.persistence.SimBenchFileStoreReader;
import de.enflexit.ea.simbench.persistence.SimBenchScheduelListPersistenceService;
import de.enflexit.ea.simbench.persistence.SimBenchStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;
import de.enflexit.geography.coordinates.UTMCoordinate;
import de.enflexit.geography.coordinates.WGS84LatLngCoordinate;
import energy.helper.NumberHelper;
import energy.optionModel.TimeRange;
import energy.schedule.loading.ScheduleTimeRange;
import energy.schedule.loading.ScheduleTimeRange.RangeType;
import energy.schedule.loading.ScheduleTimeRangeController;

/**
 * The Class SimBench_CsvTopologyImporter.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimBenchCsvTopologyImporter extends AbstractNetworkModelCsvImporter implements NetworkModelImportService {

	private static final String LAYOUT_DEFAULT_LAYOUT = GeneralGraphSettings4MAS.DEFAULT_LAYOUT_SETTINGS_NAME;
	private static final String LAYOUT_GeoCoordinates_WGS84 = "Geo-Coordinates WGS84";
	private static final String LAYOUT_GeoCoordinates_UTM = "Geo-Coordinates UTM 32U";
	
	private static final String SIMBENCH_Coordinates	 = "Coordinates.csv";
	private static final String SIMBENCH_ExternalNet	 = "ExternalNet.csv";
	private static final String SIMBENCH_Line			 = "Line.csv";
	private static final String SIMBENCH_LineType		 = "LineType.csv";
	private static final String SIMBENCH_Load			 = "Load.csv";
	private static final String SIMBENCH_LoadProfile	 = "LoadProfile.csv";
	private static final String SIMBENCH_Measurement	 = "Measurement.csv";
	private static final String SIMBENCH_Node			 = "Node.csv";
	private static final String SIMBENCH_NodePFResult	 = "NodePFResult.csv";
	private static final String SIMBENCH_RES			 = "RES.csv";
	private static final String SIMBENCH_RESProfile		 = "RESProfile.csv";
	private static final String SIMBENCH_Storage		 = "Storage.csv";
	private static final String SIMBENCH_StorageProfile  = "StorageProfile.csv";
	private static final String SIMBENCH_StudyCases		 = "StudyCases.csv";
	private static final String SIMBENCH_Transformer	 = "Transformer.csv";
	private static final String SIMBENCH_TransformerType = "TransformerType.csv";

	
	public enum StorageDestination {
		Base64,
		File,
		Database,
		SimBenchService
	}

	private List<FileFilter> fileFilterList;
	
	// --- From here variables for the topology import ------------------------
	private NetworkModel networkModel;
	
	private String layoutIdDefault;
	private String layoutIdGeoCoordinateWGS84;
	private String layoutIdGeoCoordinateUTM;

	private MapSettings mapSettings;
	private Vector<Point2D> defaultPositionVector;
	
	private StorageDestination storageDestination = StorageDestination.SimBenchService;
	private SimBenchScheduelListPersistenceService persistenceService;
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#getFileFilters()
	 */
	@Override
	public List<FileFilter> getFileFilters() {
		if (fileFilterList==null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(this.createFileFilter(".csv", "SimBench Low Voltage Eletrical Network (csv files)"));
		}
		return fileFilterList;
	}
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter#getListOfRequiredFileNames()
	 */
	@Override
	protected Vector<String> getListOfRequiredFileNames() {
		
		Vector<String> fileNameVector = new Vector<>(); 
		fileNameVector.add(SIMBENCH_Coordinates);
		fileNameVector.add(SIMBENCH_ExternalNet);
		fileNameVector.add(SIMBENCH_Line);
		fileNameVector.add(SIMBENCH_LineType);
		fileNameVector.add(SIMBENCH_Load);
		fileNameVector.add(SIMBENCH_LoadProfile);
		fileNameVector.add(SIMBENCH_Measurement);
		fileNameVector.add(SIMBENCH_Node);
		fileNameVector.add(SIMBENCH_NodePFResult);
		fileNameVector.add(SIMBENCH_RES);
		fileNameVector.add(SIMBENCH_RESProfile);
		fileNameVector.add(SIMBENCH_Storage);
		fileNameVector.add(SIMBENCH_StorageProfile);
		fileNameVector.add(SIMBENCH_StudyCases);
		fileNameVector.add(SIMBENCH_Transformer);
		fileNameVector.add(SIMBENCH_TransformerType);
		return fileNameVector;
	}
	
	/**
	 * Return the current NetworkModel that has to be generated by this importer.
	 * @return the networkType model
	 */
	protected NetworkModel getNetworkModel() {
		if (networkModel == null) {
			networkModel = new NetworkModel();
			networkModel.setGeneralGraphSettings4MAS(this.graphController.getNetworkModel().getGeneralGraphSettings4MAS());
			networkModel.getMapSettingsTreeMap().put(this.getLayoutIdGeoCoordinateUTM(), this.getMapSettings());
		}
		return networkModel;
	}
	
	/**
	 * Returns the {@link MapSettings}.
	 * @return the map settings
	 */
	private MapSettings getMapSettings() {
		if (mapSettings==null) {
			mapSettings = new MapSettings();
			mapSettings.setUTMLongitudeZone(32);
			mapSettings.setUTMLatitudeZone("U");
			mapSettings.setMapScale(MapScale.s3x);
			mapSettings.setMapTileTransparency(50);
		}
		return mapSettings;
	}
	
	/**
	 * Return the layout-ID for default coordinates.
	 * @return the layout id OGE internal
	 */
	public String getLayoutIdDefault() {
		if (layoutIdDefault==null) {
			layoutIdDefault = this.getNetworkModel().getGeneralGraphSettings4MAS().getLayoutIdByLayoutName(LAYOUT_DEFAULT_LAYOUT);
		}
		return layoutIdDefault;
	}
	/**
	 * Returns the layout-ID for WGS84 geo coordinate.
	 * @return the layout id geo coordinate
	 */
	public String getLayoutIdGeoCoordinateWGS84() {
		if (layoutIdGeoCoordinateWGS84==null) {
			layoutIdGeoCoordinateWGS84 = this.getNetworkModel().getGeneralGraphSettings4MAS().getLayoutIdByLayoutName(LAYOUT_GeoCoordinates_WGS84); 
		}
		return layoutIdGeoCoordinateWGS84;
	}
	/**
	 * Returns the layout-ID for UTM geo coordinate WGS84.
	 * @return the layout id geo coordinate
	 */
	public String getLayoutIdGeoCoordinateUTM() {
		if (layoutIdGeoCoordinateUTM==null) {
			layoutIdGeoCoordinateUTM = this.getNetworkModel().getGeneralGraphSettings4MAS().getLayoutIdByLayoutName(LAYOUT_GeoCoordinates_UTM); 
		}
		return layoutIdGeoCoordinateUTM;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#cleanupImporter()
	 */
	@Override
	public void cleanupImporter() {
		
		super.cleanupImporter();
		
		this.networkModel = null;
		
		this.layoutIdDefault = null;
		this.layoutIdGeoCoordinateWGS84 = null;
		this.layoutIdGeoCoordinateUTM = null;
		
		this.mapSettings = null;
		
		this.defaultPositionVector = null;
		
		this.persistenceService = null;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#importNetworkModelFromFile(java.io.File)
	 */
	@Override
	public NetworkModel importNetworkModelFromFile(File directoryFile) {
		
		try {
			// --- Set status information -------------------------------------
			Application.setStatusBarMessage(this.getClass().getSimpleName() + ": Import files from " + directoryFile.getParent() + " ... ");

			this.debug = true;
			
			// --- Read the csv files -----------------------------------------
			this.readCsvFiles(directoryFile, true); 
			// --- Show import preview if this.debug is set to true -----------
			this.showImportPreview();
		
			// --- Set working directory of SimBenchFileStore -----------------
			SimBenchFileStore.getInstance().setSimBenchDirectoryFile(directoryFile, false);
			// --- Set setup TimeModel ----------------------------------------
			this.setTimeRangeToSetupTimeModel();
			
			// --- The main import work to be done ----------------------------
			this.createNodes(directoryFile);
			this.createLines();
			
			this.showError();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Application.setStatusBarMessageReady();
		}
		
		// --- Return the NetworkModel ----------------------------------------
		return this.getNetworkModel();
	}


	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.NetworkModelFileImporter#getAbstractEnvironmentModel()
	 */
	@Override
	public AbstractEnvironmentModel getAbstractEnvironmentModel() {
		return null;
	}
	
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
		return 1;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here: some methods for getting the lines ------------------------------------------
	// --------------------------------------------------------------------------------------------	
	/**
	 * Creates the lines.
	 */
	private void createLines() {
		
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(SIMBENCH_Line);
		Vector<Vector<String>> lineDataVector = this.getDataVectorOfCsvFile(SIMBENCH_Line);
			
		String colNameID = "id";
		String colNameNodeA = "nodeA";
		String colNameNodeB = "nodeB";
		String colNameType = "type";
		String colNameLength = "length";
		
		int ciID = nodeCsvController.getDataModel().findColumn(colNameID);
		int ciNodeA = nodeCsvController.getDataModel().findColumn(colNameNodeA);
		int ciNodeB = nodeCsvController.getDataModel().findColumn(colNameNodeB);
		int ciType = nodeCsvController.getDataModel().findColumn(colNameType);
		int ciLength = nodeCsvController.getDataModel().findColumn(colNameLength);
		
		for (int i = 0; i < lineDataVector.size(); i++) {
			
			// --- Get line data row ----------------------------------------------------
			Vector<String> row = lineDataVector.get(i);
			String lineID  = row.get(ciID);
			String nodeA = row.get(ciNodeA);
			String nodeB = row.get(ciNodeB);
			String lineType = row.get(ciType);
			String lineLength = row.get(ciLength);

			Float lengthInKilometer = this.parseFloat(lineLength);
			Float lengthInMeter = null;
			if (lengthInKilometer!=null) {
				lengthInMeter = lengthInKilometer * 1000f;
			}
			
			// --- Get the voltage level of that line -----------------------------------
			boolean isTriPhaseNetwork = true;
			
			HashMap<String, String> nodeRowHashMapNodeA = this.getDataRowHashMap(SIMBENCH_Node, "id", nodeA);
			double voltageLevelNodeA = this.parseDouble(nodeRowHashMapNodeA.get("vmR")) * 1000;
			HashMap<String, String> nodeRowHashMapNodeB = this.getDataRowHashMap(SIMBENCH_Node, "id", nodeB);
			double voltageLevelNodeB = this.parseDouble(nodeRowHashMapNodeB.get("vmR")) * 1000;
			if (voltageLevelNodeA!=voltageLevelNodeB) {
				System.err.println("[" + this.getClass().getName() + "] Different voltage levels were found for line '" + lineID + "' between node '" + nodeA + "' and node '" + nodeB + "'");
			} else {
				isTriPhaseNetwork = SimBenchFileStore.isTriPhaseVoltageLevel(voltageLevelNodeA);
			}
			
			// --- Get resistance and reactance  --------------------------------------------
			HashMap<String, String> dataRowHashMap = this.getDataRowHashMap(SIMBENCH_LineType, "id", lineType);
			Float lineResistance = this.parseFloat(dataRowHashMap.get("r"));
			Float lineReactance = this.parseFloat(dataRowHashMap.get("x"));
			//Herleitung der Capacitance
			//double b / 1E-6= 2 * 50 * Math.PI * matGridData.get(i).getdC()*1E-9;
			//Float linCapacitance = this.parseFloat(dataRowHashMap.get("b")) / (float)(1E6 * 2 * 50 * Math.PI * 1E-9);
			Float lineCapacitance = this.parseFloat(dataRowHashMap.get("b")) / ((float)(100 * Math.PI * 1E-3));
			Float maxCurrent = this.parseFloat(dataRowHashMap.get("iMax"));
			
			
			// --- Prepare new NetworkComponent -----------------------------------------
			NetworkModel newCompNM = null;
			CableProperties cableProperties = null;
			
			// --- Create new NetworkComponent by using the NetworkComponentFactory -----
			// POSSIBLY a case separation for specific NetworkComponent types? 
			
			if (isTriPhaseNetwork==true) {
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Cable");
			} else {
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvCable");
			}
			cableProperties = new CableProperties();
			
			
			// --- Get NetworkComponent and GrahNode for renaming -----------------------
			NetworkComponent newNetComp = newCompNM.getNetworkComponents().values().iterator().next();
			Object[] graphNodes = newCompNM.getGraphElementsOfNetworkComponent(newNetComp, new GraphNode()).toArray();
			GraphNode newGraphNodeFrom = (GraphNode) graphNodes[0];
			GraphNode newGraphNodeTo = (GraphNode) graphNodes[1];

			// --- Rename NetworkComponent and GraphNode --------------------------------
			newCompNM.renameNetworkComponent(newNetComp.getId(), lineID);
			newCompNM.renameGraphNode(newGraphNodeFrom.getId(), this.getLocalGraphNodeName(nodeA));
			newCompNM.renameGraphNode(newGraphNodeTo.getId(), this.getLocalGraphNodeName(nodeB));
			
			
			// --- Set the parameters for the component ------------------------
			cableProperties.setLength(new UnitValue(lengthInMeter, "m"));
			cableProperties.setLinearResistance(new UnitValue(lineResistance, "Ω/km"));
			cableProperties.setLinearReactance(new UnitValue(lineReactance, "Ω/km"));
			cableProperties.setLinearCapacitance(new UnitValue(lineCapacitance, "nF/km"));
			cableProperties.setMaxCurrent(new UnitValue(maxCurrent, "A"));

			// --- Add an empty time series chart object to match the requirements of the adapter ------
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());

			// --- Set the data model --------------
			Object[] dataModel = new Object[3];
			dataModel[0] = cableProperties;
			if (isTriPhaseNetwork==true) {
				dataModel[1] = new TriPhaseCableState();
			} else {
				dataModel[1] = new UniPhaseCableState();
			}
			dataModel[2] = tsc;
			newNetComp.setDataModel(dataModel);
			
			// --- Merge into the new NetworkModel --------------------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, false);
			
		} // end for
		
	}
	/**
	 * Returns the local graph node name by using the already created GraphNodes in the local NetworkModel.
	 *
	 * @param edgeNodeName the edge node name (from SimBench)
	 * @return the local graph node name
	 */
	private String getLocalGraphNodeName(String edgeNodeName) {
		
		List<GraphNode> nodeList = new ArrayList<>(this.getNetworkModel().getGraph().getVertices());
		for (int i = 0; i < nodeList.size(); i++) {
			String localNodeName = nodeList.get(i).getId();
			if (localNodeName.equals(edgeNodeName)==true) {
				return localNodeName;
			} else if (this.isLocalGraphNode(edgeNodeName, localNodeName)==true) {
				return localNodeName;
			}
		}
		return null;
	}
	/**
	 * Checks if the specified edge node name matches the specified local graph node.
	 *
	 * @param edgeNodeName the edge node name
	 * @param localGraphNodeName the local graph node name
	 * @return true, if is local graph node
	 */
	private boolean isLocalGraphNode(String edgeNodeName, String localGraphNodeName) {
		
		boolean isLocalGraphNode = false;
		if (edgeNodeName.startsWith(localGraphNodeName)==true) {
			String verifyier = edgeNodeName.substring(localGraphNodeName.length());
			isLocalGraphNode = verifyier.startsWith("_");
		}
		return isLocalGraphNode;
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here: some methods for getting the nodes ------------------------------------------
	// --------------------------------------------------------------------------------------------	
	/**
	 * Creates the nodes in the NetworkModel.
	 */
	private void createNodes(File directoryFile) {
		
		// --- Check if the directory file is part of the project directory -------------
		File directory = directoryFile;
		if (directory.isDirectory()==false) {
			directory = directory.getParentFile();
		}
		String sbPathName = directory.getAbsolutePath();
		String projectPathName = Application.getProjectFocused().getProjectFolderFullPath();
		if (sbPathName.startsWith(projectPathName)==true) {
			sbPathName = sbPathName.substring(projectPathName.length(), sbPathName.length());
		}
		
		// --- Get the base data to create the nodes ------------------------------------
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(SIMBENCH_Node);
		Vector<Vector<String>> nodeDataVector = this.getDataVectorOfCsvFile(SIMBENCH_Node);
			
		String colNameID = "id";
		String colNameType = "type";
		String colNameCoord = "coordID";
		
		int ciID = nodeCsvController.getDataModel().findColumn(colNameID);
		int ciType = nodeCsvController.getDataModel().findColumn(colNameType);
		int ciCoordID = nodeCsvController.getDataModel().findColumn(colNameCoord);
		
		for (int i = 0; i < nodeDataVector.size(); i++) {
			
			// --- Get node data row ----------------------------------------------------
			Vector<String> row = nodeDataVector.get(i);
			String nodeID  = row.get(ciID);
			String type = row.get(ciType);
			String coordID = row.get(ciCoordID);
			
			if (type.equals("busbar")==false && type.equals("node")==false) continue;
			
			// --- Get the voltage Level of that node -----------------------------------
			HashMap<String, String> nodeRowHashMapNodeA = this.getDataRowHashMap(SIMBENCH_Node, "id", nodeID);
			double voltageLevel = this.parseDouble(nodeRowHashMapNodeA.get("vmR")) * 1000;
			
			// --- Provide some user information ----------------------------------------
			Application.setStatusBarMessage(this.getClass().getSimpleName() + ": Import node '" + nodeID  + "' - (" + (i+1) +  "/" + nodeDataVector.size() + ")");

			// --- Get the corresponding data row of the coordinates --------------------
			HashMap<String, String> dataRowHashMap = this.getDataRowHashMap(SIMBENCH_Coordinates, "id", coordID);
			String coordXString = dataRowHashMap.get("x");
			String coordYString = dataRowHashMap.get("y");
			
			// --- Prepare new NetworkComponent -----------------------------------------
			NetworkModel newCompNM = null;
			String newNetCompID = nodeID; 
			
			// --- Create new NetworkComponent by using the NetworkComponentFactory -----
			Object netCompDataModel = null;
			TreeMap<String, String> netCompStorageSettings = null;
			
			// --------------------------------------------------------------------------
			// --- Case separation for NetworkCmponents ---------------------------------
			// --------------------------------------------------------------------------
			boolean isCreatingTransformer = false;
			String transformerID = this.getTransformerID(nodeID);
			if (transformerID!=null) {
				// --- Create transformer ? ---------------------------------------------
				HashMap<String, String> transformerRowHashMap = this.getDataRowHashMap(SIMBENCH_Transformer, "id", transformerID);
				String nodeLV = transformerRowHashMap.get("nodeLV");
				// --- Only create low voltage node of SimBench model ------------------- 
				if ((nodeLV.equals(nodeID) || this.isLocalGraphNode(nodeLV, nodeID)) && this.getNetworkModel().getNetworkComponent(transformerID)==null) {
					// --- Create transformer from factory ------------------------------
					if (SimBenchFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Transformer");
					} else {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvTransformer");
					}
					newNetCompID = transformerID;
					isCreatingTransformer = true;
					// --- Add the storage settings to the component --------------------
					netCompStorageSettings = this.getTransformerStorageSettings();
					
				} else {
					// --- Do not create this component again ---------------------------
					continue;
				}
				
			} else {
				// --- Prosumer or CableCabinet ? ---------------------------------------
				HashMap<String, String> loadRowHashMap = this.getDataRowHashMap(SIMBENCH_Load, "node", nodeID);
				if (loadRowHashMap==null) {
					// --- Create CableCabinet ------------------------------------------
					if (SimBenchFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "CableCabinet");
					} else {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvCableCabinet");
					}
					
				} else {
					// --- Create Prosumer model ---------------------------------------- 
					if (SimBenchFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Prosumer");
					} else {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvProsumer");
					}
					
					// ------------------------------------------------------------------
					// --- Create ScheduleList and storage settings and save them -------
					// ------------------------------------------------------------------
					netCompDataModel = this.getPersistenceService().loadScheduleList(directoryFile, SimBenchFileStore.SIMBENCH_Node, i, this.getScheduleTimeRange());
					// --- Create storage settings --------------------------------------
					netCompStorageSettings = new TreeMap<>();
					netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.ScheduleList.toString());
					switch (this.storageDestination) {
					case Base64:
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.NetworkElementBase64.toString());
						break;
						
					case File:
						// --- Get the file location for the ScheduleList ---------------
						String profile = loadRowHashMap.get("profile");
						String fileName = NetworkComponent.class.getSimpleName() + "_Prosumer_SL_" + profile + ".xml";
						Project project = Application.getProjectFocused();
						File slFile = EomDataModelStorageHandler.getFileSuggestion(project, fileName);
						File slDirectory = slFile.getParentFile();
						String relPath = EomDataModelStorageHandler.getRelativeProjectPath(project, slFile);
						
						// --- Create directory, if it not exists -----------------------
						if (slDirectory.exists()==false) slDirectory.mkdirs();
						
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.File.toString());
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, relPath);
						break;
						
					case Database:
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.Database.toString());
						break;
						
					case SimBenchService:
						// --- Set to storage settings --------------------------
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.Customized.toString());
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_CUSTOMIZED_STORAGE_HANDLER, SimBenchStorageHandler.class.getName()); 
						netCompStorageSettings.put(SimBenchStorageHandler.SIM_BENCH_SETTING_PATH_NAME, sbPathName);
						netCompStorageSettings.put(SimBenchStorageHandler.SIM_BENCH_SETTING_FILE_NAME, SimBenchFileStore.SIMBENCH_Node);
						netCompStorageSettings.put(SimBenchStorageHandler.SIM_BENCH_SETTING_ROW_INDEX, i + "");
						break;
					}
				}
			}
			
			// --------------------------------------------------------------------------
			// --- Get the actual NetworkComponent --------------------------------------
			// --------------------------------------------------------------------------
			NetworkComponent newComp = newCompNM.getNetworkComponents().values().iterator().next();
			String nodeName = newComp.getGraphElementIDs().iterator().next();
			GraphNode graphNode = (GraphNode) newCompNM.getGraphElement(nodeName);

			// --- Rename the elements --------------------------------------------------
			newCompNM.renameNetworkComponent(newComp.getId(), newNetCompID);
			newCompNM.renameGraphNode(graphNode.getId(), nodeID);
			
			// --- Define the GraphNode positions ---------------------------------------
			double wgs84Long = this.parseDouble(coordXString);
			double wgs84Lat  = this.parseDouble(coordYString);
			this.setGraphNodeCoordinates(graphNode, wgs84Lat, wgs84Long);
			
			// --------------------------------------------------------------------------
			// --- Define first part of the GraphNode's data model ----------------------
			ElectricalNodeProperties nodeProps = new ElectricalNodeProperties();
			if (isCreatingTransformer==true) {
				// --- Create low voltage node data model -----------
				UnitValue uValue = new UnitValue();
				uValue.setValue((float)voltageLevel);
				uValue.setUnit("V");
				TransformerNodeProperties transformerProps = new TransformerNodeProperties();
				transformerProps.setRatedVoltage(uValue);
				nodeProps = transformerProps;
			}
			nodeProps.setIsLoadNode(true);
			
			// --- Define TimeSeriesChart as second part of the GraphNode data model ----
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
			
			// --------------------------------------------------------------------------
			// --- Set the data model to the GraphNode ----------------------------------
			Object[] dataModel = new Object[3];
			dataModel[0] = nodeProps;
			if (SimBenchFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
				dataModel[1] = new TriPhaseElectricalNodeState();
			} else {
				dataModel[1] = new UniPhaseElectricalNodeState();
			}
			dataModel[2] = tsc;
			graphNode.setDataModel(dataModel);

			// --------------------------------------------------------------------------
			// --- Set the data model to the NetworkComponent ---------------------------
			newComp.setDataModel(netCompDataModel);
			newComp.setDataModelStorageSettings(netCompStorageSettings);
			
			// --- Merge into the new NetworkModel --------------------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, false);
			
		} // end for
		
		// --- Adjust the node positions for the default layout ------------------------- 
		this.adjustDefaultPositions();
	}
	
	/**
	 * Checks if the specified node ID represents a transformer and returns either the 
	 * ID of the transformer or <code>null</code>:.
	 *
	 * @param nodeID the node ID
	 * @return the transformer ID or <code>null</code>
	 */
	private String getTransformerID(String nodeID) {
		
		CsvDataController transformerCsvController = this.getCsvDataControllerOfCsvFile(SIMBENCH_Transformer);
		Vector<Vector<String>> transformerDataVector = this.getDataVectorOfCsvFile(SIMBENCH_Transformer);
		
		String colID = "id";
		String colNodeHV = "nodeHV";
		String colNodeLV = "nodeLV";
		
		int ciID = transformerCsvController.getDataModel().findColumn(colID);
		int ciNodeHV = transformerCsvController.getDataModel().findColumn(colNodeHV);
		int ciNodeLV = transformerCsvController.getDataModel().findColumn(colNodeLV);
		
		for (int i = 0; i < transformerDataVector.size(); i++) {
		
			Vector<String> row = transformerDataVector.get(i);
			String id = row.get(ciID);
			String idNodeHV = row.get(ciNodeHV);
			String idNodeLV = row.get(ciNodeLV);
			
			if (idNodeHV.equals(nodeID) || this.isLocalGraphNode(idNodeHV, nodeID)) {
				// --- High voltage node ------------------
				return id;
				
			} else if (idNodeLV.equals(nodeID) || this.isLocalGraphNode(idNodeLV, nodeID)) {
				// --- Low voltage node -------------------
				return id;
				
			}
		}
		return null;
	}
	/**
	 * Returns the transformer storage settings.
	 * @return the transformer storage settings
	 */
	private TreeMap<String, String> getTransformerStorageSettings() {
		TreeMap<String, String> tSettings = new TreeMap<>();
		tSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.TechnicalSystem.toString());
		tSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.File.toString());
		tSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, "/eomModels/EomModel_Transformer.xml");
		return tSettings; 
	}
	
	
	/**
	 * Sets the GraphNode coordinates.
	 *
	 * @param graphNode the graph node
	 * @param latNorthSouth the latitude value (North / South)
	 * @param longEastWest the longitude value (East / West)
	 */
	private void setGraphNodeCoordinates(GraphNode graphNode, double latNorthSouth, double longEastWest) {
		
		// --- Set GraphNode position according to node -------------
		Point2D pointGeoWGS84 = new Point2D.Double(latNorthSouth, longEastWest);
		
		// --- Calculate to UTM coordinate --------------------------
		WGS84LatLngCoordinate coordWGS84 = new WGS84LatLngCoordinate(pointGeoWGS84.getX(), pointGeoWGS84.getY());
		UTMCoordinate coordUTM = coordWGS84.getUTMCoordinate(this.getMapSettings().getUTMLongitudeZone(), this.getMapSettings().getUTMLatitudeZone());
		Point2D pointUTM = new Point2D.Double(coordUTM.getEasting(), coordUTM.getNorthing());
		
		// --- Set default layout to Default ------------------------
		Point2D pointDefault = this.getDefaultLayoutPosition(pointGeoWGS84);
		graphNode.setPosition(pointDefault);
		this.getDefaultPositionVector().add(pointDefault);
		
		// --- Set positions to alternative layouts -----------------
		graphNode.getPositionTreeMap().put(this.getLayoutIdDefault(), pointDefault);
		graphNode.getPositionTreeMap().put(this.getLayoutIdGeoCoordinateWGS84(), pointGeoWGS84);
		graphNode.getPositionTreeMap().put(this.getLayoutIdGeoCoordinateUTM(), pointUTM);
	}
	/**
	 * Return the default layout position, derived from the WGS84 coordinates.
	 *
	 * @param wgs84Point the WGS84 point
	 * @return the default layout position
	 */
	private Point2D getDefaultLayoutPosition(Point2D wgs84Point) {
		double xPos = NumberHelper.round(wgs84Point.getY() *  100000.0, 1);
		double yPos = NumberHelper.round(wgs84Point.getX() * -100000.0, 1);
		return new Point2D.Double(xPos, yPos);
	}
	/**
	 * Reminder for the default positions and a later correction.
	 * @return the default position vector
	 */
	private Vector<Point2D> getDefaultPositionVector() {
		if (defaultPositionVector==null) {
			defaultPositionVector = new Vector<>();
		}
		return defaultPositionVector;
	}
	/**
	 * Adjusts the default node positions.
	 */
	private void adjustDefaultPositions() {

		if (this.getDefaultPositionVector().size()==0) return;
		
		// --- Get the spreading rectangle of the default positions -----------
		Point2D initialPoint = this.getDefaultPositionVector().get(0);
		Rectangle2D spreadRectangle = new Rectangle2D.Double(initialPoint.getX(), initialPoint.getY(), 0, 0);
		for (int i = 0; i < this.getDefaultPositionVector().size(); i++) {
			Point2D singlePos = this.getDefaultPositionVector().get(i);
			spreadRectangle.add(singlePos);
		}
		
		// --- Calculate movement ---------------------------------------------
		double moveX = 0;
		double moveY = 0;
		
		if (spreadRectangle.getX()>=0) {
			moveX = -spreadRectangle.getX(); 
		} else {
			moveX = spreadRectangle.getX();
		}
		
		if (spreadRectangle.getY()>=0) {
			moveY = spreadRectangle.getY();
		} else {
			moveY = -spreadRectangle.getY();
		}

		// --- Move the default positions -------------------------------------
		for (int i = 0; i < this.getDefaultPositionVector().size(); i++) {
			Point2D singlePos = this.getDefaultPositionVector().get(i);
			singlePos.setLocation(singlePos.getX() + moveX, singlePos.getY() + moveY);
		}
		
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here: some methods for creating system schedules can be found ---------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the SimBench persistence service
	 * @return the SimBench persistence service
	 */
	public SimBenchScheduelListPersistenceService getPersistenceService() {
		if (persistenceService==null) {
			persistenceService = new SimBenchScheduelListPersistenceService();
		}
		return persistenceService;
	}
	
	/**
	 * Sets the time range to time model.
	 */
	private void setTimeRangeToSetupTimeModel() {

		TimeRange timeRange = this.getDataTimeRange();
		if (timeRange!=null) {
			// --- Get the current time model -----------------------
			TimeModelController tmc = this.graphController.getProject().getTimeModelController();
			TimeModel timeModel = tmc.getTimeModel();
			if (timeModel instanceof TimeModelDateBased) {
				// --- Adjust the current time model ----------------
				TimeModelDateBased timeModelDateBased = (TimeModelDateBased) timeModel;
				timeModelDateBased.setTimeStart(timeRange.getTimeFrom());
				timeModelDateBased.setTimeStop(timeRange.getTimeTo());
				// --- Refresh project settings ---------------------
				tmc.saveTimeModelToSimulationSetup();
			}
		}
	}
	/**
	 * Returns the data time range and will configure the current {@link ScheduleTimeRange}.
	 * @return the data time range
	 */
	public TimeRange getDataTimeRange() {
		TimeRange timeRange = new SimBenchFileStoreReader().getTimeRangeOfData();
		if (timeRange!=null) {
			// --- Adjust ScheduleTimeRange to max 100 states from start time -
			ScheduleTimeRange str = new ScheduleTimeRange();
			str.setRangeType(RangeType.StartTimeAndNumber);
			str.setTimeFrom(timeRange.getTimeFrom());
			str.setTimeTo(timeRange.getTimeTo());
			str.setNumberOfSystemStates(100);
			
			ScheduleTimeRangeController.setScheduleTimeRange(str);
		}
		return timeRange;
	}
	/**
	 * Returns the currently configures {@link ScheduleTimeRange}.
	 * @return the schedule time range
	 */
	private ScheduleTimeRange getScheduleTimeRange() {
		return ScheduleTimeRangeController.getScheduleTimeRange();
	}
	
	
	// --------------------------------------------------------------------------------------------
	// --- From here: Some general help methods ---------------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Return a data row as HashMap, where the key is the column name and the value the row value.
	 *
	 * @param csvFileName the csv file name
	 * @param keyColumnName the key column name
	 * @param keyValue the key value to search for
	 * @return the data row as HashMap
	 */
	private HashMap<String, String> getDataRowHashMap(String csvFileName, String keyColumnName, String keyValue) {
		
		HashMap<String, String> dataRowHashMap = null;
		
		CsvDataController csvController = this.getCsvDataControllerOfCsvFile(csvFileName);
		if (csvController!=null) {
			
			// --- Find the right row -------------------------------
			int idColumnIndex = csvController.getDataModel().findColumn(keyColumnName);
			if (idColumnIndex!=-1) {
				// --- Found key column -----------------------------
				int dataRowIndex = -1;
				for (int rowIndex = 0; rowIndex < csvController.getDataModel().getRowCount(); rowIndex++) {
					String currKeyValue = (String)csvController.getDataModel().getValueAt(rowIndex, idColumnIndex);
					if (currKeyValue.equals(keyValue)==true) {
						dataRowIndex = rowIndex;
						break;
					}
				}
				
				if (dataRowIndex!=-1) {
					// --- Found row, get values --------------------
					dataRowHashMap = new HashMap<>();
					for (int i = 0; i < csvController.getDataModel().getColumnCount(); i++) {
						String colName = csvController.getDataModel().getColumnName(i);
						String value = (String) csvController.getDataModel().getValueAt(dataRowIndex, i);
						dataRowHashMap.put(colName, value);
					}
				}
			}
		}
		return dataRowHashMap;
	}
	
	/**
	 * Returns the data vector of csv file.
	 *
	 * @param csvFileName the csv file name
	 * @return the data vector of csv file
	 */
	@SuppressWarnings("unchecked")
	private Vector<Vector<String>> getDataVectorOfCsvFile(String csvFileName) {
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(csvFileName);
		if (nodeCsvController!=null) {
			return new Vector<Vector<String>>((Collection<? extends Vector<String>>) nodeCsvController.getDataModel().getDataVector());
		}
		return null;
	}
	
	/**
	 * Return the CsvDataController of the specified csv file.
	 *
	 * @param csvFileName the csv file name
	 * @return the CsvDataController or <code>null</code>
	 */
	private CsvDataController getCsvDataControllerOfCsvFile(String csvFileName) {
		return this.getCsvDataController().get(csvFileName);
	}
	
	/**
	 * Parses the specified string to a double value .
	 *
	 * @param doubleString the double string
	 * @return the double value or null
	 */
	private Double parseDouble(String doubleString) {
		Double dValue = null;
		if (doubleString!=null && doubleString.isEmpty()==false) {
			// --- Replace decimal separator ? ----------------------
			if (doubleString.contains(",")==true) {
				doubleString = doubleString.replace(",", ".");
			}
			// --- Try to parse the double string -------------------
			try {
				dValue = Double.parseDouble(doubleString);
			} catch (Exception ex) {
				// --- No exception will be thrown ------------------
			}
		}
		return dValue;
	}

	/**
	 * Parses the specified string to a double value .
	 *
	 * @param floatString the float string
	 * @return the float value or null
	 */
	private Float parseFloat(String floatString) {
		Float fValue = null;
		if (floatString!=null && floatString.isEmpty()==false) {
			// --- Replace decimal separator ? ----------------------
			if (floatString.contains(",")==true) {
				floatString = floatString.replace(",", ".");
			}
			// --- Try to parse the double string -------------------
			try {
				fValue = Float.parseFloat(floatString);
			} catch (Exception ex) {
				// --- No exception will be thrown ------------------
			}
		}
		return fValue;
	}

}
