package de.enflexit.ea.core.eomStateStream;

import java.util.List;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;

import agentgui.simulationService.agents.SimulationManagerAgent;
import agentgui.simulationService.time.TimeModelContinuous;
import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.AbstractStateInputStream;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import de.enflexit.eom.awb.stateStream.SystemStateDispatcher;
import de.enflexit.eom.awb.stateStream.SystemStateDispatcherAgentConnector;
import energy.EomController;
import energy.FixedVariableList;
import energy.GlobalInfo;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.EvaluationProcess;
import energy.helper.TechnicalSystemGroupHelper;
import energy.helper.TechnicalSystemHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.persistence.ScheduleList_StorageHandler;
import energy.schedule.ScheduleTransformer;
import energy.schedule.ScheduleTransformerDiscreteTime;
import energy.schedule.ScheduleTransformerKeyValue;
import energygroup.GroupController;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import energygroup.evaluation.IOSelectTreeAction;

/**
 * The Class EomModelStateInputStream handles the input stream that are to be produced by 
 * EOM models and is the default type to be used for energy agents.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class EomModelStateInputStream extends AbstractStateInputStream {

	protected boolean debug = false;
	protected String debugNetworkComponentID = "n4";
	
	private SystemStateDispatcherAgentConnector dispatchConnector;
	private AbstractStateQueueKeeper stateQueueKeeper;

	private Schedule scheduleEnergyTransmission;
	private Integer indexLastStateAccess;
	
	private TechnicalSystemStateEvaluation tsseAnswerNext;

	private Vector<String> variableIDsForSetPoints;

	private EomController realTimeStrategyController;
	private AbstractEvaluationStrategy realTimeStrategy;
	
	private TimeTrigger inputTimeTrigger;
	
	
	/**
	 * Instantiates a new EOM model input data stream.
	 * @param ioSimulated the instance of {@link AbstractIOSimulated} that owns this input stream 
	 */
	public EomModelStateInputStream(AbstractIOSimulated ioSimulated) {
		super(ioSimulated);
	}

	/**
	 * Checks if the operating mode of the instance is 'debug'. For this, the local variables {@link #debug} 
	 * and {@link #debugNetworkComponentID} will be evaluated. 
	 * 
	 * @return true, if the class is executed in the debug mode
	 */
	protected boolean isDebug() {
		return (this.debug==true && (this.debugNetworkComponentID==null || this.debugNetworkComponentID.equals(this.getNetworkComponent().getId())==true));	
	}
	
	/**
	 * Returns the NetworkComponent for which this input stream works.
	 * @return the network component
	 */
	public NetworkComponent getNetworkComponent() {
		return this.getIoSimulated().getNetworkComponent();
	}
	
	/**
	 * Return the SystemStateDispatcherAgentConnector for this stream.
	 * @return the system state dispatcher agent connector
	 */
	private SystemStateDispatcherAgentConnector getConnectorToSystemStateDispatcher() {
		if (dispatchConnector==null) {
			dispatchConnector = new SystemStateDispatcherAgentConnector(this.getIoSimulated().getEnergyAgent());
		}
		return dispatchConnector;
	}
	/**
	 * Resets connector to {@link SystemStateDispatcher}.
	 */
	private void resetConnectorToSystemStateDispatcher() {
		this.dispatchConnector = null;
	}
	
	
	/**
	 * Returns the state queue keeper that manages the reload actions for the local Schedule 
	 * for energy and state transmissions to the simulation manager.
	 * 
	 * @return the state queue keeper
	 */
	protected AbstractStateQueueKeeper getStateQueueKeeper() {
		return stateQueueKeeper;
	}
	/**
	 * Sets the state queue monitor.
	 * @param stateQueueKeeper the new state queue monitor
	 */
	public void setStateQueueKeeper(AbstractStateQueueKeeper abstractStateQueueKeeper) {
		this.stateQueueKeeper = abstractStateQueueKeeper;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, methods for the preparation ahead simulation can be found --------- 
	// ----------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.eomStateStream.AbstractStateInputStream#prepareForSimulation()
	 */
	@Override
	public void prepareForSimulation() {

		// --- Invoke connector to dispatcher - reset if not needed -----------
		boolean requiresStateQueueKeeper = this.getConnectorToSystemStateDispatcher().isGlobalScheduleTimeRangeDefined();
		if (requiresStateQueueKeeper==false) this.resetConnectorToSystemStateDispatcher(); 

		// --- Get NetworkComponent's data model to work on ------------------- 
		Schedule scheduleToUse = null;
		NetworkComponent netComp = this.getIoSimulated().getNetworkComponent();
		Object dataModelOfNetworkComponent = netComp.getDataModel();

		// --- Get the start time of simulation -------------------------------
		Long startTime = this.getIoSimulated().getTime();
		
		// --- First check for data model -------------------------------------
		if (dataModelOfNetworkComponent==null) {
			// --- Try to load from storage settings --------------------------
			dataModelOfNetworkComponent = EomModelLoader.loadEomModel(netComp);
		}
		
		// --- Check data model and prepare -----------------------------------
		if (dataModelOfNetworkComponent==null) {
			// --- No data model specified for NetworkComponent ---------------
			System.err.println("[IOSimulated] No data model specified for NetworkComponent " + netComp.getId());
			
		} else {
			// --- A data model is specified ----------------------------------
			if (this.getIoSimulated().getExecutionDataBase()==ExecutionDataBase.SensorData) {
				// ------------------------------------------------------------
				// --- Simulation based on Sensor data ------------------------
				// ------------------------------------------------------------
				if (netComp.getType().equals("Sensor") && dataModelOfNetworkComponent instanceof Object[] ) {
					// --- Found an object array as data model ----------------
					Object[] dataModelArrayOfNetworkComponent = (Object[]) dataModelOfNetworkComponent;
					for (int i = 0; i < dataModelArrayOfNetworkComponent.length; i++) {
						if (dataModelArrayOfNetworkComponent[i] instanceof ScheduleList) {
							// --- Found the ScheduleList of the Sensor -------
							ScheduleList scheduleList = (ScheduleList) dataModelArrayOfNetworkComponent[i];
							if (scheduleList.getSchedules().size()>0) {
								scheduleToUse = scheduleList.getSchedules().get(0);
								// --- Start a queue keeper? ------------------
								if (requiresStateQueueKeeper==true) {
									this.setStateQueueKeeper(new StateQueueKeeperScheduleList(this, this.getConnectorToSystemStateDispatcher()));
								}
								break;
							}
						}
					}
				}
				
			} else {
				// ------------------------------------------------------------
				// --- Simulation based on power node information -------------
				// ------------------------------------------------------------
				if (dataModelOfNetworkComponent instanceof ScheduleList) {
					// --- ScheduleList ---------------------------------------
					ScheduleList scheduleList = (ScheduleList) dataModelOfNetworkComponent;
					if (scheduleList.getSchedules().size()>0) {
						scheduleToUse = scheduleList.getSchedules().get(0); 
						// --- Start a queue keeper? ------------------
						if (requiresStateQueueKeeper==true) {
							this.setStateQueueKeeper(new StateQueueKeeperScheduleList(this, this.getConnectorToSystemStateDispatcher()));
						}
					}

				} else if (dataModelOfNetworkComponent instanceof TechnicalSystem) {
					// --------------------------------------------------------
					// --- TechnicalSystem ------------------------------------
					// --------------------------------------------------------
					TechnicalSystem technicalSystem = (TechnicalSystem) dataModelOfNetworkComponent;
					TechnicalSystemHelper.adjustEvaluationStartTime(technicalSystem, startTime);
					OptionModelController omc = new OptionModelController();
					omc.setTechnicalSystem(technicalSystem);
					omc.setControllingAgent(this.getIoSimulated().getEnergyAgent());
					// --- Execute evaluation ---------------------------------
					EvaluationProcess ep = omc.getEvaluationProcess();
					// --- Try to get real time strategy ----------------------
					AbstractEvaluationStrategyRT realTimeStrategy = (AbstractEvaluationStrategyRT) omc.getEvaluationStrategyRT();
					if (realTimeStrategy==null) {
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						// +++ Use static schedule from evaluation ++++++++++++
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						ep.startEvaluation();	
						this.waitForEvaluationProcess(ep);
						// --- Get resulting schedule -------------------------
						ScheduleList scheduleList = ep.getEvaluationResults();
						scheduleToUse = scheduleList.getSchedules().get(0);
						// --- Start a queue keeper? ------------------
						if (requiresStateQueueKeeper==true) {
							this.setStateQueueKeeper(new StateQueueKeeperTechnicalSystem(this, this.getConnectorToSystemStateDispatcher()));
						}
					} else {
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						// +++ Implement RealTimeStrategy +++++++++++++++++++++
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						this.setRealTimeStrategyController(omc);
						this.setRealTimeStrategy(new IOSetPointStrategyTechnicalSystemRT(omc, this.getIoSimulated(), realTimeStrategy.getInitialStateAdaptionConfigured(), netComp.getId()));
					}
					// --- Determine set point variables for the system -------
					this.setVariableIDsForSystemSetPoints(technicalSystem.getSystemVariables());
					
				} else if (dataModelOfNetworkComponent instanceof TechnicalSystemGroup) {
					// --------------------------------------------------------
					// --- TechnicalSystemGroup -------------------------------
					// --------------------------------------------------------
					TechnicalSystemGroup systemGroup = (TechnicalSystemGroup) dataModelOfNetworkComponent;
					TechnicalSystemGroupHelper.adjustEvaluationStartTime(systemGroup, startTime);
					GroupController gc = new GroupController();
					gc.setTechnicalSystemGroup(systemGroup);
					OptionModelController omc = gc.getGroupOptionModelController();
					omc.setControllingAgent(this.getIoSimulated().getEnergyAgent());
					// --- Execute evaluation ---------------------------------
					EvaluationProcess ep = omc.getEvaluationProcess();
					// --- Try to get real time strategy ----------------------
					AbstractGroupEvaluationStrategyRT realTimeStrategy = (AbstractGroupEvaluationStrategyRT) omc.getEvaluationStrategyRT();
					if (realTimeStrategy==null) {
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						// +++ Use static schedule from evaluation ++++++++++++
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						ep.startEvaluation();	
						this.waitForEvaluationProcess(ep);
						// --- Get resulting schedule -------------------------
						ScheduleList scheduleList = ep.getEvaluationResults();
						if (scheduleList.getSchedules().size()>0) {
							scheduleToUse = scheduleList.getSchedules().get(0);
						} else {
							scheduleToUse = null;
						}
						
						// --- Start a queue keeper? ------------------
						if (requiresStateQueueKeeper==true) {
							this.setStateQueueKeeper(new StateQueueKeeperTechnicalSystemGroup(this, this.getConnectorToSystemStateDispatcher()));
						}
					} else {
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						// +++ Implement RealTimeStrategy +++++++++++++++++++++
						// ++++++++++++++++++++++++++++++++++++++++++++++++++++
						this.setRealTimeStrategyController(gc);
						this.setRealTimeStrategy(new IOSetPointStrategyTechnicalSystemGroupRT(omc, this.getIoSimulated(), realTimeStrategy.getInitialStateAdaptionConfigured(), netComp.getId()));
					}
					// --- Determine set point variables for the system -------
					this.setVariableIDsForSystemSetPoints(systemGroup.getTechnicalSystem().getSystemVariables());
					
				}
			}
		}
		
		// --- Set the schedule that is to be used for simulation -------------
		this.setScheduleEnergyTransmission(this.getScheduleToExecuteTransformed(scheduleToUse));
	}
	/**
	 * Waits for the end of an evaluation process.
	 */
	protected void waitForEvaluationProcess(EvaluationProcess ep) {
		while (ep.isRunning()==true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	/**
	 * Will transform the specified Schedule to a timely equidistant Schedule (for discrete simulations) or
	 * to a Schedule that corresponds to the energy transmission configuration in the HyGrid settings.
	 * In case that sensor data are used for the simulations, the Schedule will be returned as is.
	 *
	 * @param scheduleToChekc the schedule to check
	 * @return the transformed schedule
	 */
	public Schedule getScheduleToExecuteTransformed(Schedule scheduleToCheck) {
		
		Schedule transformedSchedule = scheduleToCheck;
		if (scheduleToCheck!=null && this.getIoSimulated().getExecutionDataBase()!=ExecutionDataBase.SensorData) {

			try {
				// --- Get the schedule for the energy transmission -----------
				ScheduleTransformer st = null;
				switch (this.getIoSimulated().getTimeModelType()) {
				case TimeModelDiscrete:
					st = new ScheduleTransformerDiscreteTime(scheduleToCheck, this.getIoSimulated().getTimeModelDiscrete().getTimeStart(), this.getIoSimulated().getTimeModelDiscrete().getStep());	
					transformedSchedule = st.getTransformedSchedule();
					break;
					
				case TimeModelContinuous:
					// --- Check state transmission type ----------------------
					switch (this.getIoSimulated().getStateTransmissionConfiguration()) {
					case AsDefined:
						transformedSchedule = SerialClone.clone(scheduleToCheck);
						break;

					case Reduced:
						st = new ScheduleTransformerKeyValue(scheduleToCheck, this.getIoSimulated().getEnergyTransmissionConfiguration());
						transformedSchedule = st.getTransformedSchedule();
						break;
					}
					break;
				}
				
			} catch (Exception ex) {
				System.err.println("[" + this.getClass().getSimpleName() + "][" + this.getNetworkComponent().getId() + "] Error while transforming the Schedule to prepare for the execution:");
				ex.printStackTrace();
			}
		}
		
		if (this.isDebug()==true) {
			String displayText = "System States before transformation: " + scheduleToCheck.getNumberOfStates() + ", System States ater transformation: " + transformedSchedule.getNumberOfStates();
			System.out.println("[" + this.getClass().getSimpleName() + "][" + this.getNetworkComponent().getId() + "] " + displayText);
		}
		return transformedSchedule;
	}
	
	/**
	 * Sets the schedule for the state and energy transmission over time. <b>This must be a so called list Schedule!</b>  
	 * @param scheduleEnergyTransmission the new schedule energy transmission
	 * @see ScheduleList_StorageHandler#convertToListSchedule(Schedule)
	 */
	public void setScheduleEnergyTransmission(Schedule scheduleEnergyTransmission) {
		
		ScheduleList_StorageHandler.convertToListSchedule(scheduleEnergyTransmission);
		this.scheduleEnergyTransmission = scheduleEnergyTransmission;
		if (this.scheduleEnergyTransmission!=null) {
			this.scheduleEnergyTransmission.setRealTimeSchedule(true, false);
			if (this.scheduleEnergyTransmission!=null && this.scheduleEnergyTransmission.getTechnicalSystemStateList().size()>0) {
				this.indexLastStateAccess = (scheduleEnergyTransmission.getTechnicalSystemStateList().size()-1);
			} else {
				this.indexLastStateAccess = null;
			}	
		}
	}
	/**
	 * Returns the Schedule for the energy and state transmission to the {@link SimulationManagerAgent}
	 * according to the current time model. This Schedule is only be used for {@link ScheduleList}'s or 
	 * systems that have no real time control behaviour.
	 * 
	 * @return the Schedule for the energy and state transmission to the SimulationManager
	 */
	public Schedule getScheduleEnergyTransmission() {
		return scheduleEnergyTransmission;
	}
	
	/**
	 * Adds the specified Schedule to schedule energy transmission.
	 * @param schedule the schedule to add 
	 */
	public void addToScheduleEnergyTransmission(Schedule scheduleToAdd) {
		
		if (scheduleToAdd==null) return;

		// --- Transform the Schedule according to simulation type --
		Schedule transformedSchedule = this.getScheduleToExecuteTransformed(scheduleToAdd);
		// --- Convert to list Schedule -----------------------------
		ScheduleList_StorageHandler.convertToListSchedule(transformedSchedule);

		// --- Add to the transmission Schedule ---------------------
		int sizeOld = this.getScheduleEnergyTransmission().getTechnicalSystemStateList().size();
		for (int i=transformedSchedule.getTechnicalSystemStateList().size()-1; i>=0; i--) {
			this.addToScheduleEnergyTransmission(transformedSchedule.getTechnicalSystemStateList().get(i));
		}
		// --- Apply length restriction check -----------------------
		int sizeInBetween = this.getScheduleEnergyTransmission().getTechnicalSystemStateList().size();
		this.getScheduleEnergyTransmission().applyScheduleLengthRestriction(this.getIoSimulated().getTime());
		
		if (this.isDebug()==true) {

			String msgPrefix = "[" + this.getClass().getSimpleName() + "][" + this.getNetworkComponent().getId() + "] ";
			
			//scheduleToAdd.updateStateTimes();
			//long timeDeliveredFrom = scheduleToAdd.getStateTimeFrom();
			//long timeDeliveredTo = scheduleToAdd.getStateTimeTo();
			
			//transformedSchedule.updateStateTimes();
			//long timeTransforemdFrom = transformedSchedule.getStateTimeFrom();
			//long timeTransforemdTo = transformedSchedule.getStateTimeTo();
			
			//SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			//System.out.println(msgPrefix + "Time Range of delivered data   - From: " + sdf.format(new Date(timeDeliveredFrom)) + ", To: " +sdf.format(new Date(timeDeliveredTo)));
			//System.out.println(msgPrefix + "Time Range of transformed data - From: " + sdf.format(new Date(timeTransforemdFrom)) + ", To: " +sdf.format(new Date(timeTransforemdTo)));
			
			int sizeNew = this.getScheduleEnergyTransmission().getTechnicalSystemStateList().size();
			System.out.println(msgPrefix + "Number of states in transmision queue before/between/after: " + sizeOld + "/" + sizeInBetween + "/" + sizeNew + " => added " + transformedSchedule.getTechnicalSystemStateList().size() + " new states.");
		}
	}
	/**
	 * Adds the the specified {@link TechnicalSystemStateEvaluation} to the local Schedule for the energy and state transmission.
	 * The states added should have a greater global time then the first element in the state list of the Schedule (descending order!).
	 * 
	 * @param tsseToAdd the TechnicalSystemStateEvaluation to add
	 */
	public void addToScheduleEnergyTransmission(TechnicalSystemStateEvaluation tsseToAdd) {

		int oldIndex = -1;
		if (this.getIndexLastStateAccess()!=null) {
			oldIndex = this.getIndexLastStateAccess();
		}
		int newIndex = oldIndex + 1;
		
		List<TechnicalSystemStateEvaluation> tsseList = this.getScheduleEnergyTransmission().getTechnicalSystemStateList();
		long timeInListLast = tsseList.get(0).getGlobalTime();
		long timeAddToList  = tsseToAdd.getGlobalTime(); 
		if (timeAddToList > timeInListLast) {
			tsseList.add(0, tsseToAdd);
			this.setIndexLastStateAccess(newIndex);
		}
	}
	
	/**
	 * Returns the last index that was accessed and used 
	 * to transfer state data to the simulation manager.
	 * 
	 * @return the index last state access
	 */
	public Integer getIndexLastStateAccess() {
		return indexLastStateAccess;
	}
	/**
	 * Sets the last index state access.
	 * @param newIndex the new index last state access
	 */
	protected void setIndexLastStateAccess(Integer newIndex) {
		this.indexLastStateAccess = newIndex;
	}
	
	/**
	 * Returns the current real time strategy, if any.
	 * @return the real time strategy
	 */
	public AbstractEvaluationStrategy getRealTimeStrategy() {
		return realTimeStrategy;
	}
	/**
	 * Sets the current real time strategy.
	 * @param realTimeStrategy the new real time strategy
	 */
	protected void setRealTimeStrategy(AbstractEvaluationStrategy realTimeStrategy) {
		this.realTimeStrategy = realTimeStrategy;
	}
	
	/**
	 * Returns the real time strategy controller that is either an {@link OptionModelController} or a {@link GroupController}.
	 * @return the real time strategy controller
	 */
	public EomController getRealTimeStrategyController() {
		return realTimeStrategyController;
	}
	/**
	 * Sets the real time strategy controller that is either an {@link OptionModelController} or a {@link GroupController}..
	 * @param realTimeStrategyController the new real time strategy controller
	 */
	protected void setRealTimeStrategyController(EomController realTimeStrategyController) {
		this.realTimeStrategyController = realTimeStrategyController;
	}
	
	/**
	 * Gets the variable id for system set points.
	 * @return the variable id for system set points
	 */
	public Vector<String> getVariableIDsForSystemSetPoints() {
		return variableIDsForSetPoints;
	}
	/**
	 * Gets the variable ID's for system set points.
	 * @param variableIDs the variable ID's
	 */
	public void setVariableIDsForSystemSetPoints(Vector<String> variableIDs) {
		this.variableIDsForSetPoints = variableIDs;
	}
	/**
	 * Sets the variable ID's for set points out of a systems list of SystemVariableDefinition.
	 * @param sysVarDefs the new variable ID's for system set points
	 */
	public void setVariableIDsForSystemSetPoints(List<SystemVariableDefinition> sysVarDefs) {
		Vector<String> idVector = new Vector<String>();
		for (SystemVariableDefinition sysVarDef : sysVarDefs) {
			if (sysVarDef.isSetPoint()==true && sysVarDef.isSetPointForUser()==false) {
				idVector.add(sysVarDef.getVariableID());
			}
		}
		this.setVariableIDsForSystemSetPoints(idVector);
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, methods for pausing and stopping can be found --------------------- 
	// ----------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.eomStateStream.AbstractStateInputStream#setPauseSimulation(boolean)
	 */
	@Override
	public void setPauseSimulation(boolean isPauseSimulation) {
		
		AbstractIOSimulated ioSimulated = this.getIoSimulated();
		if (ioSimulated.getTimeModelType()==TimeModelType.TimeModelContinuous) {
			
			TimeModelContinuous tmCont = ioSimulated.getTimeModelContinuous();
			if (isPauseSimulation==true) {
				// --- Set time model to not executed ---------------
				if (tmCont.isExecuted()==true) {
					synchronized (tmCont) {
						tmCont.setExecuted(false);
					}
				}
				// --- Pause internal Threads -----------------------
				if (this.inputTimeTrigger!=null) {
					this.inputTimeTrigger.pause();
				}
				
			} else {
				// --- Set time model to executed -------------------				
				if (tmCont.isExecuted()==false) {
					synchronized (tmCont) {
						tmCont.setExecuted(true);
					}
				}
				// --- Restart internal Threads ---------------------
				if (this.inputTimeTrigger!=null) {
					this.inputTimeTrigger.resume();
				}
				
			}
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.eomStateStream.AbstractStateInputStream#setStopSimulation()
	 */
	@Override
	public void stopSimulation() {
		this.stopTimeTriggerForSystemInput();
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, methods for the simulation runtime can be found ------------------- 
	// ----------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractStateInputStream#getSystemState(long)
	 */
	@Override
	public TechnicalSystemStateEvaluation getSystemStateForTimeModelContinuous(long simTime) {
		
		TechnicalSystemStateEvaluation tsseAnswer = null;
		AbstractIOSimulated ioSimulated = this.getIoSimulated();
		
		if (this.getRealTimeStrategy()!=null) {
			// --------------------------------------------------------
			// --- Work on the real time strategy for this system ----- 
			// --------------------------------------------------------
			TechnicalSystemStateEvaluation tsseLocal = this.getTechnicalSystemStateEvaluationFromRealTimeStrategy(simTime);
			if (tsseLocal!=null) {
				// --- Adjust system state regarding parent and time --
				tsseAnswer = this.getTechnicalSystemStateEvaluationCloneWithoutParent(tsseLocal);
			}
			
		} else {
			// --------------------------------------------------------
			// --- Work on the static schedule for this system --------
			// --------------------------------------------------------
			tsseAnswer = this.getTechnicalSystemStateEvaluation4Time(simTime, true);	
		}
		
		// --- Start the process according to the simulated time ------  
		if (this.getRealTimeStrategy()!=null) {
			// --------------------------------------------------------
			// --- Work with the real time strategy for this system --- 
			// --------------------------------------------------------
			if (tsseAnswer!=null) {
				this.startTimeTriggerForSystemInputRT(tsseAnswer.getGlobalTime());
			} else {
				// --- Send that this IO will not send further information --
				ioSimulated.getSimulationConnector().sendManagerNotification(STATE_CONFIRMATION.Done);
			}
			
		} else {
			// --------------------------------------------------------
			// --- Work on the static schedule for this system --------
			// --------------------------------------------------------
			this.startTimeTriggerForSystemInput();
		}
		return tsseAnswer;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractStateInputStream#getSystemState(long, long)
	 */
	@Override
	public TechnicalSystemStateEvaluation getSystemStatesForTimeModelDiscrete(long simTime, long timeStep) {
		
		TechnicalSystemStateEvaluation tsseAnswer = null;
		AbstractIOSimulated ioSimulated = this.getIoSimulated();
		long simStepBegin = simTime - timeStep;
		if (this.getRealTimeStrategy()!=null) {
			// --------------------------------------------------------
			// --- Work on the real time strategy for this system ----- 
			// --------------------------------------------------------
			TechnicalSystemStateEvaluation tsseLocal = this.getTechnicalSystemStateEvaluationFromRealTimeStrategy(simTime);
			if (tsseLocal!=null) {
				tsseAnswer = TechnicalSystemStateHelper.copyTechnicalSystemstateEvaluationWithLimitedParents(tsseLocal, simStepBegin);
			}
			
		} else {
			// --------------------------------------------------------
			// --- Work on the static schedule for this system --------
			// --------------------------------------------------------
			if (this.tsseAnswerNext!=null && this.tsseAnswerNext.getGlobalTime()==simTime) {
				tsseAnswer = this.tsseAnswerNext;
			} else {
				TechnicalSystemStateEvaluation scheduleStateForTime = this.getTechnicalSystemStateEvaluation4Time(simTime, false, false);
				tsseAnswer = TechnicalSystemStateHelper.copyTechnicalSystemstateEvaluationWithLimitedParents(scheduleStateForTime, simStepBegin);
			}
			
			// ------------------------------------------------------------
			// --- Get the system state for the next time step ------------
			// ------------------------------------------------------------
			long simTimeNext = simTime + ioSimulated.getTimeModelDiscrete().getStep();
			TechnicalSystemStateEvaluation scheduleStateForNextTime = this.getTechnicalSystemStateEvaluation4Time(simTimeNext - timeStep, false, false);
			tsseAnswer = TechnicalSystemStateHelper.copyTechnicalSystemstateEvaluationWithLimitedParents(scheduleStateForNextTime, simStepBegin);
			// ------------------------------------------------------------
		}
		return tsseAnswer;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.eomStateStream.AbstractStateInputStream#getIOSettings(long, energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public FixedVariableList getIOSettings(long simTime, TechnicalSystemStateEvaluation tsseAnswer) {

		FixedVariableList ioSettings = null;

		if (tsseAnswer!=null) {

			if (this.getIoSimulated().getTimeModelType()==TimeModelType.TimeModelContinuous) {
				// --- Slightly adjust state time to simulation start ---------
				if (tsseAnswer.getGlobalTime()-tsseAnswer.getStateTime()==simTime) {
					tsseAnswer.setStateTime(tsseAnswer.getStateTime()+1);
				}
			}
			
			// --- Check for real time group evaluation strategy --------------
			AbstractGroupEvaluationStrategyRT rtStrategyTSG = null;
			if (this.getRealTimeStrategy() instanceof AbstractGroupEvaluationStrategyRT) {
				rtStrategyTSG = (AbstractGroupEvaluationStrategyRT) this.getRealTimeStrategy();
			}
			
			// --- Prepare IO settings ----------------------------------------
			if (rtStrategyTSG!=null) {
				IOSelectTreeAction ioSelect = new IOSelectTreeAction(this.getIoSimulated().getInternalDataModel().getGroupController(), rtStrategyTSG);
				ioSettings = ioSelect.getIoListsOfAggregation();
			} else {
				ioSettings = new FixedVariableList(); 
				ioSettings.addAll(tsseAnswer.getIOlist());
			}
		}
		return ioSettings;
	}

	
	
	/**
	 * Returns the system state from the current real time strategy for the specified time.
	 *
	 * @param simStepEndTime the sim step end time
	 * @return the technical system state evaluation from real time strategy
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluationFromRealTimeStrategy(long simStepEndTime) {
		
		AbstractEvaluationStrategy rtStrategy = this.getRealTimeStrategy();
		if (rtStrategy==null) return null;

		TechnicalSystemStateEvaluation tsseLocal = null;
		if (rtStrategy instanceof AbstractEvaluationStrategyRT) {
			// --- Control a single system ------------------------
			AbstractEvaluationStrategyRT rtStrategyTS = (AbstractEvaluationStrategyRT) rtStrategy; 
			rtStrategyTS.runEvaluationUntil(simStepEndTime); 
			tsseLocal = rtStrategyTS.getTechnicalSystemStateEvaluation();
			
		} else if (rtStrategy instanceof AbstractGroupEvaluationStrategyRT) {
			// --- Control an aggregation -------------------------
			AbstractGroupEvaluationStrategyRT rtStrategyTSG = (AbstractGroupEvaluationStrategyRT) rtStrategy;
			rtStrategyTSG.runEvaluationUntil(simStepEndTime); 
			tsseLocal = rtStrategyTSG.getTechnicalSystemStateEvaluation();
		}
		return tsseLocal;	
	}
	
	/**
	 * Gets the technical system state evaluation 4 time. Returns a clone without history.
	 *
	 * @param simulationTime the simulation time
	 * @param isGetNextState the is get next state
	 * @return the technical system state evaluation 4 time
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation4Time(long simulationTime, boolean isGetNextState) {
		return this.getTechnicalSystemStateEvaluation4Time(simulationTime, isGetNextState, true);
	}
	
	
	/**
	 * Returns the TechnicalSystemStateEvaluation for the specified time.
	 *
	 * @param simulationTime the simulation time
	 * @param isGetNextState set true, if you want to get the next system state
	 * @param createClone specifies if the method should return a clone without history (true), or the original TSSE (false)
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation4Time(long simulationTime, boolean isGetNextState, boolean createClone) {
		
		Schedule schedule = this.getScheduleEnergyTransmission();
		if (schedule==null) return null;
		
		TechnicalSystemStateEvaluation tsse4Time = null;
		if (schedule.getTechnicalSystemStateEvaluation()!=null & schedule.getTechnicalSystemStateList().size()==0) {
			tsse4Time = this.getTechnicalSystemStateEvaluation4TimeTreeSchedule(schedule, simulationTime, isGetNextState, createClone);
		} else if (schedule.getTechnicalSystemStateList().size()!=0) {
			tsse4Time = this.getTechnicalSystemStateEvaluation4TimeListSchedule(schedule, simulationTime, isGetNextState, createClone);
		}
		return tsse4Time;
	}
	
	/**
	 * Returns the TechnicalSystemStateEvaluation for the specified time from a list organized Schedule.
	 *
	 * @param schedule the schedule
	 * @param simulationTime the simulation time
	 * @param isGetNextState set true, if you want to get the next system state
	 * @param createClone the create clone
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation4TimeListSchedule(Schedule schedule, long simulationTime, boolean isGetNextState, boolean createClone) {
		
		if (schedule==null) return null;
		
		TechnicalSystemStateEvaluation tsse4Time = null;
		TechnicalSystemStateEvaluation tsseWork = null;
		int iMax = schedule.getTechnicalSystemStateList().size()-1;
		
		int iStart = -1;
		if (this.indexLastStateAccess==null) {
			iStart = iMax;
		} else {
			iStart = this.indexLastStateAccess;
		}
		if (iStart<0) return null;
		if (iStart>=schedule.getTechnicalSystemStateList().size()) iStart = iMax;
		
		// --- Check start time of state --------------------------------------
		tsseWork = schedule.getTechnicalSystemStateList().get(iStart);
		if ((tsseWork.getGlobalTime() - tsseWork.getStateTime()) > simulationTime) {
			// --- Restart search from list end (timely begin of Schedule) ---- 
			iStart = iMax;
			// --- Check if the first state is before simulation time ---------
			tsseWork = schedule.getTechnicalSystemStateList().get(iStart);
			if ((tsseWork.getGlobalTime() - tsseWork.getStateTime()) > simulationTime) {
				return null;
			}
		}
		
		for (int i=iStart; i>=0; i--) {
			// ----------------------------------------------------------------
			// --- Is the whole time range to be considered? ------------------
			// ----------------------------------------------------------------
			tsseWork = schedule.getTechnicalSystemStateList().get(i);
			boolean tsseFound = false;
			if (isGetNextState==true) {
				// --- Time range ---------------------------------------------
				long timeTo = tsseWork.getGlobalTime(); 
				long timeFrom = timeTo - tsseWork.getStateTime();
				tsseFound = (simulationTime>=timeFrom & simulationTime<timeTo);
			} else {
				// --- Exact time ---------------------------------------------
				tsseFound = (tsseWork.getGlobalTime()==simulationTime);
			}
			// ----------------------------------------------------------------
			if (tsseFound==true) {
				this.indexLastStateAccess = i;
				break;
			}
		}
		
		// --- Trigger the queue keeper? --------------------------------------
		if (this.getStateQueueKeeper()!=null) this.getStateQueueKeeper().onPickedTechnicalSystemState();

		// --- Something found ? ----------------------------------------------
		if (tsseWork!=null) {
			if (createClone==true) {
				tsse4Time = this.getTechnicalSystemStateEvaluationCloneWithoutParent(tsseWork);
			} else {
				tsse4Time = tsseWork;
			}
		}
		return tsse4Time;
	}
	
	/**
	 * Returns the TechnicalSystemStateEvaluation for the specified time from a tree organized Schedule.
	 *
	 * @param simulationTime the simulation time
	 * @param isGetNextState set true, if you want to get the next system state
	 * @return the technical system state evaluation
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation4TimeTreeSchedule(Schedule schedule, long simulationTime, boolean isGetNextState, boolean createClone) {
	
		if (schedule==null) return null;
		
		TechnicalSystemStateEvaluation tsse4Time = null;
		TechnicalSystemStateEvaluation tsseWork = schedule.getTechnicalSystemStateEvaluation();
		if (tsseWork!=null) {
			
			boolean tsseFound = false;
			while (tsseFound==false) {
				if (tsseWork.getParent()==null) {
					tsseWork = null;
					break;
				} else {
					tsseWork = tsseWork.getParent();
				}
				// ------------------------------------------------------------
				// --- Is the whole time range to be considered? --------------
				// ------------------------------------------------------------
				if (isGetNextState==true) {
					// --- Time range -----------------------------------------
					long timeTo = tsseWork.getGlobalTime(); 
					long timeFrom = timeTo - tsseWork.getStateTime();
					tsseFound = (simulationTime>=timeFrom & simulationTime<timeTo);
				} else {
					// --- Exact time -----------------------------------------
					tsseFound = (tsseWork.getGlobalTime()==simulationTime);
				}
				// ------------------------------------------------------------
			}
			// --- Something found ? ------------------------------------------
			if (tsseWork!=null) {
				if (createClone==true) {
					tsse4Time = this.getTechnicalSystemStateEvaluationCloneWithoutParent(tsseWork);
				} else {
					tsse4Time = tsseWork;
				}
			}
		}
		return tsse4Time;
	}
	
	/**
	 * Returns a clone of the specified {@link TechnicalSystemStateEvaluation} without without a parent.
	 *
	 * @param tsseLocal the tsse local
	 * @return the technical system state evaluation clone without parent
	 */
	private TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluationCloneWithoutParent(TechnicalSystemStateEvaluation tsseLocal) {
		
		TechnicalSystemStateEvaluation tsseAnswer = null;
		if (tsseLocal!=null) {
			// --- Temporally, remove parent system state ---------------
			synchronized (tsseLocal) {
				TechnicalSystemStateEvaluation tsseParent = tsseLocal.getParent();
				tsseLocal.setParent(null);
				tsseAnswer = SerialClone.clone(tsseLocal);
				if (tsseParent!=null) {
					tsseLocal.setParent(tsseParent);
				}
			}
		}
		return tsseAnswer;
	}
	
	
	/**
	 * Starts a TimeTrigger for the system input, if a static {@link Schedule} is used with the {@link TimeModelContinuous}.
	 */
	private void startTimeTriggerForSystemInput() {

		// --- Maybe, stop the current TimeTrigger ------------------
		this.stopTimeTriggerForSystemInput();
		
		// --- Send that this IO will not send further information --
		if (this.getScheduleEnergyTransmission()==null) {
			this.getIoSimulated().getSimulationConnector().sendManagerNotification(STATE_CONFIRMATION.Done);
			return;
		}
		
		// --- Start the TimeTrigger --------------------------------
		if (this.isDebug()==true) {
			System.out.println("[" + this.getClass().getSimpleName() + "][" + this.getNetworkComponent().getId() + "] Starting TimeTrigger!");
		}
		this.inputTimeTrigger = new TimeTrigger(true, this.getIoSimulated().getTimeModelContinuous(), this.getScheduleEnergyTransmission(), new TimeTriggerListener() {
			/* (non-Javadoc)
			 * @see de.enflexit.energyAgent.core.TimeTriggerListener#fireTimeTrigger(long)
			 */
			@Override
			public Long fireTimeTrigger(long triggerTime) {
				// --- Get the next system state --------------------
				TechnicalSystemStateEvaluation tsse = getTechnicalSystemStateEvaluation4Time(triggerTime, true);
				// --- Set the measurements of this IO-interface ----
				FixedVariableList ioSettings = null;
				if (tsse!=null) {
					// --- Prepare IO settings ----------------------
					ioSettings = new FixedVariableList(); 
					ioSettings.addAll(tsse.getIOlist());
				}
				// --- Set IO Settings of this behaviour ------------
				EomModelStateInputStream.this.getIoSimulated().setMeasurementsFromSystem(ioSettings);
				// --- Inform manager -------------------------------
				EomModelStateInputStream.this.getIoSimulated().getSimulationConnector().sendManagerNotification(tsse);
				// --- Some debug output, if configured -------------
				if (EomModelStateInputStream.this.isDebug()==true) {
					String displayText = "Trigger time: " + GlobalInfo.getInstance().getDateTimeAsString(triggerTime, "dd.MM.yyyy hh:mm:ss") + " => ";
					displayText += "TSSE time: " + GlobalInfo.getInstance().getDateTimeAsString(tsse.getGlobalTime() - tsse.getStateTime(), "dd.MM.yyyy hh:mm:ss,SSS");
					displayText += " to " + GlobalInfo.getInstance().getDateTimeAsString(tsse.getGlobalTime(), "hh:mm:ss,SSS");
					System.out.println("\n[" + TimeTrigger.class.getSimpleName() + "][" + EomModelStateInputStream.this.getNetworkComponent().getId() + "] " + displayText);
				}
				return null;
			}
			/* (non-Javadoc)
			 * @see de.enflexit.energyAgent.core.TimeTriggerListener#setTimeTriggerFinalized()
			 */
			@Override
			public void setTimeTriggerFinalized() {
				EomModelStateInputStream.this.getIoSimulated().getSimulationConnector().sendManagerNotification(STATE_CONFIRMATION.Done);
			}
		});	
		this.inputTimeTrigger.setDebug(this.isDebug());
		this.inputTimeTrigger.setMillisecondsToStopTimeTrigger(this.getIoSimulated().getTimeModelContinuous().getTimeStop());
		this.inputTimeTrigger.executeInNewThread(this.getIoSimulated().getEnergyAgent().getLocalName() + "_IO");
	}
	
	/**
	 * Starts a TimeTrigger for the system input, if a dynamic system is used with the {@link TimeModelContinuous}.
	 * @param startTime the initial start time for the {@link TimeTrigger}
	 */
	private void startTimeTriggerForSystemInputRT(long startTime) {
		
		// --- Maybe, stop the current TimeTrigger ------------------
		this.stopTimeTriggerForSystemInput();
		
		// --- Start the TimeTrigger --------------------------------
		this.inputTimeTrigger = new TimeTrigger(true, this.getIoSimulated().getTimeModelContinuous(), startTime, new TimeTriggerListener() {
			/* (non-Javadoc)
			 * @see de.enflexit.energyAgent.core.TimeTriggerListener#fireTimeTrigger(long)
			 */
			@Override
			public Long fireTimeTrigger(long triggerTime) {
				
				// ----------------------------------------------------------------------
				// --- Directly transmit the next system state --------------------------
				// ----------------------------------------------------------------------
				AbstractEvaluationStrategyRT rtStrategyTS = null;
				AbstractGroupEvaluationStrategyRT rtStrategyTSG = null;
				TechnicalSystemStateEvaluation tsseLocal = null;

				// --- Get the real time strategy to determine current system state ----- 
				AbstractEvaluationStrategy rtStrategy = getRealTimeStrategy();
				if (rtStrategy instanceof AbstractEvaluationStrategyRT) {
					// --- IOSetPointStrategyTechnicalSystemRT --------------------------
					rtStrategyTS = (AbstractEvaluationStrategyRT) getRealTimeStrategy();
					tsseLocal = rtStrategyTS.getTechnicalSystemStateEvaluation();
							
				} else if (rtStrategy instanceof AbstractGroupEvaluationStrategyRT) {
					// --- IOSetPointStrategyTechnicalSystemGroupRT ---------------------
					rtStrategyTSG = (AbstractGroupEvaluationStrategyRT) getRealTimeStrategy();
					tsseLocal = rtStrategyTSG.getTechnicalSystemStateEvaluation();
				}
				
				if (tsseLocal.getGlobalTime() > EomModelStateInputStream.this.getIoSimulated().getTimeModelContinuous().getTimeStart()) {
					// --- Adjust state regarding parent and time -----------------------
					TechnicalSystemStateEvaluation tsse = getTechnicalSystemStateEvaluationCloneWithoutParent(tsseLocal);
					// --- Set the measurements of this IO-interface --------------------
					FixedVariableList ioSettings = null;
					if (tsse!=null) {
						// --- Prepare IO settings --------------------------------------
						if (rtStrategyTSG!=null) {
							IOSelectTreeAction ioSelect = new IOSelectTreeAction((GroupController) getRealTimeStrategyController(), rtStrategyTSG);
							ioSettings = ioSelect.getIoListsOfAggregation();
						} else {
							ioSettings = new FixedVariableList(); 
							ioSettings.addAll(tsse.getIOlist());
						}
					}
					// --- Set IO Settings of this behaviour ----------------------------
					EomModelStateInputStream.this.getIoSimulated().setMeasurementsFromSystem(ioSettings);
					// --- Inform manager about state -----------------------------------
					EomModelStateInputStream.this.getIoSimulated().getSimulationConnector().sendManagerNotification(tsse);
				}
				
				// ----------------------------------------------------------------------
				// --- Either wait for the time below or a changes of the set points ----
				// ----------------------------------------------------------------------
				long timeForStateRepetition = tsseLocal.getGlobalTime() + tsseLocal.getStateTime();
				long sleepTime = inputTimeTrigger.getTimeToSleep(timeForStateRepetition);
				if (sleepTime>0) {
					try {
						Object setPointSynchronizer = EomModelStateInputStream.this.getIoSimulated().getSetPointSynchronizer();
						synchronized (setPointSynchronizer) {
							setPointSynchronizer.wait(sleepTime);
						}
						
					} catch (InterruptedException ie) {
						//ie.printStackTrace();
					}
				}
				
				// --- Evaluate the next step -------------------------------------------
				if (rtStrategyTS!=null) {
					// --- For TechnicalSystem ------------
					rtStrategyTS.runEvaluationUntil(tsseLocal.getGlobalTime()+1); 
					tsseLocal = rtStrategyTS.getTechnicalSystemStateEvaluation();
				}
				if (rtStrategyTSG!=null) {
					// --- For TechnicalSystemGroup -------
					rtStrategyTSG.runEvaluationUntil(tsseLocal.getGlobalTime()+1); 
					tsseLocal = rtStrategyTSG.getTechnicalSystemStateEvaluation();
				}

				// --- Return next event time -------------------------------------------
				return tsseLocal.getGlobalTime();
			}
			/* (non-Javadoc)
			 * @see de.enflexit.energyAgent.core.TimeTriggerListener#setTimeTriggerFinalized()
			 */
			@Override
			public void setTimeTriggerFinalized() {
				EomModelStateInputStream.this.getIoSimulated().getSimulationConnector().sendManagerNotification(STATE_CONFIRMATION.Done);
			}
		});
		this.inputTimeTrigger.setDebug(this.isDebug());
		this.inputTimeTrigger.setMillisecondsToStopTimeTrigger(this.getIoSimulated().getTimeModelContinuous().getTimeStop());
		this.inputTimeTrigger.executeInNewThread(this.getIoSimulated().getEnergyAgent().getLocalName() + "_IO");
	}
	/**
	 * Stop TimeTrigger for the system input.
	 */
	private void stopTimeTriggerForSystemInput() {
		if (this.inputTimeTrigger!=null) {
			this.inputTimeTrigger.stopTimeTrigger();
			this.inputTimeTrigger = null;
		}
	}

}
