package de.enflexit.ea.core.ops.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import agentgui.core.application.Application;
import de.enflexit.db.hibernate.SessionFactoryMonitor.SessionFactoryState;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.dataModel.opsOntology.FieldDataRequest;
import de.enflexit.ea.core.dataModel.opsOntology.LongValue;
import de.enflexit.ea.core.dataModel.opsOntology.ScheduleRangeDefinition;
import de.enflexit.ea.core.ops.OpsController;
import de.enflexit.ea.core.ops.OpsControllerEvent;
import de.enflexit.ea.core.ops.OpsControllerListener;
import de.enflexit.ea.core.ops.fieldDataRequest.gui.FieldDataRequestDialog;
import de.enflexit.eom.database.EomDatabaseConnection;

/**
 * The Class JFrameOpsControlTools provide the menus and the toolbar to control the OPS interaction.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class JFrameOpsControlTools implements ActionListener, OpsControllerListener {

	private OpsController opsController;
	
	private JMenuBar jMenuBarOpsControl;
	
	private JMenu jMenuConnection;
	private JMenuItem jMenuItemConnect;
	private JMenuItem jMenuItemDisconnect;
	
	private JMenu jMenuCeaInteraction;
	private JMenuItem jMenuItemCeaCheckCommunication;
	private JMenuItem jMenuItemCeaUpdateOnSiteInstallations;
	private JMenuItem jMenuItemRequestFieldData;
	private JMenuItem jMenuItemStartLiveMonitoring;
	private JMenuItem jMenuItemStopLiveMonitoring;
	
	private JToolBar jToolBarOpsControl;
	private JButton jButtonConnect;
	private JButton jButtonDisConnect;
	private JButton jButtonFieldDataRequest;
	private JButton jButtonStartLiveMonitoring;
	private JButton jButtonStopLiveMonitoring;
	
	/**
	 * Instantiates the class for the control tools .
	 * @param opsController the required OPS controller
	 */
	public JFrameOpsControlTools(OpsController opsController) {
		if (opsController==null) {
			throw new NullPointerException("The OPS controller is not allowed to be null!");
		}
		this.opsController = opsController;
		this.opsController.addOpsControllerListener(this);
		this.setOpsConnected();
	}
	
	// --------------------------------------------------------------
	// --- From elements for the menus ------------------------------
	// --------------------------------------------------------------	
	/**
	 * Returns the JMenu bar for the ops control.
	 * @return the JMenu bar ops control
	 */
	public JMenuBar getJMenuBarOpsControl() {
		if (jMenuBarOpsControl==null) {
			jMenuBarOpsControl = new JMenuBar();
			jMenuBarOpsControl.add(this.getJMenuConnection());
			jMenuBarOpsControl.add(this.getJMenuCeaInteraction());
		}
		return jMenuBarOpsControl;
	}

	// --- Menu Connection ------------------------------------------
	private JMenu getJMenuConnection() {
		if (jMenuConnection==null) {
			jMenuConnection = new JMenu("Connection");
			jMenuConnection.add(this.getJMenuItemConnect());
			jMenuConnection.add(this.getJMenuItemDisconnect());
		}
		return jMenuConnection;
	}	
	private JMenuItem getJMenuItemConnect() {
		if (jMenuItemConnect==null) {
			jMenuItemConnect = new JMenuItem("Connect to CEA");
			jMenuItemConnect.setIcon(ImageHelper.getInternalImageIcon("Play.png"));
			jMenuItemConnect.addActionListener(this);
		}
		return jMenuItemConnect;
	}
	private JMenuItem getJMenuItemDisconnect() {
		if (jMenuItemDisconnect==null) {
			jMenuItemDisconnect = new JMenuItem("Disconnect from CEA");
			jMenuItemDisconnect.setIcon(ImageHelper.getInternalImageIcon("Stop.png"));
			jMenuItemDisconnect.addActionListener(this);
		}
		return jMenuItemDisconnect;
	}
	
	// --- Menu CEA-Request -----------------------------------------
	private JMenu getJMenuCeaInteraction() {
		if (jMenuCeaInteraction==null) {
			jMenuCeaInteraction = new JMenu("CEA - Interaction");
			jMenuCeaInteraction.add(this.getJMenuItemCeaCheckCommunication());
			jMenuCeaInteraction.add(this.getJMenuItemCeaUpdateOnSiteInstallations());
			jMenuCeaInteraction.addSeparator();
			jMenuCeaInteraction.add(this.getJMenuItemRequestFieldData());
			jMenuCeaInteraction.addSeparator();
			jMenuCeaInteraction.add(this.getJMenuItemStartLiveMonitoring());
			jMenuCeaInteraction.add(this.getJMenuItemStopLiveMonitoring());
		}
		return jMenuCeaInteraction;
	}	
	private JMenuItem getJMenuItemCeaCheckCommunication() {
		if (jMenuItemCeaCheckCommunication==null) {
			jMenuItemCeaCheckCommunication = new JMenuItem("Check Communication with CEA");
			jMenuItemCeaCheckCommunication.addActionListener(this);
		}
		return jMenuItemCeaCheckCommunication;
	}
	private JMenuItem getJMenuItemCeaUpdateOnSiteInstallations() {
		if (jMenuItemCeaUpdateOnSiteInstallations==null) {
			jMenuItemCeaUpdateOnSiteInstallations = new JMenuItem("Update on-site Installations");
			jMenuItemCeaUpdateOnSiteInstallations.addActionListener(this);
		}
		return jMenuItemCeaUpdateOnSiteInstallations;
	}
	
	/**
	 * Gets the j menu item request field data.
	 * @return the j menu item request field data
	 */
	private JMenuItem getJMenuItemRequestFieldData() {
		if (jMenuItemRequestFieldData==null) {
			jMenuItemRequestFieldData = new JMenuItem("Request Field Data");
			jMenuItemRequestFieldData.setToolTipText("Request logged data from deployed field agents");
			jMenuItemRequestFieldData.addActionListener(this);
		}
		return jMenuItemRequestFieldData;
	}
	
	/**
	 * Gets the j menu item start live monitoring.
	 * @return the j menu item start live monitoring
	 */
	private JMenuItem getJMenuItemStartLiveMonitoring() {
		if (jMenuItemStartLiveMonitoring==null) {
			jMenuItemStartLiveMonitoring = new JMenuItem("Start Live Monitoring");
			jMenuItemStartLiveMonitoring.setToolTipText("Get and display live data from the deployed field agents");
			jMenuItemStartLiveMonitoring.setIcon(ImageHelper.getInternalImageIcon("LiveMonitoringStart.png"));
			jMenuItemStartLiveMonitoring.addActionListener(this);
		}
		return jMenuItemStartLiveMonitoring;
	}
	
	/**
	 * Gets the j menu item stop live monitoring.
	 * @return the j menu item stop live monitoring
	 */
	private JMenuItem getJMenuItemStopLiveMonitoring() {
		if (jMenuItemStopLiveMonitoring==null) {
			jMenuItemStopLiveMonitoring = new JMenuItem("Stop Live Monitoring");
			jMenuItemStopLiveMonitoring.setToolTipText("Stop the live monitoring");
			jMenuItemStopLiveMonitoring.setIcon(ImageHelper.getInternalImageIcon("LiveMonitoringStop.png"));
			jMenuItemStopLiveMonitoring.addActionListener(this);
		}
		return jMenuItemStopLiveMonitoring;
	}
	
	
	// --------------------------------------------------------------
	// --- From elements for the toolbar ----------------------------
	// --------------------------------------------------------------	
	/**
	 * Returns the JToolBar for the ops control.
	 * @return the j tool bar ops control
	 */
	public JToolBar getJToolBarOpsControl() {
		if (jToolBarOpsControl==null) {
			jToolBarOpsControl = new JToolBar();
			jToolBarOpsControl.setPreferredSize(new Dimension(400, 26));
			jToolBarOpsControl.setFloatable(false);
			jToolBarOpsControl.add(this.getJButtonConnect());
			jToolBarOpsControl.add(this.getJButtonDisconnect());
			jToolBarOpsControl.addSeparator();
			jToolBarOpsControl.add(this.getJButtonFieldDataRequest());
			jToolBarOpsControl.addSeparator();
			jToolBarOpsControl.add(this.getJButtonStartLiveMonitoring());
			jToolBarOpsControl.add(this.getJButtonStopLiveMonitoring());
		}
		return jToolBarOpsControl;
	}
	
	/**
	 * Gets the j button connect.
	 * @return the j button connect
	 */
	private JButton getJButtonConnect() {
		if (jButtonConnect==null) {
			jButtonConnect = new JButton();
			jButtonConnect.setToolTipText("Connect to CEA");
			jButtonConnect.setIcon(ImageHelper.getInternalImageIcon("Play.png"));
			jButtonConnect.addActionListener(this);
		}
		return jButtonConnect;
	}
	
	/**
	 * Gets the j button disconnect.
	 * @return the j button disconnect
	 */
	private JButton getJButtonDisconnect() {
		if (jButtonDisConnect==null) {
			jButtonDisConnect = new JButton();
			jButtonDisConnect.setToolTipText("Disconnect from CEA");
			jButtonDisConnect.setIcon(ImageHelper.getInternalImageIcon("Stop.png"));
			jButtonDisConnect.addActionListener(this);
		}
		return jButtonDisConnect;
	}
	
	/**
	 * Gets the j button field data request.
	 * @return the j button field data request
	 */
	private JButton getJButtonFieldDataRequest() {
		if (jButtonFieldDataRequest==null) {
			jButtonFieldDataRequest = new JButton();
			jButtonFieldDataRequest.setToolTipText("Send user-configured field data request");
			jButtonFieldDataRequest.setIcon(ImageHelper.getInternalImageIcon("FieldData.png"));
			jButtonFieldDataRequest.addActionListener(this);
		}
		return jButtonFieldDataRequest;
	}
	
	/**
	 * Gets the j button start live monitoring.
	 * @return the j button start live monitoring
	 */
	private JButton getJButtonStartLiveMonitoring() {
		if (jButtonStartLiveMonitoring==null) {
			jButtonStartLiveMonitoring = new JButton();
			jButtonStartLiveMonitoring.setToolTipText("Start live monitoring");
			jButtonStartLiveMonitoring.setIcon(ImageHelper.getInternalImageIcon("LiveMonitoringStart.png"));
			jButtonStartLiveMonitoring.addActionListener(this);
		}
		return jButtonStartLiveMonitoring;
	}
	
	/**
	 * Gets the j button stop live monitoring.
	 * @return the j button stop live monitoring
	 */
	private JButton getJButtonStopLiveMonitoring() {
		if (jButtonStopLiveMonitoring==null) {
			jButtonStopLiveMonitoring = new JButton();
			jButtonStopLiveMonitoring.setToolTipText("Stop live monitoring");
			jButtonStopLiveMonitoring.setIcon(ImageHelper.getInternalImageIcon("LiveMonitoringStop.png"));
			jButtonStopLiveMonitoring.addActionListener(this);
		}
		return jButtonStopLiveMonitoring;
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource()==this.getJMenuItemConnect() || ae.getSource()==this.getJButtonConnect()) {
			// --- Connect to CEA, if not already done --------------
			this.executeWithinThread(OpsExecutable.START_CEA_CONNECTOR_AGENT);
			
		} else if (ae.getSource()==this.getJMenuItemDisconnect() || ae.getSource()==this.getJButtonDisconnect()) {
			// --- Disconnect from the CEA --------------------------
			this.executeWithinThread(OpsExecutable.DISCONNECT_FROM_FIELD);
			
		} else if (ae.getSource()==this.getJMenuItemCeaCheckCommunication()) {
			this.opsController.sendCeaO2AMessage(ConversationID.OPS_CONNECTING_REQUEST);
		
		} else if (ae.getSource()==this.getJMenuItemCeaUpdateOnSiteInstallations()) {
			this.opsController.sendCeaO2AMessage(ConversationID.OPS_UPDATE_ON_SITE_INSTALLATIONS);
			
		} else if (ae.getSource()==this.getJMenuItemRequestFieldData() || ae.getSource()==this.getJButtonFieldDataRequest()) {
			FieldDataRequest dataRequest = this.prepareDataRequest();
			if (dataRequest!=null) {
				Object[] args = new Object[1];
				args[0] = dataRequest;
				this.executeWithinThread(OpsExecutable.REQUEST_FIELD_DATA, args);
			}
			
		} else if (ae.getSource()==this.getJMenuItemStartLiveMonitoring() || ae.getSource()==this.getJButtonStartLiveMonitoring()) {
			this.opsController.sendCeaO2AMessage(ConversationID.OPS_LIVE_MONITORING_START);
			this.setLiveMonitoringCommandState(true);
			
		} else if (ae.getSource()==this.getJMenuItemStopLiveMonitoring() || ae.getSource()==this.getJButtonStopLiveMonitoring()) {
			this.opsController.sendCeaO2AMessage(ConversationID.OPS_LIVE_MONITORING_STOP);
			this.setLiveMonitoringCommandState(false);
		}
	}
	
	/**
	 * Prepare data request.
	 * @return the field data request
	 */
	private FieldDataRequest prepareDataRequest() {
		FieldDataRequest dataRequest = null;
		FieldDataRequestDialog requestDialog = new FieldDataRequestDialog();
		requestDialog.setVisible(true);
		if (requestDialog.isCanceled()==false) {
			dataRequest = new FieldDataRequest();
			List<String> agentIDs = requestDialog.getSelectedAgentIDs();
			for (int i=0; i<agentIDs.size(); i++) {
				dataRequest.getAgentIDs().add(agentIDs.get(i));
			}
			
			ScheduleRangeDefinition rangeDefinition = new ScheduleRangeDefinition();
			switch(requestDialog.getSystemStateRangeType()) {
			case AllStates:
				rangeDefinition.setIncludeAllStates(true);
				break;
			case TimeRange:
				LongValue timestampFrom = new LongValue();
				timestampFrom.setLongValue(requestDialog.getTimeRangeFrom());
				rangeDefinition.setTimestampFrom(timestampFrom);
				LongValue timestampTo = new LongValue();
				timestampTo.setLongValue(requestDialog.getTimeRangeTo());
				rangeDefinition.setTimestampTo(timestampTo);
				break;
			case StartDateAndNumber:
				LongValue timestampFirstState = new LongValue();
				timestampFirstState.setLongValue(requestDialog.getTimeRangeFrom());
				rangeDefinition.setTimestampFrom(timestampFirstState);
				rangeDefinition.setNumberOfStates(requestDialog.getNumberOfStatesToLoad());
				break;
			}
			
			dataRequest.setScheduleRangeDefinition(rangeDefinition);
			
			// --- Always using current setup //TODO make selectable?
			dataRequest.setSetup(Application.getProjectFocused().getSimulationSetupCurrent());
		}
		
		return dataRequest;
		
	}

	/**
	 * Private enumeration to distinguish an OPS executable.
	 */
	private enum OpsExecutable {
		START_CEA_CONNECTOR_AGENT,
		DISCONNECT_FROM_FIELD,
		REQUEST_FIELD_DATA
	}
	
	/**
	 * Executes the specified OPS executable throw an own thread.
	 * @param execute the execute
	 */
	private void executeWithinThread(final OpsExecutable execute) {
		this.executeWithinThread(execute, null);
	}
	
	/**
	 * Executes the specified OPS executable throw an own thread.
	 * @param execute the execute
	 * @param parameters the parameters for the executable (if required)
	 */
	private void executeWithinThread(final OpsExecutable execute, Object[] parameters) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				switch (execute) {
				case START_CEA_CONNECTOR_AGENT:
					// --- Connect to CEA, if not already done ----------------
					JFrameOpsControlTools.this.opsController.startCeaConnectorAgent();
					break;

				case DISCONNECT_FROM_FIELD:
					// --- Disconnect from the CEA ----------------------------
					JFrameOpsControlTools.this.opsController.disconnectFromFieldArea();
					break;
					
				case REQUEST_FIELD_DATA:
					// --- Start an agent to handle the field data request ----
					JFrameOpsControlTools.this.opsController.startFieldDataRequestAgent((FieldDataRequest) parameters[0]);
					break;
				}
				
			}
		}, "OPS-Executer").start();
	}
	
	
	/**
	 * Sets the OPS visualization connected.
	 */
	private void setOpsConnected() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				boolean isConnected = JFrameOpsControlTools.this.opsController.isConnected();
				
				JFrameOpsControlTools.this.getJMenuItemConnect().setEnabled(!isConnected);
				JFrameOpsControlTools.this.getJButtonConnect().setEnabled(!isConnected);
				
				JFrameOpsControlTools.this.getJMenuItemDisconnect().setEnabled(isConnected);
				JFrameOpsControlTools.this.getJButtonDisconnect().setEnabled(isConnected);
				
				JFrameOpsControlTools.this.getJMenuCeaInteraction().setEnabled(isConnected);
				
				// --- Database requests require CEA connection and database connection -----------
				boolean dbAvailable = (EomDatabaseConnection.getInstance().getSessionFactoryMonitor().getSessionFactoryState()==SessionFactoryState.Created);
				JFrameOpsControlTools.this.getJMenuItemRequestFieldData().setEnabled(isConnected&&dbAvailable);
				JFrameOpsControlTools.this.getJButtonFieldDataRequest().setEnabled(isConnected&&dbAvailable);
				
				JFrameOpsControlTools.this.getJMenuItemStartLiveMonitoring().setEnabled(isConnected);
				JFrameOpsControlTools.this.getJButtonStartLiveMonitoring().setEnabled(isConnected);
				
				JFrameOpsControlTools.this.getJMenuItemStopLiveMonitoring().setEnabled(false);
				JFrameOpsControlTools.this.getJButtonStopLiveMonitoring().setEnabled(false);
				
			}
		});
		
	}
	
	/**
	 * Enable/disable the live monitoring related commands according to the current state of the live monitoring.
	 * @param isLiveMonitoringRunning the current state of the live monitoring
	 */
	private void setLiveMonitoringCommandState(boolean isLiveMonitoringRunning) {
		// --- Enable start commands if the live monitoring is currently not running 
		this.getJButtonStartLiveMonitoring().setEnabled(!isLiveMonitoringRunning);
		this.getJMenuItemStartLiveMonitoring().setEnabled(!isLiveMonitoringRunning);
		// --- Enable stop commands if the live monitoring is currently running
		this.getJButtonStopLiveMonitoring().setEnabled(isLiveMonitoringRunning);
		this.getJMenuItemStopLiveMonitoring().setEnabled(isLiveMonitoringRunning);
	}
	
	/* (non-Javadoc)
	 * @see hygrid.ops.OpsControllerListener#onOpsControllerEvent(hygrid.ops.OpsControllerEvent)
	 */
	@Override
	public void onOpsControllerEvent(OpsControllerEvent controllerEvent) {
		
		switch (controllerEvent.getControllerEvent()) {
		case OPS_CONNECTED:
		case OPS_DISCONNECTED:
			this.setOpsConnected();
			break;
		}
	}

}

