package de.enflexit.ea.ui;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Font;


/**
 * The Class JPanelGeneralInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelGeneralInformation extends JPanel {

	private static final long serialVersionUID = -1052414258048129787L;

	private JDialogEnergyAgent jDialogEnergyAgent;
	private JLabel lblNewLabel;
	
	/**
	 * Instantiates a new j panel general information.
	 * @param jDialogEnergyAgent the j dialog energy agent
	 */
	public JPanelGeneralInformation(JDialogEnergyAgent jDialogEnergyAgent) {
		this.jDialogEnergyAgent = jDialogEnergyAgent;
		this.initialize();
	}
	
	private void initialize() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(getLblNewLabel(), gbc_lblNewLabel);
	}
	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Hello World");
			lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return lblNewLabel;
	}
}
