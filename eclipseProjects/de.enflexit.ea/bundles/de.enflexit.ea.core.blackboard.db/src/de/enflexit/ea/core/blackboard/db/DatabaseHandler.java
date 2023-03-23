package de.enflexit.ea.core.blackboard.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import agentgui.core.application.Application;
import agentgui.core.project.Project;
import agentgui.simulationService.time.TimeModel;
import agentgui.simulationService.time.TimeModelDateBased;
import de.enflexit.ea.core.blackboard.db.dataModel.AbstractStateResult;
import de.enflexit.ea.core.blackboard.db.dataModel.DataOverview;
import de.enflexit.ea.core.blackboard.db.dataModel.EdgeResult;
import de.enflexit.ea.core.blackboard.db.dataModel.NetworkState;
import de.enflexit.ea.core.blackboard.db.dataModel.NodeResult;
import de.enflexit.ea.core.blackboard.db.dataModel.TrafoResult;

/**
 * The Class DatabaseHandler can be used to {@link NetworkState} data.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class DatabaseHandler {
	 
	private List<NetworkState> networkStateListToSave;
	private boolean doTerminateThread;
	
	private Session session;
	
	/**
	 * Instantiates a new database handler.
	 */
	public DatabaseHandler() { }
	/**
	 * Instantiates a new database handler.
	 * @param session the session instance to use
	 */
	public DatabaseHandler(Session session) {
		this.setSession(session);
	}
	
	/**
	 * Returns the current session instance.
	 * @return the session
	 */
	public Session getSession() {
		if (session==null) {
			session = BlackboardDatabaseConnectionService.getInstance().getNewDatabaseSession();
		}
		return session;
	}
	/**
	 * Sets the current session instance.
	 * @param session the new session
	 */
	public void setSession(Session session) {
		if (this.session!=null) {
			if (session==null) {
				this.session.close();
			} else {
				if (this.session!=session) {
					this.session.close();
				}
			}
		}
		this.session = session;
	}
	/**
	 * Disposes this database handler by closing the database session.
	 */
	public void dispose() {
		this.setSession(null);
	}
	
	
	// --------------------------------------------------------------
	// --- From here, creating a new ExecutionID --------------------
	// --------------------------------------------------------------	
	/**
	 * Returns a new execution ID based on the specified executer description.
	 * @param executerDescription the executer description
	 * 
	 * @return the new execution ID to be used to save data
	 */
	public Integer getNewExecutionID(String executerDescription) {
		
		// --- Get current project information ------------
		Project currProject = Application.getProjectFocused();
		String projectName = currProject.getProjectName();
		String setupName   = currProject.getSimulationSetupCurrent();
		
		// --- Get time information -----------------------
		Calendar setupTimeFrom = null;
		Calendar setupTimeTo   = null;
		TimeModel tm = currProject.getTimeModelController().getTimeModel();
		if (tm!=null && tm instanceof TimeModelDateBased) {
			TimeModelDateBased tmDateBased = (TimeModelDateBased) tm;
			setupTimeFrom = Calendar.getInstance();
			setupTimeFrom.setTimeInMillis(tmDateBased.getTimeStart());
			setupTimeTo   = Calendar.getInstance();
			setupTimeTo.setTimeInMillis(tmDateBased.getTimeStop());
		}
		
		// --- Create a new DataOverview instance ----- 
		DataOverview dataOverView = new DataOverview();
		
		dataOverView.setExecuter(executerDescription);
		dataOverView.setExecutionTime(Calendar.getInstance());
		
		dataOverView.setProjectName(projectName);
		dataOverView.setSetupName(setupName);
		dataOverView.setSetupTimeFrom(setupTimeFrom);
		dataOverView.setSetupTimeTo(setupTimeTo);
		
		// --- Save DataOverview in table -----------------
		if (this.saveDataOverview(dataOverView)==true) {
			return dataOverView.getIdExecution();
		}
		return 0;
	}
	/**
	 * Saves the specified NetworkState.
	 *
	 * @param networkState the NetworkState
	 * @param idSetup the database ID of the setup
	 * @param duplicateDataStrategy the duplicate data strategy to use if the NetworkState already exists 
	 * @return true, if successful
	 */
	public boolean saveDataOverview(DataOverview dataOverview) {

		if (dataOverview==null) return false;
		
		boolean successful = false;
		Transaction transaction = null;
		try {
			// --- Save the DataOverview ------------------
			if (this.getSession()==null) return false;
			
			transaction = this.getSession().beginTransaction();
			this.getSession().save(dataOverview);
			this.getSession().flush();
			this.getSession().clear();
			transaction.commit();
			successful = true;
			
		} catch (Exception ex) {
			if (transaction!=null) transaction.rollback();
			ex.printStackTrace();
			successful = false;
		}
		return successful;
	}
	
	
	// --------------------------------------------------------------
	// --- From here, working on data -------------------------------
	// --------------------------------------------------------------	
	/**
	 * Returns the list of NetworkState to save.
	 * @return the network state list to save
	 */
	public List<NetworkState> getNetworkStateListToSave() {
		if (networkStateListToSave==null) {
			networkStateListToSave = new ArrayList<>();
		}
		return networkStateListToSave;
	}
	
	/**
	 * Adds a NetworkState that is to save.
	 * @param networkState the network state
	 */
	public void addNetworkStateToSave(NetworkState networkState) {
		this.getNetworkStateListToSave().add(networkState);
		synchronized (this.getNetworkStateListToSave()) {
			this.getNetworkStateListToSave().notifyAll();
		}
	}
	/**
	 * Stop network state save thread if no further job is to be done.
	 */
	public void stopNetworkStateSaveThread() {
		this.doTerminateThread = true;
		synchronized (this.getNetworkStateListToSave()) {
			this.getNetworkStateListToSave().notifyAll();
		}
	}
	
	/**
	 * Start network state save thread.
	 */
	public void startNetworkStateSaveThread() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				while (true) {
					// --- Wait for the trigger to save -------------
					if (DatabaseHandler.this.getNetworkStateListToSave().isEmpty()==true) {
						// --- Terminate? ---------------------------
						if (doTerminateThread==true) break;
						// --- Wait for the next state --------------
						try {
							synchronized (DatabaseHandler.this.getNetworkStateListToSave()) {
								DatabaseHandler.this.getNetworkStateListToSave().wait();
							}
							
						} catch (InterruptedException iEx) {
							iEx.printStackTrace();
						}
					}
					
					// --- Get first NetworkState -------------------
					if (DatabaseHandler.this.getNetworkStateListToSave().isEmpty()==false) {
						NetworkState networkState = DatabaseHandler.this.getNetworkStateListToSave().remove(0);
						String timeText = new SimpleDateFormat("dd.MM.yy HH:mm").format(networkState.getStateTime().getTime());
						String statusText = "Saving network state for " + timeText + " - " + DatabaseHandler.this.getNetworkStateListToSave().size() + " network states remaining in queue.";
						Application.setStatusBarMessage(statusText);
						DatabaseHandler.this.saveNetworkState(networkState);
					}
				} // end while
				
				Application.setStatusBarMessageReady();
			}
		}, "NetworkState-SaveThread").start();
	}
	
	/**
	 * Saves the specified NetworkState in the current thread.
	 * @param networkState the network state
	 */
	public void saveNetworkState(NetworkState networkState) {
		
		Session sessionToUse = this.getSession();
		if (sessionToUse==null) return;
		
		Transaction transaction = null;
		try {

			if (sessionToUse.isOpen()==false) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Session not open - skip saving NetworkState.");
				return;
			}
			
			// --- Saving in own transaction? --- 
			transaction = sessionToUse.beginTransaction();
			
			this.saveStateResultUsingNativeSQL(networkState.getNodeResultList(), sessionToUse);
			this.saveStateResultUsingNativeSQL(networkState.getEdgeResultList(), sessionToUse);
			this.saveStateResultUsingNativeSQL(networkState.getTrafoResultList(), sessionToUse);
			
			// --- Saving in own transaction? ---
			sessionToUse.flush();
			sessionToUse.clear();
			transaction.commit();
			
		} catch (Exception ex) {
			if (transaction!=null) transaction.rollback();
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Saves the specified state results by using the native SQL interface.
	 *
	 * @param networkStateList the network state list
	 * @return true, if successful
	 */
	public boolean saveStateResultUsingNativeSQL(List<? extends AbstractStateResult> networkStateList) {
		return this.saveStateResultUsingNativeSQL(networkStateList, this.getSession());
	}
	/**
	 * Saves the specified state results by using the native SQL interface.
	 *
	 * @param networkStateList the network state list
	 * @return true, if successful
	 */
	public boolean saveStateResultUsingNativeSQL(List<? extends AbstractStateResult> networkStateList, Session sessionToUse) {
		
		if (networkStateList==null || networkStateList.size()==0) return false;
		
		boolean successful = false;
		Transaction transaction = null;
		boolean isOpenTransaction = sessionToUse.getTransaction()!=null && sessionToUse.getTransaction().isActive();
		
		// ------------------------------------------------
		// --- Create native SQL statement --  
		String sql = "INSERT INTO "; 
		try {
			
			if (networkStateList.get(0) instanceof NodeResult) {
				sql += "ea_noderesult";
			} else if (networkStateList.get(0) instanceof EdgeResult) {
				sql += "ea_edgeresult";
			} else if (networkStateList.get(0) instanceof TrafoResult) {
				sql += "ea_traforesult";
			}
			sql += " VALUES ";
			
			// --- Create value string ----------
			String sqlValues = "";
			for (int i = 0; i < networkStateList.size(); i++) {
				String valuePart = networkStateList.get(i).getSQLInsertValueArray();
				if (valuePart!=null) {
					if (sqlValues.isEmpty()==false) {
						sqlValues += ", ";
					}
					sqlValues += valuePart;
				}
			}
			sql += sqlValues + ";";
			
			
			// --------------------------------------------
			// --- Saving in own transaction? --- 
			if (isOpenTransaction==false) {
				transaction = sessionToUse.beginTransaction();
			}
			
			// --- Execute SQL statement --------
			Query<?> query = sessionToUse.createNativeQuery(sql);
			query.executeUpdate();
			
			// --- Saving in own transaction? ---
			if (isOpenTransaction==false) {
				sessionToUse.flush();
				sessionToUse.clear();
				transaction.commit();
			}
			successful = true;
			
		} catch (Exception ex) {
			if (transaction!=null) transaction.rollback();
			System.err.println("[" + this.getClass().getSimpleName() + "] Error execution '" + sql + "'");
			ex.printStackTrace();
			successful = false;
		}
		return successful;
	}

}
