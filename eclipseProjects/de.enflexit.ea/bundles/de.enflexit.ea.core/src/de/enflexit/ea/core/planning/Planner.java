package de.enflexit.ea.core.planning;

import de.enflexit.common.Observable;
import de.enflexit.common.Observer;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import energy.planning.EomPlanner;
import energy.planning.EomPlannerEvent;
import energy.planning.EomPlannerEvent.PlannerEventType;
import energy.planning.EomPlannerListener;

/**
 * The Class Planner provides the planning capabilities of an energy agent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class Planner extends EomPlanner {

	protected AbstractEnergyAgent energyAgent;
	protected AbstractInternalDataModel<?> internalDataModel;
	
	/**
	 * Instantiates a new planner instance for an energy agent.
	 * @param energyAgent the energy agent
	 */
	public Planner(AbstractEnergyAgent energyAgent) {
		this(energyAgent, null);
	}
	/**
	 * Instantiates a new planner instance for the energy agent.
	 *
	 * @param energyAgent the energy agent
	 * @param pl the EomPlannerListener to register at the planner
	 */
	public Planner(AbstractEnergyAgent energyAgent, EomPlannerListener pl) {
		if (energyAgent==null) throw new NullPointerException("The energy agent instance is not allowed to be null!");
		this.energyAgent = energyAgent;
		if (pl!=null) this.addPlannerListener(pl);
		this.setControllingEntityName(this.energyAgent.getLocalName());
		this.initialize();
	}
	/**
	 * Returns the internal data model of the current energy agent.
	 * @return the internal data model
	 */
	private AbstractInternalDataModel<?> getInternalDataModel() {
		if (internalDataModel==null) {
			internalDataModel = this.energyAgent.getInternalDataModel(); 
		}
		return internalDataModel;
	}
	/**
	 * Initialize.
	 */
	private void initialize() {

		try {
			// --- Create model copy in new local EomController ----- 
			switch (this.getInternalDataModel().getTypeOfControlledSystem()) {
			case TechnicalSystem:
				this.getOptionModelController().setTechnicalSystem(this.getInternalDataModel().getOptionModelController().getTechnicalSystemCopy());
				break;
				
			case TechnicalSystemGroup:
				this.getGroupController().setTechnicalSystemGroup(this.getInternalDataModel().getGroupController().getTechnicalSystemGroupCopy());
				break;
				
			case None:
				this.waitForControlledSystem();
				break;
			}
			
			// --- Notify about initialization ----------------------
			this.notifyPlannerListener(new EomPlannerEvent(PlannerEventType.Initialized));
			
		} catch (Exception ex) {
			this.print("Planner initiation failed!", isDoDryRun());
			ex.printStackTrace();
			// --- Notify about failed initialization ---------------
			this.notifyPlannerListener(new EomPlannerEvent(PlannerEventType.InitializationFailed));
			
		}
	}
	/**
	 * Will register as observer of the current internal data model and wait for a controlled system.
	 */
	private void waitForControlledSystem() {
		
		this.getInternalDataModel().addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object updateObject) {
				// --- Got a NetworkComponent ? -------------------------------  
				if (updateObject==AbstractInternalDataModel.CHANGED.NETWORK_COMPONENT) {
					if (Planner.this.getControlledSystemType()!=ControlledSystemType.None) {
						Planner.this.getInternalDataModel().deleteObserver(this);
						Planner.this.initialize();
					}
				}
			}
		});
	}
	
}
