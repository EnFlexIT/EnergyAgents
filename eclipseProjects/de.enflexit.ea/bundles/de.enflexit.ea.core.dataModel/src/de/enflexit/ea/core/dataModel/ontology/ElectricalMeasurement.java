package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ElectricalMeasurement
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
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
