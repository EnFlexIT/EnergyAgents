package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import agentgui.core.application.Application;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.globalDataModel.cea.CeaConfigModel;
import jade.core.behaviours.SimpleBehaviour;
import jade.mtp.MTPException;
import jade.wrapper.StaleProxyException;

/**
 * The Class StartMtpBehaviour.
 */
public class StartMtpBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -4943150723404510047L;

	private boolean isExecutedCoreAction;
	private CentralExecutiveAgent cea;
	
	/**
	 * Instantiates a new start mtp behaviour.
	 * @param cea the instance of the CEA
	 */
	public StartMtpBehaviour(CentralExecutiveAgent cea) {
		this.cea = cea;
	}
	
	@Override
	public boolean done() {
		return this.isExecutedCoreAction;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {

		CeaConfigModel ceaConfigModel = this.cea.getInternalDataModel().getCeaConfigModel();
		if (ceaConfigModel!=null) {
			
			this.isExecutedCoreAction = true;
			
			if (ceaConfigModel.isStartSecondMTP()==false) return;

			String mtpAddress = ceaConfigModel.getCompleteMTPAddress();
			try {
				// --- Install the second MTP ---------------------------
				Application.getJadePlatform().getMainContainer().installMTP(mtpAddress, jade.mtp.http.MessageTransportProtocol.class.getName()); 
				
			} catch (MTPException | StaleProxyException e1) {
				System.err.println("[" + this.getClass().getSimpleName() + "] Could not start second MTP-port on " + mtpAddress);
				//e1.printStackTrace();
			}
			
		} else {
			this.block(500);
		}
	}

}
