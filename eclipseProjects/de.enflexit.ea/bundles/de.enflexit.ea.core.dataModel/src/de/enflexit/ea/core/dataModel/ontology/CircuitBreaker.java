package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CircuitBreaker
* @author ontology bean generator
* @version 2020/09/18, 14:32:58
*/
public class CircuitBreaker implements Concept {

   /**
* Protege name: isClosed
   */
   private boolean isClosed;
   public void setIsClosed(boolean value) { 
    this.isClosed=value;
   }
   public boolean getIsClosed() {
     return this.isClosed;
   }

   /**
* Protege name: isControllable
   */
   private boolean isControllable;
   public void setIsControllable(boolean value) { 
    this.isControllable=value;
   }
   public boolean getIsControllable() {
     return this.isControllable;
   }

   /**
   * The ID of the component next to which the breaker is located
* Protege name: atComponent
   */
   private String atComponent;
   public void setAtComponent(String value) { 
    this.atComponent=value;
   }
   public String getAtComponent() {
     return this.atComponent;
   }

   /**
* Protege name: breakerID
   */
   private String breakerID;
   public void setBreakerID(String value) { 
    this.breakerID=value;
   }
   public String getBreakerID() {
     return this.breakerID;
   }

}
