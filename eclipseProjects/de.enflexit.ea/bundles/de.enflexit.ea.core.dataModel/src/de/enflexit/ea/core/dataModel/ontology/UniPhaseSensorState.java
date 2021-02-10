package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseSensorState
* @author ontology bean generator
* @version 2021/02/9, 23:45:16
*/
public class UniPhaseSensorState extends UniPhaseCableState{ 

   /**
* Protege name: measuredVoltage
   */
   private UnitValue measuredVoltage;
   public void setMeasuredVoltage(UnitValue value) { 
    this.measuredVoltage=value;
   }
   public UnitValue getMeasuredVoltage() {
     return this.measuredVoltage;
   }

}
