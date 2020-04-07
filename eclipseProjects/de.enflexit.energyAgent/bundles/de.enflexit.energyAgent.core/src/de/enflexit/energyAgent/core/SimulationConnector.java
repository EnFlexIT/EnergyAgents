package de.enflexit.energyAgent.core;

import agentgui.simulationService.environment.EnvironmentModel;
import agentgui.simulationService.transaction.DisplayAgentNotification;
import jade.core.AID;

/**
 * The SimulationConnectorInterface describes the needed methods for the connection of.
 * the simulation to the tested environment 
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public interface SimulationConnector {

	/**
	 * Should try to get the environment model and start the initialisation of the {@link AbstractIOSimulated}
	 * by calling {@link AbstractIOSimulated#initialize(agentgui.simulationService.environment.EnvironmentModel)}.
	 *
	 * @see AbstractIOSimulated#initialize(agentgui.simulationService.environment.EnvironmentModel)
	 */
	public void pickEnvironmentModelAndStart();
	
	/**
	 * This method will be used by the to inform the AbstractIOSimulate about changes in the environment. It can be either used
	 * to do this asynchronously or synchronously. It is highly recommended 
	 * to do this asynchronously, so that the agency can act parallel and not
	 * sequentially.
	 *
	 * @param envModel the current or new EnvironmentModel
	 * @param aSynchron true, if this should be done asynchronously
	 */

	
	/**
	 * Sets the environment model to the SimulationConnectorLocal.
	 *
	 * @param envModel the new EnvironmentModel
	 * @param aSynchron the a synchron
	 */
	public void setEnvironmentModel(EnvironmentModel envModel, boolean aSynchron);
	
	/**
	 * This method can be used to transfer any kind of information to the Manager of the current environment model.
	 *
	 * @param notification the notification
	 * @return true, if successful
	 */
	public boolean sendManagerNotification(Object notification);

	/**
	 * This method can be used to transfer any kind of information to one member of the current environment model.
	 *
	 * @param receiverAID the AID of receiver agent
	 * @param notification the notification
	 * @return true, if successful
	 */
	public boolean sendAgentNotification(AID receiverAID, Object notification);
	
	/**
	 * Notify display agents about changes with a {@link DisplayAgentNotification}.
	 * 
	 * @param displayAgentNotification the display agent message
	 */
	public void sendDisplayAgentNotification(DisplayAgentNotification displayAgentNotification);
		
	/**
	 * This method sets the answer respectively the change of a single simulation agent
	 * back to the central simulation manager.
	 *
	 * @param myNextState the next state of this agent in the next instance of the environment model
	 */
	public void setMyStimulusAnswer(Object myNextState);
	
	/**
	 * This method will be invoked in case that the interface should be finalized.
	 */
	public void onEnd();
	
}
