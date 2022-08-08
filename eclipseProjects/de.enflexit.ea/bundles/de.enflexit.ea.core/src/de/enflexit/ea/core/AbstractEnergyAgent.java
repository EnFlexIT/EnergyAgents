package de.enflexit.ea.core;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.core.application.Application;
import agentgui.core.config.GlobalInfo.DeviceSystemExecutionMode;
import agentgui.core.config.GlobalInfo.ExecutionMode;
import agentgui.core.project.setup.AgentClassElement4SimStart;
import agentgui.simulationService.environment.EnvironmentModel;
import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.common.ontology.AgentStartArgument;
import de.enflexit.common.ontology.AgentStartConfiguration;
import de.enflexit.common.ontology.OntologyClassTreeObject;
import de.enflexit.common.ontology.OntologyVisualizationHelper;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import de.enflexit.ea.core.behaviour.DefaultMessageReceiveBehaviour;
import de.enflexit.ea.core.behaviour.LiveMonitoringSubscriptionResponder;
import de.enflexit.ea.core.behaviour.O2ABehaviour;
import de.enflexit.ea.core.behaviour.PhoneBookRegistrationBehaviour;
import de.enflexit.ea.core.behaviour.PlatformUpdateBehaviour;
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
import de.enflexit.ea.core.monitoring.MonitoringBehaviourRT;
import de.enflexit.ea.core.monitoring.MonitoringListenerForLogging;
import de.enflexit.ea.core.monitoring.MonitoringListenerForLogging.LoggingDestination;
import energy.FixedVariableList;
import energy.optionModel.FixedVariable;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemState;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
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
	
	
	private AgentOperatingMode operatingMode;
	
	private EnergyAgentIO agentIOBehaviour;
	private SimulationConnector simulationConnector;

	protected DefaultMessageReceiveBehaviour defaultMessageReceiveBehaviour;

	private ControlBehaviourRT controlBehaviourRT;
	
	private ThreadedBehaviourFactory threadedBehaviourFactory;
	private Behaviour threadedMonitoringBehaviour;
	protected MonitoringBehaviourRT monitoringBehaviourRT;
	private MonitoringListenerForLogging logWriter;
	private LiveMonitoringSubscriptionResponder liveMonitoringSubscriptionResponder;
	
	private PlatformUpdateBehaviour updateBehaviour;
	
	/**
	 * Returns the internal data model of this agent.
	 * @return the internal data model
	 */
	public abstract AbstractInternalDataModel getInternalDataModel();
	

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
	 * On environment model set (for Nils).
	 */
	protected final void onEnvironmentModelSet() {
		// --- Register at the central phone book if necessary --------------------------
		this.addBehaviour(new PhoneBookRegistrationBehaviour(this, this.getInternalDataModel().getCentralAgentAID(), true));
		// --- Call the individual setup method for energy agents ----------------------- 
		this.setupEnergyAgent();	
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
		
		// --- Stop the log writer --------------------------------------------
		this.stopSystemStateLogWriter();
		this.stopMonitoringBehaviourRT();
		
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
	protected Codec getAgentCodec() {
		return new SLCodec();
	}
	/**
	 * Gets the ontology for this agent. Override to specify your own ontology.
	 * @return the agent ontology
	 */
	protected Ontology getAgentOntology() {
		return HyGridOntology.getInstance();
	}
	
	/**
	 * Gets the ontology for OPS interactions. Override to specify your own ontology.
	 * @return the ops ontology
	 */
	protected Ontology getOpsOntology() {
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
					operatingMode = agentInfo.getAgentOperatingMode();
				}
			}
			// --- Use 'real' as backup solution ----------------------------------------
			if (operatingMode==null) {
				System.out.println("[" + this.getLocalName() + "] Use backup operating mode 'RealSystem'.");
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
	protected LoggingDestination getLoggingDestination() {
		return LoggingDestination.EomDatabase;
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
					if (dm instanceof ScheduleList) {
						// ----------------------------------------------------
						// --- ScheduleList -----------------------------------
						this.getInternalDataModel().getScheduleController().setScheduleList((ScheduleList) dm);

					} else if (dm instanceof TechnicalSystem){
						// ----------------------------------------------------
						// --- TechnicalSystems -------------------------------
						if (this.getInternalDataModel().getBundleModelForTechnicalSystem()==null) {
							this.getInternalDataModel().getOptionModelController().setTechnicalSystem((TechnicalSystem) dm);
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
						this.getInternalDataModel().getGroupController().setTechnicalSystemGroup((TechnicalSystemGroup) dm);
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
			System.out.println(this.getLocalName() + ": Resetting monitoring strategy");
			this.getMonitoringBehaviourRT().resetEvaluationProcess();
		}
		if (this.controlBehaviourRT != null) {
			System.out.println(this.getLocalName() + ": Resetting control strategy");
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
	
}