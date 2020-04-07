package de.enflexit.energyAgent.ops.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.energyAgent.ops.OpsController;
import de.enflexit.energyAgent.ops.OpsControllerEvent;
import de.enflexit.energyAgent.ops.OpsControllerListener;
import de.enflexit.energyAgent.ops.plugin.OpsPlugin;

/**
 * The Class JFrameOpsControl provides the graphical representation
 * for the Agent.HyGrid OPS.
 *
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class JFrameOpsControl extends JFrame implements OpsControllerListener {

	private static final long serialVersionUID = 3894964016475228766L;
	
	private OpsController opsController;
	
	private JFrameOpsControlTools jFrameOpsControlTools;
	private JFrameOpsControlStatusBar jFrameOpsControlStatusBar;
	
	private JTabbedPane jTabbedPaneOpsContent;
	
	/**
	 * Instantiates a new JFrame for the OPS control.
	 * @param opsController the required OPS controller
	 */
	public JFrameOpsControl(OpsController opsController) {
		if (opsController==null) {
			throw new NullPointerException("The OPS controller is not allowed to be null!");
		}
		this.opsController = opsController;
		this.opsController.addOpsControllerListener(this);
		this.initialize();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setAutoRequestFocus(true);
		this.setTitle(OpsPlugin.BUNDLE_TITLE);

		// --- Set the frame to 80% of the screen size --------------
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) Math.ceil(screenSize.getWidth()*0.8);
		int height = (int) Math.ceil(screenSize.getHeight()*0.8);
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		
		this.setJMenuBar(this.getJFrameOpsControlTools().getJMenuBarOpsControl());
		this.getContentPane().add(this.getJFrameOpsControlTools().getJToolBarOpsControl(), BorderLayout.NORTH);
		this.getContentPane().add(this.getjJFrameOpsControlStatusBar(), BorderLayout.SOUTH);
		this.getContentPane().add(this.getjTabbedPaneOpsContent());
		
	}
	
	/**
	 * Returns the JFrame for the OPS control tools (menu and toolbar).
	 * @return the JFrameOpsControlTools
	 */
	private JFrameOpsControlTools getJFrameOpsControlTools() {
		if (jFrameOpsControlTools==null) {
			jFrameOpsControlTools = new JFrameOpsControlTools(this.opsController);
		}
		return jFrameOpsControlTools;
	}
	/**
	 * Returns the JFrame for the OPS control status bar.
	 * @return the JFrameOpsControlStatusBar
	 */
	private JFrameOpsControlStatusBar getjJFrameOpsControlStatusBar() {
		if (jFrameOpsControlStatusBar==null) {
			jFrameOpsControlStatusBar = new JFrameOpsControlStatusBar(this.opsController);
		}
		return jFrameOpsControlStatusBar;
	}
	
	public JTabbedPane getjTabbedPaneOpsContent() {
		if (jTabbedPaneOpsContent==null) {
			jTabbedPaneOpsContent = new JTabbedPane();
			jTabbedPaneOpsContent.setUI(new AwbBasicTabbedPaneUI());
		}
		return jTabbedPaneOpsContent;
	}
	
	
	/**
	 * Sets the status text in the status bar.
	 * @param newStatusText the new status text
	 */
	public void setStatusText(String newStatusText) {
		this.getjJFrameOpsControlStatusBar().setStatusText(newStatusText);
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
			break;
		}
	}
	
}

