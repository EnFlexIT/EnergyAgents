package de.enflexit.ea.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GasMeasurement
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class GasMeasurement extends Measurement{ 

//////////////////////////// User code
public GasNodeState getGasNodeStateNotNull() {
	if (this.gasNodeState == null) this.gasNodeState = new GasNodeState(); 
	return this.gasNodeState;
}
   /**
* Protege name: gasNodeState
   */
   private GasNodeState gasNodeState;
   public void setGasNodeState(GasNodeState value) { 
    this.gasNodeState=value;
   }
   public GasNodeState getGasNodeState() {
     return this.gasNodeState;
   }

}
