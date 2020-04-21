package de.enflexit.ea.core.ops.agent;

import java.io.Serializable;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.core.application.Application;
import agentgui.simulationService.environment.EnvironmentModel;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.cea.CeaConfigModel;
import de.enflexit.ea.core.dataModel.deployment.AgentSpecifier;
import jade.core.AID;
import jade.wrapper.AgentController;

/**
 * The Class CeaConnectorInternalDataModel.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class CeaConnectorInternalDataModel implements Serializable {

	private static final long serialVersionUID = 5020295051309425320L;
	
	private CeaConnectorAgent ceaConnectorAgent;
	
	private NetworkModel networkModel;
	private HyGridAbstractEnvironmentModel hygridAbstractEnvironmentModel;
	private CeaConfigModel ceaConfigModel;
	
	private AgentController liveMonitoringAgentController;
	
	/**
	 * Instantiates a new CeaConnectorInternalDataModel.
	 * @param ceaConnectorAgent the cea connector agent
	 */
	public CeaConnectorInternalDataModel(CeaConnectorAgent ceaConnectorAgent) {
		this.ceaConnectorAgent = ceaConnectorAgent;
	}

	/**
	 * Returns the current network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		if (networkModel==null) {
			EnvironmentModel environmentModel = Application.getProjectFocused().getEnvironmentController().getEnvironmentModel();
			if (environmentModel!=null) {
				networkModel = (NetworkModel) environmentModel.getDisplayEnvironment();
			}
		}
		return networkModel;
	}
	/**
	 * Returns the HyGrid abstract environment model.
	 * @return the HyGrid abstract environment model
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		if (hygridAbstractEnvironmentModel==null) {
			// --- Initialize network model and component -------------------------------
			EnvironmentModel environmentModel = Application.getProjectFocused().getEnvironmentController().getEnvironmentModel();
			if (environmentModel!=null) {
				// --- Set the time model type according to the environment model -------
				hygridAbstractEnvironmentModel = (HyGridAbstractEnvironmentModel) environmentModel.getAbstractEnvironment();
				hygridAbstractEnvironmentModel.setTimeModelType(environmentModel.getTimeModel());
			}
		}
		return hygridAbstractEnvironmentModel;
	}
	/**
	 * Returns the CEA configuration model.
	 * @return the {@link CeaConfigModel}
	 */
	public CeaConfigModel getCeaConfigModel() {
		if (ceaConfigModel==null) {
			Vector<NetworkComponent> netCompVector = this.getNetworkModel().getNetworkComponentVectorSorted();
			for (int i = 0; i < netCompVector.size(); i++) {
				NetworkComponent netComp = netCompVector.get(i);
				String agentClassName = this.getNetworkModel().getAgentClassName(netComp);
				if (agentClassName.equals(CentralExecutiveAgent.class.getName() )) {
					// --- Found the required NetworkComponent ------
					if (netComp.getDataModel() instanceof CeaConfigModel) {
						ceaConfigModel = (CeaConfigModel) netComp.getDataModel(); 
					}
					break;
				}
			}
		}
		return ceaConfigModel;
	}
	/**
	 * Gets the AID of the CEA from deployment settings.
	 * @return the aid of CEA from deployment settings
	 */
	private AID getAidOfCeaFromDeploymentSettings() {
		AID aid = null;
		if (this.getHyGridAbstractEnvironmentModel()!=null) {
			AgentSpecifier ceaSpecifier = this.getHyGridAbstractEnvironmentModel().getDeploymentSettingsModel().getCentralAgentSpecifier();
			aid = ceaSpecifier.getAID();
		}
		return aid;
	}
	/**
	 * Gets the aid of CEA from {@link CeaConfigModel}.
	 * @return the aid of CEA from cea configuration model
	 */
	private AID getAidOfCeaFromCeaConfigModel() {

		AID aid = null;
		AID aidWork = (AID) this.getAidOfCeaFromDeploymentSettings();
		if (this.getCeaConfigModel()!=null && this.getCeaConfigModel().isStartSecondMTP()==true && aidWork!=null) {

			aidWork = (AID)aidWork.clone();
			aidWork.clearAllAddresses();
			aidWork.clearAllResolvers();
			
			String mtpProtocol = this.getCeaConfigModel().getMtpProtocol().toString().toLowerCase();
			String mtpUrl = this.getCeaConfigModel().getUrlOrIp();
			int mtpPort = this.getCeaConfigModel().getMtpPort();
			String ceaMTPAddress = mtpProtocol + "://" + mtpUrl + ":" + mtpPort + "/acc";
			aidWork.addAddresses(ceaMTPAddress);

			aid = aidWork;
		}
		return aid;
	}
	/**
	 * Gets the aid of CEA as a local agent.
	 * @return the aid of CEA as local agent
	 */
	private AID getAidOfCeaAsLocalAgent() {
		AID aid = null;
		if (this.getHyGridAbstractEnvironmentModel()!=null) {
			AgentSpecifier ceaSpecifier = this.getHyGridAbstractEnvironmentModel().getDeploymentSettingsModel().getCentralAgentSpecifier();
			aid = new AID(ceaSpecifier.getAgentName(), AID.ISLOCALNAME);
		}
		return aid;
	}
	
	/**
	 * Returns the vector of possible AIDs of the CEA .
	 * @return the central agent AID vector
	 */
	public Vector<AID> getCentralAgentAIDVector() {
		
		Vector<AID> ceaVector = new Vector<>();

		// --- 1. Get the possible local AID ---------------------------------- 
		AID aidLocal = this.getAidOfCeaAsLocalAgent();
		if (aidLocal!=null) {
			ceaVector.add(aidLocal);
		}

		// --- 2. Take the alternative MTP from the CEA settings --------------
		AID aidCeaConfigModel = this.getAidOfCeaFromCeaConfigModel();
		if (aidCeaConfigModel!=null) {
			ceaVector.add(aidCeaConfigModel);
		}
		
		// --- 3. Take the address from the DeploymentSettings ---------------- 
		AID aidCeaDeployment = this.getAidOfCeaFromDeploymentSettings(); 
		if (aidCeaDeployment!=null) {
			ceaVector.add(aidCeaDeployment);
		}
		
		return ceaVector;
	}

	/**
	 * @return the liveMonitoringAgentController
	 */
	public AgentController getLiveMonitoringAgentController() {
		return liveMonitoringAgentController;
	}

	/**
	 * @param liveMonitoringAgentController the liveMonitoringAgentController to set
	 */
	public void setLiveMonitoringAgentController(AgentController liveMonitoringAgentController) {
		this.liveMonitoringAgentController = liveMonitoringAgentController;
	}

}
