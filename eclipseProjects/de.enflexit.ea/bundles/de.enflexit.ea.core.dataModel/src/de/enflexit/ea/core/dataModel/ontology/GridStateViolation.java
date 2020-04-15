package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GridStateViolation
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class GridStateViolation extends GridStateControl{ 

   /**
* Protege name: chosenFlexibilityImag
   */
   private float chosenFlexibilityImag;
   public void setChosenFlexibilityImag(float value) { 
    this.chosenFlexibilityImag=value;
   }
   public float getChosenFlexibilityImag() {
     return this.chosenFlexibilityImag;
   }

   /**
* Protege name: actuatorName
   */
   private String actuatorName;
   public void setActuatorName(String value) { 
    this.actuatorName=value;
   }
   public String getActuatorName() {
     return this.actuatorName;
   }

   /**
* Protege name: requiredPowerImag
   */
   private float requiredPowerImag;
   public void setRequiredPowerImag(float value) { 
    this.requiredPowerImag=value;
   }
   public float getRequiredPowerImag() {
     return this.requiredPowerImag;
   }

   /**
* Protege name: cable
   */
   private String cable;
   public void setCable(String value) { 
    this.cable=value;
   }
   public String getCable() {
     return this.cable;
   }

   /**
* Protege name: possibleFlexibilityOffers
   */
   private List possibleFlexibilityOffers = new ArrayList();
   public void addPossibleFlexibilityOffers(FlexibilityOffer elem) { 
     List oldList = this.possibleFlexibilityOffers;
     possibleFlexibilityOffers.add(elem);
   }
   public boolean removePossibleFlexibilityOffers(FlexibilityOffer elem) {
     List oldList = this.possibleFlexibilityOffers;
     boolean result = possibleFlexibilityOffers.remove(elem);
     return result;
   }
   public void clearAllPossibleFlexibilityOffers() {
     List oldList = this.possibleFlexibilityOffers;
     possibleFlexibilityOffers.clear();
   }
   public Iterator getAllPossibleFlexibilityOffers() {return possibleFlexibilityOffers.iterator(); }
   public List getPossibleFlexibilityOffers() {return possibleFlexibilityOffers; }
   public void setPossibleFlexibilityOffers(List l) {possibleFlexibilityOffers = l; }

   /**
* Protege name: componentOfViolation
   */
   private String componentOfViolation;
   public void setComponentOfViolation(String value) { 
    this.componentOfViolation=value;
   }
   public String getComponentOfViolation() {
     return this.componentOfViolation;
   }

   /**
* Protege name: chosenFlexibilityReal
   */
   private float chosenFlexibilityReal;
   public void setChosenFlexibilityReal(float value) { 
    this.chosenFlexibilityReal=value;
   }
   public float getChosenFlexibilityReal() {
     return this.chosenFlexibilityReal;
   }

   /**
* Protege name: actualBranchCurrent
   */
   private float actualBranchCurrent;
   public void setActualBranchCurrent(float value) { 
    this.actualBranchCurrent=value;
   }
   public float getActualBranchCurrent() {
     return this.actualBranchCurrent;
   }

   /**
* Protege name: requiredPowerReal
   */
   private float requiredPowerReal;
   public void setRequiredPowerReal(float value) { 
    this.requiredPowerReal=value;
   }
   public float getRequiredPowerReal() {
     return this.requiredPowerReal;
   }

   /**
* Protege name: requiredCurrentAdjustment
   */
   private float requiredCurrentAdjustment;
   public void setRequiredCurrentAdjustment(float value) { 
    this.requiredCurrentAdjustment=value;
   }
   public float getRequiredCurrentAdjustment() {
     return this.requiredCurrentAdjustment;
   }

   /**
* Protege name: kindOfViolation
   */
   private String kindOfViolation;
   public void setKindOfViolation(String value) { 
    this.kindOfViolation=value;
   }
   public String getKindOfViolation() {
     return this.kindOfViolation;
   }

   /**
* Protege name: actualNodalVoltage
   */
   private float actualNodalVoltage;
   public void setActualNodalVoltage(float value) { 
    this.actualNodalVoltage=value;
   }
   public float getActualNodalVoltage() {
     return this.actualNodalVoltage;
   }

   /**
* Protege name: requiredVoltageAdjustment
   */
   private float requiredVoltageAdjustment;
   public void setRequiredVoltageAdjustment(float value) { 
    this.requiredVoltageAdjustment=value;
   }
   public float getRequiredVoltageAdjustment() {
     return this.requiredVoltageAdjustment;
   }

   /**
* Protege name: nPhaseOfViolation
   */
   private int nPhaseOfViolation;
   public void setNPhaseOfViolation(int value) { 
    this.nPhaseOfViolation=value;
   }
   public int getNPhaseOfViolation() {
     return this.nPhaseOfViolation;
   }

   /**
* Protege name: cableOverloadInPercent
   */
   private float cableOverloadInPercent;
   public void setCableOverloadInPercent(float value) { 
    this.cableOverloadInPercent=value;
   }
   public float getCableOverloadInPercent() {
     return this.cableOverloadInPercent;
   }

}
