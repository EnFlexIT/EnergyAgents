package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UnitValue
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
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
/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
 @Override
 public boolean equals(Object compObj) {
  if (compObj==null || !(compObj instanceof UnitValue)) return false;
  // --- Compare value and unit ---
  UnitValue compUnitValue = (UnitValue) compObj;
  if (compUnitValue.getValue()!=this.getValue()) return false;
 
  String compUnit = compUnitValue.getUnit();
  String thisUnit = this.getUnit();
  if (compUnit==null && thisUnit==null) {
    // --- Nothing to do here ----
  } else if ((compUnit==null && thisUnit!=null) || (compUnit!=null && thisUnit==null)) {
    return false;
  } else {
    if (compUnit.equals(thisUnit)==false) return false;   	 
  }
  return true;
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

}
