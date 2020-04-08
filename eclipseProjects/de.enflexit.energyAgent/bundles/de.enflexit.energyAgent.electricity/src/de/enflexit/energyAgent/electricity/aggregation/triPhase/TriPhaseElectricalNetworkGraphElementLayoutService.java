package de.enflexit.energyAgent.electricity.aggregation.triPhase;

import java.awt.Color;
import java.util.TreeMap;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphElementLayout;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.core.project.Project;
import de.enflexit.energyAgent.core.aggregation.HyGridGraphElementLayoutSettings;
import de.enflexit.energyAgent.core.aggregation.HyGridGraphElementLayoutSettingsPanel;
import hygrid.env.ColorSettingsCollection;
import hygrid.env.GraphElementLayoutSettingsPersistenceTreeMap;
import hygrid.globalDataModel.GlobalHyGridConstants;
import hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings;
import hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettingsPanel;
import hygrid.globalDataModel.graphLayout.GraphElementLayoutService;
import hygrid.globalDataModel.ontology.TriPhaseCableState;
import hygrid.globalDataModel.ontology.TriPhaseElectricalNodeState;

/**
 * {@link GraphElementLayoutService} implementation for tri-phase low voltage distribution grids.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class TriPhaseElectricalNetworkGraphElementLayoutService implements GraphElementLayoutService {
	
	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#getDomain()
	 */
	@Override
	public String getDomain() {
		return GlobalHyGridConstants.HYGRID_DOMAIN_ELECTRICITY_400V;
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#getGraphElementLayout(org.awb.env.networkModel.GraphElement)
	 */
	@Override
	public GraphElementLayout getGraphElementLayout(GraphElement graphElement, NetworkModel networkModel, AbstractGraphElementLayoutSettings layoutSettings) {
		HyGridGraphElementLayoutSettings hygridLayoutSettings = (HyGridGraphElementLayoutSettings) layoutSettings;
		if (graphElement instanceof GraphNode) {
			return this.getGraphElementLayoutForNode((GraphNode) graphElement, networkModel, hygridLayoutSettings.getColorSettingsForNodes());
		} else if (graphElement instanceof GraphEdge) {
			return this.getGraphElementLayoutForEdge((GraphEdge) graphElement, networkModel, hygridLayoutSettings.getColorSettingsForEdges());
		}
		return null;
	}
	

	/**
	 * Gets the graph element layout for a node.
	 * @param graphNode the node
	 * @param networkModel the network model
	 * @param colorSettings the color settings
	 * @return the graph element layout
	 */
	private GraphElementLayout getGraphElementLayoutForNode(GraphNode graphNode, NetworkModel networkModel, ColorSettingsCollection colorSettings) {
		GraphElementLayout layout = graphNode.getGraphElementLayout(networkModel);
		
		if (layout!=null) {
			// --- If the defined color is set to black or white, do not adjust the color --------
			Color currentColor = layout.getColor();
			if (currentColor.equals(Color.WHITE) || currentColor.equals(Color.BLACK)) {
				return null;
			}
			
			// --- Get the state object from the data model ------------------- 
			Object[] dataModelArray = null;
			Object dataModel = graphNode.getDataModel();
			if (dataModel instanceof Object[]) {
				dataModelArray = (Object[]) graphNode.getDataModel();
			} else {
				if (dataModel instanceof TreeMap<?, ?>) {
					@SuppressWarnings("unchecked")
					TreeMap<String, Object> dmTreeMap = (TreeMap<String, Object>) dataModel;
					dataModelArray = (Object[]) dmTreeMap.get(this.getDomain());
				}
			}
			
			// --- Create the GraphElementLayout ------------------------------
			if (dataModelArray!=null) {
				TriPhaseElectricalNodeState triPhaseNodeState = (TriPhaseElectricalNodeState) dataModelArray[1];
				
				// --- Determine the new color based on the state ----------------- 
				float voltageL1 = triPhaseNodeState.getL1().getVoltageAbs().getValue();
				float voltageL2 = triPhaseNodeState.getL2().getVoltageAbs().getValue();
				float voltageL3 = triPhaseNodeState.getL3().getVoltageAbs().getValue();
				float minVoltage = Math.min(voltageL1, Math.max(voltageL2, voltageL3));
				float maxVoltage = Math.max(voltageL1, Math.max(voltageL2, voltageL3));
				
				Color minColor = colorSettings.getColorForValue(minVoltage);
				Color maxColor = colorSettings.getColorForValue(maxVoltage);
				
				// --- Decide which color to use ----------------------------
				Color newColor = null;
				if (minColor==null && maxColor!=null) {
					newColor = maxColor;
				} else if (minColor!=null && maxColor==null) {
					newColor = minColor;
				} else if (minColor.equals(maxColor)==true) {
					newColor = minColor;
				} else {
					int avgR = (minColor.getRed() + maxColor.getRed())/2;
					int avgG = (minColor.getGreen() + maxColor.getGreen())/2;
					int avgB = (minColor.getBlue() + maxColor.getBlue())/2;
					newColor = new Color(avgR, avgG, avgB);
				}
				if (newColor!=null && newColor.equals(layout.getColor())==false) {
					// --- Define the new layout of the GraphEdge ---------------
					layout.setColor(newColor);
				}
			}
		}
		
		return layout;
	}

	
	/**
	 * Gets the graph element layout for an edge.
	 * @param edge the edge
	 * @param networkModel the network model
	 * @param colorSettingsForEdges the color settings for edges
	 * @return the graph element layout
	 */
	private GraphElementLayout getGraphElementLayoutForEdge(GraphEdge edge, NetworkModel networkModel, ColorSettingsCollection colorSettingsForEdges) {
		
		NetworkComponent netComp = networkModel.getGraphElementToNetworkComponentHash().get(edge).get(0);
		
		// --- Get the state object from the data model ------------------- 
		Object[] dataModelArray = null;
		Object dataModel = netComp.getDataModel();
		if (dataModel instanceof Object[]) {
			dataModelArray = (Object[]) netComp.getDataModel();
		} else {
			if (dataModel instanceof TreeMap<?, ?>) {
				@SuppressWarnings("unchecked")
				TreeMap<String, Object> dmTreeMap = (TreeMap<String, Object>) dataModel;
				dataModelArray = (Object[]) dmTreeMap.get(this.getDomain());
			}
		}
		
		// --- Create the GraphElementLayout ------------------------
		GraphElementLayout layout = null;
		if (dataModelArray!=null) {
			
			TriPhaseCableState cableState = (TriPhaseCableState) dataModelArray[1];
			
			// --- Define the new color of the GraphEdge ------------
			float maxUtility = Math.max(cableState.getUtil_L1(), Math.max(cableState.getUtil_L2(), cableState.getUtil_L3()));
			Color newColor = colorSettingsForEdges.getColorForValue(maxUtility);
			
			// --- Define the new layout of the GraphEdge -----------
			if (newColor!=null) {
				layout = edge.getGraphElementLayout(networkModel);
				if (layout!=null && layout.getMarkerColor().equals(newColor)==false){
					layout.setMarkerShow(true);
					layout.setMarkerStrokeWidth(10.0f);
					layout.setMarkerColor(newColor);
				}
			}
		}
		return layout;
	}
	
	
	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#getGraphElementLayoutSettingPanel(agentgui.core.project.Project, java.lang.String)
	 */
	@Override
	public AbstractGraphElementLayoutSettingsPanel getGraphElementLayoutSettingPanel(Project project, String domain) {
		return new HyGridGraphElementLayoutSettingsPanel(project, domain);
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#convertInstanceToTreeMap(hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings)
	 */
	@Override
	public GraphElementLayoutSettingsPersistenceTreeMap convertInstanceToTreeMap(AbstractGraphElementLayoutSettings settings) {
		GraphElementLayoutSettingsPersistenceTreeMap settingsTreeMap = new GraphElementLayoutSettingsPersistenceTreeMap();
		settingsTreeMap.setDomain(this.getDomain());
		settingsTreeMap.setSettingsTreeMap(settings.getSettingsAsTreeMap());
		return settingsTreeMap;
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#convertTreeMapToInstance(hygrid.env.GraphElementLayoutSettingsTreeMap)
	 */
	@Override
	public AbstractGraphElementLayoutSettings convertTreeMapToInstance(GraphElementLayoutSettingsPersistenceTreeMap treeMap) {
		HyGridGraphElementLayoutSettings layoutSettings = new HyGridGraphElementLayoutSettings();
		layoutSettings.setSettingsFromTreeMap(treeMap.getSettingsTreeMap());
		return layoutSettings;
	}

}
