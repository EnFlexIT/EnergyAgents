package de.enflexit.ea.validation;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.ea.core.dataModel.ontology.CableProperties;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * The Class ValidateCable.
 */
public class ValidateCable extends HyGridValidationAdapter {

	
	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationAdapter#validateNetworkComponent(org.awb.env.networkModel.NetworkComponent)
	 */
	@Override
	public HyGridValidationMessage validateNetworkComponent(NetworkComponent netComp) {
		
		// Check all types of cables for given parameters
		if (netComp.getType().equals("Cable")==true || netComp.getType().equals("MediumVoltageCable")==true) {
			
			// Extract dataModel
			Object[] dataModel = (Object[]) netComp.getDataModel();
			// Check if dataModel exists
			if (dataModel==null || (dataModel.getClass().isArray()==true && dataModel[0]==null)) {
				// --- No data model exists. --------------------------------------------
				String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": No Data Model found for this Component!";
				String description = "Try to edit the cable properties of " + netComp.getType() + " " + netComp.getId() + " - this is possibly already enough.";
				return new HyGridValidationMessage(message, MessageType.Error,description);
			} else {
				// -- Get the actual cable model ----------------------------------------
				CableProperties cdm = (CableProperties) dataModel[0];
				// Check for a permitted length of the cable
				if (cdm.getLength()==null || cdm.getLength().getValue() == 0) {
					String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": Length is not specified or zero!";
					String description = "Edit the length of the Cable.\nOtherwise the subnetwork can't be calculated.";
					return new HyGridValidationMessage(message, MessageType.Error,description);
				}
				
				// Check for linear Resistance or Inductance and warn if both are zero
				if (cdm.getLinearResistance().getValue()==0 && cdm.getLinearReactance().getValue()==0) {
					String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": Linear Resistance and Linear Reactance are both zero!";
					String description = "Edit at least one of the needed values.\nOtherwise the subnetwork can't be calculated.";
					return new HyGridValidationMessage(message, MessageType.Error,description);
				}
			}
		}
		
		// Check if medium voltage cable has a linear capacitance
		if (netComp.getType().equals("MediumVoltageCable")==true) {
			
			// Extract dataModel
			Object[] dataModel = (Object[]) netComp.getDataModel();
			CableProperties cableDataModel = (CableProperties) dataModel[0];
			
			// Check for linear Capacitance and warn if zero
			if(cableDataModel.getLinearCapacitance().getValue() == 0) {
				String message = "Warning for " + netComp.getType() + " " + netComp.getId() + ": Linear Capacitance is zero! ";
				String description = "Fill in the cable's capacitance, because it is not negligible for voltage levels above low voltage.\nThe subnetwork can still be calculated without this value.";
				return new HyGridValidationMessage(message, MessageType.Warning,description);
			}
		}
		
		return null;
	}
	
	
}
