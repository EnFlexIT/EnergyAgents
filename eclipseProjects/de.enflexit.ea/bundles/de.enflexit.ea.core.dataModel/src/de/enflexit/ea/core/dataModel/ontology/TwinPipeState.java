package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TwinPipeState
* @author ontology bean generator
* @version 2021/02/9, 23:45:16
*/
public class TwinPipeState extends EdgeComponentState{ 

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

}
