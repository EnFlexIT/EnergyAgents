package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseCableWithBreaker
* @author ontology bean generator
* @version 2023/07/13, 21:15:48
*/
public class UniPhaseCableWithBreaker extends UniPhaseCableState{ 

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
