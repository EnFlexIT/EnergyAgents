package de.enflexit.ea.electricity.aggregation.uniPhase;

import de.enflexit.ea.core.dataModel.ontology.SlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSlackNodeState;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class UniPhaseElectricalSlackNodeHandler.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class UniPhaseElectricalSlackNodeHandler extends AbstractSlackNodeHandler {

	private UniPhaseSlackNodeState slackNodeState;

	/**
	 * Instantiates a new UniPhaseElectricalSlackNodeHandler.
	 * @param electricalNetworkCalculationStrategy the electrical network calculation strategy
	 */
	public UniPhaseElectricalSlackNodeHandler(AbstractElectricalNetworkCalculationStrategy electricalNetworkCalculationStrategy) {
		super(electricalNetworkCalculationStrategy);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getDefaultVoltageLevel()
	 */
	@Override
	public float getDefaultVoltageLevel() {
		AbstractElectricalNetworkConfiguration subNetConfig = this.getElectricalNetworkConfiguration();
		if (subNetConfig!=null) {
			double ratedVoltageLevel = subNetConfig.getConfiguredRatedVoltageFromNetwork();
			return  (float) (ratedVoltageLevel / Math.sqrt(3));
		}
		// --- Just as a (hopefully not necessary) backup -----------
		return (float) (10000 / Math.sqrt(3));
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#setSlackNodeState(de.enflexit.ea.core.dataModel.ontology.SlackNodeState)
	 */
	@Override
	public void setSlackNodeState(SlackNodeState slackNodeState) {
		if (slackNodeState instanceof UniPhaseSlackNodeState) {
			UniPhaseSlackNodeState newState = (UniPhaseSlackNodeState) slackNodeState;
			if (newState.equals(this.getSlackNodeState())==false) {
				this.slackNodeState = (UniPhaseSlackNodeState) slackNodeState;
				this.setChangedSlackNodeState(true);
			}
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeState()
	 */
	@Override
	public UniPhaseSlackNodeState getSlackNodeState() {
		if (slackNodeState==null) {
			slackNodeState = createUniPhaseSlackNodeState();
			slackNodeState.getVoltageReal().setValue(this.getInitialVoltageLevel());
			slackNodeState.getVoltageImag().setValue(0);
		}
		return slackNodeState;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeState(energy.domain.DefaultDomainModelElectricity.Phase)
	 */
	@Override
	public UniPhaseSlackNodeState getSlackNodeState(Phase phase) {
		
		UniPhaseSlackNodeState upsns = null;
		if (phase==Phase.AllPhases) {
			upsns = this.getSlackNodeState();
		}
		return upsns;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeStateFromLastTransformerSystemState(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public SlackNodeState getSlackNodeStateFromLastTransformerState(TechnicalSystemStateEvaluation tsseLast) {
		return getUniPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(tsseLast, (double)this.getDefaultVoltageLevel() * Math.sqrt(3));
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeStateFromLastSensorState(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public SlackNodeState getSlackNodeStateFromLastSensorState(TechnicalSystemStateEvaluation tsseLast) {

		float errorIndicatingValue = 0;
		UniPhaseSlackNodeState upSns = createUniPhaseSlackNodeState(errorIndicatingValue);
		
		for (int i = 0; i < tsseLast.getIOlist().size(); i++) {
			FixedDouble fvIO = (FixedDouble) tsseLast.getIOlist().get(i);
			if (fvIO.getVariableID().equals("Voltage")) {
				upSns.getVoltageReal().setValue((float)fvIO.getValue());
				upSns.getVoltageReal().setValue(0f);
			}
		} 
		
		// --- Check for invalid slack node state -------------------
		if (isErrorInUniPhaseSlackNodeState(upSns, errorIndicatingValue)) {
			upSns = null;
		}
		return upSns;
	}

}
