package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CableWithBreakerProperties
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class CableWithBreakerProperties extends CableProperties{ 

   /**
* Protege name: breakerEnd
   */
   private CircuitBreaker breakerEnd;
   public void setBreakerEnd(CircuitBreaker value) { 
    this.breakerEnd=value;
   }
   public CircuitBreaker getBreakerEnd() {
     return this.breakerEnd;
   }

   /**
* Protege name: breakerBegin
   */
   private CircuitBreaker breakerBegin;
   public void setBreakerBegin(CircuitBreaker value) { 
    this.breakerBegin=value;
   }
   public CircuitBreaker getBreakerBegin() {
     return this.breakerBegin;
   }

}
