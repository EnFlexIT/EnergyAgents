package de.enflexit.ea.core.eomStateStream;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.eom.awb.stateStream.SystemStateDispatcherAgentConnector;
import energy.optionModel.TechnicalSystem;

/**
 * The Class StateQueueKeeperTechnicalSystem extends the {@link AbstractStateQueueKeeper}
 * but is designed to work with {@link TechnicalSystem} representations for {@link NetworkComponent}s.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class StateQueueKeeperTechnicalSystem extends AbstractStateQueueKeeper {

	
	/**
	 * Instantiates a new state queue keeper for a TechnicalSystem.
	 *
	 * @param owningEomInputStream the owning eom input stream
	 * @param dispatchConnector the dispatch connector
	 */
	public StateQueueKeeperTechnicalSystem(EomModelStateInputStream owningEomInputStream, SystemStateDispatcherAgentConnector dispatchConnector) {
		super(owningEomInputStream, dispatchConnector);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.eomStateStream.AbstractStateQueueKeeper#checkRemainingStatesAndPossiblyStartStartLoading(int, boolean)
	 */
	@Override
	public void checkRemainingStatesAndPossiblyStartLoading(int remainingStatesInQueue, boolean isDataReloadRecommended) {
		// TODO Auto-generated method stub
		
	}

	
}
