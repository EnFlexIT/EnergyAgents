package de.enflexit.energyAgent.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CableWithBreaker
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class CableWithBreaker extends Cable{ 

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
