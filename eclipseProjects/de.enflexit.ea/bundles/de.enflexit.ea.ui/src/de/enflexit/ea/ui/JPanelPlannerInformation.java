package de.enflexit.ea.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

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
	 * @param eomPlannerResult the eom planner result
	 */
	private void addPlannerResult(String tabTitle, EomPlannerResult eomPlannerResult) {
		
		if (eomPlannerResult==null) return; 
		this.addTab(tabTitle, new EomPlannerResultPanel(eomPlannerResult));
	}
	/**
	 * Removes all EomPlannerResults and their panel .
	 */
	private void removeEomPlannerResults() {
		while ((this.getTabCount()-1) > 0) {
			this.removeTabAt(this.getTabCount()-1);
		}
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				PlanningInformation plInfo = JPanelPlannerInformation.this.swingUiModelInterface.getEnergyAgent().getPlanningInformation();
				JPanelPlannerInformation.this.getJPanelPlanningProperties().setProperties(plInfo);
				JPanelPlannerInformation.this.removeEomPlannerResults();
				JPanelPlannerInformation.this.addPlannerResult(TAB_HEADER_REAL_TIME_PLANNING, plInfo.getRealTimePlannerResult());
				if (plInfo.getPlannerResultTreeMap() != null) {
					for (String plannerName : plInfo.getPlannerResultTreeMap().keySet()) {
						JPanelPlannerInformation.this.addPlannerResult(plannerName, plInfo.getPlannerResultTreeMap().get(plannerName));
					}
				}
			}
		});
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
			}
			
		} else {
			PropertyEvent pe = (PropertyEvent) evt.getNewValue();
			switch (pe) {
			case UpdateView:
				this.setDisplayInformation();
				break;

			default:
				break;
			}
			
		}
		
	}
	
}
