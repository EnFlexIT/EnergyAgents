package de.enflexit.ea.electricity.aggregation.triPhase;

import de.enflexit.ea.core.aggregation.AbstractSubNetworkConfiguration;
import de.enflexit.ea.core.dataModel.ontology.SlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSlackNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSlackNodeState;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkCalculationStrategy;
import de.enflexit.ea.electricity.aggregation.AbstractElectricalNetworkConfiguration;
import de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler;
import energy.domain.DefaultDomainModelElectricity.Phase;
import energy.optionModel.FixedDouble;
import energy.optionModel.TechnicalSystemStateEvaluation;

/**
 * The Class ThriPhaseElectricalSlackNodeHandler.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class TriPhaseElectricalSlackNodeHandler extends AbstractSlackNodeHandler {

	private TriPhaseSlackNodeState slackNodeState;
	
	/**
	 * Instantiates a new TriPhaseElectricalSlackNodeHandler.
	 * @param electricalNetworkCalculationStrategy the electrical network calculation strategy
	 */
	public TriPhaseElectricalSlackNodeHandler(AbstractElectricalNetworkCalculationStrategy electricalNetworkCalculationStrategy) {
		super(electricalNetworkCalculationStrategy);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getDefaultVoltageLevel()
	 */
	@Override
	public float getDefaultVoltageLevel() {
		AbstractSubNetworkConfiguration subNetConfig = this.getSubNetworkConfiguration();
		if (subNetConfig!=null && subNetConfig instanceof AbstractElectricalNetworkConfiguration) {
			double ratedVoltageLevel = ((AbstractElectricalNetworkConfiguration)subNetConfig).getConfiguredRatedVoltageFromNetwork();
			return (float) (ratedVoltageLevel / Math.sqrt(3));
		}
		// --- Just as a (hopefully not necessary) backup -----------
		return (float) (400.0 / Math.sqrt(3));
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#setSlackNodeState(de.enflexit.ea.core.dataModel.ontology.SlackNodeState)
	 */
	@Override
	public void setSlackNodeState(SlackNodeState slackNodeState) {
		if (slackNodeState instanceof TriPhaseSlackNodeState) {
			TriPhaseSlackNodeState newState = (TriPhaseSlackNodeState) slackNodeState;
			if (newState.equals(this.getSlackNodeState())==false) {
				this.slackNodeState = (TriPhaseSlackNodeState) slackNodeState;
				this.setChangedSlackNodeState(true);
			}
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeState()
	 */
	@Override
	public TriPhaseSlackNodeState getSlackNodeState() {
		if (slackNodeState==null) {
			slackNodeState = createTriPhaseSlackNodeState();

			slackNodeState.getSlackNodeStateL1().getVoltageReal().setValue(this.getInitialVoltageLevel());
			slackNodeState.getSlackNodeStateL1().getVoltageImag().setValue(0);
			
			slackNodeState.getSlackNodeStateL2().getVoltageReal().setValue(this.getInitialVoltageLevel());
			slackNodeState.getSlackNodeStateL2().getVoltageImag().setValue(0);
			
			slackNodeState.getSlackNodeStateL3().getVoltageReal().setValue(this.getInitialVoltageLevel());
			slackNodeState.getSlackNodeStateL3().getVoltageImag().setValue(0);
		}
		return slackNodeState;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeState(energy.domain.DefaultDomainModelElectricity.Phase)
	 */
	@Override
	public UniPhaseSlackNodeState getSlackNodeState(Phase phase) {
		
		UniPhaseSlackNodeState upsns = null;
		switch (phase) {
		case L1:
			upsns = this.getSlackNodeState().getSlackNodeStateL1();
			break;
		case L2:
			upsns = this.getSlackNodeState().getSlackNodeStateL2();
			break;
		case L3:
			upsns = this.getSlackNodeState().getSlackNodeStateL3();
			break;
		default:
			break;
		}
		return upsns;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeStateFromLastTransformerSystemState(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public SlackNodeState getSlackNodeStateFromLastTransformerState(TechnicalSystemStateEvaluation tsseLast) {
		return getTriPhaseSlackNodeStateFromTechnicalSystemStateEvaluation(tsseLast);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.aggregation.AbstractSlackNodeHandler#getSlackNodeStateFromLastSensorState(energy.optionModel.TechnicalSystemStateEvaluation)
	 */
	@Override
	public SlackNodeState getSlackNodeStateFromLastSensorState(TechnicalSystemStateEvaluation tsseLast) {
		
		float errorIndicatingValue = 0;
		TriPhaseSlackNodeState tpSns = createTriPhaseSlackNodeState(errorIndicatingValue);
		
		for (int i = 0; i < tsseLast.getIOlist().size(); i++) {
			FixedDouble fvIO = (FixedDouble) tsseLast.getIOlist().get(i);
			if (fvIO.getVariableID().equals("Voltage L1")) {
				tpSns.getSlackNodeStateL1().getVoltageReal().setValue((float)fvIO.getValue());
				tpSns.getSlackNodeStateL1().getVoltageReal().setValue(0f);
			}
			if (fvIO.getVariableID().equals("Voltage L2")) {
				tpSns.getSlackNodeStateL2().getVoltageReal().setValue((float)fvIO.getValue());
				tpSns.getSlackNodeStateL2().getVoltageImag().setValue(0f);
			}
			if (fvIO.getVariableID().equals("Voltage L3")) {
				tpSns.getSlackNodeStateL3().getVoltageReal().setValue((float)fvIO.getValue());
				tpSns.getSlackNodeStateL3().getVoltageImag().setValue(0f);
			}
		} 
		
		// --- Check for invalid slack node state -------------------
		if (isErrorInTriPhaseSlackNodeState(tpSns, errorIndicatingValue)) {
			tpSns = null;
		}
		return tpSns;
	}

}
