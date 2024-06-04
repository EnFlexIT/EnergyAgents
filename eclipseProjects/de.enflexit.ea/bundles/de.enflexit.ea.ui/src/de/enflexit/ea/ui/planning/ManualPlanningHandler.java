package de.enflexit.ea.ui.planning;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.ea.core.planning.Planner;
import de.enflexit.ea.ui.JDialogEnergyAgent;
import energy.OptionModelController;
import energy.optionModel.gui.ApplicationDialog;
import energy.optionModel.gui.ApplicationFrame;
import energy.optionModel.gui.MainPanel.MainPanelView;
import energy.planning.EomPlannerEvent;
import energy.planning.EomPlannerEvent.PlannerEventType;
import energygroup.GroupController;
import energygroup.GroupNotification;
import energygroup.GroupNotification.Reason;
import energygroup.gui.GroupApplicationDialog;
import energygroup.gui.GroupApplicationFrame;
import energygroup.gui.GroupTree.GroupNodeCaptionStyle;

/**
 * The Class ManualPlanningHandler.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ManualPlanningHandler extends Planner {

	private JDialogEnergyAgent jDialogEnergyAgent;
	
	private Window planningWindow;
	private WindowAdapter planningFrameAdapter;

	private boolean isDisposed;
	
	/**
	 * Instantiates a new manual planning handler.
	 * @param jDialogEnergyAgent the parent JDialogEnergyAgent
	 */
	public ManualPlanningHandler(JDialogEnergyAgent jDialogEnergyAgent) {
		super(jDialogEnergyAgent.getEnergyAgent(), "ManualPlanner");
		this.jDialogEnergyAgent = jDialogEnergyAgent;
	}

	/**
	 * Checks if this ManualPlanningHandler was disposed.
	 * @return true, if is disposed
	 */
	public boolean isDisposed() {
		return isDisposed;
	}
	/**
	 * Dispose.
	 */
	public void dispose() {
		if (this.getPlanningWindow()!=null) {
			this.getPlanningWindow().setVisible(false);
			this.getPlanningWindow().dispose();
			return;
		}
	}
	
	/**
	 * Returns the planning frame.
	 * @return the planning frame
	 */
	private Window getPlanningWindow() {
		return planningWindow;
	}
	/**
	 * Sets the planning frame.
	 * @param planningFrame the new planning frame
	 */
	private void setPlanningWindow(Window planningFrame) {
		if (this.planningWindow!=null) {
			this.planningWindow.removeWindowListener(this.getPlanningFrameAdapter());
		}
		this.planningWindow = planningFrame;
		if (this.planningWindow!=null) {
			this.planningWindow.addWindowListener(this.getPlanningFrameAdapter());
		}
	}
	/**
	 * Returns the local planning frame {@link WindowListener}.
	 * @return the planning frame adapter
	 */
	private WindowAdapter getPlanningFrameAdapter() {
		if (planningFrameAdapter==null) {
			planningFrameAdapter = new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					// --- Remove EvaluationProcessListener first -------------
					Window pDialog = we.getWindow();
					if (pDialog instanceof ApplicationFrame) {
						OptionModelController omc = ((ApplicationFrame)pDialog).getOptionModelController();
						omc.getEvaluationProcess().removeEvaluationProcessListener(ManualPlanningHandler.this);
						
					} else if (pDialog instanceof GroupApplicationFrame) {
						GroupController gc = ((GroupApplicationFrame)pDialog).getGroupController();
						gc.getGroupOptionModelController().getEvaluationProcess().removeEvaluationProcessListener(ManualPlanningHandler.this);
					}
					// --- Reset local variables ------------------------------
					ManualPlanningHandler.this.setPlanningWindow(null);
					ManualPlanningHandler.this.isDisposed = true;
				}
			};
		}
		return planningFrameAdapter;
	}
	
	/**
	 * Opens the manual planning perspective.
	 */
	public void openOrFocusManualPlanning() {

		// --- UI is already open=> focus it ------------------------
		if (this.getPlanningWindow()!=null) {
			this.getPlanningWindow().toFront();
			this.getPlanningWindow().requestFocus();
			return;
		}
		
		String agentName = this.getEnergyAgent().getLocalName();
		Window ownerWindow = this.jDialogEnergyAgent.getOwner();
		
		switch (this.getControlledSystemType()) {
		case None:
			// --- Nothing to plan ------------------------
			String msg = "Energy Agent '" + agentName + "' does not control any system and thus allows no manual planning!";
			JOptionPane.showMessageDialog(ownerWindow, msg, "Manual Planning for " + agentName, JOptionPane.WARNING_MESSAGE);
			break;
			
		case TechnicalSystem:
			// --- A TechnicalSystem under control --------
			this.getOptionModelController().getEvaluationProcess().addEvaluationProcessListener(this);
			// --- Create and show UI ---------------------
			ApplicationDialog appDialog = new ApplicationDialog(ownerWindow, this.getOptionModelController(), MainPanelView.EvaluationView);
			appDialog.setTitle("Planning for TechnicalSystem controlled by agent '" + agentName + "'");
			this.setPlanningWindow(appDialog);
			break;
			
		case TechnicalSystemGroup:
			// --- A TechnicalSystemGroup under control ---
			this.getGroupController().getGroupOptionModelController().getEvaluationProcess().addEvaluationProcessListener(this);
			// --- Create and show UI ---------------------			
			GroupApplicationDialog groupAppDialog = new GroupApplicationDialog(ownerWindow, this.getGroupController(), MainPanelView.EvaluationView);
			groupAppDialog.setTitle("Planning for TechnicalSystemGroup controlled by agent '" + agentName + "'");
			this.setPlanningWindow(groupAppDialog);
			// --- Set GroupTree to required style -------- 
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ManualPlanningHandler.this.getGroupController().setChangedAndNotifyObservers(new GroupNotification(Reason.GroupTreeViewChanged, GroupNodeCaptionStyle.Both));
				}
			});
			break;
		}
		
		// --- Adjust dialog size and position ------------
		if (this.getPlanningWindow()!=null && ownerWindow!=null) {
			Integer newWidth  = (int)(ownerWindow.getWidth() * 0.75); 
			Integer newHeigth = (int)(ownerWindow.getHeight() * 0.75);
			this.getPlanningWindow().setSize(newWidth, newHeigth);
			WindowSizeAndPostionController.setJDialogPositionOnScreen(this.getPlanningWindow(), JDialogPosition.ScreenTopLeft);
		}
	}

	// ----------------------------------------------------	
	// --- From here, EvaluationProcessListener handling --
	// ----------------------------------------------------
	/* (non-Javadoc)
	 * @see energy.evaluation.EvaluationProcessListener#setEvaluationExecuted(boolean)
	 */
	@Override
	public void setEvaluationExecuted(boolean isExecuted) {

		// --- React on finalized planning calls ------------------------------ 
		if (isExecuted==false) {
			EomPlannerEvent planerEvent = new EomPlannerEvent(PlannerEventType.PlanningFinalized, this);
			this.getEnergyAgent().getPlanningDispatcherManager().onPlannerEvent(planerEvent);
			this.jDialogEnergyAgent.updateView();
		}
	}
	
}
