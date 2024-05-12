package de.enflexit.ea.core.configuration.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.enflexit.ea.core.configuration.SetupConfigurationAttributeService;
import de.enflexit.ea.core.configuration.SetupConfigurationAttributeWithUI;
import de.enflexit.ea.core.configuration.SetupConfigurationServiceHelper;
import de.enflexit.ea.core.configuration.model.SetupConfigurationModel;

/**
 * The Class SetupConfigurationPanel.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class SetupConfigurationPanel extends JPanel {
	
	private static final long serialVersionUID = -208261216805973521L;

	private SetupConfigurationModel setupConfigModel;
	
	private SetupConfigurationToolBar jToolBarSetupConfiguration;
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
		this.add(this.getJToolBarSetupConfiguration(), BorderLayout.NORTH);
		this.add(this.getJTabbedPane(), BorderLayout.CENTER);
	}
	/**
	 * Disposes the current panel.
	 */
	public void dispose() {
		
		// --- Dispose individual configuration panel ---------------
		List<SetupConfigurationAttributeService> attributeServiceListWithUIs = SetupConfigurationServiceHelper.getSetupConfigurationAttributeListWithUIs();
		for (SetupConfigurationAttributeService attrbuteService : attributeServiceListWithUIs) {
			try {
				SetupConfigurationAttributeWithUI<?> attribute = (SetupConfigurationAttributeWithUI<?>) attrbuteService.getSetupConfigurationAttribute();
				attribute.disposeAttributeConfigurationPanel();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// --- Dispose the SetupConfigurationModel ------------------
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
	 * Returns the SetupConfigurationToolBar.
	 * @return the JToolBar for the setup configuration
	 */
	private SetupConfigurationToolBar getJToolBarSetupConfiguration() {
		if (jToolBarSetupConfiguration==null) {
			jToolBarSetupConfiguration = new SetupConfigurationToolBar(this.getSetupConfigurationModel());
		}
		return jToolBarSetupConfiguration;
	}
	/**
	 * Returns the local JTabbedPane.
	 * @return the JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			jTabbedPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTabbedPane.addTab(" Configuration Table   ", null, this.getSetupConfigurationTablePanel(), "Setup Configuration Table");
			
			// --- Get the individual attribute UI components -----------------
			List<SetupConfigurationAttributeService> attributeServiceListWithUIs = SetupConfigurationServiceHelper.getSetupConfigurationAttributeListWithUIs();
			for (SetupConfigurationAttributeService attrbuteService : attributeServiceListWithUIs) {
				
				try {
					SetupConfigurationAttributeWithUI<?> attribute = (SetupConfigurationAttributeWithUI<?>) attrbuteService.getSetupConfigurationAttribute();
					JComponent component = attribute.getAttributeConfigurationPanel();
					if (component!=null) {
						component.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
						jTabbedPane.addTab(" " + attrbuteService.getAttributeName() + "   ", null, component, "Setup Configuration Table");
					}
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
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
