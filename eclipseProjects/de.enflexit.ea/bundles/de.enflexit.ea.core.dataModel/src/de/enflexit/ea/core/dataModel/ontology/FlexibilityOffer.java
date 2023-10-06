package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: FlexibilityOffer
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class FlexibilityOffer extends GridStateControl{ 

   /**
* Protege name: priority
   */
   private int priority;
   public void setPriority(int value) { 
    this.priority=value;
   }
   public int getPriority() {
     return this.priority;
   }

   /**
* Protege name: actuatorName
   */
   private String actuatorName;
   public void setActuatorName(String value) { 
    this.actuatorName=value;
   }
   public String getActuatorName() {
     return this.actuatorName;
   }

   /**
* Protege name: possibleCurrentAdjustment
   */
   private float possibleCurrentAdjustment;
   public void setPossibleCurrentAdjustment(float value) { 
    this.possibleCurrentAdjustment=value;
   }
   public float getPossibleCurrentAdjustment() {
     return this.possibleCurrentAdjustment;
   }

   /**
* Protege name: possiblePowerImag
   */
   private float possiblePowerImag;
   public void setPossiblePowerImag(float value) { 
    this.possiblePowerImag=value;
   }
   public float getPossiblePowerImag() {
     return this.possiblePowerImag;
   }

   /**
* Protege name: possiblePowerReal
   */
   private float possiblePowerReal;
   public void setPossiblePowerReal(float value) { 
    this.possiblePowerReal=value;
   }
   public float getPossiblePowerReal() {
     return this.possiblePowerReal;
   }

   /**
* Protege name: possibleVoltageAdjustment
   */
   private float possibleVoltageAdjustment;
   public void setPossibleVoltageAdjustment(float value) { 
    this.possibleVoltageAdjustment=value;
   }
   public float getPossibleVoltageAdjustment() {
     return this.possibleVoltageAdjustment;
   }

}
