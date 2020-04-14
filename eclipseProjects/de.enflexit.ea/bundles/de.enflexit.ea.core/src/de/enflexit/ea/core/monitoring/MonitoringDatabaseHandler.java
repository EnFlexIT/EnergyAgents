package de.enflexit.ea.core.monitoring;

import java.util.List;

import de.enflexit.db.hibernate.SessionFactoryMonitor;
import de.enflexit.db.hibernate.SessionFactoryMonitor.SessionFactoryState;
import de.enflexit.eom.database.DatabaseBundleInfo;
import de.enflexit.eom.database.DatabaseStorageHandler_ScheduleList;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class MonitoringDatabaseHandler can be used to continuously write system states 
 * into the EOM database structure.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MonitoringDatabaseHandler {

	private DatabaseStorageHandler_ScheduleList dbHandlerScheduleList;
	
	private ScheduleList scheduleList;
	private Integer idScheduleList;
	private Integer idSchedule;
	
	/**
	 * Instantiates a new monitoring database handler.
	 * @param scheduleList the schedule list to which the system states belog to
	 */
	public MonitoringDatabaseHandler(ScheduleList scheduleList) {
		if (scheduleList==null) {
			throw new NullPointerException("The ScheduleList is not allowed to be null for the " + this.getClass().getSimpleName());
		}
		this.scheduleList = scheduleList;
	}
	
	
	
	/**
	 * Returns the DB handler for {@link ScheduleList}s.
	 * @return the db handler schedule list
	 */
	private DatabaseStorageHandler_ScheduleList getDatabaseStorageHandlerForScheduleList() {
		if (dbHandlerScheduleList==null) {
			dbHandlerScheduleList = new DatabaseStorageHandler_ScheduleList();
		}
		return dbHandlerScheduleList;
	}
	/**
	 * Returns the the SessionFactoryMonitor that provides information 
	 * about the state of the hibernate SessionFactory.
	 * @return the session factory monitor
	 */
	public static synchronized SessionFactoryMonitor getSessionFactoryMonitor() {
		return DatabaseBundleInfo.getSessionFactoryMonitor();
	}
	/**
	 * Gets the session factory state.
	 * @return the session factory state
	 */
	private SessionFactoryState getSessionFactoryState() {
		SessionFactoryMonitor sessMonitor = getSessionFactoryMonitor();
		if (sessMonitor!=null) {
			return sessMonitor.getSessionFactoryState();
		}
		return null;
	}
	
	/**
	 * Indicates, if database actions can be executed depending of the state of the session factory.
	 * @return true, if database actions can be done
	 */
	private boolean isDoDatabaseActions() {
		boolean doDatabaseAction = false;
		SessionFactoryState dbState = this.getSessionFactoryState();
		if (dbState!=null && dbState==SessionFactoryState.Created) {
			doDatabaseAction = true;
		}
		return doDatabaseAction;
	}
	/**
	 * Checks for connection errors that indicate that a database connection can not be 
	 * established. Thus, the database logging should be avoided or skipped.
	 * @return true, if successful
	 */
	public boolean hasConnectionErrors() {
		boolean hasConnectionErrors = false;
		switch (this.getSessionFactoryState()) {
		case CheckDBConectionFailed:
		case InitializationProcessFailed:
			hasConnectionErrors = true;
			break;

		default:
			break;
		}
		return hasConnectionErrors;
	}
	
	/**
	 * Gets the database ID of the current ScheduleList.
	 * @return the ScheduleList-ID
	 */
	private Integer getIdScheduleList() {
		if (this.idScheduleList==null) {
			
			// --- Check the state of the hibernate session factory -----------
			if (isDoDatabaseActions()==false) return null;
			
			// --- Get the database ID for this schedule list  ------------
			List<Integer> idList = this.getDatabaseStorageHandlerForScheduleList().getScheduleListIDs(this.scheduleList.getNetworkID(), this.scheduleList.getSystemID(), this.scheduleList.getSetup());
			if (idList!=null && idList.size()>0) {
				// --- Check for number of ScheduleList's -----------------
				if (idList.size()==1) {
					// --- Everything is fine -----------------------------
					this.idScheduleList = idList.get(0);
					
				} else {
					// --- Multiple results were found --------------------
					System.err.println("[" + this.getClass().getSimpleName() + "] Multiple ScheduleLists were found with the identifiers systemID='" + this.scheduleList.getSystemID() + "' and networkID='" + this.scheduleList.getNetworkID() + "'");
				}
				
			} else {
				// --- ScheduleList was not save yet => Do that now -------
				if (this.getDatabaseStorageHandlerForScheduleList().saveScheduleList(null, this.scheduleList, null, true, false, false)==true) {
					this.idScheduleList = this.scheduleList.getIdScheduleList(); 
				}
			}
			
		}
		return this.idScheduleList;
	}
	
	/**
	 * Returns the id schedule.
	 * @return the id schedule
	 */
	private Integer getIdSchedule() {
		if (idSchedule==null && this.getIdScheduleList()!=null) {
			
			// --- Check the state of the hibernate session factory -----------
			if (isDoDatabaseActions()==false) return null;
			
			// --- Get the monitoring schedule id for this schedule list ------
			List<?> idList = this.getDatabaseStorageHandlerForScheduleList().getScheduleIDs(this.getIdScheduleList(), MonitoringStrategyRT.class.getName());
			if (idList!=null && idList.size()>0) {
				// --- Check for number of ScheduleList's ---------------------
				if (idList.size()==1) {
					// --- Everything is fine ---------------------------------
					this.idSchedule = (Integer) idList.get(0);
				} else {
					// --- Multiple results were found ------------------------
					System.err.println("[" + this.getClass().getSimpleName() + "] Multiple Schedules were found with the identifier systemID='" + this.scheduleList.getSystemID() + "' and networkID='" + this.scheduleList.getNetworkID() + "'");
				}
			}
		}
		return idSchedule;
	}
	
	/**
	 * Writes the technical system state to the EOM database.
	 *
	 * @param tsseToWrite the TechnicalSystemStateEvaluation to write
	 * @return true, if successful
	 */
	public boolean writeTechnicalSystemState(TechnicalSystemStateEvaluation tsseToWrite) {
	
		boolean success = false;
		
		// --- Write system state to database ---------------------------------
		Integer idSchedule = this.getIdSchedule();
		if (tsseToWrite!=null && idSchedule!=null) {
			success = this.getDatabaseStorageHandlerForScheduleList().saveTechnicalSystemStateEvaluation(idSchedule, tsseToWrite);
		}
		return success;
	}
	
	
}
