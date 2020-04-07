package de.enflexit.energyAgent.core.behaviour;

import java.util.ArrayList;
import java.util.Vector;

import de.enflexit.energyAgent.core.AbstractEnergyAgent;
import hygrid.globalDataModel.cea.ConversationID;
import hygrid.globalDataModel.phonebook.PhoneBookEntry;
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
				if (msg.getConversationId().equals(ConversationID.PHONEBOOK_QUERY_SIMPLE.toString())) {
					// --- Handle simple phone book query replies -------------
					this.handleSimplePhoneBookQueryReply(msg);
					
				} else if (msg.getConversationId().equals(ConversationID.PHONEBOOK_QUERY_COMPLEX.toString())) {
					
					// --- Handle complex phone book query replies ------------
					this.handleComplexPhoneBookQueryReply(msg);
					
				} else if (msg.getConversationId().equals(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS.toString()) && msg.getPerformative()==ACLMessage.PROPAGATE) {
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

	/**
	 * Handle phone book query reply.
	 * @param msg the query reply from the the CEA
	 */
	private void handleSimplePhoneBookQueryReply(ACLMessage msg) {
		
		// --- Extract the AID from the message ---------------------
		AID aid = null;
		if (msg.getPerformative()==ACLMessage.INFORM_REF) {
			try {
				aid = (AID) msg.getContentObject();
			} catch (UnreadableException unrEx) {
				System.err.println(myAgent.getLocalName() + ": Error extracting AID from the CEA reply");
				unrEx.printStackTrace();
			}
		}
		
		// --- Add to the agent's local phone book if successful ----
		if (aid!=null) {
			this.getEnergyAgent().getInternalDataModel().addAidToPhoneBook(aid);
		} else {
			System.err.println(myAgent.getLocalName() + ": CEA phone book query failed");
		}
	}
	
	private void handleComplexPhoneBookQueryReply(ACLMessage msg) {
		if (msg.getPerformative()==ACLMessage.INFORM_REF) {
			try {
				@SuppressWarnings("unchecked")
				ArrayList<PhoneBookEntry> entriesList = (ArrayList<PhoneBookEntry>) msg.getContentObject();
				if (entriesList!=null && entriesList.size()>0) {
					this.getEnergyAgent().getInternalDataModel().addEntriesToPhoneBook(entriesList);
				} else {
					System.err.println(myAgent.getLocalName() + ": Phone book reply was empty!");
				}
			} catch (UnreadableException e) {
				System.err.println(myAgent.getLocalName() + ": Error extracting content from phone book reply");
				e.printStackTrace();
			}
		}
	}

}
