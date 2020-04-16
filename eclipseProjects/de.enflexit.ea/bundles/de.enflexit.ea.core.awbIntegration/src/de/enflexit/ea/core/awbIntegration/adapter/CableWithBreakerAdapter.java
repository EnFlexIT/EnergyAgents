package de.enflexit.ea.core.awbIntegration.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import org.awb.env.networkModel.adapter.NetworkComponentAdapter;

/**
 * This abstract superclass provides the context menu interaction functionality for breakers,
 * which is identical for uni- and tri-phase breakers.
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public abstract class CableWithBreakerAdapter extends NetworkComponentAdapter implements ActionListener {

	private JMenuItem jMenuItemAction;
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		Vector<JComponent> myTools = new Vector<JComponent>();
		myTools.add(this.getJMenuItemAction());
		return myTools;
	}
	
	/**
	 * Gets the j menu item action.
	 * @return the j menu item action
	 */
	private JMenuItem getJMenuItemAction() {
		if (jMenuItemAction==null) {
			jMenuItemAction = new JMenuItem("Switch Breaker!");
			jMenuItemAction.addActionListener(this);
		}
		return jMenuItemAction;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		System.out.println("Breaker was switched - Do somthing!");
	}

}
