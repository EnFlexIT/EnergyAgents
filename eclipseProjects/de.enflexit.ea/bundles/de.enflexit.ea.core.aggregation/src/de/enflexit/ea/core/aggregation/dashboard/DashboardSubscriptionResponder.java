package de.enflexit.ea.core.aggregation.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.helper.DomainCluster;

import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.dashboard.DashboardSubscription.SubscriptionBy;
import de.enflexit.ea.core.aggregation.dashboard.DashboardSubscription.SubscriptionFor;
import de.enflexit.ea.core.dataModel.ontology.DynamicComponentState;
import energy.optionModel.TechnicalSystemStateEvaluation;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionResponder;

/**
 * The Class DashboardSubscriptionResponder.
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class DashboardSubscriptionResponder extends SubscriptionResponder {

	private static final long serialVersionUID = -2349228912000862029L;
	
	private HashMap<DashboardSubscription, Subscription> subscriptionsHashMap;
	
	/**
	 * Instantiates a new DashboardSubscriptionResponder.
	 *
	 * @param agent the agent
	 * @param messageTemplate the message template
	 */
	public DashboardSubscriptionResponder(Agent agent) {
		super(agent, createMessageTemplate());
	}

	/**
	 * Creates the message template.
	 * @return the message template
	 */
	private static MessageTemplate createMessageTemplate() {
		return MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE);
	}
	
	/**
	 * Notify subscribers.
	 * @param aggregations the aggregations
	 */
	public void notifySubscribers(List<AbstractSubNetworkConfiguration> aggregations) {
		
		List<DashboardSubscription> subscriptions = new ArrayList<DashboardSubscription>(this.getSubscriptionsHashMap().keySet());
		for (int i=0; i<aggregations.size(); i++) {
			AbstractSubNetworkConfiguration aggregation = aggregations.get(i);  

			for (int j=0; j<subscriptions.size(); j++) {
				DashboardSubscription subscription = subscriptions.get(j);
				if (subscription.getDomain().equals(aggregation.getDomain())) {
					System.out.println("[" + this.getClass().getSimpleName() + "] Subscription found for " + aggregation.getDomain());
					try {
						ACLMessage notificationMessage = this.prepareNotificationMessage(subscription, aggregation);
						this.getSubscriptionsHashMap().get(subscription).notify(notificationMessage);
						
					} catch (IOException e) {
						System.err.println("[" + this.getClass().getSimpleName() + "] Error creating notification essage for domain " + aggregations.get(i).getDomain());
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
	private ACLMessage prepareNotificationMessage(DashboardSubscription subscriptionSpecifier, AbstractSubNetworkConfiguration subNetworkConfiguration) throws IOException {
		ACLMessage notificationMessage = new ACLMessage(ACLMessage.INFORM);
			
		Vector<NetworkComponent> aggregationComponents = subNetworkConfiguration.getDomainCluster().getNetworkComponents();
		DashboardSubscriptionUpdate dashboardUpdate = new DashboardSubscriptionUpdate();
		dashboardUpdate.setSubscriptionFor(subscriptionSpecifier.getSubscriptionFor());
		
		
		for (int i=0; i<aggregationComponents.size(); i++) {
			NetworkComponent netComp = aggregationComponents.get(i);
			
			if (this.isComponentRelevant(netComp, subscriptionSpecifier)) {
				if (subscriptionSpecifier.getSubscriptionFor() == SubscriptionFor.DOMAIN_DATAMODEL_STATE) {
					NetworkModel aggregationModel = subNetworkConfiguration.getSubNetworkModel();
					DynamicComponentState componentState = this.getStateObjectFromNetworkComponent(netComp, aggregationModel, subscriptionSpecifier.getDomain());
					if (componentState!=null) {
						dashboardUpdate.getUpdateObjects().put(netComp.getId(), componentState);
					}
				} else if (subscriptionSpecifier.getSubscriptionFor() == SubscriptionFor.CURRENT_TSSE) {
					//TODO extract TSSEs
				}
			} 
			
			notificationMessage.setContentObject(dashboardUpdate);
		}
		return notificationMessage;
	}
	
	/**
	 * Gets the state object from the given network component.
	 * @param networkComponent the network component
	 * @param networkModel the network model
	 * @param domain the domain of interest
	 * @return the state object
	 */
	private DynamicComponentState getStateObjectFromNetworkComponent(NetworkComponent networkComponent, NetworkModel networkModel, String domain) {
		DynamicComponentState componentState = null;
		Object dataModelObject = null;
		Object[] dataModelArray = null;
		List<GraphElement> graphElements = networkModel.getGraphElementsFromNetworkComponent(networkComponent);
		if (graphElements.size()==1) {
			// --- Distribution node -> get data model from the node ----------
			GraphNode graphNode = (GraphNode) graphElements.get(0);
			dataModelObject = graphNode.getDataModel();
		} else {
			// --- Edge component -> get the data model from the component ---- 
			dataModelObject = networkComponent.getDataModel();
		}
		
		// --- Handle Object[] or TreeMap based data models -------------------
		if (dataModelObject!=null) {
			if (dataModelObject instanceof TreeMap<?, ?>) {
				@SuppressWarnings("unchecked")
				TreeMap<String, Object[]> dmTreeMap = (TreeMap<String, Object[]>) dataModelObject;
				dataModelArray = dmTreeMap.get(domain);
			} else {
				dataModelArray = (Object[]) dataModelObject;
			}
		}

		// --- Get the state object from the data model -----------------------
		if (dataModelArray!=null) {
			componentState = (DynamicComponentState) dataModelArray[1];
		} else {
			System.err.println("[" + this.getClass().getSimpleName() + "] Couldn't get state data model from network component " + networkComponent.getId());
		}
		
		return componentState;
	}
	
	private TechnicalSystemStateEvaluation getTsseFromAggregation(String componentID, AbstractSubNetworkConfiguration aggregation) {
		TechnicalSystemStateEvaluation tsse = null;
		AbstractAggregationHandler agh = aggregation.getAggregationHandler();
		DomainCluster dc = aggregation.getDomainCluster();
		System.out.println("[" + this.getClass().getSimpleName() + "] Here is a breakpoint for debugging!");
		return tsse;
	}
	
	/**
	 * Checks if the given component is relevant for the subscription.
	 * @param networkComponent the network component
	 * @param compStateSubscription the subscription
	 * @return true, if the component is relevant
	 */
	private boolean isComponentRelevant(NetworkComponent networkComponent, DashboardSubscription compStateSubscription) {
		// --- Subscription based on component types ----------------
		if (compStateSubscription.getSubscriptionBy()==SubscriptionBy.COMPONENT_TYPE && compStateSubscription.getSubscriptionSpecifiers().contains(networkComponent.getType())) {
			return true;
		} else if (compStateSubscription.getSubscriptionBy()==SubscriptionBy.COMPONENT_ID && compStateSubscription.getSubscriptionSpecifiers().contains(networkComponent.getId())){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleSubscription(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleSubscription(ACLMessage subscriptionMessage) throws NotUnderstoodException, RefuseException {

		// --- Let the superclass handle the subscription -----------
		ACLMessage response = super.handleSubscription(subscriptionMessage);
		
		// --- Remember the resulting subscription object -----------
		
		try {
			DashboardSubscription subscriptionSpecifier = (DashboardSubscription) subscriptionMessage.getContentObject();
			Subscription subscription = this.getSubscription(subscriptionMessage);
			
			this.getSubscriptionsHashMap().put(subscriptionSpecifier, subscription);
			
		} catch (UnreadableException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error extracting content object!");
			e.printStackTrace();
		}
		
		return response;
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

}
