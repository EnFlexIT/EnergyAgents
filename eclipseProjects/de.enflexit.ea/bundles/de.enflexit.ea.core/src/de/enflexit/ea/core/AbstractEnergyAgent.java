package de.enflexit.ea.core;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import de.enflexit.awb.core.Application;
import de.enflexit.awb.core.config.GlobalInfo.DeviceSystemExecutionMode;
import de.enflexit.awb.core.config.GlobalInfo.ExecutionMode;
import de.enflexit.awb.core.project.setup.AgentClassElement4SimStart;
import de.enflexit.awb.simulation.environment.EnvironmentModel;
import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.common.ontology.AgentStartArgument;
import de.enflexit.common.ontology.AgentStartConfiguration;
import de.enflexit.common.ontology.OntologyClassTreeObject;
import de.enflexit.common.ontology.OntologyVisualizationHelper;
import de.enflexit.common.performance.PerformanceMeasurements;
import de.enflexit.ea.core.AbstractInternalDataModel.ControlledSystemType;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import de.enflexit.ea.core.behaviour.DefaultMessageReceiveBehaviour;
import de.enflexit.ea.core.behaviour.LiveMonitoringSubscriptionResponder;
import de.enflexit.ea.core.behaviour.O2ABehaviour;
import de.enflexit.ea.core.behaviour.PlatformUpdateBehaviour;
import de.enflexit.ea.core.behaviour.TestTickerBehaviour;
import de.enflexit.ea.core.dataModel.PlatformUpdater;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.dataModel.deployment.AgentDeploymentInformation;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.deployment.SetupExtension;
import de.enflexit.ea.core.dataModel.deployment.StartArgument;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import de.enflexit.ea.core.eomStateStream.EomModelLoader;
import de.enflexit.ea.core.monitoring.MonitoringBehaviourRT;
import de.enflexit.ea.core.monitoring.MonitoringListenerForLogging;
import de.enflexit.ea.core.monitoring.MonitoringListenerForLogging.LoggingDestination;
import de.enflexit.ea.core.planning.AbstractPlanningDispatcherManager;
import de.enflexit.ea.core.planning.PlanningDispatcher;
import de.enflexit.ea.core.ui.EnergyAgentUiConnector;
import de.enflexit.ea.core.ui.GeneralInformation;
import de.enflexit.ea.core.ui.PlanningInformation;
import de.enflexit.ea.core.ui.RealTimeInformation;
import de.enflexit.jade.phonebook.AbstractPhoneBookEntry;
import de.enflexit.jade.phonebook.behaviours.PhoneBookRegistrationInitiator;
import de.enflexit.jade.phonebook.behaviours.PhoneBookRegistrationResponder;
import energy.FixedVariableList;
import energy.helper.TechnicalSystemGroupHelper;
import energy.helper.TechnicalSystemHelper;
import energy.optionModel.FixedVariable;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemState;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Abstract superclass for implementing energy agents.
 *
 * @author Hanno-Felix Wagner - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractEnergyAgent extends Agent implements Observer {

	private static final long serialVersionUID = -6729957368366493537L;
	
	private boolean isStartTestTickerBehaviour = false;
	private String testTickerLocalName = "n0";
	
	private boolean isDoPerformanceMeasurements = false;
	private String performanceMeasuringAgent;
	
	private TestTickerBehaviour testTickerBehaviour;
	
	
	private AgentOperatingMode operatingMode;
	
	private EnergyAgentIO agentIOBehaviour;
	private SimulationConnector simulationConnector;

	protected DefaultMessageReceiveBehaviour defaultMessageReceiveBehaviour;

	private PlanningDispatcher planningDispatcher;
	private boolean isPlanningDispatcherTerminated;
	
	private ControlBehaviourRT controlBehaviourRT;
	
	private ThreadedBehaviourFactory threadedBehaviourFactory;
	private Behaviour threadedMonitoringBehaviour;
	protected MonitoringBehaviourRT monitoringBehaviourRT;
	private MonitoringListenerForLogging logWriter;
	private LiveMonitoringSubscriptionResponder liveMonitoringSubscriptionResponder;
	
	private PlatformUpdateBehaviour updateBehaviour;
	
	private EnergyAgentUiConnector uiConnector;
	
	
	/**
	 * Returns the internal data model of this agent.
	 * @return the internal data model
	 */
	public abstract AbstractInternalDataModel<? extends AbstractPhoneBookEntry> getInternalDataModel();
	

	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected final void setup() {
		
		// --- Register codec and ontology ----------------------------------------------
		this.getContentManager().registerLanguage(this.getAgentCodec());
		this.getContentManager().registerOntology(this.getAgentOntology());
		this.getContentManager().registerOntology(this.getOpsOntology());
		
		// --- Load the TechnicalSystem to the agents OptionModelController? ------------
		if (this.getInternalDataModel().getBundleModelForTechnicalSystem()!=null) {
			this.getInternalDataModel().getOptionModelController();
		}
		
		// --- If deployed, get the environment model from the project ------------------
		if (this.isSimulation()==false) {
			
			// --- Initialize network model and component -------------------------------
			EnvironmentModel environmentModel = Application.getProjectFocused().getEnvironmentController().getEnvironmentModel();
			if (environmentModel!=null) {
				// --- Set project and setup properties ---------------------------------
				this.getInternalDataModel().setProjectProperties(environmentModel.getProjectProperties());
				this.getInternalDataModel().setSetupProperties(environmentModel.getSetupProperties());
				
				// --- Set the time model type according to the environment model -------
				HyGridAbstractEnvironmentModel abstractEnvironmentModel = (HyGridAbstractEnvironmentModel) environmentModel.getAbstractEnvironment();
				abstractEnvironmentModel.setTimeModelType(environmentModel.getTimeModel());
				this.getInternalDataModel().setHyGridAbstractEnvironmentModel(abstractEnvironmentModel);
				
				// --- Set NetworkModel and networkComponent ----------------------------
				NetworkModel networkModel = (NetworkModel) environmentModel.getDisplayEnvironment();
				this.getInternalDataModel().setNetworkModel(networkModel, false);
				NetworkComponent networkComponent = networkModel.getNetworkComponent(this.getLocalName());
				if (networkComponent!=null) {
					this.getInternalDataModel().setNetworkComponent(networkComponent);
				}
				
			}
		}
		
		// --- Start the IO Behaviour ---------------------------------------------------
		this.startEnergyAgentIO();
		
		// --- Start monitoring-related Behaviours depending on the operating mode ------
		if (this.getAgentOperatingMode()==AgentOperatingMode.TestBedReal) {
			this.startMonitoringBehaviourRT();
		} else if (this.getAgentOperatingMode()==AgentOperatingMode.RealSystem) {
			this.startSystemStateLogWriter();
			this.startLiveMonitoringSubscriptionResponder();
		}
		
		// --- Start the O2ABehaviour for handling results of database requests ---------
		this.startO2ABehaviour();
		
		// --- Connect to the simulation (done by the IOSimulated in the other cases) ---
		if (this.getAgentOperatingMode()==AgentOperatingMode.TestBedReal) {
			this.getSimulationConnector();
		}
		
		// --- Start the default message receive behaviour ------------------------------
		this.startDefaultMessageReceiveBehaviour();
		
		// --- Finalize energy agent setup for non-simulations --------------------------
		if (this.isSimulation()==false) {
			this.onEnvironmentModelSet();
		}
		
		// --- Start test ticker behaviour? ---------------------------------------------
		if (this.isStartTestTickerBehaviour==true && (this.testTickerLocalName==null || this.testTickerLocalName.isBlank()==true || this.getLocalName().equals(this.testTickerLocalName) )) {
			this.testTickerBehaviour = new TestTickerBehaviour(this, 10 * 1000);
			this.addBehaviour(this.testTickerBehaviour);
		}
	}
	
	/**
	 * Starts the O2ABehaviour.
	 */
	private void startO2ABehaviour() {
		O2ABehaviour o2aBehaviour = new O2ABehaviour();
		this.addBehaviour(o2aBehaviour);
		this.setO2AManager(o2aBehaviour);
		this.setEnabledO2ACommunication(true, 0);
	}
	
	/**
	 * This method is called after the environment model is set.
	 */
	protected final void onEnvironmentModelSet() {
		// --- Register at the central phone book if necessary --------------------------
		this.startPhoneBookRegistration();
		// --- Call the individual setup method for energy agents ----------------------- 
		this.setupEnergyAgent();	
	}
	/**
	 * Starts the phone book registration at the central phone book maintainer.
	 * @see AbstractInternalDataModel#getCentralPhoneBookMaintainerAID()
	 */
	private void startPhoneBookRegistration() {
		
		AID aidPhoneBookMaintainer = this.getInternalDataModel().getCentralPhoneBookMaintainerAID();
		if (aidPhoneBookMaintainer!=null) {
			AbstractPhoneBookEntry myPhoneBookEntry = this.getInternalDataModel().getMyPhoneBookEntry();
			PhoneBookRegistrationInitiator phoneBookRegistrationInitiator = new PhoneBookRegistrationInitiator(this, myPhoneBookEntry, aidPhoneBookMaintainer, true);
			phoneBookRegistrationInitiator.addPhoneBookListener(this.getInternalDataModel());
			this.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(MessageTemplate.MatchConversationId(PhoneBookRegistrationResponder.CONVERSATION_ID));
			this.addBehaviour(phoneBookRegistrationInitiator);
		}
	}
	
	
	/**
	 * This method can be overridden to perform agent-specific setup tasks.<br> 
	 * Use this instead of the regular {@link Agent#setup()} method of JADE agents.
	 */
	protected void setupEnergyAgent() {
		// --- Empty default implementation - override this method to implement agent-specific setup actions
	}

	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected final void takeDown() {
		
		// --- Stop test ticker behaviour? ------------------------------------
		if (this.testTickerBehaviour!=null) {
			this.removeBehaviour(this.testTickerBehaviour);
			this.testTickerBehaviour = null;
		}
		
		// --- Close UI -------------------------------------------------------
		this.getUIConnector().closeUI();
		
		// --- Stop the log writer --------------------------------------------
		this.stopSystemStateLogWriter();
		this.stopMonitoringBehaviourRT();
		this.terminatePlanningDispatcher();
		
		// --- Perform general take down actions ------------------------------
		switch (this.getAgentOperatingMode()) {
		case Simulation:
			if (this.agentIOBehaviour!=null) ((AbstractIOSimulated)this.getEnergyAgentIO()).stopSimulation();
			break;

		case TestBedSimulation:
			break;
			
		case TestBedReal:
			break;

		case RealSystem:
			break;
			
		case RealSystemSimulatedIO:
			break;
		}
		
		// --- Perform agent-specific take down actions ----------------------- 
		this.takeDownEnergyAgent();
	}
	/**
	 * This method can be used to perform agent-specific take down tasks.
	 */
	protected void takeDownEnergyAgent() {
		// --- Empty default implementation - override this method to implement agent-specific take down actions
	}

	/**
	 * Gets the codec for this agent. Override to specify your own codec.
	 * @return the agent codec
	 */
	public Codec getAgentCodec() {
		return new SLCodec();
	}
	/**
	 * Gets the ontology for this agent. Override to specify your own ontology.
	 * @return the agent ontology
	 */
	public Ontology getAgentOntology() {
		return HyGridOntology.getInstance();
	}
	
	/**
	 * Gets the ontology for OPS interactions. Override to specify your own ontology.
	 * @return the ops ontology
	 */
	public Ontology getOpsOntology() {
		return OpsOntology.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#getArguments()
	 */
	@Override
	public Object[] getArguments() {
		
		if (this.getAgentOperatingMode()==AgentOperatingMode.Simulation) {
			// --- Simulation - use JADE standard -----------------------------
			return super.getArguments();
			
		} else {
			// --- Testbed or real - get arguments from the project -----------
			return this.getStartArgumentsFromProject().toArray();
		}
	}

	/**
	 * Checks if the current execution is a simulation.
	 * @return true, if the execution is simulation
	 */
	private boolean isSimulation() {
		// --- Check application settings -----------------
		if (Application.getGlobalInfo().getExecutionMode()==ExecutionMode.DEVICE_SYSTEM) {
			if (Application.getGlobalInfo().getDeviceServiceExecutionMode()==DeviceSystemExecutionMode.AGENT) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Gets the current {@link AgentOperatingMode}.
	 * @return the operating mode
	 */
	public AgentOperatingMode getAgentOperatingMode() {
		if (operatingMode==null) {
			if (this.isSimulation()==true) {
				// --- The simulation case ----------------------------------------------
				operatingMode = AgentOperatingMode.Simulation;
			} else {
				
				// --- Possible other cases - check the SetupExtension ------------------
				SetupExtension setEx = this.getInternalDataModel().getHyGridAbstractEnvironmentModel().getSetupExtension();
				if (setEx!=null) {
					AgentDeploymentInformation agentInfo = setEx.getDeploymentGroupsHelper().getAgentDeploymentInformation(this.getLocalName());
					if (agentInfo!=null) {
						operatingMode = agentInfo.getAgentOperatingMode();
					}
				}
			}
			// --- Use 'real' as backup solution ----------------------------------------
			if (operatingMode==null) {
				this.print("Use backup operating mode 'RealSystem'.", false);
				operatingMode = AgentOperatingMode.RealSystem;
			}
		}
		return operatingMode;
	}
	
	
	/**
	 * Starts the energy agent IO.
	 */
	protected void startEnergyAgentIO() {
		if (((Behaviour)this.getEnergyAgentIO()).getAgent()==null) {
			this.addBehaviour((Behaviour) this.getEnergyAgentIO());
		}
	}
	/**
	 * Returns the IO behaviour that is - depending on the current {@link AgentOperatingMode} -  
	 * either an extension of {@link AbstractIOSimulated} or {@link AbstractIOReal}.
	 * 
	 * @return the IO behaviour to use for the current operating mode
	 */
	public final EnergyAgentIO getEnergyAgentIO() {
		if (agentIOBehaviour==null && this.getAgentOperatingMode()!=null) {
			// --- Is this a simulation or is this the real application -------
			switch (this.getAgentOperatingMode()) {
			case Simulation:
			case TestBedSimulation:
				agentIOBehaviour = this.getIOSimulated();
				if (agentIOBehaviour==null) {
					// --- As backup solution --------------------------------- 
					agentIOBehaviour = new DefaultIOSimulated(this);
				}
				break;
				
			case TestBedReal:
			case RealSystem:
				agentIOBehaviour = this.getIOReal();
				break;
				
			case RealSystemSimulatedIO:
				//TODO instantiate the correct IO behaviour for this operating mode
				break;
			
			}
		}
		return agentIOBehaviour;
	}
	
	/**
	 * Stop energy agent IO.
	 */
	protected void stopEnergyAgentIO() {
		if (this.agentIOBehaviour!=null) {
			if (((Behaviour)this.agentIOBehaviour).getAgent()!=null) {
				this.removeBehaviour((Behaviour) this.agentIOBehaviour);
			}
			this.agentIOBehaviour = null;
		}
	}
	/**
	 * Gets the IO behaviour for simulation.
	 * @return the IO simulated
	 */
	public abstract AbstractIOSimulated getIOSimulated();
	/**
	 * Gets the IO behaviour for real systems.
	 * @return the IO real
	 */
	public abstract AbstractIOReal getIOReal();
		
	
	/**
	 * Return the threaded behaviour factory.
	 * @return the threaded behaviour factory
	 */
	public ThreadedBehaviourFactory getThreadedBehaviourFactory() {
		if (threadedBehaviourFactory==null) {
			threadedBehaviourFactory = new ThreadedBehaviourFactory();
		}
		return threadedBehaviourFactory;
	}
	/**
	 * Starts the real time control behaviour MonitoringBehaviourRT, if not already done.
	 */
	protected void startMonitoringBehaviourRT() {
		if (this.getMonitoringBehaviourRT().getAgent()==null) {
			this.threadedMonitoringBehaviour = this.getThreadedBehaviourFactory().wrap(this.getMonitoringBehaviourRT()); 
			this.addBehaviour(this.threadedMonitoringBehaviour);
		}
	}
	/**
	 * Gets the monitoring behaviour RT.
	 * @return the monitoring behaviour RT
	 */
	public MonitoringBehaviourRT getMonitoringBehaviourRT(){
		if (monitoringBehaviourRT==null) {
			monitoringBehaviourRT = new MonitoringBehaviourRT(this);
		}
		return monitoringBehaviourRT;
	}
	/**
	 * Stops the real time control behaviour MonitoringBehaviourRT, if not already done.
	 */
	protected void stopMonitoringBehaviourRT() {
		if (this.monitoringBehaviourRT!=null) {
			if (this.monitoringBehaviourRT.getAgent()!=null) {
				// --- Interrupt the threaded behaviour -------------
				this.getThreadedBehaviourFactory().getThread(this.getMonitoringBehaviourRT()).interrupt();
				if (threadedMonitoringBehaviour!=null) {
					this.removeBehaviour(this.threadedMonitoringBehaviour);
					this.threadedMonitoringBehaviour = null;
				}
			}
			this.monitoringBehaviourRT=null;
		}
	}
	/**
	 * Checks if the monitoring is activated.
	 * @return true, if is activated monitoring
	 */
	public boolean isActivatedMonitoring() {
		if (this.threadedMonitoringBehaviour!=null && this.monitoringBehaviourRT!=null) {
			if (this.threadedMonitoringBehaviour.getAgent()!=null && this.monitoringBehaviourRT.getAgent()!=null ) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Starts the system state log writer.
	 */
	protected void startSystemStateLogWriter() {
		if (this.monitoringBehaviourRT==null) {
			this.startMonitoringBehaviourRT();
		}
		this.getMonitoringBehaviourRT().addMonitoringListener(this.getSystenStateLogWriter());
	}
	/**
	 * Stops the system state log writer.
	 */
	protected void stopSystemStateLogWriter() {
		if (this.monitoringBehaviourRT!=null && this.logWriter!=null) {
			this.monitoringBehaviourRT.removeMonitoringListener(this.logWriter);
			this.logWriter.doShutdown();
			this.logWriter = null;
		}
	}
	/**
	 * Returns or initiates the {@link MonitoringListenerForLogging}.
	 * @return the log writer
	 */
	public MonitoringListenerForLogging getSystenStateLogWriter() {
		if (logWriter==null) {
			logWriter = new MonitoringListenerForLogging(this, this.getLoggingDestination());
			logWriter.start();
		}
		return logWriter;
	}
	/**
	 * Returns the logging destination, where the default is {@link LoggingDestination#EomDatabase}. 
	 * Overwrite this method to change the destination of system state logging.
	 * @return the logging destination
	 */
	public LoggingDestination getLoggingDestination() {
		return LoggingDestination.EomDatabase;
	}
	/**
	 * Checks if the log writer is activated.
	 * @return true, if is activated log writer
	 */
	public boolean isActivatedLogWriter() {
		if (this.logWriter!=null) {
			return true;
		}
		return false;
	}
	
	

	/**
	 * Gets the simulation connector.
	 * @return the simulation connector
	 */
	public SimulationConnector getSimulationConnector() {
		if (simulationConnector == null) {
			switch (this.getAgentOperatingMode()) {
			case Simulation:
				simulationConnector = new SimulationConnectorLocal(this, (AbstractIOSimulated) this.getEnergyAgentIO());
				break;
				
			case TestBedSimulation:
				simulationConnector = new SimulationConnectorRemote(this, (AbstractIOSimulated) this.getEnergyAgentIO());
				break;
				
			case TestBedReal:
				simulationConnector = new SimulationConnectorRemoteForIOReal(this);
				break;
	
			default:
				// --- No simulation connector for the non-simulative modes -------------
				break;
			}
		}
		return simulationConnector;
	}

	
	// ----------------------------------------------------------------------------------
	// --- From here, handling of the planning dispatcher -------------------------------
	// ----------------------------------------------------------------------------------	
	/**
	 * Returns the energy agents {@link PlanningDispatcher} instance.<br><br>
	 * To work with the dispatcher, overwrite method {@link #getPlanningDispatcherManager()} 
	 * and implement your own planning dispatcher listener.
	 * 
	 * @return the energy agents PlanningDispatcher instance
	 */
	public final PlanningDispatcher getPlanningDispatcher() {
		if (planningDispatcher==null) {
			// --- Check if PlanningDispatcher was terminated ---------------------------
			if (this.isPlanningDispatcherTerminated==true) {
				this.print("The planning dispatcher was terminated! To restart the dispatcher again, invoke #enablePlanningDispatcherAfterTermination() first.", true);
				return null;
			}
			
			// --- Get the planning dispatcher listener first ---------------------------
			AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> pdl = this.getPlanningDispatcherManager();
			if (pdl==null) {
				String errDesc = "To use the energy agents planning dispatcher, a corresponding manager needs to be defined.\n";
				errDesc += "=> Overwrite the method 'getPlanningDispatcherManager()' in your energy agent class to do so!" ;
				this.print(errDesc, true);
			} else {
				planningDispatcher = new PlanningDispatcher(this, pdl);
			}
		}
		return planningDispatcher;
	}
	/**
	 * Has to return the planning dispatcher manager that belongs to the agents {@link PlanningDispatcher}.<br>
	 * By default, this method returns <code>null</code>. Overwrite this method and implement your own 
	 * {@link AbstractPlanningDispatcherManager} to activate the planning capabilities of an energy agent.
	 *  
	 * @return the planning dispatcher manager (by default <code>null</code>
	 */
	public AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> getPlanningDispatcherManager() {
		return null;
	}
	/**
	 * Checks if, at all, a planning is possible. Means, that either a TechnicalSystem or a TechnicalSystemGroup
	 * needs to be under the control of the current energy agent.
	 *  
	 * @return true, if a planning is possible
	 */
	public boolean isPlanningPossible() {
		boolean isTechnicalSystem = this.getInternalDataModel().getTypeOfControlledSystem()==ControlledSystemType.TechnicalSystem;
		boolean isTechnicalSystemGroup = this.getInternalDataModel().getTypeOfControlledSystem()==ControlledSystemType.TechnicalSystemGroup;
		return isTechnicalSystem || isTechnicalSystemGroup;
	}
	/**
	 * Checks if the planning is activated.
	 * @return true, if the planning is activated
	 */
	public boolean isPlanningActivated() {
		return planningDispatcher!=null;
	}
	
	/**
	 * Terminates the current planning dispatcher, as well as executed planning processes.
	 */
	private void terminatePlanningDispatcher() {
		if (this.planningDispatcher!=null) {
			this.isPlanningDispatcherTerminated = true;
			this.planningDispatcher.terminate();
			this.planningDispatcher = null;
		}
	}
	/**
	 * Checks if the planning dispatcher was terminated.
	 * @return true, if is planning dispatcher terminated
	 */
	public final boolean isPlanningDispatcherTerminated() {
		return isPlanningDispatcherTerminated;
	}
	/**
	 * Enable planning dispatcher.
	 */
	public final void enablePlanningDispatcherAfterTermination() {
		this.isPlanningDispatcherTerminated = false;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, handling of the real time control ---------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Gets the real time control behaviour.
	 * @return the ControlBehaviourRT
	 */
	public ControlBehaviourRT getControlBehaviourRT() {
		if (controlBehaviourRT==null) {
			controlBehaviourRT = new ControlBehaviourRT(this);
		}
		return controlBehaviourRT;
	}
	/**
	 * Start real time control behaviour ControlBehaviourRT, if not already done.
	 */
	protected void startControlBehaviourRT() {
		if (this.getControlBehaviourRT().getAgent()==null) {
			this.addBehaviour(this.getControlBehaviourRT());
		}
	}
	/**
	 * Stops the real time control behaviour ControlBehaviourRT, if not already done.
	 */
	protected void stopControlBehaviourRT() {
		if (this.controlBehaviourRT!=null) {
			if (this.controlBehaviourRT.getAgent()!=null) {
				this.removeBehaviour(this.controlBehaviourRT);
			}
		}
		this.controlBehaviourRT = null;
	}
	/**
	 * Checks if the real time control behaviour is executed by the current Energy Agent.
	 * @return true, if is executed control behaviour RT
	 */
	public boolean isExecutedControlBehaviourRT() {
		if (this.controlBehaviourRT==null || this.controlBehaviourRT.getAgent()==null) return false;
		return true;
	}
	
	
	/**
	 * Gets the message receive behaviour.
	 * @return the message receive behaviour
	 */
	public DefaultMessageReceiveBehaviour getDefaultMessageReceiveBehaviour() {
		if (defaultMessageReceiveBehaviour == null) {
			defaultMessageReceiveBehaviour = new DefaultMessageReceiveBehaviour();
		}
		return defaultMessageReceiveBehaviour;
	}
	/**
	 * Starts the default message receive behaviour.
	 */
	protected void startDefaultMessageReceiveBehaviour() {
		if (this.getDefaultMessageReceiveBehaviour().getAgent()==null) {
			this.addBehaviour(this.getDefaultMessageReceiveBehaviour());
			// --- If running in testbed or real mode, add a template to ignore CEA messages ------
			if (this.getAgentOperatingMode() != AgentOperatingMode.Simulation) {
				MessageTemplate proxyRegistration = MessageTemplate.MatchConversationId(ConversationID.PROXY_REGISTRATION.toString());
				this.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(proxyRegistration);
			}
		}
	}
	/**
	 * Stops the default message receive behaviour.
	 */
	protected void stopDefaultMessageReceiveBehaviour() {
		if (this.defaultMessageReceiveBehaviour!=null) {
			if (this.defaultMessageReceiveBehaviour.getAgent()!=null) {
				this.removeBehaviour(this.defaultMessageReceiveBehaviour);
			}
			this.defaultMessageReceiveBehaviour = null;
		}
	}
	/**
	 * Checks if the default message receive behaviour is executed by the current Energy Agent.
	 * @return true, if is executed default message receive behaviour
	 */
	protected boolean isExecutedDefaultMessageReceiveBehaviour() {
		if (this.defaultMessageReceiveBehaviour==null || this.defaultMessageReceiveBehaviour.getAgent()==null) return false;
		return true;
	}
	/**
	 * Handle incoming ACL messages from the {@link DefaultMessageReceiveBehaviour}.
	 * @param message the message
	 */
	public void handleIncomingMessage(ACLMessage message) {
		// --- Empty default implementation - override this method for agent-specific method handling -------
	}
	
	
	/**
	 * Gets the current value of the specified setpoint.
	 * @param setpointID the setpoint ID
	 * @return the current setpoint value
	 */
	protected FixedVariable getCurrentSetpoint(String setpointID){
		
		FixedVariable currentSetpoint = null;
		FixedVariableList currentSetPoints = this.getEnergyAgentIO().getSetPointsToSystem();
		if (currentSetPoints != null) {
			currentSetpoint = currentSetPoints.getVariable(setpointID);
		} else {
			// --- Currently not running, get the set points from the defined initial state
			TechnicalSystemState tss = (TechnicalSystemState) this.getInternalDataModel().getOptionModelController().getTechnicalSystem().getEvaluationSettings().getEvaluationStateList().get(0);
			FixedVariableList fvl = new FixedVariableList();
			fvl.addAll(tss.getIOlist());
			currentSetpoint = fvl.getVariable(setpointID);
		}
		return currentSetpoint;
	}

	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public final void update(Observable observable, Object updateObject) {
		
		// --- Will be invoked if the internal data model has changed ---------
		if (observable instanceof AbstractInternalDataModel) {
			
			if (updateObject==AbstractInternalDataModel.CHANGED.NETWORK_COMPONENT) {
				// ------------------------------------------------------------
				// --- Get the actual data model of the NetworkComponent ------ 
				NetworkComponent netComp = this.getInternalDataModel().getNetworkComponent();
				if (netComp!=null) {
					// --------------------------------------------------------
					// --- Check the data model of the network component ------
					Object dm = netComp.getDataModel();

					// --------------------------------------------------------
					// --- Load EOM model from file settings? -----------------
					if (dm==null) {
						dm = EomModelLoader.loadEomModel(netComp);
					}
					
					// --------------------------------------------------------
					// --- Case Separation EOM model type ---------------------
					if (dm instanceof ScheduleList) {
						// ----------------------------------------------------
						// --- ScheduleList -----------------------------------
						this.getInternalDataModel().getScheduleController().setScheduleList((ScheduleList) dm);

					} else if (dm instanceof TechnicalSystem){
						// ----------------------------------------------------
						// --- TechnicalSystems -------------------------------
						TechnicalSystem ts = (TechnicalSystem) dm;
						TechnicalSystemHelper.adjustEvaluationStartTime(ts, this.getEnergyAgentIO().getTime());
						if (this.getInternalDataModel().getBundleModelForTechnicalSystem()==null) {
							this.getInternalDataModel().getOptionModelController().setTechnicalSystem(ts);
							if (this.getInternalDataModel().getOptionModelController().getEvaluationStrategyRT()!=null) {
								
								// --- Add a real time control, if configured ---------
								// --- For testbed agents, it is to early to do this -- 
								if (this.getAgentOperatingMode()!=AgentOperatingMode.TestBedReal) {
									if (this.agentIOBehaviour!=null) {
										this.startControlBehaviourRT();
									}
								}
							}
						}
						
					} else if (dm instanceof TechnicalSystemGroup){
						// ----------------------------------------------------
						// --- TechnicalSystemGroup ---------------------------
						TechnicalSystemGroup tsg = (TechnicalSystemGroup) dm;
						TechnicalSystemGroupHelper.adjustEvaluationStartTime(tsg, this.getEnergyAgentIO().getTime());
						this.getInternalDataModel().getGroupController().setTechnicalSystemGroup(tsg);
						if (this.getInternalDataModel().getGroupController().getGroupOptionModelController().getEvaluationStrategyRT()!=null) {
							// --- Add a real time control, if configured -----
							// --- => For testbed agents, this is to early ! -- 
							if (this.getAgentOperatingMode()!=AgentOperatingMode.TestBedReal) {
								if (this.agentIOBehaviour!=null) {
									this.startControlBehaviourRT();
								}
							}
						}
					} // end data model object
				} // end NetworkComponent
				
			} // end updateObject
			
		} // end internal data model
		
		// --- Callback method for subclass-specific handling of observer updates
		this.onObserverUpdate(observable, updateObject);
	}
	
	/**
	 * This method can be used to implement agent-specific handling of observer updates. It is called by the {@link AbstractEnergyAgent}'s update() method, 
	 * which is declared final to make sure subclasses to not accidentally remove required functionality.
	 * @param observable the observable
	 * @param updateObject the update object
	 */
	public void onObserverUpdate(Observable observable, Object updateObject){
		// --- Empty default implementation. This method can be overridden by subclasses to implement agent-specific handling of observer updates.
	}
	
	/**
	 * Sets the time offset to match the simulation time. Only possible for {@link AgentOperatingMode#TestBedReal}
	 * @param timeOffset the new time offset for simulation
	 */
	protected void setTimeOffsetForSimulation(long timeOffset) {
		
		if (this.getAgentOperatingMode() != AgentOperatingMode.TestBedReal) return; 
		
		// --- Set the time offset for the IO behaviour -----------------------
		AbstractIOReal ioBehaviour = (AbstractIOReal) this.getEnergyAgentIO();
		if(ioBehaviour != null) {
			ioBehaviour.setTimeOffset(timeOffset);
		}
		
		// --- If there are monitoring or control strategies, reset them ------ 
		if (this.monitoringBehaviourRT != null) {
			this.print("Resetting monitoring strategy", false);
			this.getMonitoringBehaviourRT().resetEvaluationProcess();
		}
		if (this.controlBehaviourRT != null) {
			this.print("Resetting control strategy", false);
			this.getControlBehaviourRT().resetEvaluationProcess();
		}
	}
	
	/**
	 * Returns the start arguments from project.
	 * @return the start arguments from project
	 */
	private Vector<Object> getStartArgumentsFromProject() {
		
		// --- Check for start arguments ----------------------------
		Vector<Object> arguments = null;
		String[] startArguments = null;
		AgentStartConfiguration agentStartConfiguration = Application.getProjectFocused().getAgentStartConfiguration();
		Vector<AgentStartArgument> argumentsVector = agentStartConfiguration.get(this.getClass().getName());

		if (argumentsVector != null && argumentsVector.size() > 0) {
			// --- There are start arguments defined for this agent class ---------------

			arguments = new Vector<>();

			// --- Read arguments from the agent start list ------------
			AgentClassElement4SimStart classElement = this.getAgentClassElementForAgent(this.getLocalName());
			startArguments = classElement.getStartArgumentsBase64();

			OntologyVisualizationHelper ovh = Application.getProjectFocused().getOntologyVisualisationHelper();

			for (int i = 0; i < startArguments.length; i++) {

				// --- Determine the ontology for the current start argument ------
				AgentStartArgument asa = argumentsVector.get(i);
				String argumentClassName = asa.getOntologyReference();
				OntologyClassTreeObject octo = ovh.getClassTreeObject(argumentClassName);
				String ontologyMainClassReference = octo.getOntologyClass().getOntologyMainClass();

				// --- Set ontology and encoded instance -------------------
				StartArgument argument = new StartArgument();
				argument.setOntologyMainClassReference(ontologyMainClassReference);
				argument.setEncodedInstance(startArguments[i]);

				arguments.add(argument);
			}

		}
		return arguments;
	}
	
	/**
	 * Gets the agent class element for agent.
	 * @return the agent class element for agent
	 */
	private AgentClassElement4SimStart getAgentClassElementForAgent(String agentID) {
		List<AgentClassElement4SimStart> classElementList = Application.getProjectFocused().getSimulationSetups().getCurrSimSetup().getAgentList();
		for (AgentClassElement4SimStart classElement : classElementList) {
			if (classElement.getStartAsName().equals(agentID)) {
				return classElement;
			}
		}
		return null;
	}

	
	/**
	 * Directly starts the platform update behaviour.
	 * @return the executed platform update behaviour
	 */
	public PlatformUpdateBehaviour startPlatformUpdateBehaviourNow() {
		PlatformUpdateBehaviour pub = new PlatformUpdateBehaviour(this, new Date());
		pub.setEnforceUpdate(true);
		this.setPlatformUpdateBehaviour(pub);
		this.startPlatformUpdateBehaviour();
		return pub;
	}
	/**
	 * Starts a new platform update behaviour.
	 */
	public void startNewPlatformUpdateBehaviour() {
		this.setPlatformUpdateBehaviour(null);
		this.startPlatformUpdateBehaviour();
	}
	/**
	 * Regularly starts a platform update behaviour according to the {@link CeaConfigModel}.
	 * @see CeaConfigModel#getMirrorInterval()
	 */
	public void startPlatformUpdateBehaviour() {
		PlatformUpdateBehaviour pub = this.getPlatformUpdateBehaviour();
		if (pub!=null) {
			this.addBehaviour(pub);
		}
	}
	/**
	 * Return the current platform update behaviour.
	 * @return the repository mirror behaviour
	 */
	public PlatformUpdateBehaviour getPlatformUpdateBehaviour() {
		if (updateBehaviour==null) {
			CeaConfigModel ceaConfigModel = this.getInternalDataModel().getCeaConfigModel();
			// --- Avoid starting the mirroring if interval was set to 0 ------
			if (ceaConfigModel!=null && ceaConfigModel.getMirrorInterval()!=0) {
				updateBehaviour = new PlatformUpdateBehaviour(this, PlatformUpdater.getDateOfNextMirrorOrUpdateInterval(0, ceaConfigModel.getMirrorInterval()));
			}
		}
		return updateBehaviour;
	}
	/**
	 * Sets the platform update behaviour.
	 * @param newPlatformUpdateBehaviour the new platform update behaviour
	 */
	public void setPlatformUpdateBehaviour(PlatformUpdateBehaviour newPlatformUpdateBehaviour) {
		if (this.updateBehaviour!=null) {
			this.removeBehaviour(this.updateBehaviour);
		}
		this.updateBehaviour = newPlatformUpdateBehaviour;
	}
	
	/**
	 * Starts the live monitoring subscription responder.
	 */
	private void startLiveMonitoringSubscriptionResponder() {
		if (this.liveMonitoringSubscriptionResponder==null) {
			this.liveMonitoringSubscriptionResponder = new LiveMonitoringSubscriptionResponder(this, LiveMonitoringSubscriptionResponder.getMessageTemplate());
			this.getMonitoringBehaviourRT().addMonitoringListener(this.liveMonitoringSubscriptionResponder);
			this.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(LiveMonitoringSubscriptionResponder.getMessageTemplate());
			this.addBehaviour(this.liveMonitoringSubscriptionResponder);
		}
	}
	
	/**
	 * Gets the live monitoring subscription responder.
	 * @return the live monitoring subscription responder
	 */
	protected LiveMonitoringSubscriptionResponder getLiveMonitoringSubscriptionResponder(){
		return this.liveMonitoringSubscriptionResponder;
	}

	
	/**
	 * Prints the specified message as error or info to the console.
	 *
	 * @param message the message
	 * @param isError the is error
	 */
	public void print(String message, boolean isError) {

		if (message==null || message.isBlank()==true) return;
		
		String msgPrefix = "[" + this.getLocalName() + "] ";
		String msgFinal  = msgPrefix + message; 
		// --- Print to console -----------------------
		if (isError==true) {
			System.err.println(msgFinal);
		} else {
			System.out.println(msgFinal);
		}
	}

	// ----------------------------------------------------
	// --- From here, UI access methods -------------------
	// ----------------------------------------------------	
	/**
	 * Returns the UI connector for this energy agent.
	 * @return the UI connector
	 */
	public EnergyAgentUiConnector getUIConnector() {
		if (uiConnector==null) {
			uiConnector = new EnergyAgentUiConnector(this);
		}
		return uiConnector;
	}
	/**
	 * Return a snapshot of the general runtime information of the agent .
	 * @return the general information
	 */
	public GeneralInformation getGeneralInformation() {
		return new GeneralInformation(this);
	}
	/**
	 * Returns a snapshot of the real time information.
	 * @return the real time information
	 */
	public RealTimeInformation getRealTimeInformation() {
		return new RealTimeInformation(this);
	}
	/**
	 * Returns the planning information.
	 * @return the planning information
	 */
	public PlanningInformation getPlanningInformation() {
		return new PlanningInformation(this);
	}

	
	// --------------------------------------------------------------
	// --- From here methods for performance measurements -----------
	// --------------------------------------------------------------	
	/**
	 * Checks if this energy agent does performance measurements.
	 * @return true, if performance measurements are to be done
	 */
	public boolean isDoPerformanceMeasurements() {
		return (this.isDoPerformanceMeasurements==true && this.performanceMeasuringAgent!=null && this.getLocalName().equals(this.performanceMeasuringAgent)==true);
	}
	/**
	 * Sets to do (or not) performance measurements for the specified agent.
	 *
	 * @param doIt the indicator to do performance measurements or not.
	 * @param eaLocalName the Energy Agents local name
	 */
	public void setDoPerformanceMeasurements(boolean doIt, String eaLocalName) {
		this.setDoPerformanceMeasurements(doIt, eaLocalName, null, null);
	}
	/**
	 * Sets to do (or not) performance measurements for the specified agent.
	 *
	 * @param doIt the indicator to do performance measurements or not.
	 * @param eaLocalName the Energy Agents local name
	 * @param noOfMeasurementsForAverage the optional number of measurements to build an average value; <code>null</code> is allowed
	 * @param individualMeasurementTaskNames the optional string array of individual measurement task names; <code>null</code> is allowed
	 */
	public void setDoPerformanceMeasurements(boolean doIt, String eaLocalName, Integer noOfMeasurementsForAverage, String[] individualMeasurementTaskNames) {
		this.isDoPerformanceMeasurements = doIt;
		this.performanceMeasuringAgent = eaLocalName;
		if (this.isDoPerformanceMeasurements==true) {
			this.registerPerformanceMeasurements(noOfMeasurementsForAverage, individualMeasurementTaskNames);
		}
	}
	/**
	 * Registers the performance measurements of the simulation manager if the
	 * local variable {@link #isDoPerformanceMeasurements} is set to true.
	 *
	 * @param noOfMeasurementsForAverage the optional number of measurements to build an average value; <code>null</code> is allowed
	 * @param individualMeasurementTaskNames the optional string array of individual measurement task names; <code>null</code> is allowed
	 */
	private void registerPerformanceMeasurements(Integer noOfMeasurementsForAverage, String[] individualMeasurementTaskNames) {
		
		if (this.isDoPerformanceMeasurements()==false) return;
			
		// --- Analyze individual tasks -----------------------------
		int noOfIndiTasks = 0;
		if (individualMeasurementTaskNames!=null && individualMeasurementTaskNames.length>0) {
			noOfIndiTasks = individualMeasurementTaskNames.length;
		}
		
		// --- Define a PerformanceGroup ----------------------------
		String[] pGroup = new String[5 + noOfIndiTasks];

		// --- Default tasks ----------------------------------------
		pGroup[0] = EnergyAgentPerformanceMeasurements.EA_PM_CONTROL_BEHAVIOUR_RT;
		pGroup[1] = EnergyAgentPerformanceMeasurements.EA_PM_CB_RT_STRATEGY_EXECUTION;
		
		pGroup[2] = EnergyAgentPerformanceMeasurements.EA_PM_CB_RT_SET_MEASUREMENTS;
		pGroup[3] = EnergyAgentPerformanceMeasurements.EA_PM_CB_RT_EXECUTE_EVALUATION;
		pGroup[4] = EnergyAgentPerformanceMeasurements.EA_PM_CB_RT_APPLY_SCHEDULE_LENGTH_RESTRICTION; 
		
		// --- Add individual tasks ---------------------------------
		if (noOfIndiTasks>0) {
			int destinIndex = 5;
			for (String task : individualMeasurementTaskNames) {
				pGroup[destinIndex] = task;
				destinIndex++;
			}
		}
		
		// --- Add as PerformanceGroup ------------------------------
		PerformanceMeasurements pm = PerformanceMeasurements.getInstance();
		pm.addPerformanceGroup("EnergyAgent Processes (" + this.getLocalName() + ")", pGroup, true);
		
		// --- Set number of steps to calculate an average ----------
		if (noOfMeasurementsForAverage!=null && noOfMeasurementsForAverage>0) {
			for (String task : pGroup) {
				pm.addPerformanceMeasurement(task, noOfMeasurementsForAverage);
			}
		}
	}
	/**
	 * Returns the singleton instance of the PerformanceMeasurements.
	 * @return the performance measurements
	 */
	public PerformanceMeasurements getPerformanceMeasurements() {
		if (this.isDoPerformanceMeasurements()==true) {
			return PerformanceMeasurements.getInstance();
		}
		return null;
	}
	/**
	 * Sets the specified measurement started.
	 * @param taskDescriptor the task descriptor
	 */
	public void setPerformanceMeasurementStarted(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setMeasurementStarted(taskDescriptor);
	}
	/**
	 * Sets the specified measurement finalized (applies regular and for loops).
	 * @param taskDescriptor the new measurement finalized
	 */
	public void setPerformanceMeasurementFinalized(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setMeasurementFinalized(taskDescriptor);
	}
	
	/**
	 * Sets the loop performance measurement started / looped.
	 * @param taskDescriptor the new loop performance measurement started
	 */
	public void setLoopPerformanceMeasurementStarted(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setLoopMeasurementStarted(taskDescriptor);
	}
	/**
	 * Sets the specified loop measurement finalized (applies regular and for loops).
	 * @param taskDescriptor the new measurement finalized
	 */
	public void setLoopPerformanceMeasurementFinalized(String taskDescriptor) {
		if (this.getPerformanceMeasurements()==null) return;
		this.getPerformanceMeasurements().setLoopMeasurementFinalized(taskDescriptor);
	}
	
}