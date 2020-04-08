/*
 * 
 */
package de.enflexit.energyAgent.core.monitoring;

import java.io.File;
import java.util.ArrayList;

import de.enflexit.energyAgent.core.AbstractEnergyAgent;
import de.enflexit.energyAgent.core.AbstractInternalDataModel.LoggingMode;
import de.enflexit.energyAgent.core.globalDataModel.DirectoryHelper.DirectoryType;
import energy.FixedVariableList;
import energy.OptionModelController;
import energy.evaluation.TechnicalSystemStateHelper;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * {@link MonitoringListener} implementation for logging purposes
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class MonitoringListenerForLogging extends Thread implements MonitoringListener {

	/** The enumeration for possible Logging destinations. */
	public enum LoggingDestination {
		Console,
		LogFile,
		EomDatabase
	}
	
	public static final String SYSTEM_MONITORING_FILE_NAME_SUFFIX = "_SystemMonitoring.bin";
	
	private AbstractEnergyAgent myEnergyAgent;
	private LoggingDestination loggingDestination;
	private MonitoringBehaviourRT rtMonitoring;
	private ScheduleList scheduleList;
	
	private boolean isDoShutdown;

	private static final int MAX_CACHE_LENGTH = 500;
	private ArrayList<TechnicalSystemStateEvaluation> tsseCache;	
	
	private MonitoringDatabaseHandler monitoringDatabaseHandler;
	
	private DayFile logFile;
	private MonitoringFileHandler monitoringFileHandler;
	
	private boolean debug = false;
	
	
	/**
	 * Instantiates a new monitoring listener for logging with the 
	 * default {@link LoggingDestination#EomDatabase}.
	 * @param myEnergyAgent the energy agent
	 */
	public MonitoringListenerForLogging(AbstractEnergyAgent energyAgent) {
		this(energyAgent, null);
	}
	/**
	 * Instantiates a new monitoring listener for logging.
	 *
	 * @param myEnergyAgent the energy agent
	 * @param loggingDestination the logging destination
	 */
	public MonitoringListenerForLogging(AbstractEnergyAgent energyAgent, LoggingDestination loggingDestination) {
		this.myEnergyAgent = energyAgent;
		this.setLoggingDestination(loggingDestination);
		this.setName(this.myEnergyAgent.getAID().getLocalName() + "#SystemStateLogger");
	}
	
	/**
	 * Returns the target logging destination.
	 * @return the logging destination
	 */
	public LoggingDestination getLoggingDestination() {
		if (loggingDestination==null) {
			loggingDestination = LoggingDestination.EomDatabase;
		}
		return loggingDestination;
	}
	/**
	 * Sets the target logging destination.
	 * @param loggingDestination the new logging destination
	 */
	public void setLoggingDestination(LoggingDestination loggingDestination) {
		if (loggingDestination!=null) {
			this.loggingDestination = loggingDestination;
		}
	}

	/**
	 * Returns the current MonitoringBehaviourRT.
	 * @return the monitoring behaviour RT
	 */
	private MonitoringBehaviourRT getMonitoringBehaviourRT() {
		return rtMonitoring;
	}
	/**
	 * Sets the MonitoringBehaviourRT
	 * @param rtMonitoring the new monitoring behaviour RT
	 */
	private void setMonitoringBehaviourRT(MonitoringBehaviourRT rtMonitoring) {
		this.rtMonitoring = rtMonitoring;
	}
	
	/**
	 * Gets the logging mode.
	 * @return the logging mode
	 */
	private LoggingMode getLoggingMode() {
		return this.myEnergyAgent.getInternalDataModel().getLoggingMode();
	}
	/**
	 * Gets the io value filter for logging.
	 * @return the io value filter for logging
	 */
	private IOListFilterForLogging getLoggingFilter() {
		return this.myEnergyAgent.getInternalDataModel().getLoggingFilter();
	}
	/**
	 * Returns the ScheduleList of the current MonitoringBehaviourRT.
	 * @return the schedule list
	 */
	private ScheduleList getScheduleList() {
		return scheduleList;
	}
	/**
	 * Sets the schedule list.
	 * @param scheduleList the new schedule list
	 */
	public void setScheduleList(ScheduleList scheduleList) {
		this.scheduleList = scheduleList;
	}
	
	/**
	 * Returns the system state cache.
	 * @return the state cache
	 */
	public ArrayList<TechnicalSystemStateEvaluation> getSystemStateCache() {
		if (tsseCache==null) {
			tsseCache = new ArrayList<>();
		}
		return tsseCache;
	}
	
	/**
	 * Shuts down the current logging thread.
	 */
	public void doShutdown() {
		this.isDoShutdown = true;
		synchronized (this.getSystemStateCache()) {
			this.getSystemStateCache().notify();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.monitoring.MonitoringListener#onMonitoringEvent(de.enflexit.energyAgent.core.monitoring.MonitoringEvent)
	 */
	@Override
	public void onMonitoringEvent(MonitoringEvent monitoringEvent) {
		
		// --- Check if the MonitoringBehaviourRT was already set ---
		if (this.getMonitoringBehaviourRT()==null && monitoringEvent.getSource() instanceof MonitoringBehaviourRT) {
			this.setMonitoringBehaviourRT((MonitoringBehaviourRT) monitoringEvent.getSource());
		}
		
		// --- Decouple agent thread from log writer ----------------
		TechnicalSystemStateEvaluation tsse = monitoringEvent.getTSSE();
		
		if (tsse!=null) {
			
			if (this.debug==true) {
				System.out.println(myEnergyAgent.getLocalName() + " - MonitoringListener received new TSSE");
				System.out.println("- GlobalTime: " + tsse.getGlobalTime());
				System.out.println("- Values: ");
				System.out.println(TechnicalSystemStateHelper.toString(tsse.getIOlist()));
			}
			
			// --- If value-based logging is configured, check if the TSSE should be logged -------
			if (this.getLoggingMode()==LoggingMode.ON_VALUE_CHANGE) {
				FixedVariableList ioList = new FixedVariableList();
				ioList.addAll(tsse.getIOlist());
				if (this.getLoggingFilter().hasChangedSignificantly(ioList, tsse.getGlobalTime())==false) {
					// --- No significant change in the IO, skip this TSSE ------------------------
					if (this.debug==true) {
						System.out.println("=> No significant change, ignoring");
					}
					return;
				} else {
					if (this.debug==true) {
						System.out.println("=> Values changed or maximum time exceeded, storing");
					}
				}
			}
			synchronized (this.getSystemStateCache()) {
				this.getSystemStateCache().add(tsse);
				this.getSystemStateCache().notify();
			}
		}
	}
		
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		while (true) {
			
			// --- Sleep until notification -------------------------
			try {
				synchronized (this.getSystemStateCache()) { 
					this.getSystemStateCache().wait();
				}
				
			} catch (IllegalMonitorStateException imse) {
				// imse.printStackTrace();
			} catch (InterruptedException ie) {
				// ie.printStackTrace();
			}

			// --- Determine the current ScheduleList? --------------
			if (this.getScheduleList()==null && this.getMonitoringBehaviourRT()!=null) {
				OptionModelController omc = this.getMonitoringBehaviourRT().getOptionModelController();
				ScheduleList sc = omc.getEvaluationProcess().getScheduleController().getScheduleList();
				this.setScheduleList(sc);
			}
			
			// --- Prevent concurrent exceptions --------------------
			ArrayList<TechnicalSystemStateEvaluation> workList = new ArrayList<>(this.getSystemStateCache());
			for (int i = 0; i < workList.size(); i++) {
				
				try {
					// --- Get system state -------------------------
					TechnicalSystemStateEvaluation tsse = workList.get(i);
					
					boolean saved = false;
					// --- Write to logging destination -------------
					switch (this.getLoggingDestination()) {
					case EomDatabase:
						saved = this.writeToDatabase(tsse);
						break;
					case LogFile:
						saved = this.writeToFile(tsse);
						break;
					case Console:
						saved = this.writeToConsole(tsse, false);
						break;
					}

					if (this.debug==true) {
						System.out.println(myEnergyAgent.getLocalName() + " wrote a new TSSE to " + this.getLoggingDestination() + " - successful: " + saved);
					}
					
					// --- Remove state from the cache --------------
					if (saved==true) {
						this.getSystemStateCache().remove(tsse);
					} else {
						// --- Restrict length of the cache ---------
						while (this.getSystemStateCache().size()>MonitoringListenerForLogging.MAX_CACHE_LENGTH) {
							TechnicalSystemStateEvaluation tsseRemoved = this.getSystemStateCache().remove(0);
							this.writeToConsole(tsseRemoved, true);
						}
						// --- Exit, since something went wrong -----
						break;
					}
				
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			if (this.isDoShutdown==true) break;
			
		} // --- end while --- 
	}

	// ----------------------------------------------------------------------------------
	// --- From here, logging to EOM database -------------------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Writes the specified TechnicalSystemStateEvaluation to the EOM database.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation to log
	 * @return true, if successful
	 */
	private boolean writeToDatabase(TechnicalSystemStateEvaluation tsse) {
		
		boolean successful = false;
		MonitoringDatabaseHandler dbHandler = this.getMonitoringDatabaseHandler();
		if (dbHandler!=null) {
			
			if (dbHandler.hasConnectionErrors()==true) {
				// --- Switch to log file writing -------------------
				System.err.println("[" + this.getClass().getSimpleName() + "] Found indication for database connection error - switch to file writing for the system state logging." );
				this.setLoggingDestination(LoggingDestination.LogFile);
				// --- Store system state to file ------------------- 
				successful = this.writeToFile(tsse);
				
			} else {
				// --- Write to database ----------------------------
				successful = dbHandler.writeTechnicalSystemState(tsse);	
			}
		}
		return successful;
	}
	
	/**
	 * Returns the current {@link MonitoringFileHandler}.
	 * @return the monitoring file handler
	 */
	private MonitoringDatabaseHandler getMonitoringDatabaseHandler() {
		if (monitoringDatabaseHandler==null && this.getScheduleList()!=null) {
			monitoringDatabaseHandler = new MonitoringDatabaseHandler(this.getScheduleList());
		}
		return monitoringDatabaseHandler;
	}
	// ----------------------------------------------------------------------------------
	// --- From here, logging to file ---------------------------------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Writes the specified TechnicalSystemStateEvaluation to the agent log file.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation to log
	 * @return true, if successful
	 */
	private boolean writeToFile(TechnicalSystemStateEvaluation tsse) {
		
		boolean successful = false;

		// --- Get current log file ---------------------------------
		DayFile dayLogFile = this.getLoggingFile();
		if (dayLogFile==null) {
			dayLogFile = createLoggingDayFileInstance(tsse.getGlobalTime());
		}
		// --- Still the same log file? -----------------------------
		if (tsse.getGlobalTime()>=dayLogFile.getTimeStampValidTo()) {
			// --- Create a new log file instance -------------------
			dayLogFile = createLoggingDayFileInstance(tsse.getGlobalTime());
		}

		// --- Save the TSSE in the specified file ------------------
		successful = this.getMonitoringFileHandler().writeObject(tsse);
		
		return successful;
	}
	
	/**
	 * Returns the current {@link MonitoringFileHandler}.
	 * @return the monitoring file handler
	 */
	private MonitoringFileHandler getMonitoringFileHandler() {
		if (monitoringFileHandler==null) {
			monitoringFileHandler = new MonitoringFileHandler(this.getLoggingFile());
		}
		return monitoringFileHandler;
	}
	
	/**
	 * Returns the logging file.
	 * @return the logging file
	 */
	private DayFile getLoggingFile() {
		return logFile;
	}
	/**
	 * Sets the logging file.
	 * @param newLogFile the new logging file
	 */
	private void setLoggingFile(DayFile newLogFile) {
		this.logFile = newLogFile;
	}
	/**
	 * Creates a new logging file based on the specified timestamp .
	 * @param timestamp the time stamp
	 * @return the day file
	 */
	private DayFile createLoggingDayFileInstance(long timestamp) {
		File logDir = this.myEnergyAgent.getInternalDataModel().getFileOrDirectory(DirectoryType.LoggingDirectory);
		DayFile newLogFile = DayFile.createDayFile(logDir, SYSTEM_MONITORING_FILE_NAME_SUFFIX, timestamp);
		this.setLoggingFile(newLogFile);
		return newLogFile;
	}
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, logging to console ------------------------------------------------
	// ----------------------------------------------------------------------------------

	/**
	 * Writes the specified TechnicalSystemStateEvaluation to console.
	 *
	 * @param tsse the TechnicalSystemStateEvaluation to log
	 * @param isRemovedFromCache the indicator if the state was removed from cache
	 * @return true, if successful
	 */
	private boolean writeToConsole(TechnicalSystemStateEvaluation tsse, boolean isRemovedFromCache) {
		String output = TechnicalSystemStateHelper.toString(tsse, false);
		if (isRemovedFromCache==true) {
			System.err.println("[Removed from system state chache] " + output);
		} else {
			System.out.println(output);
		}
		return true;
	}
	
}
