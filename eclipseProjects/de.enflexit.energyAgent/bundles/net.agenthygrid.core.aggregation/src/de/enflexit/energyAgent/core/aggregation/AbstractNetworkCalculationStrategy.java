package de.enflexit.energyAgent.core.aggregation;

import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.core.application.Application;
import agentgui.core.project.Project;
import energy.DisplayHelper;
import energy.OptionModelController;
import energy.evaluation.EvaluationProcess;
import energy.evaluation.TechnicalSystemStateDeltaEvaluation;
import energy.optionModel.EnergyFlowInWatt;
import energy.optionModel.EnergyFlowMeasured;
import energy.optionModel.EnergyMeasurement;
import energy.optionModel.EnergyUnitFactorPrefixSI;
import energy.optionModel.GoodFlow;
import energy.optionModel.GoodFlowMeasured;
import energy.optionModel.GoodMeasurement;
import energy.optionModel.GroupMember;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.ScheduleController;
import energy.schedule.ScheduleNotification;
import energygroup.GroupTreeNodeObject;
import energygroup.evaluation.AbstractGroupEvaluationStrategy;
import energygroup.evaluation.AddResultTreeAction;
import energygroup.evaluation.MemberEvaluationStrategyScheduList;

/**
 * The Class AbstractNetworkCalculationStrategy serves as super class for individual network calculation
 * strategies. Thus, individual, domain specific network calculations can be realized (e.g. for electrical,
 * heat or natural gas networks).
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractNetworkCalculationStrategy extends AbstractGroupEvaluationStrategy {

	private boolean debug = false;
	
	private AbstractAggregationHandler aggregationHandler;
	private AbstractSubNetworkConfiguration subAggregationConfiguration;
	
	private NetworkModel networkModel;
	private long evaluationStepEndTime;
	
	private TechnicalSystemStateEvaluation tsse;
	private Schedule scheduleResult;
	private AddResultTreeAction addResultTreeAction;
	
	/**
	 * Instantiates a new abstract network calculation strategy.
	 * @param optionModelController the option model controller of the aggregator 
	 */
	public AbstractNetworkCalculationStrategy(OptionModelController optionModelController) {
		super(optionModelController);
	}
	
	/**
	 * Gets the domain.
	 * @return the domain
	 */
	protected String getDomain() {
		return this.subAggregationConfiguration.getDomain();
	}
	
	/**
	 * Sets the aggregation handler and registers it as listener for the actual network calculation.
	 * @param aggregationHandler the new aggregation handler
	 */
	public void setAggregationHandler(AbstractAggregationHandler aggregationHandler) {
		this.aggregationHandler = aggregationHandler;
	}
	/**
	 * Returns the current aggregation handler.
	 * @return the aggregation handler
	 */
	public AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}
	
	/**
	 * Sets the current sub aggregation configuration.
	 * @param subAggregationConfiguration the new sub aggregation configuration
	 */
	public void setSubAggregationConfiguration(AbstractSubNetworkConfiguration subAggregationConfiguration) {
		this.subAggregationConfiguration = subAggregationConfiguration;
	}
	/**
	 * Returns the current sub aggregation configuration.
	 * @return the sub aggregation configuration
	 */
	public AbstractSubNetworkConfiguration getSubAggregationConfiguration() {
		return subAggregationConfiguration;
	}
	
	/**
	 * Returns the current Agent.Workbench {@link NetworkModel} from the aggregation handler, which
	 * has to be transferred via the constructor of the aggregation handler. 
	 * 
	 * @see AbstractAggregationHandler#getNetworkModel()
	 * @return the network model
	 */
	protected NetworkModel getNetworkModel() {
		if (this.networkModel==null) {
			// --- First, try to get the network model from the aggregator ----
			this.networkModel = this.getAggregationHandler().getNetworkModel();
			if (this.networkModel==null) {
				// ----------------------------------------------------------------
				// --- The NetworkModel was not set after initiating this class ---
				// --- Try to get the model from the simulation setup -------------
				// ----------------------------------------------------------------
				Project currProject = Application.getProjectFocused();
				if (currProject!=null && currProject.getEnvironmentController()!=null) {
					GraphEnvironmentController graphController =  (GraphEnvironmentController) currProject.getEnvironmentController();
					this.networkModel = graphController.getNetworkModel();
				}	
			}
		}
		return this.networkModel;
	}
	
	/**
	 * Returns the time step that is configured with the HyGird setup.
	 * @return the time step configured
	 */
	protected long getTimeStepConfigured() {
		AbstractSubAggregationBuilder subAggregationBuilder = this.getSubAggregationConfiguration().getSubAggregationBuilder();
		return subAggregationBuilder.getTimeStepConfigured();
	}
	/**
	 * Will be executed to run the EOM evaluation until the specified time .
	 * @param evaluationStepEndTime the evaluation step end time
	 */
	public void runEvaluationUntil(long evaluationStepEndTime) {
		this.runEvaluationUntil(evaluationStepEndTime, false);
	}
	
	/**
	 * This method will execute the EOM evaluation until the specified time, optionally rebuilding the EOM decission tree
	 * @param evaluationStepEndTime the evaluation step end time
	 * @param rebuildDecisionGraph if true, the decision graph will be rebuilt
	 */
	public void runEvaluationUntil(long evaluationStepEndTime, boolean rebuildDecisionGraph) {
		this.evaluationStepEndTime = evaluationStepEndTime;
		this.runEvaluation(rebuildDecisionGraph);
	}
	/**
	 * Returns the time, at which the evaluation should stop.
	 * @return the time where evaluation interrupts
	 */
	public long getEvaluationStepEndTime() {
		return evaluationStepEndTime;
	}
	
	/**
	 * Does the preprocessing.
	 */
	private void doPreprocessing() {
		AbstractNetworkCalculationPreprocessor preProcessor = this.getSubAggregationConfiguration().getNetworkCalculationPreprocessor();
		if (preProcessor!=null) {
			try {
				preProcessor.doPreprocessing(this.getEvaluationStepEndTime());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see energy.evaluation.AbstractEvaluationStrategy#runEvaluation()
	 */
	@Override
	public void runEvaluation() {
		this.runEvaluation(false);
	}
		
		
	/**
	 * Execute the evaluation.
	 * @param rebuildDecisionGraph If true, the decision graph will be rebuilt
	 */
	public void runEvaluation(boolean rebuildDecisionGraph) {
		
		this.setDebug(false);
		
		// --- Get Start situation ------------------------------------------------------
		TechnicalSystemStateEvaluation tsse = this.getTechnicalSystemStateEvaluation();
		
		// --- Check / do the preprocessing (e.g. a state estimation --------------------
		this.doPreprocessing();
		
		// --- Search by walking through time -------------------------------------------
		while (tsse.getGlobalTime() < this.getEvaluationStepEndTime()) {
			
			long duration = 0;
			// --- Check if the next time step will reach the interrupt time ------------
			if (tsse.getGlobalTime() + this.getTimeStepConfigured() > this.getEvaluationStepEndTime()) {
				duration = this.getEvaluationStepEndTime() - tsse.getGlobalTime();
				this.debugPrintLine(tsse.getGlobalTime(), " ===> (Old TSSE GlobalTime) State Time: " + tsse.getStateTime());
				this.debugPrintLine(this.getEvaluationStepEndTime(), " ===> (Evaluation End Time) Duration: " + duration);
			} 
			
			// --------------------------------------------------------------------------
			// --- Get the possible subsequent steps and states -------------------------
			// --------------------------------------------------------------------------
			Vector<TechnicalSystemStateDeltaEvaluation> deltaSteps = this.getAllDeltaEvaluationsStartingFromTechnicalSystemState(tsse, duration, rebuildDecisionGraph);
			if (deltaSteps.size()==0) {
				System.err.println("No further 'deltaStepsPossible' => interrupt search!");
				break;
			}
			
			// --- Prepare the next time step -------------------------------------------
			TechnicalSystemStateDeltaEvaluation tssDeltaEvaluation = deltaSteps.get(0);
			
			// --- Replace TechnicalSystemStateEvaluation by the next one ---------------
			TechnicalSystemStateEvaluation tsseNext = this.getNextTechnicalSystemStateEvaluation(tsse, tssDeltaEvaluation);
			
			// --------------------------------------------------------------------------
			// --- Set new current TechnicalSystemStateEvaluation -----------------------
			// --------------------------------------------------------------------------
			if (tsseNext==null) {
				System.err.println("No possible delta evaluation could be found!");
				break;
			} 
			// --- Set intermediate state as result -------------------------------------
			tsse = tsseNext;
			this.debugPrintLine(tsseNext.getGlobalTime(), " ===> (Evaluation Step Time) StateTime: " + tsseNext.getStateTime());
			
			this.setTechnicalSystemStateEvaluation(tsse);
			this.setIntermediateStateToResult(tsse);
		}
		
		this.debugPrintLine(null, "");
	}
	
	/**
	 * Returns the current (or last) system state as {@link TechnicalSystemStateEvaluation}.
	 * @return the technical system state evaluation
	 */
	public TechnicalSystemStateEvaluation getTechnicalSystemStateEvaluation() {
		if (tsse==null) {
			// --- Update initial states I/O-List and the energy flows --------
			TechnicalSystemState tssStart = (TechnicalSystemState) this.getEvaluationSettings().getEvaluationStateList().get(0);
			this.optionModelController.updateTechnicalSystemStateIOList(tssStart);
			// --- Get energy flows, execute network calculation --------------
			this.optionModelController.updateTechnicalSystemStateEnergyFlows(tssStart, 0, false);
			// --- Convert to a TechnicalSystemStateEvaluation ---------------- 
			tsse = this.optionModelController.convertToTechnicalSystemStateEvaluation(tssStart);
			// --- Set this as the initial state for the evaluation -----------
			this.getEvaluationProcess().setInitialTechnicalSystemStateEvaluation(tsse);
			this.setIntermediateStateToResult(tsse);
			this.debugPrintLine(tsse.getGlobalTime(), "Initial TSSE for calculation");
		}
		return tsse;
	}
	/**
	 * Sets the technical system state evaluation.
	 * @param newTSSE the new technical system state evaluation
	 */
	public void setTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation newTSSE) {
		this.tsse = newTSSE;
	}
	
	/**
	 * Adds the intermediate state to the result schedule.
	 * @param tsse the intermediate {@link TechnicalSystemStateEvaluation}
	 */
	protected void setIntermediateStateToResult(TechnicalSystemStateEvaluation tsse) {
		if (tsse!=null) { 
			if (this.scheduleResult==null) {
				// --- First intermediate result ----------------------------------------
				this.scheduleResult = this.addStateToResults(tsse);
				this.scheduleResult.setRealTimeSchedule(true);
			} else {
				// --- New intermediate result ------------------------------------------
				this.scheduleResult.setTechnicalSystemStateEvaluation(tsse);
				this.scheduleResult.setCalculationTime(System.currentTimeMillis() - this.evaluationStart); 
				// --- Update visualization ---------------------------------------------
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// --- Update the result view -------------------------------------------
						getEvaluationProcess().getScheduleController().setChangedAndNotifyObservers(new ScheduleNotification(ScheduleNotification.Reason.ScheduleUpdated, scheduleResult));
						// --- Remind the sub result schedules in the evaluation process ------
						getEvaluationProcess().addSubSchedules(scheduleResult, getAddResultTreeAction().getSubScheduleHash());
						// --- Start to collect the sub results -------------------------------
						getAddResultTreeAction().doGroupTreeAction();
					}
				});
			}
		}
	}
	
	/**
	 * Gets the AddResultTreeAction that enables to remind the sub results and to place
	 * them in the {@link EvaluationProcess}.
	 * 
	 * @return the adds the result tree action
	 */
	private AddResultTreeAction getAddResultTreeAction() {
		if (addResultTreeAction==null) {
			addResultTreeAction = new AddResultTreeAction(this.getGroupController(), this);
		}
		return addResultTreeAction;
	}
	
	// ----------------------------------------------------------------------------------
	// --- From here the list of abstract methods can be found --------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Has to Terminate all related strategy instances. For example if a three phase electrical
	 * calculation is organized in several threads, they should be terminated here.
	 */
	public abstract void terminateRelatedStrategyInstances();
	
	
	
	/**
	 * Update member evaluation strategy schedule list.
	 * @param sc the {@link ScheduleController} that controls the actual schedule
	 */
	public void restartMemberEvaluationStrategyScheduleList(ScheduleController sc) {
		// --- Try to receive the corresponding GroupMember ---------
		GroupMember gm = this.getGroupController().getGroupTreeModel().getGroupMember(sc);
		if (gm!=null) {
			DefaultMutableTreeNode currentNode = this.getGroupController().getGroupTreeModel().getGroupMemberToNodeHash().get(gm);
			GroupTreeNodeObject gtno = this.getGroupController().getGroupTreeModel().getGroupTreeNodeObject(gm);
			MemberEvaluationStrategyScheduList subEvaluStrat =  (MemberEvaluationStrategyScheduList) gtno.getGroupMemberEvaluationStrategy(this);
			if (subEvaluStrat==null) {
				subEvaluStrat = new MemberEvaluationStrategyScheduList(this.getGroupController().getGroupOptionModelController(), this, currentNode, gtno, sc);
				gtno.setGroupMemberEvaluationStrategy(this, subEvaluStrat);
			}
			subEvaluStrat.getInitialTechnicalSystemStateEvaluation();
		}
	}
	/**
	 * Append the {@link TechnicalSystemStateEvaluation} to {@link MemberEvaluationStrategyScheduList}.
	 * @param sc the {@link ScheduleController} that manages the {@link ScheduleList} or {@link Schedule} respectively 
	 * @param tsse the {@link TechnicalSystemStateEvaluation} to add
	 */
	public void appendToMemberEvaluationStrategyScheduleList(ScheduleController sc, TechnicalSystemStateEvaluation tsse) {
		// --- Try to receive the corresponding GroupMember ---------
		GroupMember gm = this.getGroupController().getGroupTreeModel().getGroupMember(sc);
		if (gm!=null) {
			GroupTreeNodeObject gtno = this.getGroupController().getGroupTreeModel().getGroupTreeNodeObject(gm);
			MemberEvaluationStrategyScheduList subEvaluStrat =  (MemberEvaluationStrategyScheduList) gtno.getGroupMemberEvaluationStrategy(this);
			if (subEvaluStrat==null) {
				this.restartMemberEvaluationStrategyScheduleList(sc);
				this.appendToMemberEvaluationStrategyScheduleList(sc, tsse);
			} else {
				subEvaluStrat.append(tsse);	
			}
		}
	}
	
	// ----------------------------------------------------------------------------------
	// --- From here, help methods for debugging a calculation strategy can be found ----
	// ----------------------------------------------------------------------------------
	/**
	 * Sets to debug the current execution. If set true, the debug prints will be shown in the console.
	 * @param debug the new debug
	 * 
	 * @see #debugPrintLine(Long, String)
	 * @see #isDebug()
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	/**
	 * Checks if the current execution is to be debugged. If true, the debug prints will be shown in the console.
	 * @return true, if is debug
	 * 
	 * @see #debugPrintLine(Long, String)
	 * @see #setDebug(boolean)
	 */
	public boolean isDebug() {
		return debug;
	}
	/**
	 * Prints the specified message. In case that the local method {@link #isDebug()} returns true.
	 * @param message the message
	 * 
	 * @see #isDebug()
	 * @see #setDebug(boolean)
	 */
	public void debugPrintLine(Long timeStamp, String message) {
		if (this.isDebug()==true) {
			if (timeStamp==null) {
				System.out.println(message);
			} else {
				DisplayHelper.systemOutPrintlnGlobalTime(timeStamp, "", message);
			}
		}
	}
	
	/**
	 * Returns an EnergyFlowMeasured that is zero over time.
	 *
	 * @param interfaceID the interface id
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @param siPrefix the the SI prefix to use
	 * @return the energy flow measured zero over time
	 */
	protected EnergyFlowMeasured getEnergyFlowMeasuredZeroOverTime(String interfaceID, long timeFrom, long timeTo, EnergyUnitFactorPrefixSI siPrefix) {
		
		// --- EnergyFlow for time from ---------
		EnergyFlowInWatt ef1 = new EnergyFlowInWatt();
		ef1.setSIPrefix(siPrefix);
		ef1.setValue(0);
		
		EnergyMeasurement em1 = new EnergyMeasurement();
		em1.setPointInTime(timeFrom);
		em1.setEnergyFlow(ef1);

		// --- EnergyFlow for time to -----------
		EnergyFlowInWatt ef2 = new EnergyFlowInWatt();
		ef2.setSIPrefix(siPrefix);
		ef2.setValue(0);
		
		EnergyMeasurement em2 = new EnergyMeasurement();
		em2.setPointInTime(timeTo);
		em2.setEnergyFlow(ef2);

		// --- Define the measurements ----------
		EnergyFlowMeasured efm = new EnergyFlowMeasured();
		efm.setStepSeries(true);
		efm.setInterfaceID(interfaceID);
		efm.getMeasurments().add(em1);
		efm.getMeasurments().add(em2);
		return efm;
	}
	
	/**
	 * returns a good flow measured that is zero over time.
	 * @param interfaceID the interface ID
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the good flow measured zero over time
	 */
	protected GoodFlowMeasured getGoodFlowMeasuredZeroOverTime(String interfaceID, long timeFrom, long timeTo) {
		GoodFlow gf1 = new GoodFlow();
		gf1.setValue(0);
		
		GoodMeasurement gm1 = new GoodMeasurement();
		gm1.setPointInTime(timeFrom);
		gm1.setGoodFlow(gf1);
		
		GoodFlow gf2 = new GoodFlow();
		gf2.setValue(0);
		
		GoodMeasurement gm2 = new GoodMeasurement();
		gm2.setPointInTime(timeTo);
		gm2.setGoodFlow(gf2);
		
		GoodFlowMeasured gfm = new GoodFlowMeasured();
		gfm.setInterfaceID(interfaceID);
		gfm.setStepSeries(true);
		gfm.getMeasurments().add(gm1);
		gfm.getMeasurments().add(gm2);
		
		return gfm;
	}
}
