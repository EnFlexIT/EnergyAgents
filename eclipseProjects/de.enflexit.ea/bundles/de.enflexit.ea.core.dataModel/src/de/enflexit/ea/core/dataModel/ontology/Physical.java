package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Physical
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class Physical implements AgentAction {

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
