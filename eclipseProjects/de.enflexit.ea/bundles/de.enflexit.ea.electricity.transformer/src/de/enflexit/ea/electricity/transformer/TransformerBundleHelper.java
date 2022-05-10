package de.enflexit.ea.electricity.transformer;

import javax.swing.ImageIcon;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;


/**
 * The Class TransformerBundleHelper provides static help methods for bundle images and properties.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class TransformerBundleHelper {

	private static final String imagePackage = "/icons/";
	private static IEclipsePreferences eclipsePreferences;
	
	public static final String PROP_TRANSFOMRER_SELECTION_FILE = "TransformerSeletionFile";
	
	
	/**
	 * Returns the eclipse preferences.
	 * @return the eclipse preferences
	 */
	public static IEclipsePreferences getEclipsePreferences() {
		if (eclipsePreferences==null) {
			Bundle localBundle = FrameworkUtil.getBundle(TransformerBundleHelper.class);
			IScopeContext iScopeContext = ConfigurationScope.INSTANCE;
			eclipsePreferences = iScopeContext.getNode(localBundle.getSymbolicName());
		}
		return eclipsePreferences;
	}
	
	/**
	 * Saves the bundle properties.
	 */
	public static void save() {
		try {
			TransformerBundleHelper.getEclipsePreferences().flush();
		} catch (BackingStoreException bsEx) {
			bsEx.printStackTrace();
		}
	}
	
	
	public static void putString(String key, String value) {
		getEclipsePreferences().put(key, value);
		save();
	}
	public static void putBoolean(String key, boolean value) {
		getEclipsePreferences().putBoolean(key, value);
		save();
	}
	public static void putInt(String key, int value) {
		getEclipsePreferences().putInt(key, value);
		save();
	}
	public static void putLong(String key, long value) {
		getEclipsePreferences().putLong(key, value);
		save();
	}
	public static void putFloat(String key, float value) {
		getEclipsePreferences().putFloat(key, value);
		save();
	}
	public static void putDouble(String key, double value) {
		getEclipsePreferences().putDouble(key, value);
		save();
	}
	public static void putByteArry(String key, byte[] value) {
		getEclipsePreferences().putByteArray(key, value);
		save();
	}
	
	
	public static String getString(String key, String defaultValue) {
		return getEclipsePreferences().get(key, defaultValue);
	}
	public static boolean getBoolean(String key, boolean defaultValue) {
		return getEclipsePreferences().getBoolean(key, defaultValue);
	}
	public static int getInt(String key, int defaultValue) {
		return getEclipsePreferences().getInt(key, defaultValue);
	}
	public static long getLong(String key, long defaultValue) {
		return getEclipsePreferences().getLong(key, defaultValue);
	}
	public static float getFloat(String key, float defaultValue) {
		return getEclipsePreferences().getFloat(key, defaultValue);
	}
	public static double getDouble(String key, double defaultValue) {
		return getEclipsePreferences().getDouble(key, defaultValue);
	}
	public static byte[] getByteArry(String key, byte[] defaultValue) {
		return getEclipsePreferences().getByteArray(key, defaultValue);
	}
	
	
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
			imageIcon = new ImageIcon(TransformerBundleHelper.class.getResource((imagePackage + fileName)));
		} catch (Exception err) {
			System.err.println("Error while searching for image file '" + fileName + "' in " + imagePackage);
			err.printStackTrace();
		}	
		return imageIcon;
	}


}
