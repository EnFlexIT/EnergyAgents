package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Sensor
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class Sensor extends Cable{ 

   /**
* Protege name: voltage L2
   */
   private float voltage_L2;
   public void setVoltage_L2(float value) { 
    this.voltage_L2=value;
   }
   public float getVoltage_L2() {
     return this.voltage_L2;
   }

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
* Protege name: voltage L3
   */
   private float voltage_L3;
   public void setVoltage_L3(float value) { 
    this.voltage_L3=value;
   }
   public float getVoltage_L3() {
     return this.voltage_L3;
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

   /**
* Protege name: voltage L1
   */
   private float voltage_L1;
   public void setVoltage_L1(float value) { 
    this.voltage_L1=value;
   }
   public float getVoltage_L1() {
     return this.voltage_L1;
   }

}
