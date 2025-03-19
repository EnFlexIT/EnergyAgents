package de.enflexit.ea.topologies.pandaPower;

import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.NetworkComponentFactory;
import org.awb.env.networkModel.maps.MapSettings;
import org.awb.env.networkModel.maps.MapSettings.MapScale;
import org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter;
import org.awb.env.networkModel.persistence.NetworkModelImportService;
import org.awb.env.networkModel.settings.DomainSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.environment.TimeModelController;
import de.enflexit.awb.core.project.Project;
import de.enflexit.awb.simulation.environment.AbstractEnvironmentModel;
import de.enflexit.awb.simulation.environment.time.TimeModel;
import de.enflexit.awb.simulation.environment.time.TimeModelDateBased;
import de.enflexit.common.csv.CsvDataController;
import de.enflexit.ea.core.awbIntegration.adapter.triPhase.TriPhaseElectricalNodeAdapter;
import de.enflexit.ea.core.awbIntegration.adapter.uniPhase.UniPhaseElectricalNodeAdapter;
import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.dataModel.ontology.CableWithBreakerProperties;
import de.enflexit.ea.core.dataModel.ontology.CircuitBreaker;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TransformerNodeProperties;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.electricity.transformer.TransformerDataModel;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TapSide;
import de.enflexit.ea.electricity.transformer.TransformerDataModel.TransformerSystemVariable;
import de.enflexit.ea.topologies.BundleHelper;
import de.enflexit.ea.topologies.pandaPower.PandaPowerNamingMap.NamingMap;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;
import de.enflexit.geography.coordinates.UTMCoordinate;
import de.enflexit.geography.coordinates.WGS84LatLngCoordinate;
import edu.uci.ics.jung.graph.Graph;
import energy.OptionModelController;
import energy.helper.NumberHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TimeRange;
import energy.optionModel.gui.sysVariables.AbstractStaticModel;
import energy.schedule.loading.ScheduleTimeRange;
import energy.schedule.loading.ScheduleTimeRange.RangeType;
import energy.schedule.loading.ScheduleTimeRangeController;
import energy.validation.AbstractTechnicalSystemChecker;

/**
 * The Class SimBench_CsvTopologyImporter.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PandaPowerTopologyImporter extends AbstractNetworkModelCsvImporter implements NetworkModelImportService {

	private static final String LAYOUT_DEFAULT_LAYOUT = GeneralGraphSettings4MAS.DEFAULT_LAYOUT_SETTINGS_NAME;
	private static final String LAYOUT_GeoCoordinates_WGS84 = "Geo-Coordinates WGS84";
	private static final String LAYOUT_GeoCoordinates_UTM = "Geo-Coordinates UTM 32U";
	

	private static final String SIMBENCH_ExternalNet	 = "ExternalNet.csv";
	
	private static final String SIMBENCH_LoadProfile	 = "LoadProfile.csv";
	private static final String SIMBENCH_Measurement	 = "Measurement.csv";
	private static final String SIMBENCH_NodePFResult	 = "NodePFResult.csv";
	private static final String SIMBENCH_RES			 = "RES.csv";
	private static final String SIMBENCH_RESProfile		 = "RESProfile.csv";
	private static final String SIMBENCH_Storage		 = "Storage.csv";
	private static final String SIMBENCH_StorageProfile  = "StorageProfile.csv";
	private static final String SIMBENCH_StudyCases		 = "StudyCases.csv";
	private static final String SIMBENCH_TransformerType = "TransformerType.csv";

	
	public enum StorageDestination {
		Base64,
		File,
		Database,
		PandaPowerService
	}

	private List<FileFilter> fileFilterList;
	
	// --- From here variables for the topology import ------------------------
	private NetworkModel networkModel;
	
	private String layoutIdDefault;
	private String layoutIdGeoCoordinateWGS84;
	private String layoutIdGeoCoordinateUTM;

	private MapSettings mapSettings;
	private boolean isDoAutoLayout;
	
	private StorageDestination storageDestination = StorageDestination.PandaPowerService;
	private PandaPowerScheduelListPersistenceService persistenceService;
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelFileImporter#getFileFilters()
	 */
	@Override
	public List<FileFilter> getFileFilters() {
		if (fileFilterList==null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(this.createFileFilter(".json", "PandaPower Eletrical Network (json file)"));
		}
		return fileFilterList;
	}
	
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.AbstractNetworkModelCsvImporter#getListOfRequiredFileNames()
	 */
	@Override
	protected Vector<String> getListOfRequiredFileNames() {
		
		Vector<String> fileNameVector = new Vector<>(); 
		fileNameVector.add(PandaPowerFileStore.PANDA_Bus);
		fileNameVector.add(PandaPowerFileStore.PANDA_Trafo);
		fileNameVector.add(PandaPowerFileStore.PANDA_Load);
		fileNameVector.add(PandaPowerFileStore.PANDA_BusGeodata);
		fileNameVector.add(PandaPowerFileStore.PANDA_Line);
		fileNameVector.add(PandaPowerFileStore.PANDA_StdLineType);
		fileNameVector.add(PandaPowerFileStore.PANDA_Switch);
		
		fileNameVector.add(SIMBENCH_ExternalNet);
		fileNameVector.add(SIMBENCH_LoadProfile);
		fileNameVector.add(SIMBENCH_Measurement);
		fileNameVector.add(SIMBENCH_NodePFResult);
		fileNameVector.add(SIMBENCH_RES);
		fileNameVector.add(SIMBENCH_RESProfile);
		fileNameVector.add(SIMBENCH_Storage);
		fileNameVector.add(SIMBENCH_StorageProfile);
		fileNameVector.add(SIMBENCH_StudyCases);
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
	
	/**
	 * Sets the panda power naming map.
	 */
	protected void setPandaPowerNamingMapToStaticMap() {
		PandaPowerNamingMap.setPandaPowerNamingMap(NamingMap.PandaPower);
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
		this.persistenceService = null;
		
		this.isDoAutoLayout = false;
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
			
			// --- Set the current PandaPowerMapping --------------------------
			this.setPandaPowerNamingMapToStaticMap();
			
			// --- Set Importer and current file ------------------------------
			PandaPowerFileStore.getInstance().setPandaPowerTopologyImporter(this);
			PandaPowerFileStore.getInstance().setPandaPowerDirectoryFile(directoryFile, true);
			
			// --- Show import preview if this.debug is set to true -----------
			this.showImportPreview(true);
			
			// --- Set setup TimeModel ----------------------------------------
			this.setTimeRangeToSetupTimeModel();
			
			// --- The main import work to be done ----------------------------
			this.createNodes(directoryFile);
			this.createLines();
			this.createSwitches();
			
			// --- If layout adjustments are required -------------------------
			if (this.isDoAutoLayout==true) {
				this.doAutoLayout();
			}
			
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
		
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.PANDA_Line);
		Vector<Vector<Object>> lineDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.PANDA_Line);
		if (nodeCsvController==null || lineDataVector==null) return;
		
		int ciIndex = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_index));
		int ciName = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.line_name));
		int ciNodeFrom = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_from_bus));
		int ciNodeTo = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_to_bus));
		int ciStdType = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.line_stdType));
		int ciLength = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_length_km));
		
		int ci_r_ohm_per_Km = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_r_ohm_per_km));
		int ci_x_ohm_per_km = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_x_ohm_per_km));
		int ci_c_nf_per_km = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_c_nf_per_km));
		int ci_MaxI = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Line_max_i_ka));
		
		for (int i = 0; i < lineDataVector.size(); i++) {
			
			// --- Get line data row ----------------------------------------------------
			Vector<Object> row = lineDataVector.get(i);
			Integer lineIndex = (Integer) row.get(ciIndex);
			String lineName  = row.get(ciName).toString();
			Integer nodeFrom = (Integer) row.get(ciNodeFrom);
			Integer nodeTo = (Integer) row.get(ciNodeTo);
			String lineStdTypeID = row.get(ciStdType)!=null ? row.get(ciStdType).toString() : null;
			Double lineLength = row.get(ciLength)!=null ? (Double) row.get(ciLength) : null;

			Double rOhmKm = (Double) row.get(ci_r_ohm_per_Km);
			Double xOhmKm = (Double) row.get(ci_x_ohm_per_km);
			Double cNfKm = (Double) row.get(ci_c_nf_per_km);
			Double maxI = (Double) row.get(ci_MaxI);
			
			Float lengthInKilometer = lineLength!=null ? lineLength.floatValue() : null;
			Float lengthInMeter = null;
			if (lengthInKilometer!=null) {
				lengthInMeter = lengthInKilometer * 1000f;
			}
			
			// --- Get the row HahsMap --------------------------------------------------
			HashMap<String, Object> lineRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Line, PandaPowerNamingMap.getColumnName(ColumnName.Line_index), lineIndex.toString());
			
			// --- Get the voltage level of that line -----------------------------------
			boolean isTriPhaseNetwork = true;
			HashMap<String, Object> nodeFromRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index), nodeFrom.toString());
			double nodeFromVoltageLevel = NumberHelper.round(BundleHelper.parseDouble(nodeFromRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_VoltageLevel))) * 1000, 0);
			HashMap<String, Object> nodeToRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index), nodeTo.toString());
			double nodeToVoltageLevel = NumberHelper.round(BundleHelper.parseDouble(nodeToRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_VoltageLevel))) * 1000, 0);
			if (nodeFromVoltageLevel!=nodeToVoltageLevel) {
				System.err.println("[" + this.getClass().getName() + "] Different voltage levels were found for line "+ lineIndex + " ('" + lineName + "') between node '" + nodeFrom + "' and node '" + nodeTo + "'");
			} else {
				isTriPhaseNetwork = PandaPowerFileStore.isTriPhaseVoltageLevel(nodeFromVoltageLevel);
			}
			
			
			// --- Get resistance, reactance, capacitance and max I ---------------------
			Float maxCurrent = 0f;
			Float lineResistance = 0f;
			Float lineReactance = 0f;
			Float lineCapacitance = 0f;
					
			HashMap<String, Object> stdLineRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_StdLineType, PandaPowerNamingMap.getColumnName(ColumnName.StdLineType_ID), lineStdTypeID);
			if (maxI!=null) {
				maxCurrent = maxI.floatValue();
			} else {
				if (stdLineRowHashMap!=null) {
					maxCurrent = BundleHelper.parseFloat(stdLineRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.StdLineType_max_i_ka)));
				}
			}
			
			if (rOhmKm!=null) {
				lineResistance = rOhmKm.floatValue();
			} else {
				if (stdLineRowHashMap!=null) {
					lineResistance = BundleHelper.parseFloat(stdLineRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.StdLineType_r_ohm_per_km)));
				}
			}
			
			if (xOhmKm!=null) {
				lineReactance = xOhmKm.floatValue();
			} else {
				if (stdLineRowHashMap!=null) {
					lineReactance = BundleHelper.parseFloat(stdLineRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.StdLineType_x_ohm_per_km)));
				}
			}
			
			if (cNfKm!=null) {
				lineCapacitance = cNfKm.floatValue();
			} else {
				if (stdLineRowHashMap!=null) {
					lineCapacitance = BundleHelper.parseFloat(stdLineRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.StdLineType_c_nf_per_km)));
				}
			}
			// --- Convert to EA unit ---------------------------------------------------
			if (lineCapacitance>0) {
				// double b / 1E-6= 2 * 50 * Math.PI * matGridData.get(i).getdC()*1E-9;
				// Float linCapacitance = this.parseFloat(dataRowHashMap.get("b")) / (float)(1E6 * 2 * 50 * Math.PI * 1E-9);
				// => lineCapacitance = lineCapacitance / ((float)(100 * Math.PI * 1E-3));
			}
			
			
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
			lineName = this.getUniqueNetworkComponentID(lineName, lineIndex);
			newCompNM.renameNetworkComponent(newNetComp.getId(), lineName);
			newCompNM.renameGraphNode(newGraphNodeFrom.getId(), this.getLocalGraphNodeName(nodeFrom));
			newCompNM.renameGraphNode(newGraphNodeTo.getId(), this.getLocalGraphNodeName(nodeTo));
			
			
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
			
			// --- Call customization method --------------------------------------------
			try {
				this.addAdditionalLineProperties(lineRowHashMap, newNetComp);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// --- Merge into the new NetworkModel --------------------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, false);
			
		} // end for
	}
	/**
	 * Enables to add additional node properties in sub classes.
	 * Overwrite this method to add additional information to the specified new NetworkComponent.
	 *
	 * @param lineRowHashMap the line row hash map
	 * @param newNC the new NetworkComponent
	 */
	protected void addAdditionalLineProperties(HashMap<String, Object> lineRowHashMap, NetworkComponent newNC) { }
	
	
	/**
	 * Creates the switches.
	 */
	private void createSwitches() {
		
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.PANDA_Switch);
		Vector<Vector<Object>> lineDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.PANDA_Switch);
		if (nodeCsvController==null || lineDataVector==null) return;
		
		int ciIndex = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Switch_Index));
		int ciName = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Switch_Name));
		int ciNodeFrom = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Switch_Bus));
		int ciNodeTo = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Switch_Element));
		
		int ciIsClosed = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Switch_Closed));
		int ciZOhm = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Switch_Z_Ohm));
		
		for (int i = 0; i < lineDataVector.size(); i++) {
			
			// --- Get line data row ----------------------------------------------------
			Vector<Object> row = lineDataVector.get(i);
			Integer switchIndex = (Integer) row.get(ciIndex);
			String switchName  = row.get(ciName).toString();
			Integer nodeFrom = ((Long) row.get(ciNodeFrom)).intValue();
			Integer nodeTo = ((Long) row.get(ciNodeTo)).intValue();
			
			Boolean isClosed = (Boolean) row.get(ciIsClosed);
			Double rOhmKm = (Double) row.get(ciZOhm);

			// --- Get the row HahsMap --------------------------------------------------
			HashMap<String, Object> switchRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Switch, PandaPowerNamingMap.getColumnName(ColumnName.Switch_Index), switchIndex.toString());
			
			// --- Get node specific information ----------------------------------------
			HashMap<String, Object> nodeFromRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index), nodeFrom.toString());
			double nodeFromVoltageLevel = NumberHelper.round(BundleHelper.parseDouble(nodeFromRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_VoltageLevel))) * 1000, 0);
			String nodeFromName = nodeFromRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_Name)).toString();
			Integer transIndex = this.getTransformerIndex(nodeFrom);
			if (transIndex!=null) {
				HashMap<String, Object> trafoFromRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Trafo, PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Index), transIndex.toString());
				if (trafoFromRowHashMap!=null) {
					nodeFromName = trafoFromRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Name)).toString();
				}
			}
			
			HashMap<String, Object> nodeToRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index), nodeTo.toString());
			double nodeToVoltageLevel = NumberHelper.round(BundleHelper.parseDouble(nodeToRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_VoltageLevel))) * 1000, 0);

			boolean isTriPhaseNetwork = true;
			if (nodeFromVoltageLevel!=nodeToVoltageLevel) {
				System.err.println("[" + this.getClass().getName() + "] Different voltage levels were found for line "+ switchIndex + " ('" + switchName + "') between node '" + nodeFrom + "' and node '" + nodeTo + "'");
			} else {
				isTriPhaseNetwork = PandaPowerFileStore.isTriPhaseVoltageLevel(nodeFromVoltageLevel);
			}
			
			
			// --- Get resistance, reactance, capacitance and max I ---------------------
			Float lengthInMeter = 0.0f;
			Float maxCurrent = 100f;
			Float lineResistance = rOhmKm==0.0 ? 0.001f : rOhmKm.floatValue(); 	// --- Default value ---
			Float lineReactance = 0f;
			Float lineCapacitance = isTriPhaseNetwork ? 0 : 0.001f; 			// --- Default value ---
			
			// --- Prepare new NetworkComponent -----------------------------------------
			NetworkModel newCompNM = null;
			
			// --- Create new NetworkComponent by using the NetworkComponentFactory -----
			if (isTriPhaseNetwork==true) {
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Breaker");
			} else {
				newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvBreaker");
			}
			
			// --- Get NetworkComponent and GrahNode for renaming -----------------------
			NetworkComponent newNetComp = newCompNM.getNetworkComponents().values().iterator().next();
			Object[] graphNodes = newCompNM.getGraphElementsOfNetworkComponent(newNetComp, new GraphNode()).toArray();
			GraphNode newGraphNodeFrom = (GraphNode) graphNodes[0];
			GraphNode newGraphNodeTo = (GraphNode) graphNodes[1];

			// --- Rename NetworkComponent and GraphNode --------------------------------
			switchName = this.getUniqueNetworkComponentID(switchName, switchIndex);
			newCompNM.renameNetworkComponent(newNetComp.getId(), switchName);
			newCompNM.renameGraphNode(newGraphNodeFrom.getId(), this.getLocalGraphNodeName(nodeFrom));
			newCompNM.renameGraphNode(newGraphNodeTo.getId(), this.getLocalGraphNodeName(nodeTo));
			
			// --- Set the parameters for the component ---------------------------------
			CableWithBreakerProperties cwbProperties = new CableWithBreakerProperties();
			cwbProperties.setLength(new UnitValue(lengthInMeter, "m"));
			cwbProperties.setLinearResistance(new UnitValue(lineResistance, "Ω/km"));
			cwbProperties.setLinearReactance(new UnitValue(lineReactance, "Ω/km"));
			cwbProperties.setLinearCapacitance(new UnitValue(lineCapacitance, "nF/km"));
			cwbProperties.setMaxCurrent(new UnitValue(maxCurrent, "A"));
			
			// --- Set CircuitBreaker instance ------------------------------------------
			CircuitBreaker cBreaker = new CircuitBreaker();
			cBreaker.setBreakerID(switchName);
			cBreaker.setAtComponent(nodeFromName);
			cBreaker.setIsClosed(isClosed);
			cwbProperties.setBreakerBegin(cBreaker);

			
			// --- Add an empty time series chart object to match the requirements of the adapter ------
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());

			// --- Set the data model --------------
			Object[] dataModel = new Object[3];
			dataModel[0] = cwbProperties;
			if (isTriPhaseNetwork==true) {
				dataModel[1] = new TriPhaseCableState();
			} else {
				dataModel[1] = new UniPhaseCableState();
			}
			dataModel[2] = tsc;
			newNetComp.setDataModel(dataModel);
			
			// --- Call customization method --------------------------------------------
			try {
				this.addAdditionalSwitchProperties(switchRowHashMap, newNetComp);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// --- Merge into the new NetworkModel --------------------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, false);
			
		} // end for
	}
	/**
	 * Enables to add additional node properties in sub classes.
	 * Overwrite this method to add additional information to the specified new NetworkComponent.
	 *
	 * @param switchRowHashMap the line row hash map
	 * @param newNC the new NetworkComponent
	 */
	protected void addAdditionalSwitchProperties(HashMap<String, Object> switchRowHashMap, NetworkComponent newNC) { }
	
	
	/**
	 * Returns the local graph node name by using the already created GraphNodes in the local NetworkModel.
	 *
	 * @param busIndex the bus index (from PandaPower)
	 * @return the local graph node name
	 */
	private String getLocalGraphNodeName(Integer busIndex) {
		
		// --- First, try to directly get GraphNode -----------------
		GraphElement ge = this.getNetworkModel().getGraphElement(busIndex.toString());
		if (ge instanceof GraphNode) {
			return ge.getId();
		}

		// --- Try searching the Graph itself -----------------------
		List<GraphNode> nodeList = new ArrayList<>(this.getNetworkModel().getGraph().getVertices());
		for (int i = 0; i < nodeList.size(); i++) {
			String localNodeName = nodeList.get(i).getId();
			if (localNodeName.equals(busIndex)==true) {
				return localNodeName;
			} else if (this.isLocalGraphNode(busIndex.toString(), localNodeName)==true) {
				return localNodeName;
			}
		}
		
		// --- Last try: check for a transformer --------------------
		Integer transformerIndex = this.getTransformerIndex(busIndex);
		if (transformerIndex!=null) {
			HashMap<String, Object> transformerRow = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Trafo, PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Index), transformerIndex.toString());
			Integer nodeLV = (Integer) transformerRow.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_HV_Bus));
			Integer nodeHV = (Integer) transformerRow.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_LV_Bus));
			if (nodeLV!=null && nodeHV!=null) {
				if (busIndex.equals(nodeLV)==true) {
					return this.getLocalGraphNodeName(nodeHV);
				} else if (busIndex.equals(nodeHV)==true) {
					return this.getLocalGraphNodeName(nodeLV);
				}
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
	private void createNodes(File ppFile) {
		
		// --- Check if the directory file is part of the project directory -------------
		String sbPathName = ppFile.getAbsolutePath();
		String projectPathName = Application.getProjectFocused().getProjectFolderFullPath();
		if (sbPathName.startsWith(projectPathName)==true) {
			sbPathName = sbPathName.substring(projectPathName.length(), sbPathName.length());
		}
		
		// --- Get the base data to create the nodes ------------------------------------
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.PANDA_Bus);
		Vector<Vector<Object>> nodeDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.PANDA_Bus);
		if (nodeDataVector==null) return;
		
		String colNameIndex = PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index);
		String colNameName = PandaPowerNamingMap.getColumnName(ColumnName.Bus_Name);
		String colNameType = PandaPowerNamingMap.getColumnName(ColumnName.Bus_Type);
		String colVoltageLevel = PandaPowerNamingMap.getColumnName(ColumnName.Bus_VoltageLevel);
		String colNameGeoData = PandaPowerNamingMap.getColumnName(ColumnName.Bus_Geodata);
		String colNameCoords = PandaPowerNamingMap.getColumnName(ColumnName.Bus_Coordinates);
		
		int ciIndex = nodeCsvController.getDataModel().findColumn(colNameIndex);
		int ciName = nodeCsvController.getDataModel().findColumn(colNameName);
		int ciType = nodeCsvController.getDataModel().findColumn(colNameType);
		int ciGeoData = nodeCsvController.getDataModel().findColumn(colNameGeoData);
		int ciCoords = nodeCsvController.getDataModel().findColumn(colNameCoords);
		
		for (int i = 0; i < nodeDataVector.size(); i++) {
			
			// --- Get node data row ----------------------------------------------------
			Vector<Object> row = nodeDataVector.get(i);
			Integer busIndex  = ciIndex!=-1 ? (Integer) row.get(ciIndex) : null;
			String nodeName  = ciName!=-1 ? row.get(ciName).toString() : null;
			String type = ciType!=-1 ? row.get(ciType).toString() : null;
			String geoData = ciGeoData!=-1 ? row.get(ciGeoData).toString() : null;
			String coords = ciCoords!=-1 ? row.get(ciCoords).toString() : null;
			
			// --- Import this node type to the NetworkModel ----------------------------
			if (PandaPowerNamingMap.getPandaPowerNamingMap().isAddNodeTypeToNetworkModel(type)==false) continue;
			
			// --- Get the voltage Level of that node -----------------------------------
			HashMap<String, Object> busRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, colNameIndex, busIndex.toString());
			double voltageLevel = NumberHelper.round(BundleHelper.parseDouble(busRowHashMap.get(colVoltageLevel)) * 1000, 0) ;
			
			// --- Provide some user information ----------------------------------------
			Application.setStatusBarMessage(this.getClass().getSimpleName() + ": Import node '" + nodeName  + "' - (" + (i+1) +  "/" + nodeDataVector.size() + ")");

			// --- Get the corresponding data row of the coordinates --------------------
			Double coordX_Lati = null;
			Double coordY_Long = null;
			if (ciCoords!=-1 || ciGeoData!=-1) {
				if (geoData!=null && geoData.isBlank()==false) {
					System.err.println("[" + this.getClass().getSimpleName() + "] ToDo: PandaPower geo data nedds to be integrated!");
				}
				if (coords!=null && coords.isBlank()==false) {
					System.err.println("[" + this.getClass().getSimpleName() + "] ToDo: PandaPower 'coords' nedds to be integrated!");
				}

			} else {
				// --- Try getting info from table 'bus_geodata' ------------------------
				HashMap<String, Object> busGeodataRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_BusGeodata, PandaPowerNamingMap.getColumnName(ColumnName.BusGeoData_Index), busIndex.toString());
				if (busGeodataRowHashMap!=null) {
					coordX_Lati = BundleHelper.parseDouble(busGeodataRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.BusGeoData_x)).toString());
					coordY_Long = BundleHelper.parseDouble(busGeodataRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.BusGeoData_y)).toString());
				} else {
					if (this.isDoAutoLayout==false) {
						System.err.println("[" + this.getClass().getSimpleName() + "] No coordinates are provided for bus elements, try auto layout!");
						this.isDoAutoLayout = true;
					}
				}
				
			}
			// --- Prepare new NetworkComponent -----------------------------------------
			NetworkModel newCompNM = null;
			String newNetCompID = nodeName; 
			
			// --- Create new NetworkComponent by using the NetworkComponentFactory -----
			Object netCompDataModel = null;
			TreeMap<String, String> netCompStorageSettings = null;
			
			// --------------------------------------------------------------------------
			// --- Case separation for NetworkCmponents ---------------------------------
			// --------------------------------------------------------------------------
			boolean isCreatingTransformer = false;
			Integer transformerIndex = this.getTransformerIndex(busIndex);
			if (transformerIndex!=null) {
				// ----------------------------------------------------------------------
				// --- Create transformer ? ---------------------------------------------
				// ----------------------------------------------------------------------
				HashMap<String, Object> transformerRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Trafo, PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Index).toString(), transformerIndex.toString());
				String transfromerName = (String) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Name));
				Integer nodeLV = (Integer) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_LV_Bus));
				// --- Only create low voltage node of SimBench model ------------------- 
				if ((nodeLV.equals(busIndex) || this.isLocalGraphNode(nodeLV.toString(), busIndex.toString())) && this.getNetworkModel().getNetworkComponent(transfromerName)==null) {
					// --- Create transformer from factory ------------------------------
					if (PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Transformer");
					} else {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvTransformer");
					}
					newNetCompID = transfromerName;
					isCreatingTransformer = true;
					// --- Get data model and the storage settings for transformer ------
					netCompDataModel = this.getTransformerDataModel(transformerRowHashMap);
					netCompStorageSettings = this.getTransformerStorageSettings(transfromerName);
					
				} else {
					// --- Do not create this component again ---------------------------
					continue;
				}
				
			} else {
				// ----------------------------------------------------------------------
				// --- Prosumer or CableCabinet ? ---------------------------------------
				// ----------------------------------------------------------------------
				HashMap<String, Object> loadRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Load, PandaPowerNamingMap.getColumnName(ColumnName.Load_Bus), busIndex.toString());
				if (loadRowHashMap==null) {
					// --- Create CableCabinet ------------------------------------------
					if (PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "CableCabinet");
					} else {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvCableCabinet");
					}
					
				} else {
					// --- Create Prosumer model ---------------------------------------- 
					if (PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "Prosumer");
					} else {
						newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), "MvProsumer");
					}
					
					// ------------------------------------------------------------------
					// --- Create ScheduleList and storage settings and save them -------
					// ------------------------------------------------------------------
					netCompDataModel = this.getPersistenceService().loadScheduleList(ppFile, PandaPowerFileStore.PANDA_Bus, i, this.getScheduleTimeRange());
					// --- Create storage settings --------------------------------------
					netCompStorageSettings = new TreeMap<>();
					netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.ScheduleList.toString());
					switch (this.storageDestination) {
					case Base64:
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.NetworkElementBase64.toString());
						break;
						
					case File:
						// --- Get the file location for the ScheduleList ---------------
						String profile = (String) loadRowHashMap.get("profile");
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
						
					case PandaPowerService:
						// --- Set to storage settings --------------------------
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.Customized.toString());
						netCompStorageSettings.put(EomDataModelStorageHandler.EOM_SETTING_CUSTOMIZED_STORAGE_HANDLER, PandaPowerStorageHandler.class.getName()); 
						netCompStorageSettings.put(PandaPowerStorageHandler.PANDA_POWER_SETTING_PATH_NAME, sbPathName);
						netCompStorageSettings.put(PandaPowerStorageHandler.PANDA_POWER_SETTING_SOURCE, PandaPowerFileStore.PANDA_Bus);
						netCompStorageSettings.put(PandaPowerStorageHandler.PANDA_POWER_SETTING_ROW_INDEX, i + "");
						break;
					}
				}
			}
			
			// --------------------------------------------------------------------------
			// --- Get the actual NetworkComponent --------------------------------------
			// --------------------------------------------------------------------------
			NetworkComponent newComp = newCompNM.getNetworkComponents().values().iterator().next();
			String graphNodeName = newComp.getGraphElementIDs().iterator().next();
			GraphNode graphNode = (GraphNode) newCompNM.getGraphElement(graphNodeName);

			// --- Rename the elements --------------------------------------------------
			newNetCompID = this.getUniqueNetworkComponentID(newNetCompID, busIndex);
			newCompNM.renameNetworkComponent(newComp.getId(), newNetCompID);
			newCompNM.renameGraphNode(graphNode.getId(), busIndex.toString());
			
			// --- Define the GraphNode positions ---------------------------------------
			if (coordX_Lati!=null & coordY_Long!=null) {
				this.setGraphNodeCoordinates(graphNode, coordX_Lati, coordY_Long);
			}
			
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
			if (transformerIndex!=null) {
				// --- For transformer --------------------
				graphNode.setDataModel(this.getTransformerGraphNodeModel(transformerIndex));
				
			} else {
				// --- For regular nodes ------------------
				Object[] dataModel = new Object[3];
				dataModel[0] = nodeProps;
				if (PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
					dataModel[1] = new TriPhaseElectricalNodeState();
				} else {
					dataModel[1] = new UniPhaseElectricalNodeState();
				}
				dataModel[2] = tsc;
				graphNode.setDataModel(dataModel);
			}

			// --------------------------------------------------------------------------
			// --- Set the data model to the NetworkComponent ---------------------------
			newComp.setDataModel(netCompDataModel);
			newComp.setDataModelStorageSettings(netCompStorageSettings);
			
			// --- Call customization method --------------------------------------------
			try {
				this.addAdditionalNodeProperties(busRowHashMap, newComp);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// --- Merge into the new NetworkModel --------------------------------------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, false);
			
		} // end for
		
	}
	/**
	 * Enables to add additional node properties in sub classes.
	 * Overwrite this method to add additional information to the specified new NetworkComponent.
	 *
	 * @param busRowHashMap the bus row hash map
	 * @param newNC the new NetworkComponent
	 */
	protected void addAdditionalNodeProperties(HashMap<String, Object> busRowHashMap, NetworkComponent newNC) {	}
	
	
	/**
	 * Checks if the specified node ID represents a transformer and returns either the 
	 * ID of the transformer or <code>null</code>:.
	 *
	 * @param busIndex the bus index
	 * @return the transformer ID or <code>null</code>
	 */
	private Integer getTransformerIndex(Integer busIndex) {
		
		CsvDataController transformerCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.PANDA_Trafo);
		Vector<Vector<Object>> transformerDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.PANDA_Trafo);
		
		String colIndex = PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Index);
		String colNodeLV = PandaPowerNamingMap.getColumnName(ColumnName.Trafo_LV_Bus);
		String colNodeHV = PandaPowerNamingMap.getColumnName(ColumnName.Trafo_HV_Bus);
		
		int ciIndex = transformerCsvController.getDataModel().findColumn(colIndex);
		int ciNodeLV = transformerCsvController.getDataModel().findColumn(colNodeLV);
		int ciNodeHV = transformerCsvController.getDataModel().findColumn(colNodeHV);
		
		for (int i = 0; i < transformerDataVector.size(); i++) {
		
			Vector<Object> row = transformerDataVector.get(i);
			Integer indexTrafo = (Integer) row.get(ciIndex);
			Integer indexBusNodeHV = (Integer) row.get(ciNodeHV);
			Integer indexBusNodeLV = (Integer) row.get(ciNodeLV);
			
			if (indexBusNodeHV.equals(busIndex) || this.isLocalGraphNode(indexBusNodeHV.toString(), busIndex.toString())) {
				// --- High voltage node ------------------
				return indexTrafo;
				
			} else if (indexBusNodeLV.equals(busIndex) || this.isLocalGraphNode(indexBusNodeLV.toString(), busIndex.toString())) {
				// --- Low voltage node -------------------
				return indexTrafo;
				
			}
		}
		return null;
	}
	
	/**
	 * Returns the transformer data model.
	 *
	 * @param transformerRowHashMap the transformer row HashMap from the PP-Table
	 * @return the transformer data model
	 */
	private TechnicalSystem getTransformerDataModel(HashMap<String, Object> transformerRowHashMap) {
		
		// --- Load the base transformer model to the OptionModelController -------------
		OptionModelController omc = new OptionModelController();
		omc.loadTechnicalSystemFromBundle("de.enflexit.ea.electricity.transformer", "EOM-INF/TransformerBaseModel.xml", null, true);
		
		// --- Get SystemVariableDefinition of 'StaticParameters' -----------------------
		SystemVariableDefinition sysVarDef = omc.getSystemVariableDefinition("StaticParameters");
		if (sysVarDef!=null && sysVarDef instanceof SystemVariableDefinitionStaticModel) {
			
			Double sn_mva = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_sn_mva)); 	// e.g. 5 MVA
			
			Double vn_hv_kv = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_vn_hv_kv)); // e.g. 20 kV
			Double vn_lv_kv = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_vn_lv_kv)); // e.g. 0.4 kV
			Boolean vn_hv_kvTriPhase = PandaPowerFileStore.isTriPhaseVoltageLevel(vn_hv_kv * 1000);
			Boolean vn_lv_kvTriPhase = PandaPowerFileStore.isTriPhaseVoltageLevel(vn_lv_kv * 1000);
			
			Double vk_percent = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_vk_percent));
			Double vkr_percent = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_vkr_percent));
			Double pfe_kw = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_pfe_kw));
			Double i0_percent = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_i0_percent));
			Double shift_degree = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_shift_degree));
			
			String tapSite = (String) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_side));
			Integer tap_neutral = (Integer) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_neutral));
			Integer tap_min = (Integer) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_min));
			Integer tap_max = (Integer) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_max));
			Integer tap_pos = BundleHelper.parseInteger(transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_pos)));
			
			Double tap_step_percent = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_step_percent));
			Double tap_step_degree = (Double) transformerRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_tap_step_degree));

			
			// --- Get the TransformerDataModel for configuration -----------------------
			SystemVariableDefinitionStaticModel sysVarDefStaticModel = (SystemVariableDefinitionStaticModel) sysVarDef;
			TransformerDataModel tdm = (TransformerDataModel) omc.getStaticModelInstance(sysVarDefStaticModel);
			
			// --- Base configuration ---------------------------------------------------
			tdm.setRatedPower_sR(sn_mva);
			tdm.setUpperVoltage_vmHV(vn_hv_kv);
			tdm.setUpperVoltage_ThriPhase(vn_hv_kvTriPhase);
			tdm.setLowerVoltage_vmLV(vn_lv_kv);
			tdm.setLowerVoltage_ThriPhase(vn_lv_kvTriPhase);
			
			tdm.setSlackNodeVoltageLevel(vn_lv_kv * 1000);
			
			// --- From PandaPower-Site:  vkr_percent = P_cu / S_trafo * 100 ------------
			double pCu = vkr_percent / 100 * sn_mva;
			
			tdm.setPhaseShift_va0(shift_degree);
			tdm.setShortCircuitImpedance_vmImp(vk_percent);
			tdm.setCopperLosses_pCu(pCu); 
			tdm.setIronLosses_pFe(pfe_kw);
			tdm.setIdleImpedance_iNoLoad(i0_percent);
			
			// --- Tap settings ---------------------------------------------------------
			if (tapSite==null) {
				tdm.setTapable(false);
				tdm.setTapSide(TapSide.HighVoltageSide);
				tdm.setTapMinimum(0);
				tdm.setTapMaximum(0);
				tdm.setTapNeutral(0);
				tdm.setVoltageDeltaPerTap_dVm(0);
				tdm.setPhaseShiftPerTap_dVa(0);
				
			} else {
				
				tdm.setTapable(true);
				switch (tapSite) {
				case "hv":
					tdm.setTapSide(TapSide.HighVoltageSide);
					break;
				case "lv":
					tdm.setTapSide(TapSide.LowVoltageSide);
					break;
				}
				
				tdm.setTapMinimum(tap_min);
				tdm.setTapMaximum(tap_max);
				tdm.setTapNeutral(tap_neutral);
				
				tdm.setVoltageDeltaPerTap_dVm(tap_step_percent);
				tdm.setPhaseShiftPerTap_dVa(tap_step_degree);
				
			}
			
			// --- Save the model settings to TechnicalSystem ---------------------------
			omc.setStaticModelInstance(sysVarDefStaticModel, tdm);
			
			// --- Execute TechnicalSystemChecker for IO-List adjustments ---------------
			AbstractTechnicalSystemChecker transformerChecker = omc.getIndividualTechnicalSystemChecker();
			if (transformerChecker!=null) {
				transformerChecker.setOptionModelController(omc);
				AbstractStaticModel asm = omc.getAbstractStaticModel(sysVarDefStaticModel);
				transformerChecker.afterStaticModelUpdate(sysVarDefStaticModel, asm);
			}
			
			// --- Further model adjustments --------------------------------------------
			if (tapSite!=null) {
				// --- Set initial tap position -----------------------------------------				
				if (omc.getEvaluationStateList().size()>0 && omc.getEvaluationStateList().get(0) instanceof TechnicalSystemState) {
					TechnicalSystemState tss = (TechnicalSystemState) omc.getEvaluationStateList().get(0);
					FixedVariable fv = TechnicalSystemStateHelper.getFixedVariable(tss.getIOlist(), TransformerSystemVariable.tapPos.name());
					if (fv!=null && fv instanceof FixedInteger) {
						FixedInteger fvTapPos = (FixedInteger) fv;
						fvTapPos.setValue(tap_pos);
					}
				}
			}
			
		}
		return omc.getTechnicalSystem();
	}
	
	/**
	 * Returns the transformer storage settings.
	 *
	 * @param transformerName the transformer name
	 * @return the transformer storage settings
	 */
	private TreeMap<String, String> getTransformerStorageSettings(String transformerName) {
		
		String emvPath = Application.getProjectFocused().getEnvironmentController().getEnvFolderPath();
		String setupName = Application.getProjectFocused().getSimulationSetupCurrent();
		String fileName = "EomModel_" + transformerName.replace(" ", "_") + ".xml";
		String projectPath = Application.getProjectFocused().getProjectFolderFullPath();
		
		Path eomFilePath = new File(emvPath).toPath().resolve(setupName).resolve(fileName);
		eomFilePath = new File(projectPath).toPath().relativize(eomFilePath);
		
		String relativePath = "/" + eomFilePath.toString().replace(File.separator, "/");
		
		TreeMap<String, String> tSettings = new TreeMap<>();
		tSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.TechnicalSystem.toString());
		tSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.File.toString());
		tSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_FILE_LOCATION, relativePath);
		return tSettings; 
	}
	/**
	 * Returns the transformer graph node model.
	 * @return the transformer graph node model
	 */
	private TreeMap<String, Object> getTransformerGraphNodeModel(Integer transformerIndex) {
		
		TreeMap<String, Object> graphNodeDataModel = new TreeMap<>();
		
		HashMap<String, Object> transformerRow = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Trafo, PandaPowerNamingMap.getColumnName(ColumnName.Trafo_Index), transformerIndex.toString());
		Double lvVoltageLevel = NumberHelper.round(BundleHelper.parseDouble(transformerRow.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_vn_lv_kv))) * 1000, 0);
		Double hvVoltageLevel = NumberHelper.round(BundleHelper.parseDouble(transformerRow.get(PandaPowerNamingMap.getColumnName(ColumnName.Trafo_vn_hv_kv))) * 1000, 0);
		
		Object[] lvDataModel = this.createTransformerSiteGraphNodeModel(lvVoltageLevel);
		Object[] hvDataModel = this.createTransformerSiteGraphNodeModel(hvVoltageLevel);
		
		graphNodeDataModel.put(this.guessDomain(lvVoltageLevel), lvDataModel);
		graphNodeDataModel.put(this.guessDomain(hvVoltageLevel), hvDataModel);
		
		return graphNodeDataModel;
	}
	/**
	 * Creates a transformer site (LV/HV) model.
	 *
	 * @param voltageLevel the voltage level
	 * @return the object[]
	 */
	private Object[] createTransformerSiteGraphNodeModel(Double voltageLevel) {
		
		Object[] siteModel = new Object[3];
		
		// --- TransformerNodeProperties --------------------
		UnitValue uvVoltageLevel = new UnitValue();
		uvVoltageLevel.setUnit("V");
		uvVoltageLevel.setValue(voltageLevel.floatValue());
		
		TransformerNodeProperties tnp = new TransformerNodeProperties();
		tnp.setRatedVoltage(uvVoltageLevel);
		siteModel[0] = tnp;
		
		// --- ElectricalNodeState ------------------------
		if (PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
			// --- TriPhaseElectricalNodeState ------------
			TriPhaseElectricalNodeState tpNodeState = new TriPhaseElectricalNodeState();
			tpNodeState.setL1(new UniPhaseElectricalNodeState());
			tpNodeState.setL2(new UniPhaseElectricalNodeState());
			tpNodeState.setL3(new UniPhaseElectricalNodeState());
			siteModel[1] = tpNodeState;
		} else {
			// --- UniPhaseElectricalNodeState ------------
			UniPhaseElectricalNodeState upNodeState = new UniPhaseElectricalNodeState();
			siteModel[1] = upNodeState; 
		}
		
		// --- Time Series chart --------------------------
		siteModel[2] = new TimeSeriesChart();
		
		return siteModel;
	}
	
	/**
	 * Checks if a target NetworkComponent ID is already used. If, the ID will be combined with the index number.
	 *
	 * @param targetID the target ID
	 * @param indexNo the index no
	 * @return the unique network component ID
	 */
	private String getUniqueNetworkComponentID(String targetID, int indexNo) {
		
		// --- Check if ID is already used ----------------
		NetworkComponent netComp = this.getNetworkModel().getNetworkComponent(targetID);
		if (netComp!=null) {
			return targetID + "_" + indexNo;
		}
		return targetID;
	}
	
	/**
	 * Will guess (try to evaluate) the domain for the specified voltage level.
	 *
	 * @param voltageLevel the voltage level
	 * @return the string
	 */
	private String guessDomain(Double voltageLevel) {
		
		String domainFound = null;
		boolean isTriPhaseVoltageLevel = PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel);
		
		GeneralGraphSettings4MAS ggs4MAS = this.getNetworkModel().getGeneralGraphSettings4MAS();
		
		List<String> domainListElectricityPhase = new ArrayList<>();
		List<String> domainList = new ArrayList<>(ggs4MAS.getDomainSettings().keySet());
		for (String domain : domainList) {
			
			DomainSettings ds = ggs4MAS.getDomainSettings().get(domain);
			boolean isTriPhaseDomain = ds.getAdapterClass()!=null && ds.getAdapterClass().equals(TriPhaseElectricalNodeAdapter.class.getName());
			boolean isUniPhaseDomain = ds.getAdapterClass()!=null && ds.getAdapterClass().equals(UniPhaseElectricalNodeAdapter.class.getName());
			
			if (isTriPhaseVoltageLevel==true && isTriPhaseDomain==true) {
				domainListElectricityPhase.add(domain);
			} else if (isTriPhaseVoltageLevel==false && isUniPhaseDomain) {
				domainListElectricityPhase.add(domain);
			}
		}
		
		// --- Check the electricity domains found ------------------
		if (domainListElectricityPhase.size()==0) {
			// --- Found nothing ------------------------------------
			System.err.println("[" + this.getClass().getSimpleName() + "] Could not find Domain for voltage level " + voltageLevel + " V");
		} else if (domainListElectricityPhase.size()==1) {
			// --- Found exact one domain ---------------------------
			domainFound = domainListElectricityPhase.get(0);
		} else {
			// --- Found several Domains ----------------------------
			String searchPhrase = "hv";  
			if (voltageLevel > 400 && voltageLevel < 50000) {
				searchPhrase = "mv";
			}
			// --- Try to find domain by search phrase --------------
			for (String domain : domainListElectricityPhase) {
				if (domain.toLowerCase().contains(searchPhrase)==true) {
					domainFound = domain;
					break;
				}
			}
		}
		return domainFound;
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
		Point2D coordDefaultLayout = new Point2D.Double(latNorthSouth, longEastWest);

		// --- Create WGS coordinate ---------------------------------
		WGS84LatLngCoordinate coordWGS84 = new WGS84LatLngCoordinate(latNorthSouth, longEastWest);
		
		// --- Calculate to UTM coordinate --------------------------
		UTMCoordinate coordUTM = null;
		try {
			coordUTM = coordWGS84.getUTMCoordinate(this.getMapSettings().getUTMLongitudeZone(), this.getMapSettings().getUTMLatitudeZone());
		} catch (Exception ex) {
		}
		
		// --- Set default layout to Default ------------------------
		graphNode.setPosition(coordDefaultLayout);
		
		// --- Set positions to position tree map -------------------
		graphNode.getPositionTreeMap().put(this.getLayoutIdDefault(), coordDefaultLayout);
		if (coordWGS84!=null) graphNode.getPositionTreeMap().put(this.getLayoutIdGeoCoordinateWGS84(), coordWGS84);
		if (coordUTM!=null)   graphNode.getPositionTreeMap().put(this.getLayoutIdGeoCoordinateUTM(), coordUTM);
		
	}
	
	// --------------------------------------------------------------------------------------------
	// --- From here: some methods for creating system schedules can be found ---------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the PandaPowerScheduelListPersistenceService
	 * @return the PandaPowerScheduelListPersistenceService
	 */
	public PandaPowerScheduelListPersistenceService getPersistenceService() {
		if (persistenceService==null) {
			persistenceService = new PandaPowerScheduelListPersistenceService();
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
		TimeRange timeRange = new PandaPowerFileStoreReader().getTimeRangeOfData();
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
	private HashMap<String, Object> getDataRowHashMap(String csvFileName, String keyColumnName, String keyValue) {
		
		HashMap<String, Object> dataRowHashMap = null;
		
		CsvDataController csvController = this.getCsvDataControllerOfCsvFile(csvFileName);
		if (csvController!=null) {
			
			// --- Find the right row -------------------------------
			int idColumnIndex = csvController.getDataModel().findColumn(keyColumnName);
			if (idColumnIndex!=-1) {
				// --- Found key column -----------------------------
				int dataRowIndex = -1;
				for (int rowIndex = 0; rowIndex < csvController.getDataModel().getRowCount(); rowIndex++) {
					String currKeyValue = csvController.getDataModel().getValueAt(rowIndex, idColumnIndex).toString();
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
						dataRowHashMap.put(colName, csvController.getDataModel().getValueAt(dataRowIndex, i));
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
	private Vector<Vector<Object>> getDataVectorOfCsvFile(String csvFileName) {
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(csvFileName);
		if (nodeCsvController!=null) {
			return new Vector<Vector<Object>>((Collection<? extends Vector<Object>>) nodeCsvController.getDataModel().getDataVector());
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

	// -----------------------------------------------------------------------
	// --- From here a auto layout methods -----------------------------------
	// -----------------------------------------------------------------------	
	
	/**
	 * Do auto layout.
	 */
	private void doAutoLayout() {
		
		boolean isPlaceRandom = true;
		
		// --- Get the Graph -------------------------------------------------
		Graph<GraphNode, GraphEdge> graph = this.getNetworkModel().getGraph();
		
		int clusterIndex = 0;
		TreeMap<String, List<GraphNode>> clusterTreeMap = new TreeMap<>();
		List<GraphNode> nodesToVisit  = new ArrayList<>(graph.getVertices());
		List<GraphNode> nodesVisited = new ArrayList<>();
		
		if (isPlaceRandom==true) {
			// --- Set random position ----------------------------------------  
			for (GraphNode node : nodesToVisit) {
				double randomX = NumberHelper.getRandomFloat(-90, 90);
				double randomY = NumberHelper.getRandomFloat(-90, 90);
				this.setGraphNodeCoordinates(node, randomX, randomY);
			}
			return;
		}
		
		
		while (nodesToVisit.size()>0) {
			
			GraphNode node = this.setNodeVisited(nodesToVisit.get(0), nodesToVisit, nodesVisited);
			
			List<GraphNode> nodeCluster = new ArrayList<>(this.getNeighborHashSet(graph, new HashSet<>(), node));
			for (GraphNode nodeNeighbor : nodeCluster) {
				this.setNodeVisited(nodeNeighbor, nodesToVisit, nodesVisited);
			}
			
			// --- Save in separate cluster -----------------------------------
			String clusterName = "Cluster_" + clusterIndex;
			clusterTreeMap.put(clusterName, nodeCluster);
			clusterIndex++;
		}
		
		System.out.println("Found " + (clusterIndex + 1) + " Cluster");
		
	}
	
	/**
	 * Sets the node visited.
	 *
	 * @param node the node
	 * @param nodesToVisit the nodes to visit
	 * @param nodesVisited the nodes visited
	 * @return the graph node
	 */
	private GraphNode setNodeVisited(GraphNode node, List<GraphNode> nodesToVisit, List<GraphNode> nodesVisited) {
		if (node!=null) {
			nodesToVisit.remove(node);
			nodesVisited.add(node);
		}
		return node;
	}

	private HashSet<GraphNode> getNeighborHashSet(Graph<GraphNode, GraphEdge> graph, HashSet<GraphNode> neighborHashSet, GraphNode sourceNode) {
		
		neighborHashSet.add(sourceNode);
		
		List<GraphNode> neighborList = new ArrayList<>(graph.getNeighbors(sourceNode)) ;
		for (GraphNode neighbor : neighborList) {
			if (neighborHashSet.contains(neighbor)==false) {
				neighborHashSet.addAll(this.getNeighborHashSet(graph, neighborHashSet, neighbor));
			}
		}
		return neighborHashSet;
	}
	
}
