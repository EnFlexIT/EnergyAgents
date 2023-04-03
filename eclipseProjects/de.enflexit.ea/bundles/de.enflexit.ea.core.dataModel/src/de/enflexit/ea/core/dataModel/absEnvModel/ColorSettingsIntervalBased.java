package de.enflexit.ea.core.dataModel.absEnvModel;

import java.awt.Color;
import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ColorSettingsIntervalBased", propOrder = {
    "lowerBound",
    "upperBound",
    "valueColorString"
})
public class ColorSettingsIntervalBased implements Serializable, Comparable<ColorSettingsIntervalBased>{

	private static final long serialVersionUID = 7685140365856289930L;
	
	private double lowerBound;
	private double upperBound;
	@XmlTransient private Color valueColor;
	private String valueColorString;
	
	@XmlTransient private boolean error;
	
	/**
	 * Instantiates a new color settings interval based.
	 */
	public ColorSettingsIntervalBased() {}
	
	/**
	 * Instantiates an interval-based color settings instance.
	 * @param lowerBound the lower bound of the interval (inclusive, >=)
	 * @param upperBound the upper bound of the interval (exclusive, <)
	 * @param valueColor the value color
	 */
	public ColorSettingsIntervalBased(double lowerBound, double upperBound, Color valueColor) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.valueColor = valueColor;
	}

	/**
	 * Gets the lower bound of the interval.
	 * @return the lower bound
	 */
	public double getLowerBound() {
		return lowerBound;
	}

	/**
	 * Sets the lower bound of the interval (inclusive, >=).
	 * @param lowerBound the new lower bound
	 */
	public void setLowerBound(double lowerBound) {
		this.lowerBound = lowerBound;
	}

	/**
	 * Gets the upper bound of the interval.
	 * @return the upper bound
	 */
	public double getUpperBound() {
		return upperBound;
	}

	/**
	 * Sets the upper bound of the interval (exclusive, <).
	 * @param upperBound the new upper bound
	 */
	public void setUpperBound(double upperBound) {
		this.upperBound = upperBound;
	}

	/**
	 * Sets the color to use.
	 * @param valueColor the new value color
	 */
	public void setValueColor(Color valueColor) {
		this.valueColor = valueColor;
		this.getValueColorString();
	}
	/**
	 * Returns the color to use for the current value.
	 * @return the value color
	 */
	@XmlTransient
	public Color getValueColor() {
		if (valueColor==null && this.valueColorString!=null) {
			valueColor = new Color(Integer.parseInt(this.valueColorString));
		}
		return valueColor;
	}

	/**
	 * Returns the color value string.
	 * @return the color value string
	 */
	public String getValueColorString() {
		if (this.getValueColor()!=null) {
			valueColorString = String.valueOf(this.getValueColor().getRGB());
		}
		return valueColorString;
	}
	/**
	 * Sets the color value string.
	 * @param colorValueString the new color value string
	 */
	public void setValueColorString(String colorValueString) {
		this.valueColorString = colorValueString;
	}
	
	/**
	 * Checks if the interval has a lower bound.
	 * @return true, if successful
	 */
	public boolean hasLowerBound() {
		return this.lowerBound!=Double.MIN_VALUE;
	}
	
	/**
	 * Checks if the interval has an upper bound.
	 * @return true, if successful
	 */
	public boolean hasUpperBound() {
		return this.upperBound!=Double.MAX_VALUE;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object compObj) {
		
		if (compObj==null) return false;
		if (compObj==this) return true;
		if (!(compObj instanceof ColorSettingsIntervalBased)) return false;
		
		ColorSettingsIntervalBased csIntComp = (ColorSettingsIntervalBased) compObj;
		
		if (csIntComp.getLowerBound()!=this.getLowerBound()) return false;
		if (csIntComp.getUpperBound()!=this.getUpperBound()) return false;

		String colorComp = csIntComp.getValueColorString();
		String colorThis = this.getValueColorString();
		if (colorComp==null && colorThis==null) {
			// - equals -
		} else if ((colorComp!=null && colorThis==null) || (colorComp==null && colorThis!=null)) {
			return false;
		} else {
			if (colorComp.equals(colorThis)==false) return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ColorSettingsIntervalBased otherColorSettings) {
		// --- Compare based on the lower bound ------
		return Double.valueOf(this.getLowerBound()).compareTo(otherColorSettings.getLowerBound());
	}
	
	/**
	 * Checks if the specified interval is valid.
	 * @return true, if is valid interval
	 */
	public boolean isValidInterval() {
		return (this.lowerBound<this.upperBound);
	}
	/**
	 * Checks if the specified value is within the interval for this color settings instance.
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean containsValue(double value) {
		return (this.getLowerBound()<=value && (this.getUpperBound()>value||this.hasUpperBound()==false));
	}

	/**
	 * Checks the error flag.
	 * @return true, if successful
	 */
	public boolean hasError() {
		return error;
	}
	/**
	 * Sets the error flag.
	 * @param error the new error
	 */
	public void setError(boolean error) {
		this.error = error;
	}
	
	
}
