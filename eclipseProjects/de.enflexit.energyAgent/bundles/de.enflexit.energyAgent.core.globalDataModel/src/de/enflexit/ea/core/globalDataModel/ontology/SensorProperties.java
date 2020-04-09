package de.enflexit.ea.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: SensorProperties
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class SensorProperties extends CableProperties{ 

   /**
* Protege name: measureLocation
   */
   private String measureLocation;
   public void setMeasureLocation(String value) { 
    this.measureLocation=value;
   }
   public String getMeasureLocation() {
     return this.measureLocation;
   }

   /**
* Protege name: sensorID
   */
   private String sensorID;
   public void setSensorID(String value) { 
    this.sensorID=value;
   }
   public String getSensorID() {
     return this.sensorID;
   }

}
