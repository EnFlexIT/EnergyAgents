package de.enflexit.ea.core.simulation.db;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import de.enflexit.ea.core.simulation.db.dataModel.SimulationOverview;
import de.enflexit.ea.core.simulation.db.dataModel.SimulationProperties;

/**
 * The Class DatabaseHandler can be used to {@link NetworkState} data.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class DatabaseHandler {
	 
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
			session = SimulationDatabaseConnectionService.getInstance().getNewDatabaseSession();
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
	 * Saves or updates the data for an executed simulation.
	 *
	 * @param simulationOverview the simulation overview to save to database
	 * @return true, if successful
	 */
	public boolean saveOrUpdateSimulationOverview(SimulationOverview simulationOverview) {

		if (simulationOverview==null) return false;
		
		boolean successful = false;
		Transaction transaction = null;
		List<SimulationProperties> simulationPropertiesList = null;
		try {
			// --- Save the SimulationOverview ----------------------
			if (this.getSession()==null) return false;
			
			// --- Extract property list ----------------------------
			simulationPropertiesList = simulationOverview.getSimulationProperties();
			simulationOverview.setSimulationProperties(null);
			
			// --- Save main data set -------------------------------
			transaction = this.getSession().beginTransaction();
			this.getSession().saveOrUpdate(simulationOverview);
			this.getSession().flush();
			this.getSession().clear();
			transaction.commit();

			// --- Save the properties ------------------------------
			successful = this.saveOrUpdateSimulationPropertiesOverview(simulationPropertiesList, simulationOverview.getIdSimulation());
					
		} catch (Exception ex) {
			if (transaction!=null) transaction.rollback();
			ex.printStackTrace();
			successful = false;
		} finally {
			if (simulationPropertiesList!=null) {
				simulationOverview.setSimulationProperties(simulationPropertiesList);
			}
		}
		return successful;
	}
	/**
	 * Saves or updates the specified list of SimulationProperties.
	 *
	 * @param simulationOverview the simulation overview to save to database
	 * @return true, if successful
	 */
	public boolean saveOrUpdateSimulationPropertiesOverview(List<SimulationProperties> simulationPropertiesList, int idSimulation) {

		if (simulationPropertiesList==null || simulationPropertiesList.size()==0) return true;
		
		boolean successful = false;
		Transaction transaction = null;
		try {
			// --- Save the SimulationOverview ------------------
			if (this.getSession()==null) return false;
			
			transaction = this.getSession().beginTransaction();
			for (SimulationProperties simProp : simulationPropertiesList) {
				simProp.setIdSimulation(idSimulation);
				this.getSession().saveOrUpdate(simProp);
				this.getSession().flush();
			}
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
	
	

}
