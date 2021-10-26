package de.enflexit.ea.core.awbIntegration.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import org.awb.env.networkModel.DataModelNetworkElement;
import org.awb.env.networkModel.GraphElement;
import org.awb.env.networkModel.adapter.AbstractDynamicGraphElementLayout;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;

import de.enflexit.ea.core.awbIntegration.ImageHelper;
import de.enflexit.ea.core.dataModel.ontology.CableWithBreakerProperties;
import de.enflexit.ea.core.dataModel.ontology.CircuitBreaker;

/**
 * This abstract superclass provides the context menu interaction functionality for breakers,
 * which is identical for uni- and tri-phase breakers.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public abstract class CableWithBreakerAdapter extends NetworkComponentAdapter implements ActionListener {

	
	// --------------------------------------------------------------
	// --- Methods to build the context menu for the component ------
	// --------------------------------------------------------------
	/**
	 * Return the current CableWithBreakerProperties that are specified in the current data model of the {@link DataModelNetworkElement}.
	 * @return the cable with breaker properties
	 */
	private CableWithBreakerProperties getCableWithBreakerProperties() {
		
		CableWithBreakerProperties breakerConfig = null;

		// --- Try to find the current CableWithBreakerProperties ---
		Object dm = this.getDataModelNetworkElement().getDataModel();
		if (dm!=null && dm.getClass().isArray()) {
			Object[] dmArray = (Object[]) dm;
			if (dmArray.length>0 && dmArray[0] instanceof CableWithBreakerProperties) {
				breakerConfig = (CableWithBreakerProperties) dmArray[0];
			}
		}
		return breakerConfig;
	}
	/**
	 * Check and returns valid circuit breaker only .
	 *
	 * @param cb the CircuitBreaker instance to check
	 * @return the valid circuit breaker
	 */
	private CircuitBreaker getValidCircuitBreaker(CircuitBreaker cb) {
		if (cb==null) return null;
		if (cb.getAtComponent()==null || cb.getAtComponent().isEmpty()) return null;
		//if (cb.getBreakerID()==null || cb.getBreakerID().isEmpty()) return null;
		return cb;
	}
	/**
	 * Adds the menu item for the specified {@link CircuitBreaker}.
	 *
	 * @param myMenuItems the my menu items
	 * @param cb the CircuitBreaker to add
	 * @param isBegin the indicator, if the breaker is at the begin of the cable
	 */
	private void addMenuItemForCircuitBreaker(Vector<JComponent> myMenuItems, CircuitBreaker cb, boolean isBegin) {
		
		if (myMenuItems==null || cb==null) return;
		
		// --- Check if we're in setup or runtime mode --------------
		if (this.isRuntimeVisualization()==true && cb.getIsControllable()==false) return;
		
		// --- Check, if to open or to close ------------------------
		String openClosePrefix = "";
		ImageIcon iIcon = null;
		if (cb.getIsClosed()==true) {
			openClosePrefix = "Open";
			iIcon = ImageHelper.getImageIcon("breakerOpen.png");
		} else {
			openClosePrefix = "Close";
			iIcon = ImageHelper.getImageIcon("breakerClosed.png");
		}
		
		// --- Define display text and action command --------------- 
		String displayText = openClosePrefix + " the " + this.getDataModelNetworkElement().getId() + " cables breaker at component '" + cb.getAtComponent() + "'";
		String actionCommand = isBegin==true ? "Begin" : "End";
		
		// --- Define menu item and add to component vector ---------
		JMenuItem jMenuItem = new JMenuItem(displayText);
		jMenuItem.setIcon(iIcon);
		jMenuItem.setActionCommand(actionCommand);
		jMenuItem.addActionListener(this);
		myMenuItems.add(jMenuItem);
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
	
		// --- Get the breaker configuration ------------------------ 
		CableWithBreakerProperties breakerConfig = this.getCableWithBreakerProperties();

		// --- Define menu item vector ------------------------------
		Vector<JComponent> myMenuItmes = new Vector<JComponent>();
		this.addMenuItemForCircuitBreaker(myMenuItmes, this.getValidCircuitBreaker(breakerConfig.getBreakerBegin()), true);
		this.addMenuItemForCircuitBreaker(myMenuItmes, this.getValidCircuitBreaker(breakerConfig.getBreakerEnd())  , false);
		return myMenuItmes;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		// --- Check if CableWithBreakerProperties are available ---- 
		CableWithBreakerProperties breakerConfig = this.getCableWithBreakerProperties();
		if (breakerConfig==null) return;
		
		// --- Get the action command of the JMenuItem --------------
		JMenuItem jMenuItemSrc = (JMenuItem) ae.getSource();
		String actionCommand = jMenuItemSrc.getActionCommand();
		
		// --- Switch/Invert the is closed value --------------------
		CircuitBreaker cb = null;
		if (actionCommand.equals("Begin")==true) {
			cb = breakerConfig.getBreakerBegin();
		} else {
			cb = breakerConfig.getBreakerEnd();
		}
		cb.setIsClosed(!cb.getIsClosed());
		
		// --- Invoke to update the visualization -------------------
		this.updateDataModelVisualization();
		this.setProjectUnsaved();
	}

	
	// --------------------------------------------------------------
	// --- Methods to provide the dynamic graph element layout ------
	// --------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.adapter.NetworkComponentAdapter#getDynamicGraphElementLayout(org.awb.env.networkModel.GraphElement)
	 */
	@Override
	public AbstractDynamicGraphElementLayout getDynamicGraphElementLayout(GraphElement graphElement) {
		return new CableWithBreakerDynamicLayout(graphElement);
	}
	
}
