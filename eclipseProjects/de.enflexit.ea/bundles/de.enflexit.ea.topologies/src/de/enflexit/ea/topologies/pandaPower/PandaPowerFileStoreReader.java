package de.enflexit.ea.topologies.pandaPower;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import de.enflexit.common.csv.CsvDataController;
import de.enflexit.ea.topologies.BundleHelper;
import energy.domain.DefaultDomainModelElectricity;
import energy.domain.DefaultDomainModelElectricity.CurrentType;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.domain.DefaultDomainModelElectricity.PowerType;
import energy.helper.NumberHelper;
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
import energy.optionModel.TimeRange;
import energy.optionModel.TimeUnit;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.persistence.ScheduleList_StorageHandler;
import energy.schedule.loading.ScheduleTimeRange;

/**
 * The SimBenchFileStoreReader enables to load SimBench data and translate it into ScheduleList's 
 *  
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PandaPowerFileStoreReader {

	// --- From here variables for the time series import ---------------------
	private static final EnergyUnitFactorPrefixSI SI_PREFIX_FOR_ENERGY_FLOWS = EnergyUnitFactorPrefixSI.KILO_K_3;
	private static final EnergyUnitFactorPrefixSI SI_PREFIX_FOR_ENERGY_AMOUNTS = EnergyUnitFactorPrefixSI.KILO_K_3;
	private static final TimeUnit TIME_UNIT_FOR_ENERGY_AMOUNTS = TimeUnit.HOUR_H;
	
	private static final String DEFAULT_TSSE_CONFIG_ID = "Config";
	private static final String DEFAULT_TSSE_STATE_ID = "Prosuming";
	private static final String[] INTERFACE_IDs_ElectricityTriPhase = {"Electricity L1", "Electricity L2", "Electricity L3"};
	private static final String INTERFACE_IDs_ElectricityUniPhase = "Electricity";
	
	private SimpleDateFormat dateFormatter;
	
	// --------------------------------------------------------------------------------------------
	// --- From here: Methods to create ScheduelList's --------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Return the ScheduleList that can be found through a node in the specified data model row.
	 * @param rowSelected the selected row in the model
	 * @param scheduleTimeRange the ScheduleTimeRange to be considered
	 * @return the ScheduleList
	 */
	public ScheduleList getScheduleListByNode(int rowSelected, ScheduleTimeRange scheduleTimeRange) {
		
		Integer nodeIndex = this.getNodeIndex(rowSelected);
		
		HashMap<String, Object> nodeRowHashMapNodeA = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index), nodeIndex.toString());
		// --- Get the voltage level ----------------------
		String netCompName = nodeRowHashMapNodeA.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_Name)).toString();
		double voltageLevel = BundleHelper.parseDouble(nodeRowHashMapNodeA.get(PandaPowerNamingMap.getColumnName(ColumnName.Bus_VoltageLevel))) * 1000;
		boolean isTriPhaseConfig = PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel);

		// --- Get the profile and load factors -----------
		HashMap<String, Object> loadRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Load, PandaPowerNamingMap.getColumnName(ColumnName.Load_Bus), nodeIndex);
		String loadName = loadRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Load_Name)).toString();
		Double pLoadFactor = BundleHelper.parseDouble(loadRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Load_p_mw)));
		Double qLoadFactor = BundleHelper.parseDouble(loadRowHashMap.get(PandaPowerNamingMap.getColumnName(ColumnName.Load_q_mvar)));
		
		// --- Get the profile and load factors for PV ----
		HashMap<String, Object> pvRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.SIMBENCH_RES, "node", nodeIndex);
		String profilePV = pvRowHashMap==null ? null : pvRowHashMap.get("profile").toString();
		Double pLoadFactorPV = pvRowHashMap==null ? 0.0 : BundleHelper.parseDouble(pvRowHashMap.get("pRES"));
		Double qLoadFactorPV = pvRowHashMap==null ? 0.0 : BundleHelper.parseDouble(pvRowHashMap.get("qRES"));

				
		// --- Create ScheduleList ------------------------
		ScheduleList sl = this.createScheduleList("Profile " + loadName, netCompName, voltageLevel);
		Schedule schedule = null;
		if (pLoadFactor!=null && qLoadFactor!=null) {
			schedule = this.getScheduleOfProfile(loadName, pLoadFactor, qLoadFactor, profilePV, pLoadFactorPV, qLoadFactorPV, scheduleTimeRange, isTriPhaseConfig);
			sl.getSchedules().add(schedule);
		}
		return sl;
	}
	/**
	 * Return the ScheduleList that can be found through a load description in the specified data model row.
	 * @param rowSelected the selected row in the model
	 * @param scheduleTimeRange the ScheduleTimeRange to be considered
	 * @return the ScheduleList
	 */
	public ScheduleList getScheduleListByLoad(int rowSelected, ScheduleTimeRange scheduleTimeRange) {
		
		Integer loadIndex = this.getLoadIndex(rowSelected);

		// --- Get the profile and load factors -----------
		HashMap<String, Object> loadRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Load, "id", loadIndex);
		String profile = loadRowHashMap.get("profile").toString();
		double pLoadFactor = BundleHelper.parseDouble(loadRowHashMap.get("pLoad"));
		double qLoadFactor = BundleHelper.parseDouble(loadRowHashMap.get("qLoad"));
		
		// --- Get the profile and load factors for PV ----
		Integer nodeID = Integer.parseInt(loadRowHashMap.get("node").toString());
		HashMap<String, Object> pvRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.SIMBENCH_RES, "node", nodeID);
		String profilePV = pvRowHashMap==null ? null : pvRowHashMap.get("profile").toString();
		double pLoadFactorPV = pvRowHashMap==null ? 0.0 : BundleHelper.parseDouble(pvRowHashMap.get("pRES"));
		double qLoadFactorPV = pvRowHashMap==null ? 0.0 : BundleHelper.parseDouble(pvRowHashMap.get("qRES"));
		
		// --- Get the voltage level ----------------------
		HashMap<String, Object> nodeRowHashMapNodeA = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Bus, "id", nodeID);
		double voltageLevel = BundleHelper.parseDouble(nodeRowHashMapNodeA.get("vmR")) * 1000;
		boolean isTriPhaseConfig = PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel);
				
		// --- Create ScheduleList ------------------------
		Schedule schedule = this.getScheduleOfProfile(profile, pLoadFactor, qLoadFactor, profilePV, pLoadFactorPV, qLoadFactorPV, scheduleTimeRange, isTriPhaseConfig);
		ScheduleList sl = this.createScheduleList("Profile " + profile, null, voltageLevel);
		sl.getSchedules().add(schedule);
		return sl;
	}

	/**
	 * Creates a ScheduleList with the specified systemID.
	 *
	 * @param systemID the system ID to use
	 * @param netCompID the ID of the corresponding NetworkComponent
	 * @param voltageLevel the voltage level
	 * @return the schedule list
	 */
	private ScheduleList createScheduleList(String systemID, String netCompID, double voltageLevel) {
		
		ScheduleList sl = new ScheduleList();
		sl.setSystemID(systemID);
		sl.setNetworkID(netCompID);
		sl.setDescription("Load data imported for " + systemID + "");
		
		
		if (PandaPowerFileStore.isTriPhaseVoltageLevel(voltageLevel)==true) {
			// --------------------------------------------------------------------------
			// --- Define domain model and interface settings for THREE phases ----------
			// --------------------------------------------------------------------------
			double voltageLevelToSet = voltageLevel==400.0 ? 230.0 : NumberHelper.round(voltageLevel / Math.sqrt(3.0), 0);
			
			for (int i=0; i < INTERFACE_IDs_ElectricityTriPhase.length; i++) {
				
				// --- Active power ----------------------------------------
				String activeInterfaceID = INTERFACE_IDs_ElectricityTriPhase[i] + " P";
				DefaultDomainModelElectricity domainModelActivePower = new DefaultDomainModelElectricity();
				domainModelActivePower.setPowerType(PowerType.ActivePower);
				domainModelActivePower.setCurrentType(CurrentType.AC);
				domainModelActivePower.setPhase(Phase.values()[i+1]);
				domainModelActivePower.setFrequency(50);
				domainModelActivePower.setRatedVoltage(voltageLevelToSet);
				
				InterfaceSetting interfaceSettingActivePower = new InterfaceSetting();
				interfaceSettingActivePower.setInterfaceID(activeInterfaceID);
				interfaceSettingActivePower.setDomainModel(domainModelActivePower);
				interfaceSettingActivePower.setDomain(EnergyCarrier.ELECTRICITY.value());
				
				sl.getInterfaceSettings().add(interfaceSettingActivePower);
				
				// --- Reactive power -------------------------------------
				String reactiveInterfaceID = INTERFACE_IDs_ElectricityTriPhase[i] + " Q";
				DefaultDomainModelElectricity domainModelReactivePower = new DefaultDomainModelElectricity();
				domainModelReactivePower.setPowerType(PowerType.ReactivePower);
				domainModelReactivePower.setCurrentType(CurrentType.AC);
				domainModelReactivePower.setPhase(Phase.values()[i+1]);
				domainModelReactivePower.setFrequency(50);
				domainModelReactivePower.setRatedVoltage(voltageLevelToSet);
				
				InterfaceSetting interfaceSettingsReactivePower = new InterfaceSetting();
				interfaceSettingsReactivePower.setInterfaceID(reactiveInterfaceID);
				interfaceSettingsReactivePower.setDomainModel(domainModelReactivePower);
				interfaceSettingsReactivePower.setDomain(EnergyCarrier.ELECTRICITY.value());
				
				sl.getInterfaceSettings().add(interfaceSettingsReactivePower);
			}
			
		} else {
			// --------------------------------------------------------------------------
			// --- Define domain model and interface settings for a SINGLE phase --------
			// --------------------------------------------------------------------------

			// --- Active power ----------------------------------------
			String activeInterfaceID = INTERFACE_IDs_ElectricityUniPhase + " P";
			DefaultDomainModelElectricity domainModelActivePower = new DefaultDomainModelElectricity();
			domainModelActivePower.setPowerType(PowerType.ActivePower);
			domainModelActivePower.setCurrentType(CurrentType.AC);
			domainModelActivePower.setPhase(Phase.AllPhases);
			domainModelActivePower.setFrequency(50);
			domainModelActivePower.setRatedVoltage(voltageLevel);
			
			InterfaceSetting interfaceSettingActivePower = new InterfaceSetting();
			interfaceSettingActivePower.setInterfaceID(activeInterfaceID);
			interfaceSettingActivePower.setDomainModel(domainModelActivePower);
			interfaceSettingActivePower.setDomain(EnergyCarrier.ELECTRICITY.value());
			
			sl.getInterfaceSettings().add(interfaceSettingActivePower);
			
			// --- Reactive power -------------------------------------
			String reactiveInterfaceID = INTERFACE_IDs_ElectricityUniPhase + " Q";
			DefaultDomainModelElectricity domainModelReactivePower = new DefaultDomainModelElectricity();
			domainModelReactivePower.setPowerType(PowerType.ReactivePower);
			domainModelReactivePower.setCurrentType(CurrentType.AC);
			domainModelReactivePower.setPhase(Phase.AllPhases);
			domainModelReactivePower.setFrequency(50);
			domainModelReactivePower.setRatedVoltage(voltageLevel);
			
			InterfaceSetting interfaceSettingsReactivePower = new InterfaceSetting();
			interfaceSettingsReactivePower.setInterfaceID(reactiveInterfaceID);
			interfaceSettingsReactivePower.setDomainModel(domainModelReactivePower);
			interfaceSettingsReactivePower.setDomain(EnergyCarrier.ELECTRICITY.value());
			
			sl.getInterfaceSettings().add(interfaceSettingsReactivePower);
			
		}
		return sl;
	}
	
	/**
	 * Return a schedule for the specified profile .
	 *
	 * @param profile the profile
	 * @param pLoadFactor the load factor
	 * @param qLoadFactor the q load factor
	 * @param profilePV the PV profile
	 * @param pLoadFactorPV the p load factor for PV
	 * @param qLoadFactorPV the q load factor for PV
	 * @param scheduleTimeRange the ScheduleTimeRange to be considered
	 * @param isTreePhaseConfig the indicator if the Schedule requires to consider three phases 
	 * @return the schedule of profile
	 */
	private Schedule getScheduleOfProfile(String profile, double pLoadFactor, double qLoadFactor, String profilePV, double pLoadFactorPV, double qLoadFactorPV, ScheduleTimeRange scheduleTimeRange, boolean isTreePhaseConfig) {
		
		// --------------------------------------------------------------------
		// --- Get time series from load profile ------------------------------
		CsvDataController loadProfileCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.SIMBENCH_LoadProfile);
		Vector<Vector<Object>> loadProfileDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.SIMBENCH_LoadProfile);
		if (loadProfileCsvController==null || loadProfileDataVector==null) return null;
		
		
		String colNameTime = "time";
		String colNamePLoad = profile + "_pload";
		String colNameQLoad = profile + "_qload";
		
		int ciTime  = loadProfileCsvController.getDataModel().findColumn(colNameTime);
		int ciPLoad = loadProfileCsvController.getDataModel().findColumn(colNamePLoad);
		int ciQLoad = loadProfileCsvController.getDataModel().findColumn(colNameQLoad);
		
		// --------------------------------------------------------------------
		// --- Get time series from PV profile --------------------------------
		CsvDataController pvProfileCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.SIMBENCH_RESProfile);
		Vector<Vector<Object>> pvProfileDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.SIMBENCH_RESProfile);

		String colNamePLoadPV = profilePV;
		String colNameQLoadPV = profilePV;
		
		int ciPLoadPV = colNamePLoadPV==null ? -1 : pvProfileCsvController.getDataModel().findColumn(colNamePLoadPV);
		int ciQLoadPV = colNameQLoadPV==null ? -1 : pvProfileCsvController.getDataModel().findColumn(colNameQLoadPV);

		
		// --- Set initial variable state ------------------------------------- 
		long timeStampPrev = -1;
		TechnicalSystemStateEvaluation tssePrev = null;

		// --- Create the Schedule --------------------------------------------
		Schedule schedule = new Schedule();
		schedule.setStrategyClass(this.getClass().getSimpleName());
		List<TechnicalSystemStateEvaluation> tsseList = schedule.getTechnicalSystemStateList();
		
		// --- Avoid double time stamps ---------------------------------------
		HashSet<Long> timeStampHashSet = new HashSet<Long>();
		long timeFrom = 0;
		long timeTo   = 0; 

		// --- Get data rows for the profile ----------------------------------
		int iMax = loadProfileDataVector.size();
		rowLoop: for (int i = 0; i < iMax; i++) {

			Vector<Object> row = loadProfileDataVector.get(i);
			String stringTime = row.get(ciTime).toString();
			String stringPLoad = row.get(ciPLoad).toString();
			String stringQLoad = row.get(ciQLoad).toString();
			
			try {
				// --- Get the time stamp -------------------------------------
				long timeStamp = this.getTimeStampFromString(stringTime);

				// --- Set time from-to ---------------------------------------
				if (i==0) timeFrom = timeStamp;
				timeTo = Math.max(timeTo, timeStamp) ;
				
				// --- Avoid double time stamps -------------------------------
				if (timeStampHashSet.contains(timeStamp)==true) continue;
				timeStampHashSet.add(timeStamp);
				
				// --- Consider the ScheduleTimeRange? ------------------------
				if (scheduleTimeRange!=null && scheduleTimeRange.getRangeType()!=null) {
					
					// --- Ahead time? ----------------------------------------
					if (timeStamp < scheduleTimeRange.getTimeFrom()) continue;
					// --- Check RangeType ------------------------------------
					switch (scheduleTimeRange.getRangeType()) {
					case TimeRange: 
						if (timeStamp > scheduleTimeRange.getTimeTo()) {
							break rowLoop;
						}
						break;
					case StartTimeAndNumber:
						if (tsseList.size() >= scheduleTimeRange.getNumberOfSystemStates()) {
							break rowLoop;
						}
						break;
					}
				}
				
				// ------------------------------------------------------------
				// --- Calculate PV feed-in -----------------------------------
				// ------------------------------------------------------------
				double pPV = 0.0;
				double qPV = 0.0;
				if (profilePV!=null) {
					// --- Get the possible PV feed ---------------------------
					Vector<Object> rowPV = this.getPVLoadDataRow(pvProfileDataVector, timeTo, i);
					String stringPLoadPV = rowPV.get(ciPLoadPV).toString();
					String stringQLoadPV = rowPV.get(ciQLoadPV).toString();
					
					pPV = - (BundleHelper.parseDouble(stringPLoadPV) * pLoadFactorPV); 
					qPV = - (BundleHelper.parseDouble(stringQLoadPV) * qLoadFactorPV);
				}
				// ------------------------------------------------------------
				
				// --- Calculate over all power values ------------------------
				double pLoadMWAllPhases = (BundleHelper.parseDouble(stringPLoad) * pLoadFactor) + pPV;
				double qLoadMWAllPhases = (BundleHelper.parseDouble(stringQLoad) * qLoadFactor) + qPV;
				
				long durationMillis = 0;
				if (timeStampPrev>0) {
					durationMillis = timeStamp - timeStampPrev;
				}
				
				// --- Create TechnicalSystemStateEvaluation ------------------
				TechnicalSystemStateEvaluation tsse = this.createTechnicalSystemStateEvaluation(timeStamp, durationMillis, pLoadMWAllPhases, qLoadMWAllPhases, isTreePhaseConfig, tssePrev); 
				tsseList.add(0, tsse);
			
				// --- Remind for next iteration ------------------------------
				timeStampPrev = timeStamp;
				tssePrev = tsse;
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} // end for

		
		// --- Finally set the state time range ------------------------------- 
		schedule.setStateTimeFrom(timeFrom);
		schedule.setStateTimeTo(timeTo);
		
		// --- Create the usual tree structure of Schedules -------------------
		ScheduleList_StorageHandler.convertToTreeSchedule(schedule);
		
		return schedule;
	}
	
	/**
	 * Return the PV load data row.
	 *
	 * @param pvProfileDataVector the PV profile data vector
	 * @param currentTime the current time
	 * @param searchStartIndex the search start index
	 * @return the PV load data row
	 */
	private Vector<Object> getPVLoadDataRow(Vector<Vector<Object>> pvProfileDataVector, long currentTime, int searchStartIndex) {
		
		Vector<Object> pvRow = null;
		
		// --- Get the initial data row that corresponds to the current search index ----
		Vector<Object> dataRowPV = pvProfileDataVector.get(searchStartIndex);
		String stringTime = dataRowPV.get(0).toString();
		long timeStamp = this.getTimeStampFromString(stringTime);
		
		// --- Check if we have a match -------------------------------------------------
		if (timeStamp!=currentTime) {
			
			System.out.println("Stop");
			
		} else {
			pvRow = dataRowPV;
		}
		return pvRow;
	}
	
	
	/**
	 * Creates the technical system state evaluation.
	 *
	 * @param time the time
	 * @param duration the duration
	 * @param pLoadMWAllPhases the P load in MW for all phases
	 * @param qLoadMWAllPhases the q load MW all phases
	 * @param isTriPhaseConfig the is tri phase config
	 * @param tssePrev the previous {@link TechnicalSystemStateEvaluation}
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation createTechnicalSystemStateEvaluation(long time, long duration, double pLoadMWAllPhases, double qLoadMWAllPhases, boolean isTriPhaseConfig, TechnicalSystemStateEvaluation tssePrev) {
		
		TechnicalSystemStateEvaluation tsse = new TechnicalSystemStateEvaluation();
		tsse.setConfigID(DEFAULT_TSSE_CONFIG_ID);
		tsse.setStateID(DEFAULT_TSSE_STATE_ID);
		tsse.setGlobalTime(time);
		tsse.setStateTime(duration);

		double pLoadkWAllPhases = pLoadMWAllPhases * 1000.0;
		double qLoadkWAllPhases = qLoadMWAllPhases * 1000.0;
		
		if (isTriPhaseConfig==true) {
			// --- Calculate to single phase ------------------
			double activePower   = pLoadkWAllPhases / 3.0;
			double reactivePower = qLoadkWAllPhases / 3.0;
			
			// --- Iterate over interfaces --------------------
			for (int i=0; i < INTERFACE_IDs_ElectricityTriPhase.length; i++) {
				// --- Active power ---------------------------
				String activeInterfaceID = INTERFACE_IDs_ElectricityTriPhase[i] + " P";
				EnergyAmount cumActive = this.getCumulatedEnergyAmount(tssePrev, activeInterfaceID);
				UsageOfInterfaceEnergy uoiActive = this.createUsageOfInterfaces(activeInterfaceID, duration, activePower, cumActive);
				tsse.getUsageOfInterfaces().add(uoiActive);
				// --- Reactive power -------------------------
				String reactiveInterfaceID = INTERFACE_IDs_ElectricityTriPhase[i] + " Q";
				EnergyAmount cumReactive = this.getCumulatedEnergyAmount(tssePrev, reactiveInterfaceID);
				UsageOfInterfaceEnergy uoiReactive = this.createUsageOfInterfaces(reactiveInterfaceID, duration, reactivePower, cumReactive);
				tsse.getUsageOfInterfaces().add(uoiReactive);
			}
		} else {
		
			// --- Active power ---------------------------
			String activeInterfaceID = INTERFACE_IDs_ElectricityUniPhase + " P";
			EnergyAmount cumActive = this.getCumulatedEnergyAmount(tssePrev, activeInterfaceID);
			UsageOfInterfaceEnergy uoiActive = this.createUsageOfInterfaces(activeInterfaceID, duration, pLoadkWAllPhases, cumActive);
			tsse.getUsageOfInterfaces().add(uoiActive);
			// --- Reactive power -------------------------
			String reactiveInterfaceID = INTERFACE_IDs_ElectricityUniPhase + " Q";
			EnergyAmount cumReactive = this.getCumulatedEnergyAmount(tssePrev, reactiveInterfaceID);
			UsageOfInterfaceEnergy uoiReactive = this.createUsageOfInterfaces(reactiveInterfaceID, duration, qLoadkWAllPhases, cumReactive);
			tsse.getUsageOfInterfaces().add(uoiReactive);
			
			
		}
		return tsse;
	}
	
	/**
	 * Return the cumulated energy amount for the specified interface.
	 *
	 * @param tssePrev the previous {@link TechnicalSystemStateEvaluation} that serves as base for the calculations.
	 * @param interfaceID the interface ID
	 * @return the cumulated energy amount
	 */
	private EnergyAmount getCumulatedEnergyAmount(TechnicalSystemStateEvaluation tssePrev, String interfaceID) {
		
		EnergyAmount eaCum = null;
		if (tssePrev!=null) {
			for (int i = 0; i < tssePrev.getUsageOfInterfaces().size(); i++) {
				AbstractUsageOfInterface uoi = tssePrev.getUsageOfInterfaces().get(i);
				if (uoi instanceof UsageOfInterfaceEnergy) {
					UsageOfInterfaceEnergy uoiEnergy = (UsageOfInterfaceEnergy) uoi;
					if (uoiEnergy.getInterfaceID().equals(interfaceID)==true) {
						eaCum = uoiEnergy.getEnergyAmountCumulated();
						break;
					}
				}
			}
		}
		return eaCum;
	}
	
	/**
	 * Create a UsageOfInterfaces instance with energy flow and cumulated energy amount.
	 *
	 * @param interfaceID The interface ID
	 * @param durationMillis the duration in milliseconds
	 * @param energyFlowValueKW The energy flow
	 * @param prevEnergyAmountCum the previous energy amount cumulated
	 * @return the UsageOfInterfaces instance
	 */
	private UsageOfInterfaceEnergy createUsageOfInterfaces(String interfaceID, long durationMillis, double energyFlowValueKW, EnergyAmount prevEnergyAmountCum){
		
		// --- Cumulate energy amounts --------------------
		double durationMillisInHours = UnitConverter.convertDuration(durationMillis, TIME_UNIT_FOR_ENERGY_AMOUNTS);
		double deltaEnergyAmount = energyFlowValueKW * durationMillisInHours;
		double cumulated = deltaEnergyAmount;
		if (prevEnergyAmountCum!=null){
			cumulated += prevEnergyAmountCum.getValue();
		}
	
		// --- Define energy flow -------------------------
		EnergyFlowInWatt eFlow = new EnergyFlowInWatt();
		eFlow.setValue(energyFlowValueKW);
		eFlow.setSIPrefix(SI_PREFIX_FOR_ENERGY_FLOWS);
		
		// --- Prepare energy amount instance -------------
		EnergyAmount eaCumulated = new EnergyAmount();
		eaCumulated.setSIPrefix(SI_PREFIX_FOR_ENERGY_AMOUNTS);
		eaCumulated.setTimeUnit(TIME_UNIT_FOR_ENERGY_AMOUNTS);
		eaCumulated.setValue(cumulated);
		
		// --- Create the usage of energy -----------------
		UsageOfInterfaceEnergy uoi = new UsageOfInterfaceEnergy();
		uoi.setInterfaceID(interfaceID);
		uoi.setEnergyFlow(eFlow);
		uoi.setEnergyAmountCumulated(eaCumulated);
		return uoi;
	}
	
	
	/**
	 * Return the node index for the specified row of table nodes. 
	 * @param rowIndex within the model
	 * @return the nodeID
	 */
	private Integer getNodeIndex(int rowIndex) {
		
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.PANDA_Bus);
		Vector<Vector<Object>> nodeDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.PANDA_Bus);
		
		int ciIndex = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Bus_Index));
		Vector<Object> dataRow = nodeDataVector.get(rowIndex);
		return (Integer)dataRow.get(ciIndex);
	}
	
	/**
	 * Return the load index for the specified row of table load. 
	 * @param rowIndex within the model
	 * @return the loadID
	 */
	private Integer getLoadIndex(int rowIndex) {
		
		CsvDataController nodeCsvController = this.getCsvDataControllerOfCsvFile(PandaPowerFileStore.PANDA_Load);
		Vector<Vector<Object>> nodeDataVector = this.getDataVectorOfCsvFile(PandaPowerFileStore.PANDA_Load);
		
		int ciID = nodeCsvController.getDataModel().findColumn(PandaPowerNamingMap.getColumnName(ColumnName.Load_Index));
		Vector<Object> dataRow = nodeDataVector.get(rowIndex);
		return (Integer) dataRow.get(ciID);
	}
	
	/**
	 * Check if the current node selection is a cable cabinet
	 * @param rowIndex the row index in the node table 
	 * @return true if the node selection is a cable cabinet
	 */
	public boolean isCableCabinetNodeSelection(int rowIndex) {
		HashMap<String, Object> loadRowHashMap = this.getDataRowHashMap(PandaPowerFileStore.PANDA_Load, "node", this.getNodeIndex(rowIndex));
		if (loadRowHashMap==null) {
			return true;
		}
		return false;
	}
	

	// --------------------------------------------------------------------------------------------
	// --- From here: Method to get the time range of the data ------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Return the time range of the currently selected data.
	 * @return the time range of data
	 */
	public TimeRange getTimeRangeOfData() {
		
		// --- Get start and end time as string each ------ 
		CsvDataController csvController = getCsvDataControllerOfCsvFile(PandaPowerFileStore.SIMBENCH_LoadProfile);
		if (csvController==null) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Unknown type for LoadProfiles, not able to evaluate the data time range!");
			return null;
		}
		
		String timeStringStart = (String) csvController.getDataModel().getValueAt(0, 0);
		String timeStringEnd = (String) csvController.getDataModel().getValueAt(csvController.getDataModel().getRowCount()-1, 0);
		
		long timeFrom = this.getTimeStampFromString(timeStringStart);
		long timeTo   = this.getTimeStampFromString(timeStringEnd);
		
		// --- Define the return instance -----------------
		TimeRange timeRange = new TimeRange();
		timeRange.setTimeFrom(timeFrom);
		timeRange.setTimeTo(timeTo);
		return timeRange;
	}
	
	
	// --------------------------------------------------------------------------------------------
	// --- From here: Some general help methods ---------------------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * Returns the local date formatter.
	 * @return the date formatter
	 */
	public SimpleDateFormat getDateFormatter() {
		if (dateFormatter==null) {
			dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		}
		return dateFormatter;
	}
	/**
	 * Returns the Date instance from the specified string by using the local date formatter.
	 *
	 * @param dateString the date string
	 * @return the date from string
	 * @see #getDateFormatter()
	 */
	public Date getDateFromString(String dateString) {
		if (dateString!=null && dateString.isEmpty()==false) {
			try {
				return this.getDateFormatter().parse(dateString);
			} catch (ParseException pEx) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error while parsing '" + dateString + "' to Date insatnce:");
				pEx.printStackTrace();
			}
		}
		return null;
	}
	/**
	 * Returns the time stamp as long from the specified string by using the local date formatter.
	 * If null, an empty or an invalid string is specified, the method will return -1.
	 *
	 * @param dateString the date string
	 * @return the time stamp from string
	 * @see #getDateFormatter()
	 */
	public long getTimeStampFromString(String dateString) {
		Date date = this.getDateFromString(dateString);
		if (date!=null) {
			return date.getTime();
		}
		return -1;
	}
	
	/**
	 * Return a data row as HashMap, where the key is the column name and the value the row value.
	 *
	 * @param ppTable the PandaPower table
	 * @param keyColumnName the key column name
	 * @param keyValue the key value to search for
	 * @return the data row as HashMap
	 */
	private HashMap<String, Object> getDataRowHashMap(String ppTable, String keyColumnName, Integer keyValue) {
		return this.getDataRowHashMap(ppTable, keyColumnName, keyValue.toString());
	}
	/**
	 * Return a data row as HashMap, where the key is the column name and the value the row value.
	 *
	 * @param ppTable the PandaPower table name
	 * @param keyColumnName the key column name
	 * @param keyValue the key value to search for
	 * @return the data row as HashMap
	 */
	private HashMap<String, Object> getDataRowHashMap(String ppTable, String keyColumnName, String keyValue) {
		
		HashMap<String, Object> dataRowHashMap = null;
		
		CsvDataController csvController = this.getCsvDataControllerOfCsvFile(ppTable);
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
						Object value = csvController.getDataModel().getValueAt(dataRowIndex, i);
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
		return PandaPowerFileStore.getInstance().getCsvDataController().get(csvFileName);
	}
	
}
