package de.enflexit.ea.core.aggregation;

import java.util.TreeMap;

import de.enflexit.ea.core.dataModel.graphLayout.AbstractGraphElementLayoutSettings;
import hygrid.env.ColorSettingsCollection;
import hygrid.env.ColorSettingsIntervalBased;

/**
 * General GraphElmentLayoutSettings implementation for all networks in the HyGrid-context
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class HyGridGraphElementLayoutSettings extends AbstractGraphElementLayoutSettings {

	private ColorSettingsCollection colorSettingsForNodes;
	private ColorSettingsCollection colorSettingsForEdges;
	
	/**
	 * Gets the color settings for nodes.
	 * @return the color settings for nodes
	 */
	public ColorSettingsCollection getColorSettingsForNodes() {
		if (colorSettingsForNodes==null) {
			colorSettingsForNodes = new ColorSettingsCollection();
		}
		return colorSettingsForNodes;
	}
	
	/**
	 * Sets the color settings for nodes.
	 * @param colorSettingsForNodes the new color settings for nodes
	 */
	public void setColorSettingsForNodes(ColorSettingsCollection colorSettingsForNodes) {
		this.colorSettingsForNodes = colorSettingsForNodes;
	}
	
	/**
	 * Gets the color settings for edges.
	 * @return the color settings for edges
	 */
	public ColorSettingsCollection getColorSettingsForEdges() {
		if (colorSettingsForEdges==null) {
			colorSettingsForEdges = new ColorSettingsCollection();
		}
		return colorSettingsForEdges;
	}
	
	/**
	 * Sets the color settings for edges.
	 * @param colorSettingsForEdges the new color settings for edges
	 */
	public void setColorSettingsForEdges(ColorSettingsCollection colorSettingsForEdges) {
		this.colorSettingsForEdges = colorSettingsForEdges;
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings#getSettingsAsTreeMap()
	 */
	@Override
	public TreeMap<String, String> getSettingsAsTreeMap() {
		TreeMap<String, String> settingsTreeMap = new TreeMap<>();

		settingsTreeMap.putAll(this.convertSettingsToTreeMap("nodes", this.getColorSettingsForNodes()));
		settingsTreeMap.putAll(this.convertSettingsToTreeMap("edges", this.getColorSettingsForEdges()));
		
		return settingsTreeMap;
	}
	
	/**
	 * Converts color settings for nodes or egdes to a TreeMap
	 * @param prefix the prefix "nodes" or "edges"
	 * @param settings the settings
	 * @return the tree map
	 */
	private TreeMap<String, String> convertSettingsToTreeMap(String prefix, ColorSettingsCollection settings) {
		TreeMap<String, String> settingsTreeMap = new TreeMap<>();
		
		settingsTreeMap.put(prefix + ".isEnabled", "" + settings.isEnabled());
		
		for (int i=0; i<settings.getColorSettingsVector().size(); i++) {
			ColorSettingsIntervalBased colorSeting4Component = settings.getColorSettingsVector().get(i);
			settingsTreeMap.put(prefix + ".settings"+i, colorSeting4Component.getLowerBound() + ";" + colorSeting4Component.getUpperBound() + ";" + colorSeting4Component.getValueColorString()); 
		}
		
		return settingsTreeMap;
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings#setSettingsFromTreeMap(java.util.TreeMap)
	 */
	@Override
	public void setSettingsFromTreeMap(TreeMap<String, String> treeMap) {
		
		for (String key : treeMap.keySet()) {
			ColorSettingsCollection settingsCollection = null;
			String[] keyParts = key.split("\\.");
			if (keyParts[0].equals("nodes")) {
				settingsCollection = this.getColorSettingsForNodes();
			} else if (keyParts[0].equals("edges")){
				settingsCollection = this.getColorSettingsForEdges();
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error parsing settings TreeMap - invalid key prefix " + keyParts[0]);
			}
			
			if (keyParts[1].equals("isEnabled")) {
				boolean isEnabled = Boolean.parseBoolean(treeMap.get(key));
				settingsCollection.setEnabled(isEnabled);
			} else if (keyParts[1].startsWith("settings")) {
				String[] valueParts = treeMap.get(key).split(";");
				ColorSettingsIntervalBased colorSettings = new ColorSettingsIntervalBased();
				colorSettings.setLowerBound(Double.parseDouble(valueParts[0]));
				colorSettings.setUpperBound(Double.parseDouble(valueParts[1]));
				colorSettings.setValueColorString(valueParts[2]);
				settingsCollection.addColorSettings(colorSettings);
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] Error parsing settings TreeMap - invalid key " + keyParts[1]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		String errorMessage = this.colorSettingsForNodes.getErrorMessage();
		if (errorMessage==null) {
			errorMessage = this.getColorSettingsForEdges().getErrorMessage();
		}
		return errorMessage;
	}
	
}
