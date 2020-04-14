package de.enflexit.ea.core.awbIntegration.plugin;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.ui.toolbar.AbstractCustomToolbarComponent;

import de.enflexit.ea.core.globalDataModel.ImageHelper;

/**
 * The Class JButtonConstructionSite that will be displayed in the toolbar of the GraphEnvironement if configured
 * in the {@link AWBIntegrationPlugIn}. The class provides a static access method to ad an individual {@link ActionListener}. 
 * 
 * @see #addActionListener(ActionListener)
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class JButtonConstructionSite extends AbstractCustomToolbarComponent {

	private static GraphEnvironmentController graphEnvironmentController;
	private static JButton jButtonConstructionSite;
	
	
	/**
	 * Instantiates a new JButtonConstructionSite.
	 * @param graphController the graph controller
	 */
	public JButtonConstructionSite(GraphEnvironmentController graphController) {
		super(graphController);
		graphEnvironmentController = graphController;
	}
	/**
	 * Returns the current {@link GraphEnvironmentController}.
	 * @return the graph environment controller
	 */
	public static GraphEnvironmentController getGraphEnvironmentController() {
		return graphEnvironmentController;
	}
	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.controller.AbstractCustomToolbarComponent#getCustomComponent()
	 */
	@Override
	public JComponent getCustomComponent() {
		return getJButtonConstructionSite();
	}

	/**
	 * Gets the JButton construction site.
	 * @return the JButton construction site
	 */
	private static JButton getJButtonConstructionSite() {
		if (jButtonConstructionSite==null) {
			jButtonConstructionSite = new JButton();
			jButtonConstructionSite.setIcon(ImageHelper.getImageIcon("ConstructionSite.png"));
			jButtonConstructionSite.setToolTipText("Construction Site ... ");
			jButtonConstructionSite.setPreferredSize(new Dimension(26, 26));
		}
		return jButtonConstructionSite;
	}
	/**
	 * Allows to add an individual action listener to the construction site button.
	 * @param al the ActionListener
	 */
	public static void addActionListener(ActionListener al) {
		List<ActionListener> aListener = new ArrayList<ActionListener>(Arrays.asList(getJButtonConstructionSite().getActionListeners()));
		if (aListener.contains(al)==false) {
			getJButtonConstructionSite().addActionListener(al);
		}
	}
	
}
