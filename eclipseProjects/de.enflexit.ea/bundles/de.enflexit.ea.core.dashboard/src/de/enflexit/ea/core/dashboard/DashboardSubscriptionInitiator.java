package de.enflexit.ea.core.dashboard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

/**
 * The Class DashboardSubscriptionInitiator.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DashboardSubscriptionInitiator extends SubscriptionInitiator {

	private static final long serialVersionUID = -3362886788353990119L;
	
	private DateFormat dateFormat;
	
	private DashboardController dashboardController;
	
	/**
	 * Instantiates a new DashboardSubscriptionInitiator.
	 *
	 * @param agent the agent
	 * @param aclMessage the initial ACL message
	 */
	public DashboardSubscriptionInitiator(Agent agent, ACLMessage aclMessage, DashboardController dashboardController) {
		super(agent, aclMessage);
		this.dashboardController = dashboardController;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage notificationMessage) {
		try {
			Object contentObject = notificationMessage.getContentObject();
			if (contentObject!=null && contentObject instanceof DashboardUpdate) {
				// --- Pass the update to the domain-specific dashboard controller ------
				DashboardUpdate dashboardUpdate = (DashboardUpdate) contentObject;
				this.dashboardController.processDashoardUpdate(dashboardUpdate);
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] Wrong content object in dashboard update message!");
			}
			//TODO Update dashboard widgets
		} catch (UnreadableException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error extracting content object!");
			e.printStackTrace();
		}
	}
	
	public DateFormat getDateFormat() {
		if (dateFormat==null) {
			dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		}
		return dateFormat;
	}
	
	
	
}
