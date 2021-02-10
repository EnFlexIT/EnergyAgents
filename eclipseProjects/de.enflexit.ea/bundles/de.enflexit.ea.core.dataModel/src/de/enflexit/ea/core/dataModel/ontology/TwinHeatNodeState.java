package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TwinHeatNodeState
* @author ontology bean generator
* @version 2021/02/9, 23:45:16
*/
public class TwinHeatNodeState extends NodeComponentState{ 

   /**
* Protege name: ambientTemperature
   */
   private UnitValue ambientTemperature;
   public void setAmbientTemperature(UnitValue value) { 
    this.ambientTemperature=value;
   }
   public UnitValue getAmbientTemperature() {
     return this.ambientTemperature;
   }

   /**
* Protege name: returnNodeState
   */
   private HeatNodeState returnNodeState;
   public void setReturnNodeState(HeatNodeState value) { 
    this.returnNodeState=value;
   }
   public HeatNodeState getReturnNodeState() {
     return this.returnNodeState;
   }

   /**
* Protege name: thermalLoad
   */
   private UnitValue thermalLoad;
   public void setThermalLoad(UnitValue value) { 
    this.thermalLoad=value;
   }
   public UnitValue getThermalLoad() {
     return this.thermalLoad;
   }

   /**
* Protege name: supplyNodeState
   */
   private HeatNodeState supplyNodeState;
   public void setSupplyNodeState(HeatNodeState value) { 
    this.supplyNodeState=value;
   }
   public HeatNodeState getSupplyNodeState() {
     return this.supplyNodeState;
   }

}
