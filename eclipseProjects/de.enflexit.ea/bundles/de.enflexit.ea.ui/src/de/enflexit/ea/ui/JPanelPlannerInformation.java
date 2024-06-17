package de.enflexit.ea.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.common.swing.OwnerDetection;
import de.enflexit.ea.core.ui.PlanningInformation;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;
import energy.planning.EomPlannerResult;
import energy.planning.ui.EomPlannerResultPanel;

/**
 * The Class JPanelPlannerInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelPlannerInformation extends JTabbedPane implements PropertyChangeListener {

	private static final long serialVersionUID = -2222117009782469384L;
	
	private static final String TAB_HEADER_REAL_TIME_PLANNING = "Real Time Planner Result";
	
	private SwingUiModelInterface swingUiModelInterface;
	private PropertiesPanel jPanelPlanningProperties;
	
	private boolean isPauseViewUpdater;
	
	/**
	 * Instantiates a new j panel real time information.
	 * @param jDialogEnergyAgent the j dialog energy agent
	 */
	public JPanelPlannerInformation(SwingUiModelInterface swingUiModelInterface) {
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
		this.initialize();
		this.setDisplayInformation();
	}
	
	/**
	 * Initializes this panel JTabbed Pane.
	 */
	private void initialize() {
		this.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.addTab(" Settings ", this.getJPanelPlanningProperties());
	}
	
	/**
	 * Gets the j panel planning properties.
	 * @return the j panel planning properties
	 */
	private PropertiesPanel getJPanelPlanningProperties() {
		if (jPanelPlanningProperties == null) {
			jPanelPlanningProperties = new PropertiesPanel(null, "Settings", true);
		}
		return jPanelPlanningProperties;
	}
	
	/**
	 * Adds the specified planner result to the local tabbed pane.
	 *
	 * @param tabTitle the tab title
	 * @param eomPlannerResult the EomPlannerResult
	 * @return true, if a tab was added 
	 */
	private boolean addPlannerResult(String tabTitle, EomPlannerResult eomPlannerResult) {
		if (eomPlannerResult==null) return false; 
		this.addTab(tabTitle, new EomPlannerResultPanel(eomPlannerResult));
		return true;
	}
	/**
	 * Returns a HashMap with all EomPlannerResultPanel currently shown.
	 * @return the HashMap of EomPlannerResultPanel
	 */
	private HashMap<String, EomPlannerResultPanel> getEomPlannerResultPanel() {
		
		HashMap<String, EomPlannerResultPanel> plannerPanelHashMap = new HashMap<>();
		for (int i = 1; i < this.getTabCount(); i++) {
			String tabTitle   = this.getTitleAt(i);
			Component tabComp = this.getComponentAt(i);
			if (tabComp instanceof EomPlannerResultPanel) {
				plannerPanelHashMap.put(tabTitle, (EomPlannerResultPanel) tabComp);
			}
		}
		return plannerPanelHashMap;
	}
	
	/**
	 * Returns the EomPlannerResult as selected in the UI.
	 * @return the EomPlannerResult as selected
	 */
	private EomPlannerResult getEomPlannerResultAsSelected() {
		
		Window owner = OwnerDetection.getOwnerWindowForComponent(this);
		String msgTitle = "Selection of Planning Result";
		String msg = null;
		
		EomPlannerResult plannerResult = null;
		int slSelectionIndex = -1;
		
		// --- Try to get the current selection -------------------------------
		int tabSelected = this.getSelectedIndex();
		Component compSelected = this.getComponentAt(tabSelected);
		if (compSelected instanceof EomPlannerResultPanel && this.getTitleAt(tabSelected).equals(TAB_HEADER_REAL_TIME_PLANNING)==false) {
			EomPlannerResultPanel resultPanel = (EomPlannerResultPanel) compSelected;
			plannerResult = resultPanel.getEomPlannerResult();
			// --- Which Schedule was selected within the ScheduleList? -------
			slSelectionIndex = resultPanel.getSelectedScheduleListIndex();
			if (slSelectionIndex==-1) {
				msg = "No actual Schedule was select within the selected planning result.";
				JOptionPane.showMessageDialog(owner, msg, msgTitle, JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
		} else {
			msg = "Please select a Schedule within a planning result for this action!";
			if (this.getTitleAt(tabSelected).equals(TAB_HEADER_REAL_TIME_PLANNING)==true) {
				msg += "\n\nThe currently selected tab '" + TAB_HEADER_REAL_TIME_PLANNING + "' contains \nthe later destination for this action.";
			}
			JOptionPane.showMessageDialog(owner, msg, msgTitle, JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// --- Extract the selected EomPlannerResult --------------------------
		return plannerResult.getEomPlannerResultFromIndexPosition(slSelectionIndex);
	}
	
	
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		
		PlanningInformation plInfo = this.swingUiModelInterface.getEnergyAgent().getPlanningInformation();
		this.getJPanelPlanningProperties().setProperties(plInfo);
		
		// --- Get all EomPlannerResultPanel ------------------------
		HashMap<String, EomPlannerResultPanel> resultPanelHM = this.getEomPlannerResultPanel();
		
		// --- Set / update tab for real time planning --------------
		EomPlannerResultPanel resultPanel = resultPanelHM.get(TAB_HEADER_REAL_TIME_PLANNING);
		if (resultPanel!=null) {
			resultPanel.setEomPlannerResult(plInfo.getRealTimePlannerResult());
			resultPanelHM.remove(TAB_HEADER_REAL_TIME_PLANNING);
		} else {
			this.addPlannerResult(TAB_HEADER_REAL_TIME_PLANNING, plInfo.getRealTimePlannerResult());
		}
		
		// --- Set or update further planning results ---------------
		if (plInfo.getPlannerResultTreeMap() != null) {
			for (String plannerName : plInfo.getPlannerResultTreeMap().keySet()) {
				// --- Get planner result an panel ------------------
				EomPlannerResult plannerResult = plInfo.getPlannerResultTreeMap().get(plannerName);
				resultPanel = resultPanelHM.get(plannerName);
				if (resultPanel!=null) {
					resultPanel.setEomPlannerResult(plannerResult);
					resultPanelHM.remove(plannerName);
				} else {
					this.addPlannerResult(plannerName, plannerResult);
				}
			}
		}
		
		// --- Remove the remaining EomPlannerResultPanel -----------
		for (String oldPlannerName : resultPanelHM.keySet()) {
			this.remove(resultPanelHM.get(oldPlannerName));
		}
		
		// --- Update the view again --------------------------------
		try {
			this.isPauseViewUpdater = true;
			this.swingUiModelInterface.firePropertyEvent(PropertyEvent.UpdateTreeView);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			this.isPauseViewUpdater = false;
		}
		
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt instanceof SwingUiDataCollector) {
			UiDataCollection uiCollection = (UiDataCollection) evt.getNewValue();
			switch (uiCollection) {
			case PlannerResultAsSelected:
				SwingUiDataCollector dataCollector = (SwingUiDataCollector) evt;
				dataCollector.setCollectedData(this.getEomPlannerResultAsSelected());
				break;
			default:
				break;
			}
			
		} else {
			PropertyEvent pe = (PropertyEvent) evt.getNewValue();
			switch (pe) {
			case UpdateView:
				if (this.isPauseViewUpdater==false) {
					this.setDisplayInformation();
				}
				break;

			default:
				break;
			}
		}
	}
	
}
