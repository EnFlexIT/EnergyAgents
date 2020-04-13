package de.enflexit.ea.prosumer;

import de.enflexit.ea.core.AbstractIOReal;
import de.enflexit.ea.core.EnergyAgentIO;
import energy.FixedVariableList;

/**
 * The Class IOReal can be used for real measurements on physical hardware.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class IOReal extends AbstractIOReal implements EnergyAgentIO {

	private static final long serialVersionUID = 3659353219575016108L;
	
	/**
	 * Instantiates this behaviour.
	 * @param agent the agent
	 */
	public IOReal(ProsumerAgent agent) {
		super(agent);
	}

	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		// Do something cyclic here in order to gather measurements 
	}
	
	/* (non-Javadoc)
	 * @see smartHouse.agent.internalDataModel.AgentIO#getInputMeasurements()
	 */
	@Override
	public FixedVariableList getMeasurementsFromSystem() {
		return null;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.energyAgent.core.EnergyAgentIO#setMeasurementsFromSystem(de.enflexit.energyAgent.core.FixedVariableList)
	 */
	@Override
	public void setMeasurementsFromSystem(FixedVariableList newmeasurements) {
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
