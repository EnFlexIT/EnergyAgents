package de.enflexit.energyAgent.core.aggregation;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;

import agentgui.core.project.Project;
import hygrid.env.ColorSettingsCollection;
import hygrid.env.HyGridAbstractEnvironmentModel;
import hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings;
import hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettingsPanel;
import hygrid.plugin.gui.ColorSettingPanel;

/**
 * GUI component for configuring {@link HyGridGraphElementLayoutSettings}
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class HyGridGraphElementLayoutSettingsPanel extends AbstractGraphElementLayoutSettingsPanel {

	private static final long serialVersionUID = 4838491676852308940L;
	
	private HyGridGraphElementLayoutSettings graphElementLayoutSettings;
	
	private JLabel jLabelGraphNodeColorSettings;
	private ColorSettingPanel jPanelColorSettingsForGraphNodes;
	private JLabel jLabelGraphEdgeColorSetting;
	private ColorSettingPanel jPanelColorSettingsForGraphEdges;
	
	/**
	 * Just for the use of the WindowBuilder.
	 */
	@Deprecated
	public HyGridGraphElementLayoutSettingsPanel() {
		this.initialize();
	}
	
	/**
	 * Instantiates a new TriPhaseElectricalNetworkLayoutSettingsPanel.
	 *
	 * @param project the project
	 * @param domain the domain
	 */
	public HyGridGraphElementLayoutSettingsPanel(Project project, String domain) {
		super(project, domain);

		// --- Initialize GUI components ------------------
		this.initialize();
		
		// --- Get stored settings from the model --------- 
		HyGridAbstractEnvironmentModel absEnv = (HyGridAbstractEnvironmentModel) project.getEnvironmentController().getAbstractEnvironmentModel();
		HyGridGraphElementLayoutSettings layoutSettings = (HyGridGraphElementLayoutSettings) absEnv.getGraphElementLayoutSettings().get(domain);
		// --- If not found, create an empty instance ----- 
		if (layoutSettings==null) {
			layoutSettings = new HyGridGraphElementLayoutSettings();
			layoutSettings.setColorSettingsForNodes(new ColorSettingsCollection());
			layoutSettings.setColorSettingsForEdges(new ColorSettingsCollection());
		}
		
		this.setGraphElementLayoutSettings(layoutSettings);
		
	}

	/**
	 * Initialize GUI components.
	 */
	private void initialize() {
		
		GridBagLayout gbl_jPanelColorSettings = new GridBagLayout();
		gbl_jPanelColorSettings.columnWidths = new int[]{0, 0, 0};
		gbl_jPanelColorSettings.rowHeights = new int[]{0, 0, 0};
		gbl_jPanelColorSettings.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_jPanelColorSettings.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gbl_jPanelColorSettings);
		GridBagConstraints gbc_jLabelGraphNodeColorSettings = new GridBagConstraints();
		gbc_jLabelGraphNodeColorSettings.fill = GridBagConstraints.HORIZONTAL;
		gbc_jLabelGraphNodeColorSettings.insets = new Insets(5, 10, 2, 5);
		gbc_jLabelGraphNodeColorSettings.gridx = 0;
		gbc_jLabelGraphNodeColorSettings.gridy = 0;
		this.add(getJLabelGraphNodeColorSettings(), gbc_jLabelGraphNodeColorSettings);
		GridBagConstraints gbc_jLabelGraphEdgeColorSetting = new GridBagConstraints();
		gbc_jLabelGraphEdgeColorSetting.fill = GridBagConstraints.HORIZONTAL;
		gbc_jLabelGraphEdgeColorSetting.insets = new Insets(5, 10, 2, 10);
		gbc_jLabelGraphEdgeColorSetting.gridx = 1;
		gbc_jLabelGraphEdgeColorSetting.gridy = 0;
		this.add(getJLabelGraphEdgeColorSetting(), gbc_jLabelGraphEdgeColorSetting);
		GridBagConstraints gbc_jPanelColorSettings4GraphNodes = new GridBagConstraints();
		gbc_jPanelColorSettings4GraphNodes.fill = GridBagConstraints.HORIZONTAL;
		gbc_jPanelColorSettings4GraphNodes.insets = new Insets(0, 10, 0, 5);
		gbc_jPanelColorSettings4GraphNodes.gridx = 0;
		gbc_jPanelColorSettings4GraphNodes.gridy = 1;
		this.add(getJPanelColorSettingsForGraphNodes(), gbc_jPanelColorSettings4GraphNodes);
		GridBagConstraints gbc_jPanelColorSettings4GraphEdges = new GridBagConstraints();
		gbc_jPanelColorSettings4GraphEdges.insets = new Insets(0, 5, 0, 10);
		gbc_jPanelColorSettings4GraphEdges.fill = GridBagConstraints.HORIZONTAL;
		gbc_jPanelColorSettings4GraphEdges.gridx = 1;
		gbc_jPanelColorSettings4GraphEdges.gridy = 1;
		this.add(getJPanelColorSettingsForGraphEdges(), gbc_jPanelColorSettings4GraphEdges);

	}
	
	private JLabel getJLabelGraphNodeColorSettings() {
		if (jLabelGraphNodeColorSettings == null) {
			jLabelGraphNodeColorSettings = new JLabel("Graph-Node Color Settings");
			jLabelGraphNodeColorSettings.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelGraphNodeColorSettings;
	}
	private ColorSettingPanel getJPanelColorSettingsForGraphNodes() {
		if (jPanelColorSettingsForGraphNodes == null) {
			jPanelColorSettingsForGraphNodes = new ColorSettingPanel();
			jPanelColorSettingsForGraphNodes.setPreferredSize(new Dimension(300, 180));
		}
		return jPanelColorSettingsForGraphNodes;
	}

	private JLabel getJLabelGraphEdgeColorSetting() {
		if (jLabelGraphEdgeColorSetting == null) {
			jLabelGraphEdgeColorSetting = new JLabel("Graph-Edge Color Settings");
			jLabelGraphEdgeColorSetting.setFont(new Font("Dialog", Font.BOLD, 13));
		}
		return jLabelGraphEdgeColorSetting;
	}
	private ColorSettingPanel getJPanelColorSettingsForGraphEdges() {
		if (jPanelColorSettingsForGraphEdges == null) {
			jPanelColorSettingsForGraphEdges = new ColorSettingPanel();
			jPanelColorSettingsForGraphEdges.setPreferredSize(new Dimension(300, 180));
		}
		return jPanelColorSettingsForGraphEdges;
	}
	
	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettingsPanel#setGraphElementLayoutSettings(hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettings)
	 */
	@Override
	public void setGraphElementLayoutSettings(AbstractGraphElementLayoutSettings settings) {
		this.graphElementLayoutSettings = (HyGridGraphElementLayoutSettings) settings;
		this.getJPanelColorSettingsForGraphNodes().setColorSettings(this.graphElementLayoutSettings.getColorSettingsForNodes());
		this.getJPanelColorSettingsForGraphEdges().setColorSettings(this.graphElementLayoutSettings.getColorSettingsForEdges());
	}

	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.graphLayout.AbstractGraphElementLayoutSettingsPanel#getGraphElementLayoutSettings()
	 */
	@Override
	public AbstractGraphElementLayoutSettings getGraphElementLayoutSettings() {
		return graphElementLayoutSettings;
	}

}
