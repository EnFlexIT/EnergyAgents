package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GasNodeState
* @author ontology bean generator
* @version 2020/09/18, 12:50:27
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
