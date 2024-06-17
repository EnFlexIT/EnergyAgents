package de.enflexit.ea.ui.planning;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.enflexit.common.DateTimeHelper;
import de.enflexit.common.swing.OwnerDetection;
import de.enflexit.common.swing.WindowSizeAndPostionController;
import de.enflexit.common.swing.WindowSizeAndPostionController.JDialogPosition;
import de.enflexit.ea.core.planning.Planner;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModelInterface;
import energy.GlobalInfo;
import energy.OptionModelController;
import energy.helper.EvaluationTimeRangeHelper;
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
public class ManualPlanningHandler extends Planner implements PropertyChangeListener {

	private SwingUiModelInterface swingUiModelInterface;
	
	private Window planningWindow;
	private WindowAdapter planningFrameAdapter;

	private boolean isDisposed;
	
	/**
	 * Instantiates a new manual planning handler.
	 * @param swingUiModelInterface the current SwingUiModelInterface
	 */
	public ManualPlanningHandler(SwingUiModelInterface swingUiModelInterface) {
		super(swingUiModelInterface.getEnergyAgent(), "ManualPlanner");
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
	}

	/**
	 * Checks if this ManualPlanningHandler was disposed.
	 * @return true, if is disposed
	 */
	public boolean isDisposed() {
		return isDisposed;
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

		// --- UI is already open=> focus it ----------------------------------
		if (this.getPlanningWindow()!=null) {
			this.getPlanningWindow().toFront();
			this.getPlanningWindow().requestFocus();
			return;
		}
		
		String agentName = this.getEnergyAgent().getLocalName();
		Window ownerWindow = OwnerDetection.getOwnerWindowForComponent((Component)this.swingUiModelInterface);
		
		switch (this.getControlledSystemType()) {
		case None:
			// --- Nothing to plan --------------------------------------------
			String msg = "Energy Agent '" + agentName + "' does not control any system and thus allows no manual planning!";
			JOptionPane.showMessageDialog(ownerWindow, msg, "Manual Planning for " + agentName, JOptionPane.WARNING_MESSAGE);
			break;
			
		case TechnicalSystem:
			// --- A TechnicalSystem under control ----------------------------
			this.adjustEvaluationTimeRange();
			this.getOptionModelController().getEvaluationProcess().addEvaluationProcessListener(this);
			// --- Create and show UI -----------------------------------------
			ApplicationDialog appDialog = new ApplicationDialog(ownerWindow, this.getOptionModelController(), MainPanelView.EvaluationView);
			appDialog.setTitle("Planning for TechnicalSystem controlled by agent '" + agentName + "'");
			this.setPlanningWindow(appDialog);
			break;
			
		case TechnicalSystemGroup:
			// --- A TechnicalSystemGroup under control -----------------------
			this.adjustEvaluationTimeRange();
			this.getGroupController().getGroupOptionModelController().getEvaluationProcess().addEvaluationProcessListener(this);
			// --- Create and show UI -----------------------------------------		
			GroupApplicationDialog groupAppDialog = new GroupApplicationDialog(ownerWindow, this.getGroupController(), MainPanelView.EvaluationView);
			groupAppDialog.setTitle("Planning for TechnicalSystemGroup controlled by agent '" + agentName + "'");
			this.setPlanningWindow(groupAppDialog);
			// --- Set GroupTree to required style ----------------------------
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ManualPlanningHandler.this.getGroupController().setChangedAndNotifyObservers(new GroupNotification(Reason.GroupTreeViewChanged, GroupNodeCaptionStyle.Both));
				}
			});
			break;
		}
		
		// --- Adjust dialog size and position --------------------------------
		if (this.getPlanningWindow()!=null && ownerWindow!=null) {
			double scale = 0.75;
			Integer newWidth  = (int)(ownerWindow.getWidth()  * scale); 
			Integer newHeigth = (int)(ownerWindow.getHeight() * scale);
			// --- Check against screen size to avoid that view is to small ---
			Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
			if (newWidth <= (screenDimension.getWidth() * scale)) {
				newWidth  = (int) (screenDimension.getWidth()  * scale);
				newHeigth = (int) (screenDimension.getHeight() * scale);
			}
			this.getPlanningWindow().setSize(newWidth, newHeigth);
			WindowSizeAndPostionController.setJDialogPositionOnScreen(this.getPlanningWindow(), JDialogPosition.ScreenCenter);
		}
	}
	/**
	 * Will adjust the evaluation time range according to the current time or the previous RT-planning end time.
	 */
	private void adjustEvaluationTimeRange() {
		
		boolean isDebug = false;
		
		// --- Get current time settings --------------------------------------
		long currTime = this.getEnergyAgent().getEnergyAgentIO().getTime();
		long currEvalStartTime = this.getCurrentOptionModelController().getEvaluationProcess().getStartTime();
		long currEvalEndTime = this.getCurrentOptionModelController().getEvaluationProcess().getEndTime();
		long currEvalTimeDiff = currEvalEndTime - currEvalStartTime;
		
		// --- Is there something to do? --------------------------------------
		if (currEvalStartTime > currTime) return;
		
		// --- Calculate new start time ---------------------------------------
		long newStartTime = this.getNewStartTime(currTime, currEvalStartTime);
		if (this.getEnergyAgent().getPlanningInformation().getRealTimePlannerResult()!=null) {
			// --- Get current real time planning end time --------------------
			long rtPlannerEndTime = this.getEnergyAgent().getPlanningInformation().getRealTimePlannerResult().getStopTime();
			if (rtPlannerEndTime > currTime) {
				newStartTime = rtPlannerEndTime;
			}
		}
		long newEndTime = newStartTime + currEvalTimeDiff;
		
		if (isDebug==true) {
			String currTimeString = DateTimeHelper.getDateTimeAsString(currTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			String currEvalStartTimeString = DateTimeHelper.getDateTimeAsString(currEvalStartTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			String newTimeString = DateTimeHelper.getDateTimeAsString(newStartTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			System.err.println("[" + this.getClass().getSimpleName() + "] CurrTime: " + currTimeString + ", Eval Start: " + currEvalStartTimeString + ", New Time: " + newTimeString);
		}
		
		// --- Apply evaluation time range to current system ------------------
		switch (this.getControlledSystemType()) {
		case TechnicalSystem:
			EvaluationTimeRangeHelper.adjustEvaluationTimeForTechnicalSystem(this.getOptionModelController().getTechnicalSystem(), newStartTime, newEndTime, null);
			break;
			
		case TechnicalSystemGroup:
			EvaluationTimeRangeHelper.adjustEvaluationTimeForTechnicalSystemGroup(this.getGroupController().getTechnicalSystemGroup(), newStartTime, newEndTime, null);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Return a new start time.
	 *
	 * @param currTime the current time
	 * @param currEvalStartTime the current evaluation start time
	 * @return the new start time
	 */
	private long getNewStartTime(long currTime, long currEvalStartTime) {
		
		boolean isDebug = false;
		long newTime = 0;
		
		// --- Move time frame ahead current time -----------------------------
		long moveTimeWith = DateTimeHelper.MILLISECONDS_FOR_MONTH_30; // --- Month = default ---
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(GlobalInfo.getInstance().getZoneIdOfApplication()));
		cal.setTime(new Date(currEvalStartTime));
		if (cal.get(Calendar.MILLISECOND)!=0) {
			moveTimeWith = cal.get(Calendar.MILLISECOND);
		} else if (cal.get(Calendar.SECOND)!=0) {
			moveTimeWith = DateTimeHelper.MILLISECONDS_FOR_SECOND * cal.get(Calendar.SECOND);
		} else if (cal.get(Calendar.MINUTE)!=0) {
			moveTimeWith = DateTimeHelper.MILLISECONDS_FOR_MINUTE * cal.get(Calendar.MINUTE);			
		} else if (cal.get(Calendar.HOUR)!=0) {
			moveTimeWith = DateTimeHelper.MILLISECONDS_FOR_HOUR;
		}
		
		// --- Calculate new time ---------------------------------------------
		long offsetFactor = (currTime - currEvalStartTime) / moveTimeWith; 
		newTime = currEvalStartTime + (offsetFactor * moveTimeWith); 
		while (newTime<=currTime) {
			newTime += moveTimeWith;	
		}
		if (isDebug==true) {
			String currTimeString = DateTimeHelper.getDateTimeAsString(currTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			String currEvalStartTimeString = DateTimeHelper.getDateTimeAsString(currEvalStartTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			String newTimeString = DateTimeHelper.getDateTimeAsString(newTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			System.err.println("[" + this.getClass().getSimpleName() + "] CurrTime: " + currTimeString + ", Eval Start: " + currEvalStartTimeString + ", New Time: " + newTimeString);
		}
		return newTime;
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
			this.swingUiModelInterface.firePropertyEvent(PropertyEvent.UpdateView);
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getNewValue() instanceof PropertyEvent == false) return;
		PropertyEvent pe = (PropertyEvent) evt.getNewValue();
		switch (pe) {
		case CloseView:
			if (this.getPlanningWindow()!=null) {
				this.getPlanningWindow().setVisible(false);
				this.getPlanningWindow().dispose();
			}
			break;

		default:
			break;
		}
		
		
		
	}
	
}
