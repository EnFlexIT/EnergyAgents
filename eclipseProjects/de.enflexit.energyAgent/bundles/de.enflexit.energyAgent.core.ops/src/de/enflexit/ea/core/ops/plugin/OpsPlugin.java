package de.enflexit.ea.core.ops.plugin;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import agentgui.core.application.Application;
import agentgui.core.config.GlobalInfo.ExecutionMode;
import agentgui.core.plugin.PlugIn;
import agentgui.core.project.Project;
import de.enflexit.ea.core.ops.OpsController;
import de.enflexit.ea.core.ops.gui.ImageHelper;
import de.enflexit.ea.core.ops.gui.JFrameOpsControl;

/**
 * The Class OpsPlugin configures the visualization to start the OPS functionalities.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class OpsPlugin extends PlugIn implements ActionListener {

	public static final String BUNDLE_TITLE = "Agent.HyGrid - OPS";
	
	private JButton jButtonOpsControl;
	
	/**
	 * Instantiates a new OPS plugin.
	 * @param currProject the current project
	 */
	public OpsPlugin(Project currProject) {
		super(currProject);
	}
	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#getName()
	 */
	@Override
	public String getName() {
		return BUNDLE_TITLE + " PLugin";
	}

	/* (non-Javadoc)
	 * @see agentgui.core.plugin.PlugIn#onPlugIn()
	 */
	@Override
	public void onPlugIn() {

		// --- Do nothing in case of non-Application ----------------
		if (Application.getGlobalInfo().getExecutionMode()==ExecutionMode.APPLICATION) {
			// --- Add the button(s) to the Main Window -------------
			this.addJToolbarComponent(this.getjButtonOpsControl());
			this.addJToolbarComponent(new JToolBar.Separator());
			super.onPlugIn();
		}
	}

	/**
	 * Returns the JButton for the OPS control.
	 * @return the JButton ops control
	 */
	private JButton getjButtonOpsControl() {
		if (jButtonOpsControl==null) {
			jButtonOpsControl = new JButton("Agent.HyGrid - OPS");
			jButtonOpsControl.setFont(new Font("Dialog", Font.BOLD, 12));
			jButtonOpsControl.setIcon(ImageHelper.getInternalImageIcon("gear.png"));
			jButtonOpsControl.addActionListener(this);
		}
		return jButtonOpsControl;
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource()==this.getjButtonOpsControl()) {
			// --- Open/Show the OPS controller ---------------------
			JFrameOpsControl opsControl = OpsController.getInstance().getJFrameOpsControl();
			if (opsControl.isVisible()==true) {
				if (opsControl.getState()==Frame.ICONIFIED) {
					opsControl.setState(Frame.NORMAL);
				}
				opsControl.requestFocus();
			} else {
				opsControl.setVisible(true);
			}
			
		}
		
	}
	
}
