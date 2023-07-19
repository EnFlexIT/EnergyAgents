package de.enflexit.ea.ui;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.common.swing.AwbBasicTabbedPaneUI;
import de.enflexit.ea.core.ui.RealTimeInformation;
import energy.EomController;
import energy.OptionModelController;
import energy.evaluation.gui.TechnicalSystemStatePanel;
import energy.schedule.ScheduleController;
import energygroup.GroupController;

import java.awt.Font;

/**
 * The Class JPanelRealTimeInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelRealTimeInformation extends JSplitPane {

	private static final long serialVersionUID = 7714692252010988662L;
	
	private JDialogEnergyAgent jDialogEnergyAgent;
	
	private PropertiesPanel jPanelRealTimeProperties;
	private JTabbedPane jTabbedPane;
	private TechnicalSystemStatePanel technicalSystemStatePanel;
	
	/**
	 * Instantiates a new j panel real time information.
	 * @param jDialogEnergyAgent the j dialog energy agent
	 */
	public JPanelRealTimeInformation(JDialogEnergyAgent jDialogEnergyAgent) {
		this.jDialogEnergyAgent = jDialogEnergyAgent;
		this.initialize();
		this.setDisplayInformation(jDialogEnergyAgent);
	}

	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation(final JDialogEnergyAgent jDialogEnergyAgent) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RealTimeInformation rtInfo = jDialogEnergyAgent.getEnergyAgent().getRealTimeInformation();
				JPanelRealTimeInformation.this.getJPanelRealTimeProperties().setProperties(rtInfo);
				JPanelRealTimeInformation.this.getTechnicalSystemStatePanel(rtInfo.getEomController()).setTechnicalSystemStateTime(rtInfo.getTechnicalSystemStateEvaluation());
			}
		});
	}
	
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
			jTabbedPane.setUI(new AwbBasicTabbedPaneUI());
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
	
}
