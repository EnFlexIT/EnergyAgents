package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatNodeState
* @author ontology bean generator
* @version 2020/09/18, 12:50:27
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
