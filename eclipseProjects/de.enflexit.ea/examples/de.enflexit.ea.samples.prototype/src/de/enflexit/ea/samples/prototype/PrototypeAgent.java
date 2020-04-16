package de.enflexit.ea.samples.prototype;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.AbstractIOSimulated;
import de.enflexit.ea.core.behaviour.DefaultMessageReceiveBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Represents a prosumer energy agent that uses the configured EOM.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PrototypeAgent extends AbstractEnergyAgent {

	private static final long serialVersionUID = -2493803948645554649L;
	private InternalDataModel internalDataModel;

	@Override
	protected void setupEnergyAgent() {
		// --- Perform agent specific setup tasks here --------------
	}

	@Override
	protected void takeDownEnergyAgent() {
		// --- Perform agent specific take down tasks here ----------
	}

	/**
	 * Gets the internal data model of this agent.
	 * @return the internal data model
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
	
	@Override
	public AbstractIOSimulated getIOSimulated() {
		return new IOSimulated(this);
	}
	
	@Override
	public AbstractIOReal getIOReal() {
		return new IOReal(this);
	}

	/**
	 * Handle incoming ACL messages from the {@link DefaultMessageReceiveBehaviour}.
	 * @param message the message
	 */
	@Override
	public void handleIncomingMessage(ACLMessage message) {
		// --- Do something with the message here ---------------
	}
	
	
	
}
