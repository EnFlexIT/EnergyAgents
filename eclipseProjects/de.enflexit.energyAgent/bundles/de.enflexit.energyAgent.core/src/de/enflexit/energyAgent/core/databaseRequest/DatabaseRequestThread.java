package de.enflexit.energyAgent.core.databaseRequest;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import de.enflexit.eom.database.DatabaseBundleInfo;
import de.enflexit.eom.database.DatabaseStorageHandler_ScheduleList;
import de.enflexit.eom.database.ScheduleListSelection;
import de.enflexit.eom.database.ScheduleListSelection.SystemStateRangeType;
import energy.optionModel.Schedule;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystemStateEvaluation;
import energy.persistence.ScheduleList_StorageHandler;
import energy.schedule.ScheduleListResetDatabaseIDAction;
import hygrid.ops.ontology.FieldDataReply;
import hygrid.ops.ontology.FieldDataRequest;
import hygrid.ops.ontology.LongValue;
import hygrid.ops.ontology.ScheduleRangeDefinition;

/**
 * This thread handles database requests initiated by the energy agents.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DatabaseRequestThread extends Thread {
	
	private static final int MAX_TSSES_PER_MESSAGE = 250;
	
	private List<FieldDataRequest> requestQueue;
	private DatabaseRequestInterface myInterface;
	
	private DatabaseStorageHandler_ScheduleList dbStorageHandler;
	
	private boolean debug = true;
	
	/**
	 * Instantiates a new database request thread.
	 */
	public DatabaseRequestThread(DatabaseRequestInterface myInterface) {
		this.myInterface = myInterface;
		this.setName(this.getClass().getSimpleName());
	}
	
	/**
	 * Gets the request queue, initializes it if necessary
	 * @return the request queue
	 */
	private List<FieldDataRequest> getRequestQueue(){
		if (requestQueue==null) {
			requestQueue = new ArrayList<>();
		}
		return requestQueue;
	}
	

	/**
	 * Gets the database storage handler.
	 * @return the database storage handler
	 */
	private DatabaseStorageHandler_ScheduleList getDbStorageHandler() {
		if (dbStorageHandler==null) {
			dbStorageHandler = new DatabaseStorageHandler_ScheduleList();
		}
		return dbStorageHandler;
	}

	/**
	 * Adds a request to the request queue.
	 * @param request the request
	 */
	protected void addRequestToQueue(FieldDataRequest request) {
		synchronized (this.getRequestQueue()) {
			this.getRequestQueue().add(request);
			this.getRequestQueue().notify();
			if (this.debug==true) {
				System.out.println("[" + this.getClass().getSimpleName() + "] New field data request for agent " + request.getAgentIDs().get(0));
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			
			// --- Check queue for requests, wait if empty ----------
			synchronized (this.getRequestQueue()) {
				if (this.getRequestQueue().size()==0) {
					try {
						this.getRequestQueue().wait();
					} catch (InterruptedException e) {
						System.err.println("[" + this.getClass().getSimpleName() + "] Waiting interrupted...");
						e.printStackTrace();
					}
				}
			}
			
			// --- Process the first request from the queue ---------
			FieldDataRequest request = this.getRequestQueue().get(0);
			this.processSingleRequest(request);
			this.getRequestQueue().remove(request);
			
		}
	}
	
	/**
	 * Process a single field data request.
	 * @param request the request
	 */
	private void processSingleRequest(FieldDataRequest request) {
		String agentID = (String) request.getAgentIDs().get(0);
		ScheduleListSelection scheduleListSelection = this.convertToScheduleListSelection(request.getScheduleRangeDefinition());

		// --- Determine the number of TSSEs this request will deliver --------
		int totalTSSEs = this.getNumberOfTSSEsForQuery(this.getScheduleID(agentID), scheduleListSelection);

		if (this.debug==true) {
			System.out.println("[" + this.getClass().getSimpleName() + "] The requested schedule for agent " + agentID + " contains " + totalTSSEs + " TSSEs");
		}
		
		if (totalTSSEs==0) {
			// --- No TSSEs found for the requested period --------------------
			FieldDataReply reply = new FieldDataReply();
			reply.setAgentID(agentID);
			reply.setScheduleListXML(null);
			reply.setMoreComming(false);
			this.myInterface.sendReplyToAgent(reply);
			return;
		}
		
		// --- Determine the start time for the first request -----------------
		long startTimeForNextRequest;
		if (scheduleListSelection.getSystemStateRangeType()==SystemStateRangeType.AllStates) {
			startTimeForNextRequest = 0;
		} else {
			startTimeForNextRequest = scheduleListSelection.getTimeRangeFrom();
		}
		
		// --- Get and send schedule parts until the request is fulfilled -----
		int sentTSSEs = 0;
		int numberOfQueries = 0;
		
		// --- Repeat until all TSSEs have been sent ----------------------
		while (sentTSSEs < totalTSSEs) {
			
			// --- Build a ScheduleListSelection for the next request ---------
			ScheduleListSelection slsForPart = new ScheduleListSelection();
			slsForPart.setSystemStateRangeType(SystemStateRangeType.StartDateAndNumber);
			slsForPart.setTimeRangeFrom(startTimeForNextRequest);
			int remainingTSSEs = totalTSSEs - sentTSSEs;
			slsForPart.setNumberOfStatesToLoad(Math.min(MAX_TSSES_PER_MESSAGE, remainingTSSEs));
			
			// --- Get the schedule list --------------------------------------
			ScheduleList scheduleList = this.getScheduleListFromDatabase(agentID, slsForPart);
			
			if (scheduleList!=null) {
				
				int tssesLoaded = this.getNumberOfTSSEsForScheduleList(scheduleList);
				sentTSSEs += tssesLoaded;

				numberOfQueries++;
				if (this.debug==true) {
					System.out.println("[" + this.getClass().getSimpleName() + "] Query no. " + numberOfQueries + " for agent " + agentID + " returned " + tssesLoaded + " TSSEs");
				}
				
				// --- Remove database IDs ------------------------------------
				ScheduleListResetDatabaseIDAction idErazor = new ScheduleListResetDatabaseIDAction(scheduleList);
				idErazor.resetIDs();
				
				// --- Convert to XML -----------------------------------------
				ScheduleList_StorageHandler slsh = new ScheduleList_StorageHandler();
				String scheduleListXML = slsh.getScheduleListAsXMLString(scheduleList); 
				
				// --- Create and send the reply ------------------------------
				FieldDataReply reply = new FieldDataReply();
				reply.setAgentID(agentID);
				reply.setTotalTSSEs(totalTSSEs);
				reply.setScheduleListXML(scheduleListXML);
				reply.setMoreComming(sentTSSEs<totalTSSEs);
				this.myInterface.sendReplyToAgent(reply);
				
				// --- Set start time for the next partial request ------------
				TechnicalSystemStateEvaluation lastTSSE = this.getLastTSSE(scheduleList);
				startTimeForNextRequest = lastTSSE.getGlobalTime() + 1;
				
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] Could not load schedule for " + agentID);
			}
		
		} // End while
		
		if (this.debug==true) {
			System.out.println("[" + this.getClass().getSimpleName() + "] Request for agent " + agentID + " completed with " + numberOfQueries + " queries");
		}
	}
	
	/**
	 * Convert an ontology-based ScheduleRangeDefinition to a ScheduleListSelection for hibernate queries.
	 * @param rangeDefinition the schedule range definition
	 * @return the schedule list selection
	 */
	private ScheduleListSelection convertToScheduleListSelection(ScheduleRangeDefinition rangeDefinition) {
		ScheduleListSelection sls = new ScheduleListSelection();
		
		if (rangeDefinition.getIncludeAllStates()==true) {
			sls.setSystemStateRangeType(SystemStateRangeType.AllStates);
		} else {
			
			LongValue timeStampFrom = rangeDefinition.getTimestampFrom();
			sls.setTimeRangeFrom(timeStampFrom.getLongValue());
			
			LongValue timeStampTo = rangeDefinition.getTimestampTo();
			if (timeStampTo!=null) {
				sls.setSystemStateRangeType(SystemStateRangeType.TimeRange);
				sls.setTimeRangeTo(timeStampTo.getLongValue());
			} else {
				sls.setSystemStateRangeType(SystemStateRangeType.StartDateAndNumber);
				sls.setNumberOfStatesToLoad(rangeDefinition.getNumberOfStates());
			}
		}
		
		return sls;
	}
	
	/**
	 * Gets the number of TSSEs for a schedule list (assuming it contains only one schedule).
	 * @param scheduleList the schedule list
	 * @return the number of TSSEs
	 */
	private int getNumberOfTSSEsForScheduleList(ScheduleList scheduleList) {
		int number = 0;
		Schedule schedule = scheduleList.getSchedules().get(0);
		if (schedule!=null) {
			if (schedule.getTechnicalSystemStateEvaluation()!=null) {
				TechnicalSystemStateEvaluation tsse = schedule.getTechnicalSystemStateEvaluation();
				while (tsse!=null) {
					number++;
					tsse = tsse.getParent();
				} 
			} else {
				number = schedule.getTechnicalSystemStateList().size();	
			}
		}
		return number;
	}
	
	/**
	 * Gets the last TSSE from a schedule list (assuming it contains only one schedule).
	 * @param scheduleList the schedule list
	 * @return the last TSSE
	 */
	private TechnicalSystemStateEvaluation getLastTSSE(ScheduleList scheduleList) {
		TechnicalSystemStateEvaluation lastTSSE = null;
		Schedule schedule = scheduleList.getSchedules().get(0);
		if (schedule!=null) {
			if (schedule.getTechnicalSystemStateEvaluation()!=null) {
				lastTSSE = schedule.getTechnicalSystemStateEvaluation();
			} else if (schedule.getTechnicalSystemStateList().isEmpty()==false){
				lastTSSE = schedule.getTechnicalSystemStateList().get(0);
			}
		}
		return lastTSSE;
	}
	
	/**
	 * Gets the number of TSSEs for a schedule query.
	 * @param scheduleID the schedule ID
	 * @param scheduleListSelection the schedule list selection
	 * @return the number of TSSEs
	 */
	private int getNumberOfTSSEsForQuery(int scheduleID, ScheduleListSelection scheduleListSelection) {
		int number = -1;

		// --- Build the HQL statement depending on the SystemStateRangeType --
		String hqlStatement = "select count(*) from TechnicalSystemStateEvaluation where idSchedule=:idSchedule";
		if (scheduleListSelection.getSystemStateRangeType()!=SystemStateRangeType.AllStates) {
			// --- Time range or start date and number ------------------------
			hqlStatement += " and globaltime>=:globalTimeFrom";
		} 
		if (scheduleListSelection.getSystemStateRangeType()==SystemStateRangeType.TimeRange) {
			// --- Time range -------------------------------------------------
			hqlStatement += " and globalTime<=:globalTimeTo";
		}

		// --- Create the query object, set required parameters ---------------
		Session session = DatabaseBundleInfo.getNewDatabaseSession();
		@SuppressWarnings("unchecked")
		Query<Long> query = session.createQuery(hqlStatement);
		query.setParameter("idSchedule", scheduleID);
		if (scheduleListSelection.getSystemStateRangeType()!=SystemStateRangeType.AllStates) {
			query.setParameter("globalTimeFrom", scheduleListSelection.getTimeRangeFrom());
		}
		if (scheduleListSelection.getSystemStateRangeType()==SystemStateRangeType.TimeRange) {
			query.setParameter("globalTimeTo", scheduleListSelection.getTimeRangeTo());
		}
		
		// --- Execute the query, get the  result ---------------------------- 
		List<Long> resultList = query.list();
		if (resultList!=null && resultList.size()==1) {
			number = resultList.get(0).intValue();
			
			if (scheduleListSelection.getSystemStateRangeType()==SystemStateRangeType.StartDateAndNumber) {
				// --- If the schedule contains less TSSEs than requested, use the lower number
				number = Math.min(number, scheduleListSelection.getNumberOfStatesToLoad());
			}
			
		} else {
			// --- Error cases, should never occur ----------------------------
			System.err.print("[" + this.getClass().getSimpleName() + "] Unexpected query result: ");
			if (resultList==null) {
				System.err.println("Result list was null!");
			} else {
				System.err.println("Result list contained " + resultList.size() + " results!");
			}
		}
		
		return number;
	}
	
	/**
	 * Gets the schedule list from database.
	 *
	 * @param agentID the agent ID
	 * @param scheduleListSelection the schedule list selection
	 * @return the schedule list from database
	 */
	private ScheduleList getScheduleListFromDatabase(String agentID, ScheduleListSelection scheduleListSelection) {
		ScheduleList scheduleList = null;
		
		int scheduleListID = this.getScheduleListID(agentID);
		if (scheduleListID>-1) {
			scheduleListSelection.setIdScheduleList(scheduleListID);
			scheduleList = this.getDbStorageHandler().loadScheduleListFromDatabase(scheduleListSelection);
		}
		
		return scheduleList;
	}
	
	/**
	 * Gets the schedule list ID for an agent.
	 * @param agentID the agent ID
	 * @return the schedule list ID
	 */
	private int getScheduleListID(String agentID) {
		int scheduleListID = -1;
		List<Integer> scheduleListIDs = this.getDbStorageHandler().getScheduleListIDs(agentID, null, null);
		if (scheduleListIDs.size()==1) {
			scheduleListID = scheduleListIDs.get(0);
		} else {
			if (scheduleListIDs.size()==0) {
				System.err.println("[" + this.getClass().getSimpleName() +"] No schedule list for agent " + agentID +  " was found in the database");
			} else if (scheduleListIDs.size()>1) {
				System.err.println("[" + this.getClass().getSimpleName() +"] More than one schedule list for agent " + agentID +  " were found in the database");
			}
		}
		return scheduleListID;
	}

	
	/**
	 * Gets the schedule ID for an agent.
	 * @param agentID the agent ID
	 * @return the schedule ID
	 */
	private int getScheduleID(String agentID) {
		int scheduleID = -1;
		
		int scheduleListID = this.getScheduleListID(agentID);
		if (scheduleListID>-1) {
			
			List<Integer> scheduleIDs = this.getDbStorageHandler().getScheduleIDs(scheduleListID, null);
			if (scheduleIDs.size()==1) {
				scheduleID = scheduleIDs.get(0);
			} else {
				if (scheduleIDs.size()==0) {
					System.err.println("[" + this.getClass().getSimpleName() +"] No schedule for agent " + agentID +  " was found in the database");
				} else if (scheduleIDs.size()>1) {
					System.err.println("[" + this.getClass().getSimpleName() +"] More than one schedule for agent " + agentID +  " were found in the database");
				}
			}
			
		}
		
		return scheduleID;
		
	}
	
}
