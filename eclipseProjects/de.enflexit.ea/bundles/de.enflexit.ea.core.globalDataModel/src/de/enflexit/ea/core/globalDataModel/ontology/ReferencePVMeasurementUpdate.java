package de.enflexit.ea.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Measurement Update of reference PV Measurement
* Protege name: ReferencePVMeasurementUpdate
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class ReferencePVMeasurementUpdate extends GridStateAgentManagement{ 

   /**
* Protege name: relativePower
   */
   private float relativePower;
   public void setRelativePower(float value) { 
    this.relativePower=value;
   }
   public float getRelativePower() {
     return this.relativePower;
   }

   /**
* Protege name: TimeStampRefPV
   */
   private String timeStampRefPV;
   public void setTimeStampRefPV(String value) { 
    this.timeStampRefPV=value;
   }
   public String getTimeStampRefPV() {
     return this.timeStampRefPV;
   }

}
