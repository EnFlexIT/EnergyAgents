package de.enflexit.energyAgent.core.globalDataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * This class uses a String to store a double value, as double is not available in Protege 3.3.1. Converison functions from/to double are provided in the additional java code field
* Protege name: DoubleValue
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class DoubleValue implements Concept {

//////////////////////////// User code
/**
    * Gets the double value.
    * @return the double value
    */
   public Double getDoubleValue() {
	   try {
		   return Double.parseDouble(this.getStringDoubleValue());
	   }catch(NumberFormatException ex) {
		   return null;
	   }
   }
   
   /**
    * Sets the double value.
    * @param value the new double value
    */
   public void setDoubleValue(double value) {
	   this.setStringDoubleValue(""+value);
   }
   
   /**
    * Instantiates a new double value.
    */
   public DoubleValue() {};
   
   /**
    * Instantiates a new double value.
    * @param value the value
    */
   public DoubleValue(double value) {
	   this.setDoubleValue(value);
   };
   /**
* Protege name: stringDoubleValue
   */
   private String stringDoubleValue;
   public void setStringDoubleValue(String value) { 
    this.stringDoubleValue=value;
   }
   public String getStringDoubleValue() {
     return this.stringDoubleValue;
   }

}
