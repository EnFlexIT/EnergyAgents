package de.enflexit.ea.core.dataModel.graphLayout;

import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.GraphElementLayout;
import org.awb.env.networkModel.NetworkModel;

import agentgui.core.project.Project;
import de.enflexit.ea.core.dataModel.absEnvModel.GraphElementLayoutSettingsPersistenceTreeMap;

/**
 * The Interface GraphElementLayoutService.
 */
public interface GraphElementLayoutService {

	/**
	 * Returns the domain of the service (e.g. Heat, Electricity).
	 * @return the domain
	 */
	public String getDomain();
	
	/**
	 * Returns the graph element layout for the specified GraphElement.
	 *
	 * @param graphElement the graph element
	 * @return the graph element layout
	 */
	public GraphElementLayout getGraphElementLayout(GraphElement graphElement, NetworkModel networkModel, AbstractGraphElementLayoutSettings layoutSettings);
	
	/**
	 * Returns the visual component to configure the layout setting.
	 *
	 * @param project the project
	 * @param domain the domain
	 * @return the graph element layout setting component
	 */
	public AbstractGraphElementLayoutSettingsPanel getGraphElementLayoutSettingPanel(Project project, String domain);
	
	/**
	 * Converts an {@link AbstractGraphElementLayoutSettings} instance to a tree map for persistence.
	 * @param settings the settings
	 * @return the tree map
	 */
	public GraphElementLayoutSettingsPersistenceTreeMap convertInstanceToTreeMap(AbstractGraphElementLayoutSettings settings);
	
	
	/**
	 * Converts a settings tree map to an {@link AbstractGraphElementLayoutSettings} instance.
	 * @param treeMap the tree map
	 * @return the settings
	 */
	public AbstractGraphElementLayoutSettings convertTreeMapToInstance(GraphElementLayoutSettingsPersistenceTreeMap treeMap);
	
}
