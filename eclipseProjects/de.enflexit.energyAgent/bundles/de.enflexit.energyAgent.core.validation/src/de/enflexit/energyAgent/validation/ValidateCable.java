package de.enflexit.energyAgent.validation;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.energyAgent.core.validation.HyGridValidationAdapter;
import de.enflexit.energyAgent.core.validation.HyGridValidationMessage;
import de.enflexit.energyAgent.core.validation.HyGridValidationMessage.MessageType;
import hygrid.globalDataModel.ontology.CableProperties;

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
			if(dataModel == null) {
				// dataModel does not exist. Please generate one by loading the properties
				String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": No Data Model found for this Component!";
				String description = "Try to rebould the component. It can be enough to edit the properties of " + netComp.getType() + " " + netComp.getId() + ".";
				return new HyGridValidationMessage(message, MessageType.Error,description);
			} else {
				CableProperties cableDataModel = (CableProperties) dataModel[0];
				
				// Check for a permitted length of the cable
				if(cableDataModel.getLength().getValue() == 0) {
					String message = "Error found for " + netComp.getType() + " " + netComp.getId() + ": Length is zero!";
					String description = "Edit the length of the Cable.\nOtherwise the subnetwork can't be calculated.";
					return new HyGridValidationMessage(message, MessageType.Error,description);
				}
				
				// Check for linear Resistance or Inductance and warn if both are zero
				if(cableDataModel.getLinearResistance().getValue() == 0 && cableDataModel.getLinearReactance().getValue() == 0) {
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
