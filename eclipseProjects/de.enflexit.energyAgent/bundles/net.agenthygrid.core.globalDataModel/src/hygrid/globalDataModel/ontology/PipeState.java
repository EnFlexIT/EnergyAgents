package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: PipeState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class PipeState extends EdgeComponentState{ 

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
* Protege name: temperatureOut
   */
   private UnitValue temperatureOut;
   public void setTemperatureOut(UnitValue value) { 
    this.temperatureOut=value;
   }
   public UnitValue getTemperatureOut() {
     return this.temperatureOut;
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
* Protege name: velocityIn
   */
   private UnitValue velocityIn;
   public void setVelocityIn(UnitValue value) { 
    this.velocityIn=value;
   }
   public UnitValue getVelocityIn() {
     return this.velocityIn;
   }

}
