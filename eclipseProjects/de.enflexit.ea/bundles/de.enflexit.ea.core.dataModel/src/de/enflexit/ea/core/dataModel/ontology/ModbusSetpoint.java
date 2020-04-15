package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ModbusSetpoint
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class ModbusSetpoint implements Concept {

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

}
