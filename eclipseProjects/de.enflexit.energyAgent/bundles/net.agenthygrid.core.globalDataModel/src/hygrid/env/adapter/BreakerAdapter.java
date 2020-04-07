/*
 * 
 */
package hygrid.env.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

/**
 * The Class BreakerAdapter describes the data model and the interaction methods 
 * with a breaker/switch within the defined {@link NetworkModel}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BreakerAdapter extends NetworkComponentAdapter implements ActionListener {
	
	private JMenuItem jMenuItemAction;
	

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getNewDataModelAdapter()
	 */
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new BreakerDataModelAdapter(this.graphController);
	}

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
