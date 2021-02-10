package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: SlackNodeSetVoltageLevelNotification
* @author ontology bean generator
* @version 2021/02/9, 23:45:16
*/
public class SlackNodeSetVoltageLevelNotification extends NetworkStateInformation{ 

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

}
