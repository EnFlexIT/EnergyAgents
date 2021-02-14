package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * This class describes static properties of a heat pipe
* Protege name: PipeProperties
* @author ontology bean generator
* @version 2021/02/15, 12:09:03
*/
public class PipeProperties extends EdgeComponentProperties{ 

   /**
* Protege name: minorLossCoefficient
   */
   private int minorLossCoefficient;
   public void setMinorLossCoefficient(int value) { 
    this.minorLossCoefficient=value;
   }
   public int getMinorLossCoefficient() {
     return this.minorLossCoefficient;
   }

   /**
* Protege name: manufacturer
   */
   private String manufacturer;
   public void setManufacturer(String value) { 
    this.manufacturer=value;
   }
   public String getManufacturer() {
     return this.manufacturer;
   }

   /**
* Protege name: roughness
   */
   private UnitValue roughness;
   public void setRoughness(UnitValue value) { 
    this.roughness=value;
   }
   public UnitValue getRoughness() {
     return this.roughness;
   }

   /**
   * The thickness of the pipe wall
* Protege name: pipeThickness
   */
   private UnitValue pipeThickness;
   public void setPipeThickness(UnitValue value) { 
    this.pipeThickness=value;
   }
   public UnitValue getPipeThickness() {
     return this.pipeThickness;
   }

   /**
* Protege name: make
   */
   private String make;
   public void setMake(String value) { 
    this.make=value;
   }
   public String getMake() {
     return this.make;
   }

   /**
* Protege name: pipeThermalConductivity
   */
   private UnitValue pipeThermalConductivity;
   public void setPipeThermalConductivity(UnitValue value) { 
    this.pipeThermalConductivity=value;
   }
   public UnitValue getPipeThermalConductivity() {
     return this.pipeThermalConductivity;
   }

   /**
* Protege name: diameter
   */
   private UnitValue diameter;
   public void setDiameter(UnitValue value) { 
    this.diameter=value;
   }
   public UnitValue getDiameter() {
     return this.diameter;
   }

   /**
* Protege name: length
   */
   private UnitValue length;
   public void setLength(UnitValue value) { 
    this.length=value;
   }
   public UnitValue getLength() {
     return this.length;
   }

   /**
* Protege name: nominalSize
   */
   private int nominalSize;
   public void setNominalSize(int value) { 
    this.nominalSize=value;
   }
   public int getNominalSize() {
     return this.nominalSize;
   }

}
