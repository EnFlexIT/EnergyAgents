package de.enflexit.ea.core.aggregation.trafficLight;

import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * The Class TrafficLight describes a single traffic light that can be switched 
 * between different colors.
 * 
 * @author Christian Derksen - DAWIS - ICB University of Duisburg - Essen
 */
public class TrafficLight extends JLabel {
	
	private static final long serialVersionUID = -933908470694530740L;

	private static final String pathImage = "/icons/";
	
	private final Dimension imageDimension = new Dimension(25, 64);
	
	/** The enumeration TrafficLightColor described the possible colors of a TrafficLight. */
	public enum TrafficLightColor {
		Green,
		Yellow,
		Red
	}
	
	private String trafficLightName;
	private TrafficLightColor tlColor;
	
	private ImageIcon imageIconTrafficLightGreen;
	private ImageIcon imageIconTrafficLightYellow;
	private ImageIcon imageIconTrafficLightRed;
	
	
	/**
	 * Instantiates a new green traffic light.
	 * @param trafficLightName the traffic light name
	 */
	public TrafficLight(String trafficLightName) {
		this(trafficLightName, TrafficLightColor.Green);
	}
	/**
	 * Instantiates a new traffic light.
	 * @param trafficLightName the traffic light name
	 * @param trafficLightColor the traffic light color
	 */
	public TrafficLight(String trafficLightName, TrafficLightColor trafficLightColor) {
		this.setTrafficLightName(trafficLightName);
		this.setTrafficLightColor(trafficLightColor);
		this.initialize();
	}
	/**
	 * Initialize the traffic light size.
	 */
	private void initialize() {
		this.setHorizontalTextPosition(JLabel.CENTER);
		this.setVerticalTextPosition(JLabel.BOTTOM);
		this.setMinimumSize(this.imageDimension);
		this.setMaximumSize(this.imageDimension);
		this.setFont(new Font("Dialog", Font.BOLD, 12));
	}
	
	/**
	 * Gets the traffic light name.
	 * @return the traffic light name
	 */
	public String getTrafficLightName() {
		if (trafficLightName==null) {
			trafficLightName = "Traffic Light";
		}
		return trafficLightName;
	}
	/**
	 * Sets the traffic light name.
	 * @param trafficLightName the new traffic light name
	 */
	public void setTrafficLightName(String trafficLightName) {
		if (trafficLightName!=null) {
			this.trafficLightName = trafficLightName;
			String displayText = "<html><center>" + this.trafficLightName.replace(" ", "<br>");
			this.setText(displayText);
		}
	}
	
	/**
	 * Sets the traffic light color.
	 * @param newTrafficLightColor the new traffic light color
	 */
	public void setTrafficLightColor(TrafficLightColor newTrafficLightColor) {
		
		if (newTrafficLightColor==null) return;
		
		this.tlColor = newTrafficLightColor;
		switch (this.tlColor) {
		case Green:
			this.setIcon(this.getImageIconTrafficLightGreen());
			break;
		case Yellow:
			this.setIcon(this.getImageIconTrafficLightYellow());
			break;
		case Red:
			this.setIcon(this.getImageIconTrafficLightRed());
			break;
		}
		this.repaint();
	}
	/**
	 * Returns the traffic light color.
	 * @return the traffic light color
	 */
	public TrafficLightColor getTrafficLightColor() {
		if (tlColor==null) {
			tlColor = TrafficLightColor.Green;
		}
		return tlColor;
	}
	
	/**
	 * Returns the image icon traffic light green.
	 * @return the image icon traffic light green
	 */
	private ImageIcon getImageIconTrafficLightGreen() {
		if (imageIconTrafficLightGreen==null) {
			imageIconTrafficLightGreen = this.getInternalImageIcon("TrafficLightGreen.png");
		}
		return imageIconTrafficLightGreen;
	}
	/**
	 * Returns the image icon traffic light yellow.
	 * @return the image icon traffic light yellow
	 */
	private ImageIcon getImageIconTrafficLightYellow() {
		if (imageIconTrafficLightYellow==null) {
			imageIconTrafficLightYellow = this.getInternalImageIcon("TrafficLightYellow.png");
		}
		return imageIconTrafficLightYellow;
	}
	/**
	 * Returns the image icon traffic light red.
	 * @return the image icon traffic light red
	 */
	private ImageIcon getImageIconTrafficLightRed() {
		if (imageIconTrafficLightRed==null) {
			imageIconTrafficLightRed = this.getInternalImageIcon("TrafficLightRed.png");
		}
		return imageIconTrafficLightRed;
	}
	/**
	 * Returns one of the internal images as ImageIcon, specified by its file name.
	 *
	 * @param imageFileName the image file name
	 * @return the internal image icon
	 */
	private ImageIcon getInternalImageIcon(String imageFileName) {
		
		String imagePath = pathImage + imageFileName;
		URL imageURL = TrafficLight.class.getResource(imagePath);
		if (imageURL!=null) {
			return new ImageIcon(imageURL);
		} else {
			System.err.println("[" + TrafficLight.class.getSimpleName() + "] Could not find ImageIcon '" + imageURL + "'");
		}
		return null;
	}
	
}
