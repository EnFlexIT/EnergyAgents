package de.enflexit.ea.core.dataModel.graphLayout;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import agentgui.core.project.Project;
import hygrid.env.GraphElementLayoutSettingsPersistenceTreeMap;
import hygrid.env.HyGridAbstractEnvironmentModel;

/**
 * The Class AbstractGraphElementLayoutSettingsPanel.
 */
public abstract class AbstractGraphElementLayoutSettingsPanel extends JPanel implements Observer {

	private static final long serialVersionUID = -4948883668143504738L;

	private Project project;
	private String domain; 
	private HyGridAbstractEnvironmentModel hyGridAbstractEnvironmentModel;
	
	
	/**
	 * Just for the use of the WindowBuilder.
	 */
	@Deprecated
	public AbstractGraphElementLayoutSettingsPanel() { }
	
	/**
	 * Instantiates a new abstract graph element layout settings panel.
	 *
	 * @param project the current project
	 * @param domain the domain
	 */
	public AbstractGraphElementLayoutSettingsPanel(Project project, String domain) {
		this.project = project;
		this.domain = domain;
		if (this.project==null) {
			throw new NullPointerException("The Project for the settings panel is not allowed to be null!");
		} else {
			this.project.addObserver(this);
		}
		if (this.domain==null || this.domain.isEmpty()==true) {
			throw new NullPointerException("The domain for the settings panel is not allowed to be null!");
		}
	}
	
	/**
	 * Returns the current project.
	 * @return the project
	 */
	protected Project getProject() {
		return project;
	}
	/**
	 * Returns the current domain.
	 * @return the domain
	 */
	protected String getDomain() {
		return domain;
	}
	/**
	 * Returns the hyGridAbstractEnvironmentModel.
	 * @return the hyGridAbstractEnvironmentModel
	 */
	protected HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		if (hyGridAbstractEnvironmentModel==null) {
			hyGridAbstractEnvironmentModel = (HyGridAbstractEnvironmentModel)this.project.getUserRuntimeObject();
		}
		return hyGridAbstractEnvironmentModel;
	}
	
	/**
	 * Sets the graph element layout settings to visualization.
	 */
	public final void setGraphElementLayoutSettingsToVisualization() {
		
		AbstractGraphElementLayoutSettings layoutSettings = this.getHyGridAbstractEnvironmentModel().getGraphElementLayoutSettings().get(this.domain);
		if (layoutSettings!=null) {
			this.setGraphElementLayoutSettings(layoutSettings);
		}
	}
	
	/**
	 * Sets the graph element layout settings.
	 * @param settings the new graph element layout settings
	 */
	public abstract void setGraphElementLayoutSettings(AbstractGraphElementLayoutSettings settings);
	
	/**
	 * Has to return the current AbstractGraphElementLayoutSettings from the panel.
	 * @return the graph element layout settings
	 */
	public abstract AbstractGraphElementLayoutSettings getGraphElementLayoutSettings();
	
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		
		if (updateObject==Project.PREPARE_FOR_SAVING) {
			// --- Save AbstractGraphElementLayoutSettings to HyGrid model --
			
			GraphElementLayoutSettingsPersistenceTreeMap layoutSettings = new GraphElementLayoutSettingsPersistenceTreeMap();
			layoutSettings.setDomain(this.getDomain());
			layoutSettings.setSettingsTreeMap(this.getGraphElementLayoutSettings().getSettingsAsTreeMap());
			
			this.getHyGridAbstractEnvironmentModel().addGraphElementSettingsTreeMap(layoutSettings);
		}
	}
	
}
