package de.enflexit.ea.core.awbIntegration.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import de.enflexit.ea.core.awbIntegration.ImageHelper;
import de.enflexit.ea.core.dataModel.visualizationMessaging.EnergyAgentVisualizationMessagging;
import de.enflexit.eom.awb.adapter.EomAdapter;
import jade.core.AID;

/**
 * The Class EnergyAgentAdapter.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class EnergyAgentAdapter extends EomAdapter {

	private JMenuItem menuItemShowUI;
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.adapter.EomAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		if (this.isRuntimeVisualization()==true) {
			Vector<JComponent> popupMenuElements = new Vector<JComponent>();
			popupMenuElements.add(this.getJMenuItemShowUI());
			return popupMenuElements;
		}
		return null;
	}
	
	/**
	 * Returns the JMenuItem for showing the energy agents UI.
	 * @return the JMenuItem for showing the energy agents UI
	 */
	private JMenuItem getJMenuItemShowUI(){
		if (this.menuItemShowUI == null){
			this.menuItemShowUI = new JMenuItem("Show state of " + this.networkComponent.getType() + " " + this.networkComponent.getId());
			this.menuItemShowUI.setIcon(ImageHelper.getImageIcon("Search.png"));
			this.menuItemShowUI.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (isRuntimeVisualization()) {
						// --- Simulation is running - send an ACL message asking the agent to show the UI -----
						EnergyAgentVisualizationMessagging.sendShowUIMessageToEnergyAgent(EnergyAgentAdapter.this.getDisplayAgent(), EnergyAgentAdapter.this.getEnergyAgentAID());
					}
				}
			});
		}
		return this.menuItemShowUI;
	}

	/**
	 * Determines the AID of the EnergyAgent represented by this adapter based on the network component ID.
	 * @return the my agent AID
	 */
	private AID getEnergyAgentAID(){
		return new AID(this.networkComponent.getId(), AID.ISLOCALNAME);
	}
	
}
