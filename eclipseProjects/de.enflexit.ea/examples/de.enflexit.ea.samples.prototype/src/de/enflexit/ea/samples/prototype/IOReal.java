package de.enflexit.ea.samples.prototype;

import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.EnergyAgentIO;
import energy.FixedVariableList;
import energy.optionModel.FixedDouble;

/**
 * The Class IOReal can be used for real measurements on physical hardware.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOReal extends AbstractIOReal implements EnergyAgentIO {

	private static final long serialVersionUID = -8020613469563083340L;

	private PrototypeAgent protAgent;

	private long cycleTime = 100;
	private FixedVariableList varList;
	
	/**
	 * Instantiates this behaviour.
	 * @param agent the agent
	 */
	public IOReal(PrototypeAgent agent) {
		super(agent);
		this.protAgent = agent;
	}

	/**
	 * Gets the current measurement.
	 * @return the current measurement
	 */
	private FixedVariableList getCurrentMeasurement() {
		
		// --- Access to the actual IO-Interface ----------
		double mVlotage = 88.88; // <= Hier Hanno :-)
		
		// --- Measure and add to the list ----------------
		FixedVariableList newMeasurements = new FixedVariableList();
		FixedDouble m1 = new FixedDouble();
		m1.setVariableID("Voltage");
		m1.setValue(mVlotage);
		newMeasurements.add(m1);
		
		return newMeasurements;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		long startTime = System.currentTimeMillis();
		this.setMeasurementsFromSystem(this.getCurrentMeasurement());
		long waitTime = cycleTime - System.currentTimeMillis() - startTime;
		block(waitTime);
	}
	
	/* (non-Javadoc)
	 * @see smartHouse.agent.internalDataModel.AgentIO#getInputMeasurements()
	 */
	@Override
	public FixedVariableList getMeasurementsFromSystem() {
		if (varList==null) {
			varList = this.getCurrentMeasurement();
		}
		return varList;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#setMeasurementsFromSystem(de.enflexit.energyAgent.core.FixedVariableList)
	 */
	@Override
	public void setMeasurementsFromSystem(FixedVariableList newMeasurements) {
		varList = newMeasurements;
		this.protAgent.getInternalDataModel().setMeasurementsFromSystem(newMeasurements);
	}

	/* (non-Javadoc)
	 * @see smartHouse.agent.internalDataModel.AgentIO#setOutputMeasurements(smartHouse.agent.internalDataModel.Measurements)
	 */
	@Override
	public void setSetPointsToSystem(FixedVariableList newOutputMeasurements) {

	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#getSetPointsToSystem()
	 */
	@Override
	public FixedVariableList getSetPointsToSystem() {
		return null;
	}

}
