package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Electrical Node State
* Protege name: ElectricalGridStateUpdate
* @author ontology bean generator
* @version 2020/09/18, 12:50:27
*/
public class ElectricalGridStateUpdate extends GridStateAgentManagement{ 

   /**
* Protege name: TimeStampGridState
   */
   private String timeStampGridState;
   public void setTimeStampGridState(String value) { 
    this.timeStampGridState=value;
   }
   public String getTimeStampGridState() {
     return this.timeStampGridState;
   }

   /**
* Protege name: GridState
   */
   private List gridState = new ArrayList();
   public void addGridState(ElectricalGridState elem) { 
     List oldList = this.gridState;
     gridState.add(elem);
   }
   public boolean removeGridState(ElectricalGridState elem) {
     List oldList = this.gridState;
     boolean result = gridState.remove(elem);
     return result;
   }
   public void clearAllGridState() {
     List oldList = this.gridState;
     gridState.clear();
   }
   public Iterator getAllGridState() {return gridState.iterator(); }
   public List getGridState() {return gridState; }
   public void setGridState(List l) {gridState = l; }

}
