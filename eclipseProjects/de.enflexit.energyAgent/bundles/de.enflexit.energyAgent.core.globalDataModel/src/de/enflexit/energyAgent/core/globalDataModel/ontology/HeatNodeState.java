package de.enflexit.energyAgent.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatNodeState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class HeatNodeState extends FluidNodeState{ 

   /**
* Protege name: temperatureSetPoint
   */
   private float temperatureSetPoint;
   public void setTemperatureSetPoint(float value) { 
    this.temperatureSetPoint=value;
   }
   public float getTemperatureSetPoint() {
     return this.temperatureSetPoint;
   }

}
