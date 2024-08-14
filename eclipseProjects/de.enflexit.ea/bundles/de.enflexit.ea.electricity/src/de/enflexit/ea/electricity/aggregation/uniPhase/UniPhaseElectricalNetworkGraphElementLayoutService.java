package de.enflexit.ea.electricity.aggregation.uniPhase;

import java.awt.Color;
import java.util.List;
import java.util.TreeMap;

import org.awb.env.networkModel.GraphEdge;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphElementLayout;
import org.awb.env.networkModel.GraphNode;
import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import agentgui.core.project.Project;
import de.enflexit.ea.core.aggregation.HyGridGraphElementLayoutSettings;
import de.enflexit.ea.core.aggregation.HyGridGraphElementLayoutSettingsPanel;
import de.enflexit.ea.core.dataModel.GlobalHyGridConstants;
import de.enflexit.ea.core.dataModel.absEnvModel.ColorSettingsCollection;
import de.enflexit.ea.core.dataModel.absEnvModel.GraphElementLayoutSettingsPersistenceTreeMap;
import de.enflexit.ea.core.dataModel.graphLayout.AbstractGraphElementLayoutSettings;
import de.enflexit.ea.core.dataModel.graphLayout.AbstractGraphElementLayoutSettingsPanel;
import de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseCableState;
import de.enflexit.ea.core.dataModel.ontology.UniPhaseElectricalNodeState;
import de.enflexit.ea.electricity.ElectricityDomainIdentification;

/**
 * {@link GraphElementLayoutService} implementation for uni-phase electrical Networks.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class UniPhaseElectricalNetworkGraphElementLayoutService implements GraphElementLayoutService {

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService#getDomainList(agentgui.core.project.Project)
	 */
	@Override
	public List<String> getDomainList(Project currProject) {
		return ElectricityDomainIdentification.getDomainList(this, currProject);
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#getGraphElementLayoutSettingPanel(agentgui.core.project.Project, java.lang.String)
	 */
	@Override
	public AbstractGraphElementLayoutSettingsPanel getGraphElementLayoutSettingPanel(Project project, String domain) {
		return new HyGridGraphElementLayoutSettingsPanel(project, domain);
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
					for (String domain : this.getDomainList(null)) {
						dataModelArray = (Object[]) dmTreeMap.get(domain);
						if (dataModelArray!=null) break;
					}
					// --- Fallback / legacy trial ----------------------------
					if (dataModelArray==null) {
						dataModelArray = (Object[]) dmTreeMap.get(GlobalHyGridConstants.DEPRECATED_DOMAIN_ELECTRICITY_10KV);
					}
				}
			}
			
			// --- Create the GraphElementLayout ------------------------------
			if (dataModelArray!=null) {
				UniPhaseElectricalNodeState triPhaseNodeState = (UniPhaseElectricalNodeState) dataModelArray[1];
				
				// --- Determine the new color based on the state ----------------- 
				float voltage = triPhaseNodeState.getVoltageAbs().getValue();
				Color newColor = colorSettings.getColorForValue(voltage);
				if (newColor!=null && newColor.equals(layout.getColor())==false) {
					// --- Define the new layout of the GraphNode ---------------
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
				for (String domain : this.getDomainList(null)) {
					dataModelArray = (Object[]) dmTreeMap.get(domain);
					if (dataModelArray!=null) break;
				}
				// --- Fallback / legacy trial ----------------------------
				if (dataModelArray==null) {
					dataModelArray = (Object[]) dmTreeMap.get(GlobalHyGridConstants.DEPRECATED_DOMAIN_ELECTRICITY_10KV);
				}
			}
		}
		
		// --- Create the GraphElementLayout ------------------------
		GraphElementLayout layout = null;
		if (dataModelArray!=null) {
			
			UniPhaseCableState cableState = (UniPhaseCableState) dataModelArray[1];
			
			// --- Define the new color of the GraphEdge ------------
			float utilization = cableState.getUtilization();
			Color newColor = colorSettingsForEdges.getColorForValue(utilization);
			
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
	 * @see de.enflexit.ea.core.dataModel.graphLayout.GraphElementLayoutService#convertInstanceToTreeMap(de.enflexit.ea.core.dataModel.graphLayout.AbstractGraphElementLayoutSettings, java.lang.String)
	 */
	@Override
	public GraphElementLayoutSettingsPersistenceTreeMap convertInstanceToTreeMap(AbstractGraphElementLayoutSettings settings, String domain) {
		GraphElementLayoutSettingsPersistenceTreeMap settingsTreeMap = new GraphElementLayoutSettingsPersistenceTreeMap();
		settingsTreeMap.setDomain(domain);
		settingsTreeMap.setSettingsTreeMap(settings.getSettingsAsTreeMap());
		return settingsTreeMap;
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.GraphElementLayoutService#convertTreeMapToInstance(de.enflexit.ea.core.dataModel.absEnvModel.GraphElementLayoutSettingsTreeMap)
	 */
	@Override
	public AbstractGraphElementLayoutSettings convertTreeMapToInstance(GraphElementLayoutSettingsPersistenceTreeMap treeMap) {
		HyGridGraphElementLayoutSettings layoutSettings = new HyGridGraphElementLayoutSettings();
		layoutSettings.setSettingsFromTreeMap(treeMap.getSettingsTreeMap());
		return layoutSettings;
	}

}
