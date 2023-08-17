package de.enflexit.ea.ui;

import java.awt.Font;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
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
	
	private PropertiesPanel jPanelPlanningProperties;
	
	
	/**
	 * Instantiates a new j panel real time information.
	 * @param jDialogEnergyAgent the j dialog energy agent
	 */
	public JPanelPlannerInformation(JDialogEnergyAgent jDialogEnergyAgent) {
		this.initialize();
		this.setDisplayInformation(jDialogEnergyAgent);
	}
	
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation(final JDialogEnergyAgent jDialogEnergyAgent) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				PlanningInformation plInfo = jDialogEnergyAgent.getEnergyAgent().getPlanningInformation();
				 JPanelPlannerInformation.this.getJPanelPlanningProperties().setProperties(plInfo);
				 JPanelPlannerInformation.this.addPlannerResult("Real Time Planner Result", plInfo.getRealTimePlannerResult());
				 for (String plannerName : plInfo.getPlannerResultTreeMap().keySet()) {
					 JPanelPlannerInformation.this.addPlannerResult(plannerName, plInfo.getPlannerResultTreeMap().get(plannerName));
				 }
			}
		});
	}
	
	/**
	 * Initializes this panel JTabbed Pane.
	 */
	private void initialize() {
		
		this.setUI(new AwbBasicTabbedPaneUI());
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
	
}
