package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: VoltageBand
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class VoltageBand implements Concept {

   /**
* Protege name: voltageMax
   */
   private float voltageMax;
   public void setVoltageMax(float value) { 
    this.voltageMax=value;
   }
   public float getVoltageMax() {
     return this.voltageMax;
   }

   /**
* Protege name: voltageMin
   */
   private float voltageMin;
   public void setVoltageMin(float value) { 
    this.voltageMin=value;
   }
   public float getVoltageMin() {
     return this.voltageMin;
   }

   /**
* Protege name: voltageAvg
   */
   private float voltageAvg;
   public void setVoltageAvg(float value) { 
    this.voltageAvg=value;
   }
   public float getVoltageAvg() {
     return this.voltageAvg;
   }

}
