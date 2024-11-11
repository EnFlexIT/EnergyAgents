package de.enflexit.ea.topologies;

import javax.swing.ImageIcon;

import energy.helper.NumberHelper;


/**
 * The Class BundleHelper provides some static help methods to be used within the bundle.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class BundleHelper {

	private static final String imagePackage = "/images/";
	
	/**
	 * Gets the image package location as String.
	 * @return the image package
	 */
	public static String getImagePackage() {
		return imagePackage;
	}
	/**
	 * Gets the image icon for the specified image.
	 *
	 * @param fileName the file name
	 * @return the image icon
	 */
	public static ImageIcon getImageIcon(String fileName) {
		String imagePackage = getImagePackage();
		ImageIcon imageIcon=null;
		try {
			imageIcon = new ImageIcon(BundleHelper.class.getResource((imagePackage + fileName)));
		} catch (Exception err) {
			System.err.println("Error while searching for image file '" + fileName + "' in " + imagePackage);
			err.printStackTrace();
		}	
		return imageIcon;
	}
	
	
	/**
	 * Parses the specified string to a Integer value .
	 *
	 * @param integerObject the double object
	 * @return the double value or null
	 */
	public static Integer parseInteger(Object integerObject) {
		if (integerObject==null) {
			return null;
		} else if (integerObject instanceof Integer) {
			return (Integer) integerObject;
		} else if (integerObject instanceof Double) {
			return ((Double) integerObject).intValue();
		} else if (integerObject instanceof String) {
			return BundleHelper.parseInteger((String) integerObject);
		}
		return null;
	}
	/**
	 * Parses the specified string to a Integer value .
	 *
	 * @param integerString the integer string
	 * @return the float value or null
	 */
	public static Integer parseInteger(String integerString) {
		Integer fValue = null;
		if (integerString!=null && integerString.isEmpty()==false) {
			// --- Replace decimal separator ? ----------------------
			if (integerString.contains(",")==true) {
				integerString = integerString.replace(",", ".");
			}
			// --- Try to parse the double string -------------------
			try {
				fValue = Integer.parseInt(integerString);
			} catch (Exception ex) {
				// --- No exception will be thrown ------------------
			}
		}
		return fValue;
	}

	
	/**
	 * Parses the specified string to a double value .
	 *
	 * @param doubleObject the double object
	 * @return the double value or null
	 */
	public static Double parseDouble(Object doubleObject) {
		if (doubleObject==null) {
			return null;
		} else if (doubleObject instanceof Double) {
			return (Double) doubleObject;
		} else if (doubleObject instanceof String) {
			return BundleHelper.parseDouble((String) doubleObject);
		}
		return null;
	}
	/**
	 * Parses the specified string to a double value .
	 *
	 * @param doubleString the double string
	 * @return the double value or null
	 */
	public static Double parseDouble(String doubleString) {
		Double dValue = null;
		if (doubleString!=null && doubleString.isEmpty()==false) {
			// --- Replace decimal separator ? ----------------------
			if (doubleString.contains(",")==true) {
				doubleString = doubleString.replace(",", ".");
			}
			// --- Determine round precision ------------------------
			Integer roundPrecision = null;
			if (doubleString.contains(".")==true) {
				roundPrecision = doubleString.length() - doubleString.indexOf("."); 
			}
			
			// --- Try to parse the double string -------------------
			try {
				dValue = Double.parseDouble(doubleString);
				if (roundPrecision!=null) {
					dValue = NumberHelper.round(dValue, roundPrecision);	
				}
			} catch (Exception ex) {
				// --- No exception will be thrown ------------------
			}
		}
		return dValue;
	}

	
	/**
	 * Parses the specified string to a double value .
	 *
	 * @param floatObject the double object
	 * @return the double value or null
	 */
	public static Float parseFloat(Object floatObject) {
		if (floatObject==null) {
			return null;
		} else if (floatObject instanceof Float) {
			return (Float) floatObject;
		} else if (floatObject instanceof String) {
			return BundleHelper.parseFloat((String) floatObject);
		}
		return null;
	}
	/**
	 * Parses the specified string to a double value .
	 *
	 * @param floatString the float string
	 * @return the float value or null
	 */
	public static Float parseFloat(String floatString) {
		Float fValue = null;
		if (floatString!=null && floatString.isEmpty()==false) {
			// --- Replace decimal separator ? ----------------------
			if (floatString.contains(",")==true) {
				floatString = floatString.replace(",", ".");
			}
			// --- Determine round precision ------------------------
			Integer roundPrecision = null;
			if (floatString.contains(".")==true) {
				roundPrecision = floatString.length() - floatString.indexOf("."); 
			}
			// --- Try to parse the double string -------------------
			try {
				fValue = Float.parseFloat(floatString);
				if (roundPrecision!=null) {
					fValue = (float) NumberHelper.round(fValue, roundPrecision);	
				}
			} catch (Exception ex) {
				// --- No exception will be thrown ------------------
			}
		}
		return fValue;
	}

}
