package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatNodeProperties
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class HeatNodeProperties extends NodeComponentProperties{ 

   /**
* Protege name: nominalPower
   */
   private UnitValue nominalPower;
   public void setNominalPower(UnitValue value) { 
    this.nominalPower=value;
   }
   public UnitValue getNominalPower() {
     return this.nominalPower;
   }

   /**
* Protege name: isLoadNode
   */
   private boolean isLoadNode;
   public void setIsLoadNode(boolean value) { 
    this.isLoadNode=value;
   }
   public boolean getIsLoadNode() {
     return this.isLoadNode;
   }

}
