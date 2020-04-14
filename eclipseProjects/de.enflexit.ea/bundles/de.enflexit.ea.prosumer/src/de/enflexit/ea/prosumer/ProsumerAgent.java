package de.enflexit.ea.prosumer;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.AbstractIOSimulated;

/**
 * Represents a prosumer energy agent that uses the configured EOM.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ProsumerAgent extends AbstractEnergyAgent {
	
	private static final long serialVersionUID = -5336718073413159211L;
	private InternalDataModel internalDataModel;
	
	
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
	 * @see de.enflexit.energyAgent.core.AbstractEnergyAgent#getIOReal()
	 */
	@Override
	public AbstractIOReal getIOReal() {
		return new IOReal(this);
	}
	
	
	
}
