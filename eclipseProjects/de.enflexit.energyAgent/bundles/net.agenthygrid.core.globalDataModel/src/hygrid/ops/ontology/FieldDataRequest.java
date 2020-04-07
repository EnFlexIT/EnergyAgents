package hygrid.ops.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Specifies a request for field data (=schedules) from deployed agents
* Protege name: FieldDataRequest
* @author ontology bean generator
* @version 2018/11/20, 12:59:18
*/
public class FieldDataRequest implements AgentAction {

   /**
* Protege name: scheduleRangeDefinition
   */
   private ScheduleRangeDefinition scheduleRangeDefinition;
   public void setScheduleRangeDefinition(ScheduleRangeDefinition value) { 
    this.scheduleRangeDefinition=value;
   }
   public ScheduleRangeDefinition getScheduleRangeDefinition() {
     return this.scheduleRangeDefinition;
   }

   /**
* Protege name: setup
   */
   private String setup;
   public void setSetup(String value) { 
    this.setup=value;
   }
   public String getSetup() {
     return this.setup;
   }

   /**
* Protege name: agentIDs
   */
   private List agentIDs = new ArrayList();
   public void addAgentIDs(String elem) { 
     List oldList = this.agentIDs;
     agentIDs.add(elem);
   }
   public boolean removeAgentIDs(String elem) {
     List oldList = this.agentIDs;
     boolean result = agentIDs.remove(elem);
     return result;
   }
   public void clearAllAgentIDs() {
     List oldList = this.agentIDs;
     agentIDs.clear();
   }
   public Iterator getAllAgentIDs() {return agentIDs.iterator(); }
   public List getAgentIDs() {return agentIDs; }
   public void setAgentIDs(List l) {agentIDs = l; }

}
