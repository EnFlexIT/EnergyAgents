package de.enflexit.ea.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.ui.PlanningInformation;
import de.enflexit.ea.ui.SwingUiFocusDescription.FocusTo;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;
import energy.planning.EomPlannerResult;
import energy.planning.events.EomPlanningEvent;
import energy.planning.events.EomPlanningEventTreeMap;

/**
 * The Class JPanelEomPlanningEventTreeMap.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelEomPlanningEventTreeMap extends JSplitPane implements PropertyChangeListener {

	private static final long serialVersionUID = -5338320863998070171L;

	private SwingUiModelInterface swingUiModelInterface;
	
	private JPanel jPanelLeft;
	private JLabel JLabelHeaderLeft;
	private JScrollPane jScrollPaneLeft;
	private DefaultListModel<EomPlanningEvent> listModel;
	private JList<EomPlanningEvent> jListEvents;
	
	private JPanelEomPlanningEvent jPanelRight;

	private EomPlanningEventTreeMap eomPlanningEventTreeMap;
	
	/**
	 * Instantiates a new j panel planning events.
	 * @param eomPlanningEventList the eom planning event list
	 */
	public JPanelEomPlanningEventTreeMap(SwingUiModelInterface swingUiModelInterface) {
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
		this.initialize();
		this.setDisplayInformation();
	}
	private void initialize() {
		this.setRightComponent(this.getJPanelEomPlanningEvent());
		this.setLeftComponent(this.getJPanelLeft());
		this.setDividerSize(8);
		this.setResizeWeight(0.333);
		this.setDividerLocation(500);
	}

	private JPanelEomPlanningEvent getJPanelEomPlanningEvent() {
		if (jPanelRight == null) {
			jPanelRight = new JPanelEomPlanningEvent();
		}
		return jPanelRight;
	}
	
	private JPanel getJPanelLeft() {
		if (jPanelLeft == null) {
			jPanelLeft = new JPanel();
			GridBagLayout gbl_jPanelRight = new GridBagLayout();
			gbl_jPanelRight.columnWidths = new int[]{0, 0};
			gbl_jPanelRight.rowHeights = new int[]{0, 0, 0};
			gbl_jPanelRight.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_jPanelRight.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			jPanelLeft.setLayout(gbl_jPanelRight);
			GridBagConstraints gbc_JLabelHeaderLeft = new GridBagConstraints();
			gbc_JLabelHeaderLeft.anchor = GridBagConstraints.WEST;
			gbc_JLabelHeaderLeft.insets = new Insets(10, 10, 0, 0);
			gbc_JLabelHeaderLeft.gridx = 0;
			gbc_JLabelHeaderLeft.gridy = 0;
			jPanelLeft.add(this.getJLabelHeaderLeft(), gbc_JLabelHeaderLeft);
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.insets = new Insets(5, 10, 0, 0);
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			jPanelLeft.add(this.getJScrollPaneLeft(), gbc_scrollPane);
		}
		return jPanelLeft;
	}
	
	private JScrollPane getJScrollPaneLeft() {
		if (jScrollPaneLeft == null) {
			jScrollPaneLeft = new JScrollPane();
			jScrollPaneLeft.setViewportView(getJListEvents());
		}
		return jScrollPaneLeft;
	}
	
	private void fillList(EomPlanningEventTreeMap eomPlanningEventTreeMap) {
		
		this.getListModel().clear();
		if (eomPlanningEventTreeMap==null) return;
		if (eomPlanningEventTreeMap.size()==0) return;
		
		for (long timeStamp: eomPlanningEventTreeMap.keySet()) {
			this.getListModel().addElement(eomPlanningEventTreeMap.get(timeStamp));
		}
	}
	
	public DefaultListModel<EomPlanningEvent> getListModel() {
		if (listModel==null) {
			listModel = new DefaultListModel<>();
		}
		return listModel;
	}
	private JList<EomPlanningEvent> getJListEvents() {
		if (jListEvents == null) {
			jListEvents = new JList<>(this.getListModel());
			jListEvents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jListEvents.setFont(new Font("Dialog", Font.PLAIN, 12));
			jListEvents.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent lsEvt) {
					if (lsEvt.getValueIsAdjusting()==true) return;
					EomPlanningEvent plEvt = JPanelEomPlanningEventTreeMap.this.getJListEvents().getSelectedValue();
					if (plEvt!=null) {
						JPanelEomPlanningEventTreeMap.this.getJPanelEomPlanningEvent().setEomPlanningEvent(plEvt);
					}
				}
			});
		}
		return jListEvents;
	}
	private JLabel getJLabelHeaderLeft() {
		if (JLabelHeaderLeft == null) {
			JLabelHeaderLeft = new JLabel("Control Event Time Table");
			JLabelHeaderLeft.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return JLabelHeaderLeft;
	}
	
	/**
	 * Returns the current EomPlanningEventTreeMap.
	 * @return the eom planning event tree map
	 */
	private EomPlanningEventTreeMap getEomPlanningEventTreeMap() {
		return eomPlanningEventTreeMap;
	}
	/**
	 * Sets the EomPlanningEventTreeMap.
	 * @param eomPlanningEventTreeMap the new eom planning event tree map
	 */
	private  void setEomPlanningEventTreeMap(EomPlanningEventTreeMap eomPlanningEventTreeMap) {
		this.eomPlanningEventTreeMap = eomPlanningEventTreeMap;
		this.fillList(eomPlanningEventTreeMap);
	}
	/**
	 * Return the next EomPlanningEvent.
	 * @return the next eom planning event
	 */
	private EomPlanningEvent getNextEomPlanningEvent() {
		
		EomPlanningEvent planEvent = null;
		if (this.getEomPlanningEventTreeMap()!=null) {
			long currTime = this.swingUiModelInterface.getEnergyAgent().getEnergyAgentIO().getTime();
			planEvent = this.getEomPlanningEventTreeMap().getNextPlanningEvent(currTime);
		}
		return planEvent;
	}
	
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		
		AbstractEnergyAgent energyAgent = JPanelEomPlanningEventTreeMap.this.swingUiModelInterface.getEnergyAgent();
		PlanningInformation plInfo = energyAgent.getPlanningInformation();
		EomPlannerResult rtPlan = plInfo.getRealTimePlannerResult();
		if (rtPlan==null || rtPlan.getMainScheduleList()==null || rtPlan.getMainScheduleList().getSchedules().size()==0) return;
		
		EomPlanningEventTreeMap eomPlEvtList = null;
		switch (energyAgent.getInternalDataModel().getTypeOfControlledSystem()) {
		case None:
			break;
		case TechnicalSystem:
			eomPlEvtList = new EomPlanningEventTreeMap(energyAgent.getInternalDataModel().getOptionModelController(), rtPlan, 0);
			break;
		case TechnicalSystemGroup:
			eomPlEvtList = new EomPlanningEventTreeMap(energyAgent.getInternalDataModel().getGroupController(), rtPlan, 0);
			break;
		}
		this.setEomPlanningEventTreeMap(eomPlEvtList);
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		// --- UiDataCollection events --------------------
		if (evt instanceof SwingUiDataCollector) {
			UiDataCollection uiDataCollection = (UiDataCollection) evt.getNewValue();
			switch (uiDataCollection) {
			case NextPlannerEvent:
				SwingUiDataCollector dataCollector = (SwingUiDataCollector) evt;
				dataCollector.setCollectedData(this.getNextEomPlanningEvent());
				break;
			default:
				break;
			}
			return;
		}
		
		// --- Simple UI events --------------------------- 
		PropertyEvent pe = (PropertyEvent) evt.getNewValue(); 
		switch (pe) {
		case UpdateView:
			this.setDisplayInformation();
			break;
		case FocusEvent:
			SwingUiFocusEvent focusEvent = (SwingUiFocusEvent) evt;
			if (focusEvent.getFocusDescription().getFocusTo()==FocusTo.NextPlanningEvent) {
				this.getJListEvents().setSelectedValue(this.getNextEomPlanningEvent(), true);
			}
			
		default:
			break;
		}
	}
	
}
