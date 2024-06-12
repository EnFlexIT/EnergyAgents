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
import javax.swing.SwingUtilities;

import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.ui.PlanningInformation;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;
import energy.planning.EomPlannerResult;
import energy.planning.events.EomPlanningEvent;
import energy.planning.events.EomPlanningEventList;

/**
 * The Class JPanelPlanningEvents.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelPlanningEvents extends JSplitPane implements PropertyChangeListener {

	private static final long serialVersionUID = -5338320863998070171L;

	private SwingUiModelInterface swingUiModelInterface;
	
	private JPanel jPanelRight;
	
	private JPanel jPanelLeft;
	private JLabel JLabelHeaderLeft;
	private JScrollPane jScrollPaneLeft;
	private DefaultListModel<EomPlanningEvent> listModel;
	private JList<EomPlanningEvent> jListEvents;
	
	
	/**
	 * Instantiates a new j panel planning events.
	 * @param eomPlanningEventList the eom planning event list
	 */
	public JPanelPlanningEvents(SwingUiModelInterface swingUiModelInterface) {
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
		this.initialize();
		this.setDisplayInformation();
	}
	private void initialize() {
		this.setResizeWeight(0.333);
		this.setDividerLocation(0.33);
		this.setDividerSize(8);
		this.setRightComponent(this.getJPanelRight());
		this.setLeftComponent(this.getJPanelLeft());
	}

	private JPanel getJPanelRight() {
		if (jPanelRight == null) {
			jPanelRight = new JPanel();
			GridBagLayout gbl_jPanelRight = new GridBagLayout();
			gbl_jPanelRight.columnWidths = new int[]{0};
			gbl_jPanelRight.rowHeights = new int[]{0};
			gbl_jPanelRight.columnWeights = new double[]{Double.MIN_VALUE};
			gbl_jPanelRight.rowWeights = new double[]{Double.MIN_VALUE};
			jPanelRight.setLayout(gbl_jPanelRight);
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
	
	private void fillList(EomPlanningEventList eomPlanningEventList) {
		
		this.getListModel().clear();
		if (eomPlanningEventList==null) return;
		if (eomPlanningEventList.size()==0) return;
		
		eomPlanningEventList.forEach(plEvent -> this.getListModel().addElement(plEvent));
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
		}
		return jListEvents;
	}
	private JLabel getJLabelHeaderLeft() {
		if (JLabelHeaderLeft == null) {
			JLabelHeaderLeft = new JLabel("Control Events");
			JLabelHeaderLeft.setFont(new Font("Dialog", Font.BOLD, 12));
		}
		return JLabelHeaderLeft;
	}
	
	
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				AbstractEnergyAgent energyAgent = JPanelPlanningEvents.this.swingUiModelInterface.getEnergyAgent();
				PlanningInformation plInfo = energyAgent.getPlanningInformation();
				EomPlannerResult rtPlan = plInfo.getRealTimePlannerResult();
				if (rtPlan==null || rtPlan.getMainScheduleList()==null || rtPlan.getMainScheduleList().getSchedules().size()==0) return;
				
				EomPlanningEventList eomPlEvtList = null;
				switch (energyAgent.getInternalDataModel().getTypeOfControlledSystem()) {
				case None:
					break;
				case TechnicalSystem:
					eomPlEvtList = new EomPlanningEventList(energyAgent.getInternalDataModel().getOptionModelController(), rtPlan, 0);
					break;
				case TechnicalSystemGroup:
					eomPlEvtList = new EomPlanningEventList(energyAgent.getInternalDataModel().getGroupController(), rtPlan, 0);
					break;
				}
				JPanelPlanningEvents.this.fillList(eomPlEvtList);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getNewValue() instanceof UiDataCollection) return;
		
		PropertyEvent pe = (PropertyEvent) evt.getNewValue(); 
		switch (pe) {
		case UpdateView:
			this.setDisplayInformation();
			break;

		default:
			break;
		}
	}
	
}
