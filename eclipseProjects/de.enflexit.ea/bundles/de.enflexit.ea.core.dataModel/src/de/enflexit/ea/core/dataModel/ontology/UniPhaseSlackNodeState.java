package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseSlackNodeState
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class UniPhaseSlackNodeState extends SlackNodeState{ 

//////////////////////////// User code
/* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
   public boolean equals(Object compObj) {
    if (compObj==null || !(compObj instanceof UniPhaseSlackNodeState)) return false;
    // --- Compare voltages -------
    UniPhaseSlackNodeState compSns = (UniPhaseSlackNodeState)compObj;
    if (this.equalUnitValue(compSns.getVoltageReal(), this.getVoltageReal())==false) return false;
    if (this.equalUnitValue(compSns.getVoltageImag(), this.getVoltageImag())==false) return false;
    return true;
   }
   private boolean equalUnitValue(UnitValue compUV, UnitValue thisUV) {
    if (compUV==null && thisUV==null) {
     // --- Nothing to do here -----------
    } else if ((compUV==null && thisUV!=null) || (compUV!=null && thisUV==null)) {
      return false;
    } else {
      if (compUV.equals(thisUV)==false) return false;
    }
    return true;
   }
   /**
* Protege name: voltageImag
   */
   private UnitValue voltageImag;
   public void setVoltageImag(UnitValue value) { 
    this.voltageImag=value;
   }
   public UnitValue getVoltageImag() {
     return this.voltageImag;
   }

   /**
* Protege name: voltageReal
   */
   private UnitValue voltageReal;
   public void setVoltageReal(UnitValue value) { 
    this.voltageReal=value;
   }
   public UnitValue getVoltageReal() {
     return this.voltageReal;
   }

}
