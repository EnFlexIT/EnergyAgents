package de.enflexit.ea.core.eomStateStream;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import agentgui.simulationService.time.TimeModelContinuous;
import energy.optionModel.Schedule;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class TimeTrigger can be used in order to fire events according to a single or a list of event times 
 * and according to the current system usage (simulation or real time application). Additionally, event times 
 * can also be defined by specifying a {@link Schedule} that has to be executed.<br> 
 * To receive trigger events, implement a {@link TimeTriggerListener} in your own
 * class and pass your listener to the constructor of this. class.
 * 
 * @see TimeTriggerListener
 * @see Schedule
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class TimeTrigger {

	private Thread ttThread;
	private TimeTriggerListener ttListener;
	
	private boolean isSimulation; 
	private TimeModelContinuous timeModel;
	
	private Long nextEventTime;
	private List<Long> eventTimes;
	private int eventTimeIndex = 0;
	
	private Schedule eventSchedule;
	private List<Long> eventScheduleTimeList;
	private long eventScheduleEndTime;
	
	private long millisecondsToFireAhead;
	private Long millisecondsToStopTrigger;
	
	private Boolean pausedTimeTrigger = false;
	private boolean stopTimeTrigger = false;
	
	
	/**
	 * Instantiates a new time trigger that will use the specified single time as next event time.
	 *
	 * @param isSimulation set true if this TimeTrigger is used within a simulation with a {@link TimeModelContinuous}
	 * @param timeModel the continuous time model that is currently used. In case that the trigger is not used in simulations this can be <code>null</code>.
	 * @param nextEventTime the next event time for the TimeTrigger
	 * @param ttListener the TimeTrigger listener
	 */
	public TimeTrigger(boolean isSimulation, TimeModelContinuous timeModel, long nextEventTime, TimeTriggerListener ttListener) {
		this.isSimulation = isSimulation;
		this.timeModel = timeModel;
		this.nextEventTime = nextEventTime;
		this.ttListener = ttListener;
		this.initialize();
	}
	/**
	 * Instantiates a new time trigger that will use the specified event times.
	 *
	 * @param isSimulation set true if this TimeTrigger is used within a simulation with a {@link TimeModelContinuous}
	 * @param timeModel the continuous time model that is currently used. In case that the trigger is not used in simulations this can be <code>null</code>.
	 * @param eventTimes the {@link List} of event times for the trigger
 	 * @param ttListener the TimeTrigger listener
	 */
	public TimeTrigger(boolean isSimulation, TimeModelContinuous timeModel, List<Long> eventTimes, TimeTriggerListener ttListener) {
		this.isSimulation = isSimulation;
		this.timeModel = timeModel;
		this.eventTimes = eventTimes;
		this.ttListener = ttListener;
		this.initialize();
	}
	/**
	 * Instantiates a new time trigger that will extract the event times from the specified {@link Schedule}.
	 *
	 * @param isSimulation set true if this TimeTrigger is used within a simulation with a {@link TimeModelContinuous}
	 * @param timeModel the continuous time model that is currently used. In case that the trigger is not used in simulations this can be <code>null</code>.
	 * @param schedule the {@link Schedule} that is to be executed
 	 * @param ttListener the TimeTrigger listener
	 */
	public TimeTrigger(boolean isSimulation, TimeModelContinuous timeModel, Schedule schedule, TimeTriggerListener ttListener) {
		this.isSimulation = isSimulation;
		this.timeModel = timeModel;
		this.eventSchedule = schedule;
		this.ttListener = ttListener;
		this.initialize();
	}
	/**
	 * Gets the event times out of the specified {@link Schedule}.
	 * @param schedule the schedule
	 * @return the schedule times
	 */
	private List<Long> getEventScheduleTimeList() {
		if (eventScheduleTimeList==null) {
			// --- Create the event time list -------------
			eventScheduleTimeList = this.createScheduleTimeList();
			eventScheduleEndTime = this.getEventListEndTime(eventScheduleTimeList);

		} else {
			// --- Has the end time changed ---------------
			TechnicalSystemStateEvaluation tsseEnd = null;
			if (this.eventSchedule.getTechnicalSystemStateEvaluation()!=null & this.eventSchedule.getTechnicalSystemStateList().size()==0) {
				tsseEnd = this.eventSchedule.getTechnicalSystemStateEvaluation();
			} else {
				tsseEnd = this.eventSchedule.getTechnicalSystemStateList().get(0);
			}
			if (tsseEnd.getGlobalTime()!=eventScheduleEndTime) {
				// --- Reload the event time list ---------
				eventScheduleTimeList = this.createScheduleTimeList();
				eventScheduleEndTime = this.getEventListEndTime(eventScheduleTimeList);
			}
		
		}
		return eventScheduleTimeList;
	}
	
	/**
	 * Return the end time out of the list of times .
	 *
	 * @param eventScheduleTimeList the event schedule time list
	 * @return the event schedule end time
	 */
	private long getEventListEndTime(List<Long> eventScheduleTimeList) {
		if (eventScheduleTimeList.size()==0) return -1;
		return eventScheduleTimeList.get(eventScheduleTimeList.size()-1); 
	}
	
	/**
	 * Creates a new schedule time list in an ascending order.
	 * @return the list of event times derived from the local event Schedule
	 */
	private List<Long> createScheduleTimeList() {
		
		List<Long> scheduleTimeList = new ArrayList<>();
		
		Schedule schedule = this.eventSchedule;
		if (schedule.getTechnicalSystemStateEvaluation()!=null & schedule.getTechnicalSystemStateList().size()==0) {
			// --- System states in parent-child form ---------------
			TechnicalSystemStateEvaluation tsse = schedule.getTechnicalSystemStateEvaluation();
			scheduleTimeList.add(tsse.getGlobalTime()-tsse.getStateTime());
			while (tsse.getParent()!=null) {
				tsse = tsse.getParent();
				scheduleTimeList.add(tsse.getGlobalTime()-tsse.getStateTime());
			}
			
		} else if (schedule.getTechnicalSystemStateList().size()!=0) {
			// --- System states in list form -----------------------
			for (int i = 0; i < schedule.getTechnicalSystemStateList().size(); i++) {
				TechnicalSystemStateEvaluation tsse = schedule.getTechnicalSystemStateList().get(i);
				scheduleTimeList.add(tsse.getGlobalTime()-tsse.getStateTime());
			}
		}
		
		// --- Sort the result -------------------------------------- 
		Collections.sort(scheduleTimeList);
		
		return scheduleTimeList;
	}
	
	/**
	 * Initialize this class.
	 */
	private void initialize() {
		// --- Sort the event times (just to be save with that) ---------------
		if (this.eventTimes!=null) {
			Collections.sort(this.eventTimes);
		} else if (this.eventSchedule!=null) {
			this.getEventScheduleTimeList();
		}
	}
	
	
	/**
	 * Executes the {@link TimeTrigger} in new thread.
	 */
	public void executeInNewThread(String threadName) {
		
		Runnable ttRunnable = new Runnable() {
			@Override
			public void run() {
				scheduleEvents();
			}
		};
		// --- Execute in a new Thread ----------
		this.ttThread = new Thread(ttRunnable, threadName);
		this.ttThread.start();
	}
	/**
	 * Executes the TimeTrigger within the current thread. Be aware that this 
	 * Method will not return until the TimeTrigger is finalized. 
	 */
	public void executeInCurrentThread() {
		this.ttThread = Thread.currentThread();
		this.scheduleEvents();
	}
	
	/**
	 * Stop the current TimeTrigger.
	 */
	public void stopTimeTrigger() {
		this.stopTimeTrigger = true;
		if (this.ttThread!=null & this.ttThread.isAlive()) {
			this.ttThread.interrupt();
			this.ttThread = null;
		} else {
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Schedules the events that were specified by the list of event times through the constructor.
	 */
	private void scheduleEvents() {
		
		Long timeForNextEvent = this.getNextEventTime(this.getTime());
		while (timeForNextEvent!=null) {

			// --- If a maximum time is defined, check that ---------
			if (this.getMillisecondsToStopTimeTrigger()!=null & this.getMillisecondsToStopTimeTrigger()<timeForNextEvent) {
				timeForNextEvent = this.getMillisecondsToStopTimeTrigger();
				this.stopTimeTrigger = true;
			}

			// --- Determine the time to sleep for the thread ------- 
			long timeToSleep = this.getTimeToSleep(timeForNextEvent);
			if (timeToSleep>0) {
				try {
					Thread.sleep(timeToSleep);
				} catch (IllegalArgumentException iaEx) {
					iaEx.printStackTrace();
				} catch (InterruptedException inEx) {
					// inEx.printStackTrace();
				}	
			}

			// --- Stop the TimeTrigger? ----------------------------
			if (this.stopTimeTrigger==true) break;

			// --- Pause this TimeTrigger? --------------------------
			if (this.pausedTimeTrigger==true) {
				this.doPause();
				
			} else {
				// --- Trigger the listener -------------------------
				if (this.ttListener!=null) {
					// --- Get monitor values for the trigger -------
					//long beforeEvent = this.getTime();
					this.setNextEventTime(this.ttListener.fireTimeTrigger(timeForNextEvent));
					//long afterEvent = this.getTime();
					//beforeEvent = Math.abs(timeForNextEvent-beforeEvent);
					//afterEvent = Math.abs(timeForNextEvent-afterEvent);
					//System.out.println( "=> Time difference between event time and simulated or real time: before fire event: " + beforeEvent + " ms - after fire event: " + afterEvent + " ms");
				}
			}
			// --- Get the next event time --------------------------
			timeForNextEvent = this.getNextEventTime(this.getTime());
		} // --- end while ---
		
		// --- Inform that this TimeTrigger is finalized ------------
		if (this.ttListener!=null) {
			this.ttListener.setTimeTriggerFinalized();	
		}
	}
	
	/**
	 * Returns, based on the time for the next event, the time to sleep .
	 *
	 * @param timeForNextEvent the time for next event
	 * @return the time to sleep
	 */
	public long getTimeToSleep(long timeForNextEvent) {
		long timeToSleep = 0;
		if (this.timeModel!=null & this.timeModel.getAccelerationFactor()!=1.0) {
			// --- Consider the acceleration of the time model --
			timeToSleep = ((long) (((double) (timeForNextEvent - this.getTime())) / this.timeModel.getAccelerationFactor())) - this.getMillisecondsToFireAhead();
		} else {
			timeToSleep = timeForNextEvent - this.getTime() - this.getMillisecondsToFireAhead();
		}
		return timeToSleep;
	}
	
	/**
	 * Returns the time for the next event .
	 *
	 * @param currentTime the current time
	 * @return the next event time
	 */
	private Long getNextEventTime(long currentTime) {
		
		Long newEventTime = null;
		if (this.nextEventTime!=null) {
			// ----------------------------------------------------------------
			// --- Get the single configured next event time ------------------
			// ----------------------------------------------------------------
			newEventTime = this.nextEventTime;
			
		} else if (this.eventSchedule!=null) {
			// ----------------------------------------------------------------
			// --- Get the Schedule event times -------------------------------
			// ----------------------------------------------------------------
			List<Long> eventScheduleTimeList = this.getEventScheduleTimeList();
			if (eventScheduleTimeList.size()>0) {
				// --- Search the event time list for the next event time -----
				long millisToFireAhead = this.getMillisecondsToFireAhead();
				
				// --- Ensure to start from right index if Schedule changed ---
				while (this.eventTimeIndex!=0 && eventScheduleTimeList.get(this.eventTimeIndex)>=currentTime) {
					this.eventTimeIndex --;
				}
				// --- Search the event time list for the next event time -----
				for (int i=this.eventTimeIndex; i<eventScheduleTimeList.size(); i++) {
					if ((eventScheduleTimeList.get(i)-millisToFireAhead)>=currentTime) {
						newEventTime = eventScheduleTimeList.get(i);
						this.eventTimeIndex = i;
						break;
					}
				}
			}
			
		} else {
			// ----------------------------------------------------------------
			// --- Get the next event time from the local time list -----------
			// ----------------------------------------------------------------
			if (this.eventTimes!=null && this.eventTimes.size()>0) {
				// --- Search the event time list for the next event time -----
				long millisToFireAhead = this.getMillisecondsToFireAhead();
				for (int i=this.eventTimeIndex; i<this.eventTimes.size(); i++) {
					if ((this.eventTimes.get(i)-millisToFireAhead)>=currentTime) {
						newEventTime = this.eventTimes.get(i);
						this.eventTimeIndex = i;
						break;
					}
				}
			}

		}
		return newEventTime;
	}
	/**
	 * Manually sets the next event time. Once you have used this method, a new event time
	 * has to be set after every time trigger event at the {@link TimeTriggerListener}.
	 * You can switch back by specifying <code>null</code> as new value.
	 * 
	 * @see TimeTriggerListener
	 * @see TimeTriggerListener#fireTimeTrigger(long)
	 * @param newNextEventTime the new next event time or <code>null</code>
	 */
	public void setNextEventTime(Long newNextEventTime) {
		this.nextEventTime = newNextEventTime;
	}

	
	/**
	 * Returns the current time, depending on the current settings.
	 * @return the current time
	 */
	private Long getTime() {
		Long time = null;
		if (this.isSimulation==false) {
			time = System.currentTimeMillis();
		} else {
			time = this.timeModel.getTime();
		}
		return time;
	}
	

	/**
	 * Checks if this TimeTrigger is paused.
	 * @return true, if this time trigger is paused 
	 */
	public boolean isPausedTimeTrigger() {
		return pausedTimeTrigger;
	}
	/**
	 * Pauses this TimeTrigger.
	 */
	public void pause() {
		this.pausedTimeTrigger = true;
		if (this.ttThread.getState()==State.TIMED_WAITING) {
			this.ttThread.interrupt();
		}
	}
	/**
	 * Do pause.
	 */
	private void doPause() {
		try {
			synchronized (this.pausedTimeTrigger) {
				this.pausedTimeTrigger.wait();
			}
		} catch (InterruptedException ie) { }
	}
	/**
	 * Resumes this TimeTrigger, if paused.
	 */
	public void resume() {
		synchronized (this.pausedTimeTrigger) {
			this.pausedTimeTrigger.notify();
			this.pausedTimeTrigger = false;
		}
	}
	
	
	/**
	 * Returns the milliseconds that are to be used to fire ahead of a specified  event.
	 * @return the milliseconds to fire ahead of a specified event
	 */
	public long getMillisecondsToFireAhead() {
		return millisecondsToFireAhead;
	}
	/**
	 * Sets the milliseconds to fire ahead of a specified event.
	 * @param millisecondsToFireAhead the new milliseconds to fire ahead
	 */
	public void setMillisecondsToFireAhead(long millisecondsToFireAhead) {
		this.millisecondsToFireAhead = millisecondsToFireAhead;
	}
	
	/**
	 * Returns the milliseconds to stop time trigger.
	 * @return the milliseconds to stop time trigger
	 */
	public Long getMillisecondsToStopTimeTrigger() {
		return millisecondsToStopTrigger;
	}
	/**
	 * Sets the milliseconds to stop time trigger.
	 * @param millisecondsToStopTrigger the new milliseconds to stop time trigger
	 */
	public void setMillisecondsToStopTimeTrigger(Long millisecondsToStopTrigger) {
		this.millisecondsToStopTrigger = millisecondsToStopTrigger;
	}
	
}
