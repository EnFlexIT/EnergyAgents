package hygrid.csvFileImport.electricalNetwork;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.helper.GraphNodePairs;
import org.awb.env.networkModel.helper.NetworkComponentFactory;
import org.awb.env.networkModel.maps.MapSettings;
import org.awb.env.networkModel.maps.MapSettings.MapScale;
import org.awb.env.networkModel.persistence.NetworkModelImportService;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesChartSettings;
import de.enflexit.common.csv.CsvDataController;
import hygrid.csvFileImport.CSV_FileImporter;
import hygrid.csvFileImport.GeoCoordinatesMapper;
import hygrid.globalDataModel.ontology.CableProperties;
import hygrid.globalDataModel.ontology.ElectricalNodeProperties;
import hygrid.globalDataModel.ontology.UniPhaseCableState;
import hygrid.globalDataModel.ontology.UniPhaseElectricalNodeState;
import hygrid.globalDataModel.ontology.UnitValue;

/**
 * The Class CsvFileImporterMediumVoltageGrid provides an import adapter for the 
 * SAG / BUW medium voltage grid descriptions.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class CSV_FileImporterMediumVoltageGrid extends CSV_FileImporter implements NetworkModelImportService{
	
	private static final float TOLERANCE_FOR_NODE_MATCHING = 0.5f;
	
	private static final String COMPONENT_ID_PREFIX = "e";
	private int nextComponentNumber = 0;
	
	// --- Expected file names --------------------------------------
	private static final String FILE_NAME_GIS_CABLES = "MV_CableSegments.csv";
	private static final String FILE_NAME_GIS_BUSBARS = "MV_BusBars.csv";
	private static final String FILE_NAME_CABLE_TYPES = "MV_CableTypes.csv";
	private static final String FILE_NAME_GIS_STATIONS = "MV_Stations.csv";
	
	// --- Column indices for the GIS data file ---------------------
	private static final int COLUMN_INDEX_COORDINATES_STRING = 0;
	private static final int COLUMN_INDEX_CABLE_DIN = 16;
	private static final int COLUMN_INDEX_CABLE_DIM = 15;
	private static final int COLUMN_INDEX_NOMINAL_VOLTAGE = 14;
	private static final int COLUMN_INDEX_LENGTH = 12;
	private static final int COLUMN_INDEX_MSLINK = 18;
	private static final int COLUMN_INDEX_ROUTE_NAME = 10;
	private static final int COLUMN_INDEX_STATION_FROM_NAME = 9;
	private static final int COLUMN_INDEX_STATION_FROM = 8;
	private static final int COLUMN_INDEX_STATION_TO_NAME = 7;
	private static final int COLUMN_INDEX_STATION_TO = 6;
	
	// --- Column indices for the cable types file ------------------
	private static final int COLUMN_INDEX_TYPE_DIN = 1;
	private static final int COLUMN_INDEX_TYPE_DIM = 2;
	private static final int COLUMN_INDEX_REACTANCE = 4;
	private static final int COLUMN_INDEX_CAPACITANCE = 5;
	private static final int COLUMN_INDEX_RESISTANCE = 6;
	private static final int COLUMN_INDEX_MAX_CURRENT = 7;
	
	// --- Column indices for stations ------------------------------
	private static final int COLUMN_INDEX_STATION_POINTS = 0;
	private static final int COLUMN_INDEX_STATION_OWNER = 10;
	private static final int COLUMN_INDEX_STATION_UNR = 9;
	private static final int COLUMN_INDEX_STATION_TYPE = 8;
	private static final int COLUMN_INDEX_STATION_NAME = 7;
	private static final int COLUMN_INDEX_STATION_MSLINK = 11;

	// --- Component types --------------------------------
	private static final String COMPONENT_TYPE_EDGE = "MediumVoltageCable";
	private static final String COMPONENT_TYPE_SLACK_NODE = "SlackNode";
	private static final String COMPONENT_TYPE_LOAD_NODE = "LoadNode";
	private static final String COMPONENT_TYPE_NON_LOAD_NODE = "NonLoadNode";
	
	// --- Geo layout name --------------------------------
	private static final String LAYOUT_NAME_GEOGRAPHICAL_UTM = "Geo-Coordinates UTM 32U";
	
	// --- Slack node ID ----------------------------------
	private static final String SLACK_NODE_STATION_ID = "185";	//TODO determine from imported data
	
	// All coordinates will be shifted by this value to avoid a 0:0 position   
	private final int COORDINATES_OFFSET = 25;
	
	// Upper limits for x and y values (the actual target range may differ in one direction, as it takes the ratio of the original data range into account) 
	private final int MAX_X_VALUE = 1200-COORDINATES_OFFSET;
	private final int MAX_Y_VALUE = 1600-COORDINATES_OFFSET;
	
	// Required for proper mapping of geocoordinates to visualization coordinates
	private double geoCoordinateMinX = Double.MAX_VALUE;
	private double geoCoordinateMaxX = Double.MIN_VALUE;
	private double geoCoordinateMinY = Double.MAX_VALUE;
	private double geoCoordinateMaxY = Double.MIN_VALUE;
	
	private GeoCoordinatesMapper coordinatesMapper;
	
	private List<FileFilter> fileFilters;
	
	/* (non-Javadoc)
	 * @see hygrid.electricalNetwork.csvImport.CsvFileImporter#importGraphFromFile(java.io.File)
	 */
	@Override
	public NetworkModel importNetworkModelFromFile(File graphFile) {
		
		// --- Reset component counter ------------------------------
		this.nextComponentNumber = 0;
		
		// --- Read the specified file ------------------------------
		if (this.readCsvFiles(graphFile) == false)
			this.showError();
		// --- Show table preview if this.isDebug() returns true ----
		this.showImportPreview();
		
		MapSettings mapSettings = new MapSettings();
		mapSettings.setUTMLongitudeZone(32);
		mapSettings.setUTMLatitudeZone("U");
		mapSettings.setMapScale(MapScale.m);
		mapSettings.setShowMapTiles(false);
		this.getNetworkModel().getMapSettingsTreeMap().put(this.getUtmLayoutID(), mapSettings);
		
		HashMap<String, HashMap<String, CableType>> cableTypes = this.importCableTypesFromFile();

		Vector<CableSection> cableSections = this.importCableSectionsFromFile();
		this.findJunctionPoints(cableSections);
		
		Vector<BusBar> busBars = this.importBusBarsFromFile();
		this.attachCablesToBusBars(cableSections, busBars);
		
		Vector<Station> stations = this.importStationsFromFile();
		this.mapBusBarsToStations(busBars, stations);
		
		this.createNodeComponents(stations);
		this.connectCableSectionsBetweenStations(cableSections);
		this.createEdgeComponents(cableSections, cableTypes);
		
		this.getNetworkModel().setLayoutIdAndExchangeLayoutSettings(this.getUtmLayoutID());
		return this.getNetworkModel();
	}
	
	
	/**
	 * Imports all cable type characteristics from a csv file.
	 * @return the cable type characteristics, structured by dim and din
	 */
	private HashMap<String, HashMap<String, CableType>> importCableTypesFromFile() {
		HashMap<String, HashMap<String, CableType>> cableTypes = new HashMap<>();
		CsvDataController dc = this.getCsvDataController().get(FILE_NAME_CABLE_TYPES);
		
		for (int i=0; i<dc.getDataModel().getRowCount(); i++) {
			@SuppressWarnings("unchecked")
			Vector<String> dataRow = (Vector<String>) dc.getDataModel().getDataVector().get(i);
			CableType cableType = this.importCableType(dataRow);
			HashMap<String, CableType> cableTypesForDim = cableTypes.get(cableType.getDim());
			if (cableTypesForDim==null) {
				cableTypesForDim = new HashMap<>();
				cableTypes.put(cableType.getDim(), cableTypesForDim);
			}
			
			cableTypesForDim.put(cableType.getDin(), cableType);
		}
		
		return cableTypes;
	}
	
	/**
	 * Imports the characteristics of a single cable type.
	 * @param dataRow the csv data row 
	 * @return the cable type characteristics
	 */
	private CableType importCableType(Vector<String> dataRow) {
		CableType cableType = new CableType();
		cableType.setDim(dataRow.get(COLUMN_INDEX_TYPE_DIM));
		cableType.setDin(dataRow.get(COLUMN_INDEX_TYPE_DIN));
		float reactanceValue = Float.parseFloat(dataRow.get(COLUMN_INDEX_REACTANCE));
		cableType.setReactance(reactanceValue);
		float capacitanceValue = Float.parseFloat(dataRow.get(COLUMN_INDEX_CAPACITANCE));
		cableType.setCapacitance(capacitanceValue);
		float resistanceValue = Float.parseFloat(dataRow.get(COLUMN_INDEX_RESISTANCE));
		cableType.setResistance(resistanceValue);
		float maxCurrentValue = Float.parseFloat(dataRow.get(COLUMN_INDEX_MAX_CURRENT));
		cableType.setMaxCurrent(maxCurrentValue);
		return cableType;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.electricalNetwork.csvImport.CsvFileImporter#getListOfValidFileNames()
	 */
	@Override
	protected Vector<String> getListOfRequiredFileNames() {
		Vector<String> requiredFileNames = new Vector<>();
		requiredFileNames.add(FILE_NAME_GIS_CABLES);
		requiredFileNames.add(FILE_NAME_GIS_BUSBARS);
		requiredFileNames.add(FILE_NAME_CABLE_TYPES);
		requiredFileNames.add(FILE_NAME_GIS_STATIONS);
		return requiredFileNames;
	}
	
	/**
	 * Gets the default layout ID.
	 * @return the default layout ID
	 */
	private String getDefaultLayoutID() {
		return this.getNetworkModel().getGeneralGraphSettings4MAS().getLayoutIdByLayoutName(GeneralGraphSettings4MAS.DEFAULT_LAYOUT_SETTINGS_NAME);
	}
	/**
	 * Gets the utm layout ID.
	 * @return the utm layout ID
	 */
	private String getUtmLayoutID() {
		return this.getNetworkModel().getGeneralGraphSettings4MAS().getLayoutIdByLayoutName(LAYOUT_NAME_GEOGRAPHICAL_UTM);
	}
	
	// ----------------------------------------------------
	// --- Methods for handling nodes and busbars ---------
	// ----------------------------------------------------
	
	/**
	 * Import bus bar data from the CSV file.
	 * @return the vector
	 */
	private Vector<BusBar> importBusBarsFromFile(){
		Vector<BusBar> busBars = new Vector<>();
		
		CsvDataController dc = this.getCsvDataController().get(FILE_NAME_GIS_BUSBARS);
		for (int i=0; i<dc.getDataModel().getRowCount(); i++) {
			@SuppressWarnings("unchecked")
			Vector<String> dataRow = (Vector<String>) dc.getDataModel().getDataVector().get(i);
			busBars.add(this.importBusBar(dataRow));
		}
		return busBars;
	}
	
	/**
	 * Import data for a single bus bar from a CSV data row.
	 * @param dataRow the data row
	 * @return the bus bar
	 */
	private BusBar importBusBar(Vector<String> dataRow) {
		BusBar busBar = new BusBar();
		busBar.setSupportPoints(this.parseWktCoordinates(dataRow.get(0)));
		busBar.setMsLink(Integer.parseInt(dataRow.get(1)));
		this.updateBoundariesForCoordinateMapping(busBar.getCenterPoint());
		return busBar;
	}
	
	/**
	 * Attach cables to bus bars.
	 * @param cables the cables
	 * @param busBars the bus bars
	 */
	private void attachCablesToBusBars(Vector<CableSection> cables, Vector<BusBar> busBars) {
		for (int i=0; i<cables.size(); i++) {
			CableSection cable = cables.get(i);
			
			for(int j=0; j<busBars.size(); j++) {
				BusBar busbar = busBars.get(j);
				if (busbar.isCableConnected(cable)) {
					busbar.getCables().add(cable);
				}
			}
		}
	}
	

	/**
	 * Creates the network components for the slack and load nodes based on the previously imported bus bars
	 * @param busBars the bus bars
	 * @return the hash map
	 */
	private HashMap<String, NetworkComponent> createNodeComponents(Vector<Station> stations){
		
		HashMap<String, NetworkComponent> components = new HashMap<>();
		for (int i=0; i<stations.size(); i++) {
			Station station = stations.get(i);
			
			// --- Ignore stations without bus bars/cables ----------
			if (station.getBusBars().size()>0) {
				
				NetworkModel networkModelForComponent;
				if (station.getID().equals(SLACK_NODE_STATION_ID)) {
					networkModelForComponent = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), COMPONENT_TYPE_SLACK_NODE);
				} else {
					networkModelForComponent = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), COMPONENT_TYPE_LOAD_NODE);
				}
				if (networkModelForComponent!=null) {
					NetworkComponent newComp = networkModelForComponent.getNetworkComponents().values().iterator().next();
					String nodeName = newComp.getGraphElementIDs().iterator().next();
					
					GraphNode graphNode = (GraphNode) networkModelForComponent.getGraphElement(nodeName);
					
					Point2D positionUTM = station.calculateCentroid();
					this.setGraphNodePositionsFromUtm(graphNode, positionUTM);
					
					Object[] dataModel = new Object[3];
					
					ElectricalNodeProperties nodeProperties = new ElectricalNodeProperties();
					nodeProperties.setDescription(station.getName());
					nodeProperties.setIsLoadNode(true);
					UniPhaseElectricalNodeState nodeState = new UniPhaseElectricalNodeState();
					TimeSeriesChart tsc = new TimeSeriesChart();
					tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
					dataModel[0] = nodeProperties;
					dataModel[1] = nodeState;
					dataModel[2] = tsc;
					graphNode.setDataModel(dataModel);
					
					for (int j=0; j<station.getBusBars().size(); j++) {
						BusBar busbar = station.getBusBars().get(j);
						for (int k=0; k<busbar.getCables().size(); k++) {
							busbar.getCables().get(k).getComponenstAtEnds().add(newComp);
						}
					}
					
					newComp.setId(COMPONENT_ID_PREFIX + station.getID());
					components.put(newComp.getId(), newComp);
					this.getNetworkModel().mergeNetworkModel(networkModelForComponent, null, true, false);
				}
			}
		}
		return components;
	}
	
	// ----------------------------------------------------
	// --- Methods for handling stations ------------------
	// ----------------------------------------------------
	
	
	/**
	 * Import all stations from the CSV file.
	 * @return the vector
	 */
	private Vector<Station> importStationsFromFile(){
		Vector<Station> stations = new Vector<>();
		CsvDataController dc = this.getCsvDataController().get(FILE_NAME_GIS_STATIONS);
		for (int i=0; i<dc.getDataModel().getRowCount(); i++) {
			@SuppressWarnings("unchecked")
			Vector<String> dataRow = (Vector<String>) dc.getDataModel().getDataVector().get(i);
			stations.add(this.importStation(dataRow));
		}
		return stations;
	}
	
	/**
	 * Import a single station from a data row.
	 * @param dataRow the data row
	 * @return the station
	 */
	private Station importStation(Vector<String> dataRow) {
		Station station = new Station();
		station.setPolygonPoints(this.parseWktCoordinates(dataRow.get(COLUMN_INDEX_STATION_POINTS)));
		station.setOwner(dataRow.get(COLUMN_INDEX_STATION_OWNER));
		station.setType(dataRow.get(COLUMN_INDEX_STATION_TYPE));
		station.setName(dataRow.get(COLUMN_INDEX_STATION_NAME));
		station.setID(dataRow.get(COLUMN_INDEX_STATION_UNR));
		station.setMsLink(Integer.parseInt(dataRow.get(COLUMN_INDEX_STATION_MSLINK)));
		return station;
	}
	
	private void mapBusBarsToStations(Vector<BusBar> busbars, Vector<Station> stations){
		
		if (this.isDebug()==true) {
			System.out.println("busbar\t\tstation\t\tdistance");
		}
		
		for (int busBarIndex=0; busBarIndex<busbars.size(); busBarIndex++) {
			
			BusBar busbar = busbars.get(busBarIndex);
			// --- Ignore bus bars with no attached cables ----------
			if (busbar.getCables().size()>0) {
				Station closestStation = null;
				double minDistance = Double.MAX_VALUE;
				for (int stationIndex=0; stationIndex<stations.size(); stationIndex++) {
					double distance = this.determineDistanceToStation(stations.get(stationIndex), busbars.get(busBarIndex).getCenterPoint());
					
					if (distance<minDistance) {
						minDistance = distance;
						closestStation = stations.get(stationIndex);
					}
				}
				if (this.isDebug()==true) {
					System.out.println(busbar.getMsLink() + "\t\t" + closestStation.getID() + "\t\t" + minDistance);
				}
				closestStation.getBusBars().addElement(busbar);
				
			}
			
		}
		
	}
	
	private double determineDistanceToStation (Station station, Point2D point) {
		double minDistance = Double.MAX_VALUE;
		// --- Determine the distance for all line segments ---------
		for (int i=0; i<station.getPolygonPoints().size()-1; i++) {
			Point2D segmentStart = station.getPolygonPoints().get(i);
			Point2D segmentEnd = station.getPolygonPoints().get(i+1);
			
			// --- Some station's polygons contain duplicate points, just ignore those
			if (segmentStart.distance(segmentEnd)>0) {
				double distance = this.distanceToLineSegment(segmentStart, segmentEnd, point);
				// --- Find the distance to the closest line segment ---- 
				if (distance<minDistance) {
					minDistance = distance;
				}
			}
			
		}
		return minDistance;
	}
	

	// ----------------------------------------------------
	// --- Methods for handling cable sections ------------
	// ----------------------------------------------------
	
	/**
	 * Import cable sections.
	 * @return the vector
	 */
	private Vector<CableSection> importCableSectionsFromFile(){
		Vector<CableSection> importedCableSections = new Vector<>();
		CsvDataController dc = this.getCsvDataController().get(FILE_NAME_GIS_CABLES);
		
		for (int i=0; i<dc.getDataModel().getRowCount(); i++) {
			@SuppressWarnings("unchecked")
			Vector<String> dataRow = (Vector<String>) dc.getDataModel().getDataVector().get(i);
			
			String stringFrom = dataRow.get(COLUMN_INDEX_STATION_FROM);
			String stringTo = dataRow.get(COLUMN_INDEX_STATION_TO);
			
			// --- Ignore cables without or with identical station entries ----
			if (stringFrom!=null && stringTo!=null && stringFrom.equals(stringTo)==false) {
				CableSection importedCableSection = this.importCableSection(dataRow);
				
				// --- Import 10kV cables only --------------------------------
				if (importedCableSection.getNominalVoltage()==10) {
					importedCableSections.add(importedCableSection);
				}
			} else {
//				System.out.println(this.getClass().getSimpleName() + ": Skipping cable section " + dataRow.get(COLUMN_INDEX_MSLINK));
			}
		}
		
		return importedCableSections;
	}
	
	/**
	 * Import a single line part from a row of GIS data.
	 * @param dataRow the data row
	 * @return the line part
	 */
	private CableSection importCableSection(Vector<String> dataRow) {
		CableSection cableSection = new CableSection();
		cableSection.setSupportPoints(this.parseWktCoordinates(dataRow.get(COLUMN_INDEX_COORDINATES_STRING)));
		cableSection.setDin(dataRow.get(COLUMN_INDEX_CABLE_DIN));
		cableSection.setDim(dataRow.get(COLUMN_INDEX_CABLE_DIM));
		cableSection.setNominalVoltage(Integer.parseInt(dataRow.get(COLUMN_INDEX_NOMINAL_VOLTAGE).substring(0, dataRow.get(COLUMN_INDEX_NOMINAL_VOLTAGE).indexOf("."))));
		cableSection.setLength(Float.parseFloat(dataRow.get(COLUMN_INDEX_LENGTH)));
		cableSection.setMsLink(Integer.parseInt(dataRow.get(COLUMN_INDEX_MSLINK)));
		cableSection.setRouteName(dataRow.get(COLUMN_INDEX_ROUTE_NAME));
		cableSection.setStationFromName(dataRow.get(COLUMN_INDEX_STATION_FROM_NAME));
		cableSection.setStationFrom(dataRow.get(COLUMN_INDEX_STATION_FROM));
		cableSection.setStationToName(dataRow.get(COLUMN_INDEX_STATION_TO_NAME));
		cableSection.setStationTo(dataRow.get(COLUMN_INDEX_STATION_TO));
		
		return cableSection;
	}
	
	/**
	 * Finds branch points between cable sections. If a cable section's end point equals 
	 * a non-end support point of another cable section, this is considered a branch
	 * point. The other cable section is split at this point, and a junction is added
	 * to connect the now three cable sections.
	 * @param cableSections The list of cable sections
	 */
	private void findJunctionPoints (Vector<CableSection> cableSections) {
		for (int i=0; i<cableSections.size(); i++) {
			CableSection section = cableSections.get(i);
			for (int j=0; j<cableSections.size(); j++) {
				if (i!=j) {
					CableSection anotherSection = cableSections.get(j);
					for (int k=1; k<anotherSection.getSupportPoints().size()-1; k++) {
						// --- Skip first and last point ------------
						Point2D supportPoint = anotherSection.getSupportPoints().get(k);
						if (section.getStartPoint().equals(supportPoint) || section.getEndPoint().equals(supportPoint)) {
							
							// --- Junktion point found ---------------
							Vector<CableSection> splitParts = this.splitCableSectionAtSupportPoint(anotherSection, supportPoint);
							
							// --- Replace the original section with the two parts
							cableSections.remove(j);
							cableSections.addAll(j, splitParts);
							j++;
							
						}
					}
				}
			}
		}
	}
	
	/**
	 * Split cable section at support point.
	 * @param cableSection the cable section
	 * @param splitPoint the split point
	 * @return the vector
	 */
	private Vector<CableSection> splitCableSectionAtSupportPoint(CableSection cableSection, Point2D splitPoint){
		Vector<CableSection> newCableSections = new Vector<>();
		for (int i=0; i<cableSection.getSupportPoints().size(); i++) {
			if (this.pointEqualsWithTolerance(cableSection.getSupportPoints().get(i), splitPoint)) {
				// --- Create new support point lists for the parts - the split point is included in both parts
				Vector<Point2D> firstPartPoints = new Vector<>(cableSection.getSupportPoints().subList(0, i+1));
				Vector<Point2D> secondPartPoints = new Vector<>(cableSection.getSupportPoints().subList(i, cableSection.getSupportPoints().size()));
				
				CableSection firstPart = cableSection.clone();
				firstPart.setSupportPoints(firstPartPoints);
				firstPart.setLength(this.calculateLength(firstPartPoints));
				firstPart.setComponenstAtEnds(new Vector<>());
				newCableSections.add(firstPart);
				
				CableSection secondPart = cableSection.clone();
				secondPart.setSupportPoints(secondPartPoints);
				secondPart.setLength(this.calculateLength(secondPartPoints));
				secondPart.setComponenstAtEnds(new Vector<>());
				newCableSections.add(secondPart);
			}
			
		}
		return newCableSections;
	}
	
	/**
	 * Try to connect cable sections with equal start and end station  
	 * @param cableSections The cable sections
	 */
	private void connectCableSectionsBetweenStations(Vector<CableSection> cableSections){
	
		// --- Group all cable sections by the stations at their ends ------------------
		HashMap<String, HashMap<String, Vector<CableSection>>> cableSectionsGroupedByStations = new HashMap<>();
		for (int i=0; i<cableSections.size(); i++) {
			String stationFrom = cableSections.get(i).getStationFrom();
			if (stationFrom!=null && stationFrom.equals("null")==false && stationFrom.equals("nicht vorhanden")==false) {
				HashMap<String, Vector<CableSection>> cableSectionsFromStation = cableSectionsGroupedByStations.get(cableSections.get(i).getStationFrom());
				if (cableSectionsFromStation==null) {
					cableSectionsFromStation = new HashMap<>();
					cableSectionsGroupedByStations.put(stationFrom, cableSectionsFromStation);
				}
				
				String stationTo = cableSections.get(i).getStationTo();
			
				if (stationTo!=null && stationTo.equals("null")==false && stationTo.equals("nicht vorhanden")==false) {
					Vector<CableSection> cableSectionsToStation = cableSectionsFromStation.get(cableSections.get(i).getStationTo());
					if (cableSectionsToStation==null) {
						cableSectionsToStation = new Vector<>();
						cableSectionsFromStation.put(stationTo, cableSectionsToStation);
					}
					cableSectionsToStation.add(cableSections.get(i));
				}
			}
		}
		
		// --- Merge the parts that share the same station entries ------------
		for (HashMap<String, Vector<CableSection>> partsFromStation : cableSectionsGroupedByStations.values()) {
			for (Vector<CableSection> partsToStation : partsFromStation.values()) {
				this.connectCableSections(partsToStation);
			}
		}
		
	}

	/**
	 * Merge the line parts that share the same start or end points, preserving the order of support points
	 * @param cableSections the line parts to be merged
	 * @return the merged line 
	 */
	private void connectCableSections(Vector<CableSection> cableSections) {
		
//		System.out.println("[" + this.getClass().getSimpleName() + "] Merging " + cableSections.size() + " line parts between " + cableSections.get(0).getStationFrom() + " and " + cableSections.get(0).getStationTo());
		for (int i=0; i<cableSections.size(); i++) {
			
			// --- Find cable sections with open ends ---------------
			if (cableSections.get(i).getComponenstAtEnds().size()<2) {
				CableSection cs1 = cableSections.get(i);
				
				for (int j=0; j<cableSections.size(); j++) {
					if (j!=i && cableSections.get(j).getComponenstAtEnds().size()<2) {
						CableSection cs2 = cableSections.get(j);
						Point2D commonEndPoint = this.findCommonEndPoint(cs1, cs2);
						if (commonEndPoint!=null && cs1.isParallel(cs2)==false) {
							// --- Adjacent section found -----------
							if (cs1.hasComponentAtPoint(commonEndPoint)==false || cs2.hasComponentAtPoint(commonEndPoint)==false) {
								
								// --- Get the component at the common end point --------
								NetworkComponent connectorComponent = cs1.getComponentAtPoint(commonEndPoint);
								if (connectorComponent==null) {
									connectorComponent = cs2.getComponentAtPoint(commonEndPoint);
								}
								if (connectorComponent==null) {
									// --- No component there yet, create a new one -----
									NetworkModel networkModelForComponent = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), COMPONENT_TYPE_NON_LOAD_NODE);
									if (networkModelForComponent!=null) {
										connectorComponent = networkModelForComponent.getNetworkComponents().values().iterator().next();
										connectorComponent.setId(this.getNextComponentID());
										String nodeName = connectorComponent.getGraphElementIDs().iterator().next();
										GraphNode graphNode = (GraphNode) networkModelForComponent.getGraphElement(nodeName);
										this.setGraphNodePositionsFromUtm(graphNode, commonEndPoint);
										
										Object[] dataModel = new Object[3];
										ElectricalNodeProperties nodeProperties = new ElectricalNodeProperties();
										UniPhaseElectricalNodeState nodeState = new UniPhaseElectricalNodeState();
										TimeSeriesChart tsc = new TimeSeriesChart();
										tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
										
										dataModel[0] = nodeProperties;
										dataModel[1] = nodeState;
										dataModel[2] = tsc;
										graphNode.setDataModel(dataModel);
										
										this.getNetworkModel().mergeNetworkModel(networkModelForComponent, null, true, false);
									}
								}
								
								if (cs1.hasComponentAtPoint(commonEndPoint)==false) {
									cs1.getComponenstAtEnds().add(connectorComponent);
								}
								if (cs2.hasComponentAtPoint(commonEndPoint)==false) {
									cs2.getComponenstAtEnds().add(connectorComponent);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if two cable sections share a common end point
	 * @param aCableSection the a cable section
	 * @param anotherCableSection another cable section
	 * @return the common end point, null if there is none
	 */
	private Point2D findCommonEndPoint(CableSection aCableSection, CableSection anotherCableSection) {
		if (this.pointEqualsWithTolerance(aCableSection.getStartPoint(), anotherCableSection.getStartPoint())
				|| this.pointEqualsWithTolerance(aCableSection.getStartPoint(), anotherCableSection.getEndPoint())) {
			return aCableSection.getStartPoint();
		} else if (this.pointEqualsWithTolerance(aCableSection.getEndPoint(), anotherCableSection.getStartPoint())
				|| this.pointEqualsWithTolerance(aCableSection.getEndPoint(), anotherCableSection.getEndPoint())) {
			return aCableSection.getEndPoint();
		} else {
			return null;
		}
	}
	
	/**
	 * Creates the edge components for all cable sections.
	 * @param cableSections the cable sections
	 */
	private void createEdgeComponents(Vector<CableSection> cableSections, HashMap<String, HashMap<String, CableType>> cableTypes) {
		for (int i=0; i<cableSections.size(); i++) {
			CableSection cableSection = cableSections.get(i);
			// --- Create network component and model -----
			NetworkModel newCompNM = NetworkComponentFactory.getNetworkModel4NetworkComponent(this.getNetworkModel(), COMPONENT_TYPE_EDGE);
			NetworkComponent netComp = newCompNM.getNetworkComponents().values().iterator().next();
			
			// --- Set ID for component and edge ----------
			String oldID = netComp.getId();
			String newID = this.getNextComponentID();
			netComp.setId(newID);
			// --- Rename edge ----------------------------
			GraphEdge edge = (GraphEdge) newCompNM.getGraphElement(oldID);
			edge.setId(newID);
			newCompNM.getGraphElements().remove(oldID);
			newCompNM.getGraphElements().put(newID, edge);
			netComp.getGraphElementIDs().remove(oldID);
			netComp.getGraphElementIDs().add(newID);
			
			// --- Set positions of end points ----------------------
			Object[] graphNodes = newCompNM.getGraphElementsOfNetworkComponent(netComp, new GraphNode()).toArray();
			GraphNode newNodeFrom = (GraphNode) graphNodes[0];
			this.setGraphNodePositionsFromUtm(newNodeFrom, cableSection.getStartPoint());
			GraphNode newNodeTo = (GraphNode) graphNodes[1];
			this.setGraphNodePositionsFromUtm(newNodeTo, cableSection.getEndPoint());
			
			// --- Create and set the static data model -------------
			
			// --- Data imported from GIS ---------------------------
			CableProperties cableModel = new CableProperties();
			UnitValue cableLength = new UnitValue(cableSection.getLength(), "m");
			cableModel.setLength(cableLength);
			cableModel.setDim(cableSection.getDim());
			cableModel.setDin(cableSection.getDin());
			
			// --- Data derived from the cable specs ----------------
			CableType cableType = null;
			HashMap<String, CableType> dimCharacteristics = cableTypes.get(cableSection.getDim());
			if (dimCharacteristics!=null) {
				cableType = cableTypes.get(cableSection.getDim()).get(cableSection.getDin()); 
			}
			if (cableType!=null) {
				cableModel.setLinearReactance(cableType.getReactance());
				cableModel.setLinearResistance(cableType.getResistance());
				cableModel.setLinearCapacitance(cableType.getCapacitance());
				cableModel.setMaxCurrent(cableType.getMaxCurrent());
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] No cable type characteristics found for DIM " + cableSection.getDim() + " " + cableSection.getDin());
			}
			
			// --- Objects for dynamic run-time data ---------------- 
			UniPhaseCableState cableState = new UniPhaseCableState();
			TimeSeriesChart tsc = new TimeSeriesChart();
			tsc.setTimeSeriesVisualisationSettings(new TimeSeriesChartSettings());
			
			Object[] dataModel = new Object[3];
			dataModel[0] = cableModel;
			dataModel[1] = cableState;
			dataModel[2] = tsc;
			netComp.setDataModel(dataModel);
			
			// --- Get adjacent node components -----------
			GraphNode graphNodeFrom = null;
			GraphNode graphNodeTo = null;
			NetworkComponent netCompFrom = cableSection.getNetworkComponentFrom();
			if (netCompFrom!=null) {
				graphNodeFrom = (GraphNode) this.getNetworkModel().getGraphElementsFromNetworkComponent(netCompFrom).get(0);
			}
			NetworkComponent netCompTo = cableSection.getNetworkComponentTo();
			if (netCompTo!=null) {
				graphNodeTo = (GraphNode) this.getNetworkModel().getGraphElementsFromNetworkComponent(netCompTo).get(0);
			}
			
			// --- Merge into the overall model -----------
			this.getNetworkModel().mergeNetworkModel(newCompNM, null, true, false);
			// --- Merge nodes with neighbours, if any ----
			if (graphNodeFrom!=null) {
				this.getNetworkModel().mergeNodes(new GraphNodePairs(graphNodeFrom, newNodeFrom));
			} else {
				//TODO create generic node for open end
			}
			if (graphNodeTo!=null) {
				this.getNetworkModel().mergeNodes(new GraphNodePairs(graphNodeTo, newNodeTo));
			} else {
				//TODO create generic node for open end
			}
		}
	}
	
	// ----------------------------------------------------
	// --- General coordinate stuff -----------------------
	// ----------------------------------------------------
	/**
	 * Extract the actual coordinates from a list of support points, as provided by QGis CSV export.
	 * @param wktCoordinatesString the support points string
	 * @return the vector List of coordinates
	 */
	private Vector<Point2D> parseWktCoordinates(String wktCoordinatesString){
		Vector<Point2D> coordinatesList = new Vector<>();

		// --- Get the part between the brackets ------------------------------
		int openingBracket = wktCoordinatesString.lastIndexOf("(");	// Match second (
		int closingBracket = wktCoordinatesString.indexOf(")");		// Match first )
		String allCoordinates = wktCoordinatesString.substring(openingBracket+1, closingBracket);
		
		// --- Extract the actual coordinates from the string -----------------
		String[] coordinates = allCoordinates.split(",");	// Pairs separated by commas
		for (int i=0; i<coordinates.length; i++) {
			String[] xyCoordinates = coordinates[i].split(" ");		// Coordinates within pairs separated by spaces
			
			double xCoordinate = Double.parseDouble(xyCoordinates[0]);
			double yCoordinate = Double.parseDouble(xyCoordinates[1]);
			coordinatesList.add(new Point2D.Double(xCoordinate, yCoordinate));
		}
		
		return coordinatesList;
	}
	
	/**
	 * Calculate length of a line that is defined by a list of support points.
	 * @param points the support points
	 * @return the length
	 */
	private float calculateLength(Vector<Point2D> points) {
		float length = 0;
		for (int i=1; i<points.size(); i++) {
			length += points.get(i).distance(points.get(i-1));
		}
		return length;
	}
	
	/**
     * Returns the distance of p3 to the segment defined by p1,p2
     * (Code by Pieter Iserbyt, taken from http://paulbourke.net/geometry/pointlineplane/DistancePoint.java)
     * @param p1 First point of the line segment
     * @param p2 Second point of the line segment
     * @param p3 Point to which we want to know the distance to the segment
     * @return The distance of p3 to the line segment defined by p1,p2
     */
    private double distanceToLineSegment(Point2D p1, Point2D p2, Point2D p3) {

		double xDelta = p2.getX() - p1.getX();
		double yDelta = p2.getY() - p1.getY();

		if ((xDelta == 0) && (yDelta == 0)) {
		    throw new IllegalArgumentException("p1 and p2 cannot be the same point");
		}
	
		double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
	
		Point2D closestPoint;
		if (u < 0) {
		    closestPoint = p1;
		} else if (u > 1) {
		    closestPoint = p2;
		} else {
		    closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}
	
		return closestPoint.distance(p3);
    }

	/**
	 * Checks if two points are equal within the specified tolerance
	 * @param point the point
	 * @param anotherPoint the another point
	 * @return true, if successful
	 */
	private boolean pointEqualsWithTolerance(Point2D point, Point2D anotherPoint) {
		if (point==null||anotherPoint==null) {
			System.out.println();	//TODO just for placing a breakpoint, remove later
		}
		return (Math.abs(point.distance(anotherPoint))<TOLERANCE_FOR_NODE_MATCHING);
	}
	
	/**
	 * Gets the graph node position.
	 *
	 * @param layoutID the layout name
	 * @param networkComponent the network component
	 * @return the graph node position
	 */
	private Point2D getGraphNodePosition(String layoutID, NetworkComponent networkComponent) {
		Point2D position = null;
		String nodeName = networkComponent.getGraphElementIDs().iterator().next();
		if (nodeName!=null) {
			GraphNode graphNode = (GraphNode) this.getNetworkModel().getGraphElement(nodeName);
			if (graphNode!=null) {
				if (layoutID!=null) {
					// --- Get the position for the specified coordinate system ---------
					position = graphNode.getPositionTreeMap().get(layoutID);
				} else {
					// --- If not specified, use the current coordinate system ----------
					position = graphNode.getPosition();
				}
			}
		}
		return position;
	}
	
	/**
	 * Sets the GraphNode positions for the different layouts based on the UTM coordinates.
	 * @param graphNode the graph node
	 * @param positionUTM the position (UTM coordinates)
	 */
	private void setGraphNodePositionsFromUtm(GraphNode graphNode, Point2D positionUTM) {
		Point2D positionDefaultLayout = this.getCoordinatesMapper().mapGeoCoordinatesToDisplay(positionUTM);
		graphNode.getPositionTreeMap().put(this.getDefaultLayoutID(), positionDefaultLayout);
		graphNode.getPositionTreeMap().put(this.getUtmLayoutID(), positionUTM);
		graphNode.setPosition(positionDefaultLayout);
	}

	/**
	 * Gets the coordinates mapper.
	 * @return the coordinates mapper
	 */
	private GeoCoordinatesMapper getCoordinatesMapper() {
		if (coordinatesMapper==null) {
			coordinatesMapper = new GeoCoordinatesMapper(geoCoordinateMinX, geoCoordinateMaxX, geoCoordinateMinY, geoCoordinateMaxY, MAX_X_VALUE, MAX_Y_VALUE);
			coordinatesMapper.setOffset(25);
			coordinatesMapper.setInvertYAxis(true);
		}
		return coordinatesMapper;
	}

	/**
	 * Updates the boundaries for coordinate mapping, i.e. checks for new min/max values
	 * @param newPoint the new point
	 */
	private void updateBoundariesForCoordinateMapping(Point2D newPoint) {
		if (newPoint.getX()<this.geoCoordinateMinX) {
			this.geoCoordinateMinX = newPoint.getX();
		}
		if (newPoint.getX()>this.geoCoordinateMaxX) {
			this.geoCoordinateMaxX = newPoint.getX();
		}
		if (newPoint.getY()<this.geoCoordinateMinY) {
			this.geoCoordinateMinY = newPoint.getY();
		}
		if (newPoint.getY()>this.geoCoordinateMaxY) {
			this.geoCoordinateMaxY = newPoint.getY();
		}
	}
	
	/**
	 * Gets the next component ID.
	 * @return the next component ID
	 */
	private String getNextComponentID() {
		String nextComponentID;
		do {
			nextComponentID = COMPONENT_ID_PREFIX + this.nextComponentNumber;
			this.nextComponentNumber++;
		} while (this.getNetworkModel().getNetworkComponent(nextComponentID)!=null);
		return nextComponentID;
	}

	/**
	 * Internal class for handling bus bar data
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class BusBar{
		private Vector<Point2D> supportPoints;
		private int msLink;
		private Vector<CableSection> cables;
		/**
		 * Gets the support points.
		 * @return the support points
		 */
		public Vector<Point2D> getSupportPoints() {
			return supportPoints;
		}
		
		/**
		 * Sets the support points.
		 * @param supportPoints the new support points
		 */
		public void setSupportPoints(Vector<Point2D> supportPoints) {
			this.supportPoints = supportPoints;
		}
		
		/**
		 * Gets the ms link.
		 * @return the ms link
		 */
		public int getMsLink() {
			return msLink;
		}
		
		/**
		 * Sets the ms link.
		 * @param msLink the new ms link
		 */
		public void setMsLink(int msLink) {
			this.msLink = msLink;
		}
		
		/**
		 * Gets the cables.
		 * @return the cables
		 */
		public Vector<CableSection> getCables() {
			if (cables==null) {
				cables = new Vector<>();
			}
			return cables;
		}
		
		/**
		 * Gets the start point.
		 * @return the start point
		 */
		public Point2D getStartPoint() {
			if (this.getSupportPoints()==null) {
				return null;
			} else {
				return this.getSupportPoints().get(0);
			}
		}
		
		/**
		 * Gets the end point.
		 * @return the end point
		 */
		public Point2D getEndPoint() {
			if (this.getSupportPoints()==null) {
				return null;
			} else {
				return this.getSupportPoints().get(this.getSupportPoints().size()-1);
			}
		}
		
		/**
		 * Gets the center point.
		 * @return the center point
		 */
		public Point2D getCenterPoint() {
			if (this.getSupportPoints()==null) {
				return null;
			} else {
				double xCoordinate = (this.getStartPoint().getX()+this.getEndPoint().getX())/2;
				double yCoordinate = (this.getStartPoint().getY()+this.getEndPoint().getY())/2;
				return new Point2D.Double(xCoordinate, yCoordinate);
			}
		}
		
		
		/**
		 * Checks if a line is connected to this BusBar. Assuming there is always a support point where the the line hits the bar.
		 * @param line the line
		 * @return true if connected
		 */
		public boolean isCableConnected(CableSection line) {
			for (int i=0; i<this.getSupportPoints().size(); i++) {
				if (pointEqualsWithTolerance(supportPoints.get(i), line.getStartPoint()) || 
						pointEqualsWithTolerance(supportPoints.get(i), line.getEndPoint())) {
					return true;
				}
			}
			return false;
		}
		
	}

	/**
	 * Internal class for handling station data
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class Station {
		private String id;
		private String type;
		private String name;
		private String owner;
		private int msLink;
		private Vector<Point2D> polygonPoints;
		private Vector<BusBar> busBars;
		public String getID() {
			return id;
		}
		public void setID(String id) {
			this.id = id;
		}
		
		@SuppressWarnings("unused")
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@SuppressWarnings("unused")
		public String getOwner() {
			return owner;
		}
		public void setOwner(String owner) {
			this.owner = owner;
		}
		@SuppressWarnings("unused")
		public int getMsLink() {
			return msLink;
		}
		public void setMsLink(int msLink) {
			this.msLink = msLink;
		}
		public Vector<Point2D> getPolygonPoints() {
			return polygonPoints;
		}
		public void setPolygonPoints(Vector<Point2D> polygonPoints) {
			this.polygonPoints = polygonPoints;
		}
		public Vector<BusBar> getBusBars() {
			if (busBars==null) {
				busBars = new Vector<>();
			}
			return busBars;
		}
		/**
		 * Calculates the centroid point of this station's polygon.
		 * @return the centroid point of this station's polygon
		 */
		public Point2D calculateCentroid() {
			double centroidX = 0;
			double centroidY = 0;
			
			for (int i=0; i<this.getPolygonPoints().size(); i++) {
				centroidX += this.getPolygonPoints().get(i).getX();
				centroidY += this.getPolygonPoints().get(i).getY();
			}
			
			Point2D centroid = new Point2D.Double(centroidX/this.getPolygonPoints().size(),	 centroidY/this.getPolygonPoints().size());
			return centroid;
		}
		
	}

	/**
	 * Internal class for temporary handling imported data before creating the actual data model
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class CableSection{
		private String din;
		private String dim;
		private int msLink;
		private float length;
		private int nominalVoltage;
		private String stationFrom;
		private String stationTo;
		private String stationFromName;
		private String stationToName;
		private Vector<Point2D> supportPoints;
		
		private Vector<NetworkComponent> componentsAtEnds;
		
		/**
		 * Gets the din.
		 * @return the din
		 */
		public String getDin() {
			return din;
		}
		
		/**
		 * Sets the din.
		 * @param din the new din
		 */
		public void setDin(String din) {
			this.din = din;
		}
		
		/**
		 * Gets the dim.
		 * @return the dim
		 */
		public String getDim() {
			return dim;
		}
		
		/**
		 * Sets the dim.
		 * @param dim the new dim
		 */
		public void setDim(String dim) {
			this.dim = dim;
		}
		
		/**
		 * Gets the station from.
		 * @return the station from
		 */
		public String getStationFrom() {
			return stationFrom;
		}
		
		/**
		 * Sets the station from.
		 * @param stationFrom the new station from
		 */
		public void setStationFrom(String stationFrom) {
			this.stationFrom = stationFrom;
		}
		
		/**
		 * Gets the station to.
		 * @return the station to
		 */
		public String getStationTo() {
			return stationTo;
		}
		
		/**
		 * Sets the station to.
		 * @param stationTo the new station to
		 */
		public void setStationTo(String stationTo) {
			this.stationTo = stationTo;
		}
		
		/**
		 * Gets the support points.
		 * @return the support points
		 */
		public Vector<Point2D> getSupportPoints() {
			return supportPoints;
		}
		
		/**
		 * Sets the support points.
		 * @param supportPoints the new support points
		 */
		public void setSupportPoints(Vector<Point2D> supportPoints) {
			this.supportPoints = supportPoints;
		}
		
		/**
		 * Gets the ms link.
		 * @return the ms link
		 */
		public int getMsLink() {
			return msLink;
		}
		
		/**
		 * Sets the ms link.
		 * @param msLink the new ms link
		 */
		public void setMsLink(int msLink) {
			this.msLink = msLink;
		}
		
		/**
		 * Gets the length.
		 * @return the length
		 */
		public float getLength() {
			return length;
		}
		
		/**
		 * Sets the length.
		 * @param length the new length
		 */
		public void setLength(float length) {
			this.length = length;
		}
		
		/**
		 * Gets the nominal voltage.
		 * @return the nominal voltage
		 */
		public int getNominalVoltage() {
			return nominalVoltage;
		}
		
		/**
		 * Sets the nominal voltage.
		 * @param nominalVoltage the new nominal voltage
		 */
		public void setNominalVoltage(int nominalVoltage) {
			this.nominalVoltage = nominalVoltage;
		}
		
		/**
		 * Sets the route name.
		 * @param routeName the new route name
		 */
		public void setRouteName(String routeName) {
		}
		
		/**
		 * Gets the station from name.
		 * @return the station from name
		 */
		public String getStationFromName() {
			return stationFromName;
		}
		
		/**
		 * Sets the station from name.
		 * @param stationFromName the new station from name
		 */
		public void setStationFromName(String stationFromName) {
			this.stationFromName = stationFromName;
		}
		
		/**
		 * Gets the station to name.
		 * @return the station to name
		 */
		public String getStationToName() {
			return stationToName;
		}
		
		/**
		 * Sets the station to name.
		 * @param stationToName the new station to name
		 */
		public void setStationToName(String stationToName) {
			this.stationToName = stationToName;
		}
		
		/**
		 * Gets the componenst at ends.
		 * @return the componenst at ends
		 */
		public Vector<NetworkComponent> getComponenstAtEnds() {
			if (componentsAtEnds==null) {
				componentsAtEnds = new Vector<>();
			}
			return componentsAtEnds;
		}
		
		/**
		 * Sets the componenst at ends.
		 * @param componenstAtEnds the new componenst at ends
		 */
		public void setComponenstAtEnds(Vector<NetworkComponent> componenstAtEnds) {
			this.componentsAtEnds = componenstAtEnds;
		}

		/**
		 * Gets the network component at the first end of this cable section.
		 * @return the component from
		 */
		public NetworkComponent getNetworkComponentFrom() {
			if (this.getComponenstAtEnds().size()>0) {
				return this.getComponenstAtEnds().get(0);
			} else {
				return null;
			}
		}
		
		/**
		 * Gets the network component at the second end of this cable section.
		 * @return the network component
		 */
		public NetworkComponent getNetworkComponentTo() {
			if (this.getComponenstAtEnds().size()>1) {
				return this.getComponenstAtEnds().get(1);
			} else {
				return null;
			}
		}
		
		
		/**
		 * Gets the start point.
		 * @return the start point
		 */
		public Point2D getStartPoint() {
			Point2D startPoint = null;
			if (this.getSupportPoints().size()>0) {
				startPoint = this.getSupportPoints().get(0);
			}
			return startPoint;
		}
		/**
		 * Gets the end point.
		 * @return the end point
		 */
		public Point2D getEndPoint() {
			Point2D endPoint = null;
			if (this.getSupportPoints().size()>0) {
				endPoint = this.getSupportPoints().get(this.getSupportPoints().size()-1);
			}
			return endPoint;
		}
		
		
		/**
		 * Checks if this cable section has a component at the specified point.
		 * @param utmCoordinate the point
		 * @return true, if successful
		 */
		public boolean hasComponentAtPoint(Point2D utmCoordinate) {
			for (int i=0; i<componentsAtEnds.size(); i++) {
				NetworkComponent endComponent = componentsAtEnds.get(i);
				Point2D endComponentPosition = getGraphNodePosition(getUtmLayoutID(), endComponent);
				if (pointEqualsWithTolerance(utmCoordinate, endComponentPosition)==true) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Gets the component at point.
		 * @param utmCoordinate the utm coordinate
		 * @return the component at point
		 */
		public NetworkComponent getComponentAtPoint(Point2D utmCoordinate) {
			for (int i=0; i<this.getComponenstAtEnds().size(); i++) {
				NetworkComponent netComp = this.getComponenstAtEnds().get(i);
				if (pointEqualsWithTolerance(getGraphNodePosition(getUtmLayoutID(), netComp), utmCoordinate)) {
					return netComp;
				}
			}
			return null;
		}
		
		
		/**
		 * Checks if this cable section is parallel to another one.
		 * @param cableSection the other cable section
		 * @return true, if is parallel
		 */
		public boolean isParallel(CableSection cableSection) {
			return (pointEqualsWithTolerance(this.getStartPoint(), cableSection.getStartPoint()) && pointEqualsWithTolerance(this.getEndPoint(), cableSection.getEndPoint()));
		}
		
		public CableSection clone() {
			CableSection clone = new CableSection();
			clone.setDin(this.getDin());
			clone.setDim(this.getDim());
			clone.setMsLink(this.getMsLink());
			clone.setLength(this.getLength());
			clone.setNominalVoltage(this.getNominalVoltage());
			clone.setStationFrom(this.getStationFrom());
			clone.setStationFromName(this.getStationFromName());
			clone.setStationTo(this.getStationTo());
			clone.setStationToName(this.getStationToName());
			clone.setSupportPoints(this.getSupportPoints());
			clone.setComponenstAtEnds(this.getComponenstAtEnds());
			return clone;
			
		}
		
	}
	
	/**
	 * Internal class for handling cable type data
	 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
	 */
	private class CableType{
		
		private static final String UNIT_REACTANCE = "Ohm/km";
		private static final String UNIT_CAPACITANCE = "nF/km";
		private static final String UNIT_RESISTANCE = "Ohm/km";
		private static final String UNIT_MAX_CURRENT = "A";
		
		private String dim;
		private String din;
		
		private UnitValue reactance;
		private UnitValue capacitance;
		private UnitValue resistance;
		private UnitValue maxCurrent;
		
		
		/**
		 * Gets the dim.
		 * @return the dim
		 */
		public String getDim() {
			return dim;
		}
		
		/**
		 * Sets the dim.
		 * @param dim the new dim
		 */
		public void setDim(String dim) {
			this.dim = dim;
		}
		
		/**
		 * Gets the din.
		 * @return the din
		 */
		public String getDin() {
			return din;
		}
		
		/**
		 * Sets the din.
		 * @param din the new din
		 */
		public void setDin(String din) {
			this.din = din;
		}
		
		/**
		 * Gets the reactance.
		 * @return the reactance
		 */
		public UnitValue getReactance() {
			return reactance;
		}
		
		/**
		 * Sets the reactance.
		 * @param reactance the new reactance
		 */
		public void setReactance(UnitValue reactance) {
			this.reactance = reactance;
		}
		
		/**
		 * Sets the reactance.
		 * @param reactance the new reactance
		 */
		public void setReactance(float reactance) {
			this.setReactance(new UnitValue(reactance, UNIT_REACTANCE));
		}
		
		/**
		 * Gets the capacitance.
		 * @return the capacitance
		 */
		public UnitValue getCapacitance() {
			return capacitance;
		}
		
		/**
		 * Sets the capacitance.
		 * @param capacitance the new capacitance
		 */
		public void setCapacitance(UnitValue capacitance) {
			this.capacitance = capacitance;
		}
		
		/**
		 * Sets the capacitance.
		 * @param capacitance the new capacitance
		 */
		public void setCapacitance(float capacitance) {
			this.setCapacitance(new UnitValue(capacitance, UNIT_CAPACITANCE));
		}
		
		/**
		 * Gets the resistance.
		 * @return the resistance
		 */
		public UnitValue getResistance() {
			return resistance;
		}
		
		/**
		 * Sets the resistance.
		 * @param resistance the new resistance
		 */
		public void setResistance(UnitValue resistance) {
			this.resistance = resistance;
		}
		
		/**
		 * Sets the resistance.
		 * @param resistance the new resistance
		 */
		public void setResistance(float resistance) {
			this.setResistance(new UnitValue(resistance, UNIT_RESISTANCE));
		}
		
		/**
		 * Gets the max current.
		 * @return the max current
		 */
		public UnitValue getMaxCurrent() {
			return maxCurrent;
		}
		
		/**
		 * Sets the max current.
		 * @param maxCurrent the new max current
		 */
		public void setMaxCurrent(UnitValue maxCurrent) {
			this.maxCurrent = maxCurrent;
		}
		
		/**
		 * Sets the max current.
		 * @param maxCurrent the new max current
		 */
		public void setMaxCurrent(float maxCurrent) {
			this.setMaxCurrent(new UnitValue(maxCurrent, UNIT_MAX_CURRENT));
		}
		
		
	}

	/* (non-Javadoc)
	 * @see hygrid.csvFileImport.NetworkTopologyImporterService#setGraphController(org.awb.env.networkModel.controller.GraphEnvironmentController)
	 */
	@Override
	public void setGraphController(GraphEnvironmentController graphController) {
		this.graphController = graphController;
	}

	/* (non-Javadoc)
	 * @see hygrid.csvFileImport.NetworkTopologyImporterService#getFileFilters()
	 */
	@Override
	public List<FileFilter> getFileFilters() {
		if (fileFilters==null) {
			fileFilters = new ArrayList<FileFilter>();
			fileFilters.add(this.createFileFilter(".zip", "Lemgo Medium Voltage Network exported GIS Data (zip File)"));
			fileFilters.add(this.createFileFilter(".csv", "Lemgo Medium Voltage Network exported GIS Data (csv File)"));
		}
		return fileFilters;
	}
}
