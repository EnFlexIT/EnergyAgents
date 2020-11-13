package de.enflexit.ea.core.dashboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.enflexit.common.ServiceFinder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

/**
 * The agent controlling the Dashboard
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DashboardAgent extends Agent {

	private static final long serialVersionUID = -6098343163112630183L;
	
	public static final String CONVERSATION_ID_BASE = "DashboardSubscription";
	
	private HashMap<String, DashboardController> dashboardControllers;
	
	private JFrame dashboardVisualizationFrame;
	private JTabbedPane dashboardsTabbedPane;
	
	private int subscriptionCounter = 0;

	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		
		Object[] startArgs = this.getArguments();
		
		if (startArgs!=null&&startArgs.length>0) {
			//TODO process start arguments
		}
		
		List<DashboardService> serviceImplementations = ServiceFinder.findServices(DashboardService.class);
		
		if (serviceImplementations.size()==0) {
			// --- No dashboars service implementations found, terminate ------
			System.out.println("[" + this.getClass().getSimpleName() + "] No DashboardService-implementations found, terminating...");
			this.addBehaviour(new OneShotBehaviour() {
				
				private static final long serialVersionUID = -4511856370792090576L;

				@Override
				public void action() {
					this.myAgent.doDelete();
				}
			});
			
		} else {
			
			// --- Prepare the aggregation-specific dashboards ----------------
			System.out.println("[" + this.getClass().getSimpleName() + "] " + serviceImplementations.size() + " DashboardService-implementations found");
			
			for (int i=0; i<serviceImplementations.size(); i++) {
				this.prepareAggregationDashboard(serviceImplementations.get(i));
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					DashboardAgent.this.getDashboardVisualizationFrame().setVisible(true);
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected void takeDown() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				DashboardAgent.this.getDashboardVisualizationFrame().dispose();
			}
		});
	}

	/**
	 * Prepare aggregation dashboard.
	 * @param dashboardService the dashboard service
	 */
	private void prepareAggregationDashboard(DashboardService dashboardService) {
		DashboardController dashboardController = dashboardService.getDashboardController();
		this.getDashboardControllers().put(dashboardController.getDomain(), dashboardController);
		this.getDashboardsTabbedPane().add(dashboardController.getDomain(), dashboardController.getDashboardPanel());
		
		try {
			for (DashboardSubscription subscription : dashboardController.getDashboardSubscriptions()) {
				ACLMessage subscriptionMessage = this.createSubscriptionMessage(subscription);
				this.addBehaviour(new DashboardSubscriptionInitiator(this, subscriptionMessage, dashboardController));
				subscriptionCounter++;
			}
		} catch (IOException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Unable to create subscripption message, setting the content object failed!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the subscription message.
	 * @param domain the domain
	 * @return the ACL message
	 * @throws IOException Setting the content object failed
	 */
	private ACLMessage createSubscriptionMessage(DashboardSubscription dashboardSubscription) throws IOException {
		ACLMessage initialMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
		initialMessage.addReceiver(this.getResponderAID());
		initialMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		initialMessage.setContentObject(dashboardSubscription);
		
		// --- Create unique conversation ID to allow multiple subscriptions ------------
		initialMessage.setConversationId(CONVERSATION_ID_BASE + "." + (this.subscriptionCounter+1));
		
		return initialMessage;
	}
	
	/**
	 * Gets the responder AID.
	 * @return the responder AID
	 */
	private AID getResponderAID() {
		// --- Assuming simulation manager with the default name on the same platform
		//TODO implement for remote SIMa and OPS
		AID responderAID = new AID("SiMa", false);
		return responderAID;
	}
	
	/**
	 * Gets the dashboard controllers.
	 * @return the dashboard controllers
	 */
	private HashMap<String, DashboardController> getDashboardControllers() {
		if (dashboardControllers==null) {
			dashboardControllers = new HashMap<String, DashboardController>();
		}
		return dashboardControllers;
	}

	/**
	 * Gets the dashboard visualization frame.
	 * @return the dashboard visualization frame
	 */
	private JFrame getDashboardVisualizationFrame() {
		if (dashboardVisualizationFrame==null) {
			dashboardVisualizationFrame = new JFrame("AWB Dashboard");
			dashboardVisualizationFrame.setContentPane(this.getDashboardsTabbedPane());
			dashboardVisualizationFrame.setSize(1024, 550);
			dashboardVisualizationFrame.setLocationRelativeTo(null);
			dashboardVisualizationFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		}
		return dashboardVisualizationFrame;
	}

	/**
	 * Gets the dashboards tabbed pane.
	 * @return the dashboards tabbed pane
	 */
	private JTabbedPane getDashboardsTabbedPane() {
		if (dashboardsTabbedPane==null) {
			dashboardsTabbedPane = new JTabbedPane();
		}
		return dashboardsTabbedPane;
	}
	
	

}
