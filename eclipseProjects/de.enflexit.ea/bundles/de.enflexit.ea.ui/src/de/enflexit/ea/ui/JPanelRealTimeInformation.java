package de.enflexit.ea.ui;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.ea.core.ui.RealTimeInformation;
import de.enflexit.ea.ui.SwingUiModel.PropertyEvent;
import de.enflexit.ea.ui.SwingUiModel.UiDataCollection;
import energy.EomController;
import energy.OptionModelController;
import energy.evaluation.gui.TechnicalSystemStatePanel;
import energy.schedule.ScheduleController;
import energygroup.GroupController;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The Class JPanelRealTimeInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelRealTimeInformation extends JSplitPane implements PropertyChangeListener {

	private static final long serialVersionUID = 7714692252010988662L;
	
	private SwingUiModelInterface swingUiModelInterface;
	
	private PropertiesPanel jPanelRealTimeProperties;
	private JTabbedPane jTabbedPane;
	private TechnicalSystemStatePanel technicalSystemStatePanel;
	
	
	/**
	 * Instantiates a new j panel real time information.
	 *
	 * @param swingUiModelInterface the swing ui model interface
	 */
	public JPanelRealTimeInformation(SwingUiModelInterface swingUiModelInterface) {
		this.swingUiModelInterface = swingUiModelInterface;
		this.swingUiModelInterface.addPropertyListener(this);
		this.initialize();
		this.setDisplayInformation();
	}
	/**
	 * Initialize.
	 */
	private void initialize() {
		this.setOneTouchExpandable(true);
		this.setDividerSize(5);
		this.setDividerLocation(0.5);
		this.setResizeWeight(0.5);
		this.setLeftComponent(getJPanelRealTimeProperties());
		this.setRightComponent(getJTabbedPane());
	}
	
	private PropertiesPanel getJPanelRealTimeProperties() {
		if (jPanelRealTimeProperties == null) {
			jPanelRealTimeProperties = new PropertiesPanel(null, "Real-Time State", true);
		}
		return jPanelRealTimeProperties;
	}
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			jTabbedPane.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return jTabbedPane;
	}
	
	private TechnicalSystemStatePanel getTechnicalSystemStatePanel(EomController eomController) {
		if (technicalSystemStatePanel==null) {
			
			OptionModelController omc = null;
			ScheduleController sc = null;
			if (eomController instanceof OptionModelController) {
				omc = (OptionModelController) eomController;
				sc = omc.getEvaluationProcess().getScheduleController();
			} else if (eomController instanceof GroupController) {
				GroupController gc = (GroupController) eomController;
				omc = gc.getGroupOptionModelController();
				sc = omc.getEvaluationProcess().getScheduleController();
			} else if (eomController instanceof ScheduleController) {
				sc = (ScheduleController) eomController;
				omc = sc.getOptionModelController();
			}
			
			technicalSystemStatePanel = new TechnicalSystemStatePanel(omc, sc, null, true);
			technicalSystemStatePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			this.getJTabbedPane().addTab(" System State ", technicalSystemStatePanel);
		}
		return technicalSystemStatePanel;
	}

	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		RealTimeInformation rtInfo = JPanelRealTimeInformation.this.swingUiModelInterface.getEnergyAgent().getRealTimeInformation();
		JPanelRealTimeInformation.this.getJPanelRealTimeProperties().setProperties(rtInfo);
		JPanelRealTimeInformation.this.getTechnicalSystemStatePanel(rtInfo.getEomController()).setTechnicalSystemStateTime(rtInfo.getTechnicalSystemStateEvaluation());
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
