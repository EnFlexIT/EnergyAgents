package de.enflexit.energyAgent.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * This class describes the dynamic properties of a node component in a heat network
* Protege name: FluidNodeState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class FluidNodeState extends NodeComponentState{ 

   /**
* Protege name: pressure
   */
   private float pressure;
   public void setPressure(float value) { 
    this.pressure=value;
   }
   public float getPressure() {
     return this.pressure;
   }

   /**
* Protege name: temperature
   */
   private float temperature;
   public void setTemperature(float value) { 
    this.temperature=value;
   }
   public float getTemperature() {
     return this.temperature;
   }

}
