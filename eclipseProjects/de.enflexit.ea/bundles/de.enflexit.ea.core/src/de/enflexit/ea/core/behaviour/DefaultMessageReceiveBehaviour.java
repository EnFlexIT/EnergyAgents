package de.enflexit.ea.core.behaviour;

import java.util.ArrayList;
import java.util.Vector;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.dataModel.phonebook.PhoneBookEntry;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Default implementation of a message receive behaviour for {@link AbstractEnergyAgent}s. It allows to 
 * specify a list of templates for messages that should be ignored by this behaviour. All
 * non-ignored messages will be received and passed to the {@link AbstractEnergyAgent}'s 
 * handleIncomingMessage()-method.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class DefaultMessageReceiveBehaviour extends CyclicBehaviour {
	
	private static final long serialVersionUID = 6261065450167363190L;

	private Vector<MessageTemplate> ignoreList;
	private MessageTemplate messageTemplate;

	private boolean debug = false;
	
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
	
	/**
	 * Removes the template with the given index from the ignore list.
	 * @param index the index
	 */
	public void removeTemplateFromIgnoreList(int index) {
		this.getIgnoreList().remove(index);
		// --- Rebuild the template -------------------
		if (this.messageTemplate != null) {
			this.messageTemplate = this.buildMessageTemplate();
		}
	}
	
	/**
	 * Clear the ignore list.
	 */
	public void clearIgnoreList() {
		this.getIgnoreList().clear();
		// --- Rebuild the template -------------------
		if (this.messageTemplate != null) {
			this.messageTemplate = this.buildMessageTemplate();
		}
	}

	/**
	 * Returns the current energy agent.
	 * @return the energy agent
	 */
	private AbstractEnergyAgent getEnergyAgent() {
		return (AbstractEnergyAgent) myAgent;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public final void action() {
		
		boolean invokeEnergyAgentsMessageReceiveMethod = true;
		ACLMessage msg = this.myAgent.receive(this.getMessageTemplate());
		
		if (msg!=null) {
			
			if (this.debug==true) {
				System.out.println(myAgent.getLocalName() + ": Received message with ConversationID " + msg.getConversationId() + " and performative " + ACLMessage.getPerformative(msg.getPerformative()) + " from agent " + msg.getSender().getName());
//				if (msg.getPerformative()==ACLMessage.FAILURE) {
//					System.out.println(msg.getContent());
//				}
			}

			if (msg.getConversationId()!=null) {
				// ------------------------------------------------------------
				// --- Messages with known conversation IDs / actions ---------
				// ------------------------------------------------------------
				if (msg.getConversationId().equals(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS.toString()) && msg.getPerformative()==ACLMessage.PROPAGATE) {
					// --- Immediately start the update behaviour -------------
					this.getEnergyAgent().startPlatformUpdateBehaviourNow();
					invokeEnergyAgentsMessageReceiveMethod = false;
			
				} else if (msg.getConversationId().equals(ConversationID.OPS_FIELD_DATA_REQUEST.toString())) {
					// --- Handle requests for field data ---------------------
					if (msg.getPerformative()==ACLMessage.REQUEST) {
						myAgent.addBehaviour(new HandleFieldDataRequestBehaviour(msg));
					}
				}
			
			}
			
			// --- Invoke default receive method in the energy agent ----------
			if (invokeEnergyAgentsMessageReceiveMethod==true) {
				this.getEnergyAgent().handleIncomingMessage(msg);
			}
			invokeEnergyAgentsMessageReceiveMethod = true;
			
		} else {
			// --- Wait for the next incoming message -------------------------
			this.block();
			
		}
	}


}
