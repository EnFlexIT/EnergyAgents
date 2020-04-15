package de.enflexit.ea.deployment.gui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import agentgui.core.application.Language;
import agentgui.core.config.InstallationPackageFinder;
import agentgui.core.config.GlobalInfo.MtpProtocol;
import agentgui.core.config.InstallationPackageFinder.InstallationPackageDescription;
import agentgui.core.gui.components.JComboBoxMtpProtocol;
import de.enflexit.common.swing.KeyAdapter4Numbers;
import de.enflexit.ea.core.dataModel.deployment.AgentOperatingMode;
import de.enflexit.ea.core.dataModel.deployment.DeploymentSettings;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;

/**
 * This panel can be used to configure the target jade platform settings
 * and {@link CentralExecutiveAgent} contact information.
 * 
 * @author Mohamed Amine JEDIDI <mohamedamine_jedidi@outlook.com>
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class DeploymentSettingsConfigurationPanel extends JPanel implements ActionListener, FocusListener{
	
	private static final long serialVersionUID = 3107290517605565765L;
	
	private DeploymentSettings deploymentSettings;

	private Dimension dimPortTextSize = new Dimension(120, 26);
	
	private JLabel jLabelAgentName;
	private JLabel jLabelUrlOrIp;
	private JLabel jLabelJadePortCeaPlatform;
	private JLabel jLabelMtpprotocol;
	private JLabel jLabelCentralAgent;
	private JLabel jLabelOperatingMode;
	
	private JTextField jTextFieldCentralAgentLocalName;
	private JTextField jTextFieldURLorIP;
	private JTextField jTextFieldJadePortCEA;
	private JComboBoxMtpProtocol jComboBoxMtpProtocol;
	private JComboBox<AgentOperatingMode> jComboBoxOperationMode;
	private JLabel jLabelAddressSeparator;
	private JTextField jTextFieldPlatformName;
	private JLabel jLabelMtpPortCeaPlatform;
	private JTextField jTextFieldMtpPortCEA;
	
	private JLabel jLabelTargetSystemSettings;
	private JLabel jLabelOperatingSystem;
	private JComboBox<InstallationPackageDescription> jComboBoxOperatingSystem;
	private DefaultComboBoxModel<InstallationPackageDescription> operatingSystemListModel;
	private JLabel jLabelJadePortTargetPlatform;
	private JTextField jTextFielJadePortTargetPlatform;
	private JLabel jLabelMtpPortTargetPlatform;
	private JTextField jTextFieldMtpPortTargetPlatform;
	private JLabel jLabelRepositories;
	private JLabel jLabelP2Repository;
	private JTextField jTextFieldP2Repository;
	private JLabel jLabelProjectRepository;
	private JTextField jTextFieldProjectRepository;
	private JCheckBox jCheckBoxP2RepositoryEnabled;
	private JCheckBox jCheckBoxProjectRepositoryEnabled;
	private JLabel jLabelTargetSystemIpAddress;
	private JTextField jTextFieldTargetSystemIpAddress;
	private JCheckBox jCheckBoxTargetSystemAutoIp;
	private JLabel jLabelGroupID;
	private JTextField jTextFieldGroupID;
	private JPanel jPanelTargetSystemSettings;
	private JPanel jPanelUpdateTarget;
	
	/**
	 * Just for window builder, use the other constructors for actual instantiation
	 */
	public DeploymentSettingsConfigurationPanel() {
		this(null);
	}
	/**
	 * Instantiates a new CentralAgentPanel.
	 */
	public DeploymentSettingsConfigurationPanel(DeploymentSettings deploymentSettings) {
		this.initialize();
		this.setDeploymentSettings(deploymentSettings);
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		GridBagConstraints gbc_jPanelTargetSystmSettings = new GridBagConstraints();
		gbc_jPanelTargetSystmSettings.insets = new Insets(10, 10, 0, 5);
		gbc_jPanelTargetSystmSettings.fill = GridBagConstraints.BOTH;
		gbc_jPanelTargetSystmSettings.gridx = 0;
		gbc_jPanelTargetSystmSettings.gridy = 0;
		add(getJPanelTargetSystemSettings(), gbc_jPanelTargetSystmSettings);
		
		GridBagConstraints gbc_jPanelUpdateTarget = new GridBagConstraints();
		gbc_jPanelUpdateTarget.insets = new Insets(10, 10, 0, 5);
		gbc_jPanelUpdateTarget.fill = GridBagConstraints.BOTH;
		gbc_jPanelUpdateTarget.gridx = 0;
		gbc_jPanelUpdateTarget.gridy = 1;
		add(getJPanelUpdateTarget(), gbc_jPanelUpdateTarget);
		
	}
	
	/**
	 * Gets the j panel target system settings.
	 *
	 * @return the j panel target system settings
	 */
	private JPanel getJPanelTargetSystemSettings() {
		if (jPanelTargetSystemSettings == null) {
			jPanelTargetSystemSettings = new JPanel();
			GridBagLayout gbl_jPanelTargetSystmSettings = new GridBagLayout();
			gbl_jPanelTargetSystmSettings.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
			gbl_jPanelTargetSystmSettings.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_jPanelTargetSystmSettings.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelTargetSystmSettings.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPanelTargetSystemSettings.setLayout(gbl_jPanelTargetSystmSettings);
			GridBagConstraints gbc_jLabelCentralAgent = new GridBagConstraints();
			gbc_jLabelCentralAgent.gridwidth = 2;
			gbc_jLabelCentralAgent.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelCentralAgent.anchor = GridBagConstraints.WEST;
			gbc_jLabelCentralAgent.gridx = 0;
			gbc_jLabelCentralAgent.gridy = 0;
			jPanelTargetSystemSettings.add(getJLabelCentralAgent(), gbc_jLabelCentralAgent);
			GridBagConstraints gbc_jLabelAgentName = new GridBagConstraints();
			gbc_jLabelAgentName.anchor = GridBagConstraints.WEST;
			gbc_jLabelAgentName.gridx = 0;
			gbc_jLabelAgentName.gridy = 1;
			jPanelTargetSystemSettings.add(getJLabelAgentName(), gbc_jLabelAgentName);
			GridBagConstraints gbc_jTextFieldAgentName = new GridBagConstraints();
			gbc_jTextFieldAgentName.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldAgentName.gridx = 1;
			gbc_jTextFieldAgentName.gridy = 1;
			jPanelTargetSystemSettings.add(getJTextFieldCentralAgentLocalName(), gbc_jTextFieldAgentName);
			GridBagConstraints gbc_jLabelAddressSeparator = new GridBagConstraints();
			gbc_jLabelAddressSeparator.insets = new Insets(0, 2, 0, 2);
			gbc_jLabelAddressSeparator.gridx = 2;
			gbc_jLabelAddressSeparator.gridy = 1;
			jPanelTargetSystemSettings.add(getJLabelAddressSeparator(), gbc_jLabelAddressSeparator);
			GridBagConstraints gbc_jTextFieldPlatformName = new GridBagConstraints();
			gbc_jTextFieldPlatformName.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldPlatformName.gridwidth = 2;
			gbc_jTextFieldPlatformName.gridx = 3;
			gbc_jTextFieldPlatformName.gridy = 1;
			jPanelTargetSystemSettings.add(getJTextFieldPlatformName(), gbc_jTextFieldPlatformName);
			GridBagConstraints gbc_jLabelUrlOrIp = new GridBagConstraints();
			gbc_jLabelUrlOrIp.anchor = GridBagConstraints.WEST;
			gbc_jLabelUrlOrIp.gridx = 0;
			gbc_jLabelUrlOrIp.gridy = 2;
			jPanelTargetSystemSettings.add(getJLabelUrlOrIp(), gbc_jLabelUrlOrIp);
			GridBagConstraints gbc_jTextFieldURLorIP = new GridBagConstraints();
			gbc_jTextFieldURLorIP.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldURLorIP.gridwidth = 4;
			gbc_jTextFieldURLorIP.gridx = 1;
			gbc_jTextFieldURLorIP.gridy = 2;
			jPanelTargetSystemSettings.add(getJTextFieldURLorIP(), gbc_jTextFieldURLorIP);
			GridBagConstraints gbc_jLabelJadePortCeaPlatform = new GridBagConstraints();
			gbc_jLabelJadePortCeaPlatform.anchor = GridBagConstraints.WEST;
			gbc_jLabelJadePortCeaPlatform.gridx = 0;
			gbc_jLabelJadePortCeaPlatform.gridy = 3;
			jPanelTargetSystemSettings.add(getJLabelJadePortCeaPlatform(), gbc_jLabelJadePortCeaPlatform);
			GridBagConstraints gbc_jTextFieldJadePortCEA = new GridBagConstraints();
			gbc_jTextFieldJadePortCEA.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldJadePortCEA.gridx = 1;
			gbc_jTextFieldJadePortCEA.gridy = 3;
			jPanelTargetSystemSettings.add(getJTextFieldJadePortCEA(), gbc_jTextFieldJadePortCEA);
			GridBagConstraints gbc_jLabelMtpPortCeaPlatform = new GridBagConstraints();
			gbc_jLabelMtpPortCeaPlatform.anchor = GridBagConstraints.WEST;
			gbc_jLabelMtpPortCeaPlatform.insets = new Insets(0, 10, 0, 5);
			gbc_jLabelMtpPortCeaPlatform.gridwidth = 2;
			gbc_jLabelMtpPortCeaPlatform.gridx = 2;
			gbc_jLabelMtpPortCeaPlatform.gridy = 3;
			jPanelTargetSystemSettings.add(getJLabelMtpPortCeaPlatform(), gbc_jLabelMtpPortCeaPlatform);
			GridBagConstraints gbc_jTextFieldMtpPortCEA = new GridBagConstraints();
			gbc_jTextFieldMtpPortCEA.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldMtpPortCEA.gridx = 4;
			gbc_jTextFieldMtpPortCEA.gridy = 3;
			jPanelTargetSystemSettings.add(getJTextFieldMtpPortCEA(), gbc_jTextFieldMtpPortCEA);
			GridBagConstraints gbc_jLabelMtpprotocol = new GridBagConstraints();
			gbc_jLabelMtpprotocol.anchor = GridBagConstraints.WEST;
			gbc_jLabelMtpprotocol.gridx = 0;
			gbc_jLabelMtpprotocol.gridy = 4;
			jPanelTargetSystemSettings.add(getJLabelMtpprotocol(), gbc_jLabelMtpprotocol);
			GridBagConstraints gbc_jComboBoxMtpProtocol = new GridBagConstraints();
			gbc_jComboBoxMtpProtocol.gridwidth = 3;
			gbc_jComboBoxMtpProtocol.fill = GridBagConstraints.HORIZONTAL;
			gbc_jComboBoxMtpProtocol.gridx = 1;
			gbc_jComboBoxMtpProtocol.gridy = 4;
			jPanelTargetSystemSettings.add(getJComboBoxMtpProtocol(), gbc_jComboBoxMtpProtocol);
			GridBagConstraints gbc_jLabelTargetSystemSettings = new GridBagConstraints();
			gbc_jLabelTargetSystemSettings.gridwidth = 2;
			gbc_jLabelTargetSystemSettings.insets = new Insets(10, 0, 5, 0);
			gbc_jLabelTargetSystemSettings.anchor = GridBagConstraints.WEST;
			gbc_jLabelTargetSystemSettings.gridx = 0;
			gbc_jLabelTargetSystemSettings.gridy = 5;
			jPanelTargetSystemSettings.add(getJLabelTargetSystemSettings(), gbc_jLabelTargetSystemSettings);
			GridBagConstraints gbc_jLabelOperatingSystem = new GridBagConstraints();
			gbc_jLabelOperatingSystem.anchor = GridBagConstraints.WEST;
			gbc_jLabelOperatingSystem.gridx = 0;
			gbc_jLabelOperatingSystem.gridy = 6;
			jPanelTargetSystemSettings.add(getJLabelOperatingSystem(), gbc_jLabelOperatingSystem);
			GridBagConstraints gbc_jComboBoxOperatingSystem = new GridBagConstraints();
			gbc_jComboBoxOperatingSystem.gridwidth = 4;
			gbc_jComboBoxOperatingSystem.fill = GridBagConstraints.HORIZONTAL;
			gbc_jComboBoxOperatingSystem.gridx = 1;
			gbc_jComboBoxOperatingSystem.gridy = 6;
			jPanelTargetSystemSettings.add(getJComboBoxOperatingSystem(), gbc_jComboBoxOperatingSystem);
			GridBagConstraints gbc_jLabelVersionTag = new GridBagConstraints();
			gbc_jLabelVersionTag.anchor = GridBagConstraints.WEST;
			gbc_jLabelVersionTag.gridx = 0;
			gbc_jLabelVersionTag.gridy = 7;
			jPanelTargetSystemSettings.add(getJLabelGroupID(), gbc_jLabelVersionTag);
			GridBagConstraints gbc_jTextFieldVersionTag = new GridBagConstraints();
			gbc_jTextFieldVersionTag.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldVersionTag.gridwidth = 4;
			gbc_jTextFieldVersionTag.gridx = 1;
			gbc_jTextFieldVersionTag.gridy = 7;
			jPanelTargetSystemSettings.add(getJTextFieldGroupID(), gbc_jTextFieldVersionTag);
			GridBagConstraints gbc_jLabelTargetSystemIpAddress = new GridBagConstraints();
			gbc_jLabelTargetSystemIpAddress.anchor = GridBagConstraints.WEST;
			gbc_jLabelTargetSystemIpAddress.gridx = 0;
			gbc_jLabelTargetSystemIpAddress.gridy = 8;
			jPanelTargetSystemSettings.add(getJLabelTargetSystemIpAddress(), gbc_jLabelTargetSystemIpAddress);
			GridBagConstraints gbc_jTextFieldTargetSystemIpAddress = new GridBagConstraints();
			gbc_jTextFieldTargetSystemIpAddress.gridwidth = 3;
			gbc_jTextFieldTargetSystemIpAddress.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldTargetSystemIpAddress.gridx = 1;
			gbc_jTextFieldTargetSystemIpAddress.gridy = 8;
			jPanelTargetSystemSettings.add(getJTextFieldTargetSystemIpAddress(), gbc_jTextFieldTargetSystemIpAddress);
			GridBagConstraints gbc_jCheckBoxTargetSystemAutoIp = new GridBagConstraints();
			gbc_jCheckBoxTargetSystemAutoIp.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxTargetSystemAutoIp.gridx = 4;
			gbc_jCheckBoxTargetSystemAutoIp.gridy = 8;
			jPanelTargetSystemSettings.add(getJCheckBoxTargetSystemAutoIp(), gbc_jCheckBoxTargetSystemAutoIp);
			GridBagConstraints gbc_jLabelJadePortTargetPlatform = new GridBagConstraints();
			gbc_jLabelJadePortTargetPlatform.anchor = GridBagConstraints.WEST;
			gbc_jLabelJadePortTargetPlatform.gridx = 0;
			gbc_jLabelJadePortTargetPlatform.gridy = 9;
			jPanelTargetSystemSettings.add(getJLabelJadePortTargetPlatform(), gbc_jLabelJadePortTargetPlatform);
			GridBagConstraints gbc_jTextFielJadePortTargetPlatform = new GridBagConstraints();
			gbc_jTextFielJadePortTargetPlatform.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFielJadePortTargetPlatform.gridx = 1;
			gbc_jTextFielJadePortTargetPlatform.gridy = 9;
			jPanelTargetSystemSettings.add(getJTextFieldJadePortTargetPlatform(), gbc_jTextFielJadePortTargetPlatform);
			GridBagConstraints gbc_jLabelMtpPortTargetPlatform = new GridBagConstraints();
			gbc_jLabelMtpPortTargetPlatform.anchor = GridBagConstraints.WEST;
			gbc_jLabelMtpPortTargetPlatform.insets = new Insets(0, 10, 0, 5);
			gbc_jLabelMtpPortTargetPlatform.gridwidth = 2;
			gbc_jLabelMtpPortTargetPlatform.gridx = 2;
			gbc_jLabelMtpPortTargetPlatform.gridy = 9;
			jPanelTargetSystemSettings.add(getJLabelMtpPortTargetPlatform(), gbc_jLabelMtpPortTargetPlatform);
			GridBagConstraints gbc_jTextFieldMtpPortTargetPlatform = new GridBagConstraints();
			gbc_jTextFieldMtpPortTargetPlatform.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldMtpPortTargetPlatform.gridx = 4;
			gbc_jTextFieldMtpPortTargetPlatform.gridy = 9;
			jPanelTargetSystemSettings.add(getJTextFieldMtpPortTargetPlatform(), gbc_jTextFieldMtpPortTargetPlatform);
			
			GridBagConstraints gbc_jLabelOperatingMode = new GridBagConstraints();
			gbc_jLabelOperatingMode.anchor = GridBagConstraints.WEST;
			gbc_jLabelOperatingMode.gridx = 0;
			gbc_jLabelOperatingMode.gridy = 10;
			jPanelTargetSystemSettings.add(getJLabelOperatingMode(), gbc_jLabelOperatingMode);
			GridBagConstraints gbc_jComboBoxOperationMode = new GridBagConstraints();
			gbc_jComboBoxOperationMode.gridwidth = 4;
			gbc_jComboBoxOperationMode.fill = GridBagConstraints.HORIZONTAL;
			gbc_jComboBoxOperationMode.gridx = 1;
			gbc_jComboBoxOperationMode.gridy = 10;
			jPanelTargetSystemSettings.add(getJComboBoxOperationMode(), gbc_jComboBoxOperationMode);
			
		}
		return jPanelTargetSystemSettings;
	}
	
	/**
	 * Gets the j panel update target.
	 *
	 * @return the j panel update target
	 */
	private JPanel getJPanelUpdateTarget() {
		if (jPanelUpdateTarget == null) {
			jPanelUpdateTarget = new JPanel();
			GridBagLayout gbl_jPanelUpdateTarget = new GridBagLayout();
			gbl_jPanelUpdateTarget.columnWidths = new int[]{0, 0, 0, 0};
			gbl_jPanelUpdateTarget.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
			gbl_jPanelUpdateTarget.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_jPanelUpdateTarget.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			jPanelUpdateTarget.setLayout(gbl_jPanelUpdateTarget);
			GridBagConstraints gbc_jLabelRepositories = new GridBagConstraints();
			gbc_jLabelRepositories.insets = new Insets(0, 0, 5, 0);
			gbc_jLabelRepositories.anchor = GridBagConstraints.WEST;
			gbc_jLabelRepositories.gridx = 0;
			gbc_jLabelRepositories.gridy = 0;
			jPanelUpdateTarget.add(getJLabelRepositories(), gbc_jLabelRepositories);
			GridBagConstraints gbc_jLabelP2Repository = new GridBagConstraints();
			gbc_jLabelP2Repository.anchor = GridBagConstraints.WEST;
			gbc_jLabelP2Repository.gridx = 0;
			gbc_jLabelP2Repository.gridy = 1;
			jPanelUpdateTarget.add(getJLabelP2Repository(), gbc_jLabelP2Repository);
			GridBagConstraints gbc_jTextFieldP2Repository = new GridBagConstraints();
			gbc_jTextFieldP2Repository.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldP2Repository.gridx = 1;
			gbc_jTextFieldP2Repository.gridy = 1;
			jPanelUpdateTarget.add(getJTextFieldP2Repository(), gbc_jTextFieldP2Repository);
			GridBagConstraints gbc_jCheckBoxP2RepositoryEnabled = new GridBagConstraints();
			gbc_jCheckBoxP2RepositoryEnabled.gridx = 2;
			gbc_jCheckBoxP2RepositoryEnabled.gridy = 1;
			jPanelUpdateTarget.add(getJCheckBoxP2RepositoryEnabled(), gbc_jCheckBoxP2RepositoryEnabled);
			GridBagConstraints gbc_jLabelProjectRepository = new GridBagConstraints();
			gbc_jLabelProjectRepository.anchor = GridBagConstraints.WEST;
			gbc_jLabelProjectRepository.gridx = 0;
			gbc_jLabelProjectRepository.gridy = 2;
			jPanelUpdateTarget.add(getJLabelProjectRepository(), gbc_jLabelProjectRepository);
			GridBagConstraints gbc_jTextFieldProjectRepository = new GridBagConstraints();
			gbc_jTextFieldProjectRepository.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldProjectRepository.gridx = 1;
			gbc_jTextFieldProjectRepository.gridy = 2;
			jPanelUpdateTarget.add(getJTextFieldProjectRepository(), gbc_jTextFieldProjectRepository);
			GridBagConstraints gbc_jCheckBoxProjectRepositoryEnabled = new GridBagConstraints();
			gbc_jCheckBoxProjectRepositoryEnabled.gridx = 2;
			gbc_jCheckBoxProjectRepositoryEnabled.gridy = 2;
			jPanelUpdateTarget.add(getJCheckBoxProjectRepositoryEnabled(), gbc_jCheckBoxProjectRepositoryEnabled);
		}
		return jPanelUpdateTarget;
	}
	
	/**
	 * Gets the JLabel.
	 * @return the jLabelCentralExecutiveAgent
	 */
	private JLabel getJLabelCentralAgent() {
		if (jLabelCentralAgent == null) {
			jLabelCentralAgent = new JLabel("Central Agent Settings");
			jLabelCentralAgent.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelCentralAgent;
	}
	/**
	 * Gets the JLabel.
	 * @return the jLabelAgentName
	 */
	private JLabel getJLabelAgentName() {
		if (jLabelAgentName == null) {
			jLabelAgentName = new JLabel(Language.translate("Agent address:", Language.EN));
			jLabelAgentName.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabelAgentName.setToolTipText("LocalName@Plattform");
		}
		return jLabelAgentName;
	}
	/**
	 * Gets the JLabel.
	 * @return the jLabelUrlOrIp
	 */
	private JLabel getJLabelUrlOrIp() {
		if (jLabelUrlOrIp == null) {
			jLabelUrlOrIp = new JLabel(Language.translate("URL or IP:",Language.EN));
			jLabelUrlOrIp.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelUrlOrIp;
	}
	/**
	 * Gets the JLabel.
	 * @return the jLabelPort
	 */
	private JLabel getJLabelJadePortCeaPlatform() {
		if (jLabelJadePortCeaPlatform == null) {
			jLabelJadePortCeaPlatform = new JLabel("JADE Port:");
			jLabelJadePortCeaPlatform.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelJadePortCeaPlatform;
	}
	/**
	 * Gets the JLabel.
	 * @return the jLabelMtpprotocol
	 */
	private JLabel getJLabelMtpprotocol() {
		if (jLabelMtpprotocol == null) {
			jLabelMtpprotocol = new JLabel("MTP Protocol:");
			jLabelMtpprotocol.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelMtpprotocol;
	}
	/**
	 * Gets the jLabelOperatingMode.
	 * @return the jLabelOperatingMode
	 */
	private JLabel getJLabelOperatingMode() {
		if (jLabelOperatingMode == null) {
			jLabelOperatingMode = new JLabel(Language.translate("Operating mode:", Language.EN));
			jLabelOperatingMode.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelOperatingMode;
	}
	/**
	 * Gets a label containing the address separator
	 * @return The label
	 */
	private JLabel getJLabelAddressSeparator() {
		if (jLabelAddressSeparator == null) {
			jLabelAddressSeparator = new JLabel("@");
			jLabelAddressSeparator.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelAddressSeparator;
	}
	/**
	 * Gets the label for the getJTextFieldMtpPort
	 * @return the label
	 */
	private JLabel getJLabelMtpPortCeaPlatform() {
		if (jLabelMtpPortCeaPlatform == null) {
			jLabelMtpPortCeaPlatform = new JLabel("MTP Port:");
			jLabelMtpPortCeaPlatform.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelMtpPortCeaPlatform;
	}
	/**
	 * Gets the JTextField.
	 * @return the jTextFieldAgentName
	 */
	private JTextField getJTextFieldCentralAgentLocalName() {
		if (jTextFieldCentralAgentLocalName == null) {
			jTextFieldCentralAgentLocalName = new JTextField();
			jTextFieldCentralAgentLocalName.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldCentralAgentLocalName.setToolTipText("Agent's local name");
			jTextFieldCentralAgentLocalName.addFocusListener(this);
			
		}
		return jTextFieldCentralAgentLocalName;
	}
	/**
	 * Gets the GUI component for specifying platform name.
	 * @return the GUI component
	 */
	private JTextField getJTextFieldPlatformName() {
		if (jTextFieldPlatformName == null) {
			jTextFieldPlatformName = new JTextField();
			jTextFieldPlatformName.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldPlatformName.setEnabled(false);
			jTextFieldPlatformName.setToolTipText("Plattform name (automatically generated)");
		}
		return jTextFieldPlatformName;
	}
	/**
	 * Gets the JTextField.
	 * @return the jTextFieldURLorIP
	 */
	private JTextField getJTextFieldURLorIP() {
		if (jTextFieldURLorIP == null) {
			jTextFieldURLorIP = new JTextField();
			jTextFieldURLorIP.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldURLorIP.setToolTipText(Language.translate("The URL or IP address of the CEA's JADE platform", Language.EN));
			jTextFieldURLorIP.addFocusListener(this);
		}
		return jTextFieldURLorIP;
	}
	/**
	 * Gets the JTextField.
	 * @return the jTextFieldPort
	 */
	private JTextField getJTextFieldJadePortCEA() {
		if (jTextFieldJadePortCEA == null) {
			jTextFieldJadePortCEA = new JTextField();
			jTextFieldJadePortCEA.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldJadePortCEA.setToolTipText(Language.translate("The port used by the CEA's JADE platform", Language.EN));
			jTextFieldJadePortCEA.setPreferredSize(this.dimPortTextSize);
			jTextFieldJadePortCEA.setMinimumSize(this.dimPortTextSize);
			jTextFieldJadePortCEA.setMaximumSize(this.dimPortTextSize);
			jTextFieldJadePortCEA.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldJadePortCEA.addFocusListener(this);
		}
		return jTextFieldJadePortCEA;
	}
	/**
	 * Gets the GUI component for specifying the MTP port.
	 * @return the GUI component
	 */
	private JTextField getJTextFieldMtpPortCEA() {
		if (jTextFieldMtpPortCEA == null) {
			jTextFieldMtpPortCEA = new JTextField();
			jTextFieldMtpPortCEA.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldMtpPortCEA.setToolTipText(Language.translate("The MTP port used by the CEA's JADE platform", Language.EN));
			jTextFieldMtpPortCEA.setPreferredSize(this.dimPortTextSize);
			jTextFieldMtpPortCEA.setMinimumSize(this.dimPortTextSize);
			jTextFieldMtpPortCEA.setMaximumSize(this.dimPortTextSize);
			jTextFieldMtpPortCEA.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldMtpPortCEA.addFocusListener(this);
		}
		return jTextFieldMtpPortCEA;
	}
	/**
	 * Gets the JComboBoxMtpProtocol.
	 * @return the jComboBoxMtpProtocol
	 */
	//TODO Make this private - requires another way to detect changes of the selected MTP in DeploymentSettingsPanel and AgentDeploymentDialog
	protected JComboBoxMtpProtocol getJComboBoxMtpProtocol() {
		if (jComboBoxMtpProtocol == null) {
			jComboBoxMtpProtocol = new JComboBoxMtpProtocol();
			jComboBoxMtpProtocol.setFont(new Font("Dialog", Font.PLAIN, 12));
			jComboBoxMtpProtocol.setToolTipText(Language.translate("The protocol to be used for comunication with CEA and ProxyAgent", Language.EN));
			jComboBoxMtpProtocol.addActionListener(this);
			
		}
		return jComboBoxMtpProtocol;
	}
	
	/**
	 * Gets the jComboBoxOperationMode.
	 * @return the jComboBoxOperationMode
	 */
	private JComboBox<AgentOperatingMode> getJComboBoxOperationMode() {
		//TODO disable or hide/romove when in agent deplyoment mode
		if (jComboBoxOperationMode == null) {
			jComboBoxOperationMode = new JComboBox<AgentOperatingMode>();
			jComboBoxOperationMode.setFont(new Font("Dialog", Font.PLAIN, 12));
			
			// --- Initialize the combo box content ----------------
			DefaultComboBoxModel<AgentOperatingMode> comboBoxModel = new DefaultComboBoxModel<>();
			comboBoxModel.addElement(AgentOperatingMode.TestBedSimulation);
			comboBoxModel.addElement(AgentOperatingMode.TestBedReal);
			comboBoxModel.addElement(AgentOperatingMode.RealSystemSimulatedIO);
			comboBoxModel.addElement(AgentOperatingMode.RealSystem);
			jComboBoxOperationMode.setModel(comboBoxModel);
			jComboBoxOperationMode.setToolTipText(Language.translate("The testbed agent's operating mode", Language.EN));
			jComboBoxOperationMode.setSelectedItem(DeploymentSettings.DEFAULT_OPERATING_MODE);
			jComboBoxOperationMode.addActionListener(this);
			
		}
		return jComboBoxOperationMode;
	}
	
	/**
	 * Automatically generate the platform name based on URL/IP and port.
	 * @return the platform name
	 */
	private String automaticallyGeneratePlatformName(){
		String platformName = null;
		if (this.deploymentSettings!=null && this.deploymentSettings.getCentralAgentSpecifier()!=null) {
			String urlOrIp = this.deploymentSettings.getCentralAgentSpecifier().getUrlOrIp();
			String jadePort = "" + this.deploymentSettings.getCentralAgentSpecifier().getJadePort();
			if(urlOrIp != null && jadePort != null) {
				platformName = urlOrIp + ":" + jadePort + "/JADE";
			}
		}
		return platformName;
	}
	/**
	 * Gets the j label target system settings.
	 *
	 * @return the j label target system settings
	 */
	private JLabel getJLabelTargetSystemSettings() {
		if (jLabelTargetSystemSettings == null) {
			jLabelTargetSystemSettings = new JLabel("Target System Settings");
			jLabelTargetSystemSettings.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelTargetSystemSettings;
	}
	
	/**
	 * Gets the j label operating system.
	 *
	 * @return the j label operating system
	 */
	private JLabel getJLabelOperatingSystem() {
		if (jLabelOperatingSystem == null) {
			jLabelOperatingSystem = new JLabel("Operating System:");
			jLabelOperatingSystem.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelOperatingSystem;
	}
	
	/**
	 * Gets the j combo box operating system.
	 *
	 * @return the j combo box operating system
	 */
	private JComboBox<InstallationPackageDescription> getJComboBoxOperatingSystem() {
		if (jComboBoxOperatingSystem == null) {
			jComboBoxOperatingSystem = new JComboBox<InstallationPackageDescription>();
			jComboBoxOperatingSystem.setFont(new Font("Dialog", Font.PLAIN, 12));
			jComboBoxOperatingSystem.setModel(getOperatingSystemListModel());
			jComboBoxOperatingSystem.setRenderer(new ListCellRenderer<InstallationPackageDescription>() {
				@Override
				public Component getListCellRendererComponent(JList<? extends InstallationPackageDescription> list, InstallationPackageDescription value, int index, boolean isSelected, boolean cellHasFocus) {
					JLabel visComp = new DefaultListCellRenderer();
					if (value!=null) {
						visComp.setText(value.toString(false));
					}
					return visComp;
				}
			});
			jComboBoxOperatingSystem.addActionListener(this);
		}
		return jComboBoxOperatingSystem;
	}
	/**
	 * Gets the operating system list model.
	 *
	 * @return the operating system list model
	 */
	private DefaultComboBoxModel<InstallationPackageDescription> getOperatingSystemListModel() {
		if(operatingSystemListModel == null) {
			InstallationPackageFinder ipf = new InstallationPackageFinder();
			Vector<InstallationPackageDescription> installationPackages = ipf.getInstallationPackageVector();
			operatingSystemListModel = new DefaultComboBoxModel<>(installationPackages);
		}
		return operatingSystemListModel;
	}
	/**
	 * Gets the j label jade port target platform.
	 *
	 * @return the j label jade port target platform
	 */
	private JLabel getJLabelJadePortTargetPlatform() {
		if (jLabelJadePortTargetPlatform == null) {
			jLabelJadePortTargetPlatform = new JLabel("JADE Port:");
			jLabelJadePortTargetPlatform.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelJadePortTargetPlatform;
	}
	
	/**
	 * Gets the j text field jade port target platform.
	 *
	 * @return the j text field jade port target platform
	 */
	private JTextField getJTextFieldJadePortTargetPlatform() {
		if (jTextFielJadePortTargetPlatform == null) {
			jTextFielJadePortTargetPlatform = new JTextField();
			jTextFielJadePortTargetPlatform.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFielJadePortTargetPlatform.setToolTipText(Language.translate("The port used by the testbed agent's JADE platform", Language.EN));
			jTextFielJadePortTargetPlatform.setPreferredSize(this.dimPortTextSize);
			jTextFielJadePortTargetPlatform.setMinimumSize(this.dimPortTextSize);
			jTextFielJadePortTargetPlatform.setMaximumSize(this.dimPortTextSize);
			jTextFielJadePortTargetPlatform.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFielJadePortTargetPlatform.addFocusListener(this);
		}
		return jTextFielJadePortTargetPlatform;
	}
	
	/**
	 * Gets the j label mtp port target platform.
	 *
	 * @return the j label mtp port target platform
	 */
	private JLabel getJLabelMtpPortTargetPlatform() {
		if (jLabelMtpPortTargetPlatform == null) {
			jLabelMtpPortTargetPlatform = new JLabel("MTP Port:");
			jLabelMtpPortTargetPlatform.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelMtpPortTargetPlatform;
	}
	
	/**
	 * Gets the j text field mtp port target platform.
	 *
	 * @return the j text field mtp port target platform
	 */
	private JTextField getJTextFieldMtpPortTargetPlatform() {
		if (jTextFieldMtpPortTargetPlatform == null) {
			jTextFieldMtpPortTargetPlatform = new JTextField();
			jTextFieldMtpPortTargetPlatform.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldMtpPortTargetPlatform.setToolTipText(Language.translate("The MTP port used by the testbed agent's JADE platform", Language.EN));
			jTextFieldMtpPortTargetPlatform.setPreferredSize(this.dimPortTextSize);
			jTextFieldMtpPortTargetPlatform.setMinimumSize(this.dimPortTextSize);
			jTextFieldMtpPortTargetPlatform.setMaximumSize(this.dimPortTextSize);
			jTextFieldMtpPortTargetPlatform.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldMtpPortTargetPlatform.addFocusListener(this);
		}
		return jTextFieldMtpPortTargetPlatform;
	}
	
	/**
	 * Gets the j label project tag.
	 * @return the j label project tag
	 */
	private JLabel getJLabelGroupID() {
		if (jLabelGroupID == null) {
			jLabelGroupID = new JLabel("Group ID:");
			jLabelGroupID.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelGroupID;
	}

	/**
	 * Gets the j text field project tag.
	 * @return the j text field project tag
	 */
	private JTextField getJTextFieldGroupID() {
		if (jTextFieldGroupID == null) {
			jTextFieldGroupID = new JTextField();
			jTextFieldGroupID.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldGroupID.addFocusListener(this);
		}
		return jTextFieldGroupID;
	}

	/**
	 * Gets the j label target system ip address.
	 *
	 * @return the j label target system ip address
	 */
	private JLabel getJLabelTargetSystemIpAddress() {
		if (jLabelTargetSystemIpAddress == null) {
			jLabelTargetSystemIpAddress = new JLabel("IP Address:");
			jLabelTargetSystemIpAddress.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelTargetSystemIpAddress;
	}
	
	/**
	 * Gets the j text field target system ip address.
	 *
	 * @return the j text field target system ip address
	 */
	private JTextField getJTextFieldTargetSystemIpAddress() {
		if (jTextFieldTargetSystemIpAddress == null) {
			jTextFieldTargetSystemIpAddress = new JTextField();
			jTextFieldTargetSystemIpAddress.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldTargetSystemIpAddress.addFocusListener(this);
		}
		return jTextFieldTargetSystemIpAddress;
	}
	
	/**
	 * Gets the j check box target system auto ip.
	 *
	 * @return the j check box target system auto ip
	 */
	private JCheckBox getJCheckBoxTargetSystemAutoIp() {
		if (jCheckBoxTargetSystemAutoIp == null) {
			jCheckBoxTargetSystemAutoIp = new JCheckBox("automatic");
			jCheckBoxTargetSystemAutoIp.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBoxTargetSystemAutoIp.addActionListener(this);
		}
		return jCheckBoxTargetSystemAutoIp;
	}

	/**
	 * Gets the j label repositories.
	 * @return the j label repositories
	 */
	private JLabel getJLabelRepositories() {
		if (jLabelRepositories == null) {
			jLabelRepositories = new JLabel("Update Repositories");
			jLabelRepositories.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelRepositories;
	}

	/**
	 * Gets the j label P 2 repository.
	 * @return the j label P 2 repository
	 */
	private JLabel getJLabelP2Repository() {
		if (jLabelP2Repository == null) {
			jLabelP2Repository = new JLabel("p2 Repository:");
			jLabelP2Repository.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelP2Repository;
	}

	/**
	 * Gets the j text field P 2 repository.
	 * @return the j text field P 2 repository
	 */
	private JTextField getJTextFieldP2Repository() {
		if (jTextFieldP2Repository == null) {
			jTextFieldP2Repository = new JTextField();
			jTextFieldP2Repository.setPreferredSize(new Dimension(200, 26));
			jTextFieldP2Repository.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldP2Repository.addFocusListener(this);
		}
		return jTextFieldP2Repository;
	}

	/**
	 * Gets the j check box p2 repository enabled.
	 * @return the j check box p2 repository enabled
	 */
	private JCheckBox getJCheckBoxP2RepositoryEnabled() {
		if (jCheckBoxP2RepositoryEnabled == null) {
			jCheckBoxP2RepositoryEnabled = new JCheckBox("enable");
			jCheckBoxP2RepositoryEnabled.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBoxP2RepositoryEnabled.setToolTipText("If disabled, this setting will be ignored and the repository from the original project will be used");
			jCheckBoxP2RepositoryEnabled.addActionListener(this);
		}
		return jCheckBoxP2RepositoryEnabled;
	}

	/**
	 * Gets the j label project repository.
	 * @return the j label project repository
	 */
	private JLabel getJLabelProjectRepository() {
		if (jLabelProjectRepository == null) {
			jLabelProjectRepository = new JLabel("Project Repository:");
			jLabelProjectRepository.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelProjectRepository;
	}

	/**
	 * Gets the j text field project repository.
	 * @return the j text field project repository
	 */
	private JTextField getJTextFieldProjectRepository() {
		if (jTextFieldProjectRepository == null) {
			jTextFieldProjectRepository = new JTextField();
			jTextFieldProjectRepository.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldProjectRepository.addFocusListener(this);
		}
		return jTextFieldProjectRepository;
	}

	/**
	 * Gets the j check box project repository enabled.
	 * @return the j check box project repository enabled
	 */
	private JCheckBox getJCheckBoxProjectRepositoryEnabled() {
		if (jCheckBoxProjectRepositoryEnabled == null) {
			jCheckBoxProjectRepositoryEnabled = new JCheckBox("enable");
			jCheckBoxProjectRepositoryEnabled.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBoxProjectRepositoryEnabled.setToolTipText("If disabled, this setting will be ignored and the repository from the original project will be used");
			jCheckBoxProjectRepositoryEnabled.addActionListener(this);
		}
		return jCheckBoxProjectRepositoryEnabled;
	}

	/**
	 * Gets the selected installation package.
	 *
	 * @return the selected installation package
	 */
	public InstallationPackageDescription getSelectedInstallationPackage() {
		return (InstallationPackageDescription) this.getJComboBoxOperatingSystem().getSelectedItem();
	}

	/**
	 * Sets the selected installation package.
	 *
	 * @param installationPackage the new selected installation package
	 */
	public void setSelectedInstallationPackage(InstallationPackageDescription installationPackage) {
		this.getJComboBoxOperatingSystem().setSelectedItem(installationPackage);
	}

	/**
	 * Sets the selected installation package based on the package description
	 * @param packageDescription the description of the package to be selected
	 */
	public void setSelectedInstallationPackageByDescription(String packageDescription) {
		for(int i=0; i<this.getOperatingSystemListModel().getSize(); i++) {
			InstallationPackageDescription ipd = this.getOperatingSystemListModel().getElementAt(i);
			if(ipd.getPacakgeDescription().equals(packageDescription)) {
				this.getJComboBoxOperatingSystem().setSelectedIndex(i);
				return;
			}
		}
	}

	/**
	 * Sets the selected installation package based on the operating system
	 * @param packageDescription the description of the package to be selected
	 */
	public void setSelectedInstallationPackageByOperatingSystem(String operatingSystem) {
		for(int i=0; i<this.getOperatingSystemListModel().getSize(); i++) {
			InstallationPackageDescription ipd = this.getOperatingSystemListModel().getElementAt(i);
			if(ipd.getOperatingSystem().equals(operatingSystem)) {
				this.getJComboBoxOperatingSystem().setSelectedItem(ipd);
				return;
			}
		}
	}
	
	/**
	 * Gets the selected operating mode.
	 * @return the selected operating mode
	 */
	public AgentOperatingMode getSelectedOperatingMode() {
		return (AgentOperatingMode) this.getJComboBoxOperationMode().getSelectedItem();
	}

	@Override
	public void focusGained(FocusEvent e) {
		// --- Nothing to do here --------------
	}

	@Override
	public void focusLost(FocusEvent e) {
		
		// --- Handle changes of the TextFields --------
	
		if (e.getSource() instanceof JTextField) {
			String newValue = ((JTextField)e.getSource()).getText();
			
			if (e.getSource() == this.getJTextFieldCentralAgentLocalName()) {
				
				// --- CEA local name -------------------
				this.deploymentSettings.getCentralAgentSpecifier().setAgentName(newValue);
				
			} else if (e.getSource() == this.getJTextFieldURLorIP()) {
				
				// --- CEA URL or IP --------------------
				this.deploymentSettings.getCentralAgentSpecifier().setUrlOrIp(newValue);
				
				// --- Generate the platform name based on the new URL or IP ------
				String platformName = this.automaticallyGeneratePlatformName();
				this.getJTextFieldPlatformName().setText(platformName);
				this.deploymentSettings.getCentralAgentSpecifier().setPlatformName(platformName);
				
			} else if (e.getSource()==this.getJTextFieldJadePortCEA() || e.getSource()==this.getJTextFieldJadePortTargetPlatform()) {
				
				// --- JADE port ------------------------
				
				if(newValue == null || newValue.length() == 0) {
					// --- No port specified, set the default port ---------------
					JOptionPane.showMessageDialog(this, "A port number must be specified! Using the default port.", "Port number required!", JOptionPane.WARNING_MESSAGE);
					newValue = "" + DeploymentSettings.DEFAULT_JADE_PORT;
					((JTextField)e.getSource()).setText(newValue);
				}
				int jadePort = Integer.parseInt(newValue);
				 if (e.getSource()==this.getJTextFieldJadePortCEA()) {
					// --- CEA settings -------------------
					 this.deploymentSettings.getCentralAgentSpecifier().setJadePort(jadePort);
					 // --- Update platform name ----------
					 String platformName = this.automaticallyGeneratePlatformName();
					this.getJTextFieldPlatformName().setText(platformName);
					this.deploymentSettings.getCentralAgentSpecifier().setPlatformName(platformName);
				 } else {
					// --- Agent platform settings --------
					 this.deploymentSettings.setJadePort(jadePort);
				 }
				
			} else if (e.getSource()==this.getJTextFieldMtpPortCEA() || e.getSource()==this.getJTextFieldMtpPortTargetPlatform()) {
				
				// --- MTP port ------------------------
				
				if(newValue == null || newValue.length() == 0) {
					// --- No port specified, set the default port ---------------
					JOptionPane.showMessageDialog(this, "A port number must be specified! Using the default port.", "Port number required!", JOptionPane.WARNING_MESSAGE);
					newValue = "" + DeploymentSettings.DEFAULT_MTP_PORT;
					((JTextField)e.getSource()).setText(newValue);
				}
				
				int mtpPort = Integer.parseInt(newValue);
				
				if (e.getSource()==this.getJTextFieldMtpPortCEA()) {
					// --- CEA settings -------------------
					this.deploymentSettings.getCentralAgentSpecifier().setMtpPort(mtpPort);
				} else {
					// --- Agent platform settings --------
					this.deploymentSettings.setMtpPort(mtpPort);
				}
			} else if (e.getSource()==this.getJTextFieldP2Repository()) {
				this.deploymentSettings.setP2Repository(newValue);
			} else if (e.getSource()==this.getJTextFieldProjectRepository()) {
				this.deploymentSettings.setProjectRepository(newValue);
			} else if (e.getSource()==this.getJTextFieldTargetSystemIpAddress()) {
				this.deploymentSettings.setTargetSystemIpAddress(newValue);
			} else if (e.getSource()==this.getJTextFieldGroupID()) {
				
				// --- Project tag ------------------------
				this.deploymentSettings.setProjectTag(newValue);
				
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.getJComboBoxMtpProtocol()) {
			MtpProtocol selectedProtocol = this.getJComboBoxMtpProtocol().getSelectedProtocol();
			this.deploymentSettings.getCentralAgentSpecifier().setMtpType(selectedProtocol.toString());
		} else if (e.getSource() == this.getJComboBoxOperationMode()) {
			AgentOperatingMode selectedOperatingMode = (AgentOperatingMode) this.getJComboBoxOperationMode().getSelectedItem();
			this.deploymentSettings.setDefaultAgentOperatingMode(selectedOperatingMode);
		} else if (e.getSource()==this.getJComboBoxOperatingSystem()) {
			InstallationPackageDescription ipd = (InstallationPackageDescription) this.getJComboBoxOperatingSystem().getSelectedItem();
			this.deploymentSettings.setTargetOperatingSystem(ipd.getOperatingSystem());
		} else if (e.getSource()==this.getJCheckBoxP2RepositoryEnabled()) {
			boolean enabled = this.getJCheckBoxP2RepositoryEnabled().isSelected();
			this.deploymentSettings.setP2RepositoryEnabled(enabled);
			this.getJTextFieldP2Repository().setEnabled(enabled);
		} else if (e.getSource()==this.getJCheckBoxProjectRepositoryEnabled()) {
			boolean enabled = this.getJCheckBoxProjectRepositoryEnabled().isSelected();
			this.deploymentSettings.setProjectRepositoryEnabled(enabled);
			this.getJTextFieldProjectRepository().setEnabled(enabled);
		} else if (e.getSource()==this.getJCheckBoxTargetSystemAutoIp()) {
			boolean autoIp = this.getJCheckBoxTargetSystemAutoIp().isSelected();
			this.deploymentSettings.setTargetSystemAutoIp(autoIp);
			// --- Enable manual IP configuration if automatic is disabled ----
			this.getJTextFieldTargetSystemIpAddress().setEnabled(!autoIp);
		}
	}

	/**
	 * Checks if all required data for deploying a atestbed agent has been specified.
	 * @return true, if successful
	 */
	public boolean checkForRequiredInputs() {
		return this.getJTextFieldCentralAgentLocalName().getText().length() > 0 
				&& this.getJTextFieldJadePortCEA().getText().length() > 0
				&& this.getJTextFieldURLorIP().getText().length() > 0;
	}

	/**
	 * Apply all changes to the deployment settings.
	 */
	public void setFormToDataModel() {
		this.deploymentSettings = this.getDeploymentSettingsFromForm();
	}
	
	/**
	 * Gets the deployment settings from form.
	 * @return the deployment settings from form
	 */
	public DeploymentSettings getDeploymentSettingsFromForm() {
		DeploymentSettings deploymentSettingsFromForm = new DeploymentSettings();
		
		String localName = this.getJTextFieldCentralAgentLocalName().getText();
		deploymentSettingsFromForm.getCentralAgentSpecifier().setAgentName(localName);
		String platformName = this.getJTextFieldPlatformName().getText();
		deploymentSettingsFromForm.getCentralAgentSpecifier().setPlatformName(platformName);
		String urlOrIp = this.getJTextFieldURLorIP().getText();
		deploymentSettingsFromForm.getCentralAgentSpecifier().setUrlOrIp(urlOrIp);
		String jadePortCEA = this.getJTextFieldJadePortCEA().getText();
		if (jadePortCEA!=null && jadePortCEA.length()>0) {
			deploymentSettingsFromForm.getCentralAgentSpecifier().setJadePort(Integer.parseInt(jadePortCEA));
		}
		String mtpPortCEA = this.getJTextFieldMtpPortCEA().getText();
		if (mtpPortCEA!=null && mtpPortCEA.length()>0) {
			deploymentSettingsFromForm.getCentralAgentSpecifier().setMtpPort(Integer.parseInt(mtpPortCEA));
		}
		MtpProtocol selectedProtocol = this.getJComboBoxMtpProtocol().getSelectedProtocol();
		deploymentSettingsFromForm.getCentralAgentSpecifier().setMtpType(selectedProtocol.toString());
		AgentOperatingMode selectedOperatingMode = (AgentOperatingMode) this.getJComboBoxOperationMode().getSelectedItem();
		deploymentSettingsFromForm.setDefaultAgentOperatingMode(selectedOperatingMode);
		String jadePort = this.getJTextFieldJadePortTargetPlatform().getText();
		if (jadePort!=null && jadePort.length()>0) {
			deploymentSettingsFromForm.setJadePort(Integer.parseInt(jadePort));
		}
		String mtpPort = this.getJTextFieldMtpPortTargetPlatform().getText();
		if (mtpPort!=null && mtpPort.length()>0) {
			deploymentSettingsFromForm.setMtpPort(Integer.parseInt(mtpPort));
		}
		String p2Repository = this.getJTextFieldP2Repository().getText();
		deploymentSettingsFromForm.setP2Repository(p2Repository);
		String projectRepository = this.getJTextFieldProjectRepository().getText();
		deploymentSettingsFromForm.setProjectRepository(projectRepository);
		boolean p2RepositoryEnabled = this.getJCheckBoxP2RepositoryEnabled().isSelected();
		deploymentSettingsFromForm.setP2RepositoryEnabled(p2RepositoryEnabled);
		boolean projectRepositoryEnabled = this.getJCheckBoxProjectRepositoryEnabled().isSelected();
		deploymentSettingsFromForm.setProjectRepositoryEnabled(projectRepositoryEnabled);
		String targetSystemIpAddress = this.getJTextFieldTargetSystemIpAddress().getText();
		deploymentSettingsFromForm.setTargetSystemIpAddress(targetSystemIpAddress);
		boolean targetSystemAutoIp = this.getJCheckBoxTargetSystemAutoIp().isSelected();
		deploymentSettingsFromForm.setTargetSystemAutoIp(targetSystemAutoIp);
		String versionTag = this.getJTextFieldGroupID().getText();
		deploymentSettingsFromForm.setProjectTag(versionTag);
		
		return deploymentSettingsFromForm;
	}
	
	/**
	 * Initialize all GUI components with the data from the provided model.
	 * @param deploymentSettings the new data model to form
	 */
	public void setDeploymentSettings(DeploymentSettings deploymentSettings) {
		
		this.deploymentSettings = deploymentSettings;
		if (this.deploymentSettings!=null) {
			
			// --- CEA local name ---------------------------------------------
			String ceaLocalName = this.deploymentSettings.getCentralAgentSpecifier().getAgentName();
			if (ceaLocalName==null) {
				ceaLocalName = DeploymentSettings.DEFAULT_CEA_LOCAL_NAME;
			}
			this.getJTextFieldCentralAgentLocalName().setText(ceaLocalName);
			
			// --- CEA URL or IP ----------------------------------------------
			String urlOrIP = this.deploymentSettings.getCentralAgentSpecifier().getUrlOrIp();
			this.getJTextFieldURLorIP().setText(urlOrIP);
			
			// --- CEA jade port ----------------------------------------------
			int ceaJadePort = deploymentSettings.getCentralAgentSpecifier().getJadePort();
			if(ceaJadePort == 0) {
				ceaJadePort = DeploymentSettings.DEFAULT_JADE_PORT;
			}
			jTextFieldJadePortCEA.setText("" + ceaJadePort);
			
			// --- CEA MTP Port -----------------------------------------------
			int ceaMtpPort = this.deploymentSettings.getCentralAgentSpecifier().getMtpPort();
			if(ceaMtpPort == 0) {
				ceaMtpPort = DeploymentSettings.DEFAULT_MTP_PORT;
			}
			jTextFieldMtpPortCEA.setText("" + ceaMtpPort);
			
			// --- CEA MTP selection ------------------------------------------
			MtpProtocol mtpProtocol;
			String mtpString = deploymentSettings.getCentralAgentSpecifier().getMtpType();
			if (mtpString == null || MtpProtocol.valueOf(mtpString) == null) {
				mtpProtocol = DeploymentSettings.DEFAULT_MTP;
			} else {
				mtpProtocol = MtpProtocol.valueOf(mtpString);
			}
			this.getJComboBoxMtpProtocol().setSelectedProtocol(mtpProtocol);
			
			// --- Platform name ----------------------------------------------
			String platformName = this.deploymentSettings.getCentralAgentSpecifier().getPlatformName();
			if (platformName==null) {
				platformName = this.automaticallyGeneratePlatformName();
			}
			this.getJTextFieldPlatformName().setText(platformName);
			
			// --- Agent Operating Mode ---------------------------------------
			AgentOperatingMode operatingMode = this.deploymentSettings.getDefaultAgentOperatingMode();
			if(operatingMode == null) {
				operatingMode = DeploymentSettings.DEFAULT_OPERATING_MODE;
			}
			this.getJComboBoxOperationMode().setSelectedItem(operatingMode);
			
			// --- Target platform jade port ----------------------------------
			int jadePort = this.deploymentSettings.getJadePort();
			if(jadePort == 0) {
				jadePort = DeploymentSettings.DEFAULT_JADE_PORT;
			}
			jTextFielJadePortTargetPlatform.setText("" + jadePort);
			
			// --- Target platform MTP port
			int mtpPort = this.deploymentSettings.getMtpPort();
			if(mtpPort == 0) {
				mtpPort = DeploymentSettings.DEFAULT_MTP_PORT;
			}
			jTextFieldMtpPortTargetPlatform.setText("" + mtpPort);
			
			// --- Version tag ------------------------------------------------
			jTextFieldGroupID.setText(this.deploymentSettings.getProjectTag());
			
			// --- Target system IP address -----------------------------------
			jTextFieldTargetSystemIpAddress.setText(this.deploymentSettings.getTargetSystemIpAddress());
			// --- Enable the text field if automatic IP detection is disabled ----------
			jTextFieldTargetSystemIpAddress.setEnabled(!deploymentSettings.isTargetSystemAutoIp());
			jCheckBoxTargetSystemAutoIp.setSelected(this.deploymentSettings.isTargetSystemAutoIp());
			
			// --- Target system repositories ---------------------------------
			jTextFieldP2Repository.setText(this.deploymentSettings.getP2Repository());
			jTextFieldP2Repository.setEnabled(this.deploymentSettings.isP2RepositoryEnabled());
			jCheckBoxP2RepositoryEnabled.setSelected(this.deploymentSettings.isP2RepositoryEnabled());
			jTextFieldProjectRepository.setText(this.deploymentSettings.getProjectRepository());
			jTextFieldProjectRepository.setEnabled(this.deploymentSettings.isProjectRepositoryEnabled());
			jCheckBoxProjectRepositoryEnabled.setSelected(this.deploymentSettings.isProjectRepositoryEnabled());

			String targetOS = this.deploymentSettings.getTargetOperatingSystem();
			if(targetOS != null) {
				this.setSelectedInstallationPackageByOperatingSystem(targetOS);
			}
			
		} else {
			// --- Null was passed - clear all inputs -------------------------
			
			// --- CEA settings -----------------------------------------------
			this.getJTextFieldCentralAgentLocalName().setText("");
			this.getJTextFieldPlatformName().setText("");
			this.getJTextFieldURLorIP().setText("");
			this.getJTextFieldJadePortCEA().setText("");
			this.getJTextFieldMtpPortCEA().setText("");
			
			// --- Target system settings -------------------------------------
			this.getJTextFieldGroupID().setText("");
			this.getJTextFieldTargetSystemIpAddress().setText("");
			this.getJCheckBoxTargetSystemAutoIp().setSelected(false);
			this.getJTextFieldJadePortTargetPlatform().setText("");
			this.getJTextFieldMtpPortTargetPlatform().setText("");
			
			// --- Update repository settings ---------------------------------
			this.getJTextFieldP2Repository().setText("");
			this.getJCheckBoxP2RepositoryEnabled().setSelected(false);
			this.getJTextFieldProjectRepository().setText("");
			this.getJCheckBoxProjectRepositoryEnabled().setSelected(false);
			
		}
	}
	
	/**
	 * Gets the deployment settings.
	 * @return the deployment settings
	 */
	public DeploymentSettings getDeploymentSettings() {
		return deploymentSettings;
	}
	
	
	/**
	 * Adds a focus listener to the text fields.
	 * @param documentListener the focus listener
	 */
	public void addDocumentListenerToTextFields(DocumentListener documentListener) {
		this.getJTextFieldCentralAgentLocalName().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldURLorIP().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldJadePortCEA().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldMtpPortCEA().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldGroupID().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldTargetSystemIpAddress().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldJadePortTargetPlatform().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldMtpPortTargetPlatform().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldP2Repository().getDocument().addDocumentListener(documentListener);
		this.getJTextFieldProjectRepository().getDocument().addDocumentListener(documentListener);
	}
	
	/**
	 * Adds an action listener to the CheckBoxes and ComboBoxes.
	 * @param actionListener the action listener
	 */
	public void addActionListenerToSelectionComponents(ActionListener actionListener) {
		this.getJCheckBoxTargetSystemAutoIp().addActionListener(actionListener);
		this.getJCheckBoxP2RepositoryEnabled().addActionListener(actionListener);
		this.getJCheckBoxProjectRepositoryEnabled().addActionListener(actionListener);
		this.getJComboBoxMtpProtocol().addActionListener(actionListener);
		this.getJComboBoxOperatingSystem().addActionListener(actionListener);
		this.getJComboBoxOperationMode().addActionListener(actionListener);
	}
}
