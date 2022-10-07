package de.enflexit.ea.core.blackboard.db;

import java.net.URL;
import java.util.Vector;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

import de.enflexit.db.hibernate.HibernateUtilities;
import de.enflexit.db.hibernate.connection.HibernateDatabaseConnectionService;

/**
 * The Class BlackboardDatabaseConnectionService provides the database connection for the Blackboard-DB.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class BlackboardDatabaseConnectionService implements HibernateDatabaseConnectionService {

	public static final String SESSION_FACTORY_ID = "de.enflexit.ea.core.blackboard.db";
	
	private static final String cfgFile = "/de/enflexit/ea/core/blackboard/db/cfg/hibernate.cfg.xml";
	private static final String mappingFilesPackage = "/de/enflexit/ea/core/blackboard/db/mappings/";
	
	
	private Bundle localBundle;
	private Configuration configuration;

	private int hibernateBatchSize = 50;
	
	
	// ----------------------------------------------------
	// --- Singleton access for the current instance ------
	private static BlackboardDatabaseConnectionService instance;
	public static BlackboardDatabaseConnectionService getInstance() {
		if (instance==null) {
			instance = new BlackboardDatabaseConnectionService();
		}
		return instance;
	}
	// --- Singleton access for the current instance ------
	// ----------------------------------------------------
	
	/**
	 * Instantiates a new database session factory handler.
	 * (Even we use a singleton here, for OSGI-service initiation a public construction is required)
	 */
	public BlackboardDatabaseConnectionService() { 
		instance = this;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.db.hibernate.connection.HibernateDatabaseConnectionService#getFactoryID()
	 */
	@Override
	public String getFactoryID() {
		return SESSION_FACTORY_ID;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.db.hibernate.connection.HibernateDatabaseConnectionService#getConfiguration()
	 */
	@Override
	public Configuration getConfiguration() {
		if (configuration==null) {
			URL url = this.getLocalBundle().getResource(cfgFile);
			configuration = new Configuration().configure(url);
			this.addInternalHibernateProperties(configuration);
			this.addMappingFileResources(configuration);
		}
		return configuration;
	}
	
	/**
	 * Returns the local bundle.
	 * @return the local bundle
	 */
	public Bundle getLocalBundle() {
		if (localBundle==null) {
			localBundle = FrameworkUtil.getBundle(BlackboardDatabaseConnectionService.class);
		}
		return localBundle;
	}
	
	/**
	 * Adds internal hibernate configuration properties.
	 * @param configuration the configuration to be used
	 */
	private void addInternalHibernateProperties(Configuration configuration) {
		configuration.setProperty("hibernate.jdbc.batch_size", ((Integer)getHibernateBatchSize()).toString());
	}
	/**
	 * Gets the hibernate batch size.
	 * @return the hibernate batch size
	 */
	public Integer getHibernateBatchSize() {
		return hibernateBatchSize;
	}

	/**
	 * Adds the hibernate mapping files to the configuration.
	 * @param conf the current hibernate configuration
	 */
	private void addMappingFileResources(Configuration conf) {
		
		Bundle bundle = this.getLocalBundle();
		if (conf==null || bundle==null) return;
		
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		if (bundleWiring==null) return; 
		
		Vector<String> mappingResources = new Vector<>(bundleWiring.listResources(mappingFilesPackage, "*.hbm.xml", BundleWiring.LISTRESOURCES_LOCAL));
		for (int i = 0; i < mappingResources.size(); i++) {
			String mappingResource = mappingResources.get(i);
			conf.addResource(mappingResource);
		}
	}
	
	
	// ------------------------------------------------------------------------
	// --- Handling for DB session factory and its configuration --------------
	// ------------------------------------------------------------------------
	/**
	 * Gets the new hibernate database session.
	 * @return the new database session
	 */
	public Session getNewDatabaseSession() {
		return getNewDatabaseSession(false);
	}
	/**
	 * Gets the new hibernate database session.
	 *
	 * @param isResetSessionFactory the reset session factory
	 * @return the new database session
	 */
	public Session getNewDatabaseSession(boolean isResetSessionFactory) {
		Session session = null;
		SessionFactory sf = HibernateUtilities.getSessionFactory(this.getFactoryID(), this.getConfiguration(), isResetSessionFactory, true);
		if (sf!=null) {
			session = sf.openSession();
		}
		return session;
	}
	/**
	 * Closes the current session factory.
	 */
	public void closeSessionFactory() {
		HibernateUtilities.closeSessionFactory(this.getFactoryID());
	}

	
	/**
	 * Sets the statistics for the SessionFactory enabled (or not).
	 * @param setEnabled the set enabled
	 */
	public void setStatisticsEnabled(boolean setEnabled) {
		HibernateUtilities.setStatisticsEnabled(this.getFactoryID(), setEnabled);
	}
	/**
	 * Write the SessionFactory statistics.
	 */
	public void writeStatistics() {
		HibernateUtilities.writeStatistics(this.getFactoryID());
	}

}
