package de.enflexit.ea.core.globalDataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Stores information about a testbed agent and its proxy agent
* Protege name: RemoteAgentInformation
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class RemoteAgentInformation implements Concept {

//////////////////////////// User code
/**
    * Checks if both AIDs (proxy and remote) have been set
    * @return Are both AIDs available?
    */
   public boolean isComplete(){
	   return (this.proxyAgentAID != null && this.remoteAgentAID != null);
   }
   /**
   * The proxy agent's AID
* Protege name: proxyAgentAID
   */
   private AID proxyAgentAID;
   public void setProxyAgentAID(AID value) { 
    this.proxyAgentAID=value;
   }
   public AID getProxyAgentAID() {
     return this.proxyAgentAID;
   }

   /**
   * The remote agent's AID
* Protege name: remoteAgentAID
   */
   private AID remoteAgentAID;
   public void setRemoteAgentAID(AID value) { 
    this.remoteAgentAID=value;
   }
   public AID getRemoteAgentAID() {
     return this.remoteAgentAID;
   }

}
