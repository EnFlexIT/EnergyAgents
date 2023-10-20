package de.enflexit.ea.core.dataModel.simulation;

import agentgui.simulationService.behaviour.SimulationServiceBehaviour;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE_CONFIRMATION;
import jade.core.Agent;
import jade.core.Location;

/**
 * Superclass for {@link SimulationServiceBehaviour}s that are supposed to actively interact with the simulation manager.
 * Takes care of all administrative interactions, so the developer can focus on the actual task.
 * 
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public abstract class AbstractSimulationIntegrationBehaviour extends SimulationServiceBehaviour {

	private static final long serialVersionUID = -5702357789764106868L;
	
	/**
	 * Instantiates a new abstract simulation integration behaviour.
	 * @param agent the agent
	 */
	public AbstractSimulationIntegrationBehaviour(Agent agent) {
		this(agent, false);
	}

	/**
	 * Instantiates a new abstract simulation integration behaviour.
	 * @param agent the agent
	 * @param passive the passive
	 */
	public AbstractSimulationIntegrationBehaviour(Agent agent, boolean passive) {
		super(agent, passive);
		this.initialize();
	}
	
	private void initialize() {
		this.sendManagerNotification(STATE_CONFIRMATION.Initialized);
		
		if (this.performSetupTasks()==true) {
			// --- Everything fine, let's get started ---------------
			this.sendManagerNotification(STATE_CONFIRMATION.Done);
		} else {
			// --- Something went wrong -----------------------------
			this.sendManagerNotification(STATE_CONFIRMATION.Error);
		}
	}
	
	/**
	 * Override this method to implement behaviour-specific setup tasks.
	 * @return true, if successful
	 */
	protected boolean performSetupTasks() {
		return true;
	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setPauseSimulation(boolean)
	 */
	@Override
	public void setPauseSimulation(boolean isPauseSimulation) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#setMigration(jade.core.Location)
	 */
	@Override
	public void setMigration(Location newLocation) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentStimulus()
	 */
	@Override
	public final void onEnvironmentStimulus() {
		Object stateObject = this.performSimulationStepTasks();
		this.setMyStimulusAnswer(stateObject);
	}
	
	/**
	 * This method performs the actual task that should be done in a simulation step. The returned object
	 * will be sent to the SimulationManager as stimulus answer (may be null if no answer object is needed.
	 * @return the object
	 */
	public abstract Object performSimulationStepTasks();

}
