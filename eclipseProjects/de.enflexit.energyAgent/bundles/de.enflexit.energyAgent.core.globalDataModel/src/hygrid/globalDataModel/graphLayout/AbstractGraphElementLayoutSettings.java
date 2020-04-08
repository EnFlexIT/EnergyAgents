package hygrid.globalDataModel.graphLayout;

import java.util.TreeMap;

/**
 * Abstract superclass for domain-specific GraphElementLayoutSettings
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public abstract class AbstractGraphElementLayoutSettings {

	/**
	 * Converts the settings to a TreeMap of Strings for persisting. 
	 * @return the settings as tree map
	 */
	public abstract TreeMap<String, String> getSettingsAsTreeMap();
	
	/**
	 * Initializes this settings instance from a TreeMap of Strings, as provided by getSettingsAsTreeMap()
	 * @param treeMap
	 */
	public abstract void setSettingsFromTreeMap(TreeMap<String, String> treeMap);
	
	/**
	 * Returns the corresponding error message if this settings instance contains configuration errors.
	 * @return the error message, null if there are no errors.
	 */
	public abstract String getErrorMessage();
	
}
