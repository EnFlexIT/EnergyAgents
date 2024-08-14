package de.enflexit.ea.electricity;

import de.enflexit.common.SerialClone;
import de.enflexit.ea.core.dataModel.ontology.ElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.TriPhaseElectricalNodeState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;

/**
 * The Class ElectricalNodeStateConverter provides static methods to convert {@link ElectricalNodeState}s
 * into UniPhaseElectricalNodeState or vice versa.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ElectricalNodeStateConverter {

	/**
	 * Converts a UniPhaseElectricalNodeState into a TriPhaseElectricalNodeState.
	 *
	 * @param elNodeState the el node state
	 * @return the tri phase electrical node state
	 */
	public static TriPhaseElectricalNodeState convertToTriPhaseElectricalNodeState(ElectricalNodeState elNodeState) {
		if (elNodeState instanceof UniPhaseElectricalNodeState) {
			return convertToTriPhaseElectricalNodeState((UniPhaseElectricalNodeState)elNodeState);
		}
		return (TriPhaseElectricalNodeState) elNodeState;
	}
	/**
	 * Converts a UniPhaseElectricalNodeState into a TriPhaseElectricalNodeState.
	 *
	 * @param upNodeState the up node state
	 * @return the tri phase electrical node state
	 */
	public static TriPhaseElectricalNodeState convertToTriPhaseElectricalNodeState(UniPhaseElectricalNodeState upNodeState) {
	
		if (upNodeState==null) return null;
		
		float p_i = (float) (upNodeState.getPNotNull().getValue() / 3.0);
		float q_i = (float) (upNodeState.getQNotNull().getValue() / 3.0);
		float s_i = (float) Math.sqrt(p_i* p_i + q_i * q_i);
		
		float cosPhi_i = p_i / s_i;
		float sinPhi_i = q_i / s_i;
		
		float voltageAbs_i = (float) (upNodeState.getVoltageAbsNotNull().getValue() / Math.sqrt(3));
		float voltageReal_i = voltageAbs_i * cosPhi_i;
		float voltageImag_i = voltageAbs_i * sinPhi_i;
		
		float current_i = (float) (p_i / voltageAbs_i);

		
		// --- Define and assign return type --------------
		TriPhaseElectricalNodeState tpNodeState = new TriPhaseElectricalNodeState();
		// --- Make copy of original to keep units --------
		UniPhaseElectricalNodeState upNodeStateL1 = SerialClone.clone(upNodeState);
		tpNodeState.setL1(upNodeStateL1);
		
		upNodeStateL1.getPNotNull().setValue(p_i);
		upNodeStateL1.getQNotNull().setValue(q_i);
		upNodeStateL1.getSNotNull().setValue(s_i);
		
		upNodeStateL1.setCosPhi(cosPhi_i);
		upNodeStateL1.getCurrentNotNull().setValue(current_i);
		upNodeStateL1.getVoltageAbsNotNull().setValue(voltageAbs_i);
		upNodeStateL1.getVoltageRealNotNull().setValue(voltageReal_i);
		upNodeStateL1.getVoltageImagNotNull().setValue(voltageImag_i);
		
		// --- Copy L1 for L2 and L3 ----------------------
		UniPhaseElectricalNodeState upNodeStateL2 = SerialClone.clone(upNodeStateL1);
		tpNodeState.setL2(upNodeStateL2);
		UniPhaseElectricalNodeState upNodeStateL3 = SerialClone.clone(upNodeStateL1);
		tpNodeState.setL3(upNodeStateL3);
				
		return tpNodeState;
	}
	
	
	
	/**
	 * Converts a TriPhaseElectricalNodeState into a UniPhaseElectricalNodeState.
	 *
	 * @param elNodeState the el node state
	 * @return the uni phase electrical node state
	 */
	public static UniPhaseElectricalNodeState convertToUniPhaseElectricalNodeState(ElectricalNodeState elNodeState) {
		if (elNodeState instanceof TriPhaseElectricalNodeState) {
			return convertToUniPhaseElectricalNodeState((TriPhaseElectricalNodeState)elNodeState);
		}
		return (UniPhaseElectricalNodeState) elNodeState;
	}
	/**
	 * Converts a TriPhaseElectricalNodeState into a UniPhaseElectricalNodeState.
	 *
	 * @param tpNodeState the tp node state
	 * @return the uni phase electrical node state
	 */
	public static UniPhaseElectricalNodeState convertToUniPhaseElectricalNodeState(TriPhaseElectricalNodeState tpNodeState) {
		
		if (tpNodeState==null) return null;

		float p_i = (float) (tpNodeState.getL1NodeStateNotNull().getPNotNull().getValue() + tpNodeState.getL2NodeStateNotNull().getPNotNull().getValue() + tpNodeState.getL3NodeStateNotNull().getPNotNull().getValue());
		float q_i = (float) (tpNodeState.getL1NodeStateNotNull().getQNotNull().getValue() + tpNodeState.getL2NodeStateNotNull().getQNotNull().getValue() + tpNodeState.getL3NodeStateNotNull().getQNotNull().getValue());
		float s_i = (float) Math.sqrt(p_i* p_i + q_i * q_i);
		
		float cosPhi_i = p_i / s_i;
		float sinPhi_i = q_i / s_i;
		
		float voltageAbsAvg = (float) ((tpNodeState.getL1NodeStateNotNull().getVoltageAbs().getValue() + tpNodeState.getL2NodeStateNotNull().getVoltageAbs().getValue() + tpNodeState.getL3NodeStateNotNull().getVoltageAbs().getValue()) / 3.0);
		
		float voltageAbs_i = (float) ((voltageAbsAvg) * Math.sqrt(3));
		float voltageReal_i = voltageAbs_i * cosPhi_i;
		float voltageImag_i = voltageAbs_i * sinPhi_i;
		
		float current_i = (float) (s_i / voltageAbs_i);

		
		// --- Define and assign return type --------------
		// --- Make copy of original to keep units --------
		UniPhaseElectricalNodeState upNodeState = SerialClone.clone(tpNodeState.getL1NodeStateNotNull());
		
		upNodeState.getPNotNull().setValue(p_i);
		upNodeState.getQNotNull().setValue(q_i);
		upNodeState.getSNotNull().setValue(s_i);
		
		upNodeState.setCosPhi(cosPhi_i);
		upNodeState.getCurrentNotNull().setValue(current_i);
		upNodeState.getVoltageAbsNotNull().setValue(voltageAbs_i);
		upNodeState.getVoltageRealNotNull().setValue(voltageReal_i);
		upNodeState.getVoltageImagNotNull().setValue(voltageImag_i);
		
		return upNodeState;
	}
	
}
