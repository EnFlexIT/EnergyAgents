package de.enflexit.ea.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.ui.planning.ManualPlanningHandler;
import de.enflexit.ea.ui.planning.ManualPlanningRealTimeSelection;

/**
 * The Class JMenuBarEnergyAgent.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JMenuBarEnergyAgent extends JMenuBar implements ActionListener {

	private static final long serialVersionUID = -5873831194278618L;

	private JDialogEnergyAgent jDialogEnergyAgent;
	
	private JMenu jMenuGeneralSettings;
		private JMenuItem jMenuItemUpdateUI;
		
	private JMenu jMenuRealTimeSettings;
	
	private JMenu jMenuPlanningTimeSettings;
		private JMenuItem jMenuItemManualPlanning;
		private JMenuItem jMenuItemManualPlanningRealTimeSelection;
	
	// --- From here handler for specific tasks --------------- 
	private ManualPlanningHandler manualPlanningHandler;
	
	
	/**
	 * Instantiates a new JMenuBarEnergyAgent.
	 * @param energyAgent the energy agent
	 */
	public JMenuBarEnergyAgent(JDialogEnergyAgent jDialogEnergyAgent) {
		this.jDialogEnergyAgent = jDialogEnergyAgent;
		this.initialize();
	}
	/**
	 * Return the local energy agent.
	 * @return the energy agent
	 */
	public AbstractEnergyAgent getEnergyAgent() {
		return this.jDialogEnergyAgent.getEnergyAgent();
	}
	
	/**
	 * Initializes this menu bar.
	 */
	private void initialize() {
		if (this.getJMenuGeneralSettings().getMenuComponentCount()>0)  this.add(this.getJMenuGeneralSettings());
		if (this.getJMenuRealTimeSettings().getMenuComponentCount()>0) this.add(this.getJMenuRealTimeSettings());
		if (this.getJMenuPlanningSettings().getMenuComponentCount()>0) this.add(this.getJMenuPlanningSettings());
	}
	
	
	// --------------------------------------------------------------
	// --- From here, GeneralSettings -------------------------------
	// --------------------------------------------------------------
	private JMenu getJMenuGeneralSettings() {
		if (jMenuGeneralSettings==null) {
			jMenuGeneralSettings = new JMenu("General Settings");
			jMenuGeneralSettings.add(this.getJMenuItemUpdateUI());
		}
		return jMenuGeneralSettings;
	}
	private JMenuItem getJMenuItemUpdateUI() {
		if (jMenuItemUpdateUI==null) {
			jMenuItemUpdateUI = new JMenuItem("Refresh View");
			jMenuItemUpdateUI.addActionListener(this);
		}
		return jMenuItemUpdateUI;
	}
	
	
	// --------------------------------------------------------------
	// --- From here, Real-Time-Settings ----------------------------
	// --------------------------------------------------------------
	private JMenu getJMenuRealTimeSettings() {
		if (jMenuRealTimeSettings==null) {
			jMenuRealTimeSettings = new JMenu("Real-Time Tools");
		}
		return jMenuRealTimeSettings;
	}

	
	// --------------------------------------------------------------
	// --- From here, Planning-Settings -----------------------------
	// --------------------------------------------------------------
	private JMenu getJMenuPlanningSettings() {
		if (jMenuPlanningTimeSettings==null) {
			jMenuPlanningTimeSettings = new JMenu("Planning Tools");
			jMenuPlanningTimeSettings.add(this.getJMenuItemManualPlanning());
			jMenuPlanningTimeSettings.add(this.getJMenuItemManualPlanningRealTimeSelection());
		}
		return jMenuPlanningTimeSettings;
	}
	
	private JMenuItem getJMenuItemManualPlanning() {
		if (jMenuItemManualPlanning==null) {
			jMenuItemManualPlanning = new JMenuItem("Manual Planning ...");
			jMenuItemManualPlanning.addActionListener(this);
		}
		return jMenuItemManualPlanning;
	}
	
	private JMenuItem getJMenuItemManualPlanningRealTimeSelection() {
		if (jMenuItemManualPlanningRealTimeSelection==null) {
			jMenuItemManualPlanningRealTimeSelection = new JMenuItem("Take current plan for real-time execution");
			jMenuItemManualPlanningRealTimeSelection.addActionListener(this);
		}
		return jMenuItemManualPlanningRealTimeSelection;
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		if (ae.getSource()==this.getJMenuItemUpdateUI()) {
			this.jDialogEnergyAgent.updateView();
			
		} else if (ae.getSource()==this.getJMenuItemManualPlanning()) {
			// --- Execute a manual planning process --------------------------
			this.getManualPlanningHandler().openOrFocusManualPlanning();
		} else if (ae.getSource()==this.getJMenuItemManualPlanningRealTimeSelection()) {
			// --- Take current plan selection for real-time execution --------
			new ManualPlanningRealTimeSelection(this.jDialogEnergyAgent).setSelectedPlanForRealTimeExecution();
		}
	}
	
	/**
	 * Returns the handler for manual planning.
	 * @return the manual planning handler
	 */
	private ManualPlanningHandler getManualPlanningHandler() {
		if (manualPlanningHandler==null || manualPlanningHandler.isDisposed()==true) {
			manualPlanningHandler = new ManualPlanningHandler(this.jDialogEnergyAgent);
		}
		return manualPlanningHandler;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		
		if (this.manualPlanningHandler!=null) this.manualPlanningHandler.dispose();
		this.manualPlanningHandler = null;
	}
	
}
