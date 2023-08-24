package de.enflexit.ea.core.dataModel.graphLayout;

import java.util.List;

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
	 * Has to return the list of domains this service is responsible for (e.g. Heat, Electricity).
	 *
	 * @param currProject the current project
	 * @return the domain list
	 */
	public List<String> getDomainList(Project currProject);
	
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
	 *
	 * @param settings the settings
	 * @param domain the domain
	 * @return the tree map
	 */
	public GraphElementLayoutSettingsPersistenceTreeMap convertInstanceToTreeMap(AbstractGraphElementLayoutSettings settings, String domain);
	
	
	/**
	 * Converts a settings tree map to an {@link AbstractGraphElementLayoutSettings} instance.
	 * @param treeMap the tree map
	 * @return the settings
	 */
	public AbstractGraphElementLayoutSettings convertTreeMapToInstance(GraphElementLayoutSettingsPersistenceTreeMap treeMap);
	
}
