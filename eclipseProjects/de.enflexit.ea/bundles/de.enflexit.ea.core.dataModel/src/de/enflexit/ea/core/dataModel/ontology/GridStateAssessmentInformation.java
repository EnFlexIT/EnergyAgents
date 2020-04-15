package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GridStateAssessmentInformation
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class GridStateAssessmentInformation extends Physical{ 

//////////////////////////// User code
public GridStateAssessment getGridStateAssessmentNotNull() {
	if (this.gridStateAssessment == null) this.gridStateAssessment = new GridStateAssessment(); 
	return this.gridStateAssessment;
}
   /**
* Protege name: gridStateAssessment
   */
   private GridStateAssessment gridStateAssessment;
   public void setGridStateAssessment(GridStateAssessment value) { 
    this.gridStateAssessment=value;
   }
   public GridStateAssessment getGridStateAssessment() {
     return this.gridStateAssessment;
   }

   /**
* Protege name: voltageRange
   */
   private VoltageBand voltageRange;
   public void setVoltageRange(VoltageBand value) { 
    this.voltageRange=value;
   }
   public VoltageBand getVoltageRange() {
     return this.voltageRange;
   }

}
