package de.enflexit.ea.electricity.transformer;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import de.enflexit.common.Observable;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.AbstractInternalDataModel.CHANGED;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.ontology.FlexibilityOffer;
import de.enflexit.ea.core.dataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.core.dataModel.phoneBook.EnergyAgentPhoneBookSearchFilter;
import de.enflexit.jade.phonebook.behaviours.PhoneBookQueryInitiator;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * 
 * @author  Jan Mehlich - EVT - University of Wuppertal (BUW)
 * @author  Marcel Ludwig - EVT - University of Wuppertal (BUW)
 */
public class TransformerAgent extends AbstractEnergyAgent {
	
	private static final long serialVersionUID = -2493803948645554649L;

	private InternalDataModel internalDataModel;
	
	private MeasurementSubscriptionInitiator subscriptionInitiatorBehaviour;

	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#getInternalDataModel()
	 */
	@Override
	public InternalDataModel getInternalDataModel() {
		if (this.internalDataModel==null) {
			this.internalDataModel = new InternalDataModel(this);
			this.internalDataModel.getOptionModelController();	// Necessary to initialize the datamodel's controlledSystemType
			this.internalDataModel.addObserver(this);
		}
		return this.internalDataModel;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#getIOSimulated()
	 */
	@Override
	public AbstractIOSimulated getIOSimulated() {
		return new IOSimulated(this);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractEnergyAgent#getIOReal()
	 */
	@Override
	public AbstractIOReal getIOReal() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#setupEnergyAgent()
	 */
	@Override
	protected void setupEnergyAgent() {

		this.startPhoneBookQueryForDistrictAgents();
		
		// --- Subscribe with the sensor of interest ----------------
		AID sensorAID = this.getSensorAID();
		if (sensorAID!=null) {
			System.out.println(this.getLocalName() + ": Sensor AID locally available, starting subscription");
			this.startSubscriptionInitiatorBehaviour();
		} else {
			System.out.println(this.getLocalName() + ": Sensor AID not locally available, requesting from the CEA");
			this.startPhoneBookQueryForLocalName(this.getIDSensorToSubscribeTo());
		}
	}
	/**
	 * Builds the sensor AID for the subscription message.
	 * @return the sensor AID
	 */
	private AID getSensorAID() {
		
		AID sensorAID = null;
		String sensorID = this.getIDSensorToSubscribeTo();
		
		if (sensorID!=null) {
			AgentOperatingMode operatingMode = this.getAgentOperatingMode();
			if (operatingMode == AgentOperatingMode.Simulation) {
				// --- Simulation mode, local name is sufficient --------
				sensorAID = new AID(sensorID, AID.ISLOCALNAME);
			} else {
				// --- Testbed or real mode, check the phone book -------
				sensorAID = this.getInternalDataModel().getAidFromPhoneBook(sensorID);
			}
		}
		return sensorAID;
	}
	/**
	 * Returns the sensor ID to which this agent will subscribe sensor information.
	 * @return the ID sensor to subscribe to
	 */
	public String getIDSensorToSubscribeTo() {
		return this.getInternalDataModel().getIDSensorToSubscribeTo();
	}
	

	
	/**
	 * Start phone book query for district agents.
	 */
	private void startPhoneBookQueryForDistrictAgents() {
		
//		try {
//			
//			AID ceaAID = this.getInternalDataModel().getCentralAgentAID();
//			if (ceaAID==null) return;
//			
//			ACLMessage queryMessage = PhoneBookQueryInitiator.getQueryMessageForComponentType(ceaAID, "DistrictAgent");
//			
//			if (phonequeryInitiator==null) {
//				phonequeryInitiator = new PhoneBookQueryInitiator(this, queryMessage);
//				this.addBehaviour(phonequeryInitiator);
//			}
//			
//		} catch (IOException e) {
//			System.err.println(this.getLocalName() + ": Error creating phone book query message!");
//		}
	}
	
	
	/**
	 * Start the subscription responder behaviour.
	 */
	private void startSubscriptionInitiatorBehaviour() {
		
		// --- Start the behaviour ------------------------
		this.addBehaviour(this.getSubscriptionInitiatorBehaviour(this.prepareSubscriptionMessage()));
		
		// --- Make sure protocol messages are not consumed by the default message receive behaviour ---
		MessageTemplate matchFipaSubscribe = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		this.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(matchFipaSubscribe);
		
	}
	/**
	 * Gets the subscription initiator.
	 * @return the subscription initiator
	 */
	private MeasurementSubscriptionInitiator getSubscriptionInitiatorBehaviour(ACLMessage message) {
		if (subscriptionInitiatorBehaviour == null) {
			subscriptionInitiatorBehaviour = new MeasurementSubscriptionInitiator(this, message);
		}
		return subscriptionInitiatorBehaviour;
	}
	
	/**
	 * Prepare the subscription message.
	 * @return the subscription message
	 */
	private ACLMessage prepareSubscriptionMessage() {
		
		ACLMessage subscriptionMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
		subscriptionMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		subscriptionMessage.setConversationId(GlobalHyGridConstants.CONVERSATION_ID_MEASUREMENT_SUBSCRIPTION);
		subscriptionMessage.addReceiver(this.getSensorAID());
		return subscriptionMessage;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#onObserverUpdate(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void onObserverUpdate(Observable observable, Object updateObject) {
		
		// --- Phone book update --------------------------
		if (observable==this.getInternalDataModel() && updateObject==CHANGED.PHONE_BOOK) {
			
			// --- If the subscription initiator is not started yet, try to start it ----
			if (this.subscriptionInitiatorBehaviour==null) {
				AID sensorAID = this.getInternalDataModel().getAidFromPhoneBook(this.getIDSensorToSubscribeTo());
				if (sensorAID!=null) {
					System.out.println(this.getLocalName() + ": Got the sensor AID from the CEA, starting the subscription");
					this.startSubscriptionInitiatorBehaviour();
				}
			}
			
			// --- Create list of aids from district Agents
//			if (this.getInternalDataModel().getAidsOfDistrictAgents()==null) {	
//				//TODO required?
//				ArrayList<PhoneBookEntry> districtAgentList=this.getInternalDataModel().getPhoneBook().getEntriesByComponentType("DistrictAgent"); 
//				Vector<AID> aidsOfDistrictAgents= new Vector<>();
//				for (int i=0; i<districtAgentList.size(); i++) {
//					aidsOfDistrictAgents.add(districtAgentList.get(i).getAID());
//				}
//				this.getInternalDataModel().setAidsOfDistrictAgents(aidsOfDistrictAgents);
//			}
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#handleIncomingMessage(jade.lang.acl.ACLMessage)
	 */
	@Override
	public void handleIncomingMessage(ACLMessage message) {
		
		if (message != null) {
			System.out.println(this.getClass().getSimpleName() + " " + this.getLocalName() + ": Receiving message from " + message.getSender().getLocalName());
			
		}
	}
	
	/**
	 * Prepare the CallForProposal message.
	 * This has to be invoked before the instance of this class is created,
	 * because a cfp-messages is needed for the constructor!
	 *
	 * @param ResponderAIDs the AIDs of all responder 
	 * @param content the content of the message
	 * @return cfpMessage the created ALCmessage  
	 * ToDo: Check if String as Content is valid or ContentObject must be used
	 * @throws IOException 
	 */
	public  Vector<ACLMessage> prepareCFP(Vector<AID> aidsOfDistrictAgents, ActuatorGoal actuatorGoal, double necessaryVoltageaAdjustment, int replyTime) throws CodecException, OntologyException {
		//reset counter for actors
		int nDistrictAgentsForCNP=  0 ;
		//create new vector for all cfp messages
		Vector<ACLMessage> vCFPs = new Vector<>();
		FlexibilityOffer flexibilityOffer = new FlexibilityOffer();
		flexibilityOffer.setPossibleVoltageAdjustment((float)necessaryVoltageaAdjustment);
		
		
		if (flexibilityOffer!=null) {
			for( int i=0; i<aidsOfDistrictAgents.size();i++) {
				AID aid = aidsOfDistrictAgents.get(i);
				ACLMessage cfpMessage = new ACLMessage(ACLMessage.CFP);
				cfpMessage.addReceiver(aid);
				
				// --- Number of involved District Agents 
				nDistrictAgentsForCNP++;
				
				//set protocol
				cfpMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				cfpMessage.setOntology(HyGridOntology.getInstance().getName());
				cfpMessage.setLanguage(new SLCodec().getName());
				
				//set content
				cfpMessage.setContent(actuatorGoal.name());
				Action content = new Action(this.getAID(), flexibilityOffer);
				getContentManager().fillContent(cfpMessage, content);
				
				//setReplyTime
				cfpMessage.setReplyByDate(new Date(System.currentTimeMillis() + replyTime));
				vCFPs.add(cfpMessage);
			}
		}
		this.getInternalDataModel().setnDistrictAgentsForCNP(nDistrictAgentsForCNP);
		
		return vCFPs;
	}
	
	/**
	 * Starts a {@link PhoneBookQueryInitiator} that requests info about the agent 
	 * with the specified local name from the CEA.
	 * @param localName the local name
	 */
	private void startPhoneBookQueryForLocalName(String localName){
		AID ceaAID = this.getInternalDataModel().getCentralAgentAID();
		EnergyAgentPhoneBookSearchFilter searchFilter = EnergyAgentPhoneBookSearchFilter.matchLocalName(localName);
		PhoneBookQueryInitiator<EnergyAgentPhoneBookEntry> queryInitiator = new PhoneBookQueryInitiator<>(this, this.getInternalDataModel().getPhoneBook(), ceaAID, searchFilter, true);
		this.addBehaviour(queryInitiator);
	}
}
