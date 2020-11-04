package de.enflexit.ea.core.dataModel.graphLayout;

import java.util.TreeMap;

/**
 * Abstract superclass for domain-specific GraphElementLayoutSettings.
 * 
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

	/**
	 * Has to check if the specified instance is equal to the current instance.
	 *
	 * @param gelsComp the AbstractGraphElementLayoutSettings to compare
	 * @return true, if the specified instance is equal
	 */
	protected abstract boolean isEqualGraphElementLayoutSettings(AbstractGraphElementLayoutSettings gelsComp);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObj) {
		if (compObj instanceof AbstractGraphElementLayoutSettings) {
			return this.isEqualGraphElementLayoutSettings((AbstractGraphElementLayoutSettings) compObj);
		}
		return false;
	}
	
}
