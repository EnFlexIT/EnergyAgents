package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ElectricalGridState
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class ElectricalGridState extends NodeComponentState{ 

   /**
* Protege name: GridStateInfo
   */
   private ElectricalNodeState gridStateInfo;
   public void setGridStateInfo(ElectricalNodeState value) { 
    this.gridStateInfo=value;
   }
   public ElectricalNodeState getGridStateInfo() {
     return this.gridStateInfo;
   }

   /**
* Protege name: GlobalName
   */
   private String globalName;
   public void setGlobalName(String value) { 
    this.globalName=value;
   }
   public String getGlobalName() {
     return this.globalName;
   }

   /**
* Protege name: LocalNodeNum
   */
   private int localNodeNum;
   public void setLocalNodeNum(int value) { 
    this.localNodeNum=value;
   }
   public int getLocalNodeNum() {
     return this.localNodeNum;
   }

}
