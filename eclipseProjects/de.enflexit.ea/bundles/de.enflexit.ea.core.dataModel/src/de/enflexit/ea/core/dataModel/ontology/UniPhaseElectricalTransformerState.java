package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseElectricalTransformerState
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class UniPhaseElectricalTransformerState extends UniPhaseElectricalNodeState{ 

   /**
* Protege name: ratedVoltage
   */
   private UnitValue ratedVoltage;
   public void setRatedVoltage(UnitValue value) { 
    this.ratedVoltage=value;
   }
   public UnitValue getRatedVoltage() {
     return this.ratedVoltage;
   }

}
