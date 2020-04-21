package de.enflexit.ea.core.dataModel.deployment;

import java.io.Serializable;
import java.util.Properties;

import agentgui.core.config.GlobalInfo.MtpProtocol;
import de.enflexit.db.hibernate.gui.DatabaseSettings;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;

/**
 * The Class DeploymentSettings represents the data model for the deployment 
 * settings of an agent and is used in the class {@link HyGridAbstractEnvironmentModel}.
 *
 * @author Mohamed Amine JEDIDI <mohamedamine_jedidi@outlook.com>
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class DeploymentSettings implements Serializable {
	
	public enum DeploymentMode {
		FULL, UPDATE
	}

	private static final long serialVersionUID = 8408971271923452212L;
	
	// --- Some defaults ---------------------
	public static final String DEFAULT_CEA_LOCAL_NAME = "CeExAg";
	public static final int DEFAULT_JADE_PORT = 1099;
	public static final int DEFAULT_MTP_PORT = 7778;
	public static final MtpProtocol DEFAULT_MTP = MtpProtocol.HTTP;
	public static final AgentOperatingMode DEFAULT_OPERATING_MODE = AgentOperatingMode.TestBedReal;
	
	private AgentSpecifier centralAgentSpecifier;
	private KeyStore keyStore;
	private TrustStore trustStore;
	private Certificate certificate;
	
	private int jadePort;
	private int mtpPort;
	
	private String targetOperatingSystem;
	private AgentOperatingMode defaultAgentOperatingMode;
	private boolean targetSystemAutoIp;
	private String targetSystemIpAddress;
	
	private boolean p2RepositoryEnabled;
	private String p2Repository;
	private boolean projectRepositoryEnabled;
	private String projectRepository;
	
	private String projectTag;
	
	private DatabaseSettings databaseSettings;
	
	private DeploymentMode deploymentMode;
	
	/**
	 * Gets the AgentSpecifier.
	 * @return the agentSpecifier
	 */
	public AgentSpecifier getCentralAgentSpecifier() {
		if (this.centralAgentSpecifier==null) {
			this.centralAgentSpecifier = new AgentSpecifier();
		}
		return this.centralAgentSpecifier;
	}
	/**
	 * Sets the AgentSpecifier.
	 * @param agentSpecifier the new AgentSpecifier
	 */
	public void setCentralAgentSpecifier(AgentSpecifier agentSpecifier) {
		this.centralAgentSpecifier = agentSpecifier;
	}
	/**
	 * Gets the KeyStore.
	 * @return the certificate
	 */
	public KeyStore getKeyStore() {
		if (this.keyStore==null) {
			this.keyStore = new KeyStore();
		}
		return this.keyStore;
	}
	/**
	 * Sets the KeyStore.
	 * @param keyStore the new KeyStore
	 */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}
	/**
	 * Gets the TrustStore.
	 * @return the key store
	 */
	public TrustStore getTrustStore() {
		if (this.trustStore==null) {
			this.trustStore = new TrustStore();
		}
		return this.trustStore;
	}
	/**
	 * Sets the TrustStore.
	 * @param trustStore the new TrustStore
	 */
	public void setTrustStore(TrustStore trustStore) {
		this.trustStore = trustStore;
	}
	/**
	 * Gets the certificate.
	 * @return the certificate
	 */
	public Certificate getCertificate() {
		if(certificate==null){
			certificate = new Certificate();
		}
		return certificate;
	}
	/**
	 * Sets the certificate.
	 * @param certificate the new certificate
	 */
	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}
	
	/**
	 * Gets the default agent operating mode.
	 * @return the default agent operating mode
	 */
	public AgentOperatingMode getDefaultAgentOperatingMode() {
		return defaultAgentOperatingMode;
	}
	
	/**
	 * Sets the default agent operating mode.
	 * @param defaultAgentOperatingMode the new default agent operating mode
	 */
	public void setDefaultAgentOperatingMode(AgentOperatingMode defaultAgentOperatingMode) {
		this.defaultAgentOperatingMode = defaultAgentOperatingMode;
	}

	/**
	 * Checks if is target system auto ip.
	 * @return true, if is target system auto ip
	 */
	public boolean isTargetSystemAutoIp() {
		return targetSystemAutoIp;
	}
	
	/**
	 * Sets the target system auto ip.
	 * @param targetSystemAutoIp the new target system auto ip
	 */
	public void setTargetSystemAutoIp(boolean targetSystemAutoIp) {
		this.targetSystemAutoIp = targetSystemAutoIp;
	}
	
	/**
	 * Gets the target system ip address.
	 * @return the target system ip address
	 */
	public String getTargetSystemIpAddress() {
		return targetSystemIpAddress;
	}
	
	/**
	 * Sets the target system ip address.
	 * @param targetSystemIpAddress the new target system ip address
	 */
	public void setTargetSystemIpAddress(String targetSystemIpAddress) {
		this.targetSystemIpAddress = targetSystemIpAddress;
	}
	
	/**
	 * Gets the jade port.
	 * @return the jade port
	 */
	public int getJadePort() {
		return jadePort;
	}

	/**
	 * Sets the jade port.
	 * @param jadePort the new jade port
	 */
	public void setJadePort(int jadePort) {
		this.jadePort = jadePort;
	}

	/**
	 * Gets the mtp port.
	 * @return the mtp port
	 */
	public int getMtpPort() {
		return mtpPort;
	}

	/**
	 * Sets the mtp port.
	 * @param mtpPort the new mtp port
	 */
	public void setMtpPort(int mtpPort) {
		this.mtpPort = mtpPort;
	}

	/**
	 * Gets the installation package description.
	 * @return the installation package description
	 */
	public String getTargetOperatingSystem() {
		return targetOperatingSystem;
	}

	/**
	 * Sets the installation package description.
	 * @param targetOperatingSystem the new installation package description
	 */
	public void setTargetOperatingSystem(String targetOperatingSystem) {
		this.targetOperatingSystem = targetOperatingSystem;
	}
	
	/**
	 * Checks if is p 2 repository enabled.
	 *
	 * @return true, if is p 2 repository enabled
	 */
	public boolean isP2RepositoryEnabled() {
		return p2RepositoryEnabled;
	}
	/**
	 * Sets the p 2 repository enabled.
	 *
	 * @param p2RepositoryEnabled the new p 2 repository enabled
	 */
	public void setP2RepositoryEnabled(boolean p2RepositoryEnabled) {
		this.p2RepositoryEnabled = p2RepositoryEnabled;
	}
	/**
	 * Gets the p2 repository.
	 * @return the p2 repository
	 */
	public String getP2Repository() {
		return p2Repository;
	}
	
	/**
	 * Sets the p2 repository.
	 * @param p2Repository the new p2 repository
	 */
	public void setP2Repository(String p2Repository) {
		this.p2Repository = p2Repository;
	}
	
	/**
	 * Checks if is project repository enabled.
	 *
	 * @return true, if is project repository enabled
	 */
	public boolean isProjectRepositoryEnabled() {
		return projectRepositoryEnabled;
	}
	/**
	 * Sets the project repository enabled.
	 *
	 * @param projectRepositoryEnabled the new project repository enabled
	 */
	public void setProjectRepositoryEnabled(boolean projectRepositoryEnabled) {
		this.projectRepositoryEnabled = projectRepositoryEnabled;
	}
	/**
	 * Gets the project repository.
	 * @return the project repository
	 */
	public String getProjectRepository() {
		return projectRepository;
	}
	
	/**
	 * Sets the project repository.
	 * @param projectRepository the new project repository
	 */
	public void setProjectRepository(String projectRepository) {
		this.projectRepository = projectRepository;
	}
	
	/**
	 * Gets the project tag.
	 * @return the project tag
	 */
	public String getProjectTag() {
		return projectTag;
	}
	
	/**
	 * Sets the project tag.
	 * @param projectTag the new project tag
	 */
	public void setProjectTag(String projectTag) {
		this.projectTag = projectTag;
	}
	
	/**
	 * Gets the database settings.
	 * @return the database settings
	 */
	public DatabaseSettings getDatabaseSettings() {
		return databaseSettings;
	}
	/**
	 * Sets the database settings.
	 * @param databaseSettings the new database settings
	 */
	public void setDatabaseSettings(DatabaseSettings databaseSettings) {
		this.databaseSettings = databaseSettings;
	}
	
	/**
	 * Gets the deployment mode.
	 * @return the deployment mode
	 */
	public DeploymentMode getDeploymentMode() {
		return deploymentMode;
	}
	
	/**
	 * Sets the deployment target.
	 * @param deploymentMode the new deployment target
	 */
	public void setDeploymentMode(DeploymentMode deploymentMode) {
		this.deploymentMode = deploymentMode;
	}
	/**
	 * Returns a copy of the current instance.
	 * @return the copy
	 */
	public DeploymentSettings getCopy() {
		DeploymentSettings copy = new DeploymentSettings();
		copy.setCentralAgentSpecifier((AgentSpecifier) this.centralAgentSpecifier.clone());
		copy.setKeyStore(this.keyStore);
		copy.setTrustStore(this.trustStore);
		copy.setCertificate(this.certificate);
		copy.setJadePort(this.jadePort);
		copy.setMtpPort(this.mtpPort);
		copy.setTargetOperatingSystem(this.getTargetOperatingSystem());
		copy.setProjectTag(this.getProjectTag());
		copy.setTargetSystemIpAddress(this.getTargetSystemIpAddress());
		copy.setTargetSystemAutoIp(this.isTargetSystemAutoIp());
		copy.setDefaultAgentOperatingMode(this.getDefaultAgentOperatingMode());
		copy.setP2RepositoryEnabled(this.isP2RepositoryEnabled());
		copy.setP2Repository(this.getP2Repository());
		copy.setProjectRepositoryEnabled(this.isProjectRepositoryEnabled());
		copy.setProjectRepository(this.getProjectRepository());
		copy.setDeploymentMode(this.getDeploymentMode());
		
		if (this.getDatabaseSettings()!=null) {
			DatabaseSettings databaseSettings = new DatabaseSettings(this.getDatabaseSettings().getDatabaseSystemName(), (Properties) this.getDatabaseSettings().getHibernateDatabaseSettings().clone());
			copy.setDatabaseSettings(databaseSettings);
		}
		
		return copy;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeploymentSettings) {
		
			DeploymentSettings otherInstance = (DeploymentSettings) obj;
			boolean specifierEquals;
			if (this.getCentralAgentSpecifier()==null && otherInstance.getCentralAgentSpecifier()==null) {
				specifierEquals = true;
			} else {
				specifierEquals = this.getCentralAgentSpecifier().equals(otherInstance.getCentralAgentSpecifier());
			}
			return (specifierEquals
					&& this.getJadePort()==otherInstance.getJadePort()
					&& this.getMtpPort()==otherInstance.getMtpPort()
					&& this.getTargetOperatingSystem().equals(otherInstance.getTargetOperatingSystem())
					&& this.isTargetSystemAutoIp()==otherInstance.isTargetSystemAutoIp()
					&& this.targetSystemIpAddress.equals(otherInstance.getTargetSystemIpAddress())
					&& this.isP2RepositoryEnabled()==otherInstance.isP2RepositoryEnabled()
					&& this.getP2Repository().equals(otherInstance.getP2Repository())
					&& this.isProjectRepositoryEnabled()==otherInstance.isProjectRepositoryEnabled()
					&& this.getProjectRepository().equals(otherInstance.getProjectRepository())
					&& this.getProjectTag().equals(otherInstance.getProjectTag())
					&& this.getDatabaseSettings().equals(otherInstance.getDatabaseSettings())
					&& this.getDeploymentMode()==otherInstance.getDeploymentMode()
			);
		} else {
			return false;
		}
	}
	
}
