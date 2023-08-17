package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ModbusSetpoint
* @author ontology bean generator
* @version 2023/07/13, 21:15:48
*/
public class ModbusSetpoint implements Concept {

   /**
* Protege name: setpointValue
   */
   private float setpointValue;
   public void setSetpointValue(float value) { 
    this.setpointValue=value;
   }
   public float getSetpointValue() {
     return this.setpointValue;
   }

   /**
* Protege name: registerIndex
   */
   private int registerIndex;
   public void setRegisterIndex(int value) { 
    this.registerIndex=value;
   }
   public int getRegisterIndex() {
     return this.registerIndex;
   }

}
