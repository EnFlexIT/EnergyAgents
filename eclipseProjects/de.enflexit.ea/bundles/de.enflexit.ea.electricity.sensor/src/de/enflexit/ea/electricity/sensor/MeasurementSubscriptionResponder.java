package de.enflexit.ea.electricity.sensor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;

public abstract class MeasurementSubscriptionResponder extends SubscriptionResponder {
	
	private static final long serialVersionUID = -2369120728349222744L;

	boolean debug= false;
	
	private SimpleDateFormat sdf;

	
	/**
	 * Gets the sdf.
	 *
	 * @return the sdf
	 */
	public SimpleDateFormat getSdf() {
		if (sdf==null) {
			sdf = new SimpleDateFormat("HH:mm:ss");
		}
		return sdf;
	}
	
	/**
	 * Instantiates a new measurement subscription responder.
	 *
	 * @param agent the agent
	 */
	protected MeasurementSubscriptionResponder(Agent agent) {
        super(agent, getMessageTemplate());
    }
	
	/**
	 * Gets the message template for this responder.
	 * @return the message template
	 */
	public static MessageTemplate getMessageTemplate() {
		MessageTemplate matchConversationID = MessageTemplate.MatchConversationId(GlobalHyGridConstants.CONVERSATION_ID_MEASUREMENT_SUBSCRIPTION);
		MessageTemplate matchProtocol = MessageTemplate.MatchProtocol(FIPA_SUBSCRIBE);
		MessageTemplate matchPerformative = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
		
		MessageTemplate matchAll = MessageTemplate.and(matchConversationID, MessageTemplate.and(matchProtocol, matchPerformative));
		return matchAll;
	}
	
	
	/* (non-Javadoc)
	 * @see jade.proto.SubscriptionResponder#handleSubscription(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage handleSubscription(ACLMessage subscription_msg) {
		
		try {
			if (debug == true) {
				System.out.println("Measurement Responder:Measurement Subscription of Sensor Agent accepted:" + this.myAgent.getName() + " from " +subscription_msg.getSender().getName());
			}
			return super.handleSubscription(subscription_msg);
		} catch (NotUnderstoodException e) {
			e.printStackTrace();
			return null;
		} catch (RefuseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
    /* (non-Javadoc)
     * @see jade.proto.SubscriptionResponder#createSubscription(jade.lang.acl.ACLMessage)
     */
    @Override
	public Subscription createSubscription(ACLMessage subsMsg) {
    	// --- Workaround to enable subscriptions from multiple agents with the same conversation ID
    	// --- Append the local name, as creating subscriptions requires a unique conversation ID
    	String originalConversationID = subsMsg.getConversationId();
    	subsMsg.setConversationId(originalConversationID + "_" + subsMsg.getSender().getLocalName());
    	Subscription subscription = super.createSubscription(subsMsg);
    	// --- Return to the original ID to prevent problems with responses
    	subsMsg.setConversationId(originalConversationID);
		return subscription;
	}

	/**
     * Sending new measurement to subscribers.
     *
     * @param inform the inform message
     */
    public void sendingNewMeasurementToSubscribers(ACLMessage inform) {
      
        // go through every subscription
		@SuppressWarnings("unchecked")
		Vector<Subscription> subs = getSubscriptions();
        for(int i=0; i<subs.size(); i++) {
            subs.elementAt(i).notify(inform);
        	if (debug==true) {
        		long timestamp = ((AbstractSensorAgent) this.myAgent).getEnergyAgentIO().getTime();
    			String timeString = this.getSdf().format(new Date(timestamp));
        		System.out.println(myAgent.getLocalName()+" sent new measurement to " + subs.get(i).getMessage().getSender().getLocalName()  + " at "+ timeString);
        	}
        }
    }
}
