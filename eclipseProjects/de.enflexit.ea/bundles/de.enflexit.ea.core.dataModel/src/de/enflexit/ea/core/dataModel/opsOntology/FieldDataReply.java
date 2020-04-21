package de.enflexit.ea.core.dataModel.opsOntology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: FieldDataReply
* @author ontology bean generator
* @version 2018/11/20, 12:59:18
*/
public class FieldDataReply implements AgentAction {

   /**
   * Indicates of there will be more partial replies for this request
* Protege name: moreComming
   */
   private boolean moreComming;
   public void setMoreComming(boolean value) { 
    this.moreComming=value;
   }
   public boolean getMoreComming() {
     return this.moreComming;
   }

   /**
   * The local name of the agent originally sending this data
* Protege name: agentID
   */
   private String agentID;
   public void setAgentID(String value) { 
    this.agentID=value;
   }
   public String getAgentID() {
     return this.agentID;
   }

   /**
   * The total number of TSSEs for the request
* Protege name: totalTSSEs
   */
   private int totalTSSEs;
   public void setTotalTSSEs(int value) { 
    this.totalTSSEs=value;
   }
   public int getTotalTSSEs() {
     return this.totalTSSEs;
   }

   /**
* Protege name: scheduleListXML
   */
   private String scheduleListXML;
   public void setScheduleListXML(String value) { 
    this.scheduleListXML=value;
   }
   public String getScheduleListXML() {
     return this.scheduleListXML;
   }

}
