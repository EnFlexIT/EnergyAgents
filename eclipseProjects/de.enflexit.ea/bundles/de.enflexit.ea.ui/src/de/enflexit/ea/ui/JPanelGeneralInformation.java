package de.enflexit.ea.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.enflexit.common.properties.PropertiesPanel;

/**
 * The Class JPanelGeneralInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelGeneralInformation extends JPanel {

	private static final long serialVersionUID = -1052414258048129787L;

	private PropertiesPanel propertiesPanel;
	
	/**
	 * Instantiates a new j panel general information.
	 * @param jDialogEnergyAgent the JDialog of the energy agent
	 */
	public JPanelGeneralInformation(JDialogEnergyAgent jDialogEnergyAgent) {
		this.initialize();
		this.setDisplayInformation(jDialogEnergyAgent);
	}
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation(final JDialogEnergyAgent jDialogEnergyAgent) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPanelGeneralInformation.this.getJPanelProperties().setProperties(jDialogEnergyAgent.getEnergyAgent().getGeneralInformation());
			}
		});
	}
	
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints gbc_PropertiesPanel = new GridBagConstraints();
		gbc_PropertiesPanel.fill = GridBagConstraints.BOTH;
		gbc_PropertiesPanel.gridx = 0;
		gbc_PropertiesPanel.gridy = 0;
		this.add(this.getJPanelProperties(), gbc_PropertiesPanel);
	}
	/**
	 * Returns the properties panel.
	 * @return the properties panel 
	 */
	private PropertiesPanel getJPanelProperties() {
		if (propertiesPanel == null) {
			propertiesPanel = new PropertiesPanel(null, "Energy Agent State", true);
		}
		return propertiesPanel;
	}
}
