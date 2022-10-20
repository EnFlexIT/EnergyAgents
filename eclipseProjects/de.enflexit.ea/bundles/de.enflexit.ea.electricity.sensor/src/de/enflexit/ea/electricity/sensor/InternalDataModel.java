package de.enflexit.ea.electricity.sensor;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.dataModel.phonebook.EnergyAgentPhoneBookEntry;
import energy.FixedVariableList;
import jade.core.Agent;

/**
 * The Class InternalDataModel represents the whole internal data model of the corresponding agent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class InternalDataModel extends AbstractInternalDataModel<EnergyAgentPhoneBookEntry> {

	private static final long serialVersionUID = 8589606262871989270L;
	
	// --- Threshold values for sending a new measurement message to district agents
	public float dMaxVoltageDeviation = 5;
	public float dMaxCurrentDeviation = 5;
	

	protected MeasurementSubscriptionResponder measurementSubscriptionResponder;
	
	FixedVariableList measurementHistory;
	
	/**
	 * Gets the measurement subscription responder.
	 *
	 * @param myAgent the my agent
	 * @return the measurement subscription responder
	 */
	public abstract MeasurementSubscriptionResponder getMeasurementSubscriptionResponder(Agent myAgent);
	
	/**
	 * Instantiates a new data model.
	 * @param myAgent the my agent
	 */
	public InternalDataModel(AbstractEnergyAgent myAgent) {
		super(myAgent);
		this.setLoggingMode(LoggingMode.ON_VALUE_CHANGE);
	}

	/**
	 * Gets the measurement history.
	 * @return the measurement history
	 */
	public FixedVariableList getMeasurementHistory() {
		return measurementHistory;
	}
	/**
	 * Sets the measurement history.
	 * @param measurementHistory the new measurement history
	 */
	public void setMeasurementHistory(FixedVariableList measurementHistory) {
		this.measurementHistory = measurementHistory;
	}
	
	/**
	 * Sets the measurement subscription responder.
	 * @param measurementSubscriptionResponder the new measurement subscription responder
	 */
	public void setMeasurementSubscriptionResponder(MeasurementSubscriptionResponder measurementSubscriptionResponder) {
		this.measurementSubscriptionResponder = measurementSubscriptionResponder;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.AbstractInternalDataModel#getPhoneBookEntryClass()
	 */
	@Override
	protected Class<EnergyAgentPhoneBookEntry> getPhoneBookEntryClass() {
		return EnergyAgentPhoneBookEntry.class;
	}
}
