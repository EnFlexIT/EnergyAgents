package de.enflexit.ea.electricity.transformer;

import de.enflexit.common.Observable;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.ontology.SlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UnitValue;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookSearchFilter;
import de.enflexit.jade.phonebook.behaviours.PhoneBookQueryInitiator;
import jade.content.Concept;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This is the main class of the TransformerAgent.
 * 
 * @author  Jan Mehlich - EVT - University of Wuppertal (BUW)
 * @author  Marcel Ludwig - EVT - University of Wuppertal (BUW)
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TransformerAgent extends AbstractEnergyAgent {
	
	private static final long serialVersionUID = -2493803948645554649L;

	private InternalDataModel internalDataModel;
	private MeasurementSubscriptionInitiator subscriptionInitiatorBehaviour;

	private boolean isStartTestTickerBehaviour = false;
	private TestTickerBehaviour testTickerBehaviour;
	
	
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

		// --- Subscribe with the sensor of interest ----------------
		AID sensorAID = this.getSensorAID();
		if (sensorAID!=null) {
			System.out.println(this.getLocalName() + ": Sensor AID locally available, starting subscription");
			this.startSubscriptionInitiatorBehaviour();
		} else {
			System.out.println(this.getLocalName() + ": Sensor AID not locally available, requesting from the CEA");
			this.startPhoneBookQueryForLocalName(this.getInternalDataModel().getIDSensorToSubscribeTo());
		}
		
		// --- Start test ticker behaviour? -------------------------
		if (this.isStartTestTickerBehaviour==true) {
			this.testTickerBehaviour = new TestTickerBehaviour(this, 10 * 1000);
			this.addBehaviour(this.testTickerBehaviour);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractEnergyAgent#takeDownEnergyAgent()
	 */
	@Override
	protected void takeDownEnergyAgent() {
		// --- Stop test ticker behaviour? --------------------------
		if (this.testTickerBehaviour!=null) {
			this.removeBehaviour(this.testTickerBehaviour);
			this.testTickerBehaviour = null;
		}
	}
	
	/**
	 * Starts a {@link PhoneBookQueryInitiator} that requests info about the agent 
	 * with the specified local name from the CEA.
	 * @param localName the local name
	 */
	private void startPhoneBookQueryForLocalName(String localName){
		AID ceaAID = this.getInternalDataModel().getCentralAgentAID();
		EnergyAgentPhoneBookSearchFilter searchFilter = EnergyAgentPhoneBookSearchFilter.matchLocalName(localName);
		PhoneBookQueryInitiator<EnergyAgentPhoneBookEntry> queryInitiator = new PhoneBookQueryInitiator<EnergyAgentPhoneBookEntry>(this, this.getInternalDataModel().getPhoneBook(), ceaAID, searchFilter, true);
		this.addBehaviour(queryInitiator);
	}
	
	/**
	 * Builds the sensor AID for the subscription message.
	 * @return the sensor AID
	 */
	private AID getSensorAID() {
		
		AID sensorAID = null;
		String sensorID = this.getInternalDataModel().getIDSensorToSubscribeTo();
		
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
	 * Start the subscription responder behaviour.
	 */
	public void startSubscriptionInitiatorBehaviour() {

		// --- If the subscription initiator is not started yet, try to start it ----
		if (this.subscriptionInitiatorBehaviour==null) {
			AID sensorAID = this.getInternalDataModel().getAidFromPhoneBook(this.getInternalDataModel().getIDSensorToSubscribeTo());
			if (sensorAID!=null) {

				System.out.println(this.getLocalName() + ": Got the sensor AID from the CEA, starting the subscription");

				// --- Define subscription message ------------------
				ACLMessage subscriptionMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
				subscriptionMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
				subscriptionMessage.setConversationId(GlobalHyGridConstants.CONVERSATION_ID_MEASUREMENT_SUBSCRIPTION);
				subscriptionMessage.addReceiver(this.getSensorAID());
				
				// --- Start the behaviour --------------------------
				this.addBehaviour(this.getSubscriptionInitiatorBehaviour(subscriptionMessage));
				
				// --- Make sure protocol messages are not consumed by the default message receive behaviour ---
				MessageTemplate matchFipaSubscribe = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
				this.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(matchFipaSubscribe);
			}
		}
		
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
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#onObserverUpdate(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void onObserverUpdate(Observable observable, Object updateObject) {
	
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#handleIncomingMessage(jade.lang.acl.ACLMessage)
	 */
	@Override
	public void handleIncomingMessage(ACLMessage message) {
		
		if (message==null) return;
		
		String msgPrefix = "[" + this.getClass().getSimpleName() + "|" + this.getLocalName() + "] ";
		
		// --- Try to extract the ontology instance -----------------
		Concept ontoInstance = null;
		try {
			Action action = (Action) this.getContentManager().extractContent(message);
			ontoInstance = action.getAction();
			
		} catch (CodecException | OntologyException ex) {
			System.err.println(msgPrefix + "Error while trying to extract message from " + message.getSender().getLocalName());
			System.err.println("");
			ex.printStackTrace();
		}
		
		// --- Work on the ontology instance ------------------------
		if (ontoInstance!=null) {

			if (ontoInstance instanceof SlackNodeState) {
				// --------------------------------------------------
				// --- Received a SlackNodeState --------------------
				// --------------------------------------------------
				Double newSlackNodeVoltageLevelReal = null;
				if (ontoInstance instanceof TriPhaseSlackNodeState) {
					// --- For a three-phase information ------------
					TriPhaseSlackNodeState tpSnState = (TriPhaseSlackNodeState) ontoInstance;
					UniPhaseSlackNodeState upSnNodeState = tpSnState.getSlackNodeStateL1();
					if (upSnNodeState!=null && upSnNodeState.getVoltageReal()!=null) {
						newSlackNodeVoltageLevelReal = Double.valueOf(upSnNodeState.getVoltageReal().getValue()) * Math.sqrt(3);
					}
					
				} else if (ontoInstance instanceof UniPhaseSlackNodeState) {
					// --- For a uni-phase information --------------
					UniPhaseSlackNodeState upSnNodeState = (UniPhaseSlackNodeState) ontoInstance;
					if (upSnNodeState!=null && upSnNodeState.getVoltageReal()!=null) {
						newSlackNodeVoltageLevelReal = Double.valueOf(upSnNodeState.getVoltageReal().getValue());
					}
				}

				if (newSlackNodeVoltageLevelReal!=null) {
					//System.out.println(msgPrefix + "Setting slack node voltage level to " + newSlackNodeVoltageLevelReal);
					this.getInternalDataModel().getTransformerDataModel().setSlackNodeVoltageLevel(newSlackNodeVoltageLevelReal);
				}
			}
		}
			
	}
	
	
	
	// ------------------------------------------------------------------------
	// --- From here, a test area ---------------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * The Class TestTickerBehaviour.
	 *
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	private class TestTickerBehaviour extends TickerBehaviour {

		private static final long serialVersionUID = -4092392813519681668L;

//		private final double[] voltageArray = {215.0, 220.0, 225.0, 230.0, 235.0, 240.0, 245.0};
		private final double[] voltageArray = {385.0, 390.0, 395.0, 400.0, 405.0, 410.0, 415.0};
		private int indexCounter;
		
		public TestTickerBehaviour(Agent agent, long period) {
			super(agent, period);
		}

		@Override
		protected void onTick() {

			// --- Test area ----------------------------------------
			double voltageLevel = this.voltageArray[this.indexCounter];
			TransformerAgent.this.sendSlackNodeStateMessage(this.getAgent().getAID(), voltageLevel, 0, false);
			
			// --- Increase count index -----------------------------
			this.indexCounter++;
			if (this.indexCounter>=this.voltageArray.length) {
				this.indexCounter=0;
			}
		}
	}
	
	/**
	 * Send slack node state message.
	 *
	 * @param receiver the receiver
	 * @param voltageLevelReal the voltage level real
	 * @param voltageLevelImag the voltage level imag
	 * @param isTriPhaseSlackNodeState the is tri phase slack node state
	 */
	private void sendSlackNodeStateMessage(AID receiver, double voltageLevelReal, double voltageLevelImag, boolean isTriPhaseSlackNodeState) {
		
		try {
			ACLMessage acl = new ACLMessage(ACLMessage.INFORM);
			acl.addReceiver(this.getAID());
			acl.setLanguage(this.getAgentCodec().getName());
			acl.setOntology(this.getAgentOntology().getName());
	
			// --- Define the slack node state ----------------
			SlackNodeState snState = null;
			if (isTriPhaseSlackNodeState==true) {
				TriPhaseSlackNodeState tpSnState = new TriPhaseSlackNodeState();
				tpSnState.setSlackNodeStateL1(this.createUniPhaseSlackNodeState(voltageLevelReal, voltageLevelImag));
				tpSnState.setSlackNodeStateL1(this.createUniPhaseSlackNodeState(voltageLevelReal, voltageLevelImag));
				tpSnState.setSlackNodeStateL1(this.createUniPhaseSlackNodeState(voltageLevelReal, voltageLevelImag));
				snState = tpSnState;
			} else {
				snState = this.createUniPhaseSlackNodeState(voltageLevelReal, voltageLevelImag);
			}
			
			Action snStateAction = new Action(this.getAID(), snState);
			this.getContentManager().fillContent(acl, snStateAction);
			this.send(acl);
			
		} catch (CodecException | OntologyException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Creates a UniPhaseSlackNodeState.
	 *
	 * @param voltageLevelReal the voltage level real
	 * @param voltageLevelImag the voltage level imag
	 * @return the uni phase slack node state
	 */
	private UniPhaseSlackNodeState createUniPhaseSlackNodeState(double voltageLevelReal, double voltageLevelImag) {
		UniPhaseSlackNodeState upSnStateL1 = new UniPhaseSlackNodeState();
		upSnStateL1.setVoltageReal(new UnitValue((float) voltageLevelReal, "V"));
		upSnStateL1.setVoltageImag(new UnitValue((float) voltageLevelImag, "V"));
		return upSnStateL1;
	}
	
}
