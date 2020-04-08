package de.enflexit.energyAgent.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatPipeState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class HeatPipeState extends PipeState{ 

   /**
* Protege name: deltaTemperature
   */
   private UnitValue deltaTemperature;
   public void setDeltaTemperature(UnitValue value) { 
    this.deltaTemperature=value;
   }
   public UnitValue getDeltaTemperature() {
     return this.deltaTemperature;
   }

   /**
* Protege name: energyLoss
   */
   private UnitValue energyLoss;
   public void setEnergyLoss(UnitValue value) { 
    this.energyLoss=value;
   }
   public UnitValue getEnergyLoss() {
     return this.energyLoss;
   }

   /**
* Protege name: deltaPressure
   */
   private UnitValue deltaPressure;
   public void setDeltaPressure(UnitValue value) { 
    this.deltaPressure=value;
   }
   public UnitValue getDeltaPressure() {
     return this.deltaPressure;
   }

}
