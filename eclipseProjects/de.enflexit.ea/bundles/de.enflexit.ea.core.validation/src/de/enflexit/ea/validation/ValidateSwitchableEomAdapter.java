package de.enflexit.ea.validation;

import java.util.ArrayList;
import java.util.List;

import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import de.enflexit.ea.core.awbIntegration.adapter.SwitchableEomAdapter;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * The Class ValidateSwitchableEomAdapter checks the graph settings for .
 */
public class ValidateSwitchableEomAdapter extends HyGridValidationAdapter {
	
	private static final String SEA_CLASS_NAME_OLD = "hygrid.agent.SwitchableEomAdapter";
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateGeneralGraphSettingsAfterFileLoad(org.awb.env.networkModel.settings.GeneralGraphSettings4MAS)
	 */
	@Override
	public HyGridValidationMessage validateGeneralGraphSettingsAfterFileLoad(GeneralGraphSettings4MAS graphSettings) {

		HyGridValidationMessage vMessage = null;
		
		List<ComponentTypeSettings> ctsList = new ArrayList<>(graphSettings.getCurrentCTS().values());
		for (int i = 0; i < ctsList.size(); i++) {
			// --- Check each ComponentTypeSettings instance -------- 
			ComponentTypeSettings cts = ctsList.get(i);
			if (cts.getAdapterClass()!=null && cts.getAdapterClass().isEmpty()==false && cts.getAdapterClass().equals(SEA_CLASS_NAME_OLD)==true) {
				cts.setAdapterClass(SwitchableEomAdapter.class.getName());
				// --- Create inform message ------------------------
				vMessage = this.getClassNameChangedMessage();
				vMessage.setMessage(vMessage.getMessage() + " in the graph settings of the network model!");
				this.printHyGridValidationMessageToConsole(vMessage);
			}
		}
		return vMessage;
	}
	
	/**
	 * Returns a default HyGridValidationMessage that the manages class name was changed.
	 * @return the class name changed message
	 */
	private HyGridValidationMessage getClassNameChangedMessage() {
		String message = "Adjusted class name for the SwitchableEomAdapter";
		String description = "Changed class name from '" + SEA_CLASS_NAME_OLD + "' to '" + SwitchableEomAdapter.class.getName() + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
