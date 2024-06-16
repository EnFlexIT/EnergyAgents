package de.enflexit.ea.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;

import de.enflexit.common.DateTimeHelper;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.planning.AbstractPlanningDispatcherManager;
import de.enflexit.ea.ui.SwingUiFocusDescription.FocusTo;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;
import de.enflexit.ea.ui.planning.ManualPlanningHandler;
import energy.GlobalInfo;
import energy.planning.EomPlannerResult;
import energy.planning.events.EomPlanningEvent;

/**
 * The Class JToolBarEnergAgent.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JToolBarEnergAgent extends JToolBar implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 7657693209073381023L;

	public enum LabelColor {
		Green (new Color(0, 127, 14)),
		Yellow(new Color(255, 255, 0)),
		Red(new Color(216, 0, 0));
		
		private Color color;
		
		private LabelColor(final Color color) {
			this.color = color;
		}
		public Color getColor() {
			return color;
		}
	}
	
	private SwingUiModelInterface swingUiModelInterface;
	private JButton jButtonRefresh;
	private JButton jButtonPlanning;
	private JButton jButtonPlanSelection;
	
	private ManualPlanningHandler manualPlanningHandler;
	
	private JLabel jLabelTimeCaption;
	private JLabel jLabelTime;
	private JLabel jLabelEventCaption;
	private JLabel jLabelEvent;
	private JButton jButtonNextEvent;
	
	private Timer timer;
	private boolean isStopTimer;
	
	
	/**
	 * Instantiates a new JToolBarEnergAgent.
	 * @param swingUiModelInterface the swing ui model interface
	 */
	public JToolBarEnergAgent(SwingUiModelInterface swingUiModelInterface) {
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
		this.initialize();
	}
	
	/**
	 * Returns the current energy agent instance.
	 * @return the energy agent
	 */
	private AbstractEnergyAgent getEnergyAgent() {
		return JToolBarEnergAgent.this.swingUiModelInterface.getEnergyAgent();
	}
	
	/**
	 * Initialize.
	 */
	private void initialize() {
		
		this.setFloatable(false);
		this.setRollover(true);
		
		this.add(this.getJButtonRefresh());
		this.addSeparator();
		
		this.add(this.getJButtonPlanning());
		this.add(this.getJButtonPlanSelection());
		this.addSeparator();
		
		this.add(this.getJLabelTimeCaption());
		this.add(this.getJLabelTime());
		this.addSeparator();
		this.add(this.getJLabelEventCaption());
		this.add(this.getJLabelEvent());
		this.addSeparator();
		this.add(this.getJButtonShowNextEvent());
		
		this.addSeparator();
		this.setTime();
	}
	
	private JButton getJButtonRefresh() {
		if (jButtonRefresh==null) {
			jButtonRefresh = new JButton();
			jButtonRefresh.setIcon(BundleHelper.getImageIcon("Refresh.png"));
			jButtonRefresh.setToolTipText("RefreshView");
			jButtonRefresh.addActionListener(this);
		}
		return jButtonRefresh;
	}
	
	private JButton getJButtonPlanning() {
		if (jButtonPlanning==null) {
			jButtonPlanning = new JButton();
			jButtonPlanning.setIcon(BundleHelper.getImageIcon("Planning.png"));
			jButtonPlanning.setToolTipText("Manual Planning ...");
			jButtonPlanning.addActionListener(this);
		}
		return jButtonPlanning;
	}
	private JButton getJButtonPlanSelection() {
		if (jButtonPlanSelection==null) {
			jButtonPlanSelection = new JButton();
			jButtonPlanSelection.setIcon(BundleHelper.getImageIcon("PlanSelection.png"));
			jButtonPlanSelection.setToolTipText("Take current plan for real-time execution");
			jButtonPlanSelection.addActionListener(this);
		}
		return jButtonPlanSelection;
	}
	
	private JButton getJButtonShowNextEvent() {
		if (jButtonNextEvent==null) {
			jButtonNextEvent = new JButton();
			jButtonNextEvent.setIcon(BundleHelper.getImageIcon("Search.png"));
			jButtonNextEvent.setToolTipText("Show Next Planner Event");
			jButtonNextEvent.addActionListener(this);
		}
		return jButtonNextEvent;
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource()==this.getJButtonRefresh()) {
			this.swingUiModelInterface.firePropertyEvent(PropertyEvent.UpdateView);
		
		} else if (ae.getSource()==this.getJButtonPlanning()) {
			// --- Execute a manual planning process --------------------------
			this.getManualPlanningHandler().openOrFocusManualPlanning();
			
		} else if (ae.getSource()==this.getJButtonPlanSelection()) {
			// --- Take current plan selection for real-time execution --------
			this.setSelectedPlanForRealTimeExecution();
			
		} else if (ae.getSource()==this.getJButtonShowNextEvent()) {
			// --- Show next planning event -----------------------------------
			this.swingUiModelInterface.fireFocusEvent(new SwingUiFocusDescription(FocusTo.Tab, JPanelEnergyAgent.TAB_TITLE_CONTROL_ASSISTANT));
			this.swingUiModelInterface.fireFocusEvent(new SwingUiFocusDescription(FocusTo.NextPlanningEvent, null));
			
		}
	}
	/**
	 * Returns the handler for manual planning.
	 * @return the manual planning handler
	 */
	private ManualPlanningHandler getManualPlanningHandler() {
		if (manualPlanningHandler==null || manualPlanningHandler.isDisposed()==true) {
			manualPlanningHandler = new ManualPlanningHandler(this.swingUiModelInterface);
		}
		return manualPlanningHandler;
	}
	
	/**
	 * Opens the manual planning perspective.
	 */
	private void setSelectedPlanForRealTimeExecution() {

		AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> pdm = this.swingUiModelInterface.getEnergyAgent().getPlanningDispatcherManager();
		if (pdm!=null) {
			// --- Get the selection as EomPlannerResult ------------ 
			EomPlannerResult eomPlannerResult = (EomPlannerResult) this.swingUiModelInterface.collectUiData(UiDataCollection.PlannerResultAsSelected);
			if (eomPlannerResult==null) return;
			
			EomPlannerResult rtPlannerResult = pdm.getPlannerResultForRealTimeExecution();
			rtPlannerResult.append(eomPlannerResult);
			this.swingUiModelInterface.firePropertyEvent(PropertyEvent.UpdateView);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getNewValue() instanceof UiDataCollection) return;
		
		PropertyEvent pe = (PropertyEvent) evt.getNewValue();
		if (pe==PropertyEvent.CloseView) {
			this.isStopTimer = true;
		}
	}
	/**
	 * Returns the JLabel for the time.
	 * @return the j label time
	 */
	private JLabel getJLabelTimeCaption() {
		if (jLabelTimeCaption==null) {
			jLabelTimeCaption = new JLabel("Time: ");
			jLabelTimeCaption.setFont(new Font("Dialog", Font.BOLD, 14));
		}
		return jLabelTimeCaption;
	}
	/**
	 * Returns the JLabel for the time.
	 * @return the j label time
	 */
	private JLabel getJLabelTime() {
		if (jLabelTime==null) {
			jLabelTime = new JLabel();
			jLabelTime.setFont(new Font("Dialog", Font.BOLD, 14));
		}
		return jLabelTime;
	}
	
	/**
	 * Returns the JLabel for the time.
	 * @return the j label time
	 */
	private JLabel getJLabelEventCaption() {
		if (jLabelEventCaption==null) {
			jLabelEventCaption = new JLabel("Next Event: ");
			jLabelEventCaption.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabelEventCaption.setForeground(LabelColor.Green.color);
		}
		return jLabelEventCaption;
	}
	/**
	 * Returns the JLabel for the time.
	 * @return the j label time
	 */
	private JLabel getJLabelEvent() {
		if (jLabelEvent==null) {
			jLabelEvent = new JLabel();
			jLabelEvent.setFont(new Font("Dialog", Font.BOLD, 14));
			jLabelEvent.setForeground(LabelColor.Green.color);
		}
		return jLabelEvent;
	}
	
	
	/**
	 * Returns the local swing {@link Timer}
	 * @return the timer
	 */
	private Timer getTimer() {
		if (timer==null) {
			timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToolBarEnergAgent.this.setTime();
				}
			});
			timer.setRepeats(true);
		}
		return timer;
	}
	/**
	 * Sets the current time to the display.
	 */
	private void setTime() {
		
		if (this.isStopTimer==true) {
			this.getTimer().stop();
			return;
		}
		
		// --- Adjust the new time delay ------------------
		long currTime = getEnergyAgent().getEnergyAgentIO().getTime();
		long nextTimerEvent = (((currTime / 1000) + 1) * 1000) + 10; 
		long delayToNextEvent = nextTimerEvent - currTime;
		this.getTimer().setDelay((int)delayToNextEvent);
		this.getTimer().restart();
		
		// --- Set Time to display ------------------------
		String timeString = DateTimeHelper.getDateTimeAsString(currTime, DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
		this.getJLabelTime().setText(timeString);
		
		// --- Get time for next planning event -----------
		EomPlanningEvent nextEvent = (EomPlanningEvent) this.swingUiModelInterface.collectUiData(UiDataCollection.NextPlannerEvent);
		if (nextEvent==null) {
			this.getJLabelEvent().setText("No plan event found!");
			this.getJLabelEvent().setForeground(LabelColor.Yellow.getColor());
			this.getJLabelEventCaption().setForeground(LabelColor.Yellow.getColor());
			
		} else {
			timeString = DateTimeHelper.getDateTimeAsString(nextEvent.getTime(), DateTimeHelper.DEFAULT_TIME_FORMAT_PATTERN, GlobalInfo.getInstance().getZoneIdOfApplication());
			this.getJLabelEvent().setText(timeString);
			// --- Adjust label color ---------------------
			long timeDiffSec = (nextEvent.getTime() - currTime) / 1000;
			if (timeDiffSec<=10) {
				this.getJLabelEvent().setForeground(LabelColor.Red.getColor());
				this.getJLabelEventCaption().setForeground(LabelColor.Red.getColor());
			} else if (timeDiffSec<30) {
				this.getJLabelEvent().setForeground(LabelColor.Yellow.getColor());
				this.getJLabelEventCaption().setForeground(LabelColor.Yellow.getColor());
			} else {
				this.getJLabelEvent().setForeground(LabelColor.Green.getColor());
				this.getJLabelEventCaption().setForeground(LabelColor.Green.getColor());
			}
		}
		
	}

}
