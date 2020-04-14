package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import java.io.IOException;
import java.util.ArrayList;

import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.globalDataModel.cea.ConversationID;
import de.enflexit.ea.core.globalDataModel.phonebook.PhoneBook;
import de.enflexit.ea.core.globalDataModel.phonebook.PhoneBookEntry;
import de.enflexit.ea.core.globalDataModel.phonebook.PhoneBookQuery;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREResponder;

/**
 * The responder side of a FIPA Query implementation for phone book requests
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class PhoneBookQueryResponder extends SimpleAchieveREResponder {
	
	private static final long serialVersionUID = 3929966505865961443L;

	private PhoneBook phoneBook;

	/**
	 * Instantiates a new phone book request responder.
	 * @param centralExecutiveAgent the central executive agent
	 */
	public PhoneBookQueryResponder(CentralExecutiveAgent centralExecutiveAgent) {
		super(centralExecutiveAgent, getMessageTemplate());
	}
	
	/**
	 * Gets the message template.
	 * @return the message template
	 */
	public static MessageTemplate getMessageTemplate() {
		MessageTemplate matchProtocol = MessageTemplate.MatchProtocol(FIPA_QUERY);
		MessageTemplate matchConversationID = MessageTemplate.MatchConversationId(ConversationID.PHONEBOOK_QUERY_COMPLEX.toString());
		MessageTemplate matchPerformative = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
		
		MessageTemplate matchAll = MessageTemplate.and(matchProtocol, MessageTemplate.and(matchConversationID, matchPerformative));
		return matchAll;
	}
	
	

	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		// --- Method not needed, just overridden to prevent the console output from jade's default implementation
		return null;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SimpleAchieveREResponder#prepareResultNotification(jade.lang.acl.ACLMessage, jade.lang.acl.ACLMessage)
	 */
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		ACLMessage reply = null;
		
		try {
			PhoneBookQuery pbQuery = (PhoneBookQuery) request.getContentObject();
			
			if (pbQuery!=null) {
				
				// --- Get the requested entries from the phone book ----------
				ArrayList<PhoneBookEntry> entriesList = null;
				switch(pbQuery.getQueryType()) {
				case AGENT_BY_LOCAL_NAME:
					PhoneBookEntry entry = this.getPhoneBook().getPhoneBookEntry(pbQuery.getSearchString());
					entriesList = new ArrayList<>();
					entriesList.add(entry);
					break;
				case AGENTS_BY_COMPONENT_TYPE:
					entriesList = this.getPhoneBook().getEntriesByComponentType(pbQuery.getSearchString());
					break;
				case CONTROLLABLE_COMPONENTS:
					entriesList = this.getPhoneBook().getControllableComponentEntries();
					break;
				
				}
				
				// --- Create the reply ---------------------------------------
				if (entriesList!=null) {
					reply = request.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContentObject(entriesList);
				}

			}
			
		} catch (UnreadableException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error extracting content object from the query message");
		} catch (IOException e) {
			System.err.println("[" + this.getClass().getSimpleName() + "] Error setting content object for the reply message");
		}
		
		// --- If an error occured, send a failure message --------------------
		if (reply==null) {
			reply = request.createReply();
			reply.setPerformative(ACLMessage.FAILURE);
		}
		
		return reply;
	}
	

	/**
	 * Gets the phone book.
	 * @return the phone book
	 */
	private PhoneBook getPhoneBook() {
		if (phoneBook==null) {
			phoneBook = ((CentralExecutiveAgent)myAgent).getInternalDataModel().getPhoneBook();
		}
		return phoneBook;
	}

}
