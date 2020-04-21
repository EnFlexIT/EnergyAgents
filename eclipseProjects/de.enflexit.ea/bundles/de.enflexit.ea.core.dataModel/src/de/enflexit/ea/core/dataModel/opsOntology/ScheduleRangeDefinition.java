package de.enflexit.ea.core.dataModel.opsOntology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * Specifies the time range or number  of states for a schedule request
* Protege name: ScheduleRangeDefinition
* @author ontology bean generator
* @version 2018/11/20, 12:59:18
*/
public class ScheduleRangeDefinition implements Concept {

   /**
* Protege name: timestampFrom
   */
   private LongValue timestampFrom;
   public void setTimestampFrom(LongValue value) { 
    this.timestampFrom=value;
   }
   public LongValue getTimestampFrom() {
     return this.timestampFrom;
   }

   /**
* Protege name: numberOfStates
   */
   private int numberOfStates;
   public void setNumberOfStates(int value) { 
    this.numberOfStates=value;
   }
   public int getNumberOfStates() {
     return this.numberOfStates;
   }

   /**
* Protege name: timestampTo
   */
   private LongValue timestampTo;
   public void setTimestampTo(LongValue value) { 
    this.timestampTo=value;
   }
   public LongValue getTimestampTo() {
     return this.timestampTo;
   }

   /**
   * If true, all states will be included. Other range settings will be ignored.
* Protege name: includeAllStates
   */
   private boolean includeAllStates;
   public void setIncludeAllStates(boolean value) { 
    this.includeAllStates=value;
   }
   public boolean getIncludeAllStates() {
     return this.includeAllStates;
   }

}
