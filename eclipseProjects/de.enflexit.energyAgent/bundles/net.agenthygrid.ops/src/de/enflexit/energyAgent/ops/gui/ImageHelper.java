package de.enflexit.energyAgent.ops.gui;

import java.net.URL;

/**
 * The Class ImageHelper.
 */
public class ImageHelper {

	private final static String localPathImageIntern = "/hygrid/ops/gui/img/";
	
	/**
	 * Returns the path to the internal image-package of Agent.GUI (de.enflexit.common.swing.img)
	 * @return path to the images, which are located in our project
	 */
	public static String getPathImageIntern(){
		return localPathImageIntern;
	}
	/**
	 * Returns one of the internal images as ImageIcon, specified by its file name.
	 * @param imageFileName the image file name
	 * @return the internal image icon
	 */
	public static javax.swing.ImageIcon getInternalImageIcon(String imageFileName) {
		String imagePath = getPathImageIntern() + imageFileName;
		URL imageURL = ImageHelper.class.getResource(imagePath);
		if (imageURL!=null) {
			return new javax.swing.ImageIcon(imageURL);
		} else {
			System.err.println(ImageHelper.class.getSimpleName() + ": Could not find ImageIcon '" + imageURL + "'");
		}
		return null;
	}
	/**
	 * Returns one of the internal images specified by its file name.
	 * @param imageFileName the image file name
	 * @return the internal image
	 */
	public static java.awt.Image getInternalImage(String imageFileName) {
		javax.swing.ImageIcon imageIcon = getInternalImageIcon(imageFileName);
		if (imageIcon!=null) {
			return imageIcon.getImage();
		}
		return null;
	}
}
