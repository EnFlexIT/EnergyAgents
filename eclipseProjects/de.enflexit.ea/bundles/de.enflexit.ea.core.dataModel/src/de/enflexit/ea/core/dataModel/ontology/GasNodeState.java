package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GasNodeState
* @author ontology bean generator
* @version 2021/02/9, 23:45:16
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
