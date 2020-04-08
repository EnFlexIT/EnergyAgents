package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ElectricalNodeProperties
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class ElectricalNodeProperties extends NodeComponentProperties{ 

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
