package de.enflexit.ea.core.globalDataModel.visualizationMessaging;

import java.io.IOException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import agentgui.core.application.Application;
import de.enflexit.ea.core.globalDataModel.cea.ConversationID;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;

/**
 * The Class FieldVisualizationMessagingHelper provides some static help methods. 
 * for the visualization messaging.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class FieldVisualizationMessagingHelper {

	public static final String WRAPPER_PARAMETER_MESSAGE_RECEIVER_LOCAL_NAMES = "msgReceiverLocalNames"; 
	
	/**
	 * Returns the local bundle context.
	 * @return the bundle context
	 */
	private static BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(FieldVisualizationMessagingHelper.class).getBundleContext();
	}
	/**
	 * Returns the currently registered field visualization service.
	 * @return the field visualization service
	 */
	public static FieldVisualizationService getFieldVisualizationService() {
		
		FieldVisualizationService fvs = null;
		
		BundleContext context = getBundleContext();
		ServiceReference<?> reference = context.getServiceReference(FieldVisualizationService.class.getName());
		if (reference!=null) {
			Object serviceObject = context.getService(reference);
			if (serviceObject!=null && serviceObject instanceof FieldVisualizationService) {
				fvs = (FieldVisualizationService) context.getService(reference);
			}
		}
		return fvs;
	}
	/**
	 * Checks if is field connected.
	 * @return true, if is field connected
	 */
	public static boolean isFieldConnected() {
		return (getFieldVisualizationService()!=null);
	}

	/**
	 * In case that the visualization is driven by the OPS (which is a FieldVisualizationService too), the 
	 * specified message will be wrapped to transfer the message via CEA to the actual agent. In case that
	 * no FieldVisualizationService can be found, simply the original message will be returned.
	 *
	 * @param message the actual message to send
	 * @return the agent aid for visualization message
	 */
	public static ACLMessage getLocalOrWrappedFieldMessage(ACLMessage message) {
		
		// --------------------------------------------------------------------
		// --- Check for receiver first ---------------------------------------
		// --------------------------------------------------------------------
		if (message.getAllReceiver().hasNext()==false) {
			System.err.println("[" + FieldVisualizationMessagingHelper.class.getSimpleName() + "] The specified ACLMessage does not contain any message receiver!");
			return message;
		}
		
		// --------------------------------------------------------------------
		// --- Check if the message has to be wrapped -------------------------
		// --------------------------------------------------------------------
		ACLMessage fieldMessage = message;
		FieldVisualizationService fVisService = getFieldVisualizationService();
		if (fVisService!=null) {
			// ----------------------------------------------------------------
			// --- Wrap the message to send via CEA as proxy ------------------
			// ----------------------------------------------------------------
			fieldMessage = new ACLMessage(ACLMessage.PROPAGATE);
			fieldMessage.setConversationId(ConversationID.OPS_FIELD_AGENT_MESSAGE.toString());
			fieldMessage.addReceiver(fVisService.getCeaAID());
			
			// --- Put local names of receiver to message parameters ----------
			String receiverLocalNames = "";
			Iterator receiverIterator = message.getAllReceiver();
			while (receiverIterator.hasNext()) {
				AID receiver = (AID) receiverIterator.next();
				if (receiverLocalNames.length()==0) {
					receiverLocalNames = receiver.getLocalName(); 
				} else {
					receiverLocalNames += "," + receiver.getLocalName();
				}
			}
			fieldMessage.addUserDefinedParameter(WRAPPER_PARAMETER_MESSAGE_RECEIVER_LOCAL_NAMES, receiverLocalNames);

			// --- Pack the original message into the content object ----------
			try {
				fieldMessage.setContentObject(message);
				
			} catch (IOException ioEx) {
				System.err.println("[" + FieldVisualizationMessagingHelper.class.getSimpleName() + "] Could not wrap field message!");
				fieldMessage = message;
				ioEx.printStackTrace();
			}
		}
		return fieldMessage;
	}
	
	/**
	 * Returns a field message reply that first, is a regular message reply {@link ACLMessage#createReply()}, but additionally 
	 * the user defined properties will be added to the reply, which allows the central executive agent to forward a message 
	 * from a field agent to the here specified display agent, running in the OPS. 
	 *
	 * @param messageToReply the message to reply
	 * @return the field message reply
	 */
	public static ACLMessage createFieldMessageReply(ACLMessage messageToReply) {
		// --- Create the regular reply -------------------
		ACLMessage fmReply = messageToReply.createReply();
		// --- Copy the user defined parameter to reply ---
		fmReply.setAllUserDefinedParameters((Properties) messageToReply.getAllUserDefinedParameters().clone());
		return fmReply;
	}
	
	/**
	 * Sets the specified status bar message.
	 * @param statusText the new status bar message
	 */
	public static void setStatusBarMessage(String statusText) {
		FieldVisualizationService fVisService =  getFieldVisualizationService();
		if (fVisService!=null) {
			fVisService.setStatusBarMessage(statusText);
		} else {
			Application.setStatusBarMessage(statusText);
		}
	}
	/**
	 * Sets 'ready' as status bar message.
	 */
	public static void setStatusBarMessageReady() {
		FieldVisualizationService fVisService =  getFieldVisualizationService();
		if (fVisService!=null) {
			fVisService.setStatusBarMessageReady();
		} else {
			Application.setStatusBarMessageReady();
		}
	}
	
}
