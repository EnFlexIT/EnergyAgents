package de.enflexit.ea.core.configuration.ui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;

/**
 * The Class SetupConfigurationPanel.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationPanel extends JPanel {
	
	private static final long serialVersionUID = -208261216805973521L;

	private SetupConfigurationModel setupConfigModel;
	private JTabbedPane jTabbedPane;
	private SetupConfigurationTablePanel setupConfigurationTablePanel;
	
	/**
	 * Instantiates a new setup configuration panel.
	 */
	public SetupConfigurationPanel() {
		this.initialize();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout(0, 0));
		this.add(this.getJTabbedPane());
		this.getSetupConfigurationModel();
	}
	/**
	 * Disposes the current panel.
	 */
	public void dispose() {
		this.getSetupConfigurationModel().dispose();
	}

	/**
	 * Returns the configuration model.
	 * @return the configuration model
	 */
	public SetupConfigurationModel getSetupConfigurationModel() {
		if (setupConfigModel==null) {
			setupConfigModel = new SetupConfigurationModel();
		}
		return setupConfigModel;
	}
	
	/**
	 * Returns the local JTabbedPane.
	 * @return the JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			jTabbedPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTabbedPane.setUI(new AwbBasicTabbedPaneUI());
			jTabbedPane.addTab(" Configuration Table ", null, this.getSetupConfigurationTablePanel(), "Setup Configuration Table");
		}
		return jTabbedPane;
	}
	
	/**
	 * Gets the setup configuration table panel.
	 * @return the setup configuration table panel
	 */
	private SetupConfigurationTablePanel getSetupConfigurationTablePanel() {
		if (setupConfigurationTablePanel == null) {
			setupConfigurationTablePanel = new SetupConfigurationTablePanel(this.getSetupConfigurationModel());
		}
		return setupConfigurationTablePanel;
	}
}
