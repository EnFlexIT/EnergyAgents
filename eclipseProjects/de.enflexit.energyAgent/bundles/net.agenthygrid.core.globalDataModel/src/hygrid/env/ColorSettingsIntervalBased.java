package hygrid.env;

import java.awt.Color;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

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

	@Override
	public int compareTo(ColorSettingsIntervalBased otherColorSettings) {
		// --- Compare based on the lower bound ------
		return new Double(this.getLowerBound()).compareTo(otherColorSettings.getLowerBound());
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
