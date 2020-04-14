package de.enflexit.ea.core.validation;

import java.util.ArrayList;

/**
 * The Interface HyGridValidationService serves as base class to register classes that implement {@link HyGridValidationAdapter}
 * and that do checks of the current Agent.HyGrid setup. To register such an OSGI-service from your bundle, add a further service
 * description to your own bundle (OSGI-INF) and configure this interface as service. 
 */
public interface HyGridValidationService {

	/**
	 * Has to return the list of classes that do the actual checks of the current HyGrid configuration.
	 *
	 * @param isHeadlessOperation the indicator, if the current execution of AWB is in headless operation mode
	 * @return the list of Agent.HyGrid validation checks
	 */
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation);
	
}
