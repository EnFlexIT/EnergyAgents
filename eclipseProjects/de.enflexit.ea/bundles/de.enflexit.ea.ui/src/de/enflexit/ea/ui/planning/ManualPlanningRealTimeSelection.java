package de.enflexit.ea.ui.planning;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.planning.AbstractPlanningDispatcherManager;
import de.enflexit.ea.ui.JDialogEnergyAgent;
import energy.planning.EomPlannerResult;

/**
 * The Class ManualPlanningHandler.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class ManualPlanningRealTimeSelection {

	private JDialogEnergyAgent jDialogEnergyAgent;
	
	/**
	 * Instantiates a new manual planning handler.
	 * @param jDialogEnergyAgent the parent JDialogEnergyAgent
	 */
	public ManualPlanningRealTimeSelection(JDialogEnergyAgent jDialogEnergyAgent) {
		this.jDialogEnergyAgent = jDialogEnergyAgent;
	}
	
	/**
	 * Opens the manual planning perspective.
	 */
	public void setSelectedPlanForRealTimeExecution() {

		AbstractPlanningDispatcherManager<? extends AbstractEnergyAgent> pdm = this.jDialogEnergyAgent.getEnergyAgent().getPlanningDispatcherManager();
		if (pdm!=null) {
			// --- Get the selection as EomPlannerResult ------------ 
			EomPlannerResult eomPlannerResult = this.jDialogEnergyAgent.getJPanelPlannerInformation().getEomPlannerResultAsSelected();
			if (eomPlannerResult==null) return;
			
			EomPlannerResult rtPlannerResult = pdm.getPlannerResultForRealTimeExecution();
			rtPlannerResult.append(eomPlannerResult);
			jDialogEnergyAgent.updateView();
		}
	}
	
}
