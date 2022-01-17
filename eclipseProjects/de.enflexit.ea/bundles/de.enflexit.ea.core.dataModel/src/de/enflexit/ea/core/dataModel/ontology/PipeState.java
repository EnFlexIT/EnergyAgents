package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: PipeState
* @author ontology bean generator
* @version 2022/01/17, 15:51:08
*/
public class PipeState extends EdgeComponentState{ 

   /**
* Protege name: temperatureIn
   */
   private UnitValue temperatureIn;
   public void setTemperatureIn(UnitValue value) { 
    this.temperatureIn=value;
   }
   public UnitValue getTemperatureIn() {
     return this.temperatureIn;
   }

   /**
* Protege name: velocityOut
   */
   private UnitValue velocityOut;
   public void setVelocityOut(UnitValue value) { 
    this.velocityOut=value;
   }
   public UnitValue getVelocityOut() {
     return this.velocityOut;
   }

   /**
* Protege name: pressureOut
   */
   private UnitValue pressureOut;
   public void setPressureOut(UnitValue value) { 
    this.pressureOut=value;
   }
   public UnitValue getPressureOut() {
     return this.pressureOut;
   }

   /**
* Protege name: velocityIn
   */
   private UnitValue velocityIn;
   public void setVelocityIn(UnitValue value) { 
    this.velocityIn=value;
   }
   public UnitValue getVelocityIn() {
     return this.velocityIn;
   }

   /**
* Protege name: pressureIn
   */
   private UnitValue pressureIn;
   public void setPressureIn(UnitValue value) { 
    this.pressureIn=value;
   }
   public UnitValue getPressureIn() {
     return this.pressureIn;
   }

   /**
   * Surrounding temperature
* Protege name: surroundingTemperature
   */
   private UnitValue surroundingTemperature;
   public void setSurroundingTemperature(UnitValue value) { 
    this.surroundingTemperature=value;
   }
   public UnitValue getSurroundingTemperature() {
     return this.surroundingTemperature;
   }

   /**
* Protege name: temperatureOut
   */
   private UnitValue temperatureOut;
   public void setTemperatureOut(UnitValue value) { 
    this.temperatureOut=value;
   }
   public UnitValue getTemperatureOut() {
     return this.temperatureOut;
   }

}
