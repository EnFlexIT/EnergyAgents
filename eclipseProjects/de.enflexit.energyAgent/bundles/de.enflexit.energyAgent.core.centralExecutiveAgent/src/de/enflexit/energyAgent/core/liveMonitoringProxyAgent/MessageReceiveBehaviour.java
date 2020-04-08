package de.enflexit.energyAgent.core.liveMonitoringProxyAgent;

import java.util.Vector;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * MessageReceiveBehaviour for the {@link LiveMonitoringProxyAgent} - mainly added to see returned failure messages 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class MessageReceiveBehaviour extends CyclicBehaviour{

	private static final long serialVersionUID = 3030508839264666425L;
	
	private Vector<MessageTemplate> ignoreList;
	private MessageTemplate messageTemplate;

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(this.getMessageTemplate());
		if (msg!=null) {
			
			// --- Process non-protocol-related messages here -------
			
			if (msg.getPerformative()==ACLMessage.FAILURE) {
				System.out.println(myAgent.getLocalName() + ": Received failure message with ConversationID " + msg.getConversationId() + " from agent " + msg.getSender().getName());
			}
			
		} else {
			block();
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
	
	/**
	 * Builds the message template as the negation of the or-conjunction of all templates from the ignore list.
	 * @return the message template
	 */
	private MessageTemplate buildMessageTemplate() {
		
		int numOfTemplates = this.getIgnoreList().size();
		if (numOfTemplates==0) {
			// --------------------------------------------------------------------------
			// --- Empty vector - no template required ----------------------------------
			// --------------------------------------------------------------------------
			return null;
			
		} else {
			// --------------------------------------------------------------------------
			// --- Build a message template that ignores all templates in the vector ----
			// --------------------------------------------------------------------------
			
			// --- Initialize with the first template -----------------------------------
			MessageTemplate ignore = getIgnoreList().get(0);
			
			// --- Sequentially add or-conjunctions for all subsequent templates --------
			for (int i=1; i<numOfTemplates; i++) {
				ignore = MessageTemplate.or(ignore, this.getIgnoreList().get(i));
			}
			
			// --- Return the negation of the previously constructed template -----------
			return MessageTemplate.not(ignore);
		}
	}
	
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
	 * Sets the ignore list.
	 * @param ignoreList the new ignore list
	 */
	public void setIgnoreList(Vector<MessageTemplate> ignoreList) {
		this.ignoreList = ignoreList;
		// --- Rebuild the template -------------------
		if (this.messageTemplate != null) {
			this.messageTemplate = this.buildMessageTemplate();
		}
	}

	/**
	 * Adds a message template to the ignore list, rebuilds the template if necessary.
	 * @param template the template
	 */
	public void addMessageTemplateToIgnoreList(MessageTemplate template) {
		this.getIgnoreList().addElement(template);
		
		// --- Rebuild the template -------------------
		if (this.messageTemplate != null) {
			this.messageTemplate = this.buildMessageTemplate();
		}
	}
	
	/**
	 * Removes the template from the ignore list.
	 * @param template the template
	 */
	public void removeTemplateFromIgnoreList(MessageTemplate template) {
		this.getIgnoreList().remove(template);
		// --- Rebuild the template -------------------
		if (this.messageTemplate != null) {
			this.messageTemplate = this.buildMessageTemplate();
		}
	}

}
