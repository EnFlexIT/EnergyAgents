package de.enflexit.ea.core.simulation.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;

import agentgui.simulationService.time.TimeModelDateBased;
import de.enflexit.common.SerialClone;
import de.enflexit.eom.awb.adapter.ui.EomToolsHelper;
import energy.DomainSettings;
import energy.OptionModelController;
import energy.optionModel.AbstractDomainModel;
import energy.optionModel.InterfaceSetting;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalInterface;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.optionModel.TechnicalSystemStateTime;
import energy.optionModel.TimeUnit;
import energy.optionModel.UsageOfInterfaceEnergy;
import energy.optionModel.UsageOfInterfaceGood;
import energy.optionModel.AbstractUsageOfInterface;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.GoodFlow;

/**
 * The Class NoSystemScheduleListCreator is used on the fly by the {@link SimulationManager} to
 * create ScheduleLists for those NetorkComponents that are using the EOM but have no EOM model specified.
 * The ScheduleList's that will be created will have proper InterfaceSetings (according the neighbors
 * found in the NetworkModel) and energy or good flows with a value of 0. 
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class NoSystemScheduleListCreator {

	private NetworkModel networkModel;
	private TimeModelDateBased timeModelDateBased;
	
	private List<DataModelNetworkElement> networkElementsWithoutEomModel;
	
	
	/**
	 * Instantiates a new 'no-system' => ScheduleList-Creator.
	 *
	 * @param networkModel the network model
	 * @param dbTimeModel the date-based time model 
	 */
	public NoSystemScheduleListCreator(NetworkModel networkModel, TimeModelDateBased dbTimeModel) {
		this.setNetworkModel(networkModel);
		this.setTimeModel(dbTimeModel);
		this.createScheduleListForNotDefinedSystems();
	}
	/**
	 * Creates the {@link ScheduleList}s for all not defined systems.
	 */
	private void createScheduleListForNotDefinedSystems() {
		
		List<DataModelNetworkElement> sysWithoutSL = this.getNetworkElementsWithoutEomModel();
		for (int i = 0; i < sysWithoutSL.size(); i++) {

			try {
				// --- Get the DataModelNetworkElement --------------
				DataModelNetworkElement networkElement = sysWithoutSL.get(i);
				
				// --- Create the ScheduleList ----------------------
				ScheduleList sl = this.createScheduleList(networkElement.getId());
				Schedule schedule = this.createSchedule();
				List<InterfaceSetting> interfaceSettings = this.createInterfaceSetting(networkElement);
				List<AbstractUsageOfInterface> uoiList = this.createUsageOfInterfaces(interfaceSettings);
				TechnicalSystemStateEvaluation tsse = this.createTechnicalSystemStateEvaluation();
				
				// --- Merge together -------------------------------
				tsse.getUsageOfInterfaces().addAll(uoiList);
				schedule.setTechnicalSystemStateEvaluation(tsse);
				sl.getSchedules().add(schedule);
				sl.getInterfaceSettings().addAll(interfaceSettings);
				
				// --- Set to the network element -------------------
				networkElement.setDataModel(sl);
				System.out.println("[" + Thread.currentThread().getName() + "][" + this.getClass().getSimpleName() + "] Created ScheduleList with zero energy or good flow for " + networkElement.getClass().getSimpleName() + " '" + networkElement.getId() + "'!");
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	public void setNetworkModel(NetworkModel networkModel) {
		this.networkModel = networkModel;
	}
	
	public TimeModelDateBased getTimeModel() {
		return timeModelDateBased;
	}
	public void setTimeModel(TimeModelDateBased dbTimeModel) {
		this.timeModelDateBased = dbTimeModel;
	}
	
	/**
	 * Will search the current NetworkModel and check for network elements that are configured to use the 
	 * EOM, but where no model is specified yet.
	 * @return the network elements without eom model
	 */
	private List<DataModelNetworkElement> getNetworkElementsWithoutEomModel() {
		if (networkElementsWithoutEomModel==null) {
			networkElementsWithoutEomModel = new ArrayList<>();
			
			List<DataModelNetworkElement> allElements = this.getNetworkModel().getDataModelNetworkElementList();
			for (int i = 0; i < allElements.size(); i++) {
				// --- Check each DataModelNetworkElement ----------- 
				DataModelNetworkElement networkElement = allElements.get(i);
				if (networkElement.getDataModel()==null && this.isEomDataModelNetworkElement(networkElement)==true) {
					networkElementsWithoutEomModel.add(networkElement);
				}
			}
		}
		return networkElementsWithoutEomModel;
	}
	/**
	 * Checks if the specified DataModelNetworkElement uses the EOM.
	 * @param networkElement the network element
	 * @return true, if the network element uses the EOM
	 */
	private boolean isEomDataModelNetworkElement(DataModelNetworkElement networkElement) {
		NetworkComponentAdapter nca = this.getNetworkModel().getNetworkComponentAdapter(null, networkElement);
		return EomToolsHelper.isEomUsingNetworkComponentAdapter(nca);
	}

	// ----------------------------------------------------------------------------------
	// --- From here, the method to create UsageOfInterfaces (flows) can be found -------
	// ----------------------------------------------------------------------------------	
	/**
	 * Creates a zero energy or good flows for the specified interface settings.
	 *
	 * @param interfaceSettings the interface settings
	 * @return the list zero energy or good flows
	 */
	private List<AbstractUsageOfInterface> createUsageOfInterfaces(List<InterfaceSetting> interfaceSettings) {
		
		List<AbstractUsageOfInterface> uoiList = new ArrayList<>();
		
		for (int i = 0; i < interfaceSettings.size(); i++) {
			InterfaceSetting intSetting = interfaceSettings.get(i);
			if (DomainSettings.isEnergyCarrier(intSetting.getDomain())==true) {
				// --- Energy interface -----------------------------
				EnergyFlowInWatt efiw = new EnergyFlowInWatt();
				efiw.setSIPrefix(EnergyUnitFactorPrefixSI.NONE_0);
				efiw.setValue(0);
				
				UsageOfInterfaceEnergy uoiEnergy = new UsageOfInterfaceEnergy();
				uoiEnergy.setInterfaceID(intSetting.getInterfaceID());
				uoiEnergy.setEnergyFlow(efiw);
				
				uoiList.add(uoiEnergy);
				
			} else {
				// --- Good Interface -------------------------------
				GoodFlow gf = new GoodFlow();
				gf.setTimeUnit(TimeUnit.SECOND_S);
				gf.setValue(0);
				
				UsageOfInterfaceGood uoiGood = new UsageOfInterfaceGood();
				uoiGood.setInterfaceID(intSetting.getInterfaceID());
				uoiGood.setGoodFlow(gf);
				
				uoiList.add(uoiGood);
			}
		}
		return uoiList;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here methods to create suitable interface settings can be found --------- 
	// ----------------------------------------------------------------------------------	
	/**
	 * Creates the interface setting according to the elements neighborhood.
	 *
	 * @param networkElement the network element
	 * @return the list
	 */
	private List<InterfaceSetting> createInterfaceSetting(DataModelNetworkElement networkElement) {
		
		List<InterfaceSetting> isList = new ArrayList<>();
		
		// --- Get EOM neighbors --------------------------------------------------------
		Vector<NetworkComponent> eomNeighbors = this.getEomNetworkComponentNeighbors(networkElement);
		for (int i = 0; i < eomNeighbors.size(); i++) {
			NetworkComponent neighbor = eomNeighbors.get(i);
			// --- Get interface settings from the actual neighbor NetworkComponent -----
			List<InterfaceSetting> intSettingsNeighbor = null;
			if (neighbor.getDataModel() instanceof  ScheduleList) {
				ScheduleList sl = (ScheduleList) neighbor.getDataModel();
				intSettingsNeighbor = this.getInterfaceSettingFromScheduleList(sl);
			} else if (neighbor.getDataModel() instanceof TechnicalSystem) {
				TechnicalSystem ts = (TechnicalSystem) neighbor.getDataModel();
				intSettingsNeighbor = this.getInterfaceSettingFromTechnicalSystem(ts);
			} else if (neighbor.getDataModel() instanceof TechnicalSystemGroup) {
				TechnicalSystemGroup tsg = (TechnicalSystemGroup) neighbor.getDataModel();
				intSettingsNeighbor = this.getInterfaceSettingFromTechnicalSystemGroup(tsg);
			}
			
			// --- Merge new InterfaceSetting into the result list ----------------------
			if (intSettingsNeighbor!=null && intSettingsNeighbor.size()>0) {
				for (int j = 0; j < intSettingsNeighbor.size(); j++) {
					InterfaceSetting intSetting = intSettingsNeighbor.get(j);
					if (this.isInterfaceSettingInList(intSetting, isList)==false) {
						isList.add(intSetting);
					}
				}
			}
		}
		return isList;
	}
	/**
	 * Checks if the specified type of InterfaceSetting is already in the list. 
	 * For this, the domain and the domain model will be compared in detail. 
	 *
	 * @param isToCheck the InterfaceSetting to check
	 * @param isList the list of InterfaceSetting
	 * @return true, if the type of InterfaceSetting in already in the specified list
	 */
	private boolean isInterfaceSettingInList(InterfaceSetting isToCheck, List<InterfaceSetting> isList) {
		
		for (int i = 0; i < isList.size(); i++) {
			InterfaceSetting isToComp = isList.get(i);
			if (isToCheck.getDomain().equals(isToComp.getDomain())==true && isToCheck.getDomainModel().equals(isToComp.getDomainModel())==true) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a copy of the interface setting from the specified ScheduleList.
	 * @param sl the ScheduleList
	 * @return the interface setting from schedule list
	 */
	private List<InterfaceSetting> getInterfaceSettingFromScheduleList(ScheduleList sl) {
		return SerialClone.clone(sl.getInterfaceSettings());
	}
	/**
	 * Returns a copy of the interface setting from the specified TechnicalSystem.
	 *
	 * @param ts the TechnicalSystem
	 * @return the interface setting from schedule list
	 */
	private List<InterfaceSetting> getInterfaceSettingFromTechnicalSystem(TechnicalSystem ts) {
		
		List<InterfaceSetting> intSettings = new ArrayList<>();
		if (ts!=null) {
			// --- Get the TechnicalInterfaceConfiguration ----------
			TechnicalInterfaceConfiguration tic = this.getTechnicalInterfaceConfigurationToUse(ts);
			if (tic!=null) {
				// --- Get the TechnicalInterfaces ------------------
				for (int i = 0; i < tic.getTechnicalInterfaces().size(); i++) {
					TechnicalInterface ti = tic.getTechnicalInterfaces().get(i);
					String interfaceID = ti.getInterfaceID();
					String domain = ti.getDomain();
					AbstractDomainModel adm = ti.getDomainModel();
					
					// --- Create and add to result list ------------
					InterfaceSetting intSet = new InterfaceSetting();
					intSet.setInterfaceID(interfaceID);
					intSet.setDomain(domain);
					intSet.setDomainModel(adm);
					intSettings.add(intSet);
				}
			}
		}
		return intSettings;
	}
	/**
	 * Returns the TechnicalInterfaceConfiguration to be used to evaluate .
	 *
	 * @param ts the TechnicalSystem
	 * @return the technical interface configuration to use
	 */
	private TechnicalInterfaceConfiguration getTechnicalInterfaceConfigurationToUse(TechnicalSystem ts) {
		
		if (ts==null) return null;
		
		TechnicalInterfaceConfiguration tic = null;
		
		// --- First Check the evaluation settings --------------------------------------
		if (ts.getEvaluationSettings()!=null && ts.getEvaluationSettings().getEvaluationStateList().size()>0) {
			TechnicalSystemStateTime tsst = ts.getEvaluationSettings().getEvaluationStateList().get(0);
			String configID = null;
			if (tsst!=null && tsst instanceof TechnicalSystemState) {
				TechnicalSystemState tss = (TechnicalSystemState) tsst;
				configID = (tss.getConfigID()!=null && tss.getConfigID().isEmpty()==false) ? tss.getConfigID() : null;
			}
			tic = new OptionModelController().getTechnicalInterfaceConfiguration(configID);
		}
		
		if (tic==null && ts.getInterfaceConfigurations().size()>0) {
			// --- Backup solution: directly access base model and use default ---------- 
			tic = ts.getInterfaceConfigurations().get(0);
		}
		return tic;
	}
	/**
	 * Returns a copy of the interface setting from the specified TechnicalSystemGroup.
	 *
	 * @param tsg the TechnicalSystemGroup
	 * @return the interface setting from schedule list
	 */
	private List<InterfaceSetting> getInterfaceSettingFromTechnicalSystemGroup(TechnicalSystemGroup tsg) {
		if (tsg!=null) {
			this.getInterfaceSettingFromTechnicalSystem(tsg.getTechnicalSystem());
		}
		return this.getInterfaceSettingFromTechnicalSystem(null);
	}

	
	// ----------------------------------------------------------------------------------
	// --- From here, internal neighbor search methods can be found --------------------- 
	// ----------------------------------------------------------------------------------	
	/**
	 * Returns the list of next neighbor {@link NetworkComponent}s that are using the EOM.
	 *
	 * @param networkElement the network element
	 * @return the EOM network component neighbors
	 */
	private Vector<NetworkComponent> getEomNetworkComponentNeighbors(DataModelNetworkElement networkElement) {
		
		// --- Get the NetworkComponent to work on --------
		NetworkComponent netComp = null;
		if (networkElement instanceof GraphNode) {
			netComp = this.getNetworkModel().getDistributionNode((GraphNode)networkElement);
		} else if (networkElement instanceof NetworkComponent) {
			netComp = (NetworkComponent) networkElement;
		}

		// --- Define search Vector -----------------------
		Vector<NetworkComponent> netCompVectorSearch = new Vector<>();
		netCompVectorSearch.add(netComp);
		
		// --- Search in the neighborhood -----------------
		Vector<NetworkComponent> eomNetCompsNeighbours = null; 
		while (eomNetCompsNeighbours==null || eomNetCompsNeighbours.size()==0) {
			
			// --- Get neighbors of current round --------- 
			Vector<NetworkComponent> netCompVectorNeighbours = this.getNetworkModel().getNeighbourNetworkComponents(netCompVectorSearch);
			if (netCompVectorNeighbours.size() == netCompVectorSearch.size()) {
				break; // No new results => leave ---------
			} else {
				netCompVectorSearch = netCompVectorNeighbours;
			}
			// --- Search the components for EOM models ---
			eomNetCompsNeighbours = this.filterForEomNetworkComponents(netCompVectorSearch);
		}
		
		// --- Found neighbors ----------------------------
		return eomNetCompsNeighbours;
	}
	/**
	 * Filters the specified NetworkComponents vector for those who using the EOM.
	 * @param netCompVector the NetworkComponent vector
	 * @return the vector
	 */
	private Vector<NetworkComponent> filterForEomNetworkComponents(Vector<NetworkComponent> netCompVector) {
		
		Vector<NetworkComponent> netCompsFiltered = new Vector<>();
		for (int i = 0; i < netCompVector.size(); i++) {
			NetworkComponent netComp = netCompVector.get(i);
			if (netComp.getDataModel()!=null && this.isEomDataModelNetworkElement(netComp)==true) {
				netCompsFiltered.add(netComp);
			}
		}
		return netCompsFiltered;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here factory methods to create ScheduleList parts can be found ---------- 
	// ----------------------------------------------------------------------------------	
	/**
	 * Creates the ScheduleList.
	 * @return the schedule list
	 */
	private ScheduleList createScheduleList(String id) {
		
		ScheduleList sl = new ScheduleList();
		sl.setSystemID("NoSystemDefinitionFound");
		sl.setNetworkID(id);
		return sl;
	}
	/**
	 * Creates a schedule.
	 * @return the schedule
	 */
	private Schedule createSchedule() {
		
		Schedule schedule = new Schedule();
		schedule.setSourceThread(Thread.currentThread().getName());
		schedule.setStrategyClass(this.getClass().getSimpleName());
		return schedule;
	}
	/**
	 * Creates the single TechnicalSystemStateEvaluation for the whole simulation time range.
	 * @return the technical system state evaluation for the whole system state
	 */
	private TechnicalSystemStateEvaluation createTechnicalSystemStateEvaluation() {
		
		TimeModelDateBased tmdb = this.getTimeModel();
		long timeFrom = tmdb.getTimeStart();
		long timeTo   = tmdb.getTimeStop();
		long duration = timeTo - timeFrom;
		
		TechnicalSystemStateEvaluation tsse = new TechnicalSystemStateEvaluation();
		tsse.setConfigID("NoSystemConfig");
		tsse.setStateID("NoSystemState");
		tsse.setGlobalTime(timeTo);
		tsse.setStateTime(duration);
		
		return tsse;
	}
	
}
