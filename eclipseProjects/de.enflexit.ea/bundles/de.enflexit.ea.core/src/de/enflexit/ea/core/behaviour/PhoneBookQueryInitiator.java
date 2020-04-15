package de.enflexit.ea.core.behaviour;

import java.io.IOException;
import java.util.ArrayList;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.dataModel.phonebook.PhoneBookEntry;
import de.enflexit.ea.core.dataModel.phonebook.PhoneBookQuery;
import de.enflexit.ea.core.dataModel.phonebook.PhoneBookQuery.QueryType;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;

/**
 * Implementation of the initiator side of the FIPA Query protocol for phone book queries 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class PhoneBookQueryInitiator extends SimpleAchieveREInitiator {

	private static final long serialVersionUID = 8124142066980889030L;

	/**
	 * Instantiates a new phone book query initiator.
	 * @param agent the agent
	 * @param msg the query message. It is recommended to use one of the static getQueryMessageFor...() methods provided by this class
	 */
	public PhoneBookQueryInitiator(AbstractEnergyAgent agent, ACLMessage msg) {
		super(agent, msg);
	}
	
	/**
	 * Gets the query message for single agent.
	 * @param ceaAID the cea AID
	 * @param localName the local name
	 * @return the query message for single agent
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ACLMessage getQueryMessageForSingleAgent(AID ceaAID, String localName) throws IOException {
		ACLMessage queryMessage = null;
		
		PhoneBookQuery pbQuery = new PhoneBookQuery();
		
		pbQuery.setQueryType(QueryType.AGENT_BY_LOCAL_NAME);
		pbQuery.setSearchString(localName);
		
		queryMessage = getQueryMessage(ceaAID);
		queryMessage.setContentObject(pbQuery);
		
		return queryMessage;
	}
	
	/**
	 * Gets the query message for component type.
	 * @param ceaAID the cea AID
	 * @param componentType the component type
	 * @return the query message for component type
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ACLMessage getQueryMessageForComponentType(AID ceaAID, String componentType) throws IOException {
		ACLMessage queryMessage = null;
		
		PhoneBookQuery pbQuery = new PhoneBookQuery();
		
		pbQuery.setQueryType(QueryType.AGENTS_BY_COMPONENT_TYPE);
		pbQuery.setSearchString(componentType);
		
		queryMessage = getQueryMessage(ceaAID);
		queryMessage.setContentObject(pbQuery);
		
		return queryMessage;
	}
	
	/**
	 * Gets the query message for controllable components.
	 * @param ceaAID the cea AID
	 * @return the query message for controllable components
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ACLMessage getQueryMessageForControllableComponents(AID ceaAID) throws IOException {
		ACLMessage queryMessage = null;
		
		PhoneBookQuery pbQuery = new PhoneBookQuery();
		
		pbQuery.setQueryType(QueryType.CONTROLLABLE_COMPONENTS);
		
		queryMessage = getQueryMessage(ceaAID);
		queryMessage.setContentObject(pbQuery);
		
		return queryMessage;
	}
	
	/**
	 * Gets the basic query message.
	 * @param ceaAID the cea AID
	 * @return the query message
	 */
	private static ACLMessage getQueryMessage(AID ceaAID) {
		ACLMessage queryMessage = new ACLMessage(ACLMessage.QUERY_REF);
		queryMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
		queryMessage.setConversationId(ConversationID.PHONEBOOK_QUERY_COMPLEX.toString());
		queryMessage.addReceiver(ceaAID);
		return queryMessage;
	}

	/* (non-Javadoc)
	 * @see jade.proto.SimpleAchieveREInitiator#handleInform(jade.lang.acl.ACLMessage)
	 */
	@Override
	protected void handleInform(ACLMessage msg) {
		ArrayList<PhoneBookEntry> resultList = this.extractResults(msg);
		if (resultList!=null && resultList.size()>0) {
			((AbstractEnergyAgent)myAgent).getInternalDataModel().addEntriesToPhoneBook(resultList);;
		} else {
			System.err.println(myAgent.getLocalName() + ": Pone book query returned an empty result!");
		}
	}
	
	/**
	 * Extracts the resulting {@link PhoneBookEntry}s from the inform message
	 * @param msg the msg
	 * @return the phone book entries
	 */
	@SuppressWarnings("unchecked")
	protected ArrayList<PhoneBookEntry> extractResults(ACLMessage msg){
		ArrayList<PhoneBookEntry> resultList = null;
		try {
			resultList = (ArrayList<PhoneBookEntry>) msg.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		return resultList;
	}

}
