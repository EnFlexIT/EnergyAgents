package de.enflexit.energyAgent.ops.agent.behaviour;

import de.enflexit.energyAgent.ops.agent.CeaConnectorAgent;
import hygrid.globalDataModel.cea.ConversationID;
import jade.core.behaviours.CyclicBehaviour;

/**
 * The Class O2ABeahaviour manages the incoming objects from the O2A queue.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class O2ABehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 567104247289648690L;

	private CeaConnectorAgent ceaConnectorAgent;
	
	/**
	 * Instantiates a new O2A behaviour.
	 * @param ceaConnectorAgent the {@link CeaConnectorAgent}
	 */
	public O2ABehaviour(CeaConnectorAgent ceaConnectorAgent) {
		this.ceaConnectorAgent = ceaConnectorAgent;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		Object appNoteObject = this.myAgent.getO2AObject();
		if (appNoteObject!=null) {
			// --- Check for a specific ConversationID of the CEA ------------- 
			if (appNoteObject instanceof ConversationID) {

				// --- Case separation ConversationID -------------------------
				ConversationID convID = (ConversationID) appNoteObject;
				switch (convID) {
				case OPS_CONNECTING_REQUEST:
					this.ceaConnectorAgent.addBehaviour(new CeaConnectingBehaviour(this.ceaConnectorAgent));
					break;

				case OPS_UPDATE_ON_SITE_INSTALLATIONS:
					this.ceaConnectorAgent.addBehaviour(new UpdateFieldInstallationBehaviour(this.ceaConnectorAgent));
					break;
					
				case OPS_LIVE_MONITORING_START:
					this.ceaConnectorAgent.addBehaviour(new LiveMonitoringInitiatorBehaviour(this.ceaConnectorAgent));
					break;
					
				case OPS_LIVE_MONITORING_STOP:
					this.ceaConnectorAgent.addBehaviour(new LiveMonitoringTerminationBehaviour());
					break;
					
				default:
					System.out.println("=> ToDo: Received ConversationID command: " + appNoteObject.toString() + " (" + appNoteObject.getClass().getName() + ")");
					break;
				}
				
			}
			
		} else {
			this.block();
		}
	}


}
