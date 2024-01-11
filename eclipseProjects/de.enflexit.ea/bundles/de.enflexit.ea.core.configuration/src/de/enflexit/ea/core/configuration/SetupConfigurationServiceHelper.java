package de.enflexit.ea.core.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.enflexit.common.ServiceFinder;

/**
 * The Class SetupConfigurationServiceHelper provides some static help methods to work 
 * with registered {@link SetupConfigurationService}s.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationServiceHelper {
	
	/**
	 * Returns the registered {@link SetupConfigurationService}s.
	 * @return the registered services
	 */
	public static List<SetupConfigurationService> getRegisteredServices() {
		return ServiceFinder.findServices(SetupConfigurationService.class, false);
	}
	
	/**
	 * Returns the list of {@link SetupConfigurationAttributeService}s in a sorted manner.
	 * @return the setup configuration attribute list
	 */
	public static List<SetupConfigurationAttributeService> getSetupConfigurationAttributeList() {
		
		List<SetupConfigurationAttributeService> attributeServiceList = new ArrayList<>();
		List<SetupConfigurationService> serviceList = getRegisteredServices();
		for (SetupConfigurationService service : serviceList) {
			for (SetupConfigurationAttribute<?> attribute : service.getConfigurationAttributeList()) {
				attributeServiceList.add(new SetupConfigurationAttributeService(service, attribute));
			}
		}
		// --- Sort the list of attributes ----------------
		Collections.sort(attributeServiceList);
		
		return attributeServiceList;
	}
	
	/**
	 * Returns the list of {@link SetupConfigurationAttributeService}s that provide an additional configuration UI in a sorted manner.
	 * @return the setup configuration attribute list
	 */
	public static List<SetupConfigurationAttributeService> getSetupConfigurationAttributeListWithUIs() {
		
		List<SetupConfigurationAttributeService> attributeServiceList = new ArrayList<>();
		for (SetupConfigurationService service : getRegisteredServices()) {
			for (SetupConfigurationAttribute<?> attribute : service.getConfigurationAttributeList()) {
				if (attribute instanceof SetupConfigurationAttributeWithUI) {
					attributeServiceList.add(new SetupConfigurationAttributeService(service, attribute));
				}
			}
		}
		// --- Sort the list of attributes ----------------
		Collections.sort(attributeServiceList);
		
		return attributeServiceList;
	}
	
}
