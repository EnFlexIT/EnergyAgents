package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: VoltageBandInformation
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class VoltageBandInformation extends Physical{ 

//////////////////////////// User code
public VoltageBand getVoltageBandNotNull() {
	if (this.voltageBand == null) this.voltageBand = new VoltageBand(); 
	return this.voltageBand;
}
   /**
* Protege name: voltageBand
   */
   private VoltageBand voltageBand;
   public void setVoltageBand(VoltageBand value) { 
    this.voltageBand=value;
   }
   public VoltageBand getVoltageBand() {
     return this.voltageBand;
   }

}
