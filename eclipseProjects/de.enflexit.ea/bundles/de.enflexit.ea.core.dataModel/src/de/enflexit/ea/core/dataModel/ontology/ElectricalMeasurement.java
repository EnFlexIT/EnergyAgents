package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ElectricalMeasurement
* @author ontology bean generator
* @version 2021/02/15, 12:09:03
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
