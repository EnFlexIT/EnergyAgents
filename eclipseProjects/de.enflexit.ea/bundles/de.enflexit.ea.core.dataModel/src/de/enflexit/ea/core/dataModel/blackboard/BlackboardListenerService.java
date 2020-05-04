package de.enflexit.ea.core.dataModel.blackboard;

/**
 * The Interface BlackboardListenerService can be used to register an OSGI service to 
 * the Blackboard and receive a notifications when the Blackboard model has changed.
 * This will be done by the SimulationManager after each network calculation execution.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public interface BlackboardListenerService {

	/**
	 * Will be invoked right after the network calculations are done.
	 * @param blackboard the blackboard with its current state
	 */
	public void onNetworkCalculationDone(Blackboard blackboard);

	
	/**
	 * Will be invoked if the simulation is done or finished and give a chance to cleanup 
	 * variable used in service.
	 */
	public void onSimulationDone();
	
}
