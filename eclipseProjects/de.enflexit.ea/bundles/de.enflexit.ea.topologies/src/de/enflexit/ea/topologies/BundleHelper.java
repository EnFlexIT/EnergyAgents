package de.enflexit.ea.topologies;

import javax.swing.ImageIcon;


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
			// --- Try to parse the double string -------------------
			try {
				dValue = Double.parseDouble(doubleString);
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
			// --- Try to parse the double string -------------------
			try {
				fValue = Float.parseFloat(floatString);
			} catch (Exception ex) {
				// --- No exception will be thrown ------------------
			}
		}
		return fValue;
	}

}
