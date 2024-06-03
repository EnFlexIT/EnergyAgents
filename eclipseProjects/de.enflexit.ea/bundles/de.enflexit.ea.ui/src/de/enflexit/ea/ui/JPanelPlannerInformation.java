package de.enflexit.ea.ui;

import java.awt.Font;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.ea.core.ui.PlanningInformation;
import energy.planning.EomPlannerResult;
import energy.planning.ui.EomPlannerResultPanel;

/**
 * The Class JPanelPlannerInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelPlannerInformation extends JTabbedPane {

	private static final long serialVersionUID = -2222117009782469384L;
	
	private JDialogEnergyAgent jDialogEnergyAgent;
	private PropertiesPanel jPanelPlanningProperties;
	
	
	/**
	 * Instantiates a new j panel real time information.
	 * @param jDialogEnergyAgent the j dialog energy agent
	 */
	public JPanelPlannerInformation(JDialogEnergyAgent jDialogEnergyAgent) {
		this.jDialogEnergyAgent = jDialogEnergyAgent;
		this.initialize();
		this.updateView();
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
	 * Updates the view according to the state of the energy agent.
	 */
	public void updateView() {
		this.setDisplayInformation();
	}
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				PlanningInformation plInfo = JPanelPlannerInformation.this.jDialogEnergyAgent.getEnergyAgent().getPlanningInformation();
				JPanelPlannerInformation.this.getJPanelPlanningProperties().setProperties(plInfo);
				JPanelPlannerInformation.this.removeEomPlannerResults();
				JPanelPlannerInformation.this.addPlannerResult("Real Time Planner Result", plInfo.getRealTimePlannerResult());
				if (plInfo.getPlannerResultTreeMap() != null) {
					for (String plannerName : plInfo.getPlannerResultTreeMap().keySet()) {
						JPanelPlannerInformation.this.addPlannerResult(plannerName, plInfo.getPlannerResultTreeMap().get(plannerName));
					}
				}
			}
		});
	}
}
