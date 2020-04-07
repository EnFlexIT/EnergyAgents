package de.enflexit.energyAgent.ops.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import de.enflexit.energyAgent.ops.OpsController;
import de.enflexit.energyAgent.ops.OpsControllerEvent;
import de.enflexit.energyAgent.ops.OpsControllerListener;

/**
 * The Class JFrameOpsControlStatusBar represents the status visualization 
 * within the {@link JFrameOpsControl}.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class JFrameOpsControlStatusBar extends JPanel implements OpsControllerListener {

	private static final long serialVersionUID = 2430486249132696024L;

	private OpsController opsController;
	private JLabel jLabelStatusMessage;
	private JSeparator separator;
	
	/**
	 * Instantiates a new JFrameOpsControlStatusBar.
	 * @param opsController the OpsController
	 */
	public JFrameOpsControlStatusBar(OpsController opsController) {
		this.opsController = opsController;
		this.opsController.addOpsControllerListener(this);
		this.initialize();
	}
	private void initialize() {
		
		this.setPreferredSize(new Dimension(200, 26));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 0;
		this.add(getSeparator(), gbc_separator);
		
		GridBagConstraints gbc_jLabelStatusMessage = new GridBagConstraints();
		gbc_jLabelStatusMessage.fill = GridBagConstraints.VERTICAL;
		gbc_jLabelStatusMessage.insets = new Insets(3, 5, 2, 5);
		gbc_jLabelStatusMessage.anchor = GridBagConstraints.WEST;
		gbc_jLabelStatusMessage.gridx = 0;
		gbc_jLabelStatusMessage.gridy = 1;
		this.add(getJLabelStatusMessage(), gbc_jLabelStatusMessage);
	}
	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
		}
		return separator;
	}	
	private JLabel getJLabelStatusMessage() {
		if (jLabelStatusMessage == null) {
			jLabelStatusMessage = new JLabel("Ready");
			jLabelStatusMessage.setFont(new Font("SansSerif", Font.PLAIN, 12));
		}
		return jLabelStatusMessage;
	}
	
	/**
	 * Sets the status text in the status bar.
	 * @param newStatusText the new status text
	 */
	public void setStatusText(String newStatusText) {
		this.getJLabelStatusMessage().setText(newStatusText);
	}
	
	/* (non-Javadoc)
	 * @see hygrid.ops.OpsControllerListener#onOpsControllerEvent(hygrid.ops.OpsControllerEvent)
	 */
	@Override
	public void onOpsControllerEvent(OpsControllerEvent controllerEvent) {

		switch (controllerEvent.getControllerEvent()) {
		case OPS_CONNECTED:
			// --- Nothing to do here yet -------
			break;
			
		case OPS_DISCONNECTED:
			// --- Nothing to do here yet -------
			this.setStatusText("Ready");
			break;
		}
		
	}
	
	
}
