package de.enflexit.ea.electricity.sensor.mvSensor;

import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseSensorState;
import de.enflexit.ea.electricity.blackboard.CurrentLevelAnswer;
import de.enflexit.ea.electricity.blackboard.VoltageLevelAnswer;
import energy.FixedVariableList;
import energy.optionModel.FixedDouble;

/**
 * The Class IOSimulated is used to simulate measurements from an energy conversion 
 * process, if the current project setup is used for simulations.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 * @author Nils Loose - SOFTEC - Paluno - University of Duisburg-Essen
 */
public class IOSimulated extends de.enflexit.ea.electricity.sensor.IOSimulated {

	private static final long serialVersionUID = 3659353219575016108L;
	
	/**
	 * Instantiates a new simulated IO behaviour for the {@link MVSensorAgent}.
	 * @param agent the current {@link MVSensorAgent}
	 */
	public IOSimulated(MVSensorAgent agent) {
		super(agent);
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.sensor.IOSimulated#commitMeasurement()
	 */
	@Override
	protected void commitMeasurement() {
		UniPhaseElectricalNodeState nodeState = (UniPhaseElectricalNodeState) this.getNodeStateAnswer();
		UniPhaseSensorState edgeState = (UniPhaseSensorState) this.getEdgeStateAnswer();
		
		// --- Create the list of measurements ----------------------------
		FixedVariableList fvList = new FixedVariableList();
		
		FixedDouble fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.VOLTAGE);
		fd.setValue(nodeState.getVoltageAbs().getValue());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.CURRENT);
		fd.setValue(edgeState.getCurrent().getValue());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.COSPHI);
		fd.setValue(edgeState.getCosPhi());
		fvList.add(fd);
		
		// --- Set this list as the new measurements from the system ------
		this.setMeasurementsFromSystem(fvList);
		this.commitMeasurementsToAgent();
	}


	@Override
	protected void processVoltageLevelAnswer(VoltageLevelAnswer vla) {
		// --- Check if the answer is of the right type, remember if so -------
		ElectricalNodeState nodeState = vla.getElectricalNodeState();
		if (nodeState instanceof UniPhaseElectricalNodeState) {
			this.setNodeStateAnswer(nodeState);
		}
	}


	@Override
	protected void processCurrentLevelAnswer(CurrentLevelAnswer cla) {
		// --- Check if the answer is of the right type, remember if so -------
		CableState cableState = cla.getCableState();
		if (cableState instanceof UniPhaseSensorState) {
			this.setEdgeStateAnswer(cableState);
		}
	}
	
}