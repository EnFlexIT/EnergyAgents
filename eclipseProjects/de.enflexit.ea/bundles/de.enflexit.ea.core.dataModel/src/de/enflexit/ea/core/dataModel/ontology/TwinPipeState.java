package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TwinPipeState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class TwinPipeState extends EdgeComponentState{ 

   /**
* Protege name: supplyPipeState
   */
   private HeatPipeState supplyPipeState;
   public void setSupplyPipeState(HeatPipeState value) { 
    this.supplyPipeState=value;
   }
   public HeatPipeState getSupplyPipeState() {
     return this.supplyPipeState;
   }

   /**
* Protege name: returnPipeState
   */
   private HeatPipeState returnPipeState;
   public void setReturnPipeState(HeatPipeState value) { 
    this.returnPipeState=value;
   }
   public HeatPipeState getReturnPipeState() {
     return this.returnPipeState;
   }

}
