package de.enflexit.ea.deployment.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import agentgui.core.application.Language;
import agentgui.core.project.Project;
import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.db.hibernate.gui.DatabaseSettingsPanel;
import de.enflexit.ea.core.dataModel.absEnvModel.HyGridAbstractEnvironmentModel;
import de.enflexit.ea.core.dataModel.deployment.DeploymentSettings;

/**
 * This panel contains the GUI components for configuring the default deployment settings. 
 * These will be stored in the project and used as default values when deploying an agent.
 * 
 * @author Mohamed Amine JEDIDI <mohamedamine_jedidi@outlook.com>
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class DeploymentSettingsProjectWindowTab extends JPanel {

	private static final long serialVersionUID = -1983567619404123877L;
	
	private static final int TAB_INDEX_DATABASE_SETTINGS = 0;

	protected Project currProject;

	private DeploymentSettingsConfigurationPanel deploymentSettingsConfigurationPanel;
	private JPanel deploymentSettingsConfigurationPanelTest;
	
	private JTabbedPane tabbedPane;
	private JScrollPane jScrollPaneDatabaseSettingsPanel;
	private DatabaseSettingsPanel databaseSettingsPanel;

	protected boolean actOnActionsOrDocumentChanges = true;
	
	private DocumentListener databaseSettingsDocumentListener;
	
	private DeploymentSettings deploymentSetting;

	/**
	 * This is the default constructor
	 */
	public DeploymentSettingsProjectWindowTab(Project project) {
		super();
		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.currProject=project;
		
		HyGridAbstractEnvironmentModel abstractDM = (HyGridAbstractEnvironmentModel) this.currProject.getUserRuntimeObject();
		this.deploymentSetting = abstractDM.getDeploymentSettingsModel();
		
		// --- Clear some fields that should not be set in the default settings ---
		this.deploymentSetting.getKeyStore().setKeyStoreName(null);
		this.deploymentSetting.getKeyStore().setCertificateName(null);
		this.deploymentSetting.getKeyStore().setPassword(null);
		this.deploymentSetting.getTrustStore().setTrustStoreName(null);
		this.deploymentSetting.getTrustStore().setPassword(null);
		
		this.actOnActionsOrDocumentChanges = false;
		this.initialize();
		this.actOnActionsOrDocumentChanges = true;
	}
	/**
	 * This method initializes this
	 * @return void
	 */
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{133, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0};
		this.setLayout(gridBagLayout);
		GridBagConstraints gbc_DeploymentSettingsConfigurationPanel = new GridBagConstraints();
		gbc_DeploymentSettingsConfigurationPanel.insets = new Insets(0, 0, 20, 0);
		gbc_DeploymentSettingsConfigurationPanel.fill = GridBagConstraints.BOTH;
		gbc_DeploymentSettingsConfigurationPanel.gridx = 0;
		gbc_DeploymentSettingsConfigurationPanel.gridy = 0;
		this.add(getDeploymentSettingsConfigurationPanel(), gbc_DeploymentSettingsConfigurationPanel);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 10, 10, 10);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		this.add(getTabbedPane(), gbc_tabbedPane);
	}
	/**
	 * Gets the CentralAgentPanel.
	 * @return the centralAgentPanel
	 */
	private DeploymentSettingsConfigurationPanel getDeploymentSettingsConfigurationPanel() {
		if (deploymentSettingsConfigurationPanel == null) {
			deploymentSettingsConfigurationPanel = new DeploymentSettingsConfigurationPanel(this.deploymentSetting);
		}
		return deploymentSettingsConfigurationPanel;
	}
	
	/**
	 * Gets a dummy panel to replace the deploymentSettingsConfigurationPanel, which causes problems when opening this class with WindowBuilder. 
	 * @return the deployment settings configuration panel test
	 */
	@SuppressWarnings("unused")
	private JPanel getDeploymentSettingsConfigurationPanelTest() {
		if (deploymentSettingsConfigurationPanelTest==null) {
			deploymentSettingsConfigurationPanelTest = new JPanel();
			deploymentSettingsConfigurationPanelTest.setPreferredSize(new Dimension(600, 300));
			deploymentSettingsConfigurationPanelTest.setMinimumSize(new Dimension(600, 300));
			deploymentSettingsConfigurationPanelTest.setMaximumSize(new Dimension(600, 300));
		}
		return deploymentSettingsConfigurationPanelTest;
	}
	
	/**
	 * Gets the tabbed pane.
	 * @return the tabbed pane
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane==null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.setUI(new AwbBasicTabbedPaneUI());
			tabbedPane.setFont(new Font("Dialog", Font.BOLD, 13));
			tabbedPane.addTab("Database Settings", this.getjScrollPaneDatabaseSettingsPanel());
			tabbedPane.setToolTipTextAt(TAB_INDEX_DATABASE_SETTINGS, Language.translate("Configure the database settings", Language.EN));
			tabbedPane.setPreferredSize(new Dimension(600, 250));
		}
		return tabbedPane;
	}
	
	private JScrollPane getjScrollPaneDatabaseSettingsPanel() {
		if (jScrollPaneDatabaseSettingsPanel==null)	{
			jScrollPaneDatabaseSettingsPanel = new JScrollPane();
			jScrollPaneDatabaseSettingsPanel.setBorder(BorderFactory.createEtchedBorder());
			jScrollPaneDatabaseSettingsPanel.setViewportView(this.getDatabaseSettingsPanel());
		}
		return jScrollPaneDatabaseSettingsPanel;
	}
	/**
	 * Gets the database settings panel.
	 * @return the database settings panel
	 */
	private DatabaseSettingsPanel getDatabaseSettingsPanel() {
		if (databaseSettingsPanel==null) {
			databaseSettingsPanel = new DatabaseSettingsPanel(this.deploymentSetting.getDatabaseSettings());
			databaseSettingsPanel.addDocumentListenerToTextFields(this.getDatabaseSettingsDocumentListener());
		}
		return databaseSettingsPanel;
	}
	
	/**
	 * Gets the database settings document listener.
	 * @return the database settings document listener
	 */
	private DocumentListener getDatabaseSettingsDocumentListener() {
		if (databaseSettingsDocumentListener==null) {
			databaseSettingsDocumentListener = new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					deploymentSetting.setDatabaseSettings(getDatabaseSettingsPanel().getDatabaseSettings());
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					deploymentSetting.setDatabaseSettings(getDatabaseSettingsPanel().getDatabaseSettings());
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					deploymentSetting.setDatabaseSettings(getDatabaseSettingsPanel().getDatabaseSettings());
				}
			};
		}
		return databaseSettingsDocumentListener;
	}
}
	
