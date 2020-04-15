package de.enflexit.ea.core.awbIntegration.plugin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;
import org.awb.env.networkModel.helper.DomainClustering;

import agentgui.core.application.Application;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.ontology.TimeSeries;
import agentgui.ontology.TimeSeriesChart;
import agentgui.ontology.TimeSeriesValuePair;
import de.enflexit.common.csv.CsvFileWriter;
import de.enflexit.ea.core.awbIntegration.plugin.gui.SimulationDataExportConfigurationDialog;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import energy.GlobalInfo;
import energy.UnitConverter;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import hygrid.csvFileImport.NetworkModelToCsvMapper;
import hygrid.csvFileImport.NetworkModelToCsvMapper.BranchDescription;
import jade.util.leap.Iterator;

/**
 * This class is responsible for exporting CSV data from a Network Model
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class SimulationDataCsvExporter {
	
	private static EnergyUnitFactorPrefixSI TARGET_UNIT = EnergyUnitFactorPrefixSI.NONE_0;
	
	private NetworkModel networkModel;
	private DomainClustering domainClustering;
	
	private NetworkModelToCsvMapper mapper12;
	
	private File basicCsvFilePath;	// The different CSV files will be derived from this base file path 
	
	private Double[][][] currentBetweenNodes;
	private Double[][] voltageAtNodes;
	private Double[][][] powerAtNodes;
	
	private DateFormat dateFormat;

	/**
	 * Constructor
	 * @param networkModel The network model to work on
	 */
	public SimulationDataCsvExporter(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
	/**
	 * Triggers the export
	 * @return Export successful?
	 */
	public void doExport(){
		
		// TODO: Check if this works!
		// --- Check the electrical distribution grid cluster -----------------
		Vector<DomainCluster> dcVector = this.getDomainClustering().getDomainClusterByDomain(GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V);
		if (dcVector.size()==0) {
			JOptionPane.showMessageDialog(null, "No " + GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V + " were found!", "Export successful!", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// --- Select CSV file ------------------------------------------------
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV File", "csv");
		fileChooser.addChoosableFileFilter(csvFilter);
		fileChooser.setFileFilter(csvFilter);
		fileChooser.setCurrentDirectory(GlobalInfo.getLastSelectedDirectory());
		int ret = fileChooser.showSaveDialog(Application.getMainWindow());
		
		if (ret==JFileChooser.APPROVE_OPTION) {
			
			this.basicCsvFilePath = fileChooser.getSelectedFile();
			
			// --- Append suffix if necessary ---------------------------------
			if(this.basicCsvFilePath.getAbsolutePath().toLowerCase().endsWith(".csv") == false){
				this.basicCsvFilePath = new File(this.basicCsvFilePath.getAbsolutePath()+".csv");
			}
			GlobalInfo.setLastSelectedDirectory(fileChooser.getCurrentDirectory());
			
			// --- Determine the simulated time range -------------------------
			SimulationSetup currentSimSetup = Application.getProjectFocused().getSimulationSetups().getCurrSimSetup();
			long startTimestamp = Long.parseLong(currentSimSetup.getTimeModelSettings().get("TimeStart"));
			long stopTimestamp = Long.parseLong(currentSimSetup.getTimeModelSettings().get("TimeStop"));
			
			long timeToExport = -1;
			boolean timeWithinRange = false;
			
			SimulationDataExportConfigurationDialog confDialog;
			do {
				// --- Show configuration dialog ------------------------------
				confDialog = new SimulationDataExportConfigurationDialog(startTimestamp);
				if(confDialog.isCanceled() == false){
					
					// --- Check if the selected time is within the simulated time range
					timeToExport = confDialog.getSelectedTimestamp();
					timeWithinRange = (timeToExport >= startTimestamp && timeToExport <= stopTimestamp);
					
					// --- If not, show a message
					if (timeWithinRange==false){
						
						JOptionPane.showMessageDialog(null, "The time you selected is outside the simulated time range.\n"
								+ "Please select a time between " + this.getDateFormat().format(new Date(startTimestamp))
								+ " and " + this.getDateFormat().format(new Date(stopTimestamp)), 
								"Time out of range!", 
								JOptionPane.WARNING_MESSAGE);
					}
				}
				
				// --- Repeat until a valid time is selected or the dialog is canceled
			} while(timeWithinRange == false && confDialog.isCanceled() == false);
			
			if (confDialog.isCanceled()==false){
				try {
				
					// --- Loop over the distribution grids found -----------------------
					for (int i = 0; i < dcVector.size(); i++) {
						
						DomainCluster dc = dcVector.get(i);
						
						// --- TODO: Moved from the constructor area to here !!
						// --- TODO: consider multiple networks here !!
						int vertexCount = networkModel.getGraph().getVertexCount();
						
						this.voltageAtNodes = new Double[vertexCount][3];
						this.powerAtNodes = new Double[vertexCount][3][2];
						this.currentBetweenNodes = new Double[vertexCount][vertexCount][3];
						
						// --- TODO: This was newly implemented
						this.setCurrentNetworkModelToCsvMapper(new NetworkModelToCsvMapper(networkModel, dc));
						
						// --- Collect required data from the network components ---
						this.collectDataFromNodes(timeToExport);
						this.collectDataFromBranches(timeToExport);
						
						// --- Export the data to CSV files -------------------------
						this.exportVoltageData();
						this.exportPowerData();
						this.exportCurrentData();
					}
					JOptionPane.showMessageDialog(null, "Export successful", "Export successful!", JOptionPane.INFORMATION_MESSAGE);
					
				} catch (IOException e) {
					
					JOptionPane.showMessageDialog(null, "Error writing CSV files!", "Export failed", JOptionPane.WARNING_MESSAGE);
					System.err.println("Error storing CSV file");
					e.printStackTrace();
				
				}
			}
			
		}
	}
	
	/**
	 * Gets the domain clustering.
	 * @return the domain clustering
	 */
	private DomainClustering getDomainClustering() {
		if (domainClustering==null) {
			domainClustering = new DomainClustering(this.networkModel);
		}
		return domainClustering;
	}
	
	/**
	 * Returns the current network model to csv mapper.
	 * @return the current network model to csv mapper
	 */
	private NetworkModelToCsvMapper getCurrentNetworkModelToCsvMapper() {
		return this.mapper12;
	}
	/**
	 * Sets the current network model to csv mapper.
	 * @param csvMapper the new current network model to csv mapper
	 */
	private void setCurrentNetworkModelToCsvMapper(NetworkModelToCsvMapper csvMapper) {
		this.mapper12 = csvMapper;
	}
	
	/**
	 * Collects current data for a specified time from all branches
	 * @param timestamp The time
	 */
	private void collectDataFromBranches(long timestamp){
		
		// Iterate over all branches
		for (BranchDescription bd : this.getCurrentNetworkModelToCsvMapper().getBranchDescription()) {
		
			// Determine the connected nodes
			int nodeFrom = bd.getNodeNumberFrom();
			int nodeTo = bd.getNodeNumberTo();
			
			// Get the time series data for this branch
			NetworkComponent branch = bd.getNetworkComponent();
			Object[] dataModel = (Object[]) branch.getDataModel();
			TimeSeriesChart tsChart = (TimeSeriesChart)dataModel[2];

			// Iterate over all data series
			Iterator allSeries = tsChart.getAllTimeSeriesChartData();
			while (allSeries.hasNext()) {
				TimeSeries currentSeries = (TimeSeries) allSeries.next();

				// Check if the series contains current data
				String seriesLabel = currentSeries.getLabel();
				if (seriesLabel.contains("Current")){
					
					// If it does, extract the value for the time of interest and put it to the right field in the array
					int phase = Integer.parseInt(seriesLabel.substring(seriesLabel.length()-1));
					TimeSeriesValuePair valuePair = this.findValuePairForTime(currentSeries, timestamp);
					if (valuePair!=null) {
						currentBetweenNodes[nodeFrom][nodeTo][phase-1] = (double) valuePair.getValue().getFloatValue();
					}
				}
			}
			
		}
	}
	
	/**
	 * Collects voltage and power data for a specified time from all nodes
	 * @param timestamp The time
	 */
	private void collectDataFromNodes(long timestamp){
		
		for (int index : this.getCurrentNetworkModelToCsvMapper().getNodeNumberToGraphNode().keySet()) {
			
			GraphNode node = this.getCurrentNetworkModelToCsvMapper().getNodeNumberToGraphNode().get(index);
			Object[] dataModel = (Object[]) node.getDataModel();
			TimeSeriesChart tsChart = (TimeSeriesChart) dataModel[1];
			Iterator allSeries = tsChart.getAllTimeSeriesChartData();
			
			while (allSeries.hasNext()) {
				
				TimeSeries currentSeries = (TimeSeries) allSeries.next();
				// Check if the series contains voltage data
				String seriesLabel = currentSeries.getLabel();
				TimeSeriesValuePair valuePair = this.findValuePairForTime(currentSeries, timestamp);

				if (seriesLabel.contains("Voltage")) {		// Voltage data
					
					// If it does, extract the value for the time of interest and put it to the right field in the array
					int phase = Integer.parseInt(seriesLabel.substring(seriesLabel.length()-1));
					if(valuePair != null && index < voltageAtNodes.length){
						voltageAtNodes[index][phase-1] = (double) valuePair.getValue().getFloatValue();
					}
					
				} else if(seriesLabel.contains("Power")) {	// Power data
					
					// Get value
					double value = valuePair.getValue().getFloatValue();
					
					// Convert SI unit if necessary
					EnergyUnitFactorPrefixSI siPrefix = this.stringToUnitPrefix(currentSeries.getUnit());
					if(siPrefix != TARGET_UNIT){
						EnergyFlowInWatt eFlow = new EnergyFlowInWatt();
						eFlow.setSIPrefix(siPrefix);
						eFlow.setValue(value);
						
						value = UnitConverter.convertEnergyFlowToWatt(eFlow);
					}
					
					// Distinguish between active and reactive power
					if(seriesLabel.contains("Active Power")){
						
						int phase = Integer.parseInt(seriesLabel.substring(seriesLabel.length()-1));
						if(valuePair != null && index < voltageAtNodes.length){
							powerAtNodes[index][phase-1][0] = value;
						}
						
					} else if(seriesLabel.contains("Reactive Power")) {
						
						int phase = Integer.parseInt(seriesLabel.substring(seriesLabel.length()-1));
						if(valuePair != null && index < voltageAtNodes.length){
							powerAtNodes[index][phase-1][1] = value;
						}
						
					}
					
				}
			} // end while
		} // end for
		
	}
	
	/**
	 * Exports the current data to CSV files, one file per phase
	 * @throws IOException Error creating the files
	 */
	private void exportCurrentData() throws IOException{

		// --- Some initializations -----------
		String filePath = this.basicCsvFilePath.getPath();
		String separator = ";";
		int vertexCount = this.currentBetweenNodes.length;
		CsvFileWriter csvFileWriter = new CsvFileWriter();
		
		// --- Build headline -----------------
		StringBuffer headline = new StringBuffer("From/To");
		for(int i=0; i<vertexCount; i++){
			headline.append(separator+(i+1));
		}

		// --- Iterate over all phases ---------
		for(int phase=0; phase<voltageAtNodes[0].length; phase++){
			
			// --- Prepare output file ------
			int phaseID = phase+1;
			String fileName = filePath.substring(0, filePath.lastIndexOf('.'))+"_current_L"+phaseID+".csv";
			File outputFile = new File(fileName);
			
			// --- Prepare export data -----------
			String[][] exportData = new String[vertexCount][vertexCount+1];
			for(int nodeFrom=0; nodeFrom<vertexCount; nodeFrom++){
				exportData[nodeFrom][0] = ""+(nodeFrom+1);		// Node ID
				
				for(int nodeTo=0; nodeTo<vertexCount; nodeTo++){
					String value;
					if(this.currentBetweenNodes[nodeFrom][nodeTo][phase] != null){
						value = ""+this.currentBetweenNodes[nodeFrom][nodeTo][phase];
					}else{
						value = "0.0";
					}
					exportData[nodeFrom][nodeTo+1] = ""+value;
				}
				
			}
			
			// --- Write the data to the file
			csvFileWriter.exportData(outputFile, exportData, headline.toString());
			
		}
		
	}
	
	/**
	 * Exports the voltage data to CSV files, one file per phase
	 * @throws IOException Error creating the files
	 */
	private void exportVoltageData() throws IOException{
		
		// --- Some initializations ----------
		String filePath = this.basicCsvFilePath.getPath();
		String headline = "Node;U";
		CsvFileWriter csvFileWriter = new CsvFileWriter();
		
		// --- Iterate over all phases ---------
		for(int phase=0; phase<voltageAtNodes[0].length; phase++){
			
			// --- Prepare output file ------
			int phaseID = phase+1;
			String fileName = filePath.substring(0, filePath.lastIndexOf('.'))+"_voltage_L"+phaseID+".csv";
			File outputFile = new File(fileName);
			
			// --- Prepare export data -----------
			String[][] exportData = new String[voltageAtNodes.length][2];
			for(int nodeIndex=0; nodeIndex<voltageAtNodes.length; nodeIndex++){
				exportData[nodeIndex][0] = ""+(nodeIndex+1);	// Node ID
				exportData[nodeIndex][1] = ""+voltageAtNodes[nodeIndex][phase];	// Active power
			}
			
			// --- Write the data to the file
			csvFileWriter.exportData(outputFile, exportData, headline);
		}
		
	}

	/**
	 * Exports the power data to CSV files, one file per phase
	 * @throws IOException Error creating the files
	 */
	private void exportPowerData(){
		
		// --- Some initializations ----------
		String filePath = this.basicCsvFilePath.getPath();
		String headline = "Node;P;Q";
		CsvFileWriter csvFileWriter = new CsvFileWriter();
		
		// --- Iterate over all phases ---------
		for(int phase=0; phase<powerAtNodes[0].length; phase++){
			
			// --- Prepare output file ------
			int phaseID = phase+1;
			String fileName = filePath.substring(0, filePath.lastIndexOf('.'))+"_power_L"+phaseID+".csv";
			File outputFile = new File(fileName);
			
			// --- Prepare export data -----------
			String[][] exportData = new String[powerAtNodes.length][3];
			for(int nodeIndex=0; nodeIndex<powerAtNodes.length; nodeIndex++){
				exportData[nodeIndex][0] = ""+(nodeIndex+1);	// Node ID
				exportData[nodeIndex][1] = ""+powerAtNodes[nodeIndex][phase][0];	// Active power
				exportData[nodeIndex][2] = ""+powerAtNodes[nodeIndex][phase][1];	// Reactive power
			}
			
			// --- Write the data to the file
			csvFileWriter.exportData(outputFile, exportData, headline);
		}
		
	}

	/**
	 * Get the {@link TimeSeriesValuePair} for a specific time from a {@link TimeSeries}
	 * @param series The series
	 * @param timestamp The time
	 * @return The value pair, null if not found
	 */
	private TimeSeriesValuePair findValuePairForTime(TimeSeries series, long timestamp){
		
		TimeSeriesValuePair vpFound = null;
		
		Iterator allPairs = series.getTimeSeriesValuePairs().iterator();
		TimeSeriesValuePair vpPrevious = null;
		
		while(allPairs.hasNext() && vpFound == null){
			TimeSeriesValuePair vpCurrent = (TimeSeriesValuePair) allPairs.next();

			if(vpCurrent.getTimestamp().getLongValue() == timestamp){
				
				// Exact match
				vpFound = vpCurrent;
			
			}else if(vpCurrent.getTimestamp().getLongValue() > timestamp){
				
				// Timestamp passed - searched time is within the previous step
				vpFound = vpPrevious;
				
			}
			
			// Remember the current value pair for the next step
			vpPrevious = vpCurrent;
			
		}
		
		return vpFound;
		
	}

	/**
	 * Get the date format, initialize if necessary
	 * @return The date format
	 */
	private DateFormat getDateFormat(){
		if(this.dateFormat == null){
			this.dateFormat = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
		}
		return this.dateFormat;
	}
	
	/**
	 * Determines the {@link EnergyUnitFactorPrefixSI} from unit given as a simple String. 
	 * Attention: Unknown prefixes will be interpreted as no prefix at all. 
	 * @param unitString The unit
	 * @return The SI prefix for this unit
	 */
	private EnergyUnitFactorPrefixSI stringToUnitPrefix(String unitString){
		
		EnergyUnitFactorPrefixSI prefix;
		
		if(unitString.startsWith("m")){
			prefix = EnergyUnitFactorPrefixSI.MILLI_M_3;
		}else if(unitString.startsWith("k")){
			prefix = EnergyUnitFactorPrefixSI.KILO_K_3;
		}else if(unitString.startsWith("M")){
			prefix = EnergyUnitFactorPrefixSI.MEGA_M_6;
		}else if(unitString.startsWith("G")){
			prefix = EnergyUnitFactorPrefixSI.GIGA_G_9;
		}else{
			prefix = EnergyUnitFactorPrefixSI.NONE_0;
		}
		
		return prefix;
	
	}
	
}
