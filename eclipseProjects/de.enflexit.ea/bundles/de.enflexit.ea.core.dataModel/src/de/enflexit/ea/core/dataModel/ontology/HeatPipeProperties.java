package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: HeatPipeProperties
* @author ontology bean generator
* @version 2020/09/18, 14:32:58
*/
public class HeatPipeProperties extends PipeProperties{ 

   /**
* Protege name: isolationThermalConductivity
   */
   private UnitValue isolationThermalConductivity;
   public void setIsolationThermalConductivity(UnitValue value) { 
    this.isolationThermalConductivity=value;
   }
   public UnitValue getIsolationThermalConductivity() {
     return this.isolationThermalConductivity;
   }

   /**
* Protege name: surrounding
   */
   private PipeSurrounding surrounding;
   public void setSurrounding(PipeSurrounding value) { 
    this.surrounding=value;
   }
   public PipeSurrounding getSurrounding() {
     return this.surrounding;
   }

   /**
* Protege name: coatingThickness
   */
   private UnitValue coatingThickness;
   public void setCoatingThickness(UnitValue value) { 
    this.coatingThickness=value;
   }
   public UnitValue getCoatingThickness() {
     return this.coatingThickness;
   }

   /**
* Protege name: isolationThickness
   */
   private UnitValue isolationThickness;
   public void setIsolationThickness(UnitValue value) { 
    this.isolationThickness=value;
   }
   public UnitValue getIsolationThickness() {
     return this.isolationThickness;
   }

   /**
* Protege name: coatingThermalConductivity
   */
   private UnitValue coatingThermalConductivity;
   public void setCoatingThermalConductivity(UnitValue value) { 
    this.coatingThermalConductivity=value;
   }
   public UnitValue getCoatingThermalConductivity() {
     return this.coatingThermalConductivity;
   }

}
