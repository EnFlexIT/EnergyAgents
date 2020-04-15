package de.enflexit.ea.core.ops;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import agentgui.core.application.Application;
import agentgui.core.jade.Platform;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.dataModel.cea.ConversationID;
import de.enflexit.ea.core.dataModel.visualizationMessaging.FieldVisualizationService;
import de.enflexit.ea.core.ops.OpsControllerEvent.OpsControllerEvents;
import de.enflexit.ea.core.ops.agent.CeaConnectorAgent;
import de.enflexit.ea.core.ops.fieldDataRequest.agent.FieldDataRequestAgent;
import de.enflexit.ea.core.ops.gui.JFrameOpsControl;
import hygrid.ops.ontology.FieldDataRequest;
import jade.core.AID;
import jade.core.Runtime;

/**
 * The Class OpsController serves as central access point to control the interaction
 * between visualization and field agents.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class OpsController implements FieldVisualizationService {

	public static final String CEA_CONNECTOR_AGENT_NAME = "CeaConnectorAgent";
	
	private BundleContext bundleContext;
	private ServiceRegistration<?> serviceRegistration;
	
	private ArrayList<OpsControllerListener> opsListener;
	
	private JFrameOpsControl jFrameOpsControl; 
	private CeaConnectorAgent ceaConnectorAgent;
	private AID ceaAID;
	
	private static OpsController thisInstance; 
	private OpsController() {}
	/**
	 * Gets the single instance of OpsController.
	 * @return single instance of OpsController
	 */
	public static OpsController getInstance() {
		if (thisInstance==null) {
			thisInstance = new OpsController();
		}
		return thisInstance;
	}
	
	/**
	 * Returns the JFrame for the OPS control.
	 * @return the j frame ops control
	 */
	public JFrameOpsControl getJFrameOpsControl() {
		if (jFrameOpsControl==null) {
			jFrameOpsControl = new JFrameOpsControl(this);
		}
		return jFrameOpsControl;
	}
	
	/**
	 * Sets the current {@link CeaConnectorAgent}
	 * @param ceaConnectorAgent the CeaConnectorAgent
	 */
	public void setCeaConnectorAgent(CeaConnectorAgent ceaConnectorAgent) {
		this.ceaConnectorAgent = ceaConnectorAgent;
		if (this.ceaConnectorAgent!=null) {
			this.ceaConnectorAgent.setEnabledO2ACommunication(true, 0);
		}
	}
	/**
	 * Returns the current {@link CeaConnectorAgent}.
	 * @return the CeaConnectorAgent
	 */
	public CeaConnectorAgent getCeaConnectorAgent() {
		return this.ceaConnectorAgent;
	}
	/**
	 * Sends an CEA message using the O2A interfaces of the agent.
	 * @param messageObject the message object
	 */
	public void sendCeaO2AMessage(Object messageObject) {
		if (this.getCeaConnectorAgent()!=null) {
			try {
				this.getCeaConnectorAgent().putO2AObject(messageObject, false);
			} catch (InterruptedException inEx) {
				inEx.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the bundle context.
	 * @return the bundle context
	 */
	private BundleContext getBundleContext() {
		if (bundleContext==null) {
			bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		}
		return bundleContext;
	}
	/**
	 * Register field visualization service.
	 */
	private void registerFieldVisualizationService() {
		this.serviceRegistration = this.getBundleContext().registerService(FieldVisualizationService.class.getName(), this, null);
	}
	/**
	 * Unregister field visualization service.
	 */
	private void unregisterFieldVisualizationService() {
		if (this.serviceRegistration!=null) {
			this.getBundleContext().ungetService(serviceRegistration.getReference());
			this.serviceRegistration = null;	
		}
	}
	
	/**
	 * Starts the JADE platform (if not already running) and the {@link CeaConnectorAgent}.
	 * @return true, if successful
	 */
	public boolean startCeaConnectorAgent() {
		
		boolean successful = false;
		if (this.isConnected()==false) {
			// --- Add Listener to Jade platform --------------------
			Runtime jadeRuntime = Runtime.instance();
			jadeRuntime.invokeOnTermination(new Runnable() {
				public void run() {
					// --- Inform listener about JADE shutdown ------
					OpsController.this.informListener(OpsControllerEvents.OPS_DISCONNECTED);
					// --- Unregister FieldVisualizationService -----
					OpsController.this.unregisterFieldVisualizationService();
				}
			});
			
			// --- Start JADE ---------------------------------------
			Platform jadePlatform = Application.getJadePlatform();
			this.setStatusBarMessage("Starting JADE ...");
			if (jadePlatform.start(false)==true) {
				
				if (jadePlatform.isAgentRunning(CEA_CONNECTOR_AGENT_NAME)==false) {
					// --- Start CEA-Connector Agent ----------------
					this.setStatusBarMessage("Starting CeaConnectorAgent ...");
					jadePlatform.startAgent(CEA_CONNECTOR_AGENT_NAME, CeaConnectorAgent.class.getName());
					while (jadePlatform.isAgentRunning(CEA_CONNECTOR_AGENT_NAME)==false) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException iEx) {
							iEx.printStackTrace();
						}
					}	

				} else {
					// --- Invoke connection request ----------------
					this.sendCeaO2AMessage(ConversationID.OPS_CONNECTING_REQUEST);
					
				}
				successful = true;
				
				// --- Register FieldVisualizationService -----------
				this.registerFieldVisualizationService();
			}
		}
		return successful;
	}
	/**
	 * Disconnect from field area.
	 * @return true, if successful
	 */
	public boolean disconnectFromFieldArea() {
		
		boolean successful = false;
		if (this.isConnected()==true) {
			Application.getJadePlatform().stop();
			successful = true;
			// --- Reset local variables ------------------
			this.setCeaAID(null);
			this.informListener(OpsControllerEvents.OPS_DISCONNECTED);
			this.setStatusBarMessage("Ready");
		}
		return successful;
	}
	/**
	 * Checks if the OPS is connected to the field area.
	 * @return true, if is connected
	 */
	public boolean isConnected() {
		
		boolean isFiledConnected = false;
		if (Application.getJadePlatform().isMainContainerRunning()==true) {
			if (Application.getJadePlatform().isAgentRunning(CEA_CONNECTOR_AGENT_NAME)==true) {
				if (this.getCeaAID()!=null) {
					isFiledConnected = true;
				}
			}
		}
		return isFiledConnected;
	}
	
	
	/**
	 * Sets the CEA AID and informs all listener that the connection to the OPS was established.
	 * @param ceaAID the new CEA AID
	 */
	public void setCeaAID(AID ceaAID) {
		this.ceaAID = ceaAID;
		if (this.ceaAID!=null) {
			System.out.println("[" + this.getClass().getSimpleName() + "] Connected to CEA: " + this.getCeaAIDdescription());
			// --- Inform listener --------------------
			this.informListener(OpsControllerEvents.OPS_CONNECTED);
		}
	}
	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.visualizationMessaging.FieldVisualizationService#getCeaAID()
	 */
	public AID getCeaAID() {
		return ceaAID;
	}
	/**
	 * Returns a textual description of the CEA's AID .
	 * @return the CEA AID description
	 */
	public String getCeaAIDdescription() {
		
		String desc = "The AID of the CEA is not defined.";
		if (this.getCeaAID()!=null) {
			// --- Try to resolve a MTP address ----------- 
			String mtpAddress = null;
			String[] addresses = this.getCeaAID().getAddressesArray();
			if (addresses.length>0) {
				mtpAddress = addresses[0];
			}
			// --- Define the AID description -------------
			desc = this.getCeaAID().getName();
			if (mtpAddress!=null) {
				desc += " (" + mtpAddress + ")"; 
			}
		}
		return desc;
	}

	
	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.visualizationMessaging.FieldVisualizationService#setStatusBarMessage(java.lang.String)
	 */
	@Override
	public void setStatusBarMessage(final String statusInfo) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				OpsController.this.getJFrameOpsControl().setStatusText(statusInfo);
			}
		});
	}
	/* (non-Javadoc)
	 * @see hygrid.globalDataModel.visualizationMessaging.FieldVisualizationService#setStatusBarMessageReady()
	 */
	@Override
	public void setStatusBarMessageReady() {
		this.setStatusBarMessage("Ready");
	}
	
	/**
	 * Informs all listener about the actual event within of this controller.
	 * @param actualEvent the actual event
	 */
	private void informListener(OpsControllerEvents actualEvent) {
		OpsControllerEvent opsEvent = new OpsControllerEvent(actualEvent);
		for (int i = 0; i < this.getOpsControllerListener().size(); i++) {
			this.getOpsControllerListener().get(i).onOpsControllerEvent(opsEvent);
		}
	}
	/**
	 * Return the registered {@link OpsControllerListener}.
	 * @return the ops controller listener
	 */
	public ArrayList<OpsControllerListener> getOpsControllerListener() {
		if (opsListener==null) {
			opsListener = new ArrayList<>();
		}
		return opsListener;
	}
	/**
	 * Adds the specified {@link OpsControllerListener} to the list of listener.
	 * @param listener the listener
	 */
	public void addOpsControllerListener(OpsControllerListener listener) {
		if (this.getOpsControllerListener().contains(listener)==false) {
			this.getOpsControllerListener().add(listener);
		}
	}
	/**
	 * Removes the specified {@link OpsControllerListener} to the list of listener.
	 * @param listener the listener
	 * @return true, if successful
	 */
	public boolean removeOpsControllerListener(OpsControllerListener listener) {
		return this.getOpsControllerListener().remove(listener);
	}
	
	
	// ------------------------------------------------------------------------
	// --- Methods related to the handling of field data requests -------------
	// ------------------------------------------------------------------------
	
	/**
	 * Starts a field data request agent to perform a specific field data request.
	 * @param ceaAid the {@link AID} of the {@link CentralExecutiveAgent}
	 * @param dataRequest the field data request specification
	 * @return true, if successful
	 */
	public void startFieldDataRequestAgent(FieldDataRequest dataRequest) {

		// --- An established CEA connection is required for performing field data requests
		if (this.isConnected()==true) {
			Platform jadePlatform = Application.getJadePlatform();
			
			// --- Find an available name for the agent -------------
			String agentName = FieldDataRequestAgent.DEFAULT_AGENT_NAME;
			int suffix = 1;
			while (jadePlatform.isAgentRunning(agentName)==true) {
				agentName = FieldDataRequestAgent.DEFAULT_AGENT_NAME + suffix;
				suffix++;
			}
			
			// --- Prepare the start arguments ----------------------
			Object[] startArgs = new Object[2];
			startArgs[0] = this.getCeaAID();
			startArgs[1] = dataRequest;
			
			jadePlatform.startAgent(agentName, FieldDataRequestAgent.class.getName(), startArgs);
			
		}
		
	}
	
}
