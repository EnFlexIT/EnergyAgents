package de.enflexit.ea.core.blackboard.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import agentgui.core.application.Application;
import de.enflexit.ea.core.blackboard.db.dataModel.AbstractStateResult;
import de.enflexit.ea.core.blackboard.db.dataModel.EdgeResult;
import de.enflexit.ea.core.blackboard.db.dataModel.NetworkState;
import de.enflexit.ea.core.blackboard.db.dataModel.NodeResult;
import de.enflexit.ea.core.blackboard.db.dataModel.TrafoResult;


/**
 * The Class DatabaseHandler can be used to save the MEO state results.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
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
	/**
	 * Returns the hibernate batch size.
	 * @return the hibernate batch size
	 */
	private int getHibernateBatchSize() {
		return BlackboardDatabaseConnectionService.getInstance().getHibernateBatchSize();
	}
	
	// --------------------------------------------------------------
	// --- From here, working on data -------------------------------
	// --------------------------------------------------------------	
	
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
		
		try {
			
			// --------------------------------------------
			// --- Create native SQL statement --  
			String sql = "INSERT INTO "; 
			if (networkStateList.get(0) instanceof NodeResult) {
				sql += "noderesult";
			} else if (networkStateList.get(0) instanceof EdgeResult) {
				sql += "edgeresult";
			} else if (networkStateList.get(0) instanceof TrafoResult) {
				sql += "traforesult";
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
			ex.printStackTrace();
			successful = false;
		}
		return successful;
	}
	

	/**
	 * Saves the specified state results.
	 *
	 * @param networkStateList the network state list
	 * @return true, if successful
	 */
	public boolean saveStateResult(List<? extends AbstractStateResult> networkStateList) {
		return this.saveStateResult(networkStateList, this.getSession());
	}
	/**
	 * Saves the specified state results.
	 *
	 * @param networkStateList the network element state list
	 * @param sessionToUse the session to use
	 * @return true, if successful
	 */
	public boolean saveStateResult(List<? extends AbstractStateResult> networkStateList, Session sessionToUse) {
		
		if (networkStateList==null) return false;
		
		boolean successful = false;
		Transaction transaction = null;
		boolean isOpenTransaction = sessionToUse.getTransaction()!=null && sessionToUse.getTransaction().isActive();
		
		try {

			// --- Saving in own transaction? --- 
			if (isOpenTransaction==false) {
				transaction = sessionToUse.beginTransaction();
			}
			
			// --- Set IdNetworkState -----------
			int batchSize = this.getHibernateBatchSize();
			for (int i = 0; i < networkStateList.size(); i++) {
				
				AbstractStateResult stateResult = networkStateList.get(i);
				
				// --- Save the state -----------
				sessionToUse.save(stateResult);
				if (i % batchSize==0) { 
			        // --- Release memory -------
					sessionToUse.flush();
					sessionToUse.clear();
			    }
			}
			
			// --- Saving in own transaction? ---
			if (isOpenTransaction==false) {
				sessionToUse.flush();
				sessionToUse.clear();
				transaction.commit();
			}
			successful = true;
			
		} catch (Exception ex) {
			if (transaction!=null) transaction.rollback();
			ex.printStackTrace();
			successful = false;
		}
		return successful;
	}

	// -------------------------------------------------------------
	// --- Some help functions -------------------------------------
	// -------------------------------------------------------------	
	
	public static final int COLUMN_Field = 0;
	public static final int COLUMN_Type = 1;
	public static final int COLUMN_Nullable = 2;
	public static final int COLUMN_Key = 3;
	public static final int COLUMN_DefaultValue = 4;
	public static final int COLUMN_Extra = 5;
	
	/**
	 * Returns the table columns of the specified instance of an {@link AbstractStateResult} as list, where the sub list elements 
	 * contain the description for each column. Here, the elements are 'Field', 'Type', 'Nullable', 'Key', 'DefaultValue' and 'Extra'.
	 * 
	 * @param stateResult the abstract state result instance
	 * @return the table column list
	 */
	public Object[][] getTableColumns(AbstractStateResult stateResult) {
		
		if (stateResult==null ) return null;

		Object[][] coulmnDescriptionArray = null;
		
		Session sessionToUse = this.getSession();
		
		Transaction transaction = null;
		boolean isOpenTransaction = sessionToUse.getTransaction()!=null && sessionToUse.getTransaction().isActive();
		
		try {
			
			// --------------------------------------------
			// --- Create native SQL statement --  
			String sql = "DESCRIBE "; 
			if (stateResult instanceof NodeResult) {
				sql += "noderesult";
			} else if (stateResult instanceof EdgeResult) {
				sql += "edgeresult";
			} else if (stateResult instanceof TrafoResult) {
				sql += "traforesult";
			}
			sql += ";";
			
			// --------------------------------------------
			// --- Saving in own transaction? --- 
			if (isOpenTransaction==false) {
				transaction = sessionToUse.beginTransaction();
			}
			
			// --- Execute SQL statement --------
			Query<?> query = sessionToUse.createNativeQuery(sql);
			List<?> columnResult = query.getResultList();
			
			// --- Commit read action? --------------------
			if (isOpenTransaction==false) {
				transaction.commit();
			}
			
			// --- Prepare result list --------------------
			coulmnDescriptionArray = new Object[columnResult.size()][6];
			for (int i = 0; i < columnResult.size(); i++) {
				Object[] singleCoulmnArray = (Object[]) columnResult.get(i);
				for (int j = 0; j < singleCoulmnArray.length; j++) {
					coulmnDescriptionArray[i][j] = singleCoulmnArray[j]; 
				}
			}
			
		} catch (Exception ex) {
			if (transaction!=null) transaction.rollback();
			ex.printStackTrace();
			coulmnDescriptionArray = null;
		}
		return coulmnDescriptionArray;
	}
	/**
	 * Checks if the specified column (column name) is part of the {@link AbstractStateResult}.
	 *
	 * @param stateResult the instance of an abstract state result
	 * @param coumnNameToCheck the column name to check
	 * @return true, if successful
	 */
	public boolean containsTableColumn(AbstractStateResult stateResult, String coumnNameToCheck) {
		
		boolean containsColumn = false;
		
		Object[][] coulmnDescriptionArray = this.getTableColumns(stateResult);
		if (coulmnDescriptionArray!=null) {
			for (int i = 0; i < coulmnDescriptionArray.length; i++) {
				String columnName = (String) coulmnDescriptionArray[i][COLUMN_Field];
				if (columnName.equals(coumnNameToCheck)==true) {
					containsColumn = true;
					break;
				}
			}
		}
		return containsColumn;
	}
	
}
