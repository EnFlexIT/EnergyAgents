package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ElectricalMeasurement
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class ElectricalMeasurement extends Measurement{ 

//////////////////////////// User code
public TriPhaseElectricalNodeState getElectricalNodeStateNotNull() {
	if (this.electricalNodeState== null) this.electricalNodeState = new TriPhaseElectricalNodeState(); 
	return this.electricalNodeState;
}
   /**
* Protege name: electricalNodeState
   */
   private TriPhaseElectricalNodeState electricalNodeState;
   public void setElectricalNodeState(TriPhaseElectricalNodeState value) { 
    this.electricalNodeState=value;
   }
   public TriPhaseElectricalNodeState getElectricalNodeState() {
     return this.electricalNodeState;
   }

}
