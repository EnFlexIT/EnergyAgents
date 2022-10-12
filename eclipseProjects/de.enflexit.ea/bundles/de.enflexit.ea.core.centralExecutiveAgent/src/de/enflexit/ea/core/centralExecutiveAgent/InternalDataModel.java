package de.enflexit.ea.core.centralExecutiveAgent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.core.application.Application;
import agentgui.core.environment.EnvironmentController;
import agentgui.core.project.Project;
import agentgui.simulationService.environment.EnvironmentModel;
import de.enflexit.common.crypto.TrustStoreController;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.ProxyCounterPartNotificationBehaviour;
import de.enflexit.ea.core.dataModel.DirectoryHelper;
import de.enflexit.ea.core.dataModel.DirectoryHelper.DirectoryType;
import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.dataModel.ontology.RemoteAgentInformation;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookEntry;
import de.enflexit.jade.phonebook.PhoneBook;
import jade.core.AID;
import jade.core.NotFoundException;
import jade.core.Profile;
import jade.mtp.MTPException;
import jade.wrapper.StaleProxyException;

/**
 * The Class for the InternalDatamodel of the CEA.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class InternalDataModel {

	private static final String CERTIFICATE_TYPE = "X.509";
	
	private CentralExecutiveAgent cea;
	
	private Project project;
	private EnvironmentModel envModel;
	private NetworkModel networkModel;
	private NetworkComponent networkComponentOfCEA;
	private CeaConfigModel ceaConfigModel;

	private PhoneBook phoneBook;
	
	private String trustStoreFile;
	private String trustStorePassword;
	private TrustStoreController trustStoreController;
	
	private HashMap<String, RemoteAgentInformation> remoteAgentDirectory;
	
	private AID liveMonitoringProxyAgentAID;
	
	/**
	 * Instantiates a new internal data model.
	 * @param myCea the my {@link CentralExecutiveAgent} instance
	 */
	public InternalDataModel(CentralExecutiveAgent myCea) {
		this.cea = myCea;
	}
	
	/**
	 * Returns the current project.
	 * @return the project
	 */
	public Project getProject() {
		if (project==null) {
			project = Application.getProjectFocused();
		}
		return project;
	}
	/**
	 * Gets the environment model from the current project.
	 * @return The environment model
	 */
	public EnvironmentModel getEnvironmentModel(){
		if (envModel==null) {
			// --- Get the model from the GraphController of the current project --------
			Project project = this.getProject();
			if (project!=null) {
				EnvironmentController envController = project.getEnvironmentController();
				if (envController!=null) {
					envModel = envController.getEnvironmentModel();
				}
			}
		}
		return envModel;
	}
	/**
	 * Returns the current NetworkModel.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		if (networkModel==null) {
			EnvironmentModel envModel = this.getEnvironmentModel();
			if (envModel!=null) {
				networkModel = (NetworkModel) envModel.getDisplayEnvironment();
			}
		}
		return networkModel;
	}
	/**
	 * Returns the CEA's NetworkComponent.
	 * @return the cea network component
	 */
	public NetworkComponent getNetworkComponentOfCEA() {
		if (networkComponentOfCEA==null) {
			NetworkModel netModel = this.getNetworkModel();
			if (netModel!=null) {
				networkComponentOfCEA = netModel.getNetworkComponent(this.cea.getLocalName());
			}
		}
		return networkComponentOfCEA;
	}
	/**
	 * Returns the {@link CeaConfigModel} that contains the user defined information.
	 * @return the cea data model
	 */
	public CeaConfigModel getCeaConfigModel() {
		if (ceaConfigModel==null) {
			NetworkComponent netComp = this.getNetworkComponentOfCEA();
			if (netComp!=null) {
				Object netCompDataModel = netComp.getDataModel();
				if (netCompDataModel!=null) {
					if (netCompDataModel instanceof CeaConfigModel) {
						ceaConfigModel = (CeaConfigModel) netCompDataModel;
					} else if (netCompDataModel.getClass().isArray()==true) {
						Object[] netCompDataModelArray = (Object[]) netCompDataModel;
						if (netCompDataModelArray.length>0 && netCompDataModelArray[0] instanceof CeaConfigModel) {
							ceaConfigModel = (CeaConfigModel) netCompDataModelArray[0];
						}
					}
				}
			}
		}
		return ceaConfigModel;
	}

	/**
	 * Returns the specified file or directory.<br> 
	 * <b>Notice:</b> For the case {@link DirectoryType#SystemMonitoringFile}, the path and file name will be adjusted 
	 * to the current system time. The directory will correspond to the current month, while for each day a file is 
	 * considered (e.g. ./[projectBaseDir]/[localAgentName]/log/03/DAY_07_SystemMonitoring.bin for the 7th of March
	 * of a year). Thus, a ring memory for one year will be constructed.
	 *
	 * @param type the type of the directory or file
	 * @return the file or directory
	 */
	public File getFileOrDirectory(DirectoryType type) {
		return DirectoryHelper.getFileOrDirectory(type, this.cea.getAID());
	}
	/**
	 * Gets the phone book.
	 * @return the phone book
	 */
	public PhoneBook getPhoneBook() {
		if (phoneBook==null) {
			phoneBook = PhoneBook.loadPhoneBook(this.getFileOrDirectory(DirectoryType.PhoneBookFile), EnergyAgentPhoneBookEntry.class);
			if (phoneBook==null) {
				// --- Create temporary PhoneBook instance ---------- 
				phoneBook = new PhoneBook();
				System.out.println("[" + this.cea.getLocalName() + "] Created temporary phonebook!");
			}
		}
		return phoneBook;
	}
	/**
	 * Gets an AID from the phone book.
	 * @param localName the local name of the agent to look up
	 * @return the agent's AID, null if not found
	 */
	public AID getAidFromPhoneBook(String localName) {
		return this.getPhoneBook().getAidForLocalName(localName);
	}
	
	/**
	 * Returns the path to the trust store file.
	 * @return the trust store file
	 */
	public String getTrustStoreFile() {
		if (trustStoreFile == null) {
			trustStoreFile = Application.getProjectFocused().getJadeConfiguration().getTrustStoreFile();
		}
		return trustStoreFile;
	}
	/**
	 * Returns the trust store password.
	 * @return the trust store password
	 */
	public String getTrustStorePassword() {
		if (trustStorePassword == null) {
			trustStorePassword = Application.getProjectFocused().getJadeConfiguration().getTrustStorePassword();
		}
		return trustStorePassword;
	}
	/**
	 * Returns the trust store controller.
	 * @return the trust store controller
	 */
	public TrustStoreController getTrustStoreController() {
		if (trustStoreController == null) {
			trustStoreController = new TrustStoreController(null);
		}
		return trustStoreController;
	}
	
	
	/**
	 * Gets the remote agent directory.
	 * @return the remote agent directory
	 */
	public HashMap<String, RemoteAgentInformation> getRemoteAgentDirectory() {
		if (this.remoteAgentDirectory == null){
			this.remoteAgentDirectory = new HashMap<String, RemoteAgentInformation>();
		}
		return remoteAgentDirectory;
	}
	/**
	 * Gets the remote agent directory entry for the specified local name.
	 * @param localName the local name
	 * @return the directory entry
	 */
	public RemoteAgentInformation getDirectoryEntry(String localName){
		RemoteAgentInformation entry = this.getRemoteAgentDirectory().get(localName);
		if (entry==null) {
			entry = new RemoteAgentInformation();
			this.getRemoteAgentDirectory().put(localName, entry);
		}
		return entry;
	}
	/**
	 * Adds a proxy agent AID to the directory.
	 * @param proxyAgentAID the proxy agent AID
	 */
	public void addProxyAgentAID(AID proxyAgentAID){
		RemoteAgentInformation entry = this.getDirectoryEntry(proxyAgentAID.getLocalName()); 
		entry.setProxyAgentAID(proxyAgentAID);
		if (entry.isComplete()) {
			this.cea.addBehaviour(new ProxyCounterPartNotificationBehaviour(entry));
		}
	}
	/**
	 * Adds a remote agent AID to the directory.
	 * @param remoteAgentAID the remote agent AID
	 */
	public void addRemoteAgentAID(AID remoteAgentAID){
		RemoteAgentInformation entry = this.getDirectoryEntry(remoteAgentAID.getLocalName());
		entry.setRemoteAgentAID(remoteAgentAID);
		if (entry.isComplete()) {
			this.cea.addBehaviour(new ProxyCounterPartNotificationBehaviour(entry));
		}
	}
	
	/**
	 * Gets the live monitoring proxy agent AID.
	 * @return the live monitoring proxy agent AID
	 */
	public AID getLiveMonitoringProxyAgentAID() {
		return liveMonitoringProxyAgentAID;
	}
	/**
	 * Sets the live monitoring proxy agent AID.
	 * @param liveMonitoringProxyAgentAID the new live monitoring proxy agent AID
	 */
	public void setLiveMonitoringProxyAgentAID(AID liveMonitoringProxyAgentAID) {
		this.liveMonitoringProxyAgentAID = liveMonitoringProxyAgentAID;
	}

	/**
	 * Adds the certificate.
	 * @param encodedCertificate the encoded certificate
	 */
	public void handleCertificate(String agentLocalName, String encodedCertificate){
		
		System.out.println(this.cea.getLocalName() + ": Received certificate from " + agentLocalName);
		
		// --- Decode and temporary store the certificate -------------
		byte[] decodedCertificateBytes = Base64.decodeBase64(encodedCertificate.getBytes());
		Certificate certificate = null;
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
			InputStream inputStream = new ByteArrayInputStream(decodedCertificateBytes);
			certificate = certFactory.generateCertificate(inputStream);
			
		} catch (CertificateException cEx) {
			cEx.printStackTrace();
		}
		
		if (certificate!=null) {
			
			X509Certificate sentCertificate = (X509Certificate) certificate;
			sentCertificate.getNotAfter().toString();
			if (sentCertificate.getNotAfter().before(new Date())) {
				System.err.println(this.cea.getLocalName() + ": Certificate for "+ agentLocalName + " is expired!");
			} else {
				// --- Check if there is already a certificate stored for this agent ------------
				TrustStoreController tsController = this.cea.getInternalDataModel().getTrustStoreController();
				tsController.openTrustStore(new File(this.cea.getInternalDataModel().getTrustStoreFile()), this.cea.getInternalDataModel().getTrustStorePassword());
				X509Certificate storedCertificate = tsController.getCertificate(agentLocalName);
				
				// --- If there is no certificate for this agent or the sent one is newer, add it to the store ---------
				if (storedCertificate==null || storedCertificate.getNotAfter().before(sentCertificate.getNotAfter())) {
					try {
						System.out.println(this.cea.getLocalName() + ": Adding certificate received from " + agentLocalName + " to the TrustStore");
						tsController.addCertificate(sentCertificate, agentLocalName);
						tsController.saveTrustStore(this.cea.getInternalDataModel().getTrustStorePassword());
						
						// --- Restart the MTP -------------------
						//	String mtpIpAddress = Application.getProjectFocused().getJadeConfiguration().getMtpIpAddress();
						String address = Application.getJadePlatform().getContainerProfile().getParameter(Profile.LOCAL_HOST, null);
						Integer port = Application.getProjectFocused().getJadeConfiguration().getLocalPortMTP();
						this.restartMTP(address, port);
						
					} catch (KeyStoreException e) {
						System.err.println(this.cea.getLocalName() + ": Error storing the certificate for agent " + agentLocalName);
						e.printStackTrace();
					}
					
				} else {
					System.out.println(this.cea.getLocalName() + ": Stored certificate for " + agentLocalName + " is up to date, irgoring the sent one");
				}
			}
		}
		
		// TODO Add the certificate to the truststore if there is no certificate for this agent or this one is newer
		// --- Delete temporary stored certificate file
		// TODO implement certificate handling
	}
	/**
	 * Restart the HTTPS MTP. 
	 * @param address the address
	 */
	private void restartMTP(String address, Integer port){
		try {
			Application.getJadePlatform().getMainContainer().uninstallMTP("https://" + address + ":" + port + "/acc");
			Application.getJadePlatform().getMainContainer().installMTP("https://" + address + ":" + port + "/acc", jade.mtp.http.MessageTransportProtocol.class.getName()); 
			
		} catch (MTPException | StaleProxyException | NotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
}
