package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseElectricalNodeState
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class UniPhaseElectricalNodeState extends ElectricalNodeState{ 

//////////////////////////// User code
/**
    * Returns the current. If null, it is initialized with a new UnitValue instance to prevent NullPointers
    * @return the current
    */
   public UnitValue getCurrentNotNull() {
    if (this.current==null) {
     this.current = new UnitValue();
    }
   return this.current;
   }
   /**
    * Returns the p. If null, it is initialized with a new UnitValue instance to prevent NullPointers.
    * @return the p
    */
   public UnitValue getPNotNull() {
    if (this.p==null) {
	 this.p = new UnitValue();
    }
   return this.p;
   }
   /**
    * Returns the q. If null, it is initialized with a new UnitValue instance to prevent NullPointers.
    * @return the q
    */
   public UnitValue getQNotNull() {
    if (this.q==null) {
     this.q = new UnitValue();
     }
   return this.q;
   }
   /**
    * Returns the s. If null, it is initialized with a new UnitValue instance to prevent NullPointers.
    * @return the s
    */
   public UnitValue getSNotNull() {
    if (this.s==null) {
     this.s = new UnitValue();
    }
   return this.s;
   }

   /**
    * Returns the absolute voltage. If null, it is initialized with a new UnitValue instance to prevent NullPointers.
    * @return the voltage absolute
    */
   public UnitValue getVoltageAbsNotNull() {
    if (this.voltageAbs==null) {
     this.voltageAbs = new UnitValue();
    }
   return this.voltageAbs;
   }
   /**
    * Returns the imaginary part voltage. If null, it is initialized with a new UnitValue instance to prevent NullPointers.
    * @return the voltage imaginary
    */
   public UnitValue getVoltageImagNotNull() {
    if (this.voltageImag==null) {
     this.voltageImag = new UnitValue();
    }
   return this.voltageImag;
   }
   /**
    * Returns the real part voltage. If null, it is initialized with a new UnitValue instance to prevent NullPointers.
    * @return the voltage real
    */
   public UnitValue getVoltageRealNotNull() {
    if (this.voltageReal==null) {
     this.voltageReal = new UnitValue();
	}
   return this.voltageReal;
   }

   /**
    * Calculates p
    * @return the calculated p
    */
   public double getPCalculated() {
    if (this.p!=null && this.p.getValue()!=0) return this.p.getValue();
    return getSNotNull().getValue() * this.getCosPhi();
   }
   /**
    * Calculates q
    * @return the calculated q
    */
   public double getQCalculated() {
    if (this.q!=null && this.q.getValue()!=0) return this.q.getValue();
    return Math.sqrt(Math.pow(this.getSNotNull().getValue(), 2) - Math.pow(this.getPNotNull().getValue(), 2));  
   }
   /**
    * Calculates s
    * @return the calculated s
    */
   public double getSCalculated() {
    if (this.s!=null && this.s.getValue()!=0) return this.s.getValue();
    return this.getVoltageAbsNotNull().getValue() * this.getCurrentNotNull().getValue(); 
   }
   /**
* Protege name: voltageImag
   */
   private UnitValue voltageImag;
   public void setVoltageImag(UnitValue value) { 
    this.voltageImag=value;
   }
   public UnitValue getVoltageImag() {
     return this.voltageImag;
   }

   /**
* Protege name: S
   */
   private UnitValue s;
   public void setS(UnitValue value) { 
    this.s=value;
   }
   public UnitValue getS() {
     return this.s;
   }

   /**
* Protege name: voltageReal
   */
   private UnitValue voltageReal;
   public void setVoltageReal(UnitValue value) { 
    this.voltageReal=value;
   }
   public UnitValue getVoltageReal() {
     return this.voltageReal;
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
* Protege name: voltageAbs
   */
   private UnitValue voltageAbs;
   public void setVoltageAbs(UnitValue value) { 
    this.voltageAbs=value;
   }
   public UnitValue getVoltageAbs() {
     return this.voltageAbs;
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

}
