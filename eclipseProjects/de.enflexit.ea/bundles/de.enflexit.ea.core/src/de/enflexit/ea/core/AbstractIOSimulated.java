package de.enflexit.ea.core;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.awb.simulation.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import de.enflexit.awb.simulation.environment.EnvironmentModel;
import de.enflexit.awb.simulation.environment.time.TimeModelContinuous;
import de.enflexit.awb.simulation.environment.time.TimeModelDiscrete;
import de.enflexit.awb.simulation.transaction.DisplayAgentNotification;
import de.enflexit.awb.simulation.transaction.EnvironmentNotification;
import de.enflexit.ea.core.AbstractInternalDataModel.ControlledSystemType;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.StateTransmission;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.core.dataModel.simulation.ControlBehaviourRTStateUpdate;
import de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorRegistration;
import de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorInterface;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep;
import de.enflexit.ea.core.dataModel.simulation.RTControlRegistration;
import de.enflexit.ea.core.eomStateStream.EomModelStateInputStream;
import energy.FixedVariableList;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleTransformerKeyValueConfiguration;
import jade.core.AID;
import jade.core.behaviours.Behaviour;

/**
 * The Class IOSimulated is used to simulate measurements from an energy conversion 
 * process, if the current setup is used in simulations.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractIOSimulated extends Behaviour implements EnergyAgentIO {

	private static final long serialVersionUID = 3659353219575016108L;

	private boolean isDone = false;
	private boolean isPaused = false;

	private EnvironmentModel myEnvironmentModel;
	private NetworkComponent networkComponent;

	private AbstractEnergyAgent energyAgent;
	private SimulationConnector simConnector;
	private AbstractStateInputStream stateInputStream;
	
	private FixedVariableList measurements;
	private FixedVariableList setPoints;

	private Object setPointSynchronizer;
	
	private boolean isSetUpdateSystemStateFromControlBehaviourRT;
	
	private AbstractDiscreteSimulationEvent discreteSimulationEvent;
	
	
	/**
	 * Instantiates a new simulated IO behaviour. Assuming {@link AgentOperatingMode} simulation as default case.
	 * @param energyAgent the current {@link AbstractEnergyAgent}
	 */
	public AbstractIOSimulated(AbstractEnergyAgent energyAgent) {
		super(energyAgent);
		this.energyAgent = energyAgent;
		this.getSimulationConnector(energyAgent.getAgentOperatingMode()).pickEnvironmentModelAndStart();
	}
		
	/**
	 * Initializes this behaviour.
	 * @param environmentModel the environment model
	 */
	public void initialize(EnvironmentModel environmentModel) {
		
		try {
			
			// --- Set the current environment model first ------------------------------
			if (environmentModel==null) return;
			this.setEnvironmentModel(environmentModel);
			
			// --- Put some information to the agents internal data model ---------------
			this.getInternalDataModel().setProjectProperties(environmentModel.getProjectProperties());
			this.getInternalDataModel().setSetupProperties(environmentModel.getSetupProperties());
			this.getInternalDataModel().setHyGridAbstractEnvironmentModel(this.getHyGridAbstractEnvironmentModel());
			this.getInternalDataModel().setNetworkModel(this.getNetworkModel(), false);
			this.getInternalDataModel().setNetworkComponent(this.getNetworkComponent());
			
			// --- In case of a TimeModelContinuous register Agent to the time model ----
			if (this.getTimeModelType()==TimeModelType.TimeModelContinuous) {
				this.getTimeModelContinuous().setTimeAskingAgent(this.getEnergyAgent());
			}
			
			// --- Prepare the details for the simulation -------------------------------
			this.getStateInputStream().prepareForSimulation();
			
			// --- Enable individual preparation (e.g. for a voltage measurement) -------
			this.prepareForSimulation(this.getNetworkModel());

			
			// --------------------------------------------------------------------------
			// --- Register RT strategy controlled system at simulation manager? --------
			if (this.getEnergyAgent().isExecutedControlBehaviourRT()==true) {
				this.getSimulationConnector().sendManagerNotification(new RTControlRegistration());
			}
			// --- Register an iterating strategy at the simulation manager? ------------
			if (this.requiresEnvironmentModelInformation()==true || this.isDiscreteIteratingRTStrategy()==true) {
				this.getSimulationConnector().sendManagerNotification(new DiscreteIteratorRegistration());
			}
			// --- Send null TSSE as registration for states from control behaviour? ----
			if (this.isSetTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel()==true) {
				this.sendControlBehaviourRTStateUpdateToEnvironmentModel(null);
			}
			
			
			// --- finalize the setup of the energy agent -------------------------------
			this.getEnergyAgent().onEnvironmentModelSet();

			// --- Send notification to simulation manager that this agent is ready -----
			this.getSimulationConnector().sendManagerNotification(STATE_CONFIRMATION.Done);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Prepare for simulation. Do the things required, before the simulation will be executed (e.g. requesting information from the {@link BlackboardAgent}
	 * @param networkModel the current network model
	 */
	protected abstract void prepareForSimulation(NetworkModel networkModel); 

	
	// ------------------------------------------------------------------------
	// --- From here, methods for calls within simulations and testbeds -------
	// ------------------------------------------------------------------------
	/**
	 * Returns the current instance of the energy agent, if needed.
	 * @return the energy agent
	 */
	public AbstractEnergyAgent getEnergyAgent() {
		return this.energyAgent;
	}
	/**
	 * Gets the internal data model.
	 * @return the internal data model
	 */
	public AbstractInternalDataModel<? extends EnergyAgentPhoneBookEntry> getInternalDataModel() {
		return this.getEnergyAgent().getInternalDataModel();
	}
	
	/**
	 * Sets the environment model for this IO.
	 * @param myEnvironmentModel the new environment model
	 */
	protected void setEnvironmentModel(EnvironmentModel myEnvironmentModel) {
		this.myEnvironmentModel = myEnvironmentModel;
	}
	/**
	 * Returns the {@link EnvironmentModel}.
	 * @return the environment model
	 */
	public EnvironmentModel getEnvironmentModel() {
		return myEnvironmentModel;
	}
	/**
	 * Return the HyGridAbstractEnvironmentModell.
	 * @return the HyGridAbstractEnvironmentModel
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		if (this.myEnvironmentModel!=null) {
			return  (HyGridAbstractEnvironmentModel) this.myEnvironmentModel.getAbstractEnvironment();
		}
		return null;
	}
	/**
	 * returns the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		if (this.myEnvironmentModel!=null) {
			return  (NetworkModel) this.myEnvironmentModel.getDisplayEnvironment();
		}
		return null;
	}
	/**
	 * Returns the NetworkComponent to work on.
	 * @return the network component
	 */
	public NetworkComponent getNetworkComponent() {
		NetworkModel networkModel = this.getNetworkModel(); 
		if (networkModel!=null && networkComponent==null) {
			NetworkComponent netCompTemp = networkModel.getNetworkComponent(this.getEnergyAgent().getLocalName());
			if (netCompTemp!=null) {
				networkComponent = netCompTemp.getCopy();
			}
		}
		return networkComponent;
	}
	
	/**
	 * Return the currently used the time model type.
	 * @return the time model type
	 */
	public TimeModelType getTimeModelType() {
		HyGridAbstractEnvironmentModel hyGridSettings = this.getHyGridAbstractEnvironmentModel();
		if (hyGridSettings!=null) {
			return hyGridSettings.getTimeModelType();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getTime()
	 */
	@Override
	public Long getTime() {
		if (this.getTimeModelType()!= null) {
			switch (this.getTimeModelType()) {
			case TimeModelDiscrete:
				return this.getTimeModelDiscrete().getTime();			
			case TimeModelContinuous:
				return this.getTimeModelContinuous().getTime();
			}
		}
		return null;
	}
	/**
	 * Returns the current {@link TimeModelDiscrete}, if this is currently used.
	 * @return the time model continuous
	 */
	public TimeModelDiscrete getTimeModelDiscrete() {
		if (this.myEnvironmentModel.getTimeModel() instanceof TimeModelDiscrete) {
			return (TimeModelDiscrete) this.myEnvironmentModel.getTimeModel();
		}
		return null;
	}
	/**
	 * Returns the current {@link TimeModelContinuous}, if this is currently used.
	 * @return the time model continuous
	 */
	public TimeModelContinuous getTimeModelContinuous() {
		if (this.myEnvironmentModel.getTimeModel() instanceof TimeModelContinuous) {
			return (TimeModelContinuous) this.myEnvironmentModel.getTimeModel();
		}
		return null;
	}
	
	/**
	 * Return the current execution data base for the simulation.
	 * @return the execution data base
	 */
	public ExecutionDataBase getExecutionDataBase() {
		return this.getInternalDataModel().getHyGridAbstractEnvironmentModel().getExecutionDataBase();
	}
	
	/**
	 * Returns the kind of {@link StateTransmission} define in the {@link HyGridAbstractEnvironmentModel}.
	 * @return the StateTransmission configuration
	 */
	public StateTransmission getStateTransmissionConfiguration() {
		HyGridAbstractEnvironmentModel hyGridSettings = this.getHyGridAbstractEnvironmentModel();
		if (hyGridSettings!=null) {
			return hyGridSettings.getStateTransmission();
		}
		return null;
	}
	/**
	 * Returns the HyGrid energy transmission configuration as {@link ScheduleTransformerKeyValueConfiguration}.
	 * @return the energy transmission configuration
	 */
	public ScheduleTransformerKeyValueConfiguration getEnergyTransmissionConfiguration() {
		HyGridAbstractEnvironmentModel hyGridSettings = this.getHyGridAbstractEnvironmentModel();
		if (hyGridSettings!=null) {
			return hyGridSettings.getEnergyTransmissionConfiguration();
		}
		return null;
	}
	
	/**
	 * Returns the simulation connector.
	 * @return the simulation connector
	 */
	public SimulationConnector getSimulationConnector() {
		return this.getSimulationConnector(AgentOperatingMode.Simulation);
	}
	/**
	 * Returns (or initializes) the simulation connector.
	 * @param operatingMode the operating mode of the Energy agent. Only required for a testbed environment 
	 * @return the simulation connector
	 */
	public SimulationConnector getSimulationConnector(AgentOperatingMode operatingMode) {
		if (simConnector==null) {
			// --- Use SimulationConnectorRemote if running in one of the two testbed modes, SimulationConnectorLocal otherwise
			if (operatingMode == AgentOperatingMode.TestBedSimulation || operatingMode == AgentOperatingMode.TestBedReal) {
				simConnector = new SimulationConnectorRemote(this.getEnergyAgent(), this);
			} else {
				simConnector = new SimulationConnectorLocal(this.getEnergyAgent(), this);
			}
		}
		return simConnector;
	}
	
	/**
	 * Return the state input stream to be used during simulations. As default, the {@link EomModelStateInputStream} will
	 * be used here. Overwrite this method to provide an individual handling of data to be used within simulations.
	 * 
	 * @return the state input stream
	 * @see AbstractStateInputStream
	 * @see EomModelStateInputStream
	 */
	public AbstractStateInputStream getStateInputStream() {
		if (stateInputStream==null) {
			stateInputStream = new EomModelStateInputStream(this);
		}
		return stateInputStream;
	}
	

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onEnd()
	 */
	@Override
	public int onEnd() {
		this.getSimulationConnector().onEnd();
		return super.onEnd();
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#done()
	 */
	@Override
	public boolean done() {
		return isDone;
	}
	/**
	 * Sets the isDone.
	 * @param isDone the new isDone
	 */
	public void setDone(boolean done) {
		this.isDone = done;
	}
	
	/**
	 * Sends a manager notification.
	 *
	 * @param notification the notification
	 * @return true, if successful
	 */
	public boolean sendManagerNotification(Object notification) {
		return this.getSimulationConnector().sendManagerNotification(notification);
	}
	/**
	 * Sends an agent notification.
	 *
	 * @param receiverAID the receiver AID
	 * @param notification the notification
	 * @return true, if successful
	 */
	public boolean sendAgentNotification(AID receiverAID, Object notification) {
		return this.getSimulationConnector().sendAgentNotification(receiverAID, notification);
	}
	/**
	 * Sends display agent notification.
	 * @param displayAgentNotification the display agent notification
	 */
	public void sendDisplayAgentNotification(DisplayAgentNotification displayAgentNotification) {
		this.getSimulationConnector().sendDisplayAgentNotification(displayAgentNotification);
	}
	
	/**
	 * Filters the specified environment notification for simulation purposes. Can not be overridden.
	 *
	 * @param notification the notification
	 * @return the environment notification
	 */
	protected final EnvironmentNotification filterEnvironmentNotification(EnvironmentNotification notification){
		
		if (notification.isComingFromManager()==true && notification.getNotification() instanceof DiscreteSimulationStep && this.getEnergyAgent().isExecutedControlBehaviourRT()==true) {
			// --- Work on a DiscreteSimulationStep coming from a central decision process -------- 
			DiscreteSimulationStep dsStep = (DiscreteSimulationStep) notification.getNotification();
			this.getEnergyAgent().getControlBehaviourRT().setSetPointsFromTechnicalSystemStateEvaluation(dsStep.getSystemState());
			return notification;
		}
		
		return this.onEnvironmentNotification(notification);
	}
	/**
	 * This method will be executed if an EnvironmentNotification arrives this agent.
	 * @param notification the notification
	 */
	protected EnvironmentNotification onEnvironmentNotification(EnvironmentNotification notification){
		return notification;
	}
	
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		// --- Nothing to do here yet -----------
		this.block(5000);
	}
	
	/**
	 * Will be invoked, if changes in the environment occur.
	 */
	public void onEnvironmentStimulus() {
		
		HyGridAbstractEnvironmentModel hyGridSettings = (HyGridAbstractEnvironmentModel) this.myEnvironmentModel.getAbstractEnvironment();
		SimulationStatus simState = hyGridSettings.getSimulationStatus();

		// --------------------------------------------------------------------
		// --- Case separation of simulation status ---------------------------
		// --------------------------------------------------------------------
		if (simState.getState()==STATE.A_DistributeEnvironmentModel) {
			// ----------------------------------------------------------------
			// --- Prepare this agent for the simulation ----------------------
			// --- => Already done within the constructor of this class. ------
			// ----------------------------------------------------------------
			
		} else if (simState.getState()==STATE.B_ExecuteSimuation) {
			// ----------------------------------------------------------------
			// --- Start / continue simulation --------------------------------
			// ----------------------------------------------------------------
			long simTime = 0;
			
			TechnicalSystemStateEvaluation tsseAnswer = null;
			
			switch (this.getTimeModelType()) {
			case TimeModelDiscrete:
				// -- Get current time ----------------------------------------
				simTime = this.getTime();
				tsseAnswer = this.getStateInputStream().getSystemStatesForTimeModelDiscrete(simTime, this.getTimeModelDiscrete().getStep());
				break;
				
			case TimeModelContinuous:
				// --- Only used at the start of the simulation ---------------
				simTime = this.getTimeModelContinuous().getTimeStart();
				tsseAnswer = this.getStateInputStream().getSystemStateForTimeModelContinuous(simTime);
				break;
			}
			
			// --- Get time depending system state ----------------------------
			FixedVariableList ioSettings = this.getStateInputStream().getIOSettings(simTime, tsseAnswer);

			// --- Callback method to interfere before the new measurements are committed to the agent
			if (this.getTimeModelType()==TimeModelType.TimeModelDiscrete) {
				this.getDiscreteSimulationEvent().internalBeforeMeasurementCommit();
			}
			
			// --- Set IO Settings of this behaviour --------------------------
			// --- => In discrete simulations, ControlBehaviourRT will not  
			// --- => be handled by the agents internal schedule controller,
			// --- => but directly by invoking the action method  
			this.setMeasurementsFromSystem(ioSettings);

			// --- Set system effect to the environment -----------------------
			switch (this.getTimeModelType()) {
			case TimeModelDiscrete:
				// ------------------------------------------------------------
				// --- Get the discrete SystemStateType of the IO simulated ---
				// ------------------------------------------------------------
				// --- RT controlled / require external information? ----------
				boolean isRTControlledSystem = this.getEnergyAgent().isExecutedControlBehaviourRT(); 
				boolean isRequiresEnvModelInformation = this.requiresEnvironmentModelInformation(); 
				DiscreteSimulationStep dsStep = null;
				if (isRTControlledSystem==false) {
					// --- Not RT controlled - (data source is ScheduleList) --
					dsStep = new DiscreteSimulationStep(tsseAnswer, DiscreteSystemStateType.Final);					
				} else {
					// --- RT controlled !!! ----------------------------------
					if (isRequiresEnvModelInformation==true) {
						// --- Take the current state as initial system state - 
						dsStep = new DiscreteSimulationStep(tsseAnswer, DiscreteSystemStateType.Iteration);
					} else {
						// -- Get system state from RT behaviour --------------
						dsStep = this.getEnergyAgent().getControlBehaviourRT().getDiscreteSimulationStep();
					}
				}
				
				// --- Callback method to interfere before the new state is transmitted
				this.getDiscreteSimulationEvent().internalBeforeSimulationStateTransmission();
				// --- Send DiscreteSimulationStep to manager agent -----------
				this.getSimulationConnector().setMyStimulusAnswer(dsStep);
				break;

			case TimeModelContinuous:
				// --- Only used at the start of the simulation ---------------
				this.getSimulationConnector().setMyStimulusAnswer(tsseAnswer);
				break;
			}
			
		} else if (simState.getState()==STATE.C_StopSimulation) {
			// ----------------------------------------------------------------
			// --- Stop simulation --------------------------------------------
			// ----------------------------------------------------------------
			this.getSimulationConnector().sendManagerNotification(STATE_CONFIRMATION.Done);
			this.block();
		}
	}
	
	// ------------------------------------------------------------------------
	// --- From here specific methods for discrete simulations can be found --- 
	// ------------------------------------------------------------------------
	/**
	 * Checks if the configured evaluation strategy is a discrete iterating RT strategy. For this, a real time strategy class 
	 * needs to implement the {@link DiscreteIteratorInterface}. For a continuous time model used in a simulation, this
	 * method always returns <code>false</code>.
	 * 
	 * @return true, if the configured evaluation strategy is able (in principle) to iterate within a single evaluation time step
	 */
	public boolean isDiscreteIteratingRTStrategy() {
		
		AbstractEvaluationStrategy strategy = null; 
		ControlledSystemType csType = this.getInternalDataModel().getTypeOfControlledSystem();
		
		if (this.getTimeModelType()==TimeModelType.TimeModelDiscrete) {
			if (csType==ControlledSystemType.TechnicalSystem) {
				strategy = this.getInternalDataModel().getOptionModelController().getEvaluationStrategyRT();
			} else if (csType==ControlledSystemType.TechnicalSystemGroup) {
				strategy = this.getInternalDataModel().getGroupController().getGroupOptionModelController().getEvaluationStrategyRT();
			}
			if (strategy!=null && strategy instanceof DiscreteIteratorInterface) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Sets the DiscreteSimulationStep (a system state and a type marker) to environment model that is managed by the Simulation Manager.
	 *
	 * @param tsse the new technical system state to be set to the environment model
	 * @param systemStateType the system state type
	 */
	public void setDiscreteSimulationStepToEnvironment(TechnicalSystemStateEvaluation tsse, DiscreteSystemStateType systemStateType) {
		if (tsse!=null) {
			DiscreteSimulationStep dsStep = new DiscreteSimulationStep(tsse, systemStateType);
			this.sendManagerNotification(dsStep);
		}
	}
	
	// ------------------------------------------------------------------------
	// --- From here specific methods for ControlBehaviourRTStateUpdate's ----- 
	// ------------------------------------------------------------------------
	/**
	 * Returns if the end system state of the real time control behaviour is to be used as state in the environment model, too.
	 * By default this method returns <code>false</code> to produce a regular simulation behaviour.
	 *  
	 * @return true, if the simulation step should be overwritten by the end state of the {@link ControlBehaviourRT}
	 * @see #setTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel(boolean)
	 */
	public boolean isSetTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel() {
		return this.isSetUpdateSystemStateFromControlBehaviourRT;
	}
	/**
	 * Sets that the {@link TechnicalSystemStateEvaluation} from {@link ControlBehaviourRT} should be used to update the environment model.
	 * @param isSetUpdateSystemStateFromControlBehaviourRT the indicator to use the state from the {@link ControlBehaviourRT} or not
	 */
	public void setTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel(boolean isSetUpdateSystemStateFromControlBehaviourRT) {
		this.isSetUpdateSystemStateFromControlBehaviourRT = isSetUpdateSystemStateFromControlBehaviourRT;
	}
	/**
	 * Sends the specified {@link TechnicalSystemStateEvaluation} to the simulation manager, to update
	 * the currently used system state.
	 * @param tsse the TechnicalSystemStateEvaluation to be used to update the environment model
	 */
	public void sendControlBehaviourRTStateUpdateToEnvironmentModel(TechnicalSystemStateEvaluation tsse) {
		if (this.isSetTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel()==true) {
			this.sendManagerNotification(new ControlBehaviourRTStateUpdate(TechnicalSystemStateHelper.copyTechnicalSystemStateEvaluationWithoutParent(tsse)));
		}
	}
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	
	
	/**
	 * Stop TimeTrigger for the system input.
	 */
	public void stopSimulation() {
		this.getStateInputStream().stopSimulation();
	}
	
	/**
	 * Sets to pause the current simulation.
	 * @param isPauseSimulation the new pause simulation
	 */
	public void setPauseSimulation(boolean isPauseSimulation) {
		this.isPaused = isPauseSimulation;
		this.getStateInputStream().setPauseSimulation(this.isPaused);
	}
	/**
	 * Checks if the simulation is currently paused.
	 * @return true, if is paused simulation
	 */
	public boolean isPausedSimulation() {
		return isPaused;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getMeasurementsFromSystem()
	 */
	@Override
	public FixedVariableList getMeasurementsFromSystem() {
		if (this.measurements==null) {
			this.measurements = new FixedVariableList();
		}
		return this.measurements;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#setMeasurementsFromSystem(de.enflexit.energyAgent.core.FixedVariableList)
	 */
	@Override
	public void setMeasurementsFromSystem(FixedVariableList newMeasurements) {
		this.measurements = newMeasurements;
		if (this.commitMeasurementsToAgentsManually()==false) {
			// --- Set new measurements to the internal data model of the Energy Agent ------
			this.commitMeasurementsToAgent();
		}
	}
	
	/**
	 * Answers the question, if the simulated IO interface requires information from the environment model before new
	 * measurement values can be committed to an energy agent (e.g. after a network calculation to get artificial sensor data).
	 * @return true, if the simulated IO interface requires environment model information
	 */
	public final boolean requiresEnvironmentModelInformation() {
		return this.commitMeasurementsToAgentsManually();
	}
	
	/**
	 * Should return true, if further measurements have to be acquired for the currents system (e.g. by the SimulationManager or Blackboard). 
	 * Do not forget to commit the measurements to the agent by using {@link #commitMeasurementsToAgent()}
	 * @return true, if the commit for the measurements will be isDone manually
	 * @see #commitMeasurementsToAgent()
	 */
	protected abstract boolean commitMeasurementsToAgentsManually();
	
	/**
	 * Commits the current measurements to the energy Agent.
	 */
	protected void commitMeasurementsToAgent() {
		// --- Callback method to interfere before the agent makes its control decision
		if (this.getTimeModelType()==TimeModelType.TimeModelDiscrete) {
			this.getDiscreteSimulationEvent().internalBeforeControlDecision();
		}
		this.getInternalDataModel().setMeasurementsFromSystem(this.getMeasurementsFromSystem());
	}
	

	/**
	 * Returns the set point synchronizer that can be used to wait that the set points were updated
	 * by a call of method  {@link #setSetPointsToSystem(FixedVariableList)}. So, this method is about
	 * threading. A thread may wait for the mentioned event and will be notified.  
	 *
	 * @return the set point synchronizer
	 * @see #setSetPointsToSystem(FixedVariableList)
	 * @see Thread#wait()
	 */
	public Object getSetPointSynchronizer() {
		if (setPointSynchronizer==null) {
			setPointSynchronizer = new Object();
		}
		return setPointSynchronizer;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#setSetPointsToSystem(de.enflexit.energyAgent.core.FixedVariableList)
	 */
	@Override
	public void setSetPointsToSystem(FixedVariableList newSetPointSettings) {
		this.setPoints = newSetPointSettings;
		// --- If initiated, notify about set point change ----------
		if (this.setPointSynchronizer!=null) {
			synchronized (this.setPointSynchronizer) {
				this.setPointSynchronizer.notifyAll();
			}
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getSetPointsToSystem()
	 */
	public FixedVariableList getSetPointsToSystem() {
		return this.setPoints;
	}

	
	
	/**
	 * Gets the discrete simulation event. Override to implement an agent-specific discrete simulation event class.
	 * @return the discrete simulation event
	 */
	protected AbstractDiscreteSimulationEvent getDiscreteSimulationEvent() {
		if (discreteSimulationEvent==null) {
			discreteSimulationEvent = new DefaultDiscreteSimulationEvent();
		}
		return discreteSimulationEvent;
	}
	
	
}