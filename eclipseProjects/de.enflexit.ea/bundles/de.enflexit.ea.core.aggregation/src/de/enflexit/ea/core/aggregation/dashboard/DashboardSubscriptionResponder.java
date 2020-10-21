package de.enflexit.ea.core.aggregation.dashboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.aggregation.dashboard.DashboardSubscription.SubscriptionBy;
import de.enflexit.ea.core.aggregation.dashboard.DashboardSubscription.SubscriptionFor;
import de.enflexit.ea.core.dataModel.ontology.DynamicComponentState;
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
	
	private HashMap<String, SubscriptionDetails> subscriptionsByDomain;
	
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
		for (int i=0; i<aggregations.size(); i++) {
			SubscriptionDetails subscriptionDetails = this.getSubscriptionsByDomain().get(aggregations.get(i).getDomain());
			if  (subscriptionDetails!=null) {
				System.out.println("[" + this.getClass().getSimpleName() + "] Subscription found for " + aggregations.get(i).getDomain());
				
				try {
					ACLMessage notificationMessage = this.prepareNotificationMessage(subscriptionDetails.getSubscriptionSpecifier(), aggregations.get(i));
					subscriptionDetails.getSubscription().notify(notificationMessage);
				} catch (IOException e) {
					System.err.println("[" + this.getClass().getSimpleName() + "] Error creating notification essage for domain " + aggregations.get(i).getDomain());
					e.printStackTrace();
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
		
		if (subscriptionSpecifier.getSubscriptionFor() == SubscriptionFor.DOMAIN_DATAMODEL_STATE) {
			
			Vector<NetworkComponent> clusterComponents = subNetworkConfiguration.getDomainCluster().getNetworkComponents();
			NetworkComponentStateUpdate stateUpdate = new NetworkComponentStateUpdate();
			
			NetworkModel aggregationModel = subNetworkConfiguration.getSubNetworkModel();
			
			for (int i=0; i<clusterComponents.size(); i++) {
				NetworkComponent netComp = clusterComponents.get(i);
				
				if (this.isComponentRelevant(netComp, subscriptionSpecifier)) {
					DynamicComponentState componentState = this.getStateObjectFromNetworkComponent(netComp, aggregationModel, subscriptionSpecifier.getDomain());
					if (componentState!=null) {
						stateUpdate.getComponentStates().put(netComp.getId(), componentState);
					}
				}
				
			}
			
			notificationMessage.setContentObject(stateUpdate);
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
			
			SubscriptionDetails subscriptionDetails = new SubscriptionDetails();
			subscriptionDetails.setSubscriptionSpecifier(subscriptionSpecifier);
			subscriptionDetails.setSubscription(subscription);
			
			this.getSubscriptionsByDomain().put(subscriptionSpecifier.getDomain(), subscriptionDetails);
			
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
	private HashMap<String, SubscriptionDetails> getSubscriptionsByDomain() {
		if (subscriptionsByDomain==null) {
			subscriptionsByDomain = new HashMap<String, SubscriptionDetails>();
		}
		return subscriptionsByDomain;
	}
	
	/**
	 * Internal class for managing subscription details
	 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
	 */
	private class SubscriptionDetails {
		private DashboardSubscription subscriptionSpecifier;
		private Subscription subscription;
		
		/**
		 * Gets the subscription specifier.
		 * @return the subscription specifier
		 */
		public DashboardSubscription getSubscriptionSpecifier() {
			return subscriptionSpecifier;
		}
		
		/**
		 * Sets the subscription specifier.
		 * @param subscriptionSpecifier the new subscription specifier
		 */
		public void setSubscriptionSpecifier(DashboardSubscription subscriptionSpecifier) {
			this.subscriptionSpecifier = subscriptionSpecifier;
		}
		
		/**
		 * Gets the subscription.
		 * @return the subscription
		 */
		public Subscription getSubscription() {
			return subscription;
		}
		
		/**
		 * Sets the subscription.
		 * @param subscription the new subscription
		 */
		public void setSubscription(Subscription subscription) {
			this.subscription = subscription;
		}
	}

}
