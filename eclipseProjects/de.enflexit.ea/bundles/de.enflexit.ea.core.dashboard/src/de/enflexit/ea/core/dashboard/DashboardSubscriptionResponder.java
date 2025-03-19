package de.enflexit.ea.core.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.awb.simulation.transaction.DisplayAgentNotification;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractNetworkCalculationStrategy;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.AggregationListener;
import de.enflexit.ea.core.dashboard.DashboardSubscription.SubscriptionBy;
import de.enflexit.ea.core.dashboard.DashboardSubscription.SubscriptionFor;
import de.enflexit.ea.core.dataModel.ontology.DynamicComponentState;
import energy.helper.TechnicalSystemStateHelper;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionResponder;
import jade.proto.states.ReplySender;

/**
 * The Class DashboardSubscriptionResponder.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DashboardSubscriptionResponder extends SubscriptionResponder implements AggregationListener {

	private static final long serialVersionUID = -2349228912000862029L;
	
	private HashMap<DashboardSubscription, Subscription> subscriptionsHashMap;
	
	private AbstractAggregationHandler aggregationHandler;
	
	/**
	 * Instantiates a new DashboardSubscriptionResponder.
	 *
	 * @param agent the agent
	 * @param messageTemplate the message template
	 */
	public DashboardSubscriptionResponder(Agent agent, AbstractAggregationHandler aggregationHandler) {
		super(agent, createMessageTemplate());
		this.aggregationHandler = aggregationHandler;
		this.aggregationHandler.addAggregationListener(this);
	}
	/**
	 * Creates the message template.
	 * @return the message template
	 */
	private static MessageTemplate createMessageTemplate() {
		return MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AggregationListener#networkCalculationDone()
	 */
	@Override
	public void networkCalculationDone() {
		this.notifySubscribers();
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AggregationListener#sendDisplayAgentNotification(agentgui.simulationService.transaction.DisplayAgentNotification)
	 */
	@Override
	public void sendDisplayAgentNotification(DisplayAgentNotification displayNotification) {
		// --- Nothing to do here
	}
	
	/**
	 * Notify subscribers.
	 * @param aggregations the aggregations
	 */
	public void notifySubscribers() {
		
		List<DashboardSubscription> subscriptions = new ArrayList<DashboardSubscription>(this.getSubscriptionsHashMap().keySet());
		for (int i=0; i<this.getAggregationHandler().getSubNetworkConfigurations().size(); i++) {
			
			AbstractSubNetworkConfiguration aggregation = this.getAggregationHandler().getSubNetworkConfigurations().get(i);

			for (int j=0; j<subscriptions.size(); j++) {
				DashboardSubscription subscription = subscriptions.get(j);
				if (subscription.getDomain().equals(aggregation.getDomain())) {
					try {
						ACLMessage notificationMessage = this.prepareNotificationMessage(subscription, aggregation);
						ReplySender.adjustReply(myAgent, notificationMessage, this.getSubscriptionsHashMap().get(subscription).getMessage());
						myAgent.send(notificationMessage);
//						this.getSubscriptionsHashMap().get(subscription).notify(notificationMessage);
						
					} catch (IOException e) {
						System.err.println("[" + this.getClass().getSimpleName() + "] Error creating notification essage for domain " + aggregation.getDomain());
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Prepare notification message.
	 * @param subNetworkConfiguration the sub network configuration
	 * @return the notification message
	 * @throws IOException Signals that setting the message's content object failed.
	 */
	private ACLMessage prepareNotificationMessage(DashboardSubscription dashboardSubscription, AbstractSubNetworkConfiguration subNetworkConfiguration) throws IOException {
		ACLMessage notificationMessage = new ACLMessage(ACLMessage.INFORM);
			
		DashboardUpdate dashboardUpdate = new DashboardUpdate();
		dashboardUpdate.setSubscriptionFor(dashboardSubscription.getSubscriptionFor());
		dashboardUpdate.setTimestamp(this.getAggregationHandler().getEvaluationEndTime());
		
		if (dashboardSubscription.getSubscriptionFor()==SubscriptionFor.CURRENT_TSSE) {
			HashMap<String, TechnicalSystemStateEvaluation> lastTSSEs = subNetworkConfiguration.getAggregationHandler().getLastTechnicalSystemStatesFromScheduleController();
			for (int i=0; i<dashboardSubscription.getSubscriptionSpecifiers().size(); i++) {
				String componentID = dashboardSubscription.getSubscriptionSpecifiers().get(i);
				TechnicalSystemStateEvaluation tsse = lastTSSEs.get(componentID);
				if (tsse!=null) {
					dashboardUpdate.getUpdateObjects().put(componentID, TechnicalSystemStateHelper.copyTechnicalSystemStateEvaluationWithoutParent(tsse));
				} else {
					System.err.println("[" + this.getClass().getSimpleName() + "] No TSSE found for network component " + componentID);
				}
			}
		} else if (dashboardSubscription.getSubscriptionFor() == SubscriptionFor.DOMAIN_DATAMODEL_STATE) {
			for (int i=0; i<dashboardSubscription.getSubscriptionSpecifiers().size(); i++) {
				
				String componentID = dashboardSubscription.getSubscriptionSpecifiers().get(i);
				DynamicComponentState componentState = this.getStateObjectForNetworkComponent(componentID, subNetworkConfiguration);
				if (componentState!=null) {
					dashboardUpdate.getUpdateObjects().put(componentID, componentState);
				}
			}
		}
		
		notificationMessage.setContentObject(dashboardUpdate);
		return notificationMessage;
	}
	
	/**
	 * Gets the calculated state object for a network component from the aggregation's {@link AbstractNetworkCalculationStrategy}.
	 * @param networkModel the network model
	 * @param componentID the component ID
	 * @param subNetworkConfiguration the sub network configuration
	 * @return the state object for network component
	 */
	private DynamicComponentState getStateObjectForNetworkComponent(String componentID, AbstractSubNetworkConfiguration subNetworkConfiguration) {
		DynamicComponentState componentState = null;
		// --- Get the graph elements for the specified network component -----
		List<GraphElement> graphElements = subNetworkConfiguration.getSubNetworkModel().getGraphElementsFromNetworkComponent(subNetworkConfiguration.getSubNetworkModel().getNetworkComponent(componentID));
		if (graphElements.size()==1) {
			// --- Node component -> use the graph node ID -------------------- 
			GraphNode graphNode = (GraphNode) graphElements.get(0);
			componentState = subNetworkConfiguration.getNetworkCalculationStrategy().getNodeStates().get(graphNode.getId());
		} else {
			// --- Edge component -> use the component ID directly ------------
			componentState = subNetworkConfiguration.getNetworkCalculationStrategy().getEdgeStates().get(componentID);
		}
		return componentState;
	}
	
	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleSubscription(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleSubscription(ACLMessage subscriptionMessage) throws NotUnderstoodException, RefuseException {

		// --- Let the superclass handle the subscription -----------
		ACLMessage response = super.handleSubscription(subscriptionMessage);
		
		try {
			DashboardSubscription dashboardSubscription = (DashboardSubscription) subscriptionMessage.getContentObject();
			// --- Convert type-based subscriptions to IDs ----------
			if (dashboardSubscription.getSubscriptionBy()==SubscriptionBy.COMPONENT_TYPE) {
				ArrayList<String> componentIDs = new ArrayList<>();
				for (int i=0; i<dashboardSubscription.getSubscriptionSpecifiers().size(); i++) {
					String componentType = dashboardSubscription.getSubscriptionSpecifiers().get(i);
					componentIDs.addAll(this.getComponentIDsByType(componentType));
				}
				dashboardSubscription.setSubscriptionBy(SubscriptionBy.COMPONENT_ID);
				dashboardSubscription.setSubscriptionSpecifiers(componentIDs);
			}
			
			// --- Remember the corresponding JADE subscription -----
			Subscription jadeSubscription = this.getSubscription(subscriptionMessage);
			this.getSubscriptionsHashMap().put(dashboardSubscription, jadeSubscription);
			
		} catch (UnreadableException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error extracting content object!");
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * Gets the IDs of all network components of the specified type.
	 * @param componentType the component type
	 * @return the component IDs
	 */
	private List<String> getComponentIDsByType(String componentType){
		List<String> componentIDs = new ArrayList<String>();
		NetworkModel networkModel = this.getAggregationHandler().getNetworkModel();
		List<NetworkComponent> networkComponents = new ArrayList<>(networkModel.getNetworkComponents().values());
		for (int i=0; i<networkComponents.size(); i++) {
			NetworkComponent networkComponent = networkComponents.get(i);
			if (networkComponent.getType().equals(componentType)) {
				componentIDs.add(networkComponent.getId());
			}
		}
		return componentIDs;
	}
	
	/**
	 * Gets the subscriptions by domain.
	 * @return the subscriptions by domain
	 */
	private HashMap<DashboardSubscription, Subscription> getSubscriptionsHashMap() {
		if (subscriptionsHashMap==null) {
			subscriptionsHashMap = new HashMap<DashboardSubscription, Subscription>();
		}
		return subscriptionsHashMap;
	}
	
	/**
	 * Gets the aggregation handler.
	 * @return the aggregation handler
	 */
	private AbstractAggregationHandler getAggregationHandler() {
		return aggregationHandler;
	}

}
