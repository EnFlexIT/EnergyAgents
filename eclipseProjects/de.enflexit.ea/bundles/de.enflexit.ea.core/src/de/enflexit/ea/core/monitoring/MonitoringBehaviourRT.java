package de.enflexit.ea.core.monitoring;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.EnergyAgentIO;
import de.enflexit.ea.core.AbstractInternalDataModel.ControlledSystemType;
import energy.FixedVariableList;
import energy.FixedVariableListForAggregation;
import energy.OptionModelController;
import energy.evaluation.AbstractEvaluationStrategyRT;
import energy.optionModel.EvaluationClass;
import energy.optionModel.InputMeasurementCalculatedByState;
import energy.optionModel.ScheduleList;
import energy.optionModel.SystemVariableDefinition;
import energy.optionModel.SystemVariableDefinitionOntology;
import energy.optionModel.SystemVariableDefinitionStaticModel;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energygroup.GroupController;
import energygroup.evaluation.AbstractGroupEvaluationStrategyRT;
import jade.core.behaviours.CyclicBehaviour;

/**
 * The Class MonitoringBehaviourRT will be used in case that a real time control 
 * was assigned as a systems default evaluation strategy.
 * 
 * @see AbstractEvaluationStrategyRT
 * @see AbstractGroupEvaluationStrategyRT
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class MonitoringBehaviourRT extends CyclicBehaviour implements Observer {

	private static final long serialVersionUID = -1460453061132067175L;
	
	private AbstractEnergyAgent energyAgent;

	private ControlledSystemType typeOfControlledSystem;
	private OptionModelController omc;
	private Vector<String> variableIDsForSetPoints;
	
	private MonitoringStrategyRT rtMonitoringStrategy;
	private AbstractGroupEvaluationStrategyRT rtGroupEvaluationStrategy;
	
	private EventListenerList listenersList;
	
	
	/**
	 * Instantiates a new real time monitoring behaviour.
	 * @param energyAgent the energy agent
	 */
	public MonitoringBehaviourRT(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
		this.initialize();
	}
	/**
	 * Gets the internal data model of the energy agent.
	 * @return the internal data model
	 */
	private AbstractInternalDataModel getInternalDataModel() {
		return this.energyAgent.getInternalDataModel();
	}
	/**
	 * Returns the EnergyAgentIO.
	 * @return the energy agent IO
	 */
	protected EnergyAgentIO getEnergyAgentIO() {
		return energyAgent.getEnergyAgentIO();
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onStart()
	 */
	@Override
	public void onStart() {
		this.getInternalDataModel().addObserver(this);
		super.onStart();
	}
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#onEnd()
	 */
	@Override
	public int onEnd() {
		this.getInternalDataModel().deleteObserver(this);
		return super.onEnd();
	}
	
	/**
	 * Initializes this behaviour.
	 */
	private void initialize() {
		// --- Remind type of controlled system and the corresponding OptionModelController -------
		this.typeOfControlledSystem = this.getInternalDataModel().getTypeOfControlledSystem();
		if (this.typeOfControlledSystem==ControlledSystemType.TechnicalSystem) {
			
			TechnicalSystem ts = this.getInternalDataModel().getOptionModelController().getTechnicalSystemCopy();
			this.omc = new OptionModelController();
			this.omc.setTechnicalSystem(ts);
			this.initControllerWithMonitoringStrategy();
			this.updateInputMeasurementsWithMonitoredMeasurements();
			
			this.rtMonitoringStrategy = (MonitoringStrategyRT) this.omc.getEvaluationStrategyRT();
			
		} else if (this.typeOfControlledSystem==ControlledSystemType.TechnicalSystemGroup) {
			
			TechnicalSystemGroup tsg = this.getInternalDataModel().getGroupController().getTechnicalSystemGroupCopy();
			GroupController gc = new GroupController();
			gc.setTechnicalSystemGroup(tsg);
			this.omc = gc.getGroupOptionModelController();
			this.initControllerWithMonitoringStrategy();
			this.updateInputMeasurementsWithMonitoredMeasurements();
			
			this.rtGroupEvaluationStrategy = (AbstractGroupEvaluationStrategyRT) this.omc.getEvaluationStrategyRT();

		}
	}

	/**
	 * Sets the realTime monitoring strategy to the current {@link TechnicalSystem} or {@link TechnicalSystemGroup}.
	 */
	private void initControllerWithMonitoringStrategy() {
		
		EvaluationClass eClass = new EvaluationClass();
		eClass.setStrategyID("RT Monitoring ");
		eClass.setDescription("Real Time Monitoring of a system");
		eClass.setClassName(MonitoringStrategyRT.class.getName());
	
		omc.getEvaluationSettings().getEvaluationClasses().add(eClass);
		omc.getEvaluationSettings().setEvaluationClass(MonitoringStrategyRT.class.getName());
		
		ScheduleList sc = omc.getEvaluationProcess().getEvaluationResults();
		sc.setNetworkID(this.energyAgent.getAID().getLocalName());
		sc.setDateSaved(Calendar.getInstance());
		sc.setDescription("Monitoring of NetworkComponent " + sc.getNetworkID() + ": " + sc.getSystemID() + "");
	}
	/**
	 * Sets the input measurements to the monitored measurements.
	 */
	private void updateInputMeasurementsWithMonitoredMeasurements() {
		
		// --- Clear input measurements ------------------- 
		omc.getInputMeasurements().clear();
		
		// --- Get Measurements for the system ------------
		List<SystemVariableDefinition> sysVarDefList = omc.getTechnicalSystem().getSystemVariables();
		for (SystemVariableDefinition sysVarDef : sysVarDefList) {
			
			boolean isOntology    = sysVarDef instanceof SystemVariableDefinitionOntology;
			boolean isStaticModel = sysVarDef instanceof SystemVariableDefinitionStaticModel;
			if ( isOntology==false && isStaticModel==false && sysVarDef.isSetPoint()==false && sysVarDef.isSetPointForUser()==false) {
				// --- Create an InputMeasurement for the system variable -
				InputMeasurementCalculatedByState imbs = new InputMeasurementCalculatedByState();
				imbs.setVariableID(sysVarDef.getVariableID());
				imbs.setCalculationClass(ValueMonitoring.class.getName());
				omc.getInputMeasurements().add(imbs);
			}
		}
	}
	
	/**
	 * Gets the listeners list.
	 * @return the listeners list
	 */
	private EventListenerList getListenersList() {
		if(this.listenersList == null){
			this.listenersList = new EventListenerList();
		}
		return this.listenersList;
	}
	
	/**
	 * Adds a {@link MonitoringListener}.
	 * @param listener The listener to add
	 */
	public void addMonitoringListener(MonitoringListener listener){
		this.getListenersList().add(MonitoringListener.class, listener);
	}
	
	/**
	 * Removes a {@link MonitoringListener}.
	 * @param listener The listener to remove
	 */
	public void removeMonitoringListener(MonitoringListener listener){
		this.getListenersList().remove(MonitoringListener.class, listener);
	}
	
	/**
	 * Notifies all registered {@link MonitoringListener}s about a {@link MonitoringEvent}.
	 * @param event The monitoring event
	 */
	protected synchronized void notifyMonitoringListeners(MonitoringEvent event){
		for (MonitoringListener listener : this.getListenersList().getListeners(MonitoringListener.class)) {
			listener.onMonitoringEvent(event);
		}
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
	
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		// --- Get the new measurements, if any ---------------------
		FixedVariableList measurements = this.getEnergyAgentIO().getMeasurementsFromSystem();
		FixedVariableList setpoints = this.getEnergyAgentIO().getSetPointsToSystem();	// TODO In die RealTime Strategie
		if (this.omc!=null && this.getEnergyAgentIO().getTime()!=null&& measurements!=null ) {

			// --- Get the current time -----------------------------
			long currentTime = this.getEnergyAgentIO().getTime();
			
			try {
				TechnicalSystemStateEvaluation tsseLast=null;
				switch (this.typeOfControlledSystem) {
				case TechnicalSystem:
					// --- Things to do for TechnicalSystems --------
					this.rtMonitoringStrategy.setMeasurementsFromSystem(measurements);
					this.rtMonitoringStrategy.setSetPointsToSystem(setpoints);
					this.rtMonitoringStrategy.runEvaluationUntil(currentTime);
					tsseLast = this.rtMonitoringStrategy.getTechnicalSystemStateEvaluation();
					break;

				case TechnicalSystemGroup:
					// --- Things to do for TechnicalSystemGroupss --
					this.rtGroupEvaluationStrategy.setMeasurementsFromSystem((FixedVariableListForAggregation) measurements);
					this.rtGroupEvaluationStrategy.setSetPointsToSystem((FixedVariableListForAggregation) setpoints);
					this.rtGroupEvaluationStrategy.runEvaluationUntil(currentTime); 
					tsseLast = this.rtGroupEvaluationStrategy.getTechnicalSystemStateEvaluation();
					break;

				default:
					break;
				}
				
				// --- Get the TSSE from the evaluation results -------
				if (tsseLast!=null) {
					tsseLast = MonitoringBehaviourRT.cloneTechnicalSystemStateEvaluation(tsseLast);
				} else {
					// und nu ???
				}
				
				// --- Create a monitoring event and notify the listeners -------
				MonitoringEvent me = new MonitoringEvent(this);
				me.setTsse(tsseLast);
				me.setMeasurements(measurements);
				me.setSetpoints(setpoints);
				me.setEventTime(currentTime);
				this.notifyMonitoringListeners(me);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		this.block();
	}
	
	public MonitoringStrategyRT getMonitoringStrategyRT() {
		return rtMonitoringStrategy;
	}

	public static TechnicalSystemStateEvaluation cloneTechnicalSystemStateEvaluation(TechnicalSystemStateEvaluation tsseFromSchedule) {
		
		TechnicalSystemStateEvaluation tsseReturn = null;
		if (tsseFromSchedule.getParent()!=null) {
			TechnicalSystemStateEvaluation tsseParent = tsseFromSchedule.getParent();
			tsseFromSchedule.setParent(null);
			tsseReturn = SerialClone.clone(tsseFromSchedule);
			tsseFromSchedule.setParent(tsseParent);
		} else {
			tsseReturn = SerialClone.clone(tsseFromSchedule);
		}
		return tsseReturn;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		if (updateObject==AbstractInternalDataModel.CHANGED.MEASUREMENTS_FROM_SYSTEM) {
			// --- Received new measurements --------------
			this.restart();
		}
	}

	/**
	 * Gets the internal OptionModelController to access the copied TechnicalSystem.
	 * @return the OptionModelController
	 */
	public OptionModelController getOptionModelController() {
		return omc;
	}
	
	/**
	 * Resets the evaluation process.
	 */
	public void resetEvaluationProcess() {
		this.getMonitoringStrategyRT().resetInitialTechnicalSystemStateEvaluation();
		this.getMonitoringStrategyRT().resetStrategy();
	}

}
