package de.enflexit.ea.electricity.scheduleImport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.persistence.NetworkModelOperationalDataImportService;

import agentgui.core.application.Application;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomModelType;
import de.enflexit.eom.awb.adapter.EomDataModelStorageHandler.EomStorageLocation;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.CurrentType;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.helper.UnitConverter;
import energy.optionModel.AbstractUsageOfInterface;
import energy.optionModel.EnergyAmount;
import energy.optionModel.EnergyCarrier;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.TimeUnit;
import energy.optionModel.UsageOfInterfaceEnergy;

/**
 * This class provides an importer service to import schedules to the network components of a tri-phase low voltage grid.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LowVoltageGridScheduleImporter extends AbstractElectricalNetworkScheduleImporter implements NetworkModelOperationalDataImportService {
	
	// --- Several constants --------------------------
	private static final String DEFAULT_STATE_ID = "Prosuming";
	private static final String DEFAULT_SYSTEM_TYPE = "Electrical Prosumer";
	private static final int COLUMN_INDEX_FOR_COMPONENT_ID = 0;
	private static final int FIRST_DATA_COLUMN_INDEX = 3;
	private static final int COLUMNS_PER_TIMESTEP = 3;
	private static final EnergyUnitFactorPrefixSI SI_PREFIX_FOR_ENERGY_FLOWS = EnergyUnitFactorPrefixSI.KILO_K_3;
	private static final EnergyUnitFactorPrefixSI SI_PREFIX_FOR_ENERGY_AMOUNTS = EnergyUnitFactorPrefixSI.KILO_K_3;
	private static final TimeUnit TIME_UNIT_FOR_ENERGY_AMOUNTS = TimeUnit.HOUR_H;
	
	private List<FileFilter> fileNameExtensionFilters;
	private GraphEnvironmentController graphController;
	
	private String[] interfaceIDs = {
			"Electricity L1",
			"Electricity L2",
			"Electricity L3"
	};

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.DataModelNetworkElementImportService#setGraphEnvironmentController(org.awb.env.networkModel.controller.GraphEnvironmentController)
	 */
	@Override
	public void setGraphEnvironmentController(GraphEnvironmentController graphController) {
		this.graphController = graphController;
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.DataModelNetworkElementImportService#getFileNameExtensionFilters()
	 */
	@Override
	public List<FileFilter> getFileNameExtensionFilters() {
		if (fileNameExtensionFilters==null) {
			fileNameExtensionFilters = new ArrayList<FileFilter>();
			fileNameExtensionFilters.add(new FileNameExtensionFilter("Schedules for a BUW-style low voltage grid (.csv)", "csv"));
		}
		return fileNameExtensionFilters;
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.persistence.DataModelNetworkElementImportService#importDataFromFile(java.io.File)
	 */
	@Override
	public void importDataFromFile(File importFile) {
		ElectricityScheduleImportConfigurationDialog configurationDialog = new ElectricityScheduleImportConfigurationDialog(Application.getMainWindow(), this);
		if(configurationDialog.isCanceled() == false){
				
			// --- Import schedules from the CSV file ---------
			List<ScheduleList> importedSchedules = this.importSchedules(importFile);
			
			// --- Assign schedules to the network components ---
			if (importedSchedules != null && importedSchedules.size() > 0){
				for (ScheduleList sl : importedSchedules) {
					NetworkComponent netComp = this.graphController.getNetworkModel().getNetworkComponent(sl.getSystemID());
					TreeMap<String, String> storageSettings = new TreeMap<>();
					storageSettings.put(EomDataModelStorageHandler.EOM_SETTING_EOM_MODEL_TYPE, EomModelType.ScheduleList.toString());
					storageSettings.put(EomDataModelStorageHandler.EOM_SETTING_STORAGE_LOCATION, EomStorageLocation.NetworkElementBase64.toString());
					netComp.setDataModelStorageSettings(storageSettings);
					netComp.setDataModel(sl);
					
					NetworkComponentAdapter netCompAdapter = this.graphController.getNetworkModel().getNetworkComponentAdapter(this.graphController, netComp);
					if (netCompAdapter!=null) {
						netCompAdapter.getStoredDataModelAdapter().getDataModelStorageHandlerInternal().saveDataModel(netComp);
					}
					
				}
				String successMessage = importedSchedules.size() + " load profiles imported!";
				JOptionPane.showMessageDialog(Application.getMainWindow(), successMessage, "Import successful", JOptionPane.INFORMATION_MESSAGE);
			} else {
				String failureMessage = "Load profile import failed!";
				JOptionPane.showMessageDialog(Application.getMainWindow(), failureMessage, "Import failed", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	/**
	 * Import the schedules from the CSV file
	 * @param csvFile The CSV file
	 * @return The list of imported schedules, null in case of an error
	 */
	private List<ScheduleList> importSchedules(File csvFile){
		
		List<ScheduleList> importedScheduleLists = null;
		
		try {
		
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			importedScheduleLists = new Vector<ScheduleList>();

			// --- Skip first row (column headers) ------------- 
			if(this.isSkipFirstRow() == true){
				br.readLine();
			}
			
			// -- Read the file linewise ---------------------
			String inBuffer;
			while((inBuffer = br.readLine()) != null){
				
				// --- Import the current line (= one system schedule) --------
				ScheduleList scheduleList = this.importSystemSchedule(inBuffer);
				if(scheduleList != null){
					importedScheduleLists.add(scheduleList);
				}
				
			}
			
			br.close();
			
		} catch (IOException e) {
			System.err.println("Error reading the CSV file!");
		}
		
		return importedScheduleLists;
		
	}
	
	/**
	 * Import a single system schedule
	 * @param csvRow The row containing the data for this system
	 * @return The imported schedule
	 */
	private ScheduleList importSystemSchedule(String csvRow){
		
		ScheduleList scheduleList = null;
		String[] parts = csvRow.split(";");
		
		if(parts.length > 0){
			
			// --- Assuming numeric system IDs in the file and n<numeric system ID> in the network model
			String systemID = "n"+parts[COLUMN_INDEX_FOR_COMPONENT_ID];
			scheduleList = new ScheduleList();
			scheduleList.setSystemID(systemID);
			scheduleList.setNetworkID(DEFAULT_SYSTEM_TYPE);
			
			// --- Define domain model and interface settings for all phases ----------------
			for(int i=0; i < COLUMNS_PER_TIMESTEP; i++){
	
				// --- Active power ----------------------------------------
				String activeInterfaceID = this.interfaceIDs[i] + " P";
				DefaultDomainModelElectricity domainModelActivePower = new DefaultDomainModelElectricity();
				domainModelActivePower.setPowerType(PowerType.ActivePower);
				domainModelActivePower.setCurrentType(CurrentType.AC);
				domainModelActivePower.setPhase(Phase.values()[i+1]);
				domainModelActivePower.setFrequency(50);
				domainModelActivePower.setRatedVoltage(230);
				
				InterfaceSetting interfaceSettingActivePower = new InterfaceSetting();
				interfaceSettingActivePower.setInterfaceID(activeInterfaceID);
				interfaceSettingActivePower.setDomainModel(domainModelActivePower);
				interfaceSettingActivePower.setDomain(EnergyCarrier.ELECTRICITY.value());
				
				scheduleList.getInterfaceSettings().add(interfaceSettingActivePower);
				
				// --- Reactive power -------------------------------------
				String reactiveInterfaceID = this.interfaceIDs[i] + " Q";
				DefaultDomainModelElectricity domainModelReactivePower = new DefaultDomainModelElectricity();
				domainModelReactivePower.setPowerType(PowerType.ReactivePower);
				domainModelReactivePower.setCurrentType(CurrentType.AC);
				domainModelReactivePower.setPhase(Phase.values()[i+1]);
				domainModelReactivePower.setFrequency(50);
				domainModelReactivePower.setRatedVoltage(230);
				
				InterfaceSetting interfaceSettingsReactivePower = new InterfaceSetting();
				interfaceSettingsReactivePower.setInterfaceID(reactiveInterfaceID);
				interfaceSettingsReactivePower.setDomainModel(domainModelReactivePower);
				interfaceSettingsReactivePower.setDomain(EnergyCarrier.ELECTRICITY.value());
				
				scheduleList.getInterfaceSettings().add(interfaceSettingsReactivePower);
				
			}
			
			int col = FIRST_DATA_COLUMN_INDEX;	// The first column containing energy flow data
			long timestamp = this.getStartDateTime().getTime();	// The initial time stamp
			timestamp += this.getStateDurationMillis();	// The state should start at timestamp, but tsse.globalTime defines the end
			
			// --- Remember the last imported TSSE -------
			TechnicalSystemStateEvaluation lastTSSE = null;
			while(col < parts.length){
				
				// --- Initialize the TSSE -------------------
				TechnicalSystemStateEvaluation tsse = new TechnicalSystemStateEvaluation();
				tsse.setStateID(DEFAULT_STATE_ID);
				tsse.setGlobalTime(timestamp);
				tsse.setStateTime(this.getStateDurationMillis());
				
				// --- One column for each phase -------------
				for(int i=0; i < COLUMNS_PER_TIMESTEP; i++){
					
					String nextVal = parts[col+i];
					// --- Energy flow ------------------------
					try{
						double apparentPower = Double.parseDouble(nextVal);
						
						// --- Active power -----------------------
						double activePower = this.getCosPhi() * apparentPower;
						String activeInterfaceID = this.interfaceIDs[i] + " P";
						UsageOfInterfaceEnergy uoiActive = this.createUsageOfInterfaces(activePower, activeInterfaceID, lastTSSE);
						tsse.getUsageOfInterfaces().add(uoiActive);
						
						// --- Reactive power --------------------
						double reactivePower = Math.sqrt(Math.pow(apparentPower, 2) - Math.pow(activePower, 2));
						String reactiveInterfaceID = this.interfaceIDs[i] + " Q";
						UsageOfInterfaceEnergy uoiReactive = this.createUsageOfInterfaces(reactivePower, reactiveInterfaceID, lastTSSE);
						tsse.getUsageOfInterfaces().add(uoiReactive);
					}catch(NumberFormatException nfe ){
						// --- A value could not be parsed ----------------
						String errorMessage = "Error importing load profile for " + systemID + ", could not parse " + nextVal + ".\nPlease check your CSV data!";
						JOptionPane.showMessageDialog(Application.getMainWindow(), errorMessage, "Error importing load profile", JOptionPane.ERROR_MESSAGE);
						return null;
					}
					
				}
				
				// --- Append to the TSSE list --------
				tsse.setParent(lastTSSE);
				// --- Prepare next iteration ---------
				lastTSSE = tsse;
				timestamp += this.getStateDurationMillis();
				col += COLUMNS_PER_TIMESTEP;
				
			}
			
			Schedule schedule = new Schedule();
			schedule.setTechnicalSystemStateEvaluation(lastTSSE);
			scheduleList.getSchedules().add(schedule);
		}
		
		return scheduleList;
	}
	
	/**
	 * Create a UsageOfInterfaces instance with energy flow and cumulated energy amount
	 * @param energyFlowValue The energy flow
	 * @param interfaceID The interface ID
	 * @param currentTSSE The current {@link TechnicalSystemStateEvaluation}
	 * @return The {@link UsageOfInterfaces} instance
	 */
	private UsageOfInterfaceEnergy createUsageOfInterfaces(double energyFlowValue, String interfaceID, TechnicalSystemStateEvaluation lastTSSE){
		
		UsageOfInterfaceEnergy uoi = new UsageOfInterfaceEnergy();
		uoi.setInterfaceID(interfaceID);
		
		// Energy Flow
		EnergyFlowInWatt eFlow = new EnergyFlowInWatt();
		eFlow.setValue(energyFlowValue);
		eFlow.setSIPrefix(SI_PREFIX_FOR_ENERGY_FLOWS);
		uoi.setEnergyFlow(eFlow);
		
		// --- Cumulated energy amount ------------
		double cumulated = 0;
		
		// --- Last TSSEs cumulated amount ------------------
		if(lastTSSE != null){
			UsageOfInterfaceEnergy lastUOI = this.getInterfaceUsageByID(lastTSSE, uoi.getInterfaceID());
			cumulated += lastUOI.getEnergyAmountCumulated().getValue();
		}
		
		// --- Additional energy for the current TSSE ---------
		if(uoi.getEnergyFlow() != null && uoi.getEnergyFlow().getValue() != 0){
			double stateDurationInEnergyAmountTimeUnit = UnitConverter.convertDuration(this.getStateDurationMillis(), TIME_UNIT_FOR_ENERGY_AMOUNTS);
			
			EnergyFlowInWatt energyFlowInEnergyAmountSiUnit = UnitConverter.convertEnergyFlowInWatt(uoi.getEnergyFlow(), SI_PREFIX_FOR_ENERGY_AMOUNTS);
			double deltaEnergyAmount = energyFlowInEnergyAmountSiUnit.getValue() * stateDurationInEnergyAmountTimeUnit;
			
			cumulated += deltaEnergyAmount;
		}
	
		// --- Prepare energy amount instance ---------------
		EnergyAmount eaCumulated = new EnergyAmount();
		eaCumulated.setSIPrefix(SI_PREFIX_FOR_ENERGY_AMOUNTS);
		eaCumulated.setTimeUnit(TIME_UNIT_FOR_ENERGY_AMOUNTS);
		eaCumulated.setValue(cumulated);
		uoi.setEnergyAmountCumulated(eaCumulated);
		
		return uoi;
	}
	
	
	/**
	 * Gets the {@link UsageOfInterfaces} instance for the interface with the given ID
	 * @param tsse The current {@link TechnicalSystemStateEvaluation}
	 * @param interfaceID The interface ID to look for
	 * @return The {@link UsageOfInterfaces} instance for the given ID, or null if not found
	 */
	private UsageOfInterfaceEnergy getInterfaceUsageByID(TechnicalSystemStateEvaluation tsse, String interfaceID){
		for(AbstractUsageOfInterface uoi : tsse.getUsageOfInterfaces()){
			if(uoi.getInterfaceID().equals(interfaceID)){
				return (UsageOfInterfaceEnergy) uoi;
			}
		}
		return null;
	}

}
