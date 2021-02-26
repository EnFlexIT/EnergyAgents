package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TransformerNodeProperties
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class TransformerNodeProperties extends ElectricalNodeProperties{ 

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
