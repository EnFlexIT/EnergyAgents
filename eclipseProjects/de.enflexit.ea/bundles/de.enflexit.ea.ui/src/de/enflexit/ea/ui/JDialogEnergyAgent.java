package de.enflexit.ea.ui;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import agentgui.core.config.GlobalInfo;
import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;

import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;

/**
 * The Class JDialogEnergyAgent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JDialogEnergyAgent extends JDialog implements SwingUiModelInterface, PropertyChangeListener {

	private static final long serialVersionUID = -2867085753299220850L;

	private AbstractEnergyAgent energyAgent;
	private SwingUiModelInterface propertyModel;

	private JMenuBarEnergyAgent jMenuBarEnergyAgent;
	private JTabbedPane jTabbedPane;
	private JPanelGeneralInformation jPanelGeneralInformation;
	private JPanelRealTimeInformation jPanelRealTimeInformation;
	private JPanelPlannerInformation jPanelPlannerInformation;
	private JPanelEomPlanningEventTreeMap jPanelEomPlanningEventTreeMap;
	
	
	/**
	 * Instantiates a new JDialog energy agent.
	 * @param energyAgent the energy agent
	 */
	public JDialogEnergyAgent(Window parentWindow, AbstractEnergyAgent energyAgent) {
		super(parentWindow);
		this.setEnergyAgent(energyAgent);
		this.initialize();
	}

	
	/**
	 * Returns the local property model for the UI.
	 * @return the property model
	 */
	private SwingUiModelInterface getSwingUiModel() {
		if (propertyModel==null) {
			propertyModel = new SwingUiModel(this.getEnergyAgent());
			propertyModel.addPropertyListener(this);
		}
		return propertyModel;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#getEnergyAgent()
	 */
	@Override
	public AbstractEnergyAgent getEnergyAgent() {
		return energyAgent;
	}
	/**
	 * Sets the local energy agent.
	 * @param energyAgent the new energy agent
	 */
	public void setEnergyAgent(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#addPropertyListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyListener(PropertyChangeListener listener) {
		this.getSwingUiModel().addPropertyListener(listener);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#removePropertyListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void removePropertyListener(PropertyChangeListener listener) {
		this.getSwingUiModel().removePropertyListener(listener);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#firePropertyEvent(de.enflexit.ea.ui.SwingUiModel.PropertyEvent)
	 */
	@Override
	public void firePropertyEvent(PropertyEvent event) {
		this.getSwingUiModel().firePropertyEvent(event);
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.ui.SwingUiModelInterface#collectUiData(de.enflexit.ea.ui.SwingUiModel.UiDataCollection)
	 */
	@Override
	public Object collectUiData(UiDataCollection dataType) {
		return this.getSwingUiModel().collectUiData(dataType);
	}
	
	
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setSize(1200, 700);
		this.setIconImage(GlobalInfo.getInternalImageAwbIcon16());
		this.setTitle("State of Energy Agent '" + this.getEnergyAgent().getLocalName() + "' (" + this.getEnergyAgent().getInternalDataModel().getNetworkComponent().getType() + ")");
		this.registerEscapeKeyStroke();
		
		this.getContentPane().setFont(new Font("Dialog", Font.PLAIN, 12));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		this.getContentPane().setLayout(gridBagLayout);
		
		this.setJMenuBar(this.getJMenuBarEnergyAgent());
		
		GridBagConstraints gbc_jTabbedPane = new GridBagConstraints();
		gbc_jTabbedPane.fill = GridBagConstraints.BOTH;
		gbc_jTabbedPane.gridx = 0;
		gbc_jTabbedPane.gridy = 0;
		this.getContentPane().add(this.getJTabbedPane(), gbc_jTabbedPane);
	
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
    
    private JMenuBarEnergyAgent getJMenuBarEnergyAgent() {
    	if (jMenuBarEnergyAgent==null) {
    		jMenuBarEnergyAgent = new JMenuBarEnergyAgent(this);
    	}
    	return jMenuBarEnergyAgent;
    }
    
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			jTabbedPane.setFont(new Font("Dialog", Font.PLAIN, 12));
			jTabbedPane.setBorder(BorderFactory.createEmptyBorder());
			jTabbedPane.addTab(" General Information ", null, getJPanelGeneralInformation(), null);
			jTabbedPane.addTab(" Real-Time Information ", null, getJPanelRealTimeInformation(), null);
			jTabbedPane.addTab(" Planner Information ", null, getJPanelPlannerInformation(), null);
			jTabbedPane.addTab(" Control Assistant ", null, getJPanelPlanningEvents(), null);
		}
		return jTabbedPane;
	}
	public JPanelGeneralInformation getJPanelGeneralInformation() {
		if (jPanelGeneralInformation == null) {
			jPanelGeneralInformation = new JPanelGeneralInformation(this);
		}
		return jPanelGeneralInformation;
	}
	public JPanelRealTimeInformation getJPanelRealTimeInformation() {
		if (jPanelRealTimeInformation == null) {
			jPanelRealTimeInformation = new JPanelRealTimeInformation(this);
		}
		return jPanelRealTimeInformation;
	}
	public JPanelPlannerInformation getJPanelPlannerInformation() {
		if (jPanelPlannerInformation == null) {
			jPanelPlannerInformation = new JPanelPlannerInformation(this);
		}
		return jPanelPlannerInformation;
	}
	public JPanelEomPlanningEventTreeMap getJPanelPlanningEvents() {
		if (jPanelEomPlanningEventTreeMap==null) {
			jPanelEomPlanningEventTreeMap = new JPanelEomPlanningEventTreeMap(this);
		}
		return jPanelEomPlanningEventTreeMap;
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
