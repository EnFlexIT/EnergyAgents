package de.enflexit.energyAgent.ops.liveMonitoring;

import hygrid.globalDataModel.cea.ConversationID;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

/**
 * This FIPA-Request implementation requests the AID of a LiveMonitoringProxyAgent 
 * from the CEA and, if successful, starts the subscription for live data 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class LiveMonitoringRequestInitiator extends SimpleAchieveREInitiator {

	private static final long serialVersionUID = 7219902902668829999L;
	
	private boolean debug = true;

	/**
	 * Instantiates a new live monitoring proxy request initiator.
	 * @param agent the agent
	 */
	public LiveMonitoringRequestInitiator(LiveMonitoringAgent agent) {
		super(agent, new ACLMessage(ACLMessage.REQUEST));
		System.out.println(myAgent.getLocalName() + ": Starting " + this.getClass().getSimpleName());
	}

	/* (non-Javadoc)
	 * @see jade.proto.SimpleAchieveREInitiator#prepareRequest(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setConversationId(ConversationID.OPS_LIVE_MONITORING_START.toString());
		
		AID ceaAid = ((LiveMonitoringAgent)myAgent).getCeaAid();
		msg.addReceiver(ceaAid);
		
		return msg;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SimpleAchieveREInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage msg) {
		String proxyName = msg.getContent();
		
		if (proxyName!=null) {
			AID proxyAID = this.getLiveMonitoringProxyAgentAID(proxyName);
			
			if (this.debug==true) {
				System.out.println(myAgent.getLocalName() + ": Received an AID for the live monitoring subscription: " + proxyAID);
			}
			
			// --- Prepare aggregation and visualization ------
			myAgent.addBehaviour(new LiveMonitoringAgentSetupBehaviour());
			
			// --- Subscribe for live monitoring updates ------
			((LiveMonitoringAgent)myAgent).startSubscription(proxyAID);
		}
	}
	
	/**
	 * Gets the LiveMonitoringProxyAgent's AID, assuming the MTP address is the same as for the CEA.
	 * @param proxyName the proxy agent's (full) name
	 * @return the live monitoring proxy agent's AID
	 */
	private AID getLiveMonitoringProxyAgentAID(String proxyName) {
		
		AID ceaAid = ((LiveMonitoringAgent)myAgent).getCeaAid();
		String mtpAddress = ceaAid.getAddressesArray()[0];
		
		AID proxyAID = new AID(proxyName, true);
		proxyAID.addAddresses(mtpAddress);
		return proxyAID;
	}

	@Override
	protected void handleFailure(ACLMessage msg) {
		// TODO Auto-generated method stub
		super.handleFailure(msg);
	}
	
	
	
	
}
