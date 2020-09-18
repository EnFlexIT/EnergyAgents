package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatPipeState
* @author ontology bean generator
* @version 2020/09/18, 12:50:27
*/
public class HeatPipeState extends PipeState{ 

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
* Protege name: deltaTemperature
   */
   private UnitValue deltaTemperature;
   public void setDeltaTemperature(UnitValue value) { 
    this.deltaTemperature=value;
   }
   public UnitValue getDeltaTemperature() {
     return this.deltaTemperature;
   }

}
