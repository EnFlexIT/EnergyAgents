package de.enflexit.ea.core.simulation.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import agentgui.simulationService.environment.AbstractDiscreteSimulationStep.DiscreteSystemStateType;
import agentgui.simulationService.environment.EnvironmentModel;
import de.enflexit.ea.core.aggregation.AbstractAggregationHandler;
import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel.TimeModelType;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStep;
import de.enflexit.ea.core.dataModel.simulation.DiscreteSimulationStepCentralDecision;
import de.enflexit.ea.core.simulation.decisionControl.AbstractCentralDecisionProcess;
import jade.core.AID;

/**
 * The {@link AbstractAggregationHandler} implementation to be used by the {@link SimulationManager}
 * 
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class AggregationHandler extends AbstractAggregationHandler {

	private HashSet<String> realTimeControlledSystems;
	
	private HashSet<String> discreteIteratingSystems;
	private HashMap<String, DiscreteSystemStateType> discreteIteratingSystemsStateTypeLog;
	
	private AbstractCentralDecisionProcess centralDecisionProcess;
	
	/**
	 * Instantiates the aggregation handler for the {@link SimulationManager}.
	 *
	 * @param environmentModel the environment model
	 * @param headlessOperation true for operation without visual representation
	 * @param ownerName the owner name
	 */
	public AggregationHandler(EnvironmentModel environmentModel, boolean headlessOperation, String ownerName) {
		super(environmentModel, headlessOperation, ownerName);
		//this.debugIsSkipActualNetworkCalculation(true);
	}

	/* (non-Javadoc)
	 * @see hygrid.aggregation.AbstractAggregationHandler#getSubnetworkConfiguration()
	 */
	@Override
	protected ArrayList<AbstractSubNetworkConfiguration> getConfigurationOfSubNetworks() {
		return null;
	}

	
	// ----------------------------------------------------------------------------------
	// --- From here, methods and extensions for discrete simulations steps -------------
	// ----------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractAggregationHandler#setAgentAnswers(java.util.Hashtable)
	 */
	@Override
	public void setAgentAnswers(Hashtable<AID, Object> agentAnswers) {
		
		// --- Clear list of pending systems for a discrete simulation -------- 
		if (this.getHyGridAbstractEnvironmentModel().getTimeModelType()==TimeModelType.TimeModelDiscrete) {
			this.getDiscreteIteratingSystemsStateTypeLog().clear();
		}
		super.setAgentAnswers(agentAnswers);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.aggregation.AbstractAggregationHandler#appendToNetworkComponentsScheduleController(java.lang.String, java.lang.Object)
	 */
	@Override
	public void appendToNetworkComponentsScheduleController(String networkComponentID, Object updateObject) {
		
		if (updateObject instanceof DiscreteSimulationStep) {
			
			// ------------------------------------------------------------------------------------
			// --- Discrete Simulation: Got DiscreteSimulationStep for NetworkComponent -----------
			// ------------------------------------------------------------------------------------
			DiscreteSimulationStep dsStep = (DiscreteSimulationStep) updateObject;
			if (dsStep instanceof DiscreteSimulationStepCentralDecision) {
				// --- Register as discrete iterating system --------------------------------------
				this.registerDiscreteIteratingSystem(networkComponentID);
				// --- Place system variability in decision process -------------------------------
				AbstractCentralDecisionProcess cdProcess = this.getCentralDecisionProcess();
				if (cdProcess!=null) {
					DiscreteSimulationStepCentralDecision dsStepCD = (DiscreteSimulationStepCentralDecision) dsStep;
					cdProcess.registerDiscreteSimulationStepCentralDecision(networkComponentID, dsStepCD);
				}
				
			} else {
				// -- Put state into state log? ---------------------------------------------------
				if (this.getDiscreteIteratingSystems().contains(networkComponentID)==true) {
					// -- System belongs to the iterating systems => put into state log -----------
					this.getDiscreteIteratingSystemsStateTypeLog().put(networkComponentID, dsStep.getDiscreteSystemStateType());
				}
			}
			
			// --- Got a new system state from a part of the network ------------------------------
			this.appendToNetworkComponentsScheduleController(networkComponentID, dsStep.getSystemState());

		} else {
			super.appendToNetworkComponentsScheduleController(networkComponentID, updateObject);
		}
	}
	
	
	/**
	 * Registers the specified real time controlled system.
	 * @param localName the local name
	 */
	public void registerRealTimeControlledSystem(String localName) {
		this.getRealTimeControlledSystems().add(localName);
	}
	/**
	 * Returns all systems that are under control of a real time strategy.
	 * @return the real time controlled systems
	 */
	public HashSet<String> getRealTimeControlledSystems() {
		if (realTimeControlledSystems==null) {
			realTimeControlledSystems = new HashSet<String>();
		}
		return realTimeControlledSystems;
	}
	
	/**
	 * Registers the specified discrete iterating system.
	 * @param localName the local name
	 */
	public void registerDiscreteIteratingSystem(String localName) {
		this.getDiscreteIteratingSystems().add(localName);
	}
	/**
	 * Holds all systems that are working with discrete iterating RT strategies.
	 * @return the discrete iterating systems
	 */
	public HashSet<String> getDiscreteIteratingSystems() {
		if (discreteIteratingSystems==null) {
			discreteIteratingSystems = new HashSet<String>();
		}
		return discreteIteratingSystems;
	}
	/**
	 * Answers the question, if there is any iterating system in this discrete simulation.
	 * @return true, if is iterating system
	 */
	public boolean isIteratingSystem() {
		return this.getDiscreteIteratingSystems().size()>0;
	}
	
	/**
	 * Return the state type log for discrete iterating systems.
	 * @return the discrete iterating systems state type log
	 */
	public HashMap<String, DiscreteSimulationStep.DiscreteSystemStateType> getDiscreteIteratingSystemsStateTypeLog() {
		if (discreteIteratingSystemsStateTypeLog==null) {
			discreteIteratingSystemsStateTypeLog = new HashMap<String, DiscreteSimulationStep.DiscreteSystemStateType>();
		}
		return discreteIteratingSystemsStateTypeLog;
	}
	/**
	 * Clears discrete iterating systems state type log.
	 */
	public void clearDiscreteIteratingSystemsStateTypeLog() {
		this.getDiscreteIteratingSystemsStateTypeLog().clear();
	}
	/**
	 * Clears the discrete iterating systems state type log form iteration state types.
	 * This is used at the end of simulation part step, before the network calculation will be executed. 
	 */
	public void clearDiscreteIteratingSystemsStateTypeLogFromIterations() {
		
		List<String> isList = new ArrayList<>(this.getDiscreteIteratingSystemsStateTypeLog().keySet()); 
		for (int i = 0; i < isList.size(); i++) {
			String isName = isList.get(i);
			DiscreteSystemStateType stateType = this.getDiscreteIteratingSystemsStateTypeLog().get(isName);
			if (stateType==DiscreteSystemStateType.Iteration) {
				this.getDiscreteIteratingSystemsStateTypeLog().remove(isName);
			}
		}
	}
	/**
	 * Checks if iterating systems are pending in a single part sequence.
	 * @return true, if is pending iterating system
	 */
	public boolean isPendingIteratingSystemInPartSequence() {
		// --- Check if target answers in part sequence are reached -----------
		int noOfIteratingSystems = this.getDiscreteIteratingSystems().size();
		int noOfIteratingSystemsAnswered = this.getDiscreteIteratingSystemsStateTypeLog().values().size();
		return noOfIteratingSystemsAnswered < noOfIteratingSystems && noOfIteratingSystemsAnswered != noOfIteratingSystems;
	}
	/**
	 * Checks if iterating systems are pending in a discrete simulation step. 
	 * If <code>false</code>, the next discrete step for the overall simulation can be executed.
	 * @return true, if iterating systems are pending
	 */
	public boolean isPendingIteratingSystemInSimulationStep() {
		// --- Check if target answers in part sequence are reached -----------
		if (this.isPendingIteratingSystemInPartSequence()==true) return true;
		// --- Check if simulation step is finalized --------------------------
		return this.getDiscreteIteratingSystemsStateTypeLog().values().contains(DiscreteSystemStateType.Iteration);
	}

	
	// ----------------------------------------------------------------------------------
	// --- From here, methods for central decision are located --------------------------
	// ----------------------------------------------------------------------------------
	/**
	 * Checks if the current HyGridAbstractEnvironmentModel specifies a central controlled snapshot simulation.
	 * @return true, if is central controlled snapshot simulation
	 */
	public boolean isCentralSnapshotSimulation() {
		return AbstractCentralDecisionProcess.isCentralControlledSnapshotSimulation(this.getHyGridAbstractEnvironmentModel());
	}
	/**
	 * Checks if a system is pending in central snapshot simulation.
	 * @return true, if is pending system in central snapshot simulation
	 */
	public boolean isPendingSystemInCentralSnapshotSimulation() {
		if (this.isCentralSnapshotSimulation()==false) return false;
		
		AbstractCentralDecisionProcess cdProcess = this.getCentralDecisionProcess(); 
		if (cdProcess==null) return true;

		int noOfRTControlledSystems = this.getRealTimeControlledSystems().size();
		int noOfSystemsAnswered = cdProcess.getSystemsVariability().size();
		return noOfSystemsAnswered < noOfRTControlledSystems && noOfSystemsAnswered!=noOfRTControlledSystems;
	}
	
	/**
	 * Returns the central decision process if configured.
	 * @return the central decision process or null if not configured so   
	 */
	public AbstractCentralDecisionProcess getCentralDecisionProcess() {
		if (centralDecisionProcess==null) {
			// --- Check if we work in a central controlled snapshot simulation ---------
			if (this.isCentralSnapshotSimulation()==true) {
				String cdClass = this.getHyGridAbstractEnvironmentModel().getSnapshotCentralDecisionClass();
				try {
					centralDecisionProcess = AbstractCentralDecisionProcess.createCentralDecisionProcess(cdClass);
					if (centralDecisionProcess!=null) {
						centralDecisionProcess.setAggregationHandler(this);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return centralDecisionProcess;
	}
	
}
