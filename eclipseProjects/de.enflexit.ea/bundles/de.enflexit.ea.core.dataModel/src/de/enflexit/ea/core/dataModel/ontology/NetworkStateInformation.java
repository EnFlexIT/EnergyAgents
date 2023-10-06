package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NetworkStateInformation
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class NetworkStateInformation implements AgentAction {

   /**
* Protege name: timeStamp
   */
   private LongValue timeStamp;
   public void setTimeStamp(LongValue value) { 
    this.timeStamp=value;
   }
   public LongValue getTimeStamp() {
     return this.timeStamp;
   }

}
