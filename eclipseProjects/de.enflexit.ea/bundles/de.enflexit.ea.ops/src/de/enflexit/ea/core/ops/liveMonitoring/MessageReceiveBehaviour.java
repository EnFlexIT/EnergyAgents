package de.enflexit.ea.core.ops.liveMonitoring;

import java.util.Vector;

import de.enflexit.ea.core.ops.agent.CeaConnectorAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The Class MessageReceiveBehaviour for the {@link CeaConnectorAgent}.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class MessageReceiveBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 8379447679627930476L;
	
	private Vector<MessageTemplate> ignoreList;
	private MessageTemplate messageTemplate;

	@SuppressWarnings("unused")
	private LiveMonitoringAgent liveMonitoringAgent;
	
	/**
	 * Instantiates a new message receive behaviour.
	 * @param liveMonitoringAgent the live monitoring agent
	 */
	public MessageReceiveBehaviour(LiveMonitoringAgent liveMonitoringAgent) {
		this.liveMonitoringAgent = liveMonitoringAgent;
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
			
			
		} else {
			// --- Wait for the next incoming message -----
			this.block();
		}
		
	}
	
}
