package de.enflexit.ea.core.configuration;

import java.io.File;

import javax.swing.ImageIcon;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;


/**
 * The Class BundleHelper provides some static help methods to be used within the bundle.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class BundleHelper {

	private static final String PREF_LAST_SELECTED_FILE = "Configurator-Last-Selected-File";
	
	private static final String imagePackage = "/images/";
	
	private static Bundle localBundle;
	private static IEclipsePreferences eclipsePreferences;
	
	
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
	 * Sets the last selected file of the current bundle.
	 * @param lastFile the new last selected file
	 */
	public static void setLastSelectedFile(File lastFile) {
		if (lastFile!=null && lastFile.exists()==true) {
			BundleHelper.getEclipsePreferences().put(PREF_LAST_SELECTED_FILE, lastFile.getAbsolutePath());
			BundleHelper.saveEclipsePreferences();
		}
	}
	/**
	 * Returns the last selected file of the current Bundle.
	 * @return the last selected file
	 */
	public static File getLastSelectedFile() {
		String lastFileString = BundleHelper.getEclipsePreferences().get(PREF_LAST_SELECTED_FILE, null);
		if (lastFileString!=null && lastFileString.isBlank()==false) {
			return new File(lastFileString);
		}
		return null;
		
	}
	
	// --------------------------------------------------------------
	// --- Provider methods to access preferences for this bundle ---
	// --------------------------------------------------------------
	/**
	 * Gets the local bundle.
	 * @return the local bundle
	 */
	public static Bundle getLocalBundle() {
		if (localBundle==null) {
			localBundle = FrameworkUtil.getBundle(BundleHelper.class);
		}
		return localBundle;
	}
	/**
	 * Returns the eclipse preferences.
	 * @return the eclipse preferences
	 */
	public static IEclipsePreferences getEclipsePreferences() {
		if (eclipsePreferences==null) {
			IScopeContext iScopeContext = ConfigurationScope.INSTANCE;
			eclipsePreferences = iScopeContext.getNode(getLocalBundle().getSymbolicName());
		}
		return eclipsePreferences;
	}
	/**
	 * Saves the current preferences.
	 */
	public static void saveEclipsePreferences() {
		try {
			getEclipsePreferences().flush();
		} catch (BackingStoreException bsEx) {
			bsEx.printStackTrace();
		}
	}
		
	
}
