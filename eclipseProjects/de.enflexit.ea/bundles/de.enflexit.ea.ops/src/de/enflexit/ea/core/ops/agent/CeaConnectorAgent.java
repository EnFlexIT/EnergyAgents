package de.enflexit.ea.core.ops.agent;

import de.enflexit.ea.core.ops.OpsController;
import de.enflexit.ea.core.ops.agent.behaviour.CeaConnectingBehaviour;
import de.enflexit.ea.core.ops.agent.behaviour.MessageReceiveBehaviour;
import de.enflexit.ea.core.ops.agent.behaviour.O2ABehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;

/**
 * The class for the CeaConnectorAgent.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class CeaConnectorAgent extends Agent {

	private static final long serialVersionUID = -416031937742748338L;

	private MessageReceiveBehaviour messageReceiveBehaviour;

	private CeaConnectorInternalDataModel internalDataModel;
	private OpsController opsController;
	
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {

		this.opsController = OpsController.getInstance(); 

		// --- Register codec and ontology --------------------------
		this.getContentManager().registerLanguage(new SLCodec());
		//this.getContentManager().registerOntology() //TODO ?;
		
		// --- Prevent to run more than on connector ----------------
		if (this.opsController.getCeaConnectorAgent()!=null) {
			this.takeDown();
			return;
		}

		// --- Register this agent as connector agent ---------------
		this.opsController.setCeaConnectorAgent(this);
		
		// --- Activate the object to agent behavior ----------------
		O2ABehaviour o2ab = new O2ABehaviour(this);
		this.addBehaviour(o2ab);
		this.setO2AManager(o2ab);

		// --- Add the ACL message receive behaviour ----------------
		this.addBehaviour(this.getMessageReceiveBehaviour());
	
		// --- Add the CeaConnectingBehaviour -----------------------
		this.addBehaviour(new CeaConnectingBehaviour(this));
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected void takeDown() {
		if (this.opsController.getCeaConnectorAgent()==this) {
			this.opsController.setCeaConnectorAgent(null);
		}
	}
	
	/**
	 * Returns the message receive behaviour.
	 * @return the message receive behaviour
	 */
	public MessageReceiveBehaviour getMessageReceiveBehaviour() {
		if (messageReceiveBehaviour==null) {
			messageReceiveBehaviour = new MessageReceiveBehaviour(this);
		}
		return messageReceiveBehaviour;
	}
	
	/**
	 * Returns the internal data model.
	 * @return the internal data model
	 */
	public CeaConnectorInternalDataModel getInternalDataModel() {
		if (internalDataModel==null) {
			internalDataModel = new CeaConnectorInternalDataModel(this);
		}
		return internalDataModel;
	}
	/**
	 * Gets the {@link OpsController}.
	 * @return the OPS controller
	 */
	public OpsController getOpsController() {
		return opsController;
	}
	/**
	 * Sets the given status info to the end user visualisation.
	 * @param newStatusInfo the new status info
	 */
	public void setStatusInfo(String newStatusInfo) {
		String myStatus = this.getClass().getSimpleName() + ": " + newStatusInfo;
		this.getOpsController().setStatusBarMessage(myStatus);
	}
	
}
