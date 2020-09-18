package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GridStateAssessment
* @author ontology bean generator
* @version 2020/09/18, 12:50:26
*/
public class GridStateAssessment implements Concept {

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

}
