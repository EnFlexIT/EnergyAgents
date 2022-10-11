package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.dataModel.ontology.ProxyAgentRegistrationRequest;
import de.enflexit.ea.core.dataModel.ontology.RemoteAgentRegistrationRequest;
import de.enflexit.ea.core.dataModel.ontology.TestbedAgentManagement;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class ProxyRegistrationBehaviour.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class ProxyRegistrationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -9149521619513782454L;

	private ACLMessage registrationRequest;
	
	/**
	 * Instantiates a new proxy registration behaviour.
	 * @param registrationRequest the registration request
	 */
	public ProxyRegistrationBehaviour(ACLMessage registrationRequest) {
		this.registrationRequest = registrationRequest;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		this.handleProxyRegistration();
	}
	
	/**
	 * Handle a proxy registration.
	 */
	private void handleProxyRegistration() {
		
		boolean debug = true;
		CentralExecutiveAgent cea = (CentralExecutiveAgent) this.myAgent;
		AID sender = this.registrationRequest.getSender();
		
		try {
			
			// --- Extract message content --------------------------
			ContentElement content = cea.getContentManager().extractContent(this.registrationRequest);
			if (content instanceof Action) {
				
				TestbedAgentManagement action = (TestbedAgentManagement) ((Action)content).getAction();
				if (action instanceof RemoteAgentRegistrationRequest) {
					// ----------------------------------------------
					// --- Remote agent -----------------------------
					// ----------------------------------------------
					if (debug==true) System.out.println(cea.getLocalName() + ": Received registration request from remote agent " + sender.getName());
					// --- Process certificate if included ----------
					RemoteAgentRegistrationRequest rar = (RemoteAgentRegistrationRequest) action;
					if (rar.getCertificateBase64()!=null) {
						cea.getInternalDataModel().handleCertificate(sender.getLocalName(), rar.getCertificateBase64());
					}
					// --- Add the AID to the directory -------------
					cea.getInternalDataModel().addRemoteAgentAID(sender);
					// --- Add the AID to the phone book ------------
					//TODO which type to use here?
//					cea.getInternalDataModel().getPhoneBook().addPhoneBookEntry(new PhoneBookEntry(sender));
				
				} else if (action instanceof ProxyAgentRegistrationRequest) {
					// ----------------------------------------------
					// --- Proxy agent ------------------------------
					// ----------------------------------------------
					if (debug==true) System.out.println(cea.getLocalName() + ": Received registration request from proxy agent " + sender.getName());
					// --- Add the AID to the directory -------------
					cea.getInternalDataModel().addProxyAgentAID(sender);
					
				}								
			}
			
		} catch (CodecException | OntologyException ex) {
			System.err.println(cea.getLocalName() + ": Error extracting content from message sent by " + sender.getName());
			ex.printStackTrace();
		}
	}
	
	
}
