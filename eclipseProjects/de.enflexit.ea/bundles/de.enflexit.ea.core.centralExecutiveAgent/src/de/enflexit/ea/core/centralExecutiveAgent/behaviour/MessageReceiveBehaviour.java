package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import java.util.Vector;

import de.enflexit.ea.core.dataModel.cea.ConversationID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The MessageReceiveBehaviour of the CentralExecutiveAgent.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class MessageReceiveBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = -4238911503040211537L;
	
	public static final String FIELD_AGENT_MESSAGE_DISPLAY_AGENT_NAME = "Field-Agent-Message-DisplayAgentName";
	public static final String FIELD_AGENT_MESSAGE_DISPLAY_AGENT_ADDRESSES = "Field-Agent-Message-DisplayAgentAddresses";
	
	private Vector<MessageTemplate> ignoreList;
	private MessageTemplate messageTemplate;
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		ACLMessage msg = this.myAgent.receive(this.getMessageTemplate());
		if (msg!=null) {
			// ------------------------------------------------------
			// --- Check if we have a display agent message ---------
			if (isDisplayAgentMessage(msg)==true) {
				this.myAgent.addBehaviour(new OpsFieldAgentAnswerBehaviour(msg));
				
			} else {
				// --------------------------------------------------
				// --- Handle messages by conversation ID -----------
				if (msg.getConversationId()!=null) {
					
					if (msg.getConversationId().equals(ConversationID.PROXY_REGISTRATION.toString()) && msg.getPerformative()==ACLMessage.REQUEST){
						this.myAgent.addBehaviour(new ProxyRegistrationBehaviour(msg));
						
					} else if (msg.getConversationId().equals(ConversationID.NETWORK_MODEL_REQUEST.toString())) {
						this.myAgent.addBehaviour(new EnvironmentModelRequestBehaviour(msg));
						
					} else if (msg.getConversationId().equals(ConversationID.OPS_CONNECTING_REQUEST.toString()) && msg.getPerformative()==ACLMessage.REQUEST) {
						this.myAgent.addBehaviour(new OpsConnectingRequestBehaviour(msg));
						
					} else if (msg.getConversationId().equals(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS.toString()) && msg.getPerformative()==ACLMessage.PROPAGATE) {
						this.myAgent.addBehaviour(new UpdatePropagationBehaviour(msg));
						
					} else if (msg.getConversationId().equals(ConversationID.OPS_FIELD_DATA_REQUEST.toString())  && msg.getPerformative()==ACLMessage.REQUEST) {
						this.myAgent.addBehaviour(new StartDatabaseProxyAgentBehaviour(msg));
					
					} else if (msg.getConversationId().equals(ConversationID.OPS_FIELD_AGENT_MESSAGE.toString()) && msg.getPerformative()==ACLMessage.PROPAGATE) {
						this.myAgent.addBehaviour(new OpsFieldAgentMessageBehaviour(msg));
						
					} else {
						System.out.println("[" + this.myAgent.getClass().getSimpleName() + "] Got unknown message with conversation ID '" + msg.getConversationId().toString() + "' and performative '"+ ACLMessage.getPerformative(msg.getPerformative()) + "' from " + msg.getSender().getName() + "!");
					}
				
				} else {
					// --------------------------------------------------
					// --- Handle messages without conversation ID ------
				}
				
			}
			
		} else {
			// --- wait for the next incoming message ---------------
			this.block();
		}
	}
	
	/**
	 * Checks if the specified message is a display agent message.
	 *
	 * @param msg the received ACL
	 * @return true, if is display agent message
	 */
	public static boolean isDisplayAgentMessage(ACLMessage msg) {
		if (msg!=null) {
			String displayAgentName = msg.getUserDefinedParameter(FIELD_AGENT_MESSAGE_DISPLAY_AGENT_NAME);
			String displayAgentAddresses = msg.getUserDefinedParameter(FIELD_AGENT_MESSAGE_DISPLAY_AGENT_ADDRESSES);; 
			if (displayAgentName!=null && displayAgentAddresses!=null) {
				return true;
			}
		}
		return false;
	}
	
	// --------------------------------------------------------------
	// --- The message template handling ---------------------------
	// --------------------------------------------------------------
	/**
	 * Gets the list of ignored message templates.
	 * @return the ignore list
	 */
	public Vector<MessageTemplate> getIgnoreList() {
		if (ignoreList == null) {
			ignoreList = new Vector<MessageTemplate>();
		}
		return ignoreList;
	}
	/**
	 * Adds a message template to the ignore list, rebuilds the template if necessary.
	 * @param template the template
	 */
	public void addMessageTemplateToIgnoreList(MessageTemplate template) {

		this.getIgnoreList().addElement(template);
		
		// --- Rebuild the template -------------------
		if (this.messageTemplate!=null) {
			this.messageTemplate=this.buildMessageTemplate();
		}
	}
	/**
	 * Builds the message template as the negation of the or-conjunction of all templates from the ignore list.
	 * @return the message template
	 */
	private MessageTemplate buildMessageTemplate() {
		
		int numOfTemplates = this.getIgnoreList().size();
		if (numOfTemplates==0) {
			// --- Empty vector - no template required ----------------------------------
			return null;
			
		} else {
			// --- Build a message template that ignores all templates in the vector ----
			
			// --- Sequentially add or-conjunctions for all subsequent templates --------
			MessageTemplate ignore = getIgnoreList().get(0);
			for (int i=1; i<numOfTemplates; i++) {
				ignore = MessageTemplate.or(ignore, this.getIgnoreList().get(i));
			}
			
			// --- Return the negation of the previously constructed template -----------
			return MessageTemplate.not(ignore);
		}
	}
	/**
	 * Gets the message template.
	 * @return the message template
	 */
	private MessageTemplate getMessageTemplate() {
		if (messageTemplate==null && this.getIgnoreList().size()>0) {
			messageTemplate = this.buildMessageTemplate();
		}
		return messageTemplate;
	}
	
	
}