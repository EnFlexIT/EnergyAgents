package de.enflexit.ea.core.behaviour;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import agentgui.simulationService.time.TimeModelDiscrete;
import de.enflexit.common.DateTimeHelper;
import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.AbstractInternalDataModel.ControlledSystemType;
import de.enflexit.ea.core.EnergyAgentIO;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.SnapshotDecisionLocation;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.core.dataModel.simulation.DiscreteIteratorInterface;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStepCentralDecision;
import energy.EomController;
import energy.FixedVariableList;
import energy.FixedVariableListForAggregation;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.evaluation.decision.AbstractDecisionSwitch;
import energy.helper.DisplayHelper;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.FixedBoolean;
import energy.optionModel.FixedDouble;
import energy.optionModel.FixedInteger;
import energy.optionModel.FixedVariable;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionBoolean;
import energy.optionModel.SystemVariableDefinitionDouble;
import energy.optionModel.SystemVariableDefinitionInteger;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemGroupStateEvaluation;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.planning.EomPlannerResult;
import energygroup.GroupController;
import energygroup.GroupTreeNodeObject;
import energygroup.evaluation.AbstractGroupEvaluationStrategy;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import energygroup.evaluation.AbstractGroupTreeAction;
import energygroup.evaluation.MemberEvaluationStrategy;
import energygroup.evaluation.SubStateCollectorTreeAction;
import jade.core.behaviours.CyclicBehaviour;

/**
 * The Class MonitoringBehaviourRT will be used in case that a real time control 
 * was assigned as a systems default evaluation strategy.
 * 
 * @see AbstractEvaluationStrategyRT
 * @see AbstractGroupEvaluationStrategyRT
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ControlBehaviourRT extends CyclicBehaviour implements Observer {

	private static final long serialVersionUID = -1460453061132067175L;

	private AbstractEnergyAgent energyAgent;
	private AbstractInternalDataModel<? extends EnergyAgentPhoneBookEntry> internalDataModel;
	private EnergyAgentIO agentIOBehaviour;
	
	private ControlledSystemType typeOfControlledSystem;
	private GroupController gc;
	private OptionModelController omc;
	
	private FixedVariableList setPoints;
	private HashMap<GroupTreeNodeObject, FixedVariableList> subSystemSetPoints;
	
	private AbstractEvaluationStrategyRT rtEvaluationStrategy;
	private AbstractGroupEvaluationStrategyRT rtGroupEvaluationStrategy;
	
	private Boolean centralControlledSnapshotSimulation;
	
	private long currentTime;
	private boolean receivedNewMeasurements;
	
	private boolean isDebug = false;
	private String debugAgent = "n0";
	
	/**
	 * Instantiates a new control behaviour that is used during real time.
	 *
	 * @param agentInternalDataModel the agent internal data model
	 * @param ioBehaviour the IO behaviour of the current agent
	 */
	public ControlBehaviourRT(AbstractEnergyAgent agent) {
		this.energyAgent = agent;
		this.internalDataModel = agent.getInternalDataModel();
		this.agentIOBehaviour = agent.getEnergyAgentIO();
		this.initialize();
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onStart()
	 */
	@Override
	public void onStart() {
		this.internalDataModel.addObserver(this);
		super.onStart();
	}
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onEnd()
	 */
	@Override
	public int onEnd() {
		this.internalDataModel.deleteObserver(this);
		return super.onEnd();
	}
	
	/**
	 * Initializes this behaviour.
	 */
	private void initialize() {
		
		// --- If execution base is 'SensorData' exit here ------------------------------
		if (this.internalDataModel.getHyGridAbstractEnvironmentModel().getExecutionDataBase()==ExecutionDataBase.SensorData) {
			this.removeBehaviourFromAgent(null);
			return;
		}
		
		// --- Remind type of controlled system and its OptionModelController -----------
		this.typeOfControlledSystem = this.internalDataModel.getTypeOfControlledSystem();
		if (this.typeOfControlledSystem==ControlledSystemType.TechnicalSystem) {
			this.omc = this.internalDataModel.getOptionModelController();
			this.setSetPointList(this.getSetPointInformation(this.omc.getTechnicalSystem().getSystemVariables()));
			this.rtEvaluationStrategy = (AbstractEvaluationStrategyRT) this.omc.getEvaluationStrategyRT();
			if (rtEvaluationStrategy==null) {
				this.removeBehaviourFromAgent("No real time strategy could be found for the Technical System '" + this.omc.getTechnicalSystem().getSystemID() + "'. Current strategy class is '" + this.omc.getEvaluationSettings().getEvaluationClass() + "'");
			}
			
		} else if (this.typeOfControlledSystem==ControlledSystemType.TechnicalSystemGroup) {
			this.gc = this.internalDataModel.getGroupController();
			this.omc = this.gc.getGroupOptionModelController();
			this.setSetPointList(this.getSetPointInformation(this.omc.getTechnicalSystem().getSystemVariables()));
			this.rtGroupEvaluationStrategy = (AbstractGroupEvaluationStrategyRT) this.omc.getEvaluationStrategyRT();
			if (this.rtGroupEvaluationStrategy==null) {
				this.removeBehaviourFromAgent("No real time strategy could be found for the Technical System Group '" + this.omc.getTechnicalSystem().getSystemID() + "'. Current strategy class is '" + this.omc.getEvaluationSettings().getEvaluationClass() + "'");
			}
		}

	}
	
	/**
	 * Returns the current {@link EomController} that is either an {@link OptionModelController} for a single 
	 * {@link TechnicalSystem} or a {@link GroupController} that controls a {@link TechnicalSystemGroup}.
	 * @return the current EomController
	 */
	public EomController getEomController() {
		EomController eomController = null;
		switch (this.internalDataModel.getTypeOfControlledSystem()) {
		case TechnicalSystem:
			eomController = omc;
			break;
		
		case TechnicalSystemGroup:
			eomController = gc;
			break;
			
		default:
			break;
		}
		return eomController;
	}
	/**
	 * Return the real time evaluation strategy of the controlled {@link TechnicalSystem} (if available).
	 * @return the real time evaluation strategy or <code>null</code>
	 */
	public AbstractEvaluationStrategyRT getRealTimeEvaluationStrategy() {
		return rtEvaluationStrategy;
	}
	/**
	 * Return the real time group evaluation strategy of the controlled {@link TechnicalSystemGroup} (if available).
	 * @return the real time group evaluation strategy or <code>null</code>
	 */
	public AbstractGroupEvaluationStrategyRT getRealTimeGroupEvaluationStrategy() {
		return rtGroupEvaluationStrategy;
	}
	
	/**
	 * Returns the last TechnicalSystemStateEvaluation that was used to control the current system(s).
	 * @return the last technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getLastTechnicalSystemStateEvaluation() {
		return this.getLastTechnicalSystemStateEvaluation(null);
	}
	/**
	 * Returns the last TechnicalSystemStateEvaluation that was used to control the current system(s).
	 *
	 * @param networkID the network ID of a sub system within an aggregation. If <code>null</code>, the main system TSSE will be returned
	 * @return the last technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getLastTechnicalSystemStateEvaluation(String networkID) {
		
		TechnicalSystemStateEvaluation tsseLast = null;
		switch (this.typeOfControlledSystem) {
		case TechnicalSystem:
			tsseLast = this.getRealTimeEvaluationStrategy().getTechnicalSystemStateEvaluation();
			if (networkID!=null) {
				System.out.println("[" + this.getClass().getSimpleName() + "|Last system state] The system under control is a TechnicalSystem, Thus, no networkID can be considered.");
			}
			break;
			
		case TechnicalSystemGroup:
			if (networkID==null) {
				tsseLast = this.getRealTimeGroupEvaluationStrategy().getTechnicalSystemStateEvaluation();
			} else {
				tsseLast = this.getRealTimeGroupEvaluationStrategy().getLatestSubSystemTSSE(networkID);
			}
			break;
		case None:
			break;
		}
		return tsseLast;
	}
	/**
	 * Returns all system states of an aggregation (a {@link TechnicalSystemGroup}) within one instance of the type {@link TechnicalSystemGroupStateEvaluation}.
	 * @return the technical system group state evaluation or <code>null</code>, e.g. if no or a single TechnicalSystem is under control of the real-time control behaviour
	 */
	public TechnicalSystemGroupStateEvaluation getTechnicalSystemGroupStateEvaluation() {
		
		if (this.typeOfControlledSystem == ControlledSystemType.TechnicalSystemGroup) {
			TechnicalSystemStateEvaluation tsseMain = this.getRealTimeGroupEvaluationStrategy().getTechnicalSystemStateEvaluation();
			TechnicalSystemGroupStateEvaluation tsgse = TechnicalSystemStateHelper.convertToTechnicalSystemGroupStateEvaluation(tsseMain);
			new SubStateCollectorTreeAction(this.getRealTimeGroupEvaluationStrategy().getGroupCalculation(), tsgse).doGroupTreeAction();
			return tsgse;
		}
		return null;
	}
	
	/**
	 * Removes the current behaviour from the agent.
	 * @param errorMsg the error message
	 */
	private void removeBehaviourFromAgent(String errorMsg) {
		if (errorMsg!=null && errorMsg.equals("")==false) {
			System.err.println(errorMsg);
		}
		this.internalDataModel.deleteObserver(this);
		if (this.myAgent!=null) this.myAgent.removeBehaviour(this);
	}
	
	/**
	 * Returns the simulated IO interface.
	 * @return the abstract IO simulated
	 */
	private AbstractIOSimulated getAbstractIOSimulated() {
		if (this.agentIOBehaviour instanceof AbstractIOSimulated) {
			return (AbstractIOSimulated) this.agentIOBehaviour;
		}
		return null;
	}
	
	/**
	 * Returns the current system set points.
	 * @return the set points
	 */
	private FixedVariableList getSetPointList() {
		return setPoints;
	}
	/**
	 * Sets the sets the point list.
	 * @param setPoints the new sets the point list
	 */
	private void setSetPointList(FixedVariableList setPoints) {
		this.setPoints = setPoints;
	}
	
	/**
	 * Returns the HashMap with the sub system set points.
	 * @return the sub system set points
	 */
	public HashMap<GroupTreeNodeObject, FixedVariableList> getSubSystemSetPoints() {
		if (subSystemSetPoints==null) {
			subSystemSetPoints = new HashMap<>();
		}
		return subSystemSetPoints;
	}
	/**
	 * Returns the sets the point list.
	 * @param gtno the {@link GroupTreeNodeObject}
	 * @return the sets the point list
	 */
	private FixedVariableList getSetPointList(GroupTreeNodeObject gtno) {
		
		FixedVariableList fvListFound = this.getSubSystemSetPoints().get(gtno);
		if (fvListFound==null) {
			switch(gtno.getGroupMemberType()) {
			case TechnicalSystem:
				fvListFound = this.getSetPointInformation(gtno.getGroupMemberOptionModelController().getTechnicalSystem().getSystemVariables());
				break;
			case TechnicalSystemGroup:
				fvListFound = this.getSetPointInformation(gtno.getGroupMemberGroupController().getGroupOptionModelController().getTechnicalSystem().getSystemVariables());
				break;

			default:
				break;
			}
			this.getSubSystemSetPoints().put(gtno, fvListFound);
		}
		return fvListFound;
	}
	
	/**
	 * Return the set points out of a systems list of {@link SystemVariableDefinition}s.
	 * @param sysVarDefs system variable definition of a system 
	 */
	private FixedVariableList getSetPointInformation(List<SystemVariableDefinition> sysVarDefs) {
		
		// --- Create Vector/List for set point (names) ------------- 
		FixedVariableList setPointList = new FixedVariableList();

		// --- Check the systems variable definition ----------------
		for (int i = 0; i < sysVarDefs.size(); i++) {
			SystemVariableDefinition sysVarDef = sysVarDefs.get(i);
			if (sysVarDef.isSetPoint()==true && sysVarDef.isSetPointForUser()==false) {
				// --- Case separation for variable type ------------
				if (sysVarDef instanceof SystemVariableDefinitionBoolean) {
					FixedBoolean fBoolean = new FixedBoolean();
					fBoolean.setVariableID(sysVarDef.getVariableID());
					fBoolean.setValue(true);
					setPointList.add(fBoolean);
					
				} else if (sysVarDef instanceof SystemVariableDefinitionInteger) {
					FixedInteger fInteger = new FixedInteger();
					fInteger.setVariableID(sysVarDef.getVariableID());
					fInteger.setValue(0);
					setPointList.add(fInteger);
					
				} else if (sysVarDef instanceof SystemVariableDefinitionDouble) {
					FixedDouble fDouble = new FixedDouble();
					fDouble.setVariableID(sysVarDef.getVariableID());
					fDouble.setValue(0);
					setPointList.add(fDouble);
				}
			}
		}
		return setPointList;
	}
	/**
	 * Gets the variable id for system set points.
	 * @return the variable id for system set points
	 */
	public Vector<String> getVariableIDsForSystemSetPoints(FixedVariableList fvList) {
		Vector<String> variableIDsForSetPoints = new Vector<>();
		for (int i=0; i<fvList.size(); i++) {
			variableIDsForSetPoints.add(fvList.get(i).getVariableID());
		}
		return variableIDsForSetPoints;
	}
	
	/**
	 * Returns if the current execution environment is a simulation.
	 * @return true, if is simulation
	 */
	private boolean isSimulation() {
		boolean isSimulation = true;
		switch (this.energyAgent.getAgentOperatingMode()) {
		case Simulation:
		case TestBedSimulation:
			isSimulation = true;
			break;
			
		case TestBedReal:
		case RealSystemSimulatedIO:
		case RealSystem:
			isSimulation = false;
			break;
		}
		return isSimulation;
	}
	
	/**
	 * Returns the current TimeModelType.
	 * @return the time model type
	 */
	private TimeModelType getTimeModelType() {
		return this.internalDataModel.getHyGridAbstractEnvironmentModel().getTimeModelType();
	}
	/**
	 * Return the current discrete time model if available.
	 * @return the time model discrete
	 */
	private TimeModelDiscrete getTimeModelDiscrete() {
		AbstractIOSimulated ioSimulated = this.getAbstractIOSimulated();
		if (ioSimulated!=null) {
			return ioSimulated.getTimeModelDiscrete();
		}
		return null;
	}
	
	
	/**
	 * In the context of simulations, sets the local system state to the environment model if configured in the simulation interface.
	 * @param tsseLocal the current local system state 
	 */
	private void sendControlBehaviourRTStateUpdateToEnvironmentModel(TechnicalSystemStateEvaluation tsseLocal) {
		if (this.isSimulation()==true) {
			AbstractIOSimulated ioSimulated = this.getAbstractIOSimulated();
			if (ioSimulated!=null && ioSimulated.isSetTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel()==true) {
				ioSimulated.sendControlBehaviourRTStateUpdateToEnvironmentModel(tsseLocal);
			}
		}
	}
	
	/**
	 * Sends a discrete simulations step to simulation manager (but only in simulations and if a discrete time model is used).
	 */
	private void sendDiscreteSimulationsStepToSimulationManager() {
		
		if (this.isSimulation()==false || this.getTimeModelType()==TimeModelType.TimeModelContinuous) return;
		
		AbstractIOSimulated ioSimulated = this.getAbstractIOSimulated();
		if (ioSimulated!=null) {
			ioSimulated.sendManagerNotification(this.getDiscreteSimulationStep());
		}
	}
	/**
	 * Returns the current {@link DiscreteSimulationStep} with the system state and the 
	 * {@link DiscreteSystemStateType} that is important for the iteration between agent 
	 * and environment in the current discrete simulation step.
	 *
	 * @return the discrete simulation step
	 */
	public DiscreteSimulationStep getDiscreteSimulationStep() {

		// --- Try to get individual system state types -------------
		Vector<TechnicalSystemStateEvaluation> tsseVector = null;
		TechnicalSystemStateEvaluation tsse = null;
		switch (this.typeOfControlledSystem) {
		case TechnicalSystem:
			if (this.isCentralControlledSnapshotSimulation()==true) {
				tsseVector = this.rtEvaluationStrategy.getAllPossibleSnapshotStatesByDefinition(this.currentTime);
			} else {
				tsse = this.rtEvaluationStrategy.getTechnicalSystemStateEvaluation();
			}
			break;
			
		case TechnicalSystemGroup:
			if (this.isCentralControlledSnapshotSimulation()==true) {
				tsseVector = this.rtEvaluationStrategy.getAllPossibleSnapshotStatesByDefinition(this.currentTime);
			} else {
				tsse = this.rtGroupEvaluationStrategy.getTechnicalSystemStateEvaluation();
			}
			break;
			
		default:
			break;
		}
		
		// --- Prepare return value ---------------------------------
		DiscreteSimulationStep dsStep = null;
		if (this.isCentralControlledSnapshotSimulation()==true) {
			// ------------------------------------------------------
			// --- Central decision process -------------------------
			// ------------------------------------------------------
			boolean requiresEnvModelInfo = false;
			AbstractIOSimulated ioSimulated = this.getAbstractIOSimulated();
			if (ioSimulated!=null) {
				requiresEnvModelInfo = ioSimulated.requiresEnvironmentModelInformation(); 
			}
			dsStep = new DiscreteSimulationStepCentralDecision(tsseVector, requiresEnvModelInfo); 
			
		} else {
			// ------------------------------------------------------
			// --- Decentral decision process -----------------------
			// ------------------------------------------------------
			// --- Use an individual discrete system state type? --------
			DiscreteSystemStateType dsTypeIndividual = this.getDiscreteSystemStateType();
			if (dsTypeIndividual==null) {
				dsTypeIndividual = DiscreteSystemStateType.Final;
			}
			dsStep = new DiscreteSimulationStep(tsse, dsTypeIndividual);
		}
		return dsStep;
	}
	/**
	 * Returns the DiscreteSystemStateType as returned by the current evaluation strategy.
	 * @return the discrete system state type
	 */
	private DiscreteSystemStateType getDiscreteSystemStateType() {
		
		if (this.getTimeModelType()!=TimeModelType.TimeModelDiscrete) return null;
		
		// --- Try to get individual system state types -------------
		DiscreteSystemStateType dsTypeIndividual = null;
		switch (this.typeOfControlledSystem) {
		case TechnicalSystem:
			if (this.rtEvaluationStrategy instanceof DiscreteIteratorInterface) {
				dsTypeIndividual = ((DiscreteIteratorInterface) this.rtEvaluationStrategy).getDiscreteSystemStateType();
			}
			break;
			
		case TechnicalSystemGroup:
			if (this.rtGroupEvaluationStrategy instanceof DiscreteIteratorInterface) {
				dsTypeIndividual = ((DiscreteIteratorInterface) this.rtGroupEvaluationStrategy).getDiscreteSystemStateType();
			}
			break;
			
		default:
			break;
		}
		return dsTypeIndividual;
	}
	
	/**
	 * Answers the question, if the current execution is part of an iteration.
	 * @return true, if is iteration step
	 */
	private boolean isDiscreteIterationStep() {
		
		if (this.getTimeModelType()!=TimeModelType.TimeModelDiscrete) return false;
		// --- Try to get individual system state types -------------
		DiscreteSystemStateType dsTypeIndividual = this.getDiscreteSystemStateType();
		return dsTypeIndividual!=null && dsTypeIndividual==DiscreteSystemStateType.Iteration; 
	}
	
	/**
	 * Checks if a central controlled snapshot simulation is executed.
	 * @return true, if is central controlled snapshot simulation
	 */
	private boolean isCentralControlledSnapshotSimulation() {
		if (centralControlledSnapshotSimulation==null) {
			HyGridAbstractEnvironmentModel hyGridAbsEnv = this.internalDataModel.getHyGridAbstractEnvironmentModel();
			if (hyGridAbsEnv!=null) {
				boolean isSnapshot = hyGridAbsEnv.isDiscreteSnapshotSimulation();
				boolean isSnapshotCentral = hyGridAbsEnv.getSnapshotDecisionLocation()==SnapshotDecisionLocation.Central;
				boolean isSnapshotCentralClassAvailable = hyGridAbsEnv.getSnapshotCentralDecisionClass()!=null;
				centralControlledSnapshotSimulation = isSnapshot && isSnapshotCentral && isSnapshotCentralClassAvailable;
			} else {
				centralControlledSnapshotSimulation = false;
			}
		}
		return centralControlledSnapshotSimulation;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		
		if (updateObject==AbstractInternalDataModel.CHANGED.MEASUREMENTS_FROM_SYSTEM) {
			
			// --- Set marker that new measurements arrived -----------------------------
			this.receivedNewMeasurements = true;
			
			// --- Are we in discrete simulations ? -------------------------------------
			switch (this.getTimeModelType()) {
			case TimeModelContinuous:
				// ----------------------------------------------------------------------
				// --- Leave the control of the behaviour by the agents internal --------
				// --- Scheduler for behaviour's                                 --------
				// ----------------------------------------------------------------------
				this.restart();
				break;
			
			case TimeModelDiscrete:
				// ----------------------------------------------------------------------
				// --- Invoke the action method to directly cause an reaction. ----------
				// --- => Avoids to integrate a wait process within IO simulated! -------
				// ----------------------------------------------------------------------
				this.action();
				break;
			}
		}
	}
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		// --- Only act if new measurements arrived ---------------------------
		if (this.receivedNewMeasurements==true) {
			
			// --- Get the current time ---------------------------------------
			this.currentTime = this.agentIOBehaviour.getTime();
			try {
				// --- Case separation for single or multiple systems ---------
				if (this.typeOfControlledSystem!=null) {
					this.debugPrint("executing real time strategy ...", false);
					switch (this.typeOfControlledSystem) {
					case TechnicalSystem:
						this.actionForTechnicalSystem();
						break;
					case TechnicalSystemGroup:
						this.actionForTechnicalSystemGroup();
						break;
					default:
						break;
					}
				}
				
			} catch (Exception ex) {
				DisplayHelper.systemOutPrintlnGlobalTime(this.currentTime, "[" + this.getClass().getSimpleName() + "]", "Error during execution of real time control behaviour:");
				ex.printStackTrace();
				
			} finally {
				this.receivedNewMeasurements = false;
			}
		}
		
		// --- Block this behaviour -------------------------------------------
		this.block();
	}
	
	/**
	 * This is the actual action for a {@link TechnicalSystem}.
	 */
	private void actionForTechnicalSystem() {
		
		if (this.rtEvaluationStrategy==null) {
			this.removeBehaviourFromAgent("No real time strategy could be found for the Technical System '" + this.omc.getTechnicalSystem().getSystemID() + "'. Current strategy class is '" + this.omc.getEvaluationSettings().getEvaluationClass() + "'");
			return;
		}
		
		// --- Get the new measurements, if any -------------------------------
		FixedVariableList measurements = this.internalDataModel.getMeasurementsFromSystem();
		if (this.omc!=null && measurements!=null && this.getSetPointList()!=null && this.getSetPointList().size()!=0) {

			// --- Execute individual evaluation strategy ---------------------
			TechnicalSystemStateEvaluation tsseLocal = null;
			try {
				if (this.isCentralControlledSnapshotSimulation()==false) {

					// --- Get evaluation start and end time ------------------
					long evalStartTime = this.currentTime;
					long evalEndTime = this.currentTime;
					TimeModelDiscrete tmd = this.getTimeModelDiscrete();
					if (tmd!=null) {
						evalStartTime = evalEndTime - tmd.getStep();
					}
					this.rtEvaluationStrategy.setEvaluationStartTime(evalStartTime);
					this.rtEvaluationStrategy.setEvaluationEndTime(evalEndTime);
					
					// --- Check if we're in a running iteration --------------
					if (this.isDiscreteIterationStep()==true) {
						// -- Revert evaluation to start time of step ---------
						this.rtEvaluationStrategy.runEvaluationBackwardsTo(evalStartTime);
					}
					
					// --- Check for decision switch --------------------------
					AbstractDecisionSwitch<? extends AbstractEvaluationStrategy> ds = this.getDecisionSwitch();
					if (ds!=null) {
						// --- Check for planner usage ------------------------
						EomPlannerResult eomPR = null;
						if (this.energyAgent.getPlanningDispatcherManager()!=null && this.energyAgent.getPlanningDispatcher()!=null) {
							eomPR = this.energyAgent.getPlanningDispatcherManager().getPlannerResultForRealTimeExecution();
						}
						ds.setEomPlannerResultForDecisions(eomPR);	
					}
					
					// --- Things to do for TechnicalSystems ------------------
					this.rtEvaluationStrategy.setMeasurementsFromSystem(measurements);
					this.rtEvaluationStrategy.runEvaluationUntil(evalEndTime);
					this.rtEvaluationStrategy.applyScheduleLengthRestriction(evalEndTime);
					tsseLocal = this.rtEvaluationStrategy.getTechnicalSystemStateEvaluation();
				}
					
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// --- If configured, set TSSE to environment model ---------------
			this.sendControlBehaviourRTStateUpdateToEnvironmentModel(tsseLocal);
			this.sendDiscreteSimulationsStepToSimulationManager();

			// --- Assign set points from tsse --------------------------------
			this.setSetPointsFromTechnicalSystemStateEvaluation(tsseLocal);

		}
	}
	
	/**
	 * Set the set points of the IO interface on the base of the specified {@link TechnicalSystemStateEvaluation}.
	 * @param tsse the system state to translate into set points
	 */
	public void setSetPointsFromTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsse) {
		
		if (tsse==null) return;
		
		// --- Take the copy of the local set point list ------------
		FixedVariableList setPointList = this.getSetPointList().getCopy();
		
		switch (typeOfControlledSystem) {
		case TechnicalSystem:
			// --- Assign TSSE-values to the set-point list ---------
			this.assignTechnicalSystemStateValuesToSetPointList(tsse, setPointList);
			// --- Assign set point to the IO-interface -------------
			this.agentIOBehaviour.setSetPointsToSystem(setPointList);
			break;

		case TechnicalSystemGroup:
			// --- Create an aggregations set-point description -----
			FixedVariableListForAggregation fvListFA = new FixedVariableListForAggregation();
			
			// --- 1. Get set-points of superordinate system --------
			this.assignTechnicalSystemStateValuesToSetPointList(tsse, setPointList);
			fvListFA.addAll(setPointList); 

			// --- 2. Get set-points of sub systems -----------------
			SetPointSelectionTreeAction sps = new SetPointSelectionTreeAction(this.gc, this.rtGroupEvaluationStrategy, fvListFA);
			sps.doGroupTreeAction();
			
			// --- Assign set point to the IO-interface -------------
			this.agentIOBehaviour.setSetPointsToSystem(fvListFA);
			break;
			
		default:
			break;
		}
	}
	
	/**
	 * Assign technical system state values to set point list.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation that contains the current set-point settings
	 * @param setPointList the set point list to configure
	 */
	private void assignTechnicalSystemStateValuesToSetPointList(TechnicalSystemStateEvaluation tsse, FixedVariableList setPointList) {
		
		Vector<String> varIDsForSetPoints = this.getVariableIDsForSystemSetPoints(setPointList);
		for (int i = 0; i < varIDsForSetPoints.size(); i++) {
			// --- Get variable-ID first ----------------------------
			String setPointID = varIDsForSetPoints.get(i);
			// --- Get value from the local evaluation --------------
			FixedVariable setPointNewValue = (FixedVariable) this.omc.getVariableState(tsse.getIOlist(), setPointID);
			FixedVariable setPointListValue = setPointList.getVariable(setPointID);
			// --- Case separation for variable type ----------------
			if (setPointNewValue instanceof FixedBoolean) {
				((FixedBoolean)setPointListValue).setValue(((FixedBoolean)setPointNewValue).isValue());
			} else if (setPointNewValue instanceof FixedInteger) {
				((FixedInteger)setPointListValue).setValue(((FixedInteger)setPointNewValue).getValue());
			} else if (setPointNewValue instanceof FixedDouble) {
				((FixedDouble)setPointListValue).setValue(((FixedDouble)setPointNewValue).getValue());
			}
		}
	}
	
	/**
	 * This is the actual action for a {@link TechnicalSystemGroup}
	 */
	private void actionForTechnicalSystemGroup() {

		if (this.rtGroupEvaluationStrategy==null) {
			this.removeBehaviourFromAgent("No real time strategy could be found for the Technical System Group '" + this.omc.getTechnicalSystem().getSystemID() + "'. Current strategy class is '" + this.omc.getEvaluationSettings().getEvaluationClass() + "'");
			return;
		}
		
		// --- Get the new measurements, if any -------------------------------
		FixedVariableList modelMeasurements = this.internalDataModel.getMeasurementsFromSystem();
		if (modelMeasurements==null) {
			return;
		} else if (!(modelMeasurements instanceof FixedVariableListForAggregation)) {
			String msg = "=> " + this.getClass().getSimpleName() + " / " + this.rtGroupEvaluationStrategy.getClass().getSimpleName() + ":\n"; 
			msg += "The measurements of a TechnicalSystemGroup have to be of type 'FixedVariableListForAggregation'! - Skip the execution of the real time strategy.";
			System.err.println(msg);
			return;
		}
		// --- Cast measurements ----------------------------------------------
		FixedVariableListForAggregation measurements = (FixedVariableListForAggregation) modelMeasurements; 
		
		// --- Check availability of required instances -----------------------
		if (this.gc!=null && this.omc!=null && measurements!=null) {

			// --- Execute individual evaluation strategy ---------------------
			TechnicalSystemStateEvaluation tsseLocal = null;
			try {
				// --- Get evaluation start and end time ----------------------
				long evalStartTime = this.currentTime;
				long evalEndTime = this.currentTime;
				TimeModelDiscrete tmd = this.getTimeModelDiscrete();
				if (tmd!=null) {
					evalStartTime = evalEndTime - tmd.getStep();
				}
				this.rtGroupEvaluationStrategy.setEvaluationStartTime(evalStartTime);
				this.rtGroupEvaluationStrategy.setEvaluationEndTime(evalEndTime);
				
				// --- Check if we're in a running iteration ------------------
				if (this.isDiscreteIterationStep()==true) {
					// -- Revert evaluation to start time of step -------------
					this.rtGroupEvaluationStrategy.runEvaluationBackwardsTo(evalStartTime);
				}

				// --- Check for decision switch ------------------------------
				AbstractDecisionSwitch<? extends AbstractEvaluationStrategy> ds = this.getDecisionSwitch();
				if (ds!=null) {
					// --- Check for planner usage ----------------------------
					EomPlannerResult eomPR = null;
					if (this.energyAgent.getPlanningDispatcherManager()!=null && this.energyAgent.getPlanningDispatcher()!=null) {
						eomPR = this.energyAgent.getPlanningDispatcherManager().getPlannerResultForRealTimeExecution();
					}
					ds.setEomPlannerResultForDecisions(eomPR);	
				}
				
				// --- Things to do for TechnicalSystemGroupss ----------------
				this.rtGroupEvaluationStrategy.setMeasurementsFromSystem(measurements);
				this.rtGroupEvaluationStrategy.runEvaluationUntil(evalEndTime); 
				this.rtGroupEvaluationStrategy.applyScheduleLengthRestriction(evalEndTime);
				tsseLocal = this.rtGroupEvaluationStrategy.getTechnicalSystemStateEvaluation();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// --- If configured, set TSSE to environment model ---------------
			this.sendControlBehaviourRTStateUpdateToEnvironmentModel(tsseLocal);
			this.sendDiscreteSimulationsStepToSimulationManager();

			// --- Assign set points from tsse --------------------------------
			this.setSetPointsFromTechnicalSystemStateEvaluation(tsseLocal);
			
		}
		
	}
	
	/**
	 * The Class SetPointSelectionTreeAction is used within the {@link ControlBehaviourRT}
	 * in order to select all set-points for an aggregation.
	 * 
	 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
	 */
	private class SetPointSelectionTreeAction extends AbstractGroupTreeAction {

		private FixedVariableListForAggregation fvListFA;
		
		/**
		 * Instantiates a new sets the point selection.
		 * @param groupController the group controller
		 */
		public SetPointSelectionTreeAction(GroupController groupController, AbstractGroupEvaluationStrategy groupEvaluationStrategy, FixedVariableListForAggregation fvListFA) {
			super(groupController, GroupTreeTraversal.SumUpSearch, groupEvaluationStrategy);
			this.fvListFA = fvListFA;
		}
		
		/* (non-Javadoc)
		 * @see energygroup.evaluation.AbstractGroupTreeAction#doNodeActionForTechnicalSystemGroup(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject)
		 */
		@Override
		public void doNodeActionForTechnicalSystemGroup(DefaultMutableTreeNode rootNode, GroupTreeNodeObject gtno) {
			// --- Happened above already - nothing to do here --------------------------
		}

		/* (non-Javadoc)
		 * @see energygroup.evaluation.AbstractGroupTreeAction#doNodeActionForGroupMemberTechnicalSystem(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject)
		 */
		@Override
		public void doNodeActionForGroupMemberTechnicalSystem(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno) {
			 
			// --- Get the network ID of the element ------------------------------------
			String networkID = gtno.getGroupMember().getNetworkID();

			// --- Get last result of the corresponding MemberEvaluationStrategy --------
			MemberEvaluationStrategy mes = gtno.getGroupMemberEvaluationStrategy(this.getGroupEvaluationStrategy());
			TechnicalSystemStateEvaluation tsseSchedule = mes.getScheduleTechnicalSystemStateEvaluation();
			
			// --- Get the set-point settings configured/used ---------------------------
			FixedVariableList setPointList = getSetPointList(gtno).getCopy();
			assignTechnicalSystemStateValuesToSetPointList(tsseSchedule, setPointList);
			this.fvListFA.addFixedVariableListForSubSystem(networkID, setPointList);
		}
		/* (non-Javadoc)
		 * @see energygroup.evaluation.AbstractGroupTreeAction#doNodeActionForGroupMemberTechnicalSystemGroup(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject)
		 */
		@Override
		public void doNodeActionForGroupMemberTechnicalSystemGroup(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno) {

			// --- Get the network ID of the element ------------------------------------
			String networkID = gtno.getGroupMember().getNetworkID();

			// --- Get last result of the corresponding MemberEvaluationStrategy --------
			MemberEvaluationStrategy mes = gtno.getGroupMemberEvaluationStrategy(this.getGroupEvaluationStrategy());
			TechnicalSystemStateEvaluation tsseSchedule = mes.getScheduleTechnicalSystemStateEvaluation();

			// --- Get the set-point settings configured/used ---------------------------
			FixedVariableList setPointList = getSetPointList(gtno).getCopy();
			assignTechnicalSystemStateValuesToSetPointList(tsseSchedule, setPointList);
			this.fvListFA.addFixedVariableListForSubSystem(networkID, setPointList);
		}
		
		/* (non-Javadoc)
		 * @see energygroup.evaluation.AbstractGroupTreeAction#doNodeActionForGroupMemberScheduleList(javax.swing.tree.DefaultMutableTreeNode, energygroup.GroupTreeNodeObject)
		 */
		@Override
		public void doNodeActionForGroupMemberScheduleList(DefaultMutableTreeNode currentNode, GroupTreeNodeObject gtno) {
			// --- Nothing to do here, since this system can't be controlled ------------
		}

	}
	
	/**
	 * Resets the evaluation process.
	 */
	public void resetEvaluationProcess() {
		if (this.typeOfControlledSystem==ControlledSystemType.TechnicalSystem) {
			this.rtEvaluationStrategy.resetInitialTechnicalSystemStateEvaluation();
			this.rtEvaluationStrategy.resetStrategy();
		} else {
			this.rtGroupEvaluationStrategy.resetInitialTechnicalSystemStateEvaluation();
			this.rtGroupEvaluationStrategy.resetStrategy();
		}
	}

	
	// -------------------------------------------------------------------------------------------------------
	// --- From here methods to interact with the AbstractDecisionSwitch of the real time control strategy ---
	// -------------------------------------------------------------------------------------------------------
	/**
	 * Returns, if available, the real-time decision switch.
	 * @return the decision switch
	 */
	public AbstractDecisionSwitch<? extends AbstractEvaluationStrategy> getDecisionSwitch() {
		if (this.rtEvaluationStrategy!=null) {
			return this.rtEvaluationStrategy.getDecisionSwitch();
		}
		if (this.rtGroupEvaluationStrategy!=null) {
			return this.rtGroupEvaluationStrategy.getDecisionSwitch();
		}
		return null;
	}
	/**
	 * Sets the decision switch priority.
	 *
	 * @param newPriority the new priority
	 * @return true, if successful, otherwise false
	 */
	public boolean setDecisionSwitchPriority(int newPriority) {
		
		AbstractDecisionSwitch<? extends AbstractEvaluationStrategy> ds = this.getDecisionSwitch();
		if (ds!=null) {
			ds.setCurrentPriorityLevel(newPriority);
			return true;
		}
		return false;
	}

	
	/**
	 * Debug print.
	 * @param message the message
	 * @param isError the is error
	 */
	private void debugPrint(String message, boolean isError) {
		
		if (this.isDebug==false) return;
		if (this.debugAgent!=null && this.debugAgent.isBlank()==false && this.debugAgent.equals(this.energyAgent.getLocalName())==false) return;
		
		String prefix = "[" + this.energyAgent.getLocalName() + "|" + this.getClass().getSimpleName() +  "] => " + DateTimeHelper.getDateAsString(new Date(this.currentTime), "dd.MM.yy HH:mm:ss,SSS") + " ";
		if (isError==true) {
			System.err.println(prefix + message);
		} else {
			System.out.println(prefix + message);
		}
	}
	
}
