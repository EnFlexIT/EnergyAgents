package de.enflexit.ea.core.centralExecutiveAgent;

import java.util.Date;

import agentgui.core.application.Application;
import agentgui.core.config.GlobalInfo.DeviceSystemExecutionMode;
import agentgui.core.config.GlobalInfo.ExecutionMode;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.LiveMonitoringProxyRequestResponder;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.MessageReceiveBehaviour;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.PhoneBookQueryResponder;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.PlatformUpdateBehaviour;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.RepositoryMirrorBehaviour;
import de.enflexit.ea.core.centralExecutiveAgent.behaviour.StartMtpBehaviour;
import de.enflexit.ea.core.dataModel.PlatformUpdater;
import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.opsOntology.OpsOntology;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;

/**
 * The Class CentralExecutiveAgent represents the administrative agent that has to be utilized to
 * control the installations in a field or testbed environment.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * 
 * @version 2.0
 * @since 29-05-2016
 */
public class CentralExecutiveAgent extends Agent {

	private static final long serialVersionUID = -738694545680671492L;

	private InternalDataModel internalDataModel;
	private RepositoryMirrorBehaviour repositoryMirrorBehaviour;
	private PlatformUpdateBehaviour updateBehaviour;
	
	private MessageReceiveBehaviour messageReceiveBehaviour;
	private PhoneBookQueryResponder phoneBookQueryResponder;
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		
		// --- Register Codec and Ontologies ------------------------
		this.getContentManager().registerLanguage(new SLCodec());
		this.getContentManager().registerOntology(HyGridOntology.getInstance());
		this.getContentManager().registerOntology(OpsOntology.getInstance());
		
		// --- Start the repository mirroring -----------------------
		this.startRepositoryMirrorBehaviourNow();
		// --- Start the update behaviour ---------------------------
		this.startPlatformUpdateBehaviour();

		// --- Start check to install as second MTP -----------------
		this.addBehaviour(new StartMtpBehaviour(this));
		
		// --- Add the message receive behaviour --------------------
		this.addBehaviour(this.getMessageReceiveBehaviour());
		
		// --- Starts the PhoneBookQueryResponder behaviour ---------
		this.startPhoneBookQueryResponder();
		
		// --- If deployed, add the responder for live monitoring requests ----
		if (this.isExecutedInSimulation()==false) {
			this.addBehaviour(new LiveMonitoringProxyRequestResponder(this));
			this.getMessageReceiveBehaviour().addMessageTemplateToIgnoreList(LiveMonitoringProxyRequestResponder.getMessageTemplate());
		}
		
	}
	
	/**
	 * Returns the overall internal data model of the CEA.
	 * @return the internal data model
	 */
	public InternalDataModel getInternalDataModel() {
		if (internalDataModel==null) {
			internalDataModel = new InternalDataModel(this);
		}
		return internalDataModel;
	}
	/**
	 * Checks if the current execution is a simulation.
	 * @return true, if the execution is simulation
	 */
	public boolean isExecutedInSimulation() {
		// --- Check application settings -----------------
		if (Application.getGlobalInfo().getExecutionMode()==ExecutionMode.DEVICE_SYSTEM) {
			if (Application.getGlobalInfo().getDeviceServiceExecutionMode()==DeviceSystemExecutionMode.AGENT) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Directly starts the repository mirror behaviour and return its instance.
	 * The instance can be used to check if the mirroring was finalized by checking 
	 * the mirror feedback.  
	 * @return the repository mirror behaviour
	 * @see RepositoryMirrorBehaviour#getMirrorJobFeedback()
	 * @see RepositoryMirrorBehaviour#isMirrorJobsAreDone()
	 */
	public RepositoryMirrorBehaviour startRepositoryMirrorBehaviourNow() {
		RepositoryMirrorBehaviour rmb = new RepositoryMirrorBehaviour(this, new Date());
		this.setRepositoryMirrorBehaviour(rmb);
		this.startRepositoryMirrorBehaviour();
		return rmb;
	}
	/**
	 * Starts a new repository mirror behaviour.
	 */
	public void startNewRepositoryMirrorBehaviour() {
		this.setRepositoryMirrorBehaviour(null);
		this.startRepositoryMirrorBehaviour();
	}
	/**
	 * Regularly starts a repository mirror behaviour according to the {@link CeaConfigModel}.
	 * @see CeaConfigModel#getMirrorInterval()
	 */
	public void startRepositoryMirrorBehaviour() {
		RepositoryMirrorBehaviour rmb = this.getRepositoryMirrorBehaviour();
		if (rmb!=null) {
			this.addBehaviour(rmb);
		}
	}
	/**
	 * Return the current repository mirror behaviour.
	 * @return the repository mirror behaviour
	 */
	public RepositoryMirrorBehaviour getRepositoryMirrorBehaviour() {
		if (repositoryMirrorBehaviour==null) {
			CeaConfigModel ceaConfigModel = this.getInternalDataModel().getCeaConfigModel();
			// --- Avoid starting the mirroring if interval was set to 0 ------
			if (ceaConfigModel!=null && ceaConfigModel.getMirrorInterval()!=0) {
				repositoryMirrorBehaviour = new RepositoryMirrorBehaviour(this, PlatformUpdater.getDateOfNextMirrorOrUpdateInterval(-5, ceaConfigModel.getMirrorInterval()));
			}
		}
		return repositoryMirrorBehaviour;
	}
	/**
	 * Sets the repository mirror behaviour.
	 * @param newRepositoryMirrorBehaviour the new repository mirror behaviour
	 */
	public void setRepositoryMirrorBehaviour(RepositoryMirrorBehaviour newRepositoryMirrorBehaviour) {
		if (this.repositoryMirrorBehaviour!=null) {
			this.removeBehaviour(this.repositoryMirrorBehaviour);
		}
		this.repositoryMirrorBehaviour = newRepositoryMirrorBehaviour;
	}
	

	/**
	 * Directly starts the platform update behaviour.
	 * @return the executed platform update behaviour
	 */
	public PlatformUpdateBehaviour startPlatformUpdateBehaviourNow() {
		PlatformUpdateBehaviour pub = new PlatformUpdateBehaviour(this, new Date());
		pub.setEnforceUpdate(true);
		this.setPlatformUpdateBehaviour(pub);
		this.startPlatformUpdateBehaviour();
		return pub;
	}
	/**
	 * Starts a new platform update behaviour.
	 */
	public void startNewPlatformUpdateBehaviour() {
		this.setPlatformUpdateBehaviour(null);
		this.startPlatformUpdateBehaviour();
	}
	/**
	 * Regularly starts a platform update behaviour according to the {@link CeaConfigModel}.
	 * @see CeaConfigModel#getMirrorInterval()
	 */
	public void startPlatformUpdateBehaviour() {
		PlatformUpdateBehaviour pub = this.getPlatformUpdateBehaviour();
		if (pub!=null) {
			this.addBehaviour(pub);
		}
	}
	/**
	 * Return the current platform update behaviour.
	 * @return the repository mirror behaviour
	 */
	public PlatformUpdateBehaviour getPlatformUpdateBehaviour() {
		if (updateBehaviour==null) {
			CeaConfigModel ceaConfigModel = this.getInternalDataModel().getCeaConfigModel();
			// --- Avoid starting the mirroring if interval was set to 0 ------
			if (ceaConfigModel!=null && ceaConfigModel.getMirrorInterval()!=0) {
				updateBehaviour = new PlatformUpdateBehaviour(this, PlatformUpdater.getDateOfNextMirrorOrUpdateInterval(0, ceaConfigModel.getMirrorInterval()));
			}
		}
		return updateBehaviour;
	}
	/**
	 * Sets the platform update behaviour.
	 * @param newPlatformUpdateBehaviour the new platform update behaviour
	 */
	public void setPlatformUpdateBehaviour(PlatformUpdateBehaviour newPlatformUpdateBehaviour) {
		if (this.updateBehaviour!=null) {
			this.removeBehaviour(this.updateBehaviour);
		}
		this.updateBehaviour = newPlatformUpdateBehaviour;
	}
	
	/**
	 * Starts phone book query responder.
	 */
	private void startPhoneBookQueryResponder() {
		if (phoneBookQueryResponder==null) {
			phoneBookQueryResponder = new PhoneBookQueryResponder(this);
			this.addBehaviour(phoneBookQueryResponder);
			this.getMessageReceiveBehaviour().addMessageTemplateToIgnoreList(PhoneBookQueryResponder.getMessageTemplate());
		}
	}
	
	/**
	 * Gets the message receive behaviour.
	 * @return the message receive behaviour
	 */
	private MessageReceiveBehaviour getMessageReceiveBehaviour() {
		if(messageReceiveBehaviour==null) {
			messageReceiveBehaviour = new MessageReceiveBehaviour();
		}
		return messageReceiveBehaviour;
	}
	
}
