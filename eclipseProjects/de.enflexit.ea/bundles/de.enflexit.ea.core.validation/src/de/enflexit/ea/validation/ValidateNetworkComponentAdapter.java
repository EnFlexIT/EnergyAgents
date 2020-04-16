package de.enflexit.ea.validation;

import java.util.ArrayList;
import java.util.List;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.settings.DomainSettings;
import org.awb.env.networkModel.settings.GeneralGraphSettings4MAS;

import de.enflexit.ea.core.awbIntegration.adapter.SwitchableEomAdapter;
import de.enflexit.ea.core.validation.HyGridValidationAdapter;
import de.enflexit.ea.core.validation.HyGridValidationMessage;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;

/**
 * Checks if the {@link NetworkComponentAdapter} that were moved to the AWB integration 
 * bundle are described correctly.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class ValidateNetworkComponentAdapter extends HyGridValidationAdapter {

	private static final String ADAPTER_PACKAGE_OLD = "hygrid.env.adapter";
	private static final String ADAPTER_PACKAGE_NEW = SwitchableEomAdapter.class.getPackage().getName();
	
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.validation.HyGridValidationAdapter#validateGeneralGraphSettingsAfterFileLoad(org.awb.env.networkModel.settings.GeneralGraphSettings4MAS)
	 */
	@Override
	public HyGridValidationMessage validateGeneralGraphSettingsAfterFileLoad(GeneralGraphSettings4MAS graphSettings) {

		HyGridValidationMessage vMessage = null;
		
		// -----------------------------------------------------------
		// --- Run through the list of domain settings ---------------
		// -----------------------------------------------------------
		List<DomainSettings> dsList = new ArrayList<>(graphSettings.getDomainSettings().values());
		for (int i = 0; i < dsList.size(); i++) {
			// --- Check each ComponentTypeSettings instance -------- 
			DomainSettings ds = dsList.get(i);
			if (ds.getAdapterClass()!=null && ds.getAdapterClass().isEmpty()==false && ds.getAdapterClass().startsWith(ADAPTER_PACKAGE_OLD)) {
				String adapterClassOld = ds.getAdapterClass();
				String adapterClassNew = ADAPTER_PACKAGE_NEW + adapterClassOld.substring(ADAPTER_PACKAGE_OLD.length());
				ds.setAdapterClass(adapterClassNew);
				// --- Create inform message ------------------------
				if (vMessage==null) {
					vMessage = this.getClassNameChangedMessage();
					this.printHyGridValidationMessageToConsole(vMessage);
				}
			}
		}
		
		// -----------------------------------------------------------
		// --- Run through the list of component type definitions ----
		// -----------------------------------------------------------
		List<ComponentTypeSettings> ctsList = new ArrayList<>(graphSettings.getCurrentCTS().values());
		for (int i = 0; i < ctsList.size(); i++) {
			// --- Check each ComponentTypeSettings instance -------- 
			ComponentTypeSettings cts = ctsList.get(i);
			if (cts.getAdapterClass()!=null && cts.getAdapterClass().isEmpty()==false && cts.getAdapterClass().startsWith(ADAPTER_PACKAGE_OLD)==true) {
				
				String adapterClassOld = cts.getAdapterClass();
				String adapterClassNew = ADAPTER_PACKAGE_NEW + adapterClassOld.substring(ADAPTER_PACKAGE_OLD.length());
				cts.setAdapterClass(adapterClassNew);
				// --- Create inform message ------------------------
				if (vMessage==null) {
					vMessage = this.getClassNameChangedMessage();
					this.printHyGridValidationMessageToConsole(vMessage);
				}
			}
		}
		return vMessage;
	}
	
	/**
	 * Returns a default HyGridValidationMessage that the manages class name was changed.
	 * @return the class name changed message
	 */
	private HyGridValidationMessage getClassNameChangedMessage() {
		String message = "Adjusted class name for NetworkCompnentAdapter!";
		String description = "Changed package name from '" + ADAPTER_PACKAGE_OLD + "' to '" + ADAPTER_PACKAGE_NEW + "'.";
		return new HyGridValidationMessage(message, MessageType.Information, description);
	}
	
}
