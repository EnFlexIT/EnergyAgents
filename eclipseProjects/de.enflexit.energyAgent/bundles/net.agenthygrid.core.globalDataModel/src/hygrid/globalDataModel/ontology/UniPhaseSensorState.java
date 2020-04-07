package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseSensorState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class UniPhaseSensorState extends UniPhaseCableState{ 

   /**
* Protege name: measuredVoltage
   */
   private UnitValue measuredVoltage;
   public void setMeasuredVoltage(UnitValue value) { 
    this.measuredVoltage=value;
   }
   public UnitValue getMeasuredVoltage() {
     return this.measuredVoltage;
   }

}
