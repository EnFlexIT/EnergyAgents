package de.enflexit.ea.core.planning;

import de.enflexit.ea.core.AbstractEnergyAgent;
import energy.optionModel.AbstractCostFunction;
import energy.optionModel.AbstractInputMeasurement;
import energy.optionModel.CostFunctionDataSeries;
import energy.optionModel.InputMeasurement;
import energy.optionModel.State;
import energy.optionModel.TechnicalInterfaceConfiguration;
import energy.planning.EomPlanner;
import energy.planning.EomPlannerEvent;
import energy.planning.EomPlannerListener;
import energy.planning.EomPlannerResult;
import energy.planning.requests.AbstractCostFunctionRequest;
import energy.planning.requests.AbstractEomPlannerRequest;
import energy.planning.requests.AbstractInputMeasurementRequest;
import energy.planning.requests.ConfigIdRequest;
import energy.planning.requests.CostFunctionDataSeriesRequest;
import energy.planning.requests.CostUnitRequest;
import energy.planning.requests.EvaluationStateListRequest;
import energy.planning.requests.InputMeasurementRequest;
import energy.planning.requests.SetPointListRequest;
import energy.planning.requests.StateIdRequest;
import energy.planning.requests.StorageLoadListRequest;
import energy.planning.requests.CostUnitRequest.CostUnitDefinition;

/**
 * The AbstractPlanningDispatcherManager serves as base class for the individual configuration and listener
 * to an energy agents {@link PlanningDispatcher}.<br><br>
 * 
 * The main task of that listener is to receive status information about the execution
 * of a specific {@link Planner}, e.g. {@link EomPlannerEvent.PlannerEventType#Initialized}
 * or {@link EomPlannerEvent.PlannerEventType#PlanningFinalized} and react accordingly.<br>
 * 
 * Further, it is its task to provide required information to individual {@link Planner}, as 
 * for example the initial system states to be considered for a planning.  
 * 
 * @see PlanningDispatcher
 * @see AbstractPlanningDispatcherEvent
 */
public abstract class AbstractPlanningDispatcherManager<Agent extends AbstractEnergyAgent> implements EomPlannerListener {

	private Agent energyAgent;
	
	/**
	 * Instantiates a new planning dispatcher manager.
	 * @param energyAgent the energy agent that controls this dispatcher manager
	 */
	public AbstractPlanningDispatcherManager(Agent energyAgent) {
		if (energyAgent == null) {
			throw new NullPointerException("The Energy Agent instance is not allowed to be null!");
		}
		this.setEnergyAgent(energyAgent);
	}
	/**
	 * Sets the energy agent.
	 * @param energyAgent the new energy agent
	 */
	private void setEnergyAgent(Agent energyAgent) {
		this.energyAgent = energyAgent;
	}
	/**
	 * Returns the energy agent instance.
	 * @return the energy agent
	 */
	public Agent getEnergyAgent() {
		return energyAgent;
	}

	
	/**
	 * Will be invoked to enable an individual planner registration.
	 * @param planningDispatcher the planning dispatcher
	 */
	public abstract void registerPlanner(PlanningDispatcher planningDispatcher);
	
	/**
	 * Returns the energy agents {@link PlanningDispatcher}.
	 * @return the planning dispatcher
	 */
	public PlanningDispatcher getPlanningDispatcher() {
		return this.getEnergyAgent().getPlanningDispatcher();
	}
	/**
	 * Checks if there is an invalid {@link PlanningDispatcher} state.
	 * @return true, if is invalid planning dispatcher state
	 */
	private boolean isInvalidPlanningDispatcherState() {
		return this.getEnergyAgent().isPlanningDispatcherTerminated()==true || this.getPlanningDispatcher()==null;
	}
	
	
	/**
	 * Starts the default planning, which means all strategies configured as planning strategies will be executed.
	 * @param planFrom the time to plan from
	 * @param planTo the time to plan to
	 */
	public void startPlanning(long planFrom, long planTo) {
		this.startPlanning(null, planFrom, planTo);
	}
	/**
	 * Starts the planning of the specified planner.
	 *
	 * @param plannerName the name of the planner (if <code>null</code>, the default planning will  
	 * be used; means that all strategies configured as planning strategies will be executed. 
	 * @param planFrom the time to plan from
	 * @param planTo the time to plan to
	 */
	public void startPlanning(String plannerName, long planFrom, long planTo) {
		PlanningDispatcher pd = this.getPlanningDispatcher();
		if (pd!=null) {
			pd.startPlanning(plannerName, planFrom, planTo);
		}
	}

	/**
	 * Returns the current EomPlannerResult for the specified Planner.
	 *
	 * @param plannerName the planner name
	 * @return the EomPlannerResult
	 */
	public EomPlannerResult getEomPlannerResult(String plannerName) {
		return this.getPlanningDispatcher().getEomPlannerResult(plannerName);
	}
	
	
	// ------------------------------------------------------------------------
	// --- General notifications of an EomPlanner -----------------------------
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onPlannerEvent(energy.planning.EomPlannerEvent)
	 */
	@Override
	public final void onPlannerEvent(EomPlannerEvent eomPlannerEvent) {
		if (this.isInvalidPlanningDispatcherState()==true) return;
		this.onPlannerEvent(this.getPlannerName(eomPlannerEvent.getEomPlanner()), (Planner)eomPlannerEvent.getEomPlanner(), eomPlannerEvent);
	}
	/**
	 * Will be invoked to informs about a {@link EomPlannerEvent}.
	 *
	 * @param plannerName the planner name
	 * @param planner the planner that was executed
	 * @param eomPlannerEvent the planner event
	 */
	public abstract void onPlannerEvent(String plannerName, Planner planner, EomPlannerEvent eomPlannerEvent);

	
	
	// ------------------------------------------------------------------------
	// --- Methods for the initial evaluation state list ----------------------
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onEvaluationStateListRequest(energy.planning.requests.EvaluationStateListRequest)
	 */
	@Override
	public final EvaluationStateListRequest onEvaluationStateListRequest(EvaluationStateListRequest eslr) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onEvaluationStateListRequest(this.getPlannerName(eslr), eslr);
	}
	/**
	 * Will be invoked by the {@link Planner} if the evaluation state list to be used is required for the evaluation or planning process.
	 *
 	 * @param plannerName the planner name
	 * @param cidr the {@link ConfigIdRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract EvaluationStateListRequest onEvaluationStateListRequest(String plannerName, EvaluationStateListRequest eslr);

	
		
	// ------------------------------------------------------------------------
	// --- Methods for the initial state of an evaluation ---------------------
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onConfigIdRequest(energy.planning.requests.ConfigIdRequest)
	 */
	@Override
	public final ConfigIdRequest onConfigIdRequest(ConfigIdRequest cidr) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onConfigIdRequest(this.getPlannerName(cidr), cidr);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if the {@link TechnicalInterfaceConfiguration} to be used is required for the evaluation or planning process.
	 *
 	 * @param plannerName the planner name
	 * @param cidr the {@link ConfigIdRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract ConfigIdRequest onConfigIdRequest(String plannerName, ConfigIdRequest cidr);
	
	
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onStateIdRequest(energy.planning.requests.StateIdRequest)
	 */
	@Override
	public final StateIdRequest onStateIdRequest(StateIdRequest sidr) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onStateIdRequest(this.getPlannerName(sidr), sidr);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if the initial {@link State} to be used is required for the evaluation or planning process.
	 *
  	 * @param plannerName the planner name
	 * @param sidr the {@link StateIdRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract StateIdRequest onStateIdRequest(String plannerName, StateIdRequest sidr);
	

	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onSetPointListRequest(energy.planning.requests.SetPointListRequest)
	 */
	@Override
	public final SetPointListRequest onSetPointListRequest(SetPointListRequest splr) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onSetPointListRequest(this.getPlannerName(splr), splr);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} to request the initial set point settings to be used for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param splr the {@link SetPointListRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract SetPointListRequest onSetPointListRequest(String plannerName, SetPointListRequest splr);
	
	
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onStorageLoadListRequest(energy.planning.requests.StorageLoadListRequest)
	 */
	@Override
	public final StorageLoadListRequest onStorageLoadListRequest(StorageLoadListRequest sllr) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onStorageLoadListRequest(this.getPlannerName(sllr), sllr);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} to request the initial storage loads to be used for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param sllr the {@link StorageLoadListRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract StorageLoadListRequest onStorageLoadListRequest(String plannerName, StorageLoadListRequest sllr);
	
	
	
	// ------------------------------------------------------------------------
	// --- Methods for the companion data: measurements & cost models ---------
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onAbstractInputMeasurementRequest(energy.planning.requests.AbstractInputMeasurementRequest)
	 */
	@Override
	public final AbstractInputMeasurementRequest onAbstractInputMeasurementRequest(AbstractInputMeasurementRequest abstractInputMeasurementRequest) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onAbstractInputMeasurementRequest(this.getPlannerName(abstractInputMeasurementRequest), abstractInputMeasurementRequest);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if an {@link AbstractInputMeasurement} is required for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param abstractInputMeasurementRequest the actual {@link AbstractInputMeasurementRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract AbstractInputMeasurementRequest onAbstractInputMeasurementRequest(String plannerName, AbstractInputMeasurementRequest abstractInputMeasurementRequest);


	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onInputMeasurementRequest(energy.planning.requests.InputMeasurementRequest)
	 */
	@Override
	public final InputMeasurementRequest onInputMeasurementRequest(InputMeasurementRequest inputMeasurementRequest) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return onInputMeasurementRequest(this.getPlannerName(inputMeasurementRequest), inputMeasurementRequest);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if an {@link InputMeasurement} is required for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param inputMeasurementRequest the actual {@link InputMeasurementRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract InputMeasurementRequest onInputMeasurementRequest(String plannerName, InputMeasurementRequest inputMeasurementRequest);
	

		
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onCostUnitRequest(energy.planning.requests.CostUnitRequest)
	 */
	@Override
	public final CostUnitRequest onCostUnitRequest(CostUnitRequest costUnitRequest) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onCostUnitRequest(this.getPlannerName(costUnitRequest), costUnitRequest);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if a cost model to be used requires an update of the {@link CostUnitDefinition} for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param costUnitRequest the actual {@link CostUnitRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract CostUnitRequest onCostUnitRequest(String plannerName, CostUnitRequest costUnitRequest);
	
	
	
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onAbstractCostFunctionRequest(energy.planning.requests.AbstractCostFunctionRequest)
	 */
	@Override
	public final AbstractCostFunctionRequest onAbstractCostFunctionRequest(AbstractCostFunctionRequest abstractCostFunctionRequest) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onAbstractCostFunctionRequest(this.getPlannerName(abstractCostFunctionRequest), abstractCostFunctionRequest);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if a cost model to be used requires an update of a {@link AbstractCostFunction} for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param abstractCostFunctionRequest the actual {@link AbstractCostFunctionRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract AbstractCostFunctionRequest onAbstractCostFunctionRequest(String plannerName, AbstractCostFunctionRequest abstractCostFunctionRequest);
	
	
	
	/* (non-Javadoc)
	 * @see energy.planning.EomPlannerListener#onCostFunctionDataSeriesRequest(energy.planning.requests.CostFunctionDataSeriesRequest)
	 */
	@Override
	public final CostFunctionDataSeriesRequest onCostFunctionDataSeriesRequest(CostFunctionDataSeriesRequest costFunctionDataSeriesRequest) {
		if (this.isInvalidPlanningDispatcherState()==true) return null;
		return this.onCostFunctionDataSeriesRequest(this.getPlannerName(costFunctionDataSeriesRequest), costFunctionDataSeriesRequest);
	}
	/**
	 * Will be invoked by the {@link EomPlanner} if a cost model to be used requires an update of a {@link CostFunctionDataSeries} for the evaluation or planning process.
	 *
	 * @param plannerName the planner name
	 * @param costFunctionDataSeriesRequest the actual {@link CostFunctionDataSeriesRequest} to answer
	 * @return has to return the answer as same type as the request but with a filled answer slot.
	 */
	public abstract CostFunctionDataSeriesRequest onCostFunctionDataSeriesRequest(String plannerName, CostFunctionDataSeriesRequest costFunctionDataSeriesRequest);
	
	
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, some help methods -------------------------------------------------
	// ----------------------------------------------------------------------------------	
	/**
	 * Return the planner name from the specified planner request.
	 *
	 * @param eomPlannerRequest the request of an EomPlanner 
	 * @return the planner name
	 */
	private String getPlannerName(AbstractEomPlannerRequest<?> eomPlannerRequest) {
		EomPlanner eomPlanner = eomPlannerRequest.getEomPlanner();
		return this.getPlannerName(eomPlanner);
	}
	/**
	 * Returns the planner name for the specified EomPlanner if that instance is of type {@link Planner}.
	 *
	 * @param eomPlanner the EomPlanner instance
	 * @return the planner name
	 */
	private String getPlannerName(EomPlanner eomPlanner) {
		if (eomPlanner instanceof Planner) {
			return ((Planner) eomPlanner).getPlannerName();
		}
		return null;
	}
	
}
