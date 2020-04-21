package de.enflexit.ea.core.dataModel.opsOntology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: LiveMonitoringUpdate
* @author ontology bean generator
* @version 2018/11/20, 12:59:18
*/
public class LiveMonitoringUpdate implements AgentAction {

   /**
   * The new TSSE sent by the agent, encoded as Base64 string
* Protege name: newTsseBase64
   */
   private String newTsseBase64;
   public void setNewTsseBase64(String value) { 
    this.newTsseBase64=value;
   }
   public String getNewTsseBase64() {
     return this.newTsseBase64;
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

}
