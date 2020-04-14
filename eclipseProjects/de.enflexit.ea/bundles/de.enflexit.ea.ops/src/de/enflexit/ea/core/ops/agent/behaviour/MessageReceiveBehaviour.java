package de.enflexit.ea.core.ops.agent.behaviour;

import java.util.Vector;

import de.enflexit.ea.core.globalDataModel.cea.ConversationID;
import de.enflexit.ea.core.ops.OpsController;
import de.enflexit.ea.core.ops.agent.CeaConnectorAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The Class MessageReceiveBehaviour for the {@link CeaConnectorAgent}.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class MessageReceiveBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 8379447679627930476L;
	
	private CeaConnectorAgent ceaConnectorAgent;

	private Vector<MessageTemplate> ignoreList;
	private MessageTemplate messageTemplate;

	private Vector<ACLMessage> ceaConnectingFeedbacks;
	private Integer noOfCeaAIDsToCheck;
	private int noOfCeaAIDsChecked;

	
	/**
	 * Instantiates a new message receive behaviour.
	 * @param ceaConAgent the current CeaConnectorAgent
	 */
	public MessageReceiveBehaviour(CeaConnectorAgent ceaConAgent) {
		this.ceaConnectorAgent = ceaConAgent;
	}
	/**
	 * Gets the {@link OpsController}.
	 * @return the ops controller
	 */
	private OpsController getOpsController() {
		return this.ceaConnectorAgent.getOpsController();
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
	
	// --------------------------------------------------------------
	// --- The actual message handling ------------------------------
	// --------------------------------------------------------------
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		ACLMessage msg = this.myAgent.receive(this.getMessageTemplate());
		if (msg!=null) {
			// --- Act on the incoming message ----------------------
			if (msg.getConversationId()!=null) {
				// --- Considering conversation IDs -----------------
				if (msg.getConversationId().equals(ConversationID.OPS_CONNECTING_REQUEST.toString())==true) {
					// --- Act on connecting request from the OPS ---
					this.onOpsConnectingRequestAnswer(msg);
					
				} else {
					// --- Currently unknown conversation IDs -------
					System.out.println(this.ceaConnectorAgent.getClass().getSimpleName() + ": Got message with unknown conversation ID " + msg.getConversationId());
				}	
				
			} else {
				// --- Considering further messages -----------------
				System.out.println(this.ceaConnectorAgent.getClass().getSimpleName() + ": Unknown message type " + msg.toString());
			}
			
		} else {
			// --- Wait for the next incoming message -----
			this.block();
		}
		
	}

	
	/**
	 * On connecting request answer.
	 * @param msg the ACLMessage received
	 */
	private void onOpsConnectingRequestAnswer(ACLMessage msg) {
		
		this.noOfCeaAIDsChecked++;
		this.getCeaConnectingFeedbacks().add(msg);
		
		// --- OPS connecting messages --------------------
		if (msg.getPerformative()==ACLMessage.CONFIRM) {
			// --- The right AID was found ----------------
			AID ceaAID;
			try {
				ceaAID = msg.getSender();
				if (msg.getContentObject()!=null) {
					ceaAID = (AID) msg.getContentObject();
				}
				this.getOpsController().setCeaAID(ceaAID);
				// --- Set status to visualization --------
				String aidDescription = "Connected to CEA: " + this.getOpsController().getCeaAIDdescription();
				this.ceaConnectorAgent.setStatusInfo(aidDescription);

			} catch (UnreadableException unReEx) {
				unReEx.printStackTrace();
			}
			
		} else {
			// --- Got negative return message ------------
			if (this.noOfCeaAIDsChecked==this.getNoOfCeaAIDsToCheck() && this.getOpsController().getCeaAID()==null) {
				// --- All AIDs were checked --------------
				this.ceaConnectorAgent.setStatusInfo("CEA connection could not be established!");
				// --- Print the ACL feedbacks ------------
				System.out.println("[" + this.ceaConnectorAgent.getClass().getSimpleName() + "] CEA connection could not be established." );
				for (int i = 0; i < this.getCeaConnectingFeedbacks().size(); i++) {
					System.out.println("[" + this.ceaConnectorAgent.getClass().getSimpleName() + "] Return Message No. " + (i+1) + ":" );
					System.out.println(this.getCeaConnectingFeedbacks().get(i));
				}
			}
		}
	}
	/**
	 * Reset the counting for CEA AIDs.
	 */
	public void resetCountingForCeaAIDs() {
		this.ceaConnectingFeedbacks=null;
		this.noOfCeaAIDsChecked=0;
		this.noOfCeaAIDsToCheck=null;
		this.getNoOfCeaAIDsToCheck();
	}
	/**
	 * Gets the CEA connecting feedbacks.
	 * @return the CEA connecting feedbacks
	 */
	private Vector<ACLMessage> getCeaConnectingFeedbacks() {
		if (ceaConnectingFeedbacks==null) {
			ceaConnectingFeedbacks = new Vector<>();
		}
		return ceaConnectingFeedbacks;
	}
	/**
	 * Returns the number of CEA AIDs to check.
	 * @return the no of cea AI ds to check
	 */
	private Integer getNoOfCeaAIDsToCheck() {
		if (noOfCeaAIDsToCheck==null) {
			noOfCeaAIDsToCheck = this.ceaConnectorAgent.getInternalDataModel().getCentralAgentAIDVector().size();
		}
		return noOfCeaAIDsToCheck;
	}
	
}
