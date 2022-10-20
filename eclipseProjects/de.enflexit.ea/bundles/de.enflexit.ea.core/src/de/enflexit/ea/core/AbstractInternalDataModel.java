package de.enflexit.ea.core;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.common.Observable;
import de.enflexit.ea.core.behaviour.PlatformUpdateBehaviour;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.dataModel.DirectoryHelper;
import de.enflexit.ea.core.dataModel.PlatformUpdater;
import de.enflexit.ea.core.dataModel.DirectoryHelper.DirectoryType;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.deployment.AgentSpecifier;
import de.enflexit.ea.core.dataModel.deployment.DeploymentGroupsHelper;
import de.enflexit.ea.core.dataModel.deployment.SetupExtension;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookSearchFilter;
import de.enflexit.ea.core.monitoring.IOListFilterForLogging;
import de.enflexit.jade.phonebook.AbstractPhoneBookEntry;
import de.enflexit.jade.phonebook.PhoneBook;
import de.enflexit.jade.phonebook.PhoneBookEvent;
import de.enflexit.jade.phonebook.PhoneBookListener;
import energy.FixedVariableList;
import energy.OptionModelController;
import energy.optionModel.TechnicalSystem;
import energy.persistence.TechnicalSystem_BundleLoader.BundleModel;
import energy.schedule.ScheduleController;
import energygroup.GroupController;
import jade.core.AID;
import jade.lang.acl.ACLMessage;


/**
 * The abstract Class AbstractInternalDataModel represents  
 * the whole internal data model of the corresponding agent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractInternalDataModel<GenericPhoneBookEntry extends EnergyAgentPhoneBookEntry> extends Observable implements Serializable, PhoneBookListener {

	private static final long serialVersionUID = 8589606262871989270L;
	
	/** The Enumeration of elements that could have been changed. */
	public static enum CHANGED {
		NETWORK_MODEL,
		NETWORK_COMPONENT,
		MEASUREMENTS_FROM_SYSTEM,
		PHONE_BOOK,
		OWN_PHONEBOOK_ENTRY_UPDATED
	}
	
	/** The Enumeration of ControlledSystemType's. */
	public static enum ControlledSystemType {
		None,
		TechnicalSystem,
		TechnicalSystemGroup
	}
	
	/**
	 * Possible logging modes.<br>
	 * ON_NEW_MEASUREMENT: Every new measurement will be logged<br>
	 * ON_ANY_CHANGE: New measurements with unchanged values will be ignored<br>
	 * ON_SIGNIFICANT_CHANGE: Changes below a certain threshold will be ignored. Thresholds can be configured using 
	 * the {@link IOListFilterForLogging}'s setGeneralThreshold() and addSpecificThresholdForVariable() methods
	 */
	public static enum LoggingMode{
		ON_NEW_MEASUREMENT,
		ON_VALUE_CHANGE
	}
	
	protected transient AbstractEnergyAgent energyAgent;

	private HyGridAbstractEnvironmentModel hyGridAbstractEnvironmentModel;
	private NetworkModel networkModel;
	private NetworkComponent networkComponent;

	private ScheduleController scheduleController;
	private OptionModelController optionModelController;
	private GroupController groupController;
 
	private ControlledSystemType controlledSystemType = ControlledSystemType.None;
	
	private FixedVariableList fixedVariableListMeasurements;

	private PhoneBook phoneBook;
	private GenericPhoneBookEntry myPhoneBookEntry;
	
	private AID centralAgentAID;
	private CeaConfigModel ceaConfigModel;
	
	private IOListFilterForLogging loggingFilter;
	private LoggingMode loggingMode = LoggingMode.ON_NEW_MEASUREMENT;
	
	private ACLMessage fieldDataRequestMessage;
	
	/**
	 * Instantiates the internal data model for an AbstractEnergyAgent.
	 * @param energyAgent the actual instance of the owning Energy Agent
	 */
	public AbstractInternalDataModel(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
	}
	/**
	 * Sets this object changed and notifies observer.
	 * @param reason the concrete reason why the model changed
	 */
	protected void setChangedAndNotify(Object reason) {
		this.setChanged();
		this.notifyObservers(reason);		
	}
	
	
	/**
	 * Sets the HyGrid abstract environment model.
	 * @param hyGridAbstractEnvironmentModel the new {@link HyGridAbstractEnvironmentModel}
	 */
	public void setHyGridAbstractEnvironmentModel(HyGridAbstractEnvironmentModel hyGridAbstractEnvironmentModel) {
		this.hyGridAbstractEnvironmentModel = hyGridAbstractEnvironmentModel;
	}
	/**
	 * Returns the HyGridAbstractEnvironmentModel.
	 * @return the HyGridAbstractEnvironmentModel 
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return hyGridAbstractEnvironmentModel;
	}

	
	/**
	 * Sets the network model and will automatically determine the NetworkComponent.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.setNetworkModel(networkModel, true);
	}
	/**
	 * Sets the network model and can additionally determine the NetworkComponent.
	 *
	 * @param networkModel the new network model
	 * @param isDetermineNetworkComponent set true, if the NetworkComponent should be determined automatically
	 */
	public void setNetworkModel(NetworkModel networkModel, boolean isDetermineNetworkComponent) {
		this.networkModel = networkModel;
		this.setChangedAndNotify(CHANGED.NETWORK_MODEL);
		if (isDetermineNetworkComponent==true && this.networkModel!=null) {
			this.setNetworkComponent(this.networkModel.getNetworkComponent(this.energyAgent.getLocalName()));
		}
	}
	/**
	 * Returns the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	
	/**
	 * Sets the NetworkComponent.
	 * @param newNetworkComponent the new NetworkComponent
	 */
	public void setNetworkComponent(NetworkComponent newNetworkComponent) {
		if (this.networkComponent!=newNetworkComponent) {
			this.networkComponent = newNetworkComponent;
			this.setChangedAndNotify(CHANGED.NETWORK_COMPONENT);	
		}
	}
	/**
	 * Returns the current NetworkComponent.
	 * @return the NetworkComponent
	 */
	public NetworkComponent getNetworkComponent() {
		if (this.networkComponent==null && this.networkModel!=null) {
			this.setNetworkComponent(this.networkModel.getNetworkComponent(this.energyAgent.getLocalName()));
		}
		return this.networkComponent;
	}
	
	/**
	 * Returns the ScheduleController for the underlying system.
	 * @return the ScheduleController
	 */
	public ScheduleController getScheduleController() {
		if (scheduleController == null) {
			scheduleController = new ScheduleController();
			this.controlledSystemType = ControlledSystemType.None;
		}
		return scheduleController;
	}
	/**
	 * Returns the OptionModelController for the underlying system.
	 * @return the OptionModelController
	 */
	public OptionModelController getOptionModelController() {
		if (optionModelController == null) {
			optionModelController = new OptionModelController();
			optionModelController.setControllingAgent(this.energyAgent);
			if (this.getBundleModelForTechnicalSystem()!=null) {
				this.loadTechnicalSystemFromBundle(optionModelController, this.getBundleModelForTechnicalSystem());
			}
			this.controlledSystemType = ControlledSystemType.TechnicalSystem;
		}
		return optionModelController;
	}
	/**
	 * Returns the bundle model specification. Overwrite, if you want to assign a 
	 * bundle located EOM-TechnicalSystem file to the current agent.
	 * @return the bundle model description
	 */
	protected BundleModel getBundleModelForTechnicalSystem() {
		return null;
	}
	/**
	 * Loads a TechnicalSystem as specified into the specified {@link OptionModelController}.
	 *
	 * @param omc the {@link OptionModelController} with which the 
	 * @param bundleMode the bundle mode
	 * @return the technical system
	 */
	public TechnicalSystem loadTechnicalSystemFromBundle(OptionModelController omc, BundleModel bundleModel) {
		if (omc==null || bundleModel==null) return null;
		omc.loadTechnicalSystemFromBundle(bundleModel.getBundleName(), bundleModel.getFileReference(), null);
		return omc.getTechnicalSystem();
	}
	
	/**
	 * Returns the GroupController for the underlying system.
	 * @return the GroupController
	 */
	public GroupController getGroupController() {
		if (groupController == null) {
			groupController = new GroupController();
			groupController.getGroupOptionModelController().setControllingAgent(this.energyAgent);
			this.controlledSystemType = ControlledSystemType.TechnicalSystemGroup;
		}
		return groupController;
	}
	
	/**
	 * Returns the type of controlled system.
	 * @return the type of controlled system
	 */
	public ControlledSystemType getTypeOfControlledSystem() {
		return controlledSystemType;
	}
	
	/**
	 * Returns the specified file or directory.<br> 
	 * <b>Notice:</b> For the case {@link DirectoryType#SystemMonitoringFile}, the path and file name will be adjusted 
	 * to the current system time. The directory will correspond to the current month, while for each day a file is 
	 * considered (e.g. ./[projectBaseDir]/[localAgentName]/log/03/DAY_07_SystemMonitoring.bin for the 7th of March
	 * of a year). Thus, a ring memory for one year will be constructed.
	 *
	 * @param type the type of the directory or file
	 * @return the file or directory
	 */
	public File getFileOrDirectory(DirectoryType type) {
		return DirectoryHelper.getFileOrDirectory(type, this.energyAgent.getAID());
	}
	
	
	/**
	 * Sets the measurements, coming from the underlying technical system.
	 * @param measurementVector the new measurements
	 */
	public void setMeasurementsFromSystem(FixedVariableList measurementVector) {
		this.fixedVariableListMeasurements = measurementVector;
		this.setChangedAndNotify(CHANGED.MEASUREMENTS_FROM_SYSTEM);
	}
	
	/**
	 * Returns the current measurements from the underlying system.
	 * @return the measurements
	 */
	public FixedVariableList getMeasurementsFromSystem() {
		return this.fixedVariableListMeasurements;
	}
	
	
	// -----------------------------------------------------
	// --- Methods for handling the CEA --------------------
	// -----------------------------------------------------
	/**
	 * Returns the {@link CeaConfigModel} if available. This is used to set the update behaviour 
	 * for the platform (e.g for repository locations or the update interval)
	 * @return the current CeaConfigModel (may be <code>null</code> also)
	 */
	public CeaConfigModel getCeaConfigModel() {
		return ceaConfigModel;
	}
	/**
	 * Sets the {@link CeaConfigModel}. Using this method, will also set the update sites (p2 and for the current project).
	 * Additionally, the {@link PlatformUpdateBehaviour} will be started according to set defined interval.
	 * @param ceaConfigModel the new CeaConfigModel
	 */
	public void setCeaConfigModel(CeaConfigModel ceaConfigModel) {
		if (ceaConfigModel!=null && (this.ceaConfigModel==null || this.ceaConfigModel.equals(ceaConfigModel)==false)) {
			// --- Set the new configuration model ------------------
			this.ceaConfigModel = ceaConfigModel;
			// --- Set update sites --------------------------------- 
			PlatformUpdater.getInstance().setUpdateSites(this.ceaConfigModel);
			if (PlatformUpdater.DEBUG_PLATFORM_UPDATE==true) {
				// --- Start the regular update behaviour ---------------
				this.energyAgent.startPlatformUpdateBehaviourNow();
			} else {
				// --- Start the regular update behaviour ---------------
				this.energyAgent.startNewPlatformUpdateBehaviour();
			}
		}
	}
	/**
	 * Returns the agent id of the CEA from the current NetworkModel.
	 * @return the agent id of CEA
	 */
	public String getAgentIdOfCea() {
		String agentIdCea = null;
		Vector<NetworkComponent> netCompVector = this.getNetworkModel().getNetworkComponentVectorSorted();
		for (int i = 0; i < netCompVector.size(); i++) {
			NetworkComponent netComp = netCompVector.get(i);
			String agentClassName = this.getNetworkModel().getAgentClassName(netComp);
			if (agentClassName!=null && agentClassName.equals(CentralExecutiveAgent.class.getName())) {
				// --- Found the required NetworkComponent ------
				agentIdCea = netComp.getId();
				break;
			}
		}
		return agentIdCea;
	}
	
	/**
	 * Return the AID of the central executive agent (CEA) if such agent is available in the current setup.
	 * @return the CEA's AID (can also be null).
	 */
	public AID getCentralAgentAID() {
		if (centralAgentAID==null) {
		
			String agentIdOfCEA = this.getAgentIdOfCea();
			SetupExtension setEx = this.getHyGridAbstractEnvironmentModel().getSetupExtension();
			AgentSpecifier ceaSpecifier = this.getHyGridAbstractEnvironmentModel().getDeploymentSettings().getCentralAgentSpecifier();
			
			if (setEx!=null) {
				DeploymentGroupsHelper dgh = setEx.getDeploymentGroupsHelper();
				boolean isDeployedCea = dgh.isAgentDeployed(agentIdOfCEA) && dgh.isDeploymentActivated(agentIdOfCEA);
				boolean isDeployedEnergyAgent = this.energyAgent.getAgentOperatingMode()!=AgentOperatingMode.Simulation;
				boolean isAgentIdOfCeaNull = agentIdOfCEA==null; 
				if (isDeployedCea==false & isDeployedEnergyAgent==false & isAgentIdOfCeaNull==false) {
					// --- CEA and local agent were NOT deployed --------
					centralAgentAID = new AID(agentIdOfCEA, AID.ISLOCALNAME);
					
				} else if (isDeployedCea==false & isDeployedEnergyAgent==true) {
					// --- NOT sure where to find the CEA ---------------
					// --- Possibly take the setter later on ------------
					// --- => Current fallback solution -----------------
					centralAgentAID = ceaSpecifier.getAID();
					
				} else {
					// --- The CEA is deployed - take AgentSpecifier ----
					centralAgentAID = ceaSpecifier.getAID();
				}
			} else {
				centralAgentAID = ceaSpecifier.getAID();
			}
		}
		return centralAgentAID;
	}
	/**
	 * Sets the central agent AID.
	 * @param centralAgentAID the new central agent AID
	 */
	public void setCentralAgentAID(AID centralAgentAID) {
		this.centralAgentAID = centralAgentAID;
	}
	
	
	// -----------------------------------------------------
	// --- Methods for handling the phone book -------------
	// -----------------------------------------------------
	/**
	 * Returns the AID of the agent that maintains the central phone book.
	 * In this default implementation this is the CEA, override this method
	 * to provide a different maintainer AID.
	 * @return the central phone book maintainer AID
	 */
	public AID getCentralPhoneBookMaintainerAID() {
		return this.getCentralAgentAID();
	}
	
	/**
	 * Returns the agent's local phone book.
	 * @return the phone book
	 */
	public PhoneBook getPhoneBook() {
		if (phoneBook==null) {
			// --- For the real application of the energy agent -----
			if (this.energyAgent.getAgentOperatingMode()==AgentOperatingMode.RealSystem) {
				File phoneBookFile = this.getFileOrDirectory(DirectoryType.PhoneBookFile);
				if (phoneBookFile.exists()) {
					phoneBook = PhoneBook.loadPhoneBook(phoneBookFile, this.getPhoneBookEntryClass());
				}
			}
			// --- Backup solution, or in all other modes -----------
			if (phoneBook==null) {
				// --- Create temporary PhoneBook instance ---------- 
				phoneBook = new PhoneBook(this.energyAgent.getAID());
				if (this.energyAgent.getAgentOperatingMode()==AgentOperatingMode.RealSystem) {
					System.out.println("[" + this.energyAgent.getLocalName() + "] Created temporary phonebook!");
				}
			}
		}
		return phoneBook;
	}
	
	/**
	 * Gets the phone book entry class.
	 * @return the phone book entry class
	 */
	protected abstract Class<GenericPhoneBookEntry> getPhoneBookEntryClass();
	
	
	/**
	 * Gets the my phone book entry.
	 * @return the my phone book entry
	 */
	public GenericPhoneBookEntry getMyPhoneBookEntry() {
		if (myPhoneBookEntry==null) {
			try {
				//TODO Figure out why this is not working for sensor agents ???
				myPhoneBookEntry = this.getPhoneBookEntryClass().getDeclaredConstructor().newInstance();
				myPhoneBookEntry.setAgentAID(this.energyAgent.getAID());
				if (this.getNetworkComponent()!=null) {
					myPhoneBookEntry.setComponentType(this.getNetworkComponent().getType());
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException	| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				System.err.println("[" + this.getClass().getSimpleName() + " of " + this.energyAgent.getLocalName() + "] Error creatong new phone book entry instance");
				e.printStackTrace();
			}
		}
		return myPhoneBookEntry;
	}
	/**
	 * Sets the my phone book entry.
	 * @param myPhoneBookEntry the new my phone book entry
	 */
	public void setMyPhoneBookEntry(GenericPhoneBookEntry myPhoneBookEntry) {
		this.myPhoneBookEntry = myPhoneBookEntry;
		this.setChangedAndNotify(CHANGED.OWN_PHONEBOOK_ENTRY_UPDATED);
	}
	
	/**
	 * Adds a single entry to phone book.
	 * @param entry the entry
	 */
	public void addEntryToPhoneBook(GenericPhoneBookEntry entry) {
		this.getPhoneBook().addEntry(entry);
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	/**
	 * Adds a list of entries to phone book.
	 * @param entries the entries
	 */
	public void addEntriesToPhoneBook(List<GenericPhoneBookEntry> entries) {
		this.getPhoneBook().addEntries(entries);
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	
	@SuppressWarnings("unchecked")
	public GenericPhoneBookEntry getPhoneBookEntry(String localName) {
		ArrayList<AbstractPhoneBookEntry> searchResults = this.getPhoneBook().getEntries(EnergyAgentPhoneBookSearchFilter.matchLocalName(localName));
		return (GenericPhoneBookEntry) searchResults.get(0);
	}
	/**
	 * Gets an AID from the phone book.
	 * @param localName the local name of the agent to look up
	 * @return the agent's AID, null if not found
	 */
	public AID getAidFromPhoneBook(String localName) {
		return this.getPhoneBook().getAidForLocalName(localName);
	}
	
	
	// -----------------------------------------------------
	// --- Methods for handling the logging mode -----------
	// -----------------------------------------------------
	
	/**
	 * Gets the logging mode.
	 * @return the logging mode
	 */
	public LoggingMode getLoggingMode() {
		return loggingMode;
	}
	/**
	 * Sets the logging mode.
	 * @param loggingMode the new logging mode
	 */
	public void setLoggingMode(LoggingMode loggingMode) {
		this.loggingMode = loggingMode;
	}
	
	/**
	 * Gets the logging filter. This implementation provides a default filter
	 * that reacts on any change of IO values. It can be further refined by defining
	 * general or variable-specific thresholds.
	 * @return the logging filter
	 */
	public IOListFilterForLogging getLoggingFilter() {
		if (loggingFilter==null && loggingMode==LoggingMode.ON_VALUE_CHANGE) {
			loggingFilter = new IOListFilterForLogging();
		}
		return loggingFilter;
	}
	
	/**
	 * Gets the field data request message.
	 * @return the field data request message
	 */
	public ACLMessage getFieldDataRequestMessage() {
		return fieldDataRequestMessage;
	}
	
	/**
	 * Sets the field data request message.
	 * @param fieldDataRequestMessage the new field data request message
	 */
	public void setFieldDataRequestMessage(ACLMessage fieldDataRequestMessage) {
		this.fieldDataRequestMessage = fieldDataRequestMessage;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.jade.phonebook.PhoneBookListener#notifyEvent(de.enflexit.jade.phonebook.PhoneBookEvent)
	 */
	@Override
	public void notifyEvent(PhoneBookEvent event) {
		// --- Added for backwards compatibility ----------
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	
}
