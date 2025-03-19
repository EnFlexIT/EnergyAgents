package de.enflexit.ea.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import de.enflexit.awb.core.config.GlobalInfo;
import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;

/**
 * The Class JDialogEnergyAgent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JDialogEnergyAgent extends JDialog implements EnergyAgentWindowInterface, PropertyChangeListener {

	private static final long serialVersionUID = -2867085753299220850L;

	private AbstractEnergyAgent energyAgent;
	private JPanelEnergyAgent jPanelEnergyAgent;
	
	/**
	 * Instantiates a new JDialog energy agent.
	 * @param energyAgent the energy agent
	 */
	public JDialogEnergyAgent(Window parentWindow, AbstractEnergyAgent energyAgent) {
		super(parentWindow);
		this.energyAgent = energyAgent;
		this.initialize();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setSize(1200, 700);
		this.setIconImage(GlobalInfo.getInternalImageAwbIcon16());
		this.setTitle("State of Energy Agent '" + this.energyAgent.getLocalName() + "' (" + this.energyAgent.getInternalDataModel().getNetworkComponent().getType() + ")");
		this.registerEscapeKeyStroke();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		this.getContentPane().setLayout(gridBagLayout);
		
		this.setJMenuBar(this.getJPanelEnergyAgent().getJMenuBarEnergyAgent());
		
		GridBagConstraints gbc_jTabbedPane = new GridBagConstraints();
		gbc_jTabbedPane.fill = GridBagConstraints.BOTH;
		gbc_jTabbedPane.gridx = 0;
		gbc_jTabbedPane.gridy = 0;
		this.getContentPane().add(this.getJPanelEnergyAgent(), gbc_jTabbedPane);
	
		WindowSizeAndPostionController.setJDialogPositionOnScreen(this, JDialogPosition.ParentBottomRight);
	}
	/**
     * Registers the escape key stroke in order to close this dialog.
     */
    private void registerEscapeKeyStroke() {
    	final ActionListener listener = new ActionListener() {
            public final void actionPerformed(final ActionEvent e) {
    			JDialogEnergyAgent.this.setVisible(false);
    			JDialogEnergyAgent.this.dispose();
            }
        };
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        this.getRootPane().registerKeyboardAction(listener, keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
    
	/**
	 * Returns the JPanelEnergyAgent.
	 * @return the j panel energy agent
	 */
	private JPanelEnergyAgent getJPanelEnergyAgent() {
		if (jPanelEnergyAgent==null) {
			jPanelEnergyAgent = new JPanelEnergyAgent(this.energyAgent);
			jPanelEnergyAgent.addPropertyListener(this);
		}
		return jPanelEnergyAgent;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.EnergyAgentWindowInterface#firePropertyEvent(de.enflexit.ea.ui.SwingUiModel.PropertyEvent)
	 */
	@Override
	public void firePropertyEvent(PropertyEvent propertyEvent) {
		this.getJPanelEnergyAgent().firePropertyEvent(propertyEvent);
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getNewValue() instanceof PropertyEvent == false) return;
		PropertyEvent pEvent = (PropertyEvent) evt.getNewValue();
		switch (pEvent) {
		case ShowOrFocusView:
			if (this.isVisible()==true) {
				this.requestFocusInWindow();
			} else {
				this.setVisible(true);
			}
			break;
		case CloseView:
			this.setVisible(false);
			this.dispose();
			break;
		default:
			break;
		}
	}
	
}
