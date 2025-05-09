package de.enflexit.ea.core.configuration.eom.systems.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JWindow;

import de.enflexit.common.swing.AwbThemeColor;
import de.enflexit.ea.core.configuration.eom.systems.SystemConfigurationManager;
import javax.swing.JSeparator;


/**
 * The Class SystemConfigurationOptions.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SystemConfigurationOptions extends JWindow implements ActionListener {

	private static final long serialVersionUID = 5320998070220387705L;
	
	private SystemConfigurationManager systemConfigurationManager;
	
	private boolean allowClosing = true;
	
	private JLabel jLabelHeader;
	private JLabel jLabelErrors;
	
	private JCheckBox jCheckBoxUseAggregationsForSingleSystems;
	private JLabel jLabelNotice;
	private JLabel jLabelConfigurationCheck;
	private JSeparator separator;

	/**
	 * Instantiates a new evaluation details curve selection.
	 *
	 * @param owner the owner
	 * @param chartPanel the chart panel
	 */
	public SystemConfigurationOptions(Window owner, SystemConfigurationManager systemConfigurationManager) {
		super(owner);
		this.systemConfigurationManager = systemConfigurationManager;
		this.initialize();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 100, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.getContentPane().setLayout(gridBagLayout);

		GridBagConstraints gbc_jLabelHeader = new GridBagConstraints();
		gbc_jLabelHeader.anchor = GridBagConstraints.WEST;
		gbc_jLabelHeader.insets = new Insets(15, 15, 0, 15);
		gbc_jLabelHeader.gridx = 0;
		gbc_jLabelHeader.gridy = 0;
		this.getContentPane().add(this.getJLabelHeader(), gbc_jLabelHeader);

		GridBagConstraints gbc_jLabelNotice = new GridBagConstraints();
		gbc_jLabelNotice.anchor = GridBagConstraints.NORTHWEST;
		gbc_jLabelNotice.insets = new Insets(10, 15, 0, 15);
		gbc_jLabelNotice.gridx = 0;
		gbc_jLabelNotice.gridy = 1;
		this.getContentPane().add(this.getJLabelNotice(), gbc_jLabelNotice);

		GridBagConstraints gbc_jCheckBoxUseAggregationsForSingleSystems = new GridBagConstraints();
		gbc_jCheckBoxUseAggregationsForSingleSystems.anchor = GridBagConstraints.WEST;
		gbc_jCheckBoxUseAggregationsForSingleSystems.insets = new Insets(10, 15, 0, 15);
		gbc_jCheckBoxUseAggregationsForSingleSystems.gridx = 0;
		gbc_jCheckBoxUseAggregationsForSingleSystems.gridy = 2;
		this.getContentPane().add(getJCheckBoxUseAggregationsForSingleSystems(), gbc_jCheckBoxUseAggregationsForSingleSystems);
		
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(10, 15, 0, 15);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 3;
		getContentPane().add(getSeparator(), gbc_separator);
		
		GridBagConstraints gbc_jLabelErrors = new GridBagConstraints();
		gbc_jLabelErrors.insets = new Insets(10, 15, 0, 15);
		gbc_jLabelErrors.anchor = GridBagConstraints.WEST;
		gbc_jLabelErrors.gridx = 0;
		gbc_jLabelErrors.gridy = 4;
		this.getContentPane().add(this.getJLabelErrors(), gbc_jLabelErrors);
		
		GridBagConstraints gbc_jLabelConfigurationCheck = new GridBagConstraints();
		gbc_jLabelConfigurationCheck.insets = new Insets(5, 15, 15, 15);
		gbc_jLabelConfigurationCheck.anchor = GridBagConstraints.WEST;
		gbc_jLabelConfigurationCheck.gridx = 0;
		gbc_jLabelConfigurationCheck.gridy = 5;
		this.getContentPane().add(this.getJLabelConfigurationCheck(), gbc_jLabelConfigurationCheck);
		
		this.setSize(650, 350);
		this.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				if (allowClosing==true) {
					setVisible(false);
					dispose();
				}
			}
			@Override
			public void windowGainedFocus(WindowEvent e) {
				// --- Nothing to do here ---
			}
		});
		
		this.setVisible(true);
		this.requestFocus();
	}
	
	private JLabel getJLabelHeader() {
		if (jLabelHeader == null) {
			jLabelHeader = new JLabel("Notice for Blueprint Definitions");
			jLabelHeader.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelHeader;
	}
	private JLabel getJLabelNotice() {
		if (jLabelNotice == null) {
			
			String notice = "<html>";
			notice += "<p>";
			notice += "In case that a new aggregation is to be created, <b>exactly one EOM-System</b> within a blueprint configuration<br>";
			notice += "must be a <b>TechnicalSystemGroup</b>!<br><br>";
			notice += "Normally, it is assumed that for <b>aggregations with a single sub system</b> no aggregation is required and that<br>";
			notice += "only the single EOM-System needs to be assigned!<br>";
			notice += "You may disable this mechanism below.<br>";
			notice += "</p>";
			notice += "</html>";
			jLabelNotice = new JLabel(notice);
			jLabelNotice.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jLabelNotice;
	}
	
	private JCheckBox getJCheckBoxUseAggregationsForSingleSystems() {
		if (jCheckBoxUseAggregationsForSingleSystems == null) {
			jCheckBoxUseAggregationsForSingleSystems = new JCheckBox("Use aggregations also for single sub systems");
			jCheckBoxUseAggregationsForSingleSystems.setFont(new Font("Dialog", Font.PLAIN, 12));
			jCheckBoxUseAggregationsForSingleSystems.addActionListener(this);
			jCheckBoxUseAggregationsForSingleSystems.setSelected(this.systemConfigurationManager.getSystemConfiguration().isUseAggregationsAlsoForSingleSubsystems());
		}
		return jCheckBoxUseAggregationsForSingleSystems;
	}
	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
		}
		return separator;
	}
	private JLabel getJLabelErrors() {
		if (jLabelErrors == null) {
			jLabelErrors = new JLabel("Configuration Check");
			jLabelErrors.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelErrors;
	}
	
	private JLabel getJLabelConfigurationCheck() {
		if (jLabelConfigurationCheck == null) {
			jLabelConfigurationCheck = new JLabel();
			jLabelConfigurationCheck.setFont(new Font("Dialog", Font.PLAIN, 12));
			this.setTextConfigurationCheck();
		}
		return jLabelConfigurationCheck;
	}
	private void setTextConfigurationCheck() {
		
		Color headerColor = AwbThemeColor.ButtonTextGreen.getColor();
		String bluePrintStateUpdate = "<b>No errors were found in the current blueprint configuration!</b>";
		// --- Get list of configuration issues -----------------
		List<String> currConfigInforamtion = this.systemConfigurationManager.getSystemConfiguration().getFaultySystemBlueprintConfigurationInformations();
		if (currConfigInforamtion.size()>0) {
			headerColor = AwbThemeColor.ButtonTextRed.getColor();
			bluePrintStateUpdate = "<b>Errors in the current blueprint configuration:</b><br>";
			for (String partState : currConfigInforamtion) {
				bluePrintStateUpdate += "- " + partState + "<br>"; 
			}
		}
		this.getJLabelConfigurationCheck().setForeground(headerColor);
		this.getJLabelConfigurationCheck().setText("<html>" + bluePrintStateUpdate + "</html>");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJCheckBoxUseAggregationsForSingleSystems()) {
			this.systemConfigurationManager.getSystemConfiguration().setUseAggregationsAlsoForSingleSubsystems(this.getJCheckBoxUseAggregationsForSingleSystems().isSelected());
			this.systemConfigurationManager.saveSettings();
			this.setTextConfigurationCheck();
		}
	}
	
}
