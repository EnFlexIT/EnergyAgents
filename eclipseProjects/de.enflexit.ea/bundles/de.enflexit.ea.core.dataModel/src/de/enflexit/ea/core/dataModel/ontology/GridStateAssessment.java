package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GridStateAssessment
* @author ontology bean generator
* @version 2023/07/13, 21:15:48
*/
public class GridStateAssessment implements Concept {

   /**
* Protege name: voltageTrafficLight
   */
   private TrafficLight voltageTrafficLight;
   public void setVoltageTrafficLight(TrafficLight value) { 
    this.voltageTrafficLight=value;
   }
   public TrafficLight getVoltageTrafficLight() {
     return this.voltageTrafficLight;
   }

   /**
* Protege name: utilizationTrafficLight
   */
   private TrafficLight utilizationTrafficLight;
   public void setUtilizationTrafficLight(TrafficLight value) { 
    this.utilizationTrafficLight=value;
   }
   public TrafficLight getUtilizationTrafficLight() {
     return this.utilizationTrafficLight;
   }

}
