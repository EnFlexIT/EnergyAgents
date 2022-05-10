package de.enflexit.ea.electricity.sensor.lvSensor;

import de.enflexit.ea.core.dataModel.ontology.CableState;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseSensorState;
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
	 * Instantiates a new simulated IO behaviour for the {@link LVSensorAgent}.
	 * @param agent the current {@link LVSensorAgent}
	 */
	public IOSimulated(LVSensorAgent agent) {
		super(agent);
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.electricity.sensor.IOSimulated#commitMeasurement()
	 */
	@Override
	protected void commitMeasurement() {
		
		// --- Get the answer states from the superclass ------------
		TriPhaseElectricalNodeState nodeState = (TriPhaseElectricalNodeState) this.getNodeStateAnswer();
		TriPhaseSensorState edgeState = (TriPhaseSensorState) this.getEdgeStateAnswer();
		
		// --- Create and populate the variable list ---------------- 
		FixedVariableList fvList = new FixedVariableList();
		
		FixedDouble fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.VOLTAGE_L1);
		fd.setValue(nodeState.getL1().getVoltageAbs().getValue());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.VOLTAGE_L2);
		fd.setValue(nodeState.getL2().getVoltageAbs().getValue());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.VOLTAGE_L3);
		fd.setValue(nodeState.getL3().getVoltageAbs().getValue());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.CURRENT_L1);
		fd.setValue(edgeState.getCurrent_L1());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.CURRENT_L2);
		fd.setValue(edgeState.getCurrent_L2());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.CURRENT_L3);
		fd.setValue(edgeState.getCurrent_L3());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.COSPHI_L1);
		fd.setValue(edgeState.getCosPhi_L1());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.COSPHI_L2);
		fd.setValue(edgeState.getCosPhi_L2());
		fvList.add(fd);
		
		fd = new FixedDouble();
		fd.setVariableID(InternalDataModel.COSPHI_L3);
		fd.setValue(edgeState.getCosPhi_L3());
		fvList.add(fd);
		
		// --- Set this list as the new measurements from the system ------------
		this.setMeasurementsFromSystem(fvList);
		this.commitMeasurementsToAgent();
	}
	
	@Override
	protected void processVoltageLevelAnswer(VoltageLevelAnswer vla) {
		// --- Check if the answer is of the right type, remember if so -------
		ElectricalNodeState nodeState = vla.getElectricalNodeState();
		if (nodeState instanceof TriPhaseElectricalNodeState) {
			this.setNodeStateAnswer(nodeState);
		}
	}

	@Override
	protected void processCurrentLevelAnswer(CurrentLevelAnswer cla) {
		// --- Check if the answer is of the right type, remember if so -------
		CableState cableState = cla.getCableState();
		if (cableState instanceof TriPhaseSensorState) {
			this.setEdgeStateAnswer(cableState);
		}
	}
}