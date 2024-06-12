package de.enflexit.ea.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;

/**
 * The Class JPanelGeneralInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelGeneralInformation extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = -1052414258048129787L;

	private SwingUiModelInterface swingUiModelInterface;
	private PropertiesPanel propertiesPanel;
	
	/**
	 * Instantiates a new j panel general information.
	 *
	 * @param swingUiModelInterface the swing ui model interface
	 */
	public JPanelGeneralInformation(SwingUiModelInterface swingUiModelInterface) {
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
		this.initialize();
		this.setDisplayInformation();
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
	
	
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPanelGeneralInformation.this.getJPanelProperties().setProperties(JPanelGeneralInformation.this.swingUiModelInterface.getEnergyAgent().getGeneralInformation());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getNewValue() instanceof UiDataCollection) return;
		
		PropertyEvent pe = (PropertyEvent) evt.getNewValue(); 
		switch (pe) {
		case UpdateView:
			this.setDisplayInformation();
			break;

		default:
			break;
		}
	}
}
