package de.enflexit.ea.core.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.AbstractInternalDataModel.ControlledSystemType;
import de.enflexit.ea.core.planning.PlannerEvent.PlannerEventType;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energygroup.GroupController;

/**
 * The Class Planner provides the planning scope of an energy agent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class Planner {

	protected AbstractEnergyAgent energyAgent;
	protected AbstractInternalDataModel internalDataModel;
	
	private List<PlannerListener> plannerListener;
	
	private OptionModelController optionModelController;
	private GroupController groupController;
	
	private ScheduleList resultScheduleList;
	
	
	/**
	 * Instantiates a new planner instance for the energy agent.
	 * @param energyAgent the energy agent
	 */
	public Planner(AbstractEnergyAgent energyAgent) {
		this(energyAgent, null);
	}
	/**
	 * Instantiates a new planner instance for the energy agent.
	 *
	 * @param energyAgent the energy agent
	 * @param pl the PlannerListener to register at the planner
	 */
	public Planner(AbstractEnergyAgent energyAgent, PlannerListener pl) {
		
		if (energyAgent==null) {
			throw new NullPointerException("The energy agent instance is not allowed to be null!");
		}
		this.energyAgent = energyAgent;
		if (pl!=null) {
			this.addPlannerListener(pl);
		}
		this.initialize();
	}
	/**
	 * Returns the internal data model of the current energy agent.
	 * @return the internal data model
	 */
	private AbstractInternalDataModel getInternalDataModel() {
		if (internalDataModel==null) {
			internalDataModel = this.energyAgent.getInternalDataModel(); 
		}
		return internalDataModel;
	}
	/**
	 * Returns the ControlledSystemType that is under the energy agents guidance.
	 * @return the controlled system type
	 */
	private ControlledSystemType getControlledSystemType() {
		return this.getInternalDataModel().getTypeOfControlledSystem();
	}
	
	// ----------------------------------------------------	
	// --- From here, initialization of the planner -------
	// ----------------------------------------------------
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		switch (this.getControlledSystemType()) {
		case TechnicalSystem:
			this.optionModelController = new OptionModelController();
			this.optionModelController.setTechnicalSystem(this.getInternalDataModel().getOptionModelController().getTechnicalSystemCopy());
			// --- Define the planning strategies to use ----
			// --- Define the timing of the planning --------
			break;

		case TechnicalSystemGroup:
			this.groupController = new GroupController();
			this.groupController.setTechnicalSystemGroup(this.getInternalDataModel().getGroupController().getTechnicalSystemGroupCopy());
			// --- Define the planning strategies to use ----
			// --- Define the timing of the planning --------
			break;
			
		case None:
			this.waitForControlledSystem();
			break;
		}

		// --- Notify about initialization --------------------------
		this.notifyPlannerListener(new PlannerEvent(PlannerEventType.Initialized));
	}
	/**
	 * Will register as observer of the current internal data model and wait for a controlled system.
	 */
	private void waitForControlledSystem() {
		
		this.getInternalDataModel().addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object updateObject) {
				// --- Got a NetworkComponent ? -------------------------------  
				if (updateObject==AbstractInternalDataModel.CHANGED.NETWORK_COMPONENT) {
					if (Planner.this.getControlledSystemType()!=ControlledSystemType.None) {
						Planner.this.getInternalDataModel().deleteObserver(this);
						Planner.this.initialize();
					}
				}
			}
		});
	}
	
	// ----------------------------------------------------	
	// --- From here, execution of the planner ------------
	// ----------------------------------------------------
	/**
	 * Starts the planning process for the system(s) under control.
	 *
	 * @param startTime the start time of the planning time range
	 * @param stopTime the stop time of the planning time range
	 * @param strategy the strategy to use for planning
	 */
	public void startPlanning(long startTime, long stopTime, AbstractEvaluationStrategy strategy) {
		this.startPlanning(startTime, stopTime, new ArrayList<>(Arrays.asList(strategy)));
	}
	/**
	 * Starts the planning process for the system(s) under control.
	 *
	 * @param startTime the start time of the planning time range
	 * @param stopTime the stop time of the planning time range
	 * @param strategyList the list of evaluation strategies to use
	 */
	public void startPlanning(long startTime, long stopTime, List<AbstractEvaluationStrategy> strategyList) {
		
		switch (this.getControlledSystemType()) {
		case TechnicalSystem:
			this.startPlanningForTechnicalSystem(startTime, stopTime, strategyList);
			break;
		case TechnicalSystemGroup:
			this.startPlanningForTechnicalSystemGroup(startTime, stopTime, strategyList);
			break;
		default:
			System.err.println("[" + this.getClass().getSimpleName() + "-" + this.energyAgent.getLocalName() + "] No system is to be controlled by the current energy agent!");
			break;
		}
	}
	/**
	 * Starts the planning process for the {@link TechnicalSystem} under control.
	 *
	 * @param startTime the start time of the planning time range
	 * @param stopTime the stop time of the planning time range
	 * @param strategyList the list of evaluation strategies to use
	 */
	private void startPlanningForTechnicalSystem(long startTime, long stopTime, List<AbstractEvaluationStrategy> strategyList) {
		
	}
	/**
	 * Starts the planning process for the {@link TechnicalSystemGroup} under control.
	 *
	 * @param startTime the start time of the planning time range
	 * @param stopTime the stop time of the planning time range
	 * @param strategyList the list of evaluation strategies to use
	 */
	private void startPlanningForTechnicalSystemGroup(long startTime, long stopTime, List<AbstractEvaluationStrategy> strategyList) {
		
	}
	
	
	
	private List<AbstractEvaluationStrategy> checkEvaluationStrategies(List<AbstractEvaluationStrategy> strategyList) {
		
		// --- Create positive list -----------------------
		List<AbstractEvaluationStrategy> strategyListChecked = new ArrayList<>();
		for (AbstractEvaluationStrategy strategy : strategyList) {
			
			switch (this.getControlledSystemType()) {
			case TechnicalSystem:
				if (this.isPlanningStrategyForTechnicalSystem(strategy)==true) strategyListChecked.add(strategy);
				break;
			case TechnicalSystemGroup:
				if (this.isPlanningStrategyForTechnicalSystemGroup(strategy)==true) strategyListChecked.add(strategy);
				break;
			default:
				break;
			}
		}
		
		// --- Check if a strategy can be used ------------
		if (strategyListChecked.size()==0) {
			// TODO
			
		}
		return strategyListChecked;
	}
	/**
	 * Checks if the specified strategy is planning strategy for a {@link TechnicalSystem}.
	 *
	 * @param strategy the strategy
	 * @return true, if is planning strategy for technical system
	 */
	private boolean isPlanningStrategyForTechnicalSystem(AbstractEvaluationStrategy strategy) {
		if (strategy!=null) {
			
		}
		return false;
	}
	/**
	 * Checks if the specified strategy is planning strategy for a {@link TechnicalSystemGroup}.
	 *
	 * @param strategy the strategy
	 * @return true, if is planning strategy for technical system group
	 */
	private boolean isPlanningStrategyForTechnicalSystemGroup(AbstractEvaluationStrategy strategy) {
		if (strategy!=null) {
			
		}
		return false;
	}
	
	
	
	/**
	 * Returns the result schedule list.
	 * @return the result schedule list
	 */
	public ScheduleList getResultScheduleList() {
		if (resultScheduleList==null) {
			// TODO
		}
		return resultScheduleList;
	}

	
	// ----------------------------------------------------	
	// --- From here, PlannerListener handling -----------
	// ----------------------------------------------------
	/**
	 * Returns the {@link PlannerListener} registered at this planner.
	 * @return the planner listener
	 */
	private List<PlannerListener> getPlannerListener() {
		if (plannerListener==null) {
			plannerListener = new ArrayList<>();
		}
		return plannerListener;
	}
	/**
	 * Adds the specified {@link PlannerListener}.
	 * @param pl the PlannerListener
	 */
	public boolean addPlannerListener(PlannerListener pl) {
		if (pl!=null && this.getPlannerListener().contains(pl)==false) {
			return this.getPlannerListener().add(pl);
		}
		return false;
	}
	/**
	 * Removes the specified {@link PlannerListener}.
	 * @param pl the PlannerListener
	 */
	public boolean removePlannerListener(PlannerListener pl) {
		if (pl==null) return false;
		return this.getPlannerListener().remove(pl);
	}
	/**
	 * Notifies the registered planner listener about an {@link PlannerEvent}.
	 * @param pe the PalnnerEvent to transfer
	 */
	private void notifyPlannerListener(PlannerEvent pe) {
		for (PlannerListener pl : this.getPlannerListener()) {
			try {
				pl.onPlannerEvent(pe);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
