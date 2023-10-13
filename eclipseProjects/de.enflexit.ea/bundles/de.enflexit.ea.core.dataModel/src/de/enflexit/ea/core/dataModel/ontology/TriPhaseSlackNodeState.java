package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: TriPhaseSlackNodeState
* @author ontology bean generator
* @version 2023/10/6, 19:38:39
*/
public class TriPhaseSlackNodeState extends SlackNodeState{ 

//////////////////////////// User code
/* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
   public boolean equals(Object compObj) {
    if (compObj==null || !(compObj instanceof TriPhaseSlackNodeState)) return false;
    // --- Compare voltages -------
    TriPhaseSlackNodeState compSns = (TriPhaseSlackNodeState)compObj;
    if (this.equalUniPhaseSlackNodeState(compSns.getSlackNodeStateL1(), this.getSlackNodeStateL1())==false) return false;
    if (this.equalUniPhaseSlackNodeState(compSns.getSlackNodeStateL2(), this.getSlackNodeStateL2())==false) return false;
    if (this.equalUniPhaseSlackNodeState(compSns.getSlackNodeStateL3(), this.getSlackNodeStateL3())==false) return false;
    return true;
   }
   private boolean equalUniPhaseSlackNodeState(UniPhaseSlackNodeState compState, UniPhaseSlackNodeState thisState) {
    if (compState==null && thisState==null) {
     // --- Nothing to do here -----------
    } else if ((compState==null && thisState!=null) || (compState!=null && thisState==null)) {
      return false;
    } else {
      if (compState.equals(thisState)==false) return false;
    }
    return true;
   }
   /**
* Protege name: slackNodeStateL2
   */
   private UniPhaseSlackNodeState slackNodeStateL2;
   public void setSlackNodeStateL2(UniPhaseSlackNodeState value) { 
    this.slackNodeStateL2=value;
   }
   public UniPhaseSlackNodeState getSlackNodeStateL2() {
     return this.slackNodeStateL2;
   }

   /**
* Protege name: slackNodeStateL3
   */
   private UniPhaseSlackNodeState slackNodeStateL3;
   public void setSlackNodeStateL3(UniPhaseSlackNodeState value) { 
    this.slackNodeStateL3=value;
   }
   public UniPhaseSlackNodeState getSlackNodeStateL3() {
     return this.slackNodeStateL3;
   }

   /**
* Protege name: slackNodeStateL1
   */
   private UniPhaseSlackNodeState slackNodeStateL1;
   public void setSlackNodeStateL1(UniPhaseSlackNodeState value) { 
    this.slackNodeStateL1=value;
   }
   public UniPhaseSlackNodeState getSlackNodeStateL1() {
     return this.slackNodeStateL1;
   }

}
