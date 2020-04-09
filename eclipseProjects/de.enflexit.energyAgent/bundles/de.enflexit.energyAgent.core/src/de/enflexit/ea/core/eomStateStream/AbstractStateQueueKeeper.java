package de.enflexit.ea.core.eomStateStream;

import java.util.ArrayList;
import java.util.List;

import de.enflexit.eom.awb.stateStream.SystemStateDispatcherAgentConnector;
import energy.optionModel.Schedule;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.schedule.loading.ScheduleTimeRange;

/**
 * The Class AbstractStateQueueKeeper is an abstract class that provides 
 * some help methods for specific state queue monitor types.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public abstract class AbstractStateQueueKeeper {

	protected EomModelStateInputStream eomInputStream;
	protected SystemStateDispatcherAgentConnector dispatchConnector;
	protected ScheduleTimeRange globalScheduleTimeRange;
	
	// --- Variables for counting and decisions -------------------------------
	/** Defines the minimum number of states that recommend to reload further system states */
	protected int minNumberOfStates = 2;
	/** The expected time consumption for loading data for the current instance. Default value is 5s */
	protected long timeConsumptionLoadProcess = 5000; // 5s
	
	// --- Variables to measure the temporal consumption of system states -----
	private long tickingTimeLast = 0;
	private List<Long> tickingIntervalList;
	private int maxNumberOfTicksForAverage = 10;
	private long tickingIntervalAverage;
	
	
	/**
	 * Instantiates a new state queue monitor.
	 *
	 * @param owningEomInputStream the current {@link EomModelStateInputStream} that holds this instance
	 * @param dispatchConnector the SystemStateDispatcherAgentConnector to use
	 */
	public AbstractStateQueueKeeper(EomModelStateInputStream owningEomInputStream, SystemStateDispatcherAgentConnector dispatchConnector) {
		this.eomInputStream = owningEomInputStream;
		this.dispatchConnector = dispatchConnector;
		this.globalScheduleTimeRange = this.dispatchConnector.getGlobalScheduleTimeRange();
	}
	
	/**
	 * Checks if the operating mode the class is 'debug'. For this, the local variables {@link #isDebug()} 
	 * and {@link #debugNetworkComponentID} will be evaluated. 
	 * 
	 * @return true, if the class is executed in the debug mode
	 */
	protected boolean isDebug() {
		return this.eomInputStream.isDebug();	
	}
	
	/**
	 * Returns the Schedule that is to be used for the energy and state transmission
	 * to the simulation manager.
	 * @return the schedule energy transmission
	 */
	public Schedule getScheduleEnergyTransmission() {
		return this.eomInputStream.getScheduleEnergyTransmission();
	}
	
	/**
	 * Extend the Schedule that is used for the energy and state transmission to the SimulationManager.
	 * @param scheduleExtension the schedule that has to extend the current transmission Schedule
	 */
	protected void extendScheduleEnergyTransmission(Schedule scheduleExtension) {
		this.eomInputStream.addToScheduleEnergyTransmission(scheduleExtension);
	}
	
	/**
	 * Returns the number of system states that are currently in the queue for an energy and state transmission.
	 * @return the number of system states in queue
	 */
	protected int getNumberOfStatesInQueue() {
		if (this.getScheduleEnergyTransmission()!=null) {
			return this.getScheduleEnergyTransmission().getTechnicalSystemStateList().size();
		}
		return 0;
	}
	/**
	 * Returns the number of system states that are remaining in the queue for an energy and state transmission.
	 * @return the number of system states in queue
	 */
	protected Integer getNumberOfStatesInQueueRemaining() {
		
		Integer remainingStates = this.eomInputStream.getIndexLastStateAccess();
		if (remainingStates==null) {
			remainingStates = 0;
		} else {
			remainingStates++;
		}
		return remainingStates;
	}
	
	/**
	 * This method will be invoked, if a {@link TechnicalSystemStateEvaluation} was picked 
	 * in the current {@link EomModelStateInputStream} and will update the local performance measurements.
	 */
	public void onPickedTechnicalSystemState() {
		
		// --- For the starting phase ---------------------
		if (this.tickingTimeLast==0) {
			this.tickingTimeLast = System.currentTimeMillis();
			return;
		}
		
		// ------------------------------------------------
		// --- Add new duration to measurement list -------
		long currentTime = System.currentTimeMillis();
		long duration = currentTime - this.tickingTimeLast;
		if (duration<=3) return;
		
		this.getTickingIntervalList().add(duration);
		this.tickingTimeLast = currentTime;
		
		// --- Reduce number of durations -----------------
		while (this.getTickingIntervalList().size() > this.maxNumberOfTicksForAverage) {
			this.getTickingIntervalList().remove(0);
		}
		// --- Finally, update the average ----------------
		this.updateTickingIntervalAverage();

		// --- Recommend to reload data? ------------------
		boolean isMoreThanThreeTicks = this.getTickingIntervalList().size()>3;
		long durationWhereNoStatesRemain = this.getTickingIntervalAverage() * this.getNumberOfStatesInQueueRemaining();
		boolean isDataReloadRecommended = isMoreThanThreeTicks==true && (this.getNumberOfStatesInQueueRemaining()<=this.minNumberOfStates || durationWhereNoStatesRemain <= this.timeConsumptionLoadProcess);

		// --- Print some debugging output? ---------------
		if (this.isDebug()==true && isDataReloadRecommended) {
			String debugText = "[" + this.getClass().getSimpleName() + "]";
			debugText += "[" + this.eomInputStream.getNetworkComponent().getId() + "] ";
			debugText += "Ticking Average: " + this.getTickingIntervalAverage() + " ms, ";
			debugText += "Remaining States in queue: " + this.getNumberOfStatesInQueueRemaining() + ", "; 
			debugText += "Recommend to reload: " + isDataReloadRecommended;
			System.out.println(debugText);
		}
		
		// ------------------------------------------------
		// --- Start a SystemStateLoadRequest? ------------
		this.checkRemainingStatesAndPossiblyStartStartLoading(this.getNumberOfStatesInQueueRemaining(), isDataReloadRecommended);
	}
	
	/**
	 * Has to consider the specified, remaining number of states and finally decide if a load process needs to be executed.
	 * If started and a resulting Schedule is available that has to extend the current transmission Schedule, use 
	 * {@link #extendScheduleEnergyTransmission(Schedule)} to add the Schedule's data.
	 * <p>
	 * The attribute '<i>isDataReloadRecommended</i>' is just a suggestion that can (not must) be taken into account.
	 * This indicator is determined by the constant measurement of 'ticking' times (see {@link #getTickingIntervalAverage()})
	 * and the time that is considered as time for reloading state data (see {@link #timeConsumptionLoadProcess}). Here, the
	 * default is set to 5s ({@link #timeConsumptionLoadProcess}), but value can be adjusted within extended classes 
	 * (e.g. by explicitly measure that time consumption). 
	 *     
	 *
	 * @param remainingStatesInQueue the remaining states in transmission queue
	 * @param isDataReloadRecommended indicator if a data reload is recommended
	 */
	public abstract void checkRemainingStatesAndPossiblyStartStartLoading(int remainingStatesInQueue, boolean isDataReloadRecommended);

	
	
	// ------------------------------------------------------------------------
	// --- Measurement of the state consumption -------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Return the list of measured interval times betweeb ticks.
	 * @return the ticking interval list
	 */
	private List<Long> getTickingIntervalList() {
		if (tickingIntervalList==null) {
			tickingIntervalList = new ArrayList<>();
		}
		return tickingIntervalList;
	}
	/**
	 * Updates the ticking interval average.
	 */
	private void updateTickingIntervalAverage() {

		// --- For the first measurement --------
		if (this.getTickingIntervalList().size()==1) {
			this.tickingIntervalAverage = this.getTickingIntervalList().get(0); 
			return;
		}
		
		// --- Summarize ------------------------
		long sum = 0;
		for (int i = 0; i < this.getTickingIntervalList().size(); i++) {
			sum += this.getTickingIntervalList().get(i);
		}
		// --- Calculate new average ------------
		this.tickingIntervalAverage = sum / this.getTickingIntervalList().size();
	}
	/**
	 * Returns the average ticking interval for sending system states 
	 * to the simulation manager in milliseconds.
	 * @return the ticking interval average in ms
	 */
	public long getTickingIntervalAverage() {
		return tickingIntervalAverage;
	}

}
