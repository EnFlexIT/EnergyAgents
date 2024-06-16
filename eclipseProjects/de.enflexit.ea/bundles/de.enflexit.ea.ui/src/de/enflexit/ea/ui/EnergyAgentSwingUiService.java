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

	private HashMap<AbstractEnergyAgent, EnergyAgentWindowInterface> agentDialogHashMap;
	private WindowAdapter windowAdapter;
	
	/**
	 * Returns the agent<->dialog hash map with agents that are currently opened.
	 * @return the agent dialog hash map
	 */
	private HashMap<AbstractEnergyAgent, EnergyAgentWindowInterface> getAgentDialogHashMap() {
		if (agentDialogHashMap==null) {
			agentDialogHashMap = new HashMap<>();
		}
		return agentDialogHashMap;
	}
	/**
	 * Returns s the energy agent for the specified EnergyAgentWindowInterface (in fact a JDialog or a JFrame)
	 *
	 * @param eaWindow the EnergyAgentWindowInterface to search for
	 * @return the energy agent found for the specified EnergyAgentWindowInterface
	 */
	private AbstractEnergyAgent getEnergyAgentFromWindow(EnergyAgentWindowInterface eaWindow) {
		
		if (eaWindow!=null) {
			for (AbstractEnergyAgent energyAgent : this.getAgentDialogHashMap().keySet()) {
				if (this.getAgentDialogHashMap().get(energyAgent).equals(eaWindow)==true) {
					return energyAgent;
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.ea.core.ui.EnergyAgentUiService#openOrFocusUI(de.enflexit.ea.core.AbstractEnergyAgent)
	 */
	@Override
	public void openOrFocusUI(final AbstractEnergyAgent energyAgent) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				EnergyAgentWindowInterface eaWindow = EnergyAgentSwingUiService.this.getAgentDialogHashMap().get(energyAgent);
				if (eaWindow==null) {
					// --- Open the default UI for an EnergyAgent -----------
					JDialogEnergyAgent eaDialogDefault = new JDialogEnergyAgent(Application.getMainWindow(), energyAgent);
					eaDialogDefault.addWindowListener(EnergyAgentSwingUiService.this.getWindowAdapter());
					EnergyAgentSwingUiService.this.getAgentDialogHashMap().put(energyAgent, eaDialogDefault);
					eaWindow = eaDialogDefault;
				}
				eaWindow.firePropertyEvent(PropertyEvent.ShowOrFocusView);
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
						EnergyAgentWindowInterface eaWindow = (EnergyAgentWindowInterface) we.getWindow();
						eaWindow.firePropertyEvent(PropertyEvent.CloseView);
						AbstractEnergyAgent energyAgent = EnergyAgentSwingUiService.this.getEnergyAgentFromWindow(eaWindow);
						if (energyAgent!=null) {
							EnergyAgentSwingUiService.this.getAgentDialogHashMap().remove(energyAgent);
						}
						
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
				EnergyAgentWindowInterface eaWindow = EnergyAgentSwingUiService.this.getAgentDialogHashMap().get(energyAgent);
				if (eaWindow!=null) {
					eaWindow.firePropertyEvent(PropertyEvent.UpdateView);
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
				EnergyAgentWindowInterface eaWindow = EnergyAgentSwingUiService.this.getAgentDialogHashMap().get(energyAgent);
				if (eaWindow!=null) {
					eaWindow.firePropertyEvent(PropertyEvent.CloseView);
					EnergyAgentSwingUiService.this.getAgentDialogHashMap().remove(energyAgent);
				}
			}
		});
	}

}
