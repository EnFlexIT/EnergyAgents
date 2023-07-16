package de.enflexit.ea.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.awb.env.networkModel.NetworkComponent;

import de.enflexit.common.properties.Properties;
import de.enflexit.common.properties.PropertiesPanel;
import de.enflexit.ea.core.AbstractEnergyAgent;
import de.enflexit.ea.core.AbstractInternalDataModel;
import de.enflexit.ea.core.behaviour.ControlBehaviourRT;
import de.enflexit.ea.core.planning.PlanningDispatcher;
import energy.evaluation.AbstractEvaluationStrategy;
import energy.evaluation.decision.AbstractDecider;
import energy.evaluation.decision.AbstractDecisionSwitch;

/**
 * The Class JPanelGeneralInformation.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class JPanelGeneralInformation extends JPanel {

	private static final long serialVersionUID = -1052414258048129787L;

	private JDialogEnergyAgent jDialogEnergyAgent;
	private PropertiesPanel propertiesPanel;
	
	
	/**
	 * Instantiates a new j panel general information.
	 * @param jDialogEnergyAgent the j dialog energy agent
	 */
	public JPanelGeneralInformation(JDialogEnergyAgent jDialogEnergyAgent) {
		this.jDialogEnergyAgent = jDialogEnergyAgent;
		this.initialize();
		this.setDisplayInformation();
	}
	/**
	 * Sets the display information.
	 */
	private void setDisplayInformation() {
		
		// --- Get the energy agent instance --------------
		AbstractEnergyAgent ea = this.jDialogEnergyAgent.getEnergyAgent();
		
		Properties eaProperties = new Properties();
		
		// ------------------------------------------------
		// --- General Agent Information ------------------
		// ------------------------------------------------
		String group = "Agent";
		// --- Agent state --------------------------------
		String aState = ea.getAgentState().getName();
		eaProperties.setStringValue(group + ".AgentState", aState);
		// --- Agent operating mode -----------------------
		eaProperties.setStringValue(group + ".OperatingMode", ea.getAgentOperatingMode().name());
		// --- IO behaviour -------------------------------
		eaProperties.setStringValue(group + ".IO-Class", ea.getEnergyAgentIO().getClass().getName());
		// --- Monitoring activated -----------------------
		eaProperties.setBooleanValue(group + ".MonitoringActive", ea.isActivatedMonitoring());
		// --- Logging activated --------------------------
		eaProperties.setBooleanValue(group + ".LoggingActive", ea.isActivatedLogWriter());
		eaProperties.setStringValue(group + ".LoggingDestintation", ea.getLoggingDestination().name());
		
		
		// ------------------------------------------------
		// --- Internal data model ------------------------
		// ------------------------------------------------
		AbstractInternalDataModel<?> intDM = ea.getInternalDataModel();
		group = "Internal Data Model";
		// --- NetworkComponent ---------------------------
		NetworkComponent netComp = intDM.getNetworkComponent();
		if (netComp!=null) {
			eaProperties.setStringValue(group + ".NetworkComponent", netComp.getId() + " (" + netComp.getType() + ")");
		}
		// --- Type of controlled system ------------------
		eaProperties.setStringValue(group + ".TypeOfControlledSystem", intDM.getTypeOfControlledSystem().name());
		// --- Type of controlled system ------------------
		eaProperties.setStringValue(group + ".CentralAgentAID", intDM.getCentralAgentAID().getName());
		// --- LoggingMode --------------------------------
		eaProperties.setStringValue(group + ".LoggingMode", intDM.getLoggingMode().name());
		
		
		// ------------------------------------------------
		// --- Real-Time control --------------------------
		// ------------------------------------------------
		group = "Real-Time Control";
		// --- Activated real-time control? ---------------
		eaProperties.setBooleanValue(group + ".Activated", ea.isExecutedControlBehaviourRT());
		if (ea.isExecutedControlBehaviourRT()==true) {
			ControlBehaviourRT rtBehav = ea.getControlBehaviourRT();
			// --- Get evaluation class -------------------
			AbstractEvaluationStrategy strategy = null;
			switch (intDM.getTypeOfControlledSystem()) {
			case TechnicalSystem:
				strategy = rtBehav.getRealTimeEvaluationStrategy();
				break;
			case TechnicalSystemGroup:
				strategy = rtBehav.getRealTimeGroupEvaluationStrategy();
				break;
			case None:
				strategy = null;
				break;
			}
			eaProperties.setStringValue(group + ".StrategyClass", strategy==null ? "No real time strategy defined." : strategy.getClass().getName());
			
			// --- Found a strategy -----------------------
			if (strategy!=null) {
				AbstractDecisionSwitch<?> dSwitch = strategy.getDecisionSwitch();
				if (dSwitch!=null) {
					// --- Found decision switch ----------
					group += ".DecisionSwitch";
					TreeMap<Integer, ?> dHierarchy = dSwitch.getDecisionHierarchy();
					for (Integer key : dHierarchy.keySet()) {
						AbstractDecider<?, ?> decider = (AbstractDecider<?, ?>) dHierarchy.get(key);
						eaProperties.setStringValue(group + ".Decider-" + key , decider.getDeciderName());
						eaProperties.setStringValue(group + ".Decider-" + key + "-Class" , decider.getClass().getName());
					}
				}
			}
		}
		
		
		// ------------------------------------------------
		// --- Planning activities ------------------------
		// ------------------------------------------------
		group = "Planning";
		// --- Planning possible --------------------------
		eaProperties.setBooleanValue(group + ".Possible", ea.isPlanningPossible());
		if (ea.isPlanningPossible()==true) {
			eaProperties.setBooleanValue(group + ".Activated", ea.isPlanningActivated());
			PlanningDispatcher pDispatcher = ea.getPlanningDispatcher();
			// TODO
			
		}

		
		// --- Put into UI --------------------------------
		this.getJPanelProperties().setProperties(eaProperties);
	}
	
	private void initialize() {
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		this.setLayout(gridBagLayout);
		
		GridBagConstraints gbc_PropertiesPanel = new GridBagConstraints();
		gbc_PropertiesPanel.fill = GridBagConstraints.BOTH;
		gbc_PropertiesPanel.gridx = 0;
		gbc_PropertiesPanel.gridy = 0;
		this.add(getJPanelProperties(), gbc_PropertiesPanel);
		
	}
	private PropertiesPanel getJPanelProperties() {
		if (propertiesPanel == null) {
			propertiesPanel = new PropertiesPanel(null, "Current Energy Agent Settings", true);
		}
		return propertiesPanel;
	}
}
