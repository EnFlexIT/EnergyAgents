package de.enflexit.ea.ui;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import agentgui.core.config.GlobalInfo;
import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.ea.core.AbstractEnergyAgent;
import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;

/**
 * The Class JDialogEnergyAgent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JDialogEnergyAgent extends JDialog {

	private static final long serialVersionUID = -2867085753299220850L;

	private AbstractEnergyAgent energyAgent;
	
	private JMenuBarEnergyAgent jMenuBarEnergyAgent;
	private JTabbedPane jTabbedPane;
	private JPanelGeneralInformation jPanelGeneralInformation;
	private JPanelRealTimeInformation jPanelRealTimeInformation;
	private JPanelPlannerInformation jPanelPlannerInformation;
	
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
	 * Sets the local energy agent.
	 * @param energyAgent the new energy agent
	 */
	public void setEnergyAgent(AbstractEnergyAgent energyAgent) {
		this.energyAgent = energyAgent;
	}
	/**
	 * Return the local energy agent.
	 * @return the energy agent
	 */
	public AbstractEnergyAgent getEnergyAgent() {
		return energyAgent;
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

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		this.getJMenuBarEnergyAgent().dispose();
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
		}
		return jTabbedPane;
	}
	private JPanelGeneralInformation getJPanelGeneralInformation() {
		if (jPanelGeneralInformation == null) {
			jPanelGeneralInformation = new JPanelGeneralInformation(this);
		}
		return jPanelGeneralInformation;
	}
	private JPanelRealTimeInformation getJPanelRealTimeInformation() {
		if (jPanelRealTimeInformation == null) {
			jPanelRealTimeInformation = new JPanelRealTimeInformation(this);
		}
		return jPanelRealTimeInformation;
	}
	private JPanelPlannerInformation getJPanelPlannerInformation() {
		if (jPanelPlannerInformation == null) {
			jPanelPlannerInformation = new JPanelPlannerInformation(this);
		}
		return jPanelPlannerInformation;
	}

	/**
	 * Update dialog.
	 */
	public void updateDialog() {
		
		this.getJPanelGeneralInformation().updateView();
		this.getJPanelRealTimeInformation().updateView();
		this.getJPanelPlannerInformation().updateView();
	}
	
}
