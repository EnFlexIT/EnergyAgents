package de.enflexit.ea.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GasNodeState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class GasNodeState extends FluidNodeState{ 

   /**
* Protege name: volumeFlow
   */
   private float volumeFlow;
   public void setVolumeFlow(float value) { 
    this.volumeFlow=value;
   }
   public float getVolumeFlow() {
     return this.volumeFlow;
   }

}
