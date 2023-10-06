package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TriPhaseElectricalNodeState
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class TriPhaseElectricalNodeState extends ElectricalNodeState{ 

//////////////////////////// User code
public UniPhaseElectricalNodeState getL1NodeStateNotNull() {
	if (this.l1 == null) this.l1 = new UniPhaseElectricalNodeState();
	return this.l1;
}

public UniPhaseElectricalNodeState getL2NodeStateNotNull() {
	if (this.l2 == null) this.l2 = new UniPhaseElectricalNodeState();
	return this.l2;
}

public UniPhaseElectricalNodeState getL3NodeStateNotNull() {
	if (this.l3 == null) this.l3 = new UniPhaseElectricalNodeState();
	return this.l3;
}
   /**
* Protege name: l1
   */
   private UniPhaseElectricalNodeState l1;
   public void setL1(UniPhaseElectricalNodeState value) { 
    this.l1=value;
   }
   public UniPhaseElectricalNodeState getL1() {
     return this.l1;
   }

   /**
* Protege name: l3
   */
   private UniPhaseElectricalNodeState l3;
   public void setL3(UniPhaseElectricalNodeState value) { 
    this.l3=value;
   }
   public UniPhaseElectricalNodeState getL3() {
     return this.l3;
   }

   /**
* Protege name: l2
   */
   private UniPhaseElectricalNodeState l2;
   public void setL2(UniPhaseElectricalNodeState value) { 
    this.l2=value;
   }
   public UniPhaseElectricalNodeState getL2() {
     return this.l2;
   }

}
