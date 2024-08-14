package de.enflexit.ea.core.configuration;

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

import de.enflexit.ea.core.configuration.model.components.ConfigurableComponent;

/**
 * The Class SetupConfigurationAttributeService serves as container for the combination
 * of a {@link SetupConfigurationService} and the List of {@link SetupConfigurationAttributeService}s.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationAttributeService implements Comparable<SetupConfigurationAttributeService> {

	private SetupConfigurationService service;
	private SetupConfigurationAttribute<?> attribute;
	
	/**
	 * Instantiates a new configuration service attribute.
	 *
	 * @param service the setup configuration service
	 * @param attribute the setup configuration attribute
	 */
	public SetupConfigurationAttributeService(SetupConfigurationService service, SetupConfigurationAttribute<?> attribute) {
		this.service = service;
		this.attribute = attribute;
	}
	/**
	 * Returns the setup configuration service.
	 * @return the setup configuration service
	 */
	public SetupConfigurationService getSetupConfigurationService() {
		return service;
	}
	/**
	 * Returns the setup configuration attribute.
	 * @return the setup configuration attribute
	 */
	public SetupConfigurationAttribute<?> getSetupConfigurationAttribute() {
		return attribute;
	}

	/**
	 * Returns the configuration options.
	 * @return the configuration options
	 */
	public List<?> getConfigurationOptions() {
		return this.getSetupConfigurationAttribute().getConfigurationOptions();
	}
	/**
	 * Checks for configuration options in a {@link SetupConfigurationAttribute}.
	 * @return true, if successful
	 */
	public boolean hasConfigurationOptions() {
		return this.getConfigurationOptions()!=null;
	}
	/**
	 * Returns the combo box model for the configuration options.
	 * @return the combo box model
	 */
	public DefaultComboBoxModel<Object> getComboBoxModel() {
		if (this.hasConfigurationOptions()==false) return null;
    	return new DefaultComboBoxModel<>(new Vector<>(this.getConfigurationOptions()));
	}
	/**
	 * Returns the index of selected configuration option.
	 *
	 * @param selectedOption the selected option
	 * @return the index of selected configuration option
	 */
	public int getIndexOfSelectedConfigurationOption(Object selectedOption) {
		
    	int indexFound = 0;
    	List<?> configOptions = this.getConfigurationOptions();
    	if (selectedOption!=null) {
    		// --- Search in option List ----------------------------
    		for (int i = 0; i < configOptions.size(); i++) {
    			
    			Object option = configOptions.get(i);
    			if (option instanceof String) {
    				// --- String comparison ------------------------
    				String optionString = (String) option;
    				if (optionString.equals(selectedOption)==true) {
    					indexFound = i;
    					break;
    				}
    				
    			} else if (option instanceof Integer) {
    				// --- Integer comparison -----------------------
    				Integer integerOption = (Integer) option;
    				if (integerOption.equals(integerOption)) {
    					indexFound = i;
    					break;
    				}
    				
    			} else if (option instanceof Float) {
    				// --- Float comparison ------------------------
    				Float floatOption = (Float) option;
    				if (floatOption.equals(floatOption)) {
    					indexFound = i;
    					break;
    				}
    			
    			} else if (option instanceof Double) {
    				// --- Double comparison ------------------------
    				Double doubleOption = (Double) option;
    				if (doubleOption.equals(doubleOption)) {
    					indexFound = i;
    					break;
    				}
    			
    			} else {
    				// --- Backups trial ----------------------------
    				if (option.equals(selectedOption)==true) {
    					indexFound = i;
    					break;
    				}
    			}
    		}
    	}
		return indexFound;
	}
	
	/**
	 * Returns the configuration option that can be identified by the specified object value.
	 *
	 * @param objectValue the object value
	 * @return the configuration option
	 */
	public Object getConfigurationOption(Object objectValue) {

		Class<?> type = this.getSetupConfigurationAttribute().getType();
		if (type==String.class) {
			// ------------------------------------------------------
			// --- For String configuration options -----------------
			// ------------------------------------------------------			
			String configOptionFound = null;
			String stringValue = (String) objectValue;

			// --- Check if option is somehow in the list -----------
			@SuppressWarnings("unchecked")
			List<String> configOptionList = (List<String>) this.getConfigurationOptions();		
			if (stringValue==null || stringValue.isBlank()==true) {
				// --- For null or empty strings --------------------
//				configOptionFound = configOptionList.get(0);
				return null;
				
			} else if (configOptionList.contains(stringValue)==true) {
				// --- Found option in list -------------------------
				configOptionFound = stringValue;
				
			} else {
				// --- Search for an equivalent ---------------------
				for (String stringConfigOption : configOptionList) {
					// --- Get the short form of the option --------- 
					int cutPos = stringConfigOption.indexOf("(");
					String stringConfigOptionShort = cutPos!=-1 ? stringConfigOption.substring(0, cutPos).trim() : stringConfigOption;
					// --- Check lower case of original -------------
					if (stringConfigOption.toLowerCase().trim().equals( stringValue.toLowerCase().trim() ) == true) {
						configOptionFound = stringConfigOption;
						break;
					}
					// --- Check lower case of short ----------------
					if (stringConfigOptionShort.toLowerCase().trim().equals( stringValue.toLowerCase().trim() ) == true) {
						configOptionFound = stringConfigOption;
						break;
					}
				}
				
			}
			// --- Change object value? ----------------------------- 
			if (configOptionFound!=null) {
				objectValue = configOptionFound;
			} else {
				objectValue = configOptionList.get(0);
			}
		
		}
		return objectValue;
	}
	
	/**
	 * Returns the simple service name.
	 * @return the service name
	 */
	public String getServiceName() {
		return this.getSetupConfigurationService().getClass().getSimpleName();
	}
	/**
	 * Returns the attribute name.
	 * @return the attribute name
	 */
	public String getAttributeName() {
		return this.getSetupConfigurationAttribute().getColumnHeader();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getServiceName() + " - " + this.getAttributeName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SetupConfigurationAttributeService scsa) {
		return this.toString().toLowerCase().compareTo(scsa.toString().toLowerCase());
	}
	
	
	/**
	 * Will return the value for the specified ConfigurableComponent by using the local .
	 *
	 * @param confComp the ConfigurableComponent to configure
	 * @return the value
	 */
	public Object getValue(ConfigurableComponent confComp) {
		
		if (confComp==null) return null;
		
		Object value = null;
		try {
			value = this.getSetupConfigurationAttribute().getValue(confComp);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return value;
	}

	/**
	 * Will set the specified new value to the ConfigurableComponent.
	 *
	 * @param confComp the ConfigurableComponent to work on
	 * @param newValue the new value
	 */
	public void setValue(ConfigurableComponent confComp, Object newValue) {
		
		if (confComp==null || newValue==null) return;
		
		try {
			this.getSetupConfigurationAttribute().setValue(confComp, newValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
