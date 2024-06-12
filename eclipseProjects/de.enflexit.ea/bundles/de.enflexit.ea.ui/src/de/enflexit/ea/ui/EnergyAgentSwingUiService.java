package de.enflexit.ea.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import agentgui.core.application.Application;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.ui.EnergyAgentUiService;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;

/**
 * The Class EnergyAgentSwingUiService.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EnergyAgentSwingUiService implements EnergyAgentUiService {

	private HashMap<AbstractEnergyAgent, SwingUiModelInterface> agentDialogHashMap;
	private WindowAdapter windowAdapter;
	
	/**
	 * Returns the agent<->dialog hash map with agents that are currently opened.
	 * @return the agent dialog hash map
	 */
	private HashMap<AbstractEnergyAgent, SwingUiModelInterface> getAgentDialogHashMap() {
		if (agentDialogHashMap==null) {
			agentDialogHashMap = new HashMap<>();
		}
		return agentDialogHashMap;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#openOrFocusUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void openOrFocusUI(final AbstractEnergyAgent energyAgent) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				SwingUiModelInterface uiModelInterface = EnergyAgentSwingUiService.this.getAgentDialogHashMap().get(energyAgent);
				if (uiModelInterface==null) {
					// --- Open the default UI for an EnergyAgent -----------
					JDialogEnergyAgent eaDialogDefault = new JDialogEnergyAgent(Application.getMainWindow(), energyAgent);
					eaDialogDefault.addWindowListener(EnergyAgentSwingUiService.this.getWindowAdapter());
					EnergyAgentSwingUiService.this.getAgentDialogHashMap().put(energyAgent, eaDialogDefault);
					uiModelInterface = eaDialogDefault;
				}
				uiModelInterface.firePropertyEvent(PropertyEvent.ShowOrFocusView);
			}
		});
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
						SwingUiModelInterface eaDialog = (SwingUiModelInterface) we.getWindow();
						eaDialog.firePropertyEvent(PropertyEvent.CloseView);
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
	public void updateUI(final AbstractEnergyAgent energyAgent) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingUiModelInterface eaDialog = EnergyAgentSwingUiService.this.getAgentDialogHashMap().get(energyAgent);
				if (eaDialog!=null) {
					eaDialog.firePropertyEvent(PropertyEvent.UpdateView);
				}
			}
		});
	}
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#closeUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void closeUI(final AbstractEnergyAgent energyAgent) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingUiModelInterface eaDialog = EnergyAgentSwingUiService.this.getAgentDialogHashMap().get(energyAgent);
				if (eaDialog!=null) {
					eaDialog.firePropertyEvent(PropertyEvent.CloseView);
					EnergyAgentSwingUiService.this.getAgentDialogHashMap().remove(energyAgent);
				}
			}
		});
	}

}
