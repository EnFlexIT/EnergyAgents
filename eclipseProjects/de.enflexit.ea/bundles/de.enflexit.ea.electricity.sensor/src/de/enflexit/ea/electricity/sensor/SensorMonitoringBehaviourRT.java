package de.enflexit.ea.electricity.sensor;

import java.text.SimpleDateFormat;
import java.util.Date;
import de.enflexit.awb.core.Application;
import de.enflexit.common.Observable;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.monitoring.MonitoringBehaviourRT;
import de.enflexit.ea.core.monitoring.MonitoringEvent;
import de.enflexit.ea.core.monitoring.MonitoringStrategyRT;
import energy.FixedVariableList;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * Special {@link MonitoringBehaviourRT} for sensor agents, generating a schedule of TSSEs without an actual EOM system model 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SensorMonitoringBehaviourRT extends MonitoringBehaviourRT {

	private static final long serialVersionUID = 5174925524077439691L;
	
	private static final String TSSE_CONFIGURATION_ID = "Default";
	private static final String TSSE_STATE_ID = "Sensing";
	
	private ScheduleList scheduleList;
	private Schedule schedule;
	
	private boolean debug = false;
	
	private boolean newMeasurement = false;

	/**
	 * Instantiates a new sensor monitoring behaviour RT.
	 *
	 * @param energyAgent the energy agent
	 */
	public SensorMonitoringBehaviourRT(AbstractEnergyAgent energyAgent) {
		super(energyAgent);
	}
	
	/**
	 * Action.
	 */
	@Override
	public void action() {
		if (this.debug==true) {
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
			System.out.println(myAgent.getLocalName() + " MonitoringBehaviour triggered at " + sdf.format(new Date(System.currentTimeMillis())) + ", new measurement: " + this.newMeasurement);
		}
		// --- Get the new measurements, if any ---------------------
		if (this.newMeasurement==true) {
			
			FixedVariableList measurements = this.getEnergyAgentIO().getMeasurementsFromSystem();
			if (measurements!=null && measurements.size()>0) {
				
				// --- Create a TSSE for the current measurements -----------------
				long currentTime = this.getEnergyAgentIO().getTime();
				TechnicalSystemStateEvaluation tsse = new TechnicalSystemStateEvaluation();
				tsse.setGlobalTime(currentTime);
				tsse.setConfigID(TSSE_CONFIGURATION_ID);
				tsse.setStateID(TSSE_STATE_ID);
				tsse.getIOlist().addAll(measurements);
				this.appendTsseToSchedule(tsse);
				
				// --- Create a monitoring event and notify the listeners ---------
				MonitoringEvent me = new MonitoringEvent(this);
				me.setTsse(tsse);
				me.setMeasurements(measurements);
				me.setEventTime(currentTime);
				this.notifyMonitoringListeners(me);
			}
			
			this.newMeasurement = false;
		}
		
		this.block();
	}
	

	/**
	 * Update.
	 *
	 * @param observable the observable
	 * @param updateObject the update object
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		if (updateObject==AbstractInternalDataModel.CHANGED.MEASUREMENTS_FROM_SYSTEM) {
			this.newMeasurement=true;
			super.update(observable, updateObject);
		}
	}

	/**
	 * Gets the schedule list.
	 * @return the schedule list
	 */
	private ScheduleList getScheduleList() {
		if (scheduleList==null) {
			scheduleList = new ScheduleList();
			scheduleList.setSystemID("Sensor");
			scheduleList.setNetworkID(myAgent.getLocalName());
			scheduleList.setSetup(Application.getProjectFocused().getSimulationSetupCurrent());
			((AbstractEnergyAgent)myAgent).getSystenStateLogWriter().setScheduleList(scheduleList);
		}
		return scheduleList;
	}

	/**
	 * Gets the schedule.
	 * @return the schedule
	 */
	private Schedule getSchedule() {
		if (schedule==null) {
			schedule = new Schedule();
			schedule.setStrategyClass(MonitoringStrategyRT.class.getName());
			this.getScheduleList().getSchedules().add(schedule);
		}
		return schedule;
	}
	
	/**
	 * Appends a tsse to the monitoring schedule.
	 * @param tsse the tsse
	 */
	private void appendTsseToSchedule(TechnicalSystemStateEvaluation tsse) {
		TechnicalSystemStateEvaluation parentTsse = this.getSchedule().getTechnicalSystemStateEvaluation();
		if (parentTsse!=null) {
			long stateTime = tsse.getGlobalTime() - parentTsse.getGlobalTime();
			tsse.setStateTime(stateTime);
		}
		tsse.setParent(this.getSchedule().getTechnicalSystemStateEvaluation());
		this.getSchedule().setTechnicalSystemStateEvaluation(tsse);
	}

}
