package de.enflexit.ea.core.dataModel.simulation;

import agentgui.simulationService.behaviour.SimulationServiceBehaviour;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.absEnvModel.SimulationStatus.STATE;
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
	
	private boolean sendStimulusAnswer;
	
	
	/**
	 * Instantiates a new abstract simulation integration behaviour.
	 * @param agent the agent executing this behaviour
	 */
	public AbstractSimulationIntegrationBehaviour(Agent agent) {
		this(agent, false);
	}
	/**
	 * Instantiates a new abstract simulation integration behaviour.
	 * @param agent the agent executing this behaviour
	 * @param passive specifies if the simulation sensor is passive
	 */
	public AbstractSimulationIntegrationBehaviour(Agent agent, boolean passive) {
		this(agent, passive, true);
	}
	/**
	 * Instantiates a new abstract simulation integration behaviour.
	 * @param agent the agent executing this behaviour
	 * @param passive specifies if the simulation sensor is passive
	 * @param sendStimulusAnswer specifies if the stimulus answer is sent automatically after performSimulaitonStepTasks is done. If set to false, this must be done in the sub class. 
	 */
	public AbstractSimulationIntegrationBehaviour(Agent agent, boolean passive, boolean sendStimulusAnswer) {
		super(agent, passive);
		this.sendStimulusAnswer = sendStimulusAnswer;
		this.initialize();
	}
	
	/**
	 * Handles the manager notifications for the initialization process.
	 */
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
	 * Returns the HyGridAbstractEnvironmentModel currently provided by the SimulationServcie.
	 * @return the current HyGridAbstractEnvironmentModel
	 */
	protected HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		if (this.myEnvironmentModel!=null) {
			return (HyGridAbstractEnvironmentModel) this.myEnvironmentModel.getAbstractEnvironment();
		}
		return null;
	}
	/**
	 * Returns the current simulation state.
	 * @return the simulation state
	 * @see STATE
	 */
	protected STATE getSimulationState() {
		HyGridAbstractEnvironmentModel hyGridAbsEnvModel = this.getHyGridAbstractEnvironmentModel();
		if (hyGridAbsEnvModel!=null) {
			return hyGridAbsEnvModel.getSimulationStatus().getState();
		}
		return null;
	}
	
	/**
	 * Override this method to implement behaviour-specific setup tasks.
	 * @return true, if successfully done
	 */
	protected boolean performSetupTasks() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#onEnvironmentStimulus()
	 */
	@Override
	public final void onEnvironmentStimulus() {
		
		// ---- Check if the simulation is finalized ----------------
		STATE simulationState = this.getSimulationState();
		if (simulationState!=null && simulationState==STATE.C_StopSimulation) {
			this.finalizeSimulation();
			this.sendManagerNotification(STATE_CONFIRMATION.Done);
			return;
		}
		
		// --- Do the discrete simulation step tasks ----------------
		Object stateObject = this.performSimulationStepTasks();
		if (this.sendStimulusAnswer==true) {
			this.setMyStimulusAnswer(stateObject);
		}
	}
	/**
	 * This method performs the actual task that should be done in a simulation step. The returned object
	 * will be sent to the SimulationManager as a so-called stimulus answer (may be null if no answer object is needed).
	 * @return the object
	 */
	public abstract Object performSimulationStepTasks();

	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.sensoring.ServiceSensorInterface#setPauseSimulation(boolean)
	 */
	@Override
	public void setPauseSimulation(boolean isPauseSimulation) { }
	
	/* (non-Javadoc)
	 * @see agentgui.simulationService.behaviour.SimulationServiceBehaviour#setMigration(jade.core.Location)
	 */
	@Override
	public void setMigration(Location newLocation) { }
	
	/**
	 * Will be called if the simulation is about to be finalized. Overwrite this 
	 * method, if you want to do finalization tasks.
	 */
	public void finalizeSimulation() {};
	
}
