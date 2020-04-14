package hygrid.env;

import java.awt.Color;
import java.util.Collections;
import java.util.Vector;

/**
 * A set of interval-based color settings.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class ColorSettingsCollection {
	
	private boolean enabled;
	private Vector<ColorSettingsIntervalBased> colorSettings;
	
	/**
	 * Checks if is enabled.
	 * @return true, if is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled.
	 * @param enabled the new enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets the color settings.
	 * @return the color settings
	 */
	public Vector<ColorSettingsIntervalBased> getColorSettingsVector() {
		if (colorSettings==null) {
			colorSettings = new Vector<>();
		}
		return colorSettings;
	}

	/**
	 * Sets the color settings.
	 * @param colorSettings the new color settings
	 */
	public void setColorSettings(Vector<ColorSettingsIntervalBased> colorSettings) {
		this.colorSettings = colorSettings;
	}
	
	/**
	 * Adds a color settings instance.
	 * @param colorSettings the color settings
	 */
	public void addColorSettings(ColorSettingsIntervalBased colorSettings) {
		//TODO Make sure there are no overlaps
		this.getColorSettingsVector().add(colorSettings);
		// --- Sort descending ----------------------------
		Collections.sort(this.getColorSettingsVector());
	}
	
	/**
	 * Gets the corresponding color settings instance for a value.
	 * @param value the value
	 * @return the color settings, null if no corresponding interval was found
	 */
	public ColorSettingsIntervalBased getColorSettingsForValue(double value) {
		for (int i=0; i<this.getColorSettingsVector().size(); i++) {
			ColorSettingsIntervalBased interval = this.getColorSettingsVector().get(i);
			if (value>=interval.getLowerBound() && (value<interval.getUpperBound()||interval.hasUpperBound()==false)) {
				return interval;
			}
		}
		
		return null;	// No matching interval found
	}

	/**
	 * Adds the new color settings.
	 * @param lowerBound the lower bound
	 * @param upperBound the upper bound
	 * @param color the color
	 * @return the color settings interval based
	 */
	public ColorSettingsIntervalBased addNewColorSettings(double lowerBound, double upperBound, Color color) {
		ColorSettingsIntervalBased newColorSettings = new ColorSettingsIntervalBased(lowerBound, upperBound, color);
		this.addColorSettings(newColorSettings);
		return newColorSettings;
	}
	
	/**
	 * Checks if the collection is free of overlaps.
	 * @return true if no overlaps are found
	 */
	public boolean hasOverlaps() {
		
		boolean overlaps = false;
		// --- Make sure the collection is sorted ---------
		Collections.sort(this.getColorSettingsVector());
		
		// --- Check for overlaps -------------------------
		for (int i=0; i<this.getColorSettingsVector().size()-1; i++) {
			ColorSettingsIntervalBased currentInterval = this.getColorSettingsVector().get(i);
			ColorSettingsIntervalBased nextInterval = this.getColorSettingsVector().get(i+1);
			
			if (nextInterval.getLowerBound()<currentInterval.getUpperBound()) {
				// --- Overlap found ----------------------
				overlaps = true;
				currentInterval.setError(true);
				nextInterval.setError(true);
			}
		}
		return overlaps;
	}

	
	/**
	 * Returns the configured color for the given value.
	 * @param value the value
	 * @return the color, null if there is no matching interval for the value
	 */
	public Color getColorForValue(double value) {
		for (int i=0; i<this.getColorSettingsVector().size(); i++) {
			ColorSettingsIntervalBased settings = this.getColorSettingsVector().get(i);
			if (settings.containsValue(value)) {
				return settings.getValueColor();
			}
		}
		return null;
	}
	
	/**
	 * Checks the defined intervals for errors.
	 * @return true if successful
	 */
	public boolean hasErrors() {
		return (this.getErrorMessage()!=null);
	}
	
	/**
	 * Gets the error message.
	 * @return the error message
	 */
	public String getErrorMessage() {
		this.resetErrorFlags();
		String errorMessage = null;
		if (this.hasOverlaps()==true) {
			errorMessage = "Overlapping intervals!";
		}
		for (int i=0; i<this.getColorSettingsVector().size(); i++) {
			if (this.getColorSettingsVector().get(i).isValidInterval()==false) {
				errorMessage = "Invalid interval - lower bound >= upper bound!";
				this.getColorSettingsVector().get(i).setError(true);
			}
		}
		return errorMessage;
	}

	/**
	 * Returns the color index for the given value, -1 if there is no matching interval
	 * @param value the value
	 * @return the color index
	 */
	public int getColorIndexForValue(float value) {
		for (int i=0; i<this.getColorSettingsVector().size(); i++) {
			if (this.getColorSettingsVector().get(i).containsValue(value)) {
				return i;
			}
		}
		return -1;
	}
	
	private void resetErrorFlags() {
		for (int i=0; i<this.getColorSettingsVector().size(); i++) {
			this.getColorSettingsVector().get(i).setError(false);
		}
	}
	
}
