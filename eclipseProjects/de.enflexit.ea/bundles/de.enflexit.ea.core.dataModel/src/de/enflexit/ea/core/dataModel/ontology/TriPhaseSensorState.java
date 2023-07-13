package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TriPhaseSensorState
* @author ontology bean generator
* @version 2023/07/13, 21:15:48
*/
public class TriPhaseSensorState extends TriPhaseCableState{ 

//////////////////////////// User code
// --- Old style getters and setters for backwards compatibility -----------
   public void setVoltage_L1(float value) { 
	   this.setMeasuredVoltageL1(new UnitValue(value, "V"));
   }
   public float getVoltage_L1() {
	   return this.getMeasuredVoltageL1().getValue();
   }
   public void setVoltage_L2(float value) { 
	   this.setMeasuredVoltageL2(new UnitValue(value, "V"));
   }
   public float getVoltage_L2() {
	   return this.getMeasuredVoltageL2().getValue();
   }
   public void setVoltage_L3(float value) { 
	   this.setMeasuredVoltageL3(new UnitValue(value, "V"));
   }
   public float getVoltage_L3() {
	   return this.getMeasuredVoltageL3().getValue();
   }
   /**
* Protege name: measuredVoltageL1
   */
   private UnitValue measuredVoltageL1;
   public void setMeasuredVoltageL1(UnitValue value) { 
    this.measuredVoltageL1=value;
   }
   public UnitValue getMeasuredVoltageL1() {
     return this.measuredVoltageL1;
   }

   /**
* Protege name: measuredVoltageL2
   */
   private UnitValue measuredVoltageL2;
   public void setMeasuredVoltageL2(UnitValue value) { 
    this.measuredVoltageL2=value;
   }
   public UnitValue getMeasuredVoltageL2() {
     return this.measuredVoltageL2;
   }

   /**
* Protege name: measuredVoltageL3
   */
   private UnitValue measuredVoltageL3;
   public void setMeasuredVoltageL3(UnitValue value) { 
    this.measuredVoltageL3=value;
   }
   public UnitValue getMeasuredVoltageL3() {
     return this.measuredVoltageL3;
   }

}
