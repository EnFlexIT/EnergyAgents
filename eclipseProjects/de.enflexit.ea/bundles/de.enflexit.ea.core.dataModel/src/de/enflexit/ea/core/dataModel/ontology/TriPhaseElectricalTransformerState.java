package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TriPhaseElectricalTransformerState
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class TriPhaseElectricalTransformerState extends TriPhaseElectricalNodeState{ 

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
