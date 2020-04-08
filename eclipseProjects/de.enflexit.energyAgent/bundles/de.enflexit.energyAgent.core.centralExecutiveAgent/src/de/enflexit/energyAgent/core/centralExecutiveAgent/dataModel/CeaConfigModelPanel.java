package de.enflexit.energyAgent.core.centralExecutiveAgent.dataModel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import agentgui.core.config.GlobalInfo.MtpProtocol;
import agentgui.core.gui.components.JComboBoxMtpProtocol;
import de.enflexit.common.swing.KeyAdapter4Numbers;
import hygrid.globalDataModel.cea.CeaConfigModel;

/**
 * The Class CeaConfigModelPanel.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class CeaConfigModelPanel extends JPanel implements DocumentListener {

	private static final long serialVersionUID = 8149031313385514098L;
	
	private CeaConfigModel ceaConfigModel;
	
	private JCheckBox jCheckBoxStartSecondMTP;
	private JLabel jLabelProtocol;
	private JLabel jLabelURLorIP;
	private JLabel jLabelPort;
	private JComboBoxMtpProtocol jComboBoxMtpProtocol;
	private JTextField jTextFieldURLorIP;
	private JTextField jTextFieldPort;
	private JLabel jLabelMTPAddress;

	private JSeparator jSeparatorHorizontal;

	private JLabel jLabelMirrorP2Header;
	private JLabel jLabelMirrorP2Source;
	private JLabel jLabelMirrorP2Destination;
	private JLabel jLabelMirrorP2ProviderURL;
	private JTextField jTextFieldP2MirrorDestination;
	private JTextField jTextFieldP2MirrorSource;
	private JTextField jTextFieldP2MirrorProviderURL;
	
	private JLabel jLabelMirrorProjectHeader;
	private JLabel jLabelMirrorProjectDestination;
	private JLabel jLabelMirrorProjectProviderURL;
	private JLabel jLabelMirrorProjectSource;
	private JTextField jTextFieldProjectMirrorSource;
	private JTextField jTextFieldProjectMirrorDestination;
	private JTextField jTextFieldProjectMirrorProviderURL;

	private JLabel jLabelMirrorIntervalHeader;
	private JLabel jLabelMirrorInterval;
	private JTextField jTextFieldMirrorInterval;

	/**
	 * Instantiates a new cea data model panel.
	 */
	public CeaConfigModelPanel() {
		this.initialize();
	}
	private void initialize() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_jCheckBoxStartSecondMTP = new GridBagConstraints();
		gbc_jCheckBoxStartSecondMTP.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxStartSecondMTP.gridwidth = 3;
		gbc_jCheckBoxStartSecondMTP.insets = new Insets(5, 5, 5, 5);
		gbc_jCheckBoxStartSecondMTP.gridx = 0;
		gbc_jCheckBoxStartSecondMTP.gridy = 0;
		add(getJCheckBoxStartSecondMTP(), gbc_jCheckBoxStartSecondMTP);
		GridBagConstraints gbc_jLabelProtocol = new GridBagConstraints();
		gbc_jLabelProtocol.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelProtocol.anchor = GridBagConstraints.WEST;
		gbc_jLabelProtocol.gridx = 0;
		gbc_jLabelProtocol.gridy = 1;
		add(getJLabelProtocol(), gbc_jLabelProtocol);
		GridBagConstraints gbc_jLabelURLorIP = new GridBagConstraints();
		gbc_jLabelURLorIP.anchor = GridBagConstraints.WEST;
		gbc_jLabelURLorIP.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelURLorIP.gridx = 1;
		gbc_jLabelURLorIP.gridy = 1;
		add(getJLabelURLorIP(), gbc_jLabelURLorIP);
		GridBagConstraints gbc_jLabelPort = new GridBagConstraints();
		gbc_jLabelPort.insets = new Insets(0, 5, 5, 5);
		gbc_jLabelPort.anchor = GridBagConstraints.WEST;
		gbc_jLabelPort.gridx = 2;
		gbc_jLabelPort.gridy = 1;
		add(getJLabelPort(), gbc_jLabelPort);
		GridBagConstraints gbc_jComboBoxMtpProtocol = new GridBagConstraints();
		gbc_jComboBoxMtpProtocol.fill = GridBagConstraints.HORIZONTAL;
		gbc_jComboBoxMtpProtocol.insets = new Insets(0, 5, 5, 0);
		gbc_jComboBoxMtpProtocol.gridx = 0;
		gbc_jComboBoxMtpProtocol.gridy = 2;
		add(getJComboBoxMtpProtocol(), gbc_jComboBoxMtpProtocol);
		GridBagConstraints gbc_jTextFieldURLorIP = new GridBagConstraints();
		gbc_jTextFieldURLorIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldURLorIP.insets = new Insets(0, 5, 5, 0);
		gbc_jTextFieldURLorIP.gridx = 1;
		gbc_jTextFieldURLorIP.gridy = 2;
		add(getJTextFieldURLorIP(), gbc_jTextFieldURLorIP);
		GridBagConstraints gbc_jTextFieldPort = new GridBagConstraints();
		gbc_jTextFieldPort.anchor = GridBagConstraints.WEST;
		gbc_jTextFieldPort.insets = new Insets(0, 5, 5, 5);
		gbc_jTextFieldPort.gridx = 2;
		gbc_jTextFieldPort.gridy = 2;
		add(getJTextFieldPort(), gbc_jTextFieldPort);
		GridBagConstraints gbc_jLabelMTPAddress = new GridBagConstraints();
		gbc_jLabelMTPAddress.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelMTPAddress.gridwidth = 2;
		gbc_jLabelMTPAddress.anchor = GridBagConstraints.WEST;
		gbc_jLabelMTPAddress.gridx = 1;
		gbc_jLabelMTPAddress.gridy = 3;
		add(getJLabelMTPAddress(), gbc_jLabelMTPAddress);
		GridBagConstraints gbc_jSeparatorHorizontal = new GridBagConstraints();
		gbc_jSeparatorHorizontal.fill = GridBagConstraints.HORIZONTAL;
		gbc_jSeparatorHorizontal.gridwidth = 3;
		gbc_jSeparatorHorizontal.insets = new Insets(0, 5, 0, 5);
		gbc_jSeparatorHorizontal.gridx = 0;
		gbc_jSeparatorHorizontal.gridy = 4;
		add(getJSeparatorHorizontal(), gbc_jSeparatorHorizontal);
		GridBagConstraints gbc_jLabelMirrorP2Header = new GridBagConstraints();
		gbc_jLabelMirrorP2Header.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorP2Header.gridwidth = 3;
		gbc_jLabelMirrorP2Header.insets = new Insets(5, 5, 5, 0);
		gbc_jLabelMirrorP2Header.gridx = 0;
		gbc_jLabelMirrorP2Header.gridy = 5;
		add(getJLabelMirrorP2Header(), gbc_jLabelMirrorP2Header);
		GridBagConstraints gbc_jLabelMirrorP2Source = new GridBagConstraints();
		gbc_jLabelMirrorP2Source.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorP2Source.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelMirrorP2Source.gridx = 0;
		gbc_jLabelMirrorP2Source.gridy = 6;
		add(getJLabelMirrorP2Source(), gbc_jLabelMirrorP2Source);
		GridBagConstraints gbc_jTextFieldp2MirrorSource = new GridBagConstraints();
		gbc_jTextFieldp2MirrorSource.gridwidth = 2;
		gbc_jTextFieldp2MirrorSource.insets = new Insets(0, 5, 5, 5);
		gbc_jTextFieldp2MirrorSource.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldp2MirrorSource.gridx = 1;
		gbc_jTextFieldp2MirrorSource.gridy = 6;
		add(getJTextFieldP2MirrorSource(), gbc_jTextFieldp2MirrorSource);
		GridBagConstraints gbc_jLabelMirrorP2Destination = new GridBagConstraints();
		gbc_jLabelMirrorP2Destination.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelMirrorP2Destination.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorP2Destination.gridx = 0;
		gbc_jLabelMirrorP2Destination.gridy = 7;
		add(getJLabelMirrorP2Destination(), gbc_jLabelMirrorP2Destination);
		GridBagConstraints gbc_jTextFieldp2MirrorDestination = new GridBagConstraints();
		gbc_jTextFieldp2MirrorDestination.gridwidth = 2;
		gbc_jTextFieldp2MirrorDestination.insets = new Insets(0, 5, 5, 5);
		gbc_jTextFieldp2MirrorDestination.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldp2MirrorDestination.gridx = 1;
		gbc_jTextFieldp2MirrorDestination.gridy = 7;
		add(getJTextFieldP2MirrorDestination(), gbc_jTextFieldp2MirrorDestination);
		GridBagConstraints gbc_jLabelMirrorP2ProviderURL = new GridBagConstraints();
		gbc_jLabelMirrorP2ProviderURL.insets = new Insets(0, 5, 0, 0);
		gbc_jLabelMirrorP2ProviderURL.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorP2ProviderURL.gridx = 0;
		gbc_jLabelMirrorP2ProviderURL.gridy = 8;
		add(getJLabelMirrorP2ProviderURL(), gbc_jLabelMirrorP2ProviderURL);
		GridBagConstraints gbc_jTextFieldp2MirrorProviderURL = new GridBagConstraints();
		gbc_jTextFieldp2MirrorProviderURL.insets = new Insets(0, 5, 0, 5);
		gbc_jTextFieldp2MirrorProviderURL.gridwidth = 2;
		gbc_jTextFieldp2MirrorProviderURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldp2MirrorProviderURL.gridx = 1;
		gbc_jTextFieldp2MirrorProviderURL.gridy = 8;
		add(getJTextFieldp2MirrorProviderURL(), gbc_jTextFieldp2MirrorProviderURL);
		GridBagConstraints gbc_jLabelMirrorProjectHeader = new GridBagConstraints();
		gbc_jLabelMirrorProjectHeader.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorProjectHeader.gridwidth = 3;
		gbc_jLabelMirrorProjectHeader.insets = new Insets(5, 5, 5, 0);
		gbc_jLabelMirrorProjectHeader.gridx = 0;
		gbc_jLabelMirrorProjectHeader.gridy = 9;
		add(getJLabelMirrorProjectHeader(), gbc_jLabelMirrorProjectHeader);
		GridBagConstraints gbc_jLabelMirrorProjectSource = new GridBagConstraints();
		gbc_jLabelMirrorProjectSource.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorProjectSource.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelMirrorProjectSource.gridx = 0;
		gbc_jLabelMirrorProjectSource.gridy = 10;
		add(getJLabelMirrorProjectSource(), gbc_jLabelMirrorProjectSource);
		GridBagConstraints gbc_jTextFieldProjectMirrorSource = new GridBagConstraints();
		gbc_jTextFieldProjectMirrorSource.gridwidth = 2;
		gbc_jTextFieldProjectMirrorSource.insets = new Insets(0, 5, 5, 5);
		gbc_jTextFieldProjectMirrorSource.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldProjectMirrorSource.gridx = 1;
		gbc_jTextFieldProjectMirrorSource.gridy = 10;
		add(getJTextFieldProjectMirrorSource(), gbc_jTextFieldProjectMirrorSource);
		GridBagConstraints gbc_jLabelMirrorProjectDestination = new GridBagConstraints();
		gbc_jLabelMirrorProjectDestination.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelMirrorProjectDestination.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorProjectDestination.gridx = 0;
		gbc_jLabelMirrorProjectDestination.gridy = 11;
		add(getJLabelMirrorProjectDestination(), gbc_jLabelMirrorProjectDestination);
		GridBagConstraints gbc_jTextFieldProjectMirrorDestination = new GridBagConstraints();
		gbc_jTextFieldProjectMirrorDestination.gridwidth = 2;
		gbc_jTextFieldProjectMirrorDestination.insets = new Insets(0, 5, 5, 5);
		gbc_jTextFieldProjectMirrorDestination.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldProjectMirrorDestination.gridx = 1;
		gbc_jTextFieldProjectMirrorDestination.gridy = 11;
		add(getJTextFieldProjectMirrorDestination(), gbc_jTextFieldProjectMirrorDestination);
		GridBagConstraints gbc_jLabelMirrorProjectProviderURL = new GridBagConstraints();
		gbc_jLabelMirrorProjectProviderURL.insets = new Insets(0, 5, 5, 0);
		gbc_jLabelMirrorProjectProviderURL.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorProjectProviderURL.gridx = 0;
		gbc_jLabelMirrorProjectProviderURL.gridy = 12;
		add(getJLabelMirrorProjectProviderURL(), gbc_jLabelMirrorProjectProviderURL);
		GridBagConstraints gbc_jTextFieldProjectMirrorProviderURL = new GridBagConstraints();
		gbc_jTextFieldProjectMirrorProviderURL.insets = new Insets(0, 5, 5, 5);
		gbc_jTextFieldProjectMirrorProviderURL.gridwidth = 2;
		gbc_jTextFieldProjectMirrorProviderURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_jTextFieldProjectMirrorProviderURL.gridx = 1;
		gbc_jTextFieldProjectMirrorProviderURL.gridy = 12;
		add(getJTextFieldProjectMirrorProviderURL(), gbc_jTextFieldProjectMirrorProviderURL);
		GridBagConstraints gbc_jLabelMirrorIntervalHeader = new GridBagConstraints();
		gbc_jLabelMirrorIntervalHeader.gridwidth = 3;
		gbc_jLabelMirrorIntervalHeader.insets = new Insets(5, 5, 5, 5);
		gbc_jLabelMirrorIntervalHeader.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorIntervalHeader.gridx = 0;
		gbc_jLabelMirrorIntervalHeader.gridy = 13;
		add(getJLabelMirrorIntervalHeader(), gbc_jLabelMirrorIntervalHeader);
		GridBagConstraints gbc_jLabelMirrorInterval = new GridBagConstraints();
		gbc_jLabelMirrorInterval.insets = new Insets(0, 5, 0, 0);
		gbc_jLabelMirrorInterval.anchor = GridBagConstraints.WEST;
		gbc_jLabelMirrorInterval.gridx = 0;
		gbc_jLabelMirrorInterval.gridy = 14;
		add(getJLabelMirrorInterval(), gbc_jLabelMirrorInterval);
		GridBagConstraints gbc_jTextFieldMirrorInterval = new GridBagConstraints();
		gbc_jTextFieldMirrorInterval.anchor = GridBagConstraints.WEST;
		gbc_jTextFieldMirrorInterval.insets = new Insets(0, 5, 0, 0);
		gbc_jTextFieldMirrorInterval.gridx = 1;
		gbc_jTextFieldMirrorInterval.gridy = 14;
		add(getJTextFieldMirrorInterval(), gbc_jTextFieldMirrorInterval);
		
		
	}
	private JCheckBox getJCheckBoxStartSecondMTP() {
		if (jCheckBoxStartSecondMTP == null) {
			jCheckBoxStartSecondMTP = new JCheckBox("Start a second MTP for external Communication");
			jCheckBoxStartSecondMTP.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jCheckBoxStartSecondMTP;
	}
	
	private JLabel getJLabelProtocol() {
		if (jLabelProtocol == null) {
			jLabelProtocol = new JLabel("Protocol");
			jLabelProtocol.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelProtocol;
	}
	private JComboBoxMtpProtocol getJComboBoxMtpProtocol() {
		if (jComboBoxMtpProtocol == null) {
			jComboBoxMtpProtocol = new JComboBoxMtpProtocol();
			jComboBoxMtpProtocol.setFont(new Font("Dialog", Font.PLAIN, 12));
			DefaultComboBoxModel<MtpProtocol> comboModel = (DefaultComboBoxModel<MtpProtocol>) jComboBoxMtpProtocol.getModel();
			comboModel.removeElement(MtpProtocol.PROXIEDHTTPS);
			jComboBoxMtpProtocol.setPreferredSize(new Dimension(80, 26));
			jComboBoxMtpProtocol.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					CeaConfigModelPanel.this.updateMtpExample();
				}
			});
		}
		return jComboBoxMtpProtocol;
	}
	
	private JLabel getJLabelURLorIP() {
		if (jLabelURLorIP == null) {
			jLabelURLorIP = new JLabel("URL or IP");
			jLabelURLorIP.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelURLorIP;
	}
	private JTextField getJTextFieldURLorIP() {
		if (jTextFieldURLorIP == null) {
			jTextFieldURLorIP = new JTextField();
			jTextFieldURLorIP.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldURLorIP.setPreferredSize(new Dimension(160, 26));
			jTextFieldURLorIP.getDocument().addDocumentListener(this);
		}
		return jTextFieldURLorIP;
	}
	
	private JLabel getJLabelPort() {
		if (jLabelPort == null) {
			jLabelPort = new JLabel("Port");
			jLabelPort.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelPort;
	}
	private JTextField getJTextFieldPort() {
		if (jTextFieldPort == null) {
			jTextFieldPort = new JTextField();
			jTextFieldPort.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldPort.setPreferredSize(new Dimension(70, 26));
			jTextFieldPort.addKeyListener(new KeyAdapter4Numbers(false));
			jTextFieldPort.getDocument().addDocumentListener(this);
		}
		return jTextFieldPort;
	}
	
	private JLabel getJLabelMTPAddress() {
		if (jLabelMTPAddress == null) {
			jLabelMTPAddress = new JLabel("http://123.123.12.3:7779/acc");
			jLabelMTPAddress.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMTPAddress;
	}
	
	private JSeparator getJSeparatorHorizontal() {
		if (jSeparatorHorizontal == null) {
			jSeparatorHorizontal = new JSeparator();
		}
		return jSeparatorHorizontal;
	}
	
	
	private JLabel getJLabelMirrorP2Header() {
		if (jLabelMirrorP2Header == null) {
			jLabelMirrorP2Header = new JLabel("p2 Mirror");
			jLabelMirrorP2Header.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorP2Header;
	}
	private JLabel getJLabelMirrorP2Source() {
		if (jLabelMirrorP2Source == null) {
			jLabelMirrorP2Source = new JLabel("Source");
			jLabelMirrorP2Source.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorP2Source;
	}
	private JTextField getJTextFieldP2MirrorSource() {
		if (jTextFieldP2MirrorSource == null) {
			jTextFieldP2MirrorSource = new JTextField();
			jTextFieldP2MirrorSource.setPreferredSize(new Dimension(200, 26));
		}
		return jTextFieldP2MirrorSource;
	}
	private JLabel getJLabelMirrorP2Destination() {
		if (jLabelMirrorP2Destination == null) {
			jLabelMirrorP2Destination = new JLabel("Destination");
			jLabelMirrorP2Destination.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorP2Destination;
	}
	private JTextField getJTextFieldP2MirrorDestination() {
		if (jTextFieldP2MirrorDestination == null) {
			jTextFieldP2MirrorDestination = new JTextField();
			jTextFieldP2MirrorDestination.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldP2MirrorDestination.setPreferredSize(new Dimension(200, 26));
		}
		return jTextFieldP2MirrorDestination;
	}
	private JLabel getJLabelMirrorP2ProviderURL() {
		if (jLabelMirrorP2ProviderURL == null) {
			jLabelMirrorP2ProviderURL = new JLabel("Provider URL");
			jLabelMirrorP2ProviderURL.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorP2ProviderURL;
	}
	private JTextField getJTextFieldp2MirrorProviderURL() {
		if (jTextFieldP2MirrorProviderURL == null) {
			jTextFieldP2MirrorProviderURL = new JTextField();
			jTextFieldP2MirrorProviderURL.setPreferredSize(new Dimension(200, 26));
			jTextFieldP2MirrorProviderURL.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jTextFieldP2MirrorProviderURL;
	}
	
	
	private JLabel getJLabelMirrorProjectHeader() {
		if (jLabelMirrorProjectHeader == null) {
			jLabelMirrorProjectHeader = new JLabel("Project Repository Mirror");
			jLabelMirrorProjectHeader.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorProjectHeader;
	}
	private JLabel getJLabelMirrorProjectSource() {
		if (jLabelMirrorProjectSource == null) {
			jLabelMirrorProjectSource = new JLabel("Source");
			jLabelMirrorProjectSource.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorProjectSource;
	}
	private JTextField getJTextFieldProjectMirrorSource() {
		if (jTextFieldProjectMirrorSource == null) {
			jTextFieldProjectMirrorSource = new JTextField();
			jTextFieldProjectMirrorSource.setPreferredSize(new Dimension(200, 26));
			jTextFieldProjectMirrorSource.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jTextFieldProjectMirrorSource;
	}
	private JLabel getJLabelMirrorProjectDestination() {
		if (jLabelMirrorProjectDestination == null) {
			jLabelMirrorProjectDestination = new JLabel("Destination");
			jLabelMirrorProjectDestination.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorProjectDestination;
	}
	private JTextField getJTextFieldProjectMirrorDestination() {
		if (jTextFieldProjectMirrorDestination == null) {
			jTextFieldProjectMirrorDestination = new JTextField();
			jTextFieldProjectMirrorDestination.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldProjectMirrorDestination.setPreferredSize(new Dimension(200, 26));
		}
		return jTextFieldProjectMirrorDestination;
	}
	private JLabel getJLabelMirrorProjectProviderURL() {
		if (jLabelMirrorProjectProviderURL == null) {
			jLabelMirrorProjectProviderURL = new JLabel("Provider URL");
			jLabelMirrorProjectProviderURL.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorProjectProviderURL;
	}
	private JTextField getJTextFieldProjectMirrorProviderURL() {
		if (jTextFieldProjectMirrorProviderURL == null) {
			jTextFieldProjectMirrorProviderURL = new JTextField();
			jTextFieldProjectMirrorProviderURL.setPreferredSize(new Dimension(200, 26));
			jTextFieldProjectMirrorProviderURL.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jTextFieldProjectMirrorProviderURL;
	}
	
	
	private JLabel getJLabelMirrorIntervalHeader() {
		if (jLabelMirrorIntervalHeader == null) {
			jLabelMirrorIntervalHeader = new JLabel("Mirror Interval (where zero indicates no mirroring)");
			jLabelMirrorIntervalHeader.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorIntervalHeader;
	}
	private JLabel getJLabelMirrorInterval() {
		if (jLabelMirrorInterval == null) {
			jLabelMirrorInterval = new JLabel("Interval [h]");
			jLabelMirrorInterval.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelMirrorInterval;
	}
	private JTextField getJTextFieldMirrorInterval() {
		if (jTextFieldMirrorInterval == null) {
			jTextFieldMirrorInterval = new JTextField();
			jTextFieldMirrorInterval.setPreferredSize(new Dimension(70, 26));
			jTextFieldMirrorInterval.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTextFieldMirrorInterval.addKeyListener(new KeyAdapter4Numbers(false));
		}
		return jTextFieldMirrorInterval;
	}
	
	
	/**
	 * Returns the {@link CeaConfigModel}.
	 * @return the cea data model
	 */
	public CeaConfigModel getCeaConfigModel() {
		if (ceaConfigModel==null) {
			ceaConfigModel = new CeaConfigModel();
			// --- Set the defaults -------------
			ceaConfigModel.setStartSecondMTP(true);
			ceaConfigModel.setMtpProtocol(MtpProtocol.HTTP);
			ceaConfigModel.setMtpPort(7778);
			ceaConfigModel.setMirrorSourceP2Repository("https://p2.enflex.it/awb/latest/");
			ceaConfigModel.setMirrorDestinationP2Repository("/opt/hygrid/mirrorP2");
			ceaConfigModel.setMirrorSourceProjectRepository("https://p2.enflex.it/awbProjectRepository/");
			ceaConfigModel.setMirrorDestinationProjectRepository("/opt/hygrid/mirrorProjects");
			ceaConfigModel.setMirrorInterval(1);
		}
		return ceaConfigModel;
	}
	/**
	 * Sets the CeaConfigModel to the visualization.
	 * @param ceaConfigModel the new cea data model
	 */
	public void setCeaConfigModel(CeaConfigModel ceaConfigModel) {
		this.ceaConfigModel = ceaConfigModel;
		this.setModelToForm();
	}
	
	/**
	 * Saves the current settings to the {@link CeaConfigModel} and does some error checks.
	 * @return true, if successful (no errors found)
	 */
	public boolean save() {
		this.setFormToModel();
		if (this.hasErrors()==true) {
			return false;
		}
		return true;
	}
	/**
	 * Checks for errors.
	 * @return true, if successful
	 */
	private boolean hasErrors() {
		
		boolean hasErrors = false;
		// TODO Do error checks here
		return hasErrors;
	}
	
	/**
	 * Sets the model to form.
	 */
	private void setModelToForm() {
	
		this.getJCheckBoxStartSecondMTP().setSelected(this.getCeaConfigModel().isStartSecondMTP());
		this.getJComboBoxMtpProtocol().setSelectedProtocol(this.getCeaConfigModel().getMtpProtocol());
		this.getJTextFieldURLorIP().setText(this.getCeaConfigModel().getUrlOrIp());
		this.getJTextFieldPort().setText("" + this.getCeaConfigModel().getMtpPort());
		
		this.getJTextFieldP2MirrorSource().setText(this.getCeaConfigModel().getMirrorSourceP2Repository());
		this.getJTextFieldP2MirrorDestination().setText(this.getCeaConfigModel().getMirrorDestinationP2Repository());
		this.getJTextFieldp2MirrorProviderURL().setText(this.getCeaConfigModel().getMirrorProviderURLP2Repository());
		
		this.getJTextFieldProjectMirrorSource().setText(this.getCeaConfigModel().getMirrorSourceProjectRepository());
		this.getJTextFieldProjectMirrorDestination().setText(this.getCeaConfigModel().getMirrorDestinationProjectRepository());
		this.getJTextFieldProjectMirrorProviderURL().setText(this.getCeaConfigModel().getMirrorProviderURLProjectRepository());
		
		this.getJTextFieldMirrorInterval().setText("" + this.getCeaConfigModel().getMirrorInterval());
	}
	/**
	 * Sets the form to model.
	 */
	private void setFormToModel() {
		
		this.getCeaConfigModel().setStartSecondMTP(this.getJCheckBoxStartSecondMTP().isSelected());
		this.getCeaConfigModel().setMtpProtocol(this.getJComboBoxMtpProtocol().getSelectedProtocol());
		this.getCeaConfigModel().setUrlOrIp(this.getTextFieldStringValue(this.getJTextFieldURLorIP()));
		this.getCeaConfigModel().setMtpPort(this.getTextFieldIntegerValue(this.getJTextFieldPort()));

		this.getCeaConfigModel().setMirrorSourceP2Repository(this.getTextFieldStringValue(this.getJTextFieldP2MirrorSource()));
		this.getCeaConfigModel().setMirrorDestinationP2Repository(this.getTextFieldStringValue(this.getJTextFieldP2MirrorDestination()));
		this.getCeaConfigModel().setMirrorProviderURLP2Repository(this.getTextFieldStringValue(this.getJTextFieldp2MirrorProviderURL()));
		
		this.getCeaConfigModel().setMirrorSourceProjectRepository(this.getTextFieldStringValue(this.getJTextFieldProjectMirrorSource()));
		this.getCeaConfigModel().setMirrorDestinationProjectRepository(this.getTextFieldStringValue(this.getJTextFieldProjectMirrorDestination()));
		this.getCeaConfigModel().setMirrorProviderURLProjectRepository(this.getTextFieldStringValue(this.getJTextFieldProjectMirrorProviderURL()));
		
		this.getCeaConfigModel().setMirrorInterval(this.getTextFieldIntegerValue(this.getJTextFieldMirrorInterval()));
		
	}

	private String getTextFieldStringValue(JTextField textField) {
		if (textField==null) return null;
		if (textField.getText()==null || textField.getText().isEmpty()) return null;
		return textField.getText().trim();
	}
	private int getTextFieldIntegerValue(JTextField textField) {
		if (textField==null) return 0;
		if (textField.getText()==null || textField.getText().isEmpty()) return 0;
		return Integer.parseInt(textField.getText().trim());
	}
	
	/**
	 * Update the MTP address example.
	 */
	private void updateMtpExample() {
		String mtpAddress = this.getJComboBoxMtpProtocol().getSelectedProtocol().toString().toLowerCase() + "://";
		mtpAddress += this.getTextFieldStringValue(this.getJTextFieldURLorIP()) + ":";
		mtpAddress += this.getTextFieldIntegerValue(this.getJTextFieldPort()) + "/acc";
		this.jLabelMTPAddress.setText(mtpAddress);
	}
	@Override
	public void insertUpdate(DocumentEvent e) {
		this.updateMtpExample();
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
		this.updateMtpExample();
	}
	@Override
	public void changedUpdate(DocumentEvent e) {
		this.updateMtpExample();
	}
	
}
