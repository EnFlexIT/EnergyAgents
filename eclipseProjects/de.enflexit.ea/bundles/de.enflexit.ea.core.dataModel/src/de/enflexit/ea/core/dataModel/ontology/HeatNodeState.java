package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatNodeState
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
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
