package de.enflexit.ea.core.awbIntegration.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.settings.ComponentTypeSettings;
import org.awb.env.networkModel.visualisation.ACLMessageForwardingListener;
import org.awb.env.networkModel.visualisation.DisplayAgent;

import agentgui.core.application.Language;
import de.enflexit.eom.awb.adapter.EomAdapter;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

/**
 * This class extends the {@link EomAdapter} and provides a context menu entry 
 * for switching the underlying system on or off.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class SwitchableEomAdapter extends EomAdapter implements ACLMessageForwardingListener {
	
	public static final String CONVERSATION_ID_TOGGLE_OPERATION = "toggleOperation";
	public static final String CONVERSATION_ID_REQUEST_OPERATION_STATE = "requestOperationState";
	
	private boolean operating;
	private JCheckBoxMenuItem menuItemOperation;
	
	private AID myAgentAID;


	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#initialize()
	 */
	@Override
	public void initialize() {
		// --- Register for DisplayAgent notifications if running in a runtime visualization
		if (this.isRuntimeVisualization()==true) {
			DisplayAgent da = this.getDisplayAgent();
			if (da!=null) {
				da.addACLMessageForwardingListener(this);
			} else {
				System.err.println("DisplayAgent is null!");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enflexit.eom.awb.EomAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		if (this.isRuntimeVisualization()==true) {
			Vector<JComponent> popupMenuElements = new Vector<JComponent>();
			popupMenuElements.add(this.getJMenuItemOperation());
			return popupMenuElements;
		}
		return null;
	}
	/**
	 * Gets the JMenuItem for switching the operating.
	 * @return the JMenuItem for switching the operating
	 */
	private JMenuItem getJMenuItemOperation(){
		this.sendRequestOperaitonStateMessage();
		if (this.menuItemOperation == null){
			this.menuItemOperation = new JCheckBoxMenuItem(Language.translate("Operating", Language.EN));
			this.menuItemOperation.setIcon(new ImageIcon(SwitchableEomAdapter.class.getResource("/hygrid/plugin/gui/img/powerButton.png")));
			this.menuItemOperation.setSelected(operating);
			this.menuItemOperation.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					operating = !operating;
					if (isRuntimeVisualization()) {
						// --- Simulation is running - send an ACL message asking the agent to switch on/off -----
						sendSwitchingMessage();
					}
				}
			});
		}
		return this.menuItemOperation;
	}
	
	/**
	 * Checks if is operating.
	 * @return true, if is operating
	 */
	public boolean isOperating() {
		return operating;
	}
	/**
	 * Sets the operating.
	 * @param operating the new operating
	 */
	public void setOperating(boolean operating) {
		this.operating = operating;
	}
	
	/**
	 * Send an ACL message asking the agent to switch off.
	 */
	private void sendSwitchingMessage(){
		
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId(CONVERSATION_ID_TOGGLE_OPERATION);
		message.addReceiver(this.getMyAgentAID());
		
		try {
			message.setContentObject(new Boolean(operating));
		} catch (IOException e1) {
			System.err.println("Error setting content object for switch on/off message");
			e1.printStackTrace();
		}
		
		this.send(message);
	}
	
	/**
	 * Sends an {@link ACLMessage} to the agent to request the current operation state.
	 */
	private void sendRequestOperaitonStateMessage(){
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setConversationId(CONVERSATION_ID_REQUEST_OPERATION_STATE);
		message.addReceiver(this.getMyAgentAID());
		this.send(message);
	}
	
	/**
	 * Determines the AID of the EnergyAgent represented by this adapter based on the network component ID.
	 * @return the my agent AID
	 */
	private AID getMyAgentAID(){
		if (this.myAgentAID == null) {
			String agentLocalName = networkComponent.getId();
			this.myAgentAID = new AID(agentLocalName, AID.ISLOCALNAME);
		}
		return this.myAgentAID;
	}

	
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.visualisation.ACLMessageForwardingListener#forwardACLMessage(jade.lang.acl.ACLMessage)
	 */
	@Override
	public void forwardACLMessage(ACLMessage notification) {
		
		if (notification instanceof ACLMessage) {
			ACLMessage msg = (ACLMessage) notification;
			
			String netCompID = msg.getSender().getLocalName();
			NetworkComponent netComp = this.graphController.getNetworkModel().getNetworkComponent(netCompID);
			ComponentTypeSettings cts = this.graphController.getNetworkModel().getGeneralGraphSettings4MAS().getCurrentCTS().get(netComp.getType());
			if (cts.getAdapterClass().equals(this.getClass().getName())) {
				
				if (msg.getConversationId() != null && msg.getConversationId().equals(CONVERSATION_ID_REQUEST_OPERATION_STATE)) {
					
					// --- A response to an operating state request ---------------
					Boolean currentlyOperating;
					try {
						currentlyOperating = (Boolean) msg.getContentObject();
						this.operating = currentlyOperating;
						if (this.menuItemOperation!=null) {
							this.menuItemOperation.setSelected(this.operating);
						}
						
					} catch (UnreadableException e) {
						System.err.println("SwitchableEomAdapter, component " + this.getMyAgentAID().getLocalName() + ": Error extracting message content!");
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	
	
}
