package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseCableState
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class UniPhaseCableState extends CableState{ 

   /**
* Protege name: lossesQ
   */
   private UnitValue lossesQ;
   public void setLossesQ(UnitValue value) { 
    this.lossesQ=value;
   }
   public UnitValue getLossesQ() {
     return this.lossesQ;
   }

   /**
* Protege name: P
   */
   private UnitValue p;
   public void setP(UnitValue value) { 
    this.p=value;
   }
   public UnitValue getP() {
     return this.p;
   }

   /**
* Protege name: cosPhi
   */
   private float cosPhi;
   public void setCosPhi(float value) { 
    this.cosPhi=value;
   }
   public float getCosPhi() {
     return this.cosPhi;
   }

   /**
* Protege name: utilization
   */
   private float utilization;
   public void setUtilization(float value) { 
    this.utilization=value;
   }
   public float getUtilization() {
     return this.utilization;
   }

   /**
* Protege name: currentReal
   */
   private UnitValue currentReal;
   public void setCurrentReal(UnitValue value) { 
    this.currentReal=value;
   }
   public UnitValue getCurrentReal() {
     return this.currentReal;
   }

   /**
* Protege name: Q
   */
   private UnitValue q;
   public void setQ(UnitValue value) { 
    this.q=value;
   }
   public UnitValue getQ() {
     return this.q;
   }

   /**
* Protege name: lossesP
   */
   private UnitValue lossesP;
   public void setLossesP(UnitValue value) { 
    this.lossesP=value;
   }
   public UnitValue getLossesP() {
     return this.lossesP;
   }

   /**
* Protege name: current
   */
   private UnitValue current;
   public void setCurrent(UnitValue value) { 
    this.current=value;
   }
   public UnitValue getCurrent() {
     return this.current;
   }

   /**
* Protege name: currentImag
   */
   private UnitValue currentImag;
   public void setCurrentImag(UnitValue value) { 
    this.currentImag=value;
   }
   public UnitValue getCurrentImag() {
     return this.currentImag;
   }

}
