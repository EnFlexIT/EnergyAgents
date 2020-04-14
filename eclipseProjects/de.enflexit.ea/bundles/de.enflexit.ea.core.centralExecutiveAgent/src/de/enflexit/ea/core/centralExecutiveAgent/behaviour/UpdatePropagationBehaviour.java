package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import java.util.List;

import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.globalDataModel.cea.ConversationID;
import de.enflexit.ea.core.globalDataModel.phonebook.PhoneBook;
import de.enflexit.ea.core.globalDataModel.phonebook.PhoneBookEntry;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


/**
 * The Class UpdatePropagationBehaviour can be used to inform all field agent
 * platforms about a required update check.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class UpdatePropagationBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8351278865917204650L;

	private enum UpdatePropagationTask {
		StartMirrorProcess,
		WaitForMirrorProcess,
		InformFieldAgents,
		UpdateOwnPlatform,
		Done
	}
	
	private CentralExecutiveAgent cea;
	private ACLMessage updateMessage;
	
	private UpdatePropagationTask task;
	private RepositoryMirrorBehaviour repoMirrorBehaviour;
	
	/**
	 * Instantiates a new update propagation behaviour.
	 *
	 * @param cea the current CentralExecutiveAgent
	 * @param updateMessage the update message
	 */
	public UpdatePropagationBehaviour(ACLMessage updateMessage) {
		this.updateMessage = updateMessage;
	}
	
	/**
	 * Returns the central executive agent.
	 * @return the central executive agent
	 */
	private CentralExecutiveAgent getCentralExecutiveAgent() {
		if (cea==null) {
			cea = (CentralExecutiveAgent) this.myAgent;
		}
		return cea;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#done()
	 */
	public boolean done() {
		return this.getTask()==UpdatePropagationTask.Done;
	}
	
	/**
	 * Returns the current task.
	 * @return the task
	 */
	private UpdatePropagationTask getTask() {
		if (task==null) {
			task = UpdatePropagationTask.StartMirrorProcess;
		}
		return task;
	}
	/**
	 * Sets the current task.
	 * @param task the new task
	 */
	private void setTask(UpdatePropagationTask task) {
		this.task = task;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		switch (this.getTask()) {
		case StartMirrorProcess:
			// --- Start the mirror process for p2 and projects -----
			this.repoMirrorBehaviour = this.getCentralExecutiveAgent().startRepositoryMirrorBehaviourNow();
			this.setTask(UpdatePropagationTask.WaitForMirrorProcess);

			// --- Answer with a confirm message --------------------
			ACLMessage reply = this.updateMessage.createReply();
			reply.setPerformative(ACLMessage.CONFIRM);
			this.myAgent.send(reply);
			break;
			
		case WaitForMirrorProcess:
			// --- Check if the mirror process was finalized --------
			if (this.isFinalizedMirroring()==true) {
				this.setTask(UpdatePropagationTask.InformFieldAgents);
			}
			break;

		case InformFieldAgents:
			// --- Forward update info to know agents ---------------
			this.propagateUpdateRequest();
			this.setTask(UpdatePropagationTask.UpdateOwnPlatform);
			break;
			
		case UpdateOwnPlatform:
			// --- Start the own update process ---------------------
			this.getCentralExecutiveAgent().startPlatformUpdateBehaviourNow();
			this.setTask(UpdatePropagationTask.Done);
			break;
			
		case Done:
			// --- Nothing to do here -------------------------------
			break;
		}
		
		// --- Block this behaviour for one second ------------------
		this.block(1000);
	}

	/**
	 * Checks if the mirroring was finalized.
	 * @return true, if is finalized mirroring
	 */
	private boolean isFinalizedMirroring() {
		return (this.repoMirrorBehaviour!=null && this.repoMirrorBehaviour.isMirrorJobsAreDone());
	}
	
	/**
	 * Informs all known field agents about the update request.
	 */
	private void propagateUpdateRequest() {
		
		try {
			// --- Get the phonebook with all known agents ---------- 
			PhoneBook pb =  this.getCentralExecutiveAgent().getInternalDataModel().getPhoneBook();
			List<PhoneBookEntry> phoneBookEntries = pb.getPhoneBookEntries();
			for (int i = 0; i < phoneBookEntries.size(); i++) {
				
				// --- Get the single PhonebookEntry ----------------
				PhoneBookEntry pbEntry = phoneBookEntries.get(i);

				// --- Create and send the update message -----------
				if (pbEntry.getAID()!=null) {
					ACLMessage message = new ACLMessage(ACLMessage.PROPAGATE);
					message.addReceiver(pbEntry.getAID());
					message.setConversationId(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS.toString());
					this.myAgent.send(message);
				}
			}
			
		} catch (Exception ex) {
			System.err.println("[" + this.getCentralExecutiveAgent().getClass().getSimpleName() + "] Error propagating update request:");
			ex.printStackTrace();
		}
	}
	
}
