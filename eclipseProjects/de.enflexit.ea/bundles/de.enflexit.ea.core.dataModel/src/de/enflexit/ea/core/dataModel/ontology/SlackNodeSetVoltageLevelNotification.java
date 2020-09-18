package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: SlackNodeSetVoltageLevelNotification
* @author ontology bean generator
* @version 2020/09/18, 12:50:27
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
