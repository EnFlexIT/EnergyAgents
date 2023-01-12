package de.enflexit.ea.core;

import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import energy.EomController;
import energy.FixedVariableList;
import energy.OptionModelController;
import energy.calculations.AbstractEvaluationCalculation;
import energy.calculations.AbstractOptionModelCalculation;
import energygroup.GroupController;

/**
 * The Class EnergyAgentConnector can be used to connect specific option model calculations
 * or evaluation calculations to a specific energy agent. Thus, it allows to access the energy agent and 
 * its internal state (e.g. the internal data model or the current measurements from the system).
 *
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @param <EnergyAgent> the type of the Energy Agent
 * @param <InternalDataModel> the type of the Internal data model 
 * 
 * @see AbstractOptionModelCalculation
 * @see AbstractEvaluationCalculation
 */
public class EnergyAgentConnector<EnergyAgent extends AbstractEnergyAgent, InternalDataModel extends AbstractInternalDataModel<? extends EnergyAgentPhoneBookEntry>> {

	private EomController eomController;
	
	/**
	 * Instantiates a new abstract energy agent connector.
	 * @param omc the OptionModelController that is under control of the current Energy Agent
	 */
	public EnergyAgentConnector(OptionModelController omc){
		this.eomController = omc;
	}
	
	/**
	 * Instantiates a new abstract energy agent connector.
	 * @param gc the GroupCopntroll that is under control of the current  Energy Agent
	 */
	public EnergyAgentConnector(GroupController gc){
		this.eomController = gc;
	}
	
	/**
	 * Returns the current energy agent instance.
	 * @return the energy agent
	 */
	@SuppressWarnings("unchecked")
	public EnergyAgent getEnergyAgent() {
		if (this.eomController!=null && this.eomController.getControllingEntity()!=null) {
			Object ce = this.eomController.getControllingEntity();
			if (ce instanceof AbstractEnergyAgent) {
				return (EnergyAgent) ce;
			}
		}
		return null;
	}
	/**
	 * Returns the internal data model of the current energy agent instance.
	 * @return the internal data model
	 */
	@SuppressWarnings("unchecked")
	public InternalDataModel getInternalDataModel() {
		EnergyAgent ea = this.getEnergyAgent();
		if (ea!=null) {
			return (InternalDataModel) ea.getInternalDataModel();
		}
		return null;
	}
	
	/**
	 * Returns the measurements from system.
	 * @return the measurements from system
	 */
	public FixedVariableList getMeasurementsFromSystem() {
		InternalDataModel intDM = this.getInternalDataModel();
		if (intDM!=null) {
			return intDM.getMeasurementsFromSystem();
		}
		return null;
	}
	
}
