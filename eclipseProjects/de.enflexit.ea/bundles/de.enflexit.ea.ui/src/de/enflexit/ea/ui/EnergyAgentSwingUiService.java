package de.enflexit.ea.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import agentgui.core.application.Application;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.ui.EnergyAgentUiService;

/**
 * The Class EnergyAgentSwingUiService.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EnergyAgentSwingUiService implements EnergyAgentUiService {

	private HashMap<AbstractEnergyAgent, JDialogEnergyAgent> agentDialogHashMap;
	private WindowAdapter windowAdapter;
	
	/**
	 * Returns the agent<->dialog hash map with agents that are currently opened.
	 * @return the agent dialog hash map
	 */
	public HashMap<AbstractEnergyAgent, JDialogEnergyAgent> getAgentDialogHashMap() {
		if (agentDialogHashMap==null) {
			agentDialogHashMap = new HashMap<>();
		}
		return agentDialogHashMap;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#openOrFocusUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void openOrFocusUI(AbstractEnergyAgent energyAgent) {
		
		JDialogEnergyAgent eaDialog = this.getAgentDialogHashMap().get(energyAgent);
		if (eaDialog==null) {
			eaDialog = new JDialogEnergyAgent(Application.getMainWindow(), energyAgent);
			eaDialog.addWindowListener(this.getWindowAdapter());
			this.getAgentDialogHashMap().put(energyAgent, eaDialog);
		}
		
		if (eaDialog.isVisible()==true) {
			eaDialog.requestFocusInWindow();
		} else {
			eaDialog.setVisible(true);
		}
	}

	/**
	 * Returns the window adapter that reacts on closing a energy agent dialog.
	 * @return the window adapter
	 */
	private WindowAdapter getWindowAdapter() {
		if (windowAdapter==null) {
			windowAdapter = new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					try {
						JDialogEnergyAgent eaDialog = (JDialogEnergyAgent) we.getSource();
						eaDialog.dispose();
						EnergyAgentSwingUiService.this.getAgentDialogHashMap().remove(eaDialog.getEnergyAgent());
					} catch (Exception ex) {
						System.err.println("[" + EnergyAgentSwingUiService.class.getSimpleName() + "] Error whil trying to close energy agent dialog:");
						ex.printStackTrace();
					}
				}
			};
		}
		return windowAdapter;
	}

	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#updateUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void updateUI(AbstractEnergyAgent energyAgent) {
		
		JDialogEnergyAgent eaDialog = this.getAgentDialogHashMap().get(energyAgent);
		if (eaDialog!=null) {
			eaDialog.updateView();
		}
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#closeUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void closeUI(AbstractEnergyAgent energyAgent) {
		
		JDialogEnergyAgent eaDialog = this.getAgentDialogHashMap().get(energyAgent);
		if (eaDialog!=null) {
			eaDialog.setVisible(false);
			eaDialog.dispose();
			this.getAgentDialogHashMap().remove(energyAgent);
		}
	}

}
