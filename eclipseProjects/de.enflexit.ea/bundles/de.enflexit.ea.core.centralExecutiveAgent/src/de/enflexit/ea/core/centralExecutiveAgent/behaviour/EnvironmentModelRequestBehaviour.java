package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import java.io.IOException;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.awb.simulation.environment.EnvironmentModel;
import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class EnvironmentModelRequestBehaviour.
 * 
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class EnvironmentModelRequestBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 4647125813747821130L;

	private ACLMessage requestMessage;
	
	/**
	 * Instantiates a new environment model request behaviour.
	 * @param requestMessage the request message
	 */
	public EnvironmentModelRequestBehaviour(ACLMessage requestMessage) {
		this.requestMessage = requestMessage;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		
		boolean debug = false;
		CentralExecutiveAgent cea = (CentralExecutiveAgent) this.myAgent;
		
		if (debug==true) System.out.println(cea.getLocalName() + ": Received request for environment model from " + this.requestMessage.getSender().getName());

		// --- Get a copy of the environment model -----------------
		EnvironmentModel environmentModel = cea.getInternalDataModel().getEnvironmentModel();
		if (environmentModel==null) {
			// --- Error getting the environment model --------------
			System.err.println(cea.getLocalName() + ": Error getting EnvironmentModel from the simulation service or project!");
			
		} else {
			// --- Produce a reduce environment model copy ----------
			EnvironmentModel envModelCopy = environmentModel.getCopy();
		
			// --- To reduce the message size, remove the component models, except for the requester's component ---- 
			String requesterLocalName = this.requestMessage.getSender().getLocalName();
			NetworkModel networkModel = (NetworkModel) envModelCopy.getDisplayEnvironment();
			Vector<NetworkComponent> netComps = networkModel.getNetworkComponentVectorSorted();
			for (int i = 0; i < netComps.size(); i++) {
				NetworkComponent networkComponent = netComps.get(i);
				if (networkComponent.getId().equals(requesterLocalName)==false) {
					networkComponent.setDataModel(null);
					networkComponent.setDataModelBase64(null);
				}
			}
			
			try {
				// --- Answer the request ---------------------------
				ACLMessage response = this.requestMessage.createReply();
				response.setPerformative(ACLMessage.INFORM);
				response.setContentObject(envModelCopy);
				cea.send(response);
				
				if (debug==true) {
					System.out.println(cea.getLocalName() + ": Sent environment model to " + this.requestMessage.getSender().getName());
				}
				
			} catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}

	}

}
