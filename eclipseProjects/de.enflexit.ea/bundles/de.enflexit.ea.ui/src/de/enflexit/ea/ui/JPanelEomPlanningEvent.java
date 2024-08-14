package de.enflexit.ea.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import energy.planning.events.EomPlanningEvent;
import energy.planning.events.EomPlanningEvent.SystemEvent;

/**
 * The Class JPanelEomPlanningEvent.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelEomPlanningEvent extends JPanel {

	private static final long serialVersionUID = -6373800150809194568L;

	private EomPlanningEvent eomPlanningEvent;
	private JLabel jLabelHeader;
	private JScrollPane jScrollPaneControlChanges;
	private JPanel JPanelSystemEvents;
	
	/**
	 * Instantiates a new JPanelEomPlanningEvent.
	 */
	public JPanelEomPlanningEvent() {
		this.initialize();
	}
	/**
	 * Sets the current EomPlanningEvent.
	 * @param eomPlanningEvent the new eom planning event
	 */
	public void setEomPlanningEvent(EomPlanningEvent eomPlanningEvent) {
		this.eomPlanningEvent = eomPlanningEvent;
		this.updateView();
	}
	/**
	 * Return the current EomPlanningEvent.
	 * @return the eom planning event
	 */
	public EomPlanningEvent getEomPlanningEvent() {
		return eomPlanningEvent;
	}
	
	private void initialize() {
	
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		GridBagConstraints gbc_jLabelHeader = new GridBagConstraints();
		gbc_jLabelHeader.anchor = GridBagConstraints.WEST;
		gbc_jLabelHeader.insets = new Insets(10, 10, 0, 0);
		gbc_jLabelHeader.gridx = 0;
		gbc_jLabelHeader.gridy = 0;
		this.add(this.getJLabelHeader(), gbc_jLabelHeader);
		GridBagConstraints gbc_jScrollPaneControlChanges = new GridBagConstraints();
		gbc_jScrollPaneControlChanges.insets = new Insets(2, 5, 5, 5);
		gbc_jScrollPaneControlChanges.fill = GridBagConstraints.BOTH;
		gbc_jScrollPaneControlChanges.gridx = 0;
		gbc_jScrollPaneControlChanges.gridy = 1;
		this.add(this.getJScrollPaneControlChanges(), gbc_jScrollPaneControlChanges);
	}
	private JLabel getJLabelHeader() {
		if (jLabelHeader == null) {
			jLabelHeader = new JLabel("Control Events");
			jLabelHeader.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return jLabelHeader;
	}
	private JScrollPane getJScrollPaneControlChanges() {
		if (jScrollPaneControlChanges == null) {
			jScrollPaneControlChanges = new JScrollPane();
			jScrollPaneControlChanges.getVerticalScrollBar().setUnitIncrement(6);
			jScrollPaneControlChanges.setViewportView(this.getJPanelSystemEvents());
			
		}
		return jScrollPaneControlChanges;
	}
	
	private JPanel getJPanelSystemEvents() {
		if (JPanelSystemEvents == null) {
			JPanelSystemEvents = new JPanel();
			JPanelSystemEvents.setLayout(new BoxLayout(JPanelSystemEvents, BoxLayout.Y_AXIS));
		}
		return JPanelSystemEvents;
	}
	
	/**
	 * Updates view.
	 */
	private void updateView() {
		
		// --- Clear the view -----------------------------
		this.getJPanelSystemEvents().removeAll();
		
		// ------------------------------------------------
		// --- Collect the SystemEvent panels -------------
		// ------------------------------------------------
		SystemEvent sysEvt = this.getEomPlanningEvent().getMainSystemEvent();
		if (sysEvt!=null) {
			this.getJPanelSystemEvents().add(new JPanelEomPlanningSystemEvent(sysEvt));
		}
		
		List<String> networkIDList = new ArrayList<>(this.getEomPlanningEvent().getSubSystemEventHashMap().keySet());
		Collections.sort(networkIDList);
		for (String networkID: networkIDList) {
			SystemEvent subSysEvt = this.getEomPlanningEvent().getSubSystemEventHashMap().get(networkID);
			this.getJPanelSystemEvents().add(new JPanelEomPlanningSystemEvent(subSysEvt));
		}
		this.getJPanelSystemEvents().validate();
		this.getJPanelSystemEvents().repaint();
	}
}
