package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UnitValue
* @author ontology bean generator
* @version 2020/09/18, 14:32:59
*/
public class UnitValue implements Concept {

//////////////////////////// User code
/**
 * Instantiates a new unit value.
 */
 public UnitValue() { }
 /**
 * Instantiates a new unit value.
 *
 * @param value the value
 * @param unit the unit
 */
 public UnitValue(float value, String unit) {
  this.setValue(value);
  this.setUnit(unit);
 }
   /**
* Protege name: value
   */
   private float value;
   public void setValue(float value) { 
    this.value=value;
   }
   public float getValue() {
     return this.value;
   }

   /**
* Protege name: unit
   */
   private String unit;
   public void setUnit(String value) { 
    this.unit=value;
   }
   public String getUnit() {
     return this.unit;
   }

}
