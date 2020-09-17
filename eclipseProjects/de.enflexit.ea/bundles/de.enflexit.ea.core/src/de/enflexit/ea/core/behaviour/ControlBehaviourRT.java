package de.enflexit.ea.core.behaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.EnergyAgentIO;
import de.enflexit.ea.core.AbstractInternalDataModel.ControlledSystemType;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.ExecutionDataBase;
import energy.EomController;
import energy.FixedVariableList;
import energy.FixedVariableListForAggregation;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategyRT;
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
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.GroupController;
import energygroup.GroupTreeNodeObject;
import energygroup.evaluation.AbstractGroupEvaluationStrategy;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import energygroup.evaluation.AbstractGroupTreeAction;
import energygroup.evaluation.MemberEvaluationStrategy;
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
	private AbstractInternalDataModel internalDataModel;
	private EnergyAgentIO agentIOBehaviour;
	
	private ControlledSystemType typeOfControlledSystem;
	private GroupController gc;
	private OptionModelController omc;
	
	private FixedVariableList setPoints;
	private HashMap<GroupTreeNodeObject, FixedVariableList> subSystemSetPoints;
	
	private AbstractEvaluationStrategyRT rtEvaluationStrategy;
	private AbstractGroupEvaluationStrategyRT rtGroupEvaluationStrategy;
	
	private long currentTime;
	
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
	
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		if (updateObject==AbstractInternalDataModel.CHANGED.MEASUREMENTS_FROM_SYSTEM) {
			this.restart(); // --- Cause the invocation of the action method -- 
		}
	}
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		// --- Get the current time ---------------------------------
		this.currentTime = this.agentIOBehaviour.getTime();
		
		// --- Case separation for single or multiple systems -------
		if (this.typeOfControlledSystem!=null) {
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
		
		// --- Get the new measurements, if any ---------------------
		FixedVariableList measurements = this.internalDataModel.getMeasurementsFromSystem();
		if (this.omc!=null && measurements!=null && this.getSetPointList()!=null && this.getSetPointList().size()!=0) {

			// --- Execute individual evaluation strategy -----------
			TechnicalSystemStateEvaluation tsseLocal = null;
			try {
				// --- Things to do for TechnicalSystems ------------
				this.rtEvaluationStrategy.setMeasurementsFromSystem(measurements);
				this.rtEvaluationStrategy.runEvaluationUntil(this.currentTime);
				tsseLocal = this.rtEvaluationStrategy.getTechnicalSystemStateEvaluation();
				
					
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// --- If configured, set TSSE to environment model -----
			this.setLocalSystemStateToEnvironmentModelInSimulations(tsseLocal);

			// --- Get the selected set points ----------------------
			if (tsseLocal!=null) {
				// --- Take the copy of the current list ------------
				FixedVariableList setPointList = this.getSetPointList().getCopy(); 
				// --- Assign TSSE-values to the set-point list -----
				this.assignTechnicalSystemStateValuesToSetPointList(tsseLocal, setPointList);
				// --- Assign set point to the IO-interface ---------
				this.agentIOBehaviour.setSetPointsToSystem(setPointList);
			}
		}
	}
	
	/**
	 * In the context of simulations, sets the local system state to the environment model if configured in the simulation interface.
	 * @param tsseLocal the new local system state to environment model
	 */
	private void setLocalSystemStateToEnvironmentModelInSimulations(TechnicalSystemStateEvaluation tsseLocal) {
		
		switch (this.energyAgent.getAgentOperatingMode()) {
		case Simulation:
		case TestBedSimulation:
			EnergyAgentIO eaIO = this.energyAgent.getEnergyAgentIO();
			if (eaIO instanceof AbstractIOSimulated) {
				AbstractIOSimulated ioSimulated = (AbstractIOSimulated) this.energyAgent.getEnergyAgentIO();
				if (ioSimulated.isSetTechnicalSystemStateFromRealTimeControlBehaviourToEnvironmentModel()==true) {
					ioSimulated.sendControlBehaviourRTStateUpdateToEnvironmentModel(tsseLocal);
				}
			}
			break;
			
		case TestBedReal:
		case RealSystemSimulatedIO:
		case RealSystem:
			// --- Nothing to do here yet -------------------
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
		
		// --- Get the new measurements, if any ---------------------
		FixedVariableList modelMeasurements = this.internalDataModel.getMeasurementsFromSystem();
		if (modelMeasurements==null) {
			return;
		} else if (!(modelMeasurements instanceof FixedVariableListForAggregation)) {
			String msg = "=> " + this.getClass().getSimpleName() + " / " + this.rtGroupEvaluationStrategy.getClass().getSimpleName() + ":\n"; 
			msg += "The measurements of a TechnicalSystemGroup have to be of type 'FixedVariableListForAggregation'! - Skip the execution of the real time strategy.";
			System.err.println(msg);
			return;
		}
		// --- Cast measurements ------------------------------------
		FixedVariableListForAggregation measurements = (FixedVariableListForAggregation) modelMeasurements; 
		
		// --- Check availability of required instances -------------
		if (this.gc!=null && this.omc!=null && measurements!=null) {

			// --- Execute individual evaluation strategy -----------
			TechnicalSystemStateEvaluation tsseLocal = null;
			try {
				// --- Things to do for TechnicalSystemGroupss ------
				this.rtGroupEvaluationStrategy.setMeasurementsFromSystem(measurements);
				this.rtGroupEvaluationStrategy.runEvaluationUntil(this.currentTime); 
				tsseLocal = this.rtGroupEvaluationStrategy.getTechnicalSystemStateEvaluation();
				
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			// --- If configured, set TSSE to environment model -----
			this.setLocalSystemStateToEnvironmentModelInSimulations(tsseLocal);

			// --- Get the selected set points ----------------------
			if (tsseLocal!=null) {
				// --- Create an aggregations set-point description -
				FixedVariableListForAggregation fvListFA = new FixedVariableListForAggregation();
				
				// --- 1. Get set-points of superordinate system ----
				FixedVariableList setPointList = this.getSetPointList().getCopy(); 
				this.assignTechnicalSystemStateValuesToSetPointList(tsseLocal, setPointList);
				fvListFA.addAll(setPointList); 

				// --- 2. Get set-points of sub systems -------------
				SetPointSelectionTreeAction sps = new SetPointSelectionTreeAction(this.gc, this.rtGroupEvaluationStrategy, fvListFA);
				sps.doGroupTreeAction();
				
				// --- Assign set point to the IO-interface ---------
				this.agentIOBehaviour.setSetPointsToSystem(fvListFA);
			}
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

}
